package stupidmodel.agents;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javolution.context.Context;
import cern.jet.random.Normal;
import fileMgmt.CreateFile;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import stupidmodel.common.FarmData;
import stupidmodel.common.HouseholdRequest;
import stupidmodel.common.HouseholdReturnsMessage;
import stupidmodel.common.ModelParameters;

public class Household {
	
	private int villageID;					// ID of current village
	private int hhID;						// ID of current HH
	private float REGION_AGENT_RES = 10.0f;
	private int year;

	///////////////////////////////
	//    SUBSISTENCE VARIABLES  //
	///////////////////////////////

	private boolean limithhSize;
	private int hhSizeLimit;
	private double wheat_calories_pc;		// wheat calories required per capita (kcal-wheat/capita/year)
	private int pct_labor;					// percent of population providing labor
	private double labor_pc;				// labor per capita (man-days/capita/year)
	private double pct_prob_birth;				// percent probability for a birth to occur
	private double avg_pct_prob_birth;			// average (and initial) percent probability for a birth, used when settling the birth % back to initial 
	private int ppl_per_birth_chk;			// number of people per birth check
	private double pct_prob_delta_birth;		// increase / decrease per probability change
	private double min_pct_prob_birth;			// minimum birth probability
	private double max_pct_prob_birth;			// maximum birth probability
	private double pct_prob_death;				// percent probability for a death to occur
	private double avg_pct_prob_death;			// average (and initial) percent probability for a death, used when settling the birth % back to initial 
	private int ppl_per_death_chk;			// number of people per death check
	private double pct_prob_delta_death;		// increase / decrease per probability change
	private double min_pct_prob_death;			// minimum death probability
	private double max_pct_prob_death;			// maximum death probability
	private int wheatLandNeed;
	private int brlyLandNeed ;
	private int grazedLandNeed; 
	private int population;
	private boolean alive; 
	private boolean wantToMigrate;
	
	///////////////////////////////
	//    FARMING VARIABLES      //
	///////////////////////////////
	private double wheat_kg_pc;				// wheat required per capita (kg-wheat/capita/year)
	private double wheat_init_yield_pha;	// initial wheat field yield per hectare (kg-wheat/ha/year)
	private double wheat_yield;				// wheat field yield (kg-wheat/ha/year)
	private double wheat_labor;				// labor required for wheat (man-days/ha/year)
	private double wheat_calories_per_kg;	// calories from wheat (kcal-wheat/kg-wheat)
	private double seed_percentage; 		//percentage of each harvest which should be devoted to planting in the following year
	private double wheat_expectation;		// in kg, farmed land need factor based upon previous year's yield
	private double barley_expectation;		// in kg, farmed land need factor based upon previous year's yield
	private double exp_yield_scalar;		// yield expectation scalar
	private double wheat_rqmt;				// household wheat requirement (kg)
	private double wheatseed;
	private double barleyseed;
	private double yield_minus_need;		// the wheat yield minus the need (kg-wheat)
	private int max_managed_land;			// maximum land cells the household can manage
	private int max_distance_cost;			// maximum distance cost from village location to land area
	private int init_max_distance_cost;		// initial max distance cost value	
	private double farmed_land_exp;			// the amount of farmed land that is expected to be required
	private float land_per_hectare;			// simulated land cells per hectare

	///////////////////////////////
	//    GRAZING VARIABLES      //
	///////////////////////////////
	private double grazed_land_exp;				// the amount of grazed land that is expected to be required
	private double grazing_impact = 1;				// impact to land cover as a result of farming
	private double total_grazing_fodder_rqmt;			// household sheep/goat total fodder requirement
	private double avg_fodder_return;			// grazing land need factor based upon previous year's yield
	private double ovicaprid_fodder_yield;
	private double ovicaprid_fodder_yield_area;
	private double init_grazing_expectation = 250; // 
	private double ovicaprid_per_person;
	private double ovicaprid_population;
	private double average_fodder_requirement;// combination of sheep and goat needs, kg/ha
	private int sheep_ratio;
	private int goat_ratio;
	private double ovicaprid_density_factor;
	private int sheep_fodder ;			//kg / year average fodder requirement
	private int goat_fodder ;			//kg / year average fodder requirement
	private double barley_fodder_percentage;
	private boolean fallow_field_grazing;
	private double fodder_from_ff_grazing; // needs to be per /ha
	private double grazing_yield_minus_need;
	private double avg_sheep_caloric_value;
	private double avg_goat_caloric_value;
	private double meat_milk_calories_pc;	// meat and milk calories required per capita (kcal-mm/capita/year)
	private double fodderyieldfromff;
	private double wheat_yield_minus_need;
	private double total_calories_pc;
	private double total_kcal_reqmt;
	private double avg_ovicaprid_caloric_value;
	private double average_fodder_requirement_from_grazing_per_animal;
	private double average_fodder_requirement_from_barley_per_animal;
	private double barley_rqmt;				// household barley requirement which is included as ovicparid fodder (kg)
	private double barley_init_yield_pha;	// initial barley field yield per hectare (kg-barley/ha/year)
	private double barley_yield;			// barley field yield (kg-barley/ha/year)

	private ArrayList <FarmData> farmsOwned = null;
	
	public Household(ModelParameters mp) {
		super();
		
		ArrayList farmsOwned = new ArrayList<FarmData>();

		this.population = mp.init_hh_pop;
		this.year = mp.date_start;
		//this.showinfolog = mp.HouseholdLogOn; // =false;
		//this.popeconlogs = mp.popeconlogs;// = true;
		this.wantToMigrate = false;
		this.sheep_fodder = mp.sheep_fodder;			//kg / year average fodder requirement
		this.goat_fodder = mp.goat_fodder;	 			//kg / year average fodder requirement
		this.avg_sheep_caloric_value = mp.sheep_return;
		this.avg_goat_caloric_value = mp.goat_return; // !! problem
		this.fallow_field_grazing = mp.fallow_field_grazing;
		this.fodder_from_ff_grazing = (2.27 * 5) + 38.64; //50-100kg  y = 2.27x + 38.64    PER HECTARE, 5 is the landcover value
		this.barley_fodder_percentage = mp.barley_fodder_percentage;
		this.limithhSize=mp.hhSizeLimit;
		this.hhSizeLimit=mp.hhSizeExtreme;

		this.ovicaprid_per_person = mp.ovicaprid_per_person;
		this.ovicaprid_population = this.population * this.ovicaprid_per_person;
		this.sheep_ratio = mp.sheep_ratio;
		this.goat_ratio = mp.goat_ratio;
		int ratio_total = this.sheep_ratio + this.goat_ratio;
		this.average_fodder_requirement = ((this.sheep_fodder * this.sheep_ratio) / ratio_total) + ((this.goat_fodder * this.goat_ratio) / ratio_total);
		this.average_fodder_requirement_from_grazing_per_animal = this.average_fodder_requirement * ((100 - this.barley_fodder_percentage) / 100);
		this.average_fodder_requirement_from_barley_per_animal =  this.average_fodder_requirement * (this.barley_fodder_percentage / 100);
		this.avg_ovicaprid_caloric_value = ((this.avg_sheep_caloric_value * this.sheep_ratio) / ratio_total) + ((this.avg_goat_caloric_value * this.goat_ratio) / ratio_total);
		this.ovicaprid_density_factor = mp.ovicaprid_density;
		this.total_grazing_fodder_rqmt = this.ovicaprid_population * this.average_fodder_requirement;	
		this.grazing_impact = this.grazing_impact * this.ovicaprid_density_factor;
		this.init_grazing_expectation = this.init_grazing_expectation * this.ovicaprid_density_factor;
		
		this.land_per_hectare = 10000.0f / (REGION_AGENT_RES * REGION_AGENT_RES);	// m^2/ha / m^2/land	
		this.init_grazing_expectation = this.init_grazing_expectation / this.land_per_hectare;
		this.wheat_expectation = 0;
		this.avg_fodder_return = 0;
		this.wheat_calories_pc = mp.totalkcal * mp.agriProportion;
		this.meat_milk_calories_pc = mp.meat_milk_calories_pc;	// kcal-mm/capita/year 
		this.total_calories_pc = this.wheat_calories_pc + this.meat_milk_calories_pc;
		//System.out.println("wheat calories per capita" + this.wheat_calories_pc);
		this.wheat_calories_per_kg = mp.wheat_calories_provided;
		//System.out.println("wheat calories per kg" + this.wheat_calories_per_kg);
		this.wheat_kg_pc = this.wheat_calories_pc / this.wheat_calories_per_kg;		// kg-wheat/capita   
		//System.out.println("wheat per capita" + this.wheat_kg_pc);
		this.wheat_rqmt = this.population * this.wheat_kg_pc;
		this.pct_labor = mp.labor_pop_pct;
		this.labor_pc = mp.man_days_per_capita;
		this.init_max_distance_cost = mp.maxfarmcost;
		this.avg_pct_prob_birth = mp.birth_init_prob;
		this.pct_prob_birth = mp.birth_init_prob;
		this.ppl_per_birth_chk = mp.birth_prob_group_size;
		this.pct_prob_delta_birth = mp.birth_prob_delta;
		this.min_pct_prob_birth = mp.birth_prob_min;
		this.max_pct_prob_birth = mp.birth_prob_max;
		this.avg_pct_prob_death = mp.death_init_prob;
		this.pct_prob_death = mp.death_init_prob;
		this.ppl_per_death_chk = mp.death_prob_group_size;
		this.pct_prob_delta_death = mp.death_prob_delta;
		this.min_pct_prob_death = mp.death_prob_min;
		this.max_pct_prob_death = mp.death_prob_max;
		this.wheat_labor = mp.wheat_labor_reqd;	
		this.wheat_init_yield_pha = mp.wheat_init_expected_yield;
		this.barley_init_yield_pha = mp.barley_init_expected_yield;
		this.total_kcal_reqmt = this.population * this.total_calories_pc ;
		this.seed_percentage = mp.seed_percentage;
		this.yield_minus_need = 0.0;										// kg
		this.max_managed_land = CalcMaxManagedLandCells();					// land


//		this.mapsetPath = mp.GISDBASE + mp.location  + File.separator;
//		this.mapsetName = mp.mapset;

		this.exp_yield_scalar = mp.yield_exp_scalar;// ??? why is this commented out?
		this.init_max_distance_cost = mp.maxfarmcost;

		
		//TODO
		//mp = null;
	}
	
	public ArrayList<FarmData> getLandToRelease(){
		
		final ArrayList<FarmData> farms = null; 
		
		//TODO Calculate which farm plots should be returned
		
		return farms;
	}
	
	public void ownFarmLand(ArrayList<FarmData> farmsToAdd){
		for (final FarmData farms : farmsToAdd) {
			this.farmsOwned.add(farms);
		}
		
	}
	
	public ArrayList<FarmData> getOwnedFarmLand(){
			return this.farmsOwned;
	}

	public void receiveAnnualReturns(HouseholdReturnsMessage hhRM){
/*
		this.farmsOwned = hhRM.getFarms(); // this should actually be an add to the array instead of an equals
		this.ovicaprid_fodder_yield = hhRM.getOCReturns();
		this.ovicaprid_fodder_yield_area = hhRM.getOCReturnArea();
*/
		
		//Using the returns Message in a weird hack
		this.ovicaprid_fodder_yield = hhRM.getOCReturns();
		this.avg_fodder_return = hhRM.getOCReturnArea();
		this.farmsOwned = hhRM.getFarms();
		FarmData fd = this.farmsOwned.get(0);
		// new FarmData((int)avg_wheat_return, (int)avg_barley_return, wheat_yield, barley_yield);
		//(final int x, final int y, final double foodProductionRate, final double variable
		this.wheat_expectation = fd.getX();
		this.barley_expectation = fd.getY();
		this.wheat_yield = fd.getFoodProductionRate();
		this.barley_yield = fd.getVariable();
				
	}
	
	
	public void evaluateAnnualReturns(){
		this.year ++;
			
		//Fallow issue
		
		//Total all the field yields
		
		// Total the oc yields
		
		
		//compare yields to need
		
		//System.out.println("PhaseUpdate_UpdatePopulation()");
		//System.out.println("HH "+ this.hhID +" updating, initial percent "+ this.pct_prob_birth+ " "+ this.population);
		double popBuffer = 0.1 * (this.total_kcal_reqmt);		// yield good if within 10% of requirement
		int growth = 0;	
		
		// subtract seed yield from overall yield for wheat and barley
		double wheat_minus_seed = this.wheat_yield - (this.wheat_yield * this.seed_percentage);
		this.barley_yield = this.barley_yield - (this.barley_yield * this.seed_percentage);
		this.wheat_yield_minus_need = wheat_minus_seed  * this.wheat_calories_per_kg ; //convert to kcal for the whole household
		double animals_fed = ((this.barley_yield * 1.25) + this.fodderyieldfromff + this.ovicaprid_fodder_yield) / this.average_fodder_requirement;	
		this.grazing_yield_minus_need = this.avg_ovicaprid_caloric_value * animals_fed; //kcal from ovicaprids
		
		// total kcal gain or loss for the household 
		this.yield_minus_need =  (this.wheat_yield_minus_need + this.grazing_yield_minus_need) - this.total_kcal_reqmt; 

		if ( Math.abs(this.yield_minus_need) <= popBuffer ) {
			this.yield_minus_need = 0.0;
		}
		// yield was less than the buffered need
		//	increase probability of death, decrease the prob of birth;
		
		//TODO Really... half or 1/3 ? its only 0.1
		if ( this.yield_minus_need < 0.0 )
		{	
			// if only 15% less than requested	then increase by 1/2 of delta prob
			if( (this.yield_minus_need * -1) < (this.total_kcal_reqmt / 6.6)){  
				this.pct_prob_death = this.pct_prob_death + (this.pct_prob_delta_death / 2); //
				this.pct_prob_birth = this.pct_prob_birth - (this.pct_prob_delta_birth / 2);
			}
			// if only 25% less than requested then increase by 2/3 of delta prob
			else if( (this.yield_minus_need * -1) < (this.total_kcal_reqmt / 4)){  	
				this.pct_prob_death = this.pct_prob_death + ((this.pct_prob_delta_death * 2) / 3);
				this.pct_prob_birth = this.pct_prob_birth - ((this.pct_prob_delta_birth * 2) / 3);
			}
			else{
				this.pct_prob_death += this.pct_prob_delta_death;
				this.pct_prob_birth -= this.pct_prob_delta_birth;
				}
			
			if ( this.pct_prob_death > this.max_pct_prob_death ){
				this.pct_prob_death = this.max_pct_prob_death;
			}
			if ( this.pct_prob_birth < this.min_pct_prob_birth ){
				this.pct_prob_birth = this.min_pct_prob_birth;
			}
		}

		// yield was greater than buffered need
		//	decrease probability of death; increase probability of birth
		else if ( this.yield_minus_need > 0.0 )
		{
			// if only 15% less than requested	then increase by 1/2 of delta prob
			if( (this.yield_minus_need * -1) < (this.total_kcal_reqmt / 6.6)){  
				this.pct_prob_death = this.pct_prob_death - (this.pct_prob_delta_death / 2); //
				this.pct_prob_birth = this.pct_prob_birth + (this.pct_prob_delta_birth / 2);
			}
			// if only 25% less than requested then increase by 2/3 of delta prob
			else if( (this.yield_minus_need * -1) < (this.total_kcal_reqmt / 4)){  	
				this.pct_prob_death = this.pct_prob_death - ((this.pct_prob_delta_death * 2) / 3);
				this.pct_prob_birth = this.pct_prob_birth + ((this.pct_prob_delta_birth * 2) / 3);
			}
			else{
				this.pct_prob_death += this.pct_prob_delta_death;
				this.pct_prob_birth -= this.pct_prob_delta_birth;
				}
			
			if ( this.pct_prob_death < this.min_pct_prob_death ){
				this.pct_prob_death = this.min_pct_prob_death;
			}
			if ( this.pct_prob_birth > this.max_pct_prob_birth ){
				this.pct_prob_birth = this.max_pct_prob_birth;
			}
			//ilog.LogDataLine("growth", "ymn > 0: probdeath:"+ this.pct_prob_death +" prob_birth:"+ this.pct_prob_birth);
		
		}
		
		// yield was within range of buffered need
		//	settle the death and birth rates toward their average (initial) values
		else
		{
			if ( this.pct_prob_death > this.avg_pct_prob_death )
			{
				this.pct_prob_death -= this.pct_prob_delta_death;
				if ( this.pct_prob_death < this.avg_pct_prob_death ){
					this.pct_prob_death = this.avg_pct_prob_death;
					}
			}
			else if ( this.pct_prob_death < this.avg_pct_prob_death )
			{
				this.pct_prob_death += this.pct_prob_delta_death;
				
				if ( this.pct_prob_death > this.avg_pct_prob_death ){
					this.pct_prob_death = this.avg_pct_prob_death;	
				}
			}
			
			// else, the death percentage is already the average; do nothing
			if ( this.pct_prob_birth > this.avg_pct_prob_birth )
			{
				this.pct_prob_birth -= this.pct_prob_delta_birth;
			
				if ( this.pct_prob_birth < this.avg_pct_prob_birth ){
					this.pct_prob_birth = this.avg_pct_prob_birth;
				}
			}
			
			else if ( this.pct_prob_birth < this.avg_pct_prob_birth )
			{
				this.pct_prob_birth += this.pct_prob_delta_birth;
				
				if ( this.pct_prob_birth > this.avg_pct_prob_birth ){
					this.pct_prob_birth = this.avg_pct_prob_birth;
				}
			}

			// else, the birth percentage is already the average; do nothing
		}
		
		int num_births = 0, num_deaths = 0;
		
		// determine if a birth occurs based upon a percentage.
		// as the size of the population increases, another birth may occur
		int num_possible_births = (int) Math.ceil((double) this.population / (double) ppl_per_birth_chk);
		double pct = 0.0;
		
		for ( int b = 0; b < num_possible_births; b++ )
		{
			pct = RandomHelper.nextDoubleFromTo(0, 1);
			if ( pct <= this.pct_prob_birth ){
				num_births += 1;
			}

		}
		
		// determine if a death occurs based upon a percentage.
		// as the size of the population increases, another person may die
		int num_possible_deaths = (int) Math.ceil((double) this.population / (double) ppl_per_death_chk);

		for ( int d = 0; d < num_possible_deaths; d++ )
		{
			pct = RandomHelper.nextDoubleFromTo(0, 1);
			if ( pct <= this.pct_prob_death ){
				num_deaths += 1;
			}
		}
		
		growth = num_births - num_deaths;
		
		if ( growth != 0 ) 
		{
			if(!ModelParameters.populationLimit)
				this.population += growth;
			
/*			if(this.limithhSize){
				if(this.population > this.hhSizeLimit){
					this.population = this.hhSizeLimit;
				}
				
			}	
*/			
			//this.population = 13; // STATIC POPULATION TEST
			this.wheat_rqmt = (double) this.population * wheat_kg_pc;
			this.wheat_rqmt = this.wheat_rqmt + (this.wheat_rqmt * this.seed_percentage);
			this.total_kcal_reqmt = (double) this.population * this.total_calories_pc;
			this.max_managed_land = CalcMaxManagedLandCells();
			this.ovicaprid_population = this.population *  this.ovicaprid_per_person;
			this.total_grazing_fodder_rqmt = this.ovicaprid_population * this.average_fodder_requirement;
			this.barley_rqmt = this.ovicaprid_population * this.average_fodder_requirement_from_barley_per_animal* (this.barley_fodder_percentage /100) ;//kg
			this.barley_rqmt = this.barley_rqmt + (this.barley_rqmt * this.seed_percentage);
		}

	}
		
	public HouseholdRequest getLandNeed(){
		
		double bly = 0;
		double wht = 0;
		// Estimate next year's yield and plant more crops if necessary and capable	

		if ( (this.wheat_expectation != 0.0) ){ // kg
			this.farmed_land_exp = Math.ceil((this.wheat_rqmt) / (this.wheat_expectation * (this.exp_yield_scalar / 100.0)));
			wht = this.farmed_land_exp;
			bly = Math.ceil((this.barley_rqmt) / (this.barley_expectation * (this.exp_yield_scalar / 100.0)));
			this.farmed_land_exp = this.farmed_land_exp + (Math.ceil((this.barley_rqmt) / (this.barley_expectation * (this.exp_yield_scalar / 100.0))));
		}
		else{ // YEAR 0
			this.wheat_rqmt = this.wheat_rqmt + (this.wheat_rqmt * this.seed_percentage);
			this.farmed_land_exp = Math.ceil(((this.wheat_rqmt) / this.wheat_init_yield_pha) * this.land_per_hectare);
			this.farmed_land_exp = this.farmed_land_exp + Math.ceil(((this.barley_rqmt) / this.barley_init_yield_pha) * this.land_per_hectare);
			wht = Math.ceil(((this.wheat_rqmt) / this.wheat_init_yield_pha) * this.land_per_hectare);
			this.barley_rqmt = this.ovicaprid_population * this.average_fodder_requirement_from_barley_per_animal* (this.barley_fodder_percentage /100) ;//kg
			this.barley_rqmt = this.barley_rqmt + (this.barley_rqmt * this.seed_percentage);
			bly = Math.ceil(((this.barley_rqmt) / this.barley_init_yield_pha) * this.land_per_hectare);
		}

		this.fodderyieldfromff = 0;
		if(this.fallow_field_grazing){
			this.fodderyieldfromff = (this.fodder_from_ff_grazing / this.land_per_hectare) *  (wht + bly); //KG OF FODDER FROM A FARMED HA * NUMBER OF FAMRED CELLS
		}
		
		if ( (this.avg_fodder_return != 0.0 ) ){ 
			this.grazed_land_exp = (this.ovicaprid_population * this.average_fodder_requirement_from_grazing_per_animal) / this.avg_fodder_return;
			
			if(this.fallow_field_grazing){
				// calculating the number of grazing cells that fodder yield is equivalent to
				this.grazed_land_exp = this.grazed_land_exp - (this.fodderyieldfromff / this.avg_fodder_return);
				if (this.grazed_land_exp < 0){
					this.grazed_land_exp = 0;
				}
			}
		}
		else{
			this.grazed_land_exp = (this.ovicaprid_population * this.average_fodder_requirement_from_grazing_per_animal) / this.init_grazing_expectation;
			if (this.grazed_land_exp < 0){
				this.grazed_land_exp = 0;
			}
			if(this.fallow_field_grazing){
				this.grazed_land_exp = this.grazed_land_exp - (this.fodderyieldfromff / this.init_grazing_expectation);
				if (this.grazed_land_exp < 0){
					this.grazed_land_exp = 0;
				}
			}
		}
		
		
		int wheatLandNeed = (int) wht;
		int brlyLandNeed  = (int) bly;
		int grazedLandNeed = (int) this.grazed_land_exp;
		
		//TODO Eventually this will only be NEW land needed
		HouseholdRequest landNeed = new HouseholdRequest(this.villageID, this.hhID, wheatLandNeed, brlyLandNeed, grazedLandNeed, this.population);

		return landNeed;
	}
	
	public int getVillageMemberID() {
		return this.villageID;
	}

	public void setVillageMemberID(int vID) {
		this.villageID = vID;
	}
	
	public String getHHInfo(){
		String info = " ,HH:,"+this.hhID+ " ,% prob birth:," + this.pct_prob_birth + ", % death,"+ this.pct_prob_death+ " ,yield minus need,"+this.yield_minus_need;
		return info;
	}
	
	public int getHhId() {
		return this.hhID;
	}

	public void setHhId(int hID) {
		this.hhID = hID;
	}
		
	public double getPopulation() {
		return this.population;
	}

	public void setPopulation(int population) {
		if (population < 1) {
			throw new IllegalArgumentException(String.format(
					"Parameter household population = %f < 1.", population));
		}

		this.population = population;
	}
	
	public boolean getOperativeStatus() {
		return this.alive;
	}

	public void setOperativeStatus(boolean animate) {
		if (population <= 0 && animate == true) {
			throw new IllegalArgumentException(String.format(
					"Parameter Problem HH Zombie:", population));
		}

		this.alive = animate;
	}
	
	// calculates how many land cells can be managed by the current population
	private int CalcMaxManagedLandCells ()
	{
		//G! this will probably all have to be recalculated
		int max_land_cells = 0;
		
		//  man-days = capita * (laboring capita/total capita (as %)) * man-days/capita 
		float pop_labor = (float) this.population * ((float) this.pct_labor / 100.0f) * (float) this.labor_pc;

		//  man-days/land/yr = man-days/ha/yr / land/ha
		// assume wheat and barley require same amount of work
		float labor_per_land = ((float) this.wheat_labor) / this.land_per_hectare;
		
		//  land/yr = man-days / man-days/land/yr
		max_land_cells = Math.round(pop_labor / labor_per_land);
		
		return max_land_cells;
	}	
	
	public boolean mortality(){
		//TODO Random Probability of losing people
		
		if (this.population <= 0){
			this.alive = false;
		}
		
		return this.alive;
	}
	
	public boolean reproduce(){
		//TODO Random Probability of adding people
		boolean householdFission = false;
		
		if (this.population > this.hhSizeLimit){
			householdFission = true;
			//TODO subtract the population moving to the new household
		}
		
		return false;
		
	}
	
	public boolean migration(){	
		return this.wantToMigrate;
	}
	
	public void migrationAccomplished(){
		this.wantToMigrate = false;
		this.wheat_expectation = 0;
		this.avg_fodder_return = 0;
	}
	
	public void die() {
		ContextUtils.getContext(this).remove(this);
	}
	
	
}
