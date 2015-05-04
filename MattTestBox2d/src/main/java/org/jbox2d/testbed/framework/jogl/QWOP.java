//TODO densities: feet = 3, head: 1,thigh: 1
//TODO frictions: feet 1.5, head 0.2, thigh 0.2
//TODO separate lower and upper arms?

package org.jbox2d.testbed.framework.jogl;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.testbed.framework.TestbedTest;

public class QWOP extends TestbedTest {
  private final boolean switchBodiesInJoint;

  Body foot1;
  Body foot2;
  Body calf1;
  Body calf2;
  Body thigh1;
  Body thigh2;
  Body torso;
  Body arm1;
  Body arm2;
  Body head;
  float torqueScale = 250;
  float fric1 = 0.2f; //This is used for everything except the feet.
  float fric2 =5;// 1.5f; //this is for the feet
  
  float neckStiff = 500;
  float neckDamp = 50f;
  float neckEquil = 0;
  float maxNeckSpeed = 5;
  
  float armSpeed = 2f;
  float ankleSpeed = 2f;
  
  RevoluteJointDef hipJoint1;
  RevoluteJointDef hipJoint2;
  RevoluteJointDef knee1;
  RevoluteJointDef knee2;
  RevoluteJointDef ankle1;
  RevoluteJointDef ankle2;
  RevoluteJointDef armJoint1;
  RevoluteJointDef armJoint2;
  RevoluteJointDef neckJoint;
  
  
  RevoluteJoint hj1;
  RevoluteJoint hj2;
  RevoluteJoint k1;
  RevoluteJoint k2;
  RevoluteJoint a1;
  RevoluteJoint a2;
  RevoluteJoint aj1;
  RevoluteJoint aj2;
  RevoluteJoint nj;
  
  Body ground;
  
  Vec2[] tree = new Vec2[11];
  Vec2[] treePlace = new Vec2[11];
  
  public QWOP(boolean switchBodiesInJoint) {
    this.switchBodiesInJoint = switchBodiesInJoint;
  }
  
  @Override
  public boolean isSaveLoadEnabled() {
    return true;
  }

  @Override
  public void initTest(boolean deserialized) {
    if (deserialized) {
      return;
    }
    
    getWorld().setGravity(new Vec2(0,-50));
    //All bodies declared here:
    //background scenery
  
    
    tree[0] = new Vec2(-3.3f,0);
    tree[1] = new Vec2(-3.3f,5);
    tree[2] = new Vec2(-10f,5);
    tree[3] = new Vec2(-3.3f,15);
    tree[4] = new Vec2(-10f,15);
    tree[5] = new Vec2(0f,25);
    tree[6] = new Vec2(10f,15);
    tree[7] = new Vec2(3.3f,15);
    tree[8] = new Vec2(10f,5);
    tree[9] = new Vec2(3.3f,5);
    tree[10] = new Vec2(3.3f,0);

    for (int i = 0; i<treePlace.length; i++){
    	treePlace[i] = new Vec2(0,0);  	
    }
    
    	//Make the ground
        BodyDef bd = new BodyDef();
        ground = getWorld().createBody(bd);
        EdgeShape groundShape = new EdgeShape();
        groundShape.set(new Vec2(-100.0f, 0.0f), new Vec2(5000.0f, 0.0f));
        Fixture groundFix = ground.createFixture(groundShape, 0.0f);
        groundFix.setFriction(1f);

      
      //This filter says that body parts can't collide.
      Filter dudeFilter = new Filter();
      dudeFilter.groupIndex = -1;

      
      //Create the feet -- foot 1 is the trailing foot to start with. It starts at an angle. The other foot starts flat atm
      PolygonShape foot = new PolygonShape();
      
      Vec2[] footVert = new Vec2[3];
      footVert[0] = new Vec2(8, 0); //toe
      footVert[1] = new Vec2(0, 4); // top of foot
      footVert[2] = new Vec2(0, 0); // heel
      
      foot.set(footVert, 3);
      
      BodyDef footDef = new BodyDef();
      footDef.type = BodyType.DYNAMIC;
      
      float footAng1 = (float)-Math.PI/5;
      
      footDef.position.set(-18, 2f);
      
      //All this just makes sure that the toe is basically at the ground and not under it.
      footDef.angle = footAng1;
      foot1 = getWorld().createBody(footDef);
      
      Fixture footFix1 = foot1.createFixture(foot,1); //keep feet from colliding with other body parts
      footFix1.setFilterData(dudeFilter);
      footFix1.m_density = 3f;
      footFix1.setFriction(fric2);
      
      
      Vec2 toePos = foot1.getWorldPoint(footVert[0].sub(foot.m_centroid)); //Get world point of vector from centroid to toe of foot.
      foot1.setTransform(new Vec2(foot1.getPosition().x,foot1.getPosition().y-toePos.y+0.01f), footAng1);
      
      footDef.position.set(10, 0.01f);
      footDef.angle = 0f;
      foot2 = getWorld().createBody(footDef);
      
      Fixture footFix2 = foot2.createFixture(foot,1);
      footFix2.setFilterData(dudeFilter);
      footFix2.m_density = 3f;
      footFix2.setFriction(fric2);

      
      //Make calves
      float calfLength = 10f;
      
      PolygonShape calf = new PolygonShape();
      calf.setAsBox(1f, calfLength); 
      
      //CALF 1:
      float calfAng1 = (float)-Math.PI/5;
      BodyDef calfDef1 = new BodyDef();
      calfDef1.type = BodyType.DYNAMIC;
      calfDef1.position.set(-10, 10);
      calfDef1.angle = calfAng1;
      
      calf1 = getWorld().createBody(calfDef1);
      Fixture calfFix1 = calf1.createFixture(calf,1);
      calfFix1.setFilterData(dudeFilter);
      calfFix1.m_density = 1f;
      calfFix1.setFriction(fric1);
      
      
      Vec2 anklePos1 = foot1.getWorldPoint(footVert[1]); //Get world point of ankle
      Vec2 calfOffset1 = calf1.getWorldPoint(new Vec2(0,-calfLength)).sub(anklePos1);
      calf1.setTransform(calf1.getPosition().sub(calfOffset1), calfAng1);
      
      //CALF2:
      float calfAng2 = (float)-Math.PI/8;
      BodyDef calfDef2 = new BodyDef();
      calfDef2.type = BodyType.DYNAMIC;
      calfDef2.position.set(10, 10);
      calfDef2.angle = calfAng2;
      
      calf2 = getWorld().createBody(calfDef2);
      Fixture calfFix2 = calf2.createFixture(calf,1);
      calfFix2.setFilterData(dudeFilter);
      calfFix2.m_density = 1f;
      calfFix2.setFriction(fric1);
      
      
      Vec2 anklePos2 = foot2.getWorldPoint(footVert[1]);
      Vec2 calfOffset2 = calf2.getWorldPoint(new Vec2(0,-calfLength)).sub(anklePos2);
      calf2.setTransform(calf2.getPosition().sub(calfOffset2), calfAng2);
      
      
      //Make thighs
      float thighLength = 10f;
      float thighWidth = 2f;
      PolygonShape thigh = new PolygonShape();
      thigh.setAsBox(thighWidth, thighLength); 
      
      Vec2 kneePos1 = calf1.getWorldPoint(new Vec2(0,calfLength)); //Get world point of ankle
      Vec2 kneePos2 = calf2.getWorldPoint(new Vec2(0,calfLength)); //Get world point of ankle
      
      double theta = Math.acos(Math.pow(kneePos1.x-kneePos2.x,2)/(2*(kneePos1.sub(kneePos2).length())*2*thighLength)); //Law of cosines to find the angle in the triangle defined by the hip and two knees.
      double alpha = Math.atan((kneePos1.y-kneePos2.y)/(kneePos2.x - kneePos1.x));
      
      float thighAng1 = (float)(-(Math.PI/2 - (theta - alpha)));
      float thighAng2 = (float)(Math.PI/2 - (theta + alpha));
      
      
      //THIGH 1:
      BodyDef thighDef1 = new BodyDef();
      thighDef1.type = BodyType.DYNAMIC;
      thighDef1.position.set(-10, 20);
      thighDef1.angle = thighAng1;
      
      thigh1 = getWorld().createBody(thighDef1);
      Fixture thighFix1 = thigh1.createFixture(thigh,1);
      thighFix1.setFilterData(dudeFilter);
      thighFix1.m_density = 1f;
      thighFix1.setFriction(fric1);

      Vec2 thighOffset1 = thigh1.getWorldPoint(new Vec2(0,-thighLength)).sub(kneePos1);

      //THIGH2:
      BodyDef thighDef2 = new BodyDef();
      thighDef2.type = BodyType.DYNAMIC;
      thighDef2.position.set(10, 20);
      thighDef2.angle = thighAng2;
      
      thigh2 = getWorld().createBody(thighDef2);
      Fixture thighFix2 = thigh2.createFixture(thigh,1);
      thighFix2.setFilterData(dudeFilter);
      thighFix2.m_density = 1f;
      thighFix2.setFriction(fric1);
      
      Vec2 thighOffset2 = thigh2.getWorldPoint(new Vec2(0,-thighLength)).sub(kneePos2);

     thigh1.setTransform(thigh1.getPosition().sub(thighOffset1), thighAng1);
     thigh2.setTransform(thigh2.getPosition().sub(thighOffset2), thighAng2);   
     
     
     //TORSO:
     float torsoLength = 12f;
     float torsoWidth = 3f;
     
     PolygonShape torsoShape = new PolygonShape();
     torsoShape.setAsBox(torsoWidth, torsoLength); 
     
     
     float torsoAng = (float)(-Math.PI/12);
     BodyDef torsoDef = new BodyDef();
     torsoDef.type = BodyType.DYNAMIC;
     torsoDef.position.set(0, 30);
     torsoDef.angle = torsoAng;
     
     torso = getWorld().createBody(torsoDef);
     Fixture torsoFix = torso.createFixture(torsoShape,1);
     torsoFix.setFilterData(dudeFilter);
     torsoFix.m_density = 1f;
     torsoFix.setFriction(fric1);
     
     Vec2 hipPos = thigh1.getWorldPoint(new Vec2(0,thighLength)); //Get world point of hip
     Vec2 torsoOffset = torso.getWorldPoint(new Vec2(0,-torsoLength)).sub(hipPos);
     torso.setTransform(torso.getPosition().sub(torsoOffset), torsoAng);
     
     //HEAD:
     float headRad = 5f;
     float headAng = (float)(-Math.PI/8);
     CircleShape headShape = new CircleShape();
     headShape.setRadius(headRad);
     
     BodyDef headDef = new BodyDef();
     headDef.type = BodyType.DYNAMIC;
     headDef.position.set(0,50);
     headDef.angle = headAng;
     
     head = getWorld().createBody(headDef);
     Fixture headFix = head.createFixture(headShape,1);
     headFix.setFilterData(dudeFilter);
     headFix.m_density = 1f;
     headFix.setFriction(fric1);
     
     Vec2 neckPos = torso.getWorldPoint(new Vec2(0,torsoLength));
     Vec2 neckOffset = head.getWorldPoint(new Vec2(0,-headRad)).sub(neckPos);
     head.setTransform(head.getPosition().sub(neckOffset), headAng);
     
     //ARMS:
     float armLength = 7f;
     float armAng1 = (float)(-Math.PI/4 + torso.getAngle());
     float armAng2 = (float)(Math.PI/4 + torso.getAngle());
     
     PolygonShape armShape1 = new PolygonShape();
     PolygonShape armShape2 = new PolygonShape();

     armShape1.setAsBox(1, armLength, new Vec2(0,0), 0f);
     armShape2.setAsBox(armLength,0.8f,new Vec2(armLength,-armLength),0f);

     BodyDef armDef1 = new BodyDef();
     armDef1.type = BodyType.DYNAMIC;
     armDef1.position.set(-10, 50f);
     
     armDef1.angle = armAng1;
     arm1 = getWorld().createBody(armDef1);
     
     Fixture armFix1 = arm1.createFixture(armShape1,2); //keep feet from colliding with other body parts
     armFix1.setFilterData(dudeFilter);
     armFix1.m_density = 1f;
     armFix1.setFriction(fric1);
     Fixture armFix2 = arm1.createFixture(armShape2,2); //keep feet from colliding with other body parts
     armFix2.setFilterData(dudeFilter);
     armFix2.m_density = 1f;
     armFix2.setFriction(fric1);
     
     Vec2 shoulderPos = torso.getWorldPoint(new Vec2(0,0.9f*torsoLength)); //Get world point of vector from centroid to toe of foot.
     Vec2 shoulderOffset1 = arm1.getWorldPoint(new Vec2(0,armLength)).sub(shoulderPos);
     arm1.setTransform(new Vec2(arm1.getPosition().sub(shoulderOffset1)), armAng1);

     //arm2
     BodyDef armDef2 = new BodyDef();
     armDef2.type = BodyType.DYNAMIC;
     armDef2.position.set(-10, 50f);
     
     armDef2.angle = armAng2;
     arm2 = getWorld().createBody(armDef2); 
     
     Fixture armFix3 = arm2.createFixture(armShape1,2); //keep feet from colliding with other body parts
     armFix3.setFilterData(dudeFilter);
     armFix3.m_density = 1f;
     armFix3.setFriction(fric1);
     Fixture armFix4 = arm2.createFixture(armShape2,2); //keep feet from colliding with other body parts
     armFix4.setFilterData(dudeFilter);
     armFix4.m_density = 1f;
     armFix4.setFriction(fric1);
     
     Vec2 shoulderOffset2 = arm2.getWorldPoint(new Vec2(0,armLength)).sub(shoulderPos);
     
     arm2.setTransform(new Vec2(arm2.getPosition().sub(shoulderOffset2)), armAng2);

    //Ankle Joints
    ankle1 = new RevoluteJointDef();
    ankle1.initialize(foot1,calf1, foot1.getWorldPoint(footVert[1]));
    ankle1.enableLimit = true;
    ankle1.upperAngle = (0.5f) - (calf1.getAngle()-foot1.getAngle());
    ankle1.lowerAngle = (-0.5f) - (calf1.getAngle()-foot1.getAngle());
    ankle1.enableMotor = false;
    ankle1.maxMotorTorque = 2000*torqueScale;
    ankle1.motorSpeed = 2f;
    a1 = (RevoluteJoint)getWorld().createJoint(ankle1);
    
    ankle2 = new RevoluteJointDef();
    ankle2.initialize(foot2,calf2, foot2.getWorldPoint(footVert[1]));
    ankle2.enableLimit = true;
    ankle2.upperAngle = (0.5f) - (calf2.getAngle()-foot2.getAngle());
    ankle2.lowerAngle = (-0.5f) - (calf2.getAngle()-foot2.getAngle());
    ankle2.enableMotor = false;
    ankle2.maxMotorTorque = 2000*torqueScale;
    ankle2.motorSpeed = 2f;
    a2 = (RevoluteJoint)getWorld().createJoint(ankle2);
      
    //Knee joints
    knee1 = new RevoluteJointDef();
    knee1.initialize(calf1,thigh1, calf1.getWorldPoint(new Vec2(0,thighLength)));
    knee1.enableLimit = true;
    knee1.upperAngle = (1.6f)-(thigh1.getAngle()-calf1.getAngle());
    knee1.lowerAngle = (0f)-(thigh1.getAngle()-calf1.getAngle());
    knee1.enableMotor = true;
    knee1.maxMotorTorque = 3000*torqueScale;
    knee1.motorSpeed = 0f;
    
    k1 = (RevoluteJoint)getWorld().createJoint(knee1);
    
    knee2 = new RevoluteJointDef();
    knee2.initialize(calf2,thigh2, calf2.getWorldPoint(new Vec2(0,thighLength)));
    knee2.enableLimit = true;
    knee2.upperAngle = (1.6f)-(thigh2.getAngle()-calf2.getAngle());
    knee2.lowerAngle = (0f)-(thigh2.getAngle()-calf2.getAngle());
    knee2.enableMotor = true;
    knee2.maxMotorTorque = 3000*torqueScale;
    knee2.motorSpeed = 0f;
    
    k2 = (RevoluteJoint)getWorld().createJoint(knee2);
    
    //Hip1 (hip to thigh1)
    hipJoint1 = new RevoluteJointDef();
    hipJoint1.initialize(torso, thigh1, thigh1.getWorldPoint(new Vec2(0,thighLength)));
    hipJoint1.enableLimit = true;
    hipJoint1.upperAngle = (float)(0.9)+(torso.getAngle()-thigh1.getAngle());
    hipJoint1.lowerAngle = (float)(-1.1)+(torso.getAngle()-thigh1.getAngle());
    hipJoint1.enableMotor = true;
    hipJoint1.motorSpeed = 0;
    hipJoint1.maxMotorTorque = 6000f*torqueScale;
    
    hj1 = (RevoluteJoint)getWorld().createJoint(hipJoint1);
    
    //Hip2
    hipJoint2 = new RevoluteJointDef();
    hipJoint2.initialize(torso, thigh2, thigh1.getWorldPoint(new Vec2(0,thighLength)));
    hipJoint2.enableLimit = true;
    hipJoint2.upperAngle = (float)(0.9)+(torso.getAngle()-thigh2.getAngle());
    hipJoint2.lowerAngle = (float)(-1.1)+(torso.getAngle()-thigh2.getAngle());
    hipJoint2.enableMotor = true;
    hipJoint2.motorSpeed = 0f;
    hipJoint2.maxMotorTorque = 6000f*torqueScale;
    
    hj2 = (RevoluteJoint)getWorld().createJoint(hipJoint2);
    
    //Neck Joint
    neckJoint = new RevoluteJointDef();
    neckJoint.initialize(torso, head, torso.getWorldPoint(new Vec2(0,torsoLength)));
    neckJoint.enableLimit = true;
    neckJoint.upperAngle = (float)(0.25)-(head.getAngle()-torso.getAngle());
    neckJoint.lowerAngle = (float)(-0.25)-(head.getAngle()-torso.getAngle());
    neckJoint.enableMotor = true;
    neckJoint.maxMotorTorque = 0*torqueScale;
    neckJoint.motorSpeed = maxNeckSpeed;    
    nj = (RevoluteJoint)getWorld().createJoint(neckJoint);
    
    //Arm Joints:
    armJoint1 = new RevoluteJointDef();
    armJoint1.initialize(torso, arm1, torso.getWorldPoint(new Vec2(0,0.8f*torsoLength)));
    armJoint1.enableLimit = true;
    armJoint1.upperAngle = (float)(Math.PI/2);
    armJoint1.lowerAngle = (float)(-2+Math.PI/2);
    armJoint1.enableMotor = true;
    armJoint1.maxMotorTorque = 1000*torqueScale;
    armJoint1.motorSpeed = 0f;
    aj1 = (RevoluteJoint)getWorld().createJoint(armJoint1);
    
    armJoint2 = new RevoluteJointDef();
    armJoint2.initialize(torso, arm2, torso.getWorldPoint(new Vec2(0,0.8f*torsoLength)));
    armJoint2.enableLimit = true;
    armJoint2.upperAngle = (float)(0);
    armJoint2.lowerAngle = (float)(-2);
    armJoint2.enableMotor = true;
    armJoint2.maxMotorTorque = 1000*torqueScale;
    armJoint2.motorSpeed = 0f;
    aj2 = (RevoluteJoint)getWorld().createJoint(armJoint2);
    
  }
  
//  @Override
//  public void processJoint(Joint argJoint, Long argTag) {
//	    argJoint.getBodyA().applyAngularImpulse(-100000);
//  }
  
  @Override
  public void keyPressed(char argKeyChar, int argKeyCode) {
    switch (argKeyChar) {
      case 'w':
        hj1.setMotorSpeed(2.5f);
        hj2.setMotorSpeed(-2.5f);
        
        aj1.setMotorSpeed(-armSpeed);
        aj2.setMotorSpeed(armSpeed);
        wOn = true;
        break;
      case 'q':

          hj1.setMotorSpeed(-2.5f);
          hj2.setMotorSpeed(2.5f);
          aj1.setMotorSpeed(armSpeed);
          aj2.setMotorSpeed(-armSpeed);
          qOn = true;
        break;

      case 'o':

          k1.setMotorSpeed(2.5f);
          k2.setMotorSpeed(-2.5f);
          hj1.setLimits(hipJoint1.lowerAngle-0.5f, hipJoint1.upperAngle+0.5f); //upper leg LIMITS change with the lower leg movement. When calf is back, thigh may be further up. When calf is extended, thigh can't be as high (relative to torso).
          hj2.setLimits(hipJoint2.lowerAngle, hipJoint2.upperAngle);
          oOn = true;
        break;

      case 'p':

          k1.setMotorSpeed(-2.5f);
          k2.setMotorSpeed(2.5f);
          hj1.setLimits(hipJoint1.lowerAngle, hipJoint1.upperAngle);
          hj2.setLimits(hipJoint2.lowerAngle-0.5f, hipJoint2.upperAngle+0.5f);
          pOn = true;
        break;
    }
  }

  @Override
  public void keyReleased(char argKeyChar, int argKeyCode) {
	    switch (argKeyChar) {
	      case 'q':
	          hj1.setMotorSpeed(0f);
	          hj2.setMotorSpeed(0f);
	          qOn = false;
	        break;

	      case 'w':
	          hj1.setMotorSpeed(0f);
	          hj2.setMotorSpeed(0f);
	          wOn = false;

	      case 'o':
	          k1.setMotorSpeed(0f);
	          k2.setMotorSpeed(0f);
	          oOn = false;
	        break;

	      case 'p':
	          k1.setMotorSpeed(0f);
	          k2.setMotorSpeed(0f);
	          pOn = false;
	        break;
	    }
	  }
 

  @Override
  public String getTestName() {
    return "QWOP Clone";
  }
  
  
  @Override //This function lets me alter stuff before the step is called in the main simulation loop
  public void mattsInput(World world){
	  //This effectively adds a spring at the neck by altering the motor properties.
	  RevoluteJoint Jrev;
	  Joint J = world.getJointList();
	  for (int i = 0; i < world.getJointCount(); i++){
		  if (J.getBodyB().equals(head)){
			  Jrev = (RevoluteJoint)J;
			  float ang = Jrev.getJointAngle();
			  float neckTorque = neckStiff*(neckEquil-ang) - neckDamp*Jrev.getJointSpeed();
			  Jrev.setMotorSpeed(maxNeckSpeed*Math.signum(neckTorque)); //Reverse motor direction if the torque is negative.
			  Jrev.setMaxMotorTorque(Math.abs(neckTorque)*torqueScale);
		  }
		  J = J.getNext();
	  }
	  
	  //This gives the coupled motion of the feet. If the ankle joint behind the knee joint, then apply positive motor motion, otherwise, negative.
	  Vec2 apos1 = new Vec2(0,0);
	  Vec2 apos2 = new Vec2(0,0);
	  a1.getAnchorB(apos1);
	  a2.getAnchorB(apos2);
	  
	  Vec2 kpos1 = new Vec2(0,0);
	  Vec2 kpos2 = new Vec2(0,0);
	  
	  k1.getAnchorA(kpos1);
	  k2.getAnchorB(kpos2);
	  
	  if (apos1.x < kpos1.x){
		  a1.setMotorSpeed(2f);
	  }else{
		  a1.setMotorSpeed(-2f);
	  }
	  
	  if (apos2.x < kpos2.x){
		  a2.setMotorSpeed(2f);
	  }else{
		  a2.setMotorSpeed(-2f);
	  }
	  
	  //camera moves in x with the torso com
	  float camCenterX = torso.getPosition().x;
	getCamera().setCamera( new Vec2(camCenterX,20),4f);

	
	//Place markers so we can tell how fast the guy is moving.
	Vec2 extents = getDebugDraw().getViewportTranform().getExtents().clone();
	
	float markerSpacing = 100f;
	
	int numMarker = (int)Math.floor(extents.x/markerSpacing);
	float firstPt = (float)Math.floor((camCenterX-extents.x/2f)/markerSpacing)*markerSpacing + markerSpacing;

	for (int i = 0; i<numMarker; i++){	
		float ptPlaceWorld = firstPt + i*markerSpacing;
	  
	    for (int j = 0; j<tree.length; j++){
	    	treePlace[j].set(tree[j].x + ptPlaceWorld,tree[j].y);
	    	
	    }
	    	    
	    getDebugDraw().drawPolygon(treePlace, 11, new Color3f(0.9f,0.2f,0.1f));
	}
	
	
	  //Reserved for other springs
	  
  }
 
}
