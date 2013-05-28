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

    @Ignore("ad-hoc profiling")
    @Test
    public void withoutProfiling() throws InterruptedException {
        Assert.assertTrue(THREAD_MX_BEAN.isThreadCpuTimeEnabled());
        long now = System.nanoTime();
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for(int i = 0; i < 10000; i++) {
            executorService.submit(new CpuWorkerSim());
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println(((System.nanoTime() - now) / 1000000) + " ms");
    }

    @Ignore("ad-hoc profiling")
    @Test
    public void withProfiling() throws InterruptedException {
        THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
        long now = System.nanoTime();
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for(int i = 0; i < 10000; i++) {
            executorService.submit(new CpuWorkerSim() {
                @Override
                public void run() {
                    long start = THREAD_MX_BEAN.getCurrentThreadCpuTime();
                    try {
                        super.run();
                    } finally {
                        long total = THREAD_MX_BEAN.getCurrentThreadCpuTime() - start;
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println(((System.nanoTime() - now) / 1000000) + " ms");
    }

    @Ignore("ad-hoc profiling")
    @Test
    public void balanceTest1() throws InterruptedException {
        Assert.assertTrue(THREAD_MX_BEAN.isThreadCpuTimeEnabled());
        Assert.assertTrue(THREAD_MX_BEAN.isThreadCpuTimeSupported());
        long now = System.nanoTime();

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 10, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
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

        for(int i = 0; i < 10000; i++) {
            executorService.submit(new CpuWorkerSim());
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println(((System.nanoTime() - now) / 1000000) + " ms");
    }

    public static class CpuWorkerSim implements Runnable {
        @Override
        public void run() {
            for(int i = 0; i < 10000; i++) {
                int a = ThreadLocalRandom.current().nextInt();
                double b = Math.pow(a, 61) * Math.pow(a, 13);
            }
        }
    }
// TODO flush this out
//    public static class IOWorkerSim implements Runnable {
//
//        public IOWorkerSim(long minWait, long maxWait) {
//
//        }
//
//        @Override
//        public void run() {
//
//        }
//    }
}
