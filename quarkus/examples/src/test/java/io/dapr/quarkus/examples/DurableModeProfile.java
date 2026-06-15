package io.dapr.quarkus.examples;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Test profile that flips the extension to the control-inversion engine
 * ({@code dapr.agentic.durable=true}): every {@code @Agent}/composite interface is served by a
 * durable-workflow-backed proxy instead of the AiServices-built bean.
 */
public class DurableModeProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("dapr.agentic.durable", "true");
    }
}
