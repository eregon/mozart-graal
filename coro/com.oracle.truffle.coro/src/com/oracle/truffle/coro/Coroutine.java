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

/**
 * Implementation of symmetric coroutines. A Coroutine will take part in thread-wide scheduling of
 * coroutines. It transfers control to the next coroutine whenever yield is called.
 * <p>
 * Similar to {@link Thread} there are two ways to implement a Coroutine: either by implementing a
 * subclass of Coroutine (and overriding {@link #run()}) or by providing a {@link Runnable} to the
 * Coroutine constructor.
 * <p>
 * An implementation of a simple Coroutine might look like this:
 * <p>
 * <hr>
 * <blockquote>
 *
 * <pre>
 * class Numbers extends Coroutine {
 *     public void run() {
 *         for (int i = 0; i &lt; 10; i++) {
 *             System.out.println(i);
 *             yield();
 *         }
 *     }
 * }
 * </pre>
 *
 * </blockquote>
 * <hr>
 * <p>
 * A Coroutine is active as soon as it is created, and will run as soon as control is transferred to
 * it:
 * <p>
 * <blockquote>
 *
 * <pre>
 * new Numbers();
 * for (int i = 0; i &lt; 10; i++)
 *     yield();
 * </pre>
 *
 * </blockquote>
 * <p>
 *
 * @author Lukas Stadler
 */
public class Coroutine extends CoroutineBase {
    private final Runnable target;

    Coroutine next;
    Coroutine last;

    public Coroutine() {
        this.target = null;
        threadSupport.addCoroutine(this, -1);
    }

    public Coroutine(Runnable target) {
        this.target = target;
        threadSupport.addCoroutine(this, -1);
    }

    public Coroutine(long stacksize) {
        this.target = null;
        threadSupport.addCoroutine(this, stacksize);
    }

    public Coroutine(Runnable target, long stacksize) {
        this.target = target;
        threadSupport.addCoroutine(this, stacksize);
    }

    /** To create the initial coroutine for a new thread */
    public Coroutine(CoroutineSupport threadSupport) {
        super(threadSupport, 0);
        this.target = null;
    }

    /**
     * Yields execution to the next coroutine in the current threads coroutine queue.
     */
    public static void yield() {
        getCurrentCoroutineSupport().symmetricYield();
    }

    public static void yieldTo(Coroutine target) {
        getCurrentCoroutineSupport().symmetricYieldTo(target);
    }

    public void stop() {
        getCurrentCoroutineSupport().symmetricStopCoroutine(this);
    }

    @Override
    protected void run() {
        assert threadSupport.verifyThread();
        if (target != null) {
            target.run();
        }
    }
}