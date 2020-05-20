# 自己动手写SQL执行引擎
## 前言
在阅读了大量关于数据库的资料后，笔者情不自禁产生了一个造数据库轮子的想法。来验证一下自己对于数据库底层原理的掌握是否牢靠。在笔者的github中给这个database起名为Freedom。      
## 整体结构
既然造轮子，那当然得从前端的网络协议交互到后端的文件存储全部给撸一遍。下面是Freedom实现的整体结构，里面包含了实现的大致模块:      
![](https://oscimg.oschina.net/oscnet/up-83cf4cad225751c169cc260f1be5de72b0f.png)      
最终存储结构当然是使用经典的B+树结构。当然在B+树和文件系统block块之间的转换则通过Buffer(Page) Manager来进行。当然了，为了完成事务，还必须要用WAL协议，其通过Log Manager来操作。    
Freedom采用的是索引组织表，通过DruidSQL Parse来将sql翻译为对应的索引操作符进而进行对应的语义操作。      
## MySQL Protocol结构
client/server之间的交互采用的是MySQL协议，这样很容易就可以和mysql client以及jdbc进行交互了。      
### query packet 
mysql通过3byte的定长包头去进行分包，进而解决tcp流的读取问题。再通过一个sequenceId来再应用层判断packet是否连续。      
![](https://oscimg.oschina.net/oscnet/up-0b7d17469107496fd399312ec786141899d.png)        
### result set packet
mysql协议部分最复杂的内容是其对于result set的读取，在NIO的方式下加重了复杂性。
Freedom通过设置一系列的读取状态可以比较好的在Netty框架下解决这一问题。      
![](https://oscimg.oschina.net/oscnet/up-fc9841db0119d848e39f84dd8830c5b81dd.png)            
### row packet
还有一个较简单的是对row格式进行读取，如上图所示,只需要按部就班的解析即可。      
![](https://oscimg.oschina.net/oscnet/up-9ba7a4e3db0a44f4dffd5dc904b731f6d6c.png)      
由于协议解析部分较为简单，在这里就不再赘述。      

## SQL Parse
Freedom采用成熟好用的Druid SQL Parse作为解析器。事实上，解析sql就是将用文本表示
的sql语义表示为一系列操作符(这里限于篇幅原因，仅仅给出select中where过滤的原理)。
### 对where的处理
例如where后面的谓词就可以表示为一系列的以树状结构组织的SQL表达式，如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-fd6c823889c9ecc83995de0d90e7804daf9.png)      
当access层通过游标提供一系列row后，就可以通过这个树状表达式来过滤出符合where要求的数据。Druid采用了Parse中常用的visitor很方便的处理上面的表达式计算操作。
### 对join的处理
对join最简单处理方案就是对两张表进行笛卡尔积，然后通过上面的where condition进行过滤，如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-3509db89d2ccfe1584e4ce3d88eb156bab1.png)            
### Freedom对于缩小笛卡尔积的处理
由于Freedom采用的是B+树作为底层存储结构，所以可以通过where谓词来界定B+树scan(搜索)的范围(也即最大搜索key和最小搜索key在B+树种中的位置)。考虑sql

```
select a.*,b.* from t_archer as a join t_rider as b where a.id>=3 and a.id<=11 b.id and b.id>=19 b.id<=31
```
那么就可以界定出在id这个索引上,a的scan范围为[3,11],如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-644fdc65e4a183a9af9e9c6c8775106a15c.png)      
b的scan范围为[19,31],如下图所示(假设两张表数据一样，便于绘图):      
![](https://oscimg.oschina.net/oscnet/up-d6bb3434b260ab8246a3bcee8255274a4ee.png)      
scan少了从原来的15\*15(一共15个元素)次循环减少到4\*4次循环,即循环次数减少到7.1%

当然如果存在join condition的话，那么Freedom在底层cursor递归处理的过程中会预先过滤掉一部分数据，进一步减少上层的过滤。

# B+Tree的磁盘结构
## leaf磁盘结构
Freedom的B+Tree是存储到磁盘里的。考虑到存储的限制以及不定长的key值，所以会变得非常复杂。Freedom以page为单位来和磁盘进行交互。叶子节点和非叶子节点都由page承载并刷入磁盘。结构如下所示:      
![](https://oscimg.oschina.net/oscnet/up-abe84d1d9409b7c46b53447387c3b3069b7.png)      
一个元组(tuple/item)在一个page中分为定长的ItemPointer和不定长的Item两部分。
其中ItemPointer里面存储了对应item的起始偏移和长度。同时ItemPointer和Item如图所示是向着中心方向进行伸张，这种结构很有效的组织了非定长Item。 
### leaf和node节点在Page中的不同
虽然leaf和node在page中组织结构一致，但其item包含的项确有区别。由于Freedom采用的是索引组织表，所以对于leaf在聚簇索引(clusterIndex)和二级索引(secondaryIndex)中对item的表示也有区别,如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-d649ccfa767c9ba83998209dfea4d576fb8.png)      
其中在二级索引搜索时通过secondaryIndex通过index-key找到对应的clusterId,再通过
clusterId在clusterIndex中找到对应的row记录。     
由于要落盘，所以Freedom在node节点中的item里面写入了index-key对应的pageno,
这样就可以容易的从磁盘恢复所有的索引结构了。
### B+Tree在文件中的组织
有了Page结构，我们就可以将数据承载在一个个page大小的内存里面，同时还可以将page刷新到对应的文件里。有了node.item中的pageno，我们就可以较容易的进行文件和内存结构之间的互相映射了。
B+树在磁盘文件中的组织如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-c1740d8e165978f46129c0f9115a4d997e8.png)      
B+树在内存中相对应的映射结构如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-cc89c3f7c515e6e572e8efb90d534372a0a.png)      
文件page和内存page中的内容基本是一致的,除了一些内存page中特有的字段，例如dirty等。
### 每个索引一个B+树
在Freedom中，每个索引都是一颗B+树，对记录的插入和修改都要对所有的B+树进行操作。
### B+Tree的测试
笔者通过一系列测试case,例如随机变长记录对B+树进行插入并落盘，修复了其中若干个非常诡异的corner case。
### B+Tree的todo
笔者这里只是完成了最简单的B+树结构，没有给其添加并发修改的锁机制，也没有在B+树做操作的时候记录log来保证B+树在宕机等灾难性情况下的一致性,所以就算完成了这么多的工作量，距离一个高并发高可用的bptree还有非常大的距离。
## Meta Data
table的元信息由create table所创建。创建之后会将元信息落盘，以便Freedom在重启的时候加载表信息。每张表的元信息只占用一页的空间，依旧复用page结构，主要保存的是聚簇索引和二级索引的信息。元信息对应的Item如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-62b37cd2e6d45c59ca4a5b2bf643f04a60d.png)      
如果想让mybatis可以自动生成关于Freedom的代码，还需实现一些特定的sql来展现Freedom的元信息。这个在笔者另一个项目rider中有这样的实现。原理如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-11a9f315bd4b77683d3b7dffd105f8c8ce3.png)      
实现了上述4类SQL之后，mybatis-generator就可以通过jdbc从Freedom获取元信息进而自动生成代码了。

## 事务支持
由于当前Freedom并没有保证并发，所以对于事务的支持只做了最简单的WAL协议。通过记录redo/undolog从而实现原子性。
### redo/undo log协议格式
Freedom在每做一个修改操作时，都会生成一条日志，其中记录了修改前(undo)和修改后(redo)的行信息，redo用来回滚,redo用来宕机recover。结构如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-8672b50ccdf8ae795a07cff3dff33a674e2.png)     
### WAL协议
WAL协议很好理解，就是在事务commit前将当前事务中所产生的的所有log记录刷入磁盘。
Freedom自然也做了这个操作，使得可以在宕机后通过log恢复出所有的数据。      
![](https://oscimg.oschina.net/oscnet/up-83029d948723aea197e01beba17b26b1741.png)      

### 回滚的实现
由于日志中记录了undo，所以对于一个事务的回滚直接通过日志进行undo即可。如下图所示:      
![](https://oscimg.oschina.net/oscnet/up-b32f8511b08a172fb35bb8649f465501ac5.png)      
### 宕机恢复
Freedom如果在page全部刷盘之后关机，则可以由通过加载page的方式获取原来的数据。
但如果突然宕机,例如kill -9之后，则可以通过WAL协议中记录的redo/undo log来重新
恢复所有的数据。由于时间和精力所限，笔者并没有实现基于LSN的检查点机制。
## Freedom运行
```
git clone https://github.com/alchemystar/Freedom.git
// 并没有做打包部署的工作，所以最简单的方法是在java编辑器里面
run alchemystar.freedom.engine.server.main
```
以下是笔者实际运行Freedom的例子:    
![](https://oscimg.oschina.net/oscnet/up-4a0c9b32a6cf09d157bb07f357261793d0a.JPEG)      
join查询      
![](https://oscimg.oschina.net/oscnet/up-5f0ea6f33bfff904e8146d995319598c08d.JPEG)      
delete回滚      
![](https://oscimg.oschina.net/oscnet/up-34e77d656b9b19035e601133b150a2a6039.JPEG)      
## Freedom todo
Freedom还有很多工作没有完成，例如有层次的锁机制和MVCC等，由于工作忙起来就耽搁了。
于是笔者就看了看MySQL源码的实现理解了一下锁和MVCC实现原理，并写了两篇博客。比起
自己动手撸实在是轻松太多了^_^。

### MVCC
https://my.oschina.net/alchemystar/blog/1927425      
### 二阶段锁 
https://my.oschina.net/alchemystar/blog/1438839      
## 尾声
在造轮子的过程中一开始是非常有激情非常快乐的。但随着系统越来越庞大，复杂性越来越高，进度就会越来越慢，还时不时要推翻自己原来的设想并重新设计，然后再协同修改关联的所有代码，就如同泥沼，越陷越深。至此，笔者才领悟了软件工程最重要的其实是控制复杂度！始终保持简洁的接口和优雅的设计是实现一个大型系统的必要条件。
## 收获与遗憾
这次造轮子的过程基本满足了笔者的初衷，通过写一个数据库来学习数据库。不仅仅是加深了理解，最重要的是笔者在写的过程中终于明白了数据库为什么要这么设计，为什么不那样设计，仅仅对书本的阅读可能并不会有这些思考与领悟。        
当然，还是有很多遗憾的，Freedom并没有实现锁机制和MVCC。由于只能在工作闲暇时间写，所以断断续续写了一两个月，工作一忙就将这个项目闲置了。现在将Freedom的设计写出来，希望大家能有所收获。            
![image](https://oscimg.oschina.net/oscnet/up-03e8bdd592b3eb9dec0a50fa5ff56192df0.JPEG)      


## 学习MySQL视频课程
![](https://oscimg.oschina.net/oscnet/up-b0adf638b6bfbfe9948140a0b22189ef8b0.JPEG)     

## github链接
https://github.com/alchemystar/Freedom     
## 码云链接
https://gitee.com/alchemystar/Freedom     

