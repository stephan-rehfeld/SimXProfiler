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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.GrayFilter;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;
import org.openide.ErrorManager;

/**
 *
 * @author Stephan Rehfeld
 */
public class ScalableImageWidget extends ImageWidget {

    private final double scale;
    
    private Image disabledImage;
    
    public ScalableImageWidget( final Scene scene, final double scale ) {
        super(scene);
        this.scale = scale;
    }
    
    @Override
    protected void paintWidget() {
        if(this.getImage() == null)
            return;
        final Graphics2D gr = getGraphics ();
  
        if( this.isPaintAsDisabled() ) {
            if( this.disabledImage == null ) {
                disabledImage = GrayFilter.createDisabledImage( super.getImage() );
                final MediaTracker tracker = new MediaTracker( this.getScene().getView () );
                tracker.addImage(disabledImage, 0) ;
                try {
                    tracker.waitForAll ();
                } catch( final InterruptedException e ) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            final Rectangle bounds = this.getBounds();
            gr.drawImage( this.disabledImage, bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, this.getImage().getWidth( null ), this.getImage().getHeight( null ), null );         
        } else {
            final Rectangle bounds = this.getBounds();
            gr.drawImage( this.getImage(), bounds.x, bounds.y, bounds.width, bounds.height, 0, 0, this.getImage().getWidth( null ), this.getImage().getHeight( null ), null );         
        }
    }
    
    @Override
    protected Rectangle calculateClientArea () {
        if( this.getImage() != null) {
            final Rectangle bounds = new Rectangle (0, 0, this.getImage().getWidth( null ), this.getImage().getHeight( null ) );
            final Point center = new Point( bounds.x + bounds.width / 2, bounds.y + bounds.height / 2 );
            final Dimension scaledSize = new Dimension( (int)(bounds.width * this.scale), (int)(bounds.height * this.scale) );
            final Point upperLeftCorner = new Point( center.x - scaledSize.width / 2, center.y - scaledSize.height / 2 );
            final Rectangle scaledBounds = new Rectangle( upperLeftCorner, scaledSize );
            return scaledBounds;
        }
        return super.calculateClientArea ();
    }
    
}
