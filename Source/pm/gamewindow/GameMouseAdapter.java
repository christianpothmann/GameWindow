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

import java.awt.event.*;

/*
 * Handler for mouse events (motion, clicks, mouse wheel).
 * Remembers events to be used when needed.
 * Thus, the game loop can check for events when the next frame is to be drawn.
 *
 * Christian Pothmann, 2014.12.03
 */
public class GameMouseAdapter extends MouseAdapter
{
    private int x, y;
    private boolean button1, button2, button3;
    private int wheelNotches;
    
    public GameMouseAdapter()
    {
        button1 = false;
        button2 = false;
		button3 = false;
        wheelNotches = 0;
    }
    
    /*
     * Overrides MouseAdapter event handlers.
     * These methods will be called automatically upon mouse events.
     */
    public void mouseMoved(MouseEvent e)
    {
        x = e.getX();
        y = e.getY();
    }
    
    public void mousePressed(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            button1 = true;
        }
        if (e.getButton() == MouseEvent.BUTTON2)
        {
            button2 = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3)
        {
            button3 = true;
        }
    }
    
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int notches = e.getWheelRotation();
        wheelNotches += notches;
    }

    /*
     * Methods to be used by Game engine.
     */ 
    public int getX()
    {
        return x;        
    }
    
    public int getY()
    {
        return y;
    }    
    
    public boolean button1Pressed()
    {
        if (button1 == true)
        {
            button1 = false;
            return true;
        }
        return false;
    }
    
    public boolean button2Pressed()
    {
        if (button2 == true)
        {
            button2 = false;
            return true;
        }
        return false;
    }

    public boolean button3Pressed()
    {
        if (button3 == true)
        {
            button3 = false;
            return true;
        }
        return false;
    }

    /*
     * Returns the number of "notches" that the mouse wheel has been rotated since the last call of this method.
     * Positive value: mouse wheel was rotated towards the user
     * Negative value: mouse wheel was rotated away from the user
     */
    public int mouseWheel()
    {
        int notches = wheelNotches;
        wheelNotches = 0;
        return notches;
    }
}