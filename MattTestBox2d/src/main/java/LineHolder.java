import java.awt.Color;
import java.util.Arrays;

//This is just used to hold all the lines between nodes for visualization.

public class LineHolder {

	public int numLines; //Total number of expected lines TODO: add error checking if the numbers don't match expected.
	public int[][] LineList; //List of lines between nodes.
	public TrialNode[][] NodeList; //List of nodes corresponding to these lines.
	public boolean[] LabelOn;
	
	Color[] ColorList; //colors which correspond to the lines in LineList.
	
	//How far have we filled this LineList array?
	private int fillIndex = 0;

	/** Create an empty holder for lines given the total number of expected lines **/
	public LineHolder(int numLines) {
		this.numLines = numLines;
		LineList = new int[numLines][4]; // rows are x1 y1 x2 y2 across
		ColorList = new Color[numLines];
		NodeList = new TrialNode[numLines][2];
		LabelOn = new boolean[numLines]; //Does this node have a text label?
		Arrays.fill(LabelOn, false);
		Arrays.fill(ColorList, Color.BLACK);
	}
	
	/** Clear the line list **/
	public void ClearList(){
		for (int i = 0; i<LineList.length; i++){
			for (int j = 0; j<LineList[0].length; i++){
				LineList[i][j] = 0;
			}
			NodeList[i][0] = null;
			NodeList[i][1] = null;
		}
		Arrays.fill(LabelOn, false);
		Arrays.fill(ColorList, Color.BLACK);
	}
	
	
	/** Give this 2 nodes and it will store the line between them. **/
	public void AddLine(TrialNode parent, TrialNode child){
		LineList[fillIndex][0] = parent.nodeLocation[0];
		LineList[fillIndex][1] = parent.nodeLocation[1];
		LineList[fillIndex][2] = child.nodeLocation[0];
		LineList[fillIndex][3] = child.nodeLocation[1];
		ColorList[fillIndex] = Color.BLACK;
		
		NodeList[fillIndex][0] = parent;
		NodeList[fillIndex][1] = child;
		
		
		LabelOn[fillIndex] = parent.LabelOn;
		
		fillIndex++;
	}
	
	/** Give this 2 nodes and it will store the line between them. Flag is for colors. TODO Clearly define color flags **/
	public void AddLine(TrialNode parent, TrialNode child,int flag){
		switch(flag){
		case 1:
			ColorList[fillIndex] = Color.RED;
			break;
		default:
			break;
		}
		
		LineList[fillIndex][0] = parent.nodeLocation[0];
		LineList[fillIndex][1] = parent.nodeLocation[1];
		LineList[fillIndex][2] = child.nodeLocation[0];
		LineList[fillIndex][3] = child.nodeLocation[1];
		fillIndex++;
	}
	
	/** Given x and y coordinates, return the nearest node. **/
	public TrialNode GetNearestNode(int x, int y){
		int closestIndex = 0;
		int closestDist = Integer.MAX_VALUE;
		int candidateDist = 0;
		
		for (int i = 0; i<numLines; i++){ //We're always just looking at the second node in a line.
			if (( LineList[i][2] == 0 && LineList[i][3] == 0) && ( LineList[i-1][2] == 0 && LineList[i-1][3] == 0)){
				break; // If the x and y are 0 twice in a row, then we've reached the end of the list.	
			}
			
			candidateDist = (x-LineList[i][2])*(x-LineList[i][2]) + (y-LineList[i][3])*(y-LineList[i][3]); //least squares
			if(candidateDist < closestDist && NodeList[i][1]!=(null)){
				closestDist = candidateDist;
				closestIndex = i;
			}
		}
		
			return NodeList[closestIndex][1];
	}

}
