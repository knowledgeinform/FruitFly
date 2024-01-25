/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.swarm.display.geoimageryfetcher;

/**
 * Class to represent x and y coordinates
 */
public class Cell
{
    /**
     * x
     */
    public int x;

    /**
     * y
     */
    public int y;

    /**
     * Constructor
     */
    public Cell ()
    {
        this (0, 0);
    }

    /**
     * Copy constructor
     * 
     * @param newCell
     */
    public Cell(Cell newCell)
    {
        x = newCell.x;
        y = newCell.y;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.x;
        hash = 89 * hash + this.y;
        return hash;
    }

    /**
     * Equality override
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof Cell))
            return false;

        if (((Cell)obj).x == this.x && ((Cell)obj).y == this.y)
            return true;
        return false;
    }

    /**
     * Constructor
     * @param newX
     * @param newY
     */
    public Cell (int newX, int newY)
    {
        x = newX;
        y = newY;
    }
}
