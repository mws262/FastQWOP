package org.jbox2d.testbed.framework.jogl;
import org.jbox2d.dynamics.World;

import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

/** The very well-known Rosenbrock objective function to be minimized. 
 */
class Rosenbrock implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible
	public double valueOf (double[] x) {
		double res = 0;
		for (int i = 0; i < x.length-1; ++i)
			res += 100 * (x[i]*x[i] - x[i+1]) * (x[i]*x[i] - x[i+1]) + 
			(x[i] - 1.) * (x[i] - 1.);
		return res;
	}
	public boolean isFeasible(double[] x) {return true; } // entire R^n is feasible
}

class QwopCost implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible
	public static boolean visOn = false;
	public static int millisdelay = 50;
	public double valueOf (double[] x) {
//		System.out.println(visOn);
		double res = 0;
		//Create a new qwop for now.
		QWOPNEW2 game = new QWOPNEW2(visOn);
		World m_world = game.getWorld();
		float timestep = 0.04f;
		int positerations = 5;
		int veliterations = 1;
		double initDist = game.TorsoBody.getPosition().x;
		
		
		for (int i = 0; i <Math.floor(x.length/4); i++){
			for (int m = 0; m<x[i*4]; m++){
				game.everyStep(false,false, false, false);
				m_world.step(timestep, positerations,veliterations);
				if(visOn)
					try {
						Thread.sleep(millisdelay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			for (int j = 0; j<x[i*4+1]; j++){
				game.everyStep(false,true, true, false);
				m_world.step(timestep, 5,5);
				if(visOn)
					try {
						Thread.sleep(millisdelay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			for (int k = 0; k<x[i*4+2]; k++){
				game.everyStep(false,false, false, false);
				m_world.step(timestep, positerations,veliterations);
				if(visOn)
					try {
						Thread.sleep(millisdelay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			for (int l = 0; l<x[i*4+3]; l++){
				game.everyStep(true,false, false, true);
				m_world.step(timestep, positerations,veliterations);
				if(visOn)
					try {
						Thread.sleep(millisdelay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		
		double inputSum = 0;
		for (int i = 0; i<x.length; i++){
			inputSum = x[i] + inputSum;
		}
		//Minimize negative progress forward.
		res = -(game.TorsoBody.getPosition().x-initDist);
		double cost = res+inputSum/10;
		return cost;
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

public class CMAExample1 {
	public static void main(String[] args) {

		double[] guess = {4,46,6,37,5,53,4,54,4,50,4,50};//,4,50,4,50};
		
		IObjectiveFunction fitfun = new QwopCost();

		// new a CMA-ES and set some initial values
		CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
		cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
		cma.setDimension(12); // overwrite some loaded properties
		cma.setInitialX(guess); // in each dimension, also setTypicalX can be used
		cma.setInitialStandardDeviation(50); // also a mandatory setting 
		cma.options.stopFitness = 1e-2;       // optional setting

		// initialize cma and get fitness array to fill in later
		double[] fitness = cma.init();  // new double[cma.parameters.getPopulationSize()];

		// initial output to files
		cma.writeToDefaultFilesHeaders(0); // 0 == overwrites old files

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
			QwopCost.visOn = true;
			
			fitfun.valueOf(bestEver);
			
		// we might return cma.getBestSolution() or cma.getBestX()

	} // main  
} // class
