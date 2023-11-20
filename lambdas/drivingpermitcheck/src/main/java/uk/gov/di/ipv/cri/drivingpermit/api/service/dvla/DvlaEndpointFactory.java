package uk.gov.di.ipv.cri.drivingpermit.api.service.dvla;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.http.client.config.RequestConfig;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.drivingpermit.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.drivingpermit.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.ConfigurationService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.configuration.DvlaConfiguration;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.DriverMatchService;
import uk.gov.di.ipv.cri.drivingpermit.api.service.dvla.endpoints.TokenRequestService;
import uk.gov.di.ipv.cri.drivingpermit.library.config.HttpRequestConfig;

@Getter
public class DvlaEndpointFactory {

    private final TokenRequestService tokenRequestService;
    private final DriverMatchService driverMatchService;

    public DvlaEndpointFactory(ServiceFactory serviceFactory, HttpRetryer httpRetryer) {

        ConfigurationService configurationService = serviceFactory.getConfigurationService();
        DvlaConfiguration dvlaConfiguration = configurationService.getDvlaConfiguration();
        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();

        // Same on all endpoints
        RequestConfig defaultRequestConfig =
                new HttpRequestConfig().getDVLAEndpointsRequestConfig();

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
}
