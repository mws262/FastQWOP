import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


//public class MetaManager {
//
//	
//	static CopyOnWriteArrayList<TreeHandle> trees = new CopyOnWriteArrayList<TreeHandle>();
//	static SimpleDistVal distVal = new SimpleDistVal();
//	public MetaManager() {
//		// TODO Auto-generated constructor stub
//	}
//
//	public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
//
//		ExhaustiveQwop eq = new ExhaustiveQwop(trees);
//		
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
//		
//		eq.idleGraphics();
//
//
//	}

	



	import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

	
	class QWOPSet implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible
		static CopyOnWriteArrayList<TreeHandle> trees = new CopyOnWriteArrayList<TreeHandle>();
		static SimpleDistVal distVal = new SimpleDistVal();
		public static ExhaustiveQwop eq;
		float treelevel = 0;
		public QWOPSet() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
			eq = new ExhaustiveQwop(trees);
		}
		public double valueOf (double[] x) {
			distVal.clearTotal();
			TreeParameters tp = new TreeParameters();
			tp.TreeLevel = treelevel;
			int[] intX = new int[x.length];
			
			//Turn all the doubles into ints.
			for (int i = 0; i<x.length; i++){
				intX[i] = (int)Math.round(x[i]);
			}
			int count = 0;
			for (int i = 0; i<tp.ActionList.length; i++){
				for (int j = 0; j<tp.ActionList[i].length; j++){
					tp.ActionList[i][j] = intX[count];
					count++;
				}
			}
			
			
			try {
				eq.RunGame(tp);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			trees.get(trees.size()-1).getRoot().evalValueFunctionTree(distVal);
			System.out.println(distVal.getTotalValue());

			treelevel+= 500f;
			return -distVal.getTotalValue();
		}
		public boolean isFeasible(double[] x) {

			for(int i = 0; i<x.length; i++){
				if (x[i]<0.){
					System.out.println('d');
					return false;
				}
			}
			return true;
		} // entire R^n is feasible
		
		public void setIdle(){
			eq.idleGraphics();
		}
	}

	public class MetaManager {
		
		
		public static void main(String[] args) {
			IObjectiveFunction fitfun = null;
			
			try {
				fitfun = new QWOPSet();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// new a CMA-ES and set some initial values
			CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
			cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties
			cma.setDimension(83); // overwrite some loaded properties
			cma.setInitialX(0.05); // in each dimension, also setTypicalX can be used
			cma.setInitialStandardDeviation(5); // also a mandatory setting 
			cma.options.stopFitness = 1e-6;       // optional setting
			
			 double[]ActionList = {
					
					0,
					26,
					19,
					20,25,30,35,40,45,49,50,51,52,
					
					0,1,2,3,
					45,46,47,48,49,50,51,52,53,54,55,
					0,1,2,3,
					45,46,47,48,49,50,51,52,53,54,55,	
					
					0,1,2,3,
					45,46,47,48,49,50,51,52,53,54,55,
					0,1,2,3,
					45,46,47,48,49,50,51,52,53,54,55,	
				
					0,1,2,
					45,46,47,
					1,2,
					50,51,
				
					};
cma.setInitialX(ActionList);
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
	            	    if(fitfun != null){                                   // assumes that the feasible domain is convex, the optimum is  
					while (!fitfun.isFeasible(pop[i]))     //   not located on (or very close to) the domain boundary,  
						pop[i] = cma.resampleSingle(i);    //   initialX is feasible and initialStandardDeviations are  
	                                                       //   sufficiently small to prevent quasi-infinite looping here
	                // compute fitness/objective value	
					fitness[i] = fitfun.valueOf(pop[i]); // fitfun.valueOf() is to be minimized
	            	    }
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
			
			double[] sol1 = cma.getBestSolution().getX();
			for(int i = 0; i< sol1.length; i++){
				System.out.print(sol1[i] + ",");
				
			}
				
			// we might return cma.getBestSolution() or cma.getBestX()
QWOPSet.eq.idleGraphics();
		} // main  
	} // class

