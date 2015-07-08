package TreeQWOP;
/**
 * 
 * Things that all my schedulable tasks must have.
 * 
 * @author Matt
 *
 */

public interface Schedulable {
	
	/** After how many iterations do we execute the DoScheduled method **/
	public void setInterval(int interval);
	
	/** Return what the interval is **/
	public int getInterval();
	
	/** Do the stuff that is only scheduled to happen every (interval) iterations **/
	public void DoScheduled();
	
	/** Things that should happen every iteration **/
	public void DoEvery();
	
	/** Make it execute now regardless **/
	public void DoNow();
	
	/** Disable the action **/
	public void Disable();
	
	

}
