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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.vmd.VMDNodeAnchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.ImmutableTupel;

/**
 *
 * @author Stephan Rehfeld
 */
public class ActorInstanceGraphScene extends GraphScene< ActorInstance, CommunicationData > {

    private final LayerWidget mainLayer;
    private final LayerWidget connectionLayer;
    private final ActorsInfoTopComponent parentWindow;
    private final Map< Widget, VMDNodeAnchor > widgetToAnchorMap;
    private final Map< ActorInstance, Double > computationScaleFactors;
    private final Map< ImmutableTupel< ActorInstance, ActorInstance >, Integer > communicationScaleFactors;
    
    ActorInstanceGraphScene( final ActorsInfoTopComponent parentWindow, final Map< ActorInstance, Double > computationScaleFactors, final Map< ImmutableTupel< ActorInstance, ActorInstance >, Integer > communicationScaleFactors ) {
        this.mainLayer = new LayerWidget( this );
        this.addChild( this.mainLayer );
        this.connectionLayer = new LayerWidget( this );
        this.addChild( this.connectionLayer );
        this.getActions().addAction( this.createSelectAction() );
        this.getActions().addAction( ActionFactory.createCenteredZoomAction( 1.1 ) );
        this.getActions().addAction( ActionFactory.createPanAction() );
        this.parentWindow = parentWindow;
        this.widgetToAnchorMap = new HashMap<>();
        this.computationScaleFactors = computationScaleFactors;
        this.communicationScaleFactors = communicationScaleFactors;
    }
    
    @Override
    protected Widget attachNodeWidget( final ActorInstance node ) {
        final ScalableIconNodeWidget widget = new ScalableIconNodeWidget(this, this.computationScaleFactors.get( node ) );
        widget.setImage( ImageUtilities.loadImage( "simx/profiler/info/application/actor.png" ));    
        widget.setLabel( node.shortId + " (" + node.type.shortTypeName + ")" );
        
        final WidgetAction.Chain actions = widget.getActions ();
        actions.addAction( ActionFactory.createMoveAction() );
        actions.addAction( createObjectHoverAction() );
        actions.addAction( createSelectAction() );   
        actions.addAction( ActionFactory.createPopupMenuAction( (final Widget widget1, Point point) -> {
            final JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem( "Start latency discovery" );
            menuItem.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final TopComponent latencyWindow = WindowManager.getDefault().findTopComponent( "LatencyDiscoveryTopComponent" );
                    latencyWindow.open();
                    parentWindow.setLatencyDiscoveryStartInstance((ActorInstance) findObject(widget1));
                }
            });
            menuItem.setText( "Start latency discovery" );
            popupMenu.add( menuItem );
            menuItem = new JMenuItem( "Start consistency discovery" );
            menuItem.setAction( new AbstractAction() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    final Set<?> selected = getSelectedObjects();
                    final Set< ActorInstance > actorInstances = new HashSet<>();
                    selected.stream().filter((o) -> ( o instanceof ActorInstance )).forEach((o) -> {
                        actorInstances.add( (ActorInstance)o );
                    });
                    parentWindow.setConsistencyDiscoveryInstances( actorInstances );
                }
                
            });
            menuItem.setText( "Start consistency discovery" );
            popupMenu.add( menuItem );
            return popupMenu;
        }));
        mainLayer.addChild(widget);
        final VMDNodeAnchor anchor = new VMDNodeAnchor( widget );
        this.widgetToAnchorMap.put( widget, anchor );
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget( final CommunicationData edge  ) {
        final ConnectionWidget widget = new ConnectionWidget (this);
        widget.setTargetAnchorShape( AnchorShapeFactory.createTriangleAnchorShape( 10 * this.communicationScaleFactors.get( edge.key ), true, false ));
        widget.setStroke( new BasicStroke( this.communicationScaleFactors.get( edge.key ) ) );
        final WidgetAction.Chain actions = widget.getActions ();
        actions.addAction( createObjectHoverAction() );
        actions.addAction( createSelectAction() );
        
        //widget.setRouter( RouterFactory.createOrthogonalSearchRouter( this.mainLayer, this.connectionLayer ) );
        
        connectionLayer.addChild( widget );
        return widget;
    }

    @Override
    protected void attachEdgeSourceAnchor( final CommunicationData edge, final ActorInstance oldSourceNode, final ActorInstance newSourceNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget sourceNodeWidget = findWidget( newSourceNode );
        final Anchor sourceAnchor = this.widgetToAnchorMap.get( sourceNodeWidget );
        edgeWidget.setSourceAnchor( sourceAnchor ); 
    }

    @Override
    protected void attachEdgeTargetAnchor( final CommunicationData edge, final ActorInstance oldTargetNode, final ActorInstance newTargetNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget targetNodeWidget = findWidget( newTargetNode );
        final Anchor targetAnchor = this.widgetToAnchorMap.get( targetNodeWidget );
        edgeWidget.setTargetAnchor( targetAnchor );
    }
    
}
