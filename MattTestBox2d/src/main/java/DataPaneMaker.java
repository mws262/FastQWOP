import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;


/**
 * 
 * Handles taking the data from DataGrabber and plotting it with JFreeChart library.
 * 
 * 
 * @author Matt
 *
 */

public class DataPaneMaker implements Schedulable, ActionListener, TabbedPaneActivator{
    
    public ChartPanel DataPane;
    
    private int interval;
    private boolean activeTab = false;
    
    //Datagrabber and datapanel are on different schedules.
    public DataGrabber data;
    private JFreeChart chart;
    
    private NumberAxis rangeAxis;
    private NumberAxis domainAxis;
    private XYLineAndShapeRenderer renderer;
    
    private int ActivePlotIndex = 0;
    
    public DataPaneMaker(String name,DataGrabber data) {
        this.data = data;
        chart = createChart(data,name);
        ChartPanel chartPanel = new ChartPanel(chart);

        this.DataPane = chartPanel;
        //chartPanel.setVerticalAxisTrace(true);
        //chartPanel.setHorizontalAxisTrace(true);
        // popup menu conflicts with axis trace
        

        //Use the reflected fields in DataGrabber to make "" vs "" labels for the drop down menu.
        String[] PlotLabels = new String[data.getSeriesCount()];
        for (int i = 0; i<data.getSeriesCount(); i++){
        	PlotLabels[i] = data.yFieldNames[i] + " vs. " + data.xFieldNames[i];
        }
        
	    JComboBox plotList = new JComboBox(PlotLabels);
	    plotList.setSelectedIndex(ActivePlotIndex);
	    AllPlotsOffExcept(ActivePlotIndex);
	    plotList.addActionListener(this);
	    
        
        chartPanel.add(plotList);
        
        
        chartPanel.setPopupMenu(null);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setVisible(true);
        chartPanel.setSize(200,400);
    }
    
    public void update(){
    	 chart.fireChartChanged();
    	 domainAxis.setRange(Range.scale(data.getXRange(ActivePlotIndex),1.1));
    	 rangeAxis.setRange(Range.scale(data.getYRange(ActivePlotIndex),1.1));
    }
   private JFreeChart createChart(XYDataset dataset,String name) {
        JFreeChart chart = ChartFactory.createScatterPlot(name,
                "X", "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
 
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setNoDataMessage("NO DATA");
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        
        renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesOutlinePaint(0, Color.black);
        renderer.setUseOutlinePaint(true);
        domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setTickMarkInsideLength(2.0f);
        domainAxis.setTickMarkOutsideLength(0.0f);
        
        rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickMarkInsideLength(2.0f);
        rangeAxis.setTickMarkOutsideLength(0.0f);

        
        return chart;
    }
/** Turn off all plots except the one specified by the index **/
private void AllPlotsOffExcept(int index){
	System.out.println(index);
	for (int i = 0; i<data.getSeriesCount(); i++){
		renderer.setSeriesVisible(i, false);
		
	}
	renderer.setSeriesVisible(index, true);
	//also change the axis labels
	domainAxis.setLabel(data.xFieldNames[index]);
	rangeAxis.setLabel(data.yFieldNames[index]);
	ActivePlotIndex = index;
	update();
}
   
@Override
public void setInterval(int interval) {
	this.interval = interval;
}

@Override
public int getInterval() {
	return interval;
}

@Override
public void DoScheduled() {
	update();
	
}

@Override
public void DoEvery() {
	//NOTHING RIGHT NOW!
}

@Override
/** Listens for the drop down menu to change **/
public void actionPerformed(ActionEvent arg0) {
	JComboBox J = (JComboBox)arg0.getSource();
	AllPlotsOffExcept(J.getSelectedIndex());
}

@Override
public void DoNow() {
	// TODO Auto-generated method stub
	
}

@Override
public void Disable() {
	// TODO Auto-generated method stub
	
}

@Override
public void ActivateTab() {
	activeTab = true;
	
}

@Override
public void DeactivateTab() {
	activeTab = false;
	
}
    
}