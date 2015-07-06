import java.util.ArrayList;


public class MetaManager {

	
	static ArrayList<TreeHandle> trees = new ArrayList<TreeHandle>();
	
	public MetaManager() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
//		GraphicsHandler GH = new GraphicsHandler();
//		GH.addUI();
		ExhaustiveQwop eq = new ExhaustiveQwop(trees);
		
		for (int i = 0; i<10; i++){
			
			for(TreeHandle th: trees){
				th.focus = false;
			}
			TreeParameters tp1 = new TreeParameters();
			tp1.TreeLevel = 500*i;
			eq.RunGame(tp1);
			
		}
		
		eq.idleGraphics();


	}

}
