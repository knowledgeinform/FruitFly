package edu.jhuapl.nstd.tase;

import java.util.*;


public interface TASE_TelemetryListener extends EventListener {
	abstract public void handleTASE_Telemetry(TASE_Telemetry telem);
}