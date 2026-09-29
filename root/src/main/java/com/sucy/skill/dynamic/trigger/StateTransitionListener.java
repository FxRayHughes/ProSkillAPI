package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.listener.SkillAPIListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.function.BooleanSupplier;

/** Converts cancelable toggle requests into confirmed state transitions. */
public final class StateTransitionListener extends SkillAPIListener {
    private final Map<UUID, Integer> airTicks = new HashMap<>();
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        boolean next = event.isSprinting();
        confirm(player, next, player::isSprinting,
                next ? StateTransitionEvent.Kind.SPRINT_STARTED : StateTransitionEvent.Kind.SPRINT_ENDED);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        boolean next = event.isGliding();
        confirm(entity, next, entity::isGliding,
                next ? StateTransitionEvent.Kind.GLIDE_STARTED : StateTransitionEvent.Kind.GLIDE_ENDED);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        boolean next = event.isFlying();
        confirm(player, next, player::isFlying,
                next ? StateTransitionEvent.Kind.FLIGHT_STARTED : StateTransitionEvent.Kind.FLIGHT_ENDED);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        boolean rising = event.getTo().getY() > event.getFrom().getY();
        boolean airborne = !player.isOnGround();
        if (!airborne) {
            airTicks.remove(player.getUniqueId());
            return;
        }
        int ticks = airTicks.merge(player.getUniqueId(), 1, Integer::sum);
        // A jump is the ground-to-air boundary; sustained flight is intentionally not
        // reported as a jump because it has no corresponding takeoff edge.
        if (rising && ticks == 1)
            Bukkit.getPluginManager().callEvent(new StateTransitionEvent(StateTransitionEvent.Kind.JUMP_TAKEOFF, player, ticks));
        // Emit the sampled count; each skill's configured threshold is matched by
        // equality, so every threshold fires once without keeping per-skill state.
        Bukkit.getPluginManager().callEvent(new StateTransitionEvent(StateTransitionEvent.Kind.AIR_THRESHOLD_CROSSED, player, ticks));
    }
    private void confirm(LivingEntity entity, boolean expected, BooleanSupplier actual,
                         StateTransitionEvent.Kind kind) {
        // The underlying Bukkit event is a request. Reading actual state next tick
        // prevents a later cancellation or replacement from being labeled completed.
        Bukkit.getScheduler().runTask(SkillAPI.getPlugin(SkillAPI.class), () -> {
            if (entity.isValid() && actual.getAsBoolean() == expected)
                Bukkit.getPluginManager().callEvent(new StateTransitionEvent(kind, entity));
        });
    }
}
