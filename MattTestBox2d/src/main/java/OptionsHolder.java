public class OptionsHolder {

	/* OUTPUT OPTIONS */
	/**  Are graphics and delays on? **/
	public static boolean visOn = false;
	
	/** Report info as the search is running? **/
	public static boolean verboseOn = true;
	public static int verboseIncrement = 1000; // How many games between outputs?
	
	/* SEARCH OPTIONS */
	/** Repeat actions to attempt periodic motion? **/
	public static boolean goPeriodic = false;
	
	/** After failure, do we go back up to the nearest unexplored node (true), or do we reset to the top (false). **/
	public static boolean marchUp = false;
	
	/** Depth of the search (number of parameters down the tree) **/
	public static int treeDepth = 8;
	
	/** List of possible actions at any point in this predefined sequence. Will wrap back to the first one whan each has been sampled from **/
	
	public static final int[][] ActionList = {
		{0,1,2,3,4},
		{20,21,22,23,24,25,26,27,28},
		{10,11,12,13,14,15,16,17,18,19},
		{49,50,51,52,53,54,55},
		{21,22,23,24,25,26,27},
		{65,66,67,68,69},
		{0,1,2,3,4,5,6,7},
		{59,60,61,62,63,64,65,66}
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
		boolean failure = game.HeadBody.getPosition().y>3 ; //ground is at 10ish with up being -, so we're calling torso above 7 being failure.	
		return failure;
	}
	
	/** Used to evaluate the cost of a specific state **/
	public static float CostFunction(QWOPGame game){
		return -game.TorsoBody.getPosition().x;
	}
	
}
