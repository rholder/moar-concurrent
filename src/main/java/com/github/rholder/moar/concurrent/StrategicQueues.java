package com.github.rholder.moar.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a helper class for instantiating StrategicBlockingQueue's.
 *
 * @author rholder
 */
public class StrategicQueues {

    /**
     * Return a StrategicBlockingQueue backed by a LinkedBlockingQueue using
     * the given QueueingStrategy.
     *
     * @param queueingStrategy the QueueingStrategy to use
     * @param <V>              the type of elements held in this collection
     */
    public static <V> StrategicBlockingQueue<V> newStrategicLinkedBlockingQueue(QueueingStrategy<V> queueingStrategy) {
        return new StrategicBlockingQueue<V>(new LinkedBlockingQueue<V>(), queueingStrategy);
    }

    /**
     * Return a StrategicBlockingQueue backed by an ArrayBlockingQueue of the
     * given capacity using the given QueueingStrategy.
     *
     * @param capacity         the capacity of the ArrayBlockingQueue
     * @param queueingStrategy the QueueingStrategy to use
     * @param <V>              the type of elements held in this collection
     */
    public static <V> StrategicBlockingQueue<V> newStrategicArrayBlockingQueue(int capacity, QueueingStrategy<V> queueingStrategy) {
        return new StrategicBlockingQueue<V>(new ArrayBlockingQueue<V>(capacity), queueingStrategy);
    }

    /**
     * Return a StrategicBlockingQueue backed by the given BlockingQueue using
     * the given QueueingStrategy.
     *
     * @param blockingQueue    the BlockingQueue to back the returned instance
     * @param queueingStrategy the QueueingStrategy to use
     * @param <V>              the type of elements held in this collection
     */
    public static <V> StrategicBlockingQueue<V> newStrategicBlockingQueue(BlockingQueue<V> blockingQueue, QueueingStrategy<V> queueingStrategy) {
        return new StrategicBlockingQueue<V>(blockingQueue, queueingStrategy);
    }
}
