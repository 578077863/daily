# redis

## 前言

![image-20220104210832937](images/redis/image-20220104210832937.png)

![image-20220104211510015](images/redis/image-20220104211510015.png)







思考：Redis 的长尾延迟维持在一定阈值以下，有哪些思路







## 疑问

### 跳表

```
请问一下老师，Redis中sorted set 底层实现是一个dict + 一个zskiplist， Redis底层为什
么要如此设计。zadd key score value 这样的形式，那如果底层采用了跳表的数据结构zs
et到底是如何存储数据的呢？dict中存储的是什么，跳表中存储的又是什么呢

作者回复: 这个问题非常好，对sorted set的底层实现，观察很仔细。
我们一般用sorted set时，会经常根据集合元素的分数进行范围查询，例如ZRANGEBYSCORE或
者ZREVRANGEBYSCORE，这些操作基于跳表就可以实现O(logN)的复杂度。此时，跳表的每个
节点同时保存了元素值和它的score。感兴趣可以进一步看下，redis源码的server.h中的zskiplist
Node结构体。
然后，就是你说的为什么还设计dict。不知道你有没有注意到，sorted set 还有ZSCORE这样的
操作，而且它的操作复杂度为O(1)。如果只有跳表，这个是做不到O(1)的，之所以可以做到O
(1)，就是因为还用了dict，里面存储的key是sorted set的member，value就是这个member的s
core。

```



# 琐碎

## redis好处,事务,持久化



## ZSet

### 1. 底层实现

跳表:

[跳表(SkipList)设计与实现(Java) - bigsai - 博客园 (cnblogs.com)](https://www.cnblogs.com/bigsai/p/14193225.html)







