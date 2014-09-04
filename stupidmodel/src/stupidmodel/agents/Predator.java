/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/agents/Predator.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel.agents;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import stupidmodel.common.Constants;
import stupidmodel.common.SMUtils;

/**
 * In <i>Model 16</i>, we have a new agent type for predators.
 * 
 * <p>
 * Predator agents have a really simple behaviour: they start random walking on
 * the grid, and as soon as they find a {@link Bug} agent, they eat it and move
 * to their position. All of the predator actions are scheduled after the
 * {@link Bug} actions.
 * </p>
 * 
 * <p>
 * Since predator agents are actually very simple, they are visualized with the
 * <code>DefaultStyleOGL2D</code> offered by the wizard on the graphical user
 * interface.
 * </p>
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @since Model 16
 * @version $Id: Predator.java 428 2011-06-18 14:19:31Z
 *          richard.legendi@gmail.com $
 */
public class Predator {

	/**
	 * Creates a new instance of <code>Predator</code> agent.
	 */
	public Predator() {
		super();
	}

	/**
	 * Predators move randomly and search for bugs in their surroundings. If
	 * they find the first, the found {@link Bug} agent is killed and they move
	 * to the location of the killed {@link Bug}.
	 * 
	 * <p>
	 * <i>Scheduled after all the {@link Bug} actions.</i>
	 * </p>
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = -1)
	public void hunt() {
		// Get the reference for the used grid
		final Grid<Object> grid = SMUtils.getGrid(this);

		// Get the grid location of this agent
		final GridPoint location = grid.getLocation(this);

		// First, a predator looks through a shuffled list of its immediately
		// neighboring cells (including its own cell)

		final List<GridCell<HabitatCell>> cellNeighborhood = new GridCellNgh<HabitatCell>(
				grid, location, HabitatCell.class,
				Constants.PREDATOR_VISION_RANGE,
				Constants.PREDATOR_VISION_RANGE).getNeighborhood(true);

		SimUtilities.shuffle(cellNeighborhood, RandomHelper.getUniform());

		// As soon as the predator finds a bug in one of these cells it "kills"
		// the bug and moves into the cell

		for (final GridCell<HabitatCell> cell : cellNeighborhood) {
			if (hasAgent(grid, cell, Bug.class)) {
				killBugAt(grid, cell);
				moveTo(grid, cell);
				return;
			}
		}

		// If these cells contain no bugs, the predator moves randomly to one of
		// them
		final GridCell<HabitatCell> randomCell = SMUtils
				.randomElementOf(cellNeighborhood);

		// However, if the cell already contains a predator, the
		// hunting predator simply quits and remains at its current
		// location
		if (hasAgent(grid, randomCell, Predator.class)) {
			return;
		}

		moveTo(grid, randomCell);
	}

	/**
	 * Utility function for {@link Predator} agents to determine if a given cell
	 * whether has an agent from a specified type located at or not.
	 * 
	 * @param <T>
	 *            the type of the agent we are searching for at the specified
	 *            location
	 * @param grid
	 *            the grid on which the {@link Predator} agent is located at;
	 *            <i>should not be <code>null</code></i>
	 * @param cell
	 *            the cell where specified agent type is searched; <i>should not
	 *            be <code>null</code></i>
	 * @param clazz
	 *            the <code>{@literal Class<T>}</code> object of the agent we
	 *            are searching for
	 * @return <code>true</code> if the number of agent instances from the
	 *         specified type at the given location is above zero;
	 *         <code>false</code> otherwise
	 */
	private <T> boolean hasAgent(final Grid<Object> grid,
			final GridCell<HabitatCell> cell, final Class<T> clazz) {
		assert (grid != null);
		assert (cell != null);

		final List<GridCell<T>> neighborhood = new GridCellNgh<T>(grid,
				cell.getPoint(), clazz, 0, 0).getNeighborhood(true);

		assert (neighborhood != null);
		assert (1 == neighborhood.size());

		final int ctr = neighborhood.get(0).size();

		return (ctr > 0);
	}

	/**
	 * The {@link Predator} agent kills the {@link Bug} agent located at the
	 * specified grid cell.
	 * 
	 * @param grid
	 *            the grid on which the {@link Predator} agent is located at;
	 *            <i>should not be <code>null</code></i>
	 * @param cell
	 *            the cell where the {@link Bug} agent is located which has to
	 *            be destroyed; <i>should not be <code>null</code></i>
	 */
	private void killBugAt(final Grid<Object> grid,
			final GridCell<HabitatCell> cell) {
		assert (grid != null);
		assert (cell != null);
		assert (hasAgent(grid, cell, Bug.class));

		final List<GridCell<Bug>> bugsAt = new GridCellNgh<Bug>(grid,
				cell.getPoint(), Bug.class, 0, 0).getNeighborhood(true);

		assert (1 == bugsAt.size());
		bugsAt.get(0).items().iterator().next().die();
	}

	/**
	 * Moves this agent on the specified grid to the pointed new location.
	 * 
	 * @param grid
	 *            the grid on which the {@link Predator} agent is located at;
	 *            <i>should not be <code>null</code></i>
	 * @param cell
	 *            the cell where the agent should migrate to; <i>should not be
	 *            <code>null</code></i>
	 */
	private void moveTo(final Grid<Object> grid,
			final GridCell<HabitatCell> cell) {
		assert (grid != null);
		assert (cell != null);

		// We have our new GridPoint to move to, so relocate agent
		final GridPoint newGridPoint = cell.getPoint();
		grid.moveTo(this, newGridPoint.getX(), newGridPoint.getY());
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
		return String.format("Predator @ location %s", location);
	}

}
