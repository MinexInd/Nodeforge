package net.minex.nodeforge.client.render.vfx;

import net.minecraft.client.gui.DrawContext;
import net.minex.nodeforge.api.graph.Connection;
import net.minex.nodeforge.api.graph.Node;
import net.minex.nodeforge.api.graph.Port;
import net.minex.nodeforge.api.graph.Position;
import net.minex.nodeforge.client.editor.state.EditorState;
import net.minex.nodeforge.client.render.BezierCurve;
import net.minex.nodeforge.client.render.PortLayout;
import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.core.graph.Graph;
import net.minex.nodeforge.core.id.ConnectionId;
import net.minex.nodeforge.core.id.NodeId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central manager and coordinator for editor canvas animations, particle simulations,
 * node trigger pulses, and cable energy flow indicators.
 */
public class VfxManager {

    private static final VfxManager INSTANCE = new VfxManager();

    private final VfxConfig config = new VfxConfig();
    private final List<CanvasParticle> particles = new CopyOnWriteArrayList<>();
    private final List<NodeActivationPulse> nodePulses = new CopyOnWriteArrayList<>();
    private final List<CableImpulse> cableImpulses = new CopyOnWriteArrayList<>();

    private final Random random = new Random();
    private double ambientFlowPhase = 0.0;

    public VfxManager() {}

    public static VfxManager getInstance() {
        return INSTANCE;
    }

    public VfxConfig getConfig() {
        return config;
    }

    /** Triggers an expanding activation ripple pulse surrounding a node. */
    public void pulseNode(NodeId nodeId, int color) {
        if (!config.isEnabled() || config.isReducedMotion() || nodeId == null) return;
        nodePulses.add(NodeActivationPulse.of(nodeId, color));
    }

    /** Triggers an energetic pulse packet traveling along a connection. */
    public void pulseCable(ConnectionId connectionId, int color) {
        if (!config.isEnabled() || config.isReducedMotion() || connectionId == null) return;
        cableImpulses.add(CableImpulse.of(connectionId, color));
    }

    /** Spawns an explosion burst of particle sparks at a world coordinate. */
    public void spawnBurst(Position pos, int count, int color) {
        if (!config.isEnabled() || config.isReducedMotion() || pos == null || count <= 0) return;

        int toSpawn = Math.min(count, Math.max(0, config.getMaxParticles() - particles.size()));
        for (int i = 0; i < toSpawn; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = 20.0 + random.nextDouble() * 60.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double lifetime = 0.3 + random.nextDouble() * 0.4;
            double size = 1.5 + random.nextDouble() * 1.5;

            particles.add(CanvasParticle.burst(pos.x(), pos.y(), vx, vy, color, size, lifetime));
        }
    }

    /** Spawns a directional connection completion spark effect. */
    public void spawnConnectionSpark(Position pos, int color) {
        spawnBurst(pos, 12, color);
    }

    /** Steps all active visual effects, particles, and pulse timelines. */
    public void tick(double deltaSeconds) {
        if (!config.isEnabled()) {
            clear();
            return;
        }
        if (deltaSeconds <= 0.0) {
            return;
        }

        // 1. Advance ambient cable flow phase
        if (config.isAmbientFlowEnabled()) {
            ambientFlowPhase = (ambientFlowPhase + deltaSeconds * 0.75) % 1.0;
        }

        // 2. Step particles
        if (!particles.isEmpty()) {
            List<CanvasParticle> deadParticles = new ArrayList<>();
            for (CanvasParticle p : particles) {
                p.step(deltaSeconds);
                if (!p.isAlive()) {
                    deadParticles.add(p);
                }
            }
            if (!deadParticles.isEmpty()) {
                particles.removeAll(deadParticles);
            }
        }

        // 3. Step node pulses
        if (!nodePulses.isEmpty()) {
            List<NodeActivationPulse> deadPulses = new ArrayList<>();
            for (NodeActivationPulse pulse : nodePulses) {
                pulse.step(deltaSeconds);
                if (!pulse.isAlive()) {
                    deadPulses.add(pulse);
                }
            }
            if (!deadPulses.isEmpty()) {
                nodePulses.removeAll(deadPulses);
            }
        }

        // 4. Step cable impulses
        if (!cableImpulses.isEmpty()) {
            List<CableImpulse> deadImpulses = new ArrayList<>();
            for (CableImpulse impulse : cableImpulses) {
                impulse.step(deltaSeconds);
                if (!impulse.isAlive()) {
                    deadImpulses.add(impulse);
                }
            }
            if (!deadImpulses.isEmpty()) {
                cableImpulses.removeAll(deadImpulses);
            }
        }
    }

    /**
     * Renders all active VFX in world coordinates within the current canvas transform.
     */
    public void render(DrawContext context, EditorState state, NodeTheme theme) {
        if (!config.isEnabled() || context == null || state == null) return;

        Graph graph = state.graph();

        // 1. Render node activation pulses
        if (!config.isReducedMotion()) {
            for (NodeActivationPulse pulse : nodePulses) {
                Node node = graph.getNode(pulse.getNodeId());
                if (node != null) {
                    pulse.render(context, node);
                }
            }
        }

        // 2. Render cable impulses and ambient energy dots
        boolean renderAmbient = config.isAmbientFlowEnabled();
        for (Connection conn : graph.getConnections()) {
            Node srcNode = graph.getNode(conn.fromNode());
            Node dstNode = graph.getNode(conn.toNode());
            if (srcNode == null || dstNode == null) continue;

            Port srcPort = srcNode.getPort(conn.fromPort());
            Port dstPort = dstNode.getPort(conn.toPort());
            if (srcPort == null || dstPort == null) continue;

            Position start = PortLayout.getPortPosition(srcNode, srcPort);
            Position end = PortLayout.getPortPosition(dstNode, dstPort);
            BezierCurve curve = BezierCurve.fromEndpoints(start, srcPort.direction(), end, dstPort.direction());

            // 2a. Ambient flow dot
            if (renderAmbient) {
                Position flowDot = curve.evaluate(ambientFlowPhase);
                int fx = (int) Math.round(flowDot.x());
                int fy = (int) Math.round(flowDot.y());
                int dotColor = (theme != null) ? (theme.cableExecutionColor() & 0x00FFFFFF) | 0xAA000000 : 0xAAFFFFFF;
                context.fill(fx - 1, fy - 1, fx + 2, fy + 2, dotColor);
            }

            // 2b. Triggered impulses
            if (!config.isReducedMotion()) {
                for (CableImpulse impulse : cableImpulses) {
                    if (Objects.equals(impulse.getConnectionId(), conn.id())) {
                        impulse.render(context, curve);
                    }
                }
            }
        }

        // 3. Render canvas particles
        if (!config.isReducedMotion()) {
            for (CanvasParticle particle : particles) {
                particle.render(context);
            }
        }
    }

    /** Clears all active particles, pulses, and impulses. */
    public void clear() {
        particles.clear();
        nodePulses.clear();
        cableImpulses.clear();
    }

    public int particleCount() {
        return particles.size();
    }

    public int nodePulseCount() {
        return nodePulses.size();
    }

    public int cableImpulseCount() {
        return cableImpulses.size();
    }
}
