import java.util.ArrayList;
/**
 * 
 * Allows viewing of a single path leading up to a node while running the tree search.
 * 
 * @author Matt
 *
 */

public class SinglePathViewer implements Schedulable{

	
	private ArrayList<TrialNode> queuedEndPoints = new ArrayList<TrialNode>();
	private QWOPInterface game;
	private int interval = 1;
	public RunnerPaneMaker runPane;
	public String sequence = "";
	
	public SinglePathViewer(QWOPInterface game) {
		this.game = game;
	}
	/** Add an endpoint to visualize the path up to. These can be queued and will be sequentially run when RunQueued is called **/
	public void AddQueuedTrial(TrialNode endpt){
		ClearQueue(); //NOTE -- FOR NOW I DON'T WANT MORE THAN ONE IN THE QUEUE SINCE IT'S CONFUSING. I'm leaving it in now in case I change my mind.
		queuedEndPoints.add(endpt);
	}
	/** Remove all elements from the queue **/
	public void ClearQueue(){
		queuedEndPoints.clear();
	}
	
	/** Execute all paths and sequentially empty the queue **/
	private void RunQueued(){
		if(runPane != null){

		//Do this for all queued paths.
		while(queuedEndPoints.size() > 0 && runPane.isActiveTab()){ //Wait to burn through the queue until this pane is even active.
			runPane.disable = false;
			//Travel up the nodes to get the full sequence of actions (since traveling up is unique, but traveling down isn't).
			TrialNode target = queuedEndPoints.get(0);
			int[] control = new int[target.TreeDepth]; //I MIGHT BE OFF BY ONE, CHECK THIS TODO
			for (int i = control.length-1; i >= 0; i--){
				control[i] = target.ControlAction;
				target = target.ParentNode;
			}
			
			
			//Turn the sequence we're about to do into a string for display.
			sequence = "";
			for (int i = 0; i<control.length-1; i++){
				sequence += (control[i] + ", ");	
			}
			sequence += (control[control.length-1] + "");	
			
			runPane.RunPanel.setLabel(sequence);
//			OptionsHolder.visOn = true;
//			game.repeatSequence = true;
			game.NewGame(true);
			try {
				game.DoSequence(control);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sequence = "";
			runPane.RunPanel.setLabel(sequence);
			queuedEndPoints.remove(0); //shift the queue down
			runPane.disable = true;
		}

		}
	}
	@Override
	public void setInterval(int interval) {
		this.interval = interval;
		
	}
	@Override
	public int getInterval() {
		return interval;
	}
	@Override
	public void DoScheduled() { //Every scheduled interval, run all the queued paths.
		RunQueued();
		
	}
	@Override
	public void DoEvery() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void DoNow() {
		RunQueued();
		
	}
	@Override
	public void Disable() {
		// TODO Auto-generated method stub
		
	}

}
