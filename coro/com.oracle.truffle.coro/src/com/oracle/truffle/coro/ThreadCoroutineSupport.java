/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.truffle.coro;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class ThreadCoroutineSupport extends CoroutineSupport {

    private static final long ALIVE = 1;
    private static final long DEAD = 0;

    static class ThreadCoroutine implements Runnable {

        final ThreadCoroutineSupport support;
        final CoroutineBase coroutine;

        boolean run = false;

        public ThreadCoroutine(ThreadCoroutineSupport support, CoroutineBase coroutine) {
            this.support = support;
            this.coroutine = coroutine;
            initialize();
        }

        void initialize() {
            coroutine.data = ALIVE;
            coroutine.threadCoroutine = this;
        }

        @Override
        public void run() {
            COROUTINE_SUPPORT.set(support);
            waitForResume();
            coroutine.startInternal();
        }

        void terminate() {
            coroutine.data = DEAD;
            coroutine.threadCoroutine = null;
            COROUTINE_SUPPORT.set(null);
        }

        void waitForResume() {
            synchronized (this) {
                while (!run) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                run = false;
            }
        }

        void resume() {
            synchronized (this) {
                assert !run;
                run = true;
                notify();
            }
        }

    }

    static class CoroutineThreadFactory implements ThreadFactory {

        private final AtomicInteger coroutineNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable target) {
            String name = "Coroutine-" + coroutineNumber.getAndIncrement();
            Thread thread = CoroutineSupport.THREAD_FACTORY.newThread(target);
            thread.setName(name);
            // Let coroutines threads die when System.exit() is called since they are blocked on
            // waitForResume() anyway.
            thread.setDaemon(true);
            return thread;
        }

    }

    static ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new CoroutineThreadFactory());

    public static void resetThreadPool() {
        THREAD_POOL = Executors.newCachedThreadPool(new CoroutineThreadFactory());
    }

    public static void shutdownThreadPool() {
        THREAD_POOL.shutdownNow();
    }

    @SuppressWarnings("unused")
    ThreadCoroutineSupport(Thread thread) {
        super(thread);
        new ThreadCoroutine(this, threadCoroutine);
    }

    @Override
    protected boolean verifyThread() {
        return COROUTINE_SUPPORT.get() == this;
    }

    @Override
    protected long getInitialCoroutineData() {
        return ALIVE;
    }

    @Override
    protected void initializeCoroutine(CoroutineBase coroutine, long stacksize) {
        THREAD_POOL.execute(new ThreadCoroutine(this, coroutine));
    }

    @Override
    protected void transferTo(CoroutineBase current, CoroutineBase target) {
        assert current != target;
        target.threadCoroutine.resume();
        current.threadCoroutine.waitForResume();
    }

    @Override
    protected void transferToAndTerminate(CoroutineBase current, CoroutineBase target) {
        assert current != target;
        assert current != threadCoroutine;
        current.threadCoroutine.terminate();
        target.threadCoroutine.resume();
    }

    @Override
    protected void transferToAndExit(CoroutineBase current, CoroutineBase target) {
        assert current != target;
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isDisposableCoroutine(CoroutineBase coroutine) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CoroutineBase doCleanupCoroutine() {
        throw new UnsupportedOperationException();
    }

}
