package edu.jhuapl.nstd.piccolo;

import java.util.*;


public interface Pic_TelemetryListener extends EventListener {
	abstract public void handlePic_Telemetry(Long currentTime, Pic_Telemetry telem);
}