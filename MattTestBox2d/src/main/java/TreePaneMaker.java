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

public class TreePaneMaker implements Schedulable{

	private int interval = 1;
	
	public static final String[] instructions = {
		"Left click & drag pans.",
		"Scroll wheel zooms.",
		"Right click & drag rotates individual branches",
		"Alt-click labels the control.",
		"Ctrl-click hides a branch.",
		"Meta-click selects a point for state viewer.",
		"Alt-scroll spaces or contracts branches hovered over.",
		"S turns score display on and off (slows graphics).",
		"Meta-S turns value display on (slows graphics)",
		"P pauses the game's graphics (speed boost?)."
	};
	
	
	
	public static int TreeDepthOffset = 0;
	
	  /** This is the root node from which the tree will be built out of **/
	  public TrialNode root;
	  private SnapshotPaneMaker SnapshotPane;

	  /** Object which holds current line data **/
	  LineHolder Lines;
	  
	  /** Min and max scaling of end costs -- gotten by taking 2.5 std devs out on either side of the mean final costs **/
	  public float minDistScaling = 0;
	  public float maxDistScaling = 0;
	  
	  public float minValScaling = 0;
	  public float maxValScaling = 0;
	  
	  public ArrayList<Float> DistHolder = new ArrayList<Float>(); //keep a list of all the costs. This is sourced from exhaustive qwop
	  public ArrayList<Float> ValHolder = new ArrayList<Float>();
	  
	  public int maxDepth = 2; //By default, using this for scaling, but should be changed from above.
	  
	  /** show score numbers by each end branch **/
  	  public boolean scoreDisplay = false;
	  public boolean valDisplay = false;
  	  /** Temp put drawing on hold for speed. Background calculations for node positions still happen though, so not fully efficient **/
  	  public boolean pauseDraw = false;
	  
	  //Internal thing to make sure that the data is there before I start to draw. 
	  private boolean startDrawing = false;
	  

	  
	  /** The actual pane made by this maker **/
	  public TreePane TreePanel;
	  
	  
	  //hold the time that the last drawing happened:
	  long lastTime;
	  long currTime;
	  int lastGameNum = 0;
	  int reportEvery = 10; //Normally games/s moves too much. Only report every handful of display changes.

	  int style = Font.BOLD;
	  Font bigFont = new Font ("Ariel", style , 36);
	  Font smallFont = new Font("Ariel",style, 14);
	  
	  public FontScaler scaleFont = new FontScaler(3,30,21);
	  
	  

	//When creating a new visualizer, wee need to know the root node so we can run down the tree and draw it.
	public TreePaneMaker(TrialNode root) {
		this.root = root;
		TreePanel = new TreePane();

        lastTime = System.currentTimeMillis(); //Grab the starting system time.
        
	}
	
	/** Call this externally to force a full update of the tree. This will go through all nodes, collect lines, and tell the graphics to update **/
	public void update(){
		startDrawing = true;
		if(root.PotentialChildren == 1){ //If the root node only has one child, then we're just going to move down and call the next node root for drawing purposes.
			root = root.GetChild(0);
		}

		 Lines = root.GetNodeLines();
		 if (!Lines.equals(null)){
			TreePanel.setTree(Lines);
		 	TreePanel.repaint();
		 }
	}
	
    public void setSnapshotPane(SnapshotPaneMaker snap){
  	  SnapshotPane = snap;
    }
 
	
	/** finds the standard deviation of all final costs to make a nice scaling for coloring. std dev to avoid outlier skewing **/
	public void ScaleDist(){
		if (!scoreDisplay) return; //Don't waste the computation if we're not displaying scores.
		//Find the mean
		float mean = 0;
		for (int i = 0; i<DistHolder.size(); i++){
			mean += DistHolder.get(i);
		}
		mean /= (float)DistHolder.size();
		
		float stdDev = 0;
		for (int i = 0; i<DistHolder.size(); i++){
			stdDev += (DistHolder.get(i)-mean)*(DistHolder.get(i)-mean);
		}
		stdDev = (float)Math.sqrt(stdDev/(float)DistHolder.size());
		
		minDistScaling = mean - stdDev*8f; //Sorta buggy thing about cost being negative means that these are actually the best ones.
		maxDistScaling = mean + stdDev*0.3f;
	}
	/** finds the standard deviation of all final costs to make a nice scaling for coloring. std dev to avoid outlier skewing **/
	public void ScaleVal(){
		if (!valDisplay) return; //Don't waste the computation if we're not displaying scores.
		//Find the mean
		float mean = 0;
		for (int i = 0; i<ValHolder.size(); i++){
			mean += ValHolder.get(i);
		}
		mean /= (float)ValHolder.size();
		
		float stdDev = 0;
		for (int i = 0; i<ValHolder.size(); i++){
			stdDev += (ValHolder.get(i)-mean)*(ValHolder.get(i)-mean);
		}
		stdDev = (float)Math.sqrt(stdDev/(float)ValHolder.size());
		
		minValScaling = mean - stdDev*3f; //Sorta buggy thing about cost being negative means that these are actually the best ones.
		maxValScaling = mean + stdDev*2f;
	}

    /** Jpanel inside the jframe **/
    class TreePane extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener,KeyListener{
  	  LineHolder Lines;
	  private SinglePathViewer pathView;
  	  boolean mouseTrack = false;
	  DecimalFormat df = new DecimalFormat("#.#");
	  int countLastReport = 0; //Some info is only reported every handful of paint calls. Keep a counter.
	  int gamespersec = 0;
	  
  	  boolean clearBackground = true;
  	  int mouseX = 0;
  	  int mouseY = 0;
  	  TrialNode focusedNode; //Node that's being selected, clicked, etc.
  	  
  	  public TreePane(){
  	  	  addMouseListener(this);
  	  	  addMouseMotionListener(this);
  	  	  addMouseWheelListener(this);
  	  	  addKeyListener(this);
  	  }

      public void paintComponent(Graphics g){
//			viewSingle.RunQueued();
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
    		  
    		  
    		  if(focusedNode != null){
    			  g.setColor(Color.RED);
    			  g.fillRect((int)focusedNode.nodeLocation[0]-5, (int)focusedNode.nodeLocation[1]-5, 10,10);   			  	  
    		  }
    		  
    		  
	      	for (int i = 0; i<Lines.numLines; i++){
	      		if(Lines.LineList[i][2] == 0 && Lines.LineList[i][3] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
	      			break;
	      		}
	      		g2.setColor(Lines.ColorList[i]);
	      		g2.setColor(getDepthColor(Lines.NodeList[i][1].TreeDepth));
	      		g2.drawLine(Lines.LineList[i][0], Lines.LineList[i][1], Lines.LineList[i][2], Lines.LineList[i][3]);
	      		
     			if (scoreDisplay && Lines.NodeList[i][1].DeadEnd){

     				g2.setColor(getScoreColor(minDistScaling,maxDistScaling,-Lines.NodeList[i][1].rawScore));
     				g2.setFont(scaleFont.InterpolateFont(minDistScaling,maxDistScaling,-Lines.NodeList[i][1].rawScore*OptionsHolder.sizeFactor));
      				g2.drawString(df.format(Lines.NodeList[i][1].rawScore), (int)Lines.NodeList[i][1].nodeLocation[0], (int)Lines.NodeList[i][1].nodeLocation[1]);
      				g2.setColor(Color.BLACK);
      			}else if (valDisplay && Lines.NodeList[i][1].DeadEnd){
     				if(Lines.NodeList[i][1].value != 0){
	     				g2.setColor(getScoreColor(minValScaling, maxValScaling, Lines.NodeList[i][1].value));
	     				g2.setFont(scaleFont.InterpolateFont(maxValScaling,minValScaling,Lines.NodeList[i][1].value*OptionsHolder.sizeFactor));
	      				g2.drawString(df.format(Lines.NodeList[i][1].value), (int)Lines.NodeList[i][1].nodeLocation[0], (int)Lines.NodeList[i][1].nodeLocation[1]);
	      				g2.setColor(Color.BLACK);
     				}
      				
      			}
	      		
	      		if(Lines.LabelOn[i]){ //Draw the label if it's turned on. NOTE: Change nodelist index back to zero for it to only display one action instead of all child node ones. Accidental change that turned out nicely.
	      			g.setColor(Color.BLACK);
	      			g.setFont(scaleFont.InterpolateFont(0, 4, OptionsHolder.sizeFactor));
	      			g.drawString(""+Lines.NodeList[i][1].ControlAction, (int)Lines.NodeList[i][1].nodeLocation[0], (int)Lines.NodeList[i][1].nodeLocation[1]);
	      		}
	      	}
	      	

	  		  //Write the instructions up too:
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
  		g.setColor(new Color(1f,1f,1f,0.8f));
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
      
      
      /** Set the single path viewer so we can queue up selected nodes for path viewing **/
      public void setSingleViewer(SinglePathViewer pathView){
    	  this.pathView = pathView;
      }
      
      
      /** Set the focused node **/
      public void setFocusNode(TrialNode focus){
    	  focusedNode = focus;
			if(pathView != null){ //For now, queue it up also for next time the animation panel is running.
				pathView.AddQueuedTrial(focusedNode);
			}
      }
      
      /** Get the focused node **/
      public TrialNode getFocusNode(){
    	  return focusedNode;
      }

      
      /** Convert a cost too a red-green color based on the std deviation sorta-full-scale **/
      public Color getScoreColor(float minScale, float maxScale, float cost)
      {
    	  
    	  float scaledCost = (cost - minScale)/(maxScale - minScale); //Scale the cost between 0 and 1 based on the std dev scaling. 
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
		}else if (arg0.isMetaDown()){
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			if (SnapshotPane != null){
				SnapshotPane.setNode(focusedNode);
			}
			if(pathView != null){
				pathView.AddQueuedTrial(focusedNode);
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

	
	// The following 2 methods are probably too complicated. when you push the arrow at the edge of one branch, this tries to jump to the nearest next branch node at the same depth.
	/** Called by key listener to change our focused node to the next adjacent one in the +1 or -1 direction **/
	private void arrowSwitchNode(int direction,int depth){
		//Stupid way of getting this one's index according to its parent.
		if(focusedNode != null){
			int thisIndex = focusedNode.ParentNode.GetChildIndex(focusedNode);
			//This set of logicals eliminates the edge cases, then takes the proposed action as default
			if (thisIndex == 0 && direction == -1){ //We're at the lowest index of this node and must head to a new parent node.
				ArrayList<TrialNode> blacklist = new ArrayList<TrialNode>(); //Keep a blacklist of nodes that already proved to be duds.
				blacklist.add(focusedNode);
				nextOver(focusedNode.ParentNode,blacklist,1,direction,focusedNode.ParentNode.GetChildIndex(focusedNode),0);
				
			}else if (thisIndex == focusedNode.ParentNode.NumChildren()-1 && direction == 1){ //We're at the highest index of this node and must head to a new parent node.
				ArrayList<TrialNode> blacklist = new ArrayList<TrialNode>();
				blacklist.add(focusedNode);
				nextOver(focusedNode.ParentNode,blacklist, 1,direction,focusedNode.ParentNode.GetChildIndex(focusedNode),0);
				
			}else{ //Otherwise we can just switch nodes within the scope of this parent.
				focusedNode = (focusedNode.ParentNode.GetChild(thisIndex+direction));
			}
			
			
			//These logicals just take the proposed motion (or not) and ignore any edges.
			if(depth == 1 && focusedNode.NumChildren()>0){ //Go further down the tree if this node has children
				focusedNode = focusedNode.GetChild(0);
			}else if(depth == -1 && focusedNode.TreeDepth>1){ //Go up the tree if this is not root.
				focusedNode = focusedNode.ParentNode;
			}
			SnapshotPane.setNode(focusedNode);
			SnapshotPane.update();
			repaint();
		}
	}
	
	/** Take a node back a layer. Don't return to node past. Try to go back out by the deficit depth amount in the +1 or -1 direction left/right **/
	private boolean nextOver(TrialNode current, ArrayList<TrialNode> blacklist, int deficitDepth, int direction,int prevIndexAbove,int numTimesTried){ // numTimesTried added to prevent some really deep node for causing some really huge search through the whole tree. If we don't succeed in a handful of iterations, just fail quietly.
		numTimesTried++;
		boolean success = false;
		//TERMINATING CONDITIONS-- fail quietly if we get back to root with nothing. Succeed if we get back to the same depth we started at.
		if (deficitDepth == 0){ //We've successfully gotten back to the same level. Great.
			focusedNode = current;
			return true;
		}else if(current.TreeDepth == 0){
			return true; // We made it back to the tree's root without any success. Just return.
		
		}else if(numTimesTried>100){// If it takes >100 movements between nodes, we'll just give up.
			return true;
		}else{
			//CCONDITIONS WE NEED TO STEP BACKWARDS TOWARDS ROOT.
			//If this new node has no children OR it's 1 child is on the blacklist, move back up the tree.
			if((prevIndexAbove+1 == current.NumChildren() && direction == 1) || (prevIndexAbove == 0 && direction == -1)){
				blacklist.add(current); 
				success = nextOver(current.ParentNode,blacklist,deficitDepth+1,direction,current.ParentNode.GetChildIndex(current),numTimesTried); //Recurse back another node.
			}else if (!(current.NumChildren() >0) || (blacklist.contains(current.GetChild(0)) && current.NumChildren() == 1)){ 
				blacklist.add(current); 
				success = nextOver(current.ParentNode,blacklist,deficitDepth+1,direction,current.ParentNode.GetChildIndex(current),numTimesTried); //Recurse back another node.
			}else{
				
				//CONDITIONS WE NEED TO GO DEEPER:
				if(direction == 1){ //March right along this previous node.
						for (int i = prevIndexAbove+1; i<current.NumChildren(); i++){
								success = nextOver(current.GetChild(i),blacklist,deficitDepth-1,direction,-1,numTimesTried);
								if(success){
									return true;
								}
							}
				}else if(direction == -1){ //March left along this previous node
						for (int i = prevIndexAbove-1; i>=0; i--){
								success = nextOver(current.GetChild(i),blacklist,deficitDepth-1,direction,current.GetChild(i).NumChildren(),numTimesTried);
								if(success){
									return true;
								}
						}
					}
				}
			}
		success = true;
		return success;

	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		
		//Navigating the focused node tree
		   int keyCode = arg0.getKeyCode();
		    switch( keyCode ) { 
		        case KeyEvent.VK_UP: //Go out the branches of the tree
		        	arrowSwitchNode(0,1);  
		            break;
		        case KeyEvent.VK_DOWN: //Go back towards root one level
		        	arrowSwitchNode(0,-1); 
		            break;
		        case KeyEvent.VK_LEFT: //Go left along an isobranch (like that word?)
		            arrowSwitchNode(-1,0);
		            break;
		        case KeyEvent.VK_RIGHT : //Go right along an isobranch
		            arrowSwitchNode(1,0);
		            break;
		     }
		    
		switch(arg0.getKeyChar()){
		case 's': // toggle the score text at the end of all branches
			if (arg0.isMetaDown()){
				valDisplay = !valDisplay;
				scoreDisplay = false;
			}else{
				scoreDisplay = !scoreDisplay;
				valDisplay = false;
			}
			break;
		case 'p': //Pause visualization.
			pauseDraw = !pauseDraw;
			break;
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
		ScaleDist();
		ScaleVal();	
	}

	@Override
	public void DoEvery() {
		update();
		
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
