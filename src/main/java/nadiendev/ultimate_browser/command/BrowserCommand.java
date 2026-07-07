package nadiendev.ultimate_browser.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import nadiendev.ultimate_browser.network.OpenBrowserPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers the /ub server-side command tree:
 *   /ub web [user] [web]      - opens a specific page for the target player
 *   /ub browser [user]        - opens the browser (to its current tab) for the target player
 *
 * Both subcommands require operator permission level 2.
 */
public final class BrowserCommand {

    private BrowserCommand() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ub")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("web")
                                .then(Commands.argument("user", EntityArgument.player())
                                        .then(Commands.argument("web", StringArgumentType.greedyString())
                                                .executes(BrowserCommand::openWeb))))
                        .then(Commands.literal("browser")
                                .then(Commands.argument("user", EntityArgument.player())
                                        .executes(BrowserCommand::openBrowser)))
        );
    }

    private static int openWeb(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "user");
        String rawUrl = StringArgumentType.getString(ctx, "web");
        final String url = (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://"))
                ? "https://" + rawUrl
                : rawUrl;
        PacketDistributor.sendToPlayer(target, new OpenBrowserPayload(url));
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Opened " + url + " for " + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int openBrowser(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "user");
        PacketDistributor.sendToPlayer(target, new OpenBrowserPayload(""));
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Opened browser for " + target.getGameProfile().getName()), true);
        return 1;
    }
}