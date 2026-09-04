# Rendering & Visual Effects (VFX)

NodeForge features an integrated visual effects and animation engine (`net.minex.nodeforge.client.render.vfx` and `net.minex.nodeforge.client.animation`) designed to enhance user feedback without coupling visual rendering to execution semantics.

---

## 1. Architectural Invariant: Semantics vs. Presentation

> [!IMPORTANT]
> **Strict Separation Invariant**:
> Visual effects, cable impulses, and particle emitters are purely aesthetic presentation systems. They exist exclusively within `src/client/java` and have zero impact on `GraphEvaluator` calculations or data results. Graphs evaluate with identical numerical results whether visual effects are active, reduced, or running on a headless server.

---

## 2. Mathematical Easing Curves (`Easing`)

The `Easing` utility (`net.minex.nodeforge.client.animation.Easing`) provides 12 standard mathematical easing curves for smooth interpolations:

| Easing Curve | In Variant | Out Variant | In-Out Variant |
| :--- | :--- | :--- | :--- |
| **Linear** | `LINEAR` | `LINEAR` | `LINEAR` |
| **Quadratic ($t^2$)** | `QUAD_IN` | `QUAD_OUT` | `QUAD_IN_OUT` |
| **Cubic ($t^3$)** | `CUBIC_IN` | `CUBIC_OUT` | `CUBIC_IN_OUT` |
| **Sine ($\sin$)** | `SINE_IN` | `SINE_OUT` | `SINE_IN_OUT` |
| **Exponential ($2^{10(t-1)}$)** | `EXPO_IN` | `EXPO_OUT` | `EXPO_IN_OUT` |
| **Elastic** | `ELASTIC_IN` | `ELASTIC_OUT` | `ELASTIC_IN_OUT` |
| **Bounce** | `BOUNCE_IN` | `BOUNCE_OUT` | `BOUNCE_IN_OUT` |

All easing functions clamp input parameters $t \notin [0.0, 1.0]$ safely to prevent visual overshoot errors.

```java
import net.minex.nodeforge.client.animation.Easing;

double normalizedTime = 0.5; // Halfway through animation
double smoothedProgress = Easing.CUBIC_OUT.apply(normalizedTime);
```

---

## 3. The VFX Coordinator (`VfxManager`)

The `VfxManager` orchestrates particle emissions, node pulse rings, and traveling cable pulses.

### 3.1 Node Activation Rings
Emit an expanding ripple ring when a node triggers or finishes execution:

```java
import net.minex.nodeforge.client.render.vfx.VfxManager;

// Emit an activation pulse around node at world coordinates (x: 200, y: 150)
VfxManager.getInstance().emitNodePulse(
        200.0, 150.0,
        140.0, 80.0,    // Node width and height
        0xFF00E5FF      // Ripple color (Cyan)
);
```

### 3.2 Traveling Cable Impulses (`CableImpulse`)
When data flows across a wire, spawn an animated energy pulse that travels smoothly along the cubic Bézier trajectory:

```java
import net.minex.nodeforge.client.render.vfx.CableImpulse;

// Spawns a traveling energy dot from source socket to target socket
VfxManager.getInstance().emitCableImpulse(
        connectionId,
        0xFFFFD600,     // Gold particle color
        1.2             // Speed factor (seconds to traverse)
);
```

### 3.3 Custom Canvas Particles (`CanvasParticle`)
Spawn custom particles with velocity, drag, and alpha fading:

```java
import net.minex.nodeforge.client.render.vfx.CanvasParticle;

CanvasParticle spark = new CanvasParticle(
        x, y,           // Spawn origin
        vx, vy,         // Velocity vector
        0xFF3366FF,     // Initial color
        0.92,           // Velocity damping (friction)
        40              // Lifetime in ticks
);

VfxManager.getInstance().addParticle(spark);
```

---

## 4. Accessibility & Reduced Motion

NodeForge includes accessibility controls for motion-sensitive users:

- **Reduced Motion Toggle**: Pressing `V` in the `NodeEditorScreen` disables rapid camera zooms, disables particle emitters, and switches cable impulses to static wire highlights.
- **Programmatic Control**:
  ```java
  VfxManager.getInstance().getConfig().setReducedMotion(true);
  ```

---

## 5. Next Steps

- Learn how to create custom canvas rendering passes in the [Canvas Layers Guide](../extensions/canvas-layers.md).
- Learn about the common extension model in the [Common Plugin API Guide](../extensions/plugin-api.md).
