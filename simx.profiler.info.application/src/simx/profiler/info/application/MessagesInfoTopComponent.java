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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;
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
import simx.profiler.model.MessageType;
import simx.profiler.model.ProfilingData;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.info.application//MessagesInfo//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MessagesInfoTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "simx.profiler.info.application.MessagesInfoTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MessagesInfoAction",
        preferredID = "MessagesInfoTopComponent"
)
@Messages({
    "CTL_MessagesInfoAction=MessagesInfo",
    "CTL_MessagesInfoTopComponent=MessagesInfo Window",
    "HINT_MessagesInfoTopComponent=This is a MessagesInfo window"
})
public final class MessagesInfoTopComponent extends TopComponent implements LookupListener {

    private Lookup.Result<CommunicationData> result = null;
    
    private final ProfilingData profilingData;
    
    private final DefaultPieDataset messagesDataSet;
    
    private CommunicationData communicationData;
    
    private final InstanceContent content;
    
    public MessagesInfoTopComponent() {
        initComponents();
        setName(Bundle.CTL_MessagesInfoTopComponent());
        setToolTipText(Bundle.HINT_MessagesInfoTopComponent());

        this.content = new InstanceContent();
        
        this.associateLookup( new AbstractLookup( this.content ) );
        
        this.profilingData = ProfilingData.getLoadedProfilingData();
        
        this.messagesDataSet = new DefaultPieDataset();
        
        this.createPieChart( this.messagesDataSet, this.graphicalPanel );
        
        final ListSelectionModel listSelectionModel = this.messageTypeInformationTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            if( messageTypeInformationTable.getSelectedRow() != -1 ) {
                final List< Map.Entry< MessageType, Integer > > data = new ArrayList<>( communicationData.getCommunicationData().entrySet() );
                final MessageType messageType = data.get( messageTypeInformationTable.getSelectedRow() ).getKey();
                content.set( Collections.singleton( messageType ), null );
            }
        });
    }

    private void createPieChart( final DefaultPieDataset data, final javax.swing.JPanel targetPanel ) {
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        graphicalPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageTypeInformationTable = new javax.swing.JTable();

        javax.swing.GroupLayout graphicalPanelLayout = new javax.swing.GroupLayout(graphicalPanel);
        graphicalPanel.setLayout(graphicalPanelLayout);
        graphicalPanelLayout.setHorizontalGroup(
            graphicalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 543, Short.MAX_VALUE)
        );
        graphicalPanelLayout.setVerticalGroup(
            graphicalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(MessagesInfoTopComponent.class, "MessagesInfoTopComponent.graphicalPanel.TabConstraints.tabTitle"), graphicalPanel); // NOI18N

        messageTypeInformationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Amount", "%"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
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
        messageTypeInformationTable.setMaximumSize(new java.awt.Dimension(2147483647, 32000));
        messageTypeInformationTable.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setViewportView(messageTypeInformationTable);
        if (messageTypeInformationTable.getColumnModel().getColumnCount() > 0) {
            messageTypeInformationTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(MessagesInfoTopComponent.class, "MessagesInfoTopComponent.messageTypeInformationTable.columnModel.title0")); // NOI18N
            messageTypeInformationTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(MessagesInfoTopComponent.class, "MessagesInfoTopComponent.messageTypeInformationTable.columnModel.title1")); // NOI18N
            messageTypeInformationTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(MessagesInfoTopComponent.class, "MessagesInfoTopComponent.messageTypeInformationTable.columnModel.title2")); // NOI18N
        }

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(MessagesInfoTopComponent.class, "MessagesInfoTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel graphicalPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable messageTypeInformationTable;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        this.result = Utilities.actionsGlobalContext().lookupResult( CommunicationData.class );
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
        Collection<? extends CommunicationData> allSelectedTypes = result.allInstances();
        if( !allSelectedTypes.isEmpty() ) {
            allSelectedTypes.stream().forEach((type) -> {
                this.setData( type );
            });
        } else {
            System.out.println( "No selection" );
        }
    }

    private void setData( final CommunicationData communicationData ) {
        this.messagesDataSet.clear();
        this.communicationData = communicationData;
        communicationData.getCommunicationData().entrySet().stream().forEach((d) -> {
            this.messagesDataSet.setValue( d.getKey().shortType, d.getValue() );
        });
        this.messageTypeInformationTable.setModel( new MessageTypeInformationTableModel( communicationData ));
    }
    
}
