package net.minex.nodeforge.client.animation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Easing")
class EasingTest {

    @ParameterizedTest
    @EnumSource(Easing.class)
    @DisplayName("all easing functions evaluate endpoints correctly")
    void endpointsCorrect(Easing easing) {
        assertEquals(0.0, easing.apply(0.0), 1e-6, "apply(0.0) should be 0");
        assertEquals(1.0, easing.apply(1.0), 1e-6, "apply(1.0) should be 1");
    }

    @ParameterizedTest
    @EnumSource(Easing.class)
    @DisplayName("all easing functions clamp out-of-bounds inputs")
    void clampsInputs(Easing easing) {
        assertEquals(0.0, easing.apply(-10.0), 1e-6);
        assertEquals(1.0, easing.apply(10.0), 1e-6);
        assertEquals(0.0, easing.apply(Double.NaN), 1e-6);
        assertEquals(0.0, easing.apply(Double.NEGATIVE_INFINITY), 1e-6);
        assertEquals(0.0, easing.apply(Double.POSITIVE_INFINITY), 1e-6);
    }

    @Test
    @DisplayName("linear easing is strictly identity")
    void linearIsIdentity() {
        assertEquals(0.25, Easing.LINEAR.apply(0.25), 1e-6);
        assertEquals(0.5, Easing.LINEAR.apply(0.5), 1e-6);
        assertEquals(0.75, Easing.LINEAR.apply(0.75), 1e-6);
    }

    @Test
    @DisplayName("quad ease-in produces squared values")
    void quadIn() {
        assertEquals(0.25 * 0.25, Easing.QUAD_IN.apply(0.25), 1e-6);
        assertEquals(0.5 * 0.5, Easing.QUAD_IN.apply(0.5), 1e-6);
    }

    @Test
    @DisplayName("monotonic easing functions never decrease")
    void monotonicCheck() {
        double lastLinear = 0.0;
        double lastQuadIn = 0.0;
        double lastQuadOut = 0.0;

        for (double t = 0.0; t <= 1.0; t += 0.05) {
            double lin = Easing.LINEAR.apply(t);
            double qIn = Easing.QUAD_IN.apply(t);
            double qOut = Easing.QUAD_OUT.apply(t);

            assertTrue(lin >= lastLinear - 1e-9);
            assertTrue(qIn >= lastQuadIn - 1e-9);
            assertTrue(qOut >= lastQuadOut - 1e-9);

            lastLinear = lin;
            lastQuadIn = qIn;
            lastQuadOut = qOut;
        }
    }
}
