package uk.gov.di.ipv.cri.drivingpermit.library.config;

import org.apache.http.client.config.RequestConfig;

/** A shared RequestConfig applied to each http request to DVLA ThirdPartyAPI endpoints. */
public class HttpRequestConfig {

    // HTTP client connection stage timeouts Totals are additive - values in milliseconds.

    // DCS - 1x endpoint
    // Connection pool Req
    private static final int DCS_HTTP_CONN_POOL_REQ_TIMEOUT_MS = 5000;
    // Initial connection attempt
    private static final int DCS_HTTP_INITIAL_CONNECTION_TIMEOUT_MS = 5000;
    // Reading a packet
    private static final int DCS_HTTP_SOCKET_READ_TIMEOUT_MS = 10000;

    // DVLA - Set on 2x endpoints (token only hit as needed)
    // Connection pool Req
    private static final int DVLA_HTTP_CONN_POOL_REQ_TIMEOUT_MS = 500;
    // Initial connection attempt
    private static final int DVLA_HTTP_INITIAL_CONNECTION_TIMEOUT_MS = 500;
    // Reading a packet (Two endpoints and 1 retry each < 30s)
    private static final int DVLA_HTTP_SOCKET_READ_TIMEOUT_MS = 7250;

    /**
     * Prevents waiting until the lambda timeout is reached when there are connection issues.
     *
     * @return RequestConfig
     */
    public RequestConfig getDCSDefaultRequestConfig() {
        // RequestConfig needs to be set on each request to the endpoint to apply the timeouts
        // Only socket read maters if the HTTP client is able to re-use a connection
        return RequestConfig.custom()
                .setConnectionRequestTimeout(DCS_HTTP_CONN_POOL_REQ_TIMEOUT_MS)
                .setConnectTimeout(DCS_HTTP_INITIAL_CONNECTION_TIMEOUT_MS)
                .setSocketTimeout(DCS_HTTP_SOCKET_READ_TIMEOUT_MS)
                .build();
    }

    /**
     * Prevents waiting until the lambda timeout is reached when there are connection issues.
     *
     * @return RequestConfig
     */
    public RequestConfig getDVLAEndpointsRequestConfig() {
        // RequestConfig is set per request for DVLA to apply the timeouts
        // Only socket read maters if the HTTP client is able to re-use a connection
        return RequestConfig.custom()
                .setConnectionRequestTimeout(DVLA_HTTP_CONN_POOL_REQ_TIMEOUT_MS)
                .setConnectTimeout(DVLA_HTTP_INITIAL_CONNECTION_TIMEOUT_MS)
                .setSocketTimeout(DVLA_HTTP_SOCKET_READ_TIMEOUT_MS)
                .build();
    }
}
