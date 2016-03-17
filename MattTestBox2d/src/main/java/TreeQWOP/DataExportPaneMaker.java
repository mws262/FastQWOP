package TreeQWOP;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
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
class DataExportPane extends JPanel {
	private JTextArea ControlData;
	private JScrollPane ControlScroll;
	private JLabel ControlLabel;
	
	private JTextArea StateData;
	private JScrollPane StateScroll;
	private JLabel StateLabel;
	
	private TrialNode focusPoint;

	
	public DataExportPane(){
//		GridLayout layout  = new GridLayout();
//		this.setMaximumSize(this.getSize());
		setLayout(null);

		// Make a text box for control actions
		ControlLabel = new JLabel("Controls data:");
		this.add(ControlLabel);
		ControlLabel.setBounds(50,10,600,10);
		
		ControlData = new JTextArea("Select a point on the tree to get data here.",2,20);
		ControlScroll = new JScrollPane(ControlData);
		this.add(ControlData);
		this.add(ControlScroll);
		ControlData.setEditable(false);
		ControlData.setLineWrap(true);
		ControlData.setBounds(50,25,600,100);
		
		// Make a text box for state data
		StateLabel = new JLabel("State data:");
		this.add(StateLabel);
		StateLabel.setBounds(50,150,600,10);
		
		StateData = new JTextArea("Select a point on the tree to get data here.",2,20);
		StateScroll = new JScrollPane(StateData);
		this.add(StateData);
		this.add(StateScroll);
		StateData.setEditable(false);
		StateData.setLineWrap(true);
		StateData.setBounds(50,165,600,600);
		StateData.setFont(new Font(StateData.getFont().getFontName(),Font.PLAIN, 8));
		
	}
	
	
	/* Gets called to update the node up to which we display the controls. */
	public void setNode(TrialNode focusPoint){
		
		// Get the control sequence up to this point and format it into a CSV-style string.
		int[] controlSequence = focusPoint.getSequence();
		String[] stateSequence = focusPoint.getStateSequenceString();
		
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
		
		
//		while (currentNode.ParentNode != null){
//			StateData.append(String.valueOf(currentNode.TreeDepth) + ", ");
//			currentNode = currentNode.ParentNode;
//		}
	}

}