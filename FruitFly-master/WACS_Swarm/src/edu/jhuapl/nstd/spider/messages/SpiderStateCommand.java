/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.spider.messages;

/**
 *
 * @author southmk1
 */
public class SpiderStateCommand extends SpiderMessage {

    /**
     * @return the state
     */
    public SpiderModeEnum getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(SpiderModeEnum state) {
        this.state = state;
    }

    private SpiderModeEnum state;

    public SpiderStateCommand() {
        super(SpiderMessageType.SPIDER_STATE_COMMAND, HeaderSize + ChecksumSize + 1);
    }
    public SpiderStateCommand(SpiderModeEnum state) {
        super(SpiderMessageType.SPIDER_STATE_COMMAND, HeaderSize + ChecksumSize + 1);
        this.state = state;
    }

    @Override
    public byte[] toByteArray() {
        switch (getState()) {
            case PowerDown:
                getData()[0] = 0x01;
                break;
            case PylonMode:
                getData()[0] = 0x02;
                break;
            case Standby:
                getData()[0] = 0x03;
                break;
            case Search:
                getData()[0] = 0x04;
                break;
            case TerminateSearch:
                getData()[0] = 0x05;
                break;
            case DataTransmit:
                getData()[0] = 0x06;
                break;

        }
        return super.toByteArray();
    }
}
