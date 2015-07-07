/**
 * This replaces some of the parameter management of OptionsHolder. Now OptionsHolder is
 * intended entirely for parameters, while this is made for parameters that might
 * be varied between creation of different trees.
 * 
 * 
 * @author Matt
 *
 */
public class TreeParameters {

	//For now, options will have these defaults, but are changeable.
	
	public float TreeLevel = 0; //plane level that the tree exists on.
	/** List of possible actions at any point in this predefined sequence. Will wrap back to the first one whan each has been sampled from **/
	public int[][] ActionList = {
		
		{0},
		{26},
		{19},
		{20,25,30,35,40,45,49,50,51,52},
		
		{0,1,2,3},
		{45,46,47,48,49,50,51,52,53,54,55},
		{0,1,2,3},
		{45,46,47,48,49,50,51,52,53,54,55},	
		
		{0,1,2,3},
		{45,46,47,48,49,50,51,52,53,54,55},
		{0,1,2,3},
		{45,46,47,48,49,50,51,52,53,54,55},	
	
		{0,1,2},
		{45,46,47},
		{1,2},
		{50,51},
	
		};
	
	// Once we get to the end of the prefix + periodic, 
		public int[][] DeviationList = {
			{0,1},
			{-2,-1,0,1,2},
			{0,1},
			{-2,-1,0,1,2}
		};
		

		/* SEARCH OPTIONS */
		
		/** After prefix + periodic, then switch to deviations instead of going back to the same list. **/
		public boolean goDeviations = false;
		
		/** Repeat actions to attempt periodic motion? **/
		public boolean goPeriodic = false;
		
		/** After failure, do we go back up to the nearest unexplored node (true), or do we reset to the top (false). **/
		public boolean marchUp = false;
		
		/** When picking an already-sampled, but not fully-explored route to try, do we do it randomly, or just grab the first one? **/
		public boolean sampleRandom = true;
		
		/** When trying to expand the tree, do we prioritize adding new nodes or treat possibilities the same? **/ // Note -- true tends to just grab the first index resulting in bad things for very deep trees.
		public boolean PrioritizeNewNodes = false;
		
		/** Depth of the search (number of parameters down the tree) **/
		public boolean limitDepth = true;
		public int treeDepth = 12;
		
		/** Settings for my heuristic search **/
		public boolean stochasticDepth = false; //NOT BOTH THIS AND LIMITDEPTH
		public int stochasticHorizon = 4; // How far do we set the currentRoot from the selected endpoint.
		public int sampleCount = 200; //How many games before we move on?
		public int forwardJump = 1; //When moving down the tree, how many nodes do we jump?
		public int backwardJump = 1; //when moving back up the tree, how many nodes do we jump?
		public boolean trimSimilar = false;
		public boolean weightedSelection = false;
		public boolean multiSelection = false;
		public int multiPointCount = 2;
		
		/** When we get to the end of the selection sequence, do we start picking again from the "periodic" portion (as opposed to looping back to choice 1. **/
		public boolean repeatSelectionInPeriodic = true; //DO NOT HAVE BOTH THIS AND GODEVIATIONS AT THE SAME TIME.
		
		
		public int prefixLength = 12; //How many elements lead up to the repeated portion.
		public int periodicLength = 4; // How many elements are the repeated portion.
		
	
	public TreeParameters() {
		// TODO Auto-generated constructor stub
	}

}
