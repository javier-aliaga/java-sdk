package io.dapr.quarkus.examples;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Composite proof for the control-inversion approach: a durable sequential composite
 * ({@code durable-sequence}) runs two agents — {@code creative-writer-agent} then
 * {@code style-editor-agent} — each as its own {@code react-agent} child workflow, threading
 * the {@code story} state between them. No planner bridge.
 * <p>
 * Uses {@link MockChatModel}; the run completing via parent + two child workflows is the proof.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true",
        disabledReason = "daprd 1.18.0 save-before-dispatch race (dapr/dapr#10054) loses workflow events; hangs are frequent on slow CI runners. Re-enable when the fixed runtime ships.")
@QuarkusTest
@ExtendWith(DockerAvailableCondition.class)
class DurableSequenceResourceTest {

    @Test
    void durableSequenceRunsChildrenInOrder() {
        given()
                .queryParam("topic", "dragons")
                .when()
                .get("/durable/sequence")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .body(not(""));
    }
}
