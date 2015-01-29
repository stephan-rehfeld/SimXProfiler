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
import java.util.List;
import java.util.Map;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
import simx.profiler.model.MessageType;
import org.jfree.util.Rotation;

/**
 * This top components presents several information about instances of actors.
 * It contains several tables and visualizations.
 * 
 * @author Stephan Rehfeld
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.info.actor//ActorInstanceInfo//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ActorInstanceInfoTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "simx.profiler.info.actor.ActorInstanceInfoTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ActorInstanceInfoAction",
        preferredID = "ActorInstanceInfoTopComponent"
)
@Messages({
    "CTL_ActorInstanceInfoAction=ActorInstanceInfo",
    "CTL_ActorInstanceInfoTopComponent=ActorInstanceInfo Window",
    "HINT_ActorInstanceInfoTopComponent=This is a ActorInstanceInfo window"
})
public final class ActorInstanceInfoTopComponent extends TopComponent implements LookupListener {

    /**
     * The reference to the lookup that is used to communicate with other
     * top components.
     */
    private Lookup.Result<ActorInstance> result = null;
    
    /**
     * The content object that is used to provide information to other
     * plug-ins.
     */
    private final InstanceContent content;
    
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
     * The actor instance that is currently presented on the top component.
     */
    private ActorInstance actorInstance;
    
    /**
     * The plot data for the frequency of the actor.
     */
    private final XYSeriesCollection frequencyPlotData;
    
    /**
     * This constructor initializes the top component. It configures the
     * tales creates the visulizations.
     */
    public ActorInstanceInfoTopComponent() {
        initComponents();
        setName(Bundle.CTL_ActorInstanceInfoTopComponent());
        setToolTipText(Bundle.HINT_ActorInstanceInfoTopComponent());

        this.content = new InstanceContent();
        
        this.associateLookup( new AbstractLookup( this.content ) );
 
        ListSelectionModel listSelectionModel = this.messageSentTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            if( messageSentTable.getSelectedRow() != -1 ) {
                messagesReceivedTable.clearSelection();
                messageProcessedTable.clearSelection();
                if( selectedMessageType != null ) content.remove( selectedMessageType );
                selectedMessageType = sentMessages.get( messageSentTable.getSelectedRow() ).getKey();
                content.add( selectedMessageType );
            }
        });
        
        listSelectionModel = this.messagesReceivedTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            if( messagesReceivedTable.getSelectedRow() != -1 ) {
                messageSentTable.clearSelection();
                messageProcessedTable.clearSelection();
                if( selectedMessageType != null ) content.remove( selectedMessageType );
                selectedMessageType = receivedMessages.get( messagesReceivedTable.getSelectedRow() ).getKey();
                content.add( selectedMessageType );
            }     
        });
        
        listSelectionModel = this.messageProcessedTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            if( messageProcessedTable.getSelectedRow() != -1 ) {
                messageSentTable.clearSelection();
                messagesReceivedTable.clearSelection();
                if( selectedMessageType != null ) content.remove( selectedMessageType );
                selectedMessageType = processedMessages.get( messageProcessedTable.getSelectedRow() ).getKey();
                content.add( selectedMessageType );
            }     
        });
        
        this.messagesSentDataSet = new DefaultPieDataset();
        this.messagesReceivedDataSet = new DefaultPieDataset();
        this.messagesProcessedDataSet = new DefaultPieDataset();
   
        
        this.createPieChart( this.messagesSentDataSet, this.messagesSentPanel );
        this.createPieChart( this.messagesReceivedDataSet, this.messagesReceivedPanel );
        this.createPieChart( this.messagesProcessedDataSet, this.messagesProcessedPanel );
        
        this.frequencyPlotData = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart( "", "", "", this.frequencyPlotData );
        final ChartPanel chartPanel = new ChartPanel( chart );
        chartPanel.setPreferredSize( new java.awt.Dimension(261, 157) );
        this.frequencyPanel.setLayout( new BorderLayout() );
        this.frequencyPanel.add( chartPanel, BorderLayout.CENTER );
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
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        instanceIdTextField = new javax.swing.JTextField();
        typeTextField = new javax.swing.JTextField();
        messagesSentTextField = new javax.swing.JTextField();
        messageReceivedTextField = new javax.swing.JTextField();
        processingTimeTextField = new javax.swing.JTextField();
        simulationLoopMessageComboBox = new javax.swing.JComboBox();
        frequencyTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        timeInMailboxTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        messagesSentPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        messageSentTable = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        messagesReceivedPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        messagesReceivedTable = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        messagesProcessedPanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        messageProcessedTable = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        baselineComboBox = new javax.swing.JComboBox();
        frequencyPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(300, 0));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel2.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel7.text")); // NOI18N

        instanceIdTextField.setEditable(false);
        instanceIdTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.instanceIdTextField.text")); // NOI18N

        typeTextField.setEditable(false);
        typeTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.typeTextField.text")); // NOI18N

        messagesSentTextField.setEditable(false);
        messagesSentTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesSentTextField.text")); // NOI18N

        messageReceivedTextField.setEditable(false);
        messageReceivedTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageReceivedTextField.text")); // NOI18N

        processingTimeTextField.setEditable(false);
        processingTimeTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.processingTimeTextField.text")); // NOI18N

        simulationLoopMessageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulationLoopMessageComboBoxActionPerformed(evt);
            }
        });

        frequencyTextField.setEditable(false);
        frequencyTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.frequencyTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel9.text")); // NOI18N

        timeInMailboxTextField.setEditable(false);
        timeInMailboxTextField.setText(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.timeInMailboxTextField.text")); // NOI18N

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
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(messagesSentTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(typeTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(instanceIdTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(processingTimeTextField)
                            .addComponent(messageReceivedTextField, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(simulationLoopMessageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(frequencyTextField)
                            .addComponent(timeInMailboxTextField)))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(instanceIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(typeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(messagesSentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(messageReceivedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(processingTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(simulationLoopMessageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(frequencyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(timeInMailboxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel3.border.title"))); // NOI18N

        javax.swing.GroupLayout messagesSentPanelLayout = new javax.swing.GroupLayout(messagesSentPanel);
        messagesSentPanel.setLayout(messagesSentPanelLayout);
        messagesSentPanelLayout.setHorizontalGroup(
            messagesSentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );
        messagesSentPanelLayout.setVerticalGroup(
            messagesSentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 149, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesSentPanel.TabConstraints.tabTitle"), messagesSentPanel); // NOI18N

        messageSentTable.setModel(new javax.swing.table.DefaultTableModel(
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
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(messageSentTable);
        if (messageSentTable.getColumnModel().getColumnCount() > 0) {
            messageSentTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageSentTable.columnModel.title0")); // NOI18N
            messageSentTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageSentTable.columnModel.title1")); // NOI18N
            messageSentTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageSentTable.columnModel.title2")); // NOI18N
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

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

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

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel6.border.title"))); // NOI18N

        javax.swing.GroupLayout messagesReceivedPanelLayout = new javax.swing.GroupLayout(messagesReceivedPanel);
        messagesReceivedPanel.setLayout(messagesReceivedPanelLayout);
        messagesReceivedPanelLayout.setHorizontalGroup(
            messagesReceivedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );
        messagesReceivedPanelLayout.setVerticalGroup(
            messagesReceivedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 149, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesReceivedPanel.TabConstraints.tabTitle"), messagesReceivedPanel); // NOI18N

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
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(messagesReceivedTable);
        if (messagesReceivedTable.getColumnModel().getColumnCount() > 0) {
            messagesReceivedTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesReceivedTable.columnModel.title0")); // NOI18N
            messagesReceivedTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesReceivedTable.columnModel.title1")); // NOI18N
            messagesReceivedTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesReceivedTable.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel9.border.title"))); // NOI18N

        javax.swing.GroupLayout messagesProcessedPanelLayout = new javax.swing.GroupLayout(messagesProcessedPanel);
        messagesProcessedPanel.setLayout(messagesProcessedPanelLayout);
        messagesProcessedPanelLayout.setHorizontalGroup(
            messagesProcessedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );
        messagesProcessedPanelLayout.setVerticalGroup(
            messagesProcessedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messagesProcessedPanel.TabConstraints.tabTitle"), messagesProcessedPanel); // NOI18N

        messageProcessedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Processing Time (ms)", "Processing Time (%)"
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
        jScrollPane4.setViewportView(messageProcessedTable);
        if (messageProcessedTable.getColumnModel().getColumnCount() > 0) {
            messageProcessedTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageProcessedTable.columnModel.title0")); // NOI18N
            messageProcessedTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageProcessedTable.columnModel.title1")); // NOI18N
            messageProcessedTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.messageProcessedTable.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jPanel11.TabConstraints.tabTitle"), jPanel11); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.jLabel8.text")); // NOI18N

        baselineComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Instance", "Application" }));
        baselineComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baselineComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(baselineComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(baselineComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane3))
        );

        frequencyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorInstanceInfoTopComponent.class, "ActorInstanceInfoTopComponent.frequencyPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout frequencyPanelLayout = new javax.swing.GroupLayout(frequencyPanel);
        frequencyPanel.setLayout(frequencyPanelLayout);
        frequencyPanelLayout.setHorizontalGroup(
            frequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        frequencyPanelLayout.setVerticalGroup(
            frequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 177, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(frequencyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(frequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        if( this.messageProcessedTable.getModel() instanceof MessageProcessingTableModel ) {
            ((MessageProcessingTableModel)this.messageProcessedTable.getModel()).setBaseline( (String) this.baselineComboBox.getSelectedItem() );
        }
    }//GEN-LAST:event_baselineComboBoxActionPerformed

    /**
     * This message is called by the combo box that selects the message type that
     * is used to calculate the frequenc of the actor.
     * 
     * @param evt The event of combo box.
     */
    private void simulationLoopMessageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulationLoopMessageComboBoxActionPerformed
        if( !this.simulationLoopMessageComboBox.getSelectedItem().equals( ""  ) ) {
            final List< Long > startTimeStamps = new ArrayList<>();

            this.actorInstance.getMessagesProcessed().stream().filter((messageProcessingEvent) -> ( messageProcessingEvent.messageInstance.type.longType.equals( this.simulationLoopMessageComboBox.getSelectedItem() ) )).forEach((messageProcessingEvent) -> {
                startTimeStamps.add( messageProcessingEvent.start );
            });
       
            Collections.sort( startTimeStamps );
            final long timeSpan = startTimeStamps.get( startTimeStamps.size() -1 ) - startTimeStamps.get( 0 );
            final double sps = (double)startTimeStamps.size() * 1000000000.0 / (double)timeSpan;
            
            final XYSeries plotData = new XYSeries( (String)this.simulationLoopMessageComboBox.getSelectedItem() );
            
            for( int i = 0; i < startTimeStamps.size(); ++i ) {
                int start = i;
                int end = i;
                while( start-1 > 0 && startTimeStamps.get( i ) - startTimeStamps.get( start ) < 500000000 ) --start;
                while( end+1 < startTimeStamps.size() && startTimeStamps.get( end ) - startTimeStamps.get( i ) < 500000000 ) ++end;
                final int steps = end - start;
                final double time = (startTimeStamps.get( i ) - startTimeStamps.get( 0 )) / 1000000000.0;
                plotData.add( time, steps );
            }
            this.frequencyTextField.setText( "" + sps );
            this.frequencyPlotData.removeAllSeries();
            this.frequencyPlotData.addSeries( plotData );
        }
    }//GEN-LAST:event_simulationLoopMessageComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox baselineComboBox;
    private javax.swing.JPanel frequencyPanel;
    private javax.swing.JTextField frequencyTextField;
    private javax.swing.JTextField instanceIdTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable messageProcessedTable;
    private javax.swing.JTextField messageReceivedTextField;
    private javax.swing.JTable messageSentTable;
    private javax.swing.JPanel messagesProcessedPanel;
    private javax.swing.JPanel messagesReceivedPanel;
    private javax.swing.JTable messagesReceivedTable;
    private javax.swing.JPanel messagesSentPanel;
    private javax.swing.JTextField messagesSentTextField;
    private javax.swing.JTextField processingTimeTextField;
    private javax.swing.JComboBox simulationLoopMessageComboBox;
    private javax.swing.JTextField timeInMailboxTextField;
    private javax.swing.JTextField typeTextField;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void componentOpened() {
        this.result = Utilities.actionsGlobalContext().lookupResult( ActorInstance.class );
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
        Collection<? extends ActorInstance> allSelectedTypes = result.allInstances();
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
    private void setData( final ActorInstance instance ) {
        if( instance == null ) throw new IllegalArgumentException( "The parameter 'instance' must not be 'null'!" );
        this.selectedMessageType = null;
        this.instanceIdTextField.setText( instance.id );
        this.typeTextField.setText( instance.type.longTypeName );
        this.messagesSentTextField.setText( "" + instance.getSentMessagesCount() );
        this.messageReceivedTextField.setText( "" + instance.getReceivesMessagesCount() );
        this.processingTimeTextField.setText( "" + ((double)instance.getOverallProcessingTime()/1000000.0) );
        this.timeInMailboxTextField.setText( "" + (instance.getAverageTimeInMailbox() / 1000000.0) );
        
        final List< String > messageTypes = new ArrayList<>();
        messageTypes.add( "" );
        instance.getProcessedMessageTypes().stream().forEach((t) -> {
            messageTypes.add( t.longType );
        });
            
        this.simulationLoopMessageComboBox.setModel(new javax.swing.DefaultComboBoxModel( messageTypes.toArray( new String[ messageTypes.size() ] ) ) );
       
        this.frequencyTextField.setText( "Choose simulation loop message" );
        
        this.sentMessages = new ArrayList<>( instance.sentMessagesStatistic().entrySet() );
        this.messageSentTable.setModel( new MessagesSentTableModel( instance.sentMessagesStatistic() ,instance.getSentMessagesCount()) );
        this.messagesSentDataSet.clear();
        this.sentMessages.stream().forEach((d) -> {
            this.messagesSentDataSet.setValue( d.getKey().shortType, d.getValue());
        });
                
        this.receivedMessages = new ArrayList<>( instance.receivedMessagesStatistic().entrySet() );      
        this.messagesReceivedTable.setModel( new MessagesReceivedTableModel( instance.receivedMessagesStatistic(), instance.getReceivesMessagesCount() ) );
        this.messagesReceivedDataSet.clear();
        this.receivedMessages.stream().forEach((d) -> {
            this.messagesReceivedDataSet.setValue( d.getKey().shortType, d.getValue());
        });
                 
        this.processedMessages = new ArrayList<>( instance.processedMessagesStatistic().entrySet() );   
        this.messageProcessedTable.setModel( new MessageProcessingTableModel( instance.processedMessagesStatistic(), (String)this.baselineComboBox.getSelectedItem(), instance.type.profilingData.applicationRunTime() ) );
        this.messagesProcessedDataSet.clear();
        this.processedMessages.stream().forEach((d) -> {
            this.messagesProcessedDataSet.setValue( d.getKey().shortType, d.getValue());
        });
        this.frequencyPlotData.removeAllSeries();
        this.actorInstance = instance;
      
    }
}
