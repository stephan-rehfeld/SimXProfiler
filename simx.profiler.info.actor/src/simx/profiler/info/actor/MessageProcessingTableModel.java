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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import simx.profiler.model.MessageType;

/**
 * This table model is the controller between a list of processed messages
 * and a swing table.
 * 
 * @author Stephan Rehfeld
 */
public class MessageProcessingTableModel implements TableModel {
    
    /**
     * The list of processes messages that is presented in the table.
     */
    private final List< Map.Entry< MessageType, Long > > processedMessages;
    
    /**
     * A set that contains observers of the model.
     */
    private final Set< TableModelListener > tableModelListener;
    
    /**
     * The total processing time of all messages that the list processedMessages
     * contains.
     */
    private final long totalProcessingTime;
    
    /**
     * The baseline for calculation of the relative processing time.
     * TODO: Change to an enum.
     */
    private String baseline;
    
    /**
     * The overall application runtime.
     */
    private final long applicationRuntime;
    
    /**
     * Creates a new table model to presents processed messages of an actor.
     * 
     * @param processedMessages The list of processed messages. Must not be null.
     * @param baseline The baseline that is used for calculation. "Application" or something differen.
     * @param applicationRuntime The overall application runtime.
     */
    MessageProcessingTableModel( final Map< MessageType, Long > processedMessages, final String baseline, final long applicationRuntime  ) {
        if( processedMessages == null ) throw new IllegalArgumentException( "The parameter 'processedMessages' must not be null!" );
        this.processedMessages = new ArrayList<>( processedMessages.entrySet() );
        this.tableModelListener = new HashSet<>();
        long totalProcessingTimeCalc = 0;
        totalProcessingTimeCalc = this.processedMessages.stream().map((e) -> e.getValue()).reduce(totalProcessingTimeCalc, (accumulator, _item) -> accumulator + _item);
        this.totalProcessingTime = totalProcessingTimeCalc;
        this.baseline = baseline;
        this.applicationRuntime = applicationRuntime;
    }
    
    @Override
    public int getRowCount() {
        return this.processedMessages.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Type";
        if( columnIndex == 1 ) return "Processing Time (ms)";
        if( columnIndex == 2 ) return "Processing Time (%)";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return Long.class;
        if( columnIndex == 2 ) return Double.class;
        return null;
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex) {
        final Map.Entry< MessageType, Long > entry  = this.processedMessages.get( rowIndex );
        if( columnIndex == 0 ) return entry.getKey().shortType;
        if( columnIndex == 1 ) return (double)entry.getValue()/1000000.0;
        if( columnIndex == 2 ) return (this.baseline.equals( "Application" )?(double)entry.getValue()*100.0/(double)this.applicationRuntime:(double)entry.getValue()*100.0/(double)this.totalProcessingTime);
        return null;
    }

    @Override
    public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex) {
        
    }

    @Override
    public void addTableModelListener( final TableModelListener l ) {
        this.tableModelListener.add( l );
    }

    @Override
    public void removeTableModelListener( final TableModelListener l ) {
        this.tableModelListener.remove( l );
    }

    /**
     * Sets a new baseline and signals the presenting table to update.
     * 
     * @param baseline The baseline. Either "Application" or something different. 
     */
    public void setBaseline( final String baseline ) {
        this.baseline = baseline;
        
        this.tableModelListener.stream().forEach((l) -> {
            l.tableChanged( new TableModelEvent( this ) );
        });
    }
    
    
    
}
