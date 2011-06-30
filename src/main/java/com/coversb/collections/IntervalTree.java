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

import java.util.LinkedList;
import java.util.List;

public class IntervalTree<T>
{
    private final IntervalNode head;

    public IntervalTree(List<Interval> intervals)
    {
        head = new IntervalNode(intervals);
    }

    public List<T> search(Interval searchInterval)
    {
        List<T> retlist = new LinkedList<T>();
        searchInternal(head, searchInterval, retlist);
        return retlist;
    }

    protected void searchInternal(IntervalNode node, Interval<T> searchInterval, List<T> retList)
    {
        if(null == node || node.v_pt == null)
            return;
        if(null == node)
            return;
        //if searchInterval.contains(node.v_pt)
        //then add every interval contained in this node to the result set then search left and right for further
        //overlapping intervals
        if(searchInterval.contains(node.v_pt))
        {
            for (Interval<T> interval : node.v_left)
            {
                retList.add(interval.Data);
            }

            searchInternal(node.left, searchInterval, retList);
            searchInternal(node.right, searchInterval, retList);
            return;
        }

        //if v.pt < searchInterval.left
        //add intervals in v with v[i].right >= searchitnerval.left
        //L contains no overlaps
        //R May
        if(node.v_pt.compareTo(searchInterval.min) < 0)
        {
            for (Interval<T> interval : node.v_right)
            {
                if(interval.max.compareTo(searchInterval.min) >= 0)
                {
                    retList.add(interval.Data);
                }
                else break;
            }
            searchInternal(node.right, searchInterval, retList);
            return;
        }

        //if v.pt > searchInterval.right
        //add intervals in v with [i].left <= searchitnerval.right
        //R contains no overlaps
        //L May
        if(node.v_pt.compareTo(searchInterval.max) > 0)
        {
            for (Interval<T> interval : node.v_left)
            {
                if(interval.min.compareTo(searchInterval.max) <= 0)
                {
                    retList.add(interval.Data);
                }
                else break;
            }
            searchInternal(node.left, searchInterval, retList);
            return;
        }
    }

}
