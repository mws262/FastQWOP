//Simple tester to run sequences of actions based on the 0,WO,0,QP pattern
package org.jbox2d.testbed.framework.jogl;

public class TestSequence {

	public TestSequence() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String args[]){
//		0, 25, 5, 25, 10, 65, 5, 65. 3rd
//		0, 25, 15, 50, 25, 65, 0, 65. 2nd
//		0, 25, 15, 65, 20, 60, 30, 65 1st
		//Sequence to test
		int[] delay = {0, 25, 15, 50, 25, 65, 0, 65};
		
		
		
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
