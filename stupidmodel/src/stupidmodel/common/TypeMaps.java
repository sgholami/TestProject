/*
 * TypeMaps.java
 *
 * Package Version: 3.0
 *   Class Version: 1.0
 *   
 * 		   Project: MEDLAND Project
 * 					Arizona State University
 * 
 * 			Author: Gary R. Mayer
 *   		  Date: 28 June 2007
 * 
 * This class provides an enumeration for all GRASS map names that the interaction
 * model (IM) uses in the simulation. 
 * 
 * NOTE: This implementation is NOT remote method invocation (RMI) safe
 * 	nor will it work if different objects are created using multiple jar files.
 * 
 * Class Fixes / Additions:
 * 1.0:
 *  - None.
 * 
 * Bugs / Issues:
 * 1.0:
 *  - None.
 */

package stupidmodel.common;

public class TypeMaps
{
	/////////////////////////////////
	//          MAP TYPES          //
	/////////////////////////////////
	public static final TypeMaps BEDROCK = new TypeMaps("bedrock", false);
	public static final TypeMaps CLEARING = new TypeMaps("clearing", true);
	public static final TypeMaps COSTSFC = new TypeMaps("cost_surface", false);
	public static TypeMaps ELEVATION = new TypeMaps("elev", true); //S* 
	public static TypeMaps FRICTION = new TypeMaps("friction", false);
	public static TypeMaps FERTILITY = new TypeMaps("fertility", true); // S*
	public static final TypeMaps HHIMPACTS = new TypeMaps("hhimpacts", true); // S*
	public static final TypeMaps IMPACTS = new TypeMaps("impacts", true);
	public static final TypeMaps ACTIONS = new TypeMaps("actions", true);
	public static TypeMaps LANDCOVER = new TypeMaps("landcover", true);
	public static final TypeMaps LCVR_CFACTOR = new TypeMaps("landcover.Cfactor", true);
	public static final TypeMaps SITE = new TypeMaps("site", true);	
	public static final TypeMaps SLOPE = new TypeMaps("slope", true);	// S*
	public static final TypeMaps SOILDEPTH = new TypeMaps("soil_depth", true);
	public static TypeMaps SOILS_KFACTOR = new TypeMaps("soil.Kfactor", false);
	public static final TypeMaps VILLAGES = new TypeMaps("villages", true);
	public static final TypeMaps NETCHANGE = new TypeMaps("ED_rate", true);
	
	private final String map_name;			// the core name of the map
	private final boolean dynamic;			// indicates that the map changes during the simulation
	
	public TypeMaps ( String map_name, boolean dynamic )
	{
		this.map_name = map_name;
		this.dynamic = dynamic;
		
	}
	
	public String toString ()
	{
		return map_name;
		
	}
	
	public boolean IsDynamic ()
	{
		return dynamic;
		
	}
	
}
