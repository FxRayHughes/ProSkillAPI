package com.sucy.skill.hook.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class MythicSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected PlaceholderInt level;
    protected PlaceholderString name;

    public MythicSkillMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.name = PlaceholderString.of(mlc.getString(new String[]{"name", "n"}, "null"));
        this.level = PlaceholderInt.of(mlc.getString(new String[]{"level", "l"}, "1"));
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return false;
        }

        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return false;
        }

        int level = this.level.get(data, target);
        Skill skill = SkillAPI.getSkill(name.get(data, target));
        if (skill == null) {
            return false;
        }

        Entity entity = target.getBukkitEntity();
        if (entity instanceof LivingEntity) {
            SkillCastAPI.cast((LivingEntity) entity, skill, level);
            return true;
        }
        return false;
    }

}
