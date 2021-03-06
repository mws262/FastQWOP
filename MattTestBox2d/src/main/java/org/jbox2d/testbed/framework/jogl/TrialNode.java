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
	
	// The actual control delays are static and immutable.
	public static final int[] ActionList1 = {0,5,10,15,20,25,30};
	public static final int[] ActionList2 = {25,30,35,40,45,50,55,60,65};
	
	
	public TrialNode(TrialNode ParentAction, int ControlIndex) {
		this.ParentNode = ParentAction;
		NodeSequence = (ParentAction.NodeSequence%2) + 1; // If parent is 1, make this one 2 and vice versa.
		
		TreeDepth = ParentAction.TreeDepth + 1; // When we make nodes, we go down the tree.
		this.ControlIndex = ControlIndex;
		
		
		//This makes sure that the list of tested nodes is set to the correct length based on whether this node is even or odd.
		if (NodeSequence == 1){
			TestedChildren = new boolean[ActionList2.length]; //children belong to 2.
			ControlAction = ActionList1[ControlIndex]; //This object belongs to 1.
		}else if (NodeSequence == 2){
			TestedChildren = new boolean[ActionList1.length];
			ControlAction = ActionList2[ControlIndex]; //This object belongs to 1.
		}else{
			throw new RuntimeException("Node sequence assignments in error");
		}
		Arrays.fill(TestedChildren, false);
		
	}
	
	public TrialNode() {
		System.out.println("Root node made. This message should only show once.");
		ParentNode = null;
		ControlIndex = -1; // This is just to make sure that this index never gets used if this is the root node.
		NodeSequence = 2; //The root node is treated as even just to make sure later nodes work right.
		TestedChildren = new boolean[ActionList1.length];
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
//				System.out.println(TreeDepth + "," + i);
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
//			System.out.println(this.TreeDepth + "th level branch over, going up");
			ParentNode.CheckExplored(); //If this one is fully explored, we should also check its parent.
		}

//		System.out.println(FullyExplored);
		return FullyExplored;
	}
	
	//Record the raw score (cost function evaluation) and the delta score.
	public void SetScore(float score){
		rawScore = score;
		diffScore = rawScore - ParentNode.rawScore;
	}

}
