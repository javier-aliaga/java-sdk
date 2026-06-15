package io.dapr.quarkus.examples;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Drop-in proof for the control-inversion entry point (uniform): plain, unchanged agent
 * interfaces are injected and called normally, but with {@code dapr.agentic.durable=true} their
 * AiServices-built bean is replaced by a durable-workflow proxy — a leaf {@link CreativeWriter}
 * runs as {@code react-agent}, and a composite {@link StoryCreator} ({@code @SequenceAgent}) runs
 * as {@code durable-sequence} over react-agent children. No code changes to the agents.
 * <p>
 * Uses {@link MockChatModel}; a non-blank result means the workflow ran and completed.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true",
        disabledReason = "daprd 1.18.0 save-before-dispatch race (dapr/dapr#10054) loses workflow events; hangs are frequent on slow CI runners. Re-enable when the fixed runtime ships.")
@QuarkusTest
@TestProfile(DurableModeProfile.class)
@ExtendWith(DockerAvailableCondition.class)
class DurableEntryPointTest {

    @Inject
    CreativeWriter creativeWriter;

    @Inject
    StoryCreator storyCreator;

    @Inject
    StoryRouter storyRouter;

    @Test
    void leafAgentRunsAsReactAgentWorkflow() {
        String story = creativeWriter.generateStory("dragons");
        assertNotNull(story);
        assertFalse(story.isBlank(), "expected the durable react-agent workflow to return a story");
    }

    @Test
    void compositeRunsAsDurableSequenceWorkflow() {
        String story = storyCreator.write("dragons", "comedy");
        assertNotNull(story);
        assertFalse(story.isBlank(), "expected the durable-sequence workflow to return a story");
    }

    @Test
    void conditionalRunsAsDurableConditionalWorkflow() {
        String story = storyRouter.route("dragons");
        assertNotNull(story);
        assertFalse(story.isBlank(), "expected the durable-conditional workflow to return a story");
    }
}
