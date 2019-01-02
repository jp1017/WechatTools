### 基本语法
* 函数定义：返回值申明 , 空返回值 , 参数申明为变量名在前 , 类型在后
* 变量定义
    * 局部只读变量 val , 只能赋值一次
    * 可重新赋值变量 var
* 注解：内联标记使用 markdown , 支持链接
* 字符串支持模版
* 空值安全机制
    * kotlin中类型系统区分一个引用是否容纳 null , 比如 String 类型的常规变量不能容纳 null , 会直接编译错误
    * 如果允许变量为 null , 比如 String , 则声明为 String？
    * 安全调用，如果 val b: String? = null , 则 println（b?.length） 当 b 为 null 时返回 null 不为 null 时返回 b.length.
* while 和 for 循环大同小异 , for 等同于 foreach
* when 代替了 switch 操作符
* 新增 in 处理区间函数

### 习惯用法
* 具有少数主构造函数参数的类写成一行
* 鼓励函数定义默认参数
* list 过滤使用 filter
* 只读 list 时使用 val
* 善用 ？ 处理 null 值的逻辑场景
* 单表达式函数直接使用 = 返回值 不用 return , 或者和其他表达式比如 when 一起使用等


### 编程规范
* 代码结构 , 文件命名/命名 , 类/方法命名与 java 一致
* 横向空白要遵循的比较多 , 需要严格遵守
    * 绝不在 (、 [ 之后或者 ]、 ) 之前留空格
    * 绝不在. 或者 ?. 左右留空格 : foo.bar().filter { it > 2 }.joinToString(), foo?.bar()
    * 在 // 之后留一个空格 : // 这是一条注释
    * 不要在用于指定类型参数的尖括号前后留空格 : ：class Map<K, V> { …… }
    * 不要在 :: 前后留空格 : Foo::class、 String::length
    * 不要在用于标记可空类型的 ? 前留空格 : String?
* 冒号场景，在分割声明与类型时冒号之前不要留空格，以下场景需要在冒号前留一个空格
    * 当它用于分隔类型与超类型时
    * 当委托给一个超类的构造函数或者同一类的另一个构造函数时
    * 在 object 关键字之后
    ```
    abstract class Foo<out T : Any> : IFoo {
        abstract fun foo(a: Int): T
    }

    class FooImpl : Foo() {
        constructor(x: String) : this(x) { …… }

        val x = object : IFoo { …… }
    }

    ```
* 函数的表达式函数体与函数声明不适合放在同一行
* 链式调用使用换行 , . 和 ?. 在下一行并单倍缩进

### 基本类型

内置的基本类型与 java 基本一致

| Type | Kotlin Bit width | Java Bit width |
| :------| :------: | :------: |
|Double| 64|64|
|Float|32|32|
|Long|64|64|
|Int|32|32|
|Short|16|16|
|Byte|8|8|

### 控制流
* if 是一个表达式 , 会返回一个值
* when 也可以返回一个值

### 类与对象
* 类与继承
    * 区分主构造函数和次构造函数，存在 init 初始化块
    * 初始化一个类的实例不需要 new 关键字
    * 继承中需要初始化基类的主构造函数
    * 继承中覆盖方法需要基类函数标注 open , 可使用 final 禁止覆盖
    * 继承中覆盖属性和覆盖方法相似
    * 不存在静态方法 , 使用伴生对象处理
*



