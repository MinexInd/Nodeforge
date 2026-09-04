package net.minex.nodeforge.client.animation;

/**
 * Standard easing functions for smooth UI and VFX animations.
 *
 * <p>All functions map an input progress value {@code t} in the range {@code [0.0, 1.0]}
 * to an output eased value. Inputs outside {@code [0.0, 1.0]} are automatically clamped.
 */
public enum Easing {

    LINEAR {
        @Override
        public double apply(double t) {
            return clamp(t);
        }
    },

    QUAD_IN {
        @Override
        public double apply(double t) {
            t = clamp(t);
            return t * t;
        }
    },

    QUAD_OUT {
        @Override
        public double apply(double t) {
            t = clamp(t);
            return t * (2.0 - t);
        }
    },

    QUAD_IN_OUT {
        @Override
        public double apply(double t) {
            t = clamp(t);
            return t < 0.5 ? 2.0 * t * t : -1.0 + (4.0 - 2.0 * t) * t;
        }
    },

    CUBIC_OUT {
        @Override
        public double apply(double t) {
            t = clamp(t) - 1.0;
            return t * t * t + 1.0;
        }
    },

    EXP_OUT {
        @Override
        public double apply(double t) {
            t = clamp(t);
            return (t >= 1.0) ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
        }
    },

    ELASTIC_OUT {
        @Override
        public double apply(double t) {
            t = clamp(t);
            if (t <= 0.0) return 0.0;
            if (t >= 1.0) return 1.0;
            double p = 0.3;
            double s = p / 4.0;
            return Math.pow(2.0, -10.0 * t) * Math.sin((t - s) * (2.0 * Math.PI) / p) + 1.0;
        }
    },

    BOUNCE_OUT {
        @Override
        public double apply(double t) {
            t = clamp(t);
            double n1 = 7.5625;
            double d1 = 2.75;

            if (t < 1.0 / d1) {
                return n1 * t * t;
            } else if (t < 2.0 / d1) {
                t -= 1.5 / d1;
                return n1 * t * t + 0.75;
            } else if (t < 2.5 / d1) {
                t -= 2.25 / d1;
                return n1 * t * t + 0.9375;
            } else {
                t -= 2.625 / d1;
                return n1 * t * t + 0.984375;
            }
        }
    };

    /** Applies the easing transformation to a normalized progress value {@code t} in {@code [0.0, 1.0]}. */
    public abstract double apply(double t);

    private static double clamp(double t) {
        if (!Double.isFinite(t)) return 0.0;
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;
        return t;
    }
}
