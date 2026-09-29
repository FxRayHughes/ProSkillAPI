package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.event.block.BlockBreakEvent;

/** Restricts the existing break event to crop or plant blocks that can be harvested. */
@SkillNode(key = "BLOCK_HARVESTED", name = "Block Harvested", nameZh = "收获方块时",
        descriptionZh = "玩家破坏可收获作物或植物时触发；保护插件取消破坏后不会触发。材质过滤仍沿用方块破坏入口的规则。", container = true)
public final class BlockHarvestedTrigger extends BlockBreakTrigger {
    @Override public String getKey() { return "BLOCK_HARVESTED"; }
    @Override public boolean shouldTrigger(BlockBreakEvent event, int level, com.sucy.skill.api.Settings settings) {
        if (!super.shouldTrigger(event, level, settings)) return false;
        String type = event.getBlock().getType().name();
        return type.endsWith("_CROP") || type.endsWith("_STEM") || type.endsWith("_BUSH")
                || type.equals("SUGAR_CANE") || type.equals("CACTUS") || type.equals("NETHER_WART")
                || type.equals("WHEAT") || type.equals("CARROTS") || type.equals("POTATOES")
                || type.equals("BEETROOTS") || type.equals("COCOA");
    }
}
