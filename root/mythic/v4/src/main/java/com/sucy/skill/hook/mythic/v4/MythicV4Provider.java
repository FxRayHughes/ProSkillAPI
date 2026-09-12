package com.sucy.skill.hook.mythic.v4;

import com.sucy.skill.hook.mythic.MythicProvider;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MythicMobs 4.x 适配实现。
 */
public class MythicV4Provider implements MythicProvider {

    @Override
    public int getMajorVersion() {
        return 4;
    }

    @Override
    public boolean isMonster(final LivingEntity target) {
        try {
            return MythicMobs.inst().getAPIHelper().isMythicMob(target);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void taunt(final LivingEntity target, final LivingEntity source, final double amount) {
        if (amount > 0) {
            MythicMobs.inst().getAPIHelper().addThreat(target, source, amount);
        } else if (amount < 0) {
            MythicMobs.inst().getAPIHelper().reduceThreat(target, source, -amount);
        }
    }

    @Override
    public boolean hasThreatTable(final LivingEntity entity) {
        try {
            if (!MythicMobs.inst().getAPIHelper().isMythicMob(entity)) return false;
            ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
            return mob != null && mob.hasThreatTable() && mob.getThreatTable() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void addThreatToMM(final LivingEntity mob, final LivingEntity target, final double amount) {
        try {
            if (!MythicMobs.inst().getAPIHelper().isMythicMob(mob)) return;
            if (amount > 0) {
                MythicMobs.inst().getAPIHelper().addThreat(mob, target, amount);
            } else if (amount < 0) {
                MythicMobs.inst().getAPIHelper().reduceThreat(mob, target, -amount);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean hasActiveTarget(final LivingEntity entity) {
        try {
            if (!MythicMobs.inst().getAPIHelper().isMythicMob(entity)) {
                return entity instanceof Creature && ((Creature) entity).getTarget() != null;
            }
            ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
            if (mob == null) return false;
            if (mob.hasThreatTable() && mob.getThreatTable() != null) {
                return mob.hasTarget();
            }
            return entity instanceof Creature && ((Creature) entity).getTarget() != null;
        } catch (Exception ex) {
            // 反射/API 异常时保守处理：假设仍有目标，避免误清理
            return true;
        }
    }

    @Override
    public void clearThreatTable(final LivingEntity entity) {
        try {
            if (!MythicMobs.inst().getAPIHelper().isMythicMob(entity)) return;
            ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
            if (mob == null) return;

            // 先重置 AI 目标状态，让怪物"忘记"当前目标，仅清表不足以让它停止追击
            try {
                mob.resetTarget();
            } catch (Exception ignored) {
            }
            if (mob.hasThreatTable() && mob.getThreatTable() != null) {
                try {
                    mob.getThreatTable().dropCombat();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (entity instanceof Creature) {
                ((Creature) entity).setTarget(null);
            }
        }
    }

    @Override
    public void shuffleThreatTable(final LivingEntity entity) {
        try {
            if (!MythicMobs.inst().getAPIHelper().isMythicMob(entity)) return;
            ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
            if (mob == null) return;

            if (mob.hasThreatTable() && mob.getThreatTable() != null) {
                try {
                    mob.getThreatTable().dropCombat();
                } catch (Exception ignored) {
                }
            }
            try {
                mob.resetTarget();
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        } finally {
            if (entity instanceof Creature) {
                ((Creature) entity).setTarget(null);
            }
        }
    }

    @Override
    public boolean castSkill(final LivingEntity caster, final String skillName) {
        return MythicMobs.inst().getAPIHelper().castSkill(caster, skillName);
    }

    @Override
    public void castSkill(final LivingEntity caster, final String skillName, final Float power) {
        final List<Entity> targets = new ArrayList<>();
        MythicMobs.inst().getAPIHelper()
                .castSkill(caster, skillName, caster, caster.getLocation(), targets, null, power);
    }

    @Override
    public void castSkill(final LivingEntity caster, final String skillName,
                          final Collection<Entity> targets, final Float power) {
        final List<Location> locations = new ArrayList<>();
        for (Entity target : targets) {
            locations.add(target.getLocation());
        }
        if (!locations.isEmpty()) {
            MythicMobs.inst().getAPIHelper()
                    .castSkill(caster, skillName, locations.get(0), targets, locations, power);
        } else {
            MythicMobs.inst().getAPIHelper()
                    .castSkill(caster, skillName, null, targets, locations, power);
        }
    }

    @Override
    public List<String> getMobAttributes(final Entity entity) {
        try {
            ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
            if (mob == null || mob.getType() == null) return Collections.emptyList();
            return mob.getType().getConfig().getStringList("psk-attribute");
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public double getMobAttribute(final LivingEntity entity, final String attrName) {
        try {
            if (!MythicMobs.inst().getAPIHelper().isMythicMob(entity)) return 0;
            ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
            if (mob == null || mob.getType() == null) return 0;
            return mob.getType().getConfig().getDouble(attrName, 0);
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public void registerListeners(final Object plugin) {
        if (!(plugin instanceof Plugin)) return;
        final Plugin owner = (Plugin) plugin;
        Bukkit.getPluginManager().registerEvents(new V4MechanicListener(this), owner);
        Bukkit.getPluginManager().registerEvents(new V4MobListener(this), owner);
    }
}
