package nadiendev.ultimate_browser.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nadiendev.ultimate_browser.UltimateBrowserMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of open tabs and PiP window state to a JSON file
 * under config/ultimate_browser/browser_state.json
 */
public class BrowserConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("ultimate_browser");
    private static final Path STATE_FILE = CONFIG_DIR.resolve("browser_state.json");

    public static class TabState {
        public String url;
        public boolean active;

        public TabState() {}

        public TabState(String url, boolean active) {
            this.url = url;
            this.active = active;
        }
    }

    public static class PipState {
        public double x = 20;
        public double y = 20;
        public double width = 480;
        public double height = 270;
        public boolean enabled = false;
    }

    public static class State {
        public List<TabState> tabs = new ArrayList<>();
        public PipState pip = new PipState();
    }

    private static State state;

    public static State get() {
        if (state == null) {
            load();
        }
        return state;
    }

    public static void load() {
        try {
            if (Files.exists(STATE_FILE)) {
                try (Reader reader = Files.newBufferedReader(STATE_FILE, StandardCharsets.UTF_8)) {
                    state = GSON.fromJson(reader, State.class);
                }
            }
        } catch (IOException e) {
            UltimateBrowserMod.LOGGER.warn("Could not read browser_state.json, using defaults", e);
        }
        if (state == null) {
            state = new State();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer writer = Files.newBufferedWriter(STATE_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(get(), writer);
            }
        } catch (IOException e) {
            UltimateBrowserMod.LOGGER.warn("Could not save browser_state.json", e);
        }
    }
}
