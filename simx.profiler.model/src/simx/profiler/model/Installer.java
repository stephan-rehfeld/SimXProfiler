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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import javax.swing.JFileChooser;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * The module installer. It loads the model on installation of the module.
 * 
 * @author Stephan Rehfeld
 */
public class Installer extends ModuleInstall {
    
    @Override
    public void restored() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        
        File targetDirectory;
        
        do {
            final int returnVal = fc.showOpenDialog( null );
            if( returnVal == JFileChooser.CANCEL_OPTION ) System.exit( 0 );
            targetDirectory = fc.getSelectedFile();
            if( !targetDirectory.exists() || !targetDirectory.isDirectory() ) {
                targetDirectory = null;
                continue;
            }
            final File[] files = targetDirectory.listFiles();
            final List< String > fileNames = new ArrayList<>();
            for( final File file : files ) fileNames.add( file.getName() );
            if( !fileNames.contains( "creationdata.csv" ) || !fileNames.contains( "messageprocessingdata.csv" ) || !fileNames.contains( "senddata.csv" ) )
                targetDirectory = null;
        } while( targetDirectory == null );
        
        final IModelLoader loader = Lookup.getDefault().lookup( IModelLoader.class );
        if( loader == null ) System.exit( 0 );
        try {
            final ProfilingData profilingData = loader.load( targetDirectory );
            ProfilingData.profilingData = profilingData;            
        } catch( final IOException | DataFormatException ex ) {
            Exceptions.printStackTrace(ex);
            System.exit( 0 );
        }
    }

}
