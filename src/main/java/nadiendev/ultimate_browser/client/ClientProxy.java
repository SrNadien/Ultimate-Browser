package nadiendev.ultimate_browser.client;

import nadiendev.ultimate_browser.UltimateBrowserMod;
import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.gui.BrowserScreen;
import nadiendev.ultimate_browser.client.gui.PipOverlay;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientProxy {

    private ClientProxy() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::register);
        NeoForge.EVENT_BUS.addListener(ClientCommand::register);
        // ClientEvents is auto-registered to the game event bus via @EventBusSubscriber
    }

    /** Auto-registered on the NeoForge game event bus. */
    @EventBusSubscriber(modid = UltimateBrowserMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        @net.neoforged.bus.api.SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            if (KeyBindings.OPEN_BROWSER.consumeClick()) {
                if (!(mc.screen instanceof BrowserScreen)) {
                    mc.setScreen(new BrowserScreen());
                }
            }

            if (KeyBindings.TOGGLE_PIP.consumeClick()) {
                BrowserManager.get().togglePip();
            }
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof BrowserScreen) return; // full screen handles its own rendering
            if (BrowserManager.get().isPipEnabled()) {
                PipOverlay.render(event.getGuiGraphics(), mc);
            }
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            BrowserManager.get().persist();
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
            BrowserManager.get().restoreFromConfig();
        }
    }
}
