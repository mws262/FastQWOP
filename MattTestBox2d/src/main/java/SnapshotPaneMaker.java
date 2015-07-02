
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;


public class SnapshotPaneMaker implements Schedulable,TabbedPaneActivator{

	//For scheduling.
	private int interval = 1;
	private boolean tabActive = false;
	
	/** Most important. QWOPInterface stays the same although worlds are often destroyed **/
	private QWOPInterface QWOPHandler;
	private World world;
	private TrialNode focusNode;
	
	public SnapshotPane SnapshotPanel;

	public SnapshotPaneMaker() {
		SnapshotPanel = new SnapshotPane();
	}
	
	public void update(){
		SnapshotPanel.repaint();
	}
	
	/** Select a node to view and update the graphics **/
	public void setNode(TrialNode focusNode){
		this.focusNode = focusNode;
		SnapshotPanel.setNode(focusNode);
		update();
	}
	/** Return the TrialNode which is currently being displayed/focused **/
	public TrialNode getNode(){
		return focusNode;
	}
	

	@Override
	public void setInterval(int interval) {
		this.interval = interval;
		
	}

	@Override
	public int getInterval() {
		// TODO Auto-generated method stub
		return interval;
	}

	@Override
	public void DoScheduled() {
		if(tabActive){ //Skip scheduled updates if this tab isn't in focus.
			update();
			try {
				Thread.sleep((long)(OptionsHolder.timestep*1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void DoEvery() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DoNow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Disable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ActivateTab() {
		tabActive = true;
		
	}

	@Override
	public void DeactivateTab() {
		tabActive = false;
		
	}

}

class SnapshotPane extends JPanel{

	Vec2 a = new Vec2(0,0);
	Vec2 b = new Vec2(0,0);
	Vec2 c = new Vec2(0,0);
	float scaling = 10f;
	int offsetx0 = 250;
	int offsetx = offsetx0;
	int offsety = 800;
	float flipud = 1f;
	float fliplr = 1f;
	
	
	public int periodicLength = 4;
	public int prefixLength = 8;
	
	int headpos = 0;
	TrialNode focusNode;
	TrialNode prevNode;
	int[] actionSequence;
	
	Color ghostRunner = new Color(0,0,0,0.2f);
	
	Font bigFont = new Font("Ariel", Font.PLAIN, 16);
	int vertTextSpacing = 17;
	int vertTextAnchor = 50;


	String prefixLabel = "";
	String periodicLabel = "";
	ArrayList<String> deviationLabel = new ArrayList<String>();

	
	public SnapshotPane(){

	}
	
	/** Select a node to view and update the graphics **/
	public void setNode(TrialNode focusNode){
		this.focusNode = focusNode; //assign the focused node to the input one
		if (focusNode != null && focusNode.NodeState != null){
			actionSequence = focusNode.getSequence(); //Ask that node for the sequence of actions leading up to this one.
//			 //Turn this into a string.
//			actions = "";
//			for (int i = 0; i<actionSequence.length-1; i++){
//				actions += actionSequence[i] + ", ";
//			}
//			actions += actionSequence[actionSequence.length - 1];


			//If we've reached the end of our candidate periodic path, then we also want to display the state before the periodic portion.
			if(focusNode.TreeDepth == prefixLength + periodicLength){
				prevNode = focusNode.ParentNode.ParentNode.ParentNode.ParentNode;
			}else{
				prevNode = null;
			}
			periodicLabel = "";
			prefixLabel = "";
			deviationLabel.clear();
			
			for (int i = 0; i<prefixLength; i++){
				if (i>actionSequence.length-1) break;
				String divider = ", ";
				if (i< prefixLength-1 && i < actionSequence.length-1){
					if(actionSequence[i+1]<10){
						divider += "  "; //If it's a single digit number, then add an extra space to make things even out.
					}
				}
				prefixLabel += (actionSequence[i] + divider);
//				if (i == OptionsHolder.prefixLength-2){
//					prefixLabel += (actionSequence[OptionsHolder.prefixLength-1] + " "); //No trailing comma on this one.
//				}
			}
			for (int i = prefixLength; i<prefixLength+periodicLength; i++){
				if (i>actionSequence.length-1) break;
				String divider = ", ";
				if (i< prefixLength+periodicLength-1 && i < actionSequence.length-1){
					if (actionSequence[i+1]<10){
						divider += "  "; //If it's a single digit number, then add an extra space to make things even out.
					}
				}
				periodicLabel += ( actionSequence[i] + divider );
			}
			int count = 0;
			String devElement = "";
			for (int i = prefixLength + periodicLength; i<actionSequence.length; i++){
//				if (i>=actionSequence.length-1) break;
				String divider = ", ";
				if (i< actionSequence.length-1){
					if (actionSequence[i+1]<10){
						divider += "  "; //If it's a single digit number, then add an extra space to make things even out.
					}
				}
				devElement += ( actionSequence[i] + divider );
				count++;
				if(count == periodicLength || i == actionSequence.length-1){ // add each periodic+deviation set as a separate string in the arraylist.
					deviationLabel.add(devElement);
					devElement = "";
					count = 0;
				}
			}
			
		}
//		System.out.println(deviationLabel.size());
	}
	

	
	/** Return the TrialNode which is currently being displayed/focused **/
	public TrialNode getNode(){
		return focusNode;
	}
	
	/** Main graphics for the runner! **/
    public void paintComponent(Graphics g){
    	super.paintComponent(g);
    	int count = 0;
    	TrialNode displayingNode = focusNode;
    	
    	if (focusNode != null && focusNode.NodeState != null){ // Make sure we actually have been capturing the state. 
   
        	while (count < 2){
	    	Shape[] shapes = StateHolder.CapturedShapes;
	    	offsetx = offsetx0 + (int)(-fliplr*scaling*displayingNode.NodeState.HeadState[0]);
    		for ( int i = 0; i<shapes.length; i++){

    			if(shapes[i].getType() == ShapeType.POLYGON){
    				//Get both the shape and its transform.
			    	PolygonShape focusShape = (PolygonShape)shapes[i];
			    	Transform trans = displayingNode.NodeState.CapturedTransforms[i];
			    	
			    	for (int k = 0; k<focusShape.getVertexCount(); k++){
			    		a = Transform.mul(trans, focusShape.getVertex(k));
			    		b = Transform.mul(trans, focusShape.getVertex((k+1) % (focusShape.getVertexCount()))); //Makes sure that the last vertex is connected to the first one.
			    		g.drawLine((int)(fliplr*scaling*a.x)+offsetx, (int)(flipud*scaling*a.y)+offsety, (int)(fliplr*scaling*b.x)+offsetx, (int)(flipud*scaling*b.y)+offsety);			    		
			    	}
			    	
	    		}else if (shapes[i].getType() == ShapeType.CIRCLE){
	    			CircleShape focusShape = (CircleShape)shapes[i];
			    	Transform trans = displayingNode.NodeState.CapturedTransforms[i];

			    	a = Transform.mul(trans, focusShape.m_p);
	    			headpos = (int)(-fliplr*scaling*a.x);
	    			g.drawOval((int)(fliplr*scaling*(a.x-fliplr*focusShape.getRadius())+offsetx), (int)(flipud*scaling*(a.y-flipud*focusShape.getRadius())+offsety), (int)(scaling*focusShape.getRadius()*2), (int)(scaling*focusShape.getRadius()*2));
	    			
	    			
	    		}else if (shapes[i].getType() == ShapeType.EDGE){
	    			
	    			EdgeShape focusShape = (EdgeShape)shapes[i];

		    		
		    		g.drawLine((int)(fliplr*scaling*focusShape.m_vertex1.x)+offsetx, (int)(flipud*scaling*focusShape.m_vertex1.y)+offsety, (int)(fliplr*scaling*focusShape.m_vertex2.x)+offsetx, (int)(flipud*scaling*focusShape.m_vertex2.y)+offsety);			    					    		

	    		}
	    		
	    	}
    		//If we have a node from before the periodic part, then loop back to display it with a ghosted color.
    		if (prevNode != null){
    			g.setFont(bigFont);
    			g.drawString("After periodic (" + (prefixLength+periodicLength) + " actions in)",100,700);
    			g.setColor(ghostRunner);
    			g.drawString("Before periodic (" + prefixLength + " actions in)",100,720);
    			displayingNode = prevNode;
    			count++;
    		}else{
    			count = 2;
    		}
    		
    	}	
        	//Other Labels:
    		//Action sequence label	
    		g.setFont(bigFont);
    		g.setColor(Color.BLACK);
    		if(prefixLabel != ""){ //Display the sequence of actions divided into prefix and periodic and deviations.
    	    	g.drawString("Running selected sequence: ", 10,vertTextAnchor);
    	    	g.setColor(Color.RED);
    	    	g.drawString(prefixLabel, 10, vertTextAnchor + vertTextSpacing);
    	    	g.setColor(Color.GREEN);
    	    	g.drawString(periodicLabel, 10, vertTextAnchor + vertTextSpacing*2);
    	    	g.setColor(Color.ORANGE);
    	    	for (int i = 0; i<deviationLabel.size(); i++){
    		    	g.drawString(deviationLabel.get(i), 10, vertTextAnchor + vertTextSpacing*(3+i));
    	    	}
    	    	g.setColor(Color.BLACK);
    		}else{
    			g.drawString("Going through the tree.", 10,50);
    		}
    		
    		
    	}else{ //No active node being displayed
    		g.setFont(bigFont);
    		g.drawString("Meta-click to select a node.", 100,200);
    		g.drawString("L-R arrows to go to next.", 100,225);
    	}
    }
}
