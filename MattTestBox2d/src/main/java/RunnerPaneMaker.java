import java.awt.Graphics;

import javax.swing.JCheckBox;
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


public class RunnerPaneMaker implements Schedulable,TabbedPaneActivator{

	//For scheduling.
	private int interval = 1;
	private boolean tabActive = false;
	
	/** Most important. QWOPInterface stays the same although worlds are often destroyed **/
	private QWOPInterface QWOPHandler;
	private World world;
	
	public RunnerPane RunPanel;

	public RunnerPaneMaker(QWOPInterface QWOPHandler) {
		this.QWOPHandler = QWOPHandler;
		RunPanel = new RunnerPane();
	}
	
	public void update(){
		if ( QWOPHandler.getWorld() != null){ //Only do something if there's a world inside the QWOPInterface
			if ( RunPanel.getWorld() != QWOPHandler.getWorld()){ //Pass down a new world if we've reset since the last display
				RunPanel.setWorld(QWOPHandler.getWorld());
			}
			RunPanel.repaint();
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
		if(tabActive){ //Skip scheduled updates if this tab isn't in focus.
			update();
			try {
				Thread.sleep((long)(OptionsHolder.timestep*1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void DoEvery() {
		// TODO Auto-generated method stub
		
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
		tabActive = true;
		
	}

	@Override
	public void DeactivateTab() {
		tabActive = false;
		
	}

}

class RunnerPane extends JPanel{
	private World world;
	
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
	public RunnerPane(){

	}
	
	/** Give the visualizer a new world to work with. Saves creating too many new objects **/
	public void setWorld(World world){
		this.world = world;
	}
	public World getWorld(){
		return world;
	}
	
	/** Main graphics for the runner! **/
    public void paintComponent(Graphics g){
    	super.paintComponent(g);
    	if(world != null){
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
}
