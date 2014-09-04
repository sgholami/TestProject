/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/StupidModelContextBuilder.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel;

import fileMgmt.CopyFile;
import fileMgmt.CreateFile;
import grass.GrassFacade;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import stupidmodel.agents.Household;
import stupidmodel.agents.Village;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.IllegalParameterException;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.GridValueLayer;
//import stupidmodel.agents.Bug;
//import stupidmodel.agents.Bug.BugSizeComparator;
import stupidmodel.agents.HabitatCell;
import stupidmodel.agents.Predator;
import stupidmodel.common.CalendarMgmt;
import stupidmodel.common.CellData;
import stupidmodel.common.Constants;
import stupidmodel.common.FarmData;
import stupidmodel.common.HouseholdRequest;
import stupidmodel.common.HouseholdReturnsMessage;
import stupidmodel.common.InfoLog;
import stupidmodel.common.SMUtils;
import stupidmodel.common.TypeMaps;
import stupidmodel.common.VillageData;
import stupidmodel.common.VillageReturnsMsg;
import stupidmodel.common.ModelParameters;
import cern.jet.random.Normal;

/**
 * Custom {@link ContextBuilder} implementation for <i>StupidModel</i> versions.
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @version $Id: StupidModelContextBuilder.java 150 2011-05-26 19:06:40Z
 *          richard.legendi@gmail.com $
 */
public class StupidModelContextBuilder extends DefaultContext<Object> implements
		ContextBuilder<Object> {
	
	
	public ModelParameters mp = null;	
	private CalendarMgmt cm = null;
	public String filename = "population_stats.txt";
	public CreateFile popdata = null;
	public CreateFile popdetail = null;
	public InfoLog ilog = null;

	private GrassFacade grass = null;
	String mapset;
	String mapsetpath;
	private double REGION_AGENT_RES = 10.0;
	public int HH_ID_COUNTER = 0;
	private Date startDate;
	private Date yearStart;
	private Date yearEnd;
	
	private Date landRequestStartTime;
	private Date landRequestEndTime;
	
	BufferedWriter performanceFile;
	

	@Override
	public Context<Object> build(final Context<Object> context) {
		assert (context != null);

		// Set a specified context ID
		context.setId(Constants.CONTEXT_ID);
		

		int gridSizeX = 50;
		int gridSizeY = 50;
		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null) // No hints
				.createContinuousSpace(Constants.SPACE_ID, context,
						new RandomCartesianAdder<Object>(),
						// From Model 15, stop using toroidal space
						new StrictBorders(), gridSizeX, gridSizeY);

		// Create a space on which agents and cells located at
		final Grid<Object> grid = GridFactoryFinder.createGridFactory(null)
				.createGrid(Constants.GRID_ID, context,
						new GridBuilderParameters<Object>(
						// From Model 15, stop using toroidal space
								new repast.simphony.space.grid.StrictBorders(),
								// This is a simple implementation of an adder
								// that doesn't perform any action
								new SimpleGridAdder<Object>(),
								// Each cell in the grid is multi-occupancy
								true,
								// Size of the grid (as read from file)
								gridSizeX, gridSizeY));

		// Create a background layer for the displayed grid that represents the
		// available (grown) food amount
/*		final GridValueLayer foodValueLayer = new GridValueLayer(
				Constants.FOOD_VALUE_LAYER_ID, // Access layer through context
				true, // Densely populated
				// repast.simphony.space.grid.StrictBorders() here results in an
				// exception when rendering
				new WrapAroundBorders(), // Not toroidal
				// Size of the grid (defined constants)
				gridSizeX, gridSizeY);*/

		//context.addValueLayer(foodValueLayer);


		createVillages(context, space, grid);
		//createHabitatCells(context, cellData, grid, foodValueLayer);

		RunEnvironment.getInstance().endAt(Constants.DEFAULT_END_AT);

		return context;
	}
	
	private void InitializeVillagesOnLandscape(){

		boolean run_quiet = true;
		String lastoutput;
		int vCount = this.mp.VData.size();	
		char[] gregionflags = {'a'};
		
		for (int i = 0; i < vCount; ++i) {
			VillageData villageData = (VillageData) this.mp.VData.get(i);
			String vlg_id = "village_" + i + "_";	
			String site_map = CurrentMap(vlg_id, TypeMaps.SITE);
			String data_fname = site_map + ".dat";
			String villageNumber = Integer.toString(i);
			String vector_site_location2 = cm.Year()+vlg_id+"site2@"+mapset;
			// create data file
			CreateFile sitedata = new CreateFile(mapsetpath, data_fname, true);
			String ew = Float.toString(villageData.getEW());
			String ns = Float.toString(villageData.getNS());
			String cost_map = CurrentMap(vlg_id, TypeMaps.COSTSFC);;
			try{
				
				lastoutput = this.grass.gRegion(CurrentMapLoc(TypeMaps.ELEVATION), null, gregionflags, REGION_AGENT_RES, REGION_AGENT_RES, run_quiet);
				
				String loc = ew + "|" + ns;
				sitedata.addLine(villageNumber + "|" + loc);
				sitedata.addNewLine();
				sitedata.writeFile();
		
				// create vector map from data file
				String[] parameters = {"format=point", "cat=1", "x=2", "y=3"};

				lastoutput = this.grass.vInASCII(sitedata.getPathName(), site_map, parameters, run_quiet);
				System.out.println(lastoutput);
		
				//convert vector point to raster and then back again, used to make sure that the village catchment matches the other grids
				String raster_village = "village_temp_locR"+ villageNumber;
				String use = "cat";
				String type = "point";
				lastoutput = this.grass.vToRast(cm.Year()+vlg_id+"site@"+mapset, raster_village, use, type, null, null, run_quiet);
				System.out.println(lastoutput);
					
				boolean overwrite = true;
				char[] flags = {'v'};
				String feature = "point";
				lastoutput = this.grass.rToVect(raster_village, flags, feature, vector_site_location2, run_quiet, overwrite);
				System.out.println(lastoutput);
				
				// create cost surface map
				char[] rwalkflags = {'k'};
				double max_cost = 0;
				int percent_memory = 0;
				int num_segments = 4;
				String walk_coefficient = "0.72,6.0,1.9998,-1.9998";
				double lambda = 0;
				double slope_factor = -0.2125; //site_map_loc
				lastoutput = this.grass.rWalk(rwalkflags, CurrentMapLoc(TypeMaps.ELEVATION), CurrentMapLoc(TypeMaps.FRICTION), cost_map, vector_site_location2 , max_cost, percent_memory, num_segments, walk_coefficient, lambda, slope_factor, run_quiet);
				System.out.println(lastoutput);			
				
				// create/update village location based upon site and population			
				char[] rcatchflags = null;
				double a = 0.72;
				double b = 6;
				double c = 1.9998;
				double d = -1.9998;
				double density = 0.0159;	 // people per sq.m
				int population = mp.init_hh_pop * villageData.getHHCount();
				int area = (int) Math.round(population/density);
				int village_clearing_landcover_value = 40; // Landcover value that will represent a village
				final String village_catchment_map = cm.Year() + vlg_id + "catchment";
				final String village_catchment_map_loc = village_catchment_map + "@" + mapset;	
			    lastoutput = this.grass.rCatchmentPy(rcatchflags, CurrentMapLoc(vlg_id, TypeMaps.COSTSFC), CurrentMapLoc(TypeMaps.ELEVATION), vector_site_location2, a, b, c, d, lambda, slope_factor, village_catchment_map, area, village_clearing_landcover_value);	
				System.out.println(lastoutput);
								
				lastoutput = this.grass.rMapcalc(CurrentMap(vlg_id, TypeMaps.CLEARING) + "='if(isnull(\"" + village_catchment_map_loc + "\"), null(), " + village_clearing_landcover_value + ")'");
				System.out.println(lastoutput);

				final String temp_landcover_map = cm.Year() + vlg_id + "temp_" + TypeMaps.LANDCOVER.toString();
				lastoutput = this.grass.rVillagesPy(CurrentMapLoc(TypeMaps.LANDCOVER), CurrentMapLoc(vlg_id, TypeMaps.CLEARING), village_clearing_landcover_value, temp_landcover_map);
				System.out.println(lastoutput);
				
				//get coordinates of new village location and return to village
				lastoutput = this.grass.gRegion(CurrentMapLoc(TypeMaps.ELEVATION), null, gregionflags, REGION_AGENT_RES, REGION_AGENT_RES, run_quiet);
				System.out.println(lastoutput);			

			}
			catch (Exception e)
			{
				System.out.println("An error occurred while initializing the village on the landscape"+	e.getMessage());
			}
		}
		
	}
	
	
	private void InitializeGrass(){

		// Create a new instance of GRAVA. Grava contains most of the GRASS info, and creates a new Mapset 
		// if the one supplied already exists.
		try {
			/*
			 * Object owner_key, String gisbase, String gisrc, String gisdbase, String location, String mapset						
			 */

			this.grass = GrassFacade.getInstance(this, this.mp.GrassPath, this.mp.grassrc6loc, this.mp.GISDBASE, this.mp.location, this.mp.mapset); 
			this.grass.GROUP = System.getProperty("user.name");		// this.grass.GROUP = "medland";
			this.mapset = this.grass.getMapset();
		}
		catch (Exception exc) {
			System.err.println("ERROR DURING GRAVA INITIALIZATION "+exc.getMessage());
		}

		this.grass.setExecutablePath(new File(this.mp.GrassPath + "bin" + File.separator));
		this.grass.setScriptsPath(new File(this.mp.APSIMPath + "Scripts" + File.separator));
		this.grass.setPythonPath(new File (this.mp.PythonPath));	

		this.mapsetpath = this.mp.GISDBASE + this.mp.location + File.separator + this.mapset + File.separator;
		//source, destination
		try {
			CopyFile.copy(new File(this.mp.parametersFile), new File(this.mapsetpath+"parameters.csv"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void InitializeLandscapeModel () throws IOException, IllegalArgumentException 
	{

		System.out.println(" ~	Running InitializeLandscapeModel()    -Creating Initial Landscape Maps");
				
		try{
			boolean run_quiet = true;
			String lastresult = null;
			String PermanentMapset = "@PERMANENT";
			char[] flags = null;			
		
			lastresult = this.grass.gRegion(TypeMaps.ELEVATION.toString() + "@PERMANENT", null, run_quiet);
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.gCopyRast(this.mp.DEM + PermanentMapset, CurrentMap(TypeMaps.ELEVATION), run_quiet);
			ilog.LogInfoLine(lastresult);

			if ( TypeMaps.SOILS_KFACTOR != null ){
				lastresult = this.grass.gCopyRast(this.mp.soilK_map + PermanentMapset, CurrentMap(TypeMaps.SOILS_KFACTOR), run_quiet);
			}

			ilog.LogInfoLine(lastresult);

			lastresult = this.grass.gCopyRast(this.mp.friction_map + PermanentMapset, CurrentMap(TypeMaps.FRICTION), run_quiet);

			ilog.LogInfoLine(lastresult);

			lastresult = this.grass.gCopyRast(this.mp.fertility_map + PermanentMapset, CurrentMap(TypeMaps.FERTILITY), run_quiet);
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.gCopyRast(this.mp.land_cover + PermanentMapset, "temp_" + CurrentMap(TypeMaps.LANDCOVER), run_quiet);
			ilog.LogInfoLine(lastresult);
			
			if(this.mp.bedrock_map != null){
				ilog.LogInfoLine("Creating soil map from existing bedrock map");
				lastresult = this.grass.gCopyRast(this.mp.bedrock_map + PermanentMapset, CurrentMap(TypeMaps.BEDROCK), run_quiet);
				ilog.LogInfoLine(lastresult);	
				lastresult = this.grass.rMapcalc(CurrentMap(TypeMaps.SOILDEPTH) + "="+CurrentMap(TypeMaps.ELEVATION)+ "-"+ CurrentMap(TypeMaps.BEDROCK));
			}
			else{
				ilog.LogInfoLine("Creating soil map with normal procedure");
				lastresult = this.grass.rSoilDepthPy(CurrentMap(TypeMaps.ELEVATION), CurrentMap(TypeMaps.BEDROCK), CurrentMap(TypeMaps.SOILDEPTH), this.mp.soil_depth_min, this.mp.soil_depth_max);
				ilog.LogInfoLine(lastresult);
			}
			
			lastresult = this.grass.rSlopeAspect(CurrentMap(TypeMaps.ELEVATION), CurrentMap(TypeMaps.SLOPE), run_quiet);
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.rReclass(this.mp.landcover_reclass_rules, "temp_" +CurrentMap(TypeMaps.LANDCOVER), "temp_" +CurrentMap(TypeMaps.LANDCOVER)+ "_reclass", null, true);
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.rMapcalc(CurrentMap(TypeMaps.LANDCOVER) + "=temp_" + CurrentMap(TypeMaps.LANDCOVER) + "_reclass");
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.gRemoveRast("temp_" + CurrentMap(TypeMaps.LANDCOVER) + "_reclass,temp_" + CurrentMap(TypeMaps.LANDCOVER), run_quiet);
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.rColors(CurrentMap(TypeMaps.LANDCOVER), flags, GrassFacade.ColorSource.RULES_FILE, this.mp.landcover_color_rules, run_quiet);
			ilog.LogInfoLine(lastresult);
			lastresult = this.grass.rColors(CurrentMap(TypeMaps.FERTILITY), flags, GrassFacade.ColorSource.RULES_FILE, this.mp.fertility_color_rules, run_quiet);
			ilog.LogInfoLine(lastresult);			

		} catch (IllegalArgumentException e) {
			System.out.println("ERROR: IOException encountered while Initializing InitializeLandModel.");
		} catch (IOException e) {
			System.out.println("ERROR: IOException encountered while Initializing InitializeLandModel.");
		}

		ilog.LogInfoLine("~	Returning from InitializeLandscapeModel()");
		System.out.println();
		
	}
	
	private String RunLandAssesser (String LandChoices ) throws IOException 
	{
		System.out.println();

		final String cfactor = cm.NextYear() + TypeMaps.LCVR_CFACTOR.toString();
		boolean run_quiet = false;
		String last_output;
		//ArrayList LandChoicesList = LandChoices;
		String LandRequestString = LandChoices;
		String dlm = ";";
		/*
		 * LandRequestMsg = Integer.toString(this.myID )+ dlm + Integer.toString(this.villageID) + dlm + 
		Integer.toString(this.population) + dlm + Double.toString(this.wheat_proportion) + dlm + Double.toString(this.barley_proportion) + 
		dlm + Integer.toString(farmedLandNeed) + dlm+ Integer.toString(grazedLandNeed);
		 */
		/*for (int p=0; p < LandChoicesList.size(); p++)
		{
				if (p == LandChoicesList.size()-1){
					LandRequestString = LandRequestString + LandChoicesList.get(p);
				}else{
					LandRequestString = LandRequestString + LandChoicesList.get(p) + dlm;
				}
		}
		*/
//TODO initializae and use the climate stuff
		landRequestStartTime = new Date();
		try{
			/*if (mp.climateDataSeries){
				this.mp.storms = (int) climateData [0] [cm.IntYear()] ; // storms/yr
				this.mp.r_factor = climateData [1] [cm.IntYear()] ;// r factor
				this.mp.annual_precip = climateData [2] [cm.IntYear()];// annual precip
				this.mp.storm_speed = climateData [3] [cm.IntYear()];// storm speed
				this.mp.storm_length = climateData [4] [cm.IntYear()];// storm length
				this.mp.storm_precip = climateData [5] [cm.IntYear()];//storm precip
			}
			 */
			char[] gregionflags = {'a'};
			last_output = this.grass.gRegion(CurrentMapLoc(TypeMaps.ELEVATION), null, gregionflags, REGION_AGENT_RES, REGION_AGENT_RES, run_quiet);
			ilog.LogInfoLine(last_output);	
			
			if (cm.IntYear() > 0 ){
				int vId = 0;
				while (vId < this.mp.VData.size()){ 
					last_output = this.grass.gCopyRast(cm.PrevYear()+"village_"+vId+"_"+ TypeMaps.CLEARING, CurrentMap("village_"+vId+"_", TypeMaps.CLEARING), run_quiet);
					vId++;
				}
			}
						
			//Combine Multiple Cost Surface Maps and Clearing Maps for each village into one variable with multiple maps
			String CostSurfaceMaps = CurrentMap("village_0_", TypeMaps.COSTSFC);
			String villageLand = CurrentMap("village_0_", TypeMaps.CLEARING);
			String VillageMaps = CurrentMap("village_0_", TypeMaps.CLEARING);
			int vId = 0;
			//System.out.println("Disregard Map Already Exists Error:");
			if(this.mp.VData.size() > 1){
				vId = 1;
				while (vId < this.mp.VData.size()){
				CostSurfaceMaps = CostSurfaceMaps + ","+CurrentMap("village_" + vId + "_", TypeMaps.COSTSFC);
				VillageMaps = VillageMaps + ","+CurrentMap("village_" + vId + "_", TypeMaps.CLEARING);
				villageLand = cm.Year()+"villageland";
				vId++;	
				}
			}
			last_output = this.grass.rPatch(VillageMaps, villageLand, run_quiet);

			final String next_landcover = cm.NextYear() + TypeMaps.LANDCOVER.toString();
			final String next_fertility = cm.NextYear() + TypeMaps.FERTILITY.toString();
			final String impacts_map = CurrentMap(TypeMaps.IMPACTS);

		String statsfile = mapsetpath + "stats";
		String fertilstatsfile = mapsetpath + "fertility";
		String maxlandcov = null;
		if (this.mp.max_landcover_map != null){
			maxlandcov = this.mp.max_landcover_map;
		}
		else{
			maxlandcov = Double.toString(this.mp.maxlcov);
		}
		ilog.LogInfoLine("Running rLandAssessPy(): This takes a while...");
		
		double dropval = 40; /// TEMP FIX SINCE THIS VARIABLE IS NOT YET IN USE
		String farmbreaks = Double.toString(this.mp.lcBreakPoint1)+","+Double.toString(this.mp.daBpoint1)+";"+Double.toString(this.mp.lcBreakPoint2)+","+Double.toString(this.mp.daBpoint2);
		char[] flags= {'t'};
		if (!this.mp.landTenure){
			flags = null;
		}		
	
			String assess_output = this.grass.rLandAssessPy(LandRequestString, this.mp.annual_precip, this.mp.maximum_barley_yield, this.mp.maximum_wheat_yield, this.mp.wood_pc, this.mp.ovicaprid_density, 
					this.mp.degrade_rate, this.mp.soil_recovery, this.mp.wood_intensity, this.mp.wooddistweight, this.mp.gdistweight, this.mp.lcovweight, this.mp.fdistweight, this.mp.sfertilweight, 
					this.mp.sdepthweight, this.mp.maxfarmcost, this.mp.maxgrazecost, maxlandcov, CurrentMap(TypeMaps.SLOPE), CurrentMap(TypeMaps.LANDCOVER), CurrentMap(TypeMaps.FERTILITY),
					CurrentMap(TypeMaps.SOILDEPTH), villageLand, CostSurfaceMaps, cm.Year(), next_landcover, next_fertility, impacts_map, this.mp.fertility_color_rules,
					this.mp.landcover_reclass_rules, this.mp.landcover_color_rules, statsfile,fertilstatsfile, farmbreaks, dropval, cm.PrevYear(), flags);
		
			//System.out.println("ASSESS OUTPUT: "+assess_output);	
			
			//r.mapcalc "integer_map = round(fp_maps)"
		
//output ;HH#:avg_agri_return,avgoc_return,wheat_yield,barley_yield,oc_yield;HH#:avg_agri_return,avgoc_return,wheat_yield,barley_yield,oc_yield;HH#:avg_agri_return,avgoc_return,wheat_yield,barley_yield,oc_yield
			last_output = this.grass.rCFactorPy(next_landcover, cfactor, this.mp.cfactor_reclass_rules, this.mp.cfactor_color_rules);
			System.out.println(last_output);

			last_output = assess_output;
		}	
		
		catch (IOException ioe)
		{
			System.out.println("ERROR: IOException encountered during execution of RunLandAsseser().");
			System.out.println(ioe.getMessage());
			throw ioe;
		}  

		landRequestEndTime = new Date();
		
		ilog.LogInfoLine("Returning From: RunLandAssesser()");
		
		return  last_output;
		
	}
	
	private void RunLandscapeEvol () throws IOException 
	{

		ilog.LogInfoLine("RunLandscapeEvol(): This takes a while too...");

		
		final String elevation_map = CurrentMapLoc(TypeMaps.ELEVATION);
		final String bedrock_map = CurrentMapLoc(TypeMaps.BEDROCK);
		String last_output = null;
		boolean run_quiet = false;
		String soil_K_factor = null;
		if ( TypeMaps.SOILS_KFACTOR != null ){
			soil_K_factor = CurrentMapLoc(TypeMaps.SOILS_KFACTOR);
		}	
		else{	soil_K_factor = String.valueOf(mp.soilK_value); }
		
		final String C_factor =  cm.NextYear() + TypeMaps.LCVR_CFACTOR.toString() + "@" + mapset;
		char[] flags = {'s'}; // was p and g
		String stats_file = this.mp.GISDBASE + this.mp.location + "/" + this.mapset + "/" + this.mapset + "_"+"Stats";

		try
		{
			char[] gregionflags = {'g', 'p'}; 
			last_output = this.grass.gRegion(CurrentMapLoc(TypeMaps.ELEVATION), null, gregionflags, -1, -1, run_quiet);

			last_output = this.grass.rLandscapeEvolPy(flags, elevation_map, bedrock_map, cm.NextYear(), TypeMaps.ELEVATION.toString(), TypeMaps.SOILDEPTH.toString(), 
					mp.soil_density, mp.storm_precip, mp.storms, mp.storm_speed, mp.storm_length, mp.stream_transport, mp.load_exponent, stats_file,
					mp.r_factor, soil_K_factor, C_factor, Double.toString(mp.kappa), mp.evolSmoothing, mp.cutoff1,  mp.cutoff2, mp.cutoff3, run_quiet);	
			ilog.LogInfoLine(last_output);	
			
			if (mp.KeepRasterMaps){ }else{
			char[] deleteflags = {'f','b'};
			last_output = this.grass.gMRemoveRast("*V*", deleteflags, run_quiet);
			
			last_output = this.grass.gMRemoveRast("*temp_landcover*", deleteflags, run_quiet);
			
			last_output = this.grass.gMRemoveRast("*Returns_map*", deleteflags, run_quiet);
			
			last_output = this.grass.gMRemoveRast("*woodgathering*", deleteflags, run_quiet);

			}
			
		}
		catch (IOException ioe)
		{
			System.out.println("ERROR: IOException encountered during execution of soil dynamic script.");
			System.out.println(ioe.getMessage());
			throw ioe;
		}  
		
		ilog.LogInfoLine("Returning from RunLandscapeEvol");

	}
	

	private void createVillages(final Context<Object> context, final ContinuousSpace<Object> space, final Grid<Object> grid) {
		assert (context != null);
		assert (space != null);
		assert (grid != null);

		final Parameters parameters = RunEnvironment.getInstance().getParameters();			
		final String settingsFile = ((String) parameters.getValue(Constants.PARAMETER_ID_SETTINGS_FILE)).toString();
		mp = new ModelParameters(settingsFile);
		
		ArrayList <VillageData> vList = this.mp.VData;
		VillageData vd;			
		final int vCount = vList.size();

		if (vCount < 0) {
			throw new IllegalParameterException("Parameter villageCount = "	+ vCount + " must be non-negative");
		}

		for (int i = 0; i < vCount; ++i) {
			final Village village = new Village();
			vd = vList.get(i);
			village.setVillageID(i);
			village.setParameterSet(this.mp);
			context.add(village);
			int hhCount = vd.getHHCount();
			final NdPoint pt = space.getLocation(village);
			grid.moveTo(village, (int) pt.getX(), (int) pt.getY());
			if (hhCount < 0) {
				throw new IllegalArgumentException(String.format("hhCount = %f < 0.", hhCount));
			}
					for (int j = 0; j < hhCount; ++j) {
						final Household household = new Household(this.mp);
						household.setOperativeStatus(true);
						household.setVillageMemberID(i);
						context.add(household);
						//int hID = (i*hhCount)+j;
						household.setHhId(this.HH_ID_COUNTER);
						this.HH_ID_COUNTER++;
						//village.addHousehold(household);
						grid.moveTo(household, (int) pt.getX(), (int) pt.getY());
					}

		}
	}
	
	
	@ScheduledMethod(start = 1, interval = 0, priority = 1)
	public void initializeModel() throws IllegalArgumentException, IOException {
		
		final Parameters parameters = RunEnvironment.getInstance()
				.getParameters();

		final String settingsFile = ((String) parameters
				.getValue(Constants.PARAMETER_ID_SETTINGS_FILE)).toString();
		
		mp = new ModelParameters(settingsFile);
	
		cm = CalendarMgmt.GetInstance(
			mp.date_start,				// start year
			mp.year_increment,			// year increment 
			5,							// num digits in year
			CalendarMgmt.COUNT_UP,		// increment direction
			mp.date_prefix,				// date prefix
			mp.date_suffix,				// date suffix
			mp.date_end);				// end year	;
	
		TypeMaps.ELEVATION = new TypeMaps(this.mp.DEM, true);
		TypeMaps.LANDCOVER = new TypeMaps(this.mp.land_cover, true);	
		TypeMaps.FRICTION = new TypeMaps(mp.friction_map, false);

		if ( mp.soilK_map == null )
		{
			TypeMaps.SOILS_KFACTOR = null;
		}
		else{
			TypeMaps.SOILS_KFACTOR = new TypeMaps(mp.soilK_map, false);
		}

		InitializeGrass();
		
		//ilog.LogDataLine("population", population_line);
		
		/*****************LOG SETTINGS**************/
        ilog = new InfoLog();
		String[] data_type = {"population","ts"};	//"ts",	 "general",  
		File dir = new File(mapsetpath + "APSimLogs");
		dir.mkdir();
		String log_path = mapsetpath + "APSimLogs" + File.separator;
		ilog.SetFileInfo(log_path, "IM_logger.txt", data_type);			
		ilog.StartAll(InfoLog.DATA, true);
		//ilog.StartAll(InfoLog.DATA + InfoLog.INFO, true);
		ilog.SetDisplay(InfoLog.INFO);
		//ilog.SetDisplay(InfoLog.NONE);
		ilog.LogAll("Mapset: " + mapset);
		ilog.LogAll("Year:" + cm.Year());
		//ilog.LogInfoLine("IM: deltint(): initialize");
	
		InitializeLandscapeModel();
		InitializeVillagesOnLandscape();
		this.HH_ID_COUNTER = getHhCount();
		this.startDate = new Date();
		this.performanceFile = new BufferedWriter(new FileWriter(new File("performance.csv")));
		this.performanceFile.write("year,duration,land request duration,landscape evolution duration,population\n");
		yearStart = new Date();
	}


	@ScheduledMethod(start = 1, interval = 1, priority = 0)
	public void activateAgents() throws IOException {
		
		
		ArrayList<Village> vList = getVillageList();
		SimUtilities.shuffle(vList, RandomHelper.getUniform());

		ilog.LogInfoLine(" - Releasing Unused Land - ");
		
		ArrayList<FarmData> allFarmsToRelease = new ArrayList<FarmData>();	
		for (final Village village : vList) {
			ArrayList<FarmData> farmsToRelease = new ArrayList<FarmData>();
			farmsToRelease = village.releaseLand();
			for ( FarmData farms : farmsToRelease){
				allFarmsToRelease.add(farms);
			}
		}
		
		//TODO GRASS RELEASES THE LAND FROM THE NEWLY MADE LIST
		
		ilog.LogInfoLine(" - HH calculating Land Need - ");
		
		ArrayList<HouseholdRequest> allHHRequests = new ArrayList <HouseholdRequest>();
		SimUtilities.shuffle(vList, RandomHelper.getUniform());
		for (final Village village : vList) {
			ArrayList<HouseholdRequest> someHHRequests = new ArrayList <HouseholdRequest>();
			someHHRequests = village.getHHLandRequests();
			for (HouseholdRequest hhRequest : someHHRequests){
				allHHRequests.add(hhRequest);
			}
		}

		
		ArrayList<VillageReturnsMsg> vReturns = new ArrayList <VillageReturnsMsg>();
		
		vReturns = landRequestGangPlank(allHHRequests);
		long landRequestDuration = landRequestEndTime.getTime() - landRequestStartTime.getTime();
		
		//vReturns = psuedoReturnsMessages();
		//System.out.println("Finished Psuedo Farming");
		
		ArrayList<Village> vList2 = getVillageList();
		
		for (VillageReturnsMsg vRM : vReturns){
			int msgVID = vRM.getVID();
			int remove = 0 ;
			for (int i = 0; i< vList2.size(); i++) {
				Village aVillage = (Village) vList2.get(i);		
				int vID = aVillage.getVillageID();
				if (vID == msgVID){
					aVillage.receiveHHLandReturns(vRM);
					remove = i;
					i = vList2.size();		
				}
			}
			vList2.remove(remove);
		}
		ilog.LogInfoLine(" - Finished Sending Return Messages - ");
		SimUtilities.shuffle(vList, RandomHelper.getUniform());
		
		for (final Village village : vList) {
			village.disseminateReturns();
		}

		ilog.LogInfoLine(" - Finished Returns Evaluation, Begining Reproduction -");
		SimUtilities.shuffle(vList, RandomHelper.getUniform());

		ilog.LogInfoLine(" - Finished HH Reproduction, Begining Mortality -");
		SimUtilities.shuffle(vList, RandomHelper.getUniform());
		for (final Village village : vList) {
			village.mortality();
		}
		System.out.println();
		System.out.println();
		vList = getVillageList();

		String villagePopLine = cm.Year();
		for (final Village village : vList) {
			System.out.println("Village "+village.getVillageID()+ " has a total population of "+village.getPopulation());
			villagePopLine = villagePopLine + ",Village:"+village.getVillageID()+ ",Pop:,"+village.getPopulation();	
		}
		ilog.LogDataLine("population", villagePopLine);
		System.out.println();
		System.out.println();
		
		Date landscapeEvolStartTime = new Date();
		try {
			RunLandscapeEvol();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Date landscapeEvolEndTime = new Date();
		long landscapeEvolDuration = landscapeEvolEndTime.getTime() - landscapeEvolStartTime.getTime();
		
		if (0 == getVillageList().size() ) {
			ilog.LogInfoLine("All villages abandoned, terminating simulation.");
			RunEnvironment.getInstance().endRun();
		}
		
		yearEnd = new Date();
		putTimeInfoInPerformanceFile(landRequestDuration, landscapeEvolDuration);
		
		if (cm.IntYear() == cm.EndYear()){
						
			ilog.LogInfoLine("End Year Reached, terminating simulation.");
			RunEnvironment.getInstance().endRun();
			Date endDate = new Date();
			double timeInSeconds = (((double)(endDate.getTime()-startDate.getTime()))/((double)1000));
			int hours = (int)timeInSeconds/3600;
			int minutes = ((int)timeInSeconds%3600)/60;
			int seconds = ((int)timeInSeconds)%60;
			System.out.println("Total time of the simulation: " + timeInSeconds + " seconds");
			System.out.println("Or: " + hours + ":" + minutes + ":" + seconds);
			this.performanceFile.close();
			
		}
		
/*		if(this.getTotalPopulation() > 210){
			ModelParameters.populationLimit = true;
		}
*/		
		cm.Increment();				// increment calendar year
		System.out.println();
		System.out.println();
		System.out.println("               ********************************************* " + cm.Year() + " *********************************************");
		System.out.println();
		System.out.println();
		
		yearStart = new Date();
	}
	
	
	protected ArrayList<VillageReturnsMsg>landRequestGangPlank (ArrayList<HouseholdRequest> allHHRequests){
		
		String LandRequestString = "";
		String dlm = ",";
		String dlm2 = ";";
		int totalRequests = allHHRequests.size();
		int population = 10; // TEMP
		int count = 0;
		
		for (HouseholdRequest hhRequest : allHHRequests){
			
			String hhRequestString = Integer.toString(hhRequest.gethhNumber())+ dlm + Integer.toString(hhRequest.getVillageNum()) + dlm + 
					Integer.toString(hhRequest.getPopulation()) + dlm + Integer.toString(hhRequest.getwheatLandNeed()) + dlm + Integer.toString(hhRequest.getbarleyLandNeed()) 
					+ dlm+ Integer.toString(hhRequest.getOCLandNeed());
					
			if (count == totalRequests-1){
				LandRequestString = LandRequestString + hhRequestString;
			}else{
				LandRequestString = LandRequestString + hhRequestString + dlm2;
			}
			count++;
		}
				
		String LandAssesReturn = null;
		
		try {
			LandAssesReturn = RunLandAssesser(LandRequestString);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		ArrayList<VillageReturnsMsg> vReturns = new ArrayList <VillageReturnsMsg>();
		
		vReturns = harpaxReturnsMessages(LandAssesReturn);
		
		return vReturns;
	}

	protected ArrayList<VillageReturnsMsg> harpaxReturnsMessages(String returnString){
		ArrayList<VillageReturnsMsg> vReturns = new ArrayList <VillageReturnsMsg>();
		VillageReturnsMsg vRM0 = new VillageReturnsMsg(0);
		VillageReturnsMsg vRM1 = new VillageReturnsMsg(1);
		
		//output ;HH#: avg_agri_return,avgoc_return, wheat_yield, barley_yield, oc_yield      ;HH#:avg_agri_return,avgoc_return,wheat_yield,barley_yield,oc_yield;HH#:avg_agri_return,avgoc_return,wheat_yield,barley_yield,oc_yield
		
		//			0			,	1			,	2		,	3		,	4			,5
		//;HH#:avg_wheat_return,avg_barley_return,avgoc_return,wheat_yield,barley_yield,oc_yield;
		
	    String data = "";
	    String[] sdata = null;
	    String[] mdata = null;
	    String[] rdata = null;
	    String dlm_regex = ";";
	    String dlm_comma = ",";
	    data = returnString;
	    HouseholdReturnsMessage hhRM = null;
	    //Split each HH, first value is useless
	    sdata = data.split(dlm_regex); 
	    dlm_regex = ":";
	        
		for ( int i = 1; i < sdata.length-1; i++ )
		{		
			mdata = sdata[i].split(dlm_regex);
			hhRM = new  HouseholdReturnsMessage(Integer.parseInt(mdata[0]));		// create new message with HH ID
			rdata = mdata [1].split(dlm_comma);
			for (int x=0;  x < rdata.length; x++){
				double avg_wheat_return = Double.parseDouble(rdata [0]);
				double avg_barley_return= Double.parseDouble(rdata [1]);
				double avgoc_return= Double.parseDouble(rdata [2]);
				double wheat_yield= Double.parseDouble(rdata [3]);
				double barley_yield= Double.parseDouble(rdata [4]);
				double oc_yield= Double.parseDouble(rdata [5]);		
				
				hhRM.setOCReturns( (double) oc_yield);
				hhRM.setOCReturnArea((double) avgoc_return); // using this variable for avg oc yield
				FarmData fData = new FarmData(avg_wheat_return, avg_barley_return, wheat_yield, barley_yield); // really x, y, return   temp change to avg_agri_return wheat_yield, barley_yield,
				hhRM.addFarm(fData);
			}
			
			//TODO this will not work if houses start being created... jsut a temp fix
			if (hhRM.gethhNumber() < 6){
				vRM0.addHouseholdReturnMsg(hhRM);
			}else{vRM1.addHouseholdReturnMsg(hhRM);}
			
		}			
		
		vReturns.add(vRM0);
		vReturns.add(vRM1);
		
		return vReturns;
	}
	
	protected ArrayList<VillageReturnsMsg> psuedoReturnsMessages(){
		ArrayList<VillageReturnsMsg> vReturns = new ArrayList <VillageReturnsMsg>();
		VillageReturnsMsg vRM0 = new VillageReturnsMsg(0);
		VillageReturnsMsg vRM1 = new VillageReturnsMsg(1);
		HouseholdReturnsMessage hhRM1 = new HouseholdReturnsMessage(1);
		HouseholdReturnsMessage hhRM2 = new HouseholdReturnsMessage(2);
		HouseholdReturnsMessage hhRM3 = new HouseholdReturnsMessage(3);
		HouseholdReturnsMessage hhRM4 = new HouseholdReturnsMessage(4);
		HouseholdReturnsMessage hhRM5 = new HouseholdReturnsMessage(5);
		HouseholdReturnsMessage hhRM0 = new HouseholdReturnsMessage(0);
		
		FarmData fd1 = new FarmData(2, 5, 500, 5);
		FarmData fd2 = new FarmData(6, 7, 500, 7);
		FarmData fd3 = new FarmData(7, 6, 500, 7);
		
		hhRM1.addFarm(fd1);
		hhRM1.addFarm(fd2);
		hhRM1.addFarm(fd3);
		hhRM1.setOCReturns(150);
		hhRM1.setOCReturnArea(50000);
		hhRM2.addFarm(fd1);
		hhRM2.addFarm(fd2);
		hhRM2.addFarm(fd3);
		hhRM2.setOCReturns(250);
		hhRM2.setOCReturnArea(50000);
		hhRM3.addFarm(fd1);
		hhRM3.addFarm(fd2);
		hhRM3.addFarm(fd3);
		hhRM3.setOCReturns(350);
		hhRM3.setOCReturnArea(50000);
		hhRM4.addFarm(fd1);
		hhRM4.addFarm(fd2);
		hhRM4.addFarm(fd3);
		hhRM4.setOCReturns(450);
		hhRM4.setOCReturnArea(50000);
		hhRM5.addFarm(fd1);
		hhRM5.addFarm(fd2);
		hhRM5.addFarm(fd3);
		hhRM5.setOCReturns(550);
		hhRM5.setOCReturnArea(50000);
		hhRM0.addFarm(fd1);
		hhRM0.addFarm(fd2);
		hhRM0.addFarm(fd3);
		hhRM0.setOCReturns(50);
		hhRM0.setOCReturnArea(50000);

		vRM0.addHouseholdReturnMsg(hhRM0);
		vRM0.addHouseholdReturnMsg(hhRM1);
		vRM0.addHouseholdReturnMsg(hhRM2);
		
		vRM1.addHouseholdReturnMsg(hhRM3);
		vRM1.addHouseholdReturnMsg(hhRM4);
		vRM1.addHouseholdReturnMsg(hhRM5);
		
		vReturns.add(vRM0);
		vReturns.add(vRM1);
		
		return vReturns;
	}
	
	public String getCurrentMapsetPath(){
		return this.mapsetpath;
	}
	
	
	
	public String getHHInfo(){
		String hhInfo = null;
		@SuppressWarnings("unchecked")
		final Iterable<Household> households = RunState.getInstance().getMasterContext()
				.getObjects(Household.class);
		final ArrayList<Household> hList = new ArrayList<Household>();

		for (final Household household : households) {
			hList.add(household);
		}
		
		for (Household household: hList){
			hhInfo = hhInfo + household.getHHInfo();
		}
		
		return hhInfo;
		
		
	}
	
	public int getHhCount(){
		@SuppressWarnings("unchecked")
		final Iterable<Household> households = RunState.getInstance().getMasterContext()
				.getObjects(Household.class);
		final ArrayList<Household> hList = new ArrayList<Household>();

		for (final Household household : households) {
			hList.add(household);
		}
		
		
		return hList.size();
	}
	
	protected ArrayList<Village> getVillageList() {
		@SuppressWarnings("unchecked")
		final Iterable<Village> villages = RunState.getInstance().getMasterContext()
				.getObjects(Village.class);
		final ArrayList<Village> vList = new ArrayList<Village>();

		for (final Village village : villages) {
			vList.add(village);
		}

		return vList;
	}

	// return current map name
	private String CurrentMap ( TypeMaps map )
	{
		String full_name = "";
		
		if ( map.IsDynamic() ){
			full_name += cm.Year();
		}
		if ( map.toString().compareTo("") != 0 ){
			full_name += map.toString();
		}
		else{
			full_name = "";
		}
		return full_name;
		
	}
	
	// return current map name with mapset suffix
	private String CurrentMapLoc ( TypeMaps map )
	{
		String full_name = CurrentMap(map);
		
		if ( full_name.compareTo("") != 0 ){
			return (full_name + "@" + mapset);
		}
		else{
			return "";
		}
	}
	
	// return current map name
	private String CurrentMap ( String specifier, TypeMaps map )
	{
		String full_name = "";
		
		if ( map.IsDynamic() ){
			full_name += cm.Year();
		}
		if ( specifier != null ){
			full_name += specifier;
		}
		full_name += map.toString();
		
		return full_name;
		
	}
	
	// return current map name with mapset suffix
	private String CurrentMapLoc ( String specifier, TypeMaps map )
	{
		String full_name = CurrentMap(specifier, map);

		return (full_name + "@" + mapset);
	}

	private int getTotalPopulation(){
		int population = 0;
		ArrayList<Village> vList = getVillageList();
		for (final Village village : vList){
			population += village.getPopulation();
		}
		return population;
	}

	
	private void putTimeInfoInPerformanceFile(long landRequestDuration, long landscapeEvolDuration) throws IOException{
		long time = this.yearEnd.getTime() - this.yearStart.getTime();
		double timeInSec = ((double)time)/((double)1000);
		this.performanceFile.write(cm.IntYear()+","+timeInSec+","+landRequestDuration+","+landscapeEvolDuration+","+getTotalPopulation()+"\n");
	}
	
}
