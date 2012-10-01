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
