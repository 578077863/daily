
## 总结

```
**MIT 6.830 数据库系统**

1. 项目简介：基于MIT 830课程完成的数据库系统  
2. 基本结构：数据存储与操作相关的类，其中使用位图来记录数据页中槽位的使用情况，达到节省空间从而使得同等大小的数据页能容纳更多记录；基于LRU淘汰策略实现的缓冲池管理对数据的操作  
3. 基本操作：实现了条件查询，连接查询(内连接)，单字段聚合，单字段分组    
4. 事务与并发：实现页级锁，基于2PL实现可重复读隔离级别。采用超时检测策略解除死锁。在日志基础上支持事务回滚    
5. 索引：采用B+树索引，实现查询，插入，删除功能    
6. 日志：基于steal/no-force策略实现的日志系统
```

```markdown

```


# BufferPool
```java

getPage(tid,pageId, Permissions perm)

根据要操作perm,判断是属于READ_ONLY 还是Exclusive

不断循环去获取锁,若获取到则退出死循环,获取不到并且超时则抛出异常. 获取到锁后先判断该数据页是否在缓冲池中,若不在则调用File类的readPage方法去读取该数据页到缓冲池中,返回该数据页

```


```markdown

首先我们锁的类型有两种,共享锁和独占锁,我定义了一个类,PageLock,该类具有两个静态常量(以0和1分别代表锁的类型),两个实例变量(加锁的事务id, 另一个变量则代表该锁的类型)


自定义一个管理锁的类LockManager

实例变量如下:

创建一个锁表,Map结构的锁表,key为数据页Id, value为对该数据页加锁的事务id和锁类型,也是一个Map数据结构


//Map<PageId, Map<TransactionId, PageLock>> 第一个map的key值代表着加了锁的页,value则是代表对该页加锁的事务及其锁的类型

该类控制对页面加锁的操作,所以可以推出应具有的方法:
1.对页面加锁
2.对页面释放锁
3.判断页面是否被指定事务锁定
4.事务提交之后释放该事务对页面加的锁

对页面加锁采用2PL协议,先判断当前数据页是否加锁,若发现没加锁,直接为其上锁后返回即可.

若当前数据页已经有锁了,则先判断 该事务之前是否对其加过锁, 若加过锁: 1.现在加的锁和之前的一样,直接返回 2.第一步处理了加的锁相同的情况,而现在处理的就是加的锁类型与之前不同的情况,若之前加的是写锁,直接返回. 若之前加的是读锁,则需要判断当前数据页上锁的数量,为1则锁升级为独占锁,大于1则返回false

若之前没加过锁:先拿到数据页上第一把锁的类型,再进行判断:若是我们当前想要加的锁是共享锁,并且数据页上第一把锁的类型为共享锁,直接加上共享锁,返回true,否则返回false. 若我们要加的是写锁,只能返回false.

这意味着写锁只能在数据页没有加锁的情况写加


根据数据页id 和事务id

到锁表中查看该事务是否已经加过锁, 若存在则 remove掉








commit:
先判断是commit还是rollback:
若是commit,那就需要flushPages,先遍历LRU链表,找出哪些数据页是当前事务修改过的,对该数据页调用setBeforeImage(),即将该数据页的数据转换成字节流保存数据页的oldData字节数组中,然后开始进行刷盘,先根据要刷盘的数据页中的oldData字节数组和数据页的pid new出一张新的数据页,将数据页和

对数据页第一次加载到内存需要调用setBeforeImage,保存初始版本,用于事务回滚,直接根据事务id找到对应修改的数据页,调用getBeforeImage覆盖回滚

由于数据页的写不能一次性完成,可能写一半机器宕机了,这里的办法是日志先刷到硬盘上,再写数据页到硬盘上


事务将修改的数据页写入到硬盘后,调用当前数据页的setBeforeImage,因为已经成功提交该数据页到硬盘上了

日志这东西直接暴力记住两张数据页和最后一次的事务id



我采用:no-steal/force,事务提交时,不断地写log刷页,不断写update记录,然后刷完全部脏页后写 commit记录,更新一下 日志的起始地址
Database.getLogFile().logCommit(tid);  
Database.getLogFile().force();


通过这样子, 日志起始地址后面就全是需要回滚的

日志中Map数据结构记录着当前事务第一条日志记录的位置，那这条记录什么时候产生的呢？是事务begin时写入到日志中

recover 获取当前chickpoint的位置往后recover

每次调用commit都会更新checkpoint的地址

写update记录也会移动checkpoint，奇怪


事务回滚：revocerPages（tid）根据事务id找到其修改的数据页，调用数据页的beforeImage()方法，拿到前一个版本的数据页，将数据页的引用指向前一个版本的数据页，并放到缓冲池中
```



# 聚合函数(Aggregate)
```markdown

传入的 groupby字段的索引下标和数据类型,聚合字段的索引下标,操作Op,通过操作Op的类型通过拿到对应的聚合函数处理器,而这些聚合函数处理器是基于策略模式创建的,目的是做解耦

聚合函数处理器实现逻辑:
首先创建一个抽象父类,该父类必须具备一个KV值映射的数据结构,这里我使用hashMap来实现,因为是单个线程来执行,不存在线程安全问题. 用hasmMap存储 groupby 的字段的值作为 key,然后结果作为value,当然了,若是没有groupby字段,则统一使用一个null来作为key值.

各个子类自己实现handler方法, Max,Min,Sum,Avg,Count


在构造函数中,根据聚合字段完成对聚合处理器的选择,判断是否有group字段,没有就设置一个常量作为group字段的值


当调用Aggregate类的 open方法, 首先打开数据源,while对该数据源进行调用上面选择好的Aggregator实现类的mergeTupleIntoGroup方法将一条条数据传送进去,在该方法中 先判断 group字段是否为null,若不为null则拿到元组的对应groupby字段的类型与要groupby字段的类型想匹配,若都通过,则进行下一步否则抛异常

若是groupby字段的索引下标为 -1, 代表着groupby字段为null,这时候创建一个String变量作为key,也就是分组字段的值,还有要聚合的字段Field,找到对应的处理器调用其handler方法

调用该处理器的iterator方法,

```


## 增删
```markdown
HeapFile先通过缓冲池对接下来要获取的数据页加写锁，判断是否有空槽，没有空槽就释放锁，读取下一数据页，若是有空槽则直接调用数据页的insert(tuple)方法,在数据页的操作中,首先判断该记录的字段类型是否与当前数据页记录的字段类型相匹配
若匹配就判断一下当前数据页是否还有空槽,因为我们不知道谁会调用这个方法,在该方法内我们得注意异常的情况
为0抛异常,不匹配也抛异常

当前字段类型匹配,也有空槽,则寻找空槽将记录放进数组中,为该记录设置一下RecordId,代表他是属于哪张数据页的第几行,标记当前页为脏页,break返回

若查询到最后都没有空槽,则需要自己创建一张空的数据页,写进磁盘,再通过BufferPool从磁盘上调进内存,再进行page的insert操作,将脏页返回

pageId是由tableId和pageNo组成

通过缓冲池的insert方法调用File的insert方法,insert方法再去调用page的insert方法将数据插入数据页中,将脏页集合返回,再将这些脏页put进缓冲池中,原因是可能有的数据页还没修改就被淘汰,导致修改完后不在缓冲池中,这样会造成数据丢失问题

File中直接根据recordId中的pageId拿到对应的数据页id,再从通过缓冲池拿到对应的数据页,调用page的方法
delete方法也是雷同,判断要删除的记录是否属于当前数据页,再判断要删除的槽是否为空,若不为空,将slot位图对应的位置为0,表示空

最后返回脏页
```


# HeapFile
HeapFile具有多少数据页：当前File大小 / 一张数据页的大小，向上取整

对File的遍历写了一个迭代器
```java
private HeapFile heapFile;  
private TransactionId transactionId;  
private Iterator<Tuple> iterator;  
private int curPage;

private class HeapFileIterator implements DbFileIterator{
	private HeapFile heapFile;  
	private TransactionId transactionId;  
	private Iterator<Tuple> iterator;  
	private int curPage;
}


//该迭代器核心就是该方法,传入要读取的页下标,iterator就切换成该页的iterator了
private Iterator<Tuple> getIterator(int pageNo) throws TransactionAbortedException, DbException {  
    if(pageNo >= 0 && pageNo < heapFile.numPages()){  
        HeapPageId heapPageId = new HeapPageId(heapFile.getId(), pageNo);  
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(transactionId, heapPageId, Permissions.READ_ONLY);  
        return page.iterator();  
    }else{  
        throw new DbException(String.format("problems opening/accessing the database pageNo %d ", pageNo));  
    }  
}

//通过hashNext判断是否next是否有剩余
@Override  
public boolean hasNext() throws DbException, TransactionAbortedException {  
    if(iterator == null){  
        return false;  
    }else if(iterator.hasNext()){  
        return true;  
    }else{  
        curPage++;  
        if(curPage >= heapFile.numPages()){  
            return false;  
        }else{  
            iterator = getIterator(curPage);  
            return hasNext();  
        }  
    }
```


```markdown
readPage(PageId id): 根据pid拿到tid和pageNo,通过RandomAccessFile打开File, 判断要获取的页是否在文件的范围内(通过字节长度判断),判断通过后,调用randomaccessFile.seek(xx)方法,xx为 pageSize * pageNo

int read = randomAccessFile.read(bytes, 0, BufferPool.getPageSize());
HeapPageId id = new HeapPageId(tableId, pageNo);  
return new HeapPage(id, bytes);

最后调用randomAccessfile.close()


insertTuple(tid, t):
```

## HeapPage类
关键属性：
```java
final HeapPageId pid;  
final TupleDesc td;  
final byte[] header;  
final Tuple[] tuples;  
final int numSlots;
private boolean dirty;  
private TransactionId ditryTId;
```

记录着当前page属于哪张表，元组的信息由TupleDesc记录
字节数组header记录着当前数据页上槽位的使用情况
Tuple数组存放记录

numSlots代表着数据页最多能存放多少记录，一条记录需要的空间是：按位来计算：当前数据页大小 * 8 / (元组全部字段类型的大小之和 + 1位标头需要的空间)，**结果向下去，因为数据页的大小是固定的，槽位可以少，但不能超出**


header的大小基于 numSlots， 1位代表一个槽，所以 numSlot/ 8，向上取整就是header的大小了


dirty，dirtyId这两个都是由最后操作的事务在操作中写上的


**遍历**：创建一个List，对Tuple数组进行遍历，若该槽位已经被使用，就将其添加进List集合中，遍历完就返回list的迭代器。

**插入：** 先对插入的元组判断其字段信息是否和当前数据页的字段信息相匹配，再判断当前数据页的空槽位是否为0，不为0就寻找到一个空槽位，让该槽位指向 等于插入的元组。然后设置该槽位已被使用，为插入的元组设置 RecordId，停止遍历


### HeapPageId类
记录着 tableId，pageNo

### Tuple类
就是元组，由[[MIT6.830 2021#Filed类]]对象的集合组成
元组的类型由 [[MIT6.830 2021#TupleDesc类]]表示
其归属记录在[[MIT6.830 2021#RecordId类]]
#### RecordId类
记录着 当前元组属于哪一数据页（pageId），在数据页中的位置（tupleNo）

#### Filed类

#### TupleDesc类
该实例由元组类型对象的集合组成，元组中每个字段对应着一个实例，每个实例描述着字段的类型与名称
## Lab2
### 2.1. 过滤和联接
SimpleDB OpIterator 类实现了关系代数的运算。现在，您将实现两个运算符，这两个运算符将使您能够执行比表扫描稍微有趣的查询。

-   _Filter_：此运算符仅返回满足指定为其构造函数一部分的 元组。因此，它会筛选出任何与谓词不匹配的元组。`Predicate`
    
-   _Join_：此运算符根据作为其构造函数的一部分传入的 a 来连接其两个子级的元组。我们只需要一个简单的嵌套循环联接，但您可以探索更有趣的联接实现。在实验室文章中描述您的实现。`JoinPredicate`

实现
-   src/java/simpledb/execution/Predicate.java
-   src/java/simpledb/execution/JoinPredicate.java
-   src/java/simpledb/execution/Filter.java
-   src/java/simpledb/execution/Join.java


Predicate.java(判断该条数据是否能通过条件查询)
```markdown
where t.getField(field) op operand
* @param field  
*            field number of passed in tuples to compare against.  
* @param op  
*            operation to use for comparison  
* @param operand  
*            field value to compare passed in tuples to
```
主要是完成`public boolean filter(Tuple t)`这个方法

JoinPredicate.java(内联查询)
```markdown
tuple1.getField(field1) == tuple2.getField(field2)
* @param field1  
*            The field index into the first tuple in the predicate  
* @param field2  
*            The field index into the second tuple in the predicate  
* @param op  
*            The operation to apply (as defined in Predicate.Op); either  
*            Predicate.Op.GREATER_THAN, Predicate.Op.LESS_THAN,  
*            Predicate.Op.EQUAL, Predicate.Op.GREATER_THAN_OR_EQ, or  
*            Predicate.Op.LESS_THAN_OR_EQ
```
主要是完成`public boolean filter(Tuple t)`这个方法

Filter.java(完成where的整体逻辑)
本质就是拿着Predicate对数据源整体数据一条条判断
```java
/**  
 * AbstractDbIterator.readNext implementation. Iterates over tuples from the * child operator, applying the predicate to them and returning those that * pass the predicate (i.e. for which the Predicate.filter() returns true.) * * @return The next tuple that passes the filter, or null if there are no  
 *         more tuples * @see Predicate#filter  
 */protected Tuple fetchNext() throws NoSuchElementException,  
        TransactionAbortedException, DbException {  
  
    while(opIterator.hasNext()){  
        Tuple tuple = opIterator.next();  
        if(tuple != null && predicate.filter(tuple)){  
            return tuple;  
        }  
    }  
    return null;  
}
```

Join.java
对传进来的两个数据源进行笛卡尔积,过程中拿着JoinPredicate进行判断
当然了,由于是内连接,设计两张表数据的连接,所以存储的tuple的tupleDesc还要调用TupleDesc.merge
```java
/**  
 * Returns the next tuple generated by the join, or null if there are no * more tuples. Logically, this is the next tuple in r1 cross r2 that * satisfies the join predicate. There are many possible implementations; * the simplest is a nested loops join. * <p>  
 * Note that the tuples returned from this particular implementation of Join * are simply the concatenation of joining tuples from the left and right * relation. Therefore, if an equality predicate is used there will be two * copies of the join attribute in the results. (Removing such duplicate * columns can be done with an additional projection operator if needed.) * <p>  
 * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6}, * joined on equality of the first column, then this returns {1,2,3,1,5,6}. * * @return The next matching tuple.  
 * @see JoinPredicate#filter  
 */protected Tuple fetchNext() throws TransactionAbortedException, DbException {  
    //TODO : 可能存在逻辑漏洞? 但我目前没发现  
  
  
 TupleDesc tupleDesc1 = child1.getTupleDesc();  
    TupleDesc tupleDesc2 = child2.getTupleDesc();  
  
    while(tuple1 != null || child1.hasNext()){  
        if(tuple1 == null){  
            tuple1 = child1.next();  
        }  
  
        if(!child2.hasNext()){  
            if(child1.hasNext()){  
                child2.rewind();  
                tuple1 = child1.next();  
            }else{  
                return null;  
            }  
        }  
  
        while(child2.hasNext()){  
            Tuple tuple2 = child2.next();  
  
            if(joinPredicate.filter(tuple1,tuple2)){  
                Tuple res = new Tuple(getTupleDesc());  
                for(int i = 0; i < tuple1.getTupleDesc().numFields(); i++){  
                    res.setField(i, tuple1.getField(i));  
                }  
                for(int i = 0; i < tuple2.getTupleDesc().numFields(); i++){  
                    res.setField(tuple1.getTupleDesc().numFields() + i, tuple2.getField(i));  
                }  
                return res;  
            }  
        }  
    }  
  
    return null;  
}
```

### 2.2. 聚合和分组
另一个 SimpleDB 运算符使用子句实现基本 SQL 聚合。您应该实现五个 SQL 聚合 （， 、 ， ）， 并支持分组。您只需要支持单个字段的聚合，并按单个字段进行分组。`GROUP BY``COUNT``SUM``AVG``MIN``MAX`

为了计算聚合，我们使用一个接口，该接口将新元组合并到聚合的现有计算中。在施工期间被告知它应该使用什么操作进行聚合。随后，客户端代码应调用子迭代器中的每个元组。合并所有元组后，客户端可以检索聚合结果的 OpIterator。结果中的每个元组都是一对形式，除非按字段分组的值为，在这种情况下，结果是该形式的单个元组。`Aggregator``Aggregator``Aggregator.mergeTupleIntoGroup()``(groupValue, aggregateValue)``Aggregator.NO_GROUPING``(aggregateValue)`

实现
-   src/java/simpledb/execution/IntegerAggregator.java
-   src/java/simpledb/execution/StringAggregator.java
-   src/java/simpledb/execution/Aggregate.java

IntegerAggregator.java
不要慌! gbfield代表 group by tuple的第x个属性进行分组；gbfieldtype 代表 第x个属性的类型
afield是 tuple 第 x 条属性要进行聚合操作
利用what 和 策略模式完成 handler的选择

用 handler中的concurrentHash存储结果（比较方便，毕竟聚合分组结果是 key ： value）
调用 handler的mergeTupleIntoGroup方法进行传入的tuple的判断
```java
    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */
```
```java
    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {

        //如果groub by 的值不为null && 分组的属性类型不一致
        if(gbfieldtype != null && !(tup.getField(gbfieldIndex).getType().equals(gbfieldtype))){
            throw new IllegalArgumentException("Given tuple has wrong type");
        }

        String key;
        if(gbfieldIndex == NO_GROUPING){
            key = NO_GROUPING_KEY;
        }else{
            key = tup.getField(gbfieldIndex).toString();
        }

        gbHandler.handle(key, tup.getField(afieldIndex));
    }
```
```java
    /**
     * Create a OpIterator over group aggregate results.
     * 
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {

        Map<String, Integer> results = gbHandler.getGbResult();
        Type[] types;
        String[] names;
        TupleDesc tupleDesc;

        List<Tuple> tuples = new ArrayList<>();
        
        // 没有分组，结果自然只有一条
        if(gbfieldIndex == NO_GROUPING){
            types = new Type[]{Type.INT_TYPE};
            names = new String[]{"aggregateVal"};
            tupleDesc = new TupleDesc(types, names);

            Integer res = results.get(NO_GROUPING_KEY);
            Tuple tuple = new Tuple(tupleDesc);
            tuple.setField(0, new IntField(res));
            tuples.add(tuple);
        }else{
            
            // 有分组，答案可能有多条
            types = new Type[]{gbfieldtype,Type.INT_TYPE};
            names = new String[]{"groupVal","aggregateVal"};
            tupleDesc = new TupleDesc(types,names);
            for(Map.Entry<String,Integer> entry:results.entrySet()){
                Tuple tuple = new Tuple(tupleDesc);
                if(gbfieldtype==Type.INT_TYPE){
                    tuple.setField(0,new IntField(Integer.parseInt(entry.getKey())));
                }else{
                    tuple.setField(0,new StringField(entry.getKey(),entry.getKey().length()));
                }
                //由于integer 是具有 max,min,avg等聚合函数, string 就只有count
                tuple.setField(1,new IntField(entry.getValue()));
                tuples.add(tuple);
            }
        }


        //这里用TupleIterator, 思考为什么? 我认为原因类似 Integer包装类, 可以更加灵活,eg:记录tupleDesc等信息
        return new TupleIterator(tupleDesc,tuples);
```


Aggregate.java
```java
    private OpIterator child;
    private int afieldIdx;
    private int gfieldIdx;
    private Aggregator.Op aop;

    private Aggregator aggregator;
    private OpIterator resIterator;
    private TupleDesc aggreDesc;


	public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop){
		...

		// 分组值的判断
        Type gbType = gfield == Aggregator.NO_GROUPING? null : child.getTupleDesc().getFieldType(gfield);

		//聚合类的选择
        switch (child.getTupleDesc().getFieldType(afield)){
            case INT_TYPE:
                aggregator = new IntegerAggregator(gfieldIdx, gbType, afieldIdx, aop);
                break;
            case STRING_TYPE:
                aggregator = new StringAggregator(gfieldIdx, gbType, afieldIdx, aop);
                break;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
	}
```

```java
 public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
        super.open();

        //数据源打开,开始进行聚合分组计算
        try {
            child.open();

            while (child.hasNext()) {
                aggregator.mergeTupleIntoGroup(child.next());
            }
        } catch (DbException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        } finally {
            child.close();
        }

		//这里遍历的数据类似于快照的数据
        resIterator = aggregator.iterator();
        resIterator.open();
    }
```
## Lab3
### 3.1

## Lab4
迭代器在遍历时只能通过迭代器修改,否则将抛出异常,modcount对不上
但flushAllPages要刷的可能很多,不可能这个方法中发现脏页,再flush这个方法中通过遍历去找这个脏页,所以用一个List装脏页,然后依次取脏页给flush这个方法,直接根据pid就可以找到对应的脏页,刷到磁盘中


玛德,这个放page的是linkedList,自带LFU算法,每一次get都会改变page的位置
```java
    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     *     break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {

//
//        for(Map.Entry<Integer,Page> entry : pages.entrySet()){
//            Page page = entry.getValue();
//            if(page.isDirty() != null){
//                flushPage(page.getId());
//            }
//        }

        List<Page> dirtyPage = new ArrayList<>();
        for(Page page : pages.values()) {
            if (page.isDirty() != null) {
                dirtyPage.add(page);
            }
        }

        for (int i = 0; i < dirtyPage.size(); i++) {
            flushPage(dirtyPage.get(i).getId());
        }
    }

    /** Remove the specific page id from the buffer pool.
        Needed by the recovery manager to ensure that the
        buffer pool doesn't keep a rolled back page in its
        cache.
        
        Also used by B+ tree files to ensure that deleted pages
        are removed from the cache so they can be reused safely
    */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Flushes a certain page to disk
     * @param pid an ID indicating the page to flush
     */
    private synchronized void flushPage(PageId pid) throws IOException {

        Page page = pages.get(pid.hashCode());
        DbFile dbFile = Database.getCatalog().getDatabaseFile(page.getId().getTableId());
        dbFile.writePage(page);
        page.markDirty(false,null);

//        Page page = null;
//        for(Map.Entry<Integer, Page> entry : pages.entrySet()) {
//            Integer key = entry.getKey();
//            if (key == pid.hashCode()) {
//                page = entry.getValue();
//                break;
//            }
//        }
////
//        DbFile file = Database.getCatalog().getDatabaseFile(page.getId().getTableId());
////        //将脏页保存下来再刷入磁盘
////        Database.getLogFile().logWrite(page.isDirty(), page.getBeforeImage(), page);
////        Database.getLogFile().force();
//        file.writePage(page);
//        page.markDirty(false, null);
    }

    /** Write all pages of the specified transaction to disk.
     */
    public synchronized  void flushPages(TransactionId tid) throws IOException {

        for(Page page : pages.values()){
            flushPage(page.getId());
        }
//        for(Map.Entry<Integer,Page> entry : pages.entrySet()){
//            Page page = entry.getValue();
//
//            page.setBeforeImage();
//
//            if(page.isDirty() == tid){
//                flushPage(page.getId());
//            }
//        }
    }
```





## 锁的粒度

```java
LockManager是由一个 ConcurrentHashMap<??,ConcurrentHashMap<TransactionId, PageLock>>维护的,

?? 
1.换为 PageId,就是页级锁
2.换为 RecordId就是tuple锁

开始写LockManager


```


