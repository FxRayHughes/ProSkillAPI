/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ParticleEffectCancel
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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

import com.sucy.skill.api.particle.EffectData;
import com.sucy.skill.api.particle.EffectManager;
import com.sucy.skill.api.particle.target.EntityTarget;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "cancel effect",
        name = "Cancel Effect",
        nameZh = "取消效果",
        description = "Stops a particle effect prematurely.",
        descriptionZh = "提前终止目标身上正在播放的粒子特效。按「特效引用键」匹配，只取消该键对应的那一个，目标身上没有特效数据时静默跳过。只要目标列表非空就返回 true，无论实际是否取消到东西。")
public class CancelEffectMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Effect Key",
            labelZh = "特效引用键",
            tooltip = "[effect-key] The key used when setting up the effect",
            tooltipZh = "要取消的特效引用键，须与当初创建特效时用的键一致。未填时默认取当前技能名，而不是字面的 default。",
            defaultValue = "default")
    private static final String KEY = "effect-key";

    @Override
    public String getKey() {
        return "cancel effect";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String key = settings.getString(KEY, skill.getName());

        for (LivingEntity target : targets) {
            EffectData effectData = EffectManager.getEffectData(new EntityTarget(target));
            if (effectData != null) { effectData.cancel(key); }
        }

        return targets.size() > 0;
    }
}
