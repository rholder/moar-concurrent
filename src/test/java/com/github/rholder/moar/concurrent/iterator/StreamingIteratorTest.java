package com.github.rholder.moar.concurrent.iterator;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jason Dunkelberger (dirkraft)
 */
public class StreamingIteratorTest {

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void testHappyPath() {
        StreamingIterator<String> sit = new StreamingIterator<String>(executorService);
        sit.attach(new Spigot<String>() {
            @Override
            public void spout(Drain<String> to) throws InterruptedException {
                for (int i = 0; i < 5; i++) {
                    to.drain("" + i);
                    // Say, it's network latency on a list of stuff that we have to get one by one.
                    Thread.sleep(50L);
                }
            }
        });

        List<String> out = new ArrayList<String>(5);
        for (String s : sit) {
            out.add(s);
        }
        Assert.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), out);
    }

    @Test
    public void testBlocking() {
        StreamingIterator<Void> sit = new StreamingIterator<Void>(executorService);
        sit.attach(new Spigot<Void>() {
            @Override
            public void spout(Drain<Void> to) throws InterruptedException {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {}
                to.drain(null);
            }
        });

        long start = System.currentTimeMillis();
        boolean once = false;
        for (Void ignored : sit) {
            // hasMore should block until the spigot can drain in an element, and then...
            Assert.assertFalse("should only emit a single item", once);
            once = true;
        }
        long end = System.currentTimeMillis();
        Assert.assertTrue("should have blocked a little while", end - start >= 100L);
    }
}
