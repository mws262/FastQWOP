package TreeQWOP;
//TODO densities: feet = 3, head: 1,thigh: 1
//TODO frictions: feet 1.5, head 0.2, thigh 0.2
//TODO separate lower and upper arms?

// Coordinate frame of real game is x forward, y down, z into the board.
//TODO:
// Left ankle to left knee dist =  4.431397
//right ankle to right knee dist = 4.208169
//right knee to hip = 4.193477
//left knee to hip = 3.559077
// left hip to head 5.0213
//right hip to head: 5.0860

//Hip to hip = 0.796501

//left foot mass 10.895, inertia 8.242
//right foot mass 11.630, inertia 9.017

//left calf inertia 16.893, mass 7.464
//right calf inertia 16.644, mass 7.407

//left thigh mass 10.037, inertia 24.546
//right thigh mass 10.540, 28.067

//head inertia: 5.483, mass 5.674

//torso mass 18.668, 79.376

//Left upper arm:
// Inertia: 5.850, mass 4.6065

//Right upper arm:
//Inertia: 8.479, mass: 5.837

//Left lower arm:
//Inertia: 4.301, Mass: 3.8445

//Right lower arm:
// Inertia: 10.768, mass: 5.990

//Foot vertices:
//Left:
//[-1.3475,-0.673750]
//[1.3475,-0.673759]
//	[1.3475,0.673750]
//		[-1.34750,0.673750]
//Centroid at 0,0
//core???
// [1.3075, 0.633750]

//Right:
// [-1.343750,-0.72125]
// [1.343750, -0.721250]
// [...]
//Core???
// [1.303750,0.681250]


//footworldPts
//Left: oops core
// 0: [2.558578, 7.28776]
//1: [5.146925, 7.66016]
//2: [4.966422, 8.91474]
//3: [ 2.378075, 8.54234]

//Left: normal ** USE THIS
// 0: [2.52468, 7.24247]
//1: [5.19221, 7.62626]
//2: [5.00032, 8.96003]
//3: [ 2.33279, 8.57624]

//right:
//0: [-1.4573205,6.38544]
//1: [0.450875,8.16247]
//2:[-0.47768, 9.15956
//3:[-2.38588,7.38253]

//right normal ** USE THIS
// 0: [-1.45933, 6.32891]
//1: [0.507408, 8.16046]
//2: [-0.475667, 9.21609]
//3: [ -2.44241, 7.38454]

//Initial conditions: appears that +y is DOWN

//The ground:
// Angle0: 0
// PosY0: 10.74375
// PosX0: 3.6 (doesn't really matter)

//torso: note appears angle is clockwise positive. horizontal is about 0.
// Angle0: -1.251
// PosY0:  -1.926 %THIS SEEMS WRONG
// PosX0: 2.525

//Head:  angle backwards again. 0 is head upright (maybe slightly tipped back??)
// Angle0: 0.0580
// PosY0:  -5.679
// PosX0: 3.896

//Left foot: This foot starts forward,0 is almost flat
// Angle0: 0.1429
// PosY0:  8.101
// PosX0: 3.763

//Right foot: This foot starts back,0 is almost flat, slightly back?
// Angle0: 0.7498
// PosY0:  7.772
// PosX0: -0.9675

//Left calf: This one looks almost vertical in its initial state. The sprite is basically horizontal.
// Angle0: -1.582
// PosY0:  5.523
// PosX0: 2.986

//Right calf: This one looks tipped forward in initial state.
// Angle0: -0.821
// PosY0:  5.381
// PosX0: 0.0850

//Left thigh:
// Angle0: -1.977
// PosY0:  1.615
// PosX0: 2.520

//Right thigh:
// Angle0: 1.468
// PosY0:  1.999
// PosX0:  1.659

//Left upper arm:
// Angle0: 0.843
// PosY0:  -2.911
// PosX0:  4.475

//Right upper arm:
// Angle0: -0.466
// PosY0:  -3.616
// PosX0:  1.165

//Left lower arm:
// Angle0: -1.251
// PosY0:  -3.06
// PosX0:  5.899

//Right lower arm:
// Angle0: -1.762
// PosY0:  -1.248
// PosX0:  0.3662


//COM INFO
//left thigh joint to torso COM: 2.169
//right thigh joint to torso COM: 2.248
//neck to torso COM: 2.866

//Joint pos:
//Neck Joint pos: (3.604, -4.581)
//Right Thigh (hip): (1.26,-0.0675)
//Left Thigh (hip): (2.01625, 0.1825)
//Left calf (knee): (3.2625,3.51625)
//Right calf (knee): (1.58,4.11375)
//Left foot (ankle): (3.15125,7.94625)
//Right foot (ankle): (-1.395,7.09)

//CLUARM joint loc (upper left arm - shoulder): (3.63875,-3.58875)
//CRUARM joint loc (upper left arm - shoulder): (2.24375, -4.1425)
//CLFARM joint (left elbow): (5.65125,-1.8125)
//CRFARM joint (right elbow): (-0.06, -2.985)

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.*;
//import org.jbox2d.testbed.framework.TestbedTest;

public class QWOPGame{// extends JFrame{

  /* World object */
  public World m_world;

  /* Individual body objects */
  public Body RFootBody;
  public Body LFootBody;
  public Body RCalfBody;
  public Body LCalfBody;
  public Body RThighBody;
  public Body LThighBody;
  public Body TorsoBody;
  public Body RUArmBody;
  public Body LUArmBody;
  public Body RLArmBody;
  public Body LLArmBody;
  public Body HeadBody;
  public Body TrackBody;

  /* Joint Definitions */
  public RevoluteJointDef RHipJDef;
  public RevoluteJointDef LHipJDef;
  public RevoluteJointDef RKneeJDef;
  public RevoluteJointDef LKneeJDef;
  public RevoluteJointDef RAnkleJDef;
  public RevoluteJointDef LAnkleJDef;
  public RevoluteJointDef RShoulderJDef;
  public RevoluteJointDef LShoulderJDef;
  public RevoluteJointDef RElbowJDef;
  public RevoluteJointDef LElbowJDef;
  public RevoluteJointDef NeckJDef;
  
  /* Joint objects */
  public RevoluteJoint RHipJ;
  public RevoluteJoint LHipJ;
  public RevoluteJoint RKneeJ;
  public RevoluteJoint LKneeJ;
  public RevoluteJoint RAnkleJ;
  public RevoluteJoint LAnkleJ;
  public RevoluteJoint RShoulderJ;
  public RevoluteJoint LShoulderJ;
  public RevoluteJoint RElbowJ;
  public RevoluteJoint LElbowJ;
  public RevoluteJoint NeckJ;

//  DrawPane plot;
  
  OptionsHolder options = new OptionsHolder();
  
  public boolean visOn = false;
  
  public QWOPGame(){
	  
  }

  public void Setup(boolean graphicsFlag) {
	
	/*
	 * World Settings
	 */
	  
    Vec2 gravity = new Vec2(0, 10f);
    m_world = new World(gravity);
    m_world.setAllowSleep(true);
    m_world.setContinuousPhysics(true);
    m_world.setWarmStarting(true);
//    m_world.setSubStepping(true);
    
    //This filter says that body parts can't collide.
    Filter dudeFilter = new Filter();
    dudeFilter.groupIndex = -1;

//    options.visOn = graphicsFlag;
    this.visOn = graphicsFlag;
    	
   /* 
    * Make the bodies and collision shapes
    */
    	
    	
    /* TRACK */
    	float TrackPosY = 8.90813f;
    	float TrackFric = 1f;
    	float TrackRest = 0.2f;

        BodyDef TrackDef = new BodyDef();
        TrackBody = m_world.createBody(TrackDef);
        EdgeShape TrackShape = new EdgeShape();
        TrackShape.set(new Vec2(-100.0f, TrackPosY), new Vec2(2000.0f, TrackPosY));
        Fixture TrackFix = TrackBody.createFixture(TrackShape, 0.0f);
        
        TrackFix.setFriction(TrackFric);
        TrackFix.setRestitution(TrackRest);
        
      /* FEET */
      
      Vec2 RFootPos = new Vec2(-0.96750f,7.77200f);
      Vec2 LFootPos = new Vec2(3.763f,8.101f);
      
      float RFootAng = 0.7498f;
      float LFootAng = 0.1429f;
      
      float RFootAngAdj = 0f;
      float LFootAngAdj = 0f;
      
      Vec2 RFootPosAdj = new Vec2(0,0);
      Vec2 LFootPosAdj = new Vec2(0,0);

      
      float RFootMass = 11.630f;
      float LFootMass = 10.895f;
      
      float RFootInertia = 9.017f;
      float LFootInertia = 8.242f;
      
      MassData RFootMassData = new MassData();
      MassData LFootMassData = new MassData();
      
      RFootMassData.I = RFootInertia;
      RFootMassData.mass = RFootMass;
      
      LFootMassData.I = LFootInertia;
      LFootMassData.mass = LFootMass;
      
      float RFootL = 2.68750f;
      float LFootL = 2.695f;
      
      float RFootH = 1.44249f;
      float LFootH = 1.34750f;
      
      float RFootFric = 1.5f;
      float LFootFric = 1.5f;

      
      //Create the fixture shapes, IE collision shapes.
      PolygonShape RFootShape = new PolygonShape();
      PolygonShape LFootShape = new PolygonShape();
      
      RFootShape.setAsBox(RFootL/2, RFootH/2,RFootPosAdj,RFootAngAdj);//, RFootPos, RFootAng);
      LFootShape.setAsBox(LFootL/2, LFootH/2,LFootPosAdj,LFootAngAdj);//, LFootPos, LFootAng);
      
      //Dynamics body definitions
      BodyDef RFootDef = new BodyDef();
      BodyDef LFootDef = new BodyDef();
      RFootDef.type = BodyType.DYNAMIC;
      LFootDef.type = BodyType.DYNAMIC;
    
      RFootDef.position.set(RFootPos);
      RFootDef.angle = RFootAng;
      
      LFootDef.position.set(LFootPos);
      LFootDef.angle = LFootAng;
      
      RFootBody = getWorld().createBody(RFootDef);
      LFootBody = getWorld().createBody(LFootDef);

      
      Fixture RFootFix = RFootBody.createFixture(RFootShape,1); //keep feet from colliding with other body parts
      Fixture LFootFix = LFootBody.createFixture(LFootShape,1);
      
      RFootFix.setFilterData(dudeFilter);
      LFootFix.setFilterData(dudeFilter);
      
      RFootFix.setFriction(RFootFric);
      LFootFix.setFriction(LFootFric);
      
      RFootBody.setMassData(RFootMassData); //Set mass and inertia
      LFootBody.setMassData(LFootMassData);
      
      /* CALVES */
      
      Vec2 RCalfPos = new Vec2(0.0850f,5.381f);
      Vec2 LCalfPos = new Vec2(2.986f,5.523f);
      
      float RCalfAng = -0.821f;
      float LCalfAng = -1.582f;
      
      float RCalfAngAdj = 1.606188724f;
      float LCalfAngAdj = 1.607108307f;
      
      Vec2 RCalfPosAdj = new Vec2(0,0);
      Vec2 LCalfPosAdj = new Vec2(0,0);
      
      float RCalfMass = 7.407f;
      float LCalfMass = 7.464f;
      
      float RCalfInertia = 16.644f;
      float LCalfInertia = 16.893f;
      
      MassData RCalfMassData = new MassData();
      MassData LCalfMassData = new MassData();
      
      RCalfMassData.I = RCalfInertia;
      RCalfMassData.mass = RCalfMass;
      
      LCalfMassData.I = LCalfInertia;
      LCalfMassData.mass = LCalfMass;
      
      //Length and width for the calves are just for collisions with the ground, so not very important.
      float RCalfL = 4.21f;
      float LCalfL = 4.43f;
      
      float RCalfW = 0.4f;
      float LCalfW = 0.4f;
      
      float RCalfFric = 0.2f;
      float LCalfFric = 0.2f;

      
      PolygonShape RCalfShape = new PolygonShape();
      PolygonShape LCalfShape = new PolygonShape();
      
      RCalfShape.setAsBox(RCalfW/2, RCalfL/2, RCalfPosAdj, RCalfAngAdj);//,RCalfPos,RCalfAng); 
      LCalfShape.setAsBox(LCalfW/2, LCalfL/2, LCalfPosAdj, LCalfAngAdj);//,LCalfPos,LCalfAng);
            
      BodyDef RCalfDef = new BodyDef();
      BodyDef LCalfDef = new BodyDef();
    
      RCalfDef.type = BodyType.DYNAMIC;
      LCalfDef.type = BodyType.DYNAMIC;
      
      RCalfDef.position.set(RCalfPos);
      RCalfDef.angle = RCalfAng;
      
      LCalfDef.position.set(LCalfPos);
      LCalfDef.angle = LCalfAng;
     
      RCalfBody = getWorld().createBody(RCalfDef);
      LCalfBody = getWorld().createBody(LCalfDef);
      
      Fixture RCalfFix = RCalfBody.createFixture(RCalfShape,1);
      Fixture LCalfFix = LCalfBody.createFixture(LCalfShape,1);
      
      RCalfFix.setFilterData(dudeFilter);
      LCalfFix.setFilterData(dudeFilter);
      
      RCalfFix.setFriction(RCalfFric);
      LCalfFix.setFriction(LCalfFric);
      
      RCalfBody.setMassData(RCalfMassData);
      LCalfBody.setMassData(LCalfMassData);
      /* THIGHS */
      
      Vec2 RThighPos = new Vec2(1.659f,1.999f);
      Vec2 LThighPos = new Vec2(2.52f,1.615f);
      
      float RThighAng = 1.468f;
      float LThighAng = -1.977f;
      
      float RThighAngAdj = -1.544382589f;
      float LThighAngAdj = 1.619256373f;
      
      Vec2 RThighPosAdj = new Vec2(0,0);
      Vec2 LThighPosAdj = new Vec2(0,0);
      
      float RThighMass = 10.54f;
      float LThighMass = 10.037f;
      
      float RThighInertia = 28.067f;
      float LThighInertia = 24.546f;
      
      MassData RThighMassData = new MassData();
      MassData LThighMassData = new MassData();
      
      RThighMassData.I = RThighInertia;
      RThighMassData.mass = RThighMass;
      
      LThighMassData.I = LThighInertia;
      LThighMassData.mass = LThighMass;
      
      //Length and width for the calves are just for collisions with the ground, so not very important.
      float RThighL = 4.19f;
      float LThighL = 3.56f;
      
      float RThighW = 0.6f;
      float LThighW = 0.6f;
      
      float RThighFric = 0.2f;
      float LThighFric = 0.2f;
     
      PolygonShape RThighShape = new PolygonShape();
      PolygonShape LThighShape = new PolygonShape();
      
      RThighShape.setAsBox(RThighW/2, RThighL/2, RThighPosAdj, RThighAngAdj);//,RThighPos,RThighAng);
      LThighShape.setAsBox(LThighW/2, LThighL/2, LThighPosAdj, LThighAngAdj);//,LThighPos,LThighAng);
      
      BodyDef RThighDef = new BodyDef();
      BodyDef LThighDef = new BodyDef();
      
      RThighDef.type = BodyType.DYNAMIC;
      LThighDef.type = BodyType.DYNAMIC;
      
      RThighDef.position.set(RThighPos);
      LThighDef.position.set(LThighPos);
      
      RThighDef.angle = RThighAng;
      LThighDef.angle = LThighAng;
      
      RThighBody = getWorld().createBody(RThighDef);
      LThighBody = getWorld().createBody(LThighDef);

      Fixture RThighFix = RThighBody.createFixture(RThighShape,1);
      Fixture LThighFix = LThighBody.createFixture(LThighShape,1);
      
      RThighFix.setFilterData(dudeFilter);
      LThighFix.setFilterData(dudeFilter);
      
      RThighFix.setFriction(RThighFric);
      LThighFix.setFriction(LThighFric);
      
      RThighBody.setMassData(RThighMassData);
      LThighBody.setMassData(LThighMassData);

     /* TORSO */

      Vec2 TorsoPos = new Vec2(2.525f,-1.926f);
      
      float TorsoAng = -1.251f;
      
      float TorsoAngAdj = 1.651902129f;
      
      Vec2 TorsoPosAdj = new Vec2(0,0);
      
      float TorsoMass = 18.668f;
      
      float TorsoInertia = 79.376f;
      
      MassData TorsoMassData = new MassData();
      
      TorsoMassData.I = TorsoInertia;
      TorsoMassData.mass = TorsoMass;
      
      //Length and width for the calves are just for collisions with the ground, so not very important.
      float TorsoL = 5f;
      float TorsoW = 1.5f;
      
      float TorsoFric = 0.2f;
      
     PolygonShape TorsoShape = new PolygonShape();
     TorsoShape.setAsBox(TorsoW/2, TorsoL/2, TorsoPosAdj, TorsoAngAdj);//,TorsoPos,TorsoAng); 
     
     BodyDef TorsoDef = new BodyDef();
     TorsoDef.type = BodyType.DYNAMIC;
     
     TorsoDef.position.set(TorsoPos);
     TorsoDef.angle = TorsoAng;
     
     TorsoBody = getWorld().createBody(TorsoDef);
     
     Fixture TorsoFix = TorsoBody.createFixture(TorsoShape,1);
     TorsoFix.setFilterData(dudeFilter);
     TorsoFix.setFriction(TorsoFric);
     
     TorsoBody.setMassData(TorsoMassData);
     
     /* HEAD */
        
     Vec2 HeadPos = new Vec2(3.896f,-5.679f);
     
     float HeadAng = 0.058f;
     
     float HeadMass = 5.674f;
     
     float HeadAngAdj = 0.201921414f;
     
     Vec2 HeadPosAdj = new Vec2(0,0);
     
     float HeadInertia = 5.483f;
     
     MassData HeadMassData = new MassData();
     
     HeadMassData.I = HeadInertia;
     HeadMassData.mass = HeadMass;
     
     //Radius is just for collision shape
     float HeadR = 1.1f;

     float HeadFric = 0.2f;

     CircleShape HeadShape = new CircleShape();
     HeadShape.setRadius(HeadR);
     
     BodyDef HeadDef = new BodyDef();
     HeadDef.type = BodyType.DYNAMIC;
     HeadDef.position.set(HeadPos);
     HeadDef.angle = HeadAng;
     
     HeadBody = getWorld().createBody(HeadDef);
     
     Fixture HeadFix = HeadBody.createFixture(HeadShape,1);
     HeadFix.setFilterData(dudeFilter);
     HeadFix.setFriction(HeadFric);
     
     HeadBody.setMassData(HeadMassData);
     
     /* UPPER ARMS */

     Vec2 RUArmPos = new Vec2(1.165f,-3.616f);
     Vec2 LUArmPos = new Vec2(4.475f,-2.911f);
     
     float RUArmAng = -0.466f;
     float LUArmAng = 0.843f;
     
     float RUArmAngAdj = 1.571196588f;
     float LUArmAngAdj = -1.690706418f;
     
     Vec2 RUArmPosAdj = new Vec2(0,0);
     Vec2 LUArmPosAdj = new Vec2(0,0);
     
     float RUArmMass = 5.837f;
     float LUArmMass = 4.6065f;
     
     float RUArmInertia = 8.479f;
     float LUArmInertia = 5.85f;
     
     MassData RUArmMassData = new MassData();
     MassData LUArmMassData = new MassData();
     
     RUArmMassData.I = RUArmInertia;
     RUArmMassData.mass = RUArmMass;
     
     LUArmMassData.I = LUArmInertia;
     LUArmMassData.mass = LUArmMass;
     
     //for collision shapes
     float RUArmL = 2.58f;
     float LUArmL = 2.68f;
     
     float RUArmW = 0.2f;
     float LUArmW = 0.15f;
     
     float RUArmFric = 0.2f;
     float LUArmFric = 0.2f;
     
     
     PolygonShape RUArmShape = new PolygonShape();
     PolygonShape LUArmShape = new PolygonShape();

     RUArmShape.setAsBox(RUArmW/2,RUArmL/2, RUArmPosAdj, RUArmAngAdj);//,RUArmPos,RUArmAng);
     LUArmShape.setAsBox(LUArmW/2,LUArmL/2, LUArmPosAdj, LUArmAngAdj);//,LUArmPos,LUArmAng);
     
     BodyDef RUArmDef = new BodyDef();
     BodyDef LUArmDef = new BodyDef();
     
     RUArmDef.type = BodyType.DYNAMIC;
     LUArmDef.type = BodyType.DYNAMIC;
     
     RUArmDef.position.set(RUArmPos);
     LUArmDef.position.set(LUArmPos);
     
     RUArmDef.angle = RUArmAng;
     LUArmDef.angle = LUArmAng;
     
     RUArmBody = getWorld().createBody(RUArmDef);
     LUArmBody = getWorld().createBody(LUArmDef);
     
     Fixture RUArmFix = RUArmBody.createFixture(RUArmShape,1);
     Fixture LUArmFix = LUArmBody.createFixture(LUArmShape,1);
     
     RUArmFix.setFriction(RUArmFric);
     LUArmFix.setFriction(LUArmFric);
     
     RUArmFix.setFilterData(dudeFilter);
     LUArmFix.setFilterData(dudeFilter);
 
     RUArmBody.setMassData(RUArmMassData);
     LUArmBody.setMassData(LUArmMassData);

     
     /* LOWER ARMS */
     
     Vec2 RLArmPos = new Vec2(0.3662f,-1.248f);
     Vec2 LLArmPos = new Vec2(5.899f,-3.06f);
     
     float RLArmAng = -1.762f;
     float LLArmAng = -1.251f;
     
     float RLArmAngAdj = 1.521319096f;
     float LLArmAngAdj = 1.447045854f;
     
     Vec2 RLArmPosAdj = new Vec2(0,0);
     Vec2 LLArmPosAdj = new Vec2(0,0);
     
     float RLArmMass = 5.99f;
     float LLArmMass = 3.8445f;
     
     float RLArmInertia = 10.768f;
     float LLArmInertia = 4.301f;
     
     MassData RLArmMassData = new MassData();
     MassData LLArmMassData = new MassData();
     
     RLArmMassData.I = RLArmInertia;
     RLArmMassData.mass = RLArmMass;
     
     LLArmMassData.I = LLArmInertia;
     LLArmMassData.mass = LLArmMass;
     
     //for collision shapes
     float RLArmL = 3.56f;
     float LLArmL = 2.54f;

     float RLArmW = 0.15f;
     float LLArmW = 0.12f;
     
     float RLArmFric = 0.2f;
     float LLArmFric = 0.2f;
     
     
     PolygonShape RLArmShape = new PolygonShape();
     PolygonShape LLArmShape = new PolygonShape();

     RLArmShape.setAsBox(RLArmW/2, RLArmL/2, RLArmPosAdj, RLArmAngAdj);//,RLArmPos,RLArmAng);
     LLArmShape.setAsBox(LLArmW/2, LLArmL/2, LLArmPosAdj, LLArmAngAdj);//,LLArmPos,LLArmAng);
     
     BodyDef RLArmDef = new BodyDef();
     BodyDef LLArmDef = new BodyDef();
     
     RLArmDef.type = BodyType.DYNAMIC;
     LLArmDef.type = BodyType.DYNAMIC;
     
     RLArmDef.position.set(RLArmPos);
     LLArmDef.position.set(LLArmPos);
     
     RLArmDef.angle = RLArmAng;
     LLArmDef.angle = LLArmAng;
     
     RLArmBody = getWorld().createBody(RLArmDef);
     LLArmBody = getWorld().createBody(LLArmDef);
     
     Fixture RLArmFix = RLArmBody.createFixture(RLArmShape,1);
     Fixture LLArmFix = LLArmBody.createFixture(LLArmShape,1);
     
     RLArmFix.setFriction(RLArmFric);
     LLArmFix.setFriction(LLArmFric);
     
     RLArmFix.setFilterData(dudeFilter);
     LLArmFix.setFilterData(dudeFilter);
     
     RLArmBody.setMassData(RLArmMassData);
     LLArmBody.setMassData(LLArmMassData);
     
     /*
      *  Joints
      */
     
     
     
     /* Joints Positions*/
     
     Vec2 RAnklePos = new Vec2(-0.96750f,7.77200f);
     Vec2 LAnklePos = new Vec2(3.763f,8.101f);
     
     Vec2 RKneePos = new Vec2(1.58f,4.11375f);
     Vec2 LKneePos = new Vec2(3.26250f,3.51625f);
     
     Vec2 RHipPos = new Vec2(1.260f,-0.06750f);
     Vec2 LHipPos = new Vec2(2.01625f,0.18125f);
     
     Vec2 RShoulderPos = new Vec2(2.24375f,-4.14250f);
     Vec2 LShoulderPos = new Vec2(3.63875f,-3.58875f);
     
     Vec2 RElbowPos = new Vec2(-0.06f,-2.985f);
     Vec2 LElbowPos = new Vec2(5.65125f,-1.8125f);
     
     Vec2 NeckPos = new Vec2(3.60400f,-4.581f);
     
    //Right Ankle:
    RAnkleJDef = new RevoluteJointDef(); 
    RAnkleJDef.initialize(RFootBody,RCalfBody, RAnklePos); //Body1, body2, anchor in world coords
    RAnkleJDef.enableLimit = true;
    RAnkleJDef.upperAngle = 0.5f;
    RAnkleJDef.lowerAngle = -0.5f;
    RAnkleJDef.enableMotor = false;
    RAnkleJDef.maxMotorTorque = 2000f;
    RAnkleJDef.motorSpeed = 0f; // Speed1,2: -2,2
    
    RAnkleJ = (RevoluteJoint)getWorld().createJoint(RAnkleJDef);
    
    //Left Ankle:
    LAnkleJDef = new RevoluteJointDef();
    LAnkleJDef.initialize(LFootBody,LCalfBody, LAnklePos);
    LAnkleJDef.enableLimit = true;
    LAnkleJDef.upperAngle = 0.5f;
    LAnkleJDef.lowerAngle = -0.5f;
    LAnkleJDef.enableMotor = false;
    LAnkleJDef.maxMotorTorque = 2000;
    LAnkleJDef.motorSpeed = 0f;// Speed1,2: 2,-2
    
    LAnkleJ = (RevoluteJoint)getWorld().createJoint(LAnkleJDef);
      
    /* Knee joints */
    
    //Right Knee:
    RKneeJDef = new RevoluteJointDef();
    RKneeJDef.initialize(RCalfBody,RThighBody, RKneePos);
    RKneeJDef.enableLimit = true;
    RKneeJDef.upperAngle = 0.3f;
    RKneeJDef.lowerAngle = -1.3f;
    RKneeJDef.enableMotor = true;//?
    RKneeJDef.maxMotorTorque = 3000;
    RKneeJDef.motorSpeed = 0f; //Speeds 1,2: -2.5,2.5
    
    RKneeJ = (RevoluteJoint)getWorld().createJoint(RKneeJDef);
    
    //Left Knee:
    LKneeJDef = new RevoluteJointDef();
    LKneeJDef.initialize(LCalfBody,LThighBody, LKneePos);
    LKneeJDef.enableLimit = true;
    LKneeJDef.upperAngle = 0f;
    LKneeJDef.lowerAngle = -1.6f;
    LKneeJDef.enableMotor = true;
    LKneeJDef.maxMotorTorque = 3000;
    LKneeJDef.motorSpeed = 0f;// Speed1,2: -2.5,2.5
    
    LKneeJ = (RevoluteJoint)getWorld().createJoint(LKneeJDef);
    
    /* Hip Joints */
    
    //Right Hip:
    RHipJDef = new RevoluteJointDef();
    RHipJDef.initialize(RThighBody,TorsoBody, RHipPos);
    RHipJDef.enableLimit = true;
    RHipJDef.upperAngle = 0.7f;
    RHipJDef.lowerAngle = -1.3f;
    RHipJDef.enableMotor = true;
    RHipJDef.motorSpeed = 0f;
    RHipJDef.maxMotorTorque = 6000f;
    
    RHipJ = (RevoluteJoint)getWorld().createJoint(RHipJDef);
    
    //Left Hip:
    LHipJDef = new RevoluteJointDef();
    LHipJDef.initialize(LThighBody,TorsoBody, LHipPos);
    LHipJDef.enableLimit = true;
    LHipJDef.upperAngle = 0.5f;
    LHipJDef.lowerAngle = -1.5f;
    LHipJDef.enableMotor = true;
    LHipJDef.motorSpeed = 0f;
    LHipJDef.maxMotorTorque = 6000f;
    
    LHipJ = (RevoluteJoint)getWorld().createJoint(LHipJDef);


    //Neck Joint
    NeckJDef = new RevoluteJointDef();
    NeckJDef.initialize(HeadBody,TorsoBody, NeckPos);
    NeckJDef.enableLimit = true;
    NeckJDef.upperAngle = 0f;
    NeckJDef.lowerAngle = -0.5f;
    NeckJDef.enableMotor = true;
    NeckJDef.maxMotorTorque = 0f;
    NeckJDef.motorSpeed = 1000f; //Arbitrarily large to allow for torque control.    
    NeckJ = (RevoluteJoint)getWorld().createJoint(NeckJDef);
    
    /* Arm Joints */
    //Right shoulder
    RShoulderJDef = new RevoluteJointDef();
    RShoulderJDef.initialize(RUArmBody,TorsoBody, RShoulderPos);
    RShoulderJDef.enableLimit = true;
    RShoulderJDef.upperAngle = 1.5f;
    RShoulderJDef.lowerAngle = -0.5f;
    RShoulderJDef.enableMotor = true;
    RShoulderJDef.maxMotorTorque = 1000f;
    RShoulderJDef.motorSpeed = 0f; // Speed 1,2: 2,-2
    RShoulderJ = (RevoluteJoint)getWorld().createJoint(RShoulderJDef);
    
    //Left shoulder
    LShoulderJDef = new RevoluteJointDef();
    LShoulderJDef.initialize(LUArmBody,TorsoBody, LShoulderPos);
    LShoulderJDef.enableLimit = true;
    LShoulderJDef.upperAngle = 0f;
    LShoulderJDef.lowerAngle = -2f;
    LShoulderJDef.enableMotor = true;
    LShoulderJDef.maxMotorTorque = 1000f;
    LShoulderJDef.motorSpeed = 0f; // Speed 1,2: -2,2
    LShoulderJ = (RevoluteJoint)getWorld().createJoint(LShoulderJDef);
    
    //Right elbow
    RElbowJDef = new RevoluteJointDef();
    RElbowJDef.initialize(RLArmBody,RUArmBody, RElbowPos);
    RElbowJDef.enableLimit = true;
    RElbowJDef.upperAngle = 0.5f;
    RElbowJDef.lowerAngle = -0.1f;
    RElbowJDef.enableMotor = true;
    RElbowJDef.maxMotorTorque = 0f;
    RElbowJDef.motorSpeed = 10f; //TODO: investigate further 
    RElbowJ = (RevoluteJoint)getWorld().createJoint(RElbowJDef);
    
    //Left elbow
    LElbowJDef = new RevoluteJointDef();
    LElbowJDef.initialize(LLArmBody,LUArmBody, LElbowPos);
    LElbowJDef.enableLimit = true;
    LElbowJDef.upperAngle = 0.5f;
    LElbowJDef.lowerAngle = -0.1f;
    LElbowJDef.enableMotor = true;
    LElbowJDef.maxMotorTorque = 0f;
    LElbowJDef.motorSpeed = 10f; //TODO: investigate further  
    LElbowJ = (RevoluteJoint)getWorld().createJoint(LElbowJDef);
    



  } 
  
  public void everyStep(boolean q, boolean w, boolean o, boolean p){
	  
	  /* Involuntary Couplings (no QWOP presses) */

	  //Springs:
	  float NeckStiff = 15f;
	  float NeckDamp = 5f;
	  
	  float RElbowStiff = 1f;
	  float LElbowStiff = 1f;
	  
	  float RElbowDamp = 0f;
	  float LElbowDamp = 0f;
	  
	  //Neck spring torque
	  float NeckTorque = -NeckStiff*NeckJ.getJointAngle() - NeckDamp*NeckJ.getJointSpeed();
	  NeckTorque = NeckTorque - 400f*(NeckJ.getJointAngle() + 0.2f); //This bizarre term is probably a roundabout way of adjust equilibrium position.
	  
	  //Elbow spring torque
	  float RElbowTorque = -RElbowStiff*RElbowJ.getJointAngle() - RElbowDamp*RElbowJ.getJointSpeed();
	  float LElbowTorque = -LElbowStiff*LElbowJ.getJointAngle() - LElbowDamp*LElbowJ.getJointSpeed();

	  //For now, using motors with high speed settings and torque limits to simulate springs. I don't know a better way for now.
	  
	  NeckJ.setMotorSpeed(1000*Math.signum(NeckTorque)); //If torque is negative, make motor speed negative.
	  RElbowJ.setMotorSpeed(1000*Math.signum(RElbowTorque));
	  LElbowJ.setMotorSpeed(1000*Math.signum(LElbowTorque));	
 
	  NeckJ.setMaxMotorTorque(Math.abs(NeckTorque));
	  RElbowJ.setMaxMotorTorque(Math.abs(RElbowTorque));
	  LElbowJ.setMaxMotorTorque(Math.abs(LElbowTorque));
	  
	  
	  
	  /* QWOP Keypress actions */
	  
	  //Ankle speeds setpoints:
	  float RAnkleSpeed1 = 2f;
	  float RAnkleSpeed2 = -2f;
	  
	  float LAnkleSpeed1 = -2f;
	  float LAnkleSpeed2 = 2f;
	  
	  float RKneeSpeed1 = -2.5f;
	  float RKneeSpeed2 = 2.5f;
	  
	  float LKneeSpeed1 = 2.5f;
	  float LKneeSpeed2 = -2.5f;
	  
	  float RHipSpeed1 = -2.5f;
	  float RHipSpeed2 = 2.5f;
	  
	  float LHipSpeed1 = 2.5f;
	  float LHipSpeed2 = -2.5f;
	  
	  float RShoulderSpeed1 = 2f;
	  float RShoulderSpeed2 = -2f;
	  
	  float LShoulderSpeed1 = -2f;
	  float LShoulderSpeed2 = 2f;
	  
	  //O Hip limits (changed to this when o is pressed):
	  float ORHipLimLo = -1.3f;
	  float ORHipLimHi = 0.7f;
	  
	  float OLHipLimLo = -1f;
	  float OLHipLimHi = 1f;
	 
	  
	  //P Hip limits:
	  float PRHipLimLo = -0.8f;
	  float PRHipLimHi = 1.2f;
	  
	  float PLHipLimLo = -1.5f;
      float PLHipLimHi = 0.5f;

	  
	  /* QW Press Stuff */
	  //See spreadsheet for complete rules and priority explanations.
	  if (q){
		  //Set speed 1 for hips:
		  LHipJ.setMotorSpeed(LHipSpeed2);
		  RHipJ.setMotorSpeed(RHipSpeed2);
		  
		  //Set speed 1 for shoulders:
		  LShoulderJ.setMotorSpeed(LShoulderSpeed2);
		  RShoulderJ.setMotorSpeed(RShoulderSpeed2);
		  
	  }else if(w){
		  //Set speed 2 for hips:
		  LHipJ.setMotorSpeed(LHipSpeed1);
		  RHipJ.setMotorSpeed(RHipSpeed1);
		  
		  //set speed 2 for shoulders:
		  LShoulderJ.setMotorSpeed(LShoulderSpeed1);
		  RShoulderJ.setMotorSpeed(RShoulderSpeed1);
		  
	  }else{
		  //Set hip and ankle speeds to 0:
		  LHipJ.setMotorSpeed(0f);
		  RHipJ.setMotorSpeed(0f);
		  
		  LShoulderJ.setMotorSpeed(0f);
		  RShoulderJ.setMotorSpeed(0f);
	  }
	  
	  //Ankle/Hip Coupling -- Requires either Q or W pressed.
	  if (q || w){
		  //Get world ankle positions (using foot and torso anchors -- TODO: see if this is correct)
		  Vec2 RAnkleCur = new Vec2(0,0);
		  Vec2 LAnkleCur = new Vec2(0,0);
		  Vec2 RHipCur = new Vec2(0,0);
		  
		  RAnkleJ.getAnchorA(RAnkleCur);
		  LAnkleJ.getAnchorA(LAnkleCur);
		 
		  RHipJ.getAnchorA(RHipCur);
		  
		  
		  // if right ankle joint is behind the right hip jiont
		  	// Set ankle motor speed to 1;
		  // else speed 2
		  if (RAnkleCur.x<RHipCur.x){
			  RAnkleJ.setMotorSpeed(RAnkleSpeed2);
		  }else{
			  RAnkleJ.setMotorSpeed(RAnkleSpeed1);
		  }
		  
		  
		  // if left ankle joint is behind RIGHT hip joint (weird it's the right one here too)
		  	// Set its motor speed to 1;
		  // else speed 2;  
		  if (LAnkleCur.x<RHipCur.x){
			  LAnkleJ.setMotorSpeed(LAnkleSpeed2);
		  }else{
			  LAnkleJ.setMotorSpeed(LAnkleSpeed1);
		  }
		  
	  }
	  
	  /* OP Keypress Stuff */
	  if (o){
		  //Set speed 1 for knees
		  // set l hip limits(-1 1)
		  //set right hip limits (-1.3,0.7)
		  RKneeJ.setMotorSpeed(RKneeSpeed2);
		  LKneeJ.setMotorSpeed(LKneeSpeed2);
		  
		  RHipJ.setLimits(ORHipLimLo, ORHipLimHi);
		  LHipJ.setLimits(OLHipLimLo, OLHipLimHi);
		  
		  
	  }else if(p){
		  //Set speed 2 for knees
		  // set L hip limits(-1.5,0.5)
		  // set R hip limits(-0.8,1.2)
		  
		  RKneeJ.setMotorSpeed(RKneeSpeed1);
		  LKneeJ.setMotorSpeed(LKneeSpeed1);
		  
		  RHipJ.setLimits(PRHipLimLo, PRHipLimHi);
		  LHipJ.setLimits(PLHipLimLo, PLHipLimHi);

	  }else{
		  
		  // Set knee speeds to 0
		  //Joint limits not changed!!
		  RKneeJ.setMotorSpeed(0f);
		  LKneeJ.setMotorSpeed(0f);  
	  }
	  
  }
  public World getWorld() {
	  return m_world;
}
}
