using System;
using System.Collections.Generic;
using System.Text;

namespace ALink
{
    public class AnacondaMessage
    {
        protected byte[] message;

        //calculate total size of message:  headerLength + dataLength + 1 byte checksum
        //headerLength = 5 = 2 sync + 1 type + 2 length   
        protected int headerLength = 5;
        protected ushort dataLength = 0;
        protected const ushort ChecksumLength = 1;

        protected byte[] syncBytes = { 0x7e, 0x7e };
        protected byte typeByte = 0x00;
        protected byte[] lengthBytes = { 0x00, 0x00 };
        protected byte[] dataBytes = null;
        protected byte checksumByte = 0x00;

        public byte[] getMessage()
        {
            return message;
        }

        public int getMessageLength()
        {
            return message.Length;
        }

    }

    public class OutgoingAnacondaMessage : AnacondaMessage
    {
        

        public enum OutgoingType
        {
            GPSdata, ModeIdle, ModeSearch1, ModeSearch2, ModeSearch3, ModeSearch4,
            ModeStandby, ModeAirframe, ModePod, ActionDelete1, ActionDelete2, ActionDelete3, ActionDelete4,
            SetDateTime, SetServoOpemLimit, SetServoClosedLimit, SetManifoldHeaterTemperature,
            SetPitotHeaterTemperature, DebugPermitDebug, DebugForbidDebug, DebugServoOpen,
            DebugServoClosed, DebugManifoldHeaterOn, DebugManifoldHeaterOff, DebugPitotHeaterOn,
            DebugPitotHeaterOff, DebugPumpsOn, DebugPumpsOff, DebugValves1Open, DebugValves2Open, 
            DebugValves3Open, DebugValves4Open, DebugValves1Closed, DebugValves2Closed, DebugValves3Closed, 
            DebugValves4Closed, DebugValvesAllClosed, DebugWriteTestFiles, DebugWriteTestFileLCDA,
            DebugWriteTestFileLCDB, DebugWriteTestFileSYS, DebugResetLCDA, DebugResetLCDB, 
            DebugReleaseResetLCDA, DebugReleaseResetLCDB
        };

        
        public OutgoingAnacondaMessage(OutgoingType type, Object data)
        {
            
            int nextByte = 0;
            byte[] temp;


            //parse the message into byte array:
            switch (type)
            {
                case OutgoingType.GPSdata:
                    //A-Link is not equipped to provide true GPS, so this is for 
                    //engineering/debugging purposes only
                    //gpsData length: 1 dataValid + 4 time_t + 4 lat + 4 lon + 2 alt = 15 bytes
                    try
                    {
                        typeByte = 0x10;    //16 decimal
                        dataLength = 15;
                        
                        GPSdata gps = (GPSdata)data;
                        dataBytes = new byte[dataLength];

                        nextByte = 0;
                        //data valid byte is set to true (1) for the purposes of A-Link 
                        temp = new byte[1] { 1 };
                        temp.CopyTo(dataBytes, nextByte);
                        nextByte = nextByte + temp.Length;

                        //get time:
                        temp = BitConverter.GetBytes(gps.time_t);
                        temp.CopyTo(dataBytes, nextByte);
                        nextByte = nextByte + temp.Length;

                        //get lat:
                        temp = BitConverter.GetBytes(gps.lat);
                        temp.CopyTo(dataBytes, nextByte);
                        nextByte = nextByte + temp.Length;

                        //get lon:
                        temp = BitConverter.GetBytes(gps.lon);
                        temp.CopyTo(dataBytes, nextByte);
                        nextByte = nextByte + temp.Length;

                        //get alt:
                        temp = BitConverter.GetBytes(gps.alt);
                        temp.CopyTo(dataBytes, nextByte);
                        nextByte = nextByte + temp.Length;
                    }
                    catch (Exception ex)
                    {
                        System.Console.Error.WriteLine(ex);
                    }
                    break;

                case OutgoingType.ModeIdle:
                    typeByte = 0x14;    //20 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModeSearch1:
                    typeByte = 0x15;    //21 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModeSearch2:
                    typeByte = 0x16;    //22 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModeSearch3:
                    typeByte = 0x17;    //23 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModeSearch4:
                    typeByte = 0x18;    //24 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModeStandby:
                    typeByte = 0x19;    //25 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModeAirframe:
                    typeByte = 0x1A;    //26 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ModePod:
                    typeByte = 0x1B;    //27 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ActionDelete1:
                    typeByte = 0x1F;    //31 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ActionDelete2:
                    typeByte = 0x20;    //32 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ActionDelete3:
                    typeByte = 0x21;    //33 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.ActionDelete4:
                    typeByte = 0x22;    //34 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.SetDateTime:
                    typeByte = 0x28;    //40 decimal
                    dataLength = 0;
                    //data????  must be something missing in communications definitions
                    break;
                
                case OutgoingType.SetServoOpemLimit:
                    typeByte = 0x29;    //41 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.SetServoClosedLimit:
                    typeByte = 0x2A;    //42 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.SetManifoldHeaterTemperature:
                    typeByte = 0x2B;    //43 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.SetPitotHeaterTemperature:
                    typeByte = 0x2C;    //44 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugPermitDebug:
                    typeByte = 0x3E;    //62 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugForbidDebug:
                    typeByte = 0x3F;    //63 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugServoOpen:
                    typeByte = 0x40;    //64 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugServoClosed:
                    typeByte = 0x41;    //65 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugManifoldHeaterOn:
                    typeByte = 0x42;    //66 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugManifoldHeaterOff:
                    typeByte = 0x43;    //67 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugPitotHeaterOn:
                    typeByte = 0x44;    //68 decimal
                    dataLength = 0;
                    break;
                    
                case OutgoingType.DebugPitotHeaterOff:
                    typeByte = 0x45;    //69 decimal
                    dataLength = 0;
                    break;

                case OutgoingType.DebugPumpsOn:
                    typeByte = 0x46;    //70 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugPumpsOff:
                    typeByte = 0x47;    //71 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves1Open:
                    typeByte = 0x48;    //72 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves2Open:
                    typeByte = 0x49;    //73 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves3Open:
                    typeByte = 0x4A;    //74 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves4Open:
                    typeByte = 0x4B;    //75 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves1Closed:
                    typeByte = 0x4C;    //76 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves2Closed:
                    typeByte = 0x4D;    //77 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves3Closed:
                    typeByte = 0x4E;    //78 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValves4Closed:
                    typeByte = 0x4F;    //79 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugValvesAllClosed:
                    typeByte = 0x50;    //80 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugWriteTestFiles:
                    typeByte = 0x51;    //81 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugWriteTestFileLCDA:
                    typeByte = 0x52;    //82 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugWriteTestFileLCDB:
                    typeByte = 0x53;    //83 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugWriteTestFileSYS:
                    typeByte = 0x54;    //84 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugResetLCDA:
                    typeByte = 0x60;    //96 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugResetLCDB:
                    typeByte = 0x61;    //97 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugReleaseResetLCDA:
                    typeByte = 0x62;    //98 decimal
                    dataLength = 0;
                    break;
                
                case OutgoingType.DebugReleaseResetLCDB:
                    typeByte = 0x63;    //99 decimal
                    dataLength = 0;
                    break;

                default:
                    System.Console.Error.WriteLine("Invalid outgoing message type.");
                    break;
            }

            //convert length to bytes:
            lengthBytes = BitConverter.GetBytes(dataLength);

            //calculate checksum:
                //sum of (typeByte + lengthBytes + dataBytes) mod 256
            int byteSum = 0;
            byteSum = byteSum + typeByte;
            foreach (byte b in lengthBytes)
            {
                byteSum = byteSum + b;
            }
            //only add dataBytes if they exist:
            if (dataLength > 0)
            {
                foreach (byte b in dataBytes)
                {
                    byteSum = byteSum + b;
                }
            }
            checksumByte = BitConverter.GetBytes( byteSum % 256)[0];

            //allocate the message
            message = new byte[headerLength + dataLength + ChecksumLength];
            //fill the message:
            nextByte = 0;
            syncBytes.CopyTo(message, nextByte);
            nextByte = nextByte + syncBytes.Length;

            message[nextByte] = typeByte;
            nextByte++;

            lengthBytes.CopyTo(message, nextByte);
            nextByte = nextByte + lengthBytes.Length;

            if (dataLength > 0)
            {
                dataBytes.CopyTo(message, nextByte);
                nextByte = nextByte + dataBytes.Length;
            }

            message[nextByte] = checksumByte;
            nextByte++;

        }

    }


    public class IncomingAnacondaMessage : AnacondaMessage
    {
        public enum IncomingType { Status  /*more to come with revision of interface document*/};

        public IncomingType type;


    }
}
