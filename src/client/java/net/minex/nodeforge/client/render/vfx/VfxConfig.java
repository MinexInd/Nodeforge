package net.minex.nodeforge.client.render.vfx;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Configuration and accessibility options for canvas visual effects and animations.
 */
public class VfxConfig {

    private boolean enabled = true;
    private boolean reducedMotion = false;
    private boolean ambientFlowEnabled = true;
    private int maxParticles = 500;

    private Consumer<VfxConfig> onChange;

    public VfxConfig() {}

    public void setOnChange(Consumer<VfxConfig> onChange) {
        this.onChange = onChange;
    }

    private void notifyChanged() {
        if (onChange != null) {
            try {
                onChange.accept(this);
            } catch (Exception ignored) {}
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyChanged();
    }

    public boolean toggleEnabled() {
        this.enabled = !this.enabled;
        notifyChanged();
        return this.enabled;
    }

    public boolean isReducedMotion() {
        return reducedMotion;
    }

    public void setReducedMotion(boolean reducedMotion) {
        this.reducedMotion = reducedMotion;
        notifyChanged();
    }

    public boolean toggleReducedMotion() {
        this.reducedMotion = !this.reducedMotion;
        notifyChanged();
        return this.reducedMotion;
    }

    public boolean isAmbientFlowEnabled() {
        return ambientFlowEnabled && enabled && !reducedMotion;
    }

    public void setAmbientFlowEnabled(boolean ambientFlowEnabled) {
        this.ambientFlowEnabled = ambientFlowEnabled;
        notifyChanged();
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = Math.max(10, maxParticles);
        notifyChanged();
    }

    /**
     * Cycles through VFX modes: Full Effects -> Reduced Motion -> Disabled.
     */
    public void cycleMode() {
        if (enabled && !reducedMotion) {
            // Full -> Reduced Motion
            this.reducedMotion = true;
        } else if (enabled && reducedMotion) {
            // Reduced Motion -> Disabled
            this.enabled = false;
            this.reducedMotion = false;
        } else {
            // Disabled -> Full
            this.enabled = true;
            this.reducedMotion = false;
        }
        notifyChanged();
    }

    /** Returns human-readable status name of active mode. */
    public String getModeName() {
        if (!enabled) return "VFX: OFF";
        if (reducedMotion) return "VFX: REDUCED";
        return "VFX: FULL";
    }
}
