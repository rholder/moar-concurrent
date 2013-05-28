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

package com.github.rholder.moar.concurrent.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.ceil;

/**
 * This is a rough implementation of an auto-balancing thread pool to optimize
 * for the following condition: optimal pool size = N * U * (1 + (W/C)) where N
 * is the number of CPU's, U is the desired utilization, W is the time each
 * thread spends waiting, and C is the time each thread spends using the CPU.
 */
public class BalancingThreadPoolExecutor extends AbstractExecutorService {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final int CPUS = Runtime.getRuntime().availableProcessors();

    private volatile long avgThreadCpuTime = 1;
    private volatile long avgThreadTotalTime = 1;

    private float targetUtilization;
    private long sampleMs;

    private ConcurrentHashMap<Thread, Tracking> liveThreads;
    private ThreadPoolExecutor threadPoolExecutor;

    public BalancingThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor, float targetUtilization, long sampleMs) {
        if (targetUtilization <= 0.0 || targetUtilization > 1.0) {
            throw new IllegalArgumentException();
        }

        if (threadPoolExecutor == null) {
            throw new NullPointerException();
        }

        this.threadPoolExecutor = threadPoolExecutor;
        this.targetUtilization = targetUtilization;
        this.sampleMs = sampleMs;
        this.liveThreads = new ConcurrentHashMap<Thread, Tracking>();
        initCollectorThread();
    }

    // TODO add regularly recurring balance() call thread
    public void balance() {
        // only try to balance when we're not terminating
        if(!isTerminated()) {
            long cpuTime = getAvgThreadCpuTime();
            long totalTime = getAvgThreadTotalTime();
            long waitTime = totalTime - cpuTime;

            // if waitTime per thread isn't at least 100 ms, then just assume no I/O bound
            waitTime = waitTime > 100000000 ? waitTime : 0;

            int size = (int) ceil((CPUS * targetUtilization * (1 + (waitTime / cpuTime))));
            size = size > 0 ? size : 1;

            // TODO remove debugging
            System.out.println(waitTime / 1000000 + " ms");
            System.out.println(cpuTime / 1000000 + " ms");
            System.out.println(size);

            // TODO might want this to be 50%, 75%, etc. of max...
            threadPoolExecutor.setCorePoolSize(size);
            threadPoolExecutor.setMaximumPoolSize(size);
        }
    }

    public long getAvgThreadCpuTime() {
        return avgThreadCpuTime;
    }

    public long getAvgThreadTotalTime() {
        return avgThreadTotalTime;
    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return threadPoolExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPoolExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(final Runnable command) {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.nanoTime();
                try {
                    command.run();
                } finally {
                    long totalTime = System.nanoTime() - start;
                    Thread thisThread = Thread.currentThread();
                    Tracking tracking = liveThreads.get(thisThread);
                    long taskCpuTime = THREAD_MX_BEAN.getThreadCpuTime(thisThread.getId());
                    if(tracking == null) {
                        // this is an untracked thread, add tracking
                        tracking = new Tracking();
                        tracking.avgWaitTime = totalTime;
                        tracking.avgCpuTime = 0; // TODO this is unseeded to begin with, do we need a seed value from new Thread start?
                        tracking.taskCount = 1;
                        liveThreads.put(thisThread, tracking);
                    } else {
                        // compute cumulative moving averages, see https://en.wikipedia.org/wiki/Moving_average
                        tracking.taskCount++;
                        tracking.avgWaitTime += (totalTime - tracking.avgWaitTime) / tracking.taskCount;
                        tracking.avgCpuTime += (taskCpuTime - tracking.avgCpuTime) / tracking.taskCount;
                    }
                }
            }
        });
    }

    private void initCollectorThread() {
        Thread collectorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean done = false;
                while (!done) {
                    try {
                        Thread.sleep(sampleMs);
                        Set<Map.Entry<Thread, Tracking>> threads = liveThreads.entrySet();
                        long liveAvgTimeTotal = 0;
                        long liveAvgCpuTotal = 0;
                        long liveCount = 0;
                        for (Map.Entry<Thread, Tracking> e : threads) {
                            if (!e.getKey().isAlive()) {
                                // thread is dead or otherwise hosed
                                threads.remove(e);
                            } else {
                                liveAvgTimeTotal += e.getValue().avgWaitTime;
                                liveAvgCpuTotal += e.getValue().avgCpuTime;
                                liveCount++;
                            }
                        }
                        if(liveCount > 0) {
                            avgThreadTotalTime = liveAvgTimeTotal / liveCount;
                            avgThreadCpuTime = liveAvgCpuTotal / liveCount;
                        }
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        done = true;
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

        collectorThread.setDaemon(true);
        collectorThread.start();
    }
}
