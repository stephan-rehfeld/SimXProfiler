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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ActorType reprents a data type of an acotor loaded of profiling data of an
 * Simulator X application. It references to ActorInstances.
 * 
 * @author Stephan Rehfeld
 */
public class ActorType {
   
    /**
     * The raw type name of the actor, as found in the profiling data files.
     */
    public final String rawTypeName;
    
    /**
     * The long type name without "class" at the beginning but with full package.
     */
    public final String longTypeName;
    
    /**
     * The short type name; the class name only.
     */
    public final String shortTypeName;
    
    /**
     * The set of all instances of this type.
     */
    private final Set< ActorInstance > instances;
    
    /**
     * The parent profiling data.
     */
    public final ProfilingData profilingData;
    
    /**
     * Total number of messages sent by actors of this type.
     */
    private int sentMessagesCount;
    
    /**
     * Total number of message received by actors of this type.
     */
    private int receivedMessagesCount;
    
    /**
     * Overall processing time of all instances of this type.
     */
    private long overallProcessingTime;
    
    /**
     * Statistical data about how many messages of a specific type has been sent
     * by instances of this actor type.
     */
    private final Map<MessageType, Integer> sentMessagesStatistic;
    
    /**
     * Statistical data about how many message of a specific type bas been
     * received by instances of this actor type.
     */
    private final Map<MessageType, Integer> receivedMessagesStatistic;
    
    /**
     * Statistical data about how much time for processing message of the given
     * type all instances spent.
     */
    private final Map<MessageType, Long> processedMessagesStatistic;
    
    
    /**
     * Statistical data about which messages and how many of them are sent by
     * instances of this type to instances of another actor types.
     */
    private final Map<ActorType, Map<MessageType, Integer>> receiverStatistics;
    
    /**
     * An internal counter that is used during model construction to calculate
     * the average time messages spent in a mailbox.
     */
    private int averageTimeInMailboxCounter;
    
    /**
     * The average type a message spent in a mailbox of actors of this type.
     */
    private double averageTimeInMailbox;
    
    /**
     * This method returns the number of instances of this type.
     * 
     * @return  The number of instances oft his type.
     */
    public int getInstancesCount() {
        return this.instances.size();
    }
    
    /**
     * This method returns a list of all instances of this type.
     * 
     * @return A list of all instances of this type.
     */
    public List<ActorInstance> getActorInstances() {
        return new ArrayList<>( this.instances );
    }
    
    /**
     * This method returns the number of sent messages.
     * 
     * @return The number of sent messages.
     */
    public int getSentMessagesCount() {
        return this.sentMessagesCount;
    }
       
    /**
     * This method returns the number of received messages.
     * 
     * @return The number of received messages.
     */
    public int getReceivedMessagesCount() {
        return this.receivedMessagesCount;
    }
     
    /**
     * This method returns the overall processing time of all instances of this
     * type.
     * 
     * @return The overall processing time of all instances of this type.
     */
    public long getOverallProcessingTime() {
        return this.overallProcessingTime;
    }
     
    /**
     * This method returns a map that contains the number of sent messages for
     * each message type.
     * 
     * @return A map that contains the number of sent messages for each message
     *         type.
     */
    public Map<MessageType, Integer> sentMessagesStatistic() {
        return new HashMap<>( this.sentMessagesStatistic );
    }
    
    /**
     * This method returns a map that contains the number of received messages
     * for each message type.
     * 
     * @return  A map that contains the number of sent message for each message
     *          type.
     */
    public Map<MessageType, Integer> receivedMessagesStatistic() {
        return new HashMap<>( this.receivedMessagesStatistic );
    }
    
    /**
     * This method returns a map that contains the accumulated proessing time
     * for each message type.
     * 
     * @return A map that contains the accumulated processing time for each
     *         message type.
     */
    public Map<MessageType, Long > processedMessagesStatistic() {
        return new HashMap<>( this.processedMessagesStatistic );
    }
    
    /**
     * This method returns a map that contains statisical data how many messages
     * of each message type has been sent to actors of another data type.
     * 
     * @return A map that contains statistical data how many messages of each
     *         message type has been sent to actors of another data type.
     */
    public Map< ActorType, Map< MessageType, Integer > > getReceiverStatistics() {
        final Map< ActorType, Map< MessageType, Integer > > r = new HashMap<>();      
        this.receiverStatistics.entrySet().stream().forEach((e) -> {     
            r.put( e.getKey(), new HashMap<>( e.getValue() ) );
        });
        return r;
    }

    public double getAverageTimeInMailbox() {
        return this.averageTimeInMailbox;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.rawTypeName);
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
        final ActorType other = (ActorType) obj;
        return Objects.equals(this.rawTypeName, other.rawTypeName);
    }

    @Override
    public String toString() {
        return "ActorType{" + "longTypeName=" + longTypeName + '}';
    }
    

    // -------------------------------------------------------------------------
    // Package internal API
    // -------------------------------------------------------------------------

    /**
     * This constructor initalizes a new instance of ActorInstance.
     * 
     * @param profilingData The parent profiling data.
     * @param rawTypeName The name of the type. Must not be null and must not be empty.
     */
    ActorType( final ProfilingData profilingData, final String rawTypeName ) {
        if( profilingData == null ) throw new IllegalArgumentException( "The parameter 'profilingData' must not be null!" );
        if( rawTypeName == null ) throw new IllegalArgumentException( "The parameter 'rawTypeName' must not be null!" );
        if( rawTypeName.isEmpty() ) throw new IllegalArgumentException( "The parameter 'rawTypeName' must not be an empty string!" );
        
        this.profilingData = profilingData;
        this.rawTypeName = rawTypeName;
        if( rawTypeName.equals( "Unknow Type" ) ) {
            this.longTypeName = rawTypeName;
            this.shortTypeName = rawTypeName;
        } else {
            String[] segments = rawTypeName.split( " " );
            this.longTypeName = segments[ segments.length -1 ];
            segments = rawTypeName.split( "\\." );
            this.shortTypeName = segments[ segments.length -1 ];
        }
        
        
        this.instances = new HashSet<>();  
        this.sentMessagesCount = 0;
        this.receivedMessagesCount = 0;
        this.overallProcessingTime = 0;
        this.sentMessagesStatistic = new HashMap<>();
        this.receivedMessagesStatistic = new HashMap<>();
        this.processedMessagesStatistic = new HashMap<>();
        this.receiverStatistics = new HashMap<>();
    }
    
    /**
     * Registers a message sent event to this actor type.
     * 
     * @param messageType The message sent event. Must not be null!
     */
    void messageSent( final MessageType messageType ) {
        if( messageType == null ) throw new IllegalArgumentException( "The parameter 'messageType' must not be null!" );
        if( this.sentMessagesStatistic.containsKey( messageType ) ) {
            this.sentMessagesStatistic.put(messageType, this.sentMessagesStatistic.get( messageType ) + 1 );
        } else {
            this.sentMessagesStatistic.put( messageType, 1 );
        }
        this.sentMessagesCount++;
    }
    
    /**
     * Creates and returns the instance information for the actor instance.
     * 
     * @param id The id of the actor instance. Must not be null or empty.
     * @param created The time stamp where the actor has been created.
     * @param supervisor The id of the supervisor of this actor.
     * 
     * @return The object that represents the instance.
     */
     ActorInstance registerInstance( final String id, final long created, final String supervisor ) {
        if( id == null ) throw new IllegalArgumentException( "The parameter 'id' must not be null!" );
        this.profilingData.registerActorCreationTimeStamp( created );
        
        final List< ActorInstance > actorInstances = this.profilingData.getActorInstances();
        ActorInstance supervisorInstance = this.profilingData.getUnknownActorInstance();
        for( final ActorInstance i : actorInstances ) {
            if( i.id.equals( supervisor ) ) {
                supervisorInstance = i;
                break;
            }
        }
        
        final ActorInstance instance = new ActorInstance( this, id, created, supervisorInstance );
        this.instances.add( instance );
        this.profilingData.registerInstance( id, instance );
        return instance;
    }
    
     /**
     * Creates and returns the instance information for the actor instance. This
     * method is a special implementation to register the Unknown Type. No
     * timestamp and supervisor is required.
     * 
     * @param id The id of the actor instance. Must not be null or empty.
     * 
     * @return The object that represents the instance.
     */ 
    ActorInstance registerInstance( final String id ) {
        if( id == null ) throw new IllegalArgumentException( "The parameter 'id' must not be null!" );
        final ActorInstance instance = new ActorInstance( this, id, -1, null );
        this.instances.add( instance );
        this.profilingData.registerInstance( id, instance );
        return instance;
    }
    
    /**
     * This method return a set that contains all instances of this type.
     * 
     * @return A set that contains all instances of this type.
     */
    Set< ActorInstance> getInstances() {
        return new HashSet<>( this.instances );
    }
    
    /**
     * This method adds a message processing event to this type.
     * 
     * @param messageProcessingEvent The messade processing event to add.
     */
    void messageProcessed( final MessageProcessingEvent messageProcessingEvent ) {
        
        if( messageProcessingEvent == null ) throw new IllegalArgumentException( "The parameter 'messageProcessingEvent' must not be null!" );
        
        final long processingTime = messageProcessingEvent.end - messageProcessingEvent.start;
        this.overallProcessingTime += processingTime;
        final MessageType messageType = messageProcessingEvent.messageInstance.type;
        
        if( this.receivedMessagesStatistic.containsKey( messageType ) ) {
            this.receivedMessagesStatistic.put( messageType, this.receivedMessagesStatistic.get( messageType ) + 1 );
        } else {
            this.receivedMessagesStatistic.put( messageType, 1 );
        }
        
        if( this.processedMessagesStatistic.containsKey( messageType ) ) {
            this.processedMessagesStatistic.put( messageType, this.processedMessagesStatistic.get( messageType ) + processingTime );
        } else {
            this.processedMessagesStatistic.put( messageType, processingTime );
        }
        
        if( messageProcessingEvent.messageInstance.getMessageSentEvents().size() == 1 ) {
            this.averageTimeInMailbox = (this.averageTimeInMailbox * this.averageTimeInMailboxCounter / (this.averageTimeInMailbox+1) ) + ((messageProcessingEvent.messageInstance.getMessageSentEvents().get( 0 ).timestamp - messageProcessingEvent.start) / (this.averageTimeInMailbox+1) );
            this.averageTimeInMailboxCounter++;
        }
        
        this.receivedMessagesCount++;
    }
    
    void registerCommunication( final ActorType type, MessageType messageType ) {
        if( this.receiverStatistics.containsKey( type ) ) {
            final Map< MessageType, Integer > d = this.receiverStatistics.get( type );
            if( d.containsKey( messageType ) ) {
                d.put( messageType, d.get( messageType ) + 1 );
            } else {
                d.put( messageType, 1 );
            }
        } else {
            final Map< MessageType, Integer > d = new HashMap<>();
            d.put( messageType, 1 );
            this.receiverStatistics.put( type, d );
        }
    }

}
