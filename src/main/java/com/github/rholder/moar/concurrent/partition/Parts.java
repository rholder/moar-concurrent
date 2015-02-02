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

import java.util.ArrayList;
import java.util.List;

/**
 * This is a a collection of numeric range partitioning utilities.
 */
public class Parts {

    /**
     * Return a list of {@link Part}'s between two values split up into equal
     * partitions up to the last chunk that may contain a range of length &lt;= the
     * given chunk size.
     *
     * For instance, a start of 23 and end of 43 with a chunkSize of 10 would
     * result in the following list of returned parts:
     * <pre>
     * [23, 32], [33, 42], [43, 43]
     * </pre>
     *
     * Notice that the final chunk contains the same start and end. This should
     * be expected in cases where the last chunk would only contain one value.
     *
     * @param start     starting value of the range to partition
     * @param end       starting value of the range to partition
     * @param chunkSize partition the range in chunks of this size, with the
     *                  last chunk containing &lt;= this value
     * @return a list of {@link Part}'s
     */
    public static List<Part> between(long start, long end, long chunkSize) {
        return among(start, (end - start) + 1, chunkSize);
    }

    /**
     * Return a list of {@link Part}'s where the total length is split up into
     * equal partitions up to the last chunk that may contain a range of length
     * &lt;= the given chunk size.
     *
     * For instance, a totalLength of 73 and a chunkSize of 22 would result in
     * the following list of returned parts:
     * <pre>
     * [0, 21], [22, 43], [44, 65], [66, 72]
     * </pre>
     *
     * @param totalLength the total length of the range to partition
     * @param chunkSize   partition the range in chunks of this size, with the
     *                    last chunk containing &lt;= this value
     * @return a list of {@link Part}'s
     */
    public static List<Part> among(long totalLength, long chunkSize) {
        return among(0, totalLength, chunkSize);
    }

    /**
     * Return a list of {@link Part}'s with the given offset where the total
     * length is split up into equal partitions up to the last chunk that may
     * contain a range of length &lt;= the given chunk size.
     *
     * For instance, an offset of 23 and total length of 21 with a chunkSize of
     * 10 would result in the following list of returned parts:
     * <pre>
     * [23, 32], [33, 42], [43, 43]
     * </pre>
     *
     * Notice that the final chunk contains the same start and end. This should
     * be expected in cases where the last chunk would only contain one value.
     *
     * @param offset      add this offset to the start and end of the calculated {@link Part}'s
     * @param totalLength the total length of the range to partition
     * @param chunkSize   partition the range in chunks of this size, with the
     *                    last chunk containing &lt;= this value
     * @return a list of {@link Part}'s
     */
    public static List<Part> among(long offset, long totalLength, long chunkSize) {
        List<Part> parts = new ArrayList<Part>();
        int i = 0;
        long start = 0;
        long end = Math.min(start + chunkSize, totalLength) - 1;
        do {
            parts.add(new Part(start + offset, end + offset));
            start = ++i * chunkSize;
            end = Math.min(start + chunkSize, totalLength) - 1;
        } while (start < totalLength);
        return parts;
    }
}