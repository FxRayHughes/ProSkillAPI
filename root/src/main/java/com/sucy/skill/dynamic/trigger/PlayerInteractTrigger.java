package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;


@SkillNode(
        key = "INTERACT",
        name = "Interact",
        nameZh = "交互时",
        description = "这个触发器是玩家在手持物品进行操作的时候触发的",
        descriptionZh = "玩家手持物品交互时触发（左右键点击空气或方块、以及压力板之类的 PHYSICAL 动作），施法者与初始目标都是这名玩家。空手交互一律不触发。命中物品的材质写入 api-item-type。已被其他插件取消的交互不触发。",
        container = true)
public class PlayerInteractTrigger implements Trigger<PlayerInteractEvent> {
    @SkillField(
            kind = FieldKind.StringListValue,
            label = "动作",
            labelZh = "技能节点",
            tooltip = "[action] 每个动作以;结尾进行区分",
            tooltipZh = "允许触发的动作列表，需与 Bukkit 的 Action 枚举名完全一致且区分大小写（LEFT_CLICK_AIR、RIGHT_CLICK_BLOCK、PHYSICAL 等）。这里不支持留空当通配：列表为空或没有匹配项就永不触发，所以想全收就得把用到的动作逐个列出。",
            defaultValue = "LEFT_CLICK_AIR,LEFT_CLICK_BLOCK,PHYSICAL,RIGHT_CLICK_AIR,RIGHT_CLICK_BLOCK")
    private static final String ACTION = "action";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "物品名",
            labelZh = "技能节点",
            tooltip = "[name] 物品的名称中包含某个内容 all为全部都可以",
            tooltipZh = "按物品显示名过滤，all 表示不限；填其他值时要求显示名“包含”该片段（子串匹配，不是全等）。比较前不去颜色代码，因此带 & 颜色的名字要连颜色一起考虑或只填不含颜色的片段。该键没有代码级默认值，手写 YAML 时漏掉会抛空指针，编辑器会预填 all。",
            defaultValue = "all")
    private static final String NAME = "name";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Lore",
            labelZh = "物品描述",
            tooltip = "[lore] 物品的描述中包含某个内容 all为全部都可以",
            tooltipZh = "按 Lore 过滤，但判定是反的：填 all 表示不限；一旦填了别的值，只有当所有 Lore 行都“不含”该文本时才通过（比较前会去掉颜色代码），也就是它实际起的是排除作用而非包含作用。而且这条判定会直接决定返回值，写了非 all 的值就等于用它一票定生死。该键无代码级默认值，手写漏掉会抛空指针。",
            defaultValue = "all")
    private static final String LORE = "lore";


    @Override
    public String getKey() {
        return "INTERACT";
    }

    @Override
    public Class<PlayerInteractEvent> getEvent() {
        return PlayerInteractEvent.class;
    }

    @Override
    public boolean shouldTrigger(final PlayerInteractEvent event, final int level, final Settings settings) {
        ItemStack itemStack = event.getItem();
        if (!event.hasItem() || itemStack == null) {
            return false;
        }
        List<String> list = settings.getStringList(ACTION);
        if (!list.contains(event.getAction().name())) {
            return false;
        }
        String name = settings.getString(NAME, "");
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
        String lore = settings.getString(LORE, "");
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
        return true;
    }

    @Override
    public void setValues(final PlayerInteractEvent event, final Map<String, Object> data) {
        data.put("api-item-type", Objects.requireNonNull(event.getItem()).getType().name());
    }

    @Override
    public LivingEntity getCaster(final PlayerInteractEvent event) {
        return event.getPlayer();
    }

    @Override
    public LivingEntity getTarget(final PlayerInteractEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
