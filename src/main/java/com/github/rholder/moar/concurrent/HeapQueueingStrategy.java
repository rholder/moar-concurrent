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
 * This QueueingStrategy slows down the rate at which items can be added to a
 * queue based on the amount of free heap space available.
 *
 * @author rholder
 * @param <E> the type of elements held in the target queue
 */
public class HeapQueueingStrategy<E> implements QueueingStrategy<E> {

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private long minimumHeapSpaceBeforeFlowControl;
    private long dequeueHint;
    private long dequeued;
    private double c;

    /**
     * Construct a new {@link HeapQueueingStrategy} with the given parameters.
     *
     * @param percentOfHeapBeforeFlowControl the percentage of the heap that must be available before the
     *                                       queue begins to start delaying addition operations
     * @param maxDelay                       the maximum amount of time to delay an addition operation in
     *                                       milliseconds
     * @param dequeueHint                    after this many dequeue operations, signal the JVM to run a
     *                                       garbage collection
     */
    public HeapQueueingStrategy(double percentOfHeapBeforeFlowControl, long maxDelay, long dequeueHint) {
        // convert percentage to the actual heap threshold
        if (percentOfHeapBeforeFlowControl > 0.0 || percentOfHeapBeforeFlowControl <= 1.0) {
            minimumHeapSpaceBeforeFlowControl = Math.round(RUNTIME.maxMemory() * percentOfHeapBeforeFlowControl);
        } else {
            throw new RuntimeException("Range must be between 0.0 and 1.0");
        }

        this.dequeueHint = dequeueHint;
        this.dequeued = 0L; // use this as a hint about when to ask for GC

        // constant used to roll in to y = x^2 * c, this will control the rate at which the queue is staggered
        c = maxDelay / ((double) minimumHeapSpaceBeforeFlowControl * minimumHeapSpaceBeforeFlowControl);
    }

    /**
     * Block for a varying amount based on how close the system is to the max
     * heap space. Only kick in when we have passed the
     * percentOfHeapBeforeFlowControl threshold.
     *
     * @param value value that is to be added to the queue
     */
    public void onBeforeAdd(E value) {
        long freeHeapSpace = RUNTIME.freeMemory() + (RUNTIME.maxMemory() - RUNTIME.totalMemory());

        // start flow control if we cross the threshold
        if (freeHeapSpace < minimumHeapSpaceBeforeFlowControl) {

            // x indicates how close we are to overflowing the heap
            long x = minimumHeapSpaceBeforeFlowControl - freeHeapSpace;

            long delay = Math.round(x * (x * c)); // delay = x^2 * c

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onAfterAdd() {
        // do nothing
    }

    public void onBeforeRemove() {
        // do nothing
    }

    /**
     * Increment the count of removed items from the queue. Calling this will
     * optionally request that the garbage collector run to free up heap space
     * for every nth dequeue, where n is the value set for the dequeueHint.
     *
     * @param value value that was removed from the queue
     */
    public void onAfterRemove(E value) {
        if (value != null) {
            dequeued++;
            if (dequeued % dequeueHint == 0) {
                RUNTIME.gc();
            }
        }
    }
}
