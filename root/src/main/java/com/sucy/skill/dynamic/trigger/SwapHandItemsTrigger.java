package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;


@SkillNode(
        key = "SWAP_HAND",
        name = "Swap Hand",
        nameZh = "切换手时",
        description = "切换主副手物品时触发",
        descriptionZh = "玩家按交换键（默认 F）互换主副手物品时触发，施法者与初始目标都是这名玩家。注意 Bukkit 语义：这里的“主手物品”指换手后进入主手的那件（即原本在副手的），“副手物品”反之。写数值时会直接取换入主手物品的类型存进 api-item-type，换手后主手为空（例如原副手是空的）会在这一步抛空指针，所以建议开启 main_enable 先做非空校验。",
        container = true)
public class SwapHandItemsTrigger implements Trigger<PlayerSwapHandItemsEvent> {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "是否需要下蹲",
            labelZh = "技能节点",
            tooltip = "[sneaking] 设置为True后Shfit+F才可以执行",
            tooltipZh = "填 True 时要求玩家处于潜行状态才触发，可用来把换手键做成“Shift+F”组合键，避免误触。默认 False 不做要求。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String SNEAKING = "sneaking";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "是否取消事件",
            labelZh = "技能节点",
            tooltip = "[cancelled] 设置为True后会阻止物品真的切换，但是会释放技能",
            tooltipZh = "填 True 时取消这次换手动作（只做技能、不真的换手）。有两个坑：一是代码在键缺失时按 False 处理，而编辑器新建节点预填 True，默认值不一致；二是这行赋值写在所有判定的最后，只要 main_lore 或 off_lore 填了非 all 的值就会提前返回，导致取消设置根本执行不到。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String CANCELLED = "cancelled";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "判断主手物品",
            labelZh = "技能节点",
            tooltip = "[main_enable] 主手必须有东西才可以执行",
            tooltipZh = "填 True 时校验换手后进入主手的物品（即原副手物品）：为空或空气则不触发，并启用 main_name / main_lore 过滤。默认 False 表示不校验主手。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String MAIN_ENABLE = "main_enable";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "主手物品名",
            labelZh = "技能节点",
            tooltip = "[main_name] 物品的名称中包含某个内容 all为全部都可以",
            tooltipZh = "按换入主手物品的显示名过滤，all 表示不限；填其他值时要求显示名“包含”该片段。仅在 main_enable 为 True 时生效，比较前不去颜色代码。该键无代码级默认值，手写 YAML 漏掉会抛空指针。",
            defaultValue = "all")
    private static final String MAIN_NAME = "main_name";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "主手Lore",
            labelZh = "描述",
            tooltip = "[main_lore] 物品的描述中包含某个内容 all为全部都可以",
            tooltipZh = "按换入主手物品的 Lore 过滤，判定是反的：填 all 不限；填了别的值时，只有当所有 Lore 行都“不含”该文本时才通过（比较前去掉颜色代码），实际起排除作用。另外一旦填了非 all 的值，代码会在这里直接返回，后面的副手校验和 cancelled 取消逻辑都不会再执行。",
            defaultValue = "all")
    private static final String MAIN_LORE = "main_lore";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "判断副手物品",
            labelZh = "技能节点",
            tooltip = "[off_enable] 副手必须有东西才可以执行",
            tooltipZh = "填 True 时校验换手后进入副手的物品（即原主手物品）：为空或空气则不触发，并启用 off_name / off_lore 过滤。默认 False。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String OFF_ENABLE = "off_enable";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "副手物品名",
            labelZh = "技能节点",
            tooltip = "[off_name] 物品的名称中包含某个内容 all为全部都可以",
            tooltipZh = "按换入副手物品的显示名过滤，all 表示不限；填其他值时要求显示名“包含”该片段。仅在 off_enable 为 True 时生效，比较前不去颜色代码；无代码级默认值，手写漏掉会抛空指针。",
            defaultValue = "all")
    private static final String OFF_NAME = "off_name";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "副手Lore",
            labelZh = "描述",
            tooltip = "[off_lore] 物品的描述中包含某个内容 all为全部都可以",
            tooltipZh = "按换入副手物品的 Lore 过滤，与 main_lore 一样是反向判定：只有所有 Lore 行都不含该文本时才通过。填了非 all 的值同样会让代码提前返回，跳过后面的 cancelled 取消逻辑。",
            defaultValue = "all")
    private static final String OFF_LORE = "off_lore";


    @Override
    public String getKey() {
        return "SWAP_HAND";
    }

    @Override
    public Class<PlayerSwapHandItemsEvent> getEvent() {
        return PlayerSwapHandItemsEvent.class;
    }

    @Override
    public boolean shouldTrigger(final PlayerSwapHandItemsEvent event, final int level, final Settings settings) {
        if (settings.getBool(SNEAKING, false)) {
            if (!event.getPlayer().isSneaking()) {
                return false;
            }
        }
        if (settings.getBool(MAIN_ENABLE, false)) {
            ItemStack itemStack = event.getMainHandItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return false;
            }
            String name = settings.getString(MAIN_NAME);
            if (!name.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    return false;
                }
                String cname = meta.getDisplayName();
                if (!cname.contains(name)) {
                    return false;
                }
            }
            String lore = settings.getString(MAIN_LORE);
            if (!lore.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return false;
                }
                List<String> clone = meta.getLore();
                if (clone == null || clone.isEmpty() || !meta.hasLore()) {
                    return false;
                }
                return clone.stream().noneMatch(i ->
                        ChatColor.stripColor(i).contains(lore)
                );
            }
        }
        if (settings.getBool(OFF_ENABLE, false)) {
            ItemStack itemStack = event.getOffHandItem();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return false;
            }
            String name = settings.getString(OFF_NAME);
            if (!name.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    return false;
                }
                String cname = meta.getDisplayName();
                if (!cname.contains(name)) {
                    return false;
                }
            }
            String lore = settings.getString(OFF_LORE);
            if (!lore.equals("all")) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return false;
                }
                List<String> clone = meta.getLore();
                if (clone == null || clone.isEmpty() || !meta.hasLore()) {
                    return false;
                }
                return clone.stream().noneMatch(i ->
                        ChatColor.stripColor(i).contains(lore)
                );
            }
        }
        event.setCancelled(settings.getBool(CANCELLED, false));
        return true;
    }

    @Override
    public void setValues(final PlayerSwapHandItemsEvent event, final Map<String, Object> data) {
        data.put("api-item-type", Objects.requireNonNull(event.getMainHandItem().getType().name()));
    }

    @Override
    public LivingEntity getCaster(final PlayerSwapHandItemsEvent event) {
        return event.getPlayer();
    }

    @Override
    public LivingEntity getTarget(final PlayerSwapHandItemsEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
