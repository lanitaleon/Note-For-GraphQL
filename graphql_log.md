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

默认的data fetcher：

`PropertyDataFetcher`根据字段名称匹配简单属性。

一般来说按照字段名称匹配属性，如果有偏差，如desc和description，解决方式有如下两种。

- 在query中使用别名。

```
query {
    bookById(id: 1) {
        id
        bookName
        count: pageCount
    }
}
```

- 在schema.graphql中自定义fetch别名。

```
directive @fetch(from : String!) on FIELD_DEFINITION

type Product {
    id : ID
    name : String
    description : String @fetch(from:"desc")
}
```

- 在代码中定义的schema可以定义`PropertyDataFetcher.fetching`。

```java
GraphQLFieldDefinition descriptionField = GraphQLFieldDefinition.newFieldDefinition()
        .name("description")
        .type(Scalars.GraphQLString)
        .build();
GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
        .dataFetcher(
                coordinates("ObjectType", "description"),
                PropertyDataFetcher.fetching("desc"))
        .build();
```

除了默认的data fetcher，还可以自定义，加载schema后通过wiring绑定自定义的data fetcher。

```java
// wiring
private RuntimeWiring buildWiring() {
    return RuntimeWiring.newRuntimeWiring()
            .type("Query", typeWiring -> typeWiring
                    .dataFetcher("bookById", graphQLDataFetchers.getBookById())
            )
            .build();
}

// data fetcher
public DataFetcher getBookById() {
    return dataFetchingEnvironment -> {
        Integer id = dataFetchingEnvironment.getArgument("id");
        return bookRepo.findById(id);
    };
}
```

`DataFetchingEnvironment`包含要获取的字段，向该字段提供了哪些参数以及其他信息，例如字段的类型，其父类型，查询根对象或查询上下文对象。常用方法如下：

- `<T> T getSource()`

父字段获取的结果，一般是DTO。

```java
private RuntimeWiring buildWiring() {
    return RuntimeWiring.newRuntimeWiring()
            .type(newTypeWiring("Book")
                    .dataFetcher("author", graphQLDataFetchers.getAuthor()))
            .build();
}

public DataFetcher getAuthor() {
    return dataFetchingEnvironment -> {
        Book book = dataFetchingEnvironment.getSource();
        return book.getAuthor();
    };
}
```

- `<T> T getRoot()`

最上层处理结果，对最上层来说，source和root是一样的。

- `Map<String, Object> getArguments()`

所有请求参数和值。

- `<T> T getContext()`

上下文信息。

- `ExecutionStepInfo getExecutionStepInfo()`

执行查询时捕获的字段类型信息。

每一次执行graphql将会建立字段和类型的调用树，通过这棵树可以debug query的问题。

- `DataFetchingFieldSelectionSet getSelectionSet()`

在当前执行字段下“选择”的子字段。

```
query {
    products {
        # the fields below represent the selection set
        name
        description
        sellingLocations {
            state
        }
    }
}
```

name，description， sellingLocations就是products的选择集。

通过预先拿到选择集，可以进行一些子查询的优化。

- `ExecutionId getExecutionId()`

执行标识，可用于日志、debug等。



### 3.Execution

https://www.graphql-java.com/documentation/v14/execution/

3.1处理异常

3.2异步调用

3.3执行策略`Execution Strategies`

`AsyncExecutionStrategy`

执行查询是异步的，但是结果会按照顺序组装。如下friends的查询不必等待enemies查询结束，但是结果会按照顺序组合。

```
query {
  hero {
    enemies {
      name
    }
    friends {
      name
    }
  }
}
```

`AsyncSerialExecutionStrategy`

mutation的执行是顺序的，一次修改结束才会执行下一次修改。

`SubscriptionExecutionStrategy`

有状态订阅，支持反应流API，一般来说指的是：

http://www.reactive-streams.org/ 

3.4Query Caching

解析和校验查询语句是耗时的，可以使用缓存提高效率，仅缓存文档，不缓存结果。

```java
Cache<String, PreparsedDocumentEntry> cache = Caffeine.newBuilder().maximumSize(10_000).build();
PreparsedDocumentProvider preparsedCache =
        (ExecutionInput executionInput, Function<ExecutionInput, PreparsedDocumentEntry> function) -> {
    Function<String, PreparsedDocumentEntry> mapCompute = key -> function.apply(executionInput);
    return cache.get(executionInput.getQuery(), mapCompute);
};
GraphQL graphQL = GraphQL.newGraphQL(StarWarsSchema.starWarsSchema)
        .preparsedDocumentProvider(preparsedCache) 
        .build();
```

将query中的参数作为变量传递提高命中率。

```java
// 传递参数
query HelloTo {
     sayHello(to: "Me") {
        greeting
     }
}
// 改为传递变量参数
query HelloTo($to: String!) {
     sayHello(to: $to) {
        greeting
     }
}
// 变量
{
   "to": "Me"
}
```





分页

权限





发现了一个详细分析graphQL的博客：

http://blog.mygraphql.com/wordpress/?cat=2



跟restful对比——什么是restful

http://blog.bhusk.com/articles/2018/05/24/1527167962745