package TreeQWOP;

import java.util.concurrent.CopyOnWriteArrayList;

/** 
 * Simple value function which sums the distances at the endpoint nodes to come up with a score.
 * 
 * @author Matt
 *
 */
public class SimpleDistVal implements GenericValueFunction {

	private float totalVal = 0;
	private ExhaustiveQwop eq;
	private CopyOnWriteArrayList<TreeHandle> trees;
	
	public SimpleDistVal(ExhaustiveQwop eq) {
		this.eq = eq;
		trees = eq.getTrees();
	}

	@Override
	public void addToValue(TrialNode t) {
		totalVal += t.rawScore;

	}

	@Override
	public float evaluateSingle(TrialNode t) {
		return t.rawScore;
	}

	@Override
	public boolean isValidNode(TrialNode t) {
		
		return t.DeadEnd; //Valid if it's a dead end node (ie end of the tree).
	}

	@Override
	public void clearTotal() {
		totalVal = 0;

	}

	@Override
	public float getTotalValue() {
		return totalVal;
	}

	@Override
	public float RunAndEval(TreeParameters tp) {
		if(eq == null){
			throw new RuntimeException("Must have a reference to a valid exhaustive qwop runner to evaluate the value in a generic cost function.");
		}
		clearTotal();
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
		//This is a really, really stupid way to do this TODO fix it.
		trees.get(trees.size()-1).getRoot().evalValueFunctionTree(this);
		
		for(int i = 0; i<trees.size(); i++){
			trees.get(i).focus = false;
		}
		System.out.println(getTotalValue());
		return getTotalValue();
	}

}
