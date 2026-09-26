# test 目录说明

这个目录**不参与项目编译**，里面放两类东西：

1. **学习小实验**（`*Demo.java`）—— 我写这个项目时，遇到想不明白的 Java 概念就写个十几行的小程序验证一下。留在这里给同样在学的朋友参考。
2. **回归测试清单**（`REGRESSION_TEST.md`）—— 手工验证程序功能是否正常。

## 学习小实验

| 文件 | 演示什么 | 会编译过吗 |
|---|---|---|
| `StaticDemo.java` | `static` 字段属于**类**（全局一份），实例字段属于**对象**（每个对象一份） | ✅ 能 |
| `ScopeDemo.java` | 局部变量声明在 `try` **之前**，`catch` 块里能看到它 | ✅ 能 |
| `ScopeDemo2.java` | 局部变量声明在 `try` **里面**，`catch` 块里看不到它 | ❌ **故意编译不过** |
| `GenericDemo.java` | `static` 成员不能引用类的泛型参数 `T` | ❌ **故意编译不过** |
| `ErasureDemo.java` | 泛型擦除：`List<String>` 和 `List<Integer>` 运行时其实是同一个类 | ✅ 能 |

### 怎么跑

能编译的那三个，随便找个目录编译运行即可：

```bash
javac -encoding UTF-8 test/ErasureDemo.java -d test/out
java -cp test/out ErasureDemo
```

**`ScopeDemo2.java` 和 `GenericDemo.java` 是反例，就是拿来看着它报错的。** 直接编译会得到这样的错误：

```
test\ScopeDemo2.java:11: 错误: 找不到符号
            System.out.println(inside);   // ← 这行应该编译报错
                               ^
  符号:   变量 inside
  位置: 类 ScopeDemo2
```

```
test\GenericDemo.java:5: 错误: 非静态类型变量 T 无法从静态上下文中引用
    private static List<T> items = new ArrayList<>();
```

这两条错误信息就是"为什么"的答案，比看书记得牢。

## 回归测试清单

`REGRESSION_TEST.md` 是一份**手工测试清单**：列出每一步该输入什么、预期看到什么。改动代码之后照着走一遍，就能确认没有改坏已有功能。

清单基于 `data/` 目录里的演示数据（5 个学生、3 个科目、10 条成绩）。测试前建议先备份 `data/`：

```bash
xcopy data data_backup /E /I
```

测完把 `data_backup` 恢复回去即可。
