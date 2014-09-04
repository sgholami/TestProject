package stupidmodel.common;

import java.util.ArrayList;

public class VillageReturnsMsg {
	
	private int villageNumber;
	private ArrayList <HouseholdReturnsMessage> hhMsgs = null;
	
	public VillageReturnsMsg (int villageNum){
		this.villageNumber = villageNum;
		hhMsgs = new ArrayList<HouseholdReturnsMessage> ();
	}
	
	public void addHouseholdReturnMsg (HouseholdReturnsMessage hhmessage){
		hhMsgs.add(hhmessage);
	}
	
	public ArrayList <HouseholdReturnsMessage>  getHouseholdReturnMsgList(){	
		return this.hhMsgs;
	}
	
	public int getVID(){
		return this.villageNumber;
	}

}
