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

package simx.profiler.info.actor;

import java.util.ArrayList;
import java.util.Map;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import simx.profiler.model.MessageType;

/**
 * This table model is the controller to present messages received of an actor
 * in a swing table.
 * 
 * @author Stephan Rehfeld
 */
public class MessagesReceivedTableModel implements TableModel {
    
    /**
     * The list of received messages that is presented in the table.
     */
    private final ArrayList< Map.Entry< MessageType, Integer > > receivedMessages;
    
    /**
     * The total number of messages received by the actor.
     */
    private final int messagesReceived;
   
    /**
     * This constructor creates a new instance of the table model. Is needs a list
     * of received messages and the total number of received messages. The list contains
     * types of messages and how often a message of this time has been received.
     * 
     * @param receivedMessages A list that contains types of messages and of often they have been received.
     * @param messagesReceived The total number of received messages.
     */
    MessagesReceivedTableModel( final Map< MessageType, Integer > receivedMessages, final int messagesReceived ) {
        if( receivedMessages == null ) throw new IllegalArgumentException( "The parameter 'receivedMessages' must not be null!" );
        this.receivedMessages = new ArrayList<>( receivedMessages.entrySet() );
        this.messagesReceived = messagesReceived;
        
    }

    @Override
    public int getRowCount() {
        return this.receivedMessages.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Type";
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
    public Object getValueAt( final int rowIndex, final int columnIndex) {
        final Map.Entry< MessageType, Integer > e = this.receivedMessages.get( rowIndex );
        if( columnIndex == 0 ) return e.getKey().shortType;
        if( columnIndex == 1 ) return e.getValue();
        if( columnIndex == 2 ) return (double)e.getValue()*100.0/(double)this.messagesReceived;
        return null;
    }

    @Override
    public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex) {
        
    }

    @Override
    public void addTableModelListener( final TableModelListener l ) {
        
    }

    @Override
    public void removeTableModelListener( final TableModelListener l ) {
        
    }
}
