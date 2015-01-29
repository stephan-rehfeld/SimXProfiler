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
import simx.profiler.model.ActorInstance;

/**
 *
 * @author Stephan Rehfeld
 */
public class MessageSentByInstanceTableModel implements TableModel {
     
    private final List< Map.Entry< ActorInstance, Integer > > sentStatistic;
    private final int timesSent;
    
    MessageSentByInstanceTableModel( final List< Map.Entry< ActorInstance, Integer > > sentStatistic, final int timesSent ) {
        if( sentStatistic == null ) throw new IllegalArgumentException( "The parameter 'sentStatistic' must not be null!" );
        this.sentStatistic = sentStatistic;
        this.timesSent = timesSent;
    }
    
    @Override
    public int getRowCount() {
        return this.sentStatistic.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Instance";
        if( columnIndex == 1 ) return "Number";
        if( columnIndex == 2 ) return "%";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return Integer.class;
        if( columnIndex == 2 ) return Double.class;
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt( final int rowIndex,  final int columnIndex ) {
        final Map.Entry< ActorInstance, Integer > e = this.sentStatistic.get( rowIndex );
        if( columnIndex == 0 ) return e.getKey().shortId;
        if( columnIndex == 1 ) return e.getValue();
        if( columnIndex == 2 ) return (double)e.getValue()*100.0/(double)this.timesSent;
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
