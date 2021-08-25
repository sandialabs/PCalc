package gov.sandia.gmp.bender.visualize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

import gov.sandia.geotess.GeoTessPosition;
//import gov.sandia.gmp.bender.phase.Phase;
//X import gov.sandia.gmp.geomodel.InterpolatedNodeLayered;
//X import gov.sandia.gmp.geomodel.Phase;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;

public class RayVisualizerChartFrame extends JFrame {

	private static final long serialVersionUID = -8019450313480283427L;
	private XYPlot plot;
	private XYSeriesCollection layers;

	public RayVisualizerChartFrame() {
		super();
		JFreeChart dataChart = ChartFactory.createXYLineChart("","Distance (degrees)", "Depth (km)", null,PlotOrientation.VERTICAL, true, false, false);
		ChartPanel chart = new ChartPanel(dataChart);
		chart.getChart().getXYPlot().getRangeAxis().setInverted(true);

        LegendTitle legend = new LegendTitle(dataChart.getPlot());
        legend.setItemFont(new Font("Courier", Font.PLAIN, 14));       
        legend.setPosition(RectangleEdge.RIGHT);
        
        dataChart.addSubtitle(legend);
        dataChart.removeSubtitle(dataChart.getLegend());

		this.layers = new XYSeriesCollection();
		this.plot = (XYPlot) dataChart.getPlot();	
		dataChart.setPadding(new RectangleInsets(20,20,20,20));
		XYLineAndShapeRenderer layerRenderer = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer rayRenderer = new XYLineAndShapeRenderer();

		this.plot.setRenderer(PlotType.LAYER.getIndex(), layerRenderer);
		this.plot.setRenderer(PlotType.RAY.getIndex(), rayRenderer);
		this.add(chart);
	}

	//X protected void plot(List<GeoTessPosition> nodes, Phase phase,
  //X 			GreatCircle greatCircle, PlotType plotType, String s) {
	//protected void plot(List<GeoTessPosition> nodes, Phase phase,
	//		GreatCircle greatCircle, PlotType plotType, String s) {
	protected void plot(List<GeoTessPosition> nodes,
			GreatCircle greatCircle, PlotType plotType, String s) {

		//X InterpolatedNodeLayered receiver = nodes.get(nodes.size() - 1);
		GeoTessPosition receiver = nodes.get(nodes.size() - 1);
		XYSeries series = new XYSeries(String.format("%1$-"+25+"s", s), false);
		double[] xy = new double[3];
		try {
			//X for (InterpolatedNodeLayered node : nodes) {
			for (GeoTessPosition node : nodes) {
				//greatCircle.transform(node.getVector(), xy);
				double x = node.distanceDegrees(receiver);
				double y = node.getDepth();
				series.add(x, y);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (plotType == PlotType.LAYER) {
			layers.addSeries(series);
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.plot.getRenderer(plotType.getIndex());
			renderer.setSeriesShapesVisible(layers.indexOf(series), false);
			this.plot.setDataset(plotType.getIndex(), layers);
		} else {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.plot.getRenderer(plotType.getIndex());
			renderer.setSeriesShape(0, ShapeUtilities.createDiamond(2));
			renderer.setSeriesPaint(0, Color.BLACK);
			renderer.setSeriesShapesVisible(0, true);
			this.plot.setDataset(plotType.getIndex(), new XYSeriesCollection(series));
		}
	}
	
	public void display() {
		this.setPreferredSize(new Dimension(1000, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	public void setXAxis(int lowerBound, int upperBound) {
		this.plot.getDomainAxis().setRange(lowerBound, upperBound);
	}

	public void setYAxis(int lowerBound, int upperBound) {
		this.plot.getRangeAxis().setRange(lowerBound, upperBound);
	}
	
	public void setAutoRange()
	{
		this.plot.getRangeAxis().setAutoRange(true);
	}
}
