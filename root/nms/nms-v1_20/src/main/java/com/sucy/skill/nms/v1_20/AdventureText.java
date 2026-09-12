package com.sucy.skill.nms.v1_20;

import com.sucy.skill.nms.NmsCapabilities;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Adventure-backed action bars and titles.
 *
 * <p>Every call goes through reflection on purpose. Adventure ships with Paper
 * but not with Spigot, and its own 4.x line renamed {@code Title.Times.of} to
 * {@code times}. Binding to the classes directly would make this module fail to
 * link on a Spigot core, which is exactly the case it needs to survive.</p>
 */
final class AdventureText {
    private Method sendActionBar;
    private Method showTitle;
    private Method deserialize;
    private Method titleOf;
    private Method timesOf;
    private Object serializer;
    private boolean resolved;
    private boolean available;

    boolean isAvailable() {
        resolve();
        return available;
    }

    boolean sendActionBar(Player player, String message) {
        resolve();
        if (!available) {
            return false;
        }
        try {
            sendActionBar.invoke(player, component(message));
            return true;
        } catch (Throwable ignored) {
            available = false;
            return false;
        }
    }

    boolean sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        resolve();
        if (!available) {
            return false;
        }
        try {
            Object times = timesOf.invoke(null, ticks(fadeIn), ticks(duration), ticks(fadeOut));
            Object value = titleOf.invoke(null,
                    component(title == null ? "" : title),
                    component(subtitle == null ? "" : subtitle),
                    times);
            showTitle.invoke(player, value);
            return true;
        } catch (Throwable ignored) {
            available = false;
            return false;
        }
    }

    private Object component(String message) throws Exception {
        return deserialize.invoke(serializer, message);
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(Math.max(0L, ticks) * 50L);
    }

    private synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> component = Class.forName("net.kyori.adventure.text.Component");
            Class<?> title = Class.forName("net.kyori.adventure.title.Title");
            Class<?> times = Class.forName("net.kyori.adventure.title.Title$Times");
            Class<?> legacy = Class.forName(
                    "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Class<?> componentLike = Class.forName("net.kyori.adventure.text.ComponentLike");

            serializer = legacy.getMethod("legacySection").invoke(null);
            deserialize = legacy.getMethod("deserialize", String.class);
            sendActionBar = NmsCapabilities.findMethod(Player.class, "sendActionBar", componentLike);
            if (sendActionBar == null) {
                sendActionBar = NmsCapabilities.findMethod(Player.class, "sendActionBar", component);
            }
            showTitle = NmsCapabilities.findMethod(Player.class, "showTitle", title);
            titleOf = title.getMethod("title", component, component, times);
            timesOf = resolveTimes(times);

            available = sendActionBar != null && showTitle != null && timesOf != null;
        } catch (Throwable ignored) {
            // Core does not ship Adventure; the caller keeps its own path.
            available = false;
        }
    }

    private static Method resolveTimes(Class<?> times) {
        Method method = NmsCapabilities.findMethod(
                times, "times", Duration.class, Duration.class, Duration.class);
        if (method != null) {
            return method;
        }
        // Adventure 4.0 - 4.9 named this factory "of".
        return NmsCapabilities.findMethod(
                times, "of", Duration.class, Duration.class, Duration.class);
    }
}
