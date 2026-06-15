package io.dapr.quarkus.examples;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Composite proofs for the control-inversion approach: parallel, loop, and conditional
 * orchestrations each run react-agent children directly (no planner bridge). Uses
 * {@link MockChatModel}; completion via parent + child workflows is the proof.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true",
        disabledReason = "daprd 1.18.0 save-before-dispatch race (dapr/dapr#10054) loses workflow events; hangs are frequent on slow CI runners. Re-enable when the fixed runtime ships.")
@QuarkusTest
@ExtendWith(DockerAvailableCondition.class)
class DurableCompositesResourceTest {

    @Test
    void parallelCompositeCompletes() {
        given().queryParam("topic", "dragons").when().get("/durable/parallel")
                .then().statusCode(200).body(notNullValue()).body(not(""));
    }

    @Test
    void loopCompositeCompletes() {
        given().queryParam("topic", "dragons").queryParam("iterations", "2")
                .when().get("/durable/loop")
                .then().statusCode(200).body(notNullValue()).body(not(""));
    }
}
