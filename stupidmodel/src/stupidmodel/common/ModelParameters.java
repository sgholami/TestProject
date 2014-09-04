package stupidmodel.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ModelParameters {
	
	// WEIGHTS
	public double wooddistweight = 3; //   jTextFieldWoodDistanceWeight
	public double gdistweight = 1; //  jTextFieldGrazingDistWight
	public double lcovweight = 1; // jTextFieldLcovDistWight
	public double fdistweight = 1; // jTextFieldFarmDistWght 
	public double sfertilweight = 1;// jTextFieldSoilFtyWight 
	public double sdepthweight = 1; // jTextFieldDepthWght   

	//CLIMATE
	public String climateDataPath = null; //"/Users/sbergin/Documents/APSIModel/climateData.csv"; //  jTextFieldClimFile 
	public boolean climateDataSeries = false; //  jComboBoxClimateSource 
	public double r_factor = 0; //5.66;
	public double annual_precip = 0.5; //0.5; 
	public int storms = 75; 
	public double storm_speed = 1.4; 
	public double storm_length = 6.0; 
	public double storm_precip = 10.0;
	
	//WOOD GATHERING
	public double wood_pc =  1662.98; //1662.98
	public double wood_intensity = 0.08; // 0.08; 
	
	//LANDSCAPE EVOL PARAMETERS
	public double soil_density = 1.25; //1.25;
	public String stream_transport = "0.001"; // 
	public double load_exponent = 1.5; //1.5;
	public String evolSmoothing = "high"; // 
	public int kappa = 1; //1;
	public double cutoff1 =  0; //
	public double cutoff2 = 0; //
	public double cutoff3 = 0; //
	
	//OTHER LANDSCAPE PARAMETERS
	public double soil_depth_min = 0.5; //
	public double soil_depth_max = 7; //
	public int soil_recovery = 1; // 
	public int maxfarmcost = 10800; //  jTextMaxFarmDistance, 
	public int maxgrazecost = 28800; // jTextMaxGrazeDistance  
	public double degrade_rate = 3; // already existed   jTextFieldFertilityImpact  
	public int maxlcov = 50; // need to make sure this is an INT with teh GUI 
	public double seed_percentage = 0.15; //
	
	//GRAZING PARAMETERS
	public double ovicaprid_per_person = 4; //4; 
	public int sheep_ratio = 1; //1;
	public int goat_ratio = 1; //1;
	public double ovicaprid_density = 1; //1; 
	public int sheep_fodder = 584; //= 584;			//kg average fodder requirement (Ullah 2008) 
	public int goat_fodder =894; //= 894;			//kg average fodder requirement (Ullah 2008) 
	public double init_grazing_expectation = 250; //250; //   kg / fodder  for each ha 
	public double barley_fodder_percentage = 10; //10;// Does Go in as a whole number 
    public double pct_lactating_animals = 0.6; //0.6; //   
    public double pct_meat_animals = 0; //0.25;  //     
	public double sheep_return  = 37625; //37625 ; //       
	public double goat_return = 75400; //75400; //     
	public boolean fallow_field_grazing = true;
	
	//VILLAGE VARIABLES
    private int num_villages = 0;
    public ArrayList VData = null;
    
    //Not sure these are really used anymore, might ahve something to do with map resolution
    public float agent_n = 1.0f;
    public float agent_s = 0.0f;
    public float agent_e = 1.0f;
    public float agent_w = 0.0f;

    //HOUSEHOLD PARAMETERS
    public int pct_clearing = 0;
    public double birth_init_prob = 0;
    public int birth_prob_group_size = 0;
    public double birth_prob_delta = 0;
    public double birth_prob_min = 0;
    public double birth_prob_max = 0;
    public double death_init_prob = 0; 
    public int death_prob_group_size = 0;
    public double death_prob_delta = 0;
    public double death_prob_min = 0;
    public double death_prob_max = 0;
    public int labor_pop_pct = 80;
    public int man_days_per_capita = 0;
    public int yield_exp_scalar = 0;
    public double wheat_reqd_pc ;//  jTextWheatRqdPerPerson  
    public double meat_milk_calories_pc = 0 ;//250000.0;	//  jTextMeatMilkRqdPerPerson 
    public int init_hh_pop = 6; //new
    
    //FARMING PARAMETERS
    public double wheat_labor_reqd = 0.0;
    public double wheat_init_expected_yield = 0.0;
    public double barley_labor_reqd = 0.0;  //  jTextBarleyLaborRqd 
    public double barley_init_expected_yield = 0.0;  //  jTextBarleyInitYield  
    public double wheat_calories_provided = 0; //03720; 
    public double maximum_wheat_yield = 0;//3500; //  jTextFieldMaxWheatYield kg/ha 
    public double maximum_barley_yield = 0; //2500; // kg/ha 
	public double wheat_reqd_per_capita = 0; 
	public double totalkcal = 0; //912500;
	public double agriProportion = 0; //= 60 / 100;
	public double pastProportion = 0; //= 40 / 100;

    public String GISDBASE = null;
    public String location = null;
    public String mapset = null;
    public String DEM = null;
    public String bedrock_map = null;
    public String max_landcover_map = null;
    public String land_cover = null;
    public double friction_value = 0.0;
    public String friction_map = null;
    public double soilK_value = 0.0; // erosion resistivity value
    public String soilK_map = null;
    public String fertility_map = null; 
    public int date_start = 0;
    public int date_change_dir = 1; // no longer modifiable
    public int year_increment = 0;
    public int date_end = 0;
    public String date_prefix = null;
    public String date_suffix = null;
        
    // SYSTEM SETTINGS PARAMETERS
    public String APSIMPath = null;
    public String GrassPath = null;
    public String PythonPath = null;
	public boolean WorldWindDisplayOn = false;	
	public boolean HouseholdLogOn = false;	
	public boolean VillageLogOn = false;	
	public boolean IMLogOn = false;	 // save
	public boolean IMLogShow = false;
	public boolean popeconlogs  = true;
	public boolean KeepRasterMaps = false;
	
	public boolean landTenure = false;
	public int hhSizeExtreme =  50 ;
	public int lcBreakPoint1 = 30;
	public int lcBreakPoint2 = 50;
	public int daBpoint1 = 25;
	public int daBpoint2 = 90;
	
	public String landcover_reclass_rules = null;
	public String landcover_color_rules = null;
	public String cfactor_reclass_rules = null;
	public String cfactor_color_rules = null;
	public String fertility_color_rules = null;
	public String impacts_color_rules = null;
	public String grassrc6loc = null;
	public boolean hhSizeLimit = true;
	public String parametersFile = null;
	public static boolean populationLimit = false;
	
	public ModelParameters(String path){
		//System.out.println("~	"+ path);
		loadParametersFromFile(path);
		parametersFile = path;
		//loadParametersFromFile2();

	}
	
	private void loadParametersFromFile(String path){
		String dlm_regex = ",";
		String data = "";
		String[] sdata = null;
		String section = "Initial";
		
		 try{
			 BufferedReader in = new BufferedReader(new FileReader(path));
			 data = in.readLine();
	         sdata = data.split(dlm_regex);
	         //CHECK VERSION
	         if (sdata[1].compareTo("1") != 0){
	        	 throw new IllegalArgumentException("Invalid version data.");
	         }
	         data = in.readLine();   
	         data = in.readLine(); 
	         section = "Landscape Values";
	         data = in.readLine();
	         sdata = data.split(dlm_regex);
	         wooddistweight = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);
	         gdistweight = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);
	         lcovweight = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	         
	         fdistweight = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	         
	         sfertilweight = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	         
	         sdepthweight = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	         
	         soil_depth_min = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	         
	         soil_depth_max = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	         
	         soil_recovery = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	 
	         soil_density = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	 
	         stream_transport = sdata [1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	 
	         load_exponent = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	 
	         evolSmoothing = sdata [1]; 
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         kappa = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         cutoff1 = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         cutoff2 = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         cutoff3 = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         maxlcov = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         lcBreakPoint1 = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         lcBreakPoint2 = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         daBpoint1 = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         daBpoint2 = Integer.parseInt(sdata[1]);
	         
	         data = in.readLine();
	         data = in.readLine();
	         section = "Climate Values";
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         climateDataPath = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 climateDataSeries = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 climateDataSeries = true;
	         }else{
	        	 throw new IllegalArgumentException("Climate Data Series Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         r_factor = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         annual_precip = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         storms = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         storm_speed= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         storm_length = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         storm_precip = Integer.parseInt(sdata[1]);
	         
	         data = in.readLine();
	         data = in.readLine();
	         section = "Household and Subsistence Values";	         
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         wood_pc = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         wood_intensity = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         seed_percentage = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         ovicaprid_per_person = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         sheep_ratio = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         goat_ratio = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         ovicaprid_density = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         sheep_fodder = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         goat_fodder = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         init_grazing_expectation = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         barley_fodder_percentage = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         pct_lactating_animals = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         pct_meat_animals = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         sheep_return = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         goat_return = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 fallow_field_grazing = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 fallow_field_grazing = true;
	         }else{
	        	 throw new IllegalArgumentException("Fallow Field Grazing Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);
	         init_hh_pop = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         pct_clearing = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         birth_init_prob = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         birth_prob_group_size = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         birth_prob_delta = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         birth_prob_min = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         birth_prob_max = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         death_init_prob = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         death_prob_group_size = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         death_prob_delta = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         death_prob_min = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         death_prob_max = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         labor_pop_pct= Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         man_days_per_capita= Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         yield_exp_scalar= Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         totalkcal= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         agriProportion= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         pastProportion= Double.parseDouble(sdata[1]);
	         agriProportion = agriProportion / 100;
	         pastProportion = pastProportion / 100;
	         meat_milk_calories_pc = totalkcal * pastProportion;//
	 	     wheat_reqd_pc = agriProportion * totalkcal; //
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         wheat_labor_reqd= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         wheat_init_expected_yield= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         barley_labor_reqd= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         barley_init_expected_yield= Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         wheat_calories_provided = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         maximum_wheat_yield = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         maximum_barley_yield = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 landTenure = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 landTenure = true;
	         }else{
	        	 throw new IllegalArgumentException("Land Tenure Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         hhSizeExtreme = Integer.parseInt(sdata[1]);
	         
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         maxfarmcost = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         maxgrazecost = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         degrade_rate = Integer.parseInt(sdata[1]);
	         
	         data = in.readLine();
	         data = in.readLine();
	         section = "System & GIS Settings";	      
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         grassrc6loc = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         APSIMPath = sdata[1];
	         String RulesPath = APSIMPath + "Rules/";
	         landcover_reclass_rules = RulesPath + "luse_reclass_rules.txt";
	     	 landcover_color_rules = RulesPath + "luse_colors.txt";
	     	 cfactor_reclass_rules = RulesPath + "cfactor_recode_rules.txt";
	     	 cfactor_color_rules = RulesPath + "cfactor_colors.txt";
	     	 impacts_color_rules = RulesPath + "impacts_colors.txt";
	     	 fertility_color_rules = RulesPath + "sfertil_colors.txt";
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         GrassPath = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         PythonPath = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 WorldWindDisplayOn = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 WorldWindDisplayOn = true;
	         }else{
	        	 throw new IllegalArgumentException("WorldWind Display Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 HouseholdLogOn = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 HouseholdLogOn = true;
	         }else{
	        	 throw new IllegalArgumentException("Household Log Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 VillageLogOn = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 VillageLogOn = true;
	         }else{
	        	 throw new IllegalArgumentException("Village Log Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 IMLogOn = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 IMLogOn = true;
	         }else{
	        	 throw new IllegalArgumentException("Model Log Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("FALSE") == 0){
	        	 KeepRasterMaps = false;
	         }else if(sdata[1].compareTo("TRUE") == 0){
	        	 KeepRasterMaps = true;
	         }else{
	        	 throw new IllegalArgumentException("Keep Raster Maps Must Be Either TRUE or FALSE.");
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         GISDBASE = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         location = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         mapset = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         DEM = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         if (sdata[1].compareTo("NULL") == 0){
	        	 bedrock_map = null;
	         }
	         else{
	        	 bedrock_map = sdata[1];
	         }
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         land_cover = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         friction_map = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         soilK_value = Double.parseDouble(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         fertility_map = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         date_start = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         year_increment = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         date_end = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         date_prefix = sdata[1];
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	
	         date_suffix = sdata[1];
	         
	         data = in.readLine();
	         data = in.readLine();
	         section = "Villages";
	         data = in.readLine();
	         sdata = data.split(dlm_regex);	

	         int numOfVillages = Integer.parseInt(sdata[1]);
	         data = in.readLine();
	         data = in.readLine();
	         VData = new ArrayList();
	         for (int i = 0; i< numOfVillages; i++) {	 
	        	 data = in.readLine();
	        	 sdata = data.split(dlm_regex);	
	        	 AddVillage(Integer.parseInt(sdata[0]), Float.parseFloat(sdata[1]), Float.parseFloat(sdata[2]));

	         }
	         		 
	         //data = in.readLine();         
	         //System.out.println(data);             
	         
	         
	        }
		 
		 catch (Exception exc)
	        {
	        	System.err.println(exc.toString());
	            System.out.println("Error reading data from file in the "+ section+ " section");
	        }
	}
	
	
	private void loadParametersFromFile2(){		
		/***
		 * Manually Set Paths
		 */
		
		/*
			pythonloc = "/usr/bin/";
			gisbase = "/Applications/GRASS/GRASS-6.5.app/Contents/MacOS";
			user_path = "/Users/sbergin/Documents/Med_Sim_Files";
		*/
		
		/***
		 * These are modified by the GUI; manual changes may be overwritten.
		 */
		
		//NEW
		
		// WEIGHTS
		wooddistweight = 3; //   jTextFieldWoodDistanceWeight
		gdistweight = 1; //  jTextFieldGrazingDistWight
		lcovweight = 1; // jTextFieldLcovDistWight
		fdistweight = 1; // jTextFieldFarmDistWght 
		sfertilweight = 1;// jTextFieldSoilFtyWight 
		sdepthweight = 1; // jTextFieldDepthWght   

		//CLIMATE
		climateDataPath = null; //"/Users/sbergin/Documents/APSIModel/climateData.csv"; //  jTextFieldClimFile 
		climateDataSeries = false; //  jComboBoxClimateSource 
		r_factor = 4.54; //5.66;
		annual_precip = 0.5; //0.5; 
		storms = 25; 
		storm_speed = 1.4; 
		storm_length = 24.0; 
		storm_precip = 20.0;
		
		//WOOD GATHERING
		wood_pc =  1662.98; //1662.98
		wood_intensity = 0.08; // 0.08; 
		
		//LANDSCAPE EVOL PARAMETERS
		soil_density = 1.25; //1.25;
		stream_transport = "0.001"; 
		load_exponent = 1.5; //1.5;
		evolSmoothing = "no"; // was none, but not sure how to send that 
		kappa = 1; //1;
		cutoff1 =  0.45; //
		cutoff2 = 1.3; //
		cutoff3 = 7.5; //
		
		//OTHER LANDSCAPE PARAMETERS
		soil_depth_min = 0.5; //
		soil_depth_max = 40; //
		soil_recovery = 1; // 
		maxfarmcost = 10800; //  jTextMaxFarmDistance, new:  
		maxgrazecost = 28800; // jTextMaxGrazeDistance  
		degrade_rate = 2; // already existed   jTextFieldFertilityImpact  
		maxlcov = 50; // need to make sure this is an INT with teh GUI 
		seed_percentage = 0.15; //
		
		//GRAZING PARAMETERS
		ovicaprid_per_person = 13; //; 
		sheep_ratio = 1; //1;
		goat_ratio = 1; //1;
		ovicaprid_density = 5; //1; 
		sheep_fodder = 584; //= 584;			//kg average fodder requirement (Ullah 2008) 
		goat_fodder =894; //= 894;			//kg average fodder requirement (Ullah 2008) 
		init_grazing_expectation = 250; //250; //   kg / fodder  for each ha 
		barley_fodder_percentage = 10; //10;// Does Go in as a whole number 
	    pct_lactating_animals = 0.6; //0.6; //   
	    pct_meat_animals = 0.25; //0.25;  //     
		sheep_return  = 16520; //37625 ; //       
		goat_return = 38560; //75400; //     
		fallow_field_grazing = true;
		
		//VILLAGE VARIABLES
	    num_villages = 0;
	    VData = null;

	    //HOUSEHOLD PARAMETERS
	    init_hh_pop = 6;
	    pct_clearing = 0;
	    birth_init_prob = 0.066;
	    birth_prob_group_size = 1;
	    birth_prob_delta = 0.01;
	    birth_prob_min = 0.01;
	    birth_prob_max = 0.2;
	    death_init_prob = 0.057; 
	    death_prob_group_size = 1;
	    death_prob_delta = 0.01;
	    death_prob_min = 0.01;
	    death_prob_max = 0.02;
	    labor_pop_pct = 85;
	    man_days_per_capita = 300;
	    yield_exp_scalar = 75;
	    
	    double totalkcal = 912500;
	    //double agriProportion = 60 / 100;
	   // double pastproportion = 40 / 100;
	    
	    meat_milk_calories_pc = totalkcal * pastProportion;//
	    wheat_reqd_pc = agriProportion * totalkcal; //
	    
	    //FARMING PARAMETERS
	    wheat_labor_reqd = 50.0f;
	    wheat_init_expected_yield = 450.0f;
	    barley_labor_reqd = 50.0;  //   
	    barley_init_expected_yield = 450.0;  //    
	    wheat_calories_provided = 3720; //; kcal/kg
	    maximum_wheat_yield = 3500;//   kg/ha 
	    maximum_barley_yield = 2500; // kg/ha 

	    GISDBASE = "/Users/sbergin/Desktop/tri-run/grassdata/";
	    location = "Penaguila_SAA2012";
	    mapset = "testmap";
	    DEM = "PaleoDEM";
	    bedrock_map = null;
	    land_cover = "landcover";
	    //friction_value = 1.0f;
	    friction_map = "friction"; //"friction_constant_map";
	    soilK_value = 0.4; // erosion resistivity value
	    soilK_map = null ; //"kfactor_constant_map";
	    fertility_map = "fertility"; 
	    date_start = 0;

	    date_change_dir = 1;
	    year_increment = 1;
	    date_end = 3;
	    date_prefix = "y";
	    date_suffix = "_";
	        
	    // SYSTEM SETTINGS PARAMETERS
	    APSIMPath = "/Users/sbergin/Desktop/SAA2012Original/APSIM/";
	    GrassPath = "/Applications/Grass/GRASS-6.4.app/Contents/MacOS/";
	    PythonPath = "/usr/bin/";
		WorldWindDisplayOn = false;	
		HouseholdLogOn = false;	
		VillageLogOn = false;	
		IMLogOn = false;	 // save
		IMLogShow = false;
		popeconlogs  = true;
		KeepRasterMaps = false;
		
		landTenure = false;
		hhSizeExtreme =  50 ;
		lcBreakPoint1 = 30;
		lcBreakPoint2 = 50;
		daBpoint1 = 25;
		daBpoint2 = 90;


		String RulesPath = APSIMPath + "Rules/";
        landcover_reclass_rules = RulesPath + "luse_reclass_rules.txt";
    	landcover_color_rules = RulesPath + "luse_colors.txt";
    	cfactor_reclass_rules = RulesPath + "cfactor_recode_rules.txt";
    	cfactor_color_rules = RulesPath + "cfactor_colors.txt";
    	impacts_color_rules = RulesPath + "impacts_colors.txt";
    	fertility_color_rules = RulesPath + "sfertil_colors.txt";
		grassrc6loc = "/Users/sbergin/Desktop/tri-run/grassdata/"; 
		
		float ns = 4284845.0f;
    	float ew = 725951.0f;
		
		 VData = new ArrayList();
		 AddVillage(3, ew, ns);
		 ns = 4282716.5f;
	     ew = 725262.06f;
	     AddVillage(3, ew, ns);
	     
	     //TODO COPY THE PARAM FILE TO THE NEW GRASS DIRECTORY
		 
	}
	
	
	public void AddVillage ( int hhCount, float vX, float vY)
    {
        VillageData vdata = new VillageData(hhCount, vX, vY);   
        this.VData.add(vdata);

    }

}
