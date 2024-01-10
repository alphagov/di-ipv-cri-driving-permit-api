package uk.gov.di.ipv.cri.drivingpermit.event.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.event.endpoints.ChangeApiKeyService;
import uk.gov.di.ipv.cri.drivingpermit.event.exceptions.SecretNotFoundException;
import uk.gov.di.ipv.cri.drivingpermit.event.util.SecretsManagerRotationStep;
import uk.gov.di.ipv.cri.drivingpermit.library.config.GlobalConstants;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.config.ParameterStoreService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.SecretsManagerService;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.library.dvla.service.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.Definitions.LAMBDA_API_KEY_CHECK_COMPLETED_ERROR;

public class ApiKeyRenewalHandler implements RequestHandler<SecretsManagerRotationEvent, String> {

    private static final Logger LOGGER = LogManager.getLogger();
    private final SecretsManagerClient secretsManagerClient;
    private final ChangeApiKeyService changeApiKeyService;
    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;
    private final EventProbe eventProbe;
    private final DvlaConfiguration dvlaConfiguration;

    @ExcludeFromGeneratedCoverageReport
    public ApiKeyRenewalHandler() {
        secretsManagerClient =
                SecretsManagerClient.builder()
                        .region(Region.EU_WEST_2)
                        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                        .build();
        ParameterStoreService parameterStoreService =
                new ParameterStoreService(ParamManager.getSsmProvider());
        SecretsManagerService secretsManagerService =
                new SecretsManagerService(secretsManagerClient);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        eventProbe = new EventProbe();
        HttpRetryer httpRetryer = new HttpRetryer(HttpClients.custom().build(), eventProbe, 0);
        dvlaConfiguration = new DvlaConfiguration(parameterStoreService, secretsManagerService);
        RequestConfig defaultRequestConfig = new HttpRequestConfig().getDefaultRequestConfig();

        changeApiKeyService =
                new ChangeApiKeyService(
                        dvlaConfiguration,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
        tokenRequestService =
                new TokenRequestService(
                        dvlaConfiguration,
                        DataStore.getClient(),
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
        driverMatchService =
                new DriverMatchService(
                        dvlaConfiguration,
                        httpRetryer,
                        defaultRequestConfig,
                        objectMapper,
                        eventProbe);
    }

    public ApiKeyRenewalHandler(
            SecretsManagerClient secretsManagerClient,
            ChangeApiKeyService changeApiKeyService,
            TokenRequestService tokenRequestService,
            EventProbe eventProbe,
            DriverMatchService driverMatchService,
            DvlaConfiguration dvlaConfiguration) {
        this.secretsManagerClient = secretsManagerClient;
        this.changeApiKeyService = changeApiKeyService;
        this.tokenRequestService = tokenRequestService;
        this.eventProbe = eventProbe;
        this.driverMatchService = driverMatchService;
        this.dvlaConfiguration = dvlaConfiguration;
    }

    @Logging
    @Metrics(captureColdStart = true)
    public String handleRequest(SecretsManagerRotationEvent input, Context context) {
        LOGGER.info("step {} secretId: {}", input.getStep(), input.getSecretId());

        Optional<SecretsManagerRotationStep> nullableStep =
                SecretsManagerRotationStep.of(input.getStep());
        if (nullableStep.isPresent()) {
            SecretsManagerRotationStep step = nullableStep.get();
            try {
                if (step == SecretsManagerRotationStep.FINISH_SECRET) {
                    // CREATE SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.CREATE_SECRET);

                    String apiKeyFromPreviousRun = null;
                    try {
                        /*Firstly, checking to see if we have an 'AWSPENDING' Secret, if we do, we will advance to test with it
                        If not, we will call DVLA to request a new API Key and advance with it
                         */
                        apiKeyFromPreviousRun = getValue(secretsManagerClient, input.getSecretId());
                        LOGGER.info("Found existing API Key in Secrets Manager, advancing to test");
                    } catch (SecretNotFoundException | ResourceNotFoundException e) {
                        LOGGER.info(
                                "'AWSPENDING' value is null, requesting a new API Key from DVLA");
                        String pendingNewApiKey = callDVLAApi();

                        LOGGER.debug("The new API Key is {}", pendingNewApiKey);
                        PutSecretValueRequest secretRequest =
                                PutSecretValueRequest.builder()
                                        .secretId(input.getSecretId())
                                        .secretString(pendingNewApiKey)
                                        .versionStages("AWSPENDING")
                                        .build();
                        secretsManagerClient.putSecretValue(secretRequest);
                        LOGGER.info("Secret has been stored in AWS successfully");
                    }
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.CREATE_SECRET);
                    // CREATE SECRET END

                    // TEST SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.TEST_SECRET);
                    /*Next step is to verify that the secret has been set in DVLA
                     * To do so, we request a token to ensure the new api key works, but this does not
                     * replace the token in the DB, and does not invalidate the existing one.
                     * Then we perform a driver match service request*/

                    if (dvlaConfiguration.isApiKeyRotationEnabled()) {
                        String testApiKey = getValue(secretsManagerClient, input.getSecretId());
                        driverMatchService.performMatch(
                                drivingPermitForm(),
                                tokenRequestService.requestToken(false),
                                testApiKey);
                    }
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.TEST_SECRET);
                    // TEST SECRET END

                    // FINISH SECRET START
                    LOGGER.info("{} commenced", SecretsManagerRotationStep.FINISH_SECRET);
                    /*Final step updates Secrets Manager to set api-key as AWSCURRENT*/
                    LOGGER.info("Updating Secret in Secrets Manager");
                    String newApiKey = getValue(secretsManagerClient, input.getSecretId());
                    updateSecret(input.getSecretId(), newApiKey);
                    LOGGER.info("{} step is complete", SecretsManagerRotationStep.FINISH_SECRET);
                    // FINISH SECRET END
                }
            } catch (OAuthErrorResponseException e) {
                // api-key Renewal Lambda Completed with an Error
                eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
                LOGGER.error("Failed to call DVLA. OAuth Exception thrown");
                throw new RuntimeException(e);
            } catch (Exception e) {
                LOGGER.error(
                        "Unhandled Exception while handling lambda {} exception {}",
                        context.getFunctionName(),
                        e.getClass());
                LOGGER.error(e.getMessage(), e);

                eventProbe.counterMetric(LAMBDA_API_KEY_CHECK_COMPLETED_ERROR);
                throw new RuntimeException(e);
            }
        }
        return "Success";
    }

    private String callDVLAApi() throws OAuthErrorResponseException, JsonProcessingException {

        String authorization = tokenRequestService.requestToken(false);
        return changeApiKeyService.sendApiKeyChangeRequest(
                dvlaConfiguration.getApiKey(), authorization);
    }

    private void updateSecret(String secretId, String newApiKey) {
        try {
            LOGGER.info("Creating a request for Secrets Manager");
            UpdateSecretRequest secretRequest =
                    UpdateSecretRequest.builder()
                            .secretId(secretId)
                            .secretString(newApiKey)
                            .build();
            LOGGER.info("Secret Manager Request created");

            secretsManagerClient.updateSecret(secretRequest);
            LOGGER.info("Secrets Manager Request Sent");

        } catch (SecretsManagerException e) {
            LOGGER.error(e.awsErrorDetails().errorMessage(), e);
            LOGGER.debug(e);
            throw e;
        }
    }

    public static String getValue(SecretsManagerClient secretsClient, String secretId) {

        try {
            GetSecretValueRequest valueRequest =
                    GetSecretValueRequest.builder()
                            .secretId(secretId)
                            .versionStage("AWSPENDING")
                            .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            if (valueResponse == null || valueResponse.secretString() == null) {
                throw new SecretNotFoundException(
                        "Error as new api key value was null when api key retrieved from Secrets Manager");
            }
            LOGGER.info("The AWSPENDING api-key has been retrieved from Secrets Manager");
            return valueResponse.secretString();

        } catch (SecretsManagerException e) {
            LOGGER.error("Get value method returned Exception {}", e.getClass().getSimpleName());
            LOGGER.debug(e);
            throw e;
        }
    }

    public DrivingPermitForm drivingPermitForm() {
        DrivingPermitForm drivingPermit = new DrivingPermitForm();
        drivingPermit.setLicenceIssuer("DVLA");

        drivingPermit.setForenames(List.of("KENNETH"));
        drivingPermit.setSurname("DECERQUEIRA");

        drivingPermit.setDateOfBirth(LocalDate.of(1965, 7, 8));

        drivingPermit.setIssueDate(LocalDate.of(2018, 4, 19));
        drivingPermit.setExpiryDate(LocalDate.of(2042, 10, 1));
        drivingPermit.setDrivingLicenceNumber("DECER607085KE9LN");
        drivingPermit.setIssueNumber("16");

        Address address = new Address();
        address.setPostalCode("BA2 5AA");
        address.setAddressCountry(GlobalConstants.UK_DRIVING_PERMIT_ADDRESS_COUNTRY);

        drivingPermit.setAddresses(List.of(address));

        return drivingPermit;
    }
}
