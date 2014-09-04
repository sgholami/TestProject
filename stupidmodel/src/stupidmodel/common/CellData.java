/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/common/CellData.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel.common;

import java.io.Serializable;
import java.util.Comparator;

import stupidmodel.agents.HabitatCell;

/**
 * A wrapper class that encapsulates parsed {@link HabitatCell} data read from
 * the input file.
 * 
 * <p>
 * Instead of assuming the space size and assuming cell food production is
 * random, food production rates are read in from a specified file, which also
 * determines the space size. Current cell objects created after parsing the
 * file with the use of this wrapper class that encapsulates the data.
 * </p>
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @since Model 15
 * @version $Id: CellData.java 428 2011-06-18 14:19:31Z
 *          richard.legendi@gmail.com $
 */
public class CellData {

	/** Specified coordinates of the cell at the used grid. */
	private final int x, y;

	/** Specified food production rate of the cell. */
	private final double foodProductionRate;

	/**
	 * Shared comparator for {@link CellData} comparison based on their
	 * <code>x</code> coordinate.
	 */
	public static final CellDataXComparator CELL_DATA_X_COMPARATOR = new CellDataXComparator();

	/**
	 * Shared comparator for {@link CellData} comparison based on their
	 * <code>y</code> coordinate.
	 */
	public static final CellDataYComparator CELL_DATA_Y_COMPARATOR = new CellDataYComparator();

	/**
	 * Simple comparator for <code>x</code> coordinates of a {@link CellData}
	 * instance.
	 */
	private static final class CellDataXComparator implements
			Comparator<CellData>, Serializable {
		/**
		 * Use <code>serialVersionUID</code> from JDK 1.0.2 for
		 * interoperability.
		 */
		private static final long serialVersionUID = 4149351630089726905L;

		/**
		 * Returns the difference between the <code>x</code> coordintanes.
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final CellData o1, final CellData o2) {
			return o1.getX() - o2.getX();
		}
	}

	/**
	 * Simple comparator for <code>y</code> coordinates of a {@link CellData}
	 * instance.
	 */
	private static final class CellDataYComparator implements
			Comparator<CellData>, Serializable {
		/**
		 * Use <code>serialVersionUID</code> from JDK 1.0.2 for
		 * interoperability.
		 */
		private static final long serialVersionUID = -5955739679291874417L;

		/**
		 * Returns the difference between the <code>x</code> coordintanes.
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final CellData o1, final CellData o2) {
			return o1.getY() - o2.getY();
		}
	}

	/**
	 * Create a new instance of {@link CellData} with the specified arguments.
	 * 
	 * <p>
	 * Arguments parsed from the input data file may be passed directly to this
	 * wrapper class to handle them in a structured way.
	 * </p>
	 * 
	 * @param x
	 *            the specified <code>x</code> coordinate; <i>must be
	 *            non-negative</i>
	 * @param y
	 *            the specified <code>y</code> coordinate; <i>must be
	 *            non-negative</i>
	 * @param foodProductionRate
	 *            the specified food production rate value; <i>must be
	 *            non-negative</i>
	 */
	public CellData(final int x, final int y, final double foodProductionRate) {
		super();

		if (x < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter x == %d < 0", x));
		}

		if (y < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter y == %d < 0", y));
		}

		if (foodProductionRate < 0) {
			throw new IllegalArgumentException(String.format(
					"Parameter foodProductionRate == %f < 0",
					foodProductionRate));
		}

		this.x = x;
		this.y = y;
		this.foodProductionRate = foodProductionRate;
	}

	/**
	 * Returns the proper coordinate component of the stored cell data.
	 * 
	 * @return the <code>x</code> coordinate of the stored cell data;
	 *         <i>non-negative</i>
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the proper coordinate component of the stored cell data.
	 * 
	 * @return the <code>y</code> coordinate of the stored cell data;
	 *         <i>non-negative</i>
	 */
	public int getY() {
		return y;
	}

	/**
	 * Returns the food production rate of the stored cell data.
	 * 
	 * @return the real food production rate of the stored cell data;
	 *         <i>non-negative</i>
	 */
	public double getFoodProductionRate() {
		return foodProductionRate;
	}

	/**
	 * Standard implementation for the <code>equals()</code> function.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof CellData) {
			final CellData other = (CellData) obj;
			return (x == other.x && y == other.y && Double
					.doubleToLongBits(foodProductionRate) == Double
					.doubleToLongBits(other.foodProductionRate));
		}

		return false;
	}

	/**
	 * Standard implementation for the <code>hashCode()</code> function.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		final long temp = Double.doubleToLongBits(foodProductionRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + x;
		result = prime * result + y;
		return result;
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
		return String.format("CellData [x=%d, y=%d, foodProductionRate=%f", x,
				y, foodProductionRate);
	}

}
