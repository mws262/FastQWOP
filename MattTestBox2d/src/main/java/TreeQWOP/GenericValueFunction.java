package TreeQWOP;
/** General form that a value function should take. 
 * This can be passed to trialnodes for recursive evaluation through a tree 
 * @author Matt
 **/
public interface GenericValueFunction {

	/** Add the value of this trialnode to a running total **/
	public void addToValue(TrialNode t);
	/** Evaluate the value of a single trialnode **/
	public float evaluateSingle(TrialNode t);
	/** Filter whether this node meets the conditions to be part of the value function **/
	public boolean isValidNode(TrialNode t);
	/** Clear the running value total **/
	public void clearTotal();
	/** Get the total value so far **/
	public float getTotalValue();
	
	/** Do everything to make the tree and evaluate it. **/
	public float RunAndEval(TreeParameters tp);
}