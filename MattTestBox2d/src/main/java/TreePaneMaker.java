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

public class TreePaneMaker implements Schedulable, TabbedPaneActivator{

	private int interval = 1;
	private boolean activeTab = true;
	private boolean slave = false; //If it's a slave, then it's just for viewing specific trees and should not display as much info.
	
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
	  
	  /** A node to potentially use over and over again as a starting point in the search if it's selected **/
	  public boolean Override = false;
	  public TrialNode OverrideNode; 
	  
	  /** The actual pane made by this maker **/
	  public TreePane TreePanel;
	  
	  private TreePaneMaker slaveMaker;
	  
	  //hold the time that the last drawing happened:
	  long lastTime;
	  long currTime;
	  int lastGameNum = 0;
	  int reportEvery = 10; //Normally games/s moves too much. Only report every handful of display changes.

	  int style = Font.BOLD;
	  Font bigFont = new Font ("Ariel", style , 36);
	  Font smallFont = new Font("Ariel",style, 14);
	  
	  public FontScaler scaleFont = new FontScaler(3,20,10);

	//When creating a new visualizer, wee need to know the root node so we can run down the tree and draw it.
	public TreePaneMaker(TrialNode root, boolean slave) {
		this.root = root;
		TreePanel = new TreePane(slave);
		this.slave = slave;
        lastTime = System.currentTimeMillis(); //Grab the starting system time.
        
	}
	
	  public void giveSlave(TreePaneMaker slaveMaker){
  		  this.slaveMaker = slaveMaker;
  		  TreePanel.giveSlave(slaveMaker);
  	  }
	
	/** Change what the tree viewer thinks is the the root node **/
	public void setRoot(TrialNode root){
		this.root = root;
		if(slave){ //If this is a slave viewer, then make an alternate tree for it.
			root.MakeAltTree(true); //True means this is the new root of the alternate tree.
		}
	}
	
	/** Call this externally to force a full update of the tree. This will go through all nodes, collect lines, and tell the graphics to update **/
	public void update(){
		startDrawing = true;
//		if(root.PotentialChildren == 1){ //If the root node only has one child, then we're just going to move down and call the next node root for drawing purposes.
//			root = root.GetChild(0);
//		}

		 Lines = root.GetNodeLines();
		 if (!Lines.equals(null)){
			TreePanel.setTree(Lines);
		 	TreePanel.repaint();
		 }
		 
		 //If we have a slave pane, and we've changed its focus node, then let it change in this pane too.
		 if(slaveMaker != null && slaveMaker.TreePanel.focusedNode != null && !slaveMaker.TreePanel.focusedNode.equals(TreePanel.focusedNode)){
			 TreePanel.focusedNode = slaveMaker.TreePanel.focusedNode;
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
	  boolean slave = false;
  	  boolean clearBackground = true;
  	  int mouseX = 0;
  	  int mouseY = 0;
  	  TrialNode focusedNode; //Node that's being selected, clicked, etc.
  	  TreePaneMaker slaveMaker; //A tree viewer can have a slave panel which can have sub-views of the tree.
  	  
  	  public TreePane(boolean slave){
  	  	  this.slave = slave;
  	  	  addKeyListener(this);
  	  	  addMouseListener(this);
  	  	  addMouseMotionListener(this);
  	  	  addMouseWheelListener(this);
  	  }
  	  
  	  private void giveSlave(TreePaneMaker slaveMaker){
  		  this.slaveMaker = slaveMaker;
  	  }

      public void paintComponent(Graphics g){
//			viewSingle.RunQueued();
    	  if(!pauseDraw){ //Temporarily stop drawing for speed.
    	   Graphics2D g2 = (Graphics2D) g; //Casting to a graphics 2d object for more control.
    	   g2.setStroke(new BasicStroke(0.5f));
    	   
    	  //Go through and draw all the lines defined.
    	  if (startDrawing){ //Make sure this exists.
    		  
//    		  if(mouseTrack || clearBackground){//Completely overwrite the background if dragging.
//        		  g.setColor(Color.WHITE);
//        		  clearBackground = false;
//        		  
//    		  }else{//allow a little bit of alpha to see where new branches are failing.
//        		  g.setColor(new Color(1f,1f,1f,1f)); //Write a new rectangle over the whole thing with some amount of alpha. This means that the failed branches will fade out.
//
//    		  }
    		  g.setColor(Color.WHITE);
    		  g.fillRect(0, 0, OptionsHolder.windowWidth,OptionsHolder.windowHeight);
    		  
    		  
    		  if(!slave){
	    		  if(focusedNode != null){
	    			  g.setColor(Color.RED);
	    			  g.fillRect((int)focusedNode.nodeLocation[0]-5, (int)focusedNode.nodeLocation[1]-5, 10,10);   			  	  
	    		  }
	    		  
	    		  
		      	for (int i = 0; i<Lines.numLines; i++){
		      		if(Lines.LineList[i][2] == 0 && Lines.LineList[i][3] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
		      			break;
		      		}
		      		if(!Lines.ColorList[i].equals(Color.BLACK)){
							g2.setColor(Lines.ColorList[i]);
					}else{
						g2.setColor(getDepthColor(Lines.NodeList[i][1].TreeDepth));
					}
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
	     			
	     			if(Lines.NodeList[i][1].colorOverride != Color.BLACK){
	     				g.setColor(Lines.NodeList[i][1].colorOverride);
     					g2.fillOval((int)(Lines.NodeList[i][1].nodeLocation[0]-10*OptionsHolder.sizeFactor), (int)(Lines.NodeList[i][1].nodeLocation[1]-10*OptionsHolder.sizeFactor), (int)(20*OptionsHolder.sizeFactor),(int)(20*OptionsHolder.sizeFactor));
     				
	     			}else
	     			//Draw failure modes by color as dots on the ends of the failed branches
	     			if (!Lines.NodeList[i][1].TempFullyExplored && Lines.NodeList[i][1].DeadEnd && OptionsHolder.failTypeDisp ){
	     				if (Lines.NodeList[i][1].FailType == StateHolder.FailMode.BACK){ // Failures -- we fell backwards
	     					g.setColor(Color.CYAN);
	     					g2.fillOval((int)(Lines.NodeList[i][1].nodeLocation[0]-5*OptionsHolder.sizeFactor), (int)(Lines.NodeList[i][1].nodeLocation[1]-5*OptionsHolder.sizeFactor), (int)(10*OptionsHolder.sizeFactor),(int)(10*OptionsHolder.sizeFactor));
	     				}else if(Lines.NodeList[i][1].FailType == StateHolder.FailMode.FRONT){ // Failures -- we fell forward.
	     					g.setColor(Color.MAGENTA);
	     					g2.fillOval((int)(Lines.NodeList[i][1].nodeLocation[0]-5*OptionsHolder.sizeFactor), (int)(Lines.NodeList[i][1].nodeLocation[1]-5*OptionsHolder.sizeFactor), (int)(10*OptionsHolder.sizeFactor),(int)(10*OptionsHolder.sizeFactor));
	     				}
	     			}
	     			
	     			if(Lines.NodeList[i][1].TempFullyExplored && OptionsHolder.failTypeDisp && Lines.NodeList[i][1].NumChildren()==0){ //These are nodes that we've stopped at due to a depth limit, but COULD go further (not failures).
     					g.setColor(Color.darkGray);
     					g2.fillOval((int)(Lines.NodeList[i][1].nodeLocation[0]-10*OptionsHolder.sizeFactor), (int)(Lines.NodeList[i][1].nodeLocation[1]-10*OptionsHolder.sizeFactor), (int)(20*OptionsHolder.sizeFactor),(int)(20*OptionsHolder.sizeFactor));
     				}
		      		
		      		if(Lines.LabelOn[i]){ //Draw the label if it's turned on. NOTE: Change nodelist index back to zero for it to only display one action instead of all child node ones. Accidental change that turned out nicely.
		      			g.setColor(Color.BLACK);
		      			g.setFont(scaleFont.InterpolateFont(0, 4, OptionsHolder.sizeFactor));
		      			g.drawString(""+Lines.NodeList[i][1].ControlAction, (int)Lines.NodeList[i][1].nodeLocation[0], (int)Lines.NodeList[i][1].nodeLocation[1]);
		      		}
		      	}
    		  }else{
	    		  if(focusedNode != null){
	    			  g.setColor(Color.RED);
	    			  g.fillRect((int)focusedNode.nodeLocation2[0]-5, (int)focusedNode.nodeLocation2[1]-5, 10,10);   			  	  
	    		  }
	    		  
	    		  
		      	for (int i = 0; i<Lines.numLines; i++){
		      		if(Lines.LineList2[i][2] == 0 && Lines.LineList2[i][3] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
		      			break;
		      		}
		      		if(!Lines.ColorList[i].equals(Color.BLACK)){
							g2.setColor(Lines.ColorList[i]);
					}else{
						g2.setColor(getDepthColor(Lines.NodeList[i][1].TreeDepth));
					}
		      		g2.drawLine(Lines.LineList2[i][0], Lines.LineList2[i][1], Lines.LineList2[i][2], Lines.LineList2[i][3]);
		      		
	     			if (scoreDisplay && Lines.NodeList[i][1].DeadEnd){
	
	     				g2.setColor(getScoreColor(minDistScaling,maxDistScaling,-Lines.NodeList[i][1].rawScore));
	     				g2.setFont(scaleFont.InterpolateFont(minDistScaling,maxDistScaling,-Lines.NodeList[i][1].rawScore*OptionsHolder.sizeFactor));
	      				g2.drawString(df.format(Lines.NodeList[i][1].rawScore), (int)Lines.NodeList[i][1].nodeLocation2[0], (int)Lines.NodeList[i][1].nodeLocation2[1]);
	      				g2.setColor(Color.BLACK);
	      			}else if (valDisplay && Lines.NodeList[i][1].DeadEnd){
	     				if(Lines.NodeList[i][1].value != 0){
	     					
	     					g2.setColor(getScoreColor(minValScaling, maxValScaling, Lines.NodeList[i][1].value));
	 
		     				g2.setFont(scaleFont.InterpolateFont(maxValScaling,minValScaling,Lines.NodeList[i][1].value*OptionsHolder.sizeFactor));
		      				g2.drawString(df.format(Lines.NodeList[i][1].value), (int)Lines.NodeList[i][1].nodeLocation2[0], (int)Lines.NodeList[i][1].nodeLocation2[1]);
		      				g2.setColor(Color.BLACK);
	     				}
	      				
	      			}
		      		
		      		if(i<Lines.LabelOn.length && Lines.LabelOn[i] && i<Lines.LabelOn.length-1){ //Draw the label if it's turned on. NOTE: Change nodelist index back to zero for it to only display one action instead of all child node ones. Accidental change that turned out nicely.
		      			g.setColor(Color.BLACK);
		      			g.setFont(scaleFont.InterpolateFont(0, 4, OptionsHolder.sizeFactor));
		      			g.drawString(""+Lines.NodeList[i][1].ControlAction, (int)Lines.NodeList[i][1].nodeLocation2[0], (int)Lines.NodeList[i][1].nodeLocation2[1]);
		      		}
		      		
	     			//Draw failure modes by color as dots on the ends of the failed branches
	     			if (Lines.NodeList[i][1].DeadEnd && OptionsHolder.failTypeDisp){
	     					if (Lines.NodeList[i][1].FailType == StateHolder.FailMode.BACK){
	     						g.setColor(Color.CYAN);
	     						g2.fillOval((int)(Lines.NodeList[i][1].nodeLocation2[0]-5*OptionsHolder.sizeFactorAlt), (int)(Lines.NodeList[i][1].nodeLocation2[1]-5*OptionsHolder.sizeFactorAlt), (int)(10*OptionsHolder.sizeFactorAlt),(int)(10*OptionsHolder.sizeFactor));
	     					}else if(Lines.NodeList[i][1].FailType == StateHolder.FailMode.FRONT){
	     						g.setColor(Color.MAGENTA);
	     						g2.fillOval((int)(Lines.NodeList[i][1].nodeLocation2[0]-5*OptionsHolder.sizeFactorAlt), (int)(Lines.NodeList[i][1].nodeLocation2[1]-5*OptionsHolder.sizeFactorAlt), (int)(10*OptionsHolder.sizeFactorAlt),(int)(10*OptionsHolder.sizeFactorAlt));
	     					}
	     			}
		      	}
    		  }
	      	

	  		  //Write the instructions up too:
	      	if( !slave ){
		  		  g.setColor(Color.BLACK);
		  		  g.setFont(smallFont);
		  		  for (int i = 0; i<instructions.length; i++){
		  			g.drawString(instructions[instructions.length-1-i], 10, OptionsHolder.windowHeight-i*25-50);
		  			  
		  		  }
	      	} 	
    	  }  		  
    	  }
  		  //Write how many games have been played:
    	  //note, still displays even when graphics are basically paused.
    	  if(!slave){ //for a slave panel, we don't want this extra info.
	  		  g.setColor(Color.WHITE);
	  		  g.setColor(new Color(1f,1f,1f,1f));
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
			if(slave){// If slave, search among the alternate node locations in the subview panel.
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			}
			focusedNode.LabelOn = true;
		}else if (arg0.isControlDown()){ //Control will hide this node and all its children.
			if(slave){// If slave, search among the alternate node locations in the subview panel.
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			}
			if(focusedNode !=null && focusedNode.TreeDepth > 1){ //Keeps stupid me from hiding everything in one click.
				focusedNode.hiddenNode = true;
				focusedNode.ParentNode.RemoveChild(focusedNode); //Try also just killing it from the tree search too.
			}
		}else if (arg0.isMetaDown()){
			if(slave){// If slave, search among the alternate node locations in the subview panel.
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
				if(slaveMaker != null){ //If we have a slave panel, then change its focus to our focus too
					slaveMaker.TreePanel.focusedNode = focusedNode;
				}
			}
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
		if(slave){// If slave, search among the alternate node locations in the subview panel.
			focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
		}else{
			focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
		}
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
			if(slave){
				root.ShiftNodesAlt(arg0.getX()-mouseX, arg0.getY()-mouseY);

			}else{
				root.ShiftNodes(arg0.getX()-mouseX, arg0.getY()-mouseY);
			}
		}else if (arg0.getButton() == MouseEvent.BUTTON3){ //Right click moves nodes.
			//Calculate the angle between the click and the parent of the click node.

			if(slave){
				
				double clickAngle = -Math.atan2((arg0.getX()-focusedNode.ParentNode.nodeLocation2[0]),(arg0.getY()-focusedNode.ParentNode.nodeLocation2[1]))+Math.PI/2.;
				clickAngle -= focusedNode.nodeAngle2; //Subtract out the current angle.
				
				focusedNode.RotateBranchAlt(clickAngle);
			}else{
				
				double clickAngle = -Math.atan2((arg0.getX()-focusedNode.ParentNode.nodeLocation[0]),(arg0.getY()-focusedNode.ParentNode.nodeLocation[1]))+Math.PI/2.;
				clickAngle -= focusedNode.nodeAngle; //Subtract out the current angle.
				
				focusedNode.RotateBranch(clickAngle);
			}	
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
			if(slave){// If slave, search among the alternate node locations in the subview panel.
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			}
			if(slave){
				focusedNode.SpaceBranchAlt(0.1);
			}else{
				focusedNode.SpaceBranch(0.1);
			}

			
		}else{
			SizeChanger*=1.1;
		}
	}else{
		if(arg0.isAltDown()){
			if(slave){// If slave, search among the alternate node locations in the subview panel.
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			}
			if(slave){
				focusedNode.SpaceBranchAlt(-0.1);
			}else{
				focusedNode.SpaceBranch(-0.1);
			}
			
		}else{
			SizeChanger*=0.9;
		}

	}
	clearBackground = true;
		
	}

	float SizeChanger = 1;
	
	public void DoResize(){
		if(SizeChanger != 1){
			if(slave){
				root.ZoomNodesAlt(SizeChanger);
				OptionsHolder.ChangeSizeFactorAlt(SizeChanger);
			}else{
				root.ZoomNodes(SizeChanger);
				OptionsHolder.ChangeSizeFactor(SizeChanger);
			}
			SizeChanger = 1;
		}
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
			if(slaveMaker != null){ //If we have a slave panel, then change its focus to our focus too
				slaveMaker.TreePanel.focusedNode = focusedNode;
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
		switch(arg0.getKeyChar()){
			case 't': // explore specific branch by setting an override node.
				//Uncolor the previous subtree, if it exists.
				if(OverrideNode != null){
					OverrideNode.ColorChildren(Color.BLACK);
				}
				
				if(focusedNode != null && !focusedNode.FullyExplored){
					OverrideNode = focusedNode;
					Override = !Override;
					if(Override){
						OverrideNode.ColorChildren(Color.ORANGE);
					}else{
						OverrideNode.ColorChildren(Color.BLACK);
					}
					
					if(slaveMaker != null){
						slaveMaker.setRoot(focusedNode);
						
					}
				}
				break;
			}
		}
	
    /** externally set the override node **/
    public void setOverride(TrialNode node){
		if(OverrideNode != null){
			OverrideNode.ColorChildren(Color.BLACK); // if we already have an override. then set this old one back to default colors.
		}
		focusedNode = node; //Change the focus to the specified new override node.
		OverrideNode = node; // make the given node our override also
		Override = true; // set override flag to true.
		OverrideNode.ColorChildren(Color.ORANGE);
			
		if(slaveMaker != null){ //If we have a slave pane, then also give it this node to visualize.
			slaveMaker.setRoot(focusedNode);		
		}
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
		if (OptionsHolder.delayTreeMoves){
			TreePanel.DoResize(); 
		}
	}

	@Override
	public void DoEvery() {
		if(!OptionsHolder.delayTreeMoves){
			TreePanel.DoResize();
		}
		if(activeTab){
			update();
		}
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
		activeTab = true;
		
	}

	@Override
	public void DeactivateTab() {
		activeTab = false;
		
	}
}
