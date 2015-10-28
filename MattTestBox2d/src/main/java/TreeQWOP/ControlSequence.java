package TreeQWOP;

/**
 * 
 * Generic way to define a sequence of QWOP key presses. 
 * 
 * @author Matt
 *
 */
public class ControlSequence {

	private int phase = -1; // phase should go 0 to (totalActions - 1)
	private int totalActions = 0;
	
	boolean[][] sequence;
	
	/** Input defines the sequence. nx4 array. n is the number of actions in the sequence.
	 *  4 represents QWOP true/false respectively. true is pressed.
	 **/
	public ControlSequence(boolean[][] sequence) {
		this.sequence = sequence;
		totalActions = sequence.length;
	}
	
	/** Return next action queued. **/
	public boolean[] getNext(){
		++phase;	
		if(phase >(totalActions-1)){
			phase = 0;
		}
		boolean[] thisAction = sequence[phase];

	
		return thisAction;
	}
	
	/** Get the number we are through the sequence. Zero-indexed. **/
	public int getPhase(){
		return phase;
	}
	/** Get the total number of actions in the sequence. **/
	public int getActionCount(){
		return totalActions;
	}
	/** Reset to fresh sequence. **/
	public void reset(){
		phase = -1;
	}
	

}
