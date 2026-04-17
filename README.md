## 这是什么?

一个用于 **按规则将 HTML 代码注入指定页面** 的 Halo 插件

相比 Halo 的默认的全局注入功能, 本插件支持 "代码片段 + 注入规则" 组合管理, 可精细控制注入范围与位置,
可根据注入规则在指定页面的指定位置注入指定代码

![preview](assets/images/preview.png)

界面入口: Halo 管理后台 -> 工具 -> Injector

## 功能特性

- 四种注入模式: `HEAD` / `FOOTER` / `ID` / `SELECTOR`
- 支持注入位置: `APPEND` / `PREPEND` / `BEFORE` / `AFTER` / `REPLACE`
- 规则组: `AND` / `OR` / `NOT`
- 路径匹配模式: `PATH_PATTERN` / `ANT` / `REGEX` / `EXACT`
- 代码片段与规则双向关联管理

## 关键概念

### 代码片段 (CodeSnippet)

你要注入的实际内容

一个片段可被多条规则复用

创建后默认是启用状态, 禁用的代码片段不会注入

### 注入规则 (InjectionRule)

定义 "把哪些代码片段注入到哪里"

创建之后默认是禁用状态, 需要手动启用

#### 注入模式说明

| 模式         | 说明                |
|------------|-------------------|
| `HEAD`     | 注入到页面 `<head>`    |
| `FOOTER`   | 注入到主题 footer 输出位置 |
| `ID`       | 按元素 `id` 定位注入     |
| `SELECTOR` | 按 CSS 选择器定位注入     |

> `ID` / `SELECTOR` 模式虽是一种更便利的方式, 但服务端需要处理完整 HTML, 性能开销通常高于 `HEAD` / `FOOTER`
>
> 为了提升速度, 对于这个情况在内部构建了缓存策略, 但仍可能会带来较大开销
>
> 常规场景建议优先使用 `HEAD` / `FOOTER`

#### 路径匹配

四种匹配方式

- `PATH_PATTERN`: Spring 风格的路径匹配
- `ANT`: Ant 风格
- `REGEX`: 正则
- `EXACT`: 精确匹配

#### 规则组

- 节点类型: `GROUP`, `PATH`
- 逻辑操作: `AND`, `OR`, `NOT`
- 路径匹配器: `PATH_PATTERN`, `ANT`, `REGEX`, `EXACT`

可以在一个 **规则组** 内添加多个 **路径匹配规则** 以及嵌套多个 **规则组**, 并且可根据 **逻辑操作符** 与前一项连接起来

可得到类似的结构:`GROUP(PATH('/posts/**') OR PATH('/archives/**') AND NOT(PATH('/admin/**')))`

## 开发环境

- Java 21+
- Node.js 18+
- pnpm

## 开发

```bash
# 构建插件
./gradlew build

# 开发前端
cd ui
pnpm install
pnpm dev
```

构建完成后, 可以在 `build/libs` 目录找到插件 jar 文件

## 许可证

[GPL-3.0](./LICENSE) © Erzbir 
