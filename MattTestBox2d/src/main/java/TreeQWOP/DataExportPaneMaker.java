package TreeQWOP;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DataExportPaneMaker implements TabbedPaneActivator {

	public DataExportPane ExportPane;
	
	private TrialNode focusPoint;
	
	public DataExportPaneMaker() {
		ExportPane = new DataExportPane();
	}
	
	/* Gets called to update the node up to which we display the controls. */
	public void setNode(TrialNode focusPoint){
		ExportPane.repaint();
		if (focusPoint != null){
			this.focusPoint = focusPoint;
			ExportPane.setNode(focusPoint);
		}

	}

	@Override
	public void ActivateTab() {
		// TODO Auto-generated method stub

	}

	@Override
	public void DeactivateTab() {
		// TODO Auto-generated method stub

	}

}
class DataExportPane extends JPanel implements ActionListener{
	private JTextArea ControlData;
	private JScrollPane ControlScroll;
	private JLabel ControlLabel;
	
	private JTextArea StateData;
	private JScrollPane StateScroll;
	private JLabel StateLabel;
	
	
	private JTextField fileNameField;
	private JButton saveButton;
	
	private TrialNode focusPoint;
	
	private int[] controlSequence;
	private String[] stateSequence;

	
	public DataExportPane(){
		FlowLayout layout  = new FlowLayout();
//		this.setMaximumSize(this.getSize());
		setLayout(null);

		// Make a text box for control actions
		ControlLabel = new JLabel("Controls data:");
		this.add(ControlLabel);
		ControlLabel.setBounds(50,10,600,10);
		
		ControlData = new JTextArea("Select a point on the tree to get data here.",2,20);
		ControlScroll = new JScrollPane(ControlData,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


		ControlData.setEditable(false);
		ControlData.setLineWrap(true);
		ControlData.setBounds(50,25,600,800);
		ControlScroll.setBounds(50,25,600,100);
		
		// Make a text box for state data
		StateLabel = new JLabel("State data:");
		this.add(StateLabel);
		StateLabel.setBounds(50,150,600,10);
		
		StateData = new JTextArea("Select a point on the tree to get data here.",15,20);
		StateScroll = new JScrollPane(StateData,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		StateData.setEditable(false);
		StateData.setLineWrap(true);
		StateData.setWrapStyleWord(true);
		StateData.setBounds(50,165,600,10000);
		StateScroll.setBounds(50,165,600,600);

		StateData.setFont(new Font(StateData.getFont().getFontName(),Font.PLAIN, 10));
		
		this.add(ControlScroll);
		this.add(StateScroll);
		
		// Make a save-to-csv button
		saveButton = new JButton("Save to csv");
		saveButton.setBounds(50,800,100,50);
		fileNameField = new JTextField();
		fileNameField.setBounds(170,800,300,50);
		
		saveButton.addActionListener(this);
		
		
		this.add(saveButton);
		this.add(fileNameField);

		
	}
	
	
	/* Gets called to update the node up to which we display the controls. */
	public void setNode(TrialNode focusPoint){
		
		// Get the control sequence up to this point and format it into a CSV-style string.
		controlSequence = focusPoint.getSequence();
		stateSequence = focusPoint.getStateSequenceString();
		
		String formatControlSeq = "";
		String formatStateSeq = "";
		
		for (int i = 0; i<controlSequence.length - 1; i++){
			formatControlSeq += controlSequence[i] + ", ";
		}
		
		formatControlSeq += controlSequence[controlSequence.length - 1];
		
		for (int i = 0; i<stateSequence.length; i++){
			formatStateSeq += stateSequence[i] + "\n";
		}
		
		ControlData.setText(formatControlSeq);
		StateData.setText(formatStateSeq);
		
	}
	
	/* Write to csv */
	private void writeToCSV(){
		try{
			FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/" + fileNameField.getText() + ".csv");
			for (int i = 0; i<controlSequence.length; i++){
				writer.append(String.valueOf(controlSequence[i]));
				writer.append(",");
				writer.append(stateSequence[i]);
				writer.append("\n");
			}
			
		    writer.flush();
		    writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(stateSequence != null){
			writeToCSV();
		}
		
	}

}