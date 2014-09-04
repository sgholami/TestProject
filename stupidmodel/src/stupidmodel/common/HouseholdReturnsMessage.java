package stupidmodel.common;

import java.util.ArrayList;

public class HouseholdReturnsMessage {
	

	private int hhNumber;
	private ArrayList <FarmData> farmHoldings = null;
	private double ocReturns;
	private double ocReturnArea;

	public HouseholdReturnsMessage (int hh){

		this.hhNumber = hh;
		farmHoldings = new ArrayList<FarmData> ();
	}
	
	public int gethhNumber(){
		return this.hhNumber;
	}
	
	public void addFarm (FarmData newFarm){
		farmHoldings.add(newFarm);
	}
	
	public ArrayList <FarmData> getFarms(){
		
		return this.farmHoldings;
	}
	
	public void setOCReturns (double returns){
		this.ocReturns = returns;
	}
	
	public double getOCReturns(){
		return this.ocReturns;
	}
	
	public void setOCReturnArea (double returnsArea){
		this.ocReturnArea = returnsArea;
	}
	
	public double getOCReturnArea(){
		return this.ocReturnArea;
	}
	
	
}
