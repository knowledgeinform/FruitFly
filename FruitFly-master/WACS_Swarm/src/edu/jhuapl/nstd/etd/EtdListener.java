/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.etd;

import java.util.EventListener;

/**
 *
 * @author kayjl1
 */
public interface EtdListener extends EventListener  {
	abstract public void handleEtd(long currentTime, String etdLine);
}
