package nadiendev.ultimate_browser.client.gui;

import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.browser.BrowserTab;
import de.keksuccino.rinku.RinkuBrowser;
import de.keksuccino.rinku.RinkuBrowserTextureBlitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class PipInteractScreen extends Screen {

    private static final int TITLE_BAR_HEIGHT = 14;
    private static final int RESIZE_HANDLE_SIZE = 12;
    private static final int MIN_W = 160;
    private static final int MIN_H = 90;

    private boolean draggingWindow = false;
    private boolean resizingWindow = false;
    private double dragOffsetX, dragOffsetY;
    private double resizeStartMouseX, resizeStartMouseY;
    private double resizeStartW, resizeStartH;

    public PipInteractScreen() {
        super(Component.translatable("gui.ultimate_browser.pip_interact"));
    }

    @Override
    protected void init() {
        BrowserManager manager = BrowserManager.get();
        manager.clampPipToScreen(this.width, this.height);
    }

    private double pipX() { return BrowserManager.get().getPipX(); }
    private double pipY() { return BrowserManager.get().getPipY(); }
    private double pipW() { return BrowserManager.get().getPipWidth(); }
    private double pipH() { return BrowserManager.get().getPipHeight(); }

    private double browserScale() {
        return this.minecraft.getWindow().getGuiScale();
    }

    private boolean inTitleBar(double mouseX, double mouseY) {
        return mouseX >= pipX() && mouseX <= pipX() + pipW()
                && mouseY >= pipY() && mouseY <= pipY() + TITLE_BAR_HEIGHT;
    }

    private boolean inResizeHandle(double mouseX, double mouseY) {
        double hx = pipX() + pipW() - RESIZE_HANDLE_SIZE;
        double hy = pipY() + pipH() - RESIZE_HANDLE_SIZE;
        return mouseX >= hx && mouseX <= pipX() + pipW()
                && mouseY >= hy && mouseY <= pipY() + pipH();
    }

    private boolean inBrowserArea(double mouseX, double mouseY) {
        return mouseX >= pipX() && mouseX <= pipX() + pipW()
                && mouseY >= pipY() + TITLE_BAR_HEIGHT && mouseY <= pipY() + pipH();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active == null) return;

        int x = (int) pipX();
        int y = (int) pipY();
        int w = (int) pipW();
        int h = (int) pipH();

        // Outer border
        graphics.fill(x - 3, y - 3, x + w + 3, y + h + 3, 0xFFFFFFFF);
        // Title/drag bar
        graphics.fill(x, y, x + w, y + TITLE_BAR_HEIGHT, 0xFF3A3A3A);
        graphics.drawString(this.font, Component.translatable("gui.ultimate_browser.pip_move"), x + 4, y + 3, 0xFFFFFF, false);

        int browserY = y + TITLE_BAR_HEIGHT;
        int browserH = h - TITLE_BAR_HEIGHT;

        RinkuBrowser browser = active.getBrowser();
        double scale = browserScale();
        int texW = Math.max(1, (int) Math.round(w * scale));
        int texH = Math.max(1, (int) Math.round(browserH * scale));
        browser.resize(texW, texH);

        if (browser.isTextureReady()) {
            RinkuBrowserTextureBlitter.blit(graphics, browser, x, browserY, w, browserH);
        }

        // Resize handle (bottom-right corner)
        int hx = x + w - RESIZE_HANDLE_SIZE;
        int hy = y + h - RESIZE_HANDLE_SIZE;
        graphics.fill(hx, hy, x + w, y + h, 0xFFAAAAAA);

        graphics.drawString(this.font, Component.translatable("gui.ultimate_browser.pip_interact_hint"),
                x, y + h + 6, 0xFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inResizeHandle(mouseX, mouseY)) {
            resizingWindow = true;
            resizeStartMouseX = mouseX;
            resizeStartMouseY = mouseY;
            resizeStartW = pipW();
            resizeStartH = pipH();
            return true;
        }
        if (inTitleBar(mouseX, mouseY)) {
            draggingWindow = true;
            dragOffsetX = mouseX - pipX();
            dragOffsetY = mouseY - pipY();
            return true;
        }
        if (inBrowserArea(mouseX, mouseY)) {
            BrowserTab active = BrowserManager.get().getActiveTab();
            if (active != null) {
                double scale = browserScale();
                int bx = (int) Math.round((mouseX - pipX()) * scale);
                int by = (int) Math.round((mouseY - pipY() - TITLE_BAR_HEIGHT) * scale);
                active.getBrowser().sendMousePress(bx, by, button);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingWindow || resizingWindow) {
            draggingWindow = false;
            resizingWindow = false;
            return true;
        }
        if (inBrowserArea(mouseX, mouseY)) {
            BrowserTab active = BrowserManager.get().getActiveTab();
            if (active != null) {
                double scale = browserScale();
                int bx = (int) Math.round((mouseX - pipX()) * scale);
                int by = (int) Math.round((mouseY - pipY() - TITLE_BAR_HEIGHT) * scale);
                active.getBrowser().sendMouseRelease(bx, by, button);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        BrowserManager manager = BrowserManager.get();

        if (draggingWindow) {
            manager.setPipBounds(mouseX - dragOffsetX, mouseY - dragOffsetY, pipW(), pipH());
            manager.clampPipToScreen(this.width, this.height);
            return true;
        }
        if (resizingWindow) {
            double newW = Math.max(MIN_W, resizeStartW + (mouseX - resizeStartMouseX));
            double newH = Math.max(MIN_H, resizeStartH + (mouseY - resizeStartMouseY));
            manager.setPipBounds(pipX(), pipY(), newW, newH);
            manager.clampPipToScreen(this.width, this.height);
            return true;
        }
        if (inBrowserArea(mouseX, mouseY)) {
            BrowserTab active = manager.getActiveTab();
            if (active != null) {
                double scale = browserScale();
                int bx = (int) Math.round((mouseX - pipX()) * scale);
                int by = (int) Math.round((mouseY - pipY() - TITLE_BAR_HEIGHT) * scale);
                active.getBrowser().sendMouseMove(bx, by);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (inBrowserArea(mouseX, mouseY)) {
            BrowserTab active = BrowserManager.get().getActiveTab();
            if (active != null) {
                double scale = browserScale();
                int bx = (int) Math.round((mouseX - pipX()) * scale);
                int by = (int) Math.round((mouseY - pipY() - TITLE_BAR_HEIGHT) * scale);
                active.getBrowser().sendMouseMove(bx, by);
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inBrowserArea(mouseX, mouseY)) {
            BrowserTab active = BrowserManager.get().getActiveTab();
            if (active != null) {
                double scale = browserScale();
                int bx = (int) Math.round((mouseX - pipX()) * scale);
                int by = (int) Math.round((mouseY - pipY() - TITLE_BAR_HEIGHT) * scale);
                active.getBrowser().sendMouseWheel(bx, by, scrollY, 0);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_M) {
            this.onClose();
            return true;
        }
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null) {
            active.getBrowser().sendKeyPress(keyCode, (long) scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null) {
            active.getBrowser().sendKeyRelease(keyCode, (long) scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null) {
            active.getBrowser().sendKeyTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        BrowserManager.get().persist();
        super.onClose();
    }
}