
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

import java.util.ArrayList;
import java.util.List;

public class IntervalTreeTest extends TestCase
{
    public void testSearch() throws Exception
    {
        List<Interval> intervals = new ArrayList<Interval>();

        intervals.add(new Interval(-300, -200));
        intervals.add(new Interval(-3, -2));
        intervals.add(new Interval(1,2));
        intervals.add(new Interval(3,6));
        intervals.add(new Interval(2,4));
        intervals.add(new Interval(5,7));
        intervals.add(new Interval(1,3));
        intervals.add(new Interval(4,6));
        intervals.add(new Interval(8,9));
        intervals.add(new Interval(15,20));
        intervals.add(new Interval(40,50));
        intervals.add(new Interval(49,60));


        IntervalTree it = new IntervalTree(intervals);

        assertEquals(3,it.search(new Interval(4,4)).size());

        assertEquals(4, it.search(new Interval(4, 5)).size());

        assertEquals(7, it.search(new Interval(-1,10)).size());

        assertEquals(0, it.search(new Interval(-1,-1)).size());

        assertEquals(5, it.search(new Interval(1,4)).size());

        assertEquals(2, it.search(new Interval(0,1)).size());

        assertEquals(0, it.search(new Interval(10,12)).size());

        List<Interval> intervals2 = new ArrayList<Interval>();

        //stravinsky 1880-1971
        intervals2.add(new Interval(1880, 1971));
        //Schoenberg
        intervals2.add(new Interval(1874, 1951));
        //Grieg
        intervals2.add(new Interval(1843, 1907));
        //Schubert
        intervals2.add(new Interval(1779, 1828));
        //Mozart
        intervals2.add(new Interval(1756, 1828));
        //Schuetz
        intervals2.add(new Interval(1585, 1672));

        IntervalTree it2 = new IntervalTree(intervals2);

        assertEquals(0, it2.search(new Interval(1829, 1842)).size());

        List<Interval> intersection1 = it2.search(new Interval(1907, 1907));
        assertEquals(3, intersection1.size());

        intersection1 = it2.search(new Interval(1780, 1790));
        assertEquals(2, intersection1.size());

    }
}
