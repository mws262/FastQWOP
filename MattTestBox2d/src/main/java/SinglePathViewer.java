import java.util.ArrayList;
/**
 * 
 * Allows viewing of a single path leading up to a node while running the tree search.
 * 
 * 
 * @author Matt
 *
 */

public class SinglePathViewer {

	
	private ArrayList<TrialNode> queuedEndPoints = new ArrayList<TrialNode>();
	private QWOPInterface game;
	
	public SinglePathViewer() {
		game = new QWOPInterface();
	}
	/** Add an endpoint to visualize the path up to. These can be queued and will be sequentially run when RunQueued is called **/
	public void AddQueuedTrial(TrialNode endpt){
		queuedEndPoints.add(endpt);
	}
	/** Remove all elements from the queue **/
	public void ClearQueue(){
		queuedEndPoints.clear();
	}
	
	/** Execute all paths and sequentially empty the queue **/
	public void RunQueued(){
		
		//Do this for all queued paths.
		while(queuedEndPoints.size() > 0){
			
			//Travel up the nodes to get the full sequence of actions (since traveling up is unique, but traveling down isn't).
			TrialNode target = queuedEndPoints.get(0);
			int[] control = new int[target.TreeDepth]; //I MIGHT BE OFF BY ONE, CHECK THIS TODO
			for (int i = control.length-1; i >= 0; i--){
				control[i] = target.ControlAction;
				target = target.ParentNode;
			}
			

//			OptionsHolder.visOn = true;
//			game.repeatSequence = true;
			game.NewGame(true);
			try {
				game.DoSequence(control);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			queuedEndPoints.remove(0); //shift the queue down
		}
	}

}
