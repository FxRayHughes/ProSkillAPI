# Fabled 功能差异研究

## 对比范围

- 本仓库：`master`，`5f34932c090b4b555fb97ad81d91854749e5a3d4`。
- Fabled：[`magemonkeystudio/fabled` 的 `dev` 分支](https://github.com/magemonkeystudio/fabled/tree/a1e556ed86b8d16bce27272d43ea7c4f57adc981)，`a1e556ed86b8d16bce27272d43ea7c4f57adc981`。
- 统计方法：读取两边 `ComponentRegistry` 中实际静态注册的节点，按 Java 类名比较；另检查相关实现和独立子系统。类名差异只能提示调查方向，不能直接当作用户能力差异。
- 这是静态源码研究，未运行 Fabled 或当前插件，也没有做配置兼容性实测。

## 本轮规划状态

| 范围 | 状态 | 说明 |
| --- | --- | --- |
| 事件入口 | 部分完成 | 已加入阶段、结果和命中对象粒度；抛射物 tick、装备变更、剥离原木等高频或版本差异入口仍未实现。 |
| 条件与数值 | 部分完成 | 已加入结构化比较、区间、距离、护盾、信号和数据读取；数学表达式、旋转/文本等能力仍复用旧脚本或已有近似节点。 |
| 信号编排 | 部分完成 | 已支持参数契约推导、多个接收根、跨技能延迟校验、循环限制和限流；世界/服务器广播已在枚举阶段限制候选数量，仍需要压力测试和超限诊断指标。 |
| 共享组与连线 | 部分完成 | 已支持 `run group` 动作和连线到共享组卡片；原生 v1 树仍不支持否定条件分支，复杂图必须先通过导出校验。 |
| 护盾 | 部分完成 | 已有独立多层资源、吸收过滤、优先级、生命周期和公开事件；已避免在最终伤害被后续监听器改写时提交错误扣盾，尚未完成跨版本伤害修饰器矩阵、并发变更测试和可选显示层。 |
| 编辑器结构化字段 | 部分完成 | 当前所有已注册 MapValue 字段都使用行式可视化控件；仍需补充导入异常、嵌套扩展字段和插件自定义 MapValue 的兼容策略。 |
| 交互 API、Action Bar、轮选技能 | 排除 | 按产品决策不吸收这部分能力。 |
| 编辑器自动保存 | 部分完成 | 设置页支持周期和编辑后防抖保存，编辑器顶部可切换；已加入串行写入和外部文件签名冲突检测，仍缺崩溃恢复草稿和多标签协调。 |

| 已注册节点 | Fabled | 本仓库 | Fabled 独有类名 |
| --- | ---: | ---: | ---: |
| 触发器 | 50 | 55 | 37 |
| 目标选择器 | 10 | 17 | 1 |
| 条件 | 55 | 60 | 16 |
| 机制 | 99 | 128 | 24 |
| 合计 | 214 | 260 | 78 |

来源：[Fabled 注册表](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/ComponentRegistry.java#L38-L259)、[本仓库注册表](../../root/src/main/java/com/sucy/skill/dynamic/ComponentRegistry.java)。本仓库额外注册了 30 个 Fabled 没有的类名，故表中的差额不等于净新增能力。

上表的“本仓库”数字来自当前 `ComponentRegistry` 的静态注册调用（55/17/60/128），只代表目录可见性，不代表每个节点都已经在所有支持的 Bukkit 版本和真实服场景中验证。后续验收必须同时记录注册数、运行时探针覆盖数和跨版本结果。

## 当前未完成清单

- **运行时事件**：`ProjectileTick`、装备变更、原木剥离、钓鱼落地阶段和通用属性外部变更没有独立入口；是否加入要先确定事件频率、版本适配和监听开销。
- **广播性能**：`world`/`server` 信号已在枚举阶段限制每条调用的候选实体数量，避免先构建全量实体列表；仍需要在真实服做大实体量压力测试，并决定是否公开“因预算截断”的诊断结果。
- **信号命名空间**：契约表按整个服务器进程共享 `channel`，不同技能或插件重名会直接冲突；需要在 API 固化前决定全局命名约定，或改为插件/技能命名空间隔离。
- **护盾验收**：已处理取消事件和最终伤害改写时的保守提交边界；仍缺少伤害修饰器、外部伤害处理器和真实伤害开关的组合矩阵，目前只有有限真实服探针。
- **编辑器恢复**：自动保存已有串行队列和外部文件变更检测；仍缺崩溃恢复草稿和多标签锁，这些属于交付前的可靠性工作，不应继续宣称自动保存“完全覆盖”。
- **测试资产**：前端有回归测试，服务端新增节点仍主要依赖编译和真实服探针，缺少可重复的 Java 单元/集成测试清单。

## 交付前验收门槛

| 优先级 | 事项 | 通过条件 |
| --- | --- | --- |
| P1 | 信号广播 | 在至少一个高实体量真实服中确认单次 `world/server` 调用只枚举预算内候选；记录投递数、截断行为和主线程耗时。 |
| P1 | 护盾结算 | 覆盖取消事件、后续插件改伤害、外部伤害处理器、真实伤害两种策略、多层排序/比例/单次上限、过期/死亡/卸载。 |
| P1 | 编辑器保存 | 已完成串行化并发写入和外部文件变更保护；仍需重启后恢复最近草稿或明确提示无法恢复。 |
| P1 | 服务端回归 | 为信号契约、跨技能加载顺序、护盾结算和事件入口建立可重复的 Java 或 Paper 探针清单，并记录 Bukkit/Paper 版本。 |
| P2 | 信号命名空间 | 明确 `channel` 的全局命名约定或隔离边界，并验证同名不同契约的错误信息和兼容策略。 |
| P2 | 事件扩展 | 对 `ProjectileTick`、装备变更、`StripLog`、`FishingGround`、外部属性变化逐项做频率、版本和收益评估，决定实现或明确排除。 |
| P2 | 图导出 | 对多父节点、共享组、循环和否定条件在导出前给出可见诊断，并固定 GROUP 嵌套/递归及失败语义。 |
| P2 | 跨版本 | 至少在项目声明的最低版本、当前主流版本和最高目标版本验证事件、`getFinalDamage`、粒子 AST 和字段导入导出。 |

## 值得吸收的能力

### 1. 更多事件入口

Fabled 将技能升级/降级、Flag 生效/过期、世界切换、冲刺、滑翔、物品消耗/丢弃、抛射物命中/每 tick，以及钓鱼的多个阶段作为技能触发器。它还把技能释放和自定义信号转成触发器。钓鱼阶段拆分尤其适合事件驱动技能设计。[注册清单](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/ComponentRegistry.java#L39-L89)

完整的 Fabled 独有触发器类名：`Air`、`ArmorEquip`、`AttributeChange`、`Chat`、`ClickLeft`、`ClickRight`、`Consume`、`DropItem`、`EntityResurrect`、`EntityTarget`、`Experience`、`FishingBite`、`FishingFail`、`FishingFish`、`FishingGrab`、`FishingGround`、`FishingReel`、`Flag`、`FlagExpire`、`FlightToggle`、`Glide`、`Harvest`、`Heal`、`ItemSwap`、`Jump`、`ProjectileHit`、`ProjectileTick`、`Riptide`、`Shear`、`Shield`、`SkillUpgrade`、`SkillDowngrade`、`Signal`、`Sprint`、`SkillCast`、`StripLog`、`WorldChange`（各名称后省略 `Trigger`）。

其中 `ClickLeft`/`ClickRight` 与本仓库的 `PlayerInteractTrigger`、`ItemSwap` 与 `SwapHandItemsTrigger` 部分重叠。应先改进现有节点的配置与编辑器表达，而不是仅为了类名新增重复节点。

### 2. 更细的条件和数值节点

独有条件类名：`ActionBar`、`Air`、`Altitude`、`AttackIndicator`、`Blocking`、`Burning`、`Color`、`Distance`、`Glide`、`Money`、`Moon`、`MythicMobType`、`Sprint`、`ValueText`、`World`、`Yaw`（各名称后省略 `Condition`）。其中世界、距离、冲刺、文本值比较较通用；金钱与 MythicMobs 条件应保持可选依赖。[Fabled 条件注册](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/ComponentRegistry.java#L103-L158)

独有机制类名：`AbortSkill`、`AirModify`、`AirSet`、`ArmorStandRemove`、`Experience`、`Fly`、`Invisibility`、`ItemDrop`、`Mine`、`Money`、`Mount`、`MythicMobSkill`、`ParticleImage`、`Shield`、`SignalEmit`、`SkillCast`、`Stat`、`Summon`、`Throw`、`ValueDivide`、`ValueLoad`、`ValueMath`、`ValueRotation`、`ValueRound`（各名称后省略 `Mechanic`，`MythicMobSkill` 原名不变）。`ValueMath` 提供受控表达式求值，本仓库已有脚本节点，值得考虑以现有公式模块实现更简单、更稳定的数学节点。[Fabled 机制注册](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/ComponentRegistry.java#L160-L259)

Fabled 另有 `WorldTarget`，选取施法者所在世界的生物；这类全世界扫描必须有数量上限和性能约束。[实现](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/target/WorldTarget.java)

### 3. 技能之间的信号与编排

`SignalEmitMechanic` 发出带名称、目标和参数的事件，`SignalTrigger` 按名称接收并把参数写进施法上下文；`SkillCastMechanic` 可从技能节点触发其他技能。这使配置能描述跨技能协作。[发送](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/mechanic/SignalEmitMechanic.java)、[接收](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/trigger/SignalTrigger.java)、[技能施放](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/dynamic/mechanic/SkillCastMechanic.java)

独立实现时，应复用本仓库现有施法上下文，定义参数类型与生命周期，并限制同步递归深度、循环触发和事件数量；不要直接照搬 Fabled 的并行参数处理。

### 4. 吸收伤害的护盾

Fabled 有独立的 `shield` 子系统：可按分类给实体叠加定时护盾，按比例吸收伤害，触发受击/破盾事件，并通过 Action Bar、Boss Bar 等显示余量。这比普通临时属性加成更像一个独立战斗资源。[护盾管理](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/shield/ShieldManager.java)、[吸收与显示](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/shield/ShieldEffect.java)

它依赖 Fabled 的 `CodexCore` 伤害扩展接口。本仓库应从自己的伤害结算入口设计护盾，先明确物理/技能/真实伤害的吸收顺序及卸载清理，再做显示层。

### 5. 施法交互与扩展 API（排除项）

Fabled 有滚轮选技能、Action Bar 文字施法栏和技能预览。本项目明确不采用交互 API、Action Bar 施法栏或轮选技能状态机；现有技能栏与粒子指示器继续按本项目自己的交互设计演进。

它另增加冷却变化、连招步骤、降级、属性变化、最大法力变化、动态触发、增益结束、推力、抛射物生命周期等公开事件，以及带 UUID、运算方式和“切换职业/世界后保留”标记的属性/统计值修饰器。这个标记不代表已写入数据库。当前仓库已有部分近似事件与临时属性机制，应按现有 API 契约增量扩展。[Fabled 事件目录](https://github.com/magemonkeystudio/fabled/tree/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/api/event)、[修饰器](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/java/studio/magemonkey/fabled/api/player/PlayerStatModifier.java)

可选集成方面，Fabled 有 Mimic、Divinity、ProtocolLib 桥接。本仓库已有 MythicMobs、Vault、PlaceholderAPI 等相关集成，不能将插件名差异直接算成功能缺失。[Fabled 插件声明](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/src/main/resources/plugin.yml#L1-L20)

## 吸收顺序建议

1. **先做低耦合节点。** 优先选择世界/距离/冲刺条件，技能升级、消耗物品、世界切换等触发器，以及基于现有公式能力的数值运算。每个节点同时补全注册、`@SkillNode`/`@SkillField` 元数据、配置默认值和中文说明。
2. **再做信号编排。** 先定义事件参数和递归边界，再添加发送/接收节点与跨技能施放；保留当前技能配置格式，单独设计 Fabled 配置导入器才考虑互通。
3. **独立设计护盾。** 护盾接入本仓库伤害流水线；交互 API、Action Bar 施法栏和轮选技能不纳入吸收范围。
4. **按需求加可选依赖。** 经济、Mimic/Divinity、图片粒子与高版本事件在对应服务端版本和插件存在时启用。`WorldTarget` 要先有扫描上限和目标过滤策略。

Fabled 使用 Java 14 编译，插件声明 API 版本 1.16/1.19，并依赖 CodexCore；本仓库主模块仍以 Java 8 兼容方式编译且拆分多个 NMS 版本。源码不能直接移植，尤其是高版本 Bukkit 事件和外部框架接口。[Fabled POM](https://github.com/magemonkeystudio/fabled/blob/a1e556ed86b8d16bce27272d43ea7c4f57adc981/pom.xml#L20-L70)、[本仓库 Gradle 配置](../../root/build.gradle.kts)

## 本仓库已有而 Fabled 没有同名注册的节点

`PlayerInteractTrigger`、`SwapHandItemsTrigger`、`NearestMultiTarget`、三个仇恨目标选择器、`DataCondition`、`DataSetMechanic`/`DataEditMechanic`、`KetherMechanic`/`JavaScriptMechanic`、更多粒子几何节点、`MythicCastMechanic`/`MythicCastTargetMechanic` 等。因此吸收工作应围绕具体使用场景和配置体验，不以类名数量为目标。

Fabled 和本仓库均声明 MIT 许可证；本研究只记录功能和设计取舍，没有复制 Fabled 代码。若未来引入其实际代码，应遵守原项目的版权与许可证声明。
