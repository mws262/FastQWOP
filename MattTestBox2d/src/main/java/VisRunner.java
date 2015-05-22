import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

class VisRunner extends JFrame{
	private World world;
	private DrawPane pane;
	
	
	public VisRunner(World world){
		this.world = world;
		JPanel panel = new JPanel();
		JFrame frame = new JFrame();
		DrawPane pane = new DrawPane(world);
		
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setPreferredSize(new Dimension(OptionsHolder.windowWidth, OptionsHolder.windowHeight));
        this.setContentPane(pane);
        this.pack();
        this.setVisible(true); 
	}
	
	/** Give the visualizer a new world to work with. Saves creating too many new objects **/
	public void SwitchWorlds(World world){
		this.world = world;
//		pane.SwitchWorlds(world);
	}
	public void Repaint(){
		pane.repaint();
	}
	
}
//Graphics stuff
class DrawPane extends JPanel{
	World world;
	Vec2 a = new Vec2(0,0);
	Vec2 b = new Vec2(0,0);
	Vec2 c = new Vec2(0,0);
	float scaling = 10f;
	int offsetx0 = 500;
	int offsetx = offsetx0;
	int offsety = 800;
	float flipud = 1f;
	float fliplr = 1f;
	
	OptionsHolder options;
	
	int headpos = 0;
	JCheckBox visOn;
	CheckBoxListener optionsList;
	public DrawPane(World world){
		this.world = world;
		visOn = new JCheckBox("Vis on?");
		this.add(visOn);
		visOn.setSelected(false);
		optionsList = new CheckBoxListener();
		visOn.addItemListener(optionsList);
	}
	
	/** Give the visualizer a new world to work with. Saves creating too many new objects **/
	public void SwitchWorlds(World world){
		this.world = world;
	}
	/** Main graphics for the runner! **/
    public void paintComponent(Graphics g){
    	super.paintComponent(g);
  	  
    	Body newbody = world.getBodyList();
    	while (newbody != null){

	    	Fixture newfixture = newbody.getFixtureList();
	    	
	    	while(newfixture != null){
	    		if(newfixture.getType() == ShapeType.POLYGON){

			    	PolygonShape newshape = (PolygonShape)newfixture.getShape();
			    	
			    	for (int k = 0; k<newshape.getVertexCount(); k++){
			    		
			    		Transform trans = newbody.getTransform();
			    		
			    		a = Transform.mul(trans, newshape.getVertex(k));
			    		b = Transform.mul(trans, newshape.getVertex((k+1) % (newshape.getVertexCount())));
			    		
			    		g.drawLine((int)(fliplr*scaling*a.x)+offsetx, (int)(flipud*scaling*a.y)+offsety, (int)(fliplr*scaling*b.x)+offsetx, (int)(flipud*scaling*b.y)+offsety);			    		
			    	}
			    	
	    		}else if (newfixture.getType() == ShapeType.CIRCLE){
	    			CircleShape newshape = (CircleShape)newfixture.getShape();
	    			headpos = (int)(-fliplr*scaling*newbody.getWorldCenter().x);
	    			g.drawOval((int)(fliplr*scaling*(newbody.getWorldCenter().x-fliplr*newshape.getRadius())+offsetx), (int)(flipud*scaling*(newbody.getWorldCenter().y-flipud*newshape.getRadius())+offsety), (int)(scaling*newshape.getRadius()*2), (int)(scaling*newshape.getRadius()*2));
	    			
	    			
	    		}else if (newfixture.getType() == ShapeType.EDGE){
	    			
	    			EdgeShape newshape = (EdgeShape)newfixture.getShape();
		    		Transform trans = newbody.getTransform();

		    		a = Transform.mul(trans, newshape.m_vertex1);
		    		b = Transform.mul(trans, newshape.m_vertex2);
		    		c = Transform.mul(trans, newshape.m_vertex2);
		    		
		    		g.drawLine((int)(fliplr*scaling*a.x)+offsetx, (int)(flipud*scaling*a.y)+offsety, (int)(fliplr*scaling*b.x)+offsetx, (int)(flipud*scaling*b.y)+offsety);			    		
		    		g.drawLine((int)(fliplr*scaling*a.x)+offsetx, (int)(flipud*scaling*a.y)+offsety, (int)(fliplr*scaling*c.x)+offsetx, (int)(flipud*scaling*c.y)+offsety);			    		

	    		}
	    		newfixture = newfixture.getNext();
	    		
	    	}
	    	
	    	newbody = newbody.getNext();
    	}
    	
    	offsetx = headpos + offsetx0;
    }
}
//TODO fixit
class CheckBoxListener implements ItemListener {
	public CheckBoxListener(){
	}
    public void itemStateChanged(ItemEvent e) {

//        Object source = e.getSource();


        if (e.getStateChange() == ItemEvent.DESELECTED){
//        	OptionsHolder.visOn = false;
        	
        }else if (e.getStateChange() == ItemEvent.SELECTED){
//        	OptionsHolder.visOn = true;
        }
    }
}
