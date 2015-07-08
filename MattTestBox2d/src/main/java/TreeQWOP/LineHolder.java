package TreeQWOP;
import java.awt.Color;
import java.util.Arrays;

/**
 * Hold all the lines used for visualization
 * 
 * 
 * @author Matt
 *
 */

public class LineHolder {

	public int numLines; //Total number of expected lines TODO: add error checking if the numbers don't match expected.
	public TrialNode[][] NodeList; //List of nodes corresponding to these lines.
	public boolean[] LabelOn;
	
	public Color[] ColorList; //colors which correspond to the lines in LineList.
	

	//How far have we filled this LineList array?
	private int fillIndex = 0;
	
	/** Create an empty holder for lines given the total number of expected lines **/
	public LineHolder(int numLines) {
		this.numLines = numLines+1;
		ColorList = new Color[numLines];
		NodeList = new TrialNode[numLines][2];
		LabelOn = new boolean[numLines]; //Does this node have a text label?
		Arrays.fill(LabelOn, false);
		Arrays.fill(ColorList, Color.BLACK);
	}
	
	/** Clear the line list **/
	public void ClearList(){
		for (int i = 0; i<NodeList.length; i++){
			NodeList[i][0] = null;
			NodeList[i][1] = null;
		}
		Arrays.fill(LabelOn, false);
		Arrays.fill(ColorList, Color.BLACK);
	}
	
	
	/** Give this 2 nodes and it will store the line between them. **/
	public void AddLine(TrialNode parent, TrialNode child){
		
		if (fillIndex >= numLines) { // Due to concurrency, there can sometimes
									// be more lines than we previously thought
									// when we made this. In this case, just
									// ignore the extra ones until the next time
									// through.
			return;
		}
		if(parent.nodeColor != null){
			ColorList[fillIndex] = parent.nodeColor;
		}else{
			ColorList[fillIndex] = Color.BLACK;
		}

		
		NodeList[fillIndex][0] = parent;
		NodeList[fillIndex][1] = child;
		
		
		LabelOn[fillIndex] = parent.LabelOn;
		numLines = NodeList.length;
		fillIndex++;
	}

}
