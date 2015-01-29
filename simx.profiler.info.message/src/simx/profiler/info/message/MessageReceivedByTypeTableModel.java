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

package simx.profiler.info.message;

import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import simx.profiler.model.ActorType;
import simx.profiler.model.ImmutableTupel;

/**
 *
 * @author Stephan Rehfeld
 */
public class MessageReceivedByTypeTableModel implements TableModel {

    private final List< Map.Entry< ActorType, ImmutableTupel< Integer, Double > > > receivedStatistic;
    private final int timesReceived;
    
    MessageReceivedByTypeTableModel( final List< Map.Entry< ActorType, ImmutableTupel< Integer, Double > > > receivedStatistic, final int timesReceived ) {
        if( receivedStatistic == null ) throw new IllegalArgumentException( "The parameter 'receivedStatistic' must not be null!" );
        this.receivedStatistic = receivedStatistic;
        this.timesReceived = timesReceived;
    }
    
    @Override
    public int getRowCount() {
        return this.receivedStatistic.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Type";
        if( columnIndex == 1 ) return "Number";
        if( columnIndex == 2 ) return "%";
        if( columnIndex == 3 ) return "Avg. Processing Time";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return Integer.class;
        if( columnIndex == 2 ) return Double.class;
        if( columnIndex == 3 ) return Double.class;
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt( final int rowIndex,  final int columnIndex ) {
        final Map.Entry< ActorType, ImmutableTupel< Integer, Double > > e = this.receivedStatistic.get( rowIndex );
        if( columnIndex == 0 ) return e.getKey().shortTypeName;
        if( columnIndex == 1 ) return e.getValue().a;
        if( columnIndex == 2 ) return (double)e.getValue().a*100.0/(double)this.timesReceived;
        if( columnIndex == 3 ) return (double)e.getValue().b/1000000.0;
        return null;
    }

    @Override
    public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex ) {
        
    }

    @Override
    public void addTableModelListener( final TableModelListener l ) {
       
    }

    @Override
    public void removeTableModelListener( final TableModelListener l ) {
        
    }
    
    
    
}
