import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;


public class QWOPPlotData extends AbstractXYDataset 
implements XYDataset, DomainInfo, RangeInfo {

	public QWOPPlotData() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Range getRangeBounds(boolean includeInterval) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getRangeLowerBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRangeUpperBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Range getDomainBounds(boolean includeInterval) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDomainLowerBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDomainUpperBound(boolean includeInterval) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemCount(int series) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Number getX(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getY(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSeriesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		// TODO Auto-generated method stub
		return null;
	}

}
