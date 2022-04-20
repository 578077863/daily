# mybatis框架

mybatis是持久层框架，底层是对JDBC的封装，基于ORM（对象和关系表的映射）思想对结果集进行了封装。  
ORM思想就是：把数据库表和javaBean对应起来，使得java操作实体类最终映射到对数据库表的操作。

## 简述原理

Mybatis基于**动态代理**，mybatis为dao接口创建代理对象，接口方法的调用返回的值其实就是代理对象返回的结果，mapper文件中定义的sql最终会被解析到代理对象中，并且mybatis负责参数绑定和结果集的解析和封装。

例如，mybatis执行select语句返回结果集，迭代这个结果集，每一行都是一条记录，创建一个空对象（先将resultType对应的类型加载入内存），变量这条记录的每一个属性，并且反射调用对象的set方法为对象封装属性值，最终将这个对象存入集合中，当结果集迭代完毕，我们就已经得到一个对象集合了。即我们将一个结果集对象成功解析为一个对象集合。

> 外循环迭代resultSet，每行对应一条数据库记录，使用resultType类型的对象进行封装，内循环遍历每一列/属性，将数据库字段和实体属性对应起来，反射调用set方法进行封装。

如果封装一个String-Object的map就更简单了，key就是属性名，value就是值，相当于使用map替代了对象，那么最终会返回一个map列表。

## 两个占位符

${} 属于静态的文本替换，生成sql语句时会将占位符的内容直接替换为传入的参数。（打印日志可以看到完整的语句）  
#{} 会将占位符替换为？，然后执行sql前使用preparedStatement将？替换为具体的参数。可以防止注入攻击。（打印日志时，参数都被 ？占位）

#{}是先编译好sql语句再替换，而${}将参数先替换再编译sql语句。  
如果使用拼接字符串作为参数，那么#{}将其看作一个单独的参数，相当于是一个无效的参数。  
总之，**应该使用 #{}**

## 反射实现：mapToObject

通过反射将Map转换为JavaBean  
例如将{“name”:”jack”,”age”:123}封装进people对象

这里有两种封装方式：  
【1】无视访问修饰符，暴力注入属性值（setAccessible设置访问性为true，强制访问/修改）

```
public static Object mapToObject(Map<String, Object> map, Class<?> beanClass){
    if (map == null)
        return null;
    Object obj = null;
    try {
        obj = beanClass.newInstance();	//先准备一个空对象（反射调用无参构造函数）
        Field[] fields = obj.getClass().getDeclaredFields(); 
        //拿到所有字段，然后遍历
        for (Field field : fields) {
            int mod = field.getModifiers(); 
            if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
            //field.getModifiers返回值可以表示一个字段类型，静态或常量不处理
                continue;
            }
            field.setAccessible(true);  //无视访问修饰符，暴力修改
            field.set(obj, map.get(field.getName()));
  //设置对象obj的field属性为map.get(field.getName()) 根据对应的变量名作为key取出value
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return obj;
}

    
```

【2】构造set方法，反射调用set方法

```
public static <T> T mapToObject(Map<String, Object> map, Class<T> obj) throws Exception {
    if (map == null) {
        return null;
    }
    Set<Map.Entry<String, Object>> sets = map.entrySet();
    T t = obj.newInstance();//空对象
    Method[] methods = obj.getDeclaredMethods();//被声明的方法
    for (Map.Entry<String, Object> entry : sets) {
        String str = entry.getKey();//属性名
        //根据key得到set方法的全称，第一个字符大写，拼接后得到最终要调用的set方法名
        String setMethod = "set"  
+ str.substring(0, 1).toUpperCase() + str.substring(1);
		//搜索相应方法，然后反射执行
        for (Method method : methods) {
            if (method.getName().equals(setMethod)) {
            // 等价于t.setXXX(entry.getValue())
                method.invoke(t, entry.getValue());
            }
        }
    }
    return t;
}

    
```


## 为什么SQL语句预编译能预防注入
在使用PreparedStatement执行SQL命令时，命令会带着占位符被数据库进行编译和解析，并放到命令缓冲区。然后，每当执行同一个PreparedStatement语句的时候，由于在缓冲区中可以发现预编译的命令，虽然会被再解析一次，但不会被再次编译。

而SQL注入只对编译过程有破坏作用，执行阶段只是把输入串作为数据处理，不需要再对SQL语句进行解析，因此解决了注入问题。

因为SQL语句编译阶段是进行词法分析、语法分析、语义分析等过程的，也就是说编译过程识别了关键字、执行逻辑之类的东西，编译结束了这条SQL语句能干什么就定了。而在编译之后加入注入的部分，就已经没办法改变执行逻辑了，这部分就只能是相当于输入字符串被处理。
