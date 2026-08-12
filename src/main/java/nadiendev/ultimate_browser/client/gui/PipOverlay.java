package nadiendev.ultimate_browser.client.gui;

import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.browser.BrowserTab;
import de.keksuccino.rinku.RinkuBrowser;
import de.keksuccino.rinku.RinkuBrowserTextureBlitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws the small Picture-in-Picture browser window over the HUD when PiP mode
 * is enabled and the full browser screen is not currently open.
 *
 * RinkuBrowser has no draw(PoseStack, ...) convenience method; Rinku ships
 * RinkuBrowserTextureBlitter instead, which blits the browser's live texture
 * with the premultiplied-alpha blend func Chromium's OSR pixels require.
 */
public final class PipOverlay {

    private PipOverlay() {}

    public static final int BORDER = 3;
    public static final int LABEL_HEIGHT = 12;
    private static final long HINT_DURATION_MS = 6000;

    private static boolean hintShown = false;
    private static long hintShownAtMillis = 0;

    public static void render(GuiGraphics graphics, Minecraft mc) {
        BrowserManager manager = BrowserManager.get();

        // Don't draw the PiP if it's turned off, or while the fullscreen
        // browser screen is open (that one already shows the page), or while
        // the interactive PiP screen (opened with "M") is handling it instead.
        if (!manager.isPipEnabled() || manager.isFullscreen()) return;
        if (mc.screen instanceof PipInteractScreen) return;

        BrowserTab active = manager.getActiveTab();
        if (active == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // Keep the PiP inside the window and capped to a sane size, no
        // matter what was loaded from config or how big/small the window is.
        manager.clampPipToScreen(screenW, screenH);

        int x = (int) manager.getPipX();
        int y = (int) manager.getPipY();
        int w = (int) manager.getPipWidth();
        int h = (int) manager.getPipHeight();

        // Border / background
        graphics.fill(x - BORDER, y - BORDER, x + w + BORDER, y + h + BORDER, 0xFF000000);

        RinkuBrowser browser = active.getBrowser();
        browser.resize(w, h);

        if (browser.isTextureReady()) {
            RinkuBrowserTextureBlitter.blit(graphics, browser, x, y, w, h);
        }

        // Hint text below, shown only once (for a few seconds) the first
        // time the PiP appears, instead of forever on every frame.
        if (!hintShown) {
            hintShown = true;
            hintShownAtMillis = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - hintShownAtMillis < HINT_DURATION_MS) {
            graphics.drawString(mc.font, net.minecraft.network.chat.Component.translatable("gui.ultimate_browser.pip_hint"),
                    x, y + h + BORDER + 4, 0xFFFFFF, true);
        }
    }
}