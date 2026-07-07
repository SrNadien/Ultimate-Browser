package nadiendev.ultimate_browser.network;

import nadiendev.ultimate_browser.UltimateBrowserMod;
import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.gui.BrowserScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {

    private NetworkHandler() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(UltimateBrowserMod.MOD_ID).versioned("1.0");
        registrar.playToClient(
                OpenBrowserPayload.TYPE,
                OpenBrowserPayload.CODEC,
                NetworkHandler::handleOpenBrowser
        );
    }

    private static void handleOpenBrowser(OpenBrowserPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            BrowserManager manager = BrowserManager.get();
            if (manager.getTabs().isEmpty()) {
                manager.restoreFromConfig();
            }
            if (!payload.url().isEmpty()) {
                if (manager.getActiveTab() != null) {
                    manager.getActiveTab().loadUrl(payload.url());
                } else {
                    manager.openTab(payload.url());
                }
            }
            mc.setScreen(new BrowserScreen());
        });
    }
}
