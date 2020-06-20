GraphQL client using GitHub API
===============================

This project provides a type safe read-only frontend to build GitHub's [GraphQL](https://graphql.org) queries.
The project uses Builder pattern with Phantom types. Internally, the application relies on Caliban to generate the 
final graphql queries. You can search for Repositories, Users, Issues / Pull Requests with various possible combinations
and search qualifiers. 

-----
INDEX
-----

1. [About GraphQL and Scala Phantom Types](#1-about-graphql-and-scala-phantom-types)
2. [Some Important Files](#2-some-important-files)
3. [Application Design](#3-application-design)
    * [3.1 Using Builder Pattern with Phantom Types](#31-using-builder-pattern-with-phantom-types)
    * [3.2 Using Caliban Client (`caliban-codegen-sbt`)](#32-using-caliban-client-caliban-codegen-sbt)
4. [Sample Usage](#4-sample-usage)
    * [4.1 Searching Repositories (builder snippet)](#41-searching-repositories-builder-snippet)
    * [4.2 Searching Users (builder snippet)](#42-searching-users-builder-snippet)
    * [4.3 Searching repositories by language and paginating results](#43-searching-repositories-by-language-and-paginating-results)
5. [Setup Instructions](#5-setup-instructions)
6. [Contributors](#6-contributors)



1\. About GraphQL and Scala Phantom Types
-----------------------------------------

According to [Introduction to GraphQL](https://graphql.org/learn/): 
> GraphQL is a query language for your API, and a server-side runtime for executing queries by using a type system you 
> define for your data. GraphQL isn't tied to any specific database or storage engine and is instead backed by your 
> existing code and data.

GraphQL was first introduced by Facebook after years of use within the organization. Since then many other 
organizations have migrated from traditional REST-based architecture. GitHub started providing GraphQL API in 2016 as 
detailed in this [blog post](https://github.blog/2016-09-14-the-github-graphql-api/). GitHub's GraphQL SDL is publicly 
available at their developer's documentation [page](https://developer.github.com/v4/public_schema/). The Schema defines 
several types and is complex. Tools such as [GraphQL Voyager](https://apis.guru/graphql-voyager/) can be used to 
visualize the schema. Further, Github also provides an explorer [page](https://developer.github.com/v4/explorer/) which 
runs the [GraphQL IDE (GraphQLI)](https://github.com/graphql/graphiql).

Although GraphQL queries are easy to write, they are not always checked, specially in weakly types languages like 
JavaScript. Moreover, it's easy to make mistakes which will result in failure when the program is under execution.
One way to rectify this is to build GraphQL queries iteratively using builder pattern which also does some validations
before executing the query. This way, incorrect/malformed GraphQL queries are never build, and an error is issues during
compile time. One interesting way to implement Builder patter is by using Phantom types. Phantom types
never get instantiated and are only a way to prevent incorrect code from compiling.



2\. Some Important Files
------------------------

    src/main/scala/com/ashessin/cs474/project/model
        GHSearch.scala                  Base class for all github search classes, instantiate using respective builder 
                                        patterns.
        GHSearchBuilder.scala           Base class for all github search builders.
    
    src/main/scala/com/ashessin/cs474/project/builder   
        GitHubQuery.scala               The root query class for using Github's GraphQL API with typechecking.
        GHRepositorySearch.scala        Github Repository search query class.
        GHUserSearch.scala              Github User search query class.
        GHIssuePrSearch.scala           Github Issue/PR search query class.
        
    src/main/scala/com/ashessin/cs474/project/graphql/client/
        Github.scala                    The boilerplate code generated from GitHub's GraphQL SDL

    src/main/resources/
        application.conf.example        Template for `application.conf` which should have the GitHub OAuth Token



3\. Application Design
----------------------

The program uses typesafe Builder pattern to generate GraphQL queries with focus on [search][1] related Github queries. 
Some pagination support is built-in and results can be iterated over by repeated calls to 
`com.ashessin.cs474.project.builder.GitHubQuery.execute` method. Depending on the needs, the queries can be as simple as
just searching by a keyword but also complex. For example, the repository search class provides 21 methods for 
instancating the Repository search class and several of these have additional range/arithmetic operators. Some sample 
GraphQL requests, and their responses see next section.

[1]: https://help.github.com/en/github/searching-for-information-on-github/searching-on-github


### 3.1 Using Builder Pattern with Phantom Types

For additional type safety Builder patterns with Phantom types have been used. This way it's easy 
to control the creation of a builder's target class and this ensures that the creation happens in a well ordered way.

The project uses approach similar to Chapter 6 (Adding generalized type constraints to the required methods) of 
[Scala Design Patterns Book](https://learning.oreilly.com/library/view/scala-design-patterns/9781785882500/) to implement
this technique.

Example: The `com.ashessin.cs474.project.builder.GitHubQueryBuilder` class must have certain mandatory fields to be 
valid. Instead of putting this in a constructor (and thus making them mandatory for instanciation), we do implicit 
type constraint checks on each call to the builder's method until it reaches a "Pass State" as determined by the 
compiler.

```
sealed trait QueryBuilderStep

sealed trait HasSearch extends QueryBuilderStep

sealed trait HasLimit extends QueryBuilderStep

sealed trait HasToken extends QueryBuilderStep

/**
 * Builder class using Phantom Types for instantiating [[com.ashessin.cs474.project.builder.GitHubQuery]].
 *
 * @param ghSearch instance of [[com.ashessin.cs474.project.model.GHSearch]] derived type
 * @param limit    number of results to fetch on each call to
 *                 [[com.ashessin.cs474.project.builder.GitHubQuery#execute()]]
 * @param token    the user's unique GitHub OAuth token
 * @param uri      uri to GitHub's GraphQl endpoint
 * @tparam PassedStep via HasSearch, HasLimit, HasToken
 */
class GitHubQueryBuilder[PassedStep <: QueryBuilderStep] private(
  var ghSearch: GHSearch,
  var limit: Int,
  var token: String,
  var uri: String
) {

//...

    def withSearch(search: GHSearch): GitHubQueryBuilder[HasSearch] = {
        this.ghSearch = search
        new GitHubQueryBuilder[HasSearch](this)
     }

    def withLimit(limit: Int)(implicit ev: PassedStep =:= HasSearch): GitHubQueryBuilder[HasLimit] = {
        //...
        new GitHubQueryBuilder[HasLimit](this)
     }

    def withToken(token: String)(implicit ev: PassedStep =:= HasLimit): GitHubQueryBuilder[HasToken] = {
        //...
        new GitHubQueryBuilder[HasToken](this)
    }

    def build()(implicit ev: PassedStep =:= HasToken): GitHubQuery = new GitHubQuery(
        ghSearch, limit, token, uri
    )
}
```

In the above builder, the user must specify `ghSearch`, `limit`, `token` for the instance to reach the `PassedStep`
implicit type. Until then, the compiler will issue an error.


### 3.2 Using Caliban Client (`caliban-codegen-sbt`)

Even with a builder pattern in place, it is quite difficult to write partial raw GraphQL Queries as different functions 
are called on the respective builder class instance. Caliban-client solves this problem since it can auto generate
boilerplate code from a GraphQL schema. This way makes it possible to write GraphQL queries using Scala code in a 
type-safe and functional fashion.

Steps to generate the boilerplate code using GitHub's public GraphQl DSL:
1. Add `caliban-codegen-sbt` plugin to your sbt project and enable it
2. Get the GraphQL SDL file
3. Run `calibanGenClient schemaPath outPath ?scalafmtPath`
    
For additional details see: 
[https://ghostdogpr.github.io/caliban/docs/client.html](https://ghostdogpr.github.io/caliban/docs/client.html)

The above steps were followed to generate `com.ashessin.cs474.project.graphql.client.Github` using 
`src/main/resources/github/schema.public.graphql` schema definition file.



4\. Sample Usage
----------------


### 4.1 Searching Repositories (builder snippet)

```scala
GitHubQueryBuilder()
  .withSearch(RepositorySearchBuilder("spark-stocksim")
    .repo("user501254", "spark-stocksim").topic("spark-sql").topics(2, ">").build())
  .withLimit(1)
  .withToken(token)
  .build()
```

Generated GraphQL (with default selection of repository fields):
```text
{
  search(first: 1, query: "spark-stocksim topic:spark-sql repo:user501254/spark-stocksim topics:>2", type: REPOSITORY) {
    pageInfo {
      endCursor
    }
    nodes {
      __typename
      ... on App {
        id
      }
      ... on MarketplaceListing {
        id
      }
      ... on Issue {
        id
      }
      ... on Organization {
        id
      }
      ... on PullRequest {
        id
      }
      ... on Repository {
        id
        nameWithOwner
        createdAt
        description
        homepageUrl
        forkCount
        isArchived
        isFork
        isPrivate
        stargazers {
          totalCount
        }
        languages(first: 3) {
          nodes {
            name
          }
        }
      }
      ... on User {
        id
      }
    }
  }
}
```


### 4.2 Searching Users (builder snippet)

```scala
GitHubQueryBuilder()
  .withSearch(RepositorySearchBuilder()
    .user("user501254").topics(1, ">").build())
  .withLimit(1)
  .withToken(token)
  .build()
```

Generated GraphQL (with default selection of user fields):
```text
{
  search(first: 15, query: "followers:>100000", type: USER) {
    pageInfo {
      endCursor
    }
    nodes {
      __typename
      ... on App {
        id
      }
      ... on MarketplaceListing {
        id
      }
      ... on Issue {
        id
      }
      ... on Organization {
        id
      }
      ... on PullRequest {
        id
      }
      ... on Repository {
        id
      }
      ... on User {
        id
        login
        name
        bio
        company
        email
        location
        websiteUrl
        followers {
          totalCount
        }
      }
    }
  }
}
```


### 4.3 Searching repositories by language and paginating results

```scala
val javaRepositoryWithManyForks = RepositorySearchBuilder()
  .language("java")
  .forks(1000, ">")
  .build()

// Builder pattern with Phantom Types
var queryContainer = GitHubQueryBuilder().withSearch(javaRepositoryWithManyForks)
  .withLimit(10)
  .withToken(token)
  .build()


// pagniate twice
println(queryContainer.execute())
println(queryContainer.execute())
// additional calls to `execute()` paginate further  
```

Initially generated GraphQL query:
```text
{"query":"query{search(first:10,query:\"forks:>1000 language:java\",type:REPOSITORY){pageInfo{endCursor} nodes{__typename ... on App{id} ... on MarketplaceListing{id} ... on Issue{id} ... on Organization{id} ... on PullRequest{id} ... on Repository{id nameWithOwner createdAt description homepageUrl forkCount isArchived isFork isPrivate stargazers{totalCount} languages(first:3){nodes{name}}} ... on User{id}}}}","variables":{}}
```

First page of response received (notice the `search.pageInfo.endCursor` key):
```json
{"data":{"search":{"pageInfo":{"endCursor":"Y3Vyc29yOjEw"},"nodes":[{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxMjEzOTU1MTA=","nameWithOwner":"CyC2018/CS-Notes","createdAt":"2018-02-13T14:56:24Z","description":":books: 技术面试必备基础知识、Leetcode、计算机操作系统、计算机网络、系统设计、Java、Python、C++","homepageUrl":"https://cyc2018.github.io/CS-Notes","forkCount":32677,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":100432},"languages":{"nodes":[{"name":"Java"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxMzI0NjQzOTU=","nameWithOwner":"Snailclimb/JavaGuide","createdAt":"2018-05-07T13:27:00Z","description":"【Java学习+面试指南】 一份涵盖大部分Java程序员所需要掌握的核心知识。","homepageUrl":"https://gitee.com/SnailClimb/JavaGuide","forkCount":26863,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":78123},"languages":{"nodes":[{"name":"Java"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkyMjc5MDQ4OA==","nameWithOwner":"iluwatar/java-design-patterns","createdAt":"2014-08-09T16:45:18Z","description":"Design patterns implemented in Java","homepageUrl":"https://java-design-patterns.com","forkCount":18495,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":57598},"languages":{"nodes":[{"name":"Java"},{"name":"HTML"},{"name":"CSS"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxNjA2NDAwOTQ=","nameWithOwner":"MisterBooo/LeetCodeAnimation","createdAt":"2018-12-06T08:01:22Z","description":"Demonstrate all the questions on LeetCode in the form of animation.（用动画的形式呈现解LeetCode题目的思路）","homepageUrl":"","forkCount":9848,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":53708},"languages":{"nodes":[{"name":"Java"},{"name":"Python"},{"name":"C++"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk1MDc3NzU=","nameWithOwner":"elastic/elasticsearch","createdAt":"2010-02-08T13:20:56Z","description":"Open Source, Distributed, RESTful Search Engine","homepageUrl":"https://www.elastic.co/products/elasticsearch","forkCount":16646,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":48730},"languages":{"nodes":[{"name":"Shell"},{"name":"Python"},{"name":"Perl"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk2Mjk2Nzkw","nameWithOwner":"spring-projects/spring-boot","createdAt":"2012-10-19T15:02:57Z","description":"Spring Boot","homepageUrl":"https://spring.io/projects/spring-boot","forkCount":29828,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":47543},"languages":{"nodes":[{"name":"Java"},{"name":"Smarty"},{"name":"HTML"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk4MTk3NTM3Mg==","nameWithOwner":"kdn251/interviews","createdAt":"2017-02-14T18:19:25Z","description":"Everything you need to know to get the job.","homepageUrl":"https://www.youtube.com/channel/UCKvwPt6BifPP54yzH99ff1g?view_as=subscriber","forkCount":9271,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":43824},"languages":{"nodes":[{"name":"Java"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxNTE4MzQwNjI=","nameWithOwner":"doocs/advanced-java","createdAt":"2018-10-06T11:38:30Z","description":"😮 互联网 Java 工程师进阶知识完全扫盲：涵盖高并发、分布式、高可用、微服务、海量数据处理等领域知识，后端同学必看，前端同学也可学习","homepageUrl":"https://doocs.github.io/advanced-java","forkCount":12046,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":43275},"languages":{"nodes":[{"name":"Java"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk3NTA4NDEx","nameWithOwner":"ReactiveX/RxJava","createdAt":"2013-01-08T20:11:48Z","description":"RxJava – Reactive Extensions for the JVM – a library for composing asynchronous and event-based programs using observable sequences for the Java VM.","homepageUrl":"","forkCount":7130,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":42594},"languages":{"nodes":[{"name":"Shell"},{"name":"Java"},{"name":"CSS"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkyMDMwMDE3Nw==","nameWithOwner":"google/guava","createdAt":"2014-05-29T16:23:17Z","description":"Google core libraries for Java","homepageUrl":"","forkCount":8340,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":37162},"languages":{"nodes":[{"name":"Java"},{"name":"CSS"},{"name":"Shell"}]}}]}}}
```

New GraphQL query which uses `search.pageInfo.endCursor` value from previous response:
```text
{"query":"query{search(after:\"Y3Vyc29yOjEw\",first:10,query:\"forks:>1000 language:java\",type:REPOSITORY){pageInfo{endCursor} nodes{__typename ... on App{id} ... on MarketplaceListing{id} ... on Issue{id} ... on Organization{id} ... on PullRequest{id} ... on Repository{id nameWithOwner createdAt description homepageUrl forkCount isArchived isFork isPrivate stargazers{totalCount} languages(first:3){nodes{name}}} ... on User{id}}}}","variables":{}}
```

Second page of response (new `search.pageInfo.endCursor`, which can be used later):
```json
{"data":{"search":{"pageInfo":{"endCursor":"Y3Vyc29yOjIw"},"nodes":[{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk1MTUyMjg1","nameWithOwner":"square/okhttp","createdAt":"2012-07-23T13:42:55Z","description":"Square’s meticulous HTTP client for Java and Kotlin.","homepageUrl":"https://square.github.io/okhttp/","forkCount":8020,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":36961},"languages":{"nodes":[{"name":"Shell"},{"name":"Java"},{"name":"Kotlin"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxMTQ4NzUz","nameWithOwner":"spring-projects/spring-framework","createdAt":"2010-12-08T04:04:45Z","description":"Spring Framework","homepageUrl":"https://spring.io/projects/spring-framework","forkCount":24793,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":36862},"languages":{"nodes":[{"name":"Groovy"},{"name":"Java"},{"name":"HTML"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk4OTIyNzU=","nameWithOwner":"square/retrofit","createdAt":"2010-09-06T21:39:43Z","description":"Type-safe HTTP client for Android and Java by Square, Inc.","homepageUrl":"https://square.github.io/retrofit/","forkCount":6518,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":35549},"languages":{"nodes":[{"name":"Shell"},{"name":"Java"},{"name":"CSS"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxMjc5ODgwMTE=","nameWithOwner":"macrozheng/mall","createdAt":"2018-04-04T01:11:44Z","description":"mall项目是一套电商系统，包括前台商城系统及后台管理系统，基于SpringBoot+MyBatis实现，采用Docker容器化部署。 前台商城系统包含首页门户、商品推荐、商品搜索、商品展示、购物车、订单流程、会员中心、客户服务、帮助中心等模块。 后台管理系统包含商品管理、订单管理、会员管理、促销管理、运营管理、内容管理、统计报表、财务管理、权限管理、设置等模块。","homepageUrl":"http://www.macrozheng.com/admin/","forkCount":14539,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":34174},"languages":{"nodes":[{"name":"Java"},{"name":"TSQL"},{"name":"Shell"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk0NzEwOTIw","nameWithOwner":"apache/dubbo","createdAt":"2012-06-19T07:56:02Z","description":"Apache Dubbo is a high-performance, java based, open source RPC framework.","homepageUrl":"http://dubbo.apache.org","forkCount":21000,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":32196},"languages":{"nodes":[{"name":"Java"},{"name":"Shell"},{"name":"Thrift"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxOTE0ODk0OQ==","nameWithOwner":"PhilJay/MPAndroidChart","createdAt":"2014-04-25T14:29:47Z","description":"A powerful 🚀 Android chart view / graph view library, supporting line- bar- pie- radar- bubble- and candlestick charts as well as scaling, dragging and animations.","homepageUrl":"","forkCount":7863,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":30606},"languages":{"nodes":[{"name":"Java"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxMTI2NzUwOQ==","nameWithOwner":"bumptech/glide","createdAt":"2013-07-08T22:52:33Z","description":"An image loading and caching library for Android focused on smooth scrolling","homepageUrl":"https://bumptech.github.io/glide/","forkCount":5313,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":28972},"languages":{"nodes":[{"name":"Java"},{"name":"Shell"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk3MDE5ODg3NQ==","nameWithOwner":"airbnb/lottie-android","createdAt":"2016-10-06T22:42:42Z","description":"Render After Effects animations natively on Android and iOS, Web, and React Native","homepageUrl":"http://airbnb.io/lottie/","forkCount":4595,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":28724},"languages":{"nodes":[{"name":"Java"},{"name":"Kotlin"},{"name":"Shell"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnk2NDU1ODE0Mw==","nameWithOwner":"Blankj/AndroidUtilCode","createdAt":"2016-07-30T18:18:32Z","description":":fire: Android developers should collect the following utils(updating).","homepageUrl":"https://blankj.com/2016/07/31/android-utils-code/","forkCount":8901,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":26938},"languages":{"nodes":[{"name":"Java"},{"name":"Groovy"},{"name":"Kotlin"}]}},{"__typename":"Repository","id":"MDEwOlJlcG9zaXRvcnkxMDgyNTI4OTI=","nameWithOwner":"proxyee-down-org/proxyee-down","createdAt":"2017-10-25T10:07:27Z","description":"http下载工具，基于http代理，支持多连接分块下载","homepageUrl":null,"forkCount":4685,"isArchived":false,"isFork":false,"isPrivate":false,"stargazers":{"totalCount":25615},"languages":{"nodes":[{"name":"Java"},{"name":"JavaScript"},{"name":"HTML"}]}}]}}}
```



5\. Setup Instructions
----------------------

1. Clone this repository using `git clone git@github.com:user501254/typed-ghgraphql.git` command.

2. Import as sbt project in [IntelliJ](https://www.jetbrains.com/idea/) or [Eclipse](https://www.eclipse.org/ide/) IDE.

3. Obtain GitHub Personal OAuth token key following [these instructions][2].

4. Copy `src/main/resources/application.conf.example` as `application.conf` and set the required key.

5. Run The `Main` class file.

[2]:(https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).



6\. Contributors
----------------

- Ashesh Singh
- Samujjwaal Dey