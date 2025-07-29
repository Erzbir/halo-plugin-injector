## 简介

这是一个用于将 HTML 代码注入到指定页面的插件

Halo 默认提供的代码注入功能只有全局注入, 无法注入到指定页面

## 说明

**插件不提供代码验证**

可以将一段代码插入到多个匹配到的页面, 并且可以选择插入到 `head` 还是 `footer`

- 如果是插入到 `head`, 需要注意合法性. 比如 `<div>` 这样的元素是不会插入到 `<head>`, 而会直接插入到 `<body>`
的第一个子元素 
- 插入到 `footer` 会插入到 `<halo:footer/>` 中, 这个通常由主题来控制位置

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

构建完成后，可以在 `build/libs` 目录找到插件 jar 文件。

## 许可证

[GPL-3.0](./LICENSE) © Erzbir 