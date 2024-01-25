package edu.jhuapl.nstd.tase;

import java.util.*;


public interface TASE_PointingAnglesListener extends EventListener {
	abstract public void handleTASE_PointingAngles(TASE_PointingAngles angles);
}