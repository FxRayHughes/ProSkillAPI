# ProSkillAPI 新编辑器

独立的 React 19 + TypeScript + Vite + React Flow + Mantine 8 前端工程。
旧 `editor/` 与服务端 Java 代码不受影响。

## 启动

要求 Node.js 22.12+，pnpm 11.20.0。使用 `packageManager` 固定工具版本。

```sh
cd new-editor
pnpm install
pnpm dev
```

开发地址默认是 http://127.0.0.1:5173；端口占用时 Vite 自动选择其他端口。

## 常用命令

| 命令                                | 用途                                           |
| ----------------------------------- | ---------------------------------------------- |
| `pnpm dev`                          | 本地开发与热更新                               |
| `pnpm typecheck`                    | TypeScript 静态检查，不生成产物                |
| `pnpm lint` / `pnpm lint:fix`       | ESLint 检查 / 自动修正                         |
| `pnpm format` / `pnpm format:check` | Prettier 格式化 / 检查                         |
| `pnpm test` / `pnpm test:watch`     | Vitest 测试 / 监听                             |
| `pnpm check`                        | 顺序执行类型、Lint、格式和测试检查，不打包     |
| `pnpm build`                        | 类型检查后生成 `dist/`，仅在明确要求构建时执行 |
| `pnpm preview`                      | 预览已经生成的 `dist/`                         |

## 分包约定

```text
src/
  app/                         # 应用装配、Provider、主题、全局样式
    bootstrap.ts               # 挂载前完成节点注册，页面解析技能文件时注册表已就绪
  features/skill-editor/
    components/                # 页面与单一职责的 Mantine/React Flow 组件
    model/                     # 类型、注册表、图规则、状态 Hook
    io/                        # SkillAPI 原生格式的解析与写出
    index.ts                   # 对外公开接口
  features/workspace/
    components/                # 主页、技能管理、职业管理、目录 Provider
    model/                     # 插件目录访问、职业模型、目录上下文
  features/config-editor/
    components/                # 配置管理页、通用表单、attributes 专用编辑器
    model/                     # 保留注释的 YAML 文档读写
  features/plugins/
    builtin/                   # 随编辑器发布的内置扩展节点
    model/                     # 插件契约与本地存储
  shared/
    lib/                       # 与技能领域无关的浏览器工具
    ui/                        # 通用可访问 UI
  main.tsx                     # 唯一浏览器入口，不承载业务逻辑
```

依赖方向：`app -> features -> shared`。业务组件通过 props 接收状态和操作；
`useSkillEditor` 是单一状态所有者，`graph.ts` 的规则不依赖 React。
样式使用 CSS Modules，MantineProvider 及必要 CSS 只在入口装配。

## 注册节点

在 `src/app/bootstrap.ts` 中注册，该模块在页面挂载前执行；模块调用 feature 公开接口：

```ts
import { registerActionNode, registerEntryNode } from './features/skill-editor';

// 持久化 ID 和端口 ID 发布后不能随意重命名，否则旧工程的接线将失效。
registerActionNode({
  id: 'my-plugin:message',
  label: '发送消息',
  description: '向当前目标发送消息',
  color: '#67c7a6',
  inputs: [{ id: 'flow', label: '执行' }],
  outputs: [{ id: 'flow', label: '完成' }],
  fields: [{ key: 'message', label: '消息', type: 'text', default: 'Hello' }],
});

registerEntryNode({
  id: 'my-plugin:join',
  label: '玩家登录',
  description: '登录入口',
  color: '#df6d77',
  outputs: [{ id: 'flow', label: '执行' }],
  fields: [],
});
```

检查器支持数字、文本、布尔、单选、多选、字符串列表，以及旧版属性的基础值/每级成长。
旧版参数通过 `pnpm catalog:sync` 从仓库的 `editor/js` 提取，覆盖 153 个注册节点和 917 项旧输入定义；
同一命令还会生成内置扩展 `proskillapi-extra` 的 5 个节点（本仓库服务端新增的粒子轮廓机制，以及现有配置中出现的第三方组件），共 158 个可用节点。
`legacy.generated.json` 与 `extra-nodes.generated.json` 是生成文件，不手工编辑；测试会对比当前旧源码，防止遗漏共享参数或条件显示规则。
要让扩展节点参与原生 YAML 的导入导出，节点定义必须提供 `legacy: { name, category, container }`，详见 `PLUGIN_DEVELOPMENT.md`。
字段标题显示中文，选项的英文值与配置键保持原样；未识别的导入选项保留，避免升级后丢失配置。
条件节点可用 `nodeRegistry.register` 注册，`kind` 为 `condition`。
注册必须在挂载前完成，重复 ID、重复端口、入口输入端口会立即报错。
扩展注册的是前端定义，不会自动向 Minecraft 服务端注册执行处理器。

## 数据边界

插件目录**只在一处选择**（主页或顶栏的文件夹按钮），技能、职业、配置三个页面共用同一个目录，
选择结果存在 IndexedDB 里，下次打开自动恢复。

编辑器直接读写这个目录，**保存即生效**，不存在中间工程格式：

| 页面     | 读写位置                                | 对应服务端加载逻辑                     |
| -------- | --------------------------------------- | -------------------------------------- |
| 技能管理 | `<插件目录>/dynamic/skill/<技能名>.yml` | `RegistrationManager.loadSkills()`     |
| 职业管理 | `<插件目录>/dynamic/class/<职业名>.yml` | `RegistrationManager` 的单文件职业加载 |
| 配置管理 | `<插件目录>/*.yml`（根目录下的配置）    | 各自的 `CommentedConfig` 加载          |

- 保存写出的是原生 `name/type/attributes/components` 结构，服务端不需要任何改动即可加载。
- 未被编辑器建模的键（包括第三方 addon 写入的字段、`attributes` 里的 `incompatible` 等列表设置）原样保留，不会在保存时丢失。
- 节点坐标写在技能段的 `editor-layout` 里。`Skill.load` 与 `DynamicSkill.load` 只读取自己认识的键，因此该段既不影响服务端，也不会被插件抹掉。
- 未识别的组件会让该技能显示为「格式异常」并说明原因，编辑器不会自动改写这类文件。
- 原生 `components` 是有序嵌套树：不支持共享子树、条件节点的否定分支，根节点必须是触发器。违反时保存会明确报错而不是静默丢弃，详见 `PLUGIN_DEVELOPMENT.md`。
- 同级节点的执行顺序按画布纵坐标从上到下排列。

`skills.yml` / `classes.yml` 是服务端首次迁移用的聚合文件（带 `loaded: true` 后会被跳过），编辑器不读写它们。

### 配置文件与注释

`config.yml`、`attributes.yml`、`groups.yml` 这些配置是手工维护的，**注释就是文档**。
因此配置管理页不做 parse/dump 往返（那会删掉全部注释），而是用 `yaml` 库的 AST 就地改值：

- 表单**由文件自身的结构生成**，每一项的 YAML 注释直接作为该项的说明文字。插件以后新增设置会自动出现，不需要改前端。
- 控件按值的形态推断：`'true'/'false'` → 开关，数字字符串 → 数字框，标量列表 → 每行一条的文本域，其余为文本框。
  写回时统一写成带引号的字符串，与插件 `getString(...).equalsIgnoreCase` 的读法一致。
- 值为映射的序列（如 `set-bonus.yml` 的部分结构）没有安全的通用表单，不渲染，保存时原样保留。
- `attributes.yml` 另有专用编辑器：属性列表增删改名、stat-key 与公式补全（`a` = 当前基础值，`v` = 投入点数）。
  条目里未建模的键（例如 `global` 块）原样保留。

测试用真实配置逐一验证：写出的文件必须包含原文件的每一行注释，且数据完全一致。

撤销历史与自动保存尚未实现，编辑状态在内存中，切换技能前需先保存。

## 参考

- https://mantine.dev/llms/guides-llms.md
- https://mantine.dev/llms/getting-started.md
- https://reactflow.dev/
