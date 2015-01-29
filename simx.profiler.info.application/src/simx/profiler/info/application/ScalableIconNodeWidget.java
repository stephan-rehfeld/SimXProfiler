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

import java.awt.Image;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Stephan Rehfeld
 */
public class ScalableIconNodeWidget extends Widget {

    private final double scale;
    
    private final ScalableImageWidget scalableImageWidget;
    private final LabelWidget labelWidget;
    
    public ScalableIconNodeWidget( final Scene scene, final double scale ) {
        super(scene);
        this.scale = 1.0;
        final LookFeel lookFeel = getScene ().getLookFeel ();
        this.setLayout( LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.CENTER, - lookFeel.getMargin () + 1));
        this.scalableImageWidget = new ScalableImageWidget( scene, scale );
        this.addChild( this.scalableImageWidget );
        
        this.labelWidget = new LabelWidget( scene );
        this.labelWidget.setFont( scene.getDefaultFont().deriveFont( 14.0f ) );
        this.addChild( this.labelWidget );
        
        this.setState( ObjectState.createNormal() );
       
    }

    @Override
    public void notifyStateChanged( final ObjectState previousState, final ObjectState state ) {
        final LookFeel lookFeel = getScene ().getLookFeel ();
        this.labelWidget.setBorder (lookFeel.getBorder (state));
        this.labelWidget.setForeground (lookFeel.getForeground (state));
    }
    
    public final void setImage( final Image image ) {
        this.scalableImageWidget.setImage (image);
    }
    
    public final void setLabel( final String label ) {
        labelWidget.setLabel( label );
    }
    
    public final ScalableImageWidget getScalableImageWidget () {
        return this.scalableImageWidget;
    }
    
    public final LabelWidget getLabelWidget() {
        return this.labelWidget;
    }
        
}
