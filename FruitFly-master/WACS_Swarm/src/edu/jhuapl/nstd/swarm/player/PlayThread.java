package edu.jhuapl.nstd.swarm.player;

import java.util.*;
import java.io.*;

public class PlayThread extends Thread {
	protected Player _player;
	protected PlayerCanvas _canvas;
	protected boolean _playing = false;
	protected long _speed;
	public static final long MAX = 3000;
    public static final long MIN = 1;
	public static final long DELTA = 100;

	public PlayThread(Player player, PlayerCanvas canvas, long speed) {
		_player = player;
		_canvas = canvas;
		_speed = speed;
	}

	public synchronized void pause() {
		_playing = false;
	}

	public synchronized void setSpeed(long in) {
		_speed = in;
	}

	public synchronized void faster() {
        long localDelta;
        if (_speed > 50)
        {
            localDelta = DELTA;
        }
        else
        {
            localDelta = _speed / 2;
        }

        if ((_speed - localDelta) < MIN)
        {
            localDelta = _speed - MIN;
        }

		_speed -= localDelta;
	}

	public synchronized void slower() 
    {
        long localDelta;

        if (_speed < 50)
        {
            localDelta = _speed;
        }
        else
        {
            localDelta = DELTA;
        }

        if ((_speed + localDelta) > MAX)
        {
            localDelta = MAX - _speed;
        }

		_speed += localDelta;
	}

	public long getSpeed() {
		return _speed;
	}
		

	public void run() {
		_playing = true;
		boolean running = true;
		while (_playing && running)
        {
			running = _player.step();
			_canvas.jgeoRepaint();
			try {
				sleep(_speed);
			} catch (Exception e) {
				_canvas.donePlaying();
				e.printStackTrace();
			}
		}
		_canvas.donePlaying();
	}
}
