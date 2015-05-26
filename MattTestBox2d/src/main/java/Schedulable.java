
public abstract class Schedulable {
	
	final int interval;
	public Schedulable(int scheduledInterval) {
		this.interval = scheduledInterval;
	}
	
	abstract void DoScheduled();
	
	abstract void DoEvery();

}
