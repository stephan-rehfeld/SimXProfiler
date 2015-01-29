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

package simx.profiler.discovery.latency;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Stephan Rehfeld
 */
public class LatencyReportTableModel implements TableModel {

    private final ProcessingTimespan start;
    
    LatencyReportTableModel( final ProcessingTimespan start ) {
        this.start = start;
    }
    
    
    @Override
    public int getRowCount() {
        return start.length();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) {
            return "Time Span";
        } else if( columnIndex == 1 ) {
            return "Actor Instance";
        } else if( columnIndex == 2 ) {
            return "Message Type";
        } else if( columnIndex == 3 ) {
            return "Min";
        } else if( columnIndex == 4 ) {
            return "Max";
        } else if( columnIndex == 5 ) {
            return "Avg.";
        } else if( columnIndex == 6 ) {
            return "Med.";
        } 
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) {
            return String.class;
        } else if( columnIndex == 1 ) {
            return String.class;
        } else if( columnIndex == 2 ) {
            return String.class;
        } else if( columnIndex == 3 ) {
            return Double.class;
        } else if( columnIndex == 4 ) {
            return Double.class;
        } else if( columnIndex == 5 ) {
            return Double.class;
        } else if( columnIndex == 6 ) {
            return Double.class;
        } 
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return true;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {
        final ProcessingTimespan c = start.get( rowIndex );
        if( columnIndex == 0 ) {
            return c.getTimespanType();
        } else if( columnIndex == 1 ) {
            return c.getActorInstance().shortId + " (" + c.getActorInstance().type.shortTypeName + ")";
        } else if( columnIndex == 2 ) {
            return c.getMessageType().shortType;
        } else if( columnIndex == 3 ) {
            return (double)c.getMin()/1000000.0;
        } else if( columnIndex == 4 ) {
            return (double)c.getMax()/1000000.0;
        } else if( columnIndex == 5 ) {
            return (double)c.getAvg()/1000000.0;
        } else if( columnIndex == 6 ) {
            return (double)c.getMed()/1000000.0;
        } 
        return null;
    }

    @Override
    public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex ) {}

    @Override
    public void addTableModelListener( final TableModelListener l ) {}

    @Override
    public void removeTableModelListener( final TableModelListener l ) {}
    
}
