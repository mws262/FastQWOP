package org.jbox2d.testbed.framework.jogl;

import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.RevoluteJoint;

public class Frame extends JFrame{

	public Drawer plot;
	public Frame() {
		  //GET GRAPHICS UP AND RUNNING:
		  JPanel panel = new JPanel();
		  JFrame frame = new JFrame();
		  plot = new Drawer();
		  setContentPane(plot);
		  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  setSize(1000,1000);
		  setVisible(true);
	}

}


class Drawer extends JPanel{
	public ArrayList<Vec2> CircleCenters= new ArrayList<Vec2>();
	public ArrayList<Float> CircleRadii= new ArrayList<Float>();
	
	public ArrayList<Vec2[]> SegmentVerts = new ArrayList<Vec2[]>();
	public ArrayList<Vec2[]> PolyVerts = new ArrayList<Vec2[]>();
	
	public Drawer(){
		
	}
    public synchronized void paintComponent(Graphics g){
//    	super.paintComponent(g);
    	float scaling = 10f;
    	//Handle all circle drawing.
    	for (int i = 0; i<CircleCenters.size(); i++){
    		g.drawOval((int)(scaling*(CircleCenters.get(i).x-CircleRadii.get(i))), (int)(scaling*(CircleCenters.get(i).y-CircleRadii.get(i))), (int)(2*CircleRadii.get(i)*scaling), (int)(2*CircleRadii.get(i)*scaling)); //Ovals are specified by the top left corner xy and the height width (ie both are diameter for a circle)
    	}
    	CircleCenters.clear();
    	CircleRadii.clear();
    	int p = 0;
    	for (Vec2[] pts: PolyVerts){
    		
    		int[] xpts = new int[pts.length];
    		int[] ypts = new int[pts.length];
    		
    		for(int i = 0; i<pts.length; i++){
    			xpts[i] = (int)(scaling*pts[i].x);
    			ypts[i] = (int)(scaling*pts[i].y);	
    		}
    		g.drawPolygon(xpts, ypts, pts.length);
    		p++;
    		if(p>100){
    			break;
    		}
    	}
    	PolyVerts.clear();
    	System.out.println(p);
    	
    	
	}
    public synchronized void addCircle(Vec2 newCenter, float newRadius){
    	CircleCenters.add(newCenter);
    	CircleRadii.add(newRadius);	
    }
    public synchronized void addSegment(Vec2 pt1, Vec2 pt2){
    	Vec2[] seg = new Vec2[2];
    	seg[0] = pt1;
    	seg[1] = pt2;
    	
    	SegmentVerts.add(seg);
    	
    }
    
    public synchronized void addPoly(Vec2[] pts){
    	
    	PolyVerts.add(pts);
    	
    }
    
 }