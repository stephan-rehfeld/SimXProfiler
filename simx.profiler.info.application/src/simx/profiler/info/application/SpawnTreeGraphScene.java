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

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;
import simx.profiler.model.ActorInstance;
import simx.profiler.model.ImmutableTupel;

/**
 *
 * @author Stephan Rehfeld
 */
public class SpawnTreeGraphScene extends GraphScene< ActorInstance, ImmutableTupel< ActorInstance, ActorInstance > >{

    private final LayerWidget mainLayer;
    private final LayerWidget connectionLayer;
    private final ActorsInfoTopComponent parentWindow;
    
    SpawnTreeGraphScene( final ActorsInfoTopComponent parentWindow ) {
        this.mainLayer = new LayerWidget( this );
        this.addChild( this.mainLayer );
        this.connectionLayer = new LayerWidget( this );
        this.addChild( this.connectionLayer );
        this.getActions().addAction( this.createSelectAction() );
        this.getActions().addAction( ActionFactory.createCenteredZoomAction( 1.1 ) );
        this.getActions().addAction( ActionFactory.createPanAction() );
        this.parentWindow = parentWindow;
    }
    
    @Override
    protected Widget attachNodeWidget( final ActorInstance node ) {
        final IconNodeWidget widget = new IconNodeWidget( this );
        widget.setImage( ImageUtilities.loadImage( "simx/profiler/info/application/actor.png" ));    
        widget.setLabel( node.shortId + " (" + node.type.shortTypeName + ")" );
        
        final WidgetAction.Chain actions = widget.getActions ();
        actions.addAction( ActionFactory.createMoveAction() );
        actions.addAction( createObjectHoverAction() );
        actions.addAction( createSelectAction() );   
        
        mainLayer.addChild( widget );
        
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget( final ImmutableTupel<ActorInstance, ActorInstance> e) {
        final ConnectionWidget widget = new ConnectionWidget (this);
        widget.setTargetAnchorShape( AnchorShape.TRIANGLE_FILLED );
        final WidgetAction.Chain actions = widget.getActions();
        actions.addAction(createObjectHoverAction() );
        actions.addAction(createSelectAction() );
        
        connectionLayer.addChild (widget);
        
        return widget;
        
    }

    @Override
    protected void attachEdgeSourceAnchor( final ImmutableTupel<ActorInstance, ActorInstance> edge, final ActorInstance oldSourceNode, final ActorInstance newSourceNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget sourceNodeWidget = findWidget( newSourceNode );
        final Anchor sourceAnchor = AnchorFactory.createRectangularAnchor( sourceNodeWidget );
        edgeWidget.setSourceAnchor( sourceAnchor ); 
    }

    @Override
    protected void attachEdgeTargetAnchor( final ImmutableTupel<ActorInstance, ActorInstance> edge, final ActorInstance oldTargetNode, final ActorInstance newTargetNode ) {
        final ConnectionWidget edgeWidget = (ConnectionWidget)findWidget( edge );
        final Widget targetNodeWidget = findWidget( newTargetNode );
        final Anchor targetAnchor = AnchorFactory.createRectangularAnchor( targetNodeWidget );
        edgeWidget.setTargetAnchor( targetAnchor );
    }
    
}
