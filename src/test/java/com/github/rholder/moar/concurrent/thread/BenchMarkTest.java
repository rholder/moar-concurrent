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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BenchMarkTest {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    private static final int TOTAL_TASKS = 500;
    private static final long MAX_WAIT = 15000;

    @Ignore("ad-hoc profiling")
    @Test
    public void cpuBoundWithoutProfiling() throws InterruptedException {
        withoutProfiling(new TaskCreator() {
            @Override
            public Runnable create() {
                return new CpuWorkerSim();
            }
        });
    }

    @Ignore("ad-hoc profiling")
    @Test
    public void cpuBoundWithBalancing() throws InterruptedException {
        withBalancingThreadPoolExecutor(new TaskCreator() {
            @Override
            public Runnable create() {
                return new CpuWorkerSim();
            }
        });
    }

    @Ignore("ad-hoc profiling")
    @Test
    public void blockingWithoutProfiling() throws InterruptedException {
        withoutProfiling(new TaskCreator() {
            @Override
            public Runnable create() {
                return new BlockingWorkerSim(0, MAX_WAIT, new CpuWorkerSim());
            }
        });
    }

    @Ignore("ad-hoc profiling")
    @Test
    public void blockingWithBalancing() throws InterruptedException {
        withBalancingThreadPoolExecutor(new TaskCreator() {
            @Override
            public Runnable create() {
                return new BlockingWorkerSim(0, MAX_WAIT, new CpuWorkerSim());
            }
        });
    }

    public void withoutProfiling(TaskCreator taskCreator) throws InterruptedException {
        Assert.assertTrue(THREAD_MX_BEAN.isThreadCpuTimeEnabled());

        // collect CPU time for the CpuWorkerSim
        long profileStart = THREAD_MX_BEAN.getCurrentThreadCpuTime();
        new CpuWorkerSim().run();
        long profileEnd = THREAD_MX_BEAN.getCurrentThreadCpuTime();

        // use average of random wait between tasks
        long wait = MAX_WAIT / 2;
        long cpu = (profileEnd - profileStart) / 1000000;
        System.out.println(wait);
        System.out.println(cpu);

        // optimal threads in pool computed as N * U * (1 + W/C)
        int optimalPoolSize = (int) Math.ceil(Runtime.getRuntime().availableProcessors() * 1.0 * (1 + wait/cpu));
        System.out.println("Using " + optimalPoolSize);

        long now = System.nanoTime();
        ExecutorService executorService = Executors.newFixedThreadPool(optimalPoolSize); // 112?
        for(int i = 0; i < TOTAL_TASKS; i++) {
            executorService.submit(taskCreator.create());
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println(((System.nanoTime() - now) / 1000000) + " ms");
    }

    public void withBalancingThreadPoolExecutor(TaskCreator taskCreator) throws InterruptedException {
        Assert.assertTrue(THREAD_MX_BEAN.isThreadCpuTimeEnabled());
        Assert.assertTrue(THREAD_MX_BEAN.isThreadCpuTimeSupported());
        long now = System.nanoTime();

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 200, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
        final BalancingThreadPoolExecutor executorService = new BalancingThreadPoolExecutor(tpe, 1.0f, 4000);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean done = false;
                while(!done) {
                    try {
                        Thread.sleep(5000);
                        executorService.balance();
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        e.printStackTrace();
                        done = true;
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();

        for(int i = 0; i < TOTAL_TASKS; i++) {
            executorService.submit(taskCreator.create());
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println(((System.nanoTime() - now) / 1000000) + " ms");
    }

    public static class CpuWorkerSim implements Runnable {
        @Override
        public void run() {
            for(int i = 0; i < 35 * 20000; i++) {
                int a = ThreadLocalRandom.current().nextInt();
                double b = Math.pow(a, 61) * Math.pow(a, 13);
            }
        }
    }

    public static class BlockingWorkerSim implements Runnable {

        public long minWait;
        public long maxWait;
        public Runnable runnable;

        public BlockingWorkerSim(long minWait, long maxWait, Runnable runnable) {
            this.minWait = minWait;
            this.maxWait = maxWait;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                // simulate blocking call by sleeping
                Thread.sleep(ThreadLocalRandom.current().nextLong(minWait, maxWait));
                runnable.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface TaskCreator {
        public Runnable create();
    }
}
