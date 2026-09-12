/**
 * SkillAPI
 * com.sucy.skill.integration.dragoncore.DragonCoreBridge
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.integration.dragoncore;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * Reflection bridge for DragonCore animation APIs. DragonCore has changed
 * helper methods between releases, so direct provider calls stay in this module
 * and SkillAPI's core can run unchanged when DragonCore is not installed.
 */
public final class DragonCoreBridge {
    private static final String CORE_API = "eos.moe.dragoncore.api.CoreAPI";
    private static final String PACKET_SENDER = "eos.moe.dragoncore.network.PacketSender";

    private DragonCoreBridge() {
        // Utility class: DragonCore exposes the relevant helpers as static APIs.
    }

    public static boolean setPlayerAnimation(Player player, String animation) {
        return invoke(PACKET_SENDER, "setPlayerAnimation",
                new Class<?>[]{Player.class, String.class},
                player, animation);
    }

    public static boolean removePlayerAnimation(Player player, String animation) {
        return invoke(PACKET_SENDER, "removePlayerAnimation",
                new Class<?>[]{Player.class, String.class},
                player, animation)
                || invoke(PACKET_SENDER, "removePlayerAnimation",
                new Class<?>[]{Player.class},
                player);
    }

    public static boolean setEntityAnimation(LivingEntity entity, String animation, int time) {
        return invoke(CORE_API, "setEntityAnimation",
                new Class<?>[]{LivingEntity.class, String.class, int.class},
                entity, animation, time)
                || invoke(PACKET_SENDER, "setModelEntityAnimation",
                new Class<?>[]{LivingEntity.class, String.class, int.class},
                entity, animation, time)
                || invoke(PACKET_SENDER, "setEntityAnimation",
                new Class<?>[]{LivingEntity.class, String.class, int.class},
                entity, animation, time);
    }

    public static boolean removeEntityAnimation(LivingEntity entity, String animation, int time) {
        return invoke(CORE_API, "removeEntityAnimation",
                new Class<?>[]{LivingEntity.class, String.class, int.class},
                entity, animation, time)
                || invoke(PACKET_SENDER, "removeModelEntityAnimation",
                new Class<?>[]{LivingEntity.class, String.class, int.class},
                entity, animation, time)
                || invoke(PACKET_SENDER, "removeEntityAnimation",
                new Class<?>[]{LivingEntity.class, String.class, int.class},
                entity, animation, time);
    }

    public static boolean setBlockAnimation(Player player, int x, int y, int z, String animation) {
        return invoke(PACKET_SENDER, "setBlockAnimation",
                new Class<?>[]{Player.class, int.class, int.class, int.class, String.class},
                player, x, y, z, animation);
    }

    public static boolean setEntityModelItemAnimation(LivingEntity entity, String animation) {
        return invoke(PACKET_SENDER, "setEntityModelItemAnimation",
                new Class<?>[]{LivingEntity.class, String.class},
                entity, animation)
                || invoke(PACKET_SENDER, "setModelItemAnimation",
                new Class<?>[]{LivingEntity.class, String.class},
                entity, animation)
                || invoke(PACKET_SENDER, "setEntityItemAnimation",
                new Class<?>[]{LivingEntity.class, String.class},
                entity, animation);
    }

    /**
     * Attempts one exact provider method. Integration failures are deliberately
     * contained so a missing DragonCore helper only disables that visual effect.
     */
    private static boolean invoke(String className, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> type = Class.forName(className);
            Method method = type.getMethod(methodName, parameterTypes);
            method.invoke(null, args);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
