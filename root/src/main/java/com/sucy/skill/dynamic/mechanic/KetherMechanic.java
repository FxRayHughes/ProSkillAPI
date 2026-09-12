/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueAddMechanic
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

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ray.mintcat.rayskillapiaddor.KetherAPI;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

@SkillNode(
        key = "kether",
        name = "Kether",
        nameZh = "凯瑟脚本",
        description = "使目标执行kether语句，目标必须是玩家",
        descriptionZh = "让玩家目标执行一段 Kether 脚本语句，依赖外部的 Kether 扩展（RaySkillAPIAddor 提供的 KetherAPI）。脚本内容取自节点设置里的引用键，该键在编辑器中没有对应字段，需直接写进 YAML。注意：当前实现的类型判断写在目标列表本身而非列表内的元素上，该条件永远不成立，因此实际不会执行任何脚本，只要填了引用键就直接返回成功。")
public class KetherMechanic extends MechanicComponent {
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "kether";
    }

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
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }
        String key = settings.getString(KEY);
        if (targets instanceof Player) {
            KetherAPI.INSTANCE.eval(((Player) targets), key);
        }
        return true;
    }
}
