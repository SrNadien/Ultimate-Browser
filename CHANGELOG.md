# Changelog

## 1.0.1 — Minecraft 1.21.1 / NeoForge

### ⚠️ Dependency change: MCEF is now Rinku

Ultimate Browser no longer runs on **MCEF**. The browser engine it depends on was
renamed and rewritten by its author (Keksuccino) as **[Rinku](https://www.curseforge.com/minecraft/mc-mods/rinku)**,
and this release moves over to it.

**To update:**

1. Install **Rinku 3.0.1 or newer**.
2. You can remove MCEF, unless another mod in your pack still needs it.

Ultimate Browser will refuse to load without Rinku, so don't skip step 1.

### Fixed

- **Web pages rendered with wrong colors and transparency.** Chromium hands its
  frames over with premultiplied alpha, which the old drawing code blended
  incorrectly, darkening the page. Pages now render the way they should.
- **Crash on first launch.** Rinku downloads Chromium the first time you run it,
  and the browser isn't ready until that finishes. Opening a world during the
  download crashed the game; Ultimate Browser now waits instead. The same applies
  when Rinku can't start at all, such as on an unsupported platform.
- **Blurry or torn frame right after resizing** the browser or the
  Picture-in-Picture window.
- Your saved Picture-in-Picture size and position are now restored even when the
  browser engine isn't available yet.

### Notes

Everything else is unchanged: tabs, Picture-in-Picture, fullscreen browsing, the
chat link interceptor and the admin commands all work as before.
