/*
 * Copyright 2012-2013 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
