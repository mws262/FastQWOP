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
	
	public String prefixLabel = "";
	public String periodicLabel = "";
	public ArrayList<String> deviationLabel = new ArrayList<String>();
	
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
			int[] control = target.getSequence();
//			int[] control = new int[target.TreeDepth]; //I MIGHT BE OFF BY ONE, CHECK THIS TODO
//			for (int i = control.length-1; i >= 0; i--){
//				control[i] = target.ControlAction;
//				target = target.ParentNode;
//			}
			
			
			//Turn the sequence we're about to do into a string for display.
//			sequence = "";
//			for (int i = 0; i<control.length-1; i++){
//				sequence += (control[i] + ", ");	
//			}
//			sequence += (control[control.length-1] + "");	
//			
//			
			periodicLabel = "";
			prefixLabel = "";
			deviationLabel.clear();
			
			for (int i = 0; i<OptionsHolder.prefixLength; i++){
				if (i>control.length-1) break;
				String divider = ", ";
				if (i< OptionsHolder.prefixLength-1 && i < control.length-1){
					if(control[i+1]<10){
						divider += "  "; //If it's a single digit number, then add an extra space to make things even out.
					}
				}
				prefixLabel += (control[i] + divider);
//				if (i == OptionsHolder.prefixLength-2){
//					prefixLabel += (control[OptionsHolder.prefixLength-1] + " "); //No trailing comma on this one.
//				}
			}
			for (int i = OptionsHolder.prefixLength; i<OptionsHolder.prefixLength+OptionsHolder.periodicLength; i++){
				if (i>control.length-1) break;
				String divider = ", ";
				if (i< OptionsHolder.prefixLength+OptionsHolder.periodicLength-1 && i < control.length-1){
					if (control[i+1]<10){
						divider += "  "; //If it's a single digit number, then add an extra space to make things even out.
					}
				}
				periodicLabel += ( control[i] + divider );
			}
			int count = 0;
			String devElement = "";
			for (int i = OptionsHolder.prefixLength + OptionsHolder.periodicLength; i<control.length; i++){
//				if (i>=control.length-1) break;
				String divider = ", ";
				if (i< control.length-1){
					if (control[i+1]<10){
						divider += "  "; //If it's a single digit number, then add an extra space to make things even out.
					}
				}
				devElement += ( control[i] + divider );
				count++;
				if(count == OptionsHolder.periodicLength || i == control.length-1){ // add each periodic+deviation set as a separate string in the arraylist.
					deviationLabel.add(devElement);
					devElement = "";
					count = 0;
				}
			}
			
			
			runPane.RunPanel.setLabel(prefixLabel,periodicLabel,deviationLabel);
//			OptionsHolder.visOn = true;
//			game.repeatSequence = true;
			game.NewGame(true);
			try {
				game.DoSequence(control);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			periodicLabel = "";
			prefixLabel = "";
			deviationLabel.clear();
			
			runPane.RunPanel.setLabel(prefixLabel,periodicLabel,deviationLabel);
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
