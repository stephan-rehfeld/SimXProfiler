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

import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import simx.profiler.model.ActorInstance;

/**
 *  This table model is the controller between a list of actor instances and
 *  a swing table.
 * 
 * @author Stephan Rehfeld
 */
public class ActorInstancesTableModel implements TableModel {

    /**
     * The list of instances that is shown in the table.
     */
    private final List< ActorInstance > instances;
    
    /**
     * The time stamp of the profiled application's start.
     */
    private final long applicationStart;
    
    /**
     * This constructor creates a new instance of the table model.
     * 
     * @param instances A list of instances that should be in the table.
     * @param applicationStart The time stamp when the profiled application started.
     */
    ActorInstancesTableModel( final List< ActorInstance > instances, final long applicationStart ) {
        if( instances == null ) throw new IllegalArgumentException( "The parameter 'instances' must not be null!" );
        this.instances = instances;
        this.applicationStart = applicationStart;
    }
    
    @Override
    public int getRowCount() {
        return this.instances.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName( final int columnIndex ) {
        if( columnIndex == 0 ) return "Instance";
        if( columnIndex == 1 ) return "Created";
        return "";
    }

    @Override
    public Class<?> getColumnClass( final int columnIndex ) {
        if( columnIndex == 0 ) return String.class;
        if( columnIndex == 1 ) return Long.class;
        return null;
        
    }

    @Override
    public boolean isCellEditable( final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt( final int rowIndex, final int columnIndex) {
        final ActorInstance actorInstance = this.instances.get( rowIndex );
        if( columnIndex == 0 ) return actorInstance.shortId;
        if( columnIndex == 1 ) return actorInstance.created - this.applicationStart;
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
