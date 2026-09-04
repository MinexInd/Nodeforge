package net.minex.nodeforge.client.render.vfx;

import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VfxManager")
class VfxManagerTest {

    private VfxManager vfx;

    @BeforeEach
    void setUp() {
        vfx = new VfxManager();
        vfx.getConfig().setEnabled(true);
        vfx.getConfig().setReducedMotion(false);
        vfx.getConfig().setMaxParticles(500);
    }

    @Test
    @DisplayName("spawns particles and prunes them upon expiration")
    void particleLifecycle() {
        assertEquals(0, vfx.particleCount());

        vfx.spawnBurst(new Position(100, 100), 20, 0xFFFF0000);
        assertEquals(20, vfx.particleCount());

        // Step physics forward slightly
        vfx.tick(0.1);
        assertTrue(vfx.particleCount() > 0);

        // Step beyond max particle lifetime (~1.0s)
        vfx.tick(1.5);
        assertEquals(0, vfx.particleCount());
    }

    @Test
    @DisplayName("enforces max particle cap")
    void maxParticleCap() {
        vfx.getConfig().setMaxParticles(25);
        vfx.spawnBurst(new Position(0, 0), 100, 0xFF00FF00);
        assertEquals(25, vfx.particleCount());
    }

    @Test
    @DisplayName("manages node activation pulses")
    void nodePulses() {
        NodeId n1 = NodeId.of("node_test");
        vfx.pulseNode(n1, 0xFFFFD700);
        assertEquals(1, vfx.nodePulseCount());

        // Step past pulse duration (0.45s)
        vfx.tick(0.2);
        assertEquals(1, vfx.nodePulseCount());

        vfx.tick(0.35);
        assertEquals(0, vfx.nodePulseCount());
    }

    @Test
    @DisplayName("manages cable traveling impulses")
    void cableImpulses() {
        ConnectionId c1 = ConnectionId.of("conn_test");
        vfx.pulseCable(c1, 0xFF00E5FF);
        assertEquals(1, vfx.cableImpulseCount());

        vfx.tick(0.2);
        assertEquals(1, vfx.cableImpulseCount());

        // Step past duration (0.45s)
        vfx.tick(0.35);
        assertEquals(0, vfx.cableImpulseCount());
    }

    @Test
    @DisplayName("reduced motion mode suppresses particle bursts and pulses")
    void reducedMotionSuppression() {
        vfx.getConfig().setReducedMotion(true);
        assertTrue(vfx.getConfig().isReducedMotion());

        vfx.spawnBurst(new Position(50, 50), 30, 0xFFFFFFFF);
        vfx.pulseNode(NodeId.of("n1"), 0xFF0000FF);
        vfx.pulseCable(ConnectionId.of("c1"), 0xFF00FF00);

        assertEquals(0, vfx.particleCount());
        assertEquals(0, vfx.nodePulseCount());
        assertEquals(0, vfx.cableImpulseCount());
    }

    @Test
    @DisplayName("disabling VFX purges existing effects")
    void disablePurgesEffects() {
        vfx.spawnBurst(new Position(0, 0), 15, 0xFFFFFFFF);
        vfx.pulseNode(NodeId.of("n1"), 0xFFFFFFFF);
        assertEquals(15, vfx.particleCount());
        assertEquals(1, vfx.nodePulseCount());

        vfx.getConfig().setEnabled(false);
        assertFalse(vfx.getConfig().isEnabled());

        vfx.tick(0.016);
        assertEquals(0, vfx.particleCount());
        assertEquals(0, vfx.nodePulseCount());
    }

    @Test
    @DisplayName("cycles VFX modes accurately")
    void cycleModes() {
        VfxConfig cfg = vfx.getConfig();
        assertTrue(cfg.isEnabled());
        assertFalse(cfg.isReducedMotion());
        assertEquals("VFX: FULL", cfg.getModeName());

        // Full -> Reduced
        cfg.cycleMode();
        assertTrue(cfg.isEnabled());
        assertTrue(cfg.isReducedMotion());
        assertEquals("VFX: REDUCED", cfg.getModeName());

        // Reduced -> Disabled
        cfg.cycleMode();
        assertFalse(cfg.isEnabled());
        assertEquals("VFX: OFF", cfg.getModeName());

        // Disabled -> Full
        cfg.cycleMode();
        assertTrue(cfg.isEnabled());
        assertFalse(cfg.isReducedMotion());
        assertEquals("VFX: FULL", cfg.getModeName());
    }

    @Test
    @DisplayName("zero or negative delta does not clear active effects")
    void zeroOrNegativeDeltaPreservesEffects() {
        vfx.spawnBurst(new Position(10, 10), 10, 0xFFFFFFFF);
        vfx.pulseNode(NodeId.of("n1"), 0xFF00FF00);
        assertEquals(10, vfx.particleCount());
        assertEquals(1, vfx.nodePulseCount());

        // Zero delta tick
        vfx.tick(0.0);
        assertEquals(10, vfx.particleCount());
        assertEquals(1, vfx.nodePulseCount());

        // Negative delta tick
        vfx.tick(-0.05);
        assertEquals(10, vfx.particleCount());
        assertEquals(1, vfx.nodePulseCount());
    }

    @Test
    @DisplayName("listener exceptions in VfxConfig do not crash notifications")
    void listenerExceptionsGraceful() {
        VfxConfig cfg = new VfxConfig();
        cfg.setOnChange(c -> { throw new RuntimeException("Test failure"); });
        assertDoesNotThrow(() -> cfg.setEnabled(false));
        assertDoesNotThrow(() -> cfg.setReducedMotion(true));
    }
}
