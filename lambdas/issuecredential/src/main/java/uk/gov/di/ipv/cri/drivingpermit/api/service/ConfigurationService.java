package uk.gov.di.ipv.cri.drivingpermit.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.util.Objects;

public class ConfigurationService {

    private final String documentCheckResultTableName;
    private final String contraindicationMappings;
    private final String parameterPrefix;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");
        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.documentCheckResultTableName =
                paramProvider.get(getParameterName("DocumentCheckResultTableName"));
    }

    public String getDocumentCheckResultTableName() {
        return documentCheckResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }
}
