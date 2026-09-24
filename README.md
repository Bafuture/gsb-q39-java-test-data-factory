# 从零实现测试数据构造库

Pair-wise GSB 标注任务仓库（第 4 批 / 39）。

| 项目 | 内容 |
|------|------|
| 任务类型 | 代码测试 |
| 任务难度 | 困难 |
| 语言/框架 | Java, Maven, JUnit 5 |
| 环境可复现等级 | 无外部依赖 |
| 构建方式 | Maven（含 mvnw wrapper，无需本机安装 Maven） |

> 本仓库是**初始环境快照**：只有工程骨架，不含任何实现代码。
> 分支说明：`main` 为初始环境；`A`、`B` 为两次独立执行各自的工作分支，均从 `main` 的同一个提交拉出。

## 运行方式

```bash
./mvnw -q verify
```

## 任务提示词

以下为本题完整的 User Prompt 原文，两次执行必须使用完全相同的文本。

写测试时最花时间的往往是构造对象：一个类几十个字段，测试其实只关心其中三个。请从零实现一个测试数据构造库，仓库里只有一个空的 Maven 工程（声明了 JUnit 5 与 AssertJ）。要求：1) 对象模板：通过反射或方法引用设置关心的字段，未设置的字段自动填充合法默认值，覆盖基本类型、字符串、集合、嵌套对象、枚举与时间类型；2) 可复现随机：给定随机种子后多次运行生成完全相同的数据；不指定种子时每次不同，需有测试验证这两点；3) 约束支持：能为字段声明约束（数值范围、字符串长度与前缀、集合大小），生成值必须满足约束；配置本身不合理时应在构建模板阶段立即报错，而不是等到生成时才发现；4) 集合与嵌套：支持按数量批量生成集合，以及嵌套对象的自动构造，嵌套深度需有上限保护；5) 边界值模式：支持生成边界值集合（最小值、最大值、空值、临界长度）用于参数化测试；6) 与 JUnit 5 集成：提供一个扩展或参数提供器，使测试方法可直接注入构造好的对象；7) `mvn -q verify` 跑通，覆盖默认填充、种子可复现性、约束校验、集合与嵌套、边界值五类测试；8) README 说明模板定义方式与扩展点。

## 提交要求

1. 在本仓库中完成提示词要求的全部内容。
2. ./mvnw -q verify 必须通过。
3. 完成后在所属分支（A 或 B）上提交，产物快照的父提交必须是初始环境快照。

---

# 库使用说明（实现文档）

测试数据构造库位于 `com.gsb.testdata`，入口为 `TestData`，JUnit 5 集成位于 `com.gsb.testdata.junit`。

## 模板定义方式

```java
Template<User> users = TestData.template(User.class, 42L)   // 第二个参数为随机种子，可省略
    .set(User::setName, "Alice")                            // 方法引用固定字段（也支持 .set("name", "Alice")）
    .constrain(User::setAge, c -> c.range(18, 60))          // 数值范围
    .constrain(User::setEmail, c -> c.length(5, 30).prefix("u-")) // 字符串长度与前缀
    .constrain(User::setTags, c -> c.size(1, 3))            // 集合大小
    .use(addressTemplate)                                   // 注册嵌套对象模板
    .maxDepth(3)                                            // 嵌套深度上限（默认 3）
    .build();

User one   = users.create();        // 单个对象：未设置的字段自动填充合法默认值
List<User> many = users.create(10); // 批量生成
List<User> edge = users.boundaryValues(); // 边界值集合（最小/最大值、null、空串、临界长度、空/满集合）
```

- 默认值覆盖：基本类型及包装类、字符串、枚举、集合（List/Set/Map/数组）、
  时间类型（`LocalDate`/`LocalDateTime`/`LocalTime`/`Instant`/`ZonedDateTime`/`Date`）、
  `BigDecimal`/`BigInteger`/`UUID`，以及任意含无参构造器的嵌套 POJO（递归自动构造，超过 `maxDepth` 的嵌套引用置为 `null`，防止自引用栈溢出）。
- 可复现随机：指定种子后，相同种子 + 相同调用序列产生完全相同的数据；不指定种子时每次运行不同。
- 约束即声明即校验：`range`/`length`/`size` 区间倒置、前缀超过最大长度、固定值违反约束、
  未知字段名、类型不兼容等都会在模板构建阶段抛出 `TemplateConfigurationException`，而不是等到生成时。

## JUnit 5 集成

```java
@ExtendWith(TestDataExtension.class)
class UserTest {

  static Template<User> userTemplate = TestData.template(User.class)
      .set(User::setName, "Injected")
      .build();

  @Test
  void auto(@Fixture User user) { /* 自动模板注入 */ }

  @Test
  void named(@Fixture("userTemplate") User user) { /* 引用测试类中的模板字段/无参方法 */ }

  @Test
  void seeded(@Fixture(value = "userTemplate", seed = 123L) User user) { /* 可复现注入 */ }
}
```

## 扩展点

- **嵌套模板注册**：`TemplateBuilder.use(template)` 让嵌套字段使用自定义模板而非默认自动构造。
- **字段固定值**：`set(...)` 支持方法引用（类型安全）或字段名字符串，适合只关心少数字段的场景。
- **约束组合**：`ConstraintBuilder` 的 `range`/`length`/`prefix`/`size`/`nullable` 可链式组合；`nullable(false)` 会把 `null` 从边界值集合中排除。
- **种子控制**：`TestData.template(Class, long)`、`TemplateBuilder.seed(long)`、`Template.withSeed(long)` 三级种子入口，可在不重建模板的情况下切换可复现序列。
- **深度保护**：`TemplateBuilder.maxDepth(int)` 调整嵌套上限，控制对象图规模。

## 测试结构

| 测试类 | 覆盖要求 |
|--------|----------|
| `DefaultFillTest` | 默认填充（基本类型/集合/枚举/时间/嵌套对象） |
| `SeedReproducibilityTest` | 种子可复现性（同种子相同、无种子不同） |
| `ConstraintValidationTest` | 约束生效 + 非法配置构建期报错 |
| `CollectionAndNestingTest` | 批量生成、嵌套自动构造、深度上限保护 |
| `BoundaryValuesTest` | 边界值集合（最值、null、临界长度、空/满集合） |
| `JunitExtensionTest` | JUnit 5 扩展参数注入 |
