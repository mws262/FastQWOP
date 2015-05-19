import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class VisTree extends JFrame{

	public static int TreeDepthOffset = 0;
	
	  /** This is the root node from which the tree will be built out of **/
	  public TrialNode root;
	
	  /** Jpanel **/
	  public DrawPane panel;
	  
	  /** Object which holds current line data **/
	  LineHolder Lines;

	
	//When creating a new visualizer, wee need to know the root node so we can run down the tree and draw it.
	public VisTree(TrialNode root) {
		this.root = root;
        panel = new DrawPane();
        setContentPane(panel);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(OptionsHolder.windowWidth, OptionsHolder.windowHeight);

        setVisible(true); 
	}
	
	/** Call this externally to force a full update of the tree. This will go throuigh all nodes, collect lines, and tell the graphics to update **/
	public void UpdateTree(){
		if(root.PotentialChildren == 1){ //If the root node only has one child, then we're just going to move down and call the next node root for drawing purposes.
			root = root.GetChild(0);
		}
		
		 Lines = root.GetNodeLines();
		 if (!Lines.equals(null)){
			panel.setTree(Lines);
		 	panel.repaint();
		 }
	}

    /** Jpanel inside the jframe **/
    class DrawPane extends JPanel{
  	  LineHolder Lines;
  	  
      public void paintComponent(Graphics g){
    	  //Go through and draw all the lines defined.

      	for (int i = 0; i<Lines.numLines; i++){
      		g.drawLine(Lines.LineList[i][0], Lines.LineList[i][1], Lines.LineList[i][2], Lines.LineList[i][3]);
      	}
       }
      /** Set the LineHolder to pay attention to **/
      public void setTree(LineHolder Lines){
      	this.Lines = Lines;
      }
   }
    
    
	

}
