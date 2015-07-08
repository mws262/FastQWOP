package TreeQWOP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles all display of trees, but NOT their geometry (geometry is stored in the nodes themselves).
 * Does colors, line drawing, node selection...
 * Now has normal jpanel and new openGL versions. In the process of switching wholy to GL but may leave other stuff as backup/debugging.
 * If this is flagged as a slave panel, it is meant for displaying subtrees only.
 * 
 * @author Matt
 *
 */


public class TreePaneMaker implements Schedulable, TabbedPaneActivator{

	private int interval = 1;
	private boolean activeTab = true; //Is this tab active? Important for passing focus between panels for mouse/keyboard callbacks.
	private boolean slave = false; //If it's a slave, then it's just for viewing specific trees and should not display as much info.
	
	//Instructions string gets displayed on the side of the main tree pane, but not the slave(s).
	public static final String[] instructions = {
		"Left click & drag pans.",
		"Scroll wheel zooms.",
		"Right click & drag rotates individual branches",
		"Alt-click labels the control.",
		"Ctrl-click hides a branch.",
		"Meta-click selects a point for state viewer.",
		"Alt-scroll spaces or contracts branches hovered over.",
		"S turns score display on and off.",
		"Meta-S turns value display.",
		"Meta-arrows rotates camera.",
		"Alt-LR arrow twists camera",
		"C toggles camera auto-follow."
	};
	
	
	
	public static int TreeDepthOffset = 0; //Do we skip an of the original depth of the tree?
	
	  /** This is the root node from which the tree will be built out of **/
	  public CopyOnWriteArrayList<TreeHandle> trees = new CopyOnWriteArrayList<TreeHandle>();
	  public TreeHandle activeTree;
	  private SnapshotPaneMaker SnapshotPane;
	  
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
	  
	  /** If this panel is the master, the give it a reference to the slave panel for passing the focus node between them **/
	  private TreePaneMaker slaveMaker;
	  
	  /** Do we use openGl or the old Graphics2d version? **/
	  public final boolean useGL;
	  
	  //hold the time that the last drawing happened:
	  private long lastTime;
	  private long currTime;
	  private int lastGameNum = 0;
	  private int reportEvery = 40; //Normally games/s moves too much. Only report every handful of display changes (OLD)

	  private int style = Font.BOLD;
	  private Font bigFont = new Font ("Ariel", style , 36);
	  private Font smallFont = new Font("Ariel",style, 14);
	  
	  public FontScaler scaleFont = new FontScaler(3,20,10);

	  /** To create a new visualizer, must supply a starting tree and whether this is a slave panel **/
	  public TreePaneMaker(CopyOnWriteArrayList<TreeHandle> trees, boolean slave, boolean useGL) {
		
        this.useGL = useGL;
        
        for(TreeHandle th: trees){
        	this.trees.add(th);
        }
        
        //Create the actual JPanel for the tree. Make sure the treepane knows if it's a slave.
		TreePanel = new TreePane(slave);
		this.slave = slave;
        lastTime = System.currentTimeMillis(); //Grab the starting system time.

	}
	
	  /** If this is the main pane, it should be given access to its slave (if it exists) **/
	  public void giveSlave(TreePaneMaker slaveMaker){
  		  this.slaveMaker = slaveMaker;
  		  TreePanel.giveSlave(slaveMaker);
  	  }
	
	/** Add another root node to the list of trees we're tracking. **/
	public void addTree(TreeHandle TH){
		
		if(!trees.contains(TH)){
			trees.add(TH);
		}else{
			System.out.println("we tried to add a root already added in TreePaneMaker.");
		}
	}
	
	/** Let the Tree maker and potentially its slave what tree we're working on **/
	public void setActiveTree(TreeHandle activeTree){
		if(!slave){
			slaveMaker.setActiveTree(activeTree);
		}
		this.activeTree = activeTree;
	}
	
	/** Call this externally to force a full update of the tree. This will go through all nodes and all trees, collect lines, and tell the graphics to update **/
	public void update(){
		startDrawing = true; //This makes sure that no drawing occurs until I actually call updates. This prevents drawing while tons of stuff is still null.
		
		TreePanel.setTree(trees);
		if(trees != null){
			TreePanel.repaint();
		}
		 
		 //If we have a slave pane, and we've changed its focus node, then let it change in this pane too.
		 if(slaveMaker != null && slaveMaker.TreePanel.focusedNode != null && !slaveMaker.TreePanel.focusedNode.equals(TreePanel.focusedNode)){
			 TreePanel.focusedNode = slaveMaker.TreePanel.focusedNode;
		 }
	}
	
	/** Have a reference to the runner snapshot pane, so we can pass new focus nodes to it. **/
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

    /**
     * Actual Jpanel itself that can be added as either a slave or a main tree display.
     * 
     * @author Matt
     *
     */
    class TreePane extends GLJPanel implements MouseListener, MouseMotionListener, MouseWheelListener,KeyListener,GLEventListener{
  	  /** Reference to the container which has all the nodes and lines we wish to display **/
      private CopyOnWriteArrayList<TreeHandle> trees = new CopyOnWriteArrayList<TreeHandle>();
  	  
      /** Reference to the runner's single run animator. Needed for passing new focused nodes around **/
	  private SinglePathViewer pathView;
	  
	  /** Flag to know whether the mouse is currently pressed. **/
  	  private boolean mouseTrack = false;
  	  
  	  /** Format numbers to truncate decimal places when displaying distances traveled **/
	  private DecimalFormat df = new DecimalFormat("#");
	  
	  private int countLastReport = 0; //Some info is only reported every handful of paint calls. Keep a counter.
	  
	  /** Games/sec counter for display on the window. **/
	  private int gamespersec = 0;
	  
	  /** Is this panel a slave panel? **/
	  private boolean slave = false;

	  /** Keep track of mouse coordinates within this panel. **/
  	  private int mouseX = 0;
  	  private int mouseY = 0;
  	  
  	  /** Reference to a selected node **/
  	  public TrialNode focusedNode; //Node that's being selected, clicked, etc.
  	  
  	  /** Reference to the slave panel's maker (if it has a slave) **/
  	  private TreePaneMaker slaveMaker; //A tree viewer can have a slave panel which can have sub-views of the tree.
  	  
  	  
  	  /** Does the camera follow the center of the newest tree? **/
  	  private boolean camFollow = true;
  	  
  	  /** GLU is the line/point graphics **/
	  private GLU glu;
	  
	  /** GlUT is used for text **/
	  private GLUT glut;
	   
	  private int width=400; // I think these values are meaningless since they change as per the layout manager.
	  private int height = 200;

	  /** Actual frame object **/
	  private Frame frame;

	  /** If using openGL, we have to put a special GLCanvas inside the frame **/
	  private GLCanvas canvas;

	  private CamManager cam;

	  //ONLY APPLIES TO GL. Camera Settings. Initially camera is pointed -z, y is up. x is left?
	  /** Scaling of node coordinates to GL coordinates. **/
	  public float oldToGLScaling = 0.01f;

	  /** Scaling of mouse movements to GL coordinates. TODO: Probably should just calculate this from view angle **/
	  public float glScaling = 3.5f;




	  /** For rendering text overlays. Note that textrenderer is for overlays while GLUT is for labels in world space **/
	  TextRenderer textRenderBig;
	  TextRenderer textRenderSmall;
		
	  /** Constructor. Set up as either GL or not, Slave or not. **/
  	  public TreePane(boolean slave){
  		  
  		  	
  	  	  	this.slave = slave;

  	  		 this.setLayout(new BorderLayout());
  	  		 GLProfile glp = GLProfile.getDefault();
  	  		 GLCapabilities caps = new GLCapabilities(glp);
  	  		 canvas = new GLCanvas(caps);
  	  		 //Not sure why, but this keeps the pane from changing sizes every time I change tabs. The actual numbers seem pretty arbitrary.
 	         canvas.setSize(new Dimension(100,30));
 	         canvas.setMaximumSize(new Dimension(100,30));
 	         canvas.setMinimumSize(new Dimension(100,30));

  	  		 //Add listeners to the canvas rather than frame if we're using GL
  	  		  canvas.addGLEventListener(this);
 	          this.add(canvas);
 	       	  canvas.setFocusable(true);
	   	  	  canvas.addKeyListener(this);
	   	  	  canvas.addMouseListener(this);
	   	  	  canvas.addMouseMotionListener(this);
	   	  	  canvas.addMouseWheelListener(this);
	   	  	  glut = new GLUT();
	   	  	  
	   	  	  textRenderBig = new TextRenderer(new Font("Calibri", Font.BOLD, 36));
	   	  	  textRenderSmall = new TextRenderer(new Font("Calibri", Font.PLAIN, 18));
	   	  	  
	   	  	  //If this is a slave window, we need to move the camera over.
	   	  	  if(!slave){
	   	  		  cam = new CamManager(width,height); //Default camera placement
	   	  	  }else{
	   	  		  //x - 10 from default for the slave panel.
	   	  		cam = new CamManager(width,height,new Vector3f(3, 5, 50),new Vector3f(3, 5, 0)); 
	   	  	  }
	        
  	  }
  	  
  	  /** If this is the master tree frame, pass it a slave panel **/
  	  private void giveSlave(TreePaneMaker slaveMaker){
  		  this.slaveMaker = slaveMaker;
  	  }
  	  
  	  /** Paint for this JPanel pretty much just asks the sub-canvas to paint. Somewhat a relic of the old graphics **/
      public void paintComponent(Graphics g){
    	  if(!pauseDraw && canvas != null){   
    		  canvas.display();
    	  }
      }
      /** Pass the list of tree handles to this panel **/
      public  void setTree(CopyOnWriteArrayList<TreeHandle> trees){
    	  if(!slave){
    		  this.trees.clear();
    		  for (TreeHandle th :trees){
    			  this.trees.add(th);
    		  }
    	  }
      }
      /** Empty out the list of trees to be drawn **/
      public  void clearTrees(){
    	  trees.clear();
      }
      /** Add a single tree for drawing **/
      public  void addSingleTree(TreeHandle tree){
    	  trees.add(tree);
      }   
      /** Set the single path viewer so we can queue up selected nodes for path viewing **/
      public void setSingleViewer(SinglePathViewer pathView){
    	  this.pathView = pathView;
      } 
      /** Set the focused node **/
      public  void setFocusNode(TrialNode focus){
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

    	  float H = (float)(scaledDepth* 0.38)+0.35f;
    	  float S = 1f; // Saturation
    	  float B = 0.85f; // Brightness

    	  return Color.getHSBColor(H, S, B);
      }


      @Override
      public void mouseClicked(MouseEvent e) {	

    	  if(slave){// If slave, search among the alternate node locations in the subview panel.
    		  focusedNode = cam.nodeFromClick(e.getX(), e.getY(), trees, oldToGLScaling, true);
    	  }else{
    		  focusedNode = cam.nodeFromClick(e.getX(), e.getY(), trees, oldToGLScaling, false);
    		  if(slaveMaker != null){ //If we have a slave panel, then change its focus to our focus too
    			  slaveMaker.TreePanel.focusedNode = focusedNode;
    		  }	
    	  }

    	  if (e.isAltDown()){ //alt click enables a label on this node
    		  focusedNode.LabelOn = true;
    	  }else if (e.isControlDown()){ //Control will hide this node and all its children.

    		  if(focusedNode !=null && focusedNode.TreeDepth > 1){ //Keeps stupid me from hiding everything in one click.
    			  focusedNode.hiddenNode = true;
    			  focusedNode.ParentNode.RemoveChild(focusedNode); //Try also just killing it from the tree search too.
    		  }
    	  }else if (e.isMetaDown()){

    		  if (SnapshotPane != null){
    			  SnapshotPane.setNode(focusedNode);
    		  }
    		  if(pathView != null){
    			  pathView.AddQueuedTrial(focusedNode);
    		  }

    	  }
      }
	@Override
	public void mouseEntered(MouseEvent e) {
		this.requestFocus();

	}
	@Override
	public void mouseExited(MouseEvent e) {

	}
	@Override
	public void mousePressed(MouseEvent e) {
		mouseTrack = true;
		mouseX = e.getX();
		mouseY = e.getY();
		if (e.getButton() == MouseEvent.BUTTON3){ //Right click moves nodes.
			if(slave){// If slave, search among the alternate node locations in the subview panel.
				focusedNode = cam.nodeFromClick(e.getX(), e.getY(), trees, oldToGLScaling, true);
			}else{
				focusedNode = cam.nodeFromClick(e.getX(), e.getY(), trees, oldToGLScaling, true);
			}
		}

	}
	@Override
	public void mouseReleased(MouseEvent e) {
		mouseTrack = false;
	}
	@Override
	public void mouseDragged(MouseEvent e){
		//Note: if using old graphics, actually move the point coordinates. if GL, then just move the camera.
		if (e.getButton() == MouseEvent.BUTTON1){ //Left click drags the whole thing.
			Vector3f relCamMove = cam.windowFrameToWorldFrameDiff(e.getX(), e.getY(), mouseX, mouseY, oldToGLScaling, glScaling);

			cam.smoothTranslateRelative(relCamMove, relCamMove, 5);

		}else if (e.getButton() == MouseEvent.BUTTON3){ //Right click moves nodes.
			Vector3f clickedpt = cam.planePtFromRay(e.getX(), e.getY(),oldToGLScaling,0);
			double clickAngle = -Math.atan2((clickedpt.x-focusedNode.ParentNode.nodeLocation[0]),(clickedpt.y-focusedNode.ParentNode.nodeLocation[1]))+Math.PI/2.;
			clickAngle -= focusedNode.nodeAngle; //Subtract out the current angle.
			focusedNode.RotateBranch(clickAngle);
		}
		mouseX = e.getX();
		mouseY = e.getY();
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	/** This zooms in and out. Also changes the size factor in OptionsHolder to keep things consistent. **/
	public  void mouseWheelMoved(MouseWheelEvent e){
	
	
	if (e.getWheelRotation()<0){ //Negative mouse direction -> zoom in.
		if(e.isAltDown()){
//			if(slave){// If slave, search among the alternate node locations in the subview panel. TODO:
//				focusedNode = Lines.GetNearestNodeAlt(e.getX(), e.getY());
//			}else{
//				focusedNode = Lines.GetNearestNode(e.getX(), e.getY());
//			}
//			if(slave){
//				focusedNode.SpaceBranchAlt(0.1);
//			}else{
//				focusedNode.SpaceBranch(0.1);
//			}


		}else{
			cam.smoothZoom(0.9f, 5);
			glScaling*=0.9;
		}
	}else{
		if(e.isAltDown()){
//			if(slave){// If slave, search among the alternate node locations in the subview panel.
//				focusedNode = Lines.GetNearestNodeAlt(e.getX(), e.getY());
//			}else{
//				focusedNode = Lines.GetNearestNode(e.getX(), e.getY());
//			}
//			if(slave){
//				focusedNode.SpaceBranchAlt(-0.1);
//			}else{
//				focusedNode.SpaceBranch(-0.1);
//			}
//			
		}else{
			cam.smoothZoom(1.1f, 5);
			glScaling*=1.1;
		}
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
			if(SnapshotPane != null){
				SnapshotPane.setNode(focusedNode);
				SnapshotPane.update();
			}
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
	public void keyPressed(KeyEvent e) {
		
		//Navigating the focused node tree
		   int keyCode = e.getKeyCode();

		    if(e.isMetaDown() && useGL){ //if we're using GL, then we'll move the camera with mac key + arrows
		    	switch( keyCode ) { 
		        case KeyEvent.VK_UP: //Go out the branches of the tree
		        	cam.smoothRotateLong(0.1f, 5);
		            break;
		        case KeyEvent.VK_DOWN: //Go back towards root one level
		        	cam.smoothRotateLong(-0.1f, 5);
		            break;
		        case KeyEvent.VK_LEFT: //Go left along an isobranch (like that word?)
		        	cam.smoothRotateLat(0.1f, 5);
		            break;
		        case KeyEvent.VK_RIGHT : //Go right along an isobranch
		        	cam.smoothRotateLat(-0.1f, 5);
		            break;
			    }
		     }else if(e.isAltDown()){
			    	switch( keyCode ) { 
			    	case KeyEvent.VK_LEFT: //Go left along an isobranch (like that word?)
			    		cam.smoothTwist(0.1f, 5);
			    		break;
			    	case KeyEvent.VK_RIGHT : //Go right along an isobranch
			    		cam.smoothTwist(-0.1f, 5);
			    		break;
			    	}
		     }else{
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
			        case KeyEvent.VK_C : //toggle camera following of new trees.
			        	camFollow = !camFollow;
			        	break;
			     }
		    	
		    }
		    
		switch(e.getKeyChar()){
		case 's': // toggle the score text at the end of all branches
			if (e.isMetaDown()){
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
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {
		switch(e.getKeyChar()){
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
//						slaveMaker.setRoot(focusedNode); //TODO fix
//						slaveMaker.TreePanel.setTree(new ArrayList);
						focusedNode.MakeAltTree(true); //True means this is the new root of the alternate tree.
						slaveMaker.TreePanel.clearTrees();
						slaveMaker.TreePanel.addSingleTree(new TreeHandle(focusedNode));
						
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
//			slaveMaker.setRoot(focusedNode); //TODO fix	
			focusedNode.MakeAltTree(true); //True means this is the new root of the alternate tree.
			slaveMaker.TreePanel.clearTrees();
			slaveMaker.TreePanel.addSingleTree(new TreeHandle(focusedNode));
		}
    }
    
    /** Draw a text string using GLUT (for openGL rendering version of my stuff) **/
	  public void drawString(String toDraw, float x, float y, float z, GL2 gl, GLUT glut ) 
      {
        // Fomat numbers with Java.
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
       
        // Printing fonts, letters and numbers is much simpler with GLUT.
        // We do not have to use our own bitmap for the font.
//        gl.glColor3f(0, 0, 0);
        gl.glRasterPos3d(x,y,z);
        glut.glutBitmapString(
          GLUT.BITMAP_HELVETICA_12, 
          toDraw);
      }

	  
	  int numtrees = 0;
	  int count = 0;
    /** 
     * Main event loop: OpenGL display
     * advance. GLEventListener implementation.
     */
	@Override
	public  void display(GLAutoDrawable drawable) {

		
		
		GL2 gl = drawable.getGL().getGL2();
		GLUT glut = new GLUT();

		//Background color that defaults when canvas is cleared.
		if (OptionsHolder.darkTheme){
			gl.glClearColor(0.1f,0.1f,0.2f,0.2f);
		}else{
			gl.glClearColor(1f,1f,1f,1f);
		}

		//clear it out.
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		if (glu == null) glu = GLU.createGLU();
		
		/* All camera updates */
		if(trees == null){
			return;
		}

		if(slave){// have slave tree gently rotate(no point to this)
			cam.twistCW(0.0005f);
			cam.smoothRotateLong((float) (0.002*Math.cos(count/100.)), 1);
			cam.smoothRotateLat((float) (0.002*Math.sin(count/100.)), 1);

			count++;
		}

		//if we want the camera to follow along with new trees. press c to toggle.
		if( camFollow && trees.size() >0 && numtrees !=trees.size() && !slave){
			
			glScaling = 3.5f;
			Vector3f tarpos = new Vector3f(oldToGLScaling*trees.get(trees.size()-1).getRoot().nodeLocation[0],oldToGLScaling*trees.get(trees.size()-1).getRoot().nodeLocation[1],oldToGLScaling*trees.get(trees.size()-1).getRoot().height);
			Vector3f campos = new Vector3f(-5,-10,50);
			campos.add(tarpos);
			cam.smoothTranslateAbsolute(campos, tarpos, 20);
			numtrees = trees.size();
		}
		cam.update(gl, glu);
		
		//Blue sky background:
//		gl.glMatrixMode(GL2.GL_MODELVIEW);
//		gl.glPushMatrix();
//		gl.glLoadIdentity();
//		gl.glMatrixMode(GL2.GL_PROJECTION);
//		gl.glPushMatrix();
//		gl.glLoadIdentity();
//		gl.glBegin (GL2.GL_QUADS);
//		//Bottom part of background:
////		gl.glColor3f(0.9f, 1.f, 1f); //These commented lines will make it a gradient background. Currently it's white.
//		gl.glColor3f(1f, 1f, 1f);
//		gl.glVertex3f (-1, -1, 0.99f);
//		gl.glVertex3f (1, -1, 0.99f);
//		//Top of background.
////		gl.glColor3f(0.1f, 0.1f, 0.6f);
//		gl.glColor3f(1f, 1f, 01f);
//		gl.glVertex3f (1, 1, 0.99f);
//		gl.glVertex3f (-1, 1, 0.99f);
//		gl.glEnd ();
//		gl.glPopMatrix();
//		gl.glMatrixMode(GL2.GL_MODELVIEW);
//		gl.glPopMatrix ();
		

		
//		for (TreeHandle th: trees){
		Iterator<TreeHandle> failsafeiterator = trees.iterator(); // Using CopyOnWriteArrayList eliminates concurrent access problems.
		while(failsafeiterator.hasNext()){
			TreeHandle th = failsafeiterator.next();

			//If display of this particular tree is not on, then continue to the next.
			if(!th.displayOn){
				continue;
			}
			
			LineHolder Lines = th.getLines(); // Grab each lineholder corresponding to each tree one at a time.
			
		//This is the GL version of the line drawing.
			float ptSize = 10*1/glScaling; //Let the points be smaller/bigger depending on zoom, but make sure to cap out the size!
			ptSize = Math.min(ptSize, 10);
			
			if(slave){


				//Display text on the tree for value/score stuff if s and meta-s are pressed.
				if (scoreDisplay){
					float minscale = 0;// this is so we have a separate colorscaling for the subtrees.
					float maxscale = 0;
					boolean firstflag = true;
					//loop through first to establish upper and lower limits
					for(int i = 0; i<Lines.NodeList.length; i++){
						if (Lines.NodeList[i][1].DeadEnd){
							if(firstflag){ //first time we find an end node, we want to record these as the new scaling limits.
								minscale = Lines.NodeList[i][1].rawScore;
								maxscale = Lines.NodeList[i][1].rawScore;
								firstflag = false;
							}else
								if(Lines.NodeList[i][1].rawScore > maxscale){
									maxscale = Lines.NodeList[i][1].rawScore;
								}else if(Lines.NodeList[i][1].rawScore < minscale){
									minscale = Lines.NodeList[i][1].rawScore;
								}

						}
					}
					//now loop through to actually set the colors and text
					for(int i = 0; i<Lines.NodeList.length; i++){
						if (Lines.NodeList[i][1].DeadEnd){
							gl.glColor3fv(getScoreColor(maxscale,minscale,Lines.NodeList[i][1].rawScore).getColorComponents(null),0);

							drawString(df.format(Lines.NodeList[i][1].rawScore), oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0],oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1],0,gl,glut);

						}
					}

				}else if (valDisplay){
					for(int i = 0; i<Lines.NodeList.length; i++){
						if(Lines.NodeList[i][1].value != 0){
							
							//TODO: FIX SCALING OF COLORS WITHIN THIS SUBTREE!!! (not as relevant for value display).

	 						gl.glColor3fv(getScoreColor(minValScaling,maxValScaling,Lines.NodeList[i][1].value).getColorComponents(null),0);

		      				drawString(df.format(Lines.NodeList[i][1].value),  oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1],0,gl,glut);
						}
					}
						
				}
				
				//Display the focused point in red.
				gl.glPointSize(5*ptSize);
				gl.glBegin(GL2.GL_POINTS);
	    		  if(focusedNode != null){ //Color the focusedNode red.
	    			  gl.glColor3f(1, 0, 0);
					  gl.glVertex3d(oldToGLScaling*focusedNode.nodeLocation2[0], oldToGLScaling*focusedNode.nodeLocation2[1], 0);		  	  
	    		  }
	    		  gl.glEnd();
				
				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glPointSize(ptSize);

				gl.glBegin(GL2.GL_POINTS);
					//GL version of the end dot drawing:

					for(int i = 0; i<Lines.NodeList.length; i++){
						if (!Lines.NodeList[i][1].TempFullyExplored && Lines.NodeList[i][1].DeadEnd && OptionsHolder.failTypeDisp ){
							if (Lines.NodeList[i][1].FailType == StateHolder.FailMode.BACK){ // Failures -- we fell backwards
								gl.glColor3f(0, 1, 1);
								gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0],oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1],0);
							}else if(Lines.NodeList[i][1].FailType == StateHolder.FailMode.FRONT){ // Failures -- we fell forward.
								gl.glColor3f(1, 0, 1);
								gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1],0);

							}
						}
					}
					gl.glEnd();	
				
				gl.glLineWidth(1f);
				gl.glBegin(GL2.GL_LINES);
				//color information is stored in each vertex.
				gl.glColor3f(1.f,0.f,0.f);
				
				for (int i = 0; i<Lines.numLines; i++){
		      		if(Lines.NodeList[i][1].nodeLocation2[0] == 0 && Lines.NodeList[i][1].nodeLocation2[1] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
		      			break;
		      		}
		      		if(!Lines.ColorList[i].equals(Color.BLACK)){
		      			gl.glColor3fv(Lines.ColorList[i].getColorComponents(null),0);
					}else{

						gl.glColor3fv(getDepthColor(Lines.NodeList[i][1].TreeDepth).getRGBColorComponents(null),0);
					}
		      		gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][0].nodeLocation2[0], oldToGLScaling*Lines.NodeList[i][0].nodeLocation2[1], 0);
		      		gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1], 0);
				}
			}else{ //NOT the slave panel.
					//Display text on the tree for value/score stuff if s and meta-s are pressed.
					if (scoreDisplay){
						for(int i = 0; i<Lines.NodeList.length; i++){
							if (Lines.NodeList[i][1].DeadEnd){
								gl.glColor3fv(getScoreColor(minDistScaling,maxDistScaling,-Lines.NodeList[i][1].rawScore).getColorComponents(null),0);

								drawString(df.format(Lines.NodeList[i][1].rawScore), oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],oldToGLScaling*Lines.NodeList[i][1].height,gl,glut);
							}
						}
					}else if (valDisplay){
						for(int i = 0; i<Lines.NodeList.length; i++){
							if(Lines.NodeList[i][1].value != 0){
								gl.glColor3fv(getScoreColor(minValScaling,maxValScaling,Lines.NodeList[i][1].value).getColorComponents(null),0);
								drawString(df.format(Lines.NodeList[i][1].value),  oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],oldToGLScaling*Lines.NodeList[i][1].height,gl,glut);
							}
						}

					}

					//Display the focused point in red.
					gl.glPointSize(5*ptSize);
					gl.glBegin(GL2.GL_POINTS);
					if(focusedNode != null){ //Color the focusedNode red.
						gl.glColor3f(1, 0, 0);
						gl.glVertex3d(oldToGLScaling*focusedNode.nodeLocation[0], oldToGLScaling*focusedNode.nodeLocation[1],  oldToGLScaling*focusedNode.height);		  	  
					}
					gl.glEnd();


					if(th.focus){
						gl.glEnable(GL2.GL_POINT_SMOOTH);
						gl.glPointSize(ptSize);

						gl.glBegin(GL2.GL_POINTS);
						//GL version of the end dot drawing:

						for(int i = 0; i<Lines.NodeList.length; i++){


							if (!Lines.NodeList[i][1].TempFullyExplored && Lines.NodeList[i][1].DeadEnd && OptionsHolder.failTypeDisp ){
								if (Lines.NodeList[i][1].FailType == StateHolder.FailMode.BACK){ // Failures -- we fell backwards
									gl.glColor3f(0, 1, 1);
									gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],  oldToGLScaling*Lines.NodeList[i][1].height);
								}else if(Lines.NodeList[i][1].FailType == StateHolder.FailMode.FRONT){ // Failures -- we fell forward.
									gl.glColor3f(1, 0, 1);
									gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],  oldToGLScaling*Lines.NodeList[i][1].height);

								}
							}
							if(Lines.NodeList[i][1].TempFullyExplored && OptionsHolder.failTypeDisp && Lines.NodeList[i][1].NumChildren()==0){ //These are nodes that we've stopped at due to a depth limit, but COULD go further (not failures).
								gl.glColor3f(0.2f, 0.2f, 0.2f);
								gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],  oldToGLScaling*Lines.NodeList[i][1].height);	 

							}
						}
						gl.glEnd();	
					}
					
					if(th.focus){ //Focused trees get thicker lines.
						gl.glLineWidth(3f);
					}else{
						gl.glLineWidth(1f);
					}
					gl.glBegin(GL2.GL_LINES);
					//color information is stored in each vertex.
					gl.glColor3f(1.f,0.f,0.f);
					for (int i = 0; i<Lines.numLines; i++){
						if(Lines.NodeList[i][1].nodeLocation[0] == 0 && Lines.NodeList[i][1].nodeLocation[1] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
							break;
						}
						if(!th.focus){ //if this tree does not have focus, grey it.
							float[] col = getDepthColor(Lines.NodeList[i][1].TreeDepth).getRGBColorComponents(null);
							gl.glColor4f(col[0], col[1], col[2], 0.5f);
						}else if(!Lines.ColorList[i].equals(Color.BLACK)){
							gl.glColor3fv(Lines.ColorList[i].getColorComponents(null),0);
						}else{						
							gl.glColor3fv(getDepthColor(Lines.NodeList[i][1].TreeDepth).getRGBColorComponents(null),0);
						}
						gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][0].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][0].nodeLocation[1], oldToGLScaling*Lines.NodeList[i][0].height);
						gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1], oldToGLScaling*Lines.NodeList[i][1].height);
					}
			}
			gl.glEnd();
		}

		if(!slave){ //for a slave panel, we don't want this extra info.
			//Draw games played and games/sec in upper left.
			textRenderBig.beginRendering(width, height); 
			if(OptionsHolder.darkTheme){
				textRenderBig.setColor(0.7f, 0.7f, 0.7f, 1.0f); 
			}else{
				textRenderBig.setColor(0f, 0f, 0f, 1.0f); 
			}


			textRenderBig.draw(OptionsHolder.gamesPlayed + " Games played", 20, height-50);

			//Draw games/s
			if(countLastReport>reportEvery){
				countLastReport = 0; //Reset the counter.
				currTime = System.currentTimeMillis();
				gamespersec = (int)((OptionsHolder.gamesPlayed-lastGameNum)*1000./(currTime-lastTime));
				textRenderBig.draw(gamespersec + "  games/s", 20, height-85);
				lastGameNum = OptionsHolder.gamesPlayed;
				lastTime = currTime;
			}else{
				textRenderBig.draw(gamespersec + "  games/s", 20, height-85);
				countLastReport++;
			}
			textRenderBig.endRendering();

			
			if(OptionsHolder.darkTheme){
				textRenderSmall.setColor(0.8f, 0.8f, 0.8f, 0.7f); 
			}else{
				textRenderSmall.setColor(0f, 0f, 0f, 0.3f); 
			}
			//Also draw instructions.
			textRenderSmall.beginRendering(width, height);
			
			for (int i = 0; i<instructions.length; i++){		
				textRenderSmall.draw(instructions[instructions.length-1-i], 20, i*20+15);
			}
			textRenderSmall.endRendering();


		}

	}
	
	@Override
	public void dispose(GLAutoDrawable e) {}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
//		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.setSwapInterval(1);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glLineWidth(1);

		gl.glEnable(GL2.GL_NORMALIZE);
		
		cam.initLighting(gl);
	    
		//Line smoothing -- get rid of the pixelated look.
		gl.glEnable( GL2.GL_LINE_SMOOTH );
		gl.glEnable( GL2.GL_POLYGON_SMOOTH );
		gl.glHint( GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST );
		gl.glHint( GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST );
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
	  
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){

		this.width  = width;
		this.height = height;	
	 	GL2 gl = drawable.getGL().getGL2();
	 	cam.setDims(gl, width, height);
		
	}
   }

	@Override
	public void setInterval(int interval) {
		this.interval = interval;
		
	}

	@Override
	public int getInterval() {
		return interval;
	}

	@Override
	public void DoScheduled() {
		ScaleDist();
		ScaleVal();	
	}

	@Override
	public void DoEvery() {
		if(activeTab){
			update();
		}
	}

	@Override
	public void DoNow() {}

	@Override
	public void Disable() {}

	@Override
	public void ActivateTab() {
		activeTab = true;	
	}

	@Override
	public void DeactivateTab() {
		activeTab = false;
	}
}
