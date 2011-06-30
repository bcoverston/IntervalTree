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

import com.coversb.collections.Interval;
import com.coversb.collections.IntervalNode;
import junit.framework.TestCase;
import com.coversb.collections.*;
import sun.jvm.hotspot.utilities.*;

import java.util.ArrayList;
import java.util.List;

public class IntervalNodeTest extends TestCase
{
    public void testFindMedianEndpoint() throws Exception
    {
        List<Interval> intervalList = new ArrayList<Interval>();
        intervalList.add(new Interval(1, 2,"range1"));
        intervalList.add(new Interval(2, 3,"range2"));

        IntervalNode node = new IntervalNode(intervalList);
        Comparable c = node.findMedianEndpoint(intervalList);

        this.assertTrue(c.compareTo(2) == 0);
        this.assertFalse(c.compareTo(1) == 0);
        this.assertFalse(c.compareTo(0) == 0);
        this.assertFalse(c.compareTo(3) == 0);
    }

     public void testGetLeftIntervals() throws Exception
    {
        List<Interval> intervalList = new ArrayList<Interval>();
        intervalList.add(new Interval(1, 2,"range1"));
        intervalList.add(new Interval(2, 3,"range2"));
        intervalList.add(new Interval(3, 4,"range2"));
        intervalList.add(new Interval(4, 5,"range2"));
        intervalList.add(new Interval(6, 7,"range2"));



        IntervalNode node = new IntervalNode(intervalList);

        List<Interval> leftInterval = node.getLeftIntervals(intervalList);
        List<Interval> rightInterval = node.getRightIntervals(intervalList);
        List<Interval> centerInterval = node.getIntersectingIntervals(intervalList);

        this.assertEquals(leftInterval.size(), 2);
        this.assertEquals(rightInterval.size(), 1);
        this.assertEquals(centerInterval.size(), 2);
    }


}
