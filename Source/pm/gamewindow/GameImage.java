/*
 * Copyright 2021 Christian Pothmann
 * 
 * This file is part of the GameWindow library. The GameWindow library is free software: 
 * you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * The GameWindow library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with the GameWindow library.  
 * If not, see <https://www.gnu.org/licenses/>.
 */

package pm.gamewindow;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.geom.*;

/*
 * Represents an image to be drawn to a GameWindow.
 *   
 * Image can be scaled and rotated.
 *    Scaling is done on basis of the original, rotation based on the scaled image.
 *    The original BufferedImages is kept for further scaling.
 *    The scaled BufferedImage is kept for further rotations.
 *    The rotated BufferedImages is kept until the next rotation.
 *    
 * To use rotation, the image must be made rotatable,
 *    meaning the original will be enlarged to support any rotation without clipping.
 *    The new image will be a square, it's width being the diagonal of the original.
 *    For rotatable images, a bounding box will automatically be calculated for collision detection.
 *    
 * Copies can be made that use the same original BufferedImage.
 *    This may be useful when several sprites use the same image but different scales or rotations.
 *    In that case, the image has to be drawn to screen before the rotation is changed for the next sprite (TRICKY!!!)
 *    If a copy with its own BufferedImage is needed, it must be loaded again from file.
 * 
 * C. Pothmann
 * Started: 22. Nov. 2014
 * Current: 31. Mar. 2019
 */
public class GameImage
{
    BufferedImage imgOriginal;   // image as loaded from file; may point to buffer of another GameImage object
    BufferedImage imgScaled;     // scaled copy of the original; points to original if image has not been scaled
    BufferedImage imgRotated;    // rotated copy of the scaled image; points to scaled image if there is no rotation
                                 // this copy is finally returned from getImage() to be drawn onto screen
    double scaleX, scaleY;       // scaling factors in x- and y-direction; a value of 1.0 means no scaling
    boolean rotatable;           // when set true, the original image is enlarged to support any rotation without clipping; cannot be undone
    double rotDegrees;           // angle of rotation, measured in degrees
    int boundX, boundY;          // bounding box for collision detection; if not rotatable, it matches the original image
    int boundWidth, boundHeight; // else, it is a square that contains the orginal image and is centered in the enlarged image
                                 // (the bounding box may be set to different values)

    /*
     * Constructor: loads image from file
     */
    public GameImage(String filename)
    {
        try 
        {
            imgOriginal = ImageIO.read(new File(filename));
        }
        catch (Exception e)
        {
        }
        scaleX      = 1.0;
        scaleY      = 1.0;
        rotatable   = false;
        rotDegrees  = 0.0;
        boundWidth  = imgOriginal.getWidth();
        boundHeight = imgOriginal.getHeight();
        imgScaled   = imgOriginal;
        imgRotated  = null;
    }

    /*
     * Copy constructor
     * The copy will use the same BufferedImage, also the same scale and rotation
     */
    public GameImage(GameImage pImage)
    {
        imgOriginal = pImage.imgOriginal;
        imgScaled   = pImage.imgScaled;
        imgRotated  = pImage.imgRotated;
        scaleX      = pImage.scaleX;
        scaleY      = pImage.scaleY;
        rotatable   = pImage.rotatable;
        rotDegrees  = pImage.rotDegrees;
        boundX      = pImage.boundX;
        boundY      = pImage.boundX;;
        boundWidth  = pImage.boundWidth;
        boundHeight = pImage.boundHeight;
    }

    /*
     * Returns width and height of the scaled image.
     * The rotated image will always be the same size as the scaled one.
     */
    public int getWidth()
    {
        return imgScaled.getWidth();
    }

    public int getHeight()
    {
        return imgScaled.getHeight();
    }

    /*
     * Returns width and height of the unscaled image.
     * If the image was made rotatable, the original is enlarged to a square to support rotation without clipping.
     */
    public int getWidthOriginal()
    {
        return imgOriginal.getWidth();
    }

    public int getHeightOriginal()
    {
        return imgOriginal.getHeight();
    }

    /*
     * Provides access to the BufferedImage (only needed for the GameWindow.drawImage() method).
     * -> no need to use this directly
     */
    public BufferedImage getBufferedImage()
    {
        if (rotatable == false)
        {
            return imgScaled;
        }
        else
        {
            return imgRotated;
        }
    }    

    /*
     * Returns scale factors for width and height.
     * Note that these are set to 1.0 if they are very close to 1.0.
     */
    public double getScaleX()
    {
        return scaleX;
    }

    public double getScaleY()
    {
        return scaleY;
    }

    /*
     * Returns the scaling factor of the area of the scaled image in relation to the area of the original.
     */
    public double getScale()
    {
        if (scaleX == scaleY)
        {
            return scaleX * scaleX;
        }
        else
        {
            return (double)(imgScaled.getWidth() * imgScaled.getHeight()) / (imgOriginal.getWidth() / imgOriginal.getHeight());
        }
    }

    /*
     * Scales the image by factor pScale (1.0 = original, negative = no change)
     * The factor specifies the scaled size of the image area, not both width and height
     * If the image was already rotated, the rotation is performed again on the scaled image.
     */
    public void setScale(double pScale)
    {
        if (pScale <= 0.0) return;
        setScale(Math.sqrt(pScale), Math.sqrt(pScale));
    }

    /*
     * Scales the images by factors pScaleX and pScaleY in each dimension (1.0 = original, negative = no change)
     * Any Rotation is performed again on the scaled image.
     */
    public void setScale(double pScaleX, double pScaleY)
    {
        // negative scales not permitted
        if (pScaleX <= 0.0 || pScaleY <= 0.0) return;

        // values close to 1.0 are set to 1.0 (to prevent unnecessary scaling due to rounding errors)
        if (pScaleX > 0.9999 && pScaleX < 1.0001) { pScaleX = 1.0; }
        if (pScaleY > 0.9999 && pScaleY < 1.0001) { pScaleY = 1.0; }
        
        // rotatable images must remain squares
        if (rotatable == true && pScaleX != pScaleY) { return; }

        // set scale attributes
        scaleX = pScaleX;
        scaleY = pScaleY;
        
        // scale image
        if (scaleX == 1.0 && scaleY == 1.0)
        {
            imgScaled = imgOriginal;
        }
        else
        {
            AffineTransform afTr = new AffineTransform();
            afTr.scale(scaleX, scaleY);
            AffineTransformOp afTrOp = new AffineTransformOp(afTr, AffineTransformOp.TYPE_BILINEAR);
            imgScaled = afTrOp.filter(imgOriginal, null);
        }

        // if image has been rotated, calculate rotation on scaled image
        // and calculate bounding box
        if (rotatable == true)
        {
            setRotation(rotDegrees);
            // bounding box is calculated as if the original image had been a square, before being enlarged for rotation
            // margin = (width - width/sqrt(2)) / 2
            int margin = (int)(0.1464 * imgScaled.getWidth());
            setBoundingBox(margin, margin, imgScaled.getWidth() - 2*margin, imgScaled.getWidth() - 2*margin);
        }
        else
        {
            setBoundingBox(0, 0, imgScaled.getWidth(), imgScaled.getHeight());
        }
    }
    
    /*
     * Scales the images to the dimensions pWidth / pHeight.
     * If the images has been rotated, the rotation is performed again on the scaled image.
     */
    public void setScaleSize(int pWidth, int pHeight)
    {
        if (pWidth <= 0 || pHeight <= 0) return;

        double scx = (double)pWidth  / imgOriginal.getWidth();
        double scy = (double)pHeight / imgOriginal.getHeight();
        setScale(scx, scy);
    }        

    /*
     * Returns true if the GameImage has been made rotatable.
     */
    public boolean isRotatable()
    {
        return rotatable;
    }

    /*
     * When invoked, the original image is enlarged to a square that will support any rotation without clipping.
     * Sets the size of the bounding box to a square that will exactly contain the original image and is centered in the enlarged image.
     * Cannot be undone.
     */
    public void makeRotatable()
    {
        if (rotatable == true) { return; }
        
        int w = imgOriginal.getWidth();
        int h = imgOriginal.getHeight();
        int sizeSq, margin;
        int sqX, sqY;
        BufferedImage imgLarge;
        
        // set attribute
        rotatable = true;

        // size of square that contains the original image
        if (w > h) { sizeSq = w; }
        else       { sizeSq = h; }
        // calculate margin (left/right/top/bottom) for enlarged square
        // enlarged square size will be the diagonal of the original (removed: plus 1 extra for antialiasing, this may be useless)
        margin = (int)((Math.sqrt(w*w + h*h) - sizeSq) / 2);
        // center original within enlarged image
        sqX = margin + (sizeSq - w) / 2;
        sqY = margin + (sizeSq - h) / 2;

        // copy original image to enlarged image
        imgLarge = new BufferedImage(sizeSq + 2*margin, sizeSq + 2*margin, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = imgLarge.createGraphics();
        graphics.drawImage(imgOriginal, sqX, sqY, w, h, null);

        // replace original with enlarged image (original is lost unless there's another GameImage object using it)
        imgOriginal = imgLarge;
        // create scaled version; this will set both imgScaled and imgRotated
        // if image was streched before making it rotatable, the scale is reset to 1.0.
        if (scaleX != scaleY)
        {
            scaleX = 1.0;
            scaleY = 1.0;
        }
        setScale(scaleX, scaleY);
    }

    /*
     * Returns the rotation in degrees.
     * Example: a value of 90.0 means 90° rotated to the right.
     */
    public double getRotation()
    {
        return rotDegrees;
    }

    /*
     * Rotates the (scaled) image by pDegrees.
     * (Will do nothing if image has not been made rotatable.)
     * Keeps the orginal and the scaled image for further rotations.
     * Rotation pivot is the center of the image.
     */
    public void setRotation(double pDegrees)
    {
        setRotation(pDegrees, imgScaled.getWidth() / 2, imgScaled.getHeight() / 2);
    }

    /*
     * Rotates the image by pDegrees.
     * Rotation pivot is the specified offset from the top left corner of the image.
     * (Will do nothing if image has not been made rotatable.)
     */
    public void setRotation(double pDegrees, int pPivotX, int pPivotY)
    {
        if (rotatable == false) { return; }

        double rotRadians;
        rotDegrees = pDegrees;
        rotRadians = Math.toRadians(rotDegrees);
        
        if (rotDegrees == 0.0)
        {
            imgRotated = imgScaled;
        }
        else
        {
            if (imgRotated == null || imgRotated == imgScaled || (imgRotated.getWidth() != imgScaled.getWidth() && imgRotated.getHeight() != imgScaled.getHeight()))
            {
                imgRotated = new BufferedImage(imgScaled.getWidth(), imgScaled.getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            AffineTransform afTr = new AffineTransform();
            afTr.rotate(rotRadians, pPivotX, pPivotY);
            AffineTransformOp afTrOp = new AffineTransformOp(afTr, AffineTransformOp.TYPE_BILINEAR);
            // draw the rotation into imgRotated -> the size of the image remains the same
            // may cause a slight error in case the rotated image is smaller than the scaled image (only in 90° or 180° cases),
            //    and if the previous rotation was drawing into that small margin (bottom and left, 1 pixel)
            //    if this really occurs, then imgRotated would first have to be cleared, or a new imgRotated created each time.
            afTrOp.filter(imgScaled, imgRotated);
        }
    }

    /*
     * Returns x-/y-offset and width/height of the bounding box for collision detection.
     * Only useful for rotatable images (else it's the same as the scaled image).
     * Will be the size of the scaled image prior to enlarging the original to support rotation,
     *    if the original was a square; else assumes it was a square (then it will be somewhere between height and width of the original rectangle).
     */
    public int getBoundX()
    {
        return boundX;
    }

    public int getBoundY()
    {
        return boundY;
    }

    public int getBoundWidth()
    {
        return boundWidth;
    }

    public int getBoundHeight()
    {
        return boundHeight;
    }

    /*
     * Sets the bounding box to custom values.
     * Caution: Scaling the image will reset the bounding box automatically.
     */
    void setBoundingBox(int px, int py, int pw, int ph)
    {
        boundX      = px;
        boundY      = py;
        boundWidth  = pw;
        boundHeight = ph;
    }
}
