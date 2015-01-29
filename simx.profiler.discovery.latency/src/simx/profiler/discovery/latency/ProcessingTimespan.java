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
import java.util.List;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.MessageType;

/**
 *
 * @author Stephan Rehfeld
 */
public class ProcessingTimespan {

    
   
    public enum TimespanType {
        BEGIN_OF_SIMULATION_LOOP_TO_MESSAGE_IN_MAILBOX,
        MESSAGE_WAITS_IN_MAIL_BOX,
        BEGIN_OF_MESSAGE_PROCESSING_TO_BEGIN_OF_SIMULATION_LOOP, 
        BEGIN_OF_MESSAGE_PROCESSING_TO_MESSAGE_IN_MAILBOX,
        FINAL_MESSAGE_PROCESSED,
        FINAL_SIMULATION_LOOP
    }
    
    private ProcessingTimespan next;
    private final ActorInstance actorInstance;
    private final MessageType messageType;
    private final TimespanType timespanType;
    
    private long min;
    private long max;
    private double avg;
    private int counter;
    private final List<Long> med;
    
    ProcessingTimespan( final ActorInstance actorInstance, final MessageType messageType, final TimespanType timespanType ) {
        this.actorInstance = actorInstance;
        this.messageType = messageType;
        this.timespanType = timespanType;
        
        this.min = Long.MAX_VALUE;
        this.max = Long.MIN_VALUE;
        this.avg = 0.0;
        this.counter = 0;
        this.med = new ArrayList<>();
    }

    public ProcessingTimespan getNext() {
        return next;
    }

    public void setNext( final ProcessingTimespan next ) {
        this.next = next;
    }

    public ActorInstance getActorInstance() {
        return actorInstance;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public TimespanType getTimespanType() {
        return timespanType;
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
    
    void registerTimespan( final long timespan ) {
        if( timespan < this.min ) min = timespan;
        if( timespan > this.max ) max = timespan;
        this.avg = avg * (double)this.counter / ((double)this.counter+1.0) + (double)timespan/((double)this.counter+1.0);
        ++this.counter;
        this.med.add( timespan );
        
    }
    
    public long getMed() {
        Collections.sort( this.med );
        if( this.med.isEmpty()) return -1;
        return this.med.get( this.med.size() / 2 );
    }
    
    int length() {
        if( this.next == null ) return 1;
        return this.next.length() + 1;
    }
    
    ProcessingTimespan get( int index ) {
        if( index == 0 ) return this;
        return this.next.get( index - 1 );
    }
    
}
