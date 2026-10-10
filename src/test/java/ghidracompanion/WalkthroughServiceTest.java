package ghidracompanion;

import ghidracompanion.model.WalkthroughStep;
import ghidracompanion.service.WalkthroughService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WalkthroughServiceTest {

    private WalkthroughService walkthroughService;

    @BeforeEach
    public void setUp() {
        walkthroughService = new WalkthroughService();
    }

    @Test
    public void testInitialWorkflowState() {
        List<WalkthroughStep> steps = walkthroughService.getSteps();
        assertNotNull(steps);
        assertEquals(5, steps.size(), "Workflow must consist of exactly 5 steps.");
        assertEquals(0, walkthroughService.getCompletedCount(), "Initial completed step count must be zero.");

        for (WalkthroughStep step : steps) {
            assertFalse(step.isCompleted());
            assertNotNull(step.getTitle());
            assertNotNull(step.getDescription());
        }
    }

    @Test
    public void testCompleteStepsAndProgress() {
        walkthroughService.setStepCompleted(1, true);
        assertEquals(1, walkthroughService.getCompletedCount());

        walkthroughService.setStepCompleted(2, true);
        walkthroughService.setStepCompleted(3, true);
        assertEquals(3, walkthroughService.getCompletedCount());

        // Toggle back
        walkthroughService.setStepCompleted(2, false);
        assertEquals(2, walkthroughService.getCompletedCount());
    }

    @Test
    public void testResetProgress() {
        walkthroughService.setStepCompleted(1, true);
        walkthroughService.setStepCompleted(2, true);
        assertEquals(2, walkthroughService.getCompletedCount());

        walkthroughService.resetProgress();
        assertEquals(0, walkthroughService.getCompletedCount());
    }
}
