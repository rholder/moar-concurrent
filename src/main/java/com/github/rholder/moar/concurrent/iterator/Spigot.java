package com.github.rholder.moar.concurrent.iterator;

/**
 * The provider of elements to a {@link StreamingIterator}. Attach it to one via {@link StreamingIterator#attach(Spigot)}.
 */
public interface Spigot<T> {

    /**
     * @param to drain elements into this
     */
    void spout(Drain<T> to) throws InterruptedException;

}
