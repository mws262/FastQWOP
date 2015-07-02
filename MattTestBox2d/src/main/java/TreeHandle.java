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
		return root.GetNodeLines();
	}
}
