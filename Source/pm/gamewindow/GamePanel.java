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

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/*
 * Subclass of JPanel: To be added to a JFrame.
 * Creates a double buffer (image) to draw onto.
 * The double buffer is drawn completely to the screen upon repaint.
 * To draw to the panel, get the double buffer's graphics context with getDBGraphics() and use g.drawImage() etc.
 * To paint the double buffer to screen, call panel.repaint().
 *
 * C. Pothmann
 * Started Nov. 22, 2014
 * Current Aug. 18, 2016
 */
public class GamePanel extends JPanel
{
    private BufferedImage doublebuffer;
    private Graphics2D    dbgraphics;

    /*
     * Construktor: sets size of the panel and initializes double buffer
     */
    public GamePanel(int width, int height)
    {
        doublebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        dbgraphics   = doublebuffer.createGraphics();
        setPreferredSize(new Dimension(width, height));
        setDoubleBuffered(false);
    }

    public void setSize(int width, int height)
    {
        setPreferredSize(new Dimension(width, height));
        doublebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        dbgraphics   = doublebuffer.createGraphics();
    }
    
    /*
     * Returns a graphics context to draw onto the double buffer
     */
    public Graphics2D getDBGraphics()
    {
        return dbgraphics;
    }

    /*
     * Invoked upon the panel's repaint event.
     * Draws the double buffer to the screen.
     * Call panel.repaint() to get this started.
     */
    public void paintComponent(Graphics graphics)
    {
        graphics.drawImage(doublebuffer, 0, 0, null);
    }
}
