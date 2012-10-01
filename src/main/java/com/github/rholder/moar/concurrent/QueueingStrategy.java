package com.github.rholder.moar.concurrent;

/**
 * Implementations of this class perform actions before and after adding or
 * removing items from a queue (i.e. as a mechanism for slowing or speeding up
 * subsequent adding based on some calculated heuristic).
 *
 * @author rholder
 * @param <E> the type of elements held in the target queue
 */
public interface QueueingStrategy<E> {

    /**
     * Perform this action before adding the given value to the queue.
     *
     * @param value
     */
    public void onBeforeAdd(E value);

    /**
     * Perform this action after adding a value to the queue.
     */
    public void onAfterAdd();

    /**
     * Perform this action before removing a value from the queue.
     */
    public void onBeforeRemove();

    /**
     * Perform this action before removing the given value from the queue.
     *
     * @param value
     */
    public void onAfterRemove(E value);
}
