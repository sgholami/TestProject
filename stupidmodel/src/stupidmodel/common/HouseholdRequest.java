package stupidmodel.common;

public class HouseholdRequest {
	
	private int villageNumber;
	private int hhNumber;
	private int wheatLandNeed;
	private int barleyLandNeed;
	private int ocLandNeed;
	private int population;
	public HouseholdRequest(int village, int hh, int wheatLandNeeded, int barleyLandNeeded, int ocLand, int pop){

		this.villageNumber = village;
		this.hhNumber = hh;
		this.wheatLandNeed = wheatLandNeeded;
		this.barleyLandNeed = barleyLandNeeded;
		this.ocLandNeed = ocLand;
		this.population = pop;
	}
	
	public int getVillageNum(){		
		return this.villageNumber;
	}
	public int gethhNumber(){		
		return this.hhNumber;
	}
	public int getwheatLandNeed(){		
		return this.wheatLandNeed;
	}
	public int getbarleyLandNeed(){		
		return this.barleyLandNeed;
	}
	public int getOCLandNeed(){		
		return this.ocLandNeed;
	}
	public int getPopulation(){		
		return this.population;
	}

}
