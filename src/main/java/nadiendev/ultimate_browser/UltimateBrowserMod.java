package nadiendev.ultimate_browser;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import nadiendev.ultimate_browser.client.ClientProxy;
import nadiendev.ultimate_browser.network.NetworkHandler;
import nadiendev.ultimate_browser.command.BrowserCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * UltimateBrowser
 *Google Chrome uwu
 */
@Mod(UltimateBrowserMod.MOD_ID)
public class UltimateBrowserMod {

    public static final String MOD_ID = "ultimate_browser";
    public static final Logger LOGGER = LoggerFactory.getLogger("UltimateBrowser");

    public UltimateBrowserMod(IEventBus modEventBus) {
        LOGGER.info("Initializing UltimateBrowser mod...");

        NetworkHandler.register(modEventBus);

        // Server-side 
        NeoForge.EVENT_BUS.addListener(BrowserCommand::onRegisterCommands);

        if (FMLEnvironment.dist.isClient()) {
            ClientProxy.init(modEventBus);
        }
    }
}
