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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.MessageType;

/**
 *
 * @author Stephan Rehfeld
 */
public class LatencyDiscoveryGraphScene extends GraphScene< LatencyDiscoveryNode, Communication > {

    private final LayerWidget mainLayer;
    private final LayerWidget connectionLayer;
    
    private final List< LatencyDiscoveryNode > orderedNodes;
    private final List< Communication > orderedEdges;
    
    private final LatencyDiscoveryGraphScene self = this;
    
    LatencyDiscoveryGraphScene() {
        this.mainLayer = new LayerWidget( this );
        addChild( this.mainLayer );
        this.connectionLayer = new LayerWidget( this );
        addChild( this.connectionLayer );
        this.getActions().addAction( this.createSelectAction() );
        this.getActions().addAction( ActionFactory.createCenteredZoomAction( 1.1 ) );
        this.getActions().addAction( ActionFactory.createPanAction() );
        
        this.orderedNodes = new ArrayList<>();
        this.orderedEdges = new ArrayList<>();
    }
    
    @Override
    protected Widget attachNodeWidget( final LatencyDiscoveryNode node ) {
        
        final IconNodeWidget widget = new IconNodeWidget(this);
        widget.setImage( ImageUtilities.loadImage( "simx/profiler/discovery/latency/actor.png" ) );      
        widget.setLabel( node.actorInstance.shortId + " (" + node.actorInstance.type.shortTypeName + ")" );
        
        final ImageWidget mailboxWidget = new ImageWidget( this );
        mailboxWidget.setImage( ImageUtilities.loadImage( "simx/profiler/discovery/latency/mailbox.png" ) );
        mailboxWidget.setPreferredLocation( new Point( 40, 39 ) );    
        widget.getImageWidget().addChild( mailboxWidget );
        
        final ImageWidget messageWidget = new ImageWidget( this );
        messageWidget.setImage( ImageUtilities.loadImage( "simx/profiler/discovery/latency/message.png" ) );
        messageWidget.setPaintAsDisabled(true);
        messageWidget.setPreferredLocation( new Point( 75, 45 ) );    
        widget.getImageWidget().addChild( messageWidget );
       
        
        final WidgetAction.Chain actions = widget.getActions ();
              
        actions.addAction( ActionFactory.createMoveAction() );
        actions.addAction( createObjectHoverAction () );
        actions.addAction( createSelectAction () );  
        
        
        actions.addAction( ActionFactory.createPopupMenuAction( (final Widget widget1, Point point) -> {
            final JPopupMenu popupMenu = new JPopupMenu();
            final JMenu setSimulationLoopMessageMenu = new JMenu( "Set Simulation Loop Message" );
            popupMenu.add( setSimulationLoopMessageMenu );
            for (final Map.Entry< MessageType, Long > d : node.actorInstance.processedMessagesStatistic().entrySet()) {
                final JMenuItem messageItem = new JMenuItem( d.getKey().shortType );
                messageItem.setAction(new AbstractAction() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final ImageWidget imageWidget = (ImageWidget) ((IconNodeWidget) widget1).getImageWidget().getChildren().get(1);
                        imageWidget.setPaintAsDisabled( false );
                        System.out.println( d.getKey() );
                        node.setSimulationLoopMessageType( d.getKey() );
                        self.validate();
                    }                     
                });
                messageItem.setText( d.getKey().shortType );
                setSimulationLoopMessageMenu.add( messageItem );
            }
            final JMenu addAdjacentMenu = new JMenu( "Add Adjacent" );
            popupMenu.add( addAdjacentMenu );
            node.actorInstance.getReceiverStatistics().entrySet().stream().map((a) -> {
                final JMenu instanceMenu = new JMenu( a.getKey().shortId + " (" + a.getKey().type.shortTypeName + ")" );
                a.getValue().entrySet().stream().map((e) -> {
                    final JMenuItem messageItem = new JMenuItem( e.getKey().shortType );
                    messageItem.setAction(new AbstractAction() {
                        @Override
                        public void actionPerformed(final ActionEvent event1) {
                            final LatencyDiscoveryNode adjacent = new LatencyDiscoveryNode( a.getKey() );
                            addNode( adjacent );
                            addAdjacentMenu.setEnabled( false );
                            popupMenu.remove( addAdjacentMenu );
                            final Communication communication = new Communication( e.getKey() );
                            addEdge( communication );
                            setEdgeSource( communication, node );
                            setEdgeTarget( communication, adjacent );
                            self.validate();
                        }
                    });
                    messageItem.setText( e.getKey().shortType );
                    return messageItem;
                }).forEach((messageItem) -> {
                    instanceMenu.add( messageItem );
                });
                return instanceMenu;
            }).forEach((instanceMenu) -> {
                addAdjacentMenu.add( instanceMenu );
            });
            return popupMenu;
        }));
        
        this.orderedNodes.add( node );
        mainLayer.addChild(widget);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget( final Communication edge ) {
        final ConnectionWidget widget = new ConnectionWidget (this);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);

        final WidgetAction.Chain actions = widget.getActions ();
        this.orderedEdges.add( edge );
        connectionLayer.addChild (widget);
        return widget;
    }

    @Override
    protected void attachEdgeSourceAnchor( final Communication edge, final LatencyDiscoveryNode oldSourceNode, final LatencyDiscoveryNode newSourceNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget sourceNodeWidget = findWidget( newSourceNode );
        final Anchor sourceAnchor = AnchorFactory.createRectangularAnchor( sourceNodeWidget );
        edgeWidget.setSourceAnchor( sourceAnchor ); 
    }

    @Override
    protected void attachEdgeTargetAnchor( final Communication edge, final LatencyDiscoveryNode oldTargetNode, final LatencyDiscoveryNode newTargetNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );  
        final Widget targetNodeWidget = ((IconNodeWidget)findWidget( newTargetNode )).getImageWidget().getChildren().get( 0 );
        final Anchor targetAnchor = AnchorFactory.createRectangularAnchor( targetNodeWidget );
        edgeWidget.setTargetAnchor( targetAnchor );
    }

    void readdLayer() {
        this.mainLayer.removeChildren();
        this.connectionLayer.removeChildren();
        addChild( this.mainLayer );
        addChild( this.connectionLayer );
        this.orderedNodes.clear();
        this.orderedEdges.clear();
    }

    public List<LatencyDiscoveryNode> getOrderedNodes() {
        return orderedNodes;
    }

    public List<Communication> getOrderedEdges() {
        return orderedEdges;
    }
    
    
    
}
