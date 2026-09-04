# Canvas & Camera Mathematics

This document details the affine coordinate transformations, camera projections, and culling algorithms governing NodeForge's visual editor canvas.

---

## 1. Dual Coordinate Systems

NodeForge strictly separates two coordinate spaces:

```text
World Space (Continuous 64-bit IEEE 754)           Screen Space (Integer Pixel Coordinates)
Node Position (x_w, y_w) in double       ───────►  Rendered at (x_s, y_s) in int pixels
```

1. **World Space**:
   A continuous 2D Cartesian plane where nodes, cables, and comment boxes reside. World coordinates use standard 64-bit `double` precision, supporting effectively unbounded canvas extents without precision truncation.
2. **Screen Space**:
   The discrete integer pixel raster representing the physical Minecraft client window.

---

## 2. Forward Projection (World to Screen)

To project a continuous world coordinate $\mathbf{p}_w = (x_w, y_w)$ into screen pixel coordinates $\mathbf{p}_s = (x_s, y_s)$:

$$\mathbf{p}_s = (\mathbf{p}_w - \mathbf{c}) \cdot s + \mathbf{o}$$

Where:
- $\mathbf{c} = (c_x, c_y)$ is the camera pan offset in world coordinates.
- $s \in [0.1, 3.0]$ is the continuous zoom scale factor.
- $\mathbf{o} = (w_{\text{screen}} / 2, \quad h_{\text{screen}} / 2)$ is the screen center origin.

Component-wise:

$$x_s = (x_w - c_x) \cdot s + \frac{w_{\text{screen}}}{2}$$

$$y_s = (y_w - c_y) \cdot s + \frac{h_{\text{screen}}}{2}$$

---

## 3. Inverse Projection (Screen to World)

When the user clicks or hovers the mouse at screen pixel coordinate $\mathbf{p}_s = (x_s, y_s)$, the corresponding world coordinate $\mathbf{p}_w$ is calculated by inverting the projection:

$$\mathbf{p}_w = \frac{\mathbf{p}_s - \mathbf{o}}{s} + \mathbf{c}$$

Component-wise:

$$x_w = \frac{x_s - w_{\text{screen}} / 2}{s} + c_x$$

$$y_w = \frac{y_s - h_{\text{screen}} / 2}{s} + c_y$$

This transformation is implemented in `Viewport.screenToWorld(double x, double y)`.

---

## 4. Zoom-to-Cursor Invariance

A common defect in visual canvas engines is camera drift during zoom interactions: if zooming scales around the canvas center rather than the mouse pointer, the element under the cursor shifts away during scroll.

NodeForge guarantees **zoom-to-cursor focal invariance**. The world position under the cursor before zooming $\mathbf{p}_{w,\text{before}}$ must equal the world position after zooming $\mathbf{p}_{w,\text{after}}$:

$$\mathbf{p}_{w,\text{cursor}} = \frac{\mathbf{p}_s - \mathbf{o}}{s_{\text{old}}} + \mathbf{c}_{\text{old}} = \frac{\mathbf{p}_s - \mathbf{o}}{s_{\text{new}}} + \mathbf{c}_{\text{new}}$$

Solving for the updated camera pan offset $\mathbf{c}_{\text{new}}$:

$$\mathbf{c}_{\text{new}} = \mathbf{c}_{\text{old}} + (\mathbf{p}_s - \mathbf{o}) \left( \frac{1}{s_{\text{old}}} - \frac{1}{s_{\text{new}}} \right)$$

By applying this adjustment on every scroll step, the point directly beneath the user's cursor remains pinned in place regardless of zoom velocity.

---

## 5. Viewport Frustum Culling

To maintain high interactive frame rates across graphs containing thousands of nodes, NodeForge calculates the visible world-space frustum bounding box every frame:

$$\text{Frustum} = \left[ \mathbf{p}_w(0, 0) - \mathbf{m}, \quad \mathbf{p}_w(w_{\text{screen}}, h_{\text{screen}}) + \mathbf{m} \right]$$

Where $\mathbf{m} = (64.0, 64.0)$ is a conservative margin accommodating socket radii and drop shadow geometry.

Before issuing draw calls:
- Nodes whose bounding boxes do not intersect this rectangle are skipped.
- Bézier spline segments whose control points lie entirely outside the frustum are culled.

---

## 6. Next Steps

- Explore the rendering and animation engine in the [Rendering & VFX Guide](rendering.md).
- Learn how to hook into the canvas pipeline in the [Canvas Layers Guide](../extensions/canvas-layers.md).
