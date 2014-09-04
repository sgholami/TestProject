package stupidmodel.common;

public class VillageData {
	
	public final int households;
	public final float villageX;
	public final float villageY;

	
	/**
	 * VillageData
	 * @param id - a unique id number for the village
	 * @param loc - site location (GRASS e-n format)
	 */
	public VillageData ( int hs, float vX, float vY)
	{
		this.households = hs;
		this.villageX = vX;
		this.villageY = vY;	
	}
	
	public float getEW(){
		return this.villageX;
	}
	
	public float getNS(){
		return this.villageY;
	}
	
	public int getHHCount(){
		return this.households;
	}

}
