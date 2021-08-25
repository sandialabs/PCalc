package gov.sandia.gmp.bender.visualize;

public enum PlotType {

	LAYER(0),
	RAY(1);
	
	private int index;
	
	private PlotType(int index)
	{
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}
	
}
