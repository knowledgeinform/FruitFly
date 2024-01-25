
package edu.jhuapl.nstd.swarm.autopilot;

import java.util.logging.*;

public class AutoPilotException extends Exception {
	public AutoPilotException() {
		super();
	}

	public AutoPilotException(Throwable t) {
		super(t);
	}

	public AutoPilotException(String message, Throwable t) {
		super(message, t);
	}

	public AutoPilotException(String message) {
		super(message);
	}


}
