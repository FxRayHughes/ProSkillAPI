package com.sucy.skill.nms;

import org.bukkit.entity.Player;

/**
 * Callback used by packet modules to report combo key presses without
 * depending on SkillAPI's event classes. The main plugin owns event creation,
 * so NMS modules remain reusable version adapters instead of business logic.
 */
public interface KeyPressDispatcher {
    /**
     * Reports a key press detected from a player packet.
     *
     * @param player player who pressed the key
     * @param keyName SkillAPI key name such as Q, LEFT, or RIGHT
     */
    void dispatch(Player player, String keyName);
}

