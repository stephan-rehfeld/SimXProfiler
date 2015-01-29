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

package simx.profiler.discovery.consistency;

import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Stephan Rehfeld
 */
public class ConsistencyDiscoveryTableModel implements TableModel {

    public final List< ConsistencyCheckData > data;

    ConsistencyDiscoveryTableModel( final List<ConsistencyCheckData> data ) {
        this.data = data;
    }
    
    @Override
    public int getRowCount() {
        return this.data.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "From";
        if( columnIndex == 1 ) return "Simulation Loop Message";
        if( columnIndex == 2 ) return "Transfer";
        if( columnIndex == 3 ) return "To";
        if( columnIndex == 4 ) return "Simulation Loop Message";
        if( columnIndex == 5 ) return "Consistent Input Data";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return String.class;
        if( columnIndex == 2 ) return String.class;
        if( columnIndex == 3 ) return String.class;
        if( columnIndex == 4 ) return String.class;
        if( columnIndex == 5 ) return String.class;
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return columnIndex == 1 || columnIndex == 2 || columnIndex == 4;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {
        final ConsistencyCheckData d = this.data.get( rowIndex );
        if( columnIndex == 0 ) {
            return d.from.shortId + " (" + d.from.type.shortTypeName + ")";
        }
        if( columnIndex == 1 ) {
            if( d.getSourceSimulationLoopMessage() == null ) return "";
            return d.getSourceSimulationLoopMessage().shortType;
        }
        
        if( columnIndex == 2 ) {
            if( d.getTransferMessage() == null ) return "";
            return d.getTransferMessage().shortType;
        }
        
        if( columnIndex == 3 ) {
            return d.to.shortId + " (" + d.to.type.shortTypeName + ")";
        }
        if( columnIndex == 4 ) {
            if( d.getTargetSimulationLoopMessage() == null ) return "";
            return d.getTargetSimulationLoopMessage().shortType;
        }
        if( columnIndex == 5 ) return d.getConsistency();
        return "";
    }

    @Override
    public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex ) {
        final ConsistencyCheckData d = this.data.get( rowIndex );
        final MessageTypeComboBoxItem item = (MessageTypeComboBoxItem)aValue;
        if( columnIndex == 1 ) {
            d.setSourceSimulationLoopMessage( item.messageType );
        } else if( columnIndex == 2 ) {
            d.setTransferMessage( item.messageType );
        } else if( columnIndex == 4 ) {
            d.setTargetSimulationLoopMessage( item.messageType );
        }
        
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        
    }
    
}
