/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.coro.ThreadCoroutineSupport.ThreadCoroutine;

public abstract class CoroutineBase {

    public static CoroutineSupport getCurrentCoroutineSupport() {
        return CoroutineSupport.currentCoroutineSupport();
    }

    transient long data;

    transient ThreadCoroutine threadCoroutine;

    transient CoroutineLocal.CoroutineLocalMap coroutineLocals = null;

    boolean finished = false;

    transient CoroutineSupport threadSupport;

    CoroutineBase() {
        this.threadSupport = getCurrentCoroutineSupport();
    }

    // creates the initial coroutine for a new thread
    CoroutineBase(CoroutineSupport threadSupport, long data) {
        this.threadSupport = threadSupport;
        this.data = data;
    }

    protected abstract void run();

    final void startInternal() {
        assert threadSupport.verifyThread();
        try {
            if (CoroutineSupport.DEBUG) {
                System.out.println("starting coroutine " + this);
            }
            run();
        } catch (Throwable t) {
            if (!(t instanceof CoroutineExitException)) {
                t.printStackTrace();
            }
        } finally {
            finished = true;
            // use getCurrentCoroutineSupport() because we might have been migrated to another
            // thread!
            if (this instanceof Coroutine) {
                getCurrentCoroutineSupport().terminateCoroutine();
            } else {
                getCurrentCoroutineSupport().terminateCallable();
            }
        }
    }

    /**
     * Returns true if this coroutine has reached its end. Under normal circumstances this happens
     * when the {@link #run()} method returns.
     */
    public final boolean isFinished() {
        return finished;
    }

    /**
     * @return the thread that this coroutine is associated with
     * @throws NullPointerException if the coroutine has terminated
     */
    public Thread getThread() {
        return threadSupport.getThread();
    }

    public static CoroutineBase current() {
        return getCurrentCoroutineSupport().getCurrent();
    }
}
