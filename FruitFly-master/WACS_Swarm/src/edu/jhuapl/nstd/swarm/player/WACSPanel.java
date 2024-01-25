package edu.jhuapl.nstd.swarm.player;

import edu.jhuapl.nstd.swarm.belief.AnacondaDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.ParticleDetectionBelief;
import edu.jhuapl.nstd.swarm.belief.PlumeDetectionBelief;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class WACSPanel extends JPanel implements ActionListener
{
    protected Player _recordingPlayer;

    protected JScrollPane _chemPane;
    protected JScrollPane _bioPane;
    protected JScrollPane _imageProcessingPane;
    protected JTextArea _chemDetectionsTextArea;
    protected JTextArea _bioDetectionsTextArea;
    protected JTextArea _imageProcessingDataTextArea;
    protected JButton _btnClearDetections;

    public WACSPanel(Player recordingPlayer)
    {
        _recordingPlayer = recordingPlayer;
        
        _chemDetectionsTextArea = new JTextArea(" Chem Detections:\n No Data");
        _chemPane = new JScrollPane(_chemDetectionsTextArea);
        _bioDetectionsTextArea = new JTextArea(" IBAC Detections:\n No Data");
        _bioPane = new JScrollPane(_bioDetectionsTextArea);
        _imageProcessingDataTextArea = new JTextArea(" IR Detections:\n No Data");
        _imageProcessingPane = new JScrollPane(_imageProcessingDataTextArea);
        _btnClearDetections = new JButton("Clear Detections");
        _btnClearDetections.addActionListener(this);

        _chemPane.setPreferredSize(new Dimension(150, 100));

        _chemDetectionsTextArea.setEditable(false);
        _chemDetectionsTextArea.setAutoscrolls(true);
        //spiderDetectionsTextArea.setSize(350, 800);

        _bioDetectionsTextArea.setEditable(false);
        _bioDetectionsTextArea.setAutoscrolls(true);
        _bioDetectionsTextArea.setSize(150, 800);

        _imageProcessingDataTextArea.setEditable(false);
        _imageProcessingDataTextArea.setAutoscrolls(true);
        _imageProcessingDataTextArea.setSize(150, 800);

        JPanel commStatPanel  = new JPanel(new GridLayout(4,1));
        commStatPanel.add(_chemPane);
        commStatPanel.add(_bioPane);
        commStatPanel.add(_imageProcessingPane);
        commStatPanel.add(_btnClearDetections);

        add(commStatPanel);
    }

    public void UpdateText()
    {
        AnacondaDetectionBelief anacondaDetectionBelief = _recordingPlayer.getAnacondaDetection();
        ParticleDetectionBelief bioPodDetectionBelief = _recordingPlayer.getBioDetection();
        PlumeDetectionBelief plumeDetectionBelief = _recordingPlayer.getPlumeDetection();

        if(anacondaDetectionBelief != null)
        {
           _chemDetectionsTextArea.setText(" " + anacondaDetectionBelief.getAnacondaDetectionString());
        }
        
        if (bioPodDetectionBelief != null)
        {
            _bioDetectionsTextArea.setText(" " + bioPodDetectionBelief.getParticleDetectionString());
        }

        if (plumeDetectionBelief != null)
        {
            _imageProcessingDataTextArea.setText(" IR Detections:\n" + plumeDetectionBelief.getPlumeDetectionString());
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == _btnClearDetections)
        {
            _recordingPlayer.clearDetections();
        }
    }
}
