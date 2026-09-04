package net.minex.nodeforge.client.animation;

import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * Lightweight, mutable interpolator for animating scalar numeric values over time.
 */
public class Tween {

    private final double startValue;
    private final double targetValue;
    private final double durationSeconds;
    private final Easing easing;

    private double elapsedSeconds;
    private boolean finished;
    private boolean cancelled;

    private DoubleConsumer onUpdate;
    private Runnable onComplete;

    public Tween(double startValue, double targetValue, double durationSeconds, Easing easing) {
        this.startValue = startValue;
        this.targetValue = targetValue;
        this.durationSeconds = Math.max(0.0001, durationSeconds);
        this.easing = Objects.requireNonNull(easing, "easing must not be null");
        this.elapsedSeconds = 0.0;
        this.finished = false;
        this.cancelled = false;
    }

    public static Tween of(double start, double target, double duration, Easing easing) {
        return new Tween(start, target, duration, easing);
    }

    public static Tween of(double start, double target, double duration) {
        return new Tween(start, target, duration, Easing.QUAD_OUT);
    }

    /** Attaches an update listener invoked on each step with the interpolated value. */
    public Tween onUpdate(DoubleConsumer onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    /** Attaches a completion callback invoked once when the tween finishes. */
    public Tween onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    /** Advances the tween by {@code deltaSeconds}. */
    public void step(double deltaSeconds) {
        if (finished || cancelled || deltaSeconds <= 0.0) return;

        elapsedSeconds += deltaSeconds;
        double progress = Math.clamp(elapsedSeconds / durationSeconds, 0.0, 1.0);
        double val = currentValue();

        if (onUpdate != null) {
            onUpdate.accept(val);
        }

        if (progress >= 1.0) {
            finished = true;
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /** Returns the current interpolated value according to the configured easing function. */
    public double currentValue() {
        double p = Math.clamp(elapsedSeconds / durationSeconds, 0.0, 1.0);
        double eased = easing.apply(p);
        return startValue + (targetValue - startValue) * eased;
    }

    /** Returns the normalized completion progress in {@code [0.0, 1.0]}. */
    public double progress() {
        return Math.clamp(elapsedSeconds / durationSeconds, 0.0, 1.0);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void reset() {
        this.elapsedSeconds = 0.0;
        this.finished = false;
        this.cancelled = false;
    }

    public double getStartValue() {
        return startValue;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }
}
