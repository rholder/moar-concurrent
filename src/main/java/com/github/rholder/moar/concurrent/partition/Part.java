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

/**
 * This is a simple immutable tuple to hold start and end values.
 */
public class Part {

    /**
     * Create a new Part.
     *
     * @param start starting value for this part
     * @param end ending value for this part
     */
    public Part(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * The starting value for this part.
     */
    public final long start;

    /**
     * The ending value for this part.
     */
    public final long end;
}