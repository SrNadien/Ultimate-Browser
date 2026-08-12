package nadiendev.ultimate_browser.client.browser;

import de.keksuccino.rinku.Rinku;
import de.keksuccino.rinku.RinkuBrowser;


public class BrowserTab {

    private final RinkuBrowser browser;
    private String title = "New Tab";
    private String url;
    private boolean closed = false;

    public BrowserTab(String startUrl, int width, int height, boolean transparent) {
        this.url = startUrl;
        this.browser = Rinku.createBrowser(startUrl, transparent, width, height);
    }

    public RinkuBrowser getBrowser() {
        return browser;
    }

    public String getUrl() {
        String current = browser.getURL();
        return (current != null && !current.isEmpty()) ? current : url;
    }

    public void loadUrl(String newUrl) {
        this.url = newUrl;
        browser.loadURL(newUrl);
    }

    /**
     * RinkuBrowser has no getTitle(); we fall back to showing the URL as
     * the tab label. Hook a CefDisplayHandler if you want real page titles.
     */
    public String getTitle() {
        return title != null && !title.equals("New Tab") ? title : getUrl();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean canGoBack() {
        return browser.canGoBack();
    }

    public boolean canGoForward() {
        return browser.canGoForward();
    }

    public void goBack() {
        if (canGoBack()) browser.goBack();
    }

    public void goForward() {
        if (canGoForward()) browser.goForward();
    }

    public void reload() {
        browser.reload();
    }

    public void resize(int width, int height) {
        browser.resize(width, height);
    }

    public void close() {
        browser.close();
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}