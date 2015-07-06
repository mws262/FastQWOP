import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class TrialNode {
	
	/** if this branch is being viewed separately, we will also be calculating alternative node positions, etc **/
	public boolean altTree = false;
	
	/** Raw score of this node's state **/
	public float rawScore = 0;
	/** Differential score (thisScore-prevStateScore) **/
	private float diffScore = 0;
	/**Estimated value of this node in a global sense (UNIMPLEMENTED) **/
	public float value = 0; //TODO
	
	public float bestInBranch = 0;
	
	/** Estimated speed dist/phys steps **/
	public float speed = 0;
	
	/** Is this state a dead end (based on failures) **/
	boolean DeadEnd = false;
	
	/** If we limit, then later want to expand the tree, we need a flag to temporarily call this a dead end. **/
	public boolean TempFullyExplored = false;
	
	/** Is this node currently fully explored? i.e. everything below it has been explored **/
	boolean FullyExplored = false;
	
	/** Failure mode (if applicable) **/
	public StateHolder.FailMode FailType = StateHolder.FailMode.UNFAILED;
	
	/** Color for this line **/
	public Color nodeColor;
	
	/** Index of the control inside of the supplied set of control options in OptionsHolder **/
	public final int ControlIndex;
	/** Actual numeric control action **/
	public final int ControlAction; //Actual delay used as control.
	
	/** Node which leads up to this node. **/
	public final TrialNode ParentNode; // The parent action may not be modified.
	
	/** What is the state after taking this node's action? **/
	public StateHolder NodeState;
	
	/** Keep track of where this node lies in the sequence **/
	private final int NodeSequence; 
	
	/** How deep is this node down the tree? 0 is root. **/
	public final int TreeDepth;
	
	/** How many children this node could potentially have if all are viable **/
	public final int PotentialChildren;
	
	/** Black means no override. If something else, it will be drawn with a dot of that color **/
	public Color colorOverride = Color.BLACK;
	
	/** Keep track of existing child nodes. Child nodes are deleted when they are failures. **/
	private ArrayList<TrialNode> ChildNodes = new ArrayList<TrialNode>();
	
	/** Array of booleans indicating whether a potential child node has been visited **/
	private boolean[] TestedChildren; //This keeps track of which child nodes we've tried.
	
	/** Random number generator for new node selection **/
	private static Random randgen = new Random();
	
	/** Parameters for visualizing the tree **/
	public float[] nodeLocation = new float[2]; //Location that this node appears on the tree visualization
	public double nodeAngle = 0; //Keep track of the angle that the line previous node to this node makes.
	public double sweepAngle = 2*Math.PI;
	public boolean LabelOn = false; //Will this node have a text label?
	public boolean hiddenNode = false; //Should we not draw this node and all children?
	
	/** These node locations are secondary for if this node is part of a subview tree (i.e. rarely used) **/
	public float[] nodeLocation2 = new float[2]; //Location that this node appears on the tree visualization
	public double nodeAngle2 = 0; //Keep track of the angle that the line previous node to this node makes.
	public double sweepAngle2 = 2*Math.PI;
	
	public TreeParameters tp;
	
	public float height = 0;
	
	//Action list order and actions.
//	public static final int[][] ActionList = OptionsHolder.ActionList;
	
//	public static final int[][] DeviationsList = OptionsHolder.DeviationList;

	public int[] OurPeriodicChoice;
	
	/** Constructor for making any nodes below the root node **/
	public TrialNode(TrialNode ParentAction, int ControlIndex) {
		this.ParentNode = ParentAction;
		tp = ParentNode.tp; //Make sure the same tree parameters are inherited all the way down.
		height = tp.TreeLevel;
		int prefix = tp.prefixLength;
		int periodic = tp.periodicLength;
		OurPeriodicChoice = new int[tp.periodicLength];
		
		TreeDepth = ParentAction.TreeDepth + 1; // When we make nodes, we go down the tree.
		this.ControlIndex = ControlIndex;
		
		//Do we repeat the last 4 elements over and over or do we start the whole sequence over.
		if(tp.repeatSelectionInPeriodic && ParentNode.NodeSequence >= prefix){ //Only do this if we're past the prefix.
			
			NodeSequence = (ParentAction.NodeSequence-prefix)%periodic + 1 + prefix;
			int NodeSequenceNext = (ParentAction.NodeSequence-prefix + 1)%periodic + 1+ prefix;
			
			ControlAction = tp.ActionList[NodeSequence - 1][ControlIndex];
			TestedChildren = new boolean[tp.ActionList[NodeSequenceNext-1].length]; //children belong to 2.
			Arrays.fill(TestedChildren, false);
			
			PotentialChildren = tp.ActionList[NodeSequenceNext-1].length;
			
		}else if(tp.goDeviations && ParentNode.NodeSequence >= prefix+periodic){
			//We've made it past both periodic and prefix. Now we switch back to the same periodic one with a bit of deviation based on index.
			OurPeriodicChoice = ParentNode.OurPeriodicChoice; //Copy the period from the previous node.
			
			NodeSequence = (ParentAction.NodeSequence-prefix-periodic)%periodic + periodic + prefix + 1;
			int NodeSequenceNext = (ParentAction.NodeSequence-prefix-periodic + 1 )%periodic + periodic + prefix + 1;
			ControlAction = tp.DeviationList[NodeSequence - 1 - periodic - prefix][ControlIndex] + OurPeriodicChoice[NodeSequence - 1 - periodic - prefix];
			TestedChildren = new boolean[tp.DeviationList[NodeSequenceNext-1 - periodic - prefix].length]; //children belong to 2.
			Arrays.fill(TestedChildren, false);
			
			PotentialChildren = tp.DeviationList[NodeSequenceNext-1 - periodic - prefix].length;
			
//			for (int i = 0; i<OurPeriodicChoice.length; i++){
//				System.out.print(OurPeriodicChoice[i]+",");
//			}
//			System.out.println();
//			System.out.println(NodeSequence);
//			System.out.println(NodeSequenceNext);
//			System.out.println("periodic: " + OurPeriodicChoice[NodeSequence - 1 - periodic - prefix]);
//			System.out.println("deviation: " + DeviationsList[NodeSequence - 1 - periodic - prefix][ControlIndex]);
		}else{
//			System.out.println("bug" + ParentNode.NodeSequence);
			NodeSequence = (ParentAction.NodeSequence%tp.ActionList.length) + 1;
			int NodeSequenceNext = ((ParentAction.NodeSequence + 1)%tp.ActionList.length) + 1; //next action might wrap around.
			
			ControlAction = tp.ActionList[NodeSequence-1][ControlIndex];
			if(tp.goDeviations && NodeSequence == prefix+periodic){
				TestedChildren = new boolean[tp.DeviationList[0].length]; //If we're at end of periodic and going deviations, then the next set of choices is from the differently-lengthed deviationslist.
				PotentialChildren = tp.DeviationList[0].length;
			}else{
				TestedChildren = new boolean[tp.ActionList[NodeSequenceNext-1].length]; //children belong to 2.
				PotentialChildren = tp.ActionList[NodeSequenceNext-1].length;
			}
			Arrays.fill(TestedChildren, false);
			

		}
			
		if(OptionsHolder.treeVisOn){
			CalcNodePos();
			if(ParentNode.altTree){
				altTree = true;
				CalcAltNodePos();
			}
		}
		
		//If we're doing periodic+specified deviations, we need to keep or build a copy of that "periodic" part.
		if(tp.goDeviations){
			OurPeriodicChoice = ParentNode.OurPeriodicChoice; //Copy the period from the previous node.
			if(ParentNode.NodeSequence >= prefix && ParentNode.NodeSequence < prefix + periodic){ //And potentially add if the list is incomplete
				OurPeriodicChoice[ParentNode.NodeSequence-prefix] = ControlAction;
//				if(altTree){
//					System.out.println("We've changed the periodic while on an alt tree");
//				}
			}
		}

		
//		//If we're doing periodic+specified deviations, we need to keep or build a copy of that "periodic" part.
//		if(OptionsHolder.goDeviations){
//			OurPeriodicChoice = ParentNode.OurPeriodicChoice; //Copy the period from the previous node.
//			if(NodeSequence >= prefix && NodeSequence < prefix + periodic){ //And potentially add if the list is incomplete
//				OurPeriodicChoice[NodeSequence-prefix] = ControlAction;
////				if(altTree){
////					System.out.println("We've changed the periodic while on an alt tree");
////				}
//			}
//		}
	}
	
	/** Constructor for creating a root node. Must specify tree parameters at root and ONLY at root. **/
	public TrialNode(TreeParameters tp) {
		System.out.println("Root node made. This message should only show once.");
		this.tp = tp;
		ParentNode = null;
		ControlIndex = -1; // This is just to make sure that this index never gets used if this is the root node.
		NodeSequence = 0; //The root node is treated as even just to make sure later nodes work right.
		TestedChildren = new boolean[tp.ActionList[0].length];
		Arrays.fill(TestedChildren, false);
		ControlAction = -1;
		TreeDepth = 0;
		height = tp.TreeLevel;
		
		PotentialChildren = tp.ActionList[0].length;
		OurPeriodicChoice = new int[tp.periodicLength];
		
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
				nodeAngle = Math.PI/2;
			}
		}else{
		
			if (ParentNode.PotentialChildren > 1){ //Catch the div by 0
				sweepAngle = ParentNode.sweepAngle/(2);
				nodeAngle = ParentNode.nodeAngle - sweepAngle/2. + (double)ControlIndex * sweepAngle/(double)(ParentNode.PotentialChildren-1);
			}else{
				sweepAngle = ParentNode.sweepAngle; //Only reduce the sweep angle if the parent one had more than one child.
				nodeAngle = ParentNode.nodeAngle;//sweepAngle/(ParentNode.PotentialChildren-1); //TODO make sure this is right, but if we have 1 or less child nodes from the parent, then the angle should go straight out in the direction the parent was also going.
			}
		}

		nodeLocation[0] = (float) (ParentNode.nodeLocation[0] + OptionsHolder.edgeLength*Math.cos(nodeAngle));
		nodeLocation[1] = (float) (ParentNode.nodeLocation[1] + OptionsHolder.edgeLength*Math.sin(nodeAngle));
	}
	
	/** Just for node visualization -- calculate the new node location on the diagram **/
	public void CalcAltNodePos(){
		
			if (ParentNode.PotentialChildren > 1){ //Catch the div by 0
				sweepAngle2 = ParentNode.sweepAngle2/(2);
				nodeAngle2 = ParentNode.nodeAngle2 - sweepAngle2/2. + (double)ControlIndex * sweepAngle2/(double)(ParentNode.PotentialChildren-1);
			}else{
				sweepAngle2 = ParentNode.sweepAngle2; //Only reduce the sweep angle if the parent one had more than one child.
				nodeAngle2 = ParentNode.nodeAngle2;//sweepAngle/(ParentNode.PotentialChildren-1); //TODO make sure this is right, but if we have 1 or less child nodes from the parent, then the angle should go straight out in the direction the parent was also going.
			}
		nodeLocation2[0] = (float) (ParentNode.nodeLocation2[0] + OptionsHolder.edgeLengthAlt*Math.cos(nodeAngle2));
		nodeLocation2[1] = (float) (ParentNode.nodeLocation2[1] + OptionsHolder.edgeLengthAlt*Math.sin(nodeAngle2));
	}
	
	
	public void MakeAltTree(boolean newroot){ //Newroot -- should we treat this node as the new root of our alternate tree?
		altTree = true;
		if(newroot){
			sweepAngle2 = 2*Math.PI;
			if ( PotentialChildren > 1 ){ //Catch the div by 0
				nodeAngle2 = 0. - sweepAngle2/2. + (double)ControlIndex * sweepAngle2/(double)(PotentialChildren-1);
			}else{
				nodeAngle2 = Math.PI/2;
			}
			nodeLocation2[0] = 400;
			nodeLocation2[1] = 400;

		}else{
			CalcAltNodePos();
		}
		
		for (TrialNode t : ChildNodes){
			t.MakeAltTree(false);
		}
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
			if(ChildNodes.get(i).DeadEnd){
				lines.AddLine(this, ChildNodes.get(i)); //Add a line between this node and all the existing children.

			}else if(ChildNodes.get(i).hiddenNode){
			 //Do nothing!	
			}else{
				lines.AddLine(this, ChildNodes.get(i)); //Add a line between this node and all the existing children.
				ChildNodes.get(i).GetNodeLines(lines); //Recurse to all the children too. This should propagate all the way down the tree.
			}
			
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
	
	/** Shift all visualization nodes by an x and y-offset from this node down. Should almost always be called from the root-level **/
	public void ShiftNodes(int x,int y){
		
		nodeLocation[0] += x;
		nodeLocation[1] += y;
		
		for (int i = 0; i<ChildNodes.size(); i++){
			ChildNodes.get(i).ShiftNodes(x,y);
		}
	}
	/** Just move nodes in the sub-view panel **/
	public void ShiftNodesAlt(int x,int y){
		
		nodeLocation2[0] += x;
		nodeLocation2[1] += y;
		
		for (int i = 0; i<ChildNodes.size(); i++){
			ChildNodes.get(i).ShiftNodesAlt(x,y);
		}
	}
	/** Rotates everything below this node about this node's parent dot **/
	public void RotateBranch(double angle){

		float relX = nodeLocation[0] - ParentNode.nodeLocation[0];
		float relY = nodeLocation[1] - ParentNode.nodeLocation[1];

		float newRelX = (float)(relX*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle)) + Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle))) - relY*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle)) - Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle))));
		float newRelY = (float)(relX*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle)) - Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle))) + relY*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle)) + Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle))));
		
		nodeLocation[0] = (newRelX + ParentNode.nodeLocation[0]);
		nodeLocation[1] = (newRelY + ParentNode.nodeLocation[1]);
		
		nodeAngle += angle; //Change the angle of this node -- mostly for bookkeeping at this point.	
		
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).RotateBranch(angle,ParentNode); 
		}
	}
	
	/** Rotates everything from this point on about a SPECIFIED node **/
	public void RotateBranch(double angle, TrialNode fulcrum){
		
		float relX = nodeLocation[0] - fulcrum.nodeLocation[0];
		float relY = nodeLocation[1] - fulcrum.nodeLocation[1];
		
		//Compound rotation origrot*Rot*origrot' -- derived symbolically.
		float newRelX = (float)(relX*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle)) + Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle))) - relY*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle)) - Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle))));
		float newRelY = (float)(relX*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle)) - Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle))) + relY*(Math.cos(nodeAngle)*(Math.cos(nodeAngle)*Math.cos(angle) - Math.sin(nodeAngle)*Math.sin(angle)) + Math.sin(nodeAngle)*(Math.cos(nodeAngle)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle))));
		
		nodeLocation[0] = (newRelX + fulcrum.nodeLocation[0]);
		nodeLocation[1] = (newRelY + fulcrum.nodeLocation[1]);
		
		nodeAngle += angle; //Change the angle of this node -- mostly for bookkeeping at this point.	
		
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).RotateBranch(angle,fulcrum); 
		}
	}
	
	/** Rotates everything below this node about this node's parent dot **/
	public void RotateBranchAlt(double angle){

		float relX = nodeLocation2[0] - ParentNode.nodeLocation2[0];
		float relY = nodeLocation2[1] - ParentNode.nodeLocation2[1];

		float newRelX = (float)(relX*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle)) + Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2))) - relY*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2)) - Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle))));
		float newRelY = (float)(relX*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2)) - Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle))) + relY*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle)) + Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2))));
		
		nodeLocation2[0] = (newRelX + ParentNode.nodeLocation2[0]);
		nodeLocation2[1] = (newRelY + ParentNode.nodeLocation2[1]);
		
		nodeAngle2 += angle; //Change the angle of this node -- mostly for bookkeeping at this point.	
		
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).RotateBranchAlt(angle,ParentNode); 
		}
	}
	
	/** Rotates everything from this point on about a SPECIFIED node **/
	public void RotateBranchAlt(double angle, TrialNode fulcrum){
		
		float relX = nodeLocation2[0] - fulcrum.nodeLocation2[0];
		float relY = nodeLocation2[1] - fulcrum.nodeLocation2[1];
		
		//Compound rotation origrot*Rot*origrot' -- derived symbolically.
		float newRelX = (float)(relX*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle)) + Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2))) - relY*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2)) - Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle))));
		float newRelY = (float)(relX*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2)) - Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle))) + relY*(Math.cos(nodeAngle2)*(Math.cos(nodeAngle2)*Math.cos(angle) - Math.sin(nodeAngle2)*Math.sin(angle)) + Math.sin(nodeAngle2)*(Math.cos(nodeAngle2)*Math.sin(angle) + Math.cos(angle)*Math.sin(nodeAngle2))));
		
		nodeLocation2[0] = (newRelX + fulcrum.nodeLocation2[0]);
		nodeLocation2[1] = (newRelY + fulcrum.nodeLocation2[1]);
		
		nodeAngle2 += angle; //Change the angle of this node -- mostly for bookkeeping at this point.	
		
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).RotateBranchAlt(angle,fulcrum); 
		}
	}
	
	/** Spread a branch out (or contract it) by a given increment **/
	public void SpaceBranch(double angle){
		sweepAngle += angle; //Change the angle that nodes sweep out from this one.
		for (int i = 0; i<ChildNodes.size(); i++){
			ChildNodes.get(i).RotateBranch(-angle/2+angle/PotentialChildren*ChildNodes.get(i).ControlIndex,this);		
		}	
	}
	
	/** Spread a branch out (or contract it) by a given increment **/
	public void SpaceBranchAlt(double angle){
		sweepAngle2 += angle; //Change the angle that nodes sweep out from this one.
		for (int i = 0; i<ChildNodes.size(); i++){
			ChildNodes.get(i).RotateBranchAlt(-angle/2+angle/PotentialChildren*ChildNodes.get(i).ControlIndex,this);		
		}	
	}
	
	/** Make it bigger/smaller. This version assumes that this is the root about which zooming occurs **/
	public void ZoomNodes(double zoomFactor){
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).ZoomNodes(zoomFactor,this);
		}
	}
	
	/** Make it bigger/smaller. This version assumes that this is the root about which zooming occurs **/
	public void ZoomNodesAlt(double zoomFactor){
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).ZoomNodesAlt(zoomFactor,this);
		}
	}
	
	/** Make it bigger/smaller. This version is for recursing through the children. **/
	public void ZoomNodes(double zoomFactor, TrialNode zoomRoot){
		
		float relX = nodeLocation[0] - zoomRoot.nodeLocation[0];
		float relY = nodeLocation[1] - zoomRoot.nodeLocation[1];
		
		nodeLocation[0] = (float) (relX*zoomFactor + zoomRoot.nodeLocation[0]);
		nodeLocation[1] = (float) (relY*zoomFactor + zoomRoot.nodeLocation[1]);
		
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).ZoomNodes(zoomFactor,zoomRoot);
		}
		
	}
	/** Make it bigger/smaller. This version is for recursing through the children. **/
	public void ZoomNodesAlt(double zoomFactor, TrialNode zoomRoot){
		
		float relX = nodeLocation2[0] - zoomRoot.nodeLocation2[0];
		float relY = nodeLocation2[1] - zoomRoot.nodeLocation2[1];
		
		nodeLocation2[0] = (float) (relX*zoomFactor + zoomRoot.nodeLocation2[0]);
		nodeLocation2[1] = (float) (relY*zoomFactor + zoomRoot.nodeLocation2[1]);
		
		for (int i = 0; i<ChildNodes.size(); i++){ //Now do the same for all nodes below this one.
			ChildNodes.get(i).ZoomNodesAlt(zoomFactor,zoomRoot);
		}
		
	}
	
	/** Color all of this node's children **/
	public void ColorChildren(Color color){
		nodeColor = color;
		for(TrialNode t: ChildNodes){
			t.ColorChildren(color);
		}
	}
	///// Tree visualization methods end ///////
	
	
	/** If we've gotten beyond the "periodic" portion (8 usually), then propagate new high scores at failure back towards root. We stop at the beginning of the periodic portion. Thus, from 8 on, bestInBranch represents the BEST you can do from this node if you make good decisions **/
	public void PropagateHighScore(float score){
		if(TreeDepth >0){
			if (score>bestInBranch){
				bestInBranch = score;
				ParentNode.PropagateHighScore(score);
			}
	
		}
	}
	
	/** Give this node a qwopinterface and capture the state **/
	public void CaptureState(QWOPInterface QWOPHandler){
		NodeState = new StateHolder(QWOPHandler);
		NodeState.CaptureState();
	}
	/** **/
	public void InjectNode(){	
	}
	
	/** Get the sequence of actions up to, and including this node **/
	public int[] getSequence(){
		int[] sequence = new int[TreeDepth];
		TrialNode current = this;
		sequence[TreeDepth-1] = current.ControlAction;
		for (int i =TreeDepth-2; i>=0; i--){
			current = current.ParentNode;
			sequence[i] = current.ControlAction;
		}
		return sequence;
		
	}
	/** Return the control action (NOT the index of the control) **/
	public int EchoControl(){
		return ControlAction;
	}
	
	/** Add a new unvisited node. **/
	public TrialNode SampleNew(){ //TODO add checking to make sure that this doesn't do weird things if all nodes are already sampled (should never occur).
		TrialNode newNode = null;
		
		if(FullyExplored){
			throw new RuntimeException("Cannot sample when there are no unexplored children.");
		}
		
		if (tp.PrioritizeNewNodes){ //Should we prioritize the selection of new nodes or just treat all the same.
			int untested = PotentialChildren - ChildNodes.size(); //How many UNTESTED nodes exist?
			if (untested>0){ //If there are zero untested nodes, skip.
				int count = 0;
				int selected = 0; 
				if(tp.sampleRandom){ //Do we select randomly amongst the possible untested nodes, or just pick the first index?
					selected = randgen.nextInt(untested);
				}
	
				
				//First try to get a completely untested node.
				for (int i = 0; i<TestedChildren.length; i++){
					if (!TestedChildren[i]){ //If the value is unused so far.
						if(count == selected){	
							TestedChildren[i] = true; //We've added this node and now we're checking it off the list.
							//Create the new object.
							newNode = new TrialNode(this,i);
							ChildNodes.add(newNode);
							return newNode;
						}else{
							count++;
						}
					}
		
				 }
			  }
		
			//If that doesn't work, then try to find an unexplored node.
			if(tp.sampleRandom){ //If sampling randomly, then we make a list of all possible unexplored nodes at this level and just pick these randomly.
				int[] unexplored = new int[ChildNodes.size()]; //Keep track of the indices of all unexplored nodes. This array is likely oversized for safety's sake.
				int unexploredCount = 0;
				
				for (int i = 0; i<ChildNodes.size(); i++){
					if(	!ChildNodes.get(i).FullyExplored && !ChildNodes.get(i).TempFullyExplored){
						unexplored[unexploredCount] = i;
						unexploredCount++;
					}
				}
				int selection = unexplored[randgen.nextInt(unexploredCount)];
				
				newNode = ChildNodes.get(selection);
				return newNode;
				
				
			}else{ //If we don't want to sample randomly amongst the not-fully-explored nodes, then we just grab the first one. This is more efficient, but not as broad of a search.
				for (int i = 0; i<ChildNodes.size(); i++){
					if(	!ChildNodes.get(i).FullyExplored && !ChildNodes.get(i).TempFullyExplored){
						newNode = ChildNodes.get(i);
						return newNode;
					}
				}
			}
		}else{		//If none of the above, then we want to sample from both new nodes and old unexplored nodes the same. (a better idea it seems)
			
			int choices = PotentialChildren;
			for (TrialNode t: ChildNodes){
				if (t.FullyExplored || t.DeadEnd || t.TempFullyExplored) choices--; //We have 1 less choice for every fully explored / dead end node.
			}
			
			int count = 0;
			
			int selected = 0; 
			if(tp.sampleRandom){ //Do we select randomly amongst the possible untested nodes, or just pick the first index?
					selected = randgen.nextInt(choices);
			}
	
				int numTested = 0; //We want to go through all potential children, but we also need to keep track of the number of tested nodes so we know the index we're at in ChildNodes. This is kind of cumbersome.
				//First try to get a completely untested node.
				for (int i = 0; i<PotentialChildren; i++){
					
					if (!TestedChildren[i]){ //If the value is unused so far.
						if(count == selected){	
							TestedChildren[i] = true; //We've added this node and now we're checking it off the list.
							//Create the new object.
							newNode = new TrialNode(this,i);
							ChildNodes.add(newNode);
							return newNode;
						}else{
							count++;
						}
					}else{ //This node must already exist.
						if (!ChildNodes.get(numTested).DeadEnd && !ChildNodes.get(numTested).FullyExplored && !ChildNodes.get(numTested).TempFullyExplored){ //If this is not fully explored, then it's potentially the choice.
							if(count == selected){	
								newNode = ChildNodes.get(numTested);
								return newNode;
							}else{
								count++;
							}
						}
						numTested++;
					}
		
				}
		}
		System.out.println(TreeDepth);
		System.out.println(TempFullyExplored);
		System.out.println(NumChildren());
		System.out.println(ChildNodes.get(0).FullyExplored);
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
		DeadNode.LabelOn = false;
//		ChildNodes.remove(DeadNode);
		DeadNode.DeadEnd = true;
		DeadNode.NodeState.flagFailure();
		DeadNode.FailType = DeadNode.NodeState.failType;
		
		DeadNode.FullyExplored = true;
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
				if(tp.limitDepth){ //We could add a fully explored node, then back up and the node below it is NOT fully explored, but it could be temp fully explored. We need to check this.
					CheckTempExplored();
				}
			}
		}
		
		this.FullyExplored = FullyExplored;
		
		if (FullyExplored && TreeDepth>0){
			ParentNode.CheckExplored(); //If this one is fully explored, we should also check its parent.
		}

		return FullyExplored;
	}
	
	/** Temporarily remove this node from action and keep track of Temp fully explored nodes too. **/
	public void TempRemove(){ //Now returns whether this node is fully explored too.
		
		TempFullyExplored = true;
		ParentNode.CheckTempExplored();
	}
	
	/** Change whether this node or any above it have become temporarily fully explored **/
	public void CheckTempExplored(){
		boolean TempFullyExplored = true;
		for (int i = 0; i<TestedChildren.length; i++){
			if (!TestedChildren[i]){ //If we find a possible option that's untested, then this node is not fully explored
				TempFullyExplored = false;
				
				break;
			}
		}
		if (FullyExplored){ //If we've visited all nodes, then we should check whether all the nodes underneath are explored or not.
				for (int i = 0; i<ChildNodes.size(); i++){
					if (!ChildNodes.get(i).FullyExplored && !ChildNodes.get(i).TempFullyExplored && !ChildNodes.get(i).DeadEnd){ // If we run into a non-explored node, then this node is not fully explored.

						TempFullyExplored = false;
						break;
					}
				}
		}
		
		this.TempFullyExplored = TempFullyExplored;
		
		if (TempFullyExplored && TreeDepth>0){
			ParentNode.CheckTempExplored(); //If this one is fully explored, we should also check its parent.
		}
	}
	
	/** Remove all those pesky temporarily explored flags when we switch the tree parameters. Recursive to all nodes below this one. **/
	public void RemoveTempExploredFlag(){
		TempFullyExplored = false;
		for (TrialNode t: ChildNodes){
			t.RemoveTempExploredFlag();
		}
	}
	
	/** Record the raw score (cost function evaluation) and the delta score. **/
	public void SetScore(float score){
		rawScore = score;
		diffScore = rawScore - ParentNode.rawScore;
	}
	
	public void SetSpeed(float speed){
		this.speed = speed;
	}
	
	/** WARNING: This is the index according to this one's list. NOT the control index. Use with caution. **/
	public int GetChildIndex(TrialNode child){
		
		return ChildNodes.indexOf(child);	//TODO: do I need error checking on this?

	}
}
