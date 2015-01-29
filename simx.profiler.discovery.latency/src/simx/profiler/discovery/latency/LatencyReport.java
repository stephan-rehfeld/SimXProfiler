/*
 * Copyright 2015 Stephan Rehfeld
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package simx.profiler.discovery.latency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Stephan Rehfeld
 */
public class LatencyReport {
    private final List< ImmutableTuple<Long,Long> > overallLatencies;
    private final ProcessingTimespan start;
    private final long min;
    private final long max;
    private final double avg;
    private final long med;
    
    
    public LatencyReport( final List< ImmutableTuple<Long,Long> > overallLatencies, final ProcessingTimespan start ) {
        this.overallLatencies = overallLatencies;
        this.start = start;
        long minBuffer = Long.MAX_VALUE;
        long maxBuffer = Long.MIN_VALUE;
        double avgBuffer = 0.0;
        
        for( final ImmutableTuple<Long,Long> d : this.overallLatencies ) {
            if( d.b < minBuffer ) minBuffer = d.b;
            if( d.b > maxBuffer ) maxBuffer = d.b;
            avgBuffer += d.b;
        }
        avgBuffer /= this.overallLatencies.size();
        final List< ImmutableTuple< Long,Long > > medBuffer = new ArrayList<>( this.overallLatencies );
        Collections.sort( medBuffer, (final ImmutableTuple<Long, Long> f, final ImmutableTuple<Long, Long> s) -> {
            if( Objects.equals(f.b, s.b) ) return 0;
            if( f.b < s.b ) return -1;
            return 1;
        });
        
        this.min = minBuffer;
        this.max = maxBuffer;
        this.avg = avgBuffer;
        this.med = medBuffer.get( medBuffer.size() / 2 ).b;
    }

    public List< ImmutableTuple< Long,Long > > getOverallLatencies() {
        return overallLatencies;
    }

    public ProcessingTimespan getStart() {
        return start;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public double getAvg() {
        return avg;
    }

    public long getMed() {
        return med;
    }

}
