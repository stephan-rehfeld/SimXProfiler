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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.vmd.VMDNodeAnchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import simx.profiler.model.ActorType;
import simx.profiler.model.ImmutableTupel;

/**
 *
 * @author Stephan Rehfeld
 */
public class ActorTypeGraphScene extends GraphScene< ActorType, CommunicationData > {

    private final LayerWidget mainLayer;
    private final LayerWidget connectionLayer;
    private final Map< Widget, VMDNodeAnchor > widgetToAnchorMap;
    private final Map< ActorType, Double > computationScaleFactors;
    private final Map< ImmutableTupel< ActorType, ActorType >, Integer > communicationScaleFactors;
            
    ActorTypeGraphScene( final ActorsInfoTopComponent parentWindow, final Map< ActorType, Double > computationScaleFactors, final Map< ImmutableTupel< ActorType, ActorType >, Integer > communicationScaleFactors  ) {
        this.mainLayer = new LayerWidget( this );
        addChild( this.mainLayer );
        this.connectionLayer = new LayerWidget( this );
        addChild( this.connectionLayer );
        this.getActions().addAction( this.createSelectAction() );
        this.getActions().addAction( ActionFactory.createCenteredZoomAction( 1.1 ) );
        this.getActions().addAction( ActionFactory.createPanAction() );
        this.widgetToAnchorMap = new HashMap<>();
        this.computationScaleFactors = computationScaleFactors;
        this.communicationScaleFactors = communicationScaleFactors;
    }
    
    @Override
    protected Widget attachNodeWidget( final ActorType node ) {
        final ScalableIconNodeWidget widget = new ScalableIconNodeWidget(this, this.computationScaleFactors.get( node ) );
        widget.setImage( ImageUtilities.loadImage( "simx/profiler/info/application/actor.png" ));
        widget.setLabel( node.shortTypeName );
        widget.getActions().addAction(ActionFactory.createMoveAction());
        mainLayer.addChild(widget);
        final WidgetAction.Chain actions = widget.getActions ();
        actions.addAction (createObjectHoverAction ());
        actions.addAction (createSelectAction ());
        final VMDNodeAnchor anchor = new VMDNodeAnchor( widget );
        this.widgetToAnchorMap.put( widget, anchor );
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget( final CommunicationData edge  ) {
        final ConnectionWidget widget = new ConnectionWidget(this);
        widget.setTargetAnchorShape( AnchorShapeFactory.createTriangleAnchorShape( 10 * this.communicationScaleFactors.get( edge.key ), true, false ));
        widget.setStroke( new BasicStroke( this.communicationScaleFactors.get( edge.key ) ) );
        final WidgetAction.Chain actions = widget.getActions();
        actions.addAction (createObjectHoverAction());
        actions.addAction (createSelectAction());

        //widget.setRouter( RouterFactory.createOrthogonalSearchRouter( this.mainLayer, this.connectionLayer ) );
    
        connectionLayer.addChild (widget);
        return widget;
    }

    @Override
    protected void attachEdgeSourceAnchor( final CommunicationData edge, final ActorType oldSourceNode, final ActorType newSourceNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget sourceNodeWidget = findWidget( newSourceNode );
        final Anchor sourceAnchor = this.widgetToAnchorMap.get( sourceNodeWidget );
        edgeWidget.setSourceAnchor( sourceAnchor ); 
       
    }

    @Override
    protected void attachEdgeTargetAnchor( final CommunicationData edge, final ActorType oldTargetNode, final ActorType newTargetNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget targetNodeWidget = findWidget( newTargetNode );
        final Anchor targetAnchor = this.widgetToAnchorMap.get( targetNodeWidget );
        edgeWidget.setTargetAnchor( targetAnchor );
    }
    
}
