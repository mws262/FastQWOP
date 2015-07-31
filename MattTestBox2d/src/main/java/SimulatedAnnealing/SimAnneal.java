package SimulatedAnnealing;

import java.util.Arrays;
import java.util.Random;

import TreeQWOP.GenericValueFunction;
import TreeQWOP.OptionsHolder;
import TreeQWOP.TreeParameters;

public class SimAnneal {

	/** Temperature of simulated annealing **/
	public float temperature = 1000;
	
	/** Largest set of values to choose from **/
	private int[][] superset;
	
	/** Limited guesses actually used to build trees. **/
	private int[][] guessA;
	private int[][] guessB;
	
	private Random rand = new Random();
	private SetDefinition set;
	private GenericValueFunction valFun;
	
	public SimAnneal(int[][] superSet, int[][] guess, GenericValueFunction val) {
		
		superset = superSet;
		valFun = val;
		
		//Create a new guess A of the correct size.
		guessA = new int[guess.length][];
		for (int l = 0; l< guess.length; l++){
			guessA[l] = new int[guess[l].length];
		}
		//Make guess B the same size, don't bother populating it yet though.
		guessB = new int[guess.length][];
		for (int l = 0; l< guess.length; l++){
			guessB[l] = new int[guess[l].length];
		}
		
		//Populate guess A with the provided guess:
		SetDefinition.fillArray(guess,guessA);
		
		// give our mutator the full set of options to start with.
		set = new SetDefinition(superset);
	}
	
	public SimAnneal(int[][] superSet, GenericValueFunction val) {
		
		superset = superSet;
		valFun = val;
		TreeParameters tp = new TreeParameters();
		int[][] guess = tp.ActionList;
		//Create a new guess A of the correct size.
		guessA = new int[guess.length][];
		for (int l = 0; l< guess.length; l++){
			guessA[l] = new int[guess[l].length];
		}
		//Make guess B the same size, don't bother populating it yet though.
		guessB = new int[guess.length][];
		for (int l = 0; l< guess.length; l++){
			guessB[l] = new int[guess[l].length];
		}
		
		//Populate guess A with the provided guess:
		SetDefinition.fillArray(guess,guessA);
		
		// give our mutator the full set of options to start with.
		set = new SetDefinition(superset);
	}
	
	/** Tester version, uses built in cost fcn **/
	public SimAnneal(int[][] superSet, int[][] guess) {
		
		superset = superSet;
		
		//Create a new guess A of the correct size.
		guessA = new int[guess.length][];
		for (int l = 0; l< guess.length; l++){
			guessA[l] = new int[guess[l].length];
		}
		//Make guess B the same size, don't bother populating it yet though.
		guessB = new int[guess.length][];
		for (int l = 0; l< guess.length; l++){
			guessB[l] = new int[guess[l].length];
		}
		
		//Populate guess A with the provided guess:
		SetDefinition.fillArray(guess,guessA);
		
		// give our mutator the full set of options to start with.
		set = new SetDefinition(superset);
	}
	public void runAnnealing(int iterations){
		float cost1 = Float.MAX_VALUE;
		float cost2 = Float.MAX_VALUE;
		
		TreeParameters tp = new TreeParameters();
		tp.ActionList = guessA;
		
		//Initial cost of actions
		if(valFun != null){
			cost1 = -valFun.RunAndEval(tp); //guess a
		}else{
			cost1 = costfcn(guessA);
		}
		
		for (int i = 0; i<iterations; i++){

			set.mutate(guessA, guessB, 1);
			float height = tp.TreeLevel;
			
			tp = new TreeParameters();
			tp.TreeLevel = height + OptionsHolder.heightSpacing;
			tp.ActionList = guessB;
			
			if(valFun != null){
				cost2 = -valFun.RunAndEval(tp);// guess b
			}else{
				cost2 = costfcn(guessB);
			}
			
			if(cost2<cost1){ // this is a better input space.
	
				SetDefinition.fillArray(guessB, guessA); //Fill A with the contents of B (mutated).
				cost1 = cost2;
				
			}else{ //this is a worse input space.
				
				float choicevar = rand.nextFloat();
				if(choicevar<acceptanceProb(cost1,cost2)){

					SetDefinition.fillArray(guessB, guessA); //Fill A with the contents of B (mutated).

					cost1 = cost2;
				}else{
					//Nothing!
				}
				
			}

			decayTemp();	
			
		}
		
		System.out.println(cost1);
		SetDefinition.printSequence(guessA);
	}
	
	//Odds of taking a new but worse solution.
	public float acceptanceProb(float cost1, float cost2){
		return (float)Math.exp((cost1-cost2)/temperature);
	}
	
	public void decayTemp(){
		temperature *= 0.99;
	}

	
	//This is a test function which just sums all the elements in x.
	public float costfcn(int[][] x){
		int sum = 0;
		
		for(int i = 0; i<x.length; i++){
			for(int j = 0; j<x[i].length; j++){
				if(x[i][j]<0){
					throw new RuntimeException("aarrggh");
				}
				sum+=x[i][j];		
			}
		}
		return sum;
	}
}
