
/*
 * Copyright (c) 2011, Benjamin Jacob Coverston
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.coversb.collections;

import junit.framework.TestCase;

public class IntervalTest extends TestCase
{
    public void testEncloses() throws Exception
    {
        Interval interval = new Interval(0,5,null);
        Interval interval1 = new Interval(0, 10, null);
        Interval interval2 = new Interval(5,10,null);
        Interval interval3 = new Interval(0, 11, null);


        this.assertTrue(interval.encloses(interval));
        this.assertTrue(interval1.encloses(interval));
        this.assertFalse(interval.encloses(interval2));
        assertTrue(interval1.encloses(interval2));
        assertFalse(interval1.encloses(interval3));
    }

    public void testContains() throws Exception
    {
        Interval interval = new Interval(0, 5, null);
        assertTrue(interval.contains(0));
        assertTrue(interval.contains(5));
        assertFalse(interval.contains(-1));
        assertFalse(interval.contains(6));
    }

    public void testIntersects() throws Exception
    {
        Interval interval = new Interval(0,5,null);
        Interval interval1 = new Interval(0, 10, null);
        Interval interval2 = new Interval(5,10,null);
        Interval interval3 = new Interval(0, 11, null);
        Interval interval5 = new Interval(6,12,null);

        assertTrue(interval.intersects(interval1));
        assertTrue(interval.intersects(interval2));
        assertTrue(interval.intersects(interval3));
        assertFalse(interval.intersects(interval5));
    }
}
