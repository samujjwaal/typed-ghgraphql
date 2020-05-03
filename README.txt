# Course Project
### Description: object-oriented pure functional design and implementation of a [GraphQL](https://graphql.org) client framework for [Github](https://github.com/) as an I/O monad.
### Grade: 25% with some small bonus - read below.
#### You can obtain this Git repo using the command git clone git@bitbucket.org:cs474_spring2020/courseproject.git.

## Preliminaries
As part of the previous homework assignments you gained experience with creating and managing your Git repositories, you have learned many design patterns, you created your model and the object-oriented design of a design pattern code generator, you learned to create JUnit or Cucumber or FlatSpec tests, you created your SBT or Gradle build scripts, and you completed your first IntelliJ plugin that generated and manipulated the source code of the loaded  Intellij projects. Congratulations!

If you have done your homeworks, you can skip the rest of the prelimininaries. Your instructor created a team for this class named CS474_Spring2020. Please contact your TA, [Mr. Mohammed Siddiq](msiddi56@uic.edu) using your UIC.EDU email account and he will add you to the team repo as developers, since Mr.Siddiq already has the admin privileges. Please use your emails from the class registration roster to add you to the team and you will receive an invitation from BitBucket to join the team. Since it is a large class, please use your UIC email address for communications or Piazza and avoid emails from other accounts like funnybunny1998@gmail.com. If you don't receive a response within 24 hours, please contact us via Piazza, since it may be a case that your direct emails went to the spam folder.

If you haven't already done so, please create create your account at [BitBucket](https://bitbucket.org/), a Git repo management system. It is imperative that you use your UIC email account that has the extension @uic.edu. Once you create an account with your UIC address, BibBucket will assign you an academic status that allows you to create private repos. Bitbucket users with free accounts cannot create private repos, which are essential for submitting your homeworks and the course project. If you have a problem with obtaining the academic account with your UIC.EDU email address, please contact Atlassian's license and billing team and ask them to enable your academic account by filling out the [Atlassian Bitbucket academic account request form](https://www.atlassian.com/software/views/bitbucket-academic-license).

Next, if you haven't done so, you will install [IntelliJ](https://www.jetbrains.com/student/) with your academic license, the JDK, the Scala runtime and the IntelliJ Scala plugin, the [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html) or the [Gradle build tool](https://gradle.org/) and make sure that you can create, compile, and run Java and Scala programs. Please make sure that you can run [various Java tools from your chosen JDK](https://docs.oracle.com/en/java/javase/index.html).

Just to remind you, in this like all other homeworks and in the course project you will use logging and configuration management frameworks. You will comment your code extensively and supply logging statements at different logging levels (e.g., TRACE, INFO, ERROR) to record information at some salient points in the executions of your programs. All input and configuration variables must be supplied through configuration files -- hardcoding these values in the source code is generally prohibited and will be punished by taking a large percentage of points from your total grade! You are expected to use [Logback](https://logback.qos.ch/) and [SLFL4J](https://www.slf4j.org/) for logging and [Typesafe Conguration Library](https://github.com/lightbend/config) for managing configuration files. These and other libraries should be imported into your project using your script [build.sbt](https://www.scala-sbt.org/1.0/docs/Basic-Def-Examples.html) or [gradle script](https://docs.gradle.org/current/userguide/writing_build_scripts.html). These libraries and frameworks are widely used in the industry, so learning them is the time well spent to improve your resumes.

## WARNING 
There are a few implementations of the OO functional GraphQL frameworks on the Internet. I know about many of them. You can study these implementations and feel free to use the ideas in your own implementation, and you must acknowledge what you use in your README. However, blindly copying large parts of some existing implementation in your code will result in receiving the grade F for the entire course with the transfer of your case of plagiarism to the Dean of Students Office, which will be followed with severe penalties. Most likely, you will be suspended or complete dismissed from the program in the worst case. Please do not plagiarize existing implementations, it is not worth it! However, if you find some functions of classes that you can reuse in your project, please feel free to do so as long as it does not constitute a major part of your project submission and you acknowledge the reuse in your project documentation.

## Introduction
The goal of this project is to gain experience with pure functional object-oriented design of a practically useful and important framework for composing and executing external GraphQL commands from Scala client programs and obtaining and processing the results of these executions. This homework is based on the material from the textbook on Functional Programming in Scala by Paul Chiusano and R�nar Bjarnason and it is modeled on the pure functional design principles described in sections 6 and 7 of the textbook.

In this homework, you will create an object-oriented design and implementation of a program that extracts and organizes git repositories data from [Github](https://github.com/). As the first step, you will create [your developer account at Github](https://developer.github.com/v4/) and you will obtain your authorization key. The [Github schema](https://developer.github.com/v4/public_schema/) is publicly available and you will study it to understand the organization of data items on Github and relationships among them. That is, for a given repo, you may obtain the information on all contributors, commits, URL for each commit, the changeset and many other metadata. For a given contributor, you may determine all projects that this contributor participated in and what type of code s/he committed. A part of this homework is that you come up with your own GraphQL queries that will slice and dice the Github schema and you will create a model and object-oriented design of your program based on your model.

This homework is based on GraphQL and it is a relatively new technology released by Facebook as an open-source software and it is already widely used. There are many resources on the Internet including its [official specification](https://graphql.github.io/graphql-spec/June2018/), [query tools](https://graphcms.com/blog/top-10-graphql-tools-for-2019/), and a [compendium of useful links](https://github.com/chentsulin/awesome-graphql#lib-java) in addition to youtube videos and various documents and examples on [Stackoverflow](https://stackoverflow.com/search?q=graphql). It is beneficial that you learn GraphQL as you go, since it is a simple declarative language that can be nicely intergrated into programs written in object-oriented languages.

This course project script is written using a retroscripting technique, in which the project outlines are generally and loosely drawn, and the individual students improvise to create the implementation that fits their refined objectives. In doing so, students are expected to stay within the basic requirements of the project and they are free to experiments. That is, it is impossible that two non-collaborating groups will submit similar project implementations! Asking questions is important, so please ask away at Piazza!

## Functionality
Once you installed and configured your Github developer account, your job is to create various GraphQL queries to understand how the process works. Consider a snippet of the Scala code below that creates a basic HTTP client for Github GraphQL endpoint and serves a basic query and obtains the response. In it, a simple GraphQL query for Github is formed, a connection to the Github endpoint is created, the query is submitted and the response is obtained. Using JSON converters the response is converted into Scala classes. Of course, feel free to experiment and use third-party libraries for your client, but remember that your time is limited and you may not be able to explore the majority of the available tools on the Internet for GraphQL.
```scala
val BASE_GHQL_URL = "https://api.github.com/graphql"
val temp="{viewer {email login url}}"
implicit val formats = DefaultFormats

val client = HttpClientBuilder.create.build
val httpUriRequest = new HttpPost(BASE_GHQL_URL)
httpUriRequest.addHeader("Authorization", "Bearer d6150dbc62ccafe310080e1b37babe13f46dbbbc")
httpUriRequest.addHeader("Accept", "application/json")
val gqlReq = new StringEntity("{\"query\":\"" + temp + "\"}" )
httpUriRequest.setEntity(gqlReq)

val response = client.execute(httpUriRequest)
System.out.println("Response:" + response)
response.getEntity match {
    case null => System.out.println("Response entity is null")
    case x if x != null => {
      val respJson = fromInputStream(x.getContent).getLines.mkString
      System.out.println(respJson)
      val viewer = parse(respJson).extract[Data]
      System.out.println(viewer)
      System.out.println(write(viewer))
  }
}
```

In your course project you will create an extensible framework with typed GraphQL commands that the clients will execute using such monadic combinators as **flatMap**, **Option**, and **Future** among the others. Each GraphQL external command type will be implemented using the pattern [Builder](https://en.wikipedia.org/wiki/Builder_pattern). Additional 1% bonus will be given if you implement this pattern using [phantom types](https://medium.com/@maximilianofelice/builder-pattern-in-scala-with-phantom-types-3e29a167e863). Here is a general outline for the code that a programmer will write to execute external commands using your framework.
```scala
val gitHubObject:Option[GHQLRespone] = (new Github).Builder().setAuthorization(BEARER,GetAuthCodeFromConfig()).setHeader(ACCEPT, APPJSON).build()
val result = gitHubObject.flatMap((new QueryCommand()).setRepo(ALLREPOS).setLanguages(List(JAVA, SCALA)).setCommits(_>200).build()).filter((new Stars(_>10))).filter(Contributors(_>10)).flatMap(PullCommits(Last(10)))
```
You see how clean the code is and we use the Scala compiler to type check it to ensure that mistakes are not made unlike typing string literals in the code where we can easily make a typo mistake and the program will crash or return an exception or incorrect results. Also, this code is clean and compact, it is easy to read, it is not cluttered with extemporaneously introduced variables like gqlReq. All error and exception handling is buried inside your framework. Your client programmers know your commands and how to compose them. Low level API calls that deal with network data transfer and data constructions and parsing are buried in your framework.

As you can see, your work can be broken down in the following three phases. In the first phase, you will select what subset of GraphQL commands you choose to support and how you will design the type system to hide the complexity of constructing these commands with proper command line parameters and the external use values (e.g., an authentication token). Keep in mind that your design must provide sufficient information hiding and allow programmers to extend it at the same time (i.e., you may consider the use of sealed traits). Executing each command results in data, so you will design data parsers for specific commands in which you will embed the information of how the resulting data is structured. For example, the execution of the command query may result in the following response.
```
{                                               
  "data": {                                     
    "googleRepo": {                             
      "name": "WebFundamentals",                
      "owner": {                                
        "id": "MDEyOk9yZ2FuaXphdGlvbjEzNDIwMDQ="
      }                                         
    },                                          
    "facebookRepo": {                           
      "name": "react",                          
      "owner": {                                
        "id": "MDEyOk9yZ2FuaXphdGlvbjY5NjMx"    
      }                                         
    }                                           
  }                                             
}                                               
```
It would be unreasonable to expose the user of your framework to this table data. You will provide combinators like `filter(_.equalTo(RepoName()) && _.equalTo(OwnerId()))` that will return the projection of the table, specifically the results of the name of the repo and the owner id. Thus, the knowledge of the resulting data structure/format will be encapsulated in the command that you design and implement.

In the second phase, you will create your underlying implementation of the actual GraphQL command executor and data retriever that will be buried in your framework, i.e., the user of your framework will not be exposed to the complexities of the actual interactions with GraphQL commands and raw JSON response data. Finally, in the third phase of the course project, you will test your framework and you will create examples and documentation for your users that will be included in your submission along with the design document that describes a model of your GraphQL command execution, its constraints and rules, your design of the classes, traits, and monadic combinators. Of course, your document should start with example program(s) that you write that use your framework, which also serve as the test cases that verify its behavior.

## Baseline Submission
Your baseline project submission should include your application design with a conceptual explanation in the document or in the comments in the source code of the architecture and design choices that you made, and the documentation that describe the build, deployment and the runtime, to be considered for grading. Your project submission should include all your source code as well as non-code artifacts (e.g., resource files if applicable), your project should be buildable using SBT. Simply copying some instrumentation examples from open-source projects and modifying them a bit will result in desk-rejecting your submission.

## Piazza collaboration
You can post questions and replies, statements, comments, discussion, etc. on Piazza. For this homework, feel free to share your ideas, mistakes, code fragments, commands from scripts, and some of your technical solutions with the rest of the class, and you can ask and advise others using Piazza on where resources and sample programs can be found on the internet, how to resolve dependencies and configuration issues. When posting question and answers on Piazza, please select the appropriate folder, i.e., courseproject to ensure that all discussion threads can be easily located. Active participants and problem solvers will receive bonuses from the big brother :-) who is watching your exchanges on Piazza (i.e., your class instructor). However, *you must not post your dockerfile or your source code!*

## Git logistics
**This is a group project,** with at least one and at most five members allowed in a group. Each student can participate in at most one group; enrolling in more than one group will result in the grade zero. Each group will select a group leader who will create a private fork and will invite the other group classmates with the write access to that fork repo. Each submission will include the names of all groupmates in the README.md and all groupmates will receive the same grade for this course project submission. Group leaders with successful submissions and good quality work will receive an additional 2% bonus for their management skills - it applied only to groups with two or more members. Please read the syllabus for the advice on how to select your team members.

If you submitted your previous homework(s), it means that you were already added as a member of CS474_Spring2020 team in Bitbucket and you will see the course project repo. You will fork this repository and your fork will be private, no one else besides you, your forkmates, the TA and your course instructor will have access to your fork. Please remember to grant a read (or write) access to your repository to your TA and your instructor and write access to your forkmates. You can commit and push your code as many times as you want. Your code will not be visible and it should not be visible to other students except for your forkmates, of course. When you push your project, your instructor and the TA will see you code in your separate private fork. Making your fork public or inviting other students except for your forkmates to join your fork before the submission deadline will result in losing your grade. For grading, only the latest push timed before the deadline will be considered. **If you push after the deadline, your grade for the homework will be zero**. For more information about using the Git and Bitbucket specifically, please use this [link as the starting point](https://confluence.atlassian.com/bitbucket/bitbucket-cloud-documentation-home-221448814.html). For those of you who struggle with the Git, I recommend a book by Ryan Hodson on Ry's Git Tutorial. The other book called Pro Git is written by Scott Chacon and Ben Straub and published by Apress and it is [freely available](https://git-scm.com/book/en/v2/). There are multiple videos on youtube that go into details of the Git organization and use.

Please follow this naming convention while submitting your work : "Firstname_Lastname_project" without quotes, where the group leader will specify her/his first and last names **exactly as the group leader is registered with the University system**, so that we can easily recognize your submission. I repeat, make sure that you will give both your TA and the course instructor the read access to your *private forked repository*.

## Discussions and submission
You can post questions and replies, statements, comments, discussion, etc. on Piazza. Remember that you cannot share your code and your solutions privately, but you can ask and advise others using Piazza and StackOverflow or some other developer networks where resources and sample programs can be found on the Internet, how to resolve dependencies and configuration issues. Yet, your implementation should be your own and you cannot share it. Alternatively, you cannot copy and paste someone else's implementation and put your name on it. Your submissions will be checked for plagiarism. **Copying code from your classmates or from some sites on the Internet will result in severe academic penalties up to the termination of your enrollment in the University**. When posting question and answers on Piazza, please select the appropriate folder, i.e., **course project** to ensure that all discussion threads can be easily located.


## Submission deadline and logistics
Wednesday, May 6, 2020 at 9:00PM via the bitbucket repository. Your submission will include the source code, your documentation with instructions and detailed explanations on how to assemble and deploy your program both in IntelliJ and CLI SBT, and a document that explains how you built and deployed your program and what your experiences are with instrumenting open-source Java programs, and the limitations of your implementation. Again, do not forget, please make sure that you will give both your TA and your instructor the read access to your private forked repository. Your name should be shown in your README.md file and other documents. Your code should compile and run from the command line using the commands like ```sbt clean compile test``` and from the docker image. Naturally, you project should be IntelliJ friendly, i.e., your graders should be able to import your code into IntelliJ and run from there. Use .gitignore to exlude files that should not be pushed into the repo.

## Evaluation criteria
- the maximum grade for this course project is 25% with the bonus up to 1% for being the group leader for a group with more than two members. Points are subtracted from this maximum grade: for example, saying that 2% is lost if some requirement is not completed means that the resulting grade will be 25%-2% => 23%; if the core functionality does not work, no bonus points will be given;
- the code does not work in that it does not produce a correct output or crashes: up to 25% lost;
- insufficient comments in your code: up to 15% lost;
- insufficient tests in your codebase: up to 15% lost;
- not having tests that test the functionality of your command implementations: up to 25% lost;
- missing essential comments and explanations from the source code that you wrote: up to 20% lost;
- your Scala code is simply a version of imperative Java code with many mutable variables exposed to your clients: up to 15% lost;
- your code does not have sufficient comments or your accompanying documents do not contain a description of how you designed and implemented the instrumenter: up to 20% lost;
- the documentation exists but it is insufficient to understand : up to 20% lost;
- the minimum grade for this course project cannot be less than zero.

That's it, folks!