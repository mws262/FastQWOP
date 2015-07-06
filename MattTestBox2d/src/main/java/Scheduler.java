import java.util.ArrayList;

/**
 * 
 * Lets me add things classes which extend schedulable. From my main search, call Iterate every time a counted action happens.
 * This will then run certain tasks every time and certain ones every n iterations.
 * 
 * @author Matt
 *
 */
public class Scheduler {

	private int count = 0;
	private ArrayList<Schedulable> TaskList = new ArrayList<Schedulable>();
	
	
	public Scheduler() {
		// TODO Auto-generated constructor stub
	}
	
	public void Iterate(){
		CheckTasks();
		count++;
	}
	
	private void CheckTasks(){
		
		for (Schedulable task : TaskList){
			if(task != null){
				if (task.getInterval() != 0 && count%task.getInterval() == 0 && count != 0){
					task.DoScheduled();
				}
				task.DoEvery();
			}
		}	
	}
	
	public void addTask(Schedulable task){
		TaskList.add(task);
	}
}
