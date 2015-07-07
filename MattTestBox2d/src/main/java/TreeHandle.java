/**
 * 
 * This holds all the info associated with a single tree.
 * 
 * @author Matt
 *
 */
public class TreeHandle {

	
	//TODO: Put plane height
	/** Root of this tree **/
	private TrialNode root;
	
	/** Optional name of this tree **/
	private String name;
	
	/** LineHolder for this tree **/
	private LineHolder LH;
	
	/** Is this tree in focus? (ie greyed out or in color for now) **/
	public boolean focus = false;
	
	/** Create a new TreeHandle by providing its root node **/
	public TreeHandle(TrialNode root) {
		this.root = root;
	}
	
	/** Alternate constructor which allows adding a qualitative naming string **/
	public TreeHandle(TrialNode root, String name) {
		this.root = root;
		this.name = name;
	}
	
	/** Return the root of this tree **/
	public TrialNode getRoot(){
		return root;
	}
	
	/** Return the parameters used to create this tree **/
	public TreeParameters getTreeParams(){
		return root.tp;
	}
	
	/** Get the holder of the lines used for visualization **/
	public LineHolder getLines(){
		LH = root.GetNodeLines();
		return LH;
	}
	
	/** Get a single number representing the value of this tree so far. **/
	public float getTreeScore(){
		
		
		
		return 0;
	}

	/** Draw this tree. **/
//	public void drawTree(GL2 gl){ //TODO
//		
//	}
}
