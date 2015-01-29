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

package simx.profiler.model;

import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class represents a type of a message.
 * 
 * @author Stephan Rehfeld
 */
public class MessageType {

    /**
     * The raw type identifier, containing some technical information.
     */
    public final String rawType;
    
    /**
     * The type name of the message, including the packages.
     */
    public final String longType;
    
    /**
     * The class name only.
     */
    public final String shortType;
    
    /**
     * A map to the instances of this messages.
     */
    private final Map< Integer, MessageInstance > instances;
    
    /**
     * How often a message of this type has been processed.
     */
    private int timesProcessed;
    
    /**
     * How often a message of this type has been sent.
     */
    private int timesSent;
    
    /**
     * The average processing time of message of this type.
     */
    private double averageProcessingTime;
    
    /**
     * Information about how often actors of a specific type sent message of this message type.
     */
    private final Map<ActorType, Integer> sentByTypeStatistic;
    
    /**
     * Back reference to the profiling data.
     */
    public final ProfilingData profilingData;
    
    /**
     * Information about how often a specific actor instance sent message of this message type.
     */
    private final Map<ActorInstance, Integer> sentByInstanceStatistic;
    
    
    private final Map<ActorType, ImmutableTupel<Integer, Double>> receivedByTypeStatistic;
    private final Map<ActorInstance, ImmutableTupel<Integer, Double>> receivedByInstanceStatistic;

    public int getTimesProcessed() {
        return timesProcessed;
    }

    public int getTimesSent() {
        return timesSent;
    }
    
    public double getAverageProcessingTime() {
        return this.averageProcessingTime;
    }

    public Map<ActorType, Integer> getSentByTypeStatistic() {
        return new HashMap<>( this.sentByTypeStatistic );
    }
    
    public Map<ActorInstance, Integer> getSentByInstanceStatistic() {
        return new HashMap<>( this.sentByInstanceStatistic );
    }

    public Map<ActorType, ImmutableTupel< Integer, Double > > getReceivedByTypeStatistic() {
        return new HashMap<>( this.receivedByTypeStatistic );
    }

    public Map<ActorInstance, ImmutableTupel< Integer, Double > > getReceivedByInstanceStatistic() {
        return new HashMap<>( this.receivedByInstanceStatistic );
    }
    
    MessageType( final String rawType, final ProfilingData profilingData ) {
        if( rawType == null ) throw new IllegalArgumentException( "The parameter 'rawType' must not be null!" );
        if( rawType.isEmpty() ) throw new IllegalArgumentException( "The parameter 'rawType' must not be an empty string!" );
        if( profilingData == null ) throw new IllegalArgumentException( "The parameter 'profilingData' must not be null!" );
        this.rawType = rawType;
        String[] segments = rawType.split( " " );
        this.longType = segments[ segments.length -1 ];
        segments = rawType.split( "\\." );
        this.shortType = segments[ segments.length -1 ];
        this.instances = new HashMap<>();
        this.timesProcessed = 0;
        this.timesSent = 0;
        this.averageProcessingTime = 0.0;
        this.sentByTypeStatistic = new HashMap<>();
        this.sentByInstanceStatistic = new HashMap<>();
        this.profilingData = profilingData;
        this.receivedByTypeStatistic = new HashMap<>();
        this.receivedByInstanceStatistic = new HashMap<>();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.rawType != null ? this.rawType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessageType other = (MessageType) obj;
        return !((this.rawType == null) ? (other.rawType != null) : !this.rawType.equals(other.rawType));
    }

    @Override
    public String toString() {
        return "MessageType{" + "type=" + longType + '}';
    }  

    MessageInstance getOrRegisterInstance( final int messageID ) {
        if( this.instances.containsKey( messageID ) ) {
            return this.instances.get( messageID );
        }
        final MessageInstance messageInstance = new MessageInstance( this, messageID );
        this.instances.put(messageID, messageInstance);
        return messageInstance;
    }

    void processed( final ActorInstance actorInstance, final long time ) {
        this.averageProcessingTime = this.averageProcessingTime * (double)this.timesProcessed / (this.timesProcessed+1.0) + (double)time / (this.timesProcessed+1.0) ;
        if( this.receivedByTypeStatistic.containsKey( actorInstance.type ) ) {
            final ImmutableTupel< Integer, Double > old = this.receivedByTypeStatistic.get( actorInstance.type );
            final double newAverage = old.b * (double)old.a / (old.a+1.0) + (double)time / (old.a+1.0) ;
            this.receivedByTypeStatistic.put( actorInstance.type, new ImmutableTupel<>( old.a+1, newAverage ) );
        } else {
            this.receivedByTypeStatistic.put( actorInstance.type, new ImmutableTupel<>( 1, (double)time ) );
        }
        if( this.receivedByInstanceStatistic.containsKey( actorInstance ) ) {
            final ImmutableTupel< Integer, Double > old = this.receivedByInstanceStatistic.get( actorInstance );
            final double newAverage = old.b * (double)old.a / (old.a+1.0) + (double)time / (old.a+1.0) ;
            this.receivedByInstanceStatistic.put( actorInstance, new ImmutableTupel<>( old.a+1, newAverage ) );
        } else {
            this.receivedByInstanceStatistic.put( actorInstance, new ImmutableTupel<>( 1, (double)time ) );
        }
        this.timesProcessed++;
    }
    
    void sent( final ActorInstance actorInstance ) {
        if( this.sentByTypeStatistic.containsKey( actorInstance.type ) ) {
            this.sentByTypeStatistic.put( actorInstance.type, this.sentByTypeStatistic.get( actorInstance.type ) + 1 );
        } else {
            this.sentByTypeStatistic.put( actorInstance.type, 1 );
        }
        
        if( this.sentByInstanceStatistic.containsKey( actorInstance ) ) {
            this.sentByInstanceStatistic.put( actorInstance, this.sentByInstanceStatistic.get( actorInstance ) + 1 );
        } else {
            this.sentByInstanceStatistic.put( actorInstance, 1 );
        }
        this.timesSent++;
    }
    
}
