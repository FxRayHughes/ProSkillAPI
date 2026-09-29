package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.signal.SignalManager;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

/**
 * Emits one contract-validated signal to a bounded set of loaded recipients.
 *
 * <p>The sender's {@code arguments} map is the source of truth for new
 * configurations: scalar values infer the channel contract and values in
 * braces resolve against the current caster's cast data. The manager rejects
 * missing, extra, or non-convertible arguments before dispatch, so a malformed
 * signal cannot partially update a recipient's context. World and server
 * scopes only inspect already loaded entities and are bounded by the manager's
 * delivery budget.</p>
 */
@SkillNode(key = "signal emit", name = "Signal Emit", nameZh = "发送信号",
        descriptionZh = "按作用域向已加载实体投递一次信号，允许其它技能的接收起点响应。参数契约由参数行的文本、数字或布尔类型自动推导；旧技能仍可保留 contract 字段。")
public final class SignalEmitMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Channel", labelZh = "频道", defaultValue = "signal")
    private static final String CHANNEL = "channel";
    @SkillField(kind = FieldKind.MapValue, label = "Contract", labelZh = "参数契约（自动）",
            tooltipZh = "由 arguments 的值类型自动推导；仅用于兼容旧配置，编辑器中只读展示。")
    private static final String CONTRACT = "contract";
    @SkillField(kind = FieldKind.ListValue, label = "Scope", labelZh = "投递范围",
            options = {"self", "target", "world", "server"},
            optionsZh = {"自身", "当前目标", "当前世界", "本服务器"}, defaultValue = "self")
    private static final String SCOPE = "scope";
    @SkillField(kind = FieldKind.MapValue, label = "Arguments", labelZh = "参数",
            tooltipZh = "填写与契约同名的值；{键} 引用施法数据，缺值将拒绝投递。")
    private static final String ARGUMENTS = "arguments";

    /** @return legacy component key used in the v1 skill tree */
    @Override public String getKey() { return "signal emit"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String channel = settings.getString(CHANNEL, "");
        try {
            Map<String, Object> values = SignalManager.arguments(caster, channel,
                    settings.getObj(ARGUMENTS, level));
            return SignalManager.emit(caster, targets, channel,
                    settings.getString(SCOPE, "self"), values);
        } catch (IllegalArgumentException exception) {
            // A malformed cast cannot tear down Bukkit's event loop or leak context.
            org.bukkit.Bukkit.getLogger().warning("Signal '" + channel + "' rejected: " + exception.getMessage());
            return false;
        }
    }
}
