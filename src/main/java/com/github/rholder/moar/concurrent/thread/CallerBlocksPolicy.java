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

package com.github.rholder.moar.concurrent.thread;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This {@link RejectedExecutionHandler} blocks the current thread until the
 * backing queue for a {@link ThreadPoolExecutor} can accept another task.  This
 * becomes useful for situations where tasks are typically long running or
 * unknown and it is undesirable to block the task submission thread for the
 * entire length of one of these tasks (which can easily happen if using the
 * {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy}). Provided
 * your queue of choice for the {@link ThreadPoolExecutor} is bounded, you'll be
 * able to continue submitting tasks up until the point where the queue is full
 * and then resume the submitting of new tasks immediately when the queue can
 * accept more without having to wait on an existing task in the submitting
 * thread to finish.
 */
public class CallerBlocksPolicy implements RejectedExecutionHandler {

    /**
     * Instead of throwing away the rejected task, put it back onto the queue
     * that the {@link ThreadPoolExecutor} is using, effectively blocking
     * execution until the put() succeeds or results in an
     * {@link InterruptedException} which is wrapped and rethrown as
     * a {@link RejectedExecutionException}.
     *
     * @param r        the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            // block on the internal queue
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            throw new RejectedExecutionException("Unexpected InterruptedException", e);
        }
    }
}