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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A StrategicBlockingQueue wraps a standard BlockingQueue, providing a
 * QueueingStrategy for performing actions before and after adding and removing
 * items from the wrapped queue.
 *
 * @author rholder
 * @param <E> the type of elements held in this collection
 */
public class StrategicBlockingQueue<E> extends WrappedBlockingQueue<E> {

    private QueueingStrategy<E> queueingStrategy;

    public StrategicBlockingQueue(BlockingQueue<E> blockingQueue, QueueingStrategy<E> queueingStrategy) {
        super(blockingQueue);
        this.queueingStrategy = queueingStrategy;
    }

    @Override
    public void put(E e) throws InterruptedException {
        queueingStrategy.onBeforeAdd(e);
        try {
            super.put(e);
        } finally {
            queueingStrategy.onAfterAdd();
        }
    }

    @Override
    public boolean add(E e) {
        queueingStrategy.onBeforeAdd(e);
        try {
            return super.add(e);
        } finally {
            queueingStrategy.onAfterAdd();
        }
    }

    @Override
    public E take() throws InterruptedException {
        queueingStrategy.onBeforeRemove();
        E value = null;
        try {
            return value = super.take();
        } finally {
            queueingStrategy.onAfterRemove(value);
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        queueingStrategy.onBeforeRemove();
        E value = null;
        try {
            return value = super.poll(timeout, unit);
        } finally {
            queueingStrategy.onAfterRemove(value);
        }
    }

    @Override
    public E poll() {
        queueingStrategy.onBeforeRemove();
        E value = null;
        try {
            return value = super.poll();
        } finally {
            queueingStrategy.onAfterRemove(value);
        }
    }

    // TODO implement drainTo() such that add/remove's use QueueingStrategy
}
