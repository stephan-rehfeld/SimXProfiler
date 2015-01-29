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
import simx.profiler.model.ActorType;
import simx.profiler.model.ProfilingData;

/**
 *
 * @author Stephan Rehfeld
 */
public class ActorTypeInformationTableModel implements TableModel {

    private final ProfilingData profilingData;
    private final List< ActorType > actorTypes;
    
    ActorTypeInformationTableModel( final ProfilingData profilingData ) {
        if( profilingData == null ) throw new IllegalArgumentException( "The parameter 'profilingData' must not be null!" );
        this.profilingData = profilingData;
        this.actorTypes = profilingData.getActorTypes();
    }
    
    
    @Override
    public int getRowCount() {
        return this.actorTypes.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Type";
        if( columnIndex == 1 ) return "Instances";
        if( columnIndex == 2 ) return "Sent Messages";
        if( columnIndex == 3 ) return "Received Messages";
        if( columnIndex == 4 ) return "Execution time (ms)";
        if( columnIndex == 5 ) return "Execution time (%)";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return Integer.class;
        if( columnIndex == 2 ) return Integer.class;
        if( columnIndex == 3 ) return Integer.class;
        if( columnIndex == 4 ) return Long.class;
        if( columnIndex == 5 ) return Double.class;
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {
        final ActorType actorType = this.actorTypes.get( rowIndex );
        if( columnIndex == 0 ) return actorType.shortTypeName;
        if( columnIndex == 1 ) return actorType.getInstancesCount();
        if( columnIndex == 2 ) return actorType.getSentMessagesCount();
        if( columnIndex == 3 ) return actorType.getReceivedMessagesCount();
        if( columnIndex == 4 ) return (double)actorType.getOverallProcessingTime()/1000000.0;
        if( columnIndex == 5 ) return (double)actorType.getOverallProcessingTime()*100.0/(double)this.profilingData.applicationRunTime();
        return null;
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
