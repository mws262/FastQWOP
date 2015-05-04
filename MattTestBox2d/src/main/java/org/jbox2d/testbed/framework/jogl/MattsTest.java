/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.testbed.framework.jogl;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.testbed.framework.TestbedTest;

public class MattsTest extends TestbedTest {
  private final boolean switchBodiesInJoint;

  public MattsTest(boolean switchBodiesInJoint) {
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
    Body ft1;
    Body ft2;
    Body ground;
    Body leg1;
    Body leg2;

    {
      CircleShape circleShape = new CircleShape();
      circleShape.m_radius = 4;
      Shape shape = circleShape;
      

      

      BodyDef bodyDef = new BodyDef();
      bodyDef.type = BodyType.DYNAMIC;
      bodyDef.position.set(-20, 8);
      bodyDef.allowSleep = false;
      ft1 = getWorld().createBody(bodyDef);
      ft1.createFixture(shape, 1);
      bodyDef.position.set(20,12);
      ft2 = getWorld().createBody(bodyDef);
      ft2.createFixture(shape,1);
      
      
      PolygonShape leg = new PolygonShape();
      leg.setAsBox(10f, 1f);
      
      bodyDef.position.set(-10, 10);
      leg1 = getWorld().createBody(bodyDef);
      leg1.createFixture(leg,1);
      bodyDef.position.set(10, 10);
      leg2 = getWorld().createBody(bodyDef);
      leg2.createFixture(leg,1);
      
      
      
    }

    {
      BodyDef bodyDef = new BodyDef();
      bodyDef.type = BodyType.STATIC;
      ground = getWorld().createBody(bodyDef);
    }

    RevoluteJointDef jointDef = new RevoluteJointDef();
//    jointDef.localAnchorA = new Vec2(10, 0);
//    jointDef.localAnchorB = new Vec2(10,0);
    
    jointDef.initialize(leg1,leg2, new Vec2(0,10));

    getWorld().createJoint(jointDef);
    
    
    RevoluteJointDef footjt1 = new RevoluteJointDef();
    footjt1.localAnchorA = new Vec2(-10, 0);
    footjt1.localAnchorB = new Vec2(1,0);
    footjt1.initialize(leg1, ft1, leg1.getWorldPoint(new Vec2(-10,0)));
    getWorld().createJoint(footjt1);
    
    
    RevoluteJointDef footjt2 = new RevoluteJointDef();
    footjt2.localAnchorA = new Vec2(-10, 0);
    footjt2.localAnchorB = new Vec2(1,0);
    footjt2.initialize(leg2, ft2, leg2.getWorldPoint(new Vec2(10,0)));
    getWorld().createJoint(footjt2);
    
    
 
  }

  @Override
  public String getTestName() {
    return "Fixed Pendulum " + (switchBodiesInJoint ? "1" : "0");
  }
  

  }
