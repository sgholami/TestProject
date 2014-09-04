package stupidmodel.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunState;
import repast.simphony.parameter.Parameter;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import stupidmodel.common.FarmData;
import stupidmodel.common.HouseholdRequest;
import stupidmodel.common.HouseholdReturnsMessage;
import stupidmodel.common.ModelParameters;
import stupidmodel.common.SMUtils;
import stupidmodel.common.Constants;
import stupidmodel.common.VillageReturnsMsg;
import cern.jet.random.Normal;

public class Village {

	private int villageID;
	ArrayList<FarmData> farmsToReleaseList = new ArrayList<FarmData>();
	ArrayList<HouseholdReturnsMessage> householdLandReturns = new ArrayList<HouseholdReturnsMessage>();
	ModelParameters mp;
	//TODO make above null annually

	/**
	 * Maximum food consumption of the bug (set to <code>1.0</code> by default).
	 */
	private double maxConsumptionRate = 1.0;

	/**
	 * A new bug parameter <code>survivalProbability</code> is initialized to
	 * <code>0.95</code>. Each time step, each bug draws a uniform random
	 * number, and if it is greater than <code>survivalProbability</code>, the
	 * bug dies and is dropped.
	 * 
	 * @since Model 12
	 */
	private double survivalProbability = 0.95;

	/**
	 * Creates a new instance of <code>Village</code> agent.
	 */
	public Village() {
		super();
	}

	
	public void setParameterSet(ModelParameters mp){
		 this.mp = mp;
	}

	public void setVillageID(final int id) {

		this.villageID = id;
	}
	
	public int getVillageID() {
		return this.villageID;
	}
	
	protected ArrayList<Household> getHouseList() {
		@SuppressWarnings("unchecked")
		final Iterable<Household> houses = RunState.getInstance().getMasterContext()
				.getObjects(Household.class);
		final ArrayList<Household> hList = new ArrayList<Household>();

		for (final Household house : houses) {
			int vID = house.getVillageMemberID();
			if (vID == this.villageID){
				hList.add(house);
			}		
		}

		return hList;
	}
	
	public double getPopulation(){
		double totalPop= 0;
		final ArrayList<Household> hhList = getHouseList();
		for (final Household hh : hhList) {
			totalPop = totalPop + hh.getPopulation();
		}	
		return totalPop;
	}
	


	/**
	 * Returns the survival probability of the bug.
	 * 
	 * @return survival probability; <i>value is on the interval
	 *         <code>[0, 1]</code></i>
	 * @see #survivalProbability
	 * @since Model 12
	 */
	@Parameter(displayName = "Bug survival probability", usageName = "survivalProbability")
	public double getSurvivalProbability() {
		return survivalProbability;
	}

	/**
	 * Sets the new survival probability of the bug (<i>it is an agent-level
	 * parameter</i>).
	 * 
	 * @param survivalProbability
	 *            the new survival probability of the agent; <i>must be on the
	 *            interval <code>[0, 1]</code></i>
	 * @see #survivalProbability
	 * @since Model 12
	 */
	public void setSurvivalProbability(final double survivalProbability) {
		if (survivalProbability < 0.0 || 1.0 < survivalProbability) {
			throw new IllegalArgumentException(
					String.format(
							"Parameter survivalProbability=%f should be in interval [0, 1].",
							survivalProbability));
		}

		this.survivalProbability = survivalProbability;
	}
	
	
	public ArrayList<FarmData> releaseLand(){
	
		//ASK VILLAGES IF THEY NEED TO RELEASE LAND
		final ArrayList<Household> hhList = getHouseList();
		final ArrayList<FarmData> farms = new ArrayList<FarmData>(); 
		
		//ADD UNWANTED LAND FROM HOUSEHOLDS TO THE RELEASE LIST
		for (final Household hh : hhList) {
			ArrayList<FarmData> farmsToRelease = hh.getLandToRelease();
			
			if (farmsToRelease != null){
				for (final FarmData rFarm : farmsToRelease) {
					farms.add(rFarm);
				}
			}
		//ADD LAND FROM DECEASED HOUSEHOLDS TO THE RELEASE LIST
		}
		for (FarmData farm : this.farmsToReleaseList){
			farms.add(farm);
		}
		this.farmsToReleaseList.clear();
		
		
		return farms;
	}
	
	
	public void disseminateReturns(){
		ArrayList <Household> hhList = getHouseList();
		for (Household aHousehold : hhList){		
			aHousehold.evaluateAnnualReturns();
		}
	}
	
	
	public void receiveHHLandReturns(VillageReturnsMsg hhReturnsMessages){
		
		VillageReturnsMsg vReturnsMsg =  hhReturnsMessages;
		ArrayList <HouseholdReturnsMessage> hhRMs = vReturnsMsg.getHouseholdReturnMsgList();
		ArrayList <Household> hhList = getHouseList();
	
		for (HouseholdReturnsMessage hhMessage : hhRMs) {
			int hhMID = hhMessage.gethhNumber();
			for (int i = 0; i< hhList.size(); i++){
				Household aHousehold = (Household) hhList.get(i);			
				if(aHousehold.getHhId() == hhMID){
					aHousehold.receiveAnnualReturns(hhMessage);
					i = hhList.size();
				}	
			}
			//TODO find a real way to do below
			//hhList.remove(remove);
		}
		
		
		
		
	}
	//TODO Make this list null each timestamp
	
	public ArrayList <HouseholdRequest> getHHLandRequests() {
		// Get number of cells needed, hh land requests
		//System.out.println("This village is steppin yo " +this.villageID );

		final ArrayList<Household> hhList = getHouseList();
		//SimUtilities.shuffle(hhList, RandomHelper.getUniform());

		HouseholdRequest hhNeeds = null;
		final ArrayList<HouseholdRequest> hhRequestList = new ArrayList<HouseholdRequest>();
		for (final Household hh : hhList) {
			hhNeeds = hh.getLandNeed();
			hhRequestList.add(hhNeeds);
		}

		return hhRequestList;
	}

	/**
	 * Return the habitat cells for those grid points where no {@link Bug} agent
	 * is located at.
	 * 
	 * @param freeCells
	 *            list of cells where no agents is located at
	 * @return list of {@link HabitatCell} objects associated for the specified
	 *         empty locations
	 */
	private List<GridCell<HabitatCell>> getHabitatCellsForLocations(
			final List<GridCell<Village>> freeCells) {
		// Parameter freeCells should contain only empty cells
		assert (freeCells.equals(SMUtils.getFreeGridCells(freeCells)));

		final ArrayList<GridCell<HabitatCell>> ret = new ArrayList<GridCell<HabitatCell>>();
		final Grid<Object> grid = SMUtils.getGrid(this);

		// Iterate over the specified location with no associated Bug agents
		for (final GridCell<Village> gridCell : freeCells) {
			final GridPoint point = gridCell.getPoint();

			// Query the HabitatCell of that location
			final List<GridCell<HabitatCell>> cells = new GridCellNgh<HabitatCell>(
					grid, point, HabitatCell.class, 0, 0).getNeighborhood(true);

			// One cell should exist on a grid cell
			assert (1 == cells.size());
			ret.add(cells.get(0));
		}

		return ret;
	}

	

	
	/**
	 * Bug growth is modified so growth equals food consumption.
	 * 
	 * <p>
	 * Food consumption is equal to the minimum of <i>(a)</i> the bug's maximum
	 * consumption rate (set to <code>1.0</code>) and <i>(b)</i> the bug's
	 * cell's food availability.
	 * </p>
	 * 
	 * <p>
	 * In previous models, a bug grew by a fixed amount of size in each time
	 * step.
	 * </p>
	 * 
	 * @return the actual eaten food value between the specified bounds;
	 *         <i>non-negative, lower or equal to
	 *         <code>maxConsumptionRate</code> and <code>foodAvailable</code>
	 *         </i>
	 * @since Model 3
	 */


	/**
	 * Returns the cell on which this agents is currently located at.
	 * 
	 * <p>
	 * Also, it contains minor assertions and ensures invariants for the model:
	 * there should be exactly one cell for each agent, no more and no less. If
	 * either constraint is broken, an <code>IllegalStateException</code> is
	 * thrown.
	 * </p>
	 * 
	 * @return the cell on which the agent is currently located at;
	 *         <code>non-null</code>
	 * @since Model 3
	 */
	protected HabitatCell getUnderlyingCell() {
		final Grid<Object> grid = SMUtils.getGrid(this);
		final GridPoint location = grid.getLocation(this);
		final Iterable<Object> objects = grid.getObjectsAt(location.getX(),
				location.getY());

		HabitatCell ret = null;

		for (final Object object : objects) {
			if (object instanceof HabitatCell) {
				final HabitatCell cell = (HabitatCell) object;
				if (ret != null) {
					throw new IllegalStateException(
							String.format(
									"Multiple cells defined for the same position;cell 1=%s, cell 2=%s",
									ret, cell));
				}

				ret = cell;
			}
		}

		if (null == ret) {
			throw new IllegalStateException(String.format(
					"Cannot find any cells for location %s", location));
		}

		return ret;
	}

	public void migration() {
		
		ArrayList<Household> hhList = getHouseList();
		for (final Household hh : hhList) {
			boolean hhWantsToMigrate = hh.migration();
			if (hhWantsToMigrate == true){
				System.out.println("HH Wants to Migrate");
				ArrayList<FarmData> hhFarmsToReleaseList = new ArrayList<FarmData>();
				hhFarmsToReleaseList = hh.getOwnedFarmLand();
				for (FarmData fd :hhFarmsToReleaseList){					
					this.farmsToReleaseList.add(fd);
				}
				//TODO HH assigned new village
				hh.migrationAccomplished();
				
			}
		}

		hhList = getHouseList();
		if (hhList.size() < 1){
			System.out.println("Village Empty, Attempting to remove");
			die();
		}
        
	}
	public void mortality() {
	/*	
		ArrayList<Household> hhList = getHouseList();
		for (final Household hh : hhList) {
			boolean hhAlive = hh.mortality();
			if (hhAlive == false){
				System.out.println("HH Bit It");
				ArrayList<FarmData> hhFarmsToReleaseList = new ArrayList<FarmData>();
				hhFarmsToReleaseList = hh.getOwnedFarmLand();
				for (FarmData fd :hhFarmsToReleaseList){					
					this.farmsToReleaseList.add(fd);
				}	 
				hh.die();
			}
		}

		hhList = getHouseList();
		if (hhList.size() < 1){
			System.out.println("Village Empty, Attempting to remove");
			die();
		}
       */ 
	}

	/**
	 * Perform the reproduction of an agent.
	 * 
	 * @since Model 12
	 */
	public int reproduce(int hhID) {
		int hhIdCounter = hhID;
		final ArrayList<Household> hhList = getHouseList();
		for (final Household hh : hhList) {
			boolean houseFission = hh.reproduce();
			if (houseFission == true){
				System.out.println("*******************       Trying to reproduce in Village "+ this.villageID+ " HHID counter "+ hhIdCounter);
				final Context<Object> context = (Context<Object>) ContextUtils.getContext(this);
				final Grid<Object> grid = SMUtils.getGrid(this);
				final GridPoint location = grid.getLocation(this);
				final Household household = new Household(this.mp);
				//TODO decide on a way to determine the starting pop for new HH
				household.setPopulation(6);
				household.setOperativeStatus(true);
				household.setVillageMemberID(this.villageID);
				household.setHhId(hhIdCounter);
				hhIdCounter++;
				context.add(household);
				System.out.println("*******************       Successful");
				//TODO what land do new households start with
			}
			
			
			
			//TODO Evaluate if this village needs to fission
		}
		return hhIdCounter;
		/*
		// Make sure the agent is big enough to reproduce
		assert (size >= Constants.MAX_BUG_SIZE);

		// Get the current context, grid and location
		@SuppressWarnings("unchecked")
		final Context<Object> context = (Context<Object>) ContextUtils.getContext(this);

		final Grid<Object> grid = SMUtils.getGrid(this);
		final GridPoint location = grid.getLocation(this);

		// Spawn the specified number of descendants

		for (int i = 0; i < Constants.BUG_REPRODUCTION_RATE; ++i) {
			// Create new bug with specified default size
			final Bug child = new Bug();
			child.setSize(0.0);

			// Get the reproduction range of the current bug
			final List<GridCell<Bug>> bugNeighborhood = new GridCellNgh<Bug>(
					grid, location, Bug.class,
					Constants.BUG_REPRODUCTION_RANGE,
					Constants.BUG_REPRODUCTION_RANGE).getNeighborhood(false);

			// We have a utility function that returns the filtered list of
			// empty GridCells objects
			final List<GridCell<Bug>> freeCells = SMUtils
					.getFreeGridCells(bugNeighborhood);

			// Model specifies if there is no empty location in vision range,
			// no new child should be spawned
			if (freeCells.isEmpty()) {
				break;
			}

			// Choose one of the possible cells randomly
			final GridCell<Bug> chosenFreeCell = SMUtils
					.randomElementOf(freeCells);

			// Add the new bug to the context and to the grid
			context.add(child);

			// We have our new GridPoint to move to, so locate agent
			final GridPoint newGridPoint = chosenFreeCell.getPoint();
			grid.moveTo(child, newGridPoint.getX(), newGridPoint.getY());
		}
		
		*/
	}

	/**
	 * Simple utility function to show how to delete an agent.
	 * 
	 * <p>
	 * In Model 16, it was modified since {@link Predator} agents can also kill
	 * a {@link Bug}, so it was modified to a <code>public</code> method.
	 * </p>
	 * 
	 * @since Model 12, Model 16
	 */
	public void die() {
		ContextUtils.getContext(this).remove(this);
	}

	/**
	 * Two new model parameters were added to the model, and put on the
	 * parameter settings window: <code>initialBugSizeMean</code> and
	 * <code>initialBugSizeSD</code>.
	 * 
	 * <p>
	 * Values of these parameters are <code>0.1</code> and <code>0.03</code>.
	 * Instead of initializing bug sizes to <code>1.0</code> (as defined in
	 * previous models), sizes are drawn from a normal distribution defined by
	 * <code>initialBugSizeMean</code> and <code>initialBugSizeSD</code>. The
	 * initial size of bugs produced via reproduction is still <code>0.0</code>.
	 * </p>
	 * 
	 * <p>
	 * Negative values are very likely to be drawn from normal distributions
	 * such as the one used here. To avoid them, a check is introduced to limit
	 * initial bug size to a minimum of zero.
	 * </p>
	 * 
	 * @param normal
	 *            a properly initialized <code>Normal</code> distribution (cf.
	 *            to <code>RandomHelper</code> documentation); <i>cannot be
	 *            <code>null</code></i>
	 * @since Model 14
	 */


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// This may happen when testing
		final String location = (ContextUtils.getContext(this) != null) ? SMUtils
				.getGrid(this).getLocation(this).toString()
				: "[?, ?]";

		// Override default Java implementation just to have a nicer
		// representation
		return String.format("Bug @ location %s, size=%f", location);
	}

}

