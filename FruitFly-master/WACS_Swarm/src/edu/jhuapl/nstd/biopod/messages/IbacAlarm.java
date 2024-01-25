/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.jhuapl.nstd.biopod.messages;

/**
 * turn alarm on or off based on isAlarm
 * @author southmk1
 */
public class IbacAlarm extends IbacCommand{
    private boolean isAlarm = false;
    public IbacAlarm()
    {
         super(BioPodMessage.IBAC_ALARM, 1);
    }

    @Override
    public byte[] toByteArray()
    {
        writeDataByte(1, isAlarm?1:0);
        return super.toByteArray();
    }

    /**
     * @return the isAlarm
     */
    public boolean isIsAlarm() {
        return isAlarm;
    }

    /**
     * @param isAlarm the isAlarm to set
     */
    public void setIsAlarm(boolean isAlarm) {
        this.isAlarm = isAlarm;
    }
   

   
}
