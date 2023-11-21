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
import java.awt.event.*;
import javax.swing.*;

/*
 * Provides a JFrame with one JPanel (the drawing area) and methods to draw images.
 * The coordinate space is related to the drawing area (0/0 is the upper left corner).
 * The repaint of the JPanel is guided by a timer to make sure it will repeat after the same time interval,
 * independet of the speed of the computer or the number of images drawn.
 * Provides methods to process mouse and keyboard events.
 *
 * To use, repeat: 
 * - erase background with clear() or drawImage()
 * - process keyboard / mouse events
 * - draw sprites
 * - copy double buffer to screen with paintFrame()
 * 
 * Log:
 * - 2014.11.22: First version
 *   Overwrites the JPanel paint-Method to call the game's draw()-method
 *   Drawback: The Game class has to create the JFrame containing the panel, forcing the programmer to take care of all JFrame-related issues.
 * - 2016.08.18: Major refactoring
 *   GamePanel uses a double buffer; everything gets drawn to the DB, and the panels's paintComponent method is overwritten to draw the DB to the screen.
 *   This way, the game programmer doesn't have to take care of any JFrame programming.
 * - 2016.11.11: Added basic image rotation
 *   Unsolved: the image on file must have enough empty space around so the edges will not be clipped by rotation
 * - 2016.12.24: Added drawLine()
 * - 2019.03.19: Added wait()
 * - 2019.04.03: Improved image rotation and added scaling
 *   An image must be made rotatable, this will add transparent space around the image automatically
 * - 2019.04.08: Added fullscreen mode
 *
 * Todo:
 * - allow resizing of window (setResizable)
 * 
 * Ideas:
 * - Clipping region, e.g. not to draw on the HUD area (the HUD may also be re-painted each frame, but maybe that time can be saved)
 * - Mouse: double click event
 * - mouseInRectangle / mouseClickInRectangle (for buttons)
 * - Coordinate system independent of panel (class Viewport? or COS?) -> really depends on the game
 * - Optimize rendering images to double buffer (use Raster? HW-Acceleration?)
 * - Effects like blending one frame over to the other, fade-in/out
 */
public class GameWindow
{
    // Essential components
    private JFrame     frame;              // JFrame containing the JPanel
    private GamePanel  panel;              // JPanel taking all inner space of the JFrame. Used to draw onto.
    private GameKeyListener keyListener;   // Keyboard handler
    private GameMouseAdapter mouseAdapter; // Mouse handler
    private GameTimer  timer;              // Timer that waits a defined interval before the next repaint
    
    // Optional components
    private Font       font;               // Current font used for drawString()
    private Color      fontColor;          // Current font color
    private boolean    fullscreen;

    /*
     * Constructor: initialises JFrame, GamePanel and keyboard and mouse handlers
     * px / py set the JFrame's location, pPanelWidth and pPanelHeight the size of the GamePanel (the JFrame adjusts to this size).
     */
    public GameWindow(int px, int py, int pPanelWidth, int pPanelHeight, String pTitle)
    {
        // initialize JFrame
        frame = new JFrame();    
        frame.setTitle(pTitle);
        frame.setResizable(false);
        frame.setLocation(px, py);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        fullscreen = false;
    
        // add GamePanel to the JFrame
        panel = new GamePanel(pPanelWidth, pPanelHeight);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        // add keyboard and mouse handlers 
        keyListener  = new GameKeyListener();
        mouseAdapter = new GameMouseAdapter();
        frame.addKeyListener        (keyListener);
        panel.addMouseListener      (mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseWheelListener (mouseAdapter);
        
        // default font and font color
        font = new Font("Arial", Font.PLAIN, 12);
        fontColor = Color.BLACK;

        // initialize timer with default length of 25 ms per frame ( = 40 frames per second)
        timer = new GameTimer(25);
        timer.startFrame();
    }
    
    /*
     * The same constructor with default values for x/y = (50/50)
     */
    public GameWindow(int pPanelWidth, int pPanelHeight, String pTitle)
    {
        this(100, 100, pPanelWidth, pPanelHeight, pTitle);
    }

    /*
     * The GameWindow will cover the whole screen.
     * JFrame and GamePanel will be resized to the size of the screen.
     * If the GameWindow is already in fullscreen mode, nothing happens.
     */
    public void enterFullscreen()
    {
        if (fullscreen == true) return;
        fullscreen = true;

        // Disposing will remove the "native screen resource" and thus make the window un-displayable.
        // This is a requirement to making the frame undecorated, and thus to enter fullscreen mode.
        // It will also remove all components, so the panel needs to be added again.
        frame.dispose();
        frame.setExtendedState(Frame.MAXIMIZED_BOTH); 
        frame.setUndecorated(true);
        frame.setVisible(true);

        // re-add GamePanel to the JFrame
        panel.setSize(frame.getWidth(), frame.getHeight());
        frame.add(panel);

        // re-add keyboard and mouse handlers must be reset, otherwise they may still indicate events that are not there anymore
        keyListener  = new GameKeyListener();
        mouseAdapter = new GameMouseAdapter();
        frame.addKeyListener        (keyListener);
        panel.addMouseListener      (mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseWheelListener (mouseAdapter);        
    }

    /*
     * If the GameWindow is in fullscreen mode, it will return to a normal window,
     * Location and size must be given again (the frame's title will keep its old value)
     * If GameWindow was not in fullscreen mode, nothing happens.
     */
    public void leaveFullscreen(int px, int py, int pPanelWidth, int pPanelHeight)
    {
        if (fullscreen == false) return;
        fullscreen = false;

        // Reset the JFrame and the GamePanel
        frame.dispose();
        frame.setExtendedState(Frame.NORMAL); 
        frame.setUndecorated(false);
        frame.setResizable(false);
        frame.setLocation(px, py);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setFocusable(true);
    
        // re-add GamePanel to the JFrame
        // since pack() will *sometimes* alter the desired panel size, repeat until satisfactory 
        // (ugly, but for far I don't know any other solution, and it seems to work)
        // to prevent an endless loop, give up after 10 tries
        int c = 0;
        do 
        {
            panel.setSize(pPanelWidth, pPanelHeight);
            frame.add(panel);
            frame.pack();
            c++;
        } while ((panel.getWidth() != pPanelWidth || panel.getHeight() != pPanelHeight) && c < 10);

        // keyboard and mouse handlers must be reset (otherwise they may indicate old events)
        keyListener  = new GameKeyListener();
        mouseAdapter = new GameMouseAdapter();
        frame.addKeyListener        (keyListener);
        panel.addMouseListener      (mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
        panel.addMouseWheelListener (mouseAdapter);
    }

    public boolean isFullscreen()
    {
        return fullscreen;
    }

    /*
     * Returns width of the outer JFrame
     */
    public int getFrameWidth()
    {
        return frame.getWidth();
    }
    
    /*
     * Returns height of the outer JFrame.
     */
    public int getFrameHeight()
    {
        return frame.getHeight();
    }

    /*
     * Moves the JFrame to the specified coordinates on the screen
     * (x/y from top-left corner of screen)
     */
    public void setLocation(int x, int y)
    {
        frame.setLocation(x, y);
    }

    /*
     * Gets the current JFrame location on screen
     */
    public Point getLocation()
    {
        return frame.getLocation();
    }

    /*
     * Returns width of the GamePanel inside the outer frame.
     */
    public int getWidth()
    {
        return panel.getWidth();
    }
    
    /*
     * Returns height of the GamePanel inside the outer frame.
     */
    public int getHeight()
    {
        return panel.getHeight();
    }

    /*
     * Changes the size the GamePanel (and the JFrame accordingly).
     */
    public void setSize(int pPanelWidth, int pPanelHeight)
    {
        // try several times to change size until satisfactory (see leaveFullscreen())
        int c = 0;
        do 
        {
            panel.setSize(pPanelWidth, pPanelHeight);
            frame.pack();
            c++;
        } while ((panel.getWidth() != pPanelWidth || panel.getHeight() != pPanelHeight) && c < 10);
    }

    /*
     * Changes the frame's title
     */
    public void setTitle(String pTitle)
    {
        frame.setTitle(pTitle);
    }
    
    public String getTitle()
    {
        return frame.getTitle();
    }

    /*
     * Draws a line in the specified color onto the panel.
     */
    public void drawLine(int px1, int py1, int px2, int py2, Color pColor)
    {
        Graphics2D graphics = panel.getDBGraphics();
        graphics.setColor(pColor);
        graphics.drawLine(px1, py1, px2, py2);
    }

    /*
     * Draws a rectangle in the specified color onto the panel.
     */
    public void drawRectangle(int px, int py, int pWidth, int pHeight, Color pColor)
    {
        Graphics2D graphics = panel.getDBGraphics();
        graphics.setColor(pColor);
        graphics.drawRect(px, py, pWidth, pHeight);        
    }

    /*
     * Draws a filled rectangle in the specified color onto the panel.
     */
    public void fillRectangle(int px, int py, int pWidth, int pHeight, Color pColor)
    {
        Graphics2D graphics = panel.getDBGraphics();
        graphics.setColor(pColor);
        graphics.fillRect(px, py, pWidth, pHeight);        
    }

    /*
     * Sets the font to be used with drawString()
     * (default font is Arial)
     */
    public void setFont(Font f)
    {
        font = f;
    }

    /*
     * Sets the color to be used with drawString()
     * (default color is black)
     */
    public void setFontColor(Color f)
    {
        fontColor = f;
    }

    /*
     * Writes a text onto the panel at location x / y.
     * Uses font / font color previously set by setFont() / setFontColor()
     */
    public void drawString(String s, int x, int y)
    {
        Graphics2D graphics = panel.getDBGraphics();
        // The font has to be set each time (probably because there's a new graphics context after each repaint)
        graphics.setFont(font);
        // The current color of the panel may have been changed in the meantime, so set it to the text color.
        graphics.setColor(fontColor);
        graphics.drawString(s, x, y);
    }

    /*
     * Draws an image onto the GamePanel.
     * x/y will be the top left corner of the image.
     */
    public void drawImage(GameImage image, int x, int y)
    {
        Graphics2D graphics = panel.getDBGraphics();
        graphics.drawImage(image.getBufferedImage(), x, y, image.getWidth(), image.getHeight(), null);
    }

    /*
     * Clears the panel to a specific color.
     */
    public void clear(Color c)
    {
        Graphics2D graphics = panel.getDBGraphics();
        graphics.setColor(c);
        graphics.fillRect(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /*
     * Clears the panel to white.
     */
    public void clear()
    {
        clear(Color.WHITE);
    }

    /*
     * Draws the double buffer to the screen.
     * Then waits until the time segment for the current frame is finished.
     * To change the lenght of a frame, use setFrameTime().
     */
    public void paintFrame()
    {        
        panel.repaint();
        timer.waitFrame();
    }
    
    /*
     * Changes the current time per frame in milliseconds.
     * Default is 25 ms per frame (i.e. 40 frames per second).
     */
    public void setFrameTime(int time)
    {
        timer.setFrameTime(time);
    }
    
    /*
     * Pauses the Game for pTime milliseconds.
     */
    public void wait(int pTime)
    {
        try
        {
            Thread.sleep(pTime);
        }
        catch (Exception e)
        {
        }
    }

    /*
     * Returns true if a given key is currently being held down.
     * For the key code constants, refer to the Java classdoc on the class KeyEvent.
     */
    public boolean isKeyDown(int keyCode)
    {
        if (keyListener.isKeyDown(keyCode))
        {
            return true;
        }
        return false;
    }

    /*
     * Returns x-coordinate of the mouse relative to the JPanel.
     */ 
    public int getMouseX()
    {
        return mouseAdapter.getX();
    }
    
    /*
     * Returns y-coordinate of the mouse relative to the JPanel.
     */ 
    public int getMouseY()
    {
        return mouseAdapter.getY();
    }
    
    /*
     * Returns true if mouse button 1 has been pressed.
     */
    public boolean mouseButton1()
    {
        return mouseAdapter.button1Pressed();
    }
    
    public boolean mouseButton2()
    {
        return mouseAdapter.button2Pressed();
    }

    public boolean mouseButton3()
    {
        return mouseAdapter.button3Pressed();
    }

    /*
     * Returns the number of "notches" that the mouse wheel has been rotated since the last call of this method.
     * Positive value: mouse wheel was rotated towards the user
     * Negative value: mouse wheel was rotated away from the user
     */
    public int mouseWheel()
    {
        return mouseAdapter.mouseWheel();
    }
}
