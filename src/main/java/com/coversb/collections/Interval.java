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

import com.google.common.collect.Ordering;

public class Interval<T>
{
    public final Comparable min;
    public final Comparable max;
    public final T Data;


    public Interval(Comparable min, Comparable max)
    {
        this.min = min;
        this.max = max;
        this.Data = null;
    }

    public Interval(Comparable min, Comparable max, T data)
    {
        this.min = min;
        this.max = max;
        this.Data = data;
    }

    public boolean encloses(Interval interval)
    {
        return (this.min.compareTo(interval.min) <= 0
            && this.max.compareTo(interval.max) >= 0);
    }

    public boolean contains(Comparable point)
    {
        return (this.min.compareTo(point) <= 0
             && this.max.compareTo(point) >= 0);
    }

    public boolean intersects(Interval interval)
    {
        return this.contains(interval.min) || this.contains(interval.min);
    }


    public static Ordering<Interval> minOrdering = new Ordering<Interval>()
    {
        @Override
        public int compare(Interval interval,Interval interval1)
        {
            return interval.min.compareTo(interval1.min);
        }
    };

    public static Ordering<Interval> maxOrdering = new Ordering<Interval>()
    {
        @Override
        public int compare(Interval interval, Interval interval1)
        {
            return interval.max.compareTo(interval1.max);
        }
    };

}
