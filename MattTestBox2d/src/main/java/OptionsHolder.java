public class OptionsHolder {

	/* OUTPUT OPTIONS */
	/**  Are graphics and delays on? **/
	public static boolean visOn = false;
	
	/** Report info as the search is running? **/
	public static boolean verboseOn = true;
	public static int verboseIncrement = 1000; // How many games between outputs?
	
	/* SEARCH OPTIONS */
	/** Repeat actions to attempt periodic motion? **/
	public static boolean goPeriodic = true;
	
	/** After failure, do we go back up to the nearest unexplored node (true), or do we reset to the top (false). **/
	public static boolean marchUp = false;
	
	/** Depth of the search (number of parameters down the tree) **/
	public static int treeDepth = 8;
	
	/** List of possible actions at any point in this predefined sequence. Will wrap back to the first one whan each has been sampled from **/
	public static final int[][] ActionList = {
		{0,2},
		{1,2,3,4,0,3,2},
		{11,12,13,14,15,16,17,18},
		{48,49,50,51,52,53,54},
		{21,22,23,24,25,26,27},
		{61,62,63,64,65,66,67,68},
		{0,1,2,3},
		{61,62,63,64,65,66,67,68}
		};

	
	/* PHYSICS SETTINGS */
	/** Physics timestep **/
	public static float timestep = 0.04f;
	/** Physics position iterations per timestep **/
	public static int positerations = 5;
	/** Physics velocity iterations per timestep **/
	public static int veliterations = 1;

	
	/* STATE EVALUATION FUNCTIONS */
	/** How do we decide when the run has failed? (currently checked at discrete decision points) **/
	public static boolean FailureCondition(QWOPGame game){
		boolean failure = game.TorsoBody.getPosition().y>7; //ground is at 10ish with up being -, so we're calling torso above 7 being failure.	
		return failure;
	}
	
	/** Used to evaluate the cost of a specific state **/
	public static float CostFunction(QWOPGame game){
		return -game.TorsoBody.getPosition().x;
	}
	
}
