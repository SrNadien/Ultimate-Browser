package nadiendev.ultimate_browser.client;

import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.gui.PipInteractScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Wires KeyBindings.INTERACT_PIP ("M") to open/close PipInteractScreen.
 *
 * NOTE: if you already have a client tick handler that consumes
 * OPEN_BROWSER/TOGGLE_PIP, move the INTERACT_PIP block below into that
 * existing handler instead of using this file, to avoid two separate
 * @EventBusSubscriber classes both polling keys every tick.
 *
 * Also double check the modid string below matches your @Mod(...) value —
 * it's assumed to be "ultimate_browser" based on your package name.
 */
@EventBusSubscriber(modid = "ultimate_browser", value = Dist.CLIENT)
public final class PipInteractionHandler {

    private PipInteractionHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        while (KeyBindings.INTERACT_PIP.consumeClick()) {
            BrowserManager manager = BrowserManager.get();

            if (mc.screen instanceof PipInteractScreen) {
                // M again while interacting: close it, back to click-through HUD.
                mc.setScreen(null);
            } else if (manager.isPipEnabled() && !manager.isFullscreen() && mc.screen == null) {
                // Only open it if the PiP is actually showing and no other
                // screen (inventory, chat, the fullscreen browser, etc.) is open.
                mc.setScreen(new PipInteractScreen());
            }
        }
    }
}