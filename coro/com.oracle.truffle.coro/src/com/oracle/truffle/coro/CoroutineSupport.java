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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class CoroutineSupport {

    private static final String NATIVE_ENABLED = System.getProperty("com.oracle.truffle.coro.native");

    private static boolean hasNativeCoroutines() {
        if ("false".equals(NATIVE_ENABLED)) {
            return false;
        }

        try {
            new NativeCoroutineSupport(Thread.currentThread());
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
        return true;
    }

    static final boolean NATIVE = hasNativeCoroutines();

    public static CoroutineSupport create(Thread thread) {
        if (NATIVE) {
            return new NativeCoroutineSupport(thread);
        } else {
            return new ThreadCoroutineSupport(thread);
        }
    }

    public interface CoroutineFactory {
        CoroutineBase createCoroutine(CoroutineSupport support);
    }

    static final CoroutineFactory DEFAULT_COROUTINE_FACTORY = new CoroutineFactory() {
        @Override
        public CoroutineBase createCoroutine(CoroutineSupport support) {
            return new Coroutine(support);
        }
    };

    static final ThreadLocal<CoroutineFactory> COROUTINE_FACTORY = new ThreadLocal<CoroutineFactory>() {
        @Override
        protected CoroutineFactory initialValue() {
            return DEFAULT_COROUTINE_FACTORY;
        }
    };

    public static void setCoroutineFactory(CoroutineFactory factory) {
        COROUTINE_FACTORY.set(factory);
    }

    static final ThreadLocal<CoroutineSupport> COROUTINE_SUPPORT = new ThreadLocal<CoroutineSupport>() {
        @Override
        protected CoroutineSupport initialValue() {
            return CoroutineSupport.create(Thread.currentThread());
        }
    };

    public static CoroutineSupport currentCoroutineSupport() {
        // Thread.currentThread().getCoroutineSupport();
        return COROUTINE_SUPPORT.get();
    }

    // Controls debugging and tracing, for maximum performance the actual if(DEBUG/TRACE) code needs
    // to be commented out
    static final boolean DEBUG = false;
    static final boolean TRACE = false;

    static final Object TERMINATED = new Object();

    // The thread that this CoroutineSupport belongs to.
    // There's only one CoroutineSupport per Thread
    private final Thread thread;
    // The initial coroutine of the Thread
    protected final CoroutineBase threadCoroutine;

    // The currently executing, symmetric or asymmetric coroutine
    CoroutineBase currentCoroutine;
    // The anchor of the doubly-linked ring of coroutines
    Coroutine scheduledCoroutines;

    CoroutineSupport(Thread thread) {
        this.thread = thread;
        threadCoroutine = COROUTINE_FACTORY.get().createCoroutine(this);
        threadCoroutine.data = getInitialCoroutineData();
        currentCoroutine = threadCoroutine;
        if (threadCoroutine instanceof Coroutine) {
            addInitialCoroutine((Coroutine) threadCoroutine);
        }
    }

    private void addInitialCoroutine(Coroutine coroutine) {
        coroutine.next = coroutine;
        coroutine.last = coroutine;
        scheduledCoroutines = coroutine;
    }

    void addCoroutine(Coroutine coroutine, long stacksize) {
        initializeCoroutine(coroutine, stacksize);
        if (DEBUG) {
            System.out.println("add Coroutine " + coroutine + ", data" + coroutine.data);
        }

        // add the coroutine into the doubly linked ring
        if (scheduledCoroutines == null) {
            addInitialCoroutine(coroutine);
        } else {
            coroutine.next = scheduledCoroutines.next;
            coroutine.last = scheduledCoroutines;
            scheduledCoroutines.next = coroutine;
            coroutine.next.last = coroutine;
        }
    }

    void addCoroutine(AsymCoroutine<?, ?> coroutine, long stacksize) {
        initializeCoroutine(coroutine, stacksize);
        if (DEBUG) {
            System.out.println("add AsymCoroutine " + coroutine + ", data" + coroutine.data);
        }

        coroutine.caller = null;
    }

    Thread getThread() {
        return thread;
    }

    public void drain() {
        if (Thread.currentThread() != thread) {
            throw new IllegalArgumentException("Cannot drain another threads CoroutineThreadSupport");
        }

        if (DEBUG) {
            System.out.println("draining");
        }
        try {
            // drain all scheduled coroutines
            while (scheduledCoroutines.next != scheduledCoroutines) {
                symmetricExitInternal(scheduledCoroutines.next);
            }

            CoroutineBase coro;
            while ((coro = doCleanupCoroutine()) != null) {
                System.out.println(coro);
                throw new NotImplementedException();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void symmetricYield() {
        if (scheduledCoroutines != currentCoroutine) {
            throw new IllegalThreadStateException("Cannot call yield from within an asymmetric coroutine");
        }
        assert currentCoroutine instanceof Coroutine;

        if (TRACE) {
            System.out.println("looking for symmetric yield...");
        }

        Coroutine next = scheduledCoroutines.next;
        if (next == scheduledCoroutines) {
            return;
        }

        if (TRACE) {
            System.out.println("symmetric yield to " + next);
        }

        final Coroutine current = scheduledCoroutines;
        scheduledCoroutines = next;
        currentCoroutine = next;

        transferTo(current, next);
    }

    public void symmetricYieldTo(Coroutine target) {
        if (scheduledCoroutines != currentCoroutine) {
            throw new IllegalThreadStateException("Cannot call yield from within an asymmetric coroutine");
        }
        assert currentCoroutine instanceof Coroutine;

        moveCoroutine(scheduledCoroutines, target);

        final Coroutine current = scheduledCoroutines;
        scheduledCoroutines = target;
        currentCoroutine = target;

        transferTo(current, target);
    }

    private static void moveCoroutine(Coroutine a, Coroutine position) {
        // remove a from the ring
        a.last.next = a.next;
        a.next.last = a.last;

        // ... and insert at the new position
        a.next = position.next;
        a.last = position;
        a.next.last = a;
        position.next = a;
    }

    public void symmetricStopCoroutine(Coroutine target) {
        if (scheduledCoroutines != currentCoroutine) {
            throw new IllegalThreadStateException("Cannot call yield from within an asymmetric coroutine");
        }
        assert currentCoroutine instanceof Coroutine;

        moveCoroutine(scheduledCoroutines, target);

        final Coroutine current = scheduledCoroutines;
        scheduledCoroutines = target;
        currentCoroutine = target;

        transferToAndExit(current, target);
    }

    void symmetricExitInternal(Coroutine coroutine) {
        if (scheduledCoroutines != currentCoroutine) {
            throw new IllegalThreadStateException("Cannot call exitNext from within an unscheduled coroutine");
        }
        assert currentCoroutine instanceof Coroutine;
        assert currentCoroutine != coroutine;

        // remove the coroutine from the ring
        coroutine.last.next = coroutine.next;
        coroutine.next.last = coroutine.last;

        if (!isDisposableCoroutine(coroutine)) {
            // and insert it before the current coroutine
            coroutine.last = scheduledCoroutines.last;
            coroutine.next = scheduledCoroutines;
            coroutine.last.next = coroutine;
            scheduledCoroutines.last = coroutine;

            final Coroutine current = scheduledCoroutines;
            scheduledCoroutines = coroutine;
            currentCoroutine = coroutine;
            transferToAndExit(current, coroutine);
        }
    }

    void asymmetricCall(AsymCoroutine<?, ?> target) {
        if (target.threadSupport != this) {
            throw new IllegalArgumentException("Cannot activate a coroutine that belongs to another thread");
        }
        if (target.caller != null) {
            throw new IllegalArgumentException("Coroutine already in use");
        }
        if (target.data == 0) {
            throw new IllegalArgumentException("Target coroutine has already finished");
        }
        if (TRACE) {
            System.out.println("yieldCall " + target + " (" + target.data + ")");
        }

        final CoroutineBase current = currentCoroutine;
        target.caller = current;
        currentCoroutine = target;
        transferTo(target.caller, target);
    }

    void asymmetricReturn(final AsymCoroutine<?, ?> current) {
        if (current != currentCoroutine) {
            throw new IllegalThreadStateException("cannot return from non-current fiber");
        }
        final CoroutineBase caller = current.caller;
        if (TRACE) {
            System.out.println("yieldReturn " + caller + " (" + caller.data + ")");
        }

        current.caller = null;
        currentCoroutine = caller;
        transferTo(current, currentCoroutine);
    }

    void asymmetricReturnAndTerminate(final AsymCoroutine<?, ?> current) {
        if (current != currentCoroutine) {
            throw new IllegalThreadStateException("cannot return from non-current fiber");
        }
        final CoroutineBase caller = current.caller;
        if (TRACE) {
            System.out.println("yieldReturn " + caller + " (" + caller.data + ")");
        }

        current.caller = null;
        currentCoroutine = caller;
        transferToAndTerminate(current, currentCoroutine);
    }

    void terminateCoroutine() {
        assert currentCoroutine == scheduledCoroutines;
        assert currentCoroutine != threadCoroutine : "cannot exit thread coroutine";
        assert scheduledCoroutines != scheduledCoroutines.next : "last coroutine shouldn't call coroutineexit";

        Coroutine old = scheduledCoroutines;
        Coroutine forward = old.next;
        currentCoroutine = forward;
        scheduledCoroutines = forward;
        old.last.next = old.next;
        old.next.last = old.last;

        if (DEBUG) {
            System.out.println("to be terminated: " + old);
        }
        transferToAndTerminate(old, forward);
    }

    void terminateCallable() {
        assert currentCoroutine != scheduledCoroutines;
        assert currentCoroutine instanceof AsymCoroutine<?, ?>;

        if (DEBUG) {
            System.out.println("to be terminated: " + currentCoroutine);
        }
        asymmetricReturnAndTerminate((AsymCoroutine<?, ?>) currentCoroutine);
    }

    public boolean isCurrent(CoroutineBase coroutine) {
        return coroutine == currentCoroutine;
    }

    public CoroutineBase getCurrent() {
        return currentCoroutine;
    }

    // Interface
    protected abstract boolean verifyThread();

    protected abstract long getInitialCoroutineData();

    protected abstract void initializeCoroutine(CoroutineBase coroutine, long stacksize);

    protected abstract void transferTo(CoroutineBase current, CoroutineBase target);

    protected abstract void transferToAndTerminate(CoroutineBase current, CoroutineBase target);

    protected abstract void transferToAndExit(CoroutineBase current, CoroutineBase target);

    protected abstract boolean isDisposableCoroutine(CoroutineBase coroutine);

    protected abstract CoroutineBase doCleanupCoroutine();

}
