package org.jbox2d.testbed.framework.jogl;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ExhaustiveQwop {

	public static boolean verbose = true;
	
	private static TrialNode RootNode;
	private static int depth = 8;
	
	private static QWOPInterface QWOPHandler;
	
	public ExhaustiveQwop() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String args[]){
		
		boolean finished = false;
		
		//Start the QWOP handler.
		QWOPHandler = new QWOPInterface();
		
		//Create the root node.
		RootNode =  new TrialNode();
		TrialNode CurrentNode;
		TrialNode NextNode;
		float currentRecord = 0;
		
		int searchspace = (int)(Math.pow((double)TrialNode.ActionList1.length, Math.ceil((double)depth/2.))*Math.pow((double)TrialNode.ActionList2.length, Math.floor((double)depth/2.)));
		System.out.println("This will take a max of " + searchspace + "  evaluations assuming no failures.");

		int verboseIncrement = 100;
		
		//I guess I'm doing breadth first before I figure out depth first.
//		TrialNode newNode = RootNode.SampleNew();
		CurrentNode = RootNode;
		int counter = 0;
		boolean failed = false;
		
		QWOPHandler.NewGame(); //Get a new game going.
		int[] oldActions = {};
		int[] bufferNew = new int[50]; //plenty big for storing new values since last fall.
		Arrays.fill(bufferNew, -1);
		int newGoodActions = 0;
		while (!finished){

			/* If last time we failed, figure out the last good point we want to explore from and come up with the sequence of actions to get there */

			//Also, we don't need to reconstruct the path if we're choosing a new action right at the starting decision.
			if (failed && (CurrentNode.TreeDepth != 0)){ //If we failed earlier, we need to go back and do all the original actions before exploring the new path.
				oldActions = new int[CurrentNode.TreeDepth];
				oldActions[oldActions.length-1] = CurrentNode.EchoControl(); //Last cached action will be the current node's action.
				
				NextNode = CurrentNode.ParentNode;
				for (int i = 0; i<oldActions.length-1; i++){ //Now go back through all the parent nodes to find their actions up the tree
					oldActions[oldActions.length-2-i] = NextNode.EchoControl();
					NextNode = NextNode.ParentNode;
				}
			
				//If we failed earlier, we'll run all those old actions we figured out above.
				QWOPHandler.NewGame(); //Start a new game.
				try {
					QWOPHandler.DoSequence(oldActions);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				failed = false;
			}
			
			/* Sample a new. unexplored node. */
			NextNode = CurrentNode.SampleNew();
			//Now execute it.
			try {
				float cost = QWOPHandler.NextAction(NextNode.EchoControl());
				NextNode.SetScore(-cost);
				if(-cost>currentRecord){
					currentRecord = -cost;
					if (verbose){
						System.out.println("New record distance: " + currentRecord);
					    System.out.print("Control used: ");
					    for (int k = 0; k<oldActions.length; k++){
					    	System.out.print(oldActions[k] + ", ");
					    }
	
					    int ct = 0;
					    while (!(bufferNew[ct]<0)){//If it is not my filler value, then there's another new value this run to report.
					    	System.out.print(bufferNew[ct] + ", ");
					    	ct++;
					    }
//					    System.out.print("[[[[[" + ct + "." + oldActions.length+"[[[[[");
					    System.out.println(NextNode.EchoControl() + ".");
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		
			/* Check Failure */
			failed = NextNode.TreeDepth==depth; //We auto "fail" if we try to expand the tree beyond the specified depth. This is probably inefficient, since we knowthis failure without going through all the steps.
			// We also fail based on the dude's state:
			failed = failed || QWOPHandler.CheckFailure();
			
			/* Handle Failure or move down the tree if successful */
			
			if (failed){ //If we fall, then remove this new node and check to see if we've completed any trees.
				boolean ExploredFlag = CurrentNode.RemoveChild(NextNode);
				
				//For diagnostics, keep track of how many potential path have been eliminated by failures.
				if (verbose){ //Don't bother unless we're spitting out this diagnostic info.
					if (NextNode.TreeDepth%2 == 0){ //Bad node is even, hence the first one after would have been odd.
						searchspace -= Math.pow(TrialNode.ActionList1.length,Math.ceil((depth-NextNode.TreeDepth)/2.))*Math.pow(TrialNode.ActionList2.length,Math.floor((depth-NextNode.TreeDepth)/2.))-1;
					}else{ //bad node was odd. It's next action would have been from the even set.
						searchspace -= Math.pow(TrialNode.ActionList2.length,Math.ceil((depth-NextNode.TreeDepth)/2.))*Math.pow(TrialNode.ActionList1.length,Math.floor((depth-NextNode.TreeDepth)/2.))-1;		
					}
				}
				
				Arrays.fill(bufferNew, -1); //We failed, so clear out the buffer of new good actions.
				newGoodActions = 0; //no new actions that are good anymore.
							
				while (ExploredFlag){ //Keep marching up the layers until we find one that isn't fully explored
	
					if (ExploredFlag && CurrentNode.TreeDepth==0){
						System.out.println("We've reached the top level.");
						finished = true;
						break; // We've reached the parent level and everything is explored.
					}else if (CurrentNode.TreeDepth==0){ //We're at the top layer, but there's still more to explore.
						break;
					}
					CurrentNode = CurrentNode.ParentNode;
					ExploredFlag = CurrentNode.FullyExplored;
				}
		
			}else{
				//Record this good new action
				bufferNew[newGoodActions] = NextNode.EchoControl();
				newGoodActions++;
				CurrentNode = NextNode; //If there's more to explore, then keep going down the tree.
			}	
			if (verbose && (counter>=verboseIncrement && counter%verboseIncrement == 0)){ //Spit out progress if verbose.
				System.out.println("We are " + counter + " runs through the search. Worst case: " + (float)counter/(float)searchspace*100f + "% complete.");
				
			}
			if (failed){
				counter++; //Right now, the counter is just recording complete runs (ie until the failure occurs.
			}
		}
		//Final info on iterations.
		String report = "Final iterations: " + counter;
		if (verbose) report += ". Search space reduced to: " + searchspace;
	}
}
