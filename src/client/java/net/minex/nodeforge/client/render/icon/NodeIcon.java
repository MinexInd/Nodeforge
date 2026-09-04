package net.minex.nodeforge.client.render.icon;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Generic icon abstraction for rendering visual badges and icons on node headers and palette entries.
 */
public interface NodeIcon {

    /**
     * Renders the icon inside the given bounding box.
     *
     * @param context      drawing context
     * @param textRenderer text font renderer
     * @param x            top-left X coordinate
     * @param y            top-left Y coordinate
     * @param size         width and height in pixels
     */
    void render(DrawContext context, TextRenderer textRenderer, int x, int y, int size);

    // ========== Built-in Icon Implementations ==========

    /**
     * An icon rendered as a colored shape badge.
     */
    record ColorSwatch(int argbColor) implements NodeIcon {
        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int size) {
            int pad = 1;
            context.fill(x + pad, y + pad, x + size - pad, y + size - pad, argbColor);
            context.drawStrokedRectangle(x + pad, y + pad, size - pad * 2, size - pad * 2, 0xFFFFFFFF);
        }
    }

    /**
     * An icon rendered as a text character or unicode glyph.
     */
    record Text(String glyph, int argbColor) implements NodeIcon {
        public Text {
            Objects.requireNonNull(glyph, "glyph must not be null");
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int size) {
            int textWidth = textRenderer.getWidth(glyph);
            int textX = x + (size - textWidth) / 2;
            int textY = y + (size - textRenderer.fontHeight) / 2 + 1;
            context.drawText(textRenderer, glyph, textX, textY, argbColor, false);
        }
    }

    /**
     * An icon rendered from a 2D Minecraft GUI texture resource {@link Identifier}.
     */
    record Texture(Identifier textureId) implements NodeIcon {
        public Texture {
            Objects.requireNonNull(textureId, "textureId must not be null");
        }

        public static Texture of(Identifier id) {
            return new Texture(id);
        }

        @Override
        public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int size) {
            context.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, textureId, x, y, size, size);
        }
    }
}
