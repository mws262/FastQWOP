package TreeQWOP;
/**
 * These should all be parameters which apply to all trees made.
 * Should mostly be physics, output, visualization, etc. settings.
 * 
 * 
 * 
 * @author Matt
 *
 */


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
	public static boolean pauseWithAdvance = false; //Each time we change the root, do we pause? Mostly for showing stuff or debugging.
	/* Tree stacking settings */
	public static enum treeStacking{
		rectangle,
		spiral
	}
	public static treeStacking stacktype = treeStacking.rectangle;
	
	//for rectangular stacking
	public static int treesPerStack = 5;
	public static int treesPerRow = 5;
	public static int treeSpacing = 1800;
	public static int heightSpacing = 600;
	public static int viewAtOnce = 500; //Trees before we start hiding all old ones except for the tops of stacks.
	public static boolean displayStackTops = true; //When hiding old trees, do we still keep the tops of the stacks?
	
	//for spiral stacking
	
	public static float stackingradius = 1800;
	public static float treespercircle = 10;
	public static float anglebetween = (float)(Math.PI/5.);
	public static float heightbetween = 300;
	
	/* PHYSICS SETTINGS */
	/** Physics timestep **/
	public static float timestep = 0.04f;
	/** Physics position iterations per timestep **/
	public static int positerations = 10;
	/** Physics velocity iterations per timestep **/
	public static int veliterations = 2;

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
	public static int windowWidth = 1920;

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

	/* FUN */
	/** Change colors with microphone sound input **/
	public static boolean soundcolors = false;
	
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
}