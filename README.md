## 这是什么？

一个用于 **将 HTML 代码按规则注入指定页面** 的 Halo 插件。

Halo 自带的代码注入更偏向全局场景；这个插件更适合：

- 仅在特定页面路径下注入
- 仅在特定模板 ID 下生效
- 仅对指定元素或选择器做插入 / 替换 / 移除

## 功能特性

- 支持四种注入模式：
    - `<head>`
    - `<footer>`
    - 指定元素 `ID`
    - `CSS 选择器`
- 支持可组合的匹配规则：
    - 页面路径匹配
    - 模板 ID 匹配
    - `AND` / `OR`
    - `NOT`
    - 条件组嵌套
- 支持两种规则编辑方式：
    - 简单模式
    - 高级模式（JSON）
- 支持按规则决定是否输出 `PluginInjector start/end` 注释标记
- 支持在可能带来额外处理开销的规则上显示性能警告
- 支持运行期高频 regex 复用编译结果，减少重复 `Pattern.compile`
- 支持前后端双重校验，避免非法规则落库后才在运行时“悄悄不生效”

![preview](assets/images/preview.png)

## 配置入口

管理后台 -> 工具 -> `Injector`

![code](assets/images/config_code.png)
![rule](assets/images/config_rule.png)

## 注入模式说明

插件提供四种注入模式：

| 模式       | 实现方式                  | 说明                                            |
| ---------- | ------------------------- | ----------------------------------------------- |
| `head`     | `TemplateHeadProcessor`   | 插入到 `<head>` 中                              |
| `footer`   | `TemplateFooterProcessor` | 插入到 `<halo:footer />` 中，具体位置由主题控制 |
| `id`       | `InjectorWebFilter`       | 通过元素 ID 定位并处理目标元素                  |
| `selector` | `InjectorWebFilter`       | 通过 CSS 选择器匹配并处理所有命中的元素         |

> `id` 与 `selector` 模式需要在服务端读取并处理完整 HTML，因此会额外引入响应改写成本。  
> 非必要时，优先使用 `head` / `footer` 模式。
>
> 如果匹配规则里包含“页面路径”条件，
> 插件就能先根据访问路径缩小范围，只处理少量需要改写 HTML 的页面。
> 如果没有页面路径条件，或者没有使用“页面路径 AND 模板 ID”这类组合，插件仍然会工作，
> 但元素 `ID` / `CSS 选择器` 模式会对所有 HTML 页面先进行处理，配置页也会显示性能警告。

## 注入位置选项

在 `id` 或 `selector` 模式下，可以选择以下位置策略：

- `append`：追加到目标元素内部末尾
- `prepend`：插入到目标元素内部开头
- `before`：插入到目标元素之前
- `after`：插入到目标元素之后
- `replace`：用代码块替换目标元素
- `remove`：直接移除目标元素

> `remove` 的语义是“删除整个元素节点”，不是清空内容，也不是隐藏元素。
> 因此 `remove` 模式下无需关联代码块；保存时会自动清空“关联代码块”这一项，
> 后端也会拒绝这类错误数据（对应内部字段 `snippetIds`）。
> 在管理后台中，选择 `remove` 后也不会再显示“关联代码块”选择区。

> 注入到 `<head>` 时仍需注意 HTML 合法性。  
> 例如 `<div>` 这类块级标签不会真正留在 `<head>` 中，而可能被浏览器或解析器移动到 `<body>`。

## 规则级附加选项

- 每条规则都可单独配置“注释标记”
- 开启时，会包裹 `<!-- PluginInjector start -->` 与 `<!-- PluginInjector end -->`
- 关闭后，注入内容原样输出，不额外添加注释
- 默认开启，便于排查页面中的注入来源

> `remove` 模式下不会显示“注释标记”选项。
> 因为它会直接删除目标元素，不会留下适合输出注释标记的位置；
> 保存时会自动关闭这一项，后端也会拒绝错误数据（内部字段名为 `wrapMarker`）。

## 匹配规则说明

每条注入规则都会使用一套“匹配规则”来描述“在什么情况下生效”。

如果你在接口文档或报错信息里看到 `matchRule`，它对应的就是这里的“匹配规则”。

### 简单模式

适合常见场景，支持：

- 页面路径规则
- 模板 ID 规则
- 全部满足（`AND`）
- 任一满足（`OR`）
- 取反（`NOT`）
- 条件组嵌套

### 高级模式（JSON）

适合复杂场景，可直接编辑规则树 JSON。

下面这些 `type`、`matcher`、`value` 都是 JSON 高级模式里的字段名。

根节点必须是 `GROUP`。叶子节点支持：

- `PATH`
    - `ANT`
    - `REGEX`
    - `EXACT`
- `TEMPLATE_ID`
    - `REGEX`
    - `EXACT`

#### JSON 示例

```json
{
    "type": "GROUP",
    "operator": "AND",
    "negate": false,
    "children": [
        {
            "type": "PATH",
            "matcher": "ANT",
            "value": "/posts/**",
            "negate": false
        },
        {
            "type": "GROUP",
            "operator": "OR",
            "negate": false,
            "children": [
                {
                    "type": "TEMPLATE_ID",
                    "matcher": "EXACT",
                    "value": "post",
                    "negate": false
                },
                {
                    "type": "TEMPLATE_ID",
                    "matcher": "REGEX",
                    "value": "^(post|page)$",
                    "negate": false
                }
            ]
        }
    ]
}
```

#### 编辑器行为

- JSON 语法错误或字段错误时，会提示具体字段路径
- 能定位时，会提示错误行列
- 简单模式下，正则表达式也会实时校验，不必等到保存时才发现错误
- 简单模式下，内部条件组和规则支持在同层内上移 / 下移调整顺序
- 在元素 `ID` / `CSS 选择器` 模式下：
    - 若规则里没有包含页面路径条件，会显示性能警告
    - 这类规则仍可保存和生效，只是会带来一些额外处理开销
- 当 JSON 草稿有误且切回简单模式时，会先弹出确认，避免草稿被静默覆盖
- 在高级模式下：
    - JSON 合法时可执行“格式化 JSON”
    - JSON 非法时可执行“重建 JSON”，按当前已生效规则重新生成

## 校验与性能

### 写入期校验

创建 / 更新规则时，后端会兜底校验：

- 根节点是否为 `GROUP`
- 匹配方式（`matcher`）是否与节点类型匹配
- 匹配内容（`value`）是否为空
- `REGEX` 是否可正常编译
- `REMOVE` 是否错误关联了代码块（内部字段名为 `snippetIds`）
- `REMOVE` 是否错误开启了注释标记（内部字段名为 `wrapMarker`）

这样做的目的，是避免非法规则落库，最后在运行时只表现为“没有生效”。

### 运行时性能

- 运行时不再为“结构合法性检查”重复编译 regex
- regex 匹配会缓存编译结果，避免同一规则在高频请求下重复 `Pattern.compile`
- `ANT` 风格的页面路径匹配复用统一的 `RouteMatcher`
- 若元素 `ID` / `CSS 选择器` 规则里没有包含页面路径条件，插件仍会继续工作，但会带来一些额外处理开销

## 开发环境

- Java 21+
- Node.js 18+
- pnpm

## 开发

```bash
# 构建插件
./gradlew build

# 前端开发
cd ui
pnpm install
pnpm dev
```

构建完成后，可在 `build/libs` 目录找到插件 jar。

## 许可证

[GPL-3.0](./LICENSE) © Erzbir
