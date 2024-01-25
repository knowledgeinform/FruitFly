/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AnacondaSpectraPanel.java
 *
 * Created on Jun 16, 2010, 1:22:42 PM
 */

package edu.jhuapl.nstd.cbrnPods.TestPanels;

import edu.jhuapl.nstd.cbrnPods.RNHistogram;
import edu.jhuapl.nstd.cbrnPods.RNHistogramDisplayGraphPanel;
import edu.jhuapl.nstd.cbrnPods.cbrnPodsInterface;
import edu.jhuapl.nstd.cbrnPods.cbrnSensorTypes;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDAHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBGMessage;
import edu.jhuapl.nstd.cbrnPods.messages.AnacondaMessages.anacondaSpectraLCDBHMessage;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMessageListener;
import edu.jhuapl.nstd.cbrnPods.messages.cbrnPodMsg;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author stipeja1
 */
public class AnacondaSpectraPanel extends javax.swing.JPanel implements cbrnPodMessageListener
{
    cbrnPodsInterface m_Pods;
    RNHistogramDisplayGraphPanel AGSpectra;
    RNHistogramDisplayGraphPanel AHSpectra;
    RNHistogramDisplayGraphPanel BGSpectra;
    RNHistogramDisplayGraphPanel BHSpectra;

    boolean initpanels;

    /** Creates new form AnacondaSpectraPanel */
    public AnacondaSpectraPanel(cbrnPodsInterface pods)
    {
        m_Pods = pods;

        initComponents();
        initpanels = false;

        msgOutput.setModel(new DefaultListModel());
        
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_G_SPECTRA, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDA_H_SPECTRA, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_G_SPECTRA, this);
        m_Pods.addPersistentListener(cbrnSensorTypes.SENSOR_ANACONDA, cbrnPodMsg.ANACONDA_LCDB_H_SPECTRA, this);

        AGSpectra = new RNHistogramDisplayGraphPanel ();
        AHSpectra = new RNHistogramDisplayGraphPanel ();
        BGSpectra = new RNHistogramDisplayGraphPanel ();
        BHSpectra = new RNHistogramDisplayGraphPanel ();

         AGSpectra.setTitle("LCDA G Spectra");
         AHSpectra.setTitle("LCDA H Spectra");
         BGSpectra.setTitle("LCDB G Spectra");
         BHSpectra.setTitle("LCDB H Spectra");

         int[] blank = new int[256];
         Arrays.fill(blank, 0);

         //AGSpectra.setSize(jPanel1.getWidth(), jPanel1.getHeight());
         AGSpectra.updateCurrentHistogram(new RNHistogram(blank));
         jPanel1.add (AGSpectra);

         //AHSpectra.setSize(jPanel2.getWidth(), jPanel2.getHeight());
         AHSpectra.updateCurrentHistogram(new RNHistogram(blank));
         jPanel2.add (AHSpectra);

        // BGSpectra.setSize(jPanel3.getWidth(), jPanel3.getHeight());
         BGSpectra.updateCurrentHistogram(new RNHistogram(blank));
         jPanel3.add (BGSpectra);

         //BHSpectra.setSize(jPanel4.getWidth(), jPanel4.getHeight());
         BHSpectra.updateCurrentHistogram(new RNHistogram(blank));
         jPanel4.add (BHSpectra);
    }

    
    public void insertMessageArea(final String message) 
    {
        try
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    DefaultListModel model = ((DefaultListModel) msgOutput.getModel());
                    model.insertElementAt(message, 0);
                    if (model.size() > 51)
                        model.setSize(51);
                }
            });
            
        }
        catch (Exception e)
        {
            //eat it
        }
    }


        @Override
    public void handleMessage(cbrnPodMsg m)
    {

        if(!initpanels)
        {
            if (jPanel1.getWidth() > 0 && jPanel2.getWidth() > 0 && jPanel3.getWidth() > 0 && jPanel4.getWidth() > 0)
            {
                initpanels = true;
                AGSpectra.setSize(jPanel1.getWidth(), jPanel1.getHeight());
                AHSpectra.setSize(jPanel2.getWidth(), jPanel2.getHeight());
                BGSpectra.setSize(jPanel3.getWidth(), jPanel3.getHeight());
                BHSpectra.setSize(jPanel4.getWidth(), jPanel4.getHeight());
            }

        }

        if(m instanceof anacondaSpectraLCDAGMessage){
            insertMessageArea("Received LCDAG Spectra Message");
            handleSpectraMessage((anacondaSpectraLCDAGMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDBGMessage){
            insertMessageArea("Received LCDBG Spectra Message");
            handleSpectraMessage((anacondaSpectraLCDBGMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDAHMessage){
            insertMessageArea("Received LCDAH Spectra Message");
            handleSpectraMessage((anacondaSpectraLCDAHMessage)m);
        }
        else if(m instanceof anacondaSpectraLCDBHMessage){
            insertMessageArea("Received LCDBH Spectra Message");
            handleSpectraMessage((anacondaSpectraLCDBHMessage)m);
        }

    }


    void handleSpectraMessage(final anacondaSpectraLCDAGMessage msg)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                AGSpectra.updateCurrentHistogram( new RNHistogram (msg.getSpectraData()));
                //AGSpectra.setStatMessage("Message Here");
            }
        });
        

    }

    void handleSpectraMessage(final anacondaSpectraLCDBGMessage msg)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                BGSpectra.updateCurrentHistogram(new RNHistogram (msg.getSpectraData()));
            }
        });
         
    }

    void handleSpectraMessage(final anacondaSpectraLCDAHMessage msg)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                AHSpectra.updateCurrentHistogram(new RNHistogram (msg.getSpectraData()));
            }
        });
         
    }

    void handleSpectraMessage(final anacondaSpectraLCDBHMessage msg)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                BHSpectra.updateCurrentHistogram(new RNHistogram (msg.getSpectraData()));
            }
        });
         
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msgOutput = new javax.swing.JList();

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 527, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 523, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 527, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 523, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(msgOutput);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                        .addGap(549, 549, 549))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList msgOutput;
    // End of variables declaration//GEN-END:variables

}
