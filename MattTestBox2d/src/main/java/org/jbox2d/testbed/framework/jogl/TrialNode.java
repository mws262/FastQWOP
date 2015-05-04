package org.jbox2d.testbed.framework.jogl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TrialNode {
	
	private float rawScore = 0;
	private float diffScore = 0;
	private float value = 0; //TODO
	
	boolean DeadEnd = false;
	boolean FullyExplored = false;
	
	//The control value, parent, and sequence in the chain may never be modified after the object is created.
	private final int ControlIndex; // -1 is used as invalid.
	private final int ControlAction; //Actual delay used as control.
	
	public final TrialNode ParentNode; // The parent action may not be modified.
	
	private final int NodeSequence; //1 for odd node, 2 for even.
	
	public final int TreeDepth;
	
	private ArrayList<TrialNode> ChildNodes = new ArrayList<TrialNode>();
	
	private boolean[] TestedChildren; //This keeps track of which child nodes we've tried.
	
	
	//Action list order and actions.
	// for 1st depth, will pick one of 1st set of actions, etc.
	// for trees greater depth than number of arrays below, it will back to the top.
	public static final int[][] ActionList = {
			{0},
			{21,22,23,24,25,26,27},
			{11,12,13,14,15,16,17,18},
			{48,49,50,51,52,53,54},
			{21,22,23,24,25,26,27},
			{61,62,63,64,65,66,67,68},
			{0,1,2,3},
			{61,62,63,64,65,66,67,68}
			};

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
	
	public int EchoControl(){
		return ControlAction;
	}
	
	public TrialNode SampleNew(){
		TrialNode newNode = null;
		for (int i = 0; i<TestedChildren.length; i++){
			if (!TestedChildren[i]){ //If the value is unused so far.
				TestedChildren[i] = true; //We've added this node and now we're checking it off the list.
				//Create the new object.
				newNode = new TrialNode(this,i);
				ChildNodes.add(newNode);
				break;
			}
		}
		return newNode;
		
		//TODO Add random sampling.	
	}
	public TrialNode GetChild(int index){
		return ChildNodes.get(index);
	}
	
	public boolean RemoveChild(TrialNode DeadNode){ //Now returns whether this node is fully explored too.
		ChildNodes.remove(DeadNode);
		CheckExplored();
		return FullyExplored;
	}
	
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
	
	//Record the raw score (cost function evaluation) and the delta score.
	public void SetScore(float score){
		rawScore = score;
		diffScore = rawScore - ParentNode.rawScore;
	}
}
