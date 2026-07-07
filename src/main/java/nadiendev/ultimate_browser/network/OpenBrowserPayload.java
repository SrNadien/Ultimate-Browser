package nadiendev.ultimate_browser.network;

import nadiendev.ultimate_browser.UltimateBrowserMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent from the server (via an OP command) to a specific client, instructing
 * it to open the browser screen, optionally navigating to a given URL first.
 *
 * If {@code url} is empty, the client just opens the browser to its current tab.
 */
public record OpenBrowserPayload(String url) implements CustomPacketPayload {

    public static final Type<OpenBrowserPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UltimateBrowserMod.MOD_ID, "open_browser"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBrowserPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeUtf(payload.url(), 32767),
                    buf -> new OpenBrowserPayload(buf.readUtf(32767))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
