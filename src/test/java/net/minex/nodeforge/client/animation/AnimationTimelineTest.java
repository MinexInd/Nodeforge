package net.minex.nodeforge.client.animation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnimationTimeline")
class AnimationTimelineTest {

    @Test
    @DisplayName("registers and advances active tweens")
    void managesTweens() {
        AnimationTimeline timeline = new AnimationTimeline();
        assertEquals(0, timeline.activeCount());

        Tween t1 = Tween.of(0.0, 10.0, 0.2, Easing.LINEAR);
        Tween t2 = Tween.of(0.0, 20.0, 0.5, Easing.QUAD_OUT);

        timeline.play(t1);
        timeline.play(t2);
        assertEquals(2, timeline.activeCount());

        timeline.step(0.25);
        // t1 finished and pruned, t2 still active
        assertEquals(1, timeline.activeCount());
        assertTrue(t1.isFinished());
        assertFalse(t2.isFinished());

        timeline.step(0.3);
        // t2 finished and pruned
        assertEquals(0, timeline.activeCount());
        assertTrue(t2.isFinished());
    }

    @Test
    @DisplayName("paused timeline suppresses steps")
    void pauseTimeline() {
        AnimationTimeline timeline = new AnimationTimeline();
        Tween t = Tween.of(0.0, 10.0, 1.0, Easing.LINEAR);
        timeline.play(t);

        timeline.setPaused(true);
        assertTrue(timeline.isPaused());

        timeline.step(0.5);
        assertEquals(0.0, t.currentValue(), 1e-6);
        assertFalse(t.isFinished());

        timeline.setPaused(false);
        timeline.step(0.5);
        assertEquals(5.0, t.currentValue(), 1e-6);
    }
}
