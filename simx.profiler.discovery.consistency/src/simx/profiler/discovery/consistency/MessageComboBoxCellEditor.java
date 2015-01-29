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


import java.awt.Component;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import simx.profiler.model.MessageType;


/**
 *
 * @author Stephan Rehfeld
 */
public class MessageComboBoxCellEditor extends DefaultCellEditor {
    
    private final DefaultComboBoxModel model;
    private final List< ConsistencyCheckData > data;
    
    MessageComboBoxCellEditor( final List< ConsistencyCheckData > data ) {
        super( new JComboBox() );
        this.model = (DefaultComboBoxModel)((JComboBox)getComponent()).getModel();
        this.data = data;
    } 

    @Override
    public Component getTableCellEditorComponent( final JTable table, final Object value, final boolean isSelected, final int row, final int column ) {
        
        this.model.removeAllElements();
        this.model.addElement( new MessageTypeComboBoxItem() );
        
        final ConsistencyCheckData d = this.data.get( row );
        if( column == 1 ) {
            d.from.getProcessedMessageTypes().stream().forEach((mt) -> {
                this.model.addElement( new MessageTypeComboBoxItem( mt ) );
            });
        }
        
        if( column == 2 ) {
            d.from.getReceiverStatistics().get( d.to ).entrySet().stream().forEach((e) -> {
                this.model.addElement( new MessageTypeComboBoxItem( e.getKey() ) );
            });
        }
        
        if( column == 4 ) {
            d.to.getProcessedMessageTypes().stream().forEach((mt) -> {
                this.model.addElement( new MessageTypeComboBoxItem( mt ) );
            });
        }
        
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    
    
    
    
}
