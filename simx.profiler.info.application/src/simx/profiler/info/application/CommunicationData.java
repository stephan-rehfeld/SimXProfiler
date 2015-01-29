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

package simx.profiler.info.application;

import java.util.Map;
import simx.profiler.model.ImmutableTupel;
import simx.profiler.model.MessageType;

/**
 *
 * @author Stephan Rehfeld
 */
public class CommunicationData {
    
    private final Map< MessageType, Integer > communicationData;
    private final int totalMessageCount;
    public final ImmutableTupel<?, ?> key;

    CommunicationData( final ImmutableTupel<?, ?> key, final Map<MessageType, Integer> communicationData ) {
        this.communicationData = communicationData;
        int c = 0;
        c = this.communicationData.entrySet().stream().map((d) -> d.getValue()).reduce(c, Integer::sum);
        this.totalMessageCount = c;
        this.key = key;
    }

    public Map<MessageType, Integer> getCommunicationData() {
        return communicationData;
    }
    
    public int totalMessagesCount() {
        return this.totalMessageCount;
    }
   
}
