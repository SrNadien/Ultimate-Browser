package nadiendev.ultimate_browser.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import nadiendev.ultimate_browser.UltimateBrowserMod;
import nadiendev.ultimate_browser.client.browser.BrowserManager;
import nadiendev.ultimate_browser.client.gui.BrowserScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Registers a purely client-side "/ub openlink <url>" command used internally
 * by the chat link interceptor. It never reaches the server; it just opens
 * the clicked URL inside a UltimateBrowser tab.
 */
public final class ClientCommand {

    private ClientCommand() {}

    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ub")
                        .then(Commands.literal("openlink")
                                .then(Commands.argument("url", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String url = StringArgumentType.getString(ctx, "url");
                                            BrowserManager manager = BrowserManager.get();
                                            if (manager.getTabs().isEmpty()) {
                                                manager.restoreFromConfig();
                                            }
                                            manager.openTab(url);
                                            Minecraft.getInstance().setScreen(new BrowserScreen());
                                            return 1;
                                        })))
        );
    }
}
