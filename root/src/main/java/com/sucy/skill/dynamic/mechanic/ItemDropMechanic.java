package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Drops a concrete item at target positions; invalid version-specific materials fail closed. */
@SkillNode(key = "item drop", name = "Item Drop", nameZh = "掉落物品",
        descriptionZh = "在目标位置生成物品实体；材质不存在时拒绝执行。")
public final class ItemDropMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Material", labelZh = "材质", defaultValue = "STONE")
    private static final String MATERIAL = "material";
    @SkillField(kind = FieldKind.IntValue, label = "Amount", labelZh = "数量", defaultValue = "1")
    private static final String AMOUNT = "amount";
    @Override public String getKey() { return "item drop"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        Material material = Material.matchMaterial(settings.getString(MATERIAL, "STONE"));
        int amount = settings.getInt(AMOUNT, 1);
        if (material == null || amount < 1 || amount > 64 || targets.isEmpty()) return false;
        for (LivingEntity target : targets)
            target.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(material, amount));
        return true;
    }
}
