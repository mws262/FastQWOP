import org.jbox2d.dynamics.World;

import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

class QwopCost2 implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible
	public static boolean visOn = false;
	public static int millisdelay = 50;
	
	QWOPInterface QWOPGame = new QWOPInterface();
	
	public double valueOf (double[] x) {
//		System.out.println(visOn);
		double res = 0;
		//Create a new qwop for now.
		QWOPGame.NewGame();
		if (visOn){
			QWOPGame.runRealtime = true;
		}

		int[] input = new int[x.length];
		for (int i = 1; i<x.length; i++){
			input[i] = (int)x[i];
			
		}
		float[] cost;
		try {
			cost = QWOPGame.DoSequence(input);
		} catch (InterruptedException e) {
			cost = new float[0];
			e.printStackTrace();
		}
		
		float sum = 0;
		for (int i = 1; i<cost.length; i++){
			sum += cost[i];
			
		}
		return (double)sum;
		
		
	}
	public boolean isFeasible(double[] x) {
		for(int i = 0; i<x.length; i++){
			if(x[i]<0){
				return false;
			}else if(x[i]>100){
				return false;
			}
		}
		
		return true;
	} 
}

public class CMAPeriodic1 {
	public static void main(String[] args) {

		double[] guess = {4,46,6,37,5,53,4,54};//4,50,4,50};//,4,50,4,50};
		//63.82350044434338 ,8.656632785933883 ,81.167479244532 ,15.969761479890483 ,25.28189559578514 ,53.9508831219439 ,16.05797684871328 ,30.152101040906793 
		IObjectiveFunction fitfun = new QwopCost2();

		// new a CMA-ES and set some initial values
		CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
		cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
		cma.setDimension(8); // overwrite some loaded properties
		cma.setInitialX(guess); // in each dimension, also setTypicalX can be used
		cma.setInitialStandardDeviation(50); // also a mandatory setting 
		cma.options.stopFitness = -1e3;       // optional setting
//		cma.

		// initialize cma and get fitness array to fill in later
		double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];

		// initial output to files
		cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files
//
		// iteration loop
		while(cma.stopConditions.getNumber() == 0) {

            // --- core iteration step ---
			double[][] pop = cma.samplePopulation(); // get a new population of solutions
			for(int i = 0; i < pop.length; ++i) {    // for each candidate solution i
            	// a simple way to handle constraints that define a convex feasible domain  
            	// (like box constraints, i.e. variable boundaries) via "blind re-sampling" 
            	                                       // assumes that the feasible domain is convex, the optimum is  
				while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,  
					pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are  
                                                       //   sufficiently small to prevent quasi-infinite looping here
                // compute fitness/objective value	
				fitness[i] = fitfun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
			}
			cma.updateDistribution(fitness);         // pass fitness array to update search distribution
            // --- end core iteration step ---

			// output to files and console 
			cma.writeToDefaultFiles();
			int outmod = 150;
			if (cma.getCountIter() % (15*outmod) == 1)
				cma.printlnAnnotation(); // might write file as well
			if (cma.getCountIter() % outmod == 1)
				cma.println(); 
		}
		// evaluate mean value as it is the best estimator for the optimum
		cma.setFitnessOfMeanX(fitfun.valueOf(cma.getMeanX())); // updates the best ever solution 

		// final output
		cma.writeToDefaultFiles(1);
		cma.println();
		cma.println("Terminated due to");
		for (String s : cma.stopConditions.getMessages())
			cma.println("  " + s);
		cma.println("best function value " + cma.getBestFunctionValue() 
				+ " at evaluation " + cma.getBestEvaluationNumber());
		//Print solution
			double[] bestEver = cma.getBestX();
			
			for(int i = 0; i<bestEver.length; i++){
				System.out.print(bestEver[i]+" ,");
			}
			//print dist travelled
			System.out.println("\nbest dist:\n"+(-cma.getBestFunctionValue()));
			QwopCost2.visOn = true;
//			double[] tester = {22.410908455267354 ,46.81565379196605 ,12.748856869271224 ,49.442531803244215 ,26.73856728326716 ,38.81182179479007 ,20.654388139679824 ,45.47628009237365};
//42.28353741454642 ,21.05727946469882 ,41.98566258068303 ,33.585270274984524 ,5.650794210078085 ,58.22004077946156 ,13.879903804646142 ,58.550856529301676 ,
			fitfun.valueOf(bestEver);
			
		// we might return cma.getBestSolution() or cma.getBestX()

	} // main  
} // class
