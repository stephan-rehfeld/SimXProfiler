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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Stephan Rehfeld
 */
public class ProfilingData {
    
    static ProfilingData profilingData;
    
    public static ProfilingData getLoadedProfilingData() {
        return ProfilingData.profilingData;
    }
    
    private final Map< String, ActorType > actorTypes;
    private final Map< String, MessageType > messageTypes;
    private final Map< String, ActorInstance > idToInstance;
    
    private long firstEventTimestamp;
    private long lastEventTimestamp;
    private int processedMessagesCount;
    
    private final ActorInstance unknownActorInstance;
    
    private final List< ParallelismEvent > parallelismEvents;
    private long overallProcessingTime;
    private int messagesSentCount;
    private int messagesProcessedCount;
    
    private int averageTimeInMailboxCounter;
    private double averageTimeInMailbox;
    
    public ProfilingData() {
        this.actorTypes = new HashMap<>();
        this.messageTypes = new HashMap<>();
        this.idToInstance = new HashMap<>();
        
        this.firstEventTimestamp = Long.MAX_VALUE;
        this.lastEventTimestamp = Long.MIN_VALUE;
        
        this.processedMessagesCount = 0;
        
        ActorType unknownActorType = this.getOrRegisterActorType( "Unknown Type" );
        this.unknownActorInstance = unknownActorType.registerInstance( "Unknown Instance" );
        this.parallelismEvents = new ArrayList<>();
    }
    
    final ActorType getOrRegisterActorType( final String typeName ) {
        ActorType actorType;
        if( this.actorTypes.containsKey( typeName ) ) {
            actorType = this.actorTypes.get( typeName );
        } else {
            actorType = new ActorType( this, typeName );
            this.actorTypes.put( typeName, actorType );
        }
        return actorType;
    }

    public void registerSending( final long sendTime, final String senderString, final String receiverString, final String type, final int messageID ) {
        
        if( sendTime < this.firstEventTimestamp ) this.firstEventTimestamp = sendTime;
        if( sendTime > this.lastEventTimestamp ) this.lastEventTimestamp = sendTime;
        this.messagesSentCount++;
        final MessageType messageType = this.getOrRegisterMessageType( type );
        final MessageInstance messageInstance = messageType.getOrRegisterInstance( messageID );
        
        final ActorInstance sender = this.getActorInstance( senderString );
        final ActorInstance receiver = this.getActorInstance( receiverString );
        
        messageInstance.registerSentEvent( sender, receiver, sendTime );
        
    }
    
    public MessageType getOrRegisterMessageType( final String type ) {
       MessageType messageType;
        if( this.messageTypes.containsKey( type ) ) {
            messageType = this.messageTypes.get( type );
        } else {
            messageType = new MessageType( type, this );
            this.messageTypes.put( type, messageType );
        }
        return messageType;
    }
    
    ActorInstance getActorInstance( final String id ) {
        if( this.idToInstance.containsKey( id ) ) {
            return this.idToInstance.get( id );
        }
        return this.unknownActorInstance;
    }
    
    void registerInstance( final String id, final ActorInstance actorInstance ) {
        this.idToInstance.put( id, actorInstance );
    }

    public long applicationRunTime() {
        return this.lastEventTimestamp - this.firstEventTimestamp;
    }
    
    public long applicationStart() {
        return this.firstEventTimestamp;
    }
    
    public List< ActorInstance > getActorInstances() {
        final List< ActorInstance > actorInstances = new ArrayList<>();
        this.getActorTypes().stream().forEach((actorType) -> {
            actorInstances.addAll( actorType.getInstances() );
        });
        return actorInstances;
    }
    
    public void registerProcessingEvent( final long start, final long end, final String messageTypeString, final int messageID, final String senderString, final String receiverString) {
        if( start < this.firstEventTimestamp ) this.firstEventTimestamp = start;
        if( start > this.lastEventTimestamp ) this.lastEventTimestamp = start;
        
        if( end < this.firstEventTimestamp ) this.firstEventTimestamp = end;
        if( end > this.lastEventTimestamp ) this.lastEventTimestamp = end;
        
        this.overallProcessingTime += end - start;
        this.messagesProcessedCount++;
        this.parallelismEvents.add( new ParallelismEvent( start, ParallelismEvent.ParallelimEventTypes.PROCESSING_START ) );
        this.parallelismEvents.add( new ParallelismEvent( end, ParallelismEvent.ParallelimEventTypes.PROCESSING_END ) );
              
        final MessageType messageType = this.getOrRegisterMessageType( messageTypeString );
        final MessageInstance messageInstance = messageType.getOrRegisterInstance( messageID );
        
        final ActorInstance sender = this.getActorInstance( senderString );
        final ActorInstance receiver = this.getActorInstance( receiverString );
        
        final MessageProcessingEvent messageProcessingEvent = new MessageProcessingEvent( sender, receiver, start, end, messageInstance );
        
        messageInstance.registerProcessingEvent( messageProcessingEvent );
        receiver.registerMessageProcessingEvent( messageProcessingEvent );   
        
        if( messageInstance.getMessageSentEvents().size() == 1 ) {
            this.averageTimeInMailbox = (this.averageTimeInMailbox * this.averageTimeInMailboxCounter / (this.averageTimeInMailbox+1) ) + ((messageInstance.getMessageSentEvents().get( 0 ).timestamp - start) / (this.averageTimeInMailbox+1) );
            this.averageTimeInMailboxCounter++;
        }
        this.processedMessagesCount++;
    }
    
    public List<ActorType> getActorTypes() {
        return new ArrayList<>(this.actorTypes.values());
    }

    void registerActorCreationTimeStamp( final long created ) {
        if( created < this.firstEventTimestamp ) this.firstEventTimestamp = created;
        if( created > this.lastEventTimestamp ) this.lastEventTimestamp = created;
    }
    
    public List< MessageType > getMessageTypes() {
        return new ArrayList<>( this.messageTypes.values() );
    }
    
    public int getProcessedMessagesCount() {
        return this.processedMessagesCount;
    }

    ActorInstance getUnknownActorInstance() {
        return this.unknownActorInstance;
    }

    public ActorInstance registerActorInstance( final String typeName, final String id, final long creationTime, final String supervisor ) {
        final ActorType type = this.getOrRegisterActorType( typeName );
        return type.registerInstance( id, creationTime, supervisor );
    }
    
    public List< ParallelismEvent > getParallelismEvents() {
        return this.parallelismEvents;
    };

    public long getOverallProcessingTime() {
        return this.overallProcessingTime;
    }

    public int getMessagesSentCount() {
        return this.messagesSentCount;
    }

    public int getMessagesProcessedCount() {
        return this.messagesProcessedCount;
    }
   
    public double getAverageTimeInMailbox() {
        return this.averageTimeInMailbox;
    }
}
