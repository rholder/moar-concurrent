package com.github.rholder.moar.concurrent;

/**
 * This is a helper class for instantiating available QueueingStrategy's.
 *
 * @author rholder
 */
public class QueueingStrategies {

    /**
     * Construct a new {@link HeapQueueingStrategy} with the given parameters.
     *
     * For example, (0.85, 5000, 10000) translates to when 85% of heap is in
     * use, start exponentially delaying additional enqueues up to a max of
     * 5000 ms, garbage collecting after every 10000 dequeues to ensure that
     *
     * @param percentOfHeapBeforeFlowControl the percentage of the heap that must be available before the
     *                                       queue begins to start delaying addition operations
     * @param maxDelay                       the maximum amount of time to delay an addition operation in
     *                                       milliseconds
     * @param dequeueHint                    after this many dequeue operations, signal the JVM to run a
     *                                       garbage collection
     */
    public static <V> QueueingStrategy<V> newHeapQueueingStrategy(double percentOfHeapBeforeFlowControl,
                                                                  long maxDelay,
                                                                  long dequeueHint) {
        return new HeapQueueingStrategy<V>(percentOfHeapBeforeFlowControl, maxDelay, dequeueHint);
    }
}
