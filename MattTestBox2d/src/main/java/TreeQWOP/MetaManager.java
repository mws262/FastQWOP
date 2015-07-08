package TreeQWOP;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import SimulatedAnnealing.SimAnneal;


public class MetaManager {
	private static int[] guess1 = {0};
	private static int[] guess2 = {26};
	private static int[] guess3 = {19};
	private static int[] guess4 = {25,30};
	private static int[] guess5 = {0,1};
	private static int[] guess6 = {45,46,47,48};
	private static int[] guess7 = {0,1};
	private static int[] guess8 = {45,46,47,48};
	private static int[] guess9 = {0,1};
	private static int[] guess10 = {45,46,47,48};
	private static int[] guess11 = {0,1};
	private static int[] guess12 = {45,46,47,48};
	private static int[] guess13 = {0,1};
	private static int[] guess14 = {45,46,47};
	private static int[] guess15 = {1,2};
	private static int[] guess16 = {50,51};
	
	private static int[][] guessA = {guess1,guess2,guess3,guess4,guess5,guess6,guess7,guess8,guess9,guess10,guess11,guess12,guess13,guess14,guess15,guess16};
	
	// Set of all possible choices of actions from which to build the tree. Each tree will be built using a subset.
	private static int[] superset1 = {0,1,2,3,4,5};
	private static int[] superset2 = {22,23,24,25,26,27,28,29,30};
	private static int[] superset3 = {17,18,19,20,21,22,23};
	private static int[] superset4 = {40,41,42,43,44,45,49,50,51,52,53,54,55};
	private static int[] superset5 = {0,1,2,3,4,5};
	private static int[] superset6 = {45,46,47,48,49,50,51,52,53,54,55};
	private static int[] superset7 = {0,1,2,3,4,5};
	private static int[] superset8 = {45,46,47,48,49,50,51,52,53,54,55};
	private static int[] superset9 = {0,1,2,3,4,5};
	private static int[] superset10 = {45,46,47,48,49,50,51,52,53,54,55};
	private static int[] superset11 = {0,1,2,3,4,5};
	private static int[] superset12 = {45,46,47,48,49,50,51,52,53,54,55};
	private static int[] superset13 = {0,1,2,3,4};
	private static int[] superset14 = {40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57};
	private static int[] superset15 = {0,1,2,3,4,5,6,7};
	private static int[] superset16 = {40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57};
	
	private static int[][] superset = {superset1,superset2,superset3,superset4,superset5,superset6,superset7,superset8,superset9,superset10,superset11,superset12,superset13,superset14,superset15,superset16};
	
	
	static CopyOnWriteArrayList<TreeHandle> trees = new CopyOnWriteArrayList<TreeHandle>();

	public MetaManager() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {

		ExhaustiveQwop eq = new ExhaustiveQwop(trees);
		SimpleDistVal distVal = new SimpleDistVal(eq);
		
		SimAnneal optim = new SimAnneal(superset, guessA, distVal);
		optim.runAnnealing(1000);
//		for (int i = 0; i<10; i++){
//			
//			for(TreeHandle th: trees){
//				th.focus = false;
//			}
//			TreeParameters tp1 = new TreeParameters();
//			tp1.treeDepth = 8;
//			tp1.TreeLevel = 500*i;
//			eq.RunGame(tp1);
//			
//		}
//		for(TreeHandle th: trees){
//			th.getRoot().evalValueFunctionTree(distVal);
//			System.out.println(distVal.getTotalValue());
//			distVal.clearTotal();
//		}
		
		eq.idleGraphics();


	}
}

	