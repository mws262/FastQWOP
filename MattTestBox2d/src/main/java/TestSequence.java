//Simple tester to run sequences of actions based on the 0,WO,0,QP pattern


public class TestSequence {

	private static QWOPInterface game;
	public TestSequence() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String args[]){
//		0, 25, 5, 25, 10, 65, 5, 65. 3rd
//		0, 25, 15, 50, 25, 65, 0, 65. 2nd
//		0, 25, 15, 65, 20, 60, 30, 65 1st
		//Sequence to test
		//0, 25, 15, 52, 26, 64, 1, 68
		//0, 21, 15, 50, 26, 70, 0, 69
		int[] delay = {2, 25, 18, 50, 0, 45, 0, 45, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};//0, 25, 19, 51, 5, 69, 0, 59};//0, 25, 19, 51, 5, 69, 0, 59 };
			//{0, 26, 17, 55, 24, 69, 2, 61};//0, 25, 14, 51, 27, 66, 5, 64};//0, 25, 14, 49, 27, 68, 1, 61};//0, 25, 14, 51, 27, 66, 0, 64};//0, 23, 14, 50, 26, 65, 0, 61};
		
		game = new QWOPInterface();
		game.NewGame(true);
		VisMasterSMALL vis = new VisMasterSMALL(game);
		vis.RunMaker.ActivateTab();
		Scheduler EveryPhys = new Scheduler();
		vis.RunMaker.setInterval(1);
		EveryPhys.addTask(vis.RunMaker);
		game.addScheduler(EveryPhys);

		game.repeatSequence= true;

		try {
			game.DoSequence(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
