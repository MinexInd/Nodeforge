package net.minex.nodeforge.client.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages active {@link Tween} instances, stepping them forward on each frame
 * and automatically pruning completed or cancelled animations.
 */
public class AnimationTimeline {

    private final List<Tween> tweens = new CopyOnWriteArrayList<>();
    private boolean paused = false;

    public AnimationTimeline() {}

    /** Registers a new tween into this timeline. */
    public Tween play(Tween tween) {
        Objects.requireNonNull(tween, "tween must not be null");
        tweens.add(tween);
        return tween;
    }

    /** Steps all active tweens by {@code deltaSeconds}. */
    public void step(double deltaSeconds) {
        if (paused || deltaSeconds <= 0.0 || tweens.isEmpty()) return;

        List<Tween> toRemove = new ArrayList<>();
        for (Tween tween : tweens) {
            tween.step(deltaSeconds);
            if (tween.isFinished() || tween.isCancelled()) {
                toRemove.add(tween);
            }
        }
        if (!toRemove.isEmpty()) {
            tweens.removeAll(toRemove);
        }
    }

    /** Returns the number of currently active tweens. */
    public int activeCount() {
        return tweens.size();
    }

    /** Clears all animations. */
    public void clear() {
        tweens.clear();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
