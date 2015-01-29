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

import simx.profiler.model.ActorInstance;
import simx.profiler.model.MessageType;

/**
 *
 * @author Stephan Rehfeld
 */
public class ConsistencyCheckData {
 
    public final ActorInstance from;
    private MessageType sourceSimulationLoopMessage;
    private MessageType transferMessage;
    public final ActorInstance to;
    private MessageType targetSimulationLoopMessage;
    private String consistency;
    
    public ConsistencyCheckData( final ActorInstance from, ActorInstance to ) {
        this.from = from;
        this.to = to;
        this.consistency = "?";
    }

    public MessageType getSourceSimulationLoopMessage() {
        return sourceSimulationLoopMessage;
    }

    public void setSourceSimulationLoopMessage( final MessageType sourceSimulationLoopMessage ) {
        this.sourceSimulationLoopMessage = sourceSimulationLoopMessage;
    }

    public MessageType getTransferMessage() {
        return transferMessage;
    }

    public void setTransferMessage( final MessageType transferMessage ) {
        this.transferMessage = transferMessage;
    }

    public MessageType getTargetSimulationLoopMessage() {
        return targetSimulationLoopMessage;
    }

    public void setTargetSimulationLoopMessage( final MessageType targetSimulationLoopMessage ) {
        this.targetSimulationLoopMessage = targetSimulationLoopMessage;
    }

    public String getConsistency() {
        return consistency;
    }

    public void setConsistency( final String consistency ) {
        this.consistency = consistency;
    }
      
}
