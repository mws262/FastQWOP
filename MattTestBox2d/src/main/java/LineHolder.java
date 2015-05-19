//This is just used to hold all the lines between nodes for visualization.

public class LineHolder {

	public int numLines; //Total number of expected lines TODO: add error checking if the numbers don't match expected.
	public int[][] LineList; //List of lines between nodes.
	
	//How far have we filled this LineList array?
	private int fillIndex = 0;
	
	/** Create an empty holder for lines given the total number of expected lines **/
	public LineHolder(int numLines) {
		this.numLines = numLines;
		LineList = new int[numLines][4]; // rows are x1 y1 x2 y2 across
	}
	
	
	/** Give this 2 nodes and it will store the line between them. **/
	public void AddLine(TrialNode parent, TrialNode child){
		LineList[fillIndex][0] = parent.nodeLocation[0];
		LineList[fillIndex][1] = parent.nodeLocation[1];
		LineList[fillIndex][2] = child.nodeLocation[0];
		LineList[fillIndex][3] = child.nodeLocation[1];
		fillIndex++;
	}

}
