package com.sucy.skill.hook.mechanic;

import com.sucy.skill.listener.SkillAPIListener;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class MythicListener extends SkillAPIListener {


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void call(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("castskillapi")) {
            event.register(new MythicSkillMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("damageType")) {
            event.register(new MythicDamageMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
    }


}
