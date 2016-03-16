package TreeQWOP;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DataExportPaneMaker implements TabbedPaneActivator {

	public DataExportPane ExportPane;
	
	public DataExportPaneMaker() {
		ExportPane = new DataExportPane();
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
	JTextArea ControlData;
	public DataExportPane(){
		// Make a text box for control actions
		ControlData = new JTextArea("Select a point on the tree to get data here.",4,30);
		this.add(ControlData);
		ControlData.setEditable(false);
		ControlData.setPos
		
		
	}
	
	
}