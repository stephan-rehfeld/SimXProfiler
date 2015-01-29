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
import java.util.List;
import java.util.Objects;

/**
 * An object of this class represents an instance of a message. It references
 * its type, and events when it was sent and processed.
 * 
 * @author Stephan Rehfeld
 */
public class MessageInstance {

    /**
     * The type of the message.
     */
    public final MessageType type;
    
    /**
     * The id of the message.
     */
    public final int messageID;
    
    /**
     * A list of events, when this message was sent.
     */
    private final List< MessageSentEvent > messageSentEvents;
    
    /**
     * A list of events when this message was processed.
     */
    private final List< MessageProcessingEvent > messageProcessingEvents;
    
    /**
     * Return a list that contains all message processing events.
     * 
     * @return A list that contains all message processing events.
     */
    public List<MessageProcessingEvent> getMessageProcessingEvents() {
        return new ArrayList<>( messageProcessingEvents );
    }
    
    /**
     * Returns a list that contains all message sent event.
     * 
     * @return  A list that contains all message processing events.
     */
    public List<MessageSentEvent> getMessageSentEvents() {
        return new ArrayList<>( this.messageSentEvents );
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final MessageInstance other = (MessageInstance) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return this.messageID == other.messageID;
    }

    @Override
    public String toString() {
        return "MessageInstance{" + "type=" + type + ", messageID=" + messageID + '}';
    } 
    
    // -------------------------------------------------------------------------
    // Package internal API
    // -------------------------------------------------------------------------
    
    /**
     * This constructor creates a new message instance.
     * 
     * @param messageType The type of the message. Must not be null.
     * @param messageID The id of the message.
     */
    MessageInstance( final MessageType type, final int messageID ) {
        if( type == null ) throw new IllegalArgumentException( "The parameter 'type' must not be null!" );
        this.type = type;
        this.messageID = messageID;
        this.messageSentEvents = new ArrayList<>();
        this.messageProcessingEvents = new ArrayList<>();
    }

    /**
     * This method registers a sent event to this instance and to the sender of the message.
     * 
     * @param sender The sender of the message. Must not be null.
     * @param receiver The receiver of the message. Must not be null.
     * @param sendTime The timestamp.
     */
    void registerSentEvent( final ActorInstance sender, final ActorInstance receiver, long sendTime ) {
        if( sender == null ) throw new IllegalArgumentException( "The parameter 'sender' must not be null!" );
        if( receiver == null ) throw new IllegalArgumentException( "The parameter 'receiver' must not be null!" );
        final MessageSentEvent messageSentEvent = new MessageSentEvent( sender, receiver, sendTime, this );
        this.messageSentEvents.add( messageSentEvent );
        sender.registerMessageSentEvent( messageSentEvent );
        this.type.sent( sender );
    }

    /**
     * This method registers a messave processing event to this instance.
     * 
     * @param messageProcessingEvent A processing processing event of this instance. Must not be null!
     */
    void registerProcessingEvent( final MessageProcessingEvent messageProcessingEvent ) {
        if( messageProcessingEvent == null ) throw new IllegalArgumentException( "The parameter 'messageProcessingEvent' must not be null!" );
        this.messageProcessingEvents.add( messageProcessingEvent );
        this.type.processed( messageProcessingEvent.receiver, messageProcessingEvent.end - messageProcessingEvent.start );
    }
    
}
