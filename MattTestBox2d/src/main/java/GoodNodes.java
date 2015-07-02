import java.util.ArrayList;
import java.util.Collections;

/**
 * Object which handles keeping a list of nodes which we want to rank and potentially select from.
 * 
 * 
 * @author Matt
 *
 */

public class GoodNodes {

	
	private ArrayList<NodeScorer> bestNodes = new ArrayList<NodeScorer>();
	ArrayList<NodeScorer> sublist = new ArrayList<NodeScorer>();
	
	public GoodNodes() {
		// TODO Auto-generated constructor stub
	}
	
	/** Give this node holder a new node to add to its sorting list **/
	public void passNew(TrialNode newEndNode, int depth){ //also give the depth of the node we would potentially go back to if we picked this node.
		NodeScorer scorer = new NodeScorer(newEndNode,depth);
		bestNodes.add(scorer);
		
	}
	public int getNodeCount(){
		return bestNodes.size();
	}
	/** Get the top x number of TrialNodes from the ranked arraylist **/
	public NodeScorer[] getTopX( int x ){
		if (x>bestNodes.size()){
			x = bestNodes.size();
		}
		//Make sure scores are current with regards to the upstream node being fully explored.
		for (NodeScorer n: bestNodes){
			n.updateScore();
		}
		Collections.sort(bestNodes); //made sort such that it's descending. Thus the best ones are at the front of the list.
//		for(int i = 0; i<bestNodes.size();i++){
//			System.out.print(bestNodes.get(i)+",");
//		}
//		System.out.println();
		
		NodeScorer[] bestList = new NodeScorer[x];
		for (int i = 0; i<x; i++){
			bestList[i] = bestNodes.get(i);
		}
		return bestList;
	}
	
	public NodeScorer getFromNode(TrialNode t){
		for(NodeScorer ns: bestNodes){
			if(ns.getNode().equals(t)){
				return ns;
			}
		}
		return null;
	}
	/** Get the top x number of TrialNodes from the ranked arraylist which are also below a specific parent node. **/
	public ArrayList<NodeScorer> getFilteredTopX( TrialNode parent, int x ){
		sublist.clear();
		TrialNode temp;
		if(true){//parent.TreeDepth == 0){
			return bestNodes;
		}
		for (NodeScorer ns: bestNodes){ //Go through all the trialnodes in our highscore list.
			temp = ns.getNode();
			int count = ns.getNode().TreeDepth;

			while (count > parent.TreeDepth){ //Go back to this nodes parents until we get to the same depth as the input parent node.
				temp =temp.ParentNode;

				count--;
			}
			if(temp.equals(parent)){ //once we're at this depth, the temp trialnode should equal the parent node if this is part of our sublist
				sublist.add(ns);
			}
			
		}
		if (x>sublist.size()){
			x = sublist.size();
		}
		//Make sure scores are current with regards to the upstream node being fully explored.
		for (NodeScorer n: sublist){
			n.updateScore();
		}
		Collections.sort(sublist); //made sort such that it's descending. Thus the best ones are at the front of the list.
		
//		NodeScorer[] bestList = new NodeScorer[x];
//		for (int i = 0; i<x; i++){
//			bestList[i] = sublist.get(i);
//		}
		return sublist;
	}
	
	
	/** Remove all nodes from the ranking list **/
	public void clearAll(){
		bestNodes.clear();
	}

}
