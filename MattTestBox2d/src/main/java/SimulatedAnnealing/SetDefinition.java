package SimulatedAnnealing;

import java.util.Random;

public class SetDefinition {

	// Set of all possible choices of actions from which to build the tree. Each tree will be built using a subset.
private int[][] superset;
	
    private static Random rand = new Random();

	public SetDefinition(int[][] superset) {
		this.superset = superset;
	}
	
	/** Mutate original, put the result in putHere. Number of mutations to be done can be specified **/
	public void mutate(int[][] original, int[][] putHere, int numMutations){
		fillArray(original,putHere); //Make it so the source and destination are initially the same.

		int[] mutIndices = new int[numMutations];
		
		// num mutations # of rows to mutate. Rows between 0 and 15 in this case.
		for (int i = 0; i<numMutations; i++){
			mutIndices[i] = randInt(0,superset.length-1);
		}
		
		for (int i = 0; i<numMutations; i++){
			if(superset[mutIndices[i]].length - original[mutIndices[i]].length == 0){
				continue;
			}
			int indexInQuestion = randInt(0,original[mutIndices[i]].length-1);
			int newIndChoice = randInt(1,superset[mutIndices[i]].length - original[mutIndices[i]].length); //index of chosen int not contained by original inside the superset
			
			//TODO Fix all these local vars.
			int count = 0;
			int iter = 0;
			int newChoice = 0;
			boolean found = false;
			while (!found){
				newChoice = superset[mutIndices[i]][iter];
				
				boolean goodflag = true;
				for(int j = 0; j<original[mutIndices[i]].length; j++){
					if (newChoice == original[mutIndices[i]][j]){
						goodflag = false;
						break;
					}
				}
				if(goodflag){
					count++;
					if(count == newIndChoice){
						found = true;
					}
				}
				iter++;
			}
			
			putHere[mutIndices[i]][indexInQuestion] = newChoice;
		}
		
	}
	
	/** Random int between min and max inclusive. **/
	private static int randInt(int min, int max) {

		if(min == max){ // prevent errors.
			return min;
		}
		if(min>max){
			return 1;
		}
	    int randomNum = rand.nextInt((max - min) + 1) + min;

		
	    return randomNum;
	}
	
	/** Fill dest with the contents of source. Must have the same dimensions. **/
	public static void fillArray(int[][] source, int[][] dest){
		for (int l = 0; l< source.length; l++){
			for (int j = 0; j<source[l].length; j++){
				dest[l][j] = source[l][j];
				
			}
		}
	}
	
	/** Print a sequence to the console **/
	public static void printSequence(int[][] sequence){
		for (int i = 0; i< sequence.length; i++){
			for (int j = 0; j<sequence[i].length; j++){
				System.out.print(sequence[i][j] + ",");
				
			}
			System.out.println();
			
		}
	}
	
	/** This one will keep any row of the actionlist consecutive. It may either shift everything or remove/add bookend values **/
	public void mutateOrdered(int[][] original, int[][] putHere, int numMutations){
		fillArray(original,putHere);
		int[] mutIndices = new int[numMutations];
		// num mutations # of rows to mutate. Rows between 0 and 15 in this case.
		for (int i = 0; i<numMutations; i++){
			mutIndices[i] = randInt(0,superset.length-1);
		}
		
		for (int i = 0; i<mutIndices.length; i++){
			int choice = randInt(0,1);
			if (choice == 0){
				choice =-1;
			}else{
				choice = 1;
			}
			shift(choice, original[mutIndices[i]], putHere[mutIndices[i]],superset[mutIndices[i]]);
			
			
		}
	}
	
	private void shift(int magnitude, int[] originalRow, int[] putRowHere, int[] supersetRow){
		
		//Figure out what the current offset is:
		int oldOffset = 0;
		boolean match = false;
		try{
		while(!match){
			for(int i = 0; i<originalRow.length; i++){
				if(originalRow[i] != supersetRow[i+oldOffset]){
					match = false;
					oldOffset++;
					break;
				}
				match = true; //will finish as true if all match.
			}
		}
		}catch(ArrayIndexOutOfBoundsException e){ //TODO FIX THIS
			for (int i =0;i< originalRow.length; i++){
				System.out.print(originalRow[i] + ",");
			}
			System.out.println();
			for (int i = 0; i<supersetRow.length; i++){
				System.out.print(supersetRow[i]+",");
			}
			System.out.println();
		}
		
		//Make sure the resulting transform is within the superset's bounds
		if(oldOffset + magnitude + originalRow.length > supersetRow.length){
			magnitude -= 1;
			
		}else if(oldOffset + magnitude < 0){
			magnitude += 1;
		}else if(supersetRow.length == originalRow.length){
			return;
		}
			
			
		
		//Actually change the values in putrowhere array.
		for (int i = 0; i< originalRow.length; i++){
		}	
	}

}
