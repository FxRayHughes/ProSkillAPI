package com.sucy.skill.combat.shield;

import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.listener.SkillAPIListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Applies custom shields to the final Bukkit health damage. The raw-damage search
 * accounts for nonlinear armor formulas instead of subtracting shield capacity
 * from the pre-armor value. Consumption waits until MONITOR to honor cancellation.
 */
public final class ShieldListener extends SkillAPIListener {
    private final Map<EntityDamageEvent, ShieldManager.Resolution> pending = new IdentityHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        double finalDamage = event.getFinalDamage();
        if (!Double.isFinite(finalDamage) || finalDamage <= 0) return;
        String kind = Skill.isSkillDamage() ? "skill"
                : event instanceof EntityDamageByEntityEvent ? "physical" : "environment";
        ShieldManager.Resolution resolution = ShieldManager.preview((LivingEntity) event.getEntity(),
                finalDamage, kind, event.getCause().name(), Skill.getDamageClassification());
        if (resolution.getAbsorbed() <= 0) return;
        double originalRaw = event.getDamage();
        if (!setFinalDamage(event, resolution.getRemainingDamage(), originalRaw)) return;
        pending.put(event, resolution);
    }

    /** Search raw damage while observing the server's own final-damage function. */
    private boolean setFinalDamage(EntityDamageEvent event, double wanted, double originalRaw) {
        if (wanted <= 0) {
            event.setDamage(0);
            return event.getFinalDamage() <= 0.001;
        }
        double low = 0;
        double high = originalRaw;
        for (int i = 0; i < 28; i++) {
            double middle = (low + high) / 2;
            event.setDamage(middle);
            if (event.getFinalDamage() < wanted) low = middle;
            else high = middle;
        }
        event.setDamage(high);
        if (Math.abs(event.getFinalDamage() - wanted) <= 0.01) return true;
        // Unsupported damage modifiers must never consume a shield for an unchanged hit.
        event.setDamage(originalRaw);
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageCommitted(EntityDamageEvent event) {
        ShieldManager.Resolution resolution = pending.remove(event);
        if (resolution == null || event.isCancelled()) return;
        // A later listener may mutate damage after the preview. Do not consume
        // capacity for a different final value, especially when it was reduced
        // to zero without marking the Bukkit event cancelled.
        double actual = event.getFinalDamage();
        if (!Double.isFinite(actual)
                || Math.abs(actual - resolution.getRemainingDamage()) > 0.01) return;
        ShieldManager.commit(resolution);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        ShieldManager.remove(event.getEntity(), null);
    }

    @Override public void cleanup() {
        pending.clear();
        ShieldManager.clear();
    }
}
