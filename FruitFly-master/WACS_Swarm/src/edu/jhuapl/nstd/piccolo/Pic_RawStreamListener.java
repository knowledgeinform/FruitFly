/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.piccolo;

/**
 *
 * @author humphjc1
 */
public interface Pic_RawStreamListener 
{
    public abstract void handlePic_RawStream(byte bytesRead[], int numBytesRead);
    
}
