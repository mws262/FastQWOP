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
	private ArrayList<TrialNode> NodeList = new ArrayList<TrialNode>();
	
	
	private double[] xMin;
	private double[] xMax;
	private double[] yMin;
	private double[] yMax;
	private Range[] xRange;
	private Range[] yRange;;
	private int numFields = 0;
	
	
	/** Names of the fields in the trialnodes which will be plotted. Y AND X MUST BE THE SAME DIMENSION **/
	public String[] xFieldNames = {"rawScore","value"};
	public String[] yFieldNames = {"value","rawScore"};
	
	
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
			
			xRange[i] = new Range(0,1);//Set some defaults to prevent errors
			yRange[i] = new Range(0,1);
		}

		xMin = new double[numFields];
		xMax = new double[numFields];
		yMin = new double[numFields];
		yMax = new double[numFields];

		
		
	}
	
	public void AddNonFailedNode(TrialNode newNode){
		NodeList.add(newNode);	
		
		//Go ahead and update allllll the ranges of all the series being potentially plotted.
		for (int i = 0; i<numFields; i++){
			//Check if we need to update the bounds of our plot.
			Float newX = 0f;
			Float newY = 0f;
			try {
				newX = (Float)xFields[i].get(newNode);
				newY = (Float)yFields[i].get(newNode);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (newY>yMax[i]){
				yMax[i] = newY;
				yRange[i] = Range.expandToInclude(yRange[i], yMax[i]);
			}else if(newY < yMin[i]){
				yMin[i] = newY;
				yRange[i] = Range.expandToInclude(yRange[i], yMin[i]);
			}
			
			if (newX>xMax[i]){
				xMax[i] = newX;
				xRange[i] = Range.expandToInclude(xRange[i], xMax[i]);
			}else if(newX < xMin[i]){
				xMin[i] = newX;
				xRange[i] = Range.expandToInclude(xRange[i], xMin[i]);
			}
		}
	
	}
	
	public void WriteToFile(){
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("Data.txt"), "utf-8"));
		    
		    for (int i = 0; i<NodeList.size(); i++){
		    	writer.write(NodeList.get(i).rawScore + "," + NodeList.get(i).value + "\n");
		    	
		    	
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

	@Override
	public Number getX(int series, int item) {
		try {
			return (Number) xFields[series].get(NodeList.get(item));
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
			return (Number) yFields[series].get(NodeList.get(item));
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
