

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

  public class Tester extends JFrame{
	  public DrawPane panel;
	  
	  public static int windowWidth = 2000;
	  public static int windowHeight = 1000;
	  public static int[] growthCenter = {windowWidth/2, windowHeight/2};
	  
     public Tester(){
          super("My Frame");

          //you can set the content pane of the frame 
          //to your custom class.


          panel = new DrawPane();
          setContentPane(panel);
          
          setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

          setSize(windowWidth, windowHeight);

          setVisible(true); 
     }

      //create a component that you can actually draw on.
      class DrawPane extends JPanel{
    	  int counter = 0;
    	  int[][] lines;
    	  
        public void paintComponent(Graphics g){
        	for (int i = 0; i<lines.length; i++){
        		g.drawLine(lines[i][0], lines[i][1], lines[i][2], lines[i][3]);
        	}
         }
        public void setTree(int[][] lines){
        	this.lines = lines;
        }
        
     }

     public static void main(String args[]){
           Tester T =  new Tester();

           
           
           int[][] lines = new int[100][4];
           
           for (int i = 0; i<100; i++){
        	   lines[i][0] = growthCenter[0];
        	   lines[i][1] = growthCenter[1];
        	   lines[i][2] = (int)(growthCenter[0]+25*Math.cos(2*Math.PI/100*i));
        	   lines[i][3] = (int)(growthCenter[1]+25*Math.sin(2*Math.PI/100*i));
        	   
               T.panel.setTree(lines);
               T.panel.repaint();
               try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        			   
           }
     }

  }
