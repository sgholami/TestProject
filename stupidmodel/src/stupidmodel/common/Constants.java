/*
 * Version info:
 *     $HeadURL: https://cscs-repast-demos.googlecode.com/svn/richard/StupidModel/tags/2011_06_18_model_16/src/stupidmodel/common/Constants.java $
 *     $LastChangedDate: 2011-08-19 12:34:40 +0200 (P, 19 aug. 2011) $
 *     $LastChangedRevision: 1006 $
 *     $LastChangedBy: richard.legendi@gmail.com $
 */
package stupidmodel.common;

import stupidmodel.StupidModelContextBuilder;
import stupidmodel.agents.Bug;
import stupidmodel.agents.Predator;

/**
 * Dedicated class to store some model-specific constant values (e.g. bug vision
 * ranges and context IDs) that is uninstantiable.
 * 
 * <p>
 * The different IDs are usually set in
 * {@link StupidModelContextBuilder#build(repast.simphony.context.Context)} and
 * used to access the shared structures (<code>Grid</code>, <code>Space</code>
 * or <code>Context</code>) easily.
 * </p>
 * 
 * <p>
 * Final class, cannot be extended.
 * </p>
 * 
 * @author Richard O. Legendi (richard.legendi)
 * @since 2.0-beta, 2011
 * @version $Id: Constants.java 149 2011-05-26 19:03:50Z
 *          richard.legendi@gmail.com $
 */
public final class Constants {

	/**
	 * Dedicated constant for the operating system dependent <i>End of Line</i>
	 * character sequence.
	 */
	public static final String EOL = System.getProperty("line.separator");

	/** The root <code>Context</code> ID. */
	public static final String CONTEXT_ID = "StupidModel";

	/** The <code>Space</code> ID. */
	public static final String SPACE_ID = "space";

	/** The <code>Grid</code> ID. */
	public static final String GRID_ID = "grid";

	/**
	 * Unique ID for the parameter representing the initial number of bugs. It
	 * is set to <code>100</code> by default on the graphical interface.
	 * 
	 * @since Model 5
	 * @see "<code>parameters.xml</code> in the <code>StupidModel.rs</code>
	 *      directory for details"
	 */
	public static final String PARAMETER_ID_BUG_COUNT = "bugCount";

	/**
	 * Unique ID for the parameter representing <code>initialBugSizeMean</code>
	 * specified in the model formulation.
	 * 
	 * @since Model 14
	 */
	public static final String PARAMETER_INITIAL_BUG_SIZE_MEAN = "initialBugSizeMean";

	/**
	 * Unique ID for the parameter representing <code>initialBugSizeSD</code>
	 * specified in the model formulation.
	 * 
	 * @since Model 14
	 */
	public static final String PARAMETER_INITIAL_BUG_SIZE_SD = "initialBugSizeSD";

	/**
	 * The first model specified an agent vision range of <code>4 * 4</code>
	 * cells.
	 */
	public static final int BUG_VISION_RANGE = 4;

	/**
	 * The specified number of children a spawning {@link Bug} agent should
	 * create during reproduction.
	 * 
	 * @since Model 12
	 */
	public static final int BUG_REPRODUCTION_RATE = 5;

	/**
	 * Default vision range of a bug to find empty cells around when spawning
	 * descendants.
	 * 
	 * @since Model 12
	 */
	public static final int BUG_REPRODUCTION_RANGE = 3;

	/**
	 * Unique ID for the food availability layer.
	 * 
	 * @since Model 3
	 */
	public static final String FOOD_VALUE_LAYER_ID = "foodValueLayer";

	/** Constant used to specify the dimensions of a cell in the displayed GUI. */
	public static final float GUI_CELL_SIZE = 15.0f;

	/**
	 * Delta value used for double comparisons: double values <code>d1</code>
	 * and <code>d2</code> declared identical if {@literal |d1 - d2| < DELTA}
	 */
	public static final double DELTA = 1e-6;

	/**
	 * In <i>Model 12</i>, the default stopping action is changed to terminate
	 * simulation at the specified tick.
	 * 
	 * @since Model 12
	 */
	public static final double DEFAULT_END_AT = 1000.0;

	/**
	 * In <i>Model 15</i> and above the cell data is not random anymore: a
	 * specified file with a given format is used to construct cell agents,
	 * defined as follows.
	 * 
	 * @since Model 15
	 */
	public static final String STUPID_CELL_DATA_FILE = "Stupid_Cell.data";

	/**
	 * The file specifying how to construct cell agents has exactly
	 * <code>3</code> lines of header messages that can be dropped when parsing
	 * it.
	 * 
	 * @since Model 15
	 */
	public static final int CELL_DATA_FILE_HEADER_LINES = 3;

	/**
	 * <i>Model 16</i> specified to create <code>200</code> {@link Predator}
	 * agents.
	 * 
	 * @since Model 16
	 */
	public static final int PREDATOR_COUNT = 200;

	/**
	 * Specifies the default sight vision range for predators where they search
	 * for {@link Bug} agents to eat.
	 * 
	 * @since Model 16
	 */
	public static final int PREDATOR_VISION_RANGE = 1;

	/**
	 * Maximum size of a bug when it should reproduce itself.
	 */
	public static final double MAX_BUG_SIZE = 10.0;
	
	
	public static final String PARAMETER_ID_VILLAGE_COUNT = "villageCount";
	public static final String PARAMETER_ID_HOUSEHOLD_COUNT = "householdCount";
	public static final String PARAMETER_ID_POPULATION_PER_HH = "householdPop";
	public static final String PARAMETER_ID_SETTINGS_FILE = "settingsFile";

	// ========================================================================

	/**
	 * Hidden constructor to ensure no instances are created.
	 */
	private Constants() {
		;
	}

}
