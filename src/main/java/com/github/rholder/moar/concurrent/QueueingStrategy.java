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
