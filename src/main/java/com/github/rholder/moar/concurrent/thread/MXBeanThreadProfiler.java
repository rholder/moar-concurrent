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

package com.github.rholder.moar.concurrent.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Use the {@link ThreadMXBean} implementation to provide thread information.
 */
public class MXBeanThreadProfiler implements ThreadProfiler {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    /**
     * ThreadContentionMonitoring and ThreadCpuTime are both turned on when
     * constructing this class. See {@link ThreadMXBean} for more details.
     */
    public MXBeanThreadProfiler() {
        if(!THREAD_MX_BEAN.isThreadContentionMonitoringSupported()) {
            throw new UnsupportedOperationException("ThreadContentionMonitoring is not supported on this platform");
        }
        THREAD_MX_BEAN.setThreadContentionMonitoringEnabled(true);

        if(!THREAD_MX_BEAN.isThreadCpuTimeSupported()) {
            throw new UnsupportedOperationException("ThreadCpuTime is not supported on this platform");
        }
        THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
    }

    @Override
    public long getThreadWaitTime(long threadId) {
        ThreadInfo threadInfo = THREAD_MX_BEAN.getThreadInfo(threadId, 0);
        return threadInfo.getWaitedTime() * 1000000;
    }

    @Override
    public long getThreadCpuTime(long threadId) {
        return THREAD_MX_BEAN.getThreadCpuTime(threadId);
    }

}
