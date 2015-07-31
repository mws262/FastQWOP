package TreeQWOP;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
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

import java.awt.event.MouseListener;


public class RunnerPaneMaker implements Schedulable,TabbedPaneActivator{

	//For scheduling.
	private int interval = 1;
	private boolean tabActive = false;
	
	/** Most important. QWOPInterface stays the same although worlds are often destroyed **/
	private QWOPInterface QWOPHandler;
	private World world;
	
	public RunnerPane RunPanel;

	
	public boolean disable = true;
	
	
	/* stuff for controlling the timestep */
//	public float k = 1f;
//	public float d = 10;
//	public long oldTime = 0;
//	public long newTime = 0;
	
	public float timestep = 0.85f*1000*OptionsHolder.timestep; // adjusted down to allow for computation. had added control to this, but turned out better to simply offset statically.
	

	public RunnerPaneMaker(QWOPInterface QWOPHandler) {
		this.QWOPHandler = QWOPHandler;
		RunPanel = new RunnerPane(QWOPHandler,this);
//		oldTime = System.currentTimeMillis();
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

//			newTime = System.currentTimeMillis();
//			timestep += k*(OptionsHolder.timestep*1000f - (float)(newTime - oldTime));
//			System.out.println(timestep);
//			System.out.println((newTime - oldTime));
//			oldTime = newTime;

//			if(timestep>(3*OptionsHolder.timestep*1000)){
//				timestep = (3*OptionsHolder.timestep*1000);
//			}else if (timestep<(0.25f*OptionsHolder.timestep*1000)){
//				timestep=(0.25f*OptionsHolder.timestep*1000);
//			}

			try {
				
				Thread.sleep((long)(timestep));
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
		QWOPHandler.manualOverride = false;
		RunPanel.requestFocus();
		
	}

	@Override
	public void DeactivateTab() {
		tabActive = false;
		QWOPHandler.manualOverride = false;
		
	}
	
	/** Tell us whether this tab is active **/
	public boolean isActiveTab() {
		return tabActive;
	}

}

class RunnerPane extends JPanel implements KeyListener,MouseListener{
	private World world;
	
	private Font QWOPLittle = new Font("Ariel", Font.BOLD,21);
	private Font QWOPBig = new Font("Ariel", Font.BOLD,28);
	Vec2 a = new Vec2(0,0);
	Vec2 b = new Vec2(0,0);
	Vec2 c = new Vec2(0,0);
	float scaling = 10f;
	int offsetx0 = 250;
	int offsetx = offsetx0;
	int offsety = 800;
	float flipud = 1f;
	float fliplr = 1f;
	
	
	public int prefixLength = 8;
	public int periodicLength = 4;
	
	//Override for manual control parameters:
	boolean manualControl = false;
	boolean Q = false;
	boolean W = false;
	boolean O = false;
	boolean P = false;
	
	Font bigFont = new Font("Ariel", Font.BOLD, 16);
	Font littleFont = new Font("Ariel", Font.PLAIN, 12);
	int vertTextSpacing = 18;
	int vertTextAnchor = 50;
	OptionsHolder options;
	QWOPInterface QWOPHandler;
	RunnerPaneMaker runmaker;
	int headpos = 0;
	DecimalFormat dc = new DecimalFormat("#.#");
	
	String prefixLabel = "";
	String periodicLabel = "";
	ArrayList<String> deviationLabel;
	public void setLabel(String prefixLabel, String periodicLabel, ArrayList<String> deviationLabel){
		this.prefixLabel = prefixLabel;
//		if(OptionsHolder.goDeviations){ //If we're doing it in terms of deviations, then keep these separate.
			this.periodicLabel = periodicLabel;
			this.deviationLabel = deviationLabel;
//		}else{ //Otherwise we throw them together since saying deviation isn't really accurate.
//			this.periodicLabel = periodicLabel;
//			for(String s: deviationLabel){
//				this.periodicLabel += s;
//			}
//		}
	}
	public RunnerPane(QWOPInterface QWOPHandler,RunnerPaneMaker runmaker){
		this.QWOPHandler = QWOPHandler;
		this.runmaker = runmaker;
	  	  addKeyListener(this);
	  	  addMouseListener(this);
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
//    	world = QWOPHandler.getWorld();
    	super.paintComponent(g);

    	//Other Labels:
		//Action sequence label
		g.setFont(bigFont);
		g.setColor(Color.BLACK);
		if(prefixLabel != ""){ //Display the sequence of actions divided into prefix and periodic and deviations.
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
	    	g.setFont(littleFont);
			g.drawString("R-key overrides to manual control.", 490,110);
			g.setFont(bigFont);
		}else if(QWOPHandler.manualOverride){
			g.drawString("Manual keyboard override.", 10,50);
		}else{
			g.drawString("Going through the tree.", 10,50);
			g.setFont(littleFont);
			g.drawString("R-key overrides to manual control.", 490,110);
			g.setFont(bigFont);
		}
		
		//This mess adds underlines under the currently executing action.
		if(prefixLabel != ""){
			int index = QWOPHandler.currentIndex;
			if (index < prefixLength){
				g.drawLine(index*32, vertTextAnchor + vertTextSpacing+1, 25+index*32, vertTextAnchor + vertTextSpacing+1);
				g.drawLine(index*32, vertTextAnchor + vertTextSpacing+2, 25+index*32, vertTextAnchor + vertTextSpacing+2);
			}else{
				int depth = (index-prefixLength)/periodicLength+2;
				index = (index-(prefixLength))%periodicLength;
				g.drawLine(index*32, vertTextAnchor + depth*vertTextSpacing+1, 25+index*32, vertTextAnchor + depth*vertTextSpacing+1);
				g.drawLine(index*32, vertTextAnchor + depth*vertTextSpacing+2, 25+index*32, vertTextAnchor + depth*vertTextSpacing+2);
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
	    		for(int i = 0; i<10; i++){
		    		g.drawString("_",((offsetx-offsetx0-i*70)%this.getWidth())+this.getWidth(),900);
	    		}

	    	}
	    	
	    	newbody = newbody.getNext();
    	}
//    	g.drawString(dc.format(-(headpos+30)/40.) + " metres", 500, 110);
    	keyDrawer(g, QWOPHandler.Q,QWOPHandler.W,QWOPHandler.O,QWOPHandler.P);
    	offsetx = headpos + offsetx0;
    }

    }
    
    public void keyDrawer(Graphics g, boolean q, boolean w, boolean o, boolean p){
    	int qOffset = (q ? 10:0);
    	int wOffset = (w ? 10:0);
    	int oOffset = (o ? 10:0);
    	int pOffset = (p ? 10:0);
    	
    	int startX = 300;
    	int startY = 30;
    	int size = 50;
    	Font activeFont;
    	FontMetrics fm;
    	Graphics2D g2 = (Graphics2D)g;
    	
    	g2.setColor(Color.DARK_GRAY);
    	g2.drawRoundRect(startX + 80 - qOffset/2, startY - qOffset/2, size + qOffset, size + qOffset, (size + qOffset)/10, (size + qOffset)/10);
    	g2.drawRoundRect(startX + 160 - wOffset/2, startY - wOffset/2, size + wOffset, size + wOffset, (size + wOffset)/10, (size + wOffset)/10);
    	g2.drawRoundRect(startX + 240 - oOffset/2, startY - oOffset/2, size + oOffset, size + oOffset, (size + oOffset)/10, (size + oOffset)/10);
    	g2.drawRoundRect(startX + 320 - pOffset/2, startY - pOffset/2, size + pOffset, size + pOffset, (size + pOffset)/10, (size + pOffset)/10);
    	
    	g2.setColor(Color.LIGHT_GRAY);
    	g2.fillRoundRect(startX + 80 - qOffset/2, startY - qOffset/2, size + qOffset, size + qOffset, (size + qOffset)/10, (size + qOffset)/10);
    	g2.fillRoundRect(startX + 160 - wOffset/2, startY - wOffset/2, size + wOffset, size + wOffset, (size + wOffset)/10, (size + wOffset)/10);
    	g2.fillRoundRect(startX + 240 - oOffset/2, startY - oOffset/2, size + oOffset, size + oOffset, (size + oOffset)/10, (size + oOffset)/10);
    	g2.fillRoundRect(startX + 320 - pOffset/2, startY - pOffset/2, size + pOffset, size + pOffset, (size + pOffset)/10, (size + pOffset)/10);
    	
    	g2.setColor(Color.BLACK);
    	
    	//Used for making sure text stays centered.
    	
    	activeFont = q ? QWOPBig:QWOPLittle;
    	g2.setFont(activeFont);
    	fm = g2.getFontMetrics();
    	g2.drawString("Q", startX + 80 + size/2-fm.stringWidth("Q")/2, startY + size/2+fm.getHeight()/3);
    	
    	
    	activeFont = w ? QWOPBig:QWOPLittle;
    	g2.setFont(activeFont);
    	fm = g2.getFontMetrics();
    	g2.drawString("W", startX + 160 + size/2-fm.stringWidth("W")/2, startY + size/2+fm.getHeight()/3);
    	
    	activeFont = o ? QWOPBig:QWOPLittle;
    	g2.setFont(activeFont);
    	fm = g2.getFontMetrics();
    	g2.drawString("O", startX + 240 + size/2-fm.stringWidth("O")/2, startY + size/2+fm.getHeight()/3);
    	
    	activeFont = p ? QWOPBig:QWOPLittle;
    	g2.setFont(activeFont);
    	fm = g2.getFontMetrics();
    	g2.drawString("P", startX + 320 + size/2-fm.stringWidth("P")/2, startY + size/2+fm.getHeight()/3);
    	
    }
    
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyChar() == 'q'){
			Q = true;
			QWOPHandler.Q = Q;
		}
		if(arg0.getKeyChar() == 'w'){
			W = true;
			QWOPHandler.W = W;
		}
		if(arg0.getKeyChar() == 'o'){
			O = true;
			QWOPHandler.O = O;
		}
		if(arg0.getKeyChar() == 'p'){
			P = true;
			QWOPHandler.P = P;
		}

		if(arg0.getKeyChar() == 'r'){
			if(!runmaker.disable){
				QWOPHandler.InstaFail();
			}
			//Unflag so we don't have residual commands between switching from manual to auto or vice versa 
			QWOPHandler.Q = false;
			QWOPHandler.W = false;
			QWOPHandler.O = false;
			QWOPHandler.P = false;
			QWOPHandler.manualOverride = !QWOPHandler.manualOverride;

		}
		
	}
	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyChar() == 'q'){
			Q = false;
			QWOPHandler.Q = Q;
		}
		if(arg0.getKeyChar() == 'w'){
			W = false;
			QWOPHandler.W = W;
		}
		if(arg0.getKeyChar() == 'o'){
			O = false;
			QWOPHandler.O = O;
		}
		if(arg0.getKeyChar() == 'p'){
			P = false;
			QWOPHandler.P = P;
		}
		
	}
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		this.requestFocus(); //Mouse position now decides whether the run pane or the tree pane has focus. There is a corresponding requestfocus in treepanemaker
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
	
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
