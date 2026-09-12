package com.sucy.skill.hook.mythic.v5;

import com.sucy.skill.hook.mythic.MythicProvider;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 把 SkillAPI 的自定义机制注册进 MythicMobs 5 的技能体系。
 * <p>
 * 注册的机制：
 * <ul>
 *   <li>{@code skillapi} / {@code castskillapi} —— 施放 SkillAPI 技能</li>
 *   <li>{@code damageType} —— 走 SkillAPI 伤害管线</li>
 *   <li>{@code clearthreats} / {@code shufflethreats} / {@code resettarget} —— 仇恨操作</li>
 * </ul>
 */
public class V5MechanicListener implements Listener {

    private final MythicProvider provider;

    public V5MechanicListener(final MythicProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void call(MythicMechanicLoadEvent event) {
        final String name = event.getMechanicName();
        final String line = event.getContainer().getConfigLine();

        // skillapi 与 castskillapi 等价，后者为兼容既有配置保留
        if (name.equalsIgnoreCase("skillapi") || name.equalsIgnoreCase("castskillapi")) {
            event.register(new V5ApiSkillMechanic(line, event.getConfig()));
        } else if (name.equalsIgnoreCase("damageType")) {
            event.register(new V5DamageMechanic(line, event.getConfig()));
        } else if (name.equalsIgnoreCase("clearthreats")) {
            event.register(new V5ThreatMechanic(line, event.getConfig(), V5ThreatMechanic.Action.CLEAR, provider));
        } else if (name.equalsIgnoreCase("shufflethreats")) {
            event.register(new V5ThreatMechanic(line, event.getConfig(), V5ThreatMechanic.Action.SHUFFLE, provider));
        } else if (name.equalsIgnoreCase("resettarget")) {
            event.register(new V5ThreatMechanic(line, event.getConfig(), V5ThreatMechanic.Action.RESET, provider));
        }
    }
}
