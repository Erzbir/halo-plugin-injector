一个用于 **将 HTML 代码注入到指定页面** 的 Halo 插件

Halo 自带的代码注入功能仅支持全局注入, 此插件允许可以根据路径和元素匹配来注入 HTML 代码

---

## 功能特性

* 按页面路径注入 HTML 代码
* 支持注入:

    * `<head>` 标签中
    * `<footer>` 标签中
    * 指定 `id` 的元素
    * 匹配 `CSS selector` 的元素

![preview](assets/images/preview.png)

## 注入机制说明

插件提供四种注入模式:

| 模式         | 实现方式                      | 说明                             |
|------------|---------------------------|--------------------------------|
| `head`     | `TemplateHeadProcessor`   | 插入到 `<head>` 标签中               |
| `footer`   | `TemplateFooterProcessor` | 插入到 `<halo:footer/>` 中(主题控制位置) |
| `id`       | `AdditionalWebFilter`     | 通过元素 ID 定位插入                   |
| `selector` | `AdditionalWebFilter`     | 通过 CSS 选择器匹配插入, 会处理所有匹配到的元素    |

> `id` 与 `selector` 模式为临时实现方案, 
> 因其需在服务端完整读取 HTML, 因此使用了 `AdditionalWebFilter`, 这会导致失去原本的 **分块传输(chunked transfer)** 特性

## 插入位置选项

在 `id` 或 `selector` 模式下, 可通过配置选择插入方式:

* `append`: 追加为目标元素的子元素
* `prepend`: 插入到目标元素第一个子元素之前
* `before`: 插入到目标元素之前
* `after`: 插入到目标元素之后

> 插入到 `<head>` 时请注意 HTML 合法性. 例如 `<div>` 等块级标签不会被放入 `<head>`, 而是自动插入到 `<body>` 的第一个子元素位置

点击查看示例配置: <a href="assets/images/config.png" alt="example">example</a>

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