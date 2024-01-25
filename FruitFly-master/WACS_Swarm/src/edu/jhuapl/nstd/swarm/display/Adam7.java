/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jhuapl.nstd.swarm.display;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author xud1
 */
public class Adam7 
{
    private static final int MAX_PASS = 7;
    public static final int[] xOffset = {0,4,0,2,0,1,0};
    public static final int[] yOffset = {0,0,4,0,2,0,1};
    public static final int[] xStep = {8,8,4,4,2,2,1};
    public static final int[] yStep = {8,8,8,4,4,2,2};
    public static final int[] xSize = {8,4,4,2,2,1,1};
    public static final int[] ySize = {8,8,4,4,2,2,1};
    public static final int[] numTiles = {1,1,2,4,8,16,32};
        
    public static byte[] GetInterlaceByteArray(int iteration, BufferedImage sourceImage)
    {
        ByteBuffer partition = ByteBuffer.allocate((sourceImage.getWidth()/8)*(sourceImage.getHeight()/8)*numTiles[iteration]);

        for (int j = xOffset[iteration]; j < sourceImage.getWidth(); j += xStep[iteration]) 
        {
            for (int k = yOffset[iteration]; k < sourceImage.getHeight(); k += yStep[iteration]) 
            {
                // store the gray-scale pixel value (fits in a byte)
                byte pixel = (byte) sourceImage.getData().getSample(j, k, 0);
                partition.put(pixel);
            }
        }          
        return partition.array();
    }
    
    public static BufferedImage GetBufferedImageFromPixelArray(byte[] pixelArray)
    {
        // create a BufferdImage object to store the pixels for the current interation
        BufferedImage partialImage = new BufferedImage(1, pixelArray.length, BufferedImage.TYPE_BYTE_GRAY);     
                
        for(int i = 0; i < pixelArray.length; i++)
        {
            partialImage.getRaster().setSample(0, i, 0, pixelArray[i]);
        }
        
        return partialImage;
    }
    
    public static int Interlace(BlockingQueue<byte[]> interlacedData, BufferedImage sourceImage, float compressionQuality)
    {                
        int totalNumberOfBytes = 0;
        
        for (int i=0; i<MAX_PASS; i++)
        {
            // create a BufferdImage object to store the pixels for the current interation
            int size = (sourceImage.getWidth()/8)*(sourceImage.getHeight()/8)*numTiles[i];
            BufferedImage interlacedImage = new BufferedImage(1, size, BufferedImage.TYPE_BYTE_GRAY);
            
            int rasterIndex = 0;            
            for (int j=xOffset[i]; j<sourceImage.getWidth(); j+=xStep[i])
            {
                for (int k=yOffset[i]; k<sourceImage.getHeight(); k+=yStep[i])
                {              
                    // store the gray-scale pixel value (fits in a byte)
                    byte pixel = (byte)sourceImage.getData().getSample(j, k, 0);                                        
                    interlacedImage.getRaster().setSample(0, rasterIndex, 0, pixel);     
                    rasterIndex++;
                }
            }                   
                
            try
            {     
                // get jpeg compressed bytes
                byte[] interlaceForTransmission = GetJPEGCompressedByteArray(interlacedImage, compressionQuality);
                
                // get non-compressed bytes
                byte[] pixelByteArray = GetInterlaceByteArray(i, sourceImage);                
             
                if( interlaceForTransmission.length > pixelByteArray.length )
                {
                    // more efficient to send just the non-compressed data
                    interlaceForTransmission = pixelByteArray;
                }
                
                // additional compression
                interlaceForTransmission = Compress(interlaceForTransmission);
                interlacedData.put(interlaceForTransmission); 
                
                totalNumberOfBytes += interlaceForTransmission.length;
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }         
        return totalNumberOfBytes;
    }
    
    public static BufferedImage Reconstruct(BufferedImage image, byte[] interlacedData, int pass)
    {
        // decompress the received data and progressively reconstruct the image
        BufferedImage receivedImage = null;
        try
        {
            InputStream in = new ByteArrayInputStream(Adam7.Decompress(interlacedData));
            receivedImage = ImageIO.read(in);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

        // check if received data was jpeg encoded
        try 
        {
            receivedImage.getRaster();
        } 
        catch (NullPointerException ex) 
        {
            // if the received data was not jpeg encoded, create buffered image from the pixel array
            System.out.println("Adam7: Received image not jpg encoded, converting to BufferedImage...");
            receivedImage = Adam7.GetBufferedImageFromPixelArray(Adam7.Decompress(interlacedData));
        }
        
        // Reconstruct an image given the interlaced data and pass number        
        int rasterIndex = 0;        
        for (int j = xOffset[pass]; j < image.getWidth(); j += xStep[pass]) 
        {
            for (int k = yOffset[pass]; k < image.getHeight(); k += yStep[pass]) 
            {               
                byte pixel = (byte)receivedImage.getRaster().getSample(0, rasterIndex, 0);
                
                // fill the tile with the same pixel values
                for (int w = j; w < (j + xSize[pass]); w++) 
                {                                                            
                    for (int h = k; h < (k + ySize[pass]); h++) 
                    {                        
                        image.getRaster().setSample(w, h, 0, pixel);
                    }                    
                }
                rasterIndex++;
            }               
        }
        return image;
    }                   
    
    public static byte[] GetJPEGCompressedByteArray(BufferedImage sourceImage, float compressionQuality)
    {
          try
          {          
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              ImageOutputStream ios = ImageIO.createImageOutputStream(baos);

              Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
              ImageWriter writer = writers.next();

              ImageWriteParam param = writer.getDefaultWriteParam();
              param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
              param.setCompressionQuality(compressionQuality);

              writer.setOutput(ios);
              writer.write(null, new IIOImage(sourceImage.getRaster(), null, null), param);

              byte[] data = baos.toByteArray();

              writer.dispose();

              return data;       
          }
          catch(Exception e)
          {
               e.printStackTrace();
          }
     
          return null;                        
    }
    
    public static byte[] Decompress(byte[] compressedBytes)
    {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedBytes);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedBytes.length);
        byte[] buffer = new byte[1024];
        
        try
        {                    
            while(!inflater.finished())
            {
                int count = inflater.inflate(buffer);
                bos.write(buffer, 0, count);
            }
        
            bos.close();
        }
        catch (IOException | DataFormatException  ex)
        {
            ex.printStackTrace();
        }
        return bos.toByteArray();
    }
    
    
    public static byte[] Compress(byte[] sourceBytes)
    {
        Deflater deflater = new Deflater();
        deflater.setInput(sourceBytes);
        deflater.finish();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(sourceBytes.length);
        byte[] buffer = new byte[1024];
        
        while(!deflater.finished())
        {
            int bytesCompressed = deflater.deflate(buffer);
            bos.write(buffer, 0, bytesCompressed);
        }
        try
        {
            bos.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
        return bos.toByteArray();
    }        
}
