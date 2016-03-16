package TreeQWOP;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private JTextArea StateData;
	
	private TrialNode focusPoint;
	
	public DataExportPane(){
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		
		// Make a text box for control actions
		this.add(new JLabel("Controls data:"));
		
		ControlData = new JTextArea("Select a point on the tree to get data here.",4,80);
		this.add(ControlData);
		ControlData.setEditable(false);
		
		// Make a text box for state data
		this.add(new JLabel("State data:"));
		
		StateData = new JTextArea("Select a point on the tree to get data here.",4,80);
		this.add(StateData);
		StateData.setEditable(false);
		
	}
	
	
	/* Gets called to update the node up to which we display the controls. */
	public void setNode(TrialNode focusPoint){
		TrialNode currentNode = focusPoint;
		this.focusPoint = focusPoint;
		ControlData.setText("");
		StateData.setText("");
		while (currentNode.ParentNode != null){
			StateData.append(String.valueOf(currentNode.TreeDepth) + ", ");
			currentNode = currentNode.ParentNode;
		}
	}

}