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
import java.util.List;
import simx.profiler.model.MessageProcessingEvent;
import simx.profiler.model.MessageSentEvent;

/**
 *
 * @author Stephan Rehfeld
 */
public class LatencyCalculator {
    
    public static LatencyReport calculateLatency( final List<LatencyDiscoveryNode> nodes, final List< Communication > edges, final LatencyDiscoveryProgressListener listener ) {
         
        ProcessingTimespan start;
        ProcessingTimespan current;
        // ---------------- CREATE DATA Structure
        // Create initial timespan
        if( nodes.isEmpty() ) return null;
        final LatencyDiscoveryNode startNode = nodes.get( 0 );
        if( startNode.getSimulationLoopMessageType() != null ) {
            // Simulation Loop to Mailbox Timespan
            start = new ProcessingTimespan( startNode.actorInstance, startNode.getSimulationLoopMessageType(), ProcessingTimespan.TimespanType.BEGIN_OF_SIMULATION_LOOP_TO_MESSAGE_IN_MAILBOX );
            // Message In Mailbox Timespan
            final LatencyDiscoveryNode secondNode = nodes.get( 1 );
            current = new ProcessingTimespan( secondNode.actorInstance, edges.get( 0 ).getMessageType(), ProcessingTimespan.TimespanType.MESSAGE_WAITS_IN_MAIL_BOX );
            start.setNext( current );
        } else {
            // Message In Mailbox Timespan
            final LatencyDiscoveryNode secondNode = nodes.get( 1 );
            current = new ProcessingTimespan( secondNode.actorInstance, edges.get( 0 ).getMessageType(), ProcessingTimespan.TimespanType.MESSAGE_WAITS_IN_MAIL_BOX );
            start = current;
        }
        
        for( int i = 1; i < nodes.size(); ++i ) {
            final LatencyDiscoveryNode node = nodes.get( i );
            ProcessingTimespan buffer;
            if( i != nodes.size() - 1 ) {
               final LatencyDiscoveryNode nextNode = nodes.get( i+1 );
               if( nodes.get( i ).getSimulationLoopMessageType() != null ) {
                   
                    // Message Processed to Simulation Loop Timespan
                    buffer = new ProcessingTimespan( node.actorInstance, node.getSimulationLoopMessageType(), ProcessingTimespan.TimespanType.BEGIN_OF_MESSAGE_PROCESSING_TO_BEGIN_OF_SIMULATION_LOOP ); 
                    current.setNext( buffer );
                    current = buffer;
                    
                    // Simulation Loop to Mailbox Timespan
                    buffer = new ProcessingTimespan( node.actorInstance, node.getSimulationLoopMessageType(), ProcessingTimespan.TimespanType.BEGIN_OF_SIMULATION_LOOP_TO_MESSAGE_IN_MAILBOX );
                    current.setNext( buffer );
                    current = buffer;
                    
                    // Message In Mailbox Timespan
                    buffer = new ProcessingTimespan( nextNode.actorInstance, edges.get( i ).getMessageType(), ProcessingTimespan.TimespanType.MESSAGE_WAITS_IN_MAIL_BOX );
                    current.setNext( buffer );
                    current = buffer;
                } else {
                    // Message Processed to Mailbox Time span
                    buffer = new ProcessingTimespan( node.actorInstance, edges.get( i ).getMessageType(), ProcessingTimespan.TimespanType.BEGIN_OF_MESSAGE_PROCESSING_TO_MESSAGE_IN_MAILBOX );
                    current.setNext( buffer );
                    current = buffer;
                    
                    // Message In Mailbox Timespan
                    buffer = new ProcessingTimespan( nextNode.actorInstance, edges.get( i ).getMessageType(), ProcessingTimespan.TimespanType.MESSAGE_WAITS_IN_MAIL_BOX );
                    current.setNext( buffer );
                    current = buffer;
                }
            } else {
                
                 if( node.getSimulationLoopMessageType() != null ) {
                     // Message Processed to Simulation Loop Timespan
                     buffer = new ProcessingTimespan( node.actorInstance, node.getSimulationLoopMessageType(), ProcessingTimespan.TimespanType.BEGIN_OF_MESSAGE_PROCESSING_TO_BEGIN_OF_SIMULATION_LOOP ); 
                     current.setNext( buffer );
                     current = buffer;
                     
                     // Final Simulation Loop Timespan
                     buffer = new ProcessingTimespan( node.actorInstance, node.getSimulationLoopMessageType(), ProcessingTimespan.TimespanType.FINAL_SIMULATION_LOOP );
                     current.setNext( buffer );
                     current = buffer;
                } else {
                     // Final Message Processed Time Span
                     buffer = new ProcessingTimespan( node.actorInstance, edges.get( i -1 ).getMessageType(), ProcessingTimespan.TimespanType.FINAL_MESSAGE_PROCESSED );
                     current.setNext( buffer );
                     current = buffer;
                }
                
            }
          
        }

        System.out.println( "Created data structure" );
        
        // ---------------- CALCULATE LATENCY
        
        final List< ImmutableTuple< Long, Long > > data = new ArrayList<>();
        final int messageEvents = start.getActorInstance().getMessagesProcessed().size();
        int counter = 0;
        if( start.getTimespanType() == ProcessingTimespan.TimespanType.BEGIN_OF_SIMULATION_LOOP_TO_MESSAGE_IN_MAILBOX ) {
            // Find all Simulation Loop messages
            
            for( final MessageProcessingEvent mpe : start.getActorInstance().getMessagesProcessed() ) {
                ++counter;
                listener.latencyDiscoveryProgress( (counter * 100) / messageEvents );
                if( mpe.messageInstance.type.equals( start.getMessageType() ) ) {
                    if( Thread.currentThread().isInterrupted() ) return null;
                    for( final MessageSentEvent mse : start.getActorInstance().getMessagesSent() ) {
                        // Find that next's message has been sent
                        if( mse.messageInstance.type.equals( start.getNext().getMessageType() ) && mse.timestamp >= mpe.start  && mse.receiver.equals( start.getNext().getActorInstance() ) ) {
                            final long timespan = mse.timestamp - mpe.start; 
                            start.registerTimespan( timespan );
                            
                            final long end = calculateLatency( start.getNext(), mse.timestamp );
                            if( end != -1 )
                                data.add( new ImmutableTuple<>( mpe.start, end - mpe.start ) );
                            break;
                        }                     
                    }
                    
                }
            }
        } else if( start.getTimespanType() == ProcessingTimespan.TimespanType.MESSAGE_WAITS_IN_MAIL_BOX ) {
            for( final MessageProcessingEvent mpe : start.getActorInstance().getMessagesProcessed() ) {
                ++counter;
                listener.latencyDiscoveryProgress( (counter * 100) / messageEvents );
                // TODO Wrong, also check if the message has been sent by the right actor
                if( mpe.messageInstance.type.equals( start.getMessageType() ) ) {
                    if( mpe.messageInstance.getMessageSentEvents().size() == 1 ) {
                        if( Thread.currentThread().isInterrupted() ) return null;
                        final long timespan = mpe.start - mpe.messageInstance.getMessageSentEvents().get( 0 ).timestamp; 
                        final long end = calculateLatency( start.getNext(), mpe.start );
                        data.add( new ImmutableTuple( mpe.messageInstance.getMessageSentEvents().get( 0 ).timestamp, end - mpe.messageInstance.getMessageSentEvents().get( 0 ).timestamp ) );
                        System.out.println( "Single send" );
                    } else {
                        // TODO: SEARCH
                        //throw new IllegalStateException( "This should not happen" );
                        System.out.println( "Double send" );
                    }
                    
                    // Find when the message has been sent
                    // Give next message into recursion
                }
            }

        } else {
            // Wrong start

            throw new IllegalStateException();
        }
        return new LatencyReport( data, start );
    }
    
    private static long calculateLatency( final ProcessingTimespan current, final long start ) {
        if( current.getTimespanType() == ProcessingTimespan.TimespanType.BEGIN_OF_MESSAGE_PROCESSING_TO_BEGIN_OF_SIMULATION_LOOP ) {
            for( final MessageProcessingEvent mpe : current.getActorInstance().getMessagesProcessed() ) {
                if( mpe.messageInstance.type.equals( current.getMessageType() ) && mpe.start >= start ) {
                    final long timespan = mpe.start - start;
                    current.registerTimespan( timespan );
                    return calculateLatency( current.getNext(), mpe.start );
                }
            }
            return -1;
        } else if( current.getTimespanType() == ProcessingTimespan.TimespanType.BEGIN_OF_MESSAGE_PROCESSING_TO_MESSAGE_IN_MAILBOX ) {
            for( final MessageSentEvent mse : current.getActorInstance().getMessagesSent() ) {
                if( mse.messageInstance.type.equals( current.getMessageType() ) && mse.timestamp >= start ) {
                    final long timespan = mse.timestamp - start;
                    current.registerTimespan( timespan );
                    return calculateLatency( current.getNext(), mse.timestamp );
                }
            }
            return -1;
        } else if( current.getTimespanType() == ProcessingTimespan.TimespanType.BEGIN_OF_SIMULATION_LOOP_TO_MESSAGE_IN_MAILBOX ) {
            for( final MessageProcessingEvent mpe : current.getActorInstance().getMessagesProcessed() ) {
                if( mpe.messageInstance.type.equals( current.getMessageType() ) && mpe.start >= start ) {   
                    for( final MessageSentEvent mse : current.getActorInstance().getMessagesSent() ) {
                        // Find that next's message has been sent
                        if( mse.messageInstance.type.equals( current.getNext().getMessageType() ) && mse.timestamp >= mpe.start ) {
                            final long timespan = mse.timestamp - mpe.start; 
                            current.registerTimespan( timespan );
                            return calculateLatency( current.getNext(), mse.timestamp );
                        }                   
                    }    
                }
            }
            return -1;
        } else if( current.getTimespanType() == ProcessingTimespan.TimespanType.MESSAGE_WAITS_IN_MAIL_BOX ) {
            for( final MessageProcessingEvent mpe : current.getActorInstance().getMessagesProcessed() ) {
                if( mpe.messageInstance.type.equals( current.getMessageType() ) && mpe.start >= start ) {   
                    final long timespan = mpe.start - start;
                    current.registerTimespan( timespan );
                    return calculateLatency( current.getNext(), mpe.start );
                }
            }
            return -1;
        } else if( current.getTimespanType() == ProcessingTimespan.TimespanType.FINAL_MESSAGE_PROCESSED ) {
            for( final MessageProcessingEvent mpe : current.getActorInstance().getMessagesProcessed() ) {
                if( mpe.messageInstance.type.equals( current.getMessageType() ) && mpe.start >= start ) {   
                    final long timespan = mpe.end - start;
                    current.registerTimespan( timespan );
                    return mpe.end;
                }
            }
            return -1;
        } else if( current.getTimespanType() == ProcessingTimespan.TimespanType.FINAL_SIMULATION_LOOP ) {
            for( final MessageProcessingEvent mpe : current.getActorInstance().getMessagesProcessed() ) {
                if( mpe.messageInstance.type.equals( current.getMessageType() ) && mpe.start >= start ) {   
                    final long timespan = mpe.end- start;
                    current.registerTimespan( timespan );
                    return mpe.end;
                }
            }
            return -1;
        }
        throw new IllegalStateException( "This should not happen!" );
    }
    
}
