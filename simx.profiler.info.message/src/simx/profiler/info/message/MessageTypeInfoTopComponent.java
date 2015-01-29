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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.ActorType;
import simx.profiler.model.ImmutableTupel;
import simx.profiler.model.MessageType;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.info.message//MessageTypeInfo//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MessageTypeInfoTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "simx.profiler.info.message.MessageTypeInfoTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MessageTypeInfoAction",
        preferredID = "MessageTypeInfoTopComponent"
)
@Messages({
    "CTL_MessageTypeInfoAction=MessageTypeInfo",
    "CTL_MessageTypeInfoTopComponent=MessageTypeInfo Window",
    "HINT_MessageTypeInfoTopComponent=This is a MessageTypeInfo window"
})
public final class MessageTypeInfoTopComponent extends TopComponent implements LookupListener {

    private Lookup.Result< MessageType > result = null;
    
    private final InstanceContent content;
    
    private List<Map.Entry<ActorType, Integer>> sentByTypes;
    private List<Map.Entry<ActorInstance, Integer>> sentByInstances;
    private List<Map.Entry<ActorType, ImmutableTupel<Integer, Double>>> receivedByTypes;
    private List<Map.Entry<ActorInstance, ImmutableTupel<Integer, Double>>> receivedByInstances;
    
    public MessageTypeInfoTopComponent() {
        initComponents();
        setName(Bundle.CTL_MessageTypeInfoTopComponent());
        setToolTipText(Bundle.HINT_MessageTypeInfoTopComponent());

        this.content = new InstanceContent();
        
        this.associateLookup( new AbstractLookup( this.content ) );
        
        ListSelectionModel listSelectionModel = this.sentByTypeTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            if( sentByTypeTable.getSelectedRow() != -1 ) {
                sentByInstancesTable.clearSelection();
                messageReceivedByTypeTable.clearSelection();
                messageReceivedByInstanceTable.clearSelection();
                final ActorType actorType = sentByTypes.get( sentByTypeTable.getSelectedRow() ).getKey();
                content.set( Collections.singleton( actorType ), null );
            }
        });
        
        listSelectionModel = this.sentByInstancesTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            if( sentByInstancesTable.getSelectedRow() != -1 ) {
                sentByTypeTable.clearSelection();
                messageReceivedByTypeTable.clearSelection();
                messageReceivedByInstanceTable.clearSelection();
                final ActorInstance actorInstance = sentByInstances.get( sentByInstancesTable.getSelectedRow() ).getKey();
                final ActorType actorType = actorInstance.type;
                final Set<Object> selected = new HashSet<>();
                Collections.addAll( selected, actorType, actorInstance );
                content.set( selected, null );
            }
        });
        
        listSelectionModel = this.messageReceivedByTypeTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            if( messageReceivedByTypeTable.getSelectedRow() != -1 ) {
                sentByInstancesTable.clearSelection();
                sentByTypeTable.clearSelection();
                messageReceivedByInstanceTable.clearSelection();
                final ActorType actorType = receivedByTypes.get( messageReceivedByTypeTable.getSelectedRow() ).getKey();
                content.set( Collections.singleton( actorType ), null );
            }
        });
        
        listSelectionModel = this.messageReceivedByInstanceTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            if( messageReceivedByInstanceTable.getSelectedRow() != -1 ) {
                sentByInstancesTable.clearSelection();
                messageReceivedByTypeTable.clearSelection();
                sentByTypeTable.clearSelection();
                final ActorInstance actorInstance = receivedByInstances.get( messageReceivedByInstanceTable.getSelectedRow() ).getKey();
                final ActorType actorType = actorInstance.type;
                final Set<Object> selected = new HashSet<>();
                Collections.addAll( selected, actorType, actorInstance );
                content.set( selected, null );
            }
        });
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        timesSentTextField = new javax.swing.JTextField();
        timesProcessedTextField = new javax.swing.JTextField();
        avergaProcessingTimeTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        sentByTypeTable = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        sentByInstancesTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        messageReceivedByTypeTable = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        messageReceivedByInstanceTable = new javax.swing.JTable();

        setMinimumSize(new java.awt.Dimension(300, 0));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jLabel4.text")); // NOI18N

        nameTextField.setEditable(false);
        nameTextField.setText(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.nameTextField.text")); // NOI18N

        timesSentTextField.setEditable(false);
        timesSentTextField.setText(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.timesSentTextField.text")); // NOI18N

        timesProcessedTextField.setEditable(false);
        timesProcessedTextField.setText(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.timesProcessedTextField.text")); // NOI18N

        avergaProcessingTimeTextField.setEditable(false);
        avergaProcessingTimeTextField.setText(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.avergaProcessingTimeTextField.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timesProcessedTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(avergaProcessingTimeTextField)
                    .addComponent(timesSentTextField)
                    .addComponent(nameTextField)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(timesSentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(timesProcessedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(avergaProcessingTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel3.border.title"))); // NOI18N

        sentByTypeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Type", "Number", "%"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(sentByTypeTable);
        if (sentByTypeTable.getColumnModel().getColumnCount() > 0) {
            sentByTypeTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.sentByTypeTable.columnModel.title0")); // NOI18N
            sentByTypeTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.sentByTypeTable.columnModel.title1")); // NOI18N
            sentByTypeTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.sentByTypeTable.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        sentByInstancesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Type", "Number", "%"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(sentByInstancesTable);
        if (sentByInstancesTable.getColumnModel().getColumnCount() > 0) {
            sentByInstancesTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.sentByInstancesTable.columnModel.title0")); // NOI18N
            sentByInstancesTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.sentByInstancesTable.columnModel.title1")); // NOI18N
            sentByInstancesTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.sentByInstancesTable.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel4.border.title"))); // NOI18N

        messageReceivedByTypeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Type", "Number", "%", "Avg. Processing Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(messageReceivedByTypeTable);
        if (messageReceivedByTypeTable.getColumnModel().getColumnCount() > 0) {
            messageReceivedByTypeTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByTypeTable.columnModel.title0")); // NOI18N
            messageReceivedByTypeTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByTypeTable.columnModel.title1")); // NOI18N
            messageReceivedByTypeTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByTypeTable.columnModel.title2")); // NOI18N
            messageReceivedByTypeTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByTypeTable.columnModel.title3")); // NOI18N
        }

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        messageReceivedByInstanceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Type", "Number", "%", "Avg. Processing Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(messageReceivedByInstanceTable);
        if (messageReceivedByInstanceTable.getColumnModel().getColumnCount() > 0) {
            messageReceivedByInstanceTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByInstanceTable.columnModel.title0")); // NOI18N
            messageReceivedByInstanceTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByInstanceTable.columnModel.title1")); // NOI18N
            messageReceivedByInstanceTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByInstanceTable.columnModel.title2")); // NOI18N
            messageReceivedByInstanceTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.messageReceivedByInstanceTable.columnModel.title3")); // NOI18N
        }

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(MessageTypeInfoTopComponent.class, "MessageTypeInfoTopComponent.jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField avergaProcessingTimeTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable messageReceivedByInstanceTable;
    private javax.swing.JTable messageReceivedByTypeTable;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTable sentByInstancesTable;
    private javax.swing.JTable sentByTypeTable;
    private javax.swing.JTextField timesProcessedTextField;
    private javax.swing.JTextField timesSentTextField;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        this.result = Utilities.actionsGlobalContext().lookupResult( MessageType.class );
        this.result.addLookupListener( this );
        this.result.allInstances();
    }

    @Override
    public void componentClosed() {
        this.result.removeLookupListener( this );
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends MessageType> allSelectedTypes = result.allInstances();
        if( !allSelectedTypes.isEmpty() ) {
            allSelectedTypes.stream().forEach((type) -> {
                this.setData( type );
            });
        } else {
            System.out.println( "No selection" );
        }
    }

    private void setData( final MessageType type ) {
        
        this.nameTextField.setText( type.longType );
        this.timesSentTextField.setText( "" + type.getTimesSent() );
        this.timesProcessedTextField.setText( "" + type.getTimesProcessed() );
        this.avergaProcessingTimeTextField.setText( "" + ((double)type.getAverageProcessingTime()/1000000.0) + " ms" );
        
        this.sentByTypes = new ArrayList<>( type.getSentByTypeStatistic().entrySet() );
        this.sentByTypeTable.setModel( new MessageSentByTypeTableModel( this.sentByTypes, type.getTimesSent() ) );
        this.sentByInstances = new ArrayList<>( type.getSentByInstanceStatistic().entrySet() );
        this.sentByInstancesTable.setModel( new MessageSentByInstanceTableModel( this.sentByInstances, type.getTimesSent() ) );
        
        this.receivedByTypes = new ArrayList<>( type.getReceivedByTypeStatistic().entrySet() );
        this.messageReceivedByTypeTable.setModel( new MessageReceivedByTypeTableModel( this.receivedByTypes, type.getTimesProcessed() ) );
        this.receivedByInstances = new ArrayList<>( type.getReceivedByInstanceStatistic().entrySet() );
        this.messageReceivedByInstanceTable.setModel( new MessageReceivedByInstanceTableModel( this.receivedByInstances, type.getTimesProcessed() ) );
    }
}
