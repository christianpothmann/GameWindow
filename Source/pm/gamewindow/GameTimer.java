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

/*
 * Simple timer: initialize with a time frame in milliseconds.
 * At the end of each frame (objects have been moved and drawn), call waitFrame(),
 * and the timer will wait until the specified time has passed.
 * This way, each frame has the same length, independent of the number of objects to process or the speed of the computer.
 *
 * C. Pothmann, 2014.11.22
 */
public class GameTimer
{
    private int frameTime;
    private long startTime;

    /*
     * Construktor:
     * Set time per frame (in milliseconds)
     */
    public GameTimer(int frameTime)
    {
        this.frameTime = frameTime;
    }

    /*
     * Change time per frame
     */
    public void setFrameTime(int frameTime)
    {
        this.frameTime = frameTime;
    }
    
    /*
     * Call this at the beginning of each frame (in the game loop)
     */
    public void startFrame()
    {
        startTime = System.currentTimeMillis();
    }

    /*
     * Call this after all objects have been processed for the current frame
     */
    public void waitFrame()
    {
        while (System.currentTimeMillis() < startTime + frameTime)
        {
        }
        startTime = System.currentTimeMillis();
    }
}
