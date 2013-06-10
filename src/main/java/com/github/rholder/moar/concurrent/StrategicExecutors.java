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

import com.github.rholder.moar.concurrent.thread.BalancingThreadPoolExecutor;
import com.github.rholder.moar.concurrent.thread.CallerBlocksPolicy;
import com.github.rholder.moar.concurrent.thread.MXBeanThreadProfiler;
import com.github.rholder.moar.concurrent.thread.ThreadProfiler;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Provide some convenience functions for creating new
 * {@link BalancingThreadPoolExecutor} instances.
 */
public class StrategicExecutors {

    public static final float DEFAULT_SMOOTHING_WEIGHT = 0.5f;
    public static final int DEFAULT_BALANCE_AFTER = 23;

    /**
     * Return a capped {@link BalancingThreadPoolExecutor} with the given
     * maximum number of threads and target utilization. The default smoothing
     * weight and balance after constants are used.
     *
     * @param maxThreads        maximum number of threads to use
     * @param targetUtilization a float between 0.0 and 1.0 representing the
     *                          percentage of the total CPU time to be used by
     *                          this pool
     */
    public static BalancingThreadPoolExecutor newBalancingThreadPoolExecutor(int maxThreads,
                                                                             float targetUtilization) {
        return newBalancingThreadPoolExecutor(maxThreads, targetUtilization, DEFAULT_SMOOTHING_WEIGHT, DEFAULT_BALANCE_AFTER);
    }

    /**
     * Return a capped {@link BalancingThreadPoolExecutor} with the given
     * maximum number of threads, target utilization, smoothing weight, and
     * balance after values.
     *
     * @param maxThreads        maximum number of threads to use
     * @param targetUtilization a float between 0.0 and 1.0 representing the
     *                          percentage of the total CPU time to be used by
     *                          this pool
     * @param smoothingWeight   smooth out the averages of the CPU and wait time
     *                          over time such that tasks aren't too heavily
     *                          skewed with old or spiking data
     * @param balanceAfter      balance the thread pool after this many tasks
     *                          have run
     */
    public static BalancingThreadPoolExecutor newBalancingThreadPoolExecutor(int maxThreads,
                                                                             float targetUtilization,
                                                                             float smoothingWeight,
                                                                             int balanceAfter) {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new CallerBlocksPolicy());
        return newBalancingThreadPoolExecutor(tpe, targetUtilization, smoothingWeight, balanceAfter);
    }

    /**
     * Return a {@link BalancingThreadPoolExecutor} with the given
     * {@link ThreadPoolExecutor}, target utilization, smoothing weight, and
     * balance after values.
     *
     * @param tpe               the underlying executor to use for this instance
     * @param targetUtilization a float between 0.0 and 1.0 representing the
     *                          percentage of the total CPU time to be used by
     *                          this pool
     * @param smoothingWeight   smooth out the averages of the CPU and wait time
     *                          over time such that tasks aren't too heavily
     *                          skewed with old or spiking data
     * @param balanceAfter      balance the thread pool after this many tasks
     *                          have run
     */
    public static BalancingThreadPoolExecutor newBalancingThreadPoolExecutor(ThreadPoolExecutor tpe,
                                                                             float targetUtilization,
                                                                             float smoothingWeight,
                                                                             int balanceAfter) {
        ThreadProfiler tp = new MXBeanThreadProfiler();
        return new BalancingThreadPoolExecutor(tpe, tp, targetUtilization, smoothingWeight, balanceAfter);
    }
}
