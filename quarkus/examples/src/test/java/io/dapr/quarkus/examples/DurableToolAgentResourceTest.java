package io.dapr.quarkus.examples;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Tool-path proof for the control-inversion approach: a tool-using agent
 * ({@code research-location-agent}) runs as the {@code react-agent} workflow, the model
 * requests a tool, the {@code agent-tool} activity executes the real {@link ResearchTools}
 * method, and its result flows back into the loop to produce the final answer.
 * <p>
 * {@link ToolCallingMockChatModel} returns one tool call then echoes the tool result, so the
 * response containing "Paris" proves {@code getCapital} executed via the activity.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true",
        disabledReason = "daprd 1.18.0 save-before-dispatch race (dapr/dapr#10054) loses workflow events; hangs are frequent on slow CI runners. Re-enable when the fixed runtime ships.")
@QuarkusTest
@TestProfile(ToolCallingChatModelProfile.class)
@ExtendWith(DockerAvailableCondition.class)
class DurableToolAgentResourceTest {

    @Test
    void durableToolAgentExecutesToolViaActivity() {
        given()
                .queryParam("country", "France")
                .when()
                .get("/durable/research")
                .then()
                .statusCode(200)
                .body(containsString("Paris"));
    }
}
