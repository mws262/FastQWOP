import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TrialNode {
	
	/** Raw score of this node's state **/
	private float rawScore = 0;
	/** Differential score (thisScore-prevStateScore) **/
	private float diffScore = 0;
	/**Estimated value of this node in a global sense (UNIMPLEMENTED) **/
	private float value = 0; //TODO
	
	/** Is this state a dead end (based on failures) **/
	boolean DeadEnd = false;
	/** Is this node currently fully explored? i.e. everything below it has been explored **/
	boolean FullyExplored = false;
	
	/** Index of the control inside of the supplied set of control options in OptionsHolder **/
	private final int ControlIndex;
	/** Actual numeric control action **/
	private final int ControlAction; //Actual delay used as control.
	
	/** Node which leads up to this node. **/
	public final TrialNode ParentNode; // The parent action may not be modified.
	
	/** Keep track of where this node lies in the sequence **/
	private final int NodeSequence; 
	
	/** How deep is this node down the tree? 0 is root. **/
	public final int TreeDepth;
	
	/** Keep track of existing child nodes. Child nodes are deleted when they are failures. **/
	private ArrayList<TrialNode> ChildNodes = new ArrayList<TrialNode>();
	
	/** Array of booleans indicating whether a potential child node has been visited **/
	private boolean[] TestedChildren; //This keeps track of which child nodes we've tried.
	
	
	//Action list order and actions.
	// for 1st depth, will pick one of 1st set of actions, etc.
	// for trees greater depth than number of arrays below, it will back to the top.
	public static final int[][] ActionList = OptionsHolder.ActionList;

	/** Constructor for making any nodes below the root node **/
	public TrialNode(TrialNode ParentAction, int ControlIndex) {
		this.ParentNode = ParentAction;

		TreeDepth = ParentAction.TreeDepth + 1; // When we make nodes, we go down the tree.
		this.ControlIndex = ControlIndex;
		
		NodeSequence = (ParentAction.NodeSequence%ActionList.length) + 1;
		int NodeSequenceNext = ((ParentAction.NodeSequence + 1)%ActionList.length) + 1; //next action might wrap around.
		
		ControlAction = ActionList[NodeSequence-1][ControlIndex];
		TestedChildren = new boolean[ActionList[NodeSequenceNext-1].length]; //children belong to 2.
		Arrays.fill(TestedChildren, false);
		
	}
	
	/** Constructor for creating a root node. **/
	public TrialNode() {
		System.out.println("Root node made. This message should only show once.");
		ParentNode = null;
		ControlIndex = -1; // This is just to make sure that this index never gets used if this is the root node.
		NodeSequence = 0; //The root node is treated as even just to make sure later nodes work right.
		TestedChildren = new boolean[ActionList[0].length];
		Arrays.fill(TestedChildren, false);
		ControlAction = -1;
		TreeDepth = 0;
	}
	
	/** Return the control action (NOT the index of the control) **/
	public int EchoControl(){
		return ControlAction;
	}
	
	/** Add a new unvisited node. **/
	public TrialNode SampleNew(){ //TODO add checking to make sure that this doesn't do weird things if all nodes are already sampled (should never occur).
		TrialNode newNode = null;
		//First try to get a completely untested node.
		for (int i = 0; i<TestedChildren.length; i++){
			if (!TestedChildren[i]){ //If the value is unused so far.
				TestedChildren[i] = true; //We've added this node and now we're checking it off the list.
				//Create the new object.
				newNode = new TrialNode(this,i);
				ChildNodes.add(newNode);
				return newNode;
			}

		}
		//If that doesn't work, then try to find an unexplored node.
		for (int i = 0; i<ChildNodes.size(); i++){
			if(	!ChildNodes.get(i).FullyExplored ){
				newNode = ChildNodes.get(i);
				return newNode;
			}
		}

		throw new RuntimeException("Error in sampling a node. Couldn't find an unexplored or untested node.");
	}

	/** Return a specific child node by index **/
	public TrialNode GetChild(int index){
		return ChildNodes.get(index);
	}
	/** Return the number of children below this one **/
	public int NumChildren(){
		return ChildNodes.size();
	}
	/** Remove a specific child node. ALSO check if this node and any above it have now become fully explored. **/
	public boolean RemoveChild(TrialNode DeadNode){ //Now returns whether this node is fully explored too.
		ChildNodes.remove(DeadNode);
		CheckExplored();
		return FullyExplored;
	}
	
	/** Change whether this node or any above it have become fully explored. (mostly automatically called when deleting children). **/
	public boolean CheckExplored(){
		boolean FullyExplored = true;
		for (int i = 0; i<TestedChildren.length; i++){
			if (!TestedChildren[i]){ //If we find a possible option that's untested, then this node is not fully explored
				FullyExplored = false;
				
				break;
			}
		}
		if (FullyExplored){ //If we've visited all nodes, then we should check whether all the nodes underneath are explored or not.

			if (ChildNodes.size() == 0){
				DeadEnd = true; // tried all possible actions, yet there are no nodes underneath. This is the end of this branch.
			}else{
				for (int i = 0; i<ChildNodes.size(); i++){
					if (!ChildNodes.get(i).FullyExplored){ // If we run into a non-explored node, then this node is not fully explored.

						FullyExplored = false;
						break;
					}
				}
			}
		}
		
		this.FullyExplored = FullyExplored;
		
		if (FullyExplored && TreeDepth>0){
			ParentNode.CheckExplored(); //If this one is fully explored, we should also check its parent.
		}

		return FullyExplored;
	}
	
	/** Record the raw score (cost function evaluation) and the delta score. **/
	public void SetScore(float score){
		rawScore = score;
		diffScore = rawScore - ParentNode.rawScore;
	}
}
