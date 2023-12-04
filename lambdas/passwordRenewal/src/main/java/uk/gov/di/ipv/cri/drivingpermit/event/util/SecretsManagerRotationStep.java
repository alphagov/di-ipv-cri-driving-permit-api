package uk.gov.di.ipv.cri.drivingpermit.event.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum SecretsManagerRotationStep {
    CREATE_SECRET("createSecret"),
    SET_SECRET("setSecret"),
    TEST_SECRET("testSecret"),
    FINISH_SECRET("finishSecret");

    private static final Map<String, SecretsManagerRotationStep> ENUM_MAP;

    static {
        Map<String, SecretsManagerRotationStep> map = new HashMap<>();
        for (SecretsManagerRotationStep instance : SecretsManagerRotationStep.values()) {
            map.put(instance.toString(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    private final String step;

    SecretsManagerRotationStep(String step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return this.step;
    }

    public static Optional<SecretsManagerRotationStep> of(String step) {
        return Optional.ofNullable(ENUM_MAP.get(step));
    }
}
