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
 * A message processing event. It references the sender, receiver and message instance.
 * It also contains the timestamp when the processing began and ended.
 * 
 * @author Stephan Rehfeld
 */
public class MessageProcessingEvent {
  
    /**
     * The sender of the message.
     */
    public final ActorInstance sender;
    
    /**
     * The receiver of the message. This is the actor where the processing happened.
     */
    public final ActorInstance receiver;
    
    /**
     * The timestamp of the begin.
     */
    public final long start;
    
    /**
     * The timestamp when the processing finished.
     */
    public final long end;
    
    /**
     * The processed message instance.
     */
    public final MessageInstance messageInstance;
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.sender);
        hash = 41 * hash + Objects.hashCode(this.receiver);
        hash = 41 * hash + (int) (this.start ^ (this.start >>> 32));
        hash = 41 * hash + (int) (this.end ^ (this.end >>> 32));
        hash = 41 * hash + Objects.hashCode(this.messageInstance);
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
        final MessageProcessingEvent other = (MessageProcessingEvent) obj;
        if (!Objects.equals(this.sender, other.sender)) {
            return false;
        }
        if (!Objects.equals(this.receiver, other.receiver)) {
            return false;
        }
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return Objects.equals(this.messageInstance, other.messageInstance);
    }

    @Override
    public String toString() {
        return "MessageProcessingEvent{" + "sender=" + sender + ", receiver=" + receiver + ", start=" + start + ", end=" + end + ", messageInstance=" + messageInstance + '}';
    }
    
    // -------------------------------------------------------------------------
    // Package internal API
    // -------------------------------------------------------------------------
    
    /**
     * This constructor creates a new message processing event.
     * 
     * @param sender The sender.
     * @param receiver The receiver.
     * @param start The timestamp when the processing started.
     * @param end The timestamp when the processing ended.
     * @param messageInstance The instance of the message.
     */
    MessageProcessingEvent( final ActorInstance sender, final ActorInstance receiver, final long start, final long end, final MessageInstance messageInstance ) {
        if( sender == null ) throw new IllegalArgumentException( "The parameter 'sender' must not be null!" );
        if( receiver == null ) throw new IllegalArgumentException( "The parameter 'receiver' must not be null!" );
        if( messageInstance == null ) throw new IllegalArgumentException( "The parameter 'messageInstance' must not be null!" );
        
        this.sender = sender;
        this.receiver = receiver;
        this.start = start;
        this.end = end;
        this.messageInstance = messageInstance;
    }
     
}
