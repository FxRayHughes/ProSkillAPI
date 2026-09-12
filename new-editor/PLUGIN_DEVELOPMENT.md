# ProSkillAPI 插件节点开发

插件使用声明式 JSON 文件注册技能节点，不执行 JavaScript，也不加载外部代码。

## 导入方式

打开「插件管理」，点击「导入插件 JSON」。导入后插件会保存到浏览器本地存储，刷新页面会自动恢复。

## 插件结构

```json
{
  "id": "example-plugin",
  "name": "示例插件",
  "version": "1.0.0",
  "description": "自定义技能节点",
  "overrides": ["MechanicDamage"],
  "nodes": []
}
```

`id`、节点 ID、端口 ID 和字段 key 是持久化协议，发布后不要随意修改。`overrides` 中的节点 ID 会覆盖已有节点定义，覆盖只改变编辑器定义、表单和展示信息，不会替换 Minecraft 服务端执行器。

## 节点定义

```json
{
  "id": "example:send-message",
  "label": "发送消息",
  "kind": "action",
  "description": "向当前目标发送消息",
  "color": "#67c7a6",
  "inputs": [{ "id": "flow", "label": "执行" }],
  "outputs": [{ "id": "flow", "label": "完成" }],
  "fields": [{ "key": "message", "label": "消息内容", "type": "text", "default": "你好" }]
}
```

`kind` 可以是 `entry`、`action` 或 `condition`。入口节点不能有输入端口。条件节点通常提供 `true` 和 `false` 两个输出端口。

## 字段类型

| 类型          | 说明                 | 额外属性                               |
| ------------- | -------------------- | -------------------------------------- |
| `text`        | 单行文本、公式、命令 | `default` 为字符串                     |
| `number`      | 整数或小数           | `default` 为数字，可加 `integer: true` |
| `boolean`     | 开关                 | `default` 为布尔值                     |
| `select`      | 单选下拉框           | `options: [{ value, label }]`          |
| `multiselect` | 多选下拉框           | `default` 为字符串数组                 |
| `string-list` | 每行一个值           | `default` 为字符串数组                 |

示例：

```json
{
  "key": "amount",
  "label": "伤害值",
  "type": "number",
  "default": 10,
  "integer": false,
  "tooltip": "支持表达式，例如 skill-level * 2"
}
```

选项的 `value` 永远是英文协议值，`label` 是界面显示值：

```json
{
  "key": "target",
  "label": "目标阵营",
  "type": "select",
  "default": "Enemy",
  "options": [
    { "value": "Enemy", "label": "敌方" },
    { "value": "Ally", "label": "友方" }
  ]
}
```

## 覆盖已有节点

```json
{
  "id": "balance-plugin",
  "name": "技能平衡调整",
  "version": "2.0.0",
  "overrides": ["MechanicDamage"],
  "nodes": [
    {
      "id": "MechanicDamage",
      "label": "造成伤害（平衡版）",
      "kind": "action",
      "description": "调整后的伤害节点",
      "color": "#ef8f5b",
      "inputs": [{ "id": "flow", "label": "执行" }],
      "outputs": [{ "id": "flow", "label": "完成" }],
      "fields": []
    }
  ]
}
```

覆盖定义必须保留原节点 ID 和必要端口，否则旧工程中的接线无法恢复。卸载插件不会自动把旧定义写回当前已打开画布；重新加载页面或重新导入工程即可恢复基础定义。

## 与原生 YAML 互通

编辑器直接读写 SkillAPI 原生技能文件（`dynamic/skill/<技能名>.yml`）。技能文件里的组件是靠**英文组件名 + 分类**来识别的，所以插件节点必须提供 `legacy` 才能参与导入导出：

```json
{
  "id": "MechanicParticleRing",
  "label": "粒子圆环",
  "kind": "action",
  "description": "沿圆周均匀绘制粒子轮廓",
  "color": "#e5b35d",
  "inputs": [{ "id": "flow", "label": "执行" }],
  "outputs": [{ "id": "flow", "label": "执行" }],
  "fields": [],
  "legacy": { "name": "Particle Ring", "category": "mechanic", "container": false }
}
```

| 字段               | 说明                                                                  |
| ------------------ | --------------------------------------------------------------------- |
| `legacy.name`      | 服务端 `getKey()` 返回的组件名，大小写不敏感，例如 `Particle Ring`    |
| `legacy.category`  | `trigger`、`target`、`condition` 或 `mechanic`，对应 YAML 里的 `type` |
| `legacy.container` | 该组件是否可以有子节点                                                |

没有 `legacy` 的节点只能在编辑器内使用：打开含有该组件的技能文件会报「未知组件」，保存时也无法写出对应的组件名。

编辑器还会在技能段里写入一个 `editor-layout` 段来记录节点坐标。SkillAPI 的 `Skill.load` 与 `DynamicSkill.load` 只读取自己认识的键，所以这个段不会影响服务端加载，也不会被插件覆盖掉。

### 原生格式的结构限制

原生 `components` 是有序嵌套树，不是任意有向图。以下结构无法写出，保存时会明确报错而不是静默丢弃：

- 一个节点被多个父节点连接（不支持共享子树，请复制节点）
- 条件节点使用了「不满足」分支（原生条件只有满足时执行的 `children`，请改用取反条件）
- 非触发器节点没有上级连接（技能的根节点必须是触发器）

同级节点的执行顺序按画布上的纵坐标从上到下排列。

## 内置扩展

`proskillapi-extra` 是随编辑器一起发布的内置扩展，提供本仓库服务端新增的粒子轮廓机制（`Particle Ring`、`Particle Polygon`、`Particle Point Line`），以及现有技能配置中出现的第三方组件（`Dash`、`Threat Lowest`）。后两个在本仓库里没有服务端实现，字段是由现有技能配置反推的。

内置扩展在导入的插件之前注册，所以用相同节点 ID 导入插件即可覆盖它。

## 注册接口

需要在源码集成节点时，可以使用：

```ts
import { registerActionNode, registerEntryNode } from './features/skill-editor';
```

JSON 插件适合普通用户和服务器作者；源码注册适合随前端一起发布的内置扩展。

## 安全与兼容性

- JSON 只描述节点、字段和端口，不执行脚本。
- 导出文件始终使用英文节点 ID、字段 key 和选项 value。
- 中文只用于界面显示。
- 节点 ID 和端口 ID 一旦发布，不建议修改。
- 插件不能自动注册服务端执行逻辑，服务端仍需要对应的 SkillAPI 执行实现。
