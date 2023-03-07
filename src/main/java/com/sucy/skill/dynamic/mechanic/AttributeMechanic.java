/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.FlagMechanic
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.api.event.TempAttributeAddEvent;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Applies a flag to each target
 */
public class AttributeMechanic extends MechanicComponent {
    private static final String KEY = "key";
    private static final String AMOUNT = "amount";
    private static final String SECONDS = "seconds";
    private static final String STACKABLE = "stackable";

    private final Map<Integer, Map<String, AttribTask>> tasks = new HashMap<>();

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String key = settings.getString(KEY, "");
        if (targets.size() == 0) {
            return false;
        }

        final Map<String, AttribTask> casterTasks = tasks.computeIfAbsent(caster.getEntityId(), HashMap::new);
        final int amount = (int) parseValues(caster, AMOUNT, level, 5);
        final double seconds = parseValues(caster, SECONDS, level, 3.0);
        final boolean stackable = settings.getString(STACKABLE, "false").equalsIgnoreCase("true");
        final int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            TempAttributeAddEvent event = AttributeAPI.tempAttribute(target, key, amount, ticks);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (SkillAPI.getAttributeManager().getAttribute(key) == null) {
                    return false;
                }
                if (event.getCaster() instanceof Player) {
                    final PlayerData data = SkillAPI.getPlayerData((Player) event.getCaster());

                    if (casterTasks.containsKey(data.getPlayerName()) && !stackable) {
                        final AttribTask old = casterTasks.remove(data.getPlayerName());
                        if (event.getValue() != old.amount) {
                            data.addBonusAttributes(event.getAttribute(), (int) (event.getValue() - old.amount));
                        }
                        old.cancel();
                    } else {
                        data.addBonusAttributes(event.getAttribute(), (int) event.getValue());
                    }

                    final AttribTask task = new AttribTask(caster.getEntityId(), data, event.getAttribute(), (int) event.getValue());
                    casterTasks.put(data.getPlayerName(), task);
                    if (event.getTick() >= 0) {
                        SkillAPI.schedule(task, (int) event.getTick());
                    }
                } else {
                    final MobAttributeData data = MobAttribute.getData(event.getCaster().getUniqueId(), true);
                    assert data != null;
                    UUID taskID = UUID.randomUUID();
                    if (casterTasks.containsKey(data.getUuid().toString()) && !stackable) {
                        final AttribTask old = casterTasks.remove(data.getUuid().toString());
                        if (event.getValue() != old.amount) {
                            data.tempAddAttribute(taskID.toString(), event.getAttribute(), event.getValue() - old.amount);
                        }
                        old.cancel();
                    } else {
                        data.tempAddAttribute(taskID.toString(), event.getAttribute(), event.getValue());
                    }

                    final AttribTask task = new AttribTask(caster.getEntityId(), data, taskID, event.getAttribute(), (int) event.getValue());
                    casterTasks.put(data.getUuid().toString(), task);
                    if (event.getTick() >= 0) {
                        SkillAPI.schedule(task, (int) event.getTick());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getKey() {
        return "attribute";
    }

    @Override
    protected void doCleanUp(final LivingEntity user) {
        final Map<String, AttribTask> casterTasks = tasks.remove(user.getEntityId());
        if (casterTasks != null) {
            casterTasks.values().forEach(AttribTask::stop);
        }
    }

    private class AttribTask extends BukkitRunnable {
        private final PlayerData data;
        private final MobAttributeData mob;

        private final UUID taskID;
        private final String attrib;
        private final int amount;
        private final int id;
        private boolean running = false;
        private boolean stopped = false;

        AttribTask(int id, MobAttributeData mob, UUID taskID, String attrib, int amount) {
            this.id = id;
            this.data = null;
            this.mob = mob;
            this.attrib = attrib;
            this.amount = amount;
            this.taskID = taskID;
            init();
        }

        AttribTask(int id, PlayerData data, String attrib, int amount) {
            this.id = id;
            this.data = data;
            this.mob = null;
            this.attrib = attrib;
            this.amount = amount;
            this.taskID = UUID.randomUUID();
            init();
        }

        public void stop() {
            if (!stopped) {
                stopped = true;
                run();
                if (running) {
                    cancel();
                }
            }
        }

        @NotNull
        @Override
        public BukkitTask runTaskLater(@NotNull final Plugin plugin, final long delay) {
            running = true;
            return super.runTaskLater(plugin, delay);
        }

        public void init() {

        }

        @Override
        public void run() {
            if (data != null) {
                data.addBonusAttributes(attrib, -amount);
                if (tasks.containsKey(id)) {
                    tasks.get(id).remove(data.getPlayerName());
                }
                running = false;
                return;
            }
            if (mob != null) {
                mob.tempRemove(taskID.toString());
                running = false;
            }
        }
    }
}
