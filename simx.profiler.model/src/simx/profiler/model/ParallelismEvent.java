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

/**
 *
 * @author Stephan Rehfeld
 */
public class ParallelismEvent implements Comparable< ParallelismEvent > {
  
    public enum ParallelimEventTypes {
        PROCESSING_START,
        PROCESSING_END
    }
    
    public final long timestamp;
    public final ParallelimEventTypes eventType;
    
    ParallelismEvent( final long timestamp, final ParallelimEventTypes eventType ) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }
       
    @Override
    public int compareTo( ParallelismEvent o ) {
        return ((Long)this.timestamp).compareTo( o.timestamp );
    }
}
