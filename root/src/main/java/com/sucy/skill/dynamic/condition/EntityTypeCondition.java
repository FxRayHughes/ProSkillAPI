/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.EntityTypeCondition
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
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.stream.Collectors;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "entity type",
        name = "Entity Type",
        nameZh = "检查实体类型",
        description = "Applies child elements if the target matches one of the selected entity types",
        descriptionZh = "检查目标的实体类型是否在勾选列表中。勾选名会转大写、空格换下划线后与实体类型枚举名精确比对；一个都不勾选时该条件永远不通过。",
        container = true)
public class EntityTypeCondition extends ConditionComponent {

    @SkillField(
            kind = FieldKind.MultiListValue,
            label = "Types",
            labelZh = "类型列表",
            tooltip = "[types] The entity types to target",
            tooltipZh = "允许的实体类型列表，命中任一即通过。名称转大写并把空格替换为下划线，须与服务端 EntityType 枚举名一致；留空则永不通过。",
            options = {"Area Effect Cloud", "Armor Stand", "Arrow", "Bat", "Blaze", "Boat", "Cave Spider", "Chicken", "Complex Part", "Cow", "Creeper", "Donkey", "Dragon Fireball", "Dropped Item", "Egg", "Elder Guardian", "Ender Crystal", "Ender Dragon", "Ender Pearl", "Ender Signal", "Enderman", "Endermite", "Evoker", "Evoker Fangs", "Experience Orb", "Falling Block", "Fireball", "Firework", "Fishing Hook", "Ghast", "Giant", "Guardian", "Horse", "Husk", "Illusioner", "Iron Golem", "Item Frame", "Leash Hitch", "Lightning", "Lingering Potion", "Llama", "Llama Spit", "Magma Cube", "Minecart", "Minecart Chest", "Minecart Command", "Minecart Furnace", "Minecart Hopper", "Minecart Mob Spawner", "Minecart Tnt", "Mule", "Mushroom Cow", "Ocelot", "Painting", "Parrot", "Pig", "Pig Zombie", "Player", "Polar Bear", "Primed Tnt", "Rabbit", "Sheep", "Shulker", "Shulker Bullet", "Silverfish", "Skeleton", "Skeleton Horse", "Slime", "Small Fireball", "Snowball", "Snowman", "Spectral Arrow", "Spider", "Splash Potion", "Squid", "Stray", "Thrown Exp Bottle", "Tipped Arrow", "Unknown", "Vex", "Villager", "Vindicator", "Weather", "Witch", "Wither", "Wither Skeleton", "Wither Skull", "Wolf", "Zombie", "Zombie Horse", "Zombie Villager"},
            optionsZh = {"云雾", "可选值2", "箭", "可选值4", "可选值5", "可选值6", "可选值7", "可选值8", "可选值9", "可选值10", "苦力怕", "可选值12", "可选值13", "物品", "可选值15", "可选值16", "可选值17", "可选值18", "可选值19", "可选值20", "可选值21", "可选值22", "可选值23", "可选值24", "可选值25", "可选值26", "可选值27", "可选值28", "可选值29", "可选值30", "可选值31", "可选值32", "可选值33", "可选值34", "可选值35", "可选值36", "物品", "可选值38", "可选值39", "药水", "可选值41", "可选值42", "可选值43", "可选值44", "可选值45", "可选值46", "可选值47", "可选值48", "可选值49", "可选值50", "可选值51", "可选值52", "可选值53", "可选值54", "可选值55", "可选值56", "僵尸", "玩家", "可选值59", "可选值60", "可选值61", "可选值62", "可选值63", "可选值64", "可选值65", "骷髅", "骷髅", "可选值68", "可选值69", "可选值70", "可选值71", "箭", "可选值73", "药水", "可选值75", "可选值76", "可选值77", "箭", "可选值79", "可选值80", "可选值81", "可选值82", "可选值83", "可选值84", "可选值85", "骷髅", "可选值87", "可选值88", "僵尸", "僵尸", "僵尸"})
    private static final String TYPE = "types";

    private Set<String> types;

    @Override
    public String getKey() {
        return "entity type";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        types = settings.getStringList(TYPE).stream()
                .map(s -> s.toUpperCase().replace(' ', '_'))
                .collect(Collectors.toSet());
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        return types.contains(target.getType().name());
    }
}
