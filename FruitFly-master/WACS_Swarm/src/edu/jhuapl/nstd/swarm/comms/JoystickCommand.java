package edu.jhuapl.nstd.swarm.comms;

import java.util.logging.*;

import java.io.Serializable;

public class JoystickCommand implements Serializable {
	private int _x;
	private int _y;

	public JoystickCommand(int x, int y) {
		_x = x;
		_y = x;
	}

	public int getX() { return _x; }
	public int getY() { return _y; }
	public String toString() {
		switch (_x) {
			case 0: return "LEFT_TURN";
			case 1: return "HARD_LEFT_TURN";
			case 2: return "RIGHT_TURN";
			case 3: return "HARD_RIGHT_TURN";
			case 4: return "NO_TURN";
		}
		return "";
	}
}
