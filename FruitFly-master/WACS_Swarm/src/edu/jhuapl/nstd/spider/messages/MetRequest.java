/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public class MetRequest extends SpiderMessage{
    
    public MetRequest()
    {
        super(SpiderMessageType.SEND_MET,HeaderSize + ChecksumSize);
    }

}
