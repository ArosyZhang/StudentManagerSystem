# 更新日志

本项目遵循[语义化版本](https://semver.org/lang/zh-CN/)，格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [1.0.1] - 2026-09-27

在 1.0.0 基础上补齐"下载即用"的发布形态。

### 新增

- **`run.bat`**：双击即可编译并直接运行，省去手敲 `javac` / `java`
- **`package-green.bat`** 与 `packaging/`：一键打包免安装的绿色版压缩包 —— 用 `jlink` 生成只含 `java.base` 模块的精简 JRE（约 27 MB），连同 jar、演示数据和启动脚本一起打包
- Release 页面提供 Windows 绿色版，解压后双击 `启动.bat` 即可运行，**使用者不需要安装 Java**

### 修复

- `data/scores.txt` 写回时按学号、科目号排序，不再因为内部 `HashMap` 的遍历顺序而每次保存都变动行序

### 文档

- README 补充三种运行方式对照表与绿色版说明，目录树同步更新

## [1.0.0] - 2026-09-27

首个发布版本：一个功能完整、注释齐备的纯 Java 控制台版学生成绩管理系统。

### 新增

- **学生管理**：增 / 删 / 查，删除学生时连带删除其全部成绩
- **科目管理**：增 / 删 / 查，删除科目时连带删除该科目的全部成绩
- **成绩管理**：按学生或按科目录入、查询、删除成绩，重复录入时会询问是否覆盖
- **全校成绩总表**：按总分降序排列，并列名次判定带浮点容差，平均分只按有成绩的科目数计算
- **数据持久化**：以纯文本文件存放在 `data/` 下，可手工编辑；程序启动时读入，用户每次修改后写回
- **控制台表格渲染**：按显示宽度对齐（中文算 2 格、英文算 1 格），超宽内容截断为 `...`
- **输入校验工具**：统一的整数 / 小数读取与范围校验，输入流结束时给出明确提示

### 修复

- **保存时原样写回无效数据行**，避免无法解析的脏数据在退出时被静默删除（唯一的真实数据丢失问题，读写两侧均已防护）
- 修复 `build.bat` 下载后中文乱码：`cmd.exe` 处理不了 LF 行尾的批处理，新增 `.gitattributes` 把 Windows 批处理固定为 CRLF
- 成绩输入支持小数，并修正了并列名次的判定逻辑
- 修复学号 / 科目编号生成时的常量隐患与遗留问题
- 数据加载前先清空集合，避免文件不存在时残留上一次运行的数据
- 输入流结束（Ctrl+Z 或管道耗尽）时不再抛出难以理解的 `NoSuchElementException`

### 重构

- 拆出 `Student`、`Subject` 实体类，并抽取 `Entity` 接口统一"编号 + 名称"的访问约定
- 引入泛型仓库 `Repository<T extends Entity>` 接管学生与科目名单，两套增删查逻辑合一
- **移除全局静态状态**，改为实例对象协作：业务逻辑归 Manager，文件读写归 `DataStore`，装配归程序入口
- 抽取 `printTable` 通用表格渲染，成绩总表改为"定义列 + 映射行"的声明式写法
- 抽取 `loadFile` / `saveFile` / `chooseFromList`，消除三处数据加载与两处选择的重复代码
- `showAllScoresTable` 按职责拆分为五个私有方法
- `normalizeId` 统一迁入 `ToolUtil`，并在加载入口处归一化

### 文档

- 为全部 10 个类补齐中文 Javadoc（类头职责与边界、公开 API 的契约、反直觉行为与使用约束）
- 重写 README，补充功能说明、运行效果、数据文件格式、项目结构与设计说明

[1.0.1]: https://github.com/ArosyZhang/StudentManagerSystem/releases/tag/v1.0.1
[1.0.0]: https://github.com/ArosyZhang/StudentManagerSystem/releases/tag/v1.0.0
