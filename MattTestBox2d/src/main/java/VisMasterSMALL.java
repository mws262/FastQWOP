import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * 
 * Minimal version of VisMaster. Used for ONLY displaying the runner and not the rest of the tree stuff.
 * 
 * @author Matt
 *
 */

public class VisMasterSMALL extends JFrame implements Schedulable{

	int visInterval = 0;
	RunnerPaneMaker RunMaker;

	JPanel RunPane;
	
	
	JTabbedPane DataTabs;
	private ArrayList<TabbedPaneActivator> TabPanes= new ArrayList<TabbedPaneActivator>(); //List of all panes in the tabbed part
	
	public VisMasterSMALL(QWOPInterface QWOPHandler){

	    Container pane = this.getContentPane();
	    pane.setLayout(new GridBagLayout());
	    
	    /* RUNNER PANE */
	    RunMaker = new RunnerPaneMaker(QWOPHandler);
	    this.RunPane = RunMaker.RunPanel;
	    this.add(RunPane);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setPreferredSize(new Dimension(OptionsHolder.windowWidth, OptionsHolder.windowHeight));
        this.setContentPane(RunPane);
        this.pack();
        this.setVisible(true); 
 
        repaint();
	}
	

	@Override
	public int getInterval() {
		return visInterval;
	}

	@Override
	public void DoScheduled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DoEvery() {
//		TreeMaker.update();
//		DataMaker.update();
		
	}

	@Override
	public void setInterval(int interval) {
		visInterval = interval;
		
	}


	@Override
	public void DoNow() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void Disable() {
		// TODO Auto-generated method stub
		
	}
}
