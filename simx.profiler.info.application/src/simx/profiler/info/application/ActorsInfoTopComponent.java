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
import java.util.Collections;
import java.util.HashMap;
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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import simx.profiler.discovery.consistency.events.StartConsistencyDiscoveryEvent;
import simx.profiler.discovery.latency.events.StartLatencyDiscoveryEvent;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.ActorType;
import simx.profiler.model.ImmutableTupel;
import simx.profiler.model.MessageType;
import simx.profiler.model.ParallelismEvent;
import simx.profiler.model.ProfilingData;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.info.application//ActorsInfo//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ActorsInfoTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "simx.profiler.info.application.ActorsInfoTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ActorsInfoAction",
        preferredID = "ActorsInfoTopComponent"
)
@Messages({
    "CTL_ActorsInfoAction=ActorsInfo",
    "CTL_ActorsInfoTopComponent=ActorsInfo Window",
    "HINT_ActorsInfoTopComponent=This is a ActorsInfo window"
})
public final class ActorsInfoTopComponent extends TopComponent {
    
    private final ProfilingData profilingData;
    private final List< ActorType > actorTypes;
    private final List< ActorInstance > actorInstances;
    private final InstanceContent content;
    private ActorType selectedActorType;
    private ActorInstance selectedActorInstance;
    private final CommunicationData applicationCommunicationData;
    private CommunicationData selectedCommunicationData;
    private StartLatencyDiscoveryEvent latencyDiscoveryStartEvent;
    private StartConsistencyDiscoveryEvent startConsistencyDiscoveryEvent;
    private final DefaultCategoryDataset parallelismHistogramDataSet;
    private final XYSeriesCollection dopPlotData;
     
    public ActorsInfoTopComponent() {
        initComponents();
        setName(Bundle.CTL_ActorsInfoTopComponent());
        setToolTipText(Bundle.HINT_ActorsInfoTopComponent());
        
        this.content = new InstanceContent();
        
        this.associateLookup( new AbstractLookup( this.content ) );
        
        
        this.profilingData = ProfilingData.getLoadedProfilingData();
        final Map< MessageType, Integer > applicationCommunicationDataLocal = new HashMap<>();
        this.profilingData.getMessageTypes().stream().forEach((messageType) -> {
            applicationCommunicationDataLocal.put( messageType, messageType.getTimesSent() );
        });
        this.applicationCommunicationData = new CommunicationData( new ImmutableTupel<>( null, null ), applicationCommunicationDataLocal );
        this.content.set( Collections.singleton( this.applicationCommunicationData ), null);
        
        this.actorTypes = this.profilingData.getActorTypes();
        this.actorTypeInformationTable.setModel( new ActorTypeInformationTableModel( this.profilingData ) );
        ListSelectionModel listSelectionModel = this.actorTypeInformationTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (final ListSelectionEvent e) -> {
            setSelectedActorType( actorTypes.get( actorTypeInformationTable.getSelectedRow() ) );
        });
        this.actorInstances = profilingData.getActorInstances();
        listSelectionModel = this.actorInstanceInformationTable.getSelectionModel();
        listSelectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        listSelectionModel.addListSelectionListener( (ListSelectionEvent e) -> {
            final ActorInstance actorInstance = actorInstances.get( actorInstanceInformationTable.getSelectedRow() );
            setSelectedActorInstance( actorInstance );
        });
        this.actorInstanceInformationTable.setModel( new ActorInstanceInformationTableModel( this.profilingData ) );
        
        long minProcessingTime = Long.MAX_VALUE;
        long maxProcessingTime = Long.MIN_VALUE;
        
        for( final ActorType type : this.actorTypes ) {
            if( type.getOverallProcessingTime() < minProcessingTime ) minProcessingTime = type.getOverallProcessingTime();
            if( type.getOverallProcessingTime() > maxProcessingTime ) maxProcessingTime = type.getOverallProcessingTime();
        }
        
        
        final Map< ImmutableTupel< ActorType, ActorType >, Integer > typeCommunicationScaleFactors = new HashMap<>();
        int minMessagesCount = Integer.MAX_VALUE;
        int maxMessagesCount = Integer.MIN_VALUE;
        for( final ActorType actorType : this.actorTypes ) {
            final Map< ActorType, Map< MessageType, Integer > > s = actorType.getReceiverStatistics();
            for( final Map.Entry< ActorType, Map< MessageType, Integer >  > e : s.entrySet() ) {
                int count = 0;
                count = e.getValue().entrySet().stream().map((d) -> d.getValue()).reduce(count, Integer::sum);
                typeCommunicationScaleFactors.put( new ImmutableTupel<>( actorType, e.getKey()) , count ); 
                if( count < minMessagesCount ) minMessagesCount = count;
                if( count > maxMessagesCount ) maxMessagesCount = count;
            }
        }

        int messagesSpan = maxMessagesCount - minMessagesCount;
        for( final Map.Entry< ImmutableTupel< ActorType, ActorType >, Integer > e : typeCommunicationScaleFactors.entrySet() ) {
            final int factor = (((e.getValue() - minMessagesCount) * 4) / messagesSpan ) + 1;
            typeCommunicationScaleFactors.put( e.getKey(), factor );
        }
        
        double timeSpan = maxProcessingTime - minProcessingTime;
        
        final Map< ActorType, Double > typeComputationScaleFactors = new HashMap<>();
        for( final ActorType type : this.actorTypes ) {
            typeComputationScaleFactors.put( type, ((double)(type.getOverallProcessingTime() - minProcessingTime) * 0.4 / timeSpan) + 0.6 );
        }
        
        final ActorTypeGraphScene actorTypeGraphScene = new ActorTypeGraphScene( this, typeComputationScaleFactors, typeCommunicationScaleFactors );
        actorTypeGraphScene.addObjectSceneListener( new ObjectSceneListener() {

            @Override
            public void objectAdded( final ObjectSceneEvent ose, final Object o) {}

            @Override
            public void objectRemoved( final ObjectSceneEvent ose, final Object o) {}

            @Override
            public void objectStateChanged( final ObjectSceneEvent ose, final Object o, final ObjectState os, final ObjectState os1 ) {}

            @Override
            public void selectionChanged( final ObjectSceneEvent ose, final Set<Object> oldSelection, final Set<Object> newSelection ) {
                boolean communicationDataSet = false;
                for( final Object o : newSelection ) {
                    if( o instanceof ActorType ) setSelectedActorType( (ActorType)o ); 
                    if( o instanceof CommunicationData ) {                   
                        setSelectedCommunicationData( (CommunicationData)o );
                        communicationDataSet = true;
                    }
                }
                if( !communicationDataSet ) setSelectedCommunicationData( null );
            }

            @Override
            public void highlightingChanged( final ObjectSceneEvent ose, final Set<Object> set, final Set<Object> set1) {}

            @Override
            public void hoverChanged( final ObjectSceneEvent ose, final Object o, final Object o1) {}

            @Override
            public void focusChanged( final ObjectSceneEvent ose, final Object o, final Object o1) { }
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED );
        this.typeScrollPane.setViewportView( actorTypeGraphScene.createView() );
        
  
        this.actorTypes.stream().forEach((actorType) -> {
            actorTypeGraphScene.addNode( actorType );
        });
        this.actorTypes.stream().forEach((actorType) -> {
            final Map< ActorType, Map< MessageType, Integer > > s = actorType.getReceiverStatistics();
            s.entrySet().stream().forEach((e) -> {
                final CommunicationData edge = new CommunicationData( new ImmutableTupel<>( actorType, e.getKey() ), e.getValue() );
                actorTypeGraphScene.addEdge( edge );
                actorTypeGraphScene.setEdgeSource( edge, actorType );
                actorTypeGraphScene.setEdgeTarget( edge, e.getKey() );
            });
        });
        
        minProcessingTime = Long.MAX_VALUE;
        maxProcessingTime = Long.MIN_VALUE;
        
        for( final ActorInstance instance : this.actorInstances ) {
            if( instance.getOverallProcessingTime() < minProcessingTime ) minProcessingTime = instance.getOverallProcessingTime();
            if( instance.getOverallProcessingTime() > maxProcessingTime ) maxProcessingTime = instance.getOverallProcessingTime();
        }

        timeSpan = maxProcessingTime - minProcessingTime;
        
        final Map< ImmutableTupel< ActorInstance, ActorInstance >, Integer > instanceCommunicationScaleFactors = new HashMap<>();
        minMessagesCount = Integer.MAX_VALUE;
        maxMessagesCount = Integer.MIN_VALUE;
        for( final ActorInstance instance : this.actorInstances ) {
            final Map< ActorInstance, Map< MessageType, Integer > > s = instance.getReceiverStatistics();
            for( final Map.Entry< ActorInstance, Map< MessageType, Integer >  > e : s.entrySet() ) {
                int count = 0;
                count = e.getValue().entrySet().stream().map((d) -> d.getValue()).reduce(count, Integer::sum);
                instanceCommunicationScaleFactors.put( new ImmutableTupel<>( instance, e.getKey()) , count ); 
                if( count < minMessagesCount ) minMessagesCount = count;
                if( count > maxMessagesCount ) maxMessagesCount = count;
            }
        }

        messagesSpan = maxMessagesCount - minMessagesCount;
        for( final Map.Entry< ImmutableTupel< ActorInstance, ActorInstance >, Integer > e : instanceCommunicationScaleFactors.entrySet() ) {
            final int factor = (((e.getValue() - minMessagesCount) * 4) / messagesSpan ) + 1;
            instanceCommunicationScaleFactors.put( e.getKey(), factor );
        }
        
        final Map< ActorInstance, Double > instanceComputationScaleFactors = new HashMap<>();
        for( final ActorInstance instance : this.actorInstances ) {
            instanceComputationScaleFactors.put( instance, ((double)(instance.getOverallProcessingTime() - minProcessingTime) * 0.4 / timeSpan) + 0.6 );
        }
        final ActorInstanceGraphScene actorInstanceGraphScene = new ActorInstanceGraphScene( this, instanceComputationScaleFactors, instanceCommunicationScaleFactors );
        
        actorInstanceGraphScene.addObjectSceneListener( new ObjectSceneListener() {

            @Override
            public void objectAdded( final ObjectSceneEvent ose, final Object o) {}

            @Override
            public void objectRemoved( final ObjectSceneEvent ose, final Object o) {}

            @Override
            public void objectStateChanged( final ObjectSceneEvent ose, final Object o, final ObjectState os, final ObjectState os1 ) {}

            @Override
            public void selectionChanged( final ObjectSceneEvent ose, final Set<Object> oldSelection, final Set<Object> newSelection ) {
                boolean communicationDataSet = false;
                for( final Object o : newSelection ) {
                    if( o instanceof ActorInstance ) setSelectedActorInstance( (ActorInstance)o );   
                    if( o instanceof CommunicationData ) {                   
                        setSelectedCommunicationData( (CommunicationData)o );
                        communicationDataSet = true;
                    }
                }
                if( !communicationDataSet ) setSelectedCommunicationData( null );
            }

            @Override
            public void highlightingChanged( final ObjectSceneEvent ose, final Set<Object> set, final Set<Object> set1) {}

            @Override
            public void hoverChanged( final ObjectSceneEvent ose, final Object o, final Object o1) {}

            @Override
            public void focusChanged( final ObjectSceneEvent ose, final Object o, final Object o1) { }
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED );
        
        this.instancesScrollPane.setViewportView( actorInstanceGraphScene.createView() );
        this.actorInstances.stream().forEach((actorInstance) -> {
            actorInstanceGraphScene.addNode( actorInstance );
        });
        this.actorInstances.stream().forEach((actorInstance) -> {
            final Map< ActorInstance, Map< MessageType, Integer > > s = actorInstance.getReceiverStatistics();
            s.entrySet().stream().forEach((e) -> {
                final CommunicationData edge = new CommunicationData( new ImmutableTupel<>( actorInstance, e.getKey() ), e.getValue() );
                actorInstanceGraphScene.addEdge( edge );
                actorInstanceGraphScene.setEdgeSource( edge, actorInstance );
                actorInstanceGraphScene.setEdgeTarget( edge, e.getKey() );
            });
        });
        
        this.dopPlotData = new XYSeriesCollection();
        JFreeChart dopChart = ChartFactory.createXYLineChart( "", "", "", this.dopPlotData );
        final ChartPanel dopChartPanel = new ChartPanel( dopChart );
        dopChartPanel.setPreferredSize( new java.awt.Dimension(261, 157) );
        this.dopPanel.setLayout( new BorderLayout() );
        this.dopPanel.add( dopChartPanel, BorderLayout.CENTER );
        
        final XYSeries plotData = new XYSeries( "Degree of Parallelism" );
        
        final List< ParallelismEvent > parallelismEvents = this.profilingData.getParallelismEvents();
        Collections.sort( parallelismEvents );
        int parallelismLevel = 1;
        long lastTimeStamp = parallelismEvents.get( 0 ).timestamp;
        final long firstTimeStamp = lastTimeStamp;
        final Map< Integer, Long > histogramData = new HashMap<>();
        plotData.add( 0, 1 );
        for( int i = 1; i < parallelismEvents.size(); ++i ) {
            if( histogramData.containsKey( parallelismLevel ) ) {               
                final long old = histogramData.get( parallelismLevel );
                histogramData.put( parallelismLevel, parallelismEvents.get( i ).timestamp - lastTimeStamp + old );
            } else {
                histogramData.put( parallelismLevel, parallelismEvents.get( i ).timestamp - lastTimeStamp );
            }
            lastTimeStamp = parallelismEvents.get( i ).timestamp;
            if( parallelismEvents.get( i ).eventType == ParallelismEvent.ParallelimEventTypes.PROCESSING_START ) {
               ++parallelismLevel; 
            } else {
               --parallelismLevel;
            }
            plotData.add( (double)(lastTimeStamp - firstTimeStamp) / 1000000000.0, parallelismLevel );
        }
        this.dopPlotData.addSeries( plotData );
        this.parallelismHistogramDataSet = new DefaultCategoryDataset();
        
        double avgParallelism1 = 0.0;
        double avgParallelism2 = 0.0;
        long t = 0;
        
        for( int i = 1; i < histogramData.size(); ++i ) {
            t += histogramData.get( i );
        }
        
        for( int i = 0; i < histogramData.size(); ++i ) {
            parallelismHistogramDataSet.addValue( (double)histogramData.get( i ) / 1000000.0, "", i==0?"Idle":""+i );
            avgParallelism1 += i * ((double)histogramData.get( i ) / this.profilingData.applicationRunTime() );
            avgParallelism2 += i * ((double)histogramData.get( i ) / t );
        }
        
        final JFreeChart chart = ChartFactory.createBarChart( "", "Parallelism", "ms", this.parallelismHistogramDataSet, PlotOrientation.VERTICAL, false, true, false );
        final ChartPanel chartPanel = new ChartPanel( chart );
        this.parallelismHistogramPanel.setLayout( new BorderLayout() );
        this.parallelismHistogramPanel.add( chartPanel, BorderLayout.CENTER );
        
        this.runtimeTextField.setText( "" + (this.profilingData.applicationRunTime() / 1000000.0) );
        this.computationTimeMsTextField.setText( "" + (this.profilingData.getOverallProcessingTime() / 1000000.0 ));
        this.computationTimePercentTextField.setText( "" + (this.profilingData.getOverallProcessingTime() * 100.0 / this.profilingData.applicationRunTime() ) );
        this.actorInstancesTextField.setText( "" + this.actorInstances.size() );
        this.messagesSentTextField.setText( "" + this.profilingData.getMessagesSentCount() );
        this.messagesSentPerSecondTextField.setText( "" + ((double)this.profilingData.getMessagesSentCount() * 1000000000.0 / this.profilingData.applicationRunTime() ) );
        this.messagesProcessedTextField.setText( "" + this.profilingData.getMessagesProcessedCount() );
        this.messagesProcessedPerSecondTextField.setText( "" + ((double)this.profilingData.getMessagesProcessedCount() * 1000000000.0 / this.profilingData.applicationRunTime() ) );
        this.averageTimeInMailboxTextField.setText( "" + (this.profilingData.getAverageTimeInMailbox() / 1000000.0) );
        this.avgParallelismWithIdleTimeTextField.setText( "" + avgParallelism1 );
        this.avgParallelismWithouIdleTimeTextField.setText( "" + avgParallelism2 );
        
        
        final SpawnTreeGraphScene spawnTreeGraphScene = new SpawnTreeGraphScene( this );
        this.spawnTreeScrollPane.setViewportView( spawnTreeGraphScene.createView() );
        
        this.actorInstances.stream().forEach((actorInstance) -> {
            spawnTreeGraphScene.addNode( actorInstance );
        });
        for( final ActorInstance actorInstance : this.actorInstances ) {
            if( actorInstance.supervisor != null ) {
                final ImmutableTupel< ActorInstance, ActorInstance > edge = new ImmutableTupel( actorInstance.supervisor, actorInstance );
                spawnTreeGraphScene.addEdge( edge );
                spawnTreeGraphScene.setEdgeSource( edge, actorInstance.supervisor );
                spawnTreeGraphScene.setEdgeTarget( edge, actorInstance );
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        includeIdleTimeCheckBox = new javax.swing.JCheckBox();
        parallelismHistogramPanel = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        avgParallelismWithouIdleTimeTextField = new javax.swing.JTextField();
        avgParallelismWithIdleTimeTextField = new javax.swing.JTextField();
        averageTimeInMailboxTextField = new javax.swing.JTextField();
        messagesProcessedPerSecondTextField = new javax.swing.JTextField();
        messagesSentPerSecondTextField = new javax.swing.JTextField();
        messagesSentTextField = new javax.swing.JTextField();
        actorInstancesTextField = new javax.swing.JTextField();
        computationTimePercentTextField = new javax.swing.JTextField();
        computationTimeMsTextField = new javax.swing.JTextField();
        runtimeTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        messagesProcessedTextField = new javax.swing.JTextField();
        dopPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        typeScrollPane = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        actorTypeInformationTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        instancesScrollPane = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        actorInstanceInformationTable = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        spawnTreeScrollPane = new javax.swing.JScrollPane();

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel9.border.title"))); // NOI18N

        includeIdleTimeCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(includeIdleTimeCheckBox, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.includeIdleTimeCheckBox.text")); // NOI18N
        includeIdleTimeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeIdleTimeCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout parallelismHistogramPanelLayout = new javax.swing.GroupLayout(parallelismHistogramPanel);
        parallelismHistogramPanel.setLayout(parallelismHistogramPanelLayout);
        parallelismHistogramPanelLayout.setHorizontalGroup(
            parallelismHistogramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        parallelismHistogramPanelLayout.setVerticalGroup(
            parallelismHistogramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(includeIdleTimeCheckBox)
                .addGap(0, 362, Short.MAX_VALUE))
            .addComponent(parallelismHistogramPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(includeIdleTimeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(parallelismHistogramPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel10.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel10.text")); // NOI18N

        avgParallelismWithouIdleTimeTextField.setEditable(false);
        avgParallelismWithouIdleTimeTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.avgParallelismWithouIdleTimeTextField.text")); // NOI18N

        avgParallelismWithIdleTimeTextField.setEditable(false);
        avgParallelismWithIdleTimeTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.avgParallelismWithIdleTimeTextField.text")); // NOI18N

        averageTimeInMailboxTextField.setEditable(false);
        averageTimeInMailboxTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.averageTimeInMailboxTextField.text")); // NOI18N

        messagesProcessedPerSecondTextField.setEditable(false);
        messagesProcessedPerSecondTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.messagesProcessedPerSecondTextField.text")); // NOI18N

        messagesSentPerSecondTextField.setEditable(false);
        messagesSentPerSecondTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.messagesSentPerSecondTextField.text")); // NOI18N

        messagesSentTextField.setEditable(false);
        messagesSentTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.messagesSentTextField.text")); // NOI18N

        actorInstancesTextField.setEditable(false);
        actorInstancesTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstancesTextField.text")); // NOI18N

        computationTimePercentTextField.setEditable(false);
        computationTimePercentTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.computationTimePercentTextField.text")); // NOI18N

        computationTimeMsTextField.setEditable(false);
        computationTimeMsTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.computationTimeMsTextField.text")); // NOI18N

        runtimeTextField.setEditable(false);
        runtimeTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.runtimeTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jLabel11.text")); // NOI18N

        messagesProcessedTextField.setEditable(false);
        messagesProcessedTextField.setText(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.messagesProcessedTextField.text")); // NOI18N

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel11))
                .addGap(51, 51, 51)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(computationTimeMsTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(computationTimePercentTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(actorInstancesTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagesSentTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagesSentPerSecondTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagesProcessedTextField)
                    .addComponent(runtimeTextField)))
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(avgParallelismWithouIdleTimeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                    .addComponent(avgParallelismWithIdleTimeTextField)
                    .addComponent(averageTimeInMailboxTextField)
                    .addComponent(messagesProcessedPerSecondTextField)))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(runtimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(computationTimeMsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(computationTimePercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(actorInstancesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(messagesSentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(messagesSentPerSecondTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(messagesProcessedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(messagesProcessedPerSecondTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(averageTimeInMailboxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(avgParallelismWithIdleTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(avgParallelismWithouIdleTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        dopPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.dopPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout dopPanelLayout = new javax.swing.GroupLayout(dopPanel);
        dopPanel.setLayout(dopPanelLayout);
        dopPanelLayout.setHorizontalGroup(
            dopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dopPanelLayout.setVerticalGroup(
            dopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 121, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane3.setViewportView(jPanel2);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(typeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(typeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        actorTypeInformationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Instances", "Sent Messages", "Received Messages", "Execution time (ms)", "Execution time (%)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(actorTypeInformationTable);
        if (actorTypeInformationTable.getColumnModel().getColumnCount() > 0) {
            actorTypeInformationTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorTypeInformationTable.columnModel.title0_1")); // NOI18N
            actorTypeInformationTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorTypeInformationTable.columnModel.title1_1")); // NOI18N
            actorTypeInformationTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorTypeInformationTable.columnModel.title2_1")); // NOI18N
            actorTypeInformationTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorTypeInformationTable.columnModel.title3_1")); // NOI18N
            actorTypeInformationTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorTypeInformationTable.columnModel.title4")); // NOI18N
            actorTypeInformationTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorTypeInformationTable.columnModel.title5")); // NOI18N
        }

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(instancesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(instancesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        actorInstanceInformationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Instance ID", "Type", "Messages Sent", "Messages Received", "Execution time (ms)", "Execution time (%)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(actorInstanceInformationTable);
        if (actorInstanceInformationTable.getColumnModel().getColumnCount() > 0) {
            actorInstanceInformationTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstanceInformationTable.columnModel.title0_1")); // NOI18N
            actorInstanceInformationTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstanceInformationTable.columnModel.title1_1")); // NOI18N
            actorInstanceInformationTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstanceInformationTable.columnModel.title2_1")); // NOI18N
            actorInstanceInformationTable.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstanceInformationTable.columnModel.title3_1")); // NOI18N
            actorInstanceInformationTable.getColumnModel().getColumn(4).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstanceInformationTable.columnModel.title4")); // NOI18N
            actorInstanceInformationTable.getColumnModel().getColumn(5).setHeaderValue(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.actorInstanceInformationTable.columnModel.title5")); // NOI18N
        }

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spawnTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spawnTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ActorsInfoTopComponent.class, "ActorsInfoTopComponent.jPanel11.TabConstraints.tabTitle"), jPanel11); // NOI18N

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

    private void includeIdleTimeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeIdleTimeCheckBoxActionPerformed
        this.parallelismHistogramDataSet.clear();
        
        final List< ParallelismEvent > parallelismEvents = this.profilingData.getParallelismEvents();
        Collections.sort( parallelismEvents );
        int parallelismLevel = 1;
        long lastTimeStamp = parallelismEvents.get( 0 ).timestamp;
        final Map< Integer, Long > histogramData = new HashMap<>();
        for( int i = 1; i < parallelismEvents.size(); ++i ) {
            if( histogramData.containsKey( parallelismLevel ) ) {               
                final long old = histogramData.get( parallelismLevel );
                histogramData.put( parallelismLevel, parallelismEvents.get( i ).timestamp - lastTimeStamp + old );
            } else {
                histogramData.put( parallelismLevel, parallelismEvents.get( i ).timestamp - lastTimeStamp );
            }
            lastTimeStamp = parallelismEvents.get( i ).timestamp;
            if( parallelismEvents.get( i ).eventType == ParallelismEvent.ParallelimEventTypes.PROCESSING_START ) {
               ++parallelismLevel; 
            } else {
               --parallelismLevel;
            }
        }
        
        for( int i = 0; i < histogramData.size(); ++i ) {
            if( i != 0 || i == 0 && this.includeIdleTimeCheckBox.isSelected() ) 
                parallelismHistogramDataSet.addValue( (double)histogramData.get( i ) / 1000000.0, "", i==0?"Idle":""+i );
        }
    }//GEN-LAST:event_includeIdleTimeCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actorInstanceInformationTable;
    private javax.swing.JTextField actorInstancesTextField;
    private javax.swing.JTable actorTypeInformationTable;
    private javax.swing.JTextField averageTimeInMailboxTextField;
    private javax.swing.JTextField avgParallelismWithIdleTimeTextField;
    private javax.swing.JTextField avgParallelismWithouIdleTimeTextField;
    private javax.swing.JTextField computationTimeMsTextField;
    private javax.swing.JTextField computationTimePercentTextField;
    private javax.swing.JPanel dopPanel;
    private javax.swing.JCheckBox includeIdleTimeCheckBox;
    private javax.swing.JScrollPane instancesScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTextField messagesProcessedPerSecondTextField;
    private javax.swing.JTextField messagesProcessedTextField;
    private javax.swing.JTextField messagesSentPerSecondTextField;
    private javax.swing.JTextField messagesSentTextField;
    private javax.swing.JPanel parallelismHistogramPanel;
    private javax.swing.JTextField runtimeTextField;
    private javax.swing.JScrollPane spawnTreeScrollPane;
    private javax.swing.JScrollPane typeScrollPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        
    }

    @Override
    public void componentClosed() {
        
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

    void setSelectedActorType( final ActorType actorType ) {
        this.selectedActorType = actorType;
        this.constructContent();
    }

    void setSelectedActorInstance( final ActorInstance actorInstance ) {     
        this.selectedActorType = actorInstance.type;
        this.selectedActorInstance = actorInstance;
        this.constructContent();            
        
    }
    
     void setSelectedCommunicationData( final CommunicationData communicationData ) {   
        this.selectedCommunicationData = communicationData;
        this.constructContent();            
        
    }
    
    void constructContent() {
        final Set<Object> selectedObjects = new HashSet<>();
        if( this.selectedActorType != null ) selectedObjects.add( this.selectedActorType );
        if( this.selectedActorInstance != null ) selectedObjects.add( this.selectedActorInstance );
        if( this.latencyDiscoveryStartEvent != null ) selectedObjects.add( this.latencyDiscoveryStartEvent );
        if( this.startConsistencyDiscoveryEvent != null ) selectedObjects.add( this.startConsistencyDiscoveryEvent );
        if( this.selectedCommunicationData != null ) 
            selectedObjects.add( this.selectedCommunicationData );
        else 
            selectedObjects.add( this.applicationCommunicationData );
        content.set( selectedObjects, null );
    }

    void setLatencyDiscoveryStartInstance(ActorInstance actorInstance) {
        this.latencyDiscoveryStartEvent = new StartLatencyDiscoveryEvent( actorInstance );
        this.constructContent();
    }

    void setConsistencyDiscoveryInstances( final Set<ActorInstance> actorInstances ) {
        this.startConsistencyDiscoveryEvent = new StartConsistencyDiscoveryEvent( actorInstances );
        this.constructContent();
    }
    
}
