package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/** Color is defined only for dyeable leather equipment in this version span. */
@SkillNode(key = "check item color", name = "Check Item Color", nameZh = "检查物品颜色",
        descriptionZh = "检查主手皮革装备的 RGB 十六进制颜色；没有颜色属性时不通过。", container = true)
public final class CheckItemColorCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "RGB", labelZh = "颜色 #RRGGBB", defaultValue = "#FFFFFF")
    private static final String COLOR = "color";
    @Override public String getKey() { return "check item color"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        if (!(target instanceof Player)) return false;
        ItemStack item = ((Player) target).getInventory().getItemInMainHand();
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof LeatherArmorMeta)) return false;
        String hex = settings.getString(COLOR, "").replace("#", "");
        if (!hex.matches("[0-9a-fA-F]{6}")) return false;
        Color actual = ((LeatherArmorMeta) meta).getColor();
        return actual != null && actual.asRGB() == Integer.parseInt(hex, 16);
    }
}
