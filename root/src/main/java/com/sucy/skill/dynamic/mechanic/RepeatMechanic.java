/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.RepeatMechanic
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Executes child components multiple times
 */
@SkillNode(
        key = "repeat",
        name = "Repeat",
        nameZh = "重复",
        description = "Applies child components multiple times. When it applies them is determined by the delay (seconds before the first application) and period (seconds between successive applications).",
        descriptionZh = "按初始延迟与固定周期，把子节点重复执行指定次数。每轮执行前剔除已死亡或失效的目标，并重新读取技能当前等级；技能不再激活或目标全部消失时提前结束。重复次数不大于 0 或目标列表为空时返回 false。与「被动」不同，本节点允许同一施法者同时存在多个重复任务。",
        container = true)
public class RepeatMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Repetitions",
            labelZh = "重复次数",
            tooltip = "[repetitions] How many times to activate child components",
            tooltipZh = "子节点执行的总次数，随技能等级缩放，默认 3；小于等于 0 时节点直接失效。")
    private static final String REPETITIONS  = "repetitions";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Delay",
            labelZh = "延迟",
            tooltip = "[delay] The initial delay before starting to apply child components",
            tooltipZh = "首次执行前的等待秒数，默认 0，不随等级缩放；内部乘 20 转成 tick。",
            defaultValue = "0")
    private static final String DELAY        = "delay";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Period",
            labelZh = "周期",
            tooltip = "[period] The time in seconds between each time applying child components",
            tooltipZh = "两次执行之间的间隔秒数，默认 1，不随等级缩放；内部乘 20 转成 tick。",
            defaultValue = "1")
    private static final String PERIOD       = "period";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Stop on Fail",
            labelZh = "失败时停止",
            tooltip = "[stop-on-fail] Whether or not to stop the repeat task early if the effects fail",
            tooltipZh = "子节点执行失败（返回 false）时是否立刻中止后续重复，默认 False 即失败也继续重复到次数用尽。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String STOP_ON_FAIL = "stop-on-fail";

    private final Map<Integer, List<RepeatTask>> tasks = new HashMap<>();

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() > 0) {
            final int count = (int) parseValues(caster, REPETITIONS, level, 3.0);
            if (count <= 0) { return false; }

            final int delay = (int) (settings.getDouble(DELAY, 0.0) * 20);
            final int period = (int) (settings.getDouble(PERIOD, 1.0) * 20);
            final boolean stopOnFail = settings.getBool(STOP_ON_FAIL, false);
            final RepeatTask task = new RepeatTask(caster, targets, count, delay, period, stopOnFail);
            tasks.computeIfAbsent(caster.getEntityId(), ArrayList::new).add(task);

            return true;
        }
        return false;
    }

    @Override
    public String getKey() {
        return "repeat";
    }

    @Override
    protected void doCleanUp(final LivingEntity caster) {
        final List<RepeatTask> casterTasks = tasks.remove(caster.getEntityId());
        if (casterTasks != null) {
            casterTasks.forEach(RepeatTask::cancel);
        }
    }

    private class RepeatTask extends BukkitRunnable {
        private final List<LivingEntity> targets;
        private final LivingEntity       caster;
        private final boolean            stopOnFail;

        private int count;

        RepeatTask(
                LivingEntity caster,
                List<LivingEntity> targets,
                int count,
                int delay,
                int period,
                boolean stopOnFail) {
            this.targets = new ArrayList<>(targets);
            this.caster = caster;
            this.count = count;
            this.stopOnFail = stopOnFail;

            SkillAPI.schedule(this, delay, period);
        }

        @Override
        public void cancel() {
            super.cancel();
            final List<RepeatTask> casterTasks = tasks.get(caster.getEntityId());
            if (casterTasks != null) {
                casterTasks.remove(this);
            }
        }

        @Override
        public void run() {
            for (int i = 0; i < targets.size(); i++) {
                if (targets.get(i).isDead() || !targets.get(i).isValid()) { targets.remove(i); }
            }

            if (!skill.isActive(caster) || targets.size() == 0) {
                cancel();
                return;
            }

            final int level = skill.getActiveLevel(caster);
            boolean success = executeChildren(caster, level, targets);

            if (--count <= 0 || (!success && stopOnFail)) {
                cancel();
            }

            if (skill.checkCancelled()) {
                cancel();
            }
        }
    }
}
