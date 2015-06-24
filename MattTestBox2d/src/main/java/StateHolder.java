import java.rmi.UnexpectedException;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;

/**
 * This object can hold a QWOPGame's state at a snapshot.
 * It can then compare itself to another state and return some metric of how close the two of them are.
 * 
 * 
 * @author Matt
 *
 */
public class StateHolder {
	
	//Convention will be x,y,theta
	public float[] TorsoState = new float[3];
	public static float TorsoWeight = 10;
	
	public float[] HeadState = new float[3];
	public static float HeadWeight = 1;
	
	public float[] RFootState = new float[3];
	public static float RFootWeight = 1;
	
	public float[] LFootState = new float[3];
	public static float LFootWeight = 1;
	
	public float[] RCalfState = new float[3];
	public static float RCalfWeight = 1;
	
	public float[] LCalfState = new float[3];
	public static float LCalfWeight = 1;
	
	public float[] RThighState = new float[3];
	public static float RThighWeight = 1;
	
	public float[] LThighState = new float[3];
	public static float LThighWeight = 1;
	
	public float[] RUArmState = new float[3];
	public static float RUArmWeight = 1;
	
	public float[] LUArmState = new float[3];
	public static float LUArmWeight = 1;
	
	public float[] RLArmState = new float[3];
	public static float RLArmWeight = 1;
	
	public float[] LLArmState = new float[3];
	public static float LLArmWeight = 1;
	
	//All the links in the body in one list:
	public float[][] AllLinks = {TorsoState,HeadState,RFootState,LFootState,RCalfState,LCalfState,RThighState,LThighState,RUArmState,LUArmState,RLArmState,LLArmState};
	
	//All the state weights in one list:
	public static float[] AllWeights = {TorsoWeight,HeadWeight,RFootWeight,LFootWeight,RCalfWeight,LCalfWeight,RThighWeight,LThighWeight,RUArmWeight,LUArmWeight,RLArmWeight,LLArmWeight};
	
	public static float XWeight = 0; //Weight for error values in x
	
	public static float YWeight = 1; //Weight for error values in y
	
	public static float AngleWeight = 1; //Weight for error values in angle./
	
	//We keep a reference to the game interface rather than a reference to the game itself. Why? The game is constantly being destroyed and recreated while the interface remains the same object all along.
	public final QWOPInterface gameInterface;
	
	public static Shape[] CapturedShapes = new Shape[14];
	 
	public Transform[] CapturedTransforms = new Transform[12];
	
	public FailMode failType = FailMode.UNFAILED;
	
	public FailMode potentialFailMode = FailMode.UNFAILED;
	
	public boolean fail = false;
	
	public enum FailMode{
		BACK,
		UNFAILED,
		FRONT
	}
	
	
	public StateHolder(QWOPInterface gameInterface) {
		this.gameInterface = gameInterface;
	}

	/** Grab a snapshot of the game's state and store it in this object **/
	public void CaptureState(){
		

		//Get torso state:
		TorsoState[0] = gameInterface.game.TorsoBody.getPosition().x;
		TorsoState[1] = gameInterface.game.TorsoBody.getPosition().y;
		TorsoState[2] = gameInterface.game.TorsoBody.getAngle();
		CapturedTransforms[0] = new Transform(gameInterface.game.TorsoBody.getTransform());
		
		//Get head state:
		HeadState[0] = gameInterface.game.HeadBody.getPosition().x;
		HeadState[1] = gameInterface.game.HeadBody.getPosition().y;
		HeadState[2] = gameInterface.game.HeadBody.getAngle();
		CapturedTransforms[1] = new Transform(gameInterface.game.HeadBody.getTransform());

		
		//Get right foot state:
		RFootState[0] = gameInterface.game.RFootBody.getPosition().x;
		RFootState[1] = gameInterface.game.RFootBody.getPosition().y;
		RFootState[2] = gameInterface.game.RFootBody.getAngle();
		CapturedTransforms[2] = new Transform(gameInterface.game.RFootBody.getTransform());
		
		//Get left foot state:
		LFootState[0] = gameInterface.game.LFootBody.getPosition().x;
		LFootState[1] = gameInterface.game.LFootBody.getPosition().y;
		LFootState[2] = gameInterface.game.LFootBody.getAngle();
		CapturedTransforms[3] = new Transform(gameInterface.game.LFootBody.getTransform());
		
		//Get right calf state:
		RCalfState[0] = gameInterface.game.RCalfBody.getPosition().x;
		RCalfState[1] = gameInterface.game.RCalfBody.getPosition().y;
		RCalfState[2] = gameInterface.game.RCalfBody.getAngle();
		CapturedTransforms[4] = new Transform(gameInterface.game.RCalfBody.getTransform());
		
		//Get left calf state:
		LCalfState[0] = gameInterface.game.LCalfBody.getPosition().x;
		LCalfState[1] = gameInterface.game.LCalfBody.getPosition().y;
		LCalfState[2] = gameInterface.game.LCalfBody.getAngle();
		CapturedTransforms[5] = new Transform(gameInterface.game.LCalfBody.getTransform());
		
		//Get right Thigh state:
		RThighState[0] = gameInterface.game.RThighBody.getPosition().x;
		RThighState[1] = gameInterface.game.RThighBody.getPosition().y;
		RThighState[2] = gameInterface.game.RThighBody.getAngle();
		CapturedTransforms[6] = new Transform(gameInterface.game.RThighBody.getTransform());
		
		//Get left Thigh state:
		LThighState[0] = gameInterface.game.LThighBody.getPosition().x;
		LThighState[1] = gameInterface.game.LThighBody.getPosition().y;
		LThighState[2] = gameInterface.game.LThighBody.getAngle();
		CapturedTransforms[7] = new Transform(gameInterface.game.LThighBody.getTransform());
		
		//Get right UArm state:
		RUArmState[0] = gameInterface.game.RUArmBody.getPosition().x;
		RUArmState[1] = gameInterface.game.RUArmBody.getPosition().y;
		RUArmState[2] = gameInterface.game.RUArmBody.getAngle();
		CapturedTransforms[8] = new Transform(gameInterface.game.RUArmBody.getTransform());
		
		//Get left UArm state:
		LUArmState[0] = gameInterface.game.LUArmBody.getPosition().x;
		LUArmState[1] = gameInterface.game.LUArmBody.getPosition().y;
		LUArmState[2] = gameInterface.game.LUArmBody.getAngle();
		CapturedTransforms[9] = new Transform(gameInterface.game.LUArmBody.getTransform());
		
		//Get right LArm state:
		RLArmState[0] = gameInterface.game.RLArmBody.getPosition().x;
		RLArmState[1] = gameInterface.game.RLArmBody.getPosition().y;
		RLArmState[2] = gameInterface.game.RLArmBody.getAngle();
		CapturedTransforms[10] = new Transform(gameInterface.game.RLArmBody.getTransform());
		
		//Get left LArm state:
		LLArmState[0] = gameInterface.game.LLArmBody.getPosition().x;
		LLArmState[1] = gameInterface.game.LLArmBody.getPosition().y;
		LLArmState[2] = gameInterface.game.LLArmBody.getAngle();
		CapturedTransforms[11] = new Transform(gameInterface.game.LLArmBody.getTransform());
		
		
		//My current understanding is that the shapes never change. Only the transforms. Hence, this is now static and we only capture the states once.
		if(CapturedShapes[0] == null){
			CapturedShapes[0] = (PolygonShape)gameInterface.game.TorsoBody.getFixtureList().getShape().clone();
			CapturedShapes[1] = (CircleShape)gameInterface.game.HeadBody.getFixtureList().getShape().clone();
			CapturedShapes[2] = (PolygonShape)gameInterface.game.RFootBody.getFixtureList().getShape().clone();
			CapturedShapes[3] = (PolygonShape)gameInterface.game.LFootBody.getFixtureList().getShape().clone();
			CapturedShapes[4] = (PolygonShape)gameInterface.game.RCalfBody.getFixtureList().getShape().clone();
			CapturedShapes[5] = (PolygonShape)gameInterface.game.LCalfBody.getFixtureList().getShape().clone();
			CapturedShapes[6] = (PolygonShape)gameInterface.game.RThighBody.getFixtureList().getShape().clone();
			CapturedShapes[7] = (PolygonShape)gameInterface.game.LThighBody.getFixtureList().getShape().clone();
			CapturedShapes[8] = (PolygonShape)gameInterface.game.RUArmBody.getFixtureList().getShape().clone();
			CapturedShapes[9] = (PolygonShape)gameInterface.game.LUArmBody.getFixtureList().getShape().clone();
			CapturedShapes[10] = (PolygonShape)gameInterface.game.RLArmBody.getFixtureList().getShape().clone();
			CapturedShapes[11] = (PolygonShape)gameInterface.game.LLArmBody.getFixtureList().getShape().clone();
			CapturedShapes[12] = (EdgeShape)gameInterface.game.TrackBody.getFixtureList().getShape().clone();
			CapturedShapes[13] = (EdgeShape)gameInterface.game.TrackBody.getFixtureList().getShape().clone();
		}

		potentialFailMode = CheckFailType();
	}
	
	/** Return the failure type **/
	private FailMode CheckFailType(){
		if(TorsoState[2]>-1.25){
			return FailMode.FRONT;
		}else{
			return FailMode.BACK;
		}
	}
	/** Flag this node/state as a failure **/
	public void flagFailure(){
		fail = true;
		failType = potentialFailMode;
	}
	
	/** Go through all the listed bodies and compare their state to the other one entered. This does an abs(This - Other) for all states and weights them according to parameters defined here **/
	public float Compare(StateHolder other){
		float error = 0;
		for (int i = 0; i<AllLinks.length; i++){
			for (int j = 0; j<AllLinks[0].length; j++){
				switch (j){
				case 0: //It's an x value
					error += XWeight*AllWeights[i]*Math.abs(AllLinks[i][j]-other.AllLinks[i][j]);
					break;
				case 1: //It's a y value
					error += YWeight*AllWeights[i]*Math.abs(AllLinks[i][j]-other.AllLinks[i][j]);
					break;
				case 2: //It's an angle
					error += AngleWeight*AllWeights[i]*Math.abs(AllLinks[i][j]-other.AllLinks[i][j]);
					break;
				default:
					System.out.println("Indexing error when comparing values from from 2 states.");
					break;
				}
				
					
			}
		}
		return error;
	}	
}
