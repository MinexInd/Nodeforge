package net.minex.nodeforge.client.animation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tween")
class TweenTest {

    @Test
    @DisplayName("interpolates scalar values over time")
    void interpolatesValues() {
        Tween tween = Tween.of(0.0, 100.0, 1.0, Easing.LINEAR);
        assertEquals(0.0, tween.currentValue(), 1e-6);
        assertFalse(tween.isFinished());

        tween.step(0.5);
        assertEquals(50.0, tween.currentValue(), 1e-6);
        assertEquals(0.5, tween.progress(), 1e-6);
        assertFalse(tween.isFinished());

        tween.step(0.5);
        assertEquals(100.0, tween.currentValue(), 1e-6);
        assertEquals(1.0, tween.progress(), 1e-6);
        assertTrue(tween.isFinished());
    }

    @Test
    @DisplayName("triggers update and complete callbacks")
    void callbacksTrigger() {
        AtomicInteger updates = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);

        Tween tween = Tween.of(10.0, 20.0, 0.2, Easing.QUAD_OUT)
                .onUpdate(v -> updates.incrementAndGet())
                .onComplete(() -> completed.set(true));

        tween.step(0.1);
        assertTrue(updates.get() > 0);
        assertFalse(completed.get());

        tween.step(0.15);
        assertTrue(completed.get());
        assertTrue(tween.isFinished());
    }

    @Test
    @DisplayName("cancellation stops animation progression")
    void cancellation() {
        Tween tween = Tween.of(0.0, 10.0, 1.0, Easing.LINEAR);
        tween.step(0.3);
        double valBefore = tween.currentValue();

        tween.cancel();
        assertTrue(tween.isCancelled());

        tween.step(0.5);
        assertEquals(valBefore, tween.currentValue(), 1e-6);
    }

    @Test
    @DisplayName("reset allows tween reuse")
    void reset() {
        Tween tween = Tween.of(0.0, 10.0, 0.5, Easing.LINEAR);
        tween.step(0.5);
        assertTrue(tween.isFinished());

        tween.reset();
        assertFalse(tween.isFinished());
        assertFalse(tween.isCancelled());
        assertEquals(0.0, tween.currentValue(), 1e-6);
    }
}
