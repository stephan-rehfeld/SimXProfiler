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

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * An interface for the mode loader to implement.
 * 
 * @author Stephan Rehfeld
 */
public interface IModelLoader {
    
    /**
     * An implementation of this method should load the profiling data from the given path and return a ProfilingData object.
     * 
     * @param path The file to load.
     * @return The loaded profile. An implementation should never return null.
     * @throws IOException
     * @throws DataFormatException 
     */
    public ProfilingData load( final File path ) throws IOException, DataFormatException;
}
