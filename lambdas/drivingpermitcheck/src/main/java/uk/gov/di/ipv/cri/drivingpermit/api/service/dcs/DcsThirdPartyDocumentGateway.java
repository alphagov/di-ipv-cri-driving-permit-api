package uk.gov.di.ipv.cri.drivingpermit.api.service.dcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.request.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.response.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ThirdPartyAPIService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DcsConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.IssuingAuthority;
import uk.gov.di.ipv.cri.drivingpermit.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.IpvCryptoException;
import uk.gov.di.ipv.cri.drivingpermit.library.exceptions.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.drivingpermit.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReply;
import uk.gov.di.ipv.cri.drivingpermit.library.util.HTTPReplyHelper;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.LocalDate;

import static uk.gov.di.ipv.cri.drivingpermit.api.domain.result.APIResultSource.DCS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.drivingpermit.library.metrics.ThirdPartyAPIEndpointMetric.DCS_RESPONSE_TYPE_VALID;

public class DcsThirdPartyDocumentGateway implements ThirdPartyAPIService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SERVICE_NAME = DcsThirdPartyDocumentGateway.class.getSimpleName();

    private static final APIResultSource API_RESULT_SOURCE = DCS;

    private final ObjectMapper objectMapper;
    private final DcsCryptographyService dcsCryptographyService;
    private final ConfigurationService configurationService;
    private final HttpRetryer httpRetryer;
    private final EventProbe eventProbe;

    private final RequestConfig defaultRequestConfig;
    private final HttpRetryStatusConfig httpRetryStatusConfig;

    public DcsThirdPartyDocumentGateway(
            ObjectMapper objectMapper,
            DcsCryptographyService dcsCryptographyService,
            ConfigurationService configurationService,
            HttpRetryer httpRetryer,
            EventProbe eventProbe) {
        this.objectMapper = objectMapper;
        this.dcsCryptographyService = dcsCryptographyService;
        this.configurationService = configurationService;
        this.httpRetryer = httpRetryer;
        this.eventProbe = eventProbe;

        this.defaultRequestConfig = new HttpRequestConfig().getDefaultRequestConfig();
        this.httpRetryStatusConfig = new DcsHttpRetryStatusConfig();
    }

    @Override
    public DocumentCheckResult performDocumentCheck(DrivingPermitForm drivingPermitData)
            throws OAuthErrorResponseException {
        LOGGER.info("Mapping person to third party document check request");

        DcsPayload dcsPayload = objectMapper.convertValue(drivingPermitData, DcsPayload.class);

        IssuingAuthority issuingAuthority =
                IssuingAuthority.valueOf(drivingPermitData.getLicenceIssuer());

        LocalDate drivingPermitExpiryDate = drivingPermitData.getExpiryDate();
        String drivingPermitDocumentNumber = drivingPermitData.getDrivingLicenceNumber();
        LocalDate drivingPermitIssueDate = drivingPermitData.getIssueDate();

        DcsConfiguration dcsConfiguration = configurationService.getDcsConfiguration();

        String dcsEndpointUri = dcsConfiguration.getEndpointUri();
        switch (issuingAuthority) {
            case DVA:
                dcsEndpointUri += "/dva-driving-licence";

                dcsPayload.setExpiryDate(drivingPermitExpiryDate);
                dcsPayload.setDriverNumber(drivingPermitDocumentNumber);

                // Note: DateOfIssue is mapped to issueDate in the front end to simplify
                // api handling of that field
                // Here (for the DVA request) it needs to be mapped back to date of issue
                dcsPayload.setDateOfIssue(drivingPermitIssueDate);
                break;
            case DVLA:
                dcsEndpointUri += "/driving-licence";

                dcsPayload.setIssueNumber(drivingPermitData.getIssueNumber());

                dcsPayload.setExpiryDate(drivingPermitExpiryDate);
                dcsPayload.setLicenceNumber(drivingPermitDocumentNumber);
                dcsPayload.setIssueDate(drivingPermitIssueDate);
                break;
            default:
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_PARSE_DRIVING_PERMIT_FORM_DATA);
        }
        JWSObject preparedDcsPayload = prepareDcsPayload(dcsPayload);

        String requestBody = preparedDcsPayload.serialize();

        URI endpoint = URI.create(dcsEndpointUri);
        HttpPost request = requestBuilder(endpoint, requestBody);

        eventProbe.counterMetric(DCS_REQUEST_CREATED.withEndpointPrefix());

        // Enforce connection timeout values
        request.setConfig(defaultRequestConfig);

        final HTTPReply httpReply;
        LOGGER.info("Submitting document check request to third party...");
        try (CloseableHttpResponse response =
                httpRetryer.sendHTTPRequestRetryIfAllowed(request, httpRetryStatusConfig)) {
            eventProbe.counterMetric(DCS_REQUEST_SEND_OK.withEndpointPrefix());
            // throws OAuthErrorResponseException on error
            httpReply = HTTPReplyHelper.retrieveResponse(response, API_RESULT_SOURCE.getName());

            if (configurationService.isLogDcsResponse()) {
                LOGGER.info("DCS response {}", httpReply.responseBody);
            }
        } catch (IOException e) {
            LOGGER.error("IOException executing http request {}", e.getMessage());
            eventProbe.counterMetric(DCS_REQUEST_SEND_ERROR.withEndpointPrefix());
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.ERROR_CONTACTING_DCS);
        }

        DocumentCheckResult documentCheckResult = thirdPartyAPIResponseHandler(httpReply);

        documentCheckResult.setApiResultSource(API_RESULT_SOURCE);

        return documentCheckResult;
    }

    private JWSObject prepareDcsPayload(DcsPayload dcsPayload) throws OAuthErrorResponseException {
        LOGGER.info("Preparing payload for DCS");
        try {
            return dcsCryptographyService.preparePayload(dcsPayload);
        } catch (CertificateException | JOSEException | JsonProcessingException e) {
            LOGGER.error(("Failed to prepare payload for DCS: " + e.getMessage()));
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_DCS_PAYLOAD);
        }
    }

    private DocumentCheckResult thirdPartyAPIResponseHandler(HTTPReply httpReply)
            throws OAuthErrorResponseException {

        if (httpReply.statusCode == 200) {
            LOGGER.info("Third party response code {}", httpReply.statusCode);

            eventProbe.counterMetric(DCS_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());

            DcsResponse unwrappedDcsResponse;
            try {
                unwrappedDcsResponse =
                        dcsCryptographyService.unwrapDcsResponse(httpReply.responseBody);
            } catch (IpvCryptoException e) {

                // Thrown from unwrapDcsResponse - IpvCryptoException (CRI internal exception) is
                // seen when a signing cert has expired and all message signatures fail verification
                // will also occur if object mapping fails and may contain all response PII
                // TODO break unwrapDcsResponse into stages to allow testing and suppress PII
                LOGGER.error(e.getMessage());

                eventProbe.counterMetric(DCS_RESPONSE_TYPE_INVALID.withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_UNWRAP_DCS_RESPONSE);
            } catch (ParseException | JOSEException e) {

                // Thrown from unwrapDcsResponse -
                // TODO rework unwrapDcsResponse into stages and review these exceptions for PII

                LOGGER.error(e.getMessage());

                eventProbe.counterMetric(DCS_RESPONSE_TYPE_INVALID.withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_UNWRAP_DCS_RESPONSE);
            }

            // isError flag is non-recoverable
            if (unwrappedDcsResponse.isError()) {

                // Logging null as errorMessage if ever null is intended
                String errorMessage = null;
                if (unwrappedDcsResponse.getErrorMessage() != null) {
                    errorMessage = unwrappedDcsResponse.getErrorMessage().toString();
                }

                LOGGER.error("DCS encountered an error: {}", errorMessage);

                eventProbe.counterMetric(
                        DCS_RESPONSE_TYPE_ERROR.withEndpointPrefix()); // A Specific Error from API

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.DCS_RETURNED_AN_ERROR_RESPONSE);
            }

            DocumentCheckResult documentCheckResult = new DocumentCheckResult();
            documentCheckResult.setExecutedSuccessfully(true);
            documentCheckResult.setTransactionId(unwrappedDcsResponse.getRequestId());
            documentCheckResult.setValid(unwrappedDcsResponse.isValid());
            LOGGER.info("Third party response Valid");
            eventProbe.counterMetric(DCS_RESPONSE_TYPE_VALID.withEndpointPrefix());

            return documentCheckResult;
        } else {

            LOGGER.error(
                    "Third party replied with Unexpected HTTP status code {}, response text: {}",
                    httpReply.statusCode,
                    httpReply.responseBody);

            eventProbe.counterMetric(DCS_RESPONSE_TYPE_ERROR.withEndpointPrefix());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_DCS_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }

    private HttpPost requestBuilder(URI endpointUri, String requestBody) {
        HttpPost request = new HttpPost(endpointUri);
        request.addHeader("Content-Type", "application/jose");
        request.setEntity(new StringEntity(requestBody, ContentType.DEFAULT_TEXT));
        return request;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
