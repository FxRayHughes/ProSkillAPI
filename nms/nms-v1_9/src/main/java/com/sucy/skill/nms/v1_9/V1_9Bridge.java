package com.sucy.skill.nms.v1_9;

import com.sucy.skill.nms.KeyPressDispatcher;
import com.sucy.skill.nms.PlayerPacketInjector;
import com.sucy.skill.nms.v1_8.V1_8Bridge;
import org.bukkit.plugin.Plugin;

/**
 * Bridge for the 1.9 NMS generation.
 *
 * <p>Delta from 1.8: 1.9 introduced the off-hand slot, and the client now sends
 * {@code PacketPlayInArmAnimation} for swings with either hand. Treating an
 * off-hand swing as a LEFT combo key made every off-hand interaction advance
 * the player's combo, so the injector filters on the hand field.</p>
 */
public class V1_9Bridge extends V1_8Bridge {
    @Override
    public String id() {
        return "v1_9";
    }

    @Override
    protected PlayerPacketInjector newPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher) {
        return new MainHandPacketInjector(plugin, dispatcher);
    }
}
