[![Build Status](https://travis-ci.org/rholder/moar-concurrent.png)](https://travis-ci.org/rholder/moar-concurrent)

This module contains a collection of useful builders and concurrency classes to
assist in modeling complex or overly tweakable concurrent processing pipelines.

Maven
--------

    <dependency>
      <groupId>com.github.rholder</groupId>
      <artifactId>moar-concurrent</artifactId>
      <version>1.0.0</version>
    </dependency>

Gradle
--------

    compile "com.github.rholder:moar-concurrent:1.0.0"

Example
--------
When 85% of heap is in use, start exponentially delaying additional enqueues up
to a max of 5000 ms, garbage collecting after every 10000 dequeues.

    QueueingStrategy<String> strategy = QueueingStrategies.newHeapQueueingStrategy(0.85, 5000, 10000);
    BlockingQueue<String> strategicQueue = StrategicQueues.newStrategicLinkedBlockingQueue(strategy);

    strategicQueue.add("some queue stuff");

Javadoc can be found [here](http://rholder.github.com/moar-concurrent/).
