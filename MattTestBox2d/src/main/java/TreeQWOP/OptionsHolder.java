package TreeQWOP;
public class OptionsHolder {

	public static TrialNode focusNode;


	/* Global variable cringe */
	/** Total games played **/
	public static int gamesPlayed = 0;

	/* OUTPUT OPTIONS */
	/** Do we use traditional java Graphics2d or openGL? **/
	public static boolean useGLMaster = true; //Main screen turn on
	public static boolean useGLSlave = true; //Slave panel in the tabbed part

	/**  Are (runner, not node) graphics and delays on? **/
	public static boolean visOn = false;

	/** Report info as the search is running? **/
	public static boolean verboseOn = false;
	public static int verboseIncrement = 1000; // How many games between outputs?
	public static boolean DataToFile = false;
	public static boolean KeepStates = true; //Do we record the state at every node. (performance issue?)

	/* Tree stacking settings */
	
	public static int treesPerStack = 5;
	public static int treesPerRow = 5;
	public static int treeSpacing = 1400;
	public static int heightSpacing = 400;
	public static int viewAtOnce = 500; //Trees before we start hiding all old ones except for the tops of stacks.
	public static boolean displayStackTops = true; //When hiding old trees, do we still keep the tops of the stacks?
	
	/* PHYSICS SETTINGS */
	/** Physics timestep **/
	public static float timestep = 0.04f;
	/** Physics position iterations per timestep **/
	public static int positerations = 5;
	/** Physics velocity iterations per timestep **/
	public static int veliterations = 1;

	/**Is it a failure when the thighs hit the ground? (not like this in game, but probably true for good actions) **/
	public static boolean thighFailure = true;

	/* Tree visualization parameters: */
	/** Use dark theme or light theme? **/
	public static boolean darkTheme = true;
	/** Should we bother keeping track of tree visualization stuff? **/
	public static boolean treeVisOn = true;

	/** Do we display the failure types as dots at the end of branches or not? **/
	public static boolean failTypeDisp = true;

	/** Display window width **/
	public static int windowWidth = 2000;

	/** Display window height **/
	public static int windowHeight = 1000;

	/** Center point for the root node -- by default, center of the window **/
	public static int[] growthCenter = {windowWidth/2, windowHeight/2};

	/** Length of a tree edge **/
	public static float edgeLength = 100;
	public static float edgeLengthAlt = 100; //Same as above but for the subview pane.

	/** General size factor **/
	public static float sizeFactor = 1f;
	public static float sizeFactorAlt = 1f;

	/** When tree visualization is zoomed in, must change the reference edge length for new branches **/
	public static void ChangeSizeFactor(float increaseFactor){
		sizeFactor *= increaseFactor;
		edgeLength *= increaseFactor;

	}
	public static void ChangeSizeFactorAlt(float increaseFactor){
		sizeFactorAlt *= increaseFactor;
		edgeLengthAlt *= increaseFactor;

	}

	/* STATE EVALUATION FUNCTIONS */ //TODO: maybe move this too?

	/** Used to evaluate the cost of a specific state **/
	public static float CostFunction(QWOPGame game){
		if(game != null){
			return -game.TorsoBody.getPosition().x;
		}else{
			return 0;
		}
	}



	//OLD OLD MOVED TO  TREEPARAMETERS

	//	/** List of possible actions at any point in this predefined sequence. Will wrap back to the first one whan each has been sampled from **/
	//	public static final int[][] ActionList = {
	////
	////0, 26, 19, 51, 2, 47, 1, 49,
	//		//2, 25, 19, 51, 1, 53, 2, 51, 2, 40, 1, 52, 2, 37, 2, 35, 2, 52, 2, 36, 0, 52, 2, 37, 3, 36, 1, 38, 4, 52, 2, 39, 2, 45, 1, 40, 4, 54, 1, 39, 3, 44, 1, 48, 2, 41, 2, 48, 3, 50, 2, 47, 2, 49, 2, 49, 3, 49.
	//
	////		{2},
	////		{25},
	////		{19},
	////		{51},
	////		//
	////		
	////		{1},
	////		{53},
	////		{2},
	////		{51},
	//	//		{0,1,2,3},
	////		{38,39,40,41,42,43,44,45,46,47,48,49,50,51,52},
	////		{0,1,2,3},
	////		{38,39,40,41,42,43,44,45,46,47,48,49,50,51,52},
	////				
	////		{0,1,2,3,4,5},
	////		{25,26,27,28},
	////		{17,18,19,20},
	////		{49,50,51,52},
	////		
	////		{0,1,2,3},
	////		{45,46,47,48,49,50,51,52,53,54,55},
	////		{0,1,2,3},
	////		{45,46,47,48,49,50,51,52,53,54,55},	
	//		
	//		
	//		{0},
	//		{26},
	//		{19},
	//		{20,25,30,35,40,45,49,50,51,52},
	//		//
	//		{0,1,2,3},
	//		{45,46,47,48,49,50,51,52,53,54,55},
	//		{0,1,2,3},
	//		{45,46,47,48,49,50,51,52,53,54,55},	
	//		
	//		{0,1,2,3},
	//		{45,46,47,48,49,50,51,52,53,54,55},
	//		{0,1,2,3},
	//		{45,46,47,48,49,50,51,52,53,54,55},	
	//		
	//		
	//
	////		
	//		{0,1,2},
	//		{45,46,47},
	//		{1,2},
	//		{50,51},
	////		
	////		{0,10},
	////		{36,48,60},
	////		{0,10},
	////		{36,48,60}
	//		
	////		
	////		{0,2},
	////		{18,19,23,24,25,26},
	////		{13,14,15,16,17,18,19},
	////		{49,50,51,52,53,54,55},
	////		//
	////		{0,1,2,3,4,5,6,7},
	////		{65,66,67,68,69},
	////		{0,1,2,3,4,5,6,7},
	////		{59,60,61,62,63,64,65,66}
	//		};
	//	
	//	// Once we get to the end of the prefix + periodic, 
	//	public static final int[][] DeviationList = {
	//		{0,1},
	//		{-2,-1,0,1,2},
	//		{0,1},
	//		{-2,-1,0,1,2}
	////		
	////		{0},
	////		{0},
	////		{0},
	////		{0,1}
	//	
	//	};
	//	
	//	
	//
	//	/* SEARCH OPTIONS */
	//	
	//	/** After prefix + periodic, then switch to deviations instead of going back to the same list. **/
	//	public static boolean goDeviations = false;
	//	
	//	/** Repeat actions to attempt periodic motion? **/
	//	public static boolean goPeriodic = false;
	//	
	//	/** After failure, do we go back up to the nearest unexplored node (true), or do we reset to the top (false). **/
	//	public static boolean marchUp =  false;
	//	
	//	/** When picking an already-sampled, but not fully-explored route to try, do we do it randomly, or just grab the first one? **/
	//	public static boolean sampleRandom = true;
	//	
	//	/** When trying to expand the tree, do we prioritize adding new nodes or treat possibilities the same? **/ // Note -- true tends to just grab the first index resulting in bad things for very deep trees.
	//	public static boolean PrioritizeNewNodes = false;
	//	
	//	/** Depth of the search (number of parameters down the tree) **/
	//	public static boolean limitDepth = false;
	//	public static int treeDepth = 12;
	//	
	//	/** Settings for my heuristic search **/
	//	public static boolean stochasticDepth = false; //NOT BOTH THIS AND LIMITDEPTH
	//	public static int stochasticHorizon = 4; // How far do we set the currentRoot from the selected endpoint.
	//	public static int sampleCount = 400; //How many games before we move on?
	//	public static int forwardJump = 1; //When moving down the tree, how many nodes do we jump?
	//	public static int backwardJump = 1; //when moving back up the tree, how many nodes do we jump?
	//	public static boolean trimSimilar = false;
	//	public static boolean weightedSelection = false;
	//	public static boolean multiSelection = false;
	//	public static int multiPointCount = 2;
	//	
	//	/** When we get to the end of the selection sequence, do we start picking again from the "periodic" portion (as opposed to looping back to choice 1. **/
	//	public static boolean repeatSelectionInPeriodic = true; //DO NOT HAVE BOTH THIS AND GODEVIATIONS AT THE SAME TIME.
	//	
	//	
	//	public static int prefixLength = 12; //How many elements lead up to the repeated portion.
	//	public static int periodicLength = 4; // How many elements are the repeated portion.
	//	
}
