/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/agents/HabitatCell.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameter;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;
import stupidmodel.StupidModelContextBuilder;
import stupidmodel.common.CellData;
import stupidmodel.common.Constants;

/**
 * Habitat cell objects have instance variables for their food availability and
 * maximum food production rate.
 * 
 * <p>
 * Below <i>Model 15</i>, the food production rate was defined by a random value
 * between <code>0</code> and <code>maximumFoodProductionRate</code>. However,
 * it was eliminated from <i>Model 15</i>, since these models work with real
 * data.
 * </p>
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @since Model 3
 * @version $Id: HabitatCell.java 183 2011-05-29 17:09:27Z
 *          richard.legendi@gmail.com $
 */
public class HabitatCell {

	/**
	 * Default comparator used to compare {@link HabitatCell} objects by their
	 * current food availability.
	 */
	public static final HabitatCellFoodAvailabilityComparator HABITAT_CELL_FOOD_AVAILABILITY_COMPARATOR = new HabitatCellFoodAvailabilityComparator();

	/**
	 * Simple comparator for {@link HabitatCell} objects that orders them
	 * descending based on the current food availability.
	 * 
	 * @author Richard O. Legendi (richard.legendi)
	 * @since 2.0-beta, 2011
	 * @since Model 11
	 */
	private final static class HabitatCellFoodAvailabilityComparator implements
			Comparator<GridCell<HabitatCell>>, Serializable {

		/**
		 * Use <code>serialVersionUID</code> from JDK 1.0.2 for
		 * interoperability.
		 */
		private static final long serialVersionUID = 3468196663094781744L;

		/**
		 * {@inheritDoc}
		 * 
		 * <p>
		 * Two cells are compared based on their
		 * {@link HabitatCell#foodAvailability} value by the default Java
		 * comparator for <code>double</code> values.
		 * </p>
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final GridCell<HabitatCell> gc1,
				final GridCell<HabitatCell> gc2) {
			// Make sure if exactly 1 HabitatCell exists
			assert (hasOneCell(gc1));
			assert (hasOneCell(gc2));

			final HabitatCell cell = gc1.items().iterator().next();
			final HabitatCell bell = gc2.items().iterator().next();
			return -Double
					.compare(cell.foodAvailability, bell.foodAvailability);
		}

		/**
		 * A function to validate that the specified <code>GridCell</code>
		 * instance has exactly one {@link HabitatCell} instance associated.
		 * 
		 * <p>
		 * <i>Assertative function.</i>
		 * </p>
		 * 
		 * @param gc
		 *            the <code>GridCell</code> object to validate
		 * @return <code>true</code> if there is exactly one {@link HabitatCell}
		 *         on the specified cell; <code>false</code> otherwise
		 */
		private boolean hasOneCell(final GridCell<HabitatCell> gc) {
			final ArrayList<HabitatCell> list = new ArrayList<HabitatCell>();
			for (final HabitatCell cell : gc.items()) {
				list.add(cell);
			}

			return (1 == list.size());
		}
	}

	// Members declared package-protected to be able to use them in the tests

	/**
	 * Default food production rate is initialized to <code>0.01</code>.
	 * 
	 * <p>
	 * It is set by the constructor, but not declared as final, since the use
	 * may want to manipulate it at the graphical interface (<i>probing</i>, as
	 * defined in <i>Model 4</i>).
	 * </p>
	 * 
	 * @since Model 15
	 */
	protected double foodProductionRate = 0.01;

	/**
	 * Represents the actual food availability at this cell, initialized to
	 * <code>0.0</code>.
	 */
	protected double foodAvailability = 0.0;

	/** Location of this cell at the grid. */
	protected final int x, y;

	/**
	 * Creates a new instance of <code>HabitatCell</code>.
	 * 
	 * <p>
	 * Previous versions created the cell directly, but from <i>Model 15</i>, we
	 * use a wrapper class for the parsed data to create a new instance
	 * properly.
	 * </p>
	 * 
	 * @param cellData
	 *            a parsed data from the specified input file containing the
	 *            coordinates and the food production rate; <i>cannot be
	 *            <code>null</code></i>
	 * @since Model 3, Model 15
	 */
	public HabitatCell(final CellData cellData) {
		if (null == cellData) {
			throw new IllegalArgumentException("Parameter cellData == null.");
		}

		this.x = cellData.getX();
		this.y = cellData.getY();
		this.foodProductionRate = cellData.getFoodProductionRate();
	}

	/**
	 * Returns the maximum food production rate of the current cell.
	 * 
	 * <p>
	 * Parameter usage II: This method was created to demonstrate the usage of
	 * parameters declared at the level of agents.
	 * </p>
	 * 
	 * @return the value of {@link #foodProductionRate}
	 * @since Model 5, Model 15
	 * @see StupidModelContextBuilder#build(repast.simphony.context.Context)
	 */
	@Parameter(displayName = "Cell maximum food production rate", usageName = "foodProductionRate")
	public double getFoodProductionRate() {
		return foodProductionRate;
	}

	/**
	 * Sets the maximum food consumption rate of the current cell.
	 * 
	 * @param foodProductionRate
	 *            the new value of {@link #foodProductionRate}; <i>must
	 *            be non-negative</i>
	 * @since Model 5, Model 15
	 */
	public void setFoodProductionRate(final double foodProductionRate) {
		if (foodProductionRate < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter foodProductionRate == %f < 0.",
					foodProductionRate));
		}

		this.foodProductionRate = foodProductionRate;
	}

	/**
	 * Returns the current food availability of this cell.
	 * 
	 * @return the <code>foodAvailability</code> of the cell; <i>a non-negative
	 *         value</i>
	 */
	public double getFoodAvailability() {
		return foodAvailability;
	}

	/**
	 * Sets the current food availability of this cell.
	 * 
	 * @param foodAvailability
	 *            the new <code>foodAvailability</code> value; <i>must be
	 *            non-negative</i>
	 */
	public void setFoodAvailability(final double foodAvailability) {
		if (foodAvailability < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter foodAvailability == %f < 0.", foodAvailability));
		}

		this.foodAvailability = foodAvailability;
	}

	/**
	 * Each time step, food availability is increased by food production. The
	 * food production in the previous versions was a random floating point
	 * number between zero and the maximum food production; from <i>Model 15</i>
	 * it is exactly defined in the specified input data file.
	 * 
	 * <p>
	 * Food production is scheduled before agent actions.
	 * </p>
	 * 
	 * @since Model 5, Model 15
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = -2)
	public void growFood() {
		foodAvailability += foodProductionRate;

		final GridValueLayer foodValueLayer = (GridValueLayer) ContextUtils
				.getContext(this).getValueLayer(Constants.FOOD_VALUE_LAYER_ID);

		if (null == foodValueLayer) {
			throw new IllegalStateException(
					"Cannot locate food value layer with ID="
							+ Constants.FOOD_VALUE_LAYER_ID + ".");
		}

		foodValueLayer.set(getFoodAvailability(), x, y);
	}

	/**
	 * A {@link Bug} agent can consume food of the cell on which it is located
	 * at.
	 * 
	 * @param eatenFood
	 *            food to consume by the caller agent from this cell; <i>must be
	 *            non-negative and below the current
	 *            <code>foodAvailability</code></i>
	 */
	public void foodConsumed(final double eatenFood) {
		if (eatenFood < 0.0) {
			throw new IllegalArgumentException(String.format(
					"eatenFood == %f < 0.0", eatenFood));
		}

		if (eatenFood > foodAvailability) {
			throw new IllegalArgumentException(String.format(
					"eatenFood == %f > foodAvailability == %f", eatenFood,
					foodAvailability));
		}

		foodAvailability -= eatenFood;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// Override default Java implementation just to have a nicer
		// representation
		return String.format(
				"HabitatCell @ location (%d, %d), foodAvailability=%f", x, y,
				foodAvailability);
	}

}
