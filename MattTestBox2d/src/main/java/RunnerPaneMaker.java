import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

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
	
	public boolean disable = true;

	public RunnerPaneMaker(QWOPInterface QWOPHandler) {
		this.QWOPHandler = QWOPHandler;
		RunPanel = new RunnerPane(QWOPHandler);
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
		if(tabActive && !disable){ //Skip scheduled updates if this tab isn't in focus.
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
	
	/** Tell us whether this tab is active **/
	public boolean isActiveTab() {
		return tabActive;
	}

}

class RunnerPane extends JPanel{
	private World world;
	
	Vec2 a = new Vec2(0,0);
	Vec2 b = new Vec2(0,0);
	Vec2 c = new Vec2(0,0);
	float scaling = 10f;
	int offsetx0 = 250;
	int offsetx = offsetx0;
	int offsety = 800;
	float flipud = 1f;
	float fliplr = 1f;
	
	Font bigFont = new Font("Ariel", Font.BOLD, 16);
	int vertTextSpacing = 18;
	int vertTextAnchor = 50;
	OptionsHolder options;
	QWOPInterface QWOPHandler;
	
	int headpos = 0;

	String prefixLabel = "";
	String periodicLabel = "";
	ArrayList<String> deviationLabel;
	public void setLabel(String prefixLabel, String periodicLabel, ArrayList<String> deviationLabel){
		this.prefixLabel = prefixLabel;
		if(OptionsHolder.goDeviations){ //If we're doing it in terms of deviations, then keep these separate.
			this.periodicLabel = periodicLabel;
			this.deviationLabel = deviationLabel;
		}else{ //Otherwise we throw them together since saying deviation isn't really accurate.
			this.periodicLabel = periodicLabel;
			for(String s: deviationLabel){
				this.periodicLabel += s;
			}
		}
	}
	public RunnerPane(QWOPInterface QWOPHandler){
		this.QWOPHandler = QWOPHandler;
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

    	//Other Labels:
		//Action sequence label
		g.setFont(bigFont);
		g.setColor(Color.BLACK);
		if(prefixLabel != ""){
	    	g.drawString("Running selected sequence: ", 10,vertTextAnchor);
	    	g.setColor(Color.RED);
	    	g.drawString(prefixLabel, 10, vertTextAnchor + vertTextSpacing);
	    	g.setColor(Color.GREEN);
	    	g.drawString(periodicLabel, 10, vertTextAnchor + vertTextSpacing*2);
	    	g.setColor(Color.ORANGE);
	    	for (int i = 0; i<deviationLabel.size(); i++){
		    	g.drawString(deviationLabel.get(i), 10, vertTextAnchor + vertTextSpacing*(3+i));
	    	}
	    	g.setColor(Color.BLACK);
		}else{
			g.drawString("Going through the tree.", 10,50);
		}
		
		//This mess adds underlines under the currently executing action.
		if(prefixLabel != ""){
			int index = QWOPHandler.currentIndex;
			if (index < OptionsHolder.prefixLength){
				g.drawLine(index*30, vertTextAnchor + vertTextSpacing+1, 25+index*30, vertTextAnchor + vertTextSpacing+1);
				g.drawLine(index*30, vertTextAnchor + vertTextSpacing+2, 25+index*30, vertTextAnchor + vertTextSpacing+2);
			}else{
				int depth = index/4;
				index = (index-(OptionsHolder.prefixLength))%4;
				g.drawLine(index*30, vertTextAnchor + depth*vertTextSpacing+1, 25+index*30, vertTextAnchor + depth*vertTextSpacing+1);
				g.drawLine(index*30, vertTextAnchor + depth*vertTextSpacing+2, 25+index*30, vertTextAnchor + depth*vertTextSpacing+2);
			}
		}
    	
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
