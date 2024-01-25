/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public class OkPowerDownResponse  extends SpiderMessage{
    public OkPowerDownResponse()
    {
        super(SpiderMessageType.SPIDER_OK_POWER_DOWN,HeaderSize+ChecksumSize);
    }

    public void parseSpiderMessage(SpiderMessage m) {
        super.parseSpiderMessage(m);
        setData(new byte[0]);
    }
}
