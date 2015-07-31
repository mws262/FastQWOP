package TreeQWOP;

import TreeQWOP.QWOPInterface.stanceType;

/**
 * This class hold a TrialNode and various info for ranking them. Used when automatically picking new branches to go down.
 * 
 * 
 * @author Matt
 *
 */
public class NodeScorer implements Comparable<NodeScorer>{

	/** TrialNode that this whole thing is about **/
	private TrialNode node;
	public TrialNode upstream;
	
	/** Score 1 -- Based on distance traveled **/
	public double score1;
	
	/** Score 2 -- Based on failure mode**/
	public double score2;
	
	/** Score 3 -- based on stance types through the whole trajectory **/
	public double score3 = 0;
	
	/** Score 4 -- based on cumulative sum of height off the ground **/
	public double score4 = 0;
	
	public NodeScorer(TrialNode node, int depth) {
		this.node = node;
		upstream = getUpstream(depth);
		//Assign Score1 based on distance travelled:
		score1 = node.rawScore*10;
		//Assign Score2 based on the failure mode;
		switch(node.NodeState.potentialFailMode){
			case BACK: //Fail back is bad.
				score2 = -900;//+50*node.TreeDepth;
				break;
			case FRONT: //Fail forward is good. TODO: Replace this with something angle based instead of this dumb totally discrete thing.
				score2 = 100;//+50*node.TreeDepth;
				break;
			case UNFAILED: //I'm using the potential fail mode which right now is just based on the torso angle. This should never occur.
				throw new RuntimeException("potential fail mode should never be UNFAILED here");
		}
		
		//For penalizing or rewarding certain stance types
		int marchingUpDepth = depth;
		TrialNode marchingUpNode = node;
		while(marchingUpDepth>0 && marchingUpDepth>depth-8){
			score3 -= 0.8*node.doubleCount;
			score3 -= 0.8 *node.flightCount;
			score4 += 0.1*node.sumBodyHeight; // body height -- positive is down.
			
			marchingUpNode = marchingUpNode.ParentNode;
			marchingUpDepth--;
		}
		
		
	}
	
	/** Return the node associated with this scorer object **/
	public TrialNode getNode(){
		return node;
	}
	
	/** Later, we might want to go back and see if the upstream node is actually fully explored. In this case we'd never want to go back there. **/
	public void updateScore(){
//		if(upstream.FullyExplored && OptionsHolder.limitDepth){
//			score2 = -10000;
//		}
	}

	@Override
	/** Used for comparing scores of one node to another for sorting purposes **/
	public int compareTo(NodeScorer other) {
		//positive -- this one is WORSE than other. -- picked this because sort is ascending.
		// Negative -- other is WORSE than this.
		// 0 -- equal.
		return (int)Math.signum(-this.totalScore() + other.totalScore());
	}
	
	/** Some way of combining scores into one meta-score **/
	public double totalScore(){
//		System.out.println(score1+","+score2+","+score3+","+score4);
		return (score1 + score2 + score3 + score4);
	}
	
	/** Grab a node further up the tree AT a specific depth **/
	public TrialNode getUpstream(int depth){
		TrialNode currentNode = node;
		while(currentNode.TreeDepth>depth){
			currentNode = currentNode.ParentNode;
		}
		return currentNode;
	}

}
