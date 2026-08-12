package nadiendev.ultimate_browser.client.browser;

import de.keksuccino.rinku.Rinku;
import nadiendev.ultimate_browser.UltimateBrowserMod;
import nadiendev.ultimate_browser.config.BrowserConfig;

import java.util.ArrayList;
import java.util.List;


public final class BrowserManager {

    private static final BrowserManager INSTANCE = new BrowserManager();

    public static BrowserManager get() {
        return INSTANCE;
    }

    private final List<BrowserTab> tabs = new ArrayList<>();
    private int activeIndex = -1;

    private boolean pipEnabled = false;
    private boolean fullscreen = false;


    private double pipX = 20, pipY = 20, pipWidth = 480, pipHeight = 270;
    private boolean pipPositioned = false;

    private BrowserManager() {}

 
    public boolean isBrowserAvailable() {
        return Rinku.isInitialized();
    }

    public void restoreFromConfig() {
        BrowserConfig.State state = BrowserConfig.get();
        pipX = state.pip.x;
        pipY = state.pip.y;
        pipWidth = state.pip.width;
        pipHeight = state.pip.height;
        pipEnabled = state.pip.enabled;

  
        if (!isBrowserAvailable()) return;

        if (state.tabs.isEmpty()) {
            openTab("https://www.google.com");
        } else {
            for (BrowserConfig.TabState t : state.tabs) {
                int idx = openTab(t.url);
                if (t.active && idx >= 0) activeIndex = idx;
            }
        }
    }

    public void persist() {
        BrowserConfig.State state = BrowserConfig.get();
        state.tabs.clear();
        for (int i = 0; i < tabs.size(); i++) {
            state.tabs.add(new BrowserConfig.TabState(tabs.get(i).getUrl(), i == activeIndex));
        }
        state.pip.x = pipX;
        state.pip.y = pipY;
        state.pip.width = pipWidth;
        state.pip.height = pipHeight;
        state.pip.enabled = pipEnabled;
        BrowserConfig.save();
    }

    /** @return the new tab's index, or -1 if Rinku isn't ready to serve browsers. */
    public int openTab(String url) {
        if (!isBrowserAvailable()) {
            UltimateBrowserMod.LOGGER.warn("Rinku is not initialized yet; cannot open browser tab for {}", url);
            return -1;
        }

        BrowserTab tab = new BrowserTab(url, (int) pipWidth, (int) pipHeight, false);
        tabs.add(tab);
        activeIndex = tabs.size() - 1;
        return activeIndex;
    }

    public void closeTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        tabs.get(index).close();
        tabs.remove(index);
        if (tabs.isEmpty()) {
            openTab("https://www.google.com");
        } else if (activeIndex >= tabs.size()) {
            activeIndex = tabs.size() - 1;
        }
    }

    public void switchTo(int index) {
        if (index >= 0 && index < tabs.size()) {
            activeIndex = index;
        }
    }

    public List<BrowserTab> getTabs() {
        return tabs;
    }

    public BrowserTab getActiveTab() {
        if (activeIndex < 0 || activeIndex >= tabs.size()) return null;
        return tabs.get(activeIndex);
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public boolean isPipEnabled() {
        return pipEnabled;
    }

    public void setPipEnabled(boolean enabled) {
        this.pipEnabled = enabled;
    }

    public void togglePip() {
        pipEnabled = !pipEnabled;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public double getPipX() { return pipX; }
    public double getPipY() { return pipY; }
    public double getPipWidth() { return pipWidth; }
    public double getPipHeight() { return pipHeight; }

    public void setPipBounds(double x, double y, double width, double height) {
        this.pipX = x;
        this.pipY = y;
        this.pipWidth = Math.max(160, width);
        this.pipHeight = Math.max(90, height);
    }

    /**
     * Keeps the PiP window inside the current game window and caps its size
     * relative to the screen, so a stale/oversized saved value (or a window
     * that got smaller) can never make the PiP cover the whole screen.
     *
     * On the very first call (nothing positioned yet, e.g. fresh install with
     * default 480x270 @ 20,20) it snaps the PiP to the bottom-right corner,
     * matching the reference layout.
     */
    public void clampPipToScreen(int screenWidth, int screenHeight) {
        double maxW = Math.max(160, screenWidth * 0.45);
        double maxH = Math.max(90, screenHeight * 0.55);

        pipWidth = Math.min(Math.max(160, pipWidth), maxW);
        pipHeight = Math.min(Math.max(90, pipHeight), maxH);

        if (!pipPositioned) {
            pipX = screenWidth - pipWidth - 20;
            pipY = (screenHeight - pipHeight) / 2.0;
            pipPositioned = true;
        }

        pipX = Math.min(Math.max(0, pipX), Math.max(0, screenWidth - pipWidth));
        pipY = Math.min(Math.max(0, pipY), Math.max(0, screenHeight - pipHeight));
    }

    public void closeAll() {
        for (BrowserTab tab : tabs) {
            tab.close();
        }
        tabs.clear();
        activeIndex = -1;
    }
}