### kotlin 调用 java
### java 调用 kotlin
最核心的是需要了解 kotlin 被编译成什么样子
* 属性, 存在 get 和 set 方法
* 某个 kotlin 文件下的函数和属性, 会被编译成某个类(可以使用@JvmName注解制定类的名字)拥有这些静态函数和静态属性, class 则保持类.
