# Visual Themes & Styling

NodeForge features an extensible visual token system (`NodeTheme`) managed by the `ThemeRegistry`. The editor supports dynamic runtime theme switching with instant canvas invalidation.

---

## 1. Built-in Theme Presets

NodeForge provides six pre-configured themes in `net.minex.nodeforge.client.render.theme.NodeTheme`:

| Theme Preset | Description | Dominant Colors |
| :--- | :--- | :--- |
| **`DARK`** | Default modern IDE theme. Neutral charcoal background with blue accents. | `#14181F` / `#1E2430` / `#00E5FF` |
| **`LIGHT`** | Clean light mode with high-contrast borders and dark typography. | `#F4F6F9` / `#FFFFFF` / `#1A202C` |
| **`MIDNIGHT`** | Deep navy-black low-light theme minimizing eye strain. | `#090C10` / `#0D1117` / `#58A6FF` |
| **`CYBERPUNK`** | High-energy neon aesthetic with vibrant magenta and cyan highlights. | `#0F051D` / `#1E0C3A` / `#FF007F` |
| **`HIGH_CONTRAST`** | Accessibility-focused theme with pure black/white edges and bold outlines. | `#000000` / `#111111` / `#FFFF00` |
| **`MINECRAFT_DARK`** | Palette echoing classic Minecraft stone and bedrock UI textures. | `#181818` / `#282828` / `#55FF55` |

Pressing `T` in the `NodeEditorScreen` cycles through these themes interactively.

---

## 2. Visual Token Architecture

A `NodeTheme` encapsulates color tokens and dimensional metrics across eight visual subsystems:

```text
NodeTheme Visual Tokens
├── Canvas Background:  Grid dots, grid lines, canvas clear color
├── Node Cards:         Card body, header fill, borders, rounded corners
├── Selection:          Hover highlight, selection outline halo, marquee fill
├── Connections:        Default cable, active wire drag, impulse particles
├── Sockets:            Execution socket, data socket, shape rules
├── Comment Boxes:      Frame border, header fill, translucent body
├── Menus & Palette:    Popup background, item hover, separator lines
└── Diagnostics HUD:    Background panel, telemetry text colors
```

---

## 3. Defining a Custom Theme

To create a bespoke theme, instantiate or extend `NodeTheme`:

```java
package com.example.mymod.client.theme;

import net.minex.nodeforge.client.render.theme.NodeTheme;
import net.minex.nodeforge.client.render.theme.ThemeId;

public class SolarizedTheme {

    public static final ThemeId SOLARIZED_ID = ThemeId.of("mymod:solarized");

    public static NodeTheme create() {
        return NodeTheme.builder()
                .canvasBackground(0xFF002B36)       // Base03 dark background
                .gridDotColor(0xFF073642)           // Base02 subtle grid dots
                .nodeBackground(0xFF073642)         // Base02 card body
                .nodeHeaderBackground(0xFF586E75)   // Base01 header
                .nodeBorder(0xFF657B83)             // Base00 border outline
                .nodeSelectedBorder(0xFF268BD2)     // Blue selection outline
                .cableColor(0xFF93A1A1)             // Base1 cable lines
                .textColor(0xFFFDF6E3)              // Base3 high-contrast text
                .build();
    }
}
```

---

## 4. Registering Themes via `NodeForgeClientPlugin`

Register your theme inside your client plugin:

```java
package com.example.mymod.client;

import com.example.mymod.client.theme.SolarizedTheme;
import net.minex.nodeforge.client.plugin.NodeForgeClientPlugin;
import net.minex.nodeforge.client.render.theme.ThemeRegistry;

public class MyModClientPlugin implements NodeForgeClientPlugin {

    @Override
    public void registerThemes(ThemeRegistry registry) {
        registry.register(SolarizedTheme.SOLARIZED_ID, SolarizedTheme.create());
    }
}
```

Once registered, your theme appears in the context menu's Theme selector and can be activated programmatically:

```java
editorScreen.setTheme(ThemeRegistry.getInstance().get(SolarizedTheme.SOLARIZED_ID));
```

---

## 5. Next Steps

- Explore canvas coordinate transformations in the [Canvas & Camera Mathematics Guide](canvas.md).
- Learn about particles and cable animations in the [Rendering & VFX Guide](rendering.md).
