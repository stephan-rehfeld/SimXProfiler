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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.MessageProcessingEvent;
import simx.profiler.model.MessageSentEvent;
import simx.profiler.model.MessageType;
import simx.profiler.model.ProfilingData;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//simx.profiler.info.application//CommunicationDetails//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CommunicationDetailsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "simx.profiler.info.application.CommunicationDetailsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CommunicationDetailsAction",
        preferredID = "CommunicationDetailsTopComponent"
)
@Messages({
    "CTL_CommunicationDetailsAction=CommunicationDetails",
    "CTL_CommunicationDetailsTopComponent=CommunicationDetails Window",
    "HINT_CommunicationDetailsTopComponent=This is a CommunicationDetails window"
})
public final class CommunicationDetailsTopComponent extends TopComponent {

    private final ProfilingData profilingData;
    
    class NamesDrawPanel extends javax.swing.JPanel {
        
        @Override
        public void paintComponent( final Graphics g ) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D)g;
            
            
            final List< ActorInstance > actorInstances = profilingData.getActorInstances();
            
            for( int i = 0; i < actorInstances.size(); ++i ) {
                g2.drawString( actorInstances.get( i ).shortId + " (" + actorInstances.get( i ).type.shortTypeName + ")", 10, 15 + 35 * i );
                
                final Line2D line = new Line2D.Double(10, 20 + 35 * i, this.getSize().width, 20 + 35 * i);
                g2.setStroke(new BasicStroke(1));
                g2.draw(line);
            }
        }
    }
    
    class CommunicationsDetailsDrawPanel extends javax.swing.JPanel {
        @Override
        public void paintComponent( final Graphics g ) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D)g;
            
            
            final List< ActorInstance > actorInstances = profilingData.getActorInstances();
            
            final int width = this.getSize().width;
            
            final Set< MessageType > messageTypes = new HashSet<>();
            
            for( int i = 0; i < actorInstances.size(); ++i ) {
                final Line2D line = new Line2D.Double(-10, 20 + 35 * i, width, 20 + 35 * i);
                g2.setStroke(new BasicStroke(1));
                g2.draw(line);
                
                final ActorInstance actorInstance = actorInstances.get( i );
                final Iterator< MessageProcessingEvent > it = actorInstance.getMessagesProcessed().iterator();
                
                while( it.hasNext() ) {
                    final MessageProcessingEvent mpe = it.next();
                    
                    final long relativeStart = mpe.start - profilingData.applicationStart();
                    final long relativeEnd = mpe.end - profilingData.applicationStart();
                    
                    //System.out.println( "1: " + (relativeEnd/1000000) + "<" + detailsScrollBar.getValue() + " = " + ((relativeEnd/1000000)  < detailsScrollBar.getValue()) );
                    //System.out.println( "2: " + (relativeStart/1000000) + ">" + (detailsScrollBar.getValue() + jSlider1.getValue()) + " = " + ((relativeStart/1000000) > (detailsScrollBar.getValue() + jSlider1.getValue())) );
                    if( (relativeEnd/1000000)  < detailsScrollBar.getValue() || (relativeStart/1000000) > (detailsScrollBar.getValue() + jSlider1.getValue()) ) {
                        continue;
                    }
                    
                    
                    final double normalizedStart = (double)(relativeStart-(long)detailsScrollBar.getValue()*1000000l)/(double)((long)jSlider1.getValue() * 1000000l);
                    final double normalizedEnd = (double)(relativeEnd-(long)detailsScrollBar.getValue()*1000000l)/(double)((long)jSlider1.getValue() * 1000000l);
                    final double w = normalizedEnd - normalizedStart;
                
                    final Rectangle2D rect = new Rectangle2D.Double( (double)width * normalizedStart, 15 + 35 * i, (double)width * w, 10  );
                    
                    g2.setColor( Color.BLACK );
                    
                    g2.draw(rect);
                    
                    final int h = mpe.messageInstance.type.hashCode();
                    messageTypes.add( mpe.messageInstance.type );
                    g2.setColor( new Color( (h >> 16) & 0xff, (h >> 8) & 0xff, h & 0xff ) );
                    
                    g2.fill(rect);
                    
                    
                    if( jSlider1.getValue() <= 1000 && mpe.messageInstance.getMessageSentEvents().size() == 1 ) {
                        
                        final MessageSentEvent mse = mpe.messageInstance.getMessageSentEvents().get( 0 );
                        final double xEnd = (double)width * normalizedStart;
                        final double yEnd = 20 + 35 * i;
                        
                        final long relativeMessageStart = mse.timestamp - profilingData.applicationStart();
                        final double normalizedMessageStart = (double)(relativeMessageStart-(long)detailsScrollBar.getValue()*1000000l)/(double)((long)jSlider1.getValue() * 1000000l);
                        
                        final double xStart = (double)width * normalizedMessageStart;
                        final double yStart = 20 + 35 * profilingData.getActorInstances().indexOf( mse.sender );
                        
                        final Line2D messageLine = new Line2D.Double(xStart, yStart, xEnd, yEnd);
                        g2.draw( messageLine );
                    }
                    g2.setColor( Color.BLACK );
                }
                
            }
            
            int i = 0;
            final Iterator< MessageType > it = messageTypes.iterator();
            final int height = this.getSize().height;
            while( it.hasNext() ) {
                final MessageType mt = it.next();
                final int h = mt.hashCode();
                g2.setColor( new Color( (h >> 16) & 0xff, (h >> 8) & 0xff, h & 0xff ) );
                g2.drawString( mt.shortType, 10, height - 10 - 15 * i );
                ++i;
            }
            g2.setColor( Color.BLACK );
        }
    }
    
    public CommunicationDetailsTopComponent() {
        this.profilingData = ProfilingData.getLoadedProfilingData();
        initComponents();
        setName(Bundle.CTL_CommunicationDetailsTopComponent());
        setToolTipText(Bundle.HINT_CommunicationDetailsTopComponent());
        
        this.jSlider1.setMinimum( 1 );
        this.jSlider1.setMaximum( (int)(this.profilingData.applicationRunTime()/1000000) );
        this.jSlider1.setValue( (int)(this.profilingData.applicationRunTime()/1000000) );
        this.timeResolutionTextField.setText( ""+ (int)(this.profilingData.applicationRunTime()/1000000) );
        this.detailsScrollBar.setMinimum( 0 );
        this.detailsScrollBar.setMaximum( (int)(this.profilingData.applicationRunTime()/1000000) );
        this.detailsScrollBar.setVisibleAmount( (int)(this.profilingData.applicationRunTime()/1000000) );
        
        this.actorIdsPanel.setLayout( new BorderLayout() );
        final NamesDrawPanel namesDrawPanel = new NamesDrawPanel();
        this.actorIdsPanel.add( namesDrawPanel, BorderLayout.CENTER );
        
        
        this.communicationDetailsPanel.setLayout( new BorderLayout() );
        final CommunicationsDetailsDrawPanel communicationsDetailsDrawPanel = new CommunicationsDetailsDrawPanel();
        this.communicationDetailsPanel.add( communicationsDetailsDrawPanel, BorderLayout.CENTER );
        
        
       
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detailsScrollBar = new javax.swing.JScrollBar();
        communicationDetailsSplitPane = new javax.swing.JSplitPane();
        actorIdsPanel = new javax.swing.JPanel();
        communicationDetailsPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        timeResolutionTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        detailsScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        detailsScrollBar.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                detailsScrollBarAdjustmentValueChanged(evt);
            }
        });

        communicationDetailsSplitPane.setDividerLocation(180);

        javax.swing.GroupLayout actorIdsPanelLayout = new javax.swing.GroupLayout(actorIdsPanel);
        actorIdsPanel.setLayout(actorIdsPanelLayout);
        actorIdsPanelLayout.setHorizontalGroup(
            actorIdsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 179, Short.MAX_VALUE)
        );
        actorIdsPanelLayout.setVerticalGroup(
            actorIdsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 419, Short.MAX_VALUE)
        );

        communicationDetailsSplitPane.setLeftComponent(actorIdsPanel);

        javax.swing.GroupLayout communicationDetailsPanelLayout = new javax.swing.GroupLayout(communicationDetailsPanel);
        communicationDetailsPanel.setLayout(communicationDetailsPanelLayout);
        communicationDetailsPanelLayout.setHorizontalGroup(
            communicationDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 351, Short.MAX_VALUE)
        );
        communicationDetailsPanelLayout.setVerticalGroup(
            communicationDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 419, Short.MAX_VALUE)
        );

        communicationDetailsSplitPane.setRightComponent(communicationDetailsPanel);

        jSlider1.setValue(0);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        timeResolutionTextField.setText(org.openide.util.NbBundle.getMessage(CommunicationDetailsTopComponent.class, "CommunicationDetailsTopComponent.timeResolutionTextField.text")); // NOI18N
        timeResolutionTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeResolutionTextFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CommunicationDetailsTopComponent.class, "CommunicationDetailsTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(timeResolutionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeResolutionTextField)
                        .addComponent(jLabel1))
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(43, 43, 43))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(detailsScrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(communicationDetailsSplitPane, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(communicationDetailsSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(detailsScrollBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        this.timeResolutionTextField.setText( ""+ this.jSlider1.getValue() );
        this.detailsScrollBar.setVisibleAmount( this.jSlider1.getValue() );
        this.communicationDetailsPanel.repaint();
    }//GEN-LAST:event_jSlider1StateChanged

    private void detailsScrollBarAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_detailsScrollBarAdjustmentValueChanged
        this.communicationDetailsPanel.repaint();
    }//GEN-LAST:event_detailsScrollBarAdjustmentValueChanged

    private void timeResolutionTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeResolutionTextFieldActionPerformed
        try {
            final int resolution = Integer.parseInt( this.timeResolutionTextField.getText() );
            this.jSlider1.setValue( resolution );
        } catch( final NumberFormatException e ) {
            
        }
    }//GEN-LAST:event_timeResolutionTextFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actorIdsPanel;
    private javax.swing.JPanel communicationDetailsPanel;
    private javax.swing.JSplitPane communicationDetailsSplitPane;
    private javax.swing.JScrollBar detailsScrollBar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextField timeResolutionTextField;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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
}
