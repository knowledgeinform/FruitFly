
package edu.jhuapl.nstd.swarm.util;

import java.util.logging.*;




import java.util.*;

import java.io.*;
import edu.jhuapl.jlib.math.*;
import edu.jhuapl.jlib.math.position.*;
import edu.jhuapl.nstd.swarm.display.*;
import edu.jhuapl.nstd.swarm.action.*;
import edu.jhuapl.nstd.swarm.belief.*;
import edu.jhuapl.nstd.swarm.belief.mode.*;
import edu.jhuapl.nstd.swarm.behavior.*;
import edu.jhuapl.nstd.swarm.behavior.group.*;
import edu.jhuapl.nstd.swarm.comms.*;

public class MemoryDump implements Runnable {

	public class Chunk {
		public byte[] _chunk;

		public Chunk(int size) {
			_chunk = new byte[size];
		}
	}

	protected int CHUNK_SIZE = Config.getConfig().getPropertyAsInteger("test.chunkSize");
	protected int _fileOut = 0;
	protected int _fileIn = 0;
	protected byte[] CHUNK = new byte[CHUNK_SIZE];
	protected int MAX_BYTES = Config.getConfig().getPropertyAsInteger("test.maxBytes");
	protected String PATH = Config.getConfig().getProperty("test.path");
	protected int _totalWriteTime = 0;
	protected int _totalReadTime = 0;

	public MemoryDump() {
		for (int i=0; i<CHUNK_SIZE; i++) {
			CHUNK[i] = (byte)(Math.random() * 255);
		}
	}

	private synchronized void populate() {
		try {
			long time = System.currentTimeMillis();
			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(PATH+"/f"+_fileOut++));
			writer.write(CHUNK);
			writer.flush();
			writer.close();
			long diff = System.currentTimeMillis() - time;
			Logger.getLogger("GLOBAL").info("WRITE: " + (diff));
			_totalWriteTime += diff;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void read() {
		try {
			long time = System.currentTimeMillis();
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream(PATH+"/f"+_fileIn++));
			reader.read(CHUNK,0,CHUNK_SIZE);
			reader.close();
			long diff = System.currentTimeMillis() - time;
			Logger.getLogger("GLOBAL").info("READ: " + (diff));
			_totalReadTime += diff;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void run() {
		long time = System.currentTimeMillis();
		while(true) {
			try {
				if ((_fileOut * CHUNK_SIZE) > MAX_BYTES) {
					Logger.getLogger("GLOBAL").info("DONE WITH READ/WRITE TEST: " + (System.currentTimeMillis() - time));
					Logger.getLogger("GLOBAL").info("WRITE AVG: " + (_totalWriteTime / (double)_fileOut));
					Logger.getLogger("GLOBAL").info("READ AVG: " + (_totalReadTime / (double)_fileIn));
					System.exit(0);
					break;
				}
				populate();
				read();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			try {
				Thread.sleep(Config.getConfig().getPropertyAsLong("test.sleep",100));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	

    
}

