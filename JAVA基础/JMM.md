# JMM

## 琐碎

i++：如果是static i的话，JVM字节码指令把静态变量和常量1都放到操作数栈，修改后的值再存入静态变量i；如果局部变量 i 的话，就是调用iinc在局部变量槽上更新



System.out.println()用了 synchronized（）使得工作内存直接到主存读取数据

