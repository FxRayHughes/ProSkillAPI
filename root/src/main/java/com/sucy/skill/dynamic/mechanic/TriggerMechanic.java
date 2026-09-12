package com.sucy.skill.dynamic.mechanic;

import com.google.common.base.Objects;
import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.ComponentRegistry;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.TriggerHandler;
import com.sucy.skill.dynamic.trigger.Trigger;
import com.sucy.skill.dynamic.trigger.TriggerComponent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.TriggerMechanic
 */
@SkillNode(
        key = "trigger",
        name = "Trigger",
        nameZh = "触发",
        description = "Listens for a trigger on the current targets for a duration.",
        descriptionZh = "为当前每个目标临时挂上一个事件监听器，持续指定秒数；期间该目标身上发生指定触发器（死亡、受伤、跳跃、潜行等）时，执行本节点的子节点。触发时会把触发者写入施法者 cast data 的 listen-target 键（List<LivingEntity>），子节点可用 Remember 目标选择器以该键取回。注意子节点仍以原施法者和原技能等级执行。监听器在 load 阶段注册，触发器名非法会直接抛异常导致技能加载失败。",
        container = true)
public class TriggerMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Trigger",
            labelZh = "触发器",
            tooltip = "[trigger] The trigger to listen for",
            tooltipZh = "要监听的事件类型，取值必须是已注册的触发器名（Death、Land、Launch、Crouch、Physical Damage、Skill Damage 等）；填错会在技能加载时抛出 IllegalArgumentException。此项在加载时读取一次，不随等级变化。",
            options = {"Crouch", "Death", "Environment Damage", "Kill", "Land", "Launch", "Physical Damage", "Skill Damage", "Took Physical Damage", "Took Skill Damage"},
            optionsZh = {"可选值1", "死亡", "可选值3", "可选值4", "可选值5", "可选值6", "可选值7", "技能伤害", "可选值9", "可选值10"},
            defaultValue = "Death")
    private static final String TRIGGER = "trigger";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long to listen to the trigger for",
            tooltipZh = "监听持续时间，单位秒，内部乘 20 转成 tick；随技能等级缩放（base + scale×(等级-1)）。默认 5 秒。到时后移除该次监听。")
    private static final String DURATION = "duration";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Stackable",
            labelZh = "可叠加",
            tooltip = "[stackable] Whether or not different players (or the same player) can listen to the same target at the same time",
            tooltipZh = "是否允许同一目标身上同时存在多次监听。设为 False 时，若目标已被监听，本次执行直接返回 false（后续目标也不再处理）。默认 True。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String STACKABLE = "stackable";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Once",
            labelZh = "仅一次",
            tooltip = "[once] Whether or not the trigger should only be used once each cast. When false, the trigger can execute as many times as it happens for the duration.",
            tooltipZh = "触发一次后是否停止。True 时首次触发即摘掉该目标的全部监听记录，本次施法只会响应一次；False 时在持续时间内可以反复触发。默认 True。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String ONCE = "once";

    private final Map<Integer, List<Context>> CASTER_MAP = new HashMap<Integer, List<Context>>();

    private TriggerHandler triggerHandler;
    private boolean once;
    private boolean stackable;

    @Override
    public void load(final DynamicSkill skill, final DataSection dataSection) {
        super.load(skill, dataSection);

        final String name = settings.getString(TRIGGER, "DEATH");
        final Trigger trigger = ComponentRegistry.getTrigger(name);
        if (trigger == null) {
            throw new IllegalArgumentException("Skill is using invalid trigger for mechanic: " + name);
        }

        final Receiver receiver = new Receiver();
        triggerHandler = new TriggerHandler(skill, "fake", trigger, receiver);
        triggerHandler.register(JavaPlugin.getPlugin(SkillAPI.class));
        once = settings.getBool(ONCE, true);
        stackable = settings.getBool(STACKABLE, true);
    }

    @Override
    public String getKey() {
        return "trigger";
    }

    @Override
    public boolean execute(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        final int ticks = (int)(20 * parseValues(caster, DURATION, level, 5));

        boolean worked = false;
        for (final LivingEntity target : targets) {
            if (!stackable && CASTER_MAP.containsKey(target.getEntityId()))
                return false;

            if (!CASTER_MAP.containsKey(target.getEntityId())) {
                CASTER_MAP.put(target.getEntityId(), new ArrayList<>());
            }
            triggerHandler.init(target, level);

            final Context context = new Context(caster, level);
            CASTER_MAP.get(target.getEntityId()).add(context);
            SkillAPI.schedule(new StopTask(target, context), ticks);
            worked = true;
        }
        return worked;
    }

    private void remove(final LivingEntity target, final Context context) {
        final List<Context> contexts = CASTER_MAP.get(target.getEntityId());
        if (contexts == null) return;

        contexts.remove(context);
        if (contexts.isEmpty()) {
            CASTER_MAP.remove(target.getEntityId());
            triggerHandler.cleanup(target);
        }
    }

    private class StopTask implements Runnable {

        private final LivingEntity target;
        private final Context context;

        public StopTask(final LivingEntity target, final Context context) {
            this.target = target;
            this.context = context;
        }

        @Override
        public void run() {
            remove(target, context);
        }
    }

    private class Receiver extends TriggerComponent {

        private Receiver() {
            final DataSection data = new DataSection();
            TriggerMechanic.this.settings.save(data);
            this.settings.load(data);
        }

        @Override
        public boolean execute(final LivingEntity target, final int level, final List<LivingEntity> targets) {
            if (!CASTER_MAP.containsKey(target.getEntityId())) return false;

            final List<Context> contexts;
            if (once)
                contexts = CASTER_MAP.remove(target.getEntityId());
            else
                contexts = CASTER_MAP.get(target.getEntityId());

            final List<LivingEntity> targetList = new ArrayList<LivingEntity>();
            targetList.add(target);

            for (final Context context : contexts) {
                DynamicSkill.getCastData(context.caster).put("listen-target", targetList);
                TriggerMechanic.this.executeChildren(context.caster, context.level, targets);
            }

            return true;
        }
    }

    private static class Context {
        public final LivingEntity caster;
        public final int level;

        public Context(final LivingEntity caster, final int level) {
            this.caster = caster;
            this.level = level;
        }

        @Override
        public boolean equals(final Object other) {
            if (other == this) return true;
            if (!(other instanceof Context)) return false;
            final Context context = (Context) other;
            return context.caster == caster && context.level == level;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(caster, level);
        }
    }
}
