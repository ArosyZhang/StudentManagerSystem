# 学生成绩管理系统 1.0

用纯 Java（无第三方依赖）写的控制台版学生成绩管理系统，我的第一个完整项目。

## 功能

主菜单四项 + 退出：

| 编号 | 功能 | 说明 |
|---|---|---|
| 1 | 打印所有学生和成绩 | 表格输出，中文按 2 字符宽度对齐 |
| 2 | 学生管理 | 增 / 删 / 改 / 查 |
| 3 | 成绩管理 | 按学生、按科目录入与查询成绩 |
| 4 | 科目管理 | 增 / 删 / 改 / 查 |
| 0 | 退出 | 退出前自动保存数据 |

## 目录结构

```
Student_Manager_System\
├─ src\StudentManagerSystem\   ← 源码
├─ bin\                        ← 编译输出（已 gitignore）
├─ build.bat                   ← 一键编译 + 打包
├─ manifest.txt                ← jar 入口声明
└─ README.md
```

## 源码说明

| 文件 | 行数 | 职责 |
|---|---|---|
| `StudentManagerSystem.java` | 66 | 程序入口 + 主菜单循环 |
| `StudentManager.java` | 278 | 学生实体与管理（学号前缀 `STU`，补零到 6 位） |
| `SubjectManager.java` | 248 | 科目实体与管理（科目号前缀 `SUB`，补零到 6 位） |
| `ScoreManager.java` | 471 | 成绩表，`Map<学号, Map<科目号, Double>>` |
| `DataStore.java` | 137 | 文件持久化：`loadAll()` / `saveAll()` |
| `ToolUtil.java` | 124 | 输入校验、显示宽度计算 |

## 编译运行

**一键编译打包**（Windows，双击 `build.bat`）：

```bat
javac -d bin -encoding UTF-8 src\StudentManagerSystem\*.java
jar -cvfm 学生管理系统1.0.jar manifest.txt -C bin .
```

**直接运行 class**：

```
java -cp bin StudentManagerSystem.StudentManagerSystem
```

> `build.bat` 是 **GBK 编码**（含中文提示），用 VS Code 打开若乱码请手动切编码。
> `manifest.txt` 里声明了入口：`Main-Class: StudentManagerSystem.StudentManagerSystem`。

## 设计备忘

- 全部用 `static` 成员和方法（写这个的时候还没学面向对象设计），所以没有实例化。
- 数据在内存里用 `ArrayList` / `HashMap` 存，启动时 `DataStore.loadAll()` 读盘、退出前 `DataStore.saveAll()` 写盘。
- `ToolUtil.dispalyWidth(String)` 用来算中文占 2 格、英文占 1 格，让控制台表格对齐；方法名拼错了（应为 `displayWidth`），留到后续版本改。
- 学号 / 科目号由 `ID_PREFIX + 补零到 ID_FIX_LENGTH` 生成。
- `StudentManagerSystem` 里保留了三个测试方法（`intDefaultSubject()`、`intDefaultStudent()`、`ScoreManager.initRandomScores()`），已注释掉，想造数据时取消注释即可。

## 后续计划

见笔记仓库 `05-复盘\学生系统后续版本规划.md`；打包精简 JRE 见 `03-流程与工具\jlink 生成精简 JRE.md`。