//Simple tester to run sequences of actions based on the 0,WO,0,QP pattern


public class TestSequence {

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
		int[] delay = {0, 25, 15, 52, 23, 68, 0, 63};//0, 25, 14, 49, 27, 68, 1, 61};//0, 25, 14, 51, 27, 66, 0, 64};//0, 23, 14, 50, 26, 65, 0, 61};
		
		
		//Check 
		double[] delayD = {78.3225186340944 ,70.86491889212473 ,23.580173250066895 ,93.52296625839308 ,12.711573486621079 ,38.82488210084247 ,55.06180085373701 ,83.73338848389253};
		
		for (int i = 0; i<delay.length; i++){
			delay[i] = (int)delayD[i];	
		}
		
		QWOPInterface game = new QWOPInterface();
		game.runRealtime = true;		
		game.NewGame();
		try {
			game.DoSequence(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
