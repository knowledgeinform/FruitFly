package edu.jhuapl.nstd.cbrnPods.Bridgeport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.Bridgeport.SensorDriverMessage.GammaDetectorMessage;
import edu.jhuapl.nstd.swarm.belief.BeliefManager;
import edu.jhuapl.nstd.swarm.util.Config;

public class BridgeportGammaDetectorUsbInterface extends AbstractBridgeportGammaDetectorInterface
{
	/**
	 * Host IP address of the Bridgeport SensorDriver.
	 */
	private static String address;

	/**
	 * Socket port used to send commands to the Bridgeport SensorDriver.
	 */
	private static final int cmdPort = 12103;

	/**
	 * Socket port used to receive raw XML telemetry from the Bridgeport SensorDriver.
	 */
	private static final int dataPort = 12104;

	/**
	 * Rate in which the raw XML telemetry is coming in from the Bridgeport SensorDriver.
	 */
	private int dataRate;
	
    /**
     * List of listeners for Bridgeport messages.  These listeners must be registered and will receive all messages from sensor
     */
    private LinkedList <BridgeportUsbMessageListener> _listeners = new LinkedList<BridgeportUsbMessageListener>();

	/**
	 * The line delimiter character ('\r')
	 */
	private static final char LINE_DELIMITER = (char) 13;
	
	/**
	 * Maximum length of the raw XML message body (minus the 4 byte msg length).
	 */
	private static final int MAX_TLM_MSG_LENGTH = 6480;

	/**
	 * Main constructor.
	 * 
	 * @param belMgr
	 */
	public BridgeportGammaDetectorUsbInterface(BeliefManager belMgr)
	{
		super(belMgr);
        this.setName ("WACS-BridgeportInterface");
		setDaemon(true);
		readConfig();

		this.start();
	}

	private InputStream initTlmStream()
	{
		InputStream dataReader = null;

		try
		{
			Socket sock = new Socket(address, dataPort);
			dataReader = sock.getInputStream();
		}
		catch (UnknownHostException e)
		{
			System.err.println("ERROR - BridgeportGammaDetectorUsbInterface.initTlmStream() - Don't know about host: \"" + address + "\".");
			return null;
		}
		catch (IOException ioe)
		{
			System.err.println("ERROR - BridgeportGammaDetectorUsbInterface.initTlmStream() - An error has occurred while trying to open a socket to \"" + address + ":" + dataPort + "\".");
			return null;
		}

		return dataReader;
	}

	/**
	 * Read config settings
	 */
	private void readConfig()
	{
		Config config = Config.getConfig();

		address = config.getProperty("Bridgeport.IpAddress", "127.0.0.1");
		dataRate = config.getPropertyAsInteger("Bridgeport.DataRate", 1000);
	}
	
	/**
     * Add a listener for gamma messages
     * 
     * @param listener Listener to add
     */
    @Override
	public void addBridgeportMessageListener(BridgeportUsbMessageListener listener)
    {
    	_listeners.add(listener);
    }
    
    /**
     * Remove a listener for gamma messages
     * 
     * @param listener Listener to remove
     */
    @Override
    public void removeBridgeportMessageListener(BridgeportUsbMessageListener listener)
    {
    	_listeners.remove(listener);
    }

	@Override
	protected boolean runLive()
	{
		m_Running = true;

		byte[] msgLenBytes, msgBytes;
		int msgLength;
		ByteBuffer msgBB;
		String msgBody;

		InputStream dataReader = initTlmStream();

		if (dataReader != null)
		{
			while (m_Running)
			{
				try
				{
					// The first four bytes are the length of the message that is following.
					msgLenBytes = new byte[4];
					msgLength = 0;

					// If we have read 4 bytes...
					if (dataReader.read(msgLenBytes) == 4)
					{
						msgBB = ByteBuffer.wrap(msgLenBytes);
						msgLength = msgBB.getInt();
						// System.out.println("Length: " + msgLength);
						
						if (msgLength > MAX_TLM_MSG_LENGTH)
						{
                                                    // System.out.println("Extra data: " + new String(msgLenBytes));
                                                    // This happens when the USB cable becomes disconnected or connected.
                                                    // We continue reading junk data until we see a proper message length.
                                                    // TODO This may not recover and possibly could be an infinite loop!!!!
                                                    continue;
						}

						msgBytes = new byte[msgLength];

						// If we have read msgLength bytes...
						if (dataReader.read(msgBytes) == msgLength)
						{
							msgBody = new String(msgBytes);
							//System.out.println(msgBody);

							if (msgBody != null && !msgBody.equals(""))
							{
								DocumentBuilder db;
								SensorDriverMessage msg;
								
								try
								{
									// Convert the string into an XML document
									db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
									InputSource is = new InputSource();
									is.setCharacterStream(new StringReader(msgBody));

									Document doc = db.parse(is);
									
									msg = parseDataFields(doc);
									
									notifyListeners(msg);
									
								}
								catch (ParserConfigurationException e)
								{
									System.err.println("ERROR - An error has occurred while parsing the Bridgeport XML data stream.");
									e.printStackTrace();
								}
								catch (SAXException e)
								{
									System.err.println("ERROR - An error has occurred while parsing the Bridgeport XML data stream.");
									e.printStackTrace();
								}
							}
						}
					}
					Thread.sleep(dataRate);
				}
				catch (IOException ioe)
				{
					System.err.println("ERROR - An error has occurred while reading data from the Bridgeport XML input stream.");
					ioe.printStackTrace();
				}
				catch (InterruptedException e)
				{
					System.err.println("ERROR - An error has occurred while reading data from the Bridgeport XML input stream.");
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.err.println("ERROR - Failed to open the Bridgeport's raw XML telemetry input stream reader.");
		}

		return false;
	}
	
	private SensorDriverMessage parseDataFields(Document document)
	{
		SensorDriverMessage msg = new SensorDriverMessage();

		Element rootElement = document.getDocumentElement();
		NodeList sensorNodeList = rootElement.getChildNodes();

		for (int i = 0; i < sensorNodeList.getLength(); i++)
		{
			Node sensorChildNode = sensorNodeList.item(i);

			if (sensorChildNode instanceof Element)
			{
				//a child element to process
				Element sensorChildElement = (Element) sensorChildNode;
				String sensorChildTagName = sensorChildElement.getTagName();

				if (sensorChildTagName.equals("timestamp"))
				{
					// Parse the message timestamp
					String timestampBase64 = sensorChildElement.getTextContent().trim();

					if (timestampBase64 != null && !timestampBase64.equals(""))
					{
//						try
//						{
							byte[] timestampBytes = Base64.decode(timestampBase64);
							
							ByteBuffer timestampBB = ByteBuffer.wrap(timestampBytes).order(ByteOrder.LITTLE_ENDIAN);
							int timestamp = timestampBB.getInt();
							msg.setTimestamp(timestamp);
//						}
//						catch (IOException e)
//						{
//							System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector timestamp.");
//						}
					}
				}
				else if (sensorChildTagName.equals("gamma_detector"))
				{
					GammaDetectorMessage gammaMsg = msg.new GammaDetectorMessage();
					NodeList gammaDetectorNodeList = sensorChildElement.getChildNodes();
					for (int j = 0; j < gammaDetectorNodeList.getLength(); j++)
					{
						Node gammaDetectorChildNode = gammaDetectorNodeList.item(j);
						
						if (gammaDetectorChildNode instanceof Element)
						{
							Element gammaDetectorChildElement = (Element) gammaDetectorChildNode;
							String gammaDetectorChildTagName = gammaDetectorChildElement.getTagName().trim();
							String gammaDetectorChildElementText = gammaDetectorChildElement.getFirstChild().getTextContent().trim();
							
							if (gammaDetectorChildElementText != null && !gammaDetectorChildElementText.equals(""))
							{
								if (gammaDetectorChildTagName.equals("serial_number"))
								{
									// Set the gamma detector serial number
									try
									{
										gammaMsg.setSerialNumber(new Integer(gammaDetectorChildElementText));
									}
									catch(NumberFormatException nfe)
									{
										System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector serial number.");
									}
								}
								else if (gammaDetectorChildTagName.equals("status"))
								{
									// Set the gamma detector status
									 gammaMsg.setStatus(Boolean.valueOf(gammaDetectorChildElementText));
								}
								else if (gammaDetectorChildTagName.equals("sample_time"))
								{
									// Set the gamma detector sample time
									byte[] sampleTimeBytes = Base64.decode(gammaDetectorChildElementText);
									
									ByteBuffer sampleTimeBB = ByteBuffer.wrap(sampleTimeBytes).order(ByteOrder.LITTLE_ENDIAN);
									int sampleTime = sampleTimeBB.getInt();
									
									gammaMsg.setSampleTime(sampleTime);
								}
								else if (gammaDetectorChildTagName.equals("high_voltage"))
								{
									// Set the gamma detector high voltage
									try
									{
										gammaMsg.setHighVoltage(new Double(gammaDetectorChildElementText));
									}
									catch(NumberFormatException nfe)
									{
										System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector high voltage setting.");
									}
								}
								else if (gammaDetectorChildTagName.equals("coarse_gain"))
								{
									// Set the gamma detector coarse gain
									try
									{
										gammaMsg.setCoarseGain(new Double(gammaDetectorChildElementText));
									}
									catch(NumberFormatException nfe)
									{
										System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector coarse gain setting.");
									}
								}
								else if (gammaDetectorChildTagName.equals("fine_gain"))
								{
									// Set the gamma detector fine gain
									try
									{
										gammaMsg.setFineGain(new Double(gammaDetectorChildElementText));
									}
									catch(NumberFormatException nfe)
									{
										System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector fine gain setting.");
									}
								}
								else if (gammaDetectorChildTagName.equals("live_time"))
								{
									// Set the gamma detector live time
									try
									{
										gammaMsg.setLiveTime(new Integer(gammaDetectorChildElementText));
									}
									catch(NumberFormatException nfe)
									{
										System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector live time.");
									}
								}
								else if (gammaDetectorChildTagName.equals("real_time"))
								{
									// Set the gamma detector real time
									try
									{
										gammaMsg.setRealTime(new Integer(gammaDetectorChildElementText));
									}
									catch(NumberFormatException nfe)
									{
										System.err.println("ERROR: BridgeportGammaDetectorUsbInterface - An error has occurred while parsing the gamma detector real time.");
									}
								}
								else if (gammaDetectorChildTagName.equals("spectrum"))
								{
									// Obtain the gamma detector message spectrum
									byte[] spectrumBytes = Base64.decode(gammaDetectorChildElementText);
									ByteBuffer spectrumBB = ByteBuffer.wrap(spectrumBytes).order(ByteOrder.LITTLE_ENDIAN);
									IntBuffer spectrumIntBB = spectrumBB.asIntBuffer();
									int[] intArray = new int[spectrumIntBB.limit()];
									spectrumIntBB.get(intArray);
						
									// Create a new histogram with the spectra data obtained from the sensor.
									RNHistogram histogramMsg = new RNHistogram(intArray.length);
									int totalCount = 0;
									
									for (int k = 0; k < intArray.length; k++)
									{
										histogramMsg.setRawValue(k, intArray[k]);
										totalCount += intArray[k];
									}
									gammaMsg.setTotalCount(totalCount);
									gammaMsg.setSpectrum(histogramMsg);
								}
							}
						}
					} // end for each gamma_detector child nodes
					
					// Compute the remaining statistics
					gammaMsg.setDeadTime(gammaMsg.getRealTime() - gammaMsg.getLiveTime());
					
					double countRate = ((double)gammaMsg.getTotalCount()) / (((double) gammaMsg.getLiveTime()) / 1000);
					gammaMsg.setCountRate(countRate);
					
					msg.addGammaDetectorMessage(gammaMsg);
				}
				else if (sensorChildTagName.equals("temperature"))
				{
					Element tempValElement = (Element) sensorChildElement.getElementsByTagName("value").item(0);
					String tempValStr = tempValElement.getTextContent().trim();
					
					if(tempValStr != null && !tempValStr.equals(""))
					{
						msg.setTemperature(new Double(tempValStr));
					}
				}				
			}
		} // end for each sensor child nodes

		return msg;
	}

	@Override
	protected void runPlayback()
	{
		// TODO Implement playback of recorded Bridgeport data file.

	}

	/**
	 * Sends the command to set the high voltage level of the device.
	 * Calling this function includes sending the UPDATE command.
	 * 
	 * @param volts Number of volts to use as the high voltage
	 */
	public void setHighVoltage(double volts)
	{
		// System.out.println("Setting high voltage to " + volts);
		String msg = "SET GD HV 1 " + volts + LINE_DELIMITER;
		int msgLen = msg.getBytes().length;

		byte[] msgArray = new byte[4 + msgLen];
		ByteBuffer bb = ByteBuffer.wrap(msgArray).order(ByteOrder.BIG_ENDIAN);
		bb.putInt(msgLen);
		bb.put(msg.getBytes());

		sendCommand(msgArray);
		sendUpdateCommand();
	}
        
        /**
         * Sends the command to set the fine gain setting of the device.
	 * Calling this function includes sending the UPDATE command.
         * 
         * @param fineGrain 
         */
        public void setFineGain(double fineGain)
        {
            String msg = "SET GD FG 1 " + fineGain + LINE_DELIMITER;
            int msgLen = msg.getBytes().length;

            byte[] msgArray = new byte[4 + msgLen];
            ByteBuffer bb = ByteBuffer.wrap(msgArray).order(ByteOrder.BIG_ENDIAN);
            bb.putInt(msgLen);
            bb.put(msg.getBytes());

            sendCommand(msgArray);
            sendUpdateCommand();
        }

	/**
	 * Sends the UPDATE command.
	 */
	private void sendUpdateCommand()
	{
		System.out.println("Sending Update command");
		String msg = "UPDATE GD" + LINE_DELIMITER;
		int msgLen = msg.getBytes().length;

		byte[] msgArray = new byte[4 + msgLen];
		ByteBuffer bb = ByteBuffer.wrap(msgArray).order(ByteOrder.BIG_ENDIAN);
		bb.putInt(msgLen);
		bb.put(msg.getBytes());

		sendCommand(msgArray);
	}

	/**
	 * Helper function that sends the given command bytes to the Bridgeport SensorDriver
	 * over the command socket.
	 * 
	 * @param cmdBytes byte array defining the command to send
	 */
	private synchronized void sendCommand(byte[] cmdBytes)
	{
		Socket sock = null;
		OutputStream cmdWriter = null;
		InputStream cmdReader = null;

		try
		{
			sock = new Socket(address, cmdPort);
			cmdWriter = sock.getOutputStream();
			cmdReader = sock.getInputStream();

			// Send the command
			cmdWriter.write(cmdBytes);

			// Read the response message length
			byte[] responseMsgLenArray = new byte[4];
			cmdReader.read(responseMsgLenArray);
			ByteBuffer bb = ByteBuffer.wrap(responseMsgLenArray).order(ByteOrder.BIG_ENDIAN);
			int responseMsgLen = bb.getInt();
			// System.out.println("Response Msg Length: " + responseMsgLen);

			// Read the response message body
			byte[] responseMsgArray = new byte[responseMsgLen];
			cmdReader.read(responseMsgArray);
			String cmdResponse = new String(responseMsgArray);

			// Check the response message body to see if it contains the OKAY acknowledgement.
			if (cmdResponse != null && !cmdResponse.contains("OKAY"))
			{
				// TODO The command was not accepted. What feedback can we provide to the user?
				System.err.println("ERROR - The Bridgeport command \"" + new String(cmdBytes) + "\" was rejected.");
			}

			// System.out.println(cmdResponse);
		}
		catch (UnknownHostException e)
		{
			System.err.println("ERROR - BridgeportGammaDetectorUsbInterface.sendCommand() - Don't know about host: \"" + address + "\".");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{

			// Close the command writer stream.
			try
			{
				if (cmdWriter != null)
				{
					cmdWriter.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			// Close the command reader stream.
			try
			{
				if (cmdReader != null)
				{
					cmdReader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			// Close the command socket.
			try
			{
				if (sock != null)
				{
					sock.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notify listeners of a histogram message
	 * 
	 * @param msg
	 *            Message to send
	 */
	void notifyListeners(SensorDriverMessage msg)
	{
		Iterator<BridgeportUsbMessageListener> itr;
		itr = _listeners.listIterator();
		while (itr.hasNext())
		{
			BridgeportUsbMessageListener listener = (BridgeportUsbMessageListener) itr.next();
			listener.handleSensorDriverMessage(msg);
		}
	}
}
