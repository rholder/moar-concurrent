/*
 * Copyright 2012-2015 Ray Holder
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

import org.junit.Assert;
import org.junit.Test;

public class HeapQueueingStrategyTest {

    private Runtime runtime = Runtime.getRuntime();

    @Test
    public void strategyWhenThresholdExceededAndReduced() throws InterruptedException {

        // start from a reasonable garbage collected memory baseline, and sleep a bit to make sure it probably happened
        runtime.gc();
        Thread.sleep(5000);

        // current % of used heap
        long freeHeapSpace = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory());
        double currentPercentFree = ((double)freeHeapSpace) / runtime.maxMemory();

        // current % + 10%
        double thresholdHeapSpace = currentPercentFree + 0.10;

        // current % + 15%
        double newHeapSpace = currentPercentFree + 0.15;
        int bytesTillOverThreshold = (int)((runtime.maxMemory() * newHeapSpace) - (runtime.maxMemory() * currentPercentFree));

        // create a strategy that slows down after 10% more of the heap is used
        QueueingStrategy<String> strategy = QueueingStrategies.newHeapQueueingStrategy(thresholdHeapSpace, 500, 10);

        // this should be fast
        long start = System.currentTimeMillis();
        for(int i = 0; i < 100; i++) {
            strategy.onBeforeAdd("foo");
        }
        long end = System.currentTimeMillis();

        // use up 15% more heap
        byte[] chunkOfRam = new byte[bytesTillOverThreshold];

        // this should queue up slowly now
        long overStart = System.currentTimeMillis();
        for(int i = 0; i < 100; i++) {
            strategy.onBeforeAdd("foo");
        }
        long overEnd = System.currentTimeMillis();
        Assert.assertTrue("Expected queueing after heap threshold was reached to be at least twice as slow.",
                (2*(end - start)) < (overEnd - overStart));

        // free up some heap, ask nicely to garbage collect, and sleep a bit to ensure the gc() probably did its thing
        chunkOfRam = null;
        runtime.gc();
        Thread.sleep(5000);

        // this should be fast again
        long underStart = System.currentTimeMillis();
        for(int i = 0; i < 100; i++) {
            strategy.onBeforeAdd("foo");
        }
        long underEnd = System.currentTimeMillis();
        Assert.assertTrue("Expected queueing after heap threshold was acceptable again to be at least twice as fast.",
                (2 * (underEnd - underStart)) < (overEnd - overStart));
    }
}
