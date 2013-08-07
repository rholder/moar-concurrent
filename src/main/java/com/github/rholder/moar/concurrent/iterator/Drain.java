package com.github.rholder.moar.concurrent.iterator;

/**
 * @author Jason Dunkelberger (dirkraft)
 */
public interface Drain<T> {

    /**
     * Takes elements which will be eventually produced by the attached {@link StreamingIterator}
     */
    public void drain(T t) throws InterruptedException;

}
