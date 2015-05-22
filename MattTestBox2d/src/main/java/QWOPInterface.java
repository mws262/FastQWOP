import java.util.Arrays;
import org.jbox2d.dynamics.World;


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
	private int currentIndex = 0; //counts number of actions taken in this run.
	
	public int stepsInRun = 0; //Always counts up until a game reset. Helps ensure we know when we're doing something periodic.
	
	
	public boolean repeatSequence = false;
	public int prefixLength = 4; //How many elements lead up to the repeated portion.
	public int periodicLength = 4; // How many elements are the repeated portion.
	
	public boolean visOn = false;
	
	public VisRunner visRun;
	
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
		
		// If we're visualizing, then either create the visualizer or pass it the new world we're working with.
		if (visOn && visRun == null){
			visRun = new VisRunner(game.getWorld());
		}else if(visOn){
			visRun.SwitchWorlds(game.getWorld());
		}
		
	}
	/** Return the current physics world **/
	public World getWorld(){
		return m_world;
	}
	
	/** Make the runner visualization update **/
	private void VisRunnerUpdate(){
		visRun.repaint();
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
	
	/* Check failure based on state */
	public boolean CheckFailure(){
//		boolean failure = game.TorsoBody.getPosition().y>5; //ground is at 10ish with up being -, so we're calling torso above 7 being failure.	
		boolean failure = OptionsHolder.FailureCondition(game); //Failure condition now stored in OptionsHolder for easier access.
		return failure;
	}
	
	/* Do sequence of actions */
	public float[] DoSequence(int delay[]) throws InterruptedException{
		float [] cost = new float[delay.length];
		
		for (int i = 0; i < delay.length; i++){
			cost[i] = NextAction(delay[i]);
		}
		return cost;
	}
	
	private void DoPeriodic() throws InterruptedException{
		boolean fallen = false;
		int[] subsequence = new int[periodicLength];
		subsequence = Arrays.copyOfRange(currentSequence, prefixLength-1, periodicLength+prefixLength-1);
//		System.out.println(subsequence.length);
		while(!fallen){
			DoSequence(subsequence);
			fallen = CheckFailure() || NormSpeed()<0.1;
//			if (NormSpeed()<0.1) System.out.println("We've killed a solution due to low periodic speed.");
		}
		
	}
	/* Do one action (delay). Action order is defined here, the actual delay is externally input. Returns the cost.*/
	public float NextAction(int delay) throws InterruptedException{
		//Select an action:
		switch (sequencePosition) { 
		
		case 1: //Start with a pause
			for (int j = 0; j<delay; j++){
				game.everyStep(false,false, false, false);
				m_world.step(timestep, veliterations, positerations);
				if (visOn){
					VisRunnerUpdate();
					Thread.sleep((long)delaymillis);
					System.out.println('d');
				}
			}
			break;
		case 2: // W-O keys down
			for (int j = 0; j<delay; j++){
				game.everyStep(false,true, true, false);
				m_world.step(timestep, veliterations, positerations);
				if (visOn){
					VisRunnerUpdate();
					Thread.sleep((long)delaymillis);
				}
			}
			break;
		case 3: //Another pause.
			for (int j = 0; j<delay; j++){
				game.everyStep(false,false, false, false);
				m_world.step(timestep, veliterations, positerations);
				if (visOn){
					VisRunnerUpdate();
					Thread.sleep((long)delaymillis);
				}
			}
			break;
		case 4: // Q-P keys down.
			for (int j = 0; j<delay; j++){
				game.everyStep(true,false, false, true);
				m_world.step(timestep, veliterations, positerations);
				if (visOn){
					VisRunnerUpdate();
					Thread.sleep((long)delaymillis);
				}
			}
			break;
		default:
			throw new RuntimeException("Tried to do an undefined step sequence action.");
		}
		
		//Increment us to the next action in the sequence:
		sequencePosition = (sequencePosition)%4 + 1;
		
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
			DoPeriodic();
		}

		return Cost(); //Return the cost associated with the new position.
	}
}
