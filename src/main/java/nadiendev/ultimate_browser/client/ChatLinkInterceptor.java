package nadiendev.ultimate_browser.client;

import nadiendev.ultimate_browser.UltimateBrowserMod;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intercepts chat messages, finds raw URLs / existing OPEN_URL click events,
 * and rewrites them to run our client-side command instead, so clicking a
 * link opens it inside a UltimateBrowser tab rather than the system browser.
 */
@EventBusSubscriber(modid = UltimateBrowserMod.MOD_ID, value = Dist.CLIENT)
public final class ChatLinkInterceptor {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);

    private ChatLinkInterceptor() {}

    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent event) {
        Component original = event.getMessage();
        MutableComponent rewritten = rewrite(original);
        if (rewritten != null) {
            event.setMessage(rewritten);
        }
    }

    private static MutableComponent rewrite(Component component) {
        String plain = component.getString();
        Matcher matcher = URL_PATTERN.matcher(plain);
        if (!matcher.find()) {
            return null;
        }

        // Rebuild the message as plain text with click events pointing at our
        // internal command, preserving the visible text exactly.
        MutableComponent result = Component.empty();
        int last = 0;
        matcher.reset();
        while (matcher.find()) {
            if (matcher.start() > last) {
                result.append(plain.substring(last, matcher.start()));
            }
            String url = matcher.group(1);
            Style linkStyle = Style.EMPTY
                    .withUnderlined(true)
                    .withColor(net.minecraft.ChatFormatting.BLUE)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ub openlink " + url));
            result.append(Component.literal(url).setStyle(linkStyle));
            last = matcher.end();
        }
        if (last < plain.length()) {
            result.append(plain.substring(last));
        }
        return result;
    }
}
