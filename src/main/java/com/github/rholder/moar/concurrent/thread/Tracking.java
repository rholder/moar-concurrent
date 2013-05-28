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

package com.github.rholder.moar.concurrent.thread;

/**
 * Collect tracking statistics for a given worker thread.
 */
public class Tracking {
    // TODO making these volatile to ensure consistency adds unreasonable overhead, test read-after-write cases more to determine if it's "good enough"
    public long taskCount = 0;
    public long avgWaitTime = 0;
    public long avgCpuTime = 0;
}
