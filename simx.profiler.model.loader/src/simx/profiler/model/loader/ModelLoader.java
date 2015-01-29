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

package simx.profiler.model.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.DataFormatException;
import org.openide.util.lookup.ServiceProvider;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.IModelLoader;
import simx.profiler.model.ProfilingData;

/**
 *
 * @author Stephan Rehfeld
 */
@ServiceProvider(service=IModelLoader.class)
public class ModelLoader implements IModelLoader {

    @Override
    public ProfilingData load( final File path ) throws IOException, DataFormatException {
        if( !path.exists() ) throw new FileNotFoundException( "Directory does not exist.");
        if( !path.isDirectory() ) throw new IllegalArgumentException( "The parameter 'path' needs to point to a directory." );
        
        final File actorCreationDataFile = new File( path.getAbsolutePath() + File.separator + "creationdata.csv" );
        if( !actorCreationDataFile.exists() ) throw new FileNotFoundException( "Actor creation data file is not in the directory" );
        
        final File sendDataFile = new File( path.getAbsolutePath() + File.separator + "senddata.csv" );
        if( !sendDataFile.exists() ) throw new FileNotFoundException( "Actor send data file is not in the directory" );
        
        final File messageProcessingDataFile = new File( path.getAbsolutePath() + File.separator + "messageprocessingdata.csv" );
        if( !messageProcessingDataFile.exists() ) throw new FileNotFoundException( "Actor message processing file is not in the directory" );
        
        long earliestAction = Long.MAX_VALUE;
        long latestAction = Long.MIN_VALUE;
        
        final ProfilingData profilingData = new ProfilingData();
        
        try( final BufferedReader actorCreationBufferedReader = new BufferedReader( new FileReader( actorCreationDataFile ) ) ) {
            String line = actorCreationBufferedReader.readLine();
            
            while( (line = actorCreationBufferedReader.readLine()) != null ) {
               final String[] elements = line.split( ";" );
               if( elements.length != 4 ) throw new DataFormatException( "The actor creation data file must contain lines with three elements." );
               
               final long creationTime = Long.parseLong( elements[0] );
               final String id = elements[1];
               final String typeName = elements[2];
               final String supervisor = elements[3];
               
               if( creationTime < earliestAction ) earliestAction = creationTime;
               if( creationTime > latestAction ) latestAction = creationTime;
               
               final ActorInstance actorInstance = profilingData.registerActorInstance( typeName, id, creationTime, supervisor );
            }
        }
        
        try( final BufferedReader sendDataBufferedReader = new BufferedReader( new FileReader( sendDataFile ) ) ) {
            
            String line = sendDataBufferedReader.readLine();
            int lineCount = 1;
            while( (line = sendDataBufferedReader.readLine() ) != null ) {
                ++lineCount;
                final String[] elements = line.split( ";" );
                if( elements.length != 5 ) throw new DataFormatException( "The actor creation data file must contain lines with five elements. In line " + lineCount + ". " + line );
                
                final long sendTime = Long.parseLong( elements[0] );
                final String senderString = elements[1];
                final String receiverString = elements[2];
                final String messageType = elements[3];
                final int messageID = Integer.parseInt( elements[4] );
                
                profilingData.registerSending( sendTime, senderString, receiverString, messageType, messageID );
            }
            
        }
        
        try( final BufferedReader messageProcessingDataBufferedReader = new BufferedReader( new FileReader( messageProcessingDataFile ) ) ) {
            
            String line = messageProcessingDataBufferedReader.readLine();
            
            while( (line = messageProcessingDataBufferedReader.readLine() ) != null ) {
                final String[] elements = line.split( ";" );
                if( elements.length != 6 ) throw new DataFormatException( "The actor creation data file must contain lines with six elements." );
                
                final long start = Long.parseLong( elements[0] );
                final long end = Long.parseLong( elements[1] );
                final String messageTypeString = elements[2];
                final int messageID = Integer.parseInt( elements[3] );
                final String senderString = elements[4];
                final String receiverString = elements[5];
                
                profilingData.registerProcessingEvent( start, end, messageTypeString, messageID, senderString, receiverString );
                
            }
            
        }
        
        return profilingData;
    }
    
}
