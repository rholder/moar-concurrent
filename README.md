[![Build Status](http://img.shields.io/travis/rholder/moar-concurrent.svg)](https://travis-ci.org/rholder/moar-concurrent) [![Latest Version](http://img.shields.io/badge/latest-1.0.3-brightgreen.svg)](https://github.com/rholder/moar-concurrent/releases/tag/v1.0.3) [![License](http://img.shields.io/badge/license-apache%202-brightgreen.svg)](https://github.com/rholder/moar-concurrent/blob/master/LICENSE)

##What is this?
This module contains a collection of useful builders and concurrency classes to
assist in modeling complex or overly tweakable concurrent processing pipelines.

##Maven
```xml
    <dependency>
      <groupId>com.github.rholder</groupId>
      <artifactId>moar-concurrent</artifactId>
      <version>1.0.3</version>
    </dependency>

```
##Gradle
```groovy
    compile "com.github.rholder:moar-concurrent:1.0.3"
```

##StrategicQueues
When 85% of heap is in use, start exponentially delaying additional enqueues up
to a max of 5000 ms, garbage collecting after every 10000 dequeues.
```java
    QueueingStrategy<String> strategy = QueueingStrategies.newHeapQueueingStrategy(0.85, 5000, 10000);
    BlockingQueue<String> strategicQueue = StrategicQueues.newStrategicLinkedBlockingQueue(strategy);

    strategicQueue.add("some queue stuff");
```

##Documentation
Javadoc can be found [here](http://rholder.github.com/moar-concurrent/javadoc/1.0.3).

##License
The moar-concurrent module is released under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0).

##Contributors
* Jason Dunkelberger (dirkraft)
* Megan Galloway (megallo)