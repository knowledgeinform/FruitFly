package wacsdataparser2009;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

/**
 *
 * @author biggimh1
 */
public class WACSParserGUI extends JFrame implements ActionListener {

        JPanel textPanel = new JPanel(new GridLayout(2,1));
        JPanel labelPanel = new JPanel(new GridLayout(2,1));
        JLabel sourceLabel = new JLabel("Source File:");
        JLabel destinationLabel = new JLabel("Destination File:");
        JLabel message = new JLabel("Ready");
        JTextField source = new JTextField(50);
        JTextField destination = new JTextField(50);
        JButton commit = new JButton("Commit");
    public WACSParserGUI(){
        super("WACS Data Parser");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setup();
        pack();
    }

    private void setup(){
        textPanel.add(source);
        textPanel.add(destination);

        labelPanel.add(sourceLabel);
        labelPanel.add(destinationLabel);

        commit.addActionListener(this);
        add(labelPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(commit, BorderLayout.EAST);
        add(message, BorderLayout.SOUTH);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// ignore
		}
		WACSParserGUI app = new WACSParserGUI();
		app.setVisible(true);
    }

    public void actionPerformed(ActionEvent event){
        String e = event.getActionCommand();

        if(e.equalsIgnoreCase("commit")){
            String sourceFileName = source.getText();
            String destFileName = destination.getText();

            try{
                WACSParser parser = new WACSParser(sourceFileName, destFileName);
                parser.parse();
            }catch(Exception IOException){
                JOptionPane.showMessageDialog(null, "File Read/Write Error");
            } 
        }
    }

}