package nadiendev.ultimate_browser.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.minecraft.client.KeyMapping;

public final class KeyBindings {

    public static final String CATEGORY = "key.categories.ultimate_browser";

    public static KeyMapping OPEN_BROWSER;
    public static KeyMapping TOGGLE_PIP;
    public static KeyMapping SCROLL_UP;
    public static KeyMapping SCROLL_DOWN;
    public static KeyMapping TOGGLE_FULLSCREEN;
    public static KeyMapping INTERACT_PIP;

    private KeyBindings() {}

    public static void register(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        OPEN_BROWSER = new KeyMapping("key.ultimate_browser.open_browser",
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                InputConstants.KEY_B, CATEGORY);

        TOGGLE_PIP = new KeyMapping("key.ultimate_browser.toggle_pip",
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                InputConstants.KEY_P, CATEGORY);

        SCROLL_UP = new KeyMapping("key.ultimate_browser.scroll_up",
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                InputConstants.KEY_UP, CATEGORY);

        SCROLL_DOWN = new KeyMapping("key.ultimate_browser.scroll_down",
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                InputConstants.KEY_DOWN, CATEGORY);

        // F12 toggles fullscreen only while the browser screen is open; handled manually
        TOGGLE_FULLSCREEN = new KeyMapping("key.ultimate_browser.toggle_fullscreen",
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                InputConstants.KEY_F12, CATEGORY);

        // M opens the interactive PiP window (drag to move, drag corner to
        // resize, click/type into the page); M again closes it.
        INTERACT_PIP = new KeyMapping("key.ultimate_browser.interact_pip",
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                InputConstants.KEY_M, CATEGORY);

        event.register(OPEN_BROWSER);
        event.register(TOGGLE_PIP);
        event.register(SCROLL_UP);
        event.register(SCROLL_DOWN);
        event.register(TOGGLE_FULLSCREEN);
        event.register(INTERACT_PIP);
    }
}