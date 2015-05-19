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
	public final int ControlIndex;
	/** Actual numeric control action **/
	public final int ControlAction; //Actual delay used as control.
	
	/** Node which leads up to this node. **/
	public final TrialNode ParentNode; // The parent action may not be modified.
	
	/** Keep track of where this node lies in the sequence **/
	private final int NodeSequence; 
	
	/** How deep is this node down the tree? 0 is root. **/
	public final int TreeDepth;
	
	/** How many children this node could potentially have if all are viable **/
	public final int PotentialChildren;
	
	/** Keep track of existing child nodes. Child nodes are deleted when they are failures. **/
	private ArrayList<TrialNode> ChildNodes = new ArrayList<TrialNode>();
	
	/** Array of booleans indicating whether a potential child node has been visited **/
	private boolean[] TestedChildren; //This keeps track of which child nodes we've tried.
	
	/** Parameters for visualizing the tree **/
	public int[] nodeLocation = new int[2]; //Location that this node appears on the tree visualization
	public double nodeAngle = 0; //Keep track of the angle that the line previous node to this node makes.
	public double sweepAngle = 2*Math.PI;
	
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
		
		PotentialChildren = ActionList[NodeSequenceNext-1].length;
		
		if(OptionsHolder.treeVisOn){
			CalcNodePos();
		}
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
		
		PotentialChildren = ActionList[0].length;
		
		
		//For the root node, the point is simply the origin as defined by the center of the window.
		nodeLocation[0] = OptionsHolder.growthCenter[0];
		nodeLocation[1] = OptionsHolder.growthCenter[1];
	}
	
	
	///// Tree visualization methods begin /////
	
	/** Just for node visualization -- calculate the new node location on the diagram **/
	public void CalcNodePos(){
		//Angle of this current node -- parent node's angle - half the total sweep + some increment so that all will span the required sweep.
		if(TreeDepth == 0){ //If this is the root node, we shouldn't change stuff yet.
			if (PotentialChildren > 1 ){ //Catch the div by 0
				nodeAngle = 0. - sweepAngle/2. + (double)ControlIndex * sweepAngle/(double)(PotentialChildren-1);
			}else{
				nodeAngle = 0;
			}
		}else{
		
			if (ParentNode.PotentialChildren > 1){ //Catch the div by 0
				sweepAngle = ParentNode.sweepAngle/(2);
				nodeAngle = ParentNode.nodeAngle - sweepAngle/2. + (double)ControlIndex * sweepAngle/(double)(ParentNode.PotentialChildren-1);
			}else{
				sweepAngle = ParentNode.sweepAngle; //Only reduce the sweep angle if the parent one had more than one child.
				nodeAngle = 0;//sweepAngle/(ParentNode.PotentialChildren-1); //TODO make sure this is right, but if we have 1 or less child nodes from the parent, then the angle should go straight out in the direction the parent was also going.
			}
		}

		nodeLocation[0] = (int) (ParentNode.nodeLocation[0] + OptionsHolder.edgeLength*Math.cos(nodeAngle));
		nodeLocation[1] = (int) (ParentNode.nodeLocation[1] + OptionsHolder.edgeLength*Math.sin(nodeAngle));
	}
	
	/** Get the list of all visualization nodes lines which lie at and below this one. **/
	public LineHolder GetNodeLines(){ //This is the top level one which has no arguments for the sake of simplicity.
		int LineCount = SumLines(); //Figure out how many lines should exist from this node on.

		//Create a new holder for all the lines we're going to figure out.
		LineHolder LineList = new LineHolder(LineCount);

		GetNodeLines(LineList); //TODO make sure that even though we don't use the output of getnodelines() that it's ok. I think we're just altering the same object, so we shouldn't have to pass the output back up the chain.
		
		return LineList;
	}
	
	/** Get all the visualization nodes which lie at and below this one. This one is not the top level one and should not be user-called. **/
	public LineHolder GetNodeLines(LineHolder lines){ //Takes the existing line list and appends some more. Passes it then further down.
		
		for (int i = 0; i<ChildNodes.size(); i++){//Iterate through all existing children and add lines from this node to those children.
			lines.AddLine(this, ChildNodes.get(i)); //Add a line between this node and all the existing children.
			ChildNodes.get(i).GetNodeLines(lines); //Recurse to all the children too. This should propagate all the way down the tree.
		}
		return lines;
	}
	
	
	/** Count all lines which branch from this one and keep summing them up recursively **/
	public int SumLines(){
		int sum = ChildNodes.size(); //Number of lines branching from this node are the number of active children of this node.
		
		for (int i = 0; i<ChildNodes.size(); i++){
			sum += ChildNodes.get(i).SumLines(); //Now recurse downward to all the active children. Add this to the sum of lines.	
		}
		
		return sum;
		
	}
	
	///// Tree visualization methods end ///////
	
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
		if(OptionsHolder.sampleRandom){ //If sampling randomly, then we make a list of all possible unexplored nodes at this level and just pick these randomly.
			int[] unexplored = new int[ChildNodes.size()]; //Keep track of the indices of all unexplored nodes. This array is likely oversized for safety's sake.
			int unexploredCount = 0;
			Random randgen = new Random();
			
			for (int i = 0; i<ChildNodes.size(); i++){
				if(	!ChildNodes.get(i).FullyExplored ){
					unexplored[unexploredCount] = i;
					unexploredCount++;
				}
			}
			int selection = unexplored[randgen.nextInt(unexploredCount)];
			
			newNode = ChildNodes.get(selection);
			return newNode;
			
			
		}else{ //If we don't want to sample randomly amongst the not-fully-explored nodes, then we just grab the first one. This is more efficient, but not as broad of a search.
			for (int i = 0; i<ChildNodes.size(); i++){
				if(	!ChildNodes.get(i).FullyExplored ){
					newNode = ChildNodes.get(i);
					return newNode;
				}
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
