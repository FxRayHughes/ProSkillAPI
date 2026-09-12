package com.sucy.skill.dynamic.mechanic;

import com.rit.sucy.version.VersionManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.DurabilityMechanic
 */
@SkillNode(
        key = "durability",
        name = "Durability",
        nameZh = "耐久",
        description = "Lowers the durability of a held item",
        descriptionZh = "修改施法者手持物品的耐久，与目标列表无关（目标数量只用来放大改动量）。施法者不是玩家时返回 false；手上没物品、或物品本身没有耐久条时也返回 false。注意实际运算是「损伤值减去 amount」，所以填正数是在修复物品、填负数才是磨损，与「Lowers the durability」的字面描述相反。物品被彻底损坏时清空该手的槽位并播放损坏音效。")
public class DurabilityMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] Amount to reduce the item's durability by",
            tooltipZh = "耐久改动量，默认 1，随技能等级缩放，并且会再乘以目标数量。正数减少损伤值（即修复物品），负数才增加损伤（磨损）。")
    private static final String AMOUNT  = "amount";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Offhand",
            labelZh = "副手",
            tooltip = "[offhand] Whether or not to apply to the offhand slot",
            tooltipZh = "为 true 时作用于副手物品，需要服务端至少为 1.9；低版本会自动退回主手。损坏时播放的音效名也按版本区分（1.9+ 用 entity.item.break，旧版用 item.break）。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String OFFHAND = "offhand";

    @Override
    public String getKey() {
        return "durability";
    }

    @Override
    public boolean execute(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        if (!(caster instanceof Player)) {
            return false;
        }

        final Player player = (Player) caster;
        final boolean offhand = settings.getBool(OFFHAND, false);
        final short amount = (short) (parseValues(caster, AMOUNT, level, 1) * targets.size());

        final ItemStack item;
        if (offhand && VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {
            item = player.getInventory().getItemInOffHand();
        } else { item = player.getInventory().getItemInHand(); }

        if (item == null || item.getType().getMaxDurability() == 0) {
            return false;
        }

        int durability = item.getType().getMaxDurability() - item.getDurability();
        if (durability <= -amount) {
            if (offhand && VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {
                player.getInventory().setItemInOffHand(null);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            // The namespaced enum constant only exists in newer Bukkit APIs; the
            // string overload keeps the plugin loadable on 1.8-1.12 as well.
            player.playSound(player.getLocation(),
                    VersionManager.isVersionAtLeast(VersionManager.V1_9_0) ? "entity.item.break" : "item.break", 1, 1);
        }
        item.setDurability((short) (item.getDurability() - amount));
        return true;
    }
}
