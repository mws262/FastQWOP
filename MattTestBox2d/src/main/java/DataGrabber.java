import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;


public class DataGrabber extends Schedulable{

	/** List of non-failed nodes **/
	private ArrayList<TrialNode> NodeList = new ArrayList<TrialNode>();
	
	public DataGrabber(int writeInterval) {
		super(writeInterval); //set the write interval
	}
	
	public void AddNonFailedNode(TrialNode newNode){
		NodeList.add(newNode);	
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

	public Double[] x;
	public Double[] y;
	@Override
	void DoScheduled() {
		System.out.println("Data written to txt.");
		WriteToFile();  
	   
		
	}

	@Override
	void DoEvery() {
		// TODO Auto-generated method stub
		//TEMP TEMP TEMP
		x = new Double[NodeList.size()];
		y = new Double[NodeList.size()];
	    for (int i = 0; i<NodeList.size(); i++){
	    	x[i] = Double.valueOf((double)NodeList.get(i).rawScore);
	    	y[i] = Double.valueOf((double)NodeList.get(i).value);
	    }  
	}
	
	
	
	

}
