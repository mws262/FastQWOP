import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
//Note: periodic solutions with noise.
public class ExhaustiveQwop {

	public static boolean verbose = OptionsHolder.verboseOn;
	
	
	public static TrialNode RootNode;
	private static int depth;
	
	public static QWOPInterface QWOPHandler;
	
	private final static Random rand = new Random();
	
	public static ArrayList<Float> DistHolder = new ArrayList<Float>(); //keep a list of all the costs
	public static ArrayList<Float> ValHolder = new ArrayList<Float>(); //keep a list of all the costs

	public DataGrabber saveInfo;
	public VisMaster VisRoot;
	private SinglePathViewer SpecificViewer;
	private Scheduler Every8;
	private Scheduler EveryEnd;
	
	public CopyOnWriteArrayList<TreeHandle> trees; //Thread safe version of ArrayList to hopefully prevent concurrentmodification exceptions.
	
	private static TreeParameters tp; //The new version of OptionsHolder for parameters we might wish to change between different trees.
	public ExhaustiveQwop(CopyOnWriteArrayList<TreeHandle> trees) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		
		QWOPHandler = new QWOPInterface();
		//Give the specific run viewer access to the physics engine and game.
		SpecificViewer = new SinglePathViewer(QWOPHandler);
		this.trees = trees;
		// Node visualization stuff
		saveInfo = new DataGrabber();
		saveInfo.setInterval(50);
		Every8 = new Scheduler(); //This scheduler gets incremented every time we find a path that makes it out to 8 without falling.
		Every8.addTask(saveInfo);
		
		VisRoot = new VisMaster(QWOPHandler,trees,saveInfo,SpecificViewer);
		EveryEnd = new Scheduler(); //This scheduler gets incremented every time we fail.
		VisRoot.setInterval(1);
		VisRoot.TreeMaker.setInterval(100);
		VisRoot.TreeMaker.DistHolder = DistHolder; //Now the treemaker has a reference to all the value and distance numbers for scaling purposes.
		VisRoot.TreeMaker.ValHolder = ValHolder;

		VisRoot.DataMaker.setInterval(1000);
		
		EveryEnd.addTask(VisRoot);
		EveryEnd.addTask(VisRoot.TreeMaker);
		EveryEnd.addTask(VisRoot.SelectTreeMaker);
		EveryEnd.addTask(VisRoot.DataMaker);
		EveryEnd.addTask(SpecificViewer); //Every end of the path, see if we've queued up any specific paths to view by hand.
		
		Scheduler EveryPhys = new Scheduler();
		VisRoot.RunMaker.setInterval(1);
		EveryPhys.addTask(VisRoot.RunMaker);
		QWOPHandler.addScheduler(EveryPhys);

		

		//Hackish way of making sure that the specific run viewer has access to the Runner pane in the tabs.
		SpecificViewer.runPane = VisRoot.RunMaker;

		
	}
	
	public void RunGame(TreeParameters treeparams) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{

		//Parameters specific to the tree we're currently running.
		tp = treeparams;
		depth = tp.treeDepth;
		
		if(tp.limitDepth && tp.stochasticDepth){
			throw new RuntimeException("Can't both limit depth and do stochastic depth. Change this in the options holder");
		}
		boolean finished = false;
		
//		//Start the QWOP handler.
//		QWOPHandler = new QWOPInterface();
		
		//Create the root node.
		RootNode =  new TrialNode(tp);
		TreeHandle currentTree = new TreeHandle(RootNode);
		currentTree.focus =  true;
		trees.add(currentTree);
		VisRoot.TreeMaker.addTree(currentTree);
		TrialNode CurrentNode;
		TrialNode NextNode;
		float currentRecord = 0;
		int maxDepth = 0;
		int currentRootDepth = 0;
		
		long searchspace = 1;
		for (int i = 0; i<depth; i++){
			searchspace *= tp.ActionList[i%(tp.ActionList.length)].length;
		}
//		System.out.println("This will take a max of " + searchspace + "  evaluations assuming no failures.");

		int verboseIncrement = OptionsHolder.verboseIncrement;
		
		CurrentNode = RootNode;

		int counter = 0;
		boolean failed = false;
		boolean reachedEndLim = false;
		
		QWOPHandler.NewGame(OptionsHolder.visOn); //Get a new game going.
//		RootNode.CaptureState(QWOPHandler);
		int[] oldActions = {};
		int[] bufferNew = new int[5000]; //plenty big for storing new values since last fall.
		Arrays.fill(bufferNew, -1);
		int newGoodActions = 0;
		
		StateHolder BeginningState = new StateHolder(QWOPHandler);
		StateHolder EndState = new StateHolder(QWOPHandler);
		float LeastError = Float.MAX_VALUE;
		float NewError = Float.MAX_VALUE;
		

		//Entity which holds new nodes at the end of paths for ranking purposes. Mainly used when we're doing the weird MPC-ish thing.
		GoodNodes nodeRankHolder = new GoodNodes();
		TrialNode currentRoot = RootNode;
		ArrayList<NodeScorer> potentialPaths = new ArrayList<NodeScorer>();
		int sampleCount = 0; //used for counting stochastic depth search version.
		int tempcounter = 0;
		
		VisRoot.RunMaker.RunPanel.periodicLength = tp.periodicLength; // Also very hackish way of making the text formatting correct. the formatter must know prefix and periodic lengths.
		VisRoot.RunMaker.RunPanel.prefixLength = tp.prefixLength;
		SpecificViewer.periodicLength = tp.periodicLength;
		SpecificViewer.prefixLength = tp.prefixLength;
		VisRoot.SnapshotMaker.SnapshotPanel.prefixLength = tp.prefixLength;
		VisRoot.SnapshotMaker.SnapshotPanel.periodicLength = tp.periodicLength;
		
		while (tempcounter<1000){//!finished){
tempcounter++;
			
			//NOTE TO SELF -- NOW ADD SOMETHING WHICH SHIFts US DOWN THE TREE ONE SPOT AND MAKES THAT THE ROOT.
			/* If last time we failed, figure out the last good point we want to explore from and come up with the sequence of actions to get there */

			/**
			 * Decide a path to go down and test an action onward
			 */
			if (currentRoot.TempFullyExplored && !currentRoot.FullyExplored && tp.limitDepth){ //We've temp fully explored this branch. We go in one depth layer and do again for the best ones if we're doing this kind of exploration.
				System.out.println("Finished tree with root depth of " + currentRoot.TreeDepth + ". Going down 1.");
				RootNode.RemoveTempExploredFlag(); // Get rid of all the temporarily explored flags here and all branches down.
				currentRootDepth++; // Increase our fake root depth for purposes of bookkeeping
				//Now find the new sorta root to go to.
				
				NodeScorer[] topfew = nodeRankHolder.getTopX(100);

				if (currentRoot.NumChildren() == 1){
					currentRoot = currentRoot.GetChild(0);
				}else{
					for (int i = 0; i<topfew.length; i++){
						currentRoot = topfew[i].getUpstream(currentRootDepth);
						if(!currentRoot.FullyExplored){
							break;
						}
					}
				}
				if(currentRoot.FullyExplored){
					while(currentRoot.FullyExplored && !currentRoot.equals(RootNode)){
						currentRoot = currentRoot.ParentNode;
						System.out.println("backing up");
					}
					currentRootDepth = currentRoot.TreeDepth;
				}
				
				CurrentNode = currentRoot;
				VisRoot.TreeMaker.TreePanel.setOverride(currentRoot);
				
				nodeRankHolder.clearAll();
			}else if(currentRoot.FullyExplored && tp.limitDepth){
				RootNode.RemoveTempExploredFlag(); // Get rid of all the temporarily explored flags here and all branches down.

				while(currentRoot.FullyExplored && !currentRoot.equals(RootNode)){
					currentRoot = currentRoot.ParentNode;
					System.out.println("backing up");
				}
				currentRootDepth = currentRoot.TreeDepth;
				nodeRankHolder.clearAll();
				CurrentNode = currentRoot;
				VisRoot.TreeMaker.TreePanel.setOverride(currentRoot);
			}

			/**
			 * Handle getting to the start line
			 * 
			 */
			// If we want to start at a non-root node, then we need to run the actions to get there.
			if ((failed || reachedEndLim) && (CurrentNode.TreeDepth != 0)){ //If we failed earlier, we need to go back and do all the original actions before exploring the new path.
				oldActions = CurrentNode.getSequence();
				//If we failed earlier, we'll run all those old actions we figured out above.
				QWOPHandler.NewGame(OptionsHolder.visOn); //Start a new game.
				try {
					QWOPHandler.DoSequence(oldActions);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				failed = false;
			}else if(failed || reachedEndLim){ //we start at root and we're already where we want to start.
				QWOPHandler.NewGame(OptionsHolder.visOn); //Start a new game.
				failed = false;
				reachedEndLim = false;
			}
			
			//When marching down, if we find that the root node is fully explored, we exit.
			if (RootNode.FullyExplored){
				break;
			}
			
			

			
			NextNode = CurrentNode.SampleNew();
			//Now execute it.
			try {
				float cost = QWOPHandler.NextAction(NextNode.EchoControl());
				NextNode.SetScore(-cost);
				NextNode.SetSpeed(QWOPHandler.Speed());
				if(OptionsHolder.KeepStates){
					NextNode.CaptureState(QWOPHandler);
				}
				//Once we get past the prefix steps, we want to back up the state because we're looking for a set of [periodicset] parameters which results in something reasonably close to periodic.
				if(NextNode.TreeDepth == tp.prefixLength){
					BeginningState.CaptureState();
					
				}else if(NextNode.TreeDepth == tp.prefixLength + tp.periodicLength){ //The end of the periodic part
					EndState.CaptureState();
					
					NewError = EndState.Compare(BeginningState);
					NextNode.value = NewError; //Using the periodic error as the value for now.
					NextNode.bestInBranch = NextNode.rawScore;
					saveInfo.AddNonFailedNode(NextNode);
					ValHolder.add(NewError);
					Every8.Iterate();

					// announce good things.
					if (NewError < LeastError){
						
						if (verbose){
							System.out.println("New record low error: " + NewError);
						    System.out.print("Control used: ");
						    for (int k = 0; k<oldActions.length; k++){
						    	System.out.print(oldActions[k] + ", ");
						    }
						    int ct = 0;
						    while (!(bufferNew[ct]<0)){//If it is not my filler value, then there's another new value this run to report.
						    	System.out.print(bufferNew[ct] + ", ");
						    	ct++;
						    }
						    System.out.println(NextNode.EchoControl() + ".");
						}
						LeastError = NewError;
					}
				}
				
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
					    System.out.println(NextNode.EchoControl() + ".");
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		
			/**
			 * Check for failure.
			 */
			// We also fail based on the dude's state:
			failed = QWOPHandler.CheckFailure();// || (OptionsHolder.limitDepth && NextNode.TreeDepth == OptionsHolder.treeDepth);
			reachedEndLim = (tp.limitDepth && (NextNode.TreeDepth - currentRootDepth) == tp.treeDepth);
			/* Handle Failure or move down the tree if successful */
			

			if (failed || reachedEndLim){ //If we fall, then remove this new node and check to see if we've completed any trees.
//				CurrentNode.RemoveChild(NextNode);
				
				//Remove the dead node, and propagate back to see if we've fully explored any nodes.
				boolean ExploredFlag = false;
				if(failed){
					ExploredFlag = CurrentNode.RemoveChild(NextNode);
				}else{ //If it isn't a failure, we're just taking it out of commission temporarily due to a depth limit.
					NextNode.TempRemove();
					ExploredFlag = (CurrentNode.FullyExplored || CurrentNode.TempFullyExplored);
				}
				
				//If we are limiting the depth, it would be nice to keep a sortable holder for scores and such.
				if (tp.limitDepth || tp.stochasticDepth){
					nodeRankHolder.passNew(NextNode,currentRootDepth+1); //give this node AND the depth we would go back to if we picked this node. This lets us check if that one is fully explored or not.
				}
				
				
				DistHolder.add(-CurrentNode.rawScore);
				CurrentNode.PropagateHighScore(CurrentNode.rawScore); //Make the score run back to the end of the periodic part so we know the max discovered score from this node.
				if (CurrentNode.TreeDepth>maxDepth) { //Keep track of the deepest we've gone so far.
					maxDepth = CurrentNode.TreeDepth; //Update the tree depth if we manage to go deeper.
					if(maxDepth>VisRoot.TreeMaker.maxDepth){
						VisRoot.TreeMaker.maxDepth = maxDepth;
					}
				}
				//Plot the new tree nodes if this setting is on:
				if(OptionsHolder.treeVisOn){
					EveryEnd.Iterate(); //This iterates the scheduler which is on the schedule of every branch ending.
				}
				
				//For diagnostics, keep track of how many potential path have been eliminated by failures.
				if (verbose){ //Don't bother unless we're spitting out this diagnostic info.
					int removedsearchspace = 1;
					for (int i = NextNode.TreeDepth; i<depth; i++){
						removedsearchspace *= tp.ActionList[i%(tp.ActionList.length)].length;
					}
					searchspace -= (removedsearchspace-1); //keep an extra one for the node we're at.
				}
				
				Arrays.fill(bufferNew, -1); //We failed, so clear out the buffer of new good actions.
				newGoodActions = 0; //no new actions that are good anymore.
				
				/*
				 * Stochastic depth stuff
				 */
				if(tp.stochasticDepth){
					sampleCount++;
					TrialNode oldroot = currentRoot;
					TrialNode selectedEnd;
					int sampleLimit = tp.sampleCount-(currentRoot.TreeDepth*4);
					if (sampleLimit<10) sampleLimit = 10; //Ensure that we always sample at least 10 before moving on.
					if(sampleCount>sampleLimit || currentRoot.FullyExplored){
						currentRootDepth = (currentRootDepth+tp.forwardJump);
						sampleCount = 0;
						ArrayList<NodeScorer> topfew = nodeRankHolder.getFilteredTopX(currentRoot,20);
					
						
						if (currentRoot.NumChildren() == 1){
							currentRoot = currentRoot.GetChild(0);
						}else{
							
							NodeScorer sh = nodeRankHolder.getFromNode(oldroot);
							if(sh != null){
								sh.getNode().colorOverride = Color.BLACK;
								potentialPaths.remove(sh);
							}
							
							int goodptcount = 0;
							for (int i = 0; i<topfew.size(); i++){
								currentRoot = topfew.get(i).getUpstream(currentRootDepth);
								if(!currentRoot.FullyExplored && !potentialPaths.contains(topfew.get(i))){ //Make sure that the upstream root is not fully explored and that the end node isn't already in the list.

									potentialPaths.add(topfew.get(i));
									topfew.get(i).getNode().colorOverride = Color.green;
									goodptcount++;
									if(goodptcount >tp.multiPointCount){
										break;
									}
								}
							}
							//Trim down the potentialPaths here
							if(tp.trimSimilar){
							for (int j = 0; j<potentialPaths.size()-1; j++){
								for (int k = j+1; k<potentialPaths.size(); k++){
									
									int[] set1 = potentialPaths.get(j).getNode().getSequence();
									int[] set2 = potentialPaths.get(k).getNode().getSequence();
									int matchCount = 0;
									for(int m = 0; m<set1.length; m++){
										if (set1[m] != set2[m]){
											break;
										}else{
											matchCount++;
										}
									}
									
									//If the two match to 50% of the longer string, then eliminate one.
									if(matchCount > 0.5*(set1.length>set2.length ? set1.length : set2.length)){
										System.out.println("eliminated 1");
										if(potentialPaths.get(j).totalScore()>potentialPaths.get(k).totalScore()){
											potentialPaths.get(k).getNode().colorOverride = Color.BLACK;
											potentialPaths.remove(k);
										}else{
											potentialPaths.get(j).getNode().colorOverride = Color.BLACK;
											potentialPaths.remove(j);
										}
									}
								}
							}
							}

							NodeScorer end = potentialPaths.get(0);
							if(!tp.multiSelection){
								Collections.sort(potentialPaths);
								end = potentialPaths.get(0);
							}else if(tp.weightedSelection){
								//Find the total score of all saved paths so we can do a random, weighted selection.
								double totalScore = 0;
								double offset = 0;
								for (NodeScorer p: potentialPaths){
									totalScore += p.totalScore();
									if(p.totalScore()<offset){ //If we have values below 0, we want to go back and add an offset to all of them.
										offset = p.totalScore();
									}
								}
								totalScore -= offset*(double)potentialPaths.size(); // Add the offset for each of the potential paths.
								
								
								double selection = rand.nextDouble()*totalScore;
								double cumulativeScore = 0;
								for (NodeScorer p: potentialPaths){
									cumulativeScore += p.totalScore()-offset;
									if(cumulativeScore>selection){
										end = p;
										break;
									}
								}
							}else{
								//equal random selection
								end = potentialPaths.get(randInt(0,potentialPaths.size()-1));
							}

							
							selectedEnd = end.getNode();
							currentRootDepth = selectedEnd.TreeDepth - tp.stochasticHorizon;
							if(currentRootDepth < 2) currentRootDepth = 0;
							//System.out.println(currentRootDepth);
							currentRoot = end.getUpstream(currentRootDepth);
							if(selectedEnd.TreeDepth-currentRoot.TreeDepth<tp.stochasticHorizon){
								System.out.println("Too close to the end. Using same as old root node.");
								currentRoot = oldroot;
							}

						}

						if(currentRoot.FullyExplored){
							while(currentRoot.FullyExplored && !currentRoot.equals(RootNode)){
								for (int n = 0; n<tp.backwardJump; n++){
									currentRoot = currentRoot.ParentNode;
								}	
							}
							currentRootDepth = currentRoot.TreeDepth;
						}
						VisRoot.TreeMaker.TreePanel.setOverride(currentRoot);
						CurrentNode = currentRoot;
					}

				}

				
				/**
				 * Pick a node to return to after going to the end of a branch (i.e. one single run).
				 * 
				 */
				
				if(VisRoot.TreeMaker.Override){ //This lets us explore a specific branch by overriding which node the process resets to.

						VisRoot.TreeMaker.OverrideNode.ColorChildren(Color.RED);
						//Once we've exhausted our options, set back to root node and turn off this search option in the treemaker.
						CurrentNode = currentRoot;
						if(CurrentNode.FullyExplored || CurrentNode.DeadEnd){
							VisRoot.TreeMaker.OverrideNode.ColorChildren(Color.BLACK);
						
						}
				}else if(tp.marchUp){
					//This method marches BACK UP the tree until we find an unexplored node to try.
					while (ExploredFlag){ //Keep marching up the layers until we find one that isn't fully explored
		
						if (ExploredFlag && CurrentNode.TreeDepth==0){ //If the cur
							System.out.println("We've reached the top level.");
							finished = true;
							break; // We've reached the parent level and everything is explored.
						}else if (CurrentNode.TreeDepth==0){ //We're at the top layer, but there's still more to explore.
							break;
						}
						CurrentNode = CurrentNode.ParentNode;
						ExploredFlag = (CurrentNode.FullyExplored || CurrentNode.TempFullyExplored);
		
					}
				}else{
					//This method marches DOWN the tree.
					CurrentNode = RootNode;
				}
				
			}else{
				/**
				 * We didn't fail this action, buffer good actions and move down the tree further.
				 */
				//Record this good new action
				bufferNew[newGoodActions] = NextNode.EchoControl();
				newGoodActions++;
				CurrentNode = NextNode; //If there's more to explore, then keep going down the tree.
			}	
			if ((failed || reachedEndLim) && verbose && (counter>=verboseIncrement && counter%verboseIncrement == 0)){ //Spit out progress if verbose.
				System.out.println("We are " + counter + " runs through the search. Worst case: " + (float)counter/(float)searchspace*100f + "% complete.");
				
			}
			if (failed || reachedEndLim){
				counter++; //Right now, the counter is just recording complete runs (ie until the failure occurs.
				OptionsHolder.gamesPlayed++;
			}
		}
		//Final info after the ENTIRE tree is explored.
		String report = "Final iterations: " + counter;
		if (verbose) report += ". Search space reduced to: " + searchspace; System.out.println(report);
	}
	
	public void idleGraphics(){
		while(true){
		Every8.Iterate();
		EveryEnd.Iterate();
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	
	//Generate a random integer between two values, inclusive.
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
}
