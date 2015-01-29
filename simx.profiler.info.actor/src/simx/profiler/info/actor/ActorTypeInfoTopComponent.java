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

import java.awt.BorderLayout;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
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
import simx.profiler.model.MessageType;
import simx.profiler.model.ProfilingData;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

/**
 * This top component displays informations about actor types. It contains
 * several tables and visualizations.
 * 
 * @author Stephan Rehfeld
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.info.actor//ActorTypeInfo//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ActorTypeInfoTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "simx.profiler.info.actor.ActorTypeInfoTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ActorTypeInfoAction",
        preferredID = "ActorTypeInfoTopComponent"
)
@Messages({
    "CTL_ActorTypeInfoAction=ActorTypeInfo",
    "CTL_ActorTypeInfoTopComponent=ActorTypeInfo Window",
    "HINT_ActorTypeInfoTopComponent=This is a ActorTypeInfo window"
})
public final class ActorTypeInfoTopComponent extends TopComponent implements LookupListener {

    /**
     * The reference to the lookup that is used to communicate with other
     * top components.
     */
    private Lookup.Result<ActorType> result = null;
    
    /**
     * The content object that is used to provide information to other
     * plug-ins.
     */
    private final InstanceContent content;
    
    /**
     * A list of instances of the presented type.
     */
    private List< ActorInstance > actorInstances;
    
     /**
     * A list of the sent messages currently presented on the top component.
     */
    private ArrayList< Map.Entry< MessageType, Integer > > sentMessages;
    
     /**
     * A list of the received messages currently presented on the top component.
     */
    private ArrayList< Map.Entry< MessageType, Integer > > receivedMessages;
    
     /**
     * A list of the processed messages currently presented on the top component.
     */
    private ArrayList< Map.Entry< MessageType, Long > > processedMessages;
    
    /**
     * The message data type curenntly selected in the UI.
     */
    private MessageType selectedMessageType;
    
    /**
     * The dataset for the visulaization of the sent message.
     */
    private final DefaultPieDataset messagesSentDataSet;
    
    /**
     * The dataset for the visulaization of the received message.
     */
    private final DefaultPieDataset messagesReceivedDataSet;
    
    /**
     * The dataset for the visulaization of the processed message.
     */
    private final DefaultPieDataset messagesProcessedDataSet;
    
    /**
     * This constructor initializes the top component. It configures the
     * tales creates the visulizations.
     */
    public ActorTypeInfoTopComponent() {
        initComponents();
        setName(Bundle.CTL_ActorTypeInfoTopComponent());
        setToolTipText(Bundle.HINT_ActorTypeInfoTopComponent());

        this.content = new InstanceContent();
        
        this.associateLookup( new AbstractLookup( this.content ) );
        
        ListSelectionModel listSelectionModel = this.instancesTable.getSelectionModel(); 
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            if( instancesTable.getSelectedRow() != -1 ) {
                final ActorInstance actorInstance = actorInstances.get( instancesTable.getSelectedRow() );
                final ActorType actorType = actorInstance.type;
                final Set<Object> selectedObjects = new HashSet<>();
                selectedObjects.add( actorType );
                selectedObjects.add( actorInstance );
                content.set( selectedObjects, null );
            }
        });
        
        listSelectionModel = this.messagesSentTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            if( messagesSentTable.getSelectedRow() != -1 ) {
                messagesReceivedTable.clearSelection();
                messagesProcessedTable.clearSelection();
                if( selectedMessageType != null ) content.remove( selectedMessageType );
                selectedMessageType = sentMessages.get( messagesSentTable.getSelectedRow() ).getKey();
                content.add( selectedMessageType );
            }     
        });
        
        listSelectionModel = this.messagesReceivedTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            if( messagesReceivedTable.getSelectedRow() != -1 ) {
                messagesSentTable.clearSelection();
                messagesProcessedTable.clearSelection();
                if( selectedMessageType != null ) content.remove( selectedMessageType );
                selectedMessageType = receivedMessages.get( messagesReceivedTable.getSelectedRow() ).getKey();
                content.add( selectedMessageType );
            }     
        });
        
        listSelectionModel = this.messagesProcessedTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            if( messagesProcessedTable.getSelectedRow() != -1 ) {
                messagesSentTable.clearSelection();
                messagesReceivedTable.clearSelection();
                if( selectedMessageType != null ) content.remove( selectedMessageType );
                selectedMessageType = processedMessages.get( messagesProcessedTable.getSelectedRow() ).getKey();
                content.add( selectedMessageType );
            }     
        });
        
        this.messagesSentDataSet = new DefaultPieDataset();
        this.messagesReceivedDataSet = new DefaultPieDataset();
        this.messagesProcessedDataSet = new DefaultPieDataset();
   
        
        this.createPieChart( this.messagesSentDataSet, this.messagesSentPanel );
        this.createPieChart( this.messagesReceivedDataSet, this.messagesReceivedPanel );
        this.createPieChart( this.messagesProcessedDataSet, this.messagesProcessedPanel );
    }
    
    /**
     * This method creates a pie chart and adds it to the target panel.
     * 
     * @param data The data set that should be visualized by the pie chart.
     * @param targetPanel The panel where the pie chart should be added to.
     */
    private void createPieChart( final DefaultPieDataset data, final javax.swing.JPanel targetPanel ) {
        if( data == null ) throw new IllegalArgumentException( "The parameter 'data' must not be 'null'!" );
        if( targetPanel == null ) throw new IllegalArgumentException( "The parameter 'targetPanel' must not be 'null'!" );
        
        data.setValue( "???", 100 );
        final JFreeChart chart = ChartFactory.createPieChart( "", data, false, false, false );
        final PiePlot plot = (PiePlot)chart.getPlot();
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        final ChartPanel chartPanel = new ChartPanel( chart );
        chartPanel.setPreferredSize( new java.awt.Dimension(261, 157) );
        targetPanel.setLayout( new BorderLayout() );
        targetPanel.add( chartPanel, BorderLayout.CENTER );   
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
        jLabel5 = new javax.swing.JLabel();
        actorTypeNameTextField = new javax.swing.JTextField();
        instancesTextField = new javax.swing.JTextField();
        messagesSentTextField = new javax.swing.JTextField();
        messagesReceivedTextField = new javax.swing.JTextField();
        processingTimeTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        timeInMailboxTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        instancesTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        messagesSentPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        messagesSentTable = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        messagesReceivedPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        messagesReceivedTable = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        messagesProcessedPanel = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        messagesProcessedTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        baselineComboBox = new javax.swing.JComboBox();

        setMinimumSize(new java.awt.Dimension(300, 100));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel5.text")); // NOI18N

        actorTypeNameTextField.setEditable(false);
        actorTypeNameTextField.setText(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.actorTypeNameTextField.text")); // NOI18N

        instancesTextField.setEditable(false);
        instancesTextField.setText(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.instancesTextField.text")); // NOI18N

        messagesSentTextField.setEditable(false);
        messagesSentTextField.setText(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesSentTextField.text")); // NOI18N

        messagesReceivedTextField.setEditable(false);
        messagesReceivedTextField.setText(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesReceivedTextField.text")); // NOI18N

        processingTimeTextField.setEditable(false);
        processingTimeTextField.setText(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.processingTimeTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel6.text")); // NOI18N

        timeInMailboxTextField.setEditable(false);
        timeInMailboxTextField.setText(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.timeInMailboxTextField.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(instancesTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagesSentTextField)
                    .addComponent(actorTypeNameTextField)
                    .addComponent(processingTimeTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagesReceivedTextField)
                    .addComponent(timeInMailboxTextField)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(actorTypeNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(instancesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(messagesSentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(messagesReceivedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(processingTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(timeInMailboxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel3.border.title_1"))); // NOI18N

        instancesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Instance", "Created"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(instancesTable);
        if (instancesTable.getColumnModel().getColumnCount() > 0) {
            instancesTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.instancesTable.columnModel.title0_1")); // NOI18N
            instancesTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.instancesTable.columnModel.title1_1")); // NOI18N
        }

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel5.border.title"))); // NOI18N

        javax.swing.GroupLayout messagesSentPanelLayout = new javax.swing.GroupLayout(messagesSentPanel);
        messagesSentPanel.setLayout(messagesSentPanelLayout);
        messagesSentPanelLayout.setHorizontalGroup(
            messagesSentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );
        messagesSentPanelLayout.setVerticalGroup(
            messagesSentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 158, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesSentPanel.TabConstraints.tabTitle"), messagesSentPanel); // NOI18N

        messagesSentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Number", "Amount (%)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Float.class
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
        jScrollPane3.setViewportView(messagesSentTable);
        if (messagesSentTable.getColumnModel().getColumnCount() > 0) {
            messagesSentTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesSentTable.columnModel.title0")); // NOI18N
            messagesSentTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesSentTable.columnModel.title1")); // NOI18N
            messagesSentTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesSentTable.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel7.border.title"))); // NOI18N

        javax.swing.GroupLayout messagesReceivedPanelLayout = new javax.swing.GroupLayout(messagesReceivedPanel);
        messagesReceivedPanel.setLayout(messagesReceivedPanelLayout);
        messagesReceivedPanelLayout.setHorizontalGroup(
            messagesReceivedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );
        messagesReceivedPanelLayout.setVerticalGroup(
            messagesReceivedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 158, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesReceivedPanel.TabConstraints.tabTitle"), messagesReceivedPanel); // NOI18N

        messagesReceivedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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
        jScrollPane4.setViewportView(messagesReceivedTable);
        if (messagesReceivedTable.getColumnModel().getColumnCount() > 0) {
            messagesReceivedTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jTable2.columnModel.title0")); // NOI18N
            messagesReceivedTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jTable2.columnModel.title1")); // NOI18N
            messagesReceivedTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jTable2.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel9.TabConstraints.tabTitle"), jPanel9); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel10.border.title"))); // NOI18N

        javax.swing.GroupLayout messagesProcessedPanelLayout = new javax.swing.GroupLayout(messagesProcessedPanel);
        messagesProcessedPanel.setLayout(messagesProcessedPanelLayout);
        messagesProcessedPanelLayout.setHorizontalGroup(
            messagesProcessedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );
        messagesProcessedPanelLayout.setVerticalGroup(
            messagesProcessedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.messagesProcessedPanel.TabConstraints.tabTitle"), messagesProcessedPanel); // NOI18N

        messagesProcessedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Processing Time (ms)", "ProcessingTime (%)"
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
        jScrollPane5.setViewportView(messagesProcessedTable);
        if (messagesProcessedTable.getColumnModel().getColumnCount() > 0) {
            messagesProcessedTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jTable2.columnModel.title0")); // NOI18N
            messagesProcessedTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jTable2.columnModel.title1")); // NOI18N
            messagesProcessedTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jTable2.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab(org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jPanel12.TabConstraints.tabTitle"), jPanel12); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ActorTypeInfoTopComponent.class, "ActorTypeInfoTopComponent.jLabel7.text")); // NOI18N

        baselineComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Type", "Application" }));
        baselineComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baselineComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(baselineComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(baselineComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane3))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    /**
     * This message is called by the combo box the select wheter the processing
     * time of processed message should be calculated relative the the run time
     * of the application or of the actor instance.
     * 
     * @param evt The event of combo box.
     */
    private void baselineComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baselineComboBoxActionPerformed
        if( this.messagesProcessedTable.getModel() instanceof MessageProcessingTableModel ) {
            ((MessageProcessingTableModel)this.messagesProcessedTable.getModel()).setBaseline( (String) this.baselineComboBox.getSelectedItem() );
        }
    }//GEN-LAST:event_baselineComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField actorTypeNameTextField;
    private javax.swing.JComboBox baselineComboBox;
    private javax.swing.JTable instancesTable;
    private javax.swing.JTextField instancesTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JPanel messagesProcessedPanel;
    private javax.swing.JTable messagesProcessedTable;
    private javax.swing.JPanel messagesReceivedPanel;
    private javax.swing.JTable messagesReceivedTable;
    private javax.swing.JTextField messagesReceivedTextField;
    private javax.swing.JPanel messagesSentPanel;
    private javax.swing.JTable messagesSentTable;
    private javax.swing.JTextField messagesSentTextField;
    private javax.swing.JTextField processingTimeTextField;
    private javax.swing.JTextField timeInMailboxTextField;
    // End of variables declaration//GEN-END:variables
   
    @Override
    public void componentOpened() {
        this.result = Utilities.actionsGlobalContext().lookupResult( ActorType.class );
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
        Collection<? extends ActorType> allSelectedTypes = result.allInstances();
        if( !allSelectedTypes.isEmpty() ) {
            allSelectedTypes.stream().forEach((type) -> {
                this.setData( type );
            });
        } else {
            System.out.println( "No selection" );
        }
    }
    
    /**
     * This method updates all tables and diagrams when a new actor instance was
     * selected in another window.
     * 
     * @param instance The selected actor instance.
     */
    private void setData( final ActorType actorType ) {
        this.content.set( Collections.singleton( actorType ), null );
        this.selectedMessageType = null;
        this.actorTypeNameTextField.setText( actorType.longTypeName );
        this.instancesTextField.setText( "" + actorType.getInstancesCount() );
        this.messagesSentTextField.setText( "" + actorType.getSentMessagesCount() );
        this.messagesReceivedTextField.setText( "" + actorType.getReceivedMessagesCount() );
        this.processingTimeTextField.setText( "" + ((double)actorType.getOverallProcessingTime()/1000000.0) );
        this.timeInMailboxTextField.setText( "" + (actorType.getAverageTimeInMailbox() / 1000000.0) );
        
        this.actorInstances = actorType.getActorInstances();
        this.instancesTable.clearSelection();
        this.instancesTable.setModel( new ActorInstancesTableModel( actorType.getActorInstances(), ProfilingData.getLoadedProfilingData().applicationStart() ));
        
        this.sentMessages = new ArrayList<>( actorType.sentMessagesStatistic().entrySet() );
        this.messagesSentTable.setModel( new MessagesSentTableModel( actorType.sentMessagesStatistic(), actorType.getSentMessagesCount() ) );
        this.messagesSentDataSet.clear();
        this.sentMessages.stream().forEach((d) -> {
            this.messagesSentDataSet.setValue( d.getKey().shortType, d.getValue());
        });
 
        
        this.receivedMessages = new ArrayList<>( actorType.receivedMessagesStatistic().entrySet() );
        this.messagesReceivedTable.setModel( new MessagesReceivedTableModel( actorType.receivedMessagesStatistic(), actorType.getReceivedMessagesCount() ) );
        this.messagesReceivedDataSet.clear();
        this.receivedMessages.stream().forEach((d) -> {        
            this.messagesReceivedDataSet.setValue( d.getKey().shortType, d.getValue());
        });
        
        this.processedMessages = new ArrayList<>( actorType.processedMessagesStatistic().entrySet() );
        this.messagesProcessedTable.setModel( new MessageProcessingTableModel( actorType.processedMessagesStatistic(), (String)this.baselineComboBox.getSelectedItem(), actorType.profilingData.applicationRunTime() ) );
        this.messagesProcessedDataSet.clear();
        this.processedMessages.stream().forEach((d) -> {
            this.messagesProcessedDataSet.setValue( d.getKey().shortType, d.getValue());
        });
      
        
    }
}
