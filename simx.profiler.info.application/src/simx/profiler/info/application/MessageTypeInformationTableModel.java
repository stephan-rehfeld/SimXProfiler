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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import simx.profiler.model.MessageType;

/**
 *
 * @author Stephan Rehfeld
 */
public class MessageTypeInformationTableModel implements TableModel {

    private final CommunicationData communicationData;
    private final List< Map.Entry< MessageType, Integer > > data;
    
    MessageTypeInformationTableModel( final CommunicationData communicationData ) {
        if( communicationData == null ) throw new IllegalArgumentException( "The parameter 'communicationData' must not be null!" );
        this.communicationData = communicationData;
        this.data = new ArrayList<>( communicationData.getCommunicationData().entrySet() );
    }
    
    @Override
    public int getRowCount() {
        return this.data.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Type";
        if( columnIndex == 1 ) return "Amount";
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
    public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
        return true;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex ) {
        final Map.Entry< MessageType, Integer > d = this.data.get( rowIndex );
        if( columnIndex == 0 ) return d.getKey().shortType;
        if( columnIndex == 1 ) return d.getValue();
        if( columnIndex == 2 ) return (double)d.getValue()*100.0/(double)this.communicationData.totalMessagesCount();
        return null;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        
    }
    
}
