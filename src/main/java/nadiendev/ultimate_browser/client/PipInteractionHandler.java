package nadiendev.ultimate_browser.client;

import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.gui.PipInteractScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;


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