/* TODO: Things I want to add:
 * Color based on cost/value/something
 * Dynamic adding of nodes.
 * Fix the integer scaling issues.
 * 
 * 
 */




import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class VisTree extends JFrame{

	
	public static final String[] instructions = {
		"Left click & drag pans.",
		"Scroll wheel zooms.",
		"Right click & drag rotates individual branches",
		"Alt-click labels the control.",
		"Ctrl-click hides a branch.",
		"Alt-scroll spaces or contracts branches hovered over.",
		"S turns score display on and off (slows graphics).",
		"P pauses the game's graphics (speed boost?)."
	};
	
	
	
	public static int TreeDepthOffset = 0;
	
	  /** This is the root node from which the tree will be built out of **/
	  public TrialNode root;
	
	  /** Jpanel **/
	  public DrawPane panel;
	  
	  /** Object which holds current line data **/
	  LineHolder Lines;
	  
	  /** Min and max scaling of end costs -- gotten by taking 2.5 std devs out on either side of the mean final costs **/
	  public float minScaling = 0;
	  public float maxScaling = 0;
	  public int maxDepth = 2; //By default, using this for scaling, but should be changed from above.
	  
	  /** show score numbers by each end branch **/
  	  public boolean scoreDisplay = false;
	  
  	  /** Temp put drawing on hold for speed. Background calculations for node positions still happen though, so not fully efficient **/
  	  public boolean pauseDraw = false;
	  
	  //Internal thing to make sure that the data is there before I start to draw. 
	  private boolean startDrawing = false;
	  
	  //hold the time that the last drawing happened:
	  long lastTime;
	  long currTime;
	  int lastGameNum = 0;
	  int reportEvery = 10; //Normally games/s moves too much. Only report every handful of display changes.

	  int style = Font.BOLD;
	  Font bigFont = new Font ("Ariel", style , 36);
	  Font smallFont = new Font("Ariel",style, 14);
	  
	  public FontScaler scaleFont = new FontScaler(3,24,21);

	//When creating a new visualizer, wee need to know the root node so we can run down the tree and draw it.
	public VisTree(TrialNode root) {
		this.root = root;
        panel = new DrawPane();

        lastTime = System.currentTimeMillis(); //Grab the starting system time.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setPreferredSize(new Dimension(OptionsHolder.windowWidth, OptionsHolder.windowHeight));
        this.setContentPane(panel);
        this.pack();
        this.setVisible(true); 
        panel.requestFocus();
        
	}
	
	/** Call this externally to force a full update of the tree. This will go through all nodes, collect lines, and tell the graphics to update **/
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
	
	/** finds the standard deviation of all final costs to make a nice scaling for coloring. std dev to avoid outlier skewing **/
	public void ScaleCosts(ArrayList<Float> costs){
		if (!scoreDisplay) return; //Don't waste the computation if we're not displaying scores.
		//Find the mean
		float mean = 0;
		for (int i = 0; i<costs.size(); i++){
			mean += costs.get(i);
		}
		mean /= (float)costs.size();
		
		float stdDev = 0;
		for (int i = 0; i<costs.size(); i++){
			stdDev += (costs.get(i)-mean)*(costs.get(i)-mean);
		}
		stdDev = (float)Math.sqrt(stdDev/(float)costs.size());
		
		minScaling = mean - stdDev*8f; //Sorta buggy thing about cost being negative means that these are actually the best ones.
		maxScaling = mean + stdDev*0.3f;
	}

    /** Jpanel inside the jframe **/
    class DrawPane extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener,KeyListener{
  	  LineHolder Lines;
  	  boolean mouseTrack = false;
	  DecimalFormat df = new DecimalFormat("#.#");
	  int countLastReport = 0; //Some info is only reported every handful of paint calls. Keep a counter.
	  int gamespersec = 0;
	  
  	  boolean clearBackground = true;
  	  int mouseX = 0;
  	  int mouseY = 0;
  	  TrialNode focusedNode; //Node that's being selected, clicked, etc.
  	  
  	  public DrawPane(){
  	  	  addMouseListener(this);
  	  	  addMouseMotionListener(this);
  	  	  addMouseWheelListener(this);
  	  	  addKeyListener(this);
  	  }

      public void paintComponent(Graphics g){
    	  if(!pauseDraw){ //Temporarily stop drawing for speed.
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
	      		g2.setColor(getDepthColor(Lines.NodeList[i][1].TreeDepth));
	      		g2.drawLine(Lines.LineList[i][0], Lines.LineList[i][1], Lines.LineList[i][2], Lines.LineList[i][3]);
	      		
     			if (scoreDisplay && Lines.NodeList[i][1].DeadEnd){
     				g2.setColor(getScoreColor(-Lines.NodeList[i][1].rawScore));
     				g2.setFont(scaleFont.InterpolateFont(maxScaling,minScaling,-Lines.NodeList[i][1].rawScore*OptionsHolder.sizeFactor));
      				g2.drawString(df.format(Lines.NodeList[i][1].rawScore), (int)Lines.NodeList[i][1].nodeLocation[0], (int)Lines.NodeList[i][1].nodeLocation[1]);
      				g2.setColor(Color.BLACK);
//     				System.out.println(maxScaling + "" + minScaling);
      			}
	      		
	      		if(Lines.LabelOn[i]){ //Draw the label if it's turned on. NOTE: Change nodelist index back to zero for it to only display one action instead of all child node ones. Accidental change that turned out nicely.
	      			g.setColor(Color.BLACK);
	      			g.setFont(scaleFont.InterpolateFont(0, 4, OptionsHolder.sizeFactor));
	      			g.drawString(""+Lines.NodeList[i][1].ControlAction, (int)Lines.NodeList[i][1].nodeLocation[0], (int)Lines.NodeList[i][1].nodeLocation[1]);
	      		}
	      	}
	      	

	  		  //Write the instructions up too:
	  		  g.setColor(Color.WHITE);
	  		  g.fillRect(0, OptionsHolder.windowHeight-instructions.length*25-50, 400, OptionsHolder.windowHeight);
	  		  g.setColor(Color.BLACK);
	  		  g.setFont(smallFont);
	  		  for (int i = 0; i<instructions.length; i++){
	  			g.drawString(instructions[instructions.length-1-i], 10, OptionsHolder.windowHeight-i*25-50);
	  			  
	  		  }
	      	
    	  }  		  
    	  }
  		  //Write how many games have been played:
    	  //note, still displays even when graphics are basically paused.
  		  g.setColor(Color.WHITE);
  		  g.fillRect(0,0,450,100);
  		  g.setColor(Color.BLACK);
  		  g.setFont(bigFont);
  		  g.drawString(OptionsHolder.gamesPlayed + " Games played", 20, 50);
  		  
  		  //Draw games/s
  		  if(countLastReport>reportEvery){
  			  countLastReport = 0; //Reset the counter.
	  		  currTime = System.currentTimeMillis();
  			  gamespersec = (int)((OptionsHolder.gamesPlayed-lastGameNum)*1000./(currTime-lastTime));
	  		  g.drawString(gamespersec + "  games/s", 30, 90);
	  		  lastGameNum = OptionsHolder.gamesPlayed;
	  		  lastTime = currTime;
  		  }else{
  			  g.drawString(gamespersec + "  games/s", 30, 90);
  			  countLastReport++;
  		  }
       }
      /** Set the LineHolder to pay attention to **/
      public void setTree(LineHolder Lines){
      	this.Lines = Lines;
      }
      
      /** Convert a cost too a red-green color based on the std deviation sorta-full-scale **/
      public Color getScoreColor(float cost)
      {
    	  
    	  float scaledCost = (cost - minScaling)/(maxScaling - minScaling); //Scale the cost between 0 and 1 based on the std dev scaling. 
    	  if(scaledCost >1) scaledCost = 1;
    	  if(scaledCost <0) scaledCost = 0;
    	  scaledCost = -(scaledCost-0.5f)+0.5f;
    	  
    	  float H = (float)(Math.pow(scaledCost,1.)* 0.38);
    	  float S = 0.9f; // Saturation
    	  float B = 0.9f; // Brightness

          return Color.getHSBColor(H, S, B);
      }
      
      /** Convert a cost too a blue scale for showing tree depth.**/
      public Color getDepthColor(int depth)
      {
    	  
    	  float scaledDepth = (float)depth/(float)maxDepth;
    	  
    	  float H = (float)(scaledDepth* 0.3)+0.45f;
    	  float S = 0.5f; // Saturation
    	  float B = 0.9f; // Brightness

          return Color.getHSBColor(H, S, B);
      }
	@Override
	public void mouseClicked(MouseEvent arg0) {	
		if (arg0.isAltDown()){ //alt click enables a label on this node
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			focusedNode.LabelOn = true;
		}else if (arg0.isControlDown()){ //Control will hide this node and all its children.
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			if(focusedNode !=null && focusedNode.TreeDepth > 1){ //Keeps stupid me from hiding everything in one click.
				focusedNode.hiddenNode = true;
				focusedNode.ParentNode.RemoveChild(focusedNode); //Try also just killing it from the tree search too.
			}
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
		clearBackground =  true;
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
		if(arg0.isAltDown()){
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			focusedNode.SpaceBranch(0.1);
			
		}else{
			root.ZoomNodes(1.1);
			OptionsHolder.ChangeSizeFactor(1.1f);
		}
	}else{
		if(arg0.isAltDown()){
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			focusedNode.SpaceBranch(-0.1);
			
		}else{
			root.ZoomNodes(0.9);//positive mouse direction -> zoom out.;
			OptionsHolder.ChangeSizeFactor(0.9f);
		}

	}
	clearBackground = true;
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
		switch(arg0.getKeyChar()){
		case 's': // toggle the score text at the end of all branches
			scoreDisplay = !scoreDisplay;
			break;
		case 'p': //Pause visualization.
			pauseDraw = !pauseDraw;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
   }
}
