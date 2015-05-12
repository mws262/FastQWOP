import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class deals with making a state discretizations, updating value functions, and interpolating when needed.
 * 
 * @author Matt
 *
 */


public class Discretizer {
	
	/** Do I want to see diagnostic info? **/
	public static boolean verbose = true;

	private static ArrayList<float[]> StateLists = new ArrayList<float[]>();
	
	/** Keep a list of the bounds for each added dimension. All contained arrays should be 1x2 **/
	private static ArrayList<float[]> stateBounds = new ArrayList<float[]>();
	
	/** Increments of each dimension discretization. Should always be same length ArrayList as stateBounds **/
	private static ArrayList<Float> increments = new ArrayList<Float>();
	
	/** Number of gridpoints in each dimension **/
	private static ArrayList<Integer> dimGridPts = new ArrayList<Integer>();
	
	private static int numDims = 0;
	
	private static int numStates = 0;

	private static float[][] MeshedStates;
	
	public Discretizer() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String args[]){
		AddDimension(0f, 3f, 0.4f);
		AddDimension(-1f, 2f, 0.6f);
		AddDimension(6f, 7f, 0.5f);
		AddDimension(4f,20f,5f);
		AddDimension(4f,8.5f,8);
		
		MeshStateLists();
	}
	
	/** Add a new dimension (e.g. calf angle, torso x position). Requires a minimum and maximum in a range and an increment. Works similar to min:increment:max in MATLAB. **/
	public static void AddDimension(float min, float max, float increment){ //This one takes an increment and calculates number of required grid points
		
		//Make an individual list of the range. We'll worry about meshing the whole thing later.
		float[] DiscreteList = new float[(int)Math.floor((max-min)/increment)+1];
		
		for (int i = 0; i<DiscreteList.length; i++){
			DiscreteList[i] = min + i*increment;
		}
		StateLists.add(DiscreteList); //Add it to our running list of states.
		if(verbose) System.out.println("Added a new dimension discretized into " + DiscreteList.length + " elements.");
		
		
		//Make the bound list and increment list.
		float[] bounds = {min, max};
		StateLists.add(bounds);
		increments.add(increment);
		dimGridPts.add(DiscreteList.length);
		//Record number of dimensions added.
		numDims++;
		//Record total number of states.
		if(numStates == 0){
			numStates = DiscreteList.length;
		}else{
			numStates *= DiscreteList.length;
		}
	}

	/** Add a new dimension (e.g. calf angle, torso x position). Requires a minimum and maximum in a range and an increment. Works similar to linspace in MATLAB. **/
	public static void AddDimension(float min, float max, int gridPts){ //This one takes the number of grid points and calculates the size of the required increment.
		
		//Make an individual list of the range. We'll worry about meshing the whole thing later.
		float[] DiscreteList = new float[gridPts];
		
		float increment = (max-min)/(float)(gridPts-1);
		for (int i = 0; i<DiscreteList.length; i++){
			DiscreteList[i] = min + i*increment;
		}
		StateLists.add(DiscreteList); //Add it to our running list of states.
		if(verbose) System.out.println("Added a new dimension discretized into " + DiscreteList.length + " elements.");
		
		
		//Make the bound list and increment list.
		float[] bounds = {min, max};
		StateLists.add(bounds);
		increments.add(increment);
		dimGridPts.add(DiscreteList.length);
		//Record number of dimensions added.
		numDims++;
		//Record total number of states.
		if(numStates == 0){
			numStates = DiscreteList.length;
		}else{
			numStates *= DiscreteList.length;
		}
	}
	
	/** Take all the previously added dimensions and create all possible combinations in an array. This may become unnecessary once barycentric interpolation is worked out. **/
	public static void MeshStateLists(){

		//Calculate the total number of states (e.g. a 3x3x3 space has 27 states).
		int totStates = 1;
		for (int i = 0; i<StateLists.size(); i++){
			totStates *= StateLists.get(i).length;
		}
		if(verbose){
			System.out.println("Number of dimensions (i.e. states): " + StateLists.size());
			System.out.println("Total number of states in the discretization (i.e. all possible combinations): " + totStates);
		}
		
		MeshedStates = new float[totStates][StateLists.size()]; //This is number of states x number of dimensions.
		
		int repeatedNumbers = 0;
		int repeatedPatterns = 0;
		int trackedRepetition = 1;
		for (int i = StateLists.size()-1; i>=0; i--){ //Iterate through all the state lists contained in this arraylist. We'll count down just to keep continuity with other stuff I've done.
			int counter = 0; //Track how far down the array we are on this pass.
			
			repeatedPatterns = totStates/(StateLists.get(i).length*trackedRepetition); //First time down, the pattern will be repeated
			repeatedNumbers = trackedRepetition;
			
			trackedRepetition *= StateLists.get(i).length; //First time through, the pattern will be repeated by length/numstates. Next time it will only be repeated length/(numstates*numprevious states)
			//e.g. first time through, might be: 1 2 3 1 2 3 1 2 3. Next time: 1 1 1 2 2 2 3 3 3.
			
			for (int j = 0; j< repeatedPatterns; j++){ //Now iterate through the number of repetitions of the pattern.
				for(int k = 0; k<StateLists.get(i).length; k++){ //Now iterate through the individual values found in this particular state list.
					for(int l = 0; l<repeatedNumbers;l++){ //Now iterate through the number of times each value is repeated.
						MeshedStates[counter][i] = StateLists.get(i)[k];
						
						counter++;
					}
				}
			}
			//Make SURE we've populated the matrix up to the end. If not, I did the previous loops wrong.
			if (counter != totStates){ throw new IndexOutOfBoundsException("When populating the state array, we ran out of values too early. Indexing issue.");}
		}
		
		//Print fully meshed state list for debugging purposes.
//		for (int i = 0; i< MeshedStates.length; i++){
//			for (int j = 0; j<MeshedStates[0].length-1; j++){
//				System.out.print(MeshedStates[i][j] + ",");
//			}
//			System.out.print(MeshedStates[i][MeshedStates[0].length-1]);
//			System.out.println();
//		}
	}
	
	/** K nearest neighbor if barycentric doesn't work out **/
	public static int FindNearestState(float[] state){	
		return 1;
	}
	
	/**Barycentric interpolation on the defined grid. Should return the nearest gridpoints on a simplex and weights for each **/
	public static void BaryTerpolate(float[] state){
		
		/* Fix out of bounds states */ 
		for (int i = 0; i<state.length; i++){ //State is lower than min bound.
			if (state[i]<stateBounds.get(i)[0]){ 
				//Report how off we are then project back.
				if (verbose){ System.out.println("State " + i + " is " + (stateBounds.get(i)[0] - state[i]) / (stateBounds.get(i)[1] - stateBounds.get(i)[0]) + "% of full range below the min. Projected back.");}
				state[i] = stateBounds.get(i)[0];
				
			}else if (state[i] > stateBounds.get(i)[1]){ //State is higher than max bound.
				//Report how off we are then project back.
				if (verbose){ System.out.println("State " + i + " is " + (state[i] - stateBounds.get(i)[1]) / (stateBounds.get(i)[1] - stateBounds.get(i)[0]) + "% of full range above the max. Projected back.");}
				state[i] = stateBounds.get(i)[1];
				
			}	
		}
		
		/* Translate and scale such that each grid cell is a unit cube. */
		float[] scaledState = new float[state.length];
		int[] xBase = new int[state.length];
		float[] cubeX = new float[state.length];
		
		for (int i = 0; i< state.length; i++){
			scaledState[i] = (state[i] - stateBounds.get(i)[0])/(stateBounds.get(i)[1] - stateBounds.get(i)[0])*dimGridPts.get(i); //Subtract out the low bound. Divide by full scale. Multiply by number of bins (bin-space representation).
			xBase[i] = (int)Math.floor(scaledState[i]); //dist to base corner of hypercube in normalized units
			cubeX[i] = scaledState[i] - (float)xBase[i]; //dist within this hypercube to the point in normalized units.
		}
		
		//Sort step:
		int[] I = {1,2,3,4};
		
		//Simplex always has nDim+1 corners.
		
		// zeros(nDim,nDim+1)
		float[][] X = new float[state.length][state.length+1];
		Arrays.fill(X, 0f);
		// zeros(nDim+1,1)
		float[] w = new float[state.length+1];
		Arrays.fill(w, 0);
		
		float wSum = 0;
		
		for (int i = 0; i<state.length; i++){
			Arrays.fill(X[I[i]],state.length+1-i,state.length+1,1f);
			w[state.length+1-i] = cubeX[I[i]] - wSum;
			wSum += w[state.length+1-i];	
		}
		
		w[0] = 1-wSum;
		
			
		
		
	}
	
}


