import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * Holds/gets/distributes data.
 * You add nodes. At scheduled intervals (see schedulable), this will go through its
 * list of nodes and grab the data. This extends the JFreeChart XYDataset, so it also
 * directly be given to a plot.
 * 
 * @author Matt
 *
 */

public class DataGrabber extends AbstractXYDataset implements Schedulable,DomainInfo,RangeInfo{

	private int writeInterval;
	
	/** List of non-failed nodes **/
	public ArrayList<TrialNode> NodeList = new ArrayList<TrialNode>();
	
	
	private double[] xMin;
	private double[] xMax;
	private double[] yMin;
	private double[] yMax;
	private Range[] xRange;
	private Range[] yRange;;
	private int numFields = 0;
	
	//Note: uses reflection to get the named parameters
	/** Names of the fields in the trialnodes which will be plotted. Y AND X MUST BE THE SAME DIMENSION **/
	public String[] xFieldNames = {"rawScore","rawScore","bestInBranch","bestInBranch"};
	public String[] yFieldNames = {"value","speed","value","speed"};
	
	// Set the relationship to be plotted. Currently supported: direct, inverse, log, and exp.
	public String[] xRelationship = {"direct","direct","direct","direct"};
	public String[] yRelationship = {"inverse","direct","inverse","direct"};
	
	/** Axis labels -- just for visualization **/
	public String[] xLabels = {"Dist Travelled (up to prefix+periodic)", "Dist Travelled (up to prefix+periodic)", "Dist (prefix+periodic+periodicdeviations)","Dist (prefix+periodic+periodicdeviations)"};
	public String[] yLabels = {"1/Periodic Error", "Speed","1/Periodic Error", "Speed"};
	
	/** Field objects for the parameters named above. **/
	private Field[] xFields;
	private Field[] yFields;
	
	
	public DataGrabber() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		numFields = xFieldNames.length;
		xFields = new Field[numFields];
		yFields = new Field[numFields];
		xRange = new Range[numFields];
		yRange = new Range[numFields];
		
		for (int i = 0; i<numFields; i++){
			xFields[i] = TrialNode.class.getField(xFieldNames[i]);
			yFields[i] = TrialNode.class.getField(yFieldNames[i]);
			
			xRange[i] = new Range(0,0.01);//Set some defaults to prevent errors
			yRange[i] = new Range(0,0.01);
		}

		xMin = new double[numFields];
		xMax = new double[numFields];
		yMin = new double[numFields];
		yMax = new double[numFields];

	}
	
	public void AddNonFailedNode(TrialNode newNode){
		NodeList.add(newNode);	
	}
	
	/** Since some values get backpropagated after their nodes get added, we may need to update the data ranges every time the plot is updated. **/
	public void UpdateBounds(){
		
		//Go ahead and update allllll the ranges of all the series being potentially plotted.
		for (int i = 0; i<numFields; i++){
			for (int j = 0; j<NodeList.size(); j++){
				//Check if we need to update the bounds of our plot.
				Float newX = 0f;
				Float newY = 0f;
				try {
					newX = (Float) xFields[i].get(NodeList.get(j));
					newY = (Float) yFields[i].get(NodeList.get(j));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//If we're doing an inverse, power, etc relationship, we need to also do the relationship for scaling purposes.
				double xTrans = DoRelationship(newX,i,0);
				double yTrans = DoRelationship(newY,i,1);
				if (yTrans>yMax[i]){
					yMax[i] = yTrans;
					yRange[i] = Range.expandToInclude(yRange[i], yMax[i]);
				}else if(yTrans < yMin[i]){
					yMin[i] = yTrans;
					yRange[i] = Range.expandToInclude(yRange[i], yMin[i]);
				}
				
				if (xTrans>xMax[i]){
					xMax[i] = xTrans;
					xRange[i] = Range.expandToInclude(xRange[i], xMax[i]);
				}else if(xTrans < xMin[i]){
					xMin[i] = xTrans;
					xRange[i] = Range.expandToInclude(xRange[i], xMin[i]);
				}
			}
		}
	}
	public void WriteToFile(){
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("Data.txt"), "utf-8"));
		    
		    for (int i = 0; i<NodeList.size(); i++){
		    	writer.write(NodeList.get(i).rawScore + "," + NodeList.get(i).value + "," + NodeList.get(i).bestInBranch + "\n");
		    	
		    	
		    }    
		} catch (IOException ex) {
			System.out.println("failed to write file.");
		} finally {
		   try {writer.close();} catch (Exception ex) {System.out.println("failed to write file.");}
		}
	}
	
	/** Return the x range of the dataset corresponding to the index **/
	public Range getXRange(int index){
		return xRange[index];
	}
	
	/** Return the y range of the dataset corresponding to the index **/
	public Range getYRange(int index){
		return yRange[index];
	}

	@Override
	public void DoScheduled() {
		if(OptionsHolder.DataToFile){
			System.out.println("Data written to txt.");
			WriteToFile();  
		}	
	}
	@Override
	public void DoEvery() {
		// TODO Auto-generated method stuff
	}

	@Override
	public int getInterval() {
		return writeInterval;
	}

	@Override
	public void setInterval(int writeInterval) {
		this.writeInterval = writeInterval;
	}

	@Override
	public int getItemCount(int series) {
		return NodeList.size();
	}

	public double DoRelationship(double val, int series, int axis){
		String[] relationship;
		if (axis == 0){
			relationship = xRelationship;
		}else if(axis == 1){
			relationship = yRelationship;
		}else{
			return -1;
		}

		
		if(relationship[series] == "direct"){
			return val;
		}else if(relationship[series] == "inverse"){
			return 1f/(val + 0.00000001); //+ epsilon to prevend div/0
		}else if (relationship[series] == "log"){
			return Math.log(val);
			
		}else if (relationship[series] == "exp"){
			return Math.exp(val);
		}else{ //just assume direct if nothing else/invalid.
			return val;	
		}
		
	}
	
	@Override
	public Number getX(int series, int item) {
		try {
			Float val = (Float) xFields[series].get(NodeList.get(item));
			
			return DoRelationship(val,series,0);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public Number getY(int series, int item) {
		try {
			Float val = (Float) yFields[series].get(NodeList.get(item));
			
			return DoRelationship(val,series,1);
			
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSeriesCount() {
		return numFields;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		// TODO Auto-generated method stub
		return "Sample" + series;
	}

	@Override
	public Range getRangeBounds(boolean includeInterval) {
		// TODO Auto-generated method stub
		return yRange[0];
	}

	@Override
	public double getRangeLowerBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return yMin[0];
	}

	@Override
	public double getRangeUpperBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return yMax[0];
	}

	@Override
	public Range getDomainBounds(boolean includeInterval) {
		// TODO Auto-generated method stub
		return xRange[0];
	}

	@Override
	public double getDomainLowerBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return xMin[0];
	}

	@Override
	public double getDomainUpperBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return xMax[0];
	}

	@Override
	public void DoNow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Disable() {
		// TODO Auto-generated method stub
		
	}

}
