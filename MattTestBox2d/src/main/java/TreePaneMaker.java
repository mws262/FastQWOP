
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
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
		"P pauses the game's graphics (N/A for GL).",
		"Meta-arrows rotates camera (N/A for JPanel)."
	};
	
	
	
	public static int TreeDepthOffset = 0; //Do we skip an of the original depth of the tree?
	
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

	//When creating a new visualizer, wee need to know the root node so we can run down the tree and draw it.
	public TreePaneMaker(TrialNode root, boolean slave, boolean useGL) {
		
        this.useGL = useGL;
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

		 Lines = root.GetNodeLines(); //Fill up the LineHolder with everything below the specified root.
		 if (!Lines.equals(null)){ //Make sure it's not null before trying to draw
			TreePanel.setTree(Lines); //pass the lineholder to the actual panel
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
      private LineHolder Lines;
  	  
      /** Reference to the runner's single run animator. Needed for passing new focused nodes around **/
	  private SinglePathViewer pathView;
	  
	  /** Flag to know whether the mouse is currently pressed. **/
  	  private boolean mouseTrack = false;
  	  
  	  /** Format numbers to truncate decimal places when displaying distances travelled **/
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
	  	
	  	
		//ONLY APPLIES TO GL. Camera Settings. Initially camera is pointed -z, y is up. x is left?
	  	/** Scaling of node coordinates to GL coordinates. **/
		public float oldToGLScaling = 0.01f;
		
		/** Scaling of mouse movements to GL coordinates. TODO: Probably should just calculate this from view angle **/
		public float glScaling = 3.5f;

		/** Vector from camera position to target position **/
	  	private Vector3f eyeToTarget = new Vector3f();
	  	
	    /** Position of the camera. */
	    public Vector3f eyePos = new Vector3f(13, 5, 50);
	    
	    /** Position of the camera's focus. */
	    public Vector3f targetPos = new Vector3f(13, 5, 0);
	    
	    /** Define world coordinate's up. */
	    public Vector3f upVec = new Vector3f(0, 1f, 0);
	    
	    /** View frustrum angle. */
	    public float viewAng = 40;
	    /** View frustrum near plane distance. */
	    public float nearPlane = 5;
	    /** View frustrum far plane distance. */
	    public float farPlane = 10000;
	    
	    /** Camera rotation. Updated every display cycle. **/
	    public float[] modelViewMat = new float[16];
	    
	    // For doing raycast point selection:
		private TrialNode chosenPt; // selected point
		private Vector3f clickVec = new Vector3f(0,0,0); // Vector ray of the mouse click.
		private Vector3f EyeToPoint = new Vector3f(0,0,0); // vector from camera to a selected point.
		
		/** For rendering text overlays. Note that textrenderer is for overlays while GLUT is for labels in world space **/
		TextRenderer textRenderBig;
		TextRenderer textRenderSmall;
		
	  /** Constructor. Set up as either GL or not, Slave or not. **/
  	  public TreePane(boolean slave){
  	  	  this.slave = slave;

  	  	  if(useGL){
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
	   	  	  if(slave){
	   	  		  eyePos.x -= 10;
	   	  		  targetPos.x -= 10;
	   	  	  }
  	  	  }else{
  	  		  //Add listeners to the frame itself if we aren't using openGL
  	  	  	  addKeyListener(this);
  	  	  	  addMouseListener(this);
  	  	  	  addMouseMotionListener(this);
  	  	  	  addMouseWheelListener(this);
  	  	  	  
  	  	  }
	        
  	  }
  	  
  	  /** If this is the master tree frame, pass it a slave panel **/
  	  private void giveSlave(TreePaneMaker slaveMaker){
  		  this.slaveMaker = slaveMaker;
  	  }
  	  
      public void paintComponent(Graphics g){

    	  if(useGL && !pauseDraw && canvas != null){   
    		  canvas.display();
    	  }else{
    	  if(!pauseDraw){ //Temporarily stop drawing for speed.
    	   Graphics2D g2 = (Graphics2D) g; //Casting to a graphics 2d object for more control.
    	   g2.setStroke(new BasicStroke(0.5f));
    	   
    	  //Go through and draw all the lines defined.
    	  if (startDrawing){ //Make sure this exists.

    		  g.setColor(Color.WHITE);
    		  g.fillRect(0, 0, OptionsHolder.windowWidth,OptionsHolder.windowHeight);
    		  
    		  
    		  //Version for the NON-slave panel. This one uses the nodePosition positions.
    		  if(!slave){
	    		  if(focusedNode != null){ //Color the focusedNode red.
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
    	  
    	  float H = (float)(scaledDepth* 0.38)+0.35f;
    	  float S = 1f; // Saturation
    	  float B = 0.85f; // Brightness

          return Color.getHSBColor(H, S, B);
      }
      
//      
//      Vector3f debugpt1 = new Vector3f();
//      Vector3f debugpt2 = new Vector3f();
      
      /** Find a vector which represents the click ray in 3D space. Mostly stolen from my cloth simulator. **/
	    private Vector3f clickVector(int mouseX, int mouseY){
	    	//Find the vector of the clicked ray in world coordinates.
	    	
	    	//Frame height in world dimensions (not pixels)
	    	float frameHeight;
	    	float frameWidth;
	    	
	    	//Click position in world dimensions
	    	float xClick;
	    	float yClick;
	    	
	    	//Vector of click direction.
	    	Vector3f ClickVec;
	    	
	    	//Vector of eye position to target position.
	    	Vector3f CamVec = new Vector3f(0,0,0);
	    	
	    	//Camera locally defined to face in y-direction:
	    	Vector3f LocalCamLookat = new Vector3f(0,1,0);
	    	
	    	Vector3f LocalCamUp = new Vector3f(1,0,0);
	    	
	    	// Axis and angle of rotation from world coords to camera coords.
	    	Vector3f RotAxis = new Vector3f(0,0,0);
	    	float TransAngle;
	    	
	    	//Rotation from world coordinates to camera coordinates in both axis angle and matrix forms.
	    	AxisAngle4f CamToGlobalRot = new AxisAngle4f(0,0,0,0);
	    	Matrix3f RotMatrix = new Matrix3f(0,0,0,0,0,0,0,0,0);
	    	
	    	
	    	//Frame height and width in world dimensions.
	    	frameHeight = (float)(2*Math.tan(viewAng/180.0*Math.PI/2.0));
	    	frameWidth = frameHeight*width/height;
	    	// Position in world dimensions of click on front viewing plane. Center is defined as zero.
	    	xClick = frameWidth*(mouseX-width/2)/width;
	    	yClick = frameHeight*(mouseY-height/2)/height;

	    	// Vector of click in camera coordinates.
	    	ClickVec = new Vector3f(-yClick,1,-xClick);
	    	ClickVec.normalize();

	    	// Camera facing origin in world coordinates.
	    	CamVec.sub(targetPos, eyePos);
	    	CamVec.normalize();
	    
	    	
	    	//Find transformation -- world frame <-> cam frame
	    	// Two step process. First I align the camera facing vector direction.
	    	// Second, I align the "up" vector.
	    	
	    	//1st rotation
	    	RotAxis.cross(LocalCamLookat, CamVec);
	    	RotAxis.normalize();
	    	TransAngle = (float)Math.acos(LocalCamLookat.dot(CamVec));
	    	CamToGlobalRot.set(RotAxis, TransAngle);
	    	RotMatrix.set(CamToGlobalRot);
	    	
	    	//2nd rotation
	    	RotMatrix.transform(LocalCamUp);
	    	RotMatrix.transform(ClickVec);

	    	RotAxis.cross(LocalCamUp, upVec);
	    	RotAxis.normalize();
	    	TransAngle = (float) Math.acos(LocalCamUp.dot(upVec));
	    	CamToGlobalRot.set(RotAxis, TransAngle);
	    	RotMatrix.set(CamToGlobalRot);    	

	    	//Transform the click vector to world coordinates
	    	RotMatrix.transform(ClickVec);
	    	
//	    	debugpt1 = (Vector3f) eyePos.clone();
//	    	
//	    	debugpt2 = (Vector3f) ClickVec.clone();
//	    	float dist = Math.abs(debugpt1.z/ClickVec.z);
//
//	    	debugpt2.scale(dist);
//	    	debugpt2.add(debugpt1);

	    	return ClickVec;
	    	
	    }
	    
	    /** Take a click vector, find the nearest node to this line. **/
	    private TrialNode nodeFromRay(Vector3f ClickVec,LineHolder lineList,boolean altFlag){ //Alt flag says whether to use Node location 2 or 1.
	    	// Determine which point is closest to the clicked ray.

	    	double tanDist;
	    	double normDistSq;
//	    	TrialNode chosenPt;
	    	
	    	double SmallestDist = 1000;
	    	for (int i = 0; i<lineList.NodeList.length; i++){
	    		//Vector from eye to a vertex.
	    		Vector3f nodePos = new Vector3f();
	    		if(altFlag){
			    	nodePos = new Vector3f(oldToGLScaling*lineList.NodeList[i][1].nodeLocation2[0],oldToGLScaling*lineList.NodeList[i][1].nodeLocation2[1],0); //FIX THIS LATER
	    		}else{
			    	nodePos = new Vector3f(oldToGLScaling*lineList.NodeList[i][1].nodeLocation[0],oldToGLScaling*lineList.NodeList[i][1].nodeLocation[1],0); //FIX THIS LATE
	    		}

	    		EyeToPoint.sub(nodePos,eyePos);
	    		
	    		tanDist = EyeToPoint.dot(ClickVec);
	    		normDistSq = EyeToPoint.lengthSquared() - tanDist*tanDist;
	    		
	    		if (normDistSq < SmallestDist){
	    			SmallestDist = normDistSq;
	    			chosenPt = lineList.NodeList[i][1];
	    		}
	    	}
	    	return chosenPt;
	    }
	    
	    /** Take a click vector, find the coordinates of the projected point at a given level. **/ //Note: assumes trees always stay perpendicular to the z-axis.
	    private Vector3f planePtFromRay(Vector3f ClickVec, float levelset){ //Alt flag says whether to use Node location 2 or 1.
	    	// Determine which point is closest to the clicked ray.

	    	Vector3f clickvec = (Vector3f)ClickVec.clone(); //Make a copy so scaling doesn't do weird things further up.
	    	
	    	float multiplier = (levelset-eyePos.z)/clickvec.z; // How many clickvecs does it take to reach the plane?
	    	
	    	clickvec.scale(multiplier); //scale so it reaches from eye to clicked point
	    	
	    	clickvec.add(eyePos); // Add the eye position so we get the actual clicked point.
	    	clickvec.scale(1/oldToGLScaling); //Scale back to coordinates on the TrialNodes
	    	return clickvec;
	    }
	    

		/**User interaction to rotate the camera longitudinally. Magnitude of rotation is in radians and may be negative.**/
		public void rotateLongitude(float magnitude){
			
			//Vector from target to eye:
			Vector3f distVec = new Vector3f();
			distVec.sub(eyePos,targetPos);
			
			//Axis to the left in the camera world. Magnitude provided by user.
			AxisAngle4f rotation = new AxisAngle4f(modelViewMat[0],modelViewMat[4],modelViewMat[8],magnitude);
			//TODO fix singularity.
			Matrix3f rotMat = new Matrix3f();
			rotMat.set(rotation);
			
			//Transform the target to eye vector
			rotMat.transform(distVec);
			
			//Add back to get absolute position and set this to be the eye position of the camera.
			eyePos.add(targetPos,distVec);

		}
		/**User interaction to rotate the camera latitude-ishly. Magnitude of rotation is in radians and may be negative.**/
		public void rotateLatitude(float magnitude){
			
			//Vector from target to eye:
			Vector3f distVec = new Vector3f();
			distVec.sub(eyePos,targetPos);
			
			//Axis to the left in the camera world. Magnitude provided by user.
			AxisAngle4f rotation = new AxisAngle4f(modelViewMat[1],modelViewMat[5],modelViewMat[9],magnitude);

			Matrix3f rotMat = new Matrix3f();
			rotMat.set(rotation);

			//Transform the target to eye vector
			rotMat.transform(distVec);
			
			//Add back to get absolute position and set this to be the eye position of the camera.
			eyePos.add(targetPos,distVec);
		}

	    
	@Override
	public void mouseClicked(MouseEvent arg0) {	
		
		if(slave){// If slave, search among the alternate node locations in the subview panel.
			if(useGL){
				clickVec = clickVector(arg0.getX(), arg0.getY());
				focusedNode = nodeFromRay(clickVec,Lines,true);
			}else{
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}
		}else{
			
			if(useGL){
				clickVec = clickVector(arg0.getX(), arg0.getY());
				focusedNode = nodeFromRay(clickVec,Lines,false);
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			}
			if(slaveMaker != null){ //If we have a slave panel, then change its focus to our focus too
				slaveMaker.TreePanel.focusedNode = focusedNode;
			}
			
			
		}
		
		if (arg0.isAltDown()){ //alt click enables a label on this node
			focusedNode.LabelOn = true;
		}else if (arg0.isControlDown()){ //Control will hide this node and all its children.

			if(focusedNode !=null && focusedNode.TreeDepth > 1){ //Keeps stupid me from hiding everything in one click.
				focusedNode.hiddenNode = true;
				focusedNode.ParentNode.RemoveChild(focusedNode); //Try also just killing it from the tree search too.
			}
		}else if (arg0.isMetaDown()){

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
			if(useGL){
				clickVec = clickVector(arg0.getX(), arg0.getY());
				focusedNode = nodeFromRay(clickVec,Lines,true);
			}else{
				focusedNode = Lines.GetNearestNodeAlt(arg0.getX(), arg0.getY());
			}
		}else{
			if(useGL){
				clickVec = clickVector(arg0.getX(), arg0.getY());
				focusedNode = nodeFromRay(clickVec,Lines,false);
			}else{
				focusedNode = Lines.GetNearestNode(arg0.getX(), arg0.getY());
			}
		}
	}
	
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		mouseTrack = false;
	}
	//TODO: change temp vars to something better.
	@Override
	public void mouseDragged(MouseEvent arg0){
		//Note: if using old graphics, actually move the point coordinates. if GL, then just move the camera.
		if (arg0.getButton() == MouseEvent.BUTTON1){ //Left click drags the whole thing.
			
			if(useGL){
				//Find x transformed from front plane coordinates (click) to world camera coordinates
				eyeToTarget.sub(targetPos,eyePos);
				Vector3f temp1 = new Vector3f();
				temp1 = (Vector3f) upVec.clone();
				temp1.scale((arg0.getY()-mouseY)*oldToGLScaling*glScaling);

				Vector3f temp2 = new Vector3f();
				
				//Find y transformed
				temp2.cross(upVec,eyeToTarget);
				temp2.normalize();
				temp2.scale((arg0.getX() - mouseX)*oldToGLScaling*glScaling);

				
				temp1.add(temp2);
				
				eyePos.x += temp1.x;
				eyePos.y += temp1.y;
				eyePos.z += temp1.z;

				targetPos.x += temp1.x;
				targetPos.y += temp1.y;
				targetPos.z += temp1.z;

//				eyePos.x -= (arg0.getX()-mouseX)*oldToGLScaling*glScaling;
//				eyePos.y += (arg0.getY() - mouseY)*oldToGLScaling*glScaling; //Old graphics had y= 0 at the top, this doesn't now.
//				targetPos.x -= (arg0.getX()-mouseX)*oldToGLScaling*glScaling;
//				targetPos.y += (arg0.getY() - mouseY)*oldToGLScaling*glScaling;
				
				
			}else{
				if(slave){
					root.ShiftNodesAlt(arg0.getX()-mouseX, arg0.getY()-mouseY);
	
				}else{
					root.ShiftNodes(arg0.getX()-mouseX, arg0.getY()-mouseY);
				}
			}
			//TODO:
		}else if (arg0.getButton() == MouseEvent.BUTTON3){ //Right click moves nodes.
			
			if(useGL){
				clickVec = clickVector(arg0.getX(), arg0.getY());
				Vector3f clickedpt = planePtFromRay(clickVec,0);
				double clickAngle = -Math.atan2((clickedpt.x-focusedNode.ParentNode.nodeLocation[0]),(clickedpt.y-focusedNode.ParentNode.nodeLocation[1]))+Math.PI/2.;
				clickAngle -= focusedNode.nodeAngle; //Subtract out the current angle.
				focusedNode.RotateBranch(clickAngle);
			}
			
//			//Calculate the angle between the click and the parent of the click node.
//			if(slave){
//				
//				double clickAngle = -Math.atan2((arg0.getX()-focusedNode.ParentNode.nodeLocation2[0]),(arg0.getY()-focusedNode.ParentNode.nodeLocation2[1]))+Math.PI/2.;
//				clickAngle -= focusedNode.nodeAngle2; //Subtract out the current angle.
//				
//				focusedNode.RotateBranchAlt(clickAngle);
//			}else{
//				
//				double clickAngle = -Math.atan2((arg0.getX()-focusedNode.ParentNode.nodeLocation[0]),(arg0.getY()-focusedNode.ParentNode.nodeLocation[1]))+Math.PI/2.;
//				clickAngle -= focusedNode.nodeAngle; //Subtract out the current angle.
//				
//				focusedNode.RotateBranch(clickAngle);
//			}	
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
			if(useGL){
				 eyeToTarget.sub(targetPos, eyePos); //Find vector from the camera eye to the target pos
//				 if(eyeToTarget.dot(eyeToTarget) != 0){ //catch div by 0 in the normalize.
//					 eyeToTarget.normalize();
//				 }else{
//					 eyeToTarget.set(0,0,1);
//				 }
				 glScaling*=0.9;
				 eyeToTarget.scale(-0.9f);
				 eyePos.add(eyeToTarget, targetPos);
			}else{
				SizeChanger*=1.1;
			}
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
			if(useGL){
				eyeToTarget.sub(targetPos, eyePos); //Find vector from the camera eye to the target pos

				 eyeToTarget.scale(-1.1f);
				 glScaling*=1.1;
				 eyePos.add(eyeToTarget, targetPos);
			}else{
				SizeChanger*=0.9;
			}
		}
	}
		
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
	public void keyPressed(KeyEvent arg0) {
		
		//Navigating the focused node tree
		   int keyCode = arg0.getKeyCode();

		    if(arg0.isMetaDown() && useGL){ //if we're using GL, then we'll move the camera with mac key + arrows
	        	eyeToTarget.sub(targetPos,eyePos); //if we're rotating the camera, we'll need to update the eye to target vector.
		    	switch( keyCode ) { 
		        case KeyEvent.VK_UP: //Go out the branches of the tree
		        	rotateLongitude(0.1f);
		            break;
		        case KeyEvent.VK_DOWN: //Go back towards root one level
		        	rotateLongitude(-0.1f);
		            break;
		        case KeyEvent.VK_LEFT: //Go left along an isobranch (like that word?)
		        	rotateLatitude(0.1f);
		            break;
		        case KeyEvent.VK_RIGHT : //Go right along an isobranch
		        	rotateLatitude(-0.1f);
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
			     }
		    	
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
    
    /** Draw a text string using GLUT (for openGL rendering version of my stuff) **/
	  public void drawString(String toDraw, float x, float y, GL2 gl, GLUT glut ) 
      {
        // Fomat numbers with Java.
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
       
        // Printing fonts, letters and numbers is much simpler with GLUT.
        // We do not have to use our own bitmap for the font.
//        gl.glColor3f(0, 0, 0);
        gl.glRasterPos2d(x,y);
        glut.glutBitmapString(
          GLUT.BITMAP_HELVETICA_12, 
          toDraw);
      }

    /** 
     * Main event loop: OpenGL display
     * advance. GLEventListener implementation.
     */
	@Override
	public void display(GLAutoDrawable drawable) {
		eyeToTarget.sub(targetPos,eyePos);
		upVec.cross(eyeToTarget,upVec);
		upVec.cross(upVec, eyeToTarget);
		upVec.normalize();
		
		

		GL2 gl = drawable.getGL().getGL2();
		GLUT glut = new GLUT();

		//Background color that defaults when canvas is cleared.
		gl.glClearColor(0.1f,0.1f,0.2f,1f);
		//clear it out.
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		//Line smoothing -- get rid of the pixelated look.
		gl.glEnable( GL2.GL_LINE_SMOOTH );
		gl.glEnable( GL2.GL_POLYGON_SMOOTH );
		gl.glHint( GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST );
		gl.glHint( GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST );
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		

		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		if (glu == null) glu = GLU.createGLU();
		
		//Camera perspective.
		gl.glLoadIdentity();
		glu.gluPerspective(viewAng, (float)width/height, nearPlane, farPlane);
		glu.gluLookAt(eyePos.x, eyePos.y, eyePos.z, targetPos.x, targetPos.y, targetPos.z, upVec.x, upVec.y, upVec.z);
		gl.glPopMatrix();
		gl.glGetFloatv(GL2.GL_MODELVIEW, modelViewMat,0);
		
	
		//Blue sky background:
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glBegin (GL2.GL_QUADS);
		//Bottom part of background:
//		gl.glColor3f(0.9f, 1.f, 1f); //These commented lines will make it a gradient background. Currently it's white.
		gl.glColor3f(1f, 1f, 1f);
		gl.glVertex3f (-1, -1, 0.99f);
		gl.glVertex3f (1, -1, 0.99f);
		//Top of background.
//		gl.glColor3f(0.1f, 0.1f, 0.6f);
		gl.glColor3f(1f, 1f, 01f);
		gl.glVertex3f (1, 1, 0.99f);
		gl.glVertex3f (-1, 1, 0.99f);
		gl.glEnd ();
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix ();
		if(Lines != null){	

		//This is the GL version of the line drawing.

			if(slave){

				
				//Display text on the tree for value/score stuff if s and meta-s are pressed. TODO: figure out text size scaling in GLUT
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

							drawString(df.format(Lines.NodeList[i][1].rawScore), Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1],gl,glut);
						
							
							//TODO: FIX SCALING OF COLORS WITHIN THIS SUBTREE!!! (not all that important)
							}
					}

				}else if (valDisplay){
					for(int i = 0; i<Lines.NodeList.length; i++){
						if(Lines.NodeList[i][1].value != 0){
							
							//TODO: FIX SCALING OF COLORS WITHIN THIS SUBTREE!!! (not as relevant for value display).

//	     					g2.setColor(getScoreColor(minValScaling, maxValScaling, Lines.NodeList[i][1].value));
	 						gl.glColor3fv(getScoreColor(minValScaling,maxValScaling,Lines.NodeList[i][1].value).getColorComponents(null),0);

//		     				g2.setFont(scaleFont.InterpolateFont(maxValScaling,minValScaling,Lines.NodeList[i][1].value*OptionsHolder.sizeFactor));
		      				drawString(df.format(Lines.NodeList[i][1].value),  Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1],gl,glut);
						}
					}
						
				}
				
				//Display the focused point in red.
				gl.glPointSize(50*1/Constants.glScaling);
				gl.glBegin(GL2.GL_POINTS);
	    		  if(focusedNode != null){ //Color the focusedNode red.
	    			  gl.glColor3f(1, 0, 0);
					  gl.glVertex3d(Constants.oldToGLScaling*focusedNode.nodeLocation2[0], Constants.oldToGLScaling*focusedNode.nodeLocation2[1], 0);		  	  
	    		  }
	    		  gl.glEnd();
				
				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glPointSize(10*1/glScaling);

				gl.glBegin(GL2.GL_POINTS);
					//GL version of the end dot drawing:

					for(int i = 0; i<Lines.NodeList.length; i++){
						if (!Lines.NodeList[i][1].TempFullyExplored && Lines.NodeList[i][1].DeadEnd && OptionsHolder.failTypeDisp ){
							if (Lines.NodeList[i][1].FailType == StateHolder.FailMode.BACK){ // Failures -- we fell backwards
								gl.glColor3f(0, 1, 1);
								gl.glVertex3d(oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1], 0);
							}else if(Lines.NodeList[i][1].FailType == StateHolder.FailMode.FRONT){ // Failures -- we fell forward.
								gl.glColor3f(1, 0, 1);
								gl.glVertex3d(Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation2[1], 0);

							}
						}
					}
					gl.glEnd();	
				
				gl.glLineWidth(0.5f);
				gl.glBegin(GL2.GL_LINES);
				//color information is stored in each vertex.
				gl.glColor3f(1.f,0.f,0.f);


				
				
				for (int i = 0; i<Lines.numLines; i++){
		      		if(Lines.LineList2[i][2] == 0 && Lines.LineList2[i][3] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
		      			break;
		      		}
		      		if(!Lines.ColorList[i].equals(Color.BLACK)){
		      			gl.glColor3fv(Lines.ColorList[i].getColorComponents(null),0);
					}else{

						gl.glColor3fv(getDepthColor(Lines.NodeList[i][1].TreeDepth).getRGBColorComponents(null),0);
					}
		      		gl.glVertex3d(Constants.oldToGLScaling*Lines.LineList2[i][0], oldToGLScaling*Lines.LineList2[i][1],0);
		      		gl.glVertex3d(Constants.oldToGLScaling*Lines.LineList2[i][2], Constants.oldToGLScaling*Lines.LineList2[i][3],0);
				}
			}else{ //NOT the slave panel.
				
				{//for (int u = 0; u<25; u= u+5){
				int u = 0;
				
				//Display text on the tree for value/score stuff if s and meta-s are pressed. TODO: figure out text size scaling in GLUT
				if (scoreDisplay){
					for(int i = 0; i<Lines.NodeList.length; i++){
						if (Lines.NodeList[i][1].DeadEnd){
							gl.glColor3fv(getScoreColor(minDistScaling,maxDistScaling,-Lines.NodeList[i][1].rawScore).getColorComponents(null),0);

							drawString(df.format(Lines.NodeList[i][1].rawScore), Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],gl,glut);
						}
					}
				}else if (valDisplay){
					for(int i = 0; i<Lines.NodeList.length; i++){
						if(Lines.NodeList[i][1].value != 0){
//	     					g2.setColor(getScoreColor(minValScaling, maxValScaling, Lines.NodeList[i][1].value));
	 						gl.glColor3fv(getScoreColor(minValScaling,maxValScaling,Lines.NodeList[i][1].value).getColorComponents(null),0);

//		     				g2.setFont(scaleFont.InterpolateFont(maxValScaling,minValScaling,Lines.NodeList[i][1].value*OptionsHolder.sizeFactor));
		      				drawString(df.format(Lines.NodeList[i][1].value),  Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1],gl,glut);
						}
					}
						
				}

				//Display the focused point in red.
				gl.glPointSize(50*1/Constants.glScaling);
				gl.glBegin(GL2.GL_POINTS);
	    		  if(focusedNode != null){ //Color the focusedNode red.
	    			  gl.glColor3f(1, 0, 0);
					  gl.glVertex3d(Constants.oldToGLScaling*focusedNode.nodeLocation[0], Constants.oldToGLScaling*focusedNode.nodeLocation[1], u);		  	  
	    		  }
	    		  gl.glEnd();
	    		  

				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glPointSize(10*1/Constants.glScaling);

				gl.glBegin(GL2.GL_POINTS);
					//GL version of the end dot drawing:
				
					for(int i = 0; i<Lines.NodeList.length; i++){

						if (!Lines.NodeList[i][1].TempFullyExplored && Lines.NodeList[i][1].DeadEnd && OptionsHolder.failTypeDisp ){
							if (Lines.NodeList[i][1].FailType == StateHolder.FailMode.BACK){ // Failures -- we fell backwards
								gl.glColor3f(0, 1, 1);
								gl.glVertex3d(Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1], u);
							}else if(Lines.NodeList[i][1].FailType == StateHolder.FailMode.FRONT){ // Failures -- we fell forward.
								gl.glColor3f(1, 0, 1);
								gl.glVertex3d(Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1], u);

							}
						}
		     			if(Lines.NodeList[i][1].TempFullyExplored && OptionsHolder.failTypeDisp && Lines.NodeList[i][1].NumChildren()==0){ //These are nodes that we've stopped at due to a depth limit, but COULD go further (not failures).
		     				gl.glColor3f(0.9f, 0.9f, 0.9f);
	     					gl.glVertex3d(Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[0], Constants.oldToGLScaling*Lines.NodeList[i][1].nodeLocation[1], u);	 
					
		     			}
					}
					gl.glEnd();	
				
				gl.glLineWidth(0.5f);
				gl.glBegin(GL2.GL_LINES);
				//color information is stored in each vertex.
				gl.glColor3f(1.f,0.f,0.f);
//				gl.glVertex3f(debugpt1.x,debugpt1.y,debugpt1.z);
//				gl.glVertex3f(debugpt2.x,debugpt2.y,debugpt2.z);
			
				for (int i = 0; i<Lines.numLines; i++){
		      		if(Lines.LineList[i][2] == 0 && Lines.LineList[i][3] == 0){ //If the x2 and y2 are 0, we've come to the end of actual lines.
		      			break;
		      		}
		      		if(!Lines.ColorList[i].equals(Color.BLACK)){
		      			gl.glColor3fv(Lines.ColorList[i].getColorComponents(null),0);
					}else{						
						gl.glColor3fv(getDepthColor(Lines.NodeList[i][1].TreeDepth).getRGBColorComponents(null),0);
					}
		      		gl.glVertex3d(Constants.oldToGLScaling*Lines.LineList[i][0], Constants.oldToGLScaling*Lines.LineList[i][1],u);
		      		gl.glVertex3d(Constants.oldToGLScaling*Lines.LineList[i][2], Constants.oldToGLScaling*Lines.LineList[i][3],u);
				}

			}
			}
		gl.glEnd();
			
		if(!slave){ //for a slave panel, we don't want this extra info.
			//Draw games played and games/sec in upper left.
			  textRenderBig.beginRendering(width, height); 
			  textRenderBig.setColor(0f, 0f, 0f, 1.0f); 

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
				
				//Also draw instructions.
			  	textRenderSmall.beginRendering(width, height);
				textRenderBig.setColor(0f, 0f, 0f, 0.3f); 
			    for (int i = 0; i<instructions.length; i++){		
			    	textRenderSmall.draw(instructions[instructions.length-1-i], 20, i*20+15);
			    }
			    textRenderSmall.endRendering();
			    
				
	       }

		}
	}

	
	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		System.err.println("INIT GL IS: " + gl.getClass().getName());

		gl.setSwapInterval(1);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glLineWidth(1);

		gl.glEnable(GL2.GL_NORMALIZE);

		// SETUP LIGHTING
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, Constants.lightPos, 0);
	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, Constants.lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, Constants.lightDiffuse, 0);
	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, Constants.lightSpecular, 0);
	    gl.glEnable(GL2.GL_LIGHT0);
	  
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){
		System.out.println("width="+width+", height="+height);
		height = Math.max(height, 1); // avoid height=0;
		
		this.width  = width;
		this.height = height;
//	
	 	GL2 gl = drawable.getGL().getGL2();
		gl.glViewport(0,0,width,height);
		
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
