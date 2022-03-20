### ServerBootstrap的childHandler()与handler()的区别
**ServerBootstrap的childHandler()与handler()的区别**
ServerBootstrap的childHandler()与handler()添加的handlers是针对不同的EventLoopGroup起作用：

通过handler添加的handlers是对bossGroup线程组起作用

通过childHandler添加的handlers是对workerGroup线程组起作用

 

**Bootstrap的handler()**
客户端Bootstrap只有handler()方法，因为客户端只需要一个事件线程组