package uk.gov.di.ipv.cri.drivingpermit.api.service.dcs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.DocumentCheckResult;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.request.DcsPayload;
import uk.gov.di.ipv.cri.drivingpermit.api.domain.dcs.response.DcsResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.error.ErrorResponse;
import uk.gov.di.ipv.cri.drivingpermit.api.exception.OAuthHttpResponseExceptionWithErrorBody;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.util.MyJwsSigner;
import uk.gov.di.ipv.cri.drivingpermit.library.domain.DrivingPermitForm;
import uk.gov.di.ipv.cri.drivingpermit.library.testdata.DrivingPermitFormTestDataGenerator;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.drivingpermit.api.util.HttpResponseUtils.createHttpResponse;

@ExtendWith(MockitoExtension.class)
class DcsThirdPartyDocumentGatewayTest {

    private static class DCSGatewayConstructorArgs {
        private final ObjectMapper objectMapper;
        private final DcsCryptographyService dcsCryptographyService;
        private final ConfigurationService configurationService;
        private final HttpRetryer httpRetryer;
        private final EventProbe eventProbe;

        private DCSGatewayConstructorArgs(
                ObjectMapper objectMapper,
                DcsCryptographyService dcsCryptographyService,
                ConfigurationService configurationService,
                HttpRetryer httpRetryer,
                EventProbe eventProbe) {

            this.objectMapper = objectMapper;
            this.dcsCryptographyService = dcsCryptographyService;
            this.httpRetryer = httpRetryer;
            this.configurationService = configurationService;
            this.eventProbe = eventProbe;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final int MOCK_HTTP_STATUS_CODE = -1;
    private DcsThirdPartyDocumentGateway dcsThirdPartyDocumentGateway;

    @Mock private HttpClient mockHttpClient;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService configurationService;
    @Mock private HttpRetryer httpRetryer;
    @Mock private DcsCryptographyService dcsCryptographyService;
    @Mock private DvaCryptographyService dvaCryptographyService;

    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {
        lenient()
                .when(configurationService.getDcsEndpointUri())
                .thenReturn("https://test-endpoint.co.uk");
        this.dcsThirdPartyDocumentGateway =
                new DcsThirdPartyDocumentGateway(
                        mockObjectMapper,
                        dcsCryptographyService,
                        configurationService,
                        httpRetryer,
                        mockEventProbe);
    }

    @Test
    void shouldInvokeThirdPartyAPI()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());

        CloseableHttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        DocumentCheckResult actualDocumentCheckResult =
                dcsThirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP300Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(300);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_30X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP400Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(400);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_40X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP500Response()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();
        DocumentCheckResult testDocumentCheckResult = new DocumentCheckResult();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(500);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_50X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void thirdPartyApiReturnsErrorOnUnhandledHTTPResponse()
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(-1);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);

        OAuthHttpResponseExceptionWithErrorBody e =
                assertThrows(
                        OAuthHttpResponseExceptionWithErrorBody.class,
                        () -> {
                            DocumentCheckResult actualFraudCheckResult =
                                    dcsThirdPartyDocumentGateway.performDocumentCheck(
                                            drivingPermitForm);
                        });

        final String EXPECTED_ERROR = ErrorResponse.DCS_ERROR_HTTP_X.getMessage();
        assertEquals(EXPECTED_ERROR, e.getErrorResponse().getMessage());

        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @ParameterizedTest
    @MethodSource("getRetryStatusCodes") // Retry status codes
    void retryThirdPartyApiHTTPResponseForStatusCode(int initialStatusCodeResponse)
            throws IOException, InterruptedException, CertificateException, ParseException,
                    JOSEException, OAuthHttpResponseExceptionWithErrorBody,
                    NoSuchAlgorithmException, InvalidKeySpecException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        DrivingPermitForm drivingPermitForm = DrivingPermitFormTestDataGenerator.generate();

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        when(this.mockObjectMapper.convertValue(any(DrivingPermitForm.class), eq(DcsPayload.class)))
                .thenReturn(new DcsPayload());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.EdDSA), new Payload(""));
        jwsObject.sign(new MyJwsSigner());
        when(this.dcsCryptographyService.preparePayload(any(DcsPayload.class)))
                .thenReturn(jwsObject);

        CloseableHttpResponse httpResponse = createHttpResponse(200);

        when(this.httpRetryer.sendHTTPRequestRetryIfAllowed(httpRequestCaptor.capture()))
                .thenReturn(httpResponse);
        when(this.dcsCryptographyService.unwrapDcsResponse(anyString()))
                .thenReturn(createSuccessDcsResponse());

        DocumentCheckResult actualFraudCheckResult =
                dcsThirdPartyDocumentGateway.performDocumentCheck(drivingPermitForm);

        assertNotNull(actualFraudCheckResult);
        assertEquals(
                TEST_ENDPOINT_URL + "/driving-licence",
                httpRequestCaptor.getValue().getURI().toString());
        assertEquals("POST", httpRequestCaptor.getValue().getMethod());

        assertEquals(
                "application/jose",
                httpRequestCaptor.getValue().getFirstHeader("Content-Type").getValue());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsProvided() {
        Map<String, DCSGatewayConstructorArgs> testCases =
                Map.of(
                        "objectMapper must not be null",
                        new DCSGatewayConstructorArgs(null, null, null, null, null),
                        "crossCoreApiConfig must not be null",
                        new DCSGatewayConstructorArgs(
                                Mockito.mock(ObjectMapper.class),
                                Mockito.mock(DcsCryptographyService.class),
                                null,
                                null,
                                null));

        testCases.forEach(
                (errorMessage, constructorArgs) ->
                        assertThrows(
                                NullPointerException.class,
                                () ->
                                        new DcsThirdPartyDocumentGateway(
                                                constructorArgs.objectMapper,
                                                constructorArgs.dcsCryptographyService,
                                                constructorArgs.configurationService,
                                                constructorArgs.httpRetryer,
                                                constructorArgs.eventProbe),
                                errorMessage));
    }

    private static Stream<Integer> getRetryStatusCodes() {
        Stream<Integer> retryStatusCodes = Stream.of(429);
        Stream<Integer> serverErrorRetryStatusCodes = IntStream.range(500, 599).boxed();
        return Stream.concat(retryStatusCodes, serverErrorRetryStatusCodes);
    }

    private static DcsResponse createSuccessDcsResponse() {
        DcsResponse dcsResponse = new DcsResponse();
        dcsResponse.setCorrelationId("1234");
        dcsResponse.setRequestId("4321");
        dcsResponse.setValid(true);
        return dcsResponse;
    }
}
