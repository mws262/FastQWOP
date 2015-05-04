package org.jbox2d.testbed.framework.jogl;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
//import org.jbox2d.testbed.framework.TestbedSettings;

public class RunQWOP {


	public RunQWOP() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String args[]) throws InterruptedException{
		QWOPNEW2 game = new QWOPNEW2(true);
		
		World m_world = game.getWorld();
		float timestep = 0.04f;
		int positerations = 5;
		int veliterations = 1;
		int delaymillis = (int)(timestep*1000);
			
		long startTime = System.nanoTime();
		
		for (int j = 0; j<20; j++){
			game.everyStep(false,false, false, false);
			m_world.step(timestep, veliterations, positerations);
			Thread.sleep((long)delaymillis);
		
		}
		
		for (int i = 0; i <1000; i++){
			
			for (int j = 0; j<55; j++){
				game.everyStep(false,true, true, false);
				m_world.step(timestep, veliterations, positerations);
				Thread.sleep((long)delaymillis);
				
				
			}
			for (int k = 0; k<15; k++){
				game.everyStep(false,false, false, false);
				m_world.step(timestep, positerations,veliterations);
				Thread.sleep((long)delaymillis);
			}
			
			for (int l = 0; l<55; l++){
				game.everyStep(true,false, false, true);
				m_world.step(timestep, positerations,veliterations);
				Thread.sleep((long)delaymillis);
			}
			for (int m = 0; m<15; m++){
				game.everyStep(false,false, false, false);
				m_world.step(timestep, positerations,veliterations);
				Thread.sleep((long)delaymillis);
			}
//			m_world.step(timestep, 5,5);
//			Body testbody = m_world.getBodyList().getNext();
		}
		long endTime = System.nanoTime();
		
		System.out.println((endTime - startTime)/1e9);
		
	}

}
