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

class NativeCoroutineSupport extends CoroutineSupport {

    static {
        registerNatives();
    }

    NativeCoroutineSupport(Thread thread) {
        super(thread);
    }

    @Override
    protected boolean verifyThread() {
        return getThread() == Thread.currentThread();
    }

    @Override
    protected long getInitialCoroutineData() {
        return getThreadCoroutine();
    }

    @Override
    protected void initializeCoroutine(CoroutineBase coroutine, long stacksize) {
        coroutine.data = createCoroutine(coroutine, stacksize);
    }

    @Override
    protected void transferTo(CoroutineBase current, CoroutineBase target) {
        switchTo(current, target);
    }

    @Override
    protected void transferToAndTerminate(CoroutineBase current, CoroutineBase target) {
        switchToAndTerminate(current, target);
    }

    @Override
    protected void transferToAndExit(CoroutineBase current, CoroutineBase target) {
        transferToAndExit(current, target);
    }

    @Override
    protected boolean isDisposableCoroutine(CoroutineBase coroutine) {
        return isDisposable(coroutine.data);
    }

    @Override
    protected CoroutineBase doCleanupCoroutine() {
        return cleanupCoroutine();
    }

    private static native void registerNatives();

    private static native long getThreadCoroutine();

    private static native long createCoroutine(CoroutineBase coroutine, long stacksize);

    private static native void switchTo(CoroutineBase current, CoroutineBase target);

    private static native void switchToAndTerminate(CoroutineBase current, CoroutineBase target);

    private static native void switchToAndExit(CoroutineBase current, CoroutineBase target);

    private static native boolean isDisposable(long coroutine);

    private static native CoroutineBase cleanupCoroutine();

}
