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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This is a pass-through class that just wraps all the methods of a BlockingQueue.
 *
 * @author rholder
 * @param <E> the type of elements held in this collection
 */
public abstract class WrappedBlockingQueue<E> implements BlockingQueue<E> {

    private BlockingQueue<E> blockingQueue;

    public WrappedBlockingQueue(BlockingQueue<E> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    public boolean add(E e) {
        return blockingQueue.add(e);
    }

    public boolean offer(E e) {
        return blockingQueue.offer(e);
    }

    public E remove() {
        return blockingQueue.remove();
    }

    public E poll() {
        return blockingQueue.poll();
    }

    public E element() {
        return blockingQueue.element();
    }

    public E peek() {
        return blockingQueue.peek();
    }

    public void put(E e) throws InterruptedException {
        blockingQueue.put(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return blockingQueue.offer(e, timeout, unit);
    }

    public E take() throws InterruptedException {
        return blockingQueue.take();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return blockingQueue.poll(timeout, unit);
    }

    public int remainingCapacity() {
        return blockingQueue.remainingCapacity();
    }

    public boolean remove(Object o) {
        return blockingQueue.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return blockingQueue.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return blockingQueue.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return blockingQueue.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return blockingQueue.retainAll(c);
    }

    public void clear() {
        blockingQueue.clear();
    }

    public int size() {
        return blockingQueue.size();
    }

    public boolean isEmpty() {
        return blockingQueue.isEmpty();
    }

    public boolean contains(Object o) {
        return blockingQueue.contains(o);
    }

    public Iterator<E> iterator() {
        return blockingQueue.iterator();
    }

    public Object[] toArray() {
        return blockingQueue.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return blockingQueue.toArray(a);
    }

    public int drainTo(Collection<? super E> c) {
        return blockingQueue.drainTo(c);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        return blockingQueue.drainTo(c, maxElements);
    }
}
