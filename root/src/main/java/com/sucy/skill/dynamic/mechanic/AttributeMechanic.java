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
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "attribute",
        name = "Attribute",
        nameZh = "属性",
        description = "Gives a player bonus attributes temporarily.",
        descriptionZh = "给目标临时增加指定属性点，到时自动扣回。玩家走 PlayerData 的加成属性，非玩家生物走 MobAttribute 的临时属性。施加前会抛出可取消的 TempAttributeAddEvent，被别的插件拦下则该目标跳过；属性名在属性管理器里不存在时整个节点中断并返回 false。目标列表为空返回 false，否则一律返回 true。技能施法者清理（doCleanUp）时会把该施法者名下所有未到期的任务立刻结算掉。")
public class AttributeMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Attribute",
            labelZh = "属性",
            tooltip = "[key] The name of the attribute to add to",
            tooltipZh = "要增加的属性名，必须是插件已注册的属性（如 Intelligence）。属性不存在时节点直接返回 false。",
            defaultValue = "Intelligence")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] How much to add to the player's attribute",
            tooltipZh = "增加的属性点数，默认 5，随技能等级缩放。取整后使用，小数会被截断。")
    private static final String AMOUNT = "amount";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] How long in seconds to give the attributes to the player",
            tooltipZh = "属性加成的持续秒数，默认 3，随技能等级缩放。内部乘 20 换算为 tick；若最终 tick 为负数则不安排归还任务，相当于永久生效直到施法者被清理。")
    private static final String SECONDS = "seconds";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Stackable",
            labelZh = "可叠加",
            tooltip = "[PREM] Whether or not applying multiple times stacks the effects",
            tooltipZh = "编辑器里该项的 tooltip 前缀误写成 [PREM]，实际配置键是 stackable。为 false 时同一施法者对同一目标重复施加不叠加，而是取消旧任务并把差值补齐；为 true 时多次施加叠加累积。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
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
        // 属性管理器只在 attributes-enabled=true 时创建；技能配置可能仍引用
        // 属性节点，因此必须在事件和属性写入前短路，避免关闭模块时产生 NPE。
        // 这是可选效果，跳过它仍让同一技能的其他子节点继续执行。
        if (SkillAPI.getAttributeManager() == null) {
            return true;
        }
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
