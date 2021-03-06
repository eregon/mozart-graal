/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

/* @test
 * @summary unit tests for coroutines
 * @run junit/othervm test.java.dyn.CoroutineTest
 */

package com.oracle.truffle.coro.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.oracle.truffle.coro.AsymCoroutine;
import com.oracle.truffle.coro.AsymRunnable;
import com.oracle.truffle.coro.Coroutine;
import com.oracle.truffle.coro.CoroutineBase;
import com.oracle.truffle.coro.CoroutineSupport;
import com.oracle.truffle.coro.CoroutineSupport.CoroutineFactory;

@SuppressWarnings("unused")
public class CoroutineTest {
    private StringBuilder seq;

    @BeforeClass
    public static void setup() {
        CoroutineSupport support = CoroutineSupport.currentCoroutineSupport();
        System.out.println("Coroutine implementation: " + support.getClass().getName());
    }

    @Before
    public void before() {
        seq = new StringBuilder();
        seq.append("a");
    }

    @Test
    public void testNewCoroutine() {
        Coroutine coro = new Coroutine() {
            @Override
            protected void run() {
                seq.append("b");
            }
        };

        assertFalse(coro.isFinished());
        Coroutine.yield();
        seq.append("c");

        assertTrue(coro.isFinished());
        assertEquals("abc", seq.toString());
    }

    @Test
    public void symSequence() {
        Coroutine coro = new Coroutine() {
            @Override
            protected void run() {
                seq.append("c");
                for (int i = 0; i < 3; i++) {
                    yield();
                    seq.append("e");
                }
            }
        };
        seq.append("b");
        assertFalse(coro.isFinished());
        Coroutine.yield();
        for (int i = 0; i < 3; i++) {
            seq.append("d");
            assertFalse(coro.isFinished());
            Coroutine.yield();
        }
        seq.append("f");
        assertTrue(coro.isFinished());
        Coroutine.yield();
        seq.append("g");
        assertEquals("abcdededefg", seq.toString());
    }

    @Test
    public void symMultiSequence() {
        for (int i = 0; i < 10; i++)
            new Coroutine() {
                @Override
                protected void run() {
                    seq.append("c");
                    yield();
                    seq.append("e");
                }
            };
        seq.append("b");
        Coroutine.yield();
        seq.append("d");
        Coroutine.yield();
        seq.append("f");
        Coroutine.yield();
        seq.append("g");
        assertEquals("abccccccccccdeeeeeeeeeefg", seq.toString());
    }

    @Test
    public void asymSequence() {
        AsymCoroutine<Void, Void> coro = new AsymCoroutine<Void, Void>() {
            @Override
            protected Void run(Void value) {
                seq.append(value + "b");
                Object o = ret();
                seq.append(o + "d");
                return null;
            }
        };
        assertFalse(coro.isFinished());
        coro.call();
        assertFalse(coro.isFinished());
        seq.append("c");
        coro.call();
        seq.append("e");
        assertTrue(coro.isFinished());

        RuntimeException exception = null;
        try {
            coro.call();
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("anullbcnullde", seq.toString());
    }

    @Test
    public void asymMultiSequence() {
        AsymCoroutine<Void, Void> coro = null;
        for (int j = 4; j >= 0; j--) {
            final AsymCoroutine<Void, Void> last = coro;
            final int i = j;
            coro = new AsymCoroutine<Void, Void>() {
                @Override
                protected Void run(Void value) {
                    seq.append("b" + i);
                    if (last != null)
                        last.call();
                    seq.append("c" + i);
                    ret();
                    seq.append("e" + i);
                    if (last != null)
                        last.call();
                    seq.append("f" + i);
                    return null;
                }
            };
        }
        seq.append("_");
        assertFalse(coro.isFinished());
        coro.call();
        assertFalse(coro.isFinished());
        seq.append("d");
        coro.call();
        seq.append("g");
        assertTrue(coro.isFinished());

        RuntimeException exception = null;
        try {
            coro.call();
        } catch (RuntimeException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("a_b0b1b2b3b4c4c3c2c1c0de0e1e2e3e4f4f3f2f1f0g", seq.toString());
    }

    @Test
    public void asymReturnValue() {
        AsymCoroutine<Integer, Integer> coro = new AsymCoroutine<Integer, Integer>() {
            @Override
            protected Integer run(Integer value0) {
                int value = value0;
                value = ret(value * 2 + 1);
                value = ret(value * 2 + 2);
                value = ret(value * 2 + 3);
                value = ret(value * 2 + 4);
                value = ret(value * 2 + 5);
                return value * 2 + 6;
            }
        };
        assertFalse(coro.isFinished());
        assertEquals(2001, (int) coro.call(1000));
        assertEquals(4002, (int) coro.call(2000));
        assertEquals(6003, (int) coro.call(3000));
        assertEquals(8004, (int) coro.call(4000));
        assertEquals(10005, (int) coro.call(5000));
        assertEquals(12006, (int) coro.call(6000));
        assertTrue(coro.isFinished());
    }

    @Test
    public void gcTest1() {
        new Coroutine() {
            @Override
            protected void run() {
                seq.append("c");
                Integer v1 = 1;
                Integer v2 = 14555668;
                yield();
                seq.append("e");
                seq.append("(" + v1 + "," + v2 + ")");
            }
        };
        seq.append("b");
        System.gc();
        Coroutine.yield();
        System.gc();
        seq.append("d");
        Coroutine.yield();
        seq.append("f");
        Coroutine.yield();
        seq.append("g");
        assertEquals("abcde(1,14555668)fg", seq.toString());
    }

    @Test
    public void exceptionTest1() {
        Coroutine coro = new Coroutine() {
            @Override
            protected void run() {
                seq.append("c");
                long temp = System.nanoTime();
                if (temp != 0)
                    throw new RuntimeException();
                yield();
                seq.append("e");
            }
        };
        seq.append("b");
        assertFalse(coro.isFinished());
        Coroutine.yield();
        seq.append("d");
        Coroutine.yield();
        seq.append("f");
        assertEquals("abcdf", seq.toString());
    }

    @Test
    public void largeStackframeTest() {
        new Coroutine() {
            @Override
            protected void run() {
                seq.append("c");
                Integer v0 = 10000;
                Integer v1 = 10001;
                Integer v2 = 10002;
                Integer v3 = 10003;
                Integer v4 = 10004;
                Integer v5 = 10005;
                Integer v6 = 10006;
                Integer v7 = 10007;
                Integer v8 = 10008;
                Integer v9 = 10009;
                Integer v10 = 10010;
                Integer v11 = 10011;
                Integer v12 = 10012;
                Integer v13 = 10013;
                Integer v14 = 10014;
                Integer v15 = 10015;
                Integer v16 = 10016;
                Integer v17 = 10017;
                Integer v18 = 10018;
                Integer v19 = 10019;
                yield();
                int sum = v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16 + v17 + v18 + v19;
                seq.append("e" + sum);
            }
        };
        seq.append("b");
        System.gc();
        Coroutine.yield();
        System.gc();
        seq.append("d");
        Coroutine.yield();
        seq.append("f");
        assertEquals("abcde200190f", seq.toString());
    }

    @Test
    public void shaTest() {
        Coroutine coro = new Coroutine(65536) {
            @Override
            protected void run() {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA");
                    digest.update("TestMessage".getBytes());
                    seq.append("b");
                    yield();
                    seq.append(digest.digest()[0]);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        };
        Coroutine.yield();
        seq.append("c");
        assertFalse(coro.isFinished());
        Coroutine.yield();
        assertTrue(coro.isFinished());
        assertEquals("abc72", seq.toString());
    }

    // @Test
    public void stackoverflowTest() {
        for (int i = 0; i < 10; i++) {
            new Coroutine(65536) {
                int i = 0;

                @Override
                protected void run() {
                    System.out.println("start");
                    try {
                        iter();
                    } catch (StackOverflowError e) {
                        System.out.println("i: " + i);
                    }
                    System.out.println("asdf");
                }

                private void iter() {
                    System.out.print(".");
                    i++;
                    iter();
                }
            };
        }
        Coroutine.yield();
    }

    @Test
    public void threadCoroutineIsAsym() throws InterruptedException {
        final Error[] error = new Error[1];
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    CoroutineSupport.setCoroutineFactory(new CoroutineFactory() {
                        @Override
                        public CoroutineBase createCoroutine(CoroutineSupport support) {
                            return new AsymCoroutine<Integer, Integer>(support);
                        }
                    });

                    Assert.assertEquals(CoroutineBase.current().getClass(), AsymCoroutine.class);

                    AsymCoroutine<Integer, Integer> asym = new AsymCoroutine<>(new AsymRunnable<Integer, Integer>() {
                        public Integer run(AsymCoroutine<? extends Integer, ? super Integer> coro, Integer value) {
                            int a = value;
                            int b = coro.ret(0);
                            return a + b;
                        }
                    });
                    asym.call(14);
                    int sum = asym.call(21);
                    Assert.assertEquals(35, sum);

                    // Cannot call a sym coro from an asym coro
                    Coroutine coro = new Coroutine(new Runnable() {
                        @Override
                        public void run() {
                            throw new Error();
                        }
                    });
                    try {
                        Coroutine.yieldTo(coro);
                        Assert.fail();
                    } catch (IllegalThreadStateException e) {
                    }
                } catch (Error e) {
                    error[0] = e;
                }
            }
        };
        t.start();
        t.join();
        if (error[0] != null) {
            throw error[0];
        }
    }
}
