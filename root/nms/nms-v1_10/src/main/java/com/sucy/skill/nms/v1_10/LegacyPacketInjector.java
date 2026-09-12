package com.sucy.skill.nms.v1_10;

import com.sucy.skill.nms.KeyPressDispatcher;
import com.sucy.skill.nms.PlayerPacketInjector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Netty injector that turns raw client packets into SkillAPI combo keys.
 *
 * <p>Pre-flattening cores have no event for a left click on air or for the drop
 * key, so the packet stream is the only source. Later generations replace the
 * handler through {@link #createHandler} rather than reimplementing the
 * pipeline plumbing.</p>
 */
public class LegacyPacketInjector implements PlayerPacketInjector {
    protected static final String HANDLER_NAME = "PacketInjector";

    protected final Plugin plugin;
    protected final KeyPressDispatcher dispatcher;
    protected Field playerCon;
    protected Field network;
    protected Method handle;
    protected Field channel;
    protected Field dropField;

    public LegacyPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        this.plugin = plugin;
        this.dispatcher = dispatcher;
        try {
            playerCon = LegacyReflection.nmsClass("EntityPlayer").getField("playerConnection");
            network = LegacyReflection.nmsClass("PlayerConnection").getField("networkManager");
            Class<?> networkManager = LegacyReflection.nmsClass("NetworkManager");
            try {
                channel = networkManager.getField("channel");
            } catch (Exception ignored) {
                // Obfuscated builds expose the channel as the field "i".
                channel = networkManager.getDeclaredField("i");
                channel.setAccessible(true);
            }
            handle = LegacyReflection.craftClass("entity.CraftPlayer").getMethod("getHandle");
        } catch (Throwable ex) {
            plugin.getLogger().warning(
                    "Failed to set up packet listener - some click combos may not behave properly");
            handle = null;
        }

        try {
            dropField = LegacyReflection.nmsClass("PacketPlayInBlockDig").getDeclaredField("c");
            dropField.setAccessible(true);
        } catch (Exception ignored) {
            // Without this field the drop key simply never fires a combo.
            dropField = null;
        }
    }

    @Override
    public boolean isWorking() {
        return handle != null;
    }

    @Override
    public void addPlayer(Player player) {
        if (!isWorking()) {
            return;
        }
        try {
            Channel ch = getChannel(player);
            if (ch.pipeline().get(HANDLER_NAME) == null) {
                ch.pipeline().addBefore("packet_handler", HANDLER_NAME, createHandler(player));
            }
        } catch (Throwable ex) {
            plugin.getLogger().warning(
                    "Failed to attach the combo packet handler for " + player.getName());
        }
    }

    @Override
    public void removePlayer(Player player) {
        if (!isWorking()) {
            return;
        }
        try {
            Channel ch = getChannel(player);
            if (ch.pipeline().get(HANDLER_NAME) != null) {
                ch.pipeline().remove(HANDLER_NAME);
            }
        } catch (Throwable ignored) {
            // The channel is already gone when the player disconnected first.
        }
    }

    /**
     * @param player owner of the channel
     * @return the handler installed into this player's pipeline
     */
    protected ChannelDuplexHandler createHandler(Player player) {
        return new KeyPacketHandler(player, dropField, dispatcher);
    }

    protected Channel getChannel(Player player) throws Exception {
        return (Channel) channel.get(network.get(playerCon.get(handle.invoke(player))));
    }

    /**
     * Maps the three packets SkillAPI treats as combo keys.
     */
    public static class KeyPacketHandler extends ChannelDuplexHandler {
        protected final Player player;
        protected final Field dropField;
        protected final KeyPressDispatcher dispatcher;

        public KeyPacketHandler(Player player, Field dropField, KeyPressDispatcher dispatcher) {
            this.player = player;
            this.dropField = dropField;
            this.dispatcher = dispatcher;
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
            String key = keyFor(packet);
            if (key != null) {
                dispatcher.dispatch(player, key);
            }
            super.channelRead(context, packet);
        }

        /**
         * @param packet inbound packet
         * @return combo key name, or null when the packet is not a combo input
         */
        protected String keyFor(Object packet) {
            switch (packet.getClass().getSimpleName()) {
                case "PacketPlayInBlockDig":
                    return isDrop(packet) ? "Q" : null;
                case "PacketPlayInArmAnimation":
                    return "LEFT";
                case "PacketPlayInUseItem":
                case "PacketPlayInBlockPlace":
                    return "RIGHT";
                default:
                    return null;
            }
        }

        protected boolean isDrop(Object packet) {
            if (dropField == null) {
                return false;
            }
            try {
                return "DROP_ITEM".equals(((Enum<?>) dropField.get(packet)).name());
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
