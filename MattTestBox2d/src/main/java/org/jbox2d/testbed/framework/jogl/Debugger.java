package org.jbox2d.testbed.framework.jogl;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;

public class Debugger extends DebugDraw{
	
	public Frame window;
	
	public Debugger() {
		window = new Frame();
	}
	public synchronized void update(){
		
		window.repaint();
	}

	@Override
	public synchronized void drawPoint(Vec2 argPoint, float argRadiusOnScreen,
			Color3f argColor) {
		// TODO Auto-generated method stub
		System.out.println('w');
		
		
	}

	@Override
	public synchronized void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
//		drawPolygon( vertices,  vertexCount,  color);
		window.plot.addPoly(vertices);
		
	}

	@Override
	public synchronized void drawCircle(Vec2 center, float radius, Color3f color) {
		window.plot.addCircle(center, radius);
		
	}

	@Override
	public synchronized void drawSolidCircle(Vec2 center, float radius, Vec2 axis,
			Color3f color) {
		window.plot.addCircle(center, radius);
		
		
	}

	@Override
	public synchronized void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
//		window.plot.addSegment(p1, p2);
//		System.out.println('d');
		
	}

	@Override
	public synchronized void drawTransform(Transform xf) {
		// TODO Auto-generated method stub
		System.out.println('q');
		
		
	}

	@Override
	public void drawString(float x, float y, String s, Color3f color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawParticles(Vec2[] centers, float radius,
			ParticleColor[] colors, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawParticlesWireframe(Vec2[] centers, float radius,
			ParticleColor[] colors, int count) {
		// TODO Auto-generated method stub
		
	}

}

