package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dcs.DcsThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaCryptographyService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.DvaThirdPartyDocumentGateway;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dva.RequestHashValidator;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaEndpointFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.DvlaThirdPartyDocumentGateway;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ThirdPartyAPIServiceFactory {

    private static final int MAX_HTTP_DCS_RETRIES = 2;
    private static final int MAX_HTTP_DVA_RETRIES = 2;
    private static final int MAX_HTTP_DVLA_RETRIES = 1;

    private static final int DCS = 0;
    private static final int DVA = 1;
    private static final int DVLA = 2;

    private final ThirdPartyAPIService[] thirdPartyAPIServices = new ThirdPartyAPIService[3];

    public ThirdPartyAPIServiceFactory(ServiceFactory serviceFactory)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        boolean tlsOnDCS = !configurationService.isDcsPerformanceStub();
        boolean tlsOnDva = !configurationService.isDvaPerformanceStub();
        boolean tlsOnDvla = !configurationService.isDvlaPerformanceStub();

        thirdPartyAPIServices[DCS] = createDcsThirdPartyDocumentGateway(serviceFactory, tlsOnDCS);
        thirdPartyAPIServices[DVA] = createDvaThirdPartyDocumentGateway(serviceFactory, tlsOnDva);
        thirdPartyAPIServices[DVLA] =
                createDvlaThirdPartyDocumentGateway(serviceFactory, tlsOnDvla);
    }

    private ThirdPartyAPIService createDcsThirdPartyDocumentGateway(
            ServiceFactory serviceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        DcsCryptographyService dcsCryptographyService =
                new DcsCryptographyService(configurationService);

        CloseableHttpClient httpClient =
                serviceFactory.generateDcsHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe, MAX_HTTP_DCS_RETRIES);

        return new DcsThirdPartyDocumentGateway(
                objectMapper,
                dcsCryptographyService,
                configurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory, boolean tlsOn)
            throws CertificateException, HttpException, NoSuchAlgorithmException, KeyStoreException,
                    IOException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();
        ConfigurationService configurationService = serviceFactory.getConfigurationService();

        DvaCryptographyService dvaCryptographyService =
                new DvaCryptographyService(configurationService);

        RequestHashValidator requestHashValidator = new RequestHashValidator();

        CloseableHttpClient httpClient =
                serviceFactory.generateDvaHttpClient(configurationService, tlsOn);

        HttpRetryer httpRetryer = new HttpRetryer(httpClient, eventProbe, MAX_HTTP_DVA_RETRIES);

        return new DvaThirdPartyDocumentGateway(
                objectMapper,
                dvaCryptographyService,
                requestHashValidator,
                configurationService,
                httpRetryer,
                eventProbe);
    }

    private ThirdPartyAPIService createDvlaThirdPartyDocumentGateway(
            ServiceFactory serviceFactory, boolean tlsOn) {

        EventProbe eventProbe = serviceFactory.getEventProbe();
        HttpRetryer httpRetryer =
                new HttpRetryer(
                        serviceFactory.generateDvlaHttpClient(), eventProbe, MAX_HTTP_DVLA_RETRIES);
        DvlaEndpointFactory dvlaEndpointFactory =
                new DvlaEndpointFactory(serviceFactory, httpRetryer);

        return new DvlaThirdPartyDocumentGateway(dvlaEndpointFactory);
    }

    public ThirdPartyAPIService getDcsThirdPartyAPIService() {
        return thirdPartyAPIServices[DCS];
    }

    public ThirdPartyAPIService getDvaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVA];
    }

    public ThirdPartyAPIService getDvlaThirdPartyAPIService() {
        return thirdPartyAPIServices[DVLA];
    }
}
