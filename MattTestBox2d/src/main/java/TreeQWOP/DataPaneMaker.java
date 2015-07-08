package TreeQWOP;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import java.awt.Paint;

/**
 * 
 * Handles taking the data from DataGrabber and plotting it with JFreeChart library.
 * 
 * 
 * @author Matt
 *
 */

public class DataPaneMaker implements Schedulable, ActionListener, TabbedPaneActivator, ChartMouseListener{
    
    public ChartPanel DataPane;
    
    private int interval;
    private boolean activeTab = false;
    
    
    private TreePaneMaker tree;
    
    //Datagrabber and datapanel are on different schedules.
    public DataGrabber data;
    private JFreeChart chart;
    
    private NumberAxis rangeAxis;
    private NumberAxis domainAxis;
    private CustomRenderer renderer = new CustomRenderer(false,true,this);
    
    private int ActivePlotIndex = 0;
    
    public int selectedPoint = -1;
    
    public DataPaneMaker(String name,DataGrabber data){
        this.data = data;
        chart = createChart(data,name);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.addChartMouseListener(this);

        this.DataPane = chartPanel;
        //chartPanel.setVerticalAxisTrace(true);
        //chartPanel.setHorizontalAxisTrace(true);
        // popup menu conflicts with axis trace
        

        //Use the reflected fields in DataGrabber to make "" vs "" labels for the drop down menu.
        String[] PlotLabels = new String[data.getSeriesCount()];
        for (int i = 0; i<data.getSeriesCount(); i++){
        	PlotLabels[i] = data.yLabels[i] + " vs. " + data.xLabels[i];
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
    
    /** Give us a link to the tree viewer. This lets us select points on the graph and have them show up on the tree **/
    public void setTreePane(TreePaneMaker tree){
    	this.tree = tree;
    }
    
    /** Check if the bounds need expanding, tell JFreeChart to update, and set the bounds correctly **/
    public void update(){
    	 data.UpdateBounds();
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
        
        
        
        plot.setRenderer(renderer);
        //Change markersize
        renderer.setSeriesShape( 0, new Rectangle2D.Double( -2.0, -2.0, 4.0, 4.0 ) );
//        renderer.setSeriesOutlinePaint(0, Color.black);
        renderer.setUseOutlinePaint(false);

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
	for (int i = 0; i<data.getSeriesCount(); i++){
		renderer.setSeriesVisible(i, false);
		
	}
	renderer.setSeriesVisible(index, true);
	//also change the axis labels
	domainAxis.setLabel(data.xLabels[index]);
	rangeAxis.setLabel(data.yLabels[index]);
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
	tree.TreePanel.requestFocus(); //This fixes the problem that using the dropdown menu changes focus to the dropdown menu and makes the tree un-interactable.
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

@Override
public void chartMouseClicked(ChartMouseEvent event) {
	 ChartEntity entity = event.getEntity();
	   if (entity == null)
	      return;
	   
	   try{
	   selectedPoint = ((XYItemEntity)entity).getItem();

	   tree.TreePanel.setFocusNode(data.NodeList.get(selectedPoint));
	   
	
	   }catch(ClassCastException e){
		   //We've tried to select a point before one exists.
	   }
	
}

@Override
public void chartMouseMoved(ChartMouseEvent event) {
	// TODO Auto-generated method stub
	
}
    
}

class CustomRenderer extends XYLineAndShapeRenderer {
DataPaneMaker pane;
Rectangle2D BigMarker = new Rectangle2D.Double( -5.0, -5.0, 10.0, 10.0 );
Color SelectedColor = new Color(0.5f,1,0.5f);
Color UnSelectedColor = new Color(1f,0.25f,0.25f);
    public CustomRenderer(boolean lines, boolean shapes, DataPaneMaker pane) {
        super(lines, shapes);
        this.pane = pane;
    }

    @Override
    public Paint getItemPaint(int row, int col) {
        if (col == pane.selectedPoint) {
            return SelectedColor;
        } else {
            return UnSelectedColor;
        }
    }
    @Override
    public Shape getItemShape(int row, int col){
        if (col == pane.selectedPoint) {
            return (Shape)BigMarker;
        } else {
            return super.getItemShape(row, col);
        }
    	
    }
}