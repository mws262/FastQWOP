
public class MetaManager {

	public MetaManager() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
//		GraphicsHandler GH = new GraphicsHandler();
//		GH.addUI();
		
		ExhaustiveQwop eq = new ExhaustiveQwop();
		
		TreeParameters tp1 = new TreeParameters();
		
		eq.RunGame(tp1);
		
		TreeParameters tp2 = new TreeParameters();
		tp2.TreeLevel = 1000;

		eq.RunGame(tp2);

	}

}
