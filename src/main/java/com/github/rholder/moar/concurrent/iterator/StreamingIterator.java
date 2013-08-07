package com.github.rholder.moar.concurrent.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An {@link Iterator} and {@link Iterable} that can be fed by multiple, arbitrary {@link Spigot}s.
 * It is a 'lazy' Iterator in the sense that not all iterable elements are necessarily loaded into memory at any
 * single point in time. Generally, a StreamingIterator may be fed by multiple threads, but should be read by a
 * single thread.
 * <p>
 * {@link #hasNext()} should precede every {@link #next()} invocation (as is the behavior in a for-each loop).
 * Unprecedented calls to next() may result in spurious <code>null</code> returns, even though there are potentially
 * more elements to process.
 * <p>
 * It is generally a good idea to call {@link #close()} when finished with the iterator as outstanding
 * resources may not have been reclaimed due to exceptions or logical errors. Presumably if the end of iteration
 * is reached, all of the Spigots have terminated, but a <code>finally</code> is strongly recommended (shown
 * in the below example).
 *
 * <hr/>
 *
 * Sample usage:
 * <pre>
 * StreamingIterator&lt;Integer&gt; sit = new StreamingIterator&lt;Integer&gt;(executorService);
 * try {
 *     sit.attach(new Spigot&lt;Integer&gt;() {
 *        {@literal @}Override
 *         public void spout(Drain<Integer> in) throws InterruptedException {
 *             // logic that drains elements to the StreamingIterator
 *             for (int i = 0; i < 10; ++i) {
 *                 in.to(i);
 *             }
 *         }
 *     });
 *     for (String s in sit) {
 *         // do stuff
 *         System.out.println(s);
 *     }
 * } finally {
 *     // Be sure to clean up resources
 *     sit.close();
 *     executorService.shutdownNow();
 * }
 * </pre>
 *
 * @author Jason Dunkelberger (dirkraft)
 */
public class StreamingIterator<T> implements Iterable<T>, Iterator<T>, Closeable {

    private final ArrayBlockingQueue<T> q;
    private final long patienceIntervalMs;
    private final ExecutorService executorService;

    private final List<SpigotWrapper> spigots = new ArrayList<SpigotWrapper>();

    /** All spigots drain to the BlockingQueue */
    private final Drain<T> drain = new Drain<T>() {
        @Override
        public void drain(T t) throws InterruptedException {
            q.put(t);
        }
    };

    /**
     * defaults:
     * <dl>
     *     <dt>queueCapacity</dt><dd>4096</dd>
     *     <dt>patienceIntervalMs</dt><dd>1000</dd>
     * </dl>
     */
    public StreamingIterator(ExecutorService executorService) {
        this(executorService, 4096, 1000L);
    }

    /**
     * @param executorService    that will execute the Spigot
     * @param queueCapacity      internal buffering capacity
     * @param patienceIntervalMs rate at which Spigot 'done-ness' may be polled in the case that this StreamingIterator
     *                           has been temporarily exhausted
     */
    public StreamingIterator(ExecutorService executorService, int queueCapacity, long patienceIntervalMs) {
        this.executorService = executorService;
        this.q = new ArrayBlockingQueue<T>(queueCapacity, true);
        this.patienceIntervalMs = patienceIntervalMs;
    }

    /**
     * attaches a spigot to this StreamingIterator which will provide the elements that are eventually iterated upon.
     */
    public void attach(final Spigot<T> spigot) {
        Future<?> spigotFuture = executorService.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    spigot.spout(drain);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        spigots.add(new SpigotWrapper(spigot, spigotFuture));
    }

    /**
     * Convenience so that a StreamingIterator may be used as an {@link Iterable}. Note that since any StreamingIterator
     * instance is itself an {@link Iterator}, it should be used as such (i.e. iterators are not reusable).
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    /**
     * Blocks if the spigot indicates that there is potentially still more stream data, but none has yet been made
     * available in this StreamingIterator. Returns <code>false</code> only when the spigot is exhausted and this
     * StreamingIterator's internal buffer is also empty.
     */
    @Override
    public boolean hasNext() {
        try {
            while (q.size() == 0 && anySpigotHasMore()) {
                Thread.sleep(patienceIntervalMs);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return q.size() > 0;
    }

    /**
     * @return The next available element, or <code>null</code> if none such is yet available. If spurious <code>null</code>
     *         returns are not desirable, each invocation should be preceded by a check with {@link #hasNext()} since
     *         that will block until there are more elements available (true), or there are definitely no more (false).
     */
    @Override
    public T next() {
        return q.poll();
    }

    @Override
    public void remove() {
        throw new RuntimeException("Not supported nor sensible.");
    }

    public void close() throws IOException {
        for (SpigotWrapper spigot : spigots) {
            spigot.future.cancel(true);
        }
    }

    private boolean anySpigotHasMore() {
        for (SpigotWrapper spigot : spigots) {
            if (spigot.hasMore()) {
                return true;
            }
        }
        return false;
    }

    private class SpigotWrapper {
        final Spigot spigot;
        final Future<?> future;

        SpigotWrapper(Spigot spigot, Future<?> future) {
            this.spigot = spigot;
            this.future = future;
        }

        boolean hasMore() {
            return !future.isDone();
        }
    }
}
