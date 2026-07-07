package nadiendev.ultimate_browser.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import nadiendev.ultimate_browser.client.KeyBindings;
import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.browser.BrowserTab;
import com.cinemamod.mcef.MCEFBrowser;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The main, full-window browser interface: address bar, tab strip, nav buttons,
 * and the rendered Chromium surface for the active tab.
 */
public class BrowserScreen extends Screen {

    private static final int TOOLBAR_HEIGHT = 28;
    private static final int TABBAR_HEIGHT = 22;

    private EditBox addressBar;
    private Button backButton;
    private Button forwardButton;
    private Button reloadButton;
    private Button newTabButton;

    public BrowserScreen() {
        super(Component.translatable("gui.ultimate_browser.title"));
    }

    @Override
    protected void init() {
        BrowserManager manager = BrowserManager.get();
        if (manager.getTabs().isEmpty()) {
            manager.restoreFromConfig();
        }
        manager.setFullscreen(false);

        int top = TABBAR_HEIGHT;

        backButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> navigateBack())
                .bounds(4, top + 2, 20, 20).build());
        forwardButton = addRenderableWidget(Button.builder(Component.literal(">"), b -> navigateForward())
                .bounds(26, top + 2, 20, 20).build());
        reloadButton = addRenderableWidget(Button.builder(Component.literal("R"), b -> reload())
                .bounds(48, top + 2, 20, 20).build());

        addressBar = new EditBox(this.font, 72, top + 4, this.width - 72 - 100, 16, Component.literal("URL"));
        addressBar.setMaxLength(1024);
        BrowserTab active = manager.getActiveTab();
        if (active != null) {
            addressBar.setValue(active.getUrl());
        }
        addRenderableWidget(addressBar);

        addRenderableWidget(Button.builder(Component.translatable("gui.ultimate_browser.go"), b -> navigateToBar())
                .bounds(this.width - 96, top + 2, 40, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.ultimate_browser.new_tab"), b -> newTab())
                .bounds(this.width - 52, top + 2, 48, 20).build());

        resizeActiveBrowser();
    }

    private void resizeActiveBrowser() {
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null) {
            int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
            int[] tex = physicalBrowserSize(y);
            active.resize(tex[0], tex[1]);
        }
    }

    /**
     * Converts the logical (GUI-scaled) browser area into the real physical
     * pixel resolution of the Minecraft window, so MCEF renders the page at
     * full sharpness instead of at Minecraft's (often lower) GUI-scale size.
     */
    private int[] physicalBrowserSize(int toolbarY) {
        double scale = this.minecraft.getWindow().getGuiScale();
        int texW = (int) Math.round(this.width * scale);
        int texH = (int) Math.round((this.height - toolbarY) * scale);
        return new int[]{Math.max(1, texW), Math.max(1, texH)};
    }

    /**
     * Converts a logical GUI mouseX/mouseY (relative to the toolbar) into the
     * physical pixel coordinates the browser texture is actually rendered at.
     * Must match the scale used in physicalBrowserSize(), or clicks land on
     * the wrong element (works "by luck" only for very large hit areas).
     */
    private int toBrowserX(double mouseX) {
        double scale = this.minecraft.getWindow().getGuiScale();
        return (int) Math.round(mouseX * scale);
    }

    private int toBrowserY(double mouseY, int toolbarY) {
        double scale = this.minecraft.getWindow().getGuiScale();
        return (int) Math.round((mouseY - toolbarY) * scale);
    }

    private void navigateBack() {
        BrowserTab t = BrowserManager.get().getActiveTab();
        if (t != null) t.goBack();
    }

    private void navigateForward() {
        BrowserTab t = BrowserManager.get().getActiveTab();
        if (t != null) t.goForward();
    }

    private void reload() {
        BrowserTab t = BrowserManager.get().getActiveTab();
        if (t != null) t.reload();
    }

    private void navigateToBar() {
        BrowserTab t = BrowserManager.get().getActiveTab();
        if (t != null && addressBar != null) {
            String value = addressBar.getValue().trim();
            if (!value.isEmpty()) {
                if (!value.startsWith("http://") && !value.startsWith("https://")) {
                    value = "https://" + value;
                }
                t.loadUrl(value);
            }
        }
    }

    private void newTab() {
        BrowserManager.get().openTab("https://www.google.com");
        this.init(this.minecraft, this.width, this.height);
    }

    @Override
    public void tick() {
        super.tick();
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null && addressBar != null && !addressBar.isFocused()) {
            addressBar.setValue(active.getUrl());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTabStrip(graphics, mouseX, mouseY);
        renderActiveBrowser(graphics);
    }

    private void renderTabStrip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<BrowserTab> tabs = BrowserManager.get().getTabs();
        int x = 2;
        for (int i = 0; i < tabs.size(); i++) {
            BrowserTab tab = tabs.get(i);
            int tabWidth = Math.min(160, Math.max(60, this.width / Math.max(1, tabs.size()) - 4));
            boolean active = i == BrowserManager.get().getActiveIndex();
            int color = active ? 0xFF3A3A3A : 0xFF1E1E1E;
            graphics.fill(x, 0, x + tabWidth, TABBAR_HEIGHT, color);
            graphics.drawString(this.font, trimTitle(tab.getTitle(), tabWidth - 20), x + 4, 6, 0xFFFFFF, false);
            // close 'x' hitbox is handled in mouseClicked
            graphics.drawString(this.font, "x", x + tabWidth - 12, 6, 0xFFAAAA, false);
            x += tabWidth + 2;
        }
        int plusX = x;
        graphics.fill(plusX, 0, plusX + 20, TABBAR_HEIGHT, 0xFF1E1E1E);
        graphics.drawCenteredString(this.font, "+", plusX + 10, 6, 0xFFFFFF);
    }

    private String trimTitle(String title, int maxWidth) {
        if (this.font.width(title) <= maxWidth) return title;
        StringBuilder sb = new StringBuilder();
        for (char c : title.toCharArray()) {
            if (this.font.width(sb.toString() + c + "...") > maxWidth) break;
            sb.append(c);
        }
        return sb + "...";
    }

    private void renderActiveBrowser(GuiGraphics graphics) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active == null) return;
        MCEFBrowser browser = active.getBrowser();
        int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
        int w = this.width;
        int h = this.height - y;
        if (!browser.isTextureReady()) return;
        net.minecraft.resources.ResourceLocation texture = browser.getTextureLocation();
        int[] tex = physicalBrowserSize(y);
        RenderSystem.enableBlend();
        // Destination (w,h) is in logical GUI units; source/texture (tex[0],tex[1])
        // is the real physical resolution the browser was rendered at, so this
        // scales down cleanly instead of stretching a low-res texture.
        graphics.blit(texture, 0, y, w, h, 0, 0, tex[0], tex[1], tex[0], tex[1]);
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY < TABBAR_HEIGHT) {
            List<BrowserTab> tabs = BrowserManager.get().getTabs();
            int x = 2;
            for (int i = 0; i < tabs.size(); i++) {
                int tabWidth = Math.min(160, Math.max(60, this.width / Math.max(1, tabs.size()) - 4));
                if (mouseX >= x && mouseX <= x + tabWidth) {
                    if (mouseX >= x + tabWidth - 14) {
                        BrowserManager.get().closeTab(i);
                    } else {
                        BrowserManager.get().switchTo(i);
                    }
                    this.init(this.minecraft, this.width, this.height);
                    return true;
                }
                x += tabWidth + 2;
            }
            int plusX = x;
            if (mouseX >= plusX && mouseX <= plusX + 20) {
                newTab();
                return true;
            }
        }

        BrowserTab active = BrowserManager.get().getActiveTab();
        int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
        if (active != null && mouseY >= y) {
            this.setFocused(null);
            active.getBrowser().sendMousePress(toBrowserX(mouseX), toBrowserY(mouseY, y), button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
        if (active != null && mouseY >= y) {
            active.getBrowser().sendMouseRelease(toBrowserX(mouseX), toBrowserY(mouseY, y), button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
        if (active != null && mouseY >= y) {
            active.getBrowser().sendMouseMove(toBrowserX(mouseX), toBrowserY(mouseY, y));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
        if (active != null && mouseY >= y) {
            active.getBrowser().sendMouseMove(toBrowserX(mouseX), toBrowserY(mouseY, y));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        int y = TABBAR_HEIGHT + TOOLBAR_HEIGHT;
        if (active != null && mouseY >= y) {
            active.getBrowser().sendMouseWheel(toBrowserX(mouseX), toBrowserY(mouseY, y), scrollY, currentModifiers());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /** Builds a GLFW-style modifier bitmask from the currently held keys. */
    private static int currentModifiers() {
        int mods = 0;
        if (hasControlDown()) mods |= org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
        if (hasShiftDown()) mods |= org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
        if (hasAltDown()) mods |= org.lwjgl.glfw.GLFW.GLFW_MOD_ALT;
        return mods;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F12) {
            BrowserManager.get().setFullscreen(!BrowserManager.get().isFullscreen());
            return true;
        }

        if (addressBar != null && addressBar.isFocused()) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
                navigateToBar();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        // Let Escape fall through so the vanilla Screen can still close this
        // menu; everything else while the page has "focus" goes only to the
        // browser, never to super.keyPressed(), otherwise Minecraft treats
        // Space/Enter as "activate the currently focused widget" (e.g. the
        // "+" new tab button) instead of typing into the page.
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null) {
            active.getBrowser().sendKeyPress(keyCode, (long) scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        BrowserTab active = BrowserManager.get().getActiveTab();
        if (active != null && (addressBar == null || !addressBar.isFocused())) {
            active.getBrowser().sendKeyRelease(keyCode, (long) scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (addressBar != null && addressBar.isFocused()) {
            return super.charTyped(codePoint, modifiers);
        }
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