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
 * Event handler for keyboard events.
 * Use boolean getKeyPressed(int keyCode) to determine if a key is currently being held down.
 * Use frame.addKeyListener(KeyListener) to add this handler to a JFrame werden.
 * The individual key codes (VK_A, VK_B, ...) are listed at
 * http://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html
 */
public class GameKeyListener
implements KeyListener
{
    private boolean[] keyPressedTable;
    private boolean[] keyDownTable;

    public GameKeyListener()
    {
        keyPressedTable = new boolean[128];
        keyDownTable    = new boolean[128];
        for (int i = 0; i < 128; i++) 
        {
            keyPressedTable[i] = false;
            keyDownTable[i]    = false;
        }
    }
    
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < 128)
        {
            keyPressedTable[keyCode] = true;
            keyDownTable[keyCode] = true;
        }
    }

    public void keyReleased(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < 128)
        {
            keyDownTable[keyCode] = false;
        }        
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public boolean isKeyPressed(int keyCode)
    {
        if (keyCode >= 0 && keyCode < 128 && keyPressedTable[keyCode] == true)
        {
            keyPressedTable[keyCode] = false;
            return true;
        }
        return false;
    }
    
    public boolean isKeyDown(int keyCode)
    {
        if (keyCode >= 0 && keyCode < 128 && keyDownTable[keyCode] == true)
        {
            return true;
        }
        return false;
    }
}