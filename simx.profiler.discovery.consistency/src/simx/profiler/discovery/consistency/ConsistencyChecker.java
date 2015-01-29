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

package simx.profiler.discovery.consistency;

import java.util.ArrayList;
import java.util.List;
import simx.profiler.model.MessageProcessingEvent;
import simx.profiler.model.MessageSentEvent;

/**
 *
 * @author Stephan Rehfeld
 */
public class ConsistencyChecker {
    
    public static void checkConsistency( final List< ConsistencyCheckData > data, final ConsistencyCheckProgressListener progressListener ) {
        int actorsCounter = 0;
        
        for( final ConsistencyCheckData d : data ) {
            if( Thread.currentThread().isInterrupted() ) return;
            ++actorsCounter;  
            progressListener.actorsCheckedProgressed( actorsCounter * 100 / data.size() );
            
            if( d.getSourceSimulationLoopMessage() == null || d.getTransferMessage() == null || d.getTargetSimulationLoopMessage() == null ) continue;
            
            int inconsistentCounter = 0;
            progressListener.communicationOfActorCheckedProgrss( 0 );
            final List< MessageProcessingEvent > sourceSimulationLoopProcessingEvents = new ArrayList<>();
            final List< MessageSentEvent > transferMessageSentEvents = new ArrayList<>();
            final List< MessageProcessingEvent > targetSimulationLoopProcessingEvents = new ArrayList<>();
            
            d.from.getMessagesProcessed().stream().filter((mpe) -> ( mpe.messageInstance.type.equals( d.getSourceSimulationLoopMessage() ) )).forEach((mpe) -> {
                sourceSimulationLoopProcessingEvents.add( mpe );
            });
            
            d.from.getMessagesSent().stream().filter((mse) -> ( mse.messageInstance.type.equals( d.getTransferMessage() ) && mse.receiver.equals( d.to ) )).forEach((mse) -> {
                transferMessageSentEvents.add( mse );
            });
            
            d.to.getMessagesProcessed().stream().filter((mpe) -> ( mpe.messageInstance.type.equals( d.getTargetSimulationLoopMessage() ) )).forEach((mpe) -> {
                targetSimulationLoopProcessingEvents.add( mpe );
            });
            int loopCounter = 0;
            
            for( final MessageProcessingEvent mpe : sourceSimulationLoopProcessingEvents ) {
                ++loopCounter;
                
                long start = Long.MAX_VALUE;
                long end = Long.MIN_VALUE;
                
                for( final MessageSentEvent mse : transferMessageSentEvents ) {
                    if( mse.timestamp >= mpe.start && mse.timestamp <= mpe.end ) {
                        if( mse.messageInstance.getMessageProcessingEvents().size() != 1 ) {
                            System.out.println( "Not supported!" );
                        } else {
                            final MessageProcessingEvent processed = mse.messageInstance.getMessageProcessingEvents().get( 0 );
                            if( processed.start < start ) start = processed.start;
                            if( processed.end > end ) end = processed.end;                          
                        }
                    }
                }
                
                for( final MessageProcessingEvent targetSimLoopPE : targetSimulationLoopProcessingEvents ) {
                    if( targetSimLoopPE.start > start && targetSimLoopPE.start < end ) {
                        ++inconsistentCounter;
                        break;
                    }
                }
                
                progressListener.communicationOfActorCheckedProgrss( loopCounter * 100 / sourceSimulationLoopProcessingEvents.size() );                
                d.setConsistency( "" + ((double)(sourceSimulationLoopProcessingEvents.size() - inconsistentCounter ) * 100.0 / (double)sourceSimulationLoopProcessingEvents.size() ));
            }
            
            
        }
    }
    
}
