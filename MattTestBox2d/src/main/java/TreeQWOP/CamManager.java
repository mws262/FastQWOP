package TreeQWOP;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/** 
 * Handle all camera motion including smooth motions, click-to-coordinate mapping, etc
 * 
 * @author Matt
 *
 */
public class CamManager {

	/** Vector from camera position to target position **/
  	private Vector3f eyeToTarget = new Vector3f();
  	
    /** Position of the camera. */
    private Vector3f eyePos = new Vector3f(13, 5, 50);
    
    /** Position of the camera's focus. */
    private Vector3f targetPos = new Vector3f(13, 5, 0);
    
    /** Define world coordinate's up. */
    private Vector3f upVec = new Vector3f(0, 1f, 0);
    
    /** View frustrum angle. */
    private float viewAng = 40;
    
    /** View frustrum near plane distance. */
    private float nearPlane = 5;
    
    /** View frustrum far plane distance. */
    private float farPlane = 10000;
    
    /** Camera rotation. Updated every display cycle. **/
    private float[] modelViewMat = new float[16];
    
    /** Width of window. **/
    private float width = 0;
    
    /** Height of window. **/
    private float height = 0;
    
    /** Position of the light. Fixed at the location of the camera. */
    public static float[] lightPos = {0f, 0f, 0f, 1f};
    public static float[] lightAmbient = {0f, 0f, 0f, 1f};
    public static float[] lightDiffuse = {0.9f, 0.9f, 0.9f, 1f};
    public static float[] lightSpecular = {1f, 1f, 1f, 1f};

    /* Queued camera movements. The convention is step size and number of needed steps */
    private ArrayList<Vector3f> eyeIncrement = new ArrayList<Vector3f>();
    private ArrayList<Integer> eyeSteps = new ArrayList<Integer>();
    
    private ArrayList<Vector3f> targetIncrement = new ArrayList<Vector3f>();
    private ArrayList<Integer> targetSteps = new ArrayList<Integer>();
    
    private ArrayList<Float> longitudeIncrement = new ArrayList<Float>();
    private ArrayList<Integer> longitudeSteps = new ArrayList<Integer>();
    
    private ArrayList<Float> latitudeIncrement = new ArrayList<Float>();
    private ArrayList<Integer> latitudeSteps = new ArrayList<Integer>();
    
    private ArrayList<Float> zoomIncrement = new ArrayList<Float>();
    private ArrayList<Integer> zoomSteps = new ArrayList<Integer>();
    
    private ArrayList<Float> twistIncrement = new ArrayList<Float>();
    private ArrayList<Integer> twistSteps = new ArrayList<Integer>();
    
    //temporary var for when adding up camera movements.
    private Vector3f netMovement = new Vector3f();
    private float net = 0;
    
    // For doing raycast point selection:
	private TrialNode chosenPt; // selected point
	private Vector3f clickVec = new Vector3f(0,0,0); // Vector ray of the mouse click.
	private Vector3f EyeToPoint = new Vector3f(0,0,0); // vector from camera to a selected point.
    
	// Temporary. just don't want to keep reallocating memory.
	private Vector3f temp1 = new Vector3f();
	private Vector3f temp2 = new Vector3f();
	
    /** Provide camera's target, etc. **/
	public CamManager(float width, float height, Vector3f eyePos, Vector3f targetPos) {
		this.eyePos = eyePos;
		this.targetPos = targetPos;
		this.width = width;
		this.height = height;
	}
	
	/** Use default camera position, target, etc **/
	public CamManager(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	/** Setup Lighting **/
	public void initLighting(GL2 gl){
		// SETUP LIGHTING
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
	    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
	    gl.glEnable(GL2.GL_LIGHT0);
	}

	/** Update all camera views and bookkeeping info. Any queued camera movements will be incremented. **/
	public void update(GL2 gl, GLU glu){
		
		
		/* Do all the queued actions. **/
		
		//Sum eye movements.
		netMovement.scale(0);
		for (int i = 0; i< eyeIncrement.size(); i++){
			netMovement.add(eyeIncrement.get(i));
			eyeSteps.set(i, eyeSteps.get(i) - 1);
			
			//Get rid of this movement increment once we've done the specified number of movements.
			if(eyeSteps.get(i) == 0){
				eyeSteps.remove(i);
				eyeIncrement.remove(i);
			}
		}
		//Put the net change back into the actual position.
		eyePos.add(netMovement);
		
		//Sum target movements.
		netMovement.scale(0);
		for (int i = 0; i< targetIncrement.size(); i++){
			netMovement.add(targetIncrement.get(i));
			targetSteps.set(i, targetSteps.get(i) - 1);
			
			//Get rid of this movement increment once we've done the specified number of movements.
			if(targetSteps.get(i) == 0){
				targetSteps.remove(i);
				targetIncrement.remove(i);
			}
		}
		//Put the net change back into the actual position.
		targetPos.add(netMovement);
		
		//Sum longitude movements.
		net = 0;
		for (int i = 0; i< longitudeIncrement.size(); i++){
			net += (longitudeIncrement.get(i));
			longitudeSteps.set(i, longitudeSteps.get(i) - 1);
			
			//Get rid of this movement increment once we've done the specified number of movements.
			if(longitudeSteps.get(i) == 0){
				longitudeSteps.remove(i);
				longitudeIncrement.remove(i);
			}
		}
		
		//Put the net change back into the actual position.
		rotateLongitude(net);
		
		//Sum latitude movements.
		net = 0;
		for (int i = 0; i< latitudeIncrement.size(); i++){
			net += (latitudeIncrement.get(i));
			latitudeSteps.set(i, latitudeSteps.get(i) - 1);
			
			//Get rid of this movement increment once we've done the specified number of movements.
			if(latitudeSteps.get(i) == 0){
				latitudeSteps.remove(i);
				latitudeIncrement.remove(i);
			}
		}
		
		//Put the net change back into the actual position.
		rotateLatitude(net);
		
		//Sum twist movements.
		net = 0;
		for (int i = 0; i< twistIncrement.size(); i++){
			net += (twistIncrement.get(i));
			twistSteps.set(i, twistSteps.get(i) - 1);
			
			//Get rid of this movement increment once we've done the specified number of movements.
			if(twistSteps.get(i) == 0){
				twistSteps.remove(i);
				twistIncrement.remove(i);
			}
		}
		
		//Put the net change back into the actual position.
		twistCW(net);
		
		//Put together all zooms
		net = -1;
		for (int i = 0; i<zoomIncrement.size(); i++){
			net *= zoomIncrement.get(i);
			zoomSteps.set(i, zoomSteps.get(i) - 1);
			
			if(zoomSteps.get(i) == 0){
				zoomSteps.remove(i);
				zoomIncrement.remove(i);
			}
		}
		//Now do the zoom:
		eyeToTarget.sub(targetPos, eyePos); //Find vector from the camera eye to the target pos
		eyeToTarget.scale(net);
		eyePos.add(eyeToTarget, targetPos);
		
		
		upVec.cross(eyeToTarget,upVec);
		upVec.cross(upVec, eyeToTarget);
		upVec.normalize();
		
		//Actually change the camera settings now.
		//Camera perspective.
		gl.glLoadIdentity();
		glu.gluPerspective(viewAng, (float)width/height, nearPlane, farPlane);
		glu.gluLookAt(eyePos.x, eyePos.y, eyePos.z, targetPos.x, targetPos.y, targetPos.z, upVec.x, upVec.y, upVec.z);
		gl.glPopMatrix();
		gl.glGetFloatv(GL2.GL_MODELVIEW, modelViewMat,0);
	}
	
	/** Change window dims **/
	public void setDims(GL2 gl, int width, int height){
		height = Math.max(height, 1); // avoid height=0;
		width = Math.max(width, 1); // avoid height=0;
		this.width = width;
		this.height = height;
		
		gl.glViewport(0,0,width,height);
	}
	
	
	/** Transform a 2d coordinate click in window coordinates to a 3d coordinate in the world frame. **/
	public Vector3f windowFrameToWorldFrameDiff(int mouseXnew, int mouseYnew, int mouseXold, int mouseYold, float oldToGLScaling, float glScaling){
		//Find x transformed from front plane coordinates (click) to world camera coordinates
			eyeToTarget.sub(targetPos,eyePos);
			Vector3f temp1 = new Vector3f();
			temp1 = (Vector3f) upVec.clone();
			temp1.scale((mouseYnew-mouseYold)*oldToGLScaling*glScaling);

			Vector3f temp2 = new Vector3f();

			//Find y transformed
			temp2.cross(upVec,eyeToTarget);
			temp2.normalize();
			temp2.scale((mouseXnew - mouseXold)*oldToGLScaling*glScaling);


			temp1.add(temp2);
			
			return temp1;
	}
	/** Twist up vector by provided angle **/
	public void twistCW(float radians){
		eyeToTarget.sub(targetPos);
		Vector3f perp = new Vector3f();
		perp.set(eyeToTarget);
		
		perp.cross(perp, upVec); //perpendicular to the upvec in the plane of the camera
		
		if (perp.dot(perp)>0){
			perp.normalize();
		}else{
			return;
		}
		
		perp.scale((float) Math.sin(radians));
		upVec.scale((float) Math.cos(radians));
		
		upVec.add(perp);
	}
	/** Zoom in/zoom out. Also spread out over a certain number of update calls **/ //TODO: make sure that other updates are also scaled?
	public void smoothZoom(float zoomFactor,int speed){
		float zoominc = (float)Math.pow(zoomFactor, 1./speed);
		zoomIncrement.add(zoominc);
		zoomSteps.add(speed);
		
	}
	
	/** Rotate the camera about the axis it's aiming **/
	public void smoothTwist(float zoomFactor,int speed){
		twistIncrement.add(zoomFactor/speed);
		twistSteps.add(speed);
		
	}

	/** Orbit camera longitudinally by a magnitude in radians with a speed factor of 'speed.' 
	 * The speed means how many update calls does it take to achieve the change.**/
	public void smoothRotateLong(float magnitude, int speed){
		longitudeIncrement.add(magnitude/speed); //find the magnitude of rotation per step
		longitudeSteps.add(speed);
	}

	/** Orbit camera latitudinally by a magnitude in radians with a speed factor of 'speed'.  
	 * The speed means how many update calls does it take to achieve the change.**/
	public void smoothRotateLat(float magnitude, int speed){
		latitudeIncrement.add(magnitude/speed); //find the magnitude of rotation per step
		latitudeSteps.add(speed);
	}
	
	/** Move camera eye and target positions to absolute positions given. This will be done in speed number of update calls. **/
	public void smoothTranslateAbsolute(Vector3f campos, Vector3f tarpos, int speed){
		Vector3f eye = new Vector3f();
		eye.set(campos);
		Vector3f target = new Vector3f();
		target = (Vector3f)tarpos.clone();
		
		
		
		//Find the difference between where we are and where we want to go.
		eye.sub(eyePos);
		target.sub(targetPos);
		
		// Scale such that this will be completed in speed-number of update calls.
		eye.scale(1f/speed);
		target.scale(1f/speed);
		
		
		eyeIncrement.add(eye); //find the magnitude of rotation per step
		eyeSteps.add(speed);
		
		targetIncrement.add(target); //find the magnitude of rotation per step
		targetSteps.add(speed);
	}
	
	/** Move camera eye and target by offset amounts given. This will be done in speed number of update calls. **/
	public void smoothTranslateRelative(Vector3f campos, Vector3f tarpos, int speed){
		Vector3f eye = (Vector3f)campos.clone();
		Vector3f target = (Vector3f)tarpos.clone();
		
		// Scale such that this will be completed in speed-number of update calls.
		eye.scale(1f/speed);
		target.scale(1f/speed);
		
		eyeIncrement.add(eye); //find the magnitude of rotation per step
		eyeSteps.add(speed);
		
		targetIncrement.add(target); //find the magnitude of rotation per step
		targetSteps.add(speed);
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

	    	return ClickVec;
	    	
	    }
	    
	    /** Take a click vector, find the nearest node to this line. **/
	    public  TrialNode nodeFromRay(Vector3f ClickVec,CopyOnWriteArrayList<TreeHandle> trees, float oldToGLScaling, boolean altFlag){ //Alt flag says whether to use Node location 2 or 1.
	    	// Determine which point is closest to the clicked ray.

	    	double tanDist;
	    	double normDistSq;
	    	LineHolder lines;
	    	
	    	double SmallestDist = Double.MAX_VALUE;
	    	for(TreeHandle th: trees){ //Loop through all trees
	    		lines = th.getLines();
	    		
		    	for (int i = 0; i<lines.NodeList.length; i++){
		    		//Vector from eye to a vertex.
		    		Vector3f nodePos = new Vector3f();
		    		if(altFlag){
				    	nodePos = new Vector3f(oldToGLScaling*lines.NodeList[i][1].nodeLocation2[0],oldToGLScaling*lines.NodeList[i][1].nodeLocation2[1],0);
		    		}else{
				    	nodePos = new Vector3f(oldToGLScaling*lines.NodeList[i][1].nodeLocation[0],oldToGLScaling*lines.NodeList[i][1].nodeLocation[1],oldToGLScaling*lines.NodeList[i][1].height);
		    		}

		    		EyeToPoint.sub(nodePos,eyePos);
		    		
		    		tanDist = EyeToPoint.dot(ClickVec);
		    		normDistSq = EyeToPoint.lengthSquared() - tanDist*tanDist;
		    		
		    		if (normDistSq < SmallestDist){
		    			SmallestDist = normDistSq;
		    			chosenPt = lines.NodeList[i][1];
		    		}
		    	}
	    	}

	    	return chosenPt;
	    }
	    
	    /** Directly get a selected node given click coordinates a reference to all the trees, a conversion between tree and GL scaling, and a flag telling us whether to look at the tree's primary or secondary coordinates. **/
	    public TrialNode nodeFromClick(int mouseX, int mouseY, CopyOnWriteArrayList<TreeHandle> trees, float oldToGLScaling, boolean altFlag){
	    	clickVec = clickVector(mouseX,mouseY);
	    	return nodeFromRay(clickVec,trees, oldToGLScaling, altFlag);
	    	
	    }
	    
	    /** Take a click vector, find the coordinates of the projected point at a given level. **/ //Note: assumes trees always stay perpendicular to the z-axis.
	    public Vector3f planePtFromRay(int mouseX, int mouseY, float oldToGLScaling, float levelset){ //Alt flag says whether to use Node location 2 or 1.
	    	// Determine which point is closest to the clicked ray.

	    	clickVec = clickVector(mouseX,mouseY); //Make a copy so scaling doesn't do weird things further up.
	    	
	    	float multiplier = (levelset-eyePos.z)/clickVec.z; // How many clickvecs does it take to reach the plane?
	    	
	    	clickVec.scale(multiplier); //scale so it reaches from eye to clicked point
	    	
	    	clickVec.add(eyePos); // Add the eye position so we get the actual clicked point.
	    	clickVec.scale(1/oldToGLScaling); //Scale back to coordinates on the TrialNodes
	    	return clickVec;
	    }
}
