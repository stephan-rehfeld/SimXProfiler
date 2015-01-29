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

import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.ProfilingData;

/**
 *
 * @author Stephan Rehfeld
 */
public class ActorInstanceInformationTableModel implements TableModel {
    
    private final ProfilingData profilingData;
    private final List< ActorInstance > actorInstances;
    
    ActorInstanceInformationTableModel( final ProfilingData profilingData ) {
        if( profilingData == null ) throw new IllegalArgumentException( "The parameter 'profilingData' must not be null!" );
        this.profilingData = profilingData;
        this.actorInstances = profilingData.getActorInstances();
    }
    
    @Override
    public int getRowCount() {
        return this.actorInstances.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Instance ID";
        if( columnIndex == 1 ) return "Type";
        if( columnIndex == 2 ) return "Messages Sent";
        if( columnIndex == 3 ) return "Messages Received";
        if( columnIndex == 4 ) return "Execution time (ms)";
        if( columnIndex == 5 ) return "Execution time (%)";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return String.class;
        if( columnIndex == 2 ) return Integer.class;
        if( columnIndex == 3 ) return Integer.class;
        if( columnIndex == 4 ) return Long.class;
        if( columnIndex == 5 ) return Double.class;
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return false;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {
        final ActorInstance actorInstance = this.actorInstances.get( rowIndex );
        
        if( columnIndex == 0 ) return actorInstance.shortId;
        if( columnIndex == 1 ) return actorInstance.type.shortTypeName;
        if( columnIndex == 2 ) return actorInstance.getSentMessagesCount();
        if( columnIndex == 3 ) return actorInstance.getReceivesMessagesCount();
        if( columnIndex == 4 ) return (double)actorInstance.getOverallProcessingTime()/1000000.0;
        if( columnIndex == 5 ) return (double)actorInstance.getOverallProcessingTime()*100.0/(double)this.profilingData.applicationRunTime();
        return "";
    }

    @Override
    public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex) {
        
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        
    }
    
}
