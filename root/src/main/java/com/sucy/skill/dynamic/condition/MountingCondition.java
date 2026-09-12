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
        key = "mounting",
        name = "Mounting",
        nameZh = "检查作为坐骑",
        description = "Applies child elements if the target is mounting one of the selected entity types",
        descriptionZh = "检查目标是否正骑在别的实体上（目标是骑乘的一方）。必须处于载具中；类型列表留空或包含“Any”时任意载具都算通过，否则载具类型需在列表内。",
        container = true)
public class MountingCondition extends ConditionComponent {

    @SkillField(
            kind = FieldKind.MultiListValue,
            label = "Types",
            labelZh = "类型列表",
            tooltip = "[types] The entity types the target can be mounting",
            tooltipZh = "允许的载具实体类型列表。留空或含“Any”表示不限类型；名称转大写并把空格替换为下划线后与 EntityType 枚举名比对。",
            options = {"Any", "Area Effect Cloud", "Armor Stand", "Arrow", "Bat", "Blaze", "Boat", "Cave Spider", "Chicken", "Complex Part", "Cow", "Creeper", "Donkey", "Dragon Fireball", "Dropped Item", "Egg", "Elder Guardian", "Ender Crystal", "Ender Dragon", "Ender Pearl", "Ender Signal", "Enderman", "Endermite", "Evoker", "Evoker Fangs", "Experience Orb", "Falling Block", "Fireball", "Firework", "Fishing Hook", "Ghast", "Giant", "Guardian", "Horse", "Husk", "Illusioner", "Iron Golem", "Item Frame", "Leash Hitch", "Lightning", "Lingering Potion", "Llama", "Llama Spit", "Magma Cube", "Minecart", "Minecart Chest", "Minecart Command", "Minecart Furnace", "Minecart Hopper", "Minecart Mob Spawner", "Minecart Tnt", "Mule", "Mushroom Cow", "Ocelot", "Painting", "Parrot", "Pig", "Pig Zombie", "Player", "Polar Bear", "Primed Tnt", "Rabbit", "Sheep", "Shulker", "Shulker Bullet", "Silverfish", "Skeleton", "Skeleton Horse", "Slime", "Small Fireball", "Snowball", "Snowman", "Spectral Arrow", "Spider", "Splash Potion", "Squid", "Stray", "Thrown Exp Bottle", "Tipped Arrow", "Unknown", "Vex", "Villager", "Vindicator", "Weather", "Witch", "Wither", "Wither Skeleton", "Wither Skull", "Wolf", "Zombie", "Zombie Horse", "Zombie Villager"},
            optionsZh = {"任意", "云雾", "可选值3", "箭", "可选值5", "可选值6", "可选值7", "可选值8", "可选值9", "可选值10", "可选值11", "苦力怕", "可选值13", "可选值14", "物品", "可选值16", "可选值17", "可选值18", "可选值19", "可选值20", "可选值21", "可选值22", "可选值23", "可选值24", "可选值25", "可选值26", "可选值27", "可选值28", "可选值29", "可选值30", "可选值31", "可选值32", "可选值33", "可选值34", "可选值35", "可选值36", "可选值37", "物品", "可选值39", "可选值40", "药水", "可选值42", "可选值43", "可选值44", "可选值45", "可选值46", "可选值47", "可选值48", "可选值49", "可选值50", "可选值51", "可选值52", "可选值53", "可选值54", "可选值55", "可选值56", "可选值57", "僵尸", "玩家", "可选值60", "可选值61", "可选值62", "可选值63", "可选值64", "可选值65", "可选值66", "骷髅", "骷髅", "可选值69", "可选值70", "可选值71", "可选值72", "箭", "可选值74", "药水", "可选值76", "可选值77", "可选值78", "箭", "可选值80", "可选值81", "可选值82", "可选值83", "可选值84", "可选值85", "可选值86", "骷髅", "可选值88", "可选值89", "僵尸", "僵尸", "僵尸"})
    private static final String TYPE = "types";

    private Set<String> types;

    @Override
    public String getKey() {
        return "mounting";
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
        return target.isInsideVehicle() && (types.isEmpty() || types.contains("ANY") || types.contains(target.getVehicle().getType().name()));
    }
}
