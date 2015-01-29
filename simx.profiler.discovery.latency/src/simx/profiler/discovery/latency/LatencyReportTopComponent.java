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

package simx.profiler.discovery.latency;

import java.awt.BorderLayout;
import java.util.Collection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.discovery.latency//LatencyReport//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "LatencyReportTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@ActionID(category = "Window", id = "simx.profiler.discovery.latency.LatencyReportTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LatencyReportAction",
        preferredID = "LatencyReportTopComponent"
)
@Messages({
    "CTL_LatencyReportAction=LatencyReport",
    "CTL_LatencyReportTopComponent=LatencyReport Window",
    "HINT_LatencyReportTopComponent=This is a LatencyReport window"
})
public final class LatencyReportTopComponent extends TopComponent implements LookupListener {

    private Lookup.Result< LatencyReport > result = null;
    
    private final XYSeriesCollection latencyPlotData;
    
    public LatencyReportTopComponent() {
        initComponents();
        setName(Bundle.CTL_LatencyReportTopComponent());
        setToolTipText(Bundle.HINT_LatencyReportTopComponent());

        this.latencyPlotData = new XYSeriesCollection();
        
        JFreeChart chart = ChartFactory.createXYLineChart( "", "", "", this.latencyPlotData );
        final ChartPanel chartPanel = new ChartPanel( chart );
        chartPanel.setPreferredSize( new java.awt.Dimension(261, 157) );
        this.latencyOverTimePanel.setLayout( new BorderLayout() );
        this.latencyOverTimePanel.add( chartPanel, BorderLayout.CENTER );
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        minLatencyTextField = new javax.swing.JTextField();
        maxLantecyTextField = new javax.swing.JTextField();
        avgLatencyTextField = new javax.swing.JTextField();
        medLatencyTextField = new javax.swing.JTextField();
        latencyOverTimePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pathTable = new javax.swing.JTable();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.jLabel4.text")); // NOI18N

        minLatencyTextField.setText(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.minLatencyTextField.text")); // NOI18N
        minLatencyTextField.setEnabled(false);

        maxLantecyTextField.setText(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.maxLantecyTextField.text")); // NOI18N
        maxLantecyTextField.setEnabled(false);

        avgLatencyTextField.setText(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.avgLatencyTextField.text")); // NOI18N
        avgLatencyTextField.setEnabled(false);

        medLatencyTextField.setText(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.medLatencyTextField.text")); // NOI18N
        medLatencyTextField.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(medLatencyTextField)
                    .addComponent(avgLatencyTextField)
                    .addComponent(minLatencyTextField)
                    .addComponent(maxLantecyTextField)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(minLatencyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(maxLantecyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(avgLatencyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(medLatencyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        latencyOverTimePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.latencyOverTimePanel.border.title"))); // NOI18N

        javax.swing.GroupLayout latencyOverTimePanelLayout = new javax.swing.GroupLayout(latencyOverTimePanel);
        latencyOverTimePanel.setLayout(latencyOverTimePanelLayout);
        latencyOverTimePanelLayout.setHorizontalGroup(
            latencyOverTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 405, Short.MAX_VALUE)
        );
        latencyOverTimePanelLayout.setVerticalGroup(
            latencyOverTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 160, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.jPanel3.border.title"))); // NOI18N

        pathTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Time Span", "Actor Instance", "Message Type", "Min", "Max", "Avg.", "Med."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Long.class, java.lang.Long.class, java.lang.Double.class, java.lang.Long.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(pathTable);
        if (pathTable.getColumnModel().getColumnCount() > 0) {
            pathTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title0")); // NOI18N
            pathTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title1")); // NOI18N
            pathTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title2")); // NOI18N
            pathTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title3")); // NOI18N
            pathTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title4")); // NOI18N
            pathTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title5")); // NOI18N
            pathTable.getColumnModel().getColumn(6).setHeaderValue(org.openide.util.NbBundle.getMessage(LatencyReportTopComponent.class, "LatencyReportTopComponent.pathTable.columnModel.title6")); // NOI18N
        }

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(latencyOverTimePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(latencyOverTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField avgLatencyTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel latencyOverTimePanel;
    private javax.swing.JTextField maxLantecyTextField;
    private javax.swing.JTextField medLatencyTextField;
    private javax.swing.JTextField minLatencyTextField;
    private javax.swing.JTable pathTable;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        this.result = Utilities.actionsGlobalContext().lookupResult( LatencyReport.class );
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
        Collection<? extends LatencyReport> allSelectedTypes = result.allInstances();
        if( !allSelectedTypes.isEmpty() ) {
            allSelectedTypes.stream().forEach((type) -> {
                this.setData( type );
            });
        } else {
            System.out.println( "No selection" );
        }
    }

    private void setData( final LatencyReport latencyReport ) {
        this.minLatencyTextField.setText( "" + ((double)latencyReport.getMin()/1000000.0) );
        this.maxLantecyTextField.setText( "" + ((double)latencyReport.getMax()/1000000.0) );
        this.avgLatencyTextField.setText( "" + ((double)latencyReport.getAvg()/1000000.0) );
        this.medLatencyTextField.setText( "" + ((double)latencyReport.getMed()/1000000.0) );
        
        this.latencyPlotData.removeAllSeries();
        
        final XYSeries plotData = new XYSeries( "Latency" );
        final long startTimestamp = latencyReport.getOverallLatencies().get( 0 ).a;
        latencyReport.getOverallLatencies().stream().forEach((d) -> {
            final double time = (d.a - startTimestamp) / 1000000000.0;
            plotData.add( time, (double)d.b / 1000000.0 );
        });
        this.latencyPlotData.addSeries( plotData );
        this.pathTable.setModel( new LatencyReportTableModel( latencyReport.getStart() ) );
    }
}
