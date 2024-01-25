package edu.jhuapl.nstd.cbrnPods.Canberra;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;

import edu.jhuapl.nstd.cbrnPods.messages.Canberra.CanberraDetectionMessage;
import edu.jhuapl.nstd.swarm.util.Config;
import edu.jhuapl.nstd.util.XCommSerialPort;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CanberraGammaDetectorInterface extends Thread
{

    /**
     * Serial connection to the Canberra.
     */
    private XCommSerialPort serialPort;
    /**
     * Port of the serial connection.
     */
    private static String port;
    /**
     * Baud rate of the serial connection.
     */
    private static int baud;
    /**
     * Flag used to designate whether the data acquisition thread is running or
     * not.
     */
    private boolean isRunning = false;
    /**
     * Flag used to designate whether to write the collected data to a log file.
     */
    private static boolean logData;
    /**
     * Folder to save log files to.
     */
    private String logFolderName;
    /**
     * Output file for logging.
     */
    private File currentLogFile;
    /**
     * Name of output file name to log data.
     */
    private String logFilename;
    /**
     * Output stream for logging.
     */
    private DataOutputStream logStream;
    /**
     * Counter for filenames to differentiate if they get too large.
     */
    private int logFileCounter = 0;
    /**
     * Maximum file size for log files. Starts a new file if we exceed this
     * value.
     */
    private static int maxLogFileBytes;
    
    private int m_ByteBufferUsedLength = 0;
    /**
     * Buffer for tracking bytes
     */
    private byte m_ByteBuffer[] = new byte [1000];
    /**
     * List of listeners for Canberra detection messages. These listeners must
     * be registered and will receive all messages from sensor
     */
    private LinkedList<CanberraDetectionMessageListener> listenerList = new LinkedList<CanberraDetectionMessageListener>();

    /**
     * The instantiation of this constructor creates a daemon thread that will
     * open a serial connection, and start parsing and logging the data from the
     * Canberra gamma sensor.
     */
    public CanberraGammaDetectorInterface()
    {
        this.setName("WACS-CanberraGammaDetectorInterface");
        setDaemon(true);
        readConfig();

        // If we are supposed to log data, initialize the log file writer.
        if (logData)
        {
            // Initialize the log filename and directory.
            setupLogFolder();
            logFilename = logFolderName + "CanberraGamma_" + System.currentTimeMillis() + "_";
        }

        // Open the serial connection to the Canberra.
        try
        {
            serialPort = new XCommSerialPort(port, baud);
        }
        catch (Exception ex)
        {
            System.err.println("ERROR - CanberraGammaDetectorInterface(): Failed to open a serial connection to the Canberra: " + port + " " + baud + ".");
            ex.printStackTrace();
            return;
        }

        // If the serial port is open, then start reading Canberra data.
        if (serialPort.isPortGood())
        {
            this.start();
        }
    }

    /**
     * Read config settings
     */
    private void readConfig()
    {
        Config config = Config.getConfig();

        port = config.getProperty("CanberraGammaDetectorInterface.serialPort");
        baud = config.getPropertyAsInteger("CanberraGammaDetectorInterface.serialBaudRate");

        logData = Config.getConfig().getPropertyAsBoolean("CanberraGammaDetectorInterface.logData", true);
        logFolderName = Config.getConfig().getProperty("CanberraGammaDetectorInterface.logFolder", "./GammaLogs");
        maxLogFileBytes = Config.getConfig().getPropertyAsInteger("CanberraGammaDetectorInterface.flatFileMaxSizeBytes", 10000000);
    }

    /**
     * For a already specified log folder name, ensure the log folder exists.
     */
    private void setupLogFolder()
    {
        while (logFolderName.endsWith("\\") || logFolderName.endsWith("/"))
        {
            logFolderName = logFolderName.substring(0, logFolderName.length() - 1);
        }

        try
        {
            File outputFolder = new File(logFolderName);

            if (!outputFolder.exists())
            {
                outputFolder.mkdirs();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        logFolderName += "/";
    }

    /**
     * Add a listener for detection messages
     *
     * @param listener Listener to add
     */
    public void addCanberraDetectionMessageListener(CanberraDetectionMessageListener listener)
    {
        listenerList.add(listener);
    }

    /**
     * Remove a listener for detection messages
     *
     * @param listener Listener to remove
     */
    public void removeCanberraDetectionMessageListener(CanberraDetectionMessageListener listener)
    {
        listenerList.remove(listener);
    }

    @Override
    public void run()
    {
        if (serialPort != null)
        {
            byte[] bytesRead;

            try
            {
                isRunning = true;
                while (isRunning)
                {
                    bytesRead = serialPort.readAvailableBytes();
                    if (bytesRead != null)
                    {
                        parseData(bytesRead);
                    }

                    Thread.sleep(500);
                }

                // Close the current log file, if it is open.
                if (logData)
                {
                    try
                    {
                        if (logStream != null)
                        {
                            logStream.flush();
                            logStream.close();
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("ERROR - CanberraGammaDetectorInterface.run(): Could not close the log file output stream for the Canberra gamma detector.");
                    }
                }

                System.out.println("INFO - CanberraGammaDetectorInterface.run(): Canberra gamma sensor shutting down...");
                isRunning = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                isRunning = false;
            }
            finally
            {
                if (serialPort != null)
                {
                    serialPort.close();
                }
            }
        }
    }

    private void parseData(byte[] bytesRead)
    {
        try
        {
            if (bytesRead.length > 0)
            {
                if (m_ByteBufferUsedLength + bytesRead.length > m_ByteBuffer.length)
                {
                    //Expand byte buffer if necessary
                    byte oldBuf[] = m_ByteBuffer;
                    m_ByteBuffer = new byte [m_ByteBufferUsedLength + bytesRead.length + 50];
                    System.arraycopy(oldBuf, 0, m_ByteBuffer, 0, m_ByteBufferUsedLength);
                }
                
                //Add new bytes to byte buffer
                System.arraycopy(bytesRead, 0, m_ByteBuffer, m_ByteBufferUsedLength, bytesRead.length);
                m_ByteBufferUsedLength += bytesRead.length;
                
                
                LinkedList <String> measurementsList = new LinkedList<String> ();
                int startIndexOfNewMessage = 0;
                for (int i = 0; i < m_ByteBufferUsedLength-1; i ++)
                {
                    if (m_ByteBuffer[i] == '\n' && m_ByteBuffer[i+1] == '\r')
                    {
                        String s = new String (m_ByteBuffer, startIndexOfNewMessage, (i-startIndexOfNewMessage));
                        measurementsList.add(s);
                        
                        startIndexOfNewMessage = i+2;
                    }
                }

                //Shift unused bytes down in buffer for next time
                m_ByteBufferUsedLength -= startIndexOfNewMessage;
                System.arraycopy(m_ByteBuffer, startIndexOfNewMessage, m_ByteBuffer, 0, m_ByteBufferUsedLength);
                
                String[] measurementsArray = new String[measurementsList.size()];
                measurementsArray = measurementsList.toArray (measurementsArray);
                

                System.out.println("ARRAY LENGTH: " + measurementsArray.length);
                for (String measurement : measurementsArray)
                {
                    System.out.println("STRING: " + measurement);



                    measurement = measurement.trim();
                    if (!measurement.equals(""))
                    {
                        // Replace the double spaces with a single space
                        measurement = measurement.replaceAll("  ", " ");

                        String[] dataArray = measurement.split(" ");

                        if (dataArray.length != 7)
                        {
                            System.err.println("ERROR - CanberraGammaDetectorInterface.parseData(): Found " + dataArray.length + " data fields; expected 7.");
                            return;
                        }
                        else
                        {
                            // Parse the total count
                            int count = Integer.parseInt(dataArray[0]);

                            // Parse the filtered dose rate
                            String filteredDoseRateStr = dataArray[1].substring(0, 4).replaceAll("_", "");
                            double filteredDoseRate = Double.parseDouble(filteredDoseRateStr);
                            String filteredDRUnits = dataArray[1].substring(4);
                            filteredDoseRate = convertToMicro(filteredDoseRate, filteredDRUnits.charAt(0));

                            // Parse the unfiltered dose rate
                            String unfilteredDoseRateStr = dataArray[2].substring(0, 4).replaceAll("_", "");
                            double unfilteredDoseRate = Double.parseDouble(unfilteredDoseRateStr);
                            String unfilteredDRUnits = dataArray[2].substring(4);
                            unfilteredDoseRate = convertToMicro(unfilteredDoseRate, unfilteredDRUnits.charAt(0));

                            // Parse the mission dose
                            String missionDoseRateStr = dataArray[3].substring(0, dataArray[3].length() - 2).replaceAll("_", "");
                            double missionDose = Double.parseDouble(missionDoseRateStr);
                            String missionDoseUnits = dataArray[3].substring(4);
                            missionDose = convertToMicro(missionDose, missionDoseUnits.charAt(0));

                            // Parse the peak rate (in cG/h)
                            String peakRateStr = dataArray[4].substring(0, dataArray[4].length() - 4).replaceAll("_", "");
                            double peakRate = Double.parseDouble(peakRateStr);

                            // Parse the temperature
                            double temperature = Double.parseDouble(dataArray[5].replaceAll("_", ""));

                            // Parse the relative time
                            String relativeTime = dataArray[6];

                            // Create the new detection message
                            CanberraDetectionMessage newMessage = new CanberraDetectionMessage();
                            long currTime = System.currentTimeMillis();
                            newMessage.setTimestampMs(currTime);
                            newMessage.setCount(count);
                            newMessage.setFilteredDoseRate(filteredDoseRate);
                            newMessage.setUnfilteredDoseRate(unfilteredDoseRate);
                            newMessage.setMisssionAccumulatedDose(missionDose);
                            newMessage.setPeakRate(peakRate);
                            newMessage.setTemperature(temperature);
                            newMessage.setRelativeTime(relativeTime);

                            // Log the data
                            if (logData)
                            {
                                ensureLogFile();
                                logData(newMessage);
                            }

                            // Notify all listeners of new detection message
                            notifyListeners(newMessage);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("ERROR - CanberraGammaDetectorInterface.parseData(): An error has occurred while parsing data from the Canberra.");
            e.printStackTrace();
            return;
        }
    }

    /**
     * Converts dose rate units to micro-whatever for consistency
     * @param value the value to convert
     * @param unit the character for the unit prefix (p, n, u, m, c, etc.)
     * @return the converted value
     */
    private double convertToMicro(double value, char unit)
    {
        switch (unit)
        {
            case 'u':
                return value; // Units are correct
            case 'm':
                return 1000 * value;
            case 'c':
                return 10000 * value;
        }
        
        return value;
    }

    /**
     * Be sure a results log file is open and not too large for logging data.
     */
    private void ensureLogFile()
    {
        // Check if the current log file exceeds the maximum size limit.
        if (currentLogFile == null || currentLogFile.length() > maxLogFileBytes)
        {
            // Close the current log file
            try
            {
                if (logStream != null)
                {
                    logStream.flush();
                    logStream.close();
                }
            }
            catch (Exception e)
            {
                System.err.println("ERROR - CanberraGammaDetectorInterface.ensureLogFile(): Could not flush output stream before making a new log file for the Canberra gamma detector.");
            }

            // Create the new log file.
            Formatter format = new Formatter();
            format.format("%1$s%2$05d%3$s", logFilename, logFileCounter++, ".dat");
            currentLogFile = new File(format.out().toString());

            try
            {
                logStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(currentLogFile), 4096));
                logHeader(logStream);
            }
            catch (Exception e)
            {
                currentLogFile = null;
                logStream = null;
                System.err.println("ERROR - CanberraGammaDetectorInterface.ensureLogFile(): Could not create log file for the Canberra gamma detector.");
            }
        }
    }

    /**
     * Write header data showing what columns are logged with the processing
     * results.
     *
     * @param logStream Stream to log header to
     * @throws Exception
     */
    private void logHeader(DataOutputStream logStream) throws Exception
    {
        logStream.writeBytes("Timestamp\t");
        logStream.writeBytes("Relative Time\t");
        logStream.writeBytes("Count\t");
        logStream.writeBytes("Filtered Dose Rate\t");
        logStream.writeBytes("Unfiltered Dose Rate\t");
        logStream.writeBytes("Mission Accumulated Dose\t");
        logStream.writeBytes("Mission Peak Rate\t");
        logStream.writeBytes("Temperature");
        logStream.writeBytes("\r\n");
        logStream.flush();
    }

    /**
     * Write the data stored in the given CanberraDetectionMessage.
     *
     * @param msg The CanberraDetectionMessage containing the data to log.
     * @throws Exception
     */
    private void logData(CanberraDetectionMessage msg) throws Exception
    {
        logStream.writeBytes(msg.getTimestampMs() + "\t");
        logStream.writeBytes(msg.getRelativeTime() + "\t");
        logStream.writeBytes(msg.getCount() + "\t");
        logStream.writeBytes(msg.getFilteredDoseRate() + "\t");
        logStream.writeBytes(msg.getUnfilteredDoseRate() + "\t");
        logStream.writeBytes(msg.getMisssionAccumulatedDose() + "\t");
        logStream.writeBytes(msg.getPeakRate() + "\t");
        logStream.writeBytes(msg.getTemperature() + "\t");
        logStream.writeBytes("\r\n");
        logStream.flush();
    }

    /**
     * Returns true if the data acquisition thread is running.
     *
     * @return True if the data acquisition thread is running.
     */
    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Stops the data acquisition thread.
     */
    public void shutdown()
    {
        isRunning = false;
    }

    /**
     * Notify listeners of a detection message
     *
     * @param msg Message to send
     */
    void notifyListeners(CanberraDetectionMessage msg)
    {
        Iterator<CanberraDetectionMessageListener> itr;
        itr = listenerList.listIterator();
        while (itr.hasNext())
        {
            CanberraDetectionMessageListener listener = (CanberraDetectionMessageListener) itr.next();
            listener.handleDetectionMessage(msg);
        }
    }
    
    public static void main (String args[])
    {
        CanberraGammaDetectorInterface br = new CanberraGammaDetectorInterface();   
        while (true)
        {
            try {
                Thread.sleep (10000000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CanberraGammaDetectorInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
