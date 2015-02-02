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

package com.github.rholder.moar.concurrent.partition;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PartsTest {

    @Test
    public void oddParts() {
        // if these were bytes, bytes 0 - 21 would be 22 bytes when 0 indexed
        long totalLength = 73;
        long chunkSize = 22;

        List<Part> parts = Parts.among(totalLength, chunkSize);
        Assert.assertEquals(4, parts.size());

        // 0 - 21
        Part p0 = parts.get(0);
        Assert.assertEquals(0, p0.start);
        Assert.assertEquals(21, p0.end);

        // we add 1 here to calculate the number of things between the start and end parts inclusive
        Assert.assertEquals(chunkSize, (p0.end - p0.start) + 1);

        // 22 - 43
        Part p1 = parts.get(1);
        Assert.assertEquals(22, p1.start);
        Assert.assertEquals(43, p1.end);
        Assert.assertEquals(chunkSize, (p1.end - p1.start) + 1);

        // 44 - 65
        Part p2 = parts.get(2);
        Assert.assertEquals(44, p2.start);
        Assert.assertEquals(65, p2.end);
        Assert.assertEquals(chunkSize, (p2.end - p2.start) + 1);

        // 66 - 72
        Part p3 = parts.get(3);
        Assert.assertEquals(66, p3.start);
        Assert.assertEquals(72, p3.end);
        Assert.assertEquals(7, (p3.end - p3.start) + 1);

        Assert.assertEquals(totalLength, (p3.end - p0.start) + 1);
    }

    @Test
    public void annoyinglyAlignedParts() {
        long totalLength = 26;
        long chunkSize = 13;

        List<Part> parts = Parts.among(totalLength, chunkSize);
        Assert.assertEquals(2, parts.size());

        // 0 - 12
        Part p0 = parts.get(0);
        Assert.assertEquals(0, p0.start);
        Assert.assertEquals(12, p0.end);
        Assert.assertEquals(chunkSize, (p0.end - p0.start) + 1);

        // 13 - 25
        Part p1 = parts.get(1);
        Assert.assertEquals(13, p1.start);
        Assert.assertEquals(25, p1.end);
        Assert.assertEquals(chunkSize, (p1.end - p1.start) + 1);

        Assert.assertEquals(totalLength, (p1.end - p0.start) + 1);
    }

    @Test
    public void annoyinglyAlignedPartsPlusOne() {
        long totalLength = 27;
        long chunkSize = 13;

        List<Part> parts = Parts.among(totalLength, chunkSize);
        Assert.assertEquals(3, parts.size());

        // 0 - 12
        Part p0 = parts.get(0);
        Assert.assertEquals(0, p0.start);
        Assert.assertEquals(12, p0.end);
        Assert.assertEquals(chunkSize, (p0.end - p0.start) + 1);

        // 13 - 25
        Part p1 = parts.get(1);
        Assert.assertEquals(13, p1.start);
        Assert.assertEquals(25, p1.end);
        Assert.assertEquals(chunkSize, (p1.end - p1.start) + 1);

        // 26 - 26
        Part p3 = parts.get(2);
        Assert.assertEquals(26, p3.start);
        Assert.assertEquals(26, p3.end);

        // the size of the inclusive range of the same start and end is 1
        Assert.assertEquals(1, (p3.end - p3.start) + 1);

        Assert.assertEquals(totalLength, (p3.end - p0.start) + 1);
    }

    @Test
    public void singlePartUnderChunkSize() {
        long totalLength = 17;
        long chunkSize = 30;

        List<Part> parts = Parts.among(totalLength, chunkSize);
        Assert.assertEquals(1, parts.size());

        // 0 - 16
        Part p0 = parts.get(0);
        Assert.assertEquals(0, p0.start);
        Assert.assertEquals(16, p0.end);
        Assert.assertEquals(totalLength, (p0.end - p0.start) + 1);
    }

    @Test
    public void singleAlignedPart() {
        long totalLength = 17;
        long chunkSize = 17;

        List<Part> parts = Parts.among(totalLength, chunkSize);

        Assert.assertEquals(1, parts.size());

        // 0 - 16
        Part p0 = parts.get(0);
        Assert.assertEquals(0, p0.start);
        Assert.assertEquals(16, p0.end);
        Assert.assertEquals(chunkSize, (p0.end - p0.start) + 1);
    }

    @Test
    public void betweenAligned() {
        long chunkSize = 10;

        List<Part> parts = Parts.between(23, 42, chunkSize);
        Assert.assertEquals(2, parts.size());

        // 23 - 32
        Part p0 = parts.get(0);
        Assert.assertEquals(23, p0.start);
        Assert.assertEquals(32, p0.end);
        Assert.assertEquals(chunkSize, (p0.end - p0.start) + 1);

        // 33 - 42
        Part p1 = parts.get(1);
        Assert.assertEquals(33, p1.start);
        Assert.assertEquals(42, p1.end);
        Assert.assertEquals(chunkSize, (p1.end - p1.start) + 1);
    }

    @Test
    public void betweenPlusOne() {
        long chunkSize = 10;

        List<Part> parts = Parts.between(23, 43, chunkSize);
        Assert.assertEquals(3, parts.size());

        // 23 - 32
        Part p0 = parts.get(0);
        Assert.assertEquals(23, p0.start);
        Assert.assertEquals(32, p0.end);
        Assert.assertEquals(chunkSize, (p0.end - p0.start) + 1);

        // 33 - 42
        Part p1 = parts.get(1);
        Assert.assertEquals(33, p1.start);
        Assert.assertEquals(42, p1.end);
        Assert.assertEquals(chunkSize, (p1.end - p1.start) + 1);

        // 43 - 43
        Part p2 = parts.get(2);
        Assert.assertEquals(43, p2.start);
        Assert.assertEquals(43, p2.end);
        Assert.assertEquals(1, (p2.end - p2.start) + 1);
    }

    @Test
    public void blah() {
        List<Part> parts = Parts.among(23, 21, 10);
        for(Part p : parts) {
            System.out.println(String.format("[%d, %d]", p.start, p.end));
        }
    }
}
