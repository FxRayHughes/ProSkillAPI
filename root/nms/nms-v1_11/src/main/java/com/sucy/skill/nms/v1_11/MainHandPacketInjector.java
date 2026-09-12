package com.sucy.skill.nms.v1_11;

import com.sucy.skill.nms.KeyPressDispatcher;
import io.netty.channel.ChannelDuplexHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

/**
 * Injector that ignores off-hand swings when producing combo keys.
 */
public class MainHandPacketInjector extends LegacyPacketInjector {
    public MainHandPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        super(plugin, dispatcher);
    }

    @Override
    protected ChannelDuplexHandler createHandler(Player player) {
        return new MainHandKeyPacketHandler(player, dropField, dispatcher);
    }

    /**
     * Reads the {@code EnumHand} carried by the 1.9+ arm animation packet.
     */
    public static class MainHandKeyPacketHandler extends KeyPacketHandler {
        private Field handField;
        private boolean handFieldResolved;

        public MainHandKeyPacketHandler(Player player, Field dropField, KeyPressDispatcher dispatcher) {
            super(player, dropField, dispatcher);
        }

        @Override
        protected String keyFor(Object packet) {
            if ("PacketPlayInArmAnimation".equals(packet.getClass().getSimpleName())) {
                return isMainHand(packet) ? "LEFT" : null;
            }
            return super.keyFor(packet);
        }

        private boolean isMainHand(Object packet) {
            Field field = resolveHandField(packet);
            if (field == null) {
                // No readable hand field: keep the 1.8 behaviour rather than
                // dropping every left click on this core.
                return true;
            }
            try {
                Object hand = field.get(packet);
                return hand == null || "MAIN_HAND".equals(((Enum<?>) hand).name());
            } catch (Exception ignored) {
                return true;
            }
        }

        private Field resolveHandField(Object packet) {
            if (handFieldResolved) {
                return handField;
            }
            handFieldResolved = true;
            for (Field field : packet.getClass().getDeclaredFields()) {
                if (field.getType().isEnum() && field.getType().getSimpleName().contains("EnumHand")) {
                    field.setAccessible(true);
                    handField = field;
                    break;
                }
            }
            return handField;
        }
    }
}
