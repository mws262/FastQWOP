package TreeQWOP;
import java.util.Arrays;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import TreeQWOP.QWOPInterface.stanceType;


public class QWOPInterface {

	private World m_world;
	public QWOPGame game;
	
	//Accounting for a single run:
	int sequencePosition = 1; //Start with action 1, increment..
	
	public final float timestep = OptionsHolder.timestep;
	private final int positerations = OptionsHolder.positerations;
	private final int veliterations = OptionsHolder.veliterations;
	private final int delaymillis = (int)(timestep*1000);
	
	private int[] currentSequence = new int[50]; //Arbitrarily larger than needed.
	public int currentIndex = 0; //counts number of actions taken in this run.
	
	public int stepsInRun = 0; //Always counts up until a game reset. Helps ensure we know when we're doing something periodic.
	
	public boolean repeatSequence = false;
	public int prefixLength = 12; //How many elements lead up to the repeated portion.
	public int periodicLength = 4; // How many elements are the repeated portion.
	
	public boolean visOn = false;
	
	private boolean failFlag = false;
	private Scheduler StepSched;
	
	/** Keep track of which feet are in contact with the ground. **/
	public boolean RFootDown = true;
	public boolean LFootDown = true;
	
	/** Do we want manual external keyboard control **/
	public boolean manualOverride = false;
	
	/** flags for each of the QWOP keys being down **/
	public boolean Q = false;
	public boolean W = false;
	public boolean O = false;
	public boolean P = false;
	
	/** The fixed sequence of QWOP to be cycled through **/
	public boolean[][] seq = {
		{false,false,false,false},
		{false,true,true,false},
		{false,false,false,false},
		{true,false,false,true}}; 
	public ControlSequence sequence = new ControlSequence(seq);
	
	/** Type of stance that occurs when executing the action of this node. I'm using this to try to eliminate double stance and very long flight phases. **/
	public enum stanceType{
		unknown,
		flightPhase,
		singleStance,
		doubleStance
	}
	
	/** What is the current runner stance? **/
	public stanceType currentStance = stanceType.doubleStance;
	/** What portion of the current command is spent in each stance? **/	
	public int flightCount = 0;
	public int doubleCount = 0;
	public int singleCount = 0;
	
	/** Summed body height over one action. generally wish to minimize this **/
	public float sumBodyHeight = 0;
	
	public QWOPInterface() {
	}
	
	/* Make a new game */
	public void NewGame(boolean visOn){
		game = new QWOPGame();
		this.visOn = visOn;
		game.Setup(visOn);
		m_world = game.getWorld();
		sequencePosition = 1; //Reset to the beginning of the predefined sequence of actions.
		currentIndex = 0;
		Arrays.fill(currentSequence, 0);
		stepsInRun = 0;
		failFlag = false; //Reset from previous failures.
		sequence.reset();
		m_world.setContactListener(new CollisionListener(game,this));
	}
	

	/** Used for if you actually want to control it using the keyboard **/
	public void enterManualOverride(){
		NewGame(true);
		while(manualOverride){
			game.everyStep(Q,W, O, P);
	
			m_world.step(timestep, veliterations, positerations);
			if (StepSched != null){
				StepSched.Iterate();
			}
			
		}
	}
	
	/** Return the current physics world **/
	public World getWorld(){
		return m_world;
	}
	
	/** add a scheduler which is iterated every phys step **/
	public void addScheduler(Scheduler StepSched){
		this.StepSched = StepSched;
	}
	
	/* Dist/phys steps */
	public float NormSpeed(){
		float normspeed = game.TorsoBody.getPosition().x/stepsInRun;
		return normspeed;
	}

	/* Cost function */
	public float Cost(){
		float cost = OptionsHolder.CostFunction(game); //Cost function now stored in OptionsHOlder for easier access.
//		return -game.TorsoBody.getPosition().x;
		return cost;
	}
	/** Calculate speed up to this point in units of dist/physsteps **/
	public float Speed(){
		if(game != null){
			return game.TorsoBody.getPosition().x/stepsInRun;
		}else{
			return 0;
		}
	}
	
	/* Check failure based on state */
	public boolean CheckFailure(){
		return failFlag;
	}
	
	/** Callback calls this to demand instant termination of phys steps **/
	public void InstaFail(){
		failFlag = true;
		if(manualOverride){
			NewGame(true);
		}
	}
	
	/* Do sequence of actions */
	public float[] DoSequence(int delay[]) throws InterruptedException{
		float [] cost = new float[delay.length];
		
		for (int i = 0; i < delay.length; i++){
			cost[i] = NextAction(delay[i]);
			if (failFlag){
				break;
			}
		}
		return cost;
	}
	
	private void DoPeriodic(int prefixLength, int periodicLength) throws InterruptedException{
		int[] subsequence = new int[periodicLength];
		this.prefixLength = prefixLength;
		this.periodicLength = periodicLength;
		subsequence = Arrays.copyOfRange(currentSequence, prefixLength-1, periodicLength+prefixLength-1);
		while(!failFlag){
			DoSequence(subsequence);
		}
		
	}
	/* Do one action (delay). Action order is defined here, the actual delay is externally input. Returns the cost.*/
	public float NextAction(int delay) throws InterruptedException{
		
		//Reset stance counters per action
		doubleCount = 0;
		singleCount = 0;
		flightCount = 0;
		sumBodyHeight = 0;
		
		boolean[] action = sequence.getNext();
//		System.out.println(action[0] + "," + action[1] + "," + action[2] + "," + action[3]);
		Q = action[0];
		W = action[1];
		O = action[2];
		P = action[3];
		for (int j = 0; j<delay; j++){
			game.everyStep(action[0],action[1],action[2],action[3]);
			m_world.step(timestep, veliterations, positerations);
			if(failFlag){
				break;
			}
			if (StepSched != null){
				StepSched.Iterate();
			}
			//Track what stance state it's in.
			switch(currentStance){
			case doubleStance:
				doubleCount++;
				break;
			case singleStance:
				singleCount++;
				break;
			case flightPhase:
				flightCount++;
				break;
			default:
				break;
			}
			sumBodyHeight += game.TorsoBody.getPosition().y;
		}
		
		
		
		
//
//		
//		//Select an action:
//		switch (sequencePosition) { 
//		
//		case 1: //Start with a pause
//			Q = false;
//			W = false;
//			O = false;
//			P = false;
//
//			for (int j = 0; j<delay; j++){
//				game.everyStep(false,false, false, false);
//				m_world.step(timestep, veliterations, positerations);
//				if(failFlag){
//					break;
//				}
//				if (StepSched != null){
//					StepSched.Iterate();
//				}
//				//Track what stance state it's in.
//				switch(currentStance){
//				case doubleStance:
//					doubleCount++;
//					break;
//				case singleStance:
//					singleCount++;
//					break;
//				case flightPhase:
//					flightCount++;
//					break;
//				default:
//					break;
//				}
//				sumBodyHeight += game.TorsoBody.getPosition().y;
//			}
//			break;
//		case 2: // W-O keys down
//
//			Q = false;
//			W = true;
//			O = true;
//			P = false;
//			for (int j = 0; j<delay; j++){
//				game.everyStep(false,true, true, false);
//				m_world.step(timestep, veliterations, positerations);
//				if(failFlag){
//					break;
//				}
//				if (StepSched != null){
//					StepSched.Iterate();
//				}
//				//Track what stance state it's in.
//				switch(currentStance){
//				case doubleStance:
//					doubleCount++;
//					break;
//				case singleStance:
//					singleCount++;
//					break;
//				case flightPhase:
//					flightCount++;
//					break;
//				default:
//					break;
//				}
//				sumBodyHeight += game.TorsoBody.getPosition().y;
//			}
//			break;
//		case 3: //Another pause.
//
//			Q = false;
//			W = false;
//			O = false;
//			P = false;
//			for (int j = 0; j<delay; j++){
//				game.everyStep(false,false, false, false);
//				m_world.step(timestep, veliterations, positerations);
//				if(failFlag){
//					break;
//				}
//				if (StepSched != null){
//					StepSched.Iterate();
//				}
//				//Track what stance state it's in.
//				switch(currentStance){
//				case doubleStance:
//					doubleCount++;
//					break;
//				case singleStance:
//					singleCount++;
//					break;
//				case flightPhase:
//					flightCount++;
//					break;
//				}
//				sumBodyHeight += game.TorsoBody.getPosition().y;
//			}
//			break;
//		case 4: // Q-P keys down.
//
//			Q = true;
//			W = false;
//			O = false;
//			P = true;
//			for (int j = 0; j<delay; j++){
//				game.everyStep(true,false, false, true);
//				m_world.step(timestep, veliterations, positerations);
//				if(failFlag){
//					break;
//				}
//				if (StepSched != null){
//					StepSched.Iterate();
//				}
//				//Track what stance state it's in.
//				switch(currentStance){
//				case doubleStance:
//					doubleCount++;
//					break;
//				case singleStance:
//					singleCount++;
//					break;
//				case flightPhase:
//					flightCount++;
//					break;
//				}
//				sumBodyHeight += game.TorsoBody.getPosition().y;
//			}
//			break;
//		default:
//			throw new RuntimeException("Tried to do an undefined step sequence action.");
//		}
//		System.out.println(stanceDuringAction.toString());
		//Increment us to the next action in the sequence:
		sequencePosition = (sequencePosition)%periodicLength + 1;
		
		if (currentIndex < 50 ){
			currentSequence[currentIndex] = delay; //Keep track of the sequence we're doing this run.
		}else{ //If we've already filled our current sequence buffer, then it's probably periodic going a long time. Don't overfill the buffer.
//			for (int i = 0; i<50; i++){
//				System.out.println(currentSequence[i] + ",");
//			}
//			System.out.println(currentSequence[50]);
		}
		currentIndex++;
		stepsInRun += delay;
		if(repeatSequence && (currentIndex == periodicLength + prefixLength)){
			DoPeriodic(prefixLength,periodicLength);
		}
		return Cost(); //Return the cost associated with the new position.
	}
}
/** Listens for collisions involving lower arms and head (implicitly with the ground) **/
class CollisionListener implements ContactListener{

	QWOPGame game;
	QWOPInterface QWOPHandler;
	
	public CollisionListener(QWOPGame game,QWOPInterface QWOPHandler){
		this.game = game;
		this.QWOPHandler = QWOPHandler;
	}
	@Override
	public void beginContact(Contact contact) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();

		//Failure when head, arms, or thighs hit the ground.
		if(fixtureA.m_body.equals(game.HeadBody) ||
				fixtureB.m_body.equals(game.HeadBody) ||
				fixtureA.m_body.equals(game.LLArmBody) ||
				fixtureB.m_body.equals(game.LLArmBody) ||
				fixtureA.m_body.equals(game.RLArmBody) ||
				fixtureB.m_body.equals(game.RLArmBody)) {
			QWOPHandler.InstaFail();
		}else if(fixtureA.m_body.equals(game.LThighBody)||
				fixtureB.m_body.equals(game.LThighBody)||
				fixtureA.m_body.equals(game.RThighBody)||
				fixtureB.m_body.equals(game.RThighBody)){
			QWOPHandler.InstaFail();
		}else if(fixtureA.m_body.equals(game.RFootBody) || fixtureB.m_body.equals(game.RFootBody)){//Track when each foot hits the ground.
			QWOPHandler.RFootDown = true;
			if(QWOPHandler.LFootDown){ //Change stance type if needed 
				QWOPHandler.currentStance = stanceType.doubleStance;
			}else{
				QWOPHandler.currentStance = stanceType.singleStance;
			}
			
		}else if(fixtureA.m_body.equals(game.LFootBody) || fixtureB.m_body.equals(game.LFootBody)){
			QWOPHandler.LFootDown = true;
			if(QWOPHandler.RFootDown){
				QWOPHandler.currentStance = stanceType.doubleStance;
			}else{
				QWOPHandler.currentStance = stanceType.singleStance;
			}
		}
	
	}

	@Override
	public void endContact(Contact contact) {
		//Track when each foot leaves the ground.
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		if(fixtureA.m_body.equals(game.RFootBody) || fixtureB.m_body.equals(game.RFootBody)){
			QWOPHandler.RFootDown = false;
			if(QWOPHandler.LFootDown){
				QWOPHandler.currentStance = stanceType.singleStance;
			}else{
				QWOPHandler.currentStance = stanceType.flightPhase;
			}
		}else if(fixtureA.m_body.equals(game.LFootBody) || fixtureB.m_body.equals(game.LFootBody)){
			QWOPHandler.LFootDown = false;
			if(QWOPHandler.RFootDown){
				QWOPHandler.currentStance = stanceType.singleStance;
			}else{
				QWOPHandler.currentStance = stanceType.flightPhase;
			}
		}
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

}