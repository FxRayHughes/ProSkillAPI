package com.sucy.skill.nms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Version boundary for behavior that used to reach directly into Minecraft
 * server internals. The main plugin must depend on this stable contract only,
 * allowing each module to adapt field names, packet classes, and public API
 * replacements for its supported core line.
 */
public interface NmsBridge {
    /**
     * @return identifier of the active implementation, used for diagnostics
     */
    default String id() {
        return getClass().getSimpleName();
    }

    /**
     * Creates a player packet injector for combo keys. Implementations may use
     * NMS, ProtocolLib, or a no-op when the running core has no stable hook.
     *
     * @param plugin plugin instance used for logging and scheduler ownership
     * @param dispatcher callback owned by the main plugin's combo event layer
     * @return packet injector for the running server core
     */
    PlayerPacketInjector createPacketInjector(Plugin plugin, KeyPressDispatcher dispatcher);

    /**
     * Records the player responsible for an entity's death. Legacy servers
     * expose this through NMS fields, while modern servers should let the caller
     * fall back to Bukkit metadata because those fields are not stable.
     *
     * @param entity damaged entity
     * @param player player responsible for the damage
     * @return true when the native killer state was updated
     */
    boolean markKiller(LivingEntity entity, Player player);

    /**
     * Finds living entities in the projectile hit box. This stays behind the
     * bridge because old NMS worlds used private AABB queries while modern
     * Bukkit exposes nearby-entity lookups.
     *
     * @param location center of the projectile collision area
     * @param radius collision radius
     * @param thrower projectile owner to exclude
     * @return living entities inside the collision area
     */
    List<LivingEntity> getColliding(Location location, double radius, LivingEntity thrower);

    /**
     * @return true when action bar messages can be sent on this server
     */
    boolean isActionBarSupported();

    /**
     * Sends an action bar message. The caller provides already formatted legacy
     * color text so implementations do not need to know about SkillAPI config
     * formatting dependencies.
     *
     * @param player player to receive the message
     * @param message formatted legacy text
     * @return true when the message was sent
     */
    boolean sendActionBar(Player player, String message);

    /**
     * Sends title text using the best implementation for the current core.
     *
     * @param player player to receive the title
     * @param title title text, nullable
     * @param subtitle subtitle text, nullable
     * @param fadeIn fade-in ticks
     * @param duration stay ticks
     * @param fadeOut fade-out ticks
     */
    void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut);

    /**
     * Removes or hides vanilla attack-damage display data while preserving the
     * custom item metadata used by SkillAPI.
     *
     * @param item source item
     * @return item with vanilla attack damage hidden when supported
     */
    ItemStack removeAttackDmg(ItemStack item);

    /**
     * Initializes legacy particle packet support. Modern cores use Bukkit's
     * public particle API and may intentionally do nothing here.
     */
    void initParticles();

    /**
     * Creates a legacy particle packet for older callers that still cache packet
     * instances. Modern cores return null because they send particles directly.
     */
    Object makeParticlePacket(
            String name,
            double x,
            double y,
            double z,
            float dx,
            float dy,
            float dz,
            float speed,
            int amount,
            Material material,
            int data) throws Exception;

    /**
     * Sends already-created packets to one player. Packet instances are opaque
     * because only the version implementation knows their concrete NMS class.
     */
    void sendPackets(Player player, Iterable<?> packets) throws Exception;
}
