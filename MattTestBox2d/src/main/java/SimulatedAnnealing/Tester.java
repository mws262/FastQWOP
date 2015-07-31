package SimulatedAnnealing;

public class Tester {
	
	private static int[] guess1 = {1,2};
	private static int[] guess2 = {1,2};
	private static int[] guess3 = {1,2};
	private static int[] guess4 = {1,2};
	private static int[] guess5 = {1,2};
	private static int[] guess6 = {1,2};
	private static int[] guess7 = {1,2};
	private static int[] guess8 = {1,2};
	private static int[] guess9 = {1,2};
	private static int[] guess10 = {1,2};
	private static int[] guess11 = {1,2};
	private static int[] guess12 = {1,2};
	private static int[] guess13 = {1,2};
	private static int[] guess14 = {1,2};
	private static int[] guess15 = {1,2};
	
	private static int[][] guessA = {guess1,guess2,guess3,guess4,guess5,guess6,guess7,guess8,guess9,guess10,guess11,guess12,guess13,guess14,guess15};
	
	// Set of all possible choices of actions from which to build the tree. Each tree will be built using a subset.
	private static int[] superAction1 = {0,1,2,3};
	private static int[] superAction2 = {0,1,2,3};
	private static int[] superAction3 = {0,1,2,3};
	private static int[] superAction4 = {0,1,2,3};
	private static int[] superAction5 = {0,1,2,3};
	private static int[] superAction6 = {0,1,2,3};
	private static int[] superAction7 = {0,1,2,3};
	private static int[] superAction8 = {0,1,2,3};
	private static int[] superAction9 = {0,1,2,3};
	private static int[] superAction10 = {0,1,2,3};
	private static int[] superAction11 = {0,1,2,3};
	private static int[] superAction12 = {0,1,2,3};
	private static int[] superAction13 = {0,1,2,3};
	private static int[] superAction14 = {0,1,2,3};
	private static int[] superAction15 = {0,1,2,3};
	
	private static int[][] superset = {superAction1,superAction2,superAction3,superAction4,superAction5,superAction6,superAction7,superAction8,superAction9,superAction10,superAction11,superAction12,superAction13,superAction14,superAction15};
	
	
	public Tester() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[]){
		
		
		SimAnneal optim = new SimAnneal(superset,guessA);
		optim.runAnnealing(5000);
		
		
	}
}
