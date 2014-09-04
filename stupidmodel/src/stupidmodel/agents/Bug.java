/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/agents/Bug.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.parameter.Parameter;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import stupidmodel.StupidModelContextBuilder;
import stupidmodel.common.Constants;
import stupidmodel.common.SMUtils;
import cern.jet.random.Normal;

/**
 * Bug agent implementation for <i>StupidModel</i> versions.
 * 
 * <p>
 * Agents in this model are very simple: they can access their neihgbourhood at
 * a grid and move to a random position within sight range (that is a fixed
 * constant for now, see {@link Constants#BUG_VISION_RANGE}.
 * </p>
 * 
 * <p>
 * If there is no free cell around the agent, it cannot move, so it stays in its
 * current position.
 * </p>
 * 
 * <p>
 * From <i>Model 12</i>, agents are die and reproduce.
 * <p>
 * 
 * </p>
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @version $Id: Bug.java 1006 2011-08-19 10:34:40Z richard.legendi@gmail.com $
 */
public class Bug {

	/**
	 * <p>
	 * From <i>Model 10</i>, agents are comparable with their <code>size</code>
	 * in descending order.
	 * </p>
	 * 
	 * <p>
	 * It would be easier to make class {@link Bug} comparable, however, it
	 * would break the contract of <code>Comparable</code>, which states:
	 * </p>
	 * 
	 * <blockquote> It is strongly recommended, but <i>not</i> strictly required
	 * that <code>{@literal (x.compareTo(y)==0) == (x.equals(y))}</code>.
	 * Generally speaking, any class that implements the <code>Comparable</code>
	 * interface and violates this condition should clearly indicate this fact.
	 * The recommended language is <i>
	 * "Note: this class has a natural ordering that is inconsistent with equals."
	 * </i> </blockquote>
	 * 
	 * @since Model 12
	 */
	public static class BugSizeComparator implements Comparator<Bug>,
			Serializable {

		/**
		 * Use <code>serialVersionUID</code> from JDK 1.0.2 for
		 * interoperability.
		 */
		private static final long serialVersionUID = 3468196663094781744L;

		/**
		 * {@inheritDoc}
		 * 
		 * <p>
		 * For the current model, <code>Bug</code> agents are comparable by
		 * their <code>size</code> values: <i>a bug is "bigger" if its size
		 * value is bigger than the value of other one</i>. For this, we compare
		 * the size values by the default Java way multiplied by
		 * <code>(-1)</code> (the default Java comparison would result in an
		 * ascending order, the multiplier makes it a descending comparison).
		 * </p>
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 * @throws NullPointerException
		 *             if parameter is null.
		 */
		@Override
		public int compare(final Bug o1, final Bug o2) {
			if (null == o1) {
				throw new IllegalArgumentException("Parameter o1 == null.");
			}

			if (null == o2) {
				throw new IllegalArgumentException("Parameter o2 == null.");
			}

			return -Double.compare(o1.getSize(), o2.getSize());
		}
	}

	/**
	 * Bugs have an instance variable for their size, which is initialized to
	 * 1.0.
	 * 
	 * @since Model 2
	 */
	private double size = 1.0;

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
	 * Creates a new instance of <code>Bug</code> agent.
	 */
	public Bug() {
		super();
	}

	/**
	 * Returns the size of the bug.
	 * 
	 * @return the size of the bug
	 * @since Model 2
	 */
	public double getSize() {
		return size;
	}

	/**
	 * Sets the size of the bug, <i>used for testing</i>.
	 * 
	 * @param size
	 *            the new size of the bug; <i>must be non-negative</i>
	 * @since Model 2
	 */
	public void setSize(final double size) {
		if (size < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter size = %f < 0.", size));
		}

		this.size = size;
	}

	/**
	 * Returns the maximum food consumption rate of the current bug.
	 * 
	 * <p>
	 * Parameter usage II: This method was created to demonstrate the usage of
	 * parameters declared at the level of agents.
	 * </p>
	 * 
	 * @return the value of {@link #maxConsumptionRate}
	 * @since Model 5
	 * @see StupidModelContextBuilder#build(repast.simphony.context.Context)
	 */
	@Parameter(displayName = "Bug maximum food consumption rate", usageName = "maxConsumptionRate")
	public double getMaxConsumptionRate() {
		return maxConsumptionRate;
	}

	/**
	 * Sets the maximum food consumption rate of the current bug.
	 * 
	 * @param maxConsumptionRate
	 *            the new value of {@link #maxConsumptionRate}; <i>must be
	 *            non-negative</i>
	 * @since Model 5
	 */
	public void setMaxConsumptionRate(final double maxConsumptionRate) {
		if (maxConsumptionRate < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter maxConsumptionRate = %f < 0.",
					maxConsumptionRate));
		}

		this.maxConsumptionRate = maxConsumptionRate;
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

	/**
	 * Implementation of the agent activity in each turn.
	 * 
	 * <p>
	 * Agents work in a very simple way: they gather their neighbourhood and
	 * check for empty locations. If any is found, one of them is randomly
	 * chosen and the agent is relocated to that location.
	 * </p>
	 */
	public void step() {
		// Reference for the used grid
		final Grid<Object> grid = SMUtils.getGrid(this);
		// Get the grid location of this Bug
		final GridPoint location = grid.getLocation(this);

		// We use the GridCellNgh class to create GridCells for the surrounding
		// neighborhood. It contains the locations, and a list of objects from
		// the specified class which is accessible from that location

		final List<GridCell<Bug>> bugNeighborhood = new GridCellNgh<Bug>(grid,
				location, Bug.class, Constants.BUG_VISION_RANGE,
				Constants.BUG_VISION_RANGE).getNeighborhood(false);

		// We have a utility function that returns the filtered list of empty
		// GridCells objects
		final List<GridCell<Bug>> freeCells = SMUtils
				.getFreeGridCells(bugNeighborhood);

		// Model specifies if there is no empty location in vision range, the
		// Bug agent cannot move
		if (freeCells.isEmpty()) {
			return;
		}

		// Model 15: A change to the bug move method is required to avoid a very
		// strong artifact now that cell food production is no longer random.
		// Near the start of a simulation, many cells will have exactly the same
		// food availability, so a bug simply would move to the first cell on
		// its list of neighbor cells. This is always the top-left cell among
		// the neighbors, so bugs move constantly up and left if all the cells
		// available to them have the same food availability. This artifact is
		// removed by randomly shuffling the list of available cells before the
		// bug loops through it to identify the best.

		SimUtilities.shuffle(freeCells, RandomHelper.getUniform());

		final List<GridCell<HabitatCell>> habitatCells = getHabitatCellsForLocations(freeCells);
		assert (habitatCells.size() == freeCells.size());

		Collections.sort(habitatCells,
				HabitatCell.HABITAT_CELL_FOOD_AVAILABILITY_COMPARATOR);

		// The first element has the most available food, it is the optimal
		// target for displacement
		final GridCell<HabitatCell> chosenFreeCell = habitatCells.get(0);

		// We have our new GridPoint to move to, so relocate agent
		final GridPoint newGridPoint = chosenFreeCell.getPoint();
		grid.moveTo(this, newGridPoint.getX(), newGridPoint.getY());
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
			final List<GridCell<Bug>> freeCells) {
		// Parameter freeCells should contain only empty cells
		assert (freeCells.equals(SMUtils.getFreeGridCells(freeCells)));

		final ArrayList<GridCell<HabitatCell>> ret = new ArrayList<GridCell<HabitatCell>>();
		final Grid<Object> grid = SMUtils.getGrid(this);

		// Iterate over the specified location with no associated Bug agents
		for (final GridCell<Bug> gridCell : freeCells) {
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
	 * Each time step, a bug grows by a fixed amount, <code>1.0</code>, and this
	 * action is scheduled after the <code>move()</code> action.
	 * 
	 * <p>
	 * From <i>Model 7</i>, this function also verifies if the biggest bug
	 * reached size of <code>100</code>, and if so, it stops the simulation.
	 * </p>
	 * 
	 * @since Model 2, Model 7, Model 12
	 */
	public void grow() {
		size += foodConsumption();

		// The model stopping rule is changed in Model 12, it was defined here.
		// Check it in the previous versions, but the task is simple by calling
		// RunEnvironment.getInstance().endRun()
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
	protected double foodConsumption() {
		final HabitatCell cell = getUnderlyingCell();
		final double foodAvailable = cell.getFoodAvailability();

		final double eatenFood = Math.min(maxConsumptionRate, foodAvailable);
		cell.foodConsumed(eatenFood);

		// Derived value of eatenFood should be >=0.
		assert (eatenFood >= 0);
		assert (eatenFood <= maxConsumptionRate);
		assert (eatenFood <= foodAvailable);

		return eatenFood;
	}

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

	/**
	 * New activity for the agents in <i>Model 12</i>.
	 * 
	 * <ul>
	 * <li>When a bug's size reaches <code>10</code>, it reproduces by splitting
	 * into <code>5</code> new bugs. Each new bug has an initial size of
	 * <code>0.0</code>, and the old bug disappears.</li>
	 * 
	 * <li>New bugs are placed at the first empty location randomly selected
	 * within <code>+/- 3</code> cells of their parent’s last location. If no
	 * location is identified within <code>5</code> random draws, then the new
	 * bug dies.</li>
	 * 
	 * <li>A new bug parameter <code>survivalProbability</code> is initialized
	 * to <code>0.95</code>. Each time step, each bug draws a uniform random
	 * number, and if it is greater than <code>survivalProbability</code>, the
	 * bug dies and is dropped.</li>
	 * </ul>
	 * 
	 * @since Model 12
	 */
	public void mortality() {

		// If size is great enough, reproduce and disappear
		if (size >= Constants.MAX_BUG_SIZE) {
			reproduce();
			die();
			return;
		}

		// Check the uniform random survival probability, and drop agent if
		// necessary
		if (SMUtils.prob(survivalProbability)) {
			die();
		}
	}

	/**
	 * Perform the reproduction of an agent.
	 * 
	 * @since Model 12
	 */
	protected void reproduce() {
		// Make sure the agent is big enough to reproduce
		assert (size >= Constants.MAX_BUG_SIZE);

		// Get the current context, grid and location
		@SuppressWarnings("unchecked")
		final Context<Object> context = (Context<Object>) ContextUtils
				.getContext(this);

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
	public void setInitialSize(final Normal normal) {
		if (null == normal) {
			throw new IllegalArgumentException("Parameter normal == null.");
		}

		// Generate a new random initial size value, and if it is negative, use
		// 0.0 instead
		final double initialSize = Math.max(normal.nextDouble(), 0.0);
		setSize(initialSize);
	}

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
		return String.format("Bug @ location %s, size=%f", location, size);
	}

}
