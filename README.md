# gsb-q39-java-test-data-factory

一个测试数据构造库：你只声明关心的字段，其余字段自动填充合法默认值；给定随机种子即可完全复现。

要求 JDK 17+，运行 `mvn -q verify` 执行全部测试。

## 快速开始

```java
Template<User> template = DataFactory.template(User.class)
        .set("name", "Alice")                    // 按字段名设置
        .set(User::getEmail, "a@example.com")    // getter 方法引用
        .set(User::setActive, true)              // setter 方法引用
        .constrain("age", Constraints.range(18, 60))
        .seed(42L)                               // 固定种子 -> 可复现
        .maxDepth(3)                             // 嵌套深度上限（默认 4）
        .build();

User one   = template.create();                  // 单个对象
List<User> many = template.createList(10);       // 批量生成
List<User> edge = template.boundaryValues("age");// 边界值集合
```

## 模板定义方式

- **显式赋值**：`set(...)` 支持字段名、getter 引用（`User::getName`）、setter 引用（`User::setName`）三种寻址方式，优先级最高。
- **约束生成**：`constrain(...)` 声明约束后，字段值在约束范围内随机生成。
- **默认填充**：未配置的字段按类型自动填充，覆盖基本类型及包装类、`String`、枚举、`List`/`Set`/`Map`、数组、`Optional`、`BigDecimal`/`BigInteger`/`UUID`、`java.time` 各类型（`LocalDate`、`LocalDateTime`、`Instant`、`ZonedDateTime` 等）以及嵌套 POJO（需无参构造器）。
- **嵌套保护**：嵌套对象递归构造，深度超过 `maxDepth` 时该层对象置为 `null`、集合置空，自引用类型（如 `Node.next`）不会无限递归。

所有配置错误都在**模板定义阶段**立即抛出（`TestDataException` / `InvalidConstraintException`），不会拖到生成时：字段不存在、值类型不匹配、约束与字段类型不兼容、约束自身不合法（如 `range(10, 1)`、前缀超过最大长度）都会 fail fast。

## 可复现随机

- `seed(42L)`：同一种子的模板多次运行生成完全相同的数据（包括批量序列）。
- 不指定种子：每次运行数据不同。
- 对应测试见 `SeedReproducibilityTest`。

## 内置约束

| 工厂方法 | 适用类型 | 说明 |
|---|---|---|
| `Constraints.range(min, max)` | 数值（含 `BigDecimal`/`BigInteger`） | 闭区间随机数 |
| `Constraints.length(min, max)` | `String` | 长度范围 |
| `Constraints.prefix(p)` | `String` | 固定前缀 |
| `Constraints.text().minLength(m).maxLength(n).prefix(p).build()` | `String` | 长度 + 前缀组合 |
| `Constraints.size(min, max)` | 集合 / `Map` / 数组 | 元素个数范围 |

## 边界值模式

`template.boundaryValues("age")` 返回一组实例：目标字段依次取最小值、最大值、刚好越界的值（min-1 / max+1）、`null`（基本类型除外）、临界长度/临界大小、空字符串/空集合；其余字段正常填充。未声明约束的字段回退到类型级边界（0、±1、空串、空集合、null）。可直接喂给 `@ParameterizedTest` 的 `@MethodSource`。

## JUnit 5 集成

```java
@ExtendWith(TestDataExtension.class)
class UserTest {

    @Test
    void injects(@Generated User user) { /* 默认模板注入 */ }

    @Test
    void reproducible(@Generated(seed = 99L) User user) { /* 可复现注入 */ }
}
```

也可以用 `TestDataExtension.registerTemplate(User.class, template)` 注册自定义模板，注入时优先使用（`clearTemplates()` 清理）。

## 扩展点

- **自定义约束**：实现 `com.gsb.testdata.constraint.Constraint`（`supports` / `generate` / `boundaryValues`），在构造器里校验配置即可接入 fail-fast 体系。
- **自定义类型生成器**：`DataFactory.registerGenerator(MyType.class, random -> ...)` 覆盖任意类型的默认填充逻辑。
- **JUnit 模板注册**：`TestDataExtension.registerTemplate(...)` 全局复用模板。

## 项目结构

```
com.gsb.testdata            DataFactory / Template / TemplateBuilder / 方法引用 / 异常
com.gsb.testdata.gen        默认值生成引擎（GenContext / ValueGenerator / Numbers）
com.gsb.testdata.constraint Constraint 接口与 range/length/prefix/size 实现
com.gsb.testdata.junit      @Generated + TestDataExtension（ParameterResolver）
```

测试覆盖五类场景：默认填充（`DefaultFillTest`）、种子可复现性（`SeedReproducibilityTest`）、约束校验（`ConstraintTest`）、集合与嵌套（`CollectionAndNestedTest`）、边界值（`BoundaryValueTest`），另有 JUnit 扩展测试（`JunitExtensionTest`、`RegisteredTemplateTest`）。
