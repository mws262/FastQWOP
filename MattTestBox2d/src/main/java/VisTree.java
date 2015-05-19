import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class VisTree extends JFrame{

	public static int TreeDepthOffset = 0;
	
	  /** This is the root node from which the tree will be built out of **/
	  public TrialNode root;
	
	  /** Jpanel **/
	  public DrawPane panel;
	  
	  /** Object which holds current line data **/
	  LineHolder Lines;
	  
	  //Internal thing to make sure that the 
	  private boolean startDrawing = false;

	  int style = Font.BOLD;
	  Font bigFont = new Font ("Ariel", style , 36);
	
	//When creating a new visualizer, wee need to know the root node so we can run down the tree and draw it.
	public VisTree(TrialNode root) {
		this.root = root;
        panel = new DrawPane();

        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setPreferredSize(new Dimension(OptionsHolder.windowWidth, OptionsHolder.windowHeight));
        this.setContentPane(panel);
        this.pack();
        this.setVisible(true); 
        
	}
	
	/** Call this externally to force a full update of the tree. This will go throuigh all nodes, collect lines, and tell the graphics to update **/
	public void UpdateTree(){
		startDrawing = true;
		if(root.PotentialChildren == 1){ //If the root node only has one child, then we're just going to move down and call the next node root for drawing purposes.
			root = root.GetChild(0);
		}

		 Lines = root.GetNodeLines();
		 if (!Lines.equals(null)){
			panel.setTree(Lines);
		 	panel.repaint();
		 }
	}

    /** Jpanel inside the jframe **/
    class DrawPane extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener{
  	  LineHolder Lines;
  	  boolean mouseTrack = false;
  	  boolean clearBackground = true;
  	  int mouseX = 0;
  	  int mouseY = 0;
  	  TrialNode focusedNode; //Node that's being selected, clicked, etc.
  	  
  	  public DrawPane(){
  	  	  addMouseListener(this);
  	  	  addMouseMotionListener(this);
  	  	  addMouseWheelListener(this);
  	  }

      public void paintComponent(Graphics g){
    	   Graphics2D g2 = (Graphics2D) g; //Casting to a graphics 2d object for more control.
    	   g2.setStroke(new BasicStroke(0.1f));
    	  //Go through and draw all the lines defined.
    	  if (startDrawing){ //Make sure this exists.
    		  
    		  if(mouseTrack || clearBackground){//Completely overwrite the background if dragging.
        		  g.setColor(Color.WHITE);
        		  clearBackground = false;
        		  
    		  }else{//allow a little bit of alpha to see where new branches are failing.
        		  g.setColor(new Color(1f,1f,1f,0.1f)); //Write a new rectangle over the whole thing with some amount of alpha. This means that the failed branches will fade out.

    		  }
    		  g.fillRect(0, 0, OptionsHolder.windowWidth,OptionsHolder.windowHeight);
    		  
	      	for (int i = 0; i<Lines.numLines; i++){
	      		if(Lines.LineList[i][2] == 0 && Lines.LineList[i][3] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
	      			break;
	      		}
	      		g2.setColor(Lines.ColorList[i]);
	      		g2.drawLine(Lines.LineList[i][0], Lines.LineList[i][1], Lines.LineList[i][2], Lines.LineList[i][3]);
	      		
	      		if(Lines.LabelOn[i]){ //Draw the label if it's turned on.
	      			g.drawString(""+Lines.NodeList[i][0].ControlAction, Lines.NodeList[i][0].nodeLocation[0], Lines.NodeList[i][0].nodeLocation[1]);
	      		}
	      	}
	      	
	  		  //Write how many games have been played:

	  		  g.setColor(Color.WHITE);
	  		  g.fillRect(0,0,450,70);
	  		  g.setColor(Color.BLACK);
	  		  g.setFont(bigFont);
	  		  g.drawString(OptionsHolder.gamesPlayed + " Games played", 20, 50);
	      	
    	  }
       }
      /** Set the LineHolder to pay attention to **/
      public void setTree(LineHolder Lines){
      	this.Lines = Lines;
      }
	@Override
	public void mouseClicked(MouseEvent arg0) {	
		if (arg0.isAltDown()){ //Right click moves nodes.
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			focusedNode.LabelOn = true;
		}
		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		mouseTrack = true;
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
	if (arg0.getButton() == MouseEvent.BUTTON3){ //Right click moves nodes.
		focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
	}
		
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		mouseTrack = false;
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0){
		
		if (arg0.getButton() == MouseEvent.BUTTON1){ //Left click drags the whole thing.
			root.ShiftNodes(arg0.getX()-mouseX, arg0.getY()-mouseY);
		}else if (arg0.getButton() == MouseEvent.BUTTON3){ //Right click moves nodes.
			//Calculate the angle between the click and the parent of the click node.
			
			double clickAngle = -Math.atan2((arg0.getX()-focusedNode.ParentNode.nodeLocation[0]),(arg0.getY()-focusedNode.ParentNode.nodeLocation[1]))+Math.PI/2.;
			clickAngle -= focusedNode.nodeAngle; //Subtract out the current angle.
			
			focusedNode.RotateBranch(clickAngle);
			
		}
			mouseX = arg0.getX();
			mouseY = arg0.getY();
	}
	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	/** This zooms in and out. Also changes the size factor in OptionsHolder to keep things consistent. **/
	public synchronized void mouseWheelMoved(MouseWheelEvent arg0){
	
	
	if (arg0.getWheelRotation()<0){ //Negative mouse direction -> zoom in.
		root.ZoomNodes(1.1);
		OptionsHolder.ChangeSizeFactor(1.1f);
	}else{
		root.ZoomNodes(0.9);//positive mouse direction -> zoom out.;
		OptionsHolder.ChangeSizeFactor(0.9f);
	}
	clearBackground = true;
		
	}
   }
}
