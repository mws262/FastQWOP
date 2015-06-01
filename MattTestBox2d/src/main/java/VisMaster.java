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


public class VisMaster extends JFrame implements Schedulable, ChangeListener{

	int visInterval = 0;
	
	DataPaneMaker DataMaker;
	TreePaneMaker TreeMaker;
	RunnerPaneMaker RunMaker;
	SnapshotPaneMaker SnapshotMaker;
	
	JPanel DataPane;
	JPanel TreePane;
	JPanel RunPane;
	JPanel SnapshotPane;
	
	
	JTabbedPane DataTabs;
	private ArrayList<TabbedPaneActivator> TabPanes= new ArrayList<TabbedPaneActivator>(); //List of all panes in the tabbed part
	
	public VisMaster(QWOPInterface QWOPHandler, TrialNode root, DataGrabber data){

	    Container pane = this.getContentPane();
	    pane.setLayout(new GridBagLayout());
	
	    ///// Tabbed panes ///////

	    
	    GridBagConstraints DataConstraints = new GridBagConstraints();
	    DataConstraints.fill = GridBagConstraints.HORIZONTAL;
	    DataConstraints.gridx = 0;
	    DataConstraints.gridy = 0;
	    DataConstraints.weightx = 0.2;
	    DataConstraints.ipady = (int)(0.85*OptionsHolder.windowHeight);
	    
	    DataTabs = new JTabbedPane();
	    DataTabs.setBorder(BorderFactory.createRaisedBevelBorder());
	    pane.add(DataTabs,DataConstraints);
	    
	    /* DATA PANE */
	    DataMaker = new DataPaneMaker("Plot1",data);
	    this.DataPane = DataMaker.DataPane;
	    DataTabs.addTab("Plots", DataPane);
	    
	    /* RUNNER PANE */
	    RunMaker = new RunnerPaneMaker(QWOPHandler);
	    this.RunPane = RunMaker.RunPanel;
	    DataTabs.addTab("Run Animation", RunPane);
    
	    /* RUNNER SNAPSHOT PANE */
	    SnapshotMaker = new SnapshotPaneMaker();
	    this.SnapshotPane = SnapshotMaker.SnapshotPanel;
	    DataTabs.addTab("State Viewer", SnapshotPane);

	    
	    //Handle listening to tab changes. Disable any updates on inactive tabs.
	    TabPanes.add(DataMaker);
	    TabPanes.add(RunMaker);
	    TabPanes.add(SnapshotMaker);
	    DataTabs.addChangeListener(this);
	    
	    //Make sure the currently active tab is actually being updated.
	    TabPanes.get(DataTabs.getSelectedIndex()).ActivateTab();
	    
	    //////////////////////////////
	    /* TREE PANE */
	    GridBagConstraints TreeConstraints = new GridBagConstraints();
	    TreeConstraints.fill = GridBagConstraints.HORIZONTAL;
	    TreeConstraints.gridx = 10;
	    TreeConstraints.gridy = 0;
	    TreeConstraints.weightx = 0.8;
	    TreeConstraints.ipady = OptionsHolder.windowHeight;
	    
	    TreeMaker = new TreePaneMaker(root);
	    this.TreePane = TreeMaker.TreePanel;
	    TreeMaker.setSnapshotPane(SnapshotMaker);
	    DataMaker.setTreePane(TreeMaker);
	    
		TreePane.setBorder(BorderFactory.createRaisedBevelBorder());
	    pane.add(TreePane,TreeConstraints);

	    
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setPreferredSize(new Dimension(OptionsHolder.windowWidth, OptionsHolder.windowHeight));
        this.setContentPane(this.getContentPane());
        this.pack();
        this.setVisible(true); 
        TreePane.requestFocus();
        repaint();
	}
	
	//Only enable scheduled updates on the panes that are active
	public void stateChanged(ChangeEvent e) {
		for (TabbedPaneActivator p: TabPanes){
			p.DeactivateTab();
		}
		TabPanes.get(DataTabs.getSelectedIndex()).ActivateTab();
		SnapshotMaker.setNode(TreeMaker.TreePanel.getFocusNode());
//	    SnapshotMaker.update(); //TEMPORARY REMOVE
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
