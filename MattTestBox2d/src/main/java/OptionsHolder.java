public class OptionsHolder {
	/* Global variable cringe */
	/** Total games played **/
	public static int gamesPlayed = 0;
	
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
	public static boolean marchUp =  false;
	
	/** When picking an already-sampled, but not fully-explored route to try, do we do it randomly, or just grab the first one? **/
	public static boolean sampleRandom = true;
	
	/** Depth of the search (number of parameters down the tree) **/
	public static int treeDepth = 50;
	
	/** List of possible actions at any point in this predefined sequence. Will wrap back to the first one whan each has been sampled from **/
	
	public static final int[][] ActionList = {
		{0,2},
		{18,19,23,24,25,26},
		{13,14,15,16,17,18,19},
		{49,50,51,52,53,54,55},
		{0,1,2,3,4,5,6,7},
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
	
	/* Tree visualization parameters: */
	  /** Should we bother keeping track of tree visualization stuff? **/
	  public static boolean treeVisOn = true;
	
	  /** Display window width **/
	  public static int windowWidth = 2000;
	  
	  /** Display window height **/
	  public static int windowHeight = 1000;
	  
	  /** Center point for the root node -- by default, center of the window **/
	  public static int[] growthCenter = {windowWidth/2, windowHeight/2};
	  
	  /** Length of a tree edge **/
	  public static float edgeLength = 100;
	  
	  /** General size factor **/
	  public static float sizeFactor = 1f;

	  /** When tree visualization is zoomed in, must change the reference edge length for new branches **/
	  public static void ChangeSizeFactor(float increaseFactor){
		  sizeFactor *= increaseFactor;
		  edgeLength *= increaseFactor;
		  
	  }
	
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
