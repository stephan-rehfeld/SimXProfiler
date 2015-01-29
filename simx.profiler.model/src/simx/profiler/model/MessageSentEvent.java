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

import java.util.Objects;

/**
 * An object of this class represents a event, when a message was sent.
 * 
 * @author Stephan Rehfeld
 */
public class MessageSentEvent {
  
    /**
     * A reference to the sender of the message.
     */
    public final ActorInstance sender;
    
    /**
     * A reference to the receiver of the message.
     */
    public final ActorInstance receiver;
    
    /**
     * The time stamp when the message was sent.
     */
    public final long timestamp;
    
    /**
     * A reference to the instance of the message.
     */
    public final MessageInstance messageInstance;
    
    /**
     * This constructor creates a new  message sent event.
     * 
     * @param sender The sender of the message.
     * @param receiver The receiver of the message.
     * @param timestamp The time stamp when the message was sent.
     * @param messageInstance The instance of the message.
     */
    public MessageSentEvent( final ActorInstance sender, final ActorInstance receiver, final long timestamp, final MessageInstance messageInstance ) {
        if( sender == null ) throw new IllegalArgumentException( "The parameter 'sender' must not be null!" );
        if( receiver == null ) throw new IllegalArgumentException( "The parameter 'receiver' must not be null!" );
        
        if( messageInstance == null ) throw new IllegalArgumentException( "The parameter 'messageInstance' must not be null!" );
        
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.messageInstance = messageInstance;       
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.sender);
        hash = 17 * hash + Objects.hashCode(this.receiver);
        hash = 17 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        hash = 17 * hash + Objects.hashCode(this.messageInstance);
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
        final MessageSentEvent other = (MessageSentEvent) obj;
        if (!Objects.equals(this.sender, other.sender)) {
            return false;
        }
        if (!Objects.equals(this.receiver, other.receiver)) {
            return false;
        }
        if (this.timestamp != other.timestamp) {
            return false;
        }
        return Objects.equals(this.messageInstance, other.messageInstance);
    }

    @Override
    public String toString() {
        return "MessageSentEvent{" + "sender=" + sender + ", receiver=" + receiver + ", timestamp=" + timestamp + ", messageInstance=" + messageInstance + '}';
    }
  
}
