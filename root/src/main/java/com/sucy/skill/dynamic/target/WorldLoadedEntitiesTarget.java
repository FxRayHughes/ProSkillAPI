package com.sucy.skill.dynamic.target;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Scans only the world's loaded living entities with an explicit global target cap. */
@SkillNode(key = "world loaded entities", name = "World Loaded Entities", nameZh = "世界已加载实体",
        descriptionZh = "只读取施法者世界中已加载实体；必须填写最多目标数，按阵营筛选。", container = true)
public final class WorldLoadedEntitiesTarget extends TargetComponent {
    @SkillField(kind = FieldKind.IntValue, label = "Maximum Targets", labelZh = "最多目标数",
            tooltipZh = "必填；必须在 1 到 256 之间，避免无界世界扫描。", defaultValue = "16")
    private static final String LIMIT = "limit";
    @SkillField(kind = FieldKind.ListValue, label = "Group", labelZh = "阵营",
            options = {"enemy", "ally", "both"}, optionsZh = {"敌方", "友方", "全部"}, defaultValue = "enemy")
    private static final String GROUP = "group";
    @Override public String getKey() { return "world loaded entities"; }
    @Override List<LivingEntity> getTargets(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(LIMIT)) return Collections.emptyList();
        int limit = settings.getInt(LIMIT, 0);
        if (limit < 1 || limit > 256) return Collections.emptyList();
        List<LivingEntity> found = new ArrayList<>();
        Set<UUID> seen = new LinkedHashSet<>();
        // World#getLivingEntities does not load chunks. Wall checks are intentionally
        // skipped here because long rays could force reads outside loaded chunks.
        for (LivingEntity entity : caster.getWorld().getLivingEntities()) {
            if (found.size() >= limit) break;
            if (!entity.isValid() || entity.equals(caster) || !seen.add(entity.getUniqueId())) continue;
            if (!SkillAPI.getSettings().isValidTarget(entity)) continue;
            if (!everyone && allies != SkillAPI.getSettings().isAlly(caster, entity)) continue;
            found.add(entity);
        }
        return found;
    }
    @Override void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) { }
}
