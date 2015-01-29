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
 *
 * An ActorInstance represents an instance of an actor loaded from the profiling
 * data of SimulatorX.
 * 
 * @author Stephan Rehfeld
 */
public class ActorInstance {
   
    /**
     * The type of the actor.
     */
    public final ActorType type;
    
    /**
     * The id of this instance. Usually the path that has been appended by Akka.
     */
    public final String id;
    
    /**
     * A short version of the id, usally the last element of the URL.
     */
    public final String shortId;
    
    /**
     * The timestamp of creation or first appearence of this actor.
     */
    public final long created;
    
    /**
     * The supervisor of this actor instance. May be null is this is the unknown instance.
     */
    public final ActorInstance supervisor;
    
    /**
     * This list contains all message sent event.
     */
    private final List< MessageSentEvent > messagesSent;
    
    /**
     * The list of all processed messages and the duration of processing.
     */
    private final List< MessageProcessingEvent > messagesProcessed;
    
    // -------------------------------------------------------------------------
    // The following data can be derived from the previous lists. They are
    // computed while the model is construted from the measured data to save
    // time while inspecting the data in the profiler.
    // -------------------------------------------------------------------------
    
    /**
     * A set that contains the type of all processed messages.
     */
    private final Set< MessageType > processedMessageTypes;
    
    /**
     * Total time how long this actor actually processed messages.
     */
    private long overallProcessingTime;
    
    /**
     * Statistical about sent messages. The map contains the number of sent
     * messages for each message type.
     */
    private final Map<MessageType, Integer> sentMessagesStatistic;
    
    /**
     * Statistical about received messages. The map contains the number of
     * received messages for each type.
     */
    private final Map<MessageType, Integer> receivedMessagesStatistic;
    
    /**
     * Statistical data about processed messages. The map contains the
     * accumulated processing time for each message type.
     */
    private final Map<MessageType, Long> processedMessagesStatistic;
    
    /**
     * Statistical data about message receitiens. For each receiver of messages
     * from this actor instance, the number of each message type is saved.
     */
    private final Map<ActorInstance, Map<MessageType, Integer>> receiverStatistics;
    
    private int averageTimeInMailboxCounter;
    
    private double averageTimeInMailbox;
    
    /**
     * Returns the number of messages sent by this actor instance.
     * 
     * @return The number of messages sent by this actor instance.
     */
    public int getSentMessagesCount() {
        return this.messagesSent.size();
    }

    /**
     * Returns the number of messages received by this actor instance.
     * 
     * @return The number of messages received by this actor instance.
     */
    public int getReceivesMessagesCount() {
        return this.messagesProcessed.size();
    }
    
    /**
     * Returns the overall processing time of this actor instance.
     * 
     * @return The overall processing time of this actor instance.
     */
    public long getOverallProcessingTime() {
        return this.overallProcessingTime;
    }

    /**
     * This method returns a set that contains the types of all processed
     * messages.
     * 
     * @return A Set that contains all message types processed by the actor.
     */
    public Set<MessageType> getProcessedMessageTypes() {
        return new HashSet<>( this.processedMessageTypes );
    }
    
    /**
     * This method returns a map that contains the type and quantity of each
     * message type sent by the actor.
     * 
     * @return  A map that contains the type and quantity of each message type
     *          sent by this actor.
     */
    public Map<MessageType, Integer> sentMessagesStatistic() {
        return new HashMap<>( this.sentMessagesStatistic );
    }

    /**
     * This method returns a map that contains the type and quantity of each
     * message type received by the actor.
     * 
     * @return  A map that contains the type and quantity of each message type
     *          received by this actor.
     */
    public Map<MessageType, Integer> receivedMessagesStatistic() {
        return new HashMap<>( this.receivedMessagesStatistic );
    }

    /**
     * This method returns a map that contains the type and accumulated
     * processing for each message type processed by the actor.
     * 
     * @return  A map that contains the type and accumulated processing time
     *          for each message type processed by this actor.
     */
    public Map<MessageType, Long> processedMessagesStatistic() {
        return new HashMap<>( this.processedMessagesStatistic );
    }

    /**
     * This method returns a map that contains the message types and number of
     * sent messages for each receipient.
     * 
     * @return A map that contais the message types and number of sent messages
     *         for each receiptient.
     */
    public Map<ActorInstance, Map<MessageType, Integer>> getReceiverStatistics() {
        return new HashMap<>( this.receiverStatistics );
    }

    /**
     * This method returns all message processed events.
     * 
     * @return A list that contains all message processed events.
     */
    public List<MessageProcessingEvent> getMessagesProcessed() {
        return new ArrayList<>( messagesProcessed );
    }
    
    /**
     * This method returns all message sent event.
     * 
     * @return  A list that contains all message sent events.
     */
    public List<MessageSentEvent> getMessagesSent() {
        return new ArrayList<>( this.messagesSent );
    }
    
    public double getAverageTimeInMailbox() {
        return this.averageTimeInMailbox;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
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
        final ActorInstance other = (ActorInstance) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return this.created == other.created;
    }

    @Override
    public String toString() {
        return "ActorInstance{" + "type=" + type + ", id=" + id + ", created=" + created + '}';
    }
    
    // -------------------------------------------------------------------------
    // Package internal API.
    // -------------------------------------------------------------------------
    
    /**
     * This constructor initializes a new instance of Actor Instance.
     * 
     * @param type The type of the actor. Must not be null.
     * @param id The id of the actor instance. Must not be null or empty.
     * @param created The timestamp where the actor has been created.
     * @param supervisor The supervisor instance of this actor instance. May be null.
     */
     ActorInstance( final ActorType type, final String id, final long created, final ActorInstance supervisor ) {
        if( type == null ) throw new IllegalArgumentException( "The parameter 'type' must not be null!" );
        if( id == null ) throw new IllegalArgumentException( "The parameter 'id' must not be null!" );
        if( id.isEmpty() ) throw new IllegalArgumentException( "The parameter 'id' must not be an empty string!" );
        
        this.type = type;
        this.id = id;
        final String[] segments = id.split( "/" );
        this.shortId = segments[ segments.length -1 ];
        
        this.created = created;
        
        this.supervisor = supervisor;
        
        this.messagesSent = new ArrayList<>();
        this.messagesProcessed = new ArrayList<>();
        this.overallProcessingTime = 0;
        this.processedMessageTypes = new HashSet<>();
        
        this.sentMessagesStatistic = new HashMap<>();
        this.receivedMessagesStatistic = new HashMap<>();
        this.processedMessagesStatistic = new HashMap<>();
        this.receiverStatistics = new HashMap<>();
    }
     
     

    /**
     * This method registers a message sending event to this actor instance.
     * 
     * @param messageSentEvent The message sent event. Must not be null.
     */
    void registerMessageSentEvent( final MessageSentEvent messageSentEvent ) {
        if( messageSentEvent == null ) throw new IllegalArgumentException( "The parameter 'messageSentEvent' must not be null!" );
        this.messagesSent.add( messageSentEvent );
        
        final MessageType messageType = messageSentEvent.messageInstance.type;
        
        this.type.messageSent( messageType );
        this.registerCommunication( messageSentEvent.receiver, messageType );
        if( this.sentMessagesStatistic.containsKey( messageType ) ) {
            this.sentMessagesStatistic.put(messageType, this.sentMessagesStatistic.get( messageType ) + 1 );
        } else {
            this.sentMessagesStatistic.put( messageType, 1 );
        }
    }

    /**
     * This method registers a message processing event.
     * 
     * @param messageProcessingEvent A message processing event. Must not be null.
     */
    void registerMessageProcessingEvent( final MessageProcessingEvent messageProcessingEvent ) {
        if( messageProcessingEvent == null ) throw new IllegalArgumentException( "The parameter 'messageProcessingEvent' must not be null!" );
        final long processingTime = messageProcessingEvent.end - messageProcessingEvent.start;
        this.messagesProcessed.add( messageProcessingEvent );
        this.overallProcessingTime += processingTime;
        this.type.messageProcessed( messageProcessingEvent );
        
        final MessageType messageType = messageProcessingEvent.messageInstance.type;
        
        this.processedMessageTypes.add( messageType );
        
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
    }
    
    private void registerCommunication( final ActorInstance receiver, MessageType messageType ) {
        this.type.registerCommunication( receiver.type, messageType );
        
        if( this.receiverStatistics.containsKey( receiver ) ) {
            final Map< MessageType, Integer > d = this.receiverStatistics.get( receiver );
            if( d.containsKey( messageType ) ) {
                d.put( messageType, d.get( messageType ) + 1 );
            } else {
                d.put( messageType, 1 );
            }
        } else {
            final Map< MessageType, Integer > d = new HashMap<>();
            d.put( messageType, 1 );
            this.receiverStatistics.put( receiver, d );
        }
    }
    
}
