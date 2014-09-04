/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/common/SMUtils.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.query.space.grid.GridCell;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import stupidmodel.agents.Bug;

/**
 * Dedicated class for declaring utility functions that is uninstantiable.
 * 
 * <p>
 * Final class, cannot be extended.
 * </p>
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @version $Id: SMUtils.java 174 2011-05-29 06:31:57Z richard.legendi@gmail.com
 *          $
 */
public final strictfp class SMUtils {

	/**
	 * Returns a filtered list of {@link GridCell} instances from the specified
	 * collection which holds no objects.
	 * 
	 * @param neighborhood
	 *            an arbitrary list of <code>GridCell</code> objects; <i>cannot
	 *            be <code>null</code></i>
	 * @return a filtered list of empty <code>GridCell</code> objects, more
	 *         specifically for which <code>size()</code> returns <code>0</code>
	 *         ; if there is no empty cell in the specified list, an empty list
	 *         is returned
	 */
	public static <T> List<GridCell<T>> getFreeGridCells(
			final List<GridCell<T>> neighborhood) {
		if (null == neighborhood) {
			throw new IllegalArgumentException(
					"Parameter neighborhood cannot be null.");
		}

		final ArrayList<GridCell<T>> ret = new ArrayList<GridCell<T>>();

		for (final GridCell<T> act : neighborhood) {
			if (0 == act.size()) {
				ret.add(act);
			}
		}

		return ret;
	}

	/**
	 * Returns a random element of the specified arbitrary list.
	 * 
	 * @param list
	 *            the list to select a random element from; <i>cannot be
	 *            <code>null</code> or empty</i>
	 * @return a random element of the specified list using the default random
	 *         generator of {@link RandomHelper}
	 */
	public static <T> T randomElementOf(final List<T> list) {
		if (null == list) {
			throw new IllegalArgumentException("Parameter list cannot be null.");
		}

		if (list.isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot return a random element from an empty list.");
		}

		return list.get(RandomHelper.nextIntFromTo(0, list.size() - 1));
	}

	/**
	 * Returns <code>true</code> or <code>false</code> with the specified
	 * probability.
	 * 
	 * @param threshold
	 *            the actual threshold level to use
	 * @return Returns <code>true</code> if a random number chosen from the
	 *         <code>[0,1)</code> interval is smaller than the parameter;
	 *         <code>false</code> otherwise
	 * @since Model 12
	 */
	public static boolean prob(final double threshold) {
		if (threshold < 0.0 || 1.0 < threshold) {
			throw new IllegalArgumentException(String.format(
					"Parameter threshold=%f should be in interval [0, 1].",
					threshold));
		}

		return (threshold < RandomHelper.nextDouble());
	}

	/**
	 * A utility function to read a specified data file that describes the real
	 * state of agents to create at the initialization of the simulation.
	 * 
	 * <p>
	 * The method uses the standard Java way to read and parse the input data.
	 * </p>
	 * 
	 * @param cellDataFileName
	 *            the name of the file to open; <i>by default, it is searched in
	 *            the root of the project directory, cannot be <code>null</code>
	 *            or empty</i>
	 * @return a properly initialized list containing all of the cell data in
	 *         the specified file in a structured way;
	 *         <code>cannot be modified</code>
	 */
	public static List<CellData> readDataFile(final String cellDataFileName) {
		if (null == cellDataFileName) {
			throw new IllegalArgumentException(
					"Parameter cellDataFileName cannot be null.");
		}

		if (cellDataFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"A file name cannot have an empty name.");
		}

		final ArrayList<CellData> ret = new ArrayList<CellData>();

		try {
			final BufferedReader br = new BufferedReader(new FileReader(
					cellDataFileName));

			try {
				// Skip header
				for (int i = 0; i < Constants.CELL_DATA_FILE_HEADER_LINES; ++i) {
					br.readLine();
				}

				// Read lines, parse data and add a new CellData for each one

				String line = null;

				while ((line = br.readLine()) != null) {
					// Split the line around whitespaces
					final String[] data = line.split("\\s+");

					// Check if current line seems all right
					if (data.length != 3) {
						throw new IllegalArgumentException(String.format(
								"File %s contains a malformed input line: %s",
								cellDataFileName, line));
					}

					try {
						int idx = 0;
						final int x = Integer.parseInt(data[idx++]);
						final int y = Integer.parseInt(data[idx++]);
						final double foodProductionRate = Double
								.parseDouble(data[idx++]);

						ret.add(new CellData(x, y, foodProductionRate));
					} catch (final NumberFormatException e) {
						throw new IllegalArgumentException(String.format(
								"File %s contains a malformed input line: %s",
								cellDataFileName, line), e);
					}
				}
			} finally {
				br.close();
			}
		} catch (final FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}

		return Collections.unmodifiableList(ret);
	}

	/**
	 * Returns a reference to the grid on which the specified agent is located
	 * at.
	 * 
	 * <p>
	 * It was a {@link Bug} level agent function, but was generalized because in
	 * <i>Model 16</i> we have multiple agent instances requiring this
	 * functionality.
	 * </p>
	 * 
	 * @param o
	 *            an object to find in the contexts and return its grid
	 *            projection; <i>cannot be <code>null</code></i>
	 * @return the <code>Grid</code> on which the agent is located; <i>cannot be
	 *         <code>null</code></i>
	 * @since Model 16
	 */
	public static Grid<Object> getGrid(final Object o) {
		@SuppressWarnings("unchecked")
		final Grid<Object> grid = (Grid<Object>) ContextUtils.getContext(o)
				.getProjection(Constants.GRID_ID);

		if (null == grid) {
			throw new IllegalStateException("Cannot locate grid in context.");
		}

		return grid;
	}

	// ========================================================================

	/**
	 * Hidden constructor to ensure no instances are created.
	 */
	private SMUtils() {
		;
	}

}
