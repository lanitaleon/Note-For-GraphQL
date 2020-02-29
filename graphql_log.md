1.是否影响后端查询构建和查询效率

2.当引入权限后，不同的参数是否可以带有权限限制，如果可以，是否又变得像json注解一样混乱

3.graphQL提供的是集成API的解决方案，区分于前端写的SQL，又或者并无区分

4.类似的实现 oData



参考博客：

[REST, GraphQL, gRPC 如何选型](https://zhuanlan.zhihu.com/p/44140864)

https://www.graphql-java.com/tutorials/getting-started-with-spring-boot/



1.改用maven到底行不行

2.前端怎么写

可以get，可以post，key是query，content-type是json

用postman选择body>graphql，勾选post

https://blog.postman.com/2019/06/18/postman-v7-2-supports-graphql/

3.可以实现增删改吗

似乎是将参数写进Mutation

https://xeblog.cn/articles/6



参考文档：

https://www.graphql-java.com/documentation/v14/



### 1.Schema

https://www.graphql-java.com/documentation/v14/schema/

两种构建方式：

- 配置文件

```json
type Foo {
    bar: String
}
```

- 代码

```java
GraphQLObjectType fooType = newObject()
    .name("Foo")
    .field(newFieldDefinition()
            .name("bar")
            .type(GraphQLString))
    .build();
```

#### 1.1Types

Types支持：

Scalar，Object，Interface，Union，InputObject，Enum

- Scalar

GraphQLString ， GraphQLBoolean ， GraphQLInt ， GraphQLFloat ， GraphQLID，

其中ID是String，除此之外还有扩展标量。

GraphQLLong，GraphQLShort，GraphQLByte，GraphQLBigDecimal，GraphQLBigInteger

- Object

对象，实体

- Interface

```
interface ComicCharacter {
    name: String;
}
```

- Union

```
type Cat {
    name: String;
    lives: Int;
}

type Dog {
    name: String;
    bonesOwned: int;
}

union Pet = Cat | Dog
```

联合类型：

https://graphql.org.cn/learn/schema-union-types.html

https://www.jianshu.com/p/3eadc50b2a15

- Enum

http://www.zhaiqianfeng.com/2017/06/learn-graphql-action-by-javascript.html

- ObjectInputType

与Object差不多，类型是Input

- Subscription

见下一节

#### 1.2Subscription

官方文档

https://www.graphql-java.com/documentation/v14/subscriptions/

官方文档的翻译版本

http://blog.mygraphql.com/wordpress/?p=106

订阅机制背景介绍，为什么选择事件驱动的订阅机制，订阅可以实现什么。

https://graphql.org/blog/subscriptions-in-graphql-and-relay/

示例代码：

https://github.com/graphql-java/graphql-java-examples/tree/master/subscription-example

使用RxJava2实现订阅发布机制，参考文档：

[Observable是如何工作的](https://www.jianshu.com/p/e432df0603e8)





### 2.Data fetching

https://www.graphql-java.com/documentation/v14/data-fetching/





分页

权限





发现了一个详细分析graphQL的博客：

http://blog.mygraphql.com/wordpress/?cat=2