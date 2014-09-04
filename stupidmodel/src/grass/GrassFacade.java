/*
 * GRASS.java
 * 
 * Package Version: 1.0
 *   Class Version: 1.1
 *  
 * 		   Project: MEDLAND Project
 * 					Arizona State University
 * 
 * 			Author: Gary R. Mayer & Sean Bergin
 *   		  Date: 15 June 2009
 *   	Last Updated: July 2010
 * 
 * Fixes / Additions:
 * 1.0:
 * 	- Initial version.
 * 
 * Bugs / Issues:
 * 	(Severity:	1 - LOW: minimal impact to correct execution and/or domain representation, to
 * 				5 - HIGH: causes program crash and/or significant domain representation problems)
 * 1.0:
 * 	- none.
 */

package grass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import misc.StringPair;
import fileMgmt.*;

/**
 * This class is a mapping of (a portion of) the GRASS software development environment to Java.
 * It provides methods to implement GRASS modules and MedLand-specific core scripts. Thus, the
 * provided methods only provides enough functionality to execute the current models. Additional
 * functionality may require expansion of the methods. For example, the g.region mapping method only allows
 * for raster map input while the g.region module in GRASS can take raster or vector maps. However,
 * there is a method to execute an external script using a command shell (e.g., Bash).
 * 
 * This class uses the singleton pattern to ensure that only one GRASS object can exist at a time.
 * This assures that all of the components within the program are using a GRASS object with the
 * same attributes.
 * 
 * The initial design and implementation was executed by Gary Mayer. Subsequent additions and the inclusion of python methods was conducted by Sean Bergin
 * 
 * @version Package: 5.0, Class: 1.0
 * @author Gary R. Mayer, ASU Dept of Computer Science and Engineering
 * @author Sean Bergin, ASU School of Human Evolution and Social Change
 */
public class GrassFacade {
	private static GrassFacade instance = null;			// GRASS object with specific attributes
	private static Object padlock = new Object();	// Internal lock for thread-safe control
	private Object owner_key = null;				// A key representing the owner/creator
													//	of the GRASS instance.
	
	public enum ColorSource { COLOR_TYPE, RASTER_MAP, RULES_FILE };
	private enum OSFlavor { UNIX, WIN, MAC, INVALID }; 
	
	// Environment constants
	private String GISDBASE = null;			// Data files path
	private String LOCATION = null;			// Location name
	private String MAPSET = null;			// Current mapset
	private String GISBASE = null;			// Source files path
	private String GISRC = null;			// Environment variable settings file.
	private String EXECUTABLES = null;		// GRASS modules path.
	private String SCRIPTS = null;			// Scripts path.
	private String PYTHON = null;			// Python executable directory.
	
	// *nux specifics
	public String GROUP = null;				// User group to specify as owner of created files
	
	// Other
	public boolean show_errors = true;		// Display error stream data from GRASS module execution
	// Note: In current versions of GRASS, the error stream contains information, not errors, from the
	// execution of the GRASS modules (e.g., percentage complete). Setting this flag to false may reduce some
	// extraneous output.
	
	// GRASS Class (static) Methods
	////////////////////////////////
	/**
	 * Gets the current instance of the GRASS object. Note that the returned value may be 'null'
	 * if it has not been initialized. 
	 */
	public static GrassFacade getInstance () {
		synchronized (GrassFacade.padlock) {
			return GrassFacade.instance;
		}
		
	}	
	
	/**
	 * Creates an instance of a GRASS object, initialized to the specified parameters, if one
	 * does not already exist. If one does exist, it ignores the parameters and returns a copy
	 * of the existing GRASS object which was previously initialized. This method will attempt
	 * to create the mapset if it does not already exist. If it does exist, it will set the mapset
	 * to be the current mapset.
	 * @param owner_key an object key used to identify the owner of the GRASS instance which helps
	 * control access to specific class methods (such as Destroy())
	 * @param gisbase_path the GRASS excutable path (a directory)
	 * @param grassrc_path the GRASS environment settings file path (the file path and name)
	 * @param gisdbase_path the GRASS data path (a directory)
	 * @param location_name the location name
	 * @param mapset_name the mapset name
	 * @return an instance of the singleton GRASS object.
	 * @throws IllegalArgumentException if the GRASS object parameters are 'null' or an empty
	 * String ("").
	 * @throws IOException if there is an error creating the mapset.
	 */
	
	public static GrassFacade getInstance ( Object owner_key, String gisbase_path, String gisrc_path,
			String gisdbase_path, String location_name, String mapset_name )
	throws IllegalArgumentException, IOException {
		synchronized (GrassFacade.padlock) {
			try {
				if ( GrassFacade.instance == null ) {
					// create new default instance
					GrassFacade.instance = new GrassFacade(owner_key, gisbase_path, gisrc_path,
							gisdbase_path, location_name, mapset_name);
					// Create a new mapset. If needed add a suffix based on the date.
					try {
						//GrassFacade.instance.createMapset();
						GrassFacade.instance.createMapset(mapset_name, true);
						GrassFacade.instance.setEnvironment();
					}
					catch (IllegalArgumentException iae) {
						System.out.println(" null coming from get instance b");
					}
				}

				return GrassFacade.instance;
			}
/**/		catch (Exception exc) {
			System.out.println(" null coming from get instance");
				System.err.println(exc.getMessage());
				return null;
			}
			finally {
				if ( GrassFacade.instance != null ) {
					GrassFacade.padlock.notifyAll();
				}
			}
		}
		
	}
	
	/**
	 * Forces the thread to wait until a valid instance of the GRASS object is created. At that
	 * time, the instance of the GRASS object is returned to the calling object. Note that a
	 * "valid instance" requires that the creating object not only create the base instance but
	 * also create a valid mapset using the CreateMapset() function of the created object.
	 * @return A non-null instance of the GRASS object.
	 */
	public static GrassFacade waitForInstance () {
		try {
			if ( GrassFacade.instance == null ) {
				GrassFacade.padlock.wait();
			}
		}
		catch (Exception exc) {
			System.err.println("An error occurred while waiting for parameters to be initialized.\n  " +
					exc.getMessage());
		}
			
		synchronized (GrassFacade.padlock) {
			return GrassFacade.instance;
		}
	}
	
	/**
	 * Forces the thread to wait a specified time for a valid instance of the GRASS object to be created.
	 * At the end of that time, the instance of the GRASS object is returned to the calling object.
	 * @return An instance of the GRASS object. The instance may be null if timeout occurs before
	 * a valid instance is created.
	 */
	public static GrassFacade waitForInstance ( long timeout ) {
		try {
			if ( GrassFacade.instance == null ) {
				GrassFacade.padlock.wait(timeout);
			}
		}
		catch (Exception exc) {
			System.err.println("An error occurred while waiting for parameters to be initialized.\n  " +
					exc.getMessage());
		}
			
		synchronized (GrassFacade.padlock) {
			return GrassFacade.instance;
		}
	}
	
	// GRASS Object Methods
	////////////////////////
	/**
	 * Destroys the GRASS object instance. Note that the caller must pass an object key
	 * to this method. The instance is only destroyed if the key is equal to the owner object
	 * key passed to the GRASS instance creation method.
	 * @param key
	 * @throws IllegalArgumentException if the key is not the owner key
	 */
	public void destroy ( Object key ) throws IllegalArgumentException {
		synchronized (GrassFacade.padlock) {
			if ( this.owner_key.equals(key) ) {
				GrassFacade.instance = null;
			}
			else {
				throw new IllegalArgumentException("Invalid key. GRASS instance not destroyed.");
			}
		}
		
	}
	
	// GRASS Environment Methods
	/////////////////////////////
	/**
	 * Creates a mapset with the previously specified name in the location specified by the GRASS object's location attribute. 
	 * @throws IllegalArgumentException if the mapset name already exists.
	 * @throws IllegalStateException if the current mapset name is 'null'.
	 * @throws IOException if there is an error while attempting to create the mapset directory and its files.
	 */
	public void createMapset () throws IllegalStateException, IOException {
		if ( this.MAPSET == null ) {
			throw new IllegalStateException("Mapset name must be specified before creation.");
		}
		
		createMapset(this.MAPSET, false);
		
	}
	
	/**
	 * Creates a mapset with the specified name in the location specified by the GRASS object's location attribute. 
	 * @param mapset_name The name to use for the new mapset.
	 * @throws IllegalArgumentException if the mapset name is 'null' or already exists.
	 * @throws IOException if there is an error while attempting to create the mapset directory and its files.
	 */
	public void createMapset ( String mapset_name ) throws IllegalArgumentException, IOException {
		if ( mapset_name == null ) {
			throw new IllegalArgumentException("Mapset name must not be 'null' for creation.");
		}
		
		createMapset(mapset_name, false);
		
	}
	
	/**
	 * Creates a mapset with the specified name in the location specified by the GRASS object's location attribute. 
	 * @param mapset_name The name to use for the mapset
	 * @param add_suffix Specifies if the method should automatically add a suffix to the mapset name if the current
	 * name already exists.
	 * @throws IllegalArgumentException if the mapset name already exists and the add_suffix parameter is false.
	 * @throws IOException if there is an error while attempting to create the mapset directory and its files.
	 */
	public void createMapset ( String mapset_name, boolean add_suffix )
	throws IllegalArgumentException, IOException {
		try {
			String location_path = this.GISDBASE + this.LOCATION + File.separator;
			//String mapset_path = location_path + mapset_name;
			String permanent = "PERMANENT";
			String permanent_path = location_path + permanent;

			if (permanent.equals(mapset_name)){
				// We assume that you want to use the permanent directory, used to check the model extents in the model initialization
				this.MAPSET = mapset_name;
			}
			else{
				if(mapset_name.length()>11){
					mapset_name = mapset_name.substring(0, 10); //added because some modules were failing with long mapset names
				}
				int run_num = 1;
				String new_mapset = "";
				String path = this.GISDBASE + File.separator + this.LOCATION  + File.separator;
				File dir = new File(path + mapset_name);
				this.MAPSET = mapset_name;
			
				if( dir.exists()){
					String run = Integer.toString(run_num);	    
					new_mapset = mapset_name + "_Run" + run;
					dir = new File(path + new_mapset);
			    
					while ( dir.exists() )
					{
						run_num++;
						run = Integer.toString(run_num);
						new_mapset = mapset_name + "_Run" + run;
						dir = new File(path + new_mapset);            
					}
			    this.MAPSET = new_mapset;
				}

			// Copy files from the PERMANENT mapset to the mapset directory.
			dir.mkdir();
			CopyFile.copy(new File(permanent_path + File.separator + "WIND"), new File(dir.getAbsolutePath() + File.separator + "WIND"));
			CopyFile.copy(new File(permanent_path + File.separator + "VAR"), new File(dir.getAbsolutePath() + File.separator + "VAR"));
			File dbf_dir = new File(dir.getAbsolutePath() + File.separator + "dbf");
			dbf_dir.mkdir();
			
			
			// Set the new directory and subfiles group ownership.
			if ( this.GROUP != null ) {
				setGroup(this.GROUP, dir, true);
			}
		  }
			
		}
		
		catch (IOException ioe) {
			throw new IOException("Mapset creation not completed.\n  " + ioe.getMessage());
		}
		catch (UnsupportedOperationException uoe) {
			// Thrown if SetGroup is used in an invalid OS. Ignore.
		}
		
		

	}
	
	/**
	 * Provides the current mapset name.
	 * @return a String containing the mapset name.
	 */
	public String getMapset () {
		return this.MAPSET;
		
	}
	
	/**
	 * Provides the full mapset path and name.
	 * @return a String containing the mapset path and name.
	 */
	public String getMapsetPath () {
		return (this.GISDBASE + this.LOCATION + File.separator + this.MAPSET);
		
	}
	
	/**
	 * Sets the GRASS executable environment by writing a new grass environment file
	 * specified by the gisrc file path when the GRASS instance was created.
	 * @throws IOException if a file write operation fails.
	 */
	public void setEnvironment () throws IOException {
		File env = new File(this.GISRC);
			
		// Delete existing file and prepare to write environment data to it.
		if ( env.exists() ) env.delete();
		FileWriter fw = new FileWriter(env);
		
		// Write GRASS environment variables to file.
		fw.write("GISDBASE: " + this.GISDBASE);
		fw.write("\nLOCATION_NAME: " + this.LOCATION);
		fw.write("\nMAPSET: " + this.MAPSET);
		fw.write("\nDIGITIZER: none");
		fw.write("\nGRASS_GUI: text\n");
		fw.flush();
		fw.close();
		
	}
	
	/**
	 * Sets the path in which to find GRASS module excutables (e.g., r.mapcalc, g.region, etc.)
	 * Note that the default path '[GISBASE]/bin' is used if it exists when the GRASS object is
	 * created.
	 * @param path The absolute directory path of GRASS module executables.
	 * @throws IllegalArgumentException if the path parameter is 'null' or not a directory.
	 */
	public void setExecutablePath ( File path ) throws IllegalArgumentException {
		if ( path == null ) {
			throw new IllegalArgumentException("Executable path may not be null.");
		}
		
		if ( !path.isDirectory() ) {
			throw new IllegalArgumentException("The file, " + path.getAbsolutePath() + ", is not a directory.");
		}
		
		this.EXECUTABLES = path.getAbsolutePath();
		if ( !this.EXECUTABLES.endsWith(File.separator) ) {
			this.EXECUTABLES = this.EXECUTABLES.concat(File.separator);
		}
		
	}
	
	/**
	 * Sets the GRASS environment to a new mapset.
	 * @param mapset_name The name of the new mapset. Note: Only use this method if the mapset already
	 * exists. If the mapset does not exist, use CreateMapset(mapset_name) instead. It will create the mapset
	 * and set the GRASS mapset attribute to this new mapset.
	 * @throws IllegalArgumentException if the mapset does not exist.
	 */
	public void setMapset (String mapset_name ) throws IllegalArgumentException {
		File mapset_dir = null;
		String location_path = this.GISDBASE + this.LOCATION + File.separator;
		if ( mapset_name.contains(File.separator) ) {
			mapset_dir = new File(mapset_name);
		}
		else {
			mapset_dir = new File(location_path + mapset_name);
		}
		
		if ( !mapset_dir.exists() ) {
			throw new IllegalArgumentException("Mapset does not exist.");
		}
		
		this.MAPSET = mapset_name;
		
	}
	
	public void setPythonPath ( File path ) throws IllegalArgumentException {
		if ( path == null ) {
			throw new IllegalArgumentException("Scripts path may not be null.");
		}
		
		if ( !path.isDirectory() ) {
			throw new IllegalArgumentException("The file, " + path.getAbsolutePath() + ", is not a directory.");
		}
		
		this.PYTHON = path.getAbsolutePath();
		if ( !this.PYTHON.endsWith(File.separator) ) {
			this.PYTHON = this.PYTHON.concat(File.separator);
		}
	}
	
	/**
	 * Sets the path in which to find GRASS scripts (e.g., r.soildepth). Note that the default
	 * path '[GISBASE]/scripts' is used if it exists when the GRASS object is created.
	 * @param path The absolute directory path of GRASS scripts.
	 * @throws IllegalArgumentException if the path parameter is 'null' or not a directory.
	 */
	public void setScriptsPath ( File path ) throws IllegalArgumentException {
		if ( path == null ) {
			throw new IllegalArgumentException("Scripts path may not be null.");
		}
		
		if ( !path.isDirectory() ) {
			throw new IllegalArgumentException("The file, " + path.getAbsolutePath() + ", is not a directory.");
		}
		
		this.SCRIPTS = path.getAbsolutePath();
		if ( !this.SCRIPTS.endsWith(File.separator) ) {
			this.SCRIPTS = this.SCRIPTS.concat(File.separator);
		}
		
	}
	
	// File Operations
	///////////////////
	/**
	 * Reads and stores a files contents as a single string. Line feed '\n' and carriage
	 * return '\r' characters are replaced by a single line feed character '\n'.
	 * @param The file whose contents are to be read and returned as a String.
	 * @return The String representation of the file contents.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if there is an error reading from the file.
	 */
	public String getFileAsString ( File file ) throws FileNotFoundException, IOException {
		String file_data = "";
		BufferedReader br = null;
		
		if ( !file.exists() ) {
			throw new FileNotFoundException("File, " + file.getAbsolutePath() + ", not found!");
		}
		
		// Read one line from the file at a time and append to a String.
		try {
			br = new BufferedReader(new FileReader(file));
			
			String fileline = br.readLine();
			while ( fileline != null ) {
				file_data = file_data.concat(fileline + "\n");
				fileline = br.readLine();
			}
		}
		
		finally {
			br.close();
		}
		
		return file_data;
		
	}
	
	/**
	 * Unix/Linux operating system function to set the executable flags for a file.
	 * @param file The file to make executable.
	 * @return A string with the operation output.
	 * @throws IOException Error buffer output resulting from the operation.
	 * @throws IllegalArgumentException if a directory is specified instead of a file.
	 * @throws UnsupportedOperationException if the method is called for an invalid
	 * operating system.
	 */
	public String makeExecutable ( File file )
	throws IOException, IllegalArgumentException, UnsupportedOperationException {
		// Only execute if Linux or Digital Unix
		if ( getOSFlavor() != GrassFacade.OSFlavor.UNIX ) {
			throw new UnsupportedOperationException("The MakeExecutable() method is not valid for the current operating system.");
		}
		
		// Check if file is a directory.
		if ( file.isDirectory() ) {
			throw new IllegalArgumentException("File is a directory. Executable flags may not be set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> (3);
		cmdList.add("/bin/chmod");
		cmdList.add("775");
		cmdList.add(file.getAbsolutePath());
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * Unix/Linux operating system function to specify a group setting for a file or directory.
	 * @param group_name The group name to append to the file (Unix/Linux)
	 * @param file
	 * @param recursive
	 * @return A String with the operation output.
	 * @throws IOException Error buffer output resulting from the operation.
	 * @throws UnsupportedOperationException if the method is called for an invalid
	 * operating system.
	 */
	public String setGroup ( String group_name, File file, boolean recursive )
	throws IOException, UnsupportedOperationException {
		// Only execute if Linux or Digital Unix
		if ( getOSFlavor() != GrassFacade.OSFlavor.UNIX ) {
			throw new UnsupportedOperationException("The MakeExecutable() method is not valid for the current operating system.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> (3);
		cmdList.add("/bin/chgrp");
		
		if ( recursive ) {
			cmdList.add("-R");
		}
		
		cmdList.add(group_name);
		cmdList.add(file.getAbsolutePath());
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}

	// GRASS Executables
	/////////////////////
	/**
	 * g.copy mapping: GRASS module. Copy a raster map. The from location can be in any known
	 * mapset location
	 * using the '@' designation (i.e., 'init_dem@PERMANENT').
	 * @param from The name of the raster map to copy from.
	 * @param to The name of the raster map to copy to.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the copy operation.
	 */
	public String gCopyRast ( String from, String to, boolean run_quiet ) 
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.copy");
		cmdList.add("rast=" + from + "," + to);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * g.gisenv mapping: GRASS module. Set the current environment mapset.
	 * @param mapset the mapset to set.
	 * @return A String with the operation output.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the mapset operation.
	 */
	public String gGisenvMapset ( String mapset ) throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.gisenv");
		cmdList.add("set=\"MAPSET=" + mapset + "\"");
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * Specifies the mapset in which the GRASS operations should occur.
	 * @param mapset The mapset name.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the mapset operation.
	 */
	public String gMapset ( String mapset ) throws IllegalStateException, IOException {
		return gMapset(mapset, null, null);
		
	}
	
	/**
	 * Specifies the mapset, location, and GISDBASE in which the GRASS operations should occur.
	 * @param mapset The mapset name.
	 * @param location The location name. Specify 'null' if the location is not to change.
	 * @param gisdbase The gisdbase name. Specify 'null' if the gisdbase is not to change.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the mapset operation.
	 */
	public String gMapset ( String mapset, String location, String gisdbase )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.mapset");
		cmdList.add("mapset=" + mapset);
		
		if ( location != null ) {
			cmdList.add("location=" + location);
		}
		
		if ( gisdbase != null ) {
			cmdList.add("gisdbase=" + gisdbase);
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * Convenience g.region mapping. Sets the boundary conditions for the geographical region.
	 * @param map_name The map to use for region settings.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the region operation.
	 */
	public String gRegion ( String map_name, String align, boolean run_quiet )
	throws IllegalStateException, IOException {
		return gRegion(map_name, align, null, -1, -1, run_quiet);
		
	}
	
	public String gRegion ( String map_name, String align, char[] flags, boolean run_quiet )
	throws IllegalStateException, IOException {
		return gRegion(map_name, align, flags, -1, -1, run_quiet);
		
	}
	
	/**
	 * g.region mapping: GRASS module. Sets the boundary conditions for the geographical region.
	 * @param map_name The map to use for region settings.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param ew_resolution Specify optional East-West resolution to use. Specify '-1' if the
	 * map's default East-West resolution is to be used.
	 * @param ns_resolution Specify optional North-South resolution to use. Specify '-1' if the
	 * map's default North-South resolution is to be used.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the region operation.
	 */
	public String gRegion ( String map_name, String align, char[] flags, double ew_resolution,  double ns_resolution, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.region");
		
		if (flags != null){
		addFlagParameter(cmdList, flags);
		}
		cmdList.add("rast=" + map_name);
		
		if (align!=null){
			cmdList.add("align=" + align);
		}
		
		if ( ew_resolution > 0 ) {
			cmdList.add("ewres=" + ew_resolution);
		}
		
		if ( ns_resolution > 0 ) {
			cmdList.add("nsres=" + ns_resolution);
		}
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * g.remove mapping: GRASS module. Delete one or more raster maps. To delete one raster
	 * map, just provide its name as the parameter. Multiple raster map names should be comma
	 * separated (no spaces).
	 * @param map_names The raster map name(s) to delete from the current mapset.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown
	 * as an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the delete operation.
	 */
	public String gRemoveRast ( String map_names, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.remove");
		cmdList.add("rast=" + map_names);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	public String gMRemoveRast ( String map_names, char[] flags, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.mremove");
		addFlagParameter(cmdList, flags);
		cmdList.add("rast=" + map_names);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * g.rename mapping. GRASS module. Renames a map file.
	 * @param original_mapname Original map name.
	 * @param new_mapname Name to which the map name will be changed.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown
	 * as an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the rename operation.
	 */
	public String gRename ( String original_mapname, String new_mapname, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "g.rename");
		cmdList.add("rast=" + original_mapname + "," + new_mapname);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.catchment mapping: Core script. Creates a raster buffer of specified area around vector points
	 * using cost distances.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param cost_map An optional input distance cost map. If specified as 'null', one is created
	 * using r.walk.
	 * @param elevation_map Input elevation map (DEM).
	 * @param vector_map Input vector sites point map.
	 * @param a Walking energy coefficient.
	 * @param b Walking energy coefficient.
	 * @param c Walking energy coefficient.
	 * @param d Walking energy coefficient.
	 * @param lambda Lambda coefficients for combining walking energy and friction cost
	 * @param slope_factor Parameter to specify travel energy cost per height step.
	 * @param output_buffer_map Output buffer map name.
	 * @param area Area of buffer (integer value to nearest 100 square map units).
	 * @param step_size Integer length of iteration step. Smaller values will create buffers
	 * closer to actual specified area, but will take significantly longer (i.e., there will be
	 * more iterations).
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the catchment operation.
	 */
	public String rCatchment ( char[] flags, String cost_map, String elevation_map, String vector_map,
			double a, double b, double c, double d, double lambda, double slope_factor,
			String output_buffer_map, int area, int step_size)
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String catchmentscript = this.SCRIPTS + "r.catchment";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		parameters.add(new StringPair("elev", elevation_map));
		parameters.add(new StringPair("vect", vector_map));
		parameters.add(new StringPair("a", Double.toString(a)));
		parameters.add(new StringPair("b", Double.toString(b)));
		parameters.add(new StringPair("c", Double.toString(c)));
		parameters.add(new StringPair("d", Double.toString(d)));
		parameters.add(new StringPair("lambda", Double.toString(lambda)));
		parameters.add(new StringPair("slope_factor", Double.toString(slope_factor)));
		parameters.add(new StringPair("buffer", output_buffer_map));
		parameters.add(new StringPair("area", Integer.toString(area)));
		parameters.add(new StringPair("step_size", Integer.toString(step_size)));

		ExternalExec.OpResult result = executeBashScript(catchmentscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.catchmentPy mapping: Core script. Creates a raster buffer of specified area around vector points
	 * using cost distances.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param cost_map An optional input distance cost map. If specified as 'null', one is created
	 * using r.walk.
	 * @param elevation_map Input elevation map (DEM).
	 * @param vector_map Input vector sites point map.
	 * @param a Walking energy coefficient.
	 * @param b Walking energy coefficient.
	 * @param c Walking energy coefficient.
	 * @param d Walking energy coefficient.
	 * @param lambda Lambda coefficients for combining walking energy and friction cost
	 * @param slope_factor Parameter to specify travel energy cost per height step.
	 * @param output_buffer_map Output buffer map name.
	 * @param area Area of buffer (integer value to nearest 100 square map units).
	 * @param step_size Integer length of iteration step. Smaller values will create buffers
	 * closer to actual specified area, but will take significantly longer (i.e., there will be
	 * more iterations).
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the catchment operation.
	 */
	public String rCatchmentPy ( char[] flags, String cost_map, String elevation_map, String vector_map,
			double a, double b, double c, double d, double lambda, double slope_factor,
			String output_buffer_map, int area, int mapval)
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String catchmentscript = this.SCRIPTS + "r.catchment.py";
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		parameters.add(new StringPair("elev", elevation_map));
		parameters.add(new StringPair("vect", vector_map));
		parameters.add(new StringPair("incost", cost_map));
		parameters.add(new StringPair("buffer", output_buffer_map));
		parameters.add(new StringPair("area", Integer.toString(area)));
		//parameters.add(new StringPair("deviation", Integer.toString(deviation)));
		parameters.add(new StringPair("mapval", Integer.toString(mapval)));

		ExternalExec.OpResult result = executePythonScript(catchmentscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.cfactor mapping: Core script. Converts a map of landcover values to a c-factor map based
	 * upon a set of reclass rules.
	 * @param input_map Input landcover map to reclassify to a c-factor map
	 * @param output_map C-factor output map name.
	 * @param reclass_rules_filepath Full filepath (directory and name) for the reclassification
	 * rules file.
	 * @param color_rules_filepath Full filepath (directory and name) for the color rules file.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the reclassification operation.
	 */
	public String rCFactor ( String input_map, String output_map,
			String reclass_rules_filepath, String color_rules_filepath )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String rcfactorscript = this.SCRIPTS + "r.cfactor";
		
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		parameters.add(new StringPair("inmap", input_map));
		parameters.add(new StringPair("outcfact", output_map));
		parameters.add(new StringPair("cfact_rules", reclass_rules_filepath));
		parameters.add(new StringPair("cfact_color", color_rules_filepath));
		
		ExternalExec.OpResult result = executeBashScript(rcfactorscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	/**
	 * r.cfactor mapping: Core script. Converts a map of landcover values to a c-factor map based
	 * upon a set of reclass rules.
	 * @param input_map Input landcover map to reclassify to a c-factor map
	 * @param output_map C-factor output map name.
	 * @param reclass_rules_filepath Full filepath (directory and name) for the reclassification
	 * rules file.
	 * @param color_rules_filepath Full filepath (directory and name) for the color rules file.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the reclassification operation.
	 */
	public String rCFactorPy ( String input_map, String output_map,
			String reclass_rules_filepath, String color_rules_filepath )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String rcfactorscript = this.SCRIPTS + "r.cfactor.py";
		
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		parameters.add(new StringPair("inmap", input_map));
		parameters.add(new StringPair("outcfact", output_map));
		parameters.add(new StringPair("cfact_rules", reclass_rules_filepath));
		parameters.add(new StringPair("cfact_color", color_rules_filepath));
		

		
		ExternalExec.OpResult result = executePythonScript(rcfactorscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	/**
	 * r.colors mapping: GRASS module. Set a raster map's colors according to a rules file.
	 * @param map The raster map whose colors should be set.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param source_type Source to use to specify colors for the map.
	 * @param source The source for color settings.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the color operation.
	 */
	public String rColors ( String map, char[] flags, ColorSource source_type, String source, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.colors");
		cmdList.add("map=" + map);
		
		addFlagParameter(cmdList, flags);
		
		String color_source = "";
		switch(source_type) {
			case COLOR_TYPE:
				color_source = "color=" + source;
				break;
			case RASTER_MAP:
				color_source = "rast=" + source;
				break;
			case RULES_FILE:
				color_source = "rules=" + source;
				break;
		}
		
		cmdList.add(color_source);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.info mapping: GRASS module. Provides information about a raster map layer.
	 * @param flags module flags.
	 * @param map_name the name of the map about which to get the information.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the color operation.
	 */
	public String rInfo ( char[] flags, String map_name ) 
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.info");
		addFlagParameter(cmdList, flags);
		cmdList.add("map=" + map_name);
		
		System.out.println("*********************** BELOW");
		System.out.println(cmdList);// *****************

		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.in.poly mapping: GRASS module. Create a raster map from ASCII polygon/line data files.
	 * @param data_file Filepath (directory and filename) containing input data.
	 * @param output_map Output map name of created raster file.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the input operation.
	 */
	public String rInPoly ( String data_file, String output_map, boolean overwrite, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.in.poly");
		cmdList.add("input=" + data_file);
		cmdList.add("output=" + output_map);
		
		if ( overwrite ) {
			cmdList.add(("--overwrite"));
		}
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.landcover.update mapping: Core script. Revises landcover values based upon an
	 * input impacts map.
	 * @param landcover_map Input map to which imapcts are applied.
	 * @param impacts_map Input map representing agent action and that modifies landcover
	 * values.
	 * @param max_landcover_value Maximum value that a landcover cell will obtain.
	 * @param output_map The map containing the revised landcover values.
	 * @param reclass_rules_filepath Full filepath (directory and name) of reclassification
	 * rules for the landcover values.
	 * @param color_rules_filepath Full filepath (directory and name) of color rules for
	 * the landcover values.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the landcover update operation.
	 */
	public String rLandCoverUpdate ( String landcover_map, String impacts_map, String soil_fert_map, String soil_depth_map, int max_landcover_value,
			String output_map, String reclass_rules_filepath, String color_rules_filepath )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String landupdtscript = this.SCRIPTS + "r.landcover.update";
		
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		parameters.add(new StringPair("inmap", landcover_map));
		parameters.add(new StringPair("impacts", impacts_map));
		parameters.add(new StringPair("sfertil", soil_fert_map));
		parameters.add(new StringPair("sdepth", soil_depth_map));
		parameters.add(new StringPair("max", Integer.toString(max_landcover_value)));
		parameters.add(new StringPair("outmap", output_map));
		parameters.add(new StringPair("lc_rules", reclass_rules_filepath));
		parameters.add(new StringPair("lc_color", color_rules_filepath));
		
		ExternalExec.OpResult result = executeBashScript(landupdtscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	/**
	 * r.landcover.update mapping: Core script. Revises landcover values based upon an
	 * input impacts map.
	 * @param landcover_map Input map to which imapcts are applied.
	 * @param impacts_map Input map representing agent action and that modifies landcover
	 * values.
	 * @param max_landcover_value Maximum value that a landcover cell will obtain.
	 * @param output_map The map containing the revised landcover values.
	 * @param reclass_rules_filepath Full filepath (directory and name) of reclassification
	 * rules for the landcover values.
	 * @param color_rules_filepath Full filepath (directory and name) of color rules for
	 * the landcover values.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the landcover update operation.
	 */
	public String rLandCoverUpdatePy ( String landcover_map, String impacts_map, String soil_fert_map, String soil_depth_map, String villages_map, int max_landcover_value,
			String output_map, String reclass_rules_filepath, String color_rules_filepath, Double wood_use, Double intensity, String cost_surface_map, String population, String catch_path)
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String landupdtscript = this.SCRIPTS + "r.landcover.update.py";
		
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		parameters.add(new StringPair("inmap", landcover_map));
		parameters.add(new StringPair("impacts", impacts_map));
		parameters.add(new StringPair("sfertil", soil_fert_map));
		parameters.add(new StringPair("sdepth", soil_depth_map));
		parameters.add(new StringPair("villages", villages_map));
		parameters.add(new StringPair("wooduse", Double.toString(wood_use)));
		parameters.add(new StringPair("intensity", Double.toString(intensity)));
		parameters.add(new StringPair("costsurf", cost_surface_map)); // can have multiple values seperated by commas
		parameters.add(new StringPair("population", population)); // needs to be a string because it can have multiple values
		parameters.add(new StringPair("max", Integer.toString(max_landcover_value)));
		parameters.add(new StringPair("outmap", output_map));
		parameters.add(new StringPair("lc_rules", reclass_rules_filepath));
		parameters.add(new StringPair("lc_color", color_rules_filepath));
		parameters.add(new StringPair("scripts_path", catch_path));

		
		ExternalExec.OpResult result = executePythonScript(landupdtscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * Convenience method for RLandscapeEvol that uses default values for kappa, cutoff
	 * values, neighborhood size, and method.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param elevation_map Input elevation (DEM) map.
	 * @param bedrock_map Input bedrock map.
	 * @param map_prefix Prefix to append to all output maps.
	 * @param output_elevation Output elevation (DEM) map name.
	 * @param output_soil Output soil depth map name.
	 * @param output_bedrock Output bedrock map name.
	 * @param stats_file Full path and filename for statistical output.
	 * @param r_factor Rainfall (R factor) constant (AVERAGE FOR WHOLE MAP AREA).
	 * @param k_factor Soil erodability index (K factor) map or constant.
	 * @param c_factor Landcover index (C factor) map or constant.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the landscape model evolution operation.
	 */
	public String rLandscapeEvol ( char[] flags, String elevation_map, String bedrock_map, String map_prefix,
			String output_elevation, String output_soil, String output_bedrock, String stats_file,
			double r_factor, String k_factor, String c_factor, boolean run_quiet )
	throws IllegalStateException, IOException {
		return rLandscapeEvol(flags, elevation_map, bedrock_map, map_prefix,
				output_elevation, output_soil, output_bedrock, stats_file,
				r_factor, k_factor, c_factor, "1", 4, 50, 7, "median", run_quiet);

	}
	
	/**
	 * r.landscape.evol mapping: Core script. Create raster maps of net erosion/depostion, the
	 * modified terrain surface (DEM) after net erosion/deposition using the USPED equation,
	 * bedrock elevations after soil production, and soil depth maps.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param elevation_map Input elevation (DEM) map.
	 * @param bedrock_map Input bedrock map.
	 * @param map_prefix Prefix to append to all output maps.
	 * @param output_elevation Output elevation (DEM) map name.
	 * @param output_soil Output soil depth map name.
	 * @param output_bedrock Output bedrock map name.
	 * @param stats_file Full path and filename for statistical output.
	 * @param r_factor Rainfall (R factor) constant (AVERAGE FOR WHOLE MAP AREA).
	 * @param k_factor Soil erodability index (K factor) map or constant.
	 * @param c_factor Landcover index (C factor) map or constant.
	 * @param kappa Hillslope diffusion (Kappa) rate map or constant (meters per kiloyear).
	 * @param cutoff1 Flow accumultion breakpoint value for shift from diffusion to overland flow
	 * (number of cells).
	 * @param cutoff2 Flow accumultion breakpoint value for shift from overland flow to channelized
	 * flow (number of cells).
	 * @param neighborhood Band-pass filter neighborhood size.
	 * @param method Neighborhood smoothing method.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the landscape model evolution operation.
	 */
	public String rLandscapeEvol ( char[] flags, String elevation_map, String bedrock_map, String map_prefix,
			String output_elevation, String output_soil, String output_bedrock, String stats_file,
			double r_factor, String k_factor, String c_factor, String kappa, int cutoff1, int cutoff2,
			int neighborhood, String method, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String landevolscript = this.SCRIPTS + "r.landscape.evol";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		// Add individual flags to parameters list
		String flagString = null;
		if ((flags != null) ) {
			for ( int c = 0; c < flags.length; c++ ) {
				flagString = "-";
				flagString = flagString + flags[c];
				//parameters.put(flagString,null );
				parameters.add(new StringPair(flagString, null));
			}
			
		}	
		/*if ( flags != null ) {
			for ( int c = 0; c < flags.length; c++ ) {
				parameters.put(String.valueOf('-' + flags[c]), null);
			}
		}*/
		
		if ( run_quiet ) {
			//parameters.put("--quiet", null);
			parameters.add(new StringPair("--quiet", null));
		}
		
		parameters.add(new StringPair("elev", elevation_map));
		parameters.add(new StringPair("initbdrk", bedrock_map));
		parameters.add(new StringPair("prefx", map_prefix));
		parameters.add(new StringPair("outdem", output_elevation));
		parameters.add(new StringPair("outsoil", output_soil));
		parameters.add(new StringPair("outbdrk", output_bedrock));
		parameters.add(new StringPair("statsout", stats_file));
		parameters.add(new StringPair("R", Double.toString(r_factor)));
		parameters.add(new StringPair("K", k_factor));
		parameters.add(new StringPair("C", c_factor));
		parameters.add(new StringPair("kappa", kappa));
		parameters.add(new StringPair("cutoff1", Integer.toString(cutoff1)));
		parameters.add(new StringPair("cutoff2", Integer.toString(cutoff2)));
		parameters.add(new StringPair("number", Integer.toString(1)));
		parameters.add(new StringPair("nbhood", Integer.toString(neighborhood)));
		parameters.add(new StringPair("method", method));
		

		/*if ( run_quiet ) {
			parameters.put("--quiet", null);
		}*/
		
		ExternalExec.OpResult result = executeBashScript(landevolscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.landscape.evol mapping: Core script. Create raster maps of net erosion/depostion, the
	 * modified terrain surface (DEM) after net erosion/deposition using the USPED equation,
	 * bedrock elevations after soil production, and soil depth maps.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param elevation_map Input elevation (DEM) map.
	 * @param bedrock_map Input bedrock map.
	 * @param map_prefix Prefix to append to all output maps.
	 * @param output_elevation Output elevation (DEM) map name.
	 * @param output_soil Output soil depth map name.
	 * @param output_bedrock Output bedrock map name.
	 * @param stats_file Full path and filename for statistical output.
	 * @param r_factor Rainfall (R factor) constant (AVERAGE FOR WHOLE MAP AREA).
	 * @param k_factor Soil erodability index (K factor) map or constant.
	 * @param c_factor Landcover index (C factor) map or constant.
	 * @param kappa Hillslope diffusion (Kappa) rate map or constant (meters per kiloyear).
	 * @param cutoff1 Flow accumultion breakpoint value for shift from diffusion to overland flow
	 * (number of cells).
	 * @param cutoff2 Flow accumultion breakpoint value for shift from overland flow to channelized
	 * flow (number of cells).
	 * @param neighborhood Band-pass filter neighborhood size.
	 * @param method Neighborhood smoothing method.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the landscape model evolution operation.
	 */
	public String rLandscapeEvolPy ( char[] flags, String elevation_map, String bedrock_map, String map_prefix,
			String output_elevation, String output_soil, double soil_density, double rain_value, int storms, double speed,
			double storm_length, String stream_transport, double load_exponent, String stats_file, double r_factor, 
			String k_factor, String c_factor, String kappa, String smoothing, double cutoff1, 
			double cutoff2, double cutoff3, boolean run_quiet )
	
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String landevolscript = this.SCRIPTS + "r.landscape.evol.py";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		// Add individual flags to parameters list
		String flagString = null;
		if ((flags != null) ) {
			for ( int c = 0; c < flags.length; c++ ) {
				flagString = "-";
				flagString = flagString + flags[c];
				//parameters.put(flagString,null );
				parameters.add(new StringPair(flagString, null));
			}	
		}	
		
		if ( run_quiet ) {
			//parameters.put("--quiet", null);
			parameters.add(new StringPair("--quiet", null));
		}
		//r.landscape.evol.py elev=y00000_elev@temporary_Run22 initbdrk=bedrock@temporary_Run22 Kt=0.0001 loadexp=1.5 
		//kappa=1 R=5.66 storms=75 stormlength=6.0 
		//speed=1.4 cutoff1=10 cutoff2=80 cutoff3=160 smoothing=high prefx=levol_ outdem=elevation outsoil=soildepth number=1
		parameters.add(new StringPair("elev", elevation_map)); //ok
		parameters.add(new StringPair("initbdrk", bedrock_map)); //ok
		parameters.add(new StringPair("prefx", map_prefix)); //ok
		parameters.add(new StringPair("outdem", output_elevation)); //ok
		parameters.add(new StringPair("outsoil", output_soil)); //ok
		parameters.add(new StringPair("sdensity", Double.toString(soil_density))); //1.25 //ok
		parameters.add(new StringPair("statsout", stats_file)); //ok
		parameters.add(new StringPair("R", Double.toString(r_factor))); //ok
		parameters.add(new StringPair("K", k_factor));//ok
		parameters.add(new StringPair("C", c_factor));//ok
		parameters.add(new StringPair("rain", Double.toString(rain_value))); //changed from meters to mm per storm event
		parameters.add(new StringPair("storms", Integer.toString(storms))); //100
		parameters.add(new StringPair("stormlength", Double.toString(storm_length))); //float
		parameters.add(new StringPair("speed", Double.toString(speed))); //100
		//parameters.add(new StringPair("infilt", Double.toString(infiltration))); //percentage or proportion
		parameters.add(new StringPair("Kt", stream_transport)); //0.01 - 0.000001
		parameters.add(new StringPair("loadexp", Double.toString(load_exponent))); //type of stream transport, 1.5 or 2.5
		parameters.add(new StringPair("kappa", kappa));
		parameters.add(new StringPair("smoothing", smoothing)); //
		parameters.add(new StringPair("cutoff1", Double.toString(cutoff1))); //need to be recalculated 900
		parameters.add(new StringPair("cutoff2", Double.toString(cutoff2))); //need to be recalculated 11250
		parameters.add(new StringPair("cutoff3", Double.toString(cutoff3))); //need to be recalculated 225000
		parameters.add(new StringPair("number", Integer.toString(1)));

		
		ExternalExec.OpResult result = executePythonScript(landevolscript, parameters);
		printErrors(result.errors);
		
		return result.output;
	}

	public String rLandAssessPy (String hhRequests, double rain_value, double max_barley_yeld, double max_wheat_yield, double wood_pc, double oc_density_factor, 
			double degrade_rate, double recovery_rate, double wood_intensity, double wooddistweight, double gdistweight, double lcovweight, double fdistweight, double sfertilweight,
			double sdepthweight, double maxfarmcost, double maxgrazecost, String maxlcov,  String slope_map, String current_landcover, String current_soil_fertility, 
			String current_soil_depth, String villageland_map, String costsurface_maps, String file_prefix, String landcover_map_out, String fertility_impacts_out,
			String impacts_out, String sf_color, String lc_rules, String lc_color, String statsfile_path, String fertilitystats_path,
			//new
			String farmbreaks, Double dropval, String prevyr, char[] flags)

	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		
		// Add individual flags to parameters list
		String flagString = null;
		if ((flags != null) ) {
			for ( int c = 0; c < flags.length; c++ ) {
				flagString = "-";
				flagString = flagString + flags[c];
				//parameters.put(flagString,null );
				parameters.add(new StringPair(flagString, null));
			}	
		}

		parameters.add(new StringPair("inputdata", hhRequests));
		parameters.add(new StringPair("precip", Double.toString(rain_value))); 
		parameters.add(new StringPair("maxbarley", Double.toString(max_barley_yeld))); 
		parameters.add(new StringPair("maxwheat", Double.toString(max_wheat_yield))); 
		parameters.add(new StringPair("wooduse", Double.toString(wood_pc))); 
		parameters.add(new StringPair("ocdensity", Double.toString(oc_density_factor))); 
		parameters.add(new StringPair("degrade_rate", Double.toString(degrade_rate))); 
		parameters.add(new StringPair("recovery", Double.toString(recovery_rate))); 
		parameters.add(new StringPair("intensity", Double.toString(wood_intensity))); 
		parameters.add(new StringPair("wooddistweight", Double.toString(wooddistweight))); 
		parameters.add(new StringPair("gdistweight", Double.toString(gdistweight))); 
		parameters.add(new StringPair("lcovweight", Double.toString(lcovweight))); 
		parameters.add(new StringPair("fdistweight", Double.toString(fdistweight))); 
		parameters.add(new StringPair("sfertilweight", Double.toString(sfertilweight))); 
		parameters.add(new StringPair("sdepthweight", Double.toString(sdepthweight))); 
		parameters.add(new StringPair("maxfarmcost", Double.toString(maxfarmcost))); 
		parameters.add(new StringPair("maxgrazecost", Double.toString(maxgrazecost))); 
		parameters.add(new StringPair("dropval", Double.toString(dropval))); 
		parameters.add(new StringPair("prevyr", prevyr)); 
		parameters.add(new StringPair("maxlcov", maxlcov)); 
		parameters.add(new StringPair("slope",slope_map )); 
		parameters.add(new StringPair("lcov", current_landcover)); 
		parameters.add(new StringPair("sfertil", current_soil_fertility)); 
		parameters.add(new StringPair("sdepth", current_soil_depth)); 
		parameters.add(new StringPair("villageland", villageland_map)); 
		parameters.add(new StringPair("costsurfs", costsurface_maps)); 
		parameters.add(new StringPair("prefix", file_prefix));
		parameters.add(new StringPair("out_lcov", landcover_map_out));
		parameters.add(new StringPair("out_fertil", fertility_impacts_out));
		parameters.add(new StringPair("out_impacts", impacts_out));
		parameters.add(new StringPair("farmbreaks", farmbreaks));
		parameters.add(new StringPair("sf_color", sf_color));
		//These are technically optional:		
		parameters.add(new StringPair("lc_rules", lc_rules));
		parameters.add(new StringPair("lc_color", lc_color));
		parameters.add(new StringPair("statsfile", statsfile_path));
		parameters.add(new StringPair("fertilstats", fertilitystats_path));		
		
		String landchooserscript = this.SCRIPTS + "r.land.assess.py";
		ExternalExec.OpResult result = executePythonScript(landchooserscript, parameters);
		printErrors(result.errors);
		
		return result.output;
	}
	
	
	
	/*
	
	public String rLandAssessPy (String hhRequests, double rain_value, double max_barley_yeld, double max_wheat_yield, double wood_pc, double oc_density_factor, 
			double degrade_rate, double recovery_rate, double wood_intensity, double wooddistweight, double gdistweight, double lcovweight, double fdistweight, double sfertilweight,
			double sdepthweight, double maxfarmcost, double maxgrazecost, String maxlcov,  String slope_map, String current_landcover, String current_soil_fertility, 
			String current_soil_depth, String villageland_map, String costsurface_maps, String file_prefix, String landcover_map_out, String fertility_impacts_out,
			String impacts_out, String sf_color, String lc_rules, String lc_color, String scripts_dir, String statsfile_path, String fertilitystats_path)

	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();

		parameters.add(new StringPair("inputdata", hhRequests));
		parameters.add(new StringPair("precip", Double.toString(rain_value))); 
		parameters.add(new StringPair("maxbarley", Double.toString(max_barley_yeld))); 
		parameters.add(new StringPair("maxwheat", Double.toString(max_wheat_yield))); 
		parameters.add(new StringPair("wooduse", Double.toString(wood_pc))); 
		parameters.add(new StringPair("ocdensity", Double.toString(oc_density_factor))); 
		parameters.add(new StringPair("degrade_rate", Double.toString(degrade_rate))); 
		parameters.add(new StringPair("recovery", Double.toString(recovery_rate))); 
		parameters.add(new StringPair("intensity", Double.toString(wood_intensity))); 
		parameters.add(new StringPair("wooddistweight", Double.toString(wooddistweight))); 
		parameters.add(new StringPair("gdistweight", Double.toString(gdistweight))); 
		parameters.add(new StringPair("lcovweight", Double.toString(lcovweight))); 
		parameters.add(new StringPair("fdistweight", Double.toString(fdistweight))); 
		parameters.add(new StringPair("sfertilweight", Double.toString(sfertilweight))); 
		parameters.add(new StringPair("sdepthweight", Double.toString(sdepthweight))); 
		parameters.add(new StringPair("maxfarmcost", Double.toString(maxfarmcost))); 
		parameters.add(new StringPair("maxgrazecost", Double.toString(maxgrazecost))); 
		parameters.add(new StringPair("maxlcov", maxlcov)); 
		parameters.add(new StringPair("slope",slope_map )); 
		parameters.add(new StringPair("lcov", current_landcover)); 
		parameters.add(new StringPair("sfertil", current_soil_fertility)); 
		parameters.add(new StringPair("sdepth", current_soil_depth)); 
		parameters.add(new StringPair("villageland", villageland_map)); 
		parameters.add(new StringPair("costsurfs", costsurface_maps)); 
		parameters.add(new StringPair("prefix", file_prefix));
		parameters.add(new StringPair("out_lcov", landcover_map_out));
		parameters.add(new StringPair("out_fertil", fertility_impacts_out));
		parameters.add(new StringPair("out_impacts", impacts_out));
		parameters.add(new StringPair("sf_color", sf_color));
		//These are technically optional:		
		parameters.add(new StringPair("lc_rules", lc_rules));
		parameters.add(new StringPair("lc_color", lc_color));
		parameters.add(new StringPair("statsfile", statsfile_path));
		parameters.add(new StringPair("fertilstats", fertilitystats_path));
		//parameters.add(new StringPair("scripts_path", scripts_dir));
		
		
		String landchooserscript = this.SCRIPTS + "r.land.assess.py";
		ExternalExec.OpResult result = executePythonScript(landchooserscript, parameters);
		printErrors(result.errors);
		
		return result.output;
	}
*/
	/**
	 * r.mapcalc mapping: GRASS module. Creates a new map using map operations.
	 * @param formula The map calculation formula to apply to create the new raster map.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the copy operation.
	 */
	public String rMapcalc ( String formula ) throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.mapcalc");
		
		// Remove special characters from the formula.
		// They cause the parameter parsing to crash.
		char[] invalidChars = new char[] { '\'', '\"' };
		for ( int c = 0; c < invalidChars.length; c++ ) {
			formula = formula.replaceAll(String.valueOf(invalidChars[c]), "");
		}
		
		cmdList.add(formula);
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.neighbors mapping: GRASS module. Creates a new map of cell values based upon a specified neighborhood
	 * size and using specified methods (average,median,mode,minimum,maximum,stddev,sum,variance,diversity,interspersion).
	 * @param inputMap The input map name
	 * @param outputMap The output map name
	 * @param method method to use to calculate the new values
	 * @param size size of the neighborhood (must be odd number)
	 * @param runQuiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the r.neighbors operation.
	 */
	public String rNeighbors ( String inputMap, String outputMap, String method, int size, boolean runQuiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.neighbors");
		
		cmdList.add("input=" + inputMap);
		cmdList.add("output=" + outputMap);
		cmdList.add("method=" + method);
		cmdList.add("size=" + String.valueOf(size));
		cmdList.add("--overwrite");
		
		if ( runQuiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.patch mapping: GRASS module. Builds a new raster map the size and resolution of the
	 * current region using two or more raster maps.
	 * @param input_maps A comma delimited string of raster maps to patch together.
	 * @param output_map The name of the raster map to create from the patched input maps.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the copy operation.
	 */
	public String rPatch ( String input_maps, String output_map, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.patch");
		
		cmdList.add("input=" + input_maps);
		cmdList.add("output=" + output_map);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.pngout.py mapping: Python script. Converts a raster map into a png file.
	 * @param in_map
	 * @param out_path_file
	 * @return a String representing the data on the output stream
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String rPngoutPy ( String in_map, String out_path_file, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String pngoutPyScript = this.SCRIPTS + "r.pngout.py";
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", in_map));
		parameters.add(new StringPair("outpath", out_path_file));

		ExternalExec.OpResult result = executePythonScript(pngoutPyScript, parameters, run_quiet);
		printErrors(result.errors);
		
		return result.output;
	}
	
	/**
	 * r.reclass mapping: GRASS module. Creates a new raster map with reclassifications of map
	 * values from an existing raster map. Uses bash scripts cat command to send reclass rules
	 * data to the GRASS r.reclass module.
	 * @param reclass_rules ASCII file containing reclassification rules.
	 * @param input_map Input raster map name from which original values are taken.
	 * @param output_map Output raster map name to contain reclassified values.
	 * @param title Optional parameter to set the title of the new raster map. Specify 'null' if
	 * no title is to be given.
	 * @param overwrite Specifies if existing raster maps with same name as output map should be
	 * overwritten.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the reclassification operation.
	 */
	public String rReclass ( String reclass_rules, String input_map, String output_map, String title, boolean overwrite )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		
		// Get external rules data to send to r.reclass method.
		String rules_input_data = getFileAsString(new File(reclass_rules));
		
		if ( rules_input_data == "" ) {
			throw new IOException("Rules file, " + reclass_rules + ", contains no data.");
		}
		
		cmdList.add(this.EXECUTABLES + "r.reclass");
		cmdList.add("input=" + input_map);
		cmdList.add("output=" + output_map);
		
		if ( title != null ) {
			cmdList.add("title=" + title);
		}
		
		if ( overwrite ) {
			cmdList.add("--overwrite");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList, rules_input_data);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.slope.aspect mapping: GRASS module. Generates raster map layers of slope,
	 * aspect, curvatures and partial derivatives from a raster map layer of true elevation
	 * values. Aspect is calculated counterclockwise from east.
	 * @param elevation_map Map containing true elvation values.
	 * @param profile_curvature_filename the file name for profile curvature data.
	 * @param tangential_curvature_filename the file name for tangential curvature data. 
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the reclassification operation.
	 */
	//"r.slope.aspect --quiet elevation="+CurrentMap(TypeMaps.ELEVATION)+ " slope="+CurrentMap(TypeMaps.SLOPE)
	public String rSlopeAspect ( String elevation_map, String slope_map, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.slope.aspect");
		cmdList.add("elevation=" + elevation_map);
		cmdList.add("slope=" + slope_map);
		//cmdList.add("tcurv=" + tangential_curvature_filename);  String tangential_curvature_filename,
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.soildepth mapping: GRASS module. Core script. Creates a soil depth map and a bedrock map based on
	 * hillslope curvature. Uses an initial elevation map to determine total landscape height (bedrock +
	 * soil depth) using a diffusion coefficient, kappa, and the age of the landscape, t, to calculate how
	 * much of the elevation is soil and how much is bedrock.
	 * @param elevation_map Input elevation map
	 * @param bedrock_map Output bedrock map name
	 * @param soildepth_map Output soil depth map name
	 * @param kappa Diffusion coefficient
	 * @param t Landscape age (years)
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the soil depth calculation operation.
	 */
	public String rSoilDepth ( String elevation_map, String bedrock_map, String soildepth_map, double kappa, int t )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String soilscript = this.SCRIPTS + "r.soildepth";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("elev", elevation_map));
		parameters.add(new StringPair("bedrock", bedrock_map));
		parameters.add(new StringPair("soildepth", soildepth_map));
		parameters.add(new StringPair("kappa", Double.toString(kappa)));
		parameters.add(new StringPair("t", Integer.toString(t)));

		
		ExternalExec.OpResult result = executeBashScript(soilscript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
		
	}
	
	/**
	 * r.soildepth mapping: GRASS module. Core script. Creates a soil depth map and a bedrock map based on
	 * hillslope curvature. Uses an initial elevation map to determine total landscape height (bedrock +
	 * soil depth) using a diffusion coefficient, kappa, and the age of the landscape, t, to calculate how
	 * much of the elevation is soil and how much is bedrock.
	 * @param elevation_map Input elevation map
	 * @param bedrock_map Output bedrock map name
	 * @param soildepth_map Output soil depth map name
	 * @param kappa Diffusion coefficient
	 * @param t Landscape age (years)
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the soil depth calculation operation.
	 */
	public String rSoilDepthPy ( String elevation_map, String bedrock_map, String soildepth_map, double min, double max )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String soilscript = this.SCRIPTS + "r.soildepth.py";
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("elev", elevation_map));
		parameters.add(new StringPair("bedrock", bedrock_map));
		parameters.add(new StringPair("soildepth", soildepth_map));
		parameters.add(new StringPair("min", Double.toString(min)));
		parameters.add(new StringPair("max", Double.toString(max)));
		
		ExternalExec.OpResult result = executePythonScript(soilscript, parameters);
		printErrors(result.errors);
		
		return result.output;
	}
	/**
	 * r.soil.fertility mapping: GRASS script. Generates soil fetility map
	 * @param in_map Current fertility map name
	 * @param impacts map to impacts to fertility
	 * @param max maximum fertility
	 * @param out_map new fertility map
	 * @param sf_color_rules soil fertility color rules file
	 * @return String with output results
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the soil fertility calculation operation.
	 */
	public String rSoilFertility ( String in_map, String impacts, int max, String out_map,
			String sf_color_rules, boolean run_quiet)
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String soilscript = this.SCRIPTS + "r.soil.fertility";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", in_map));
		parameters.add(new StringPair("impacts", impacts));
		parameters.add(new StringPair("max", Integer.toString(max)));
		parameters.add(new StringPair("outmap", out_map));
		parameters.add(new StringPair("sf_color", sf_color_rules));
		
		ExternalExec.OpResult result = executeBashScript(soilscript, parameters, run_quiet);
		printErrors(result.errors);
		
		return result.output;
	}
	
	/**
	 * r.soil.fertility mapping: GRASS script. Generates soil fetility map
	 * @param in_map Current fertility map name
	 * @param impacts map to impacts to fertility
	 * @param max maximum fertility
	 * @param out_map new fertility map
	 * @param sf_color_rules soil fertility color rules file
	 * @return String with output results
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the soil fertility calculation operation.
	 */
	public String rSoilFertilityPy ( String in_map, String impacts, int recovery, String out_map,
			String sf_color_rules, boolean run_quiet)
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String soilscript = this.SCRIPTS + "r.soil.fertility.py";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", in_map));
		parameters.add(new StringPair("impacts", impacts));
		parameters.add(new StringPair("recovery", Integer.toString(recovery)));
		parameters.add(new StringPair("outmap", out_map));
		parameters.add(new StringPair("sf_color", sf_color_rules));	

		ExternalExec.OpResult result = executePythonScript(soilscript, parameters, run_quiet);
		printErrors(result.errors);
		
		return result.output;
	}
	
	/**
	 * r.stats mapping: GRASS module. Generates area statistics for raster maps.
	 * @param flags
	 * @param input_map One or more (comma delimited) input raster map names.
	 * @param output_file Optional output file name which to save the results. If
	 * specified as 'null', the results will be output to the standard output stream
	 * and provided as the String returned from this method.
	 * @param field_seperator  Optional field seperator. Specify 'null' to use default space.
	 * @param no_value Optional no value character. Specifiy 'null' to use default '*'. 
	 * @param num_steps Optional value to specify number of subranges to collect data from.
	 * Specify '-1' to use default of 255.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the execution path is not set.
	 * @throws IOException Error buffer output resulting from the statistical operation.
	 */
	public String rStats ( char[] flags, String input_maps, String output_file,
			String field_seperator, String no_value, int num_steps )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}

		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.stats");
		addFlagParameter(cmdList, flags);
		cmdList.add("input=" + input_maps);
		
		if ( output_file != null ) {
			cmdList.add("output=" + output_file);
		}
		
		if ( field_seperator != null ) {
			cmdList.add("fs=" + field_seperator);
		}
		
		if ( no_value != null ) {
			cmdList.add("nv=" + no_value);
		}
		
		if ( num_steps > 0 ) {
			cmdList.add("nsteps=" + num_steps);
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	// uses v flag, feature=point
	public String rToVect(String inmap, char []flags, String feature, String outmap, boolean run_quiet, boolean overwrite)
	throws IllegalStateException, IOException {

		//r.to.vect -v input=inmap output=outmap feature=point --overwrite --quiet 
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.to.vect");		
		cmdList.add("input=" + inmap);
		cmdList.add("output=" + outmap);
		
		if(feature != null){
			cmdList.add("feature=" + feature);
		}
		
		addFlagParameter(cmdList, flags);
				
		if (overwrite) {
			cmdList.add("--overwrite");
		}
		
		if (run_quiet) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
	}
	
	/**
	 * r.walk mapping: GRASS module. Creates a raster map showing the lowest cumulative cost to move
	 * from a starting vector point to any other location on a raster elevation map.
	 * @param flags An optional array of character flags to append to the operation. Specify 'null'
	 * if no flags are to be set.
	 * @param elevation_map The elevation raster map to calculate the cost of movement across.
	 * @param friction_map An input map specifying the friction costs across the elevation map.
	 * @param output_map The name of the output raster map to contain the movement costs.
	 * @param start_points The name of a vector map contain a vector point from which to start the
	 * cost calculations.
	 * @param max_cost An optional maximum cost up to which the module should calculate cumulative
	 * costs. Specify '-1' for default of 0.
	 * @param percent_memory An optional parameter to specify the amount of the cost map to keep in
	 * memory. Specify '-1' for a default fo 100.
	 * @param num_segments An optional parameter to specify the number of segments to create. Specify
	 * '-1' for a default of 4.
	 * @param walk_coefficient Optional walking coefficient parameters as a comma delimited string of
	 * four real values. Specify 'null' for default of '0.72,6.0,1.9998,-1.9998'.
	 * @param lambda Lambda coefficients for combining walking energy and friction cost
	 * @param slope_factor Optional parameter to specify travel energy cost per height step.
	 * @param run_quiet Specifies if the operation should provide feedback while running.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the walk operation.
	 */
	public String rWalk ( char[] flags, String elevation_map, String friction_map, String output_map,
			String start_points, double max_cost, int percent_memory, int num_segments, String walk_coefficient,
			double lambda, double slope_factor, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.walk");
		addFlagParameter(cmdList, flags);
		cmdList.add("elevation=" + elevation_map);
		cmdList.add("friction=" + friction_map);
		cmdList.add("output=" + output_map);
		cmdList.add("start_points=" + start_points);
		
		if ( max_cost > 0 ) {
			cmdList.add("max_cost=" + max_cost);
		}
		
		if ( percent_memory > 0 ) {
			cmdList.add("percent_memory=" + percent_memory);
		}
		
		if ( num_segments >= 0 ) {
			cmdList.add("nseg=" + num_segments);
		}
		
		if ( walk_coefficient != null ) {
			cmdList.add("walk_coeff=" + walk_coefficient);
		}
		
		cmdList.add("lambda=" + lambda);
		cmdList.add("slope_factor=" + slope_factor);
		
		if ( run_quiet ) {
			cmdList.add("--quiet");
		}
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.what mapping: GRASS module. Queries raster map layers on their category values
	 * and category labels.
	 * @param input_map The input map from which to query values.
	 * @param east_north A list of comma-delimited east-north coordinates to query.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the walk operation.
	 */
	public String rWhatEN ( String input_map, ArrayList<String> east_north )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "r.what");
		cmdList.add("input=" + input_map);
		
		String coordinates = "east_north=";
		
		for ( int c = 0; c < east_north.size(); c++ ) {
			if ( c > 0 ) {
				coordinates += ",";
			}
			
			coordinates += east_north.get(c);
		}
		
		cmdList.add(coordinates);
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}
	
	/**
	 * r.villages mapping: Core script. Patches a map of villages onto a land cover map.
	 * @param input_map The landcover raster map to patch the village lcoations onto.
	 * @param village_map The raster map of village locations.
	 * @param landcover_value The value to assign to village locations on the patched
	 * output map.
	 * @param output_map The name of the output map patched from the landcover and village
	 * maps.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the village patch operation.
	 */
	public String rVillages ( String input_map, String village_map, int landcover_value, String output_map )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		String villagescript = this.SCRIPTS + "r.villages";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", input_map));
		parameters.add(new StringPair("villages", village_map));
		parameters.add(new StringPair("val", Integer.toString(landcover_value)));
		parameters.add(new StringPair("output", output_map));
		
		ExternalExec.OpResult result = executeBashScript(villagescript, parameters);
		printErrors(result.errors);
		
		return result.output;
		
	}
	/**
	 * r.villages mapping: Core script. Patches a map of villages onto a land cover map.
	 * @param input_map The landcover raster map to patch the village lcoations onto.
	 * @param village_map The raster map of village locations.
	 * @param landcover_value The value to assign to village locations on the patched
	 * output map.
	 * @param output_map The name of the output map patched from the landcover and village
	 * maps.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the scripts path is not set.
	 * @throws IOException Error buffer output resulting from the village patch operation.
	 */
	public String rVillagesPy ( String input_map, String village_map, int landcover_value, String output_map )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		/*
		 * 
		 * 
		String pngoutPyScript = this.SCRIPTS + "r.pngout.py";
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", in_map));
		parameters.add(new StringPair("outpath", out_path_file));

		ExternalExec.OpResult result = executePythonScript(pngoutPyScript, parameters, run_quiet);
		printErrors(result.errors);
		
		return result.output;
		 */
		
		String villagescript = this.SCRIPTS + "r.villages.py";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", input_map));
		parameters.add(new StringPair("villages", village_map));
		parameters.add(new StringPair("val", Integer.toString(landcover_value)));
		parameters.add(new StringPair("output", output_map));
		
		ExternalExec.OpResult result = executePythonScript(villagescript, parameters);
		printErrors(result.errors);
		
		
		return result.output;
		
	}
	/**
	 * v.in.ascii mapping: GRASS module. Creates a new vector map using a data file and GRASS
	 * v.in.ascii module.
	 * @param data_file An ASCII file containing the vector point to create a map from.
	 * @param output_map The output vector map name.
	 * @return A String with the operation output. Any output to the error stream is thrown as
	 * an exception.
	 * @throws IllegalStateException if the executable path is not set.
	 * @throws IOException Error buffer output resulting from the v.in.ascii operation.
	 */
	public String vInASCII ( String data_file, String output_map, String[] parameters, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.EXECUTABLES == null ) {
			throw new IllegalStateException("Executable path not set.");
		}
		
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "v.in.ascii");		
		cmdList.add("input=" + data_file);
		cmdList.add("output=" + output_map);
		
		// parameters required for such entries:
		// "format=point", "cat=1", "x=2", "y=3"
		if ( parameters != null) {
			for ( String parameter : parameters) {
				cmdList.add(parameter);
			}
		}
		
		if (run_quiet) {
			cmdList.add("--quiet");
		}
		//System.out.println("vinascii:" + cmdList.toString());
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
		
	}

	/**
	 * 
	 * @param in_map
	 * @param out_path_file
	 * @param run_quiet
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String vPngoutPy ( String in_map, String out_path_file, boolean run_quiet )
	throws IllegalStateException, IOException {
		if ( this.SCRIPTS == null ) {
			throw new IllegalStateException("Scripts path not set.");
		}
		
		if ( this.PYTHON == null ) {
			throw new IllegalStateException("Python executable path not set.");
		}
		
		String pngoutPyScript = this.SCRIPTS + "v.pngout.py";
		//HashMap<String, String> parameters = new HashMap<String,String> ();
		ArrayList<StringPair> parameters = new ArrayList<StringPair> ();
		parameters.add(new StringPair("inmap", in_map));
		parameters.add(new StringPair("outpath", out_path_file));
		
		ExternalExec.OpResult result = executePythonScript(pngoutPyScript, parameters, run_quiet);
		printErrors(result.errors);
		
		return result.output;
	}
	

	public String vToRast(String inmap, String outmap, String use, String type, String column, String labelcolumn,  boolean run_quiet)
	throws IllegalStateException, IOException {

		//v.to.rast input=inmap output=outmap use=cat type=point,line,area 
		ArrayList<String> cmdList = new ArrayList<String> ();
		cmdList.add(this.EXECUTABLES + "v.to.rast");		
		cmdList.add("input=" + inmap);
		
		if(use != null){
			cmdList.add("use=" + use);
		}
		if(type != null){
			cmdList.add("type=" + type);
		}
		if(column != null){
			cmdList.add("column=" + column);
		} 
		if(labelcolumn != null){
			cmdList.add("labelcolumn=" + labelcolumn);
		}
		cmdList.add("output=" + outmap);
			
		if (run_quiet) {
			cmdList.add("--quiet");
		}
		
		//System.out.println("vtorast:" + cmdList.toString());
		
		ExternalExec.OpResult result = executeCmd(cmdList);
		printErrors(result.errors);
		
		return result.output;
	}
	
	

	
	////////////////////////////////////////////////
	///              PRIVATE METHODS             ///
	////////////////////////////////////////////////
	private ExternalExec.OpResult executeCmd ( ArrayList<String> cmdList )
	throws IOException {
		return executeCmd(cmdList, null);
		
	}
	
	/**
	 * Executes the command list with associated parameters. Any scripts executed using
	 * this function require explicit pathnames for other scripts and modules used within
	 * them. For example, '/usr/local/grass-6.3.cvs/bin/g.region' vice just 'g.region'.
	 * Also note that, because current GRASS modules return informaton on the error stream,
	 * an exception cannot be thrown when data on the error stream is present. For now, it
	 * is best to capture both streams, as well as the module exit value, and then allow
	 * each calling function to specifically decide what to do with the data. 
	 * @param cmdList Command to execute along with its input parameters
	 * @param input External input to provide to the process. 
	 * @return An OpResult object that contains the data captured on both the output and
	 * error streams, and the integer exit value
	 * @throws IOException Error resulting from the command execution.
	 */
	private ExternalExec.OpResult executeCmd ( ArrayList<String> cmdList, String input )
	throws IOException {
		//Hashtable<String, String> env = null;
		ArrayList<StringPair> env = null;
		ExternalExec executor = new ExternalExec();
		ExternalExec.OpResult result = null;
		
		try {
			// Specify Java subprocess environment variables required to execute GRASS modules.
			// Note that these are different than the GRASS environment variables themselves
			// (e.g., location and mapset), which are specified by the GRASSRC variable.
			//
			// Note that the PATH variable is a command shell specific entity (e.g., Bash) and
			// is not used by the Java runtime.exec process directly. 
			env = new ArrayList<StringPair> (); //new Hashtable<String,String> ();
			

			env.add(new StringPair("GISBASE", this.GISBASE)); //env.put("GISBASE", this.GISBASE);
			env.add(new StringPair("GISRC", this.GISRC));  // env.put("GISRC", this.GISRC);
			
			String libPath = System.getenv("LD_LIBRARY_PATH");
			if (libPath == null) {
				libPath = "/usr/local/bin/" + File.pathSeparator +	this.GISBASE + "lib/";
			}
			else {
				libPath += File.pathSeparator + "/usr/local/bin/" + File.pathSeparator +	this.GISBASE + "lib/";
			}

			env.add(new StringPair("LD_LIBRARY_PATH", libPath));
			
			//env.add(new StringPair("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH") + File.pathSeparator +
			//		"/usr/local/bin" + File.pathSeparator +	this.GISBASE + "lib"));
			env.add(new StringPair("GIS_LOCK", "$$"));
			/*env.put("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH") + File.pathSeparator +
					"/usr/local/bin" + File.pathSeparator +	this.GISBASE + "lib");
			env.put("GIS_LOCK", "$$");
			*/

			
			if ( input == null )
				result = executor.execute(cmdList, env);
			else
				result = executor.execute(cmdList, env, input);
		}
		catch (IOException ioe) {
			// Specify what command was being executed and forward the received error.
			throw new IOException("An exception occurred while executing, " + cmdList.get(0) + ":\n  " +
					ioe.getMessage());
		}
		
		return result;
		
	}
	
	/**
	 * Executes a Bash script with associated parameters. The script must exist within the directory
	 * specified by a call to the setScriptsDirectory() method. Otherwise, an 'Executable Not Found'
	 * error will result. Any scripts executed using this function require explicit pathnames for
	 * other scripts and modules used within them. For example, '/usr/local/grass-6.3.cvs/bin/g.region'
	 * vice just 'g.region'. Also note that, because current GRASS modules return informaton on the
	 * error stream, an exception cannot be thrown when data on the error stream is present. For now,
	 * it is best to capture both streams, as well as the module exit value, and then allow each calling
	 * function to specifically decide what to do with the data. 
	 * @param script Name of script to execute
	 * @param parameters Script input parameters
	 * @return An OpResult object that contains the data captured on both the output and
	 * error streams, and the integer exit value
	 * @throws IOException Error resulting from the command execution.
	 */
	//private ExternalExec.OpResult executeBashScript ( String script, HashMap<String, String> parameters ) throws IOException {
	private ExternalExec.OpResult executeBashScript ( String script, ArrayList<StringPair> parameters ) throws IOException {
		return executeBashScript(script, parameters, false);
	}
	
	/**
	 * Executes a Bash script with associated parameters. The script must exist within the directory
	 * specified by a call to the setScriptsDirectory() method. Otherwise, an 'Executable Not Found'
	 * error will result. Further note that the PATH variable is a command shell specific entity (e.g.,
	 * Bash) and is not used by the Java runtime.exec process directly. Therefore, any scripts executed
	 * using this function require explicit pathnames for other scripts and modules used within them.
	 * For example, '/usr/local/grass-6.3.cvs/bin/g.region' vice just 'g.region'. Also note that,
	 * because current GRASS modules return informaton on the error stream, an exception cannot be
	 * thrown when data on the error stream is present. For now, it is best to capture both streams,
	 * as well as the module exit value, and then allow each calling function to specifically decide
	 * what to do with the data. 
	 * @param script
	 * @param parameters
	 * @param run_quiet
	 * @return
	 * @throws IOException Error resulting from the command execution.
	 */
	//private ExternalExec.OpResult executeBashScript ( String script, HashMap<String, String> parameters, boolean run_quiet ) throws IOException {
	private ExternalExec.OpResult executeBashScript ( String script, ArrayList<StringPair> parameters, boolean run_quiet ) throws IOException {
		//Hashtable<String, String> env = new Hashtable<String, String> ();
		ArrayList<StringPair> env = new ArrayList<StringPair> ();
		ExternalExec executor = new ExternalExec();
		ExternalExec.OpResult result = null;
		ArrayList<String> cmdList = new ArrayList<String> ();
		
		try {
			// Specify Java subprocess environment variables required to execute GRASS modules.
			// Note that these are different than the GRASS environment variables themselves
			// (e.g., location and mapset), which are specified by the GRASSRC variable. These
			// will be passed to the command shell which is executed by the subprocess.
			env.add(new StringPair("GISBASE", this.GISBASE));
			env.add(new StringPair("GISRC", this.GISRC));
			env.add(new StringPair("PATH", System.getenv("PATH") + File.pathSeparator + 
					this.GISBASE + "bin" + File.pathSeparator +
					this.GISBASE + "scripts"));
			env.add(new StringPair("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH") + File.pathSeparator +
					"/usr/local/bin" + File.pathSeparator +	this.GISBASE + "lib"));
			env.add(new StringPair("GIS_LOCK", "$$"));
			
			/*
			env.put("GISBASE", this.GISBASE);
			env.put("GISRC", this.GISRC);
			env.put("PATH", System.getenv("PATH") + File.pathSeparator + 
					this.GISBASE + "bin" + File.pathSeparator +
					this.GISBASE + "scripts");
			env.put("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH") + File.pathSeparator +
					"/usr/local/bin" + File.pathSeparator +	this.GISBASE + "lib");
			env.put("GIS_LOCK", "$$");
			*/
			// Choose the appropriate command shell based upon operating system.
			OSFlavor osFlavor = getOSFlavor();
			if ( (osFlavor == OSFlavor.UNIX) || (osFlavor == OSFlavor.MAC)  ) {
				cmdList.add("/bin/bash");
			}
			
			else if ( osFlavor == OSFlavor.WIN ) {
				cmdList.add("cmd.exe");
			}
			
			else return null;
			
			// Add script to execute and its associated parameters.
			cmdList.add(script);
			
			for (StringPair parameter : parameters) {
				if ( parameter.value != null ) {
					cmdList.add(parameter.key + "=" + parameter.value);
				}
				else {
					cmdList.add(parameter.key);
				}
			}
			/*
			if ( parameters != null ) {
				for ( String parameter : parameters.keySet() ) {
					String value = parameters.get(parameter);
					
					if ( value != null ) {
						cmdList.add(parameter + "=" + value);
					}
					else {
						cmdList.add(parameter);
					}
				}
			}
			*/
			if ( run_quiet) {
				cmdList.add("--quiet");
			}
			
			result = executor.execute(cmdList, env);
		}
		catch (IOException ioe) {
			throw new IOException("An exception occurred while executing " +
					script + ":\n  " + ioe.getMessage());
		}
		
		return result;
		
	}
	
	//private ExternalExec.OpResult executePythonScript ( String script, HashMap<String, String> parameters ) throws IOException {
	private ExternalExec.OpResult executePythonScript ( String script, ArrayList<StringPair> parameters ) throws IOException {
		return executePythonScript(script, parameters, false);
	}
	

	private ExternalExec.OpResult executePythonScript ( String script, ArrayList<StringPair> parameters, boolean run_quiet ) throws IOException {

		ArrayList<StringPair> env = new ArrayList<StringPair> ();
		ExternalExec executor = new ExternalExec();
		ExternalExec.OpResult result = null;
		ArrayList<String> cmdList = new ArrayList<String> ();
		
		try {
			// Specify Java subprocess environment variables required to execute GRASS modules.
			// Note that these are different than the GRASS environment variables themselves
			// (e.g., location and mapset), which are specified by the GRASSRC variable. These
			// will be passed to the command shell which is executed by the subprocess.
			env.add(new StringPair("GISBASE", this.GISBASE));
			//env.add(new StringPair("GRASS_ADDON_PATH", "/Users/sbergin/Documents/APSIModel/APSIM/Scripts"));
			env.add(new StringPair("GISRC", this.GISRC));
			env.add(new StringPair("PATH", System.getenv("PATH") + File.pathSeparator + 
					this.GISBASE + "bin" + File.pathSeparator +
					this.GISBASE + "scripts"));
			env.add(new StringPair("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH") + File.pathSeparator +
					"/usr/local/bin" + File.pathSeparator +	this.GISBASE + "lib"));
			env.add(new StringPair("GIS_LOCK", "$$"));


			// Set the python executable path and name
			cmdList.add(this.PYTHON + "python");
			
			// Add script to execute and its associated parameters.
			cmdList.add(script);
			
			for (StringPair parameter : parameters) {
				if ( parameter.value != null ) {
					cmdList.add(parameter.key + "=" + parameter.value);
				}
				else {
					cmdList.add(parameter.key);
				}
			}

			if (run_quiet) {
				cmdList.add("--quiet");
			}

			result = executor.execute(cmdList, env);
		}
		catch (IOException ioe) {
			throw new IOException("An exception occurred while executing " +
					script + ":\n  " + ioe.getMessage());
		}
		
		return result;
		
	}
	
	/**
	 * Constructor.
	 * @param owner_key
	 * @param gisdbase
	 * @param location
	 * @param gisbase
	 * @param grassrc
	 * @param envScript
	 * @param mapset
	 */
	private GrassFacade ( Object owner_key, String gisbase, String gisrc, String gisdbase,
			String location, String mapset )
	throws IllegalArgumentException, IOException {
		
		if ( (gisbase == null) || (gisrc == null) || (gisdbase == null) ||
				(location == null) || (mapset == null) ||
				(gisbase == "") || (gisrc == "") || (gisdbase == "") ||
				(location == "") || (mapset == "") ) {
			throw new IllegalArgumentException("The GRASS object parameters may not be 'null' nor an empty String (\"\").");
		}
		
		this.owner_key = owner_key;
		
		if ( !gisbase.endsWith(File.separator) ) {
			this.GISBASE = gisbase.concat(File.separator);
		}
		else {
			this.GISBASE = gisbase;
		}
		
		if ( !gisrc.endsWith(File.separator) ) {
			this.GISRC = gisrc.concat(File.separator + ".grassrc6");
		}
		else {
			this.GISRC = gisrc + ".grassrc6";
		}
		
		if ( !gisdbase.endsWith(File.separator) ) {
			this.GISDBASE = gisdbase.concat(File.separator);
		}
		else {
			this.GISDBASE = gisdbase;
		}
		
		this.LOCATION = location;
		this.MAPSET = mapset;

		
		// Set default executable and scripts paths, if valid.
		File executable_dir = new File(this.GISBASE + "bin");

		if ( executable_dir.exists() ) {
			this.EXECUTABLES = executable_dir.getAbsolutePath();
		}

		if ( !this.EXECUTABLES.endsWith(File.separator) ) {
			this.EXECUTABLES = this.EXECUTABLES.concat(File.separator);
		}

/*		
		File scripts_dir = new File(this.GISBASE + "scripts");
		if ( scripts_dir.exists() ) {
			this.SCRIPTS = scripts_dir.getAbsolutePath();
		}
		
		if ( !this.SCRIPTS.endsWith(File.separator) ) {
			this.SCRIPTS = this.SCRIPTS.concat(File.separator);
		}
*/
		setEnvironment();		
	}
	
	/**
	 * Add a string representing the flag parameters if there are any.
	 * @param cmdList The array list in which to insert the flag parameters.
	 * @param flags A character array of flags.
	 */
	private void addFlagParameter ( ArrayList<String> cmdList, char[] flags ) {
		String flagString = null;
		
		if ( (cmdList != null) && (flags != null) ) {
			flagString = "-";
			for ( int c = 0; c < flags.length; c++ ) {
				flagString = flagString + flags[c];
			}
			
			cmdList.add(flagString);
		}
		
	}
	
	/**
	 * Add a parameter for each flag (with a '-' prefix)
	 * @param cmdList The array list in which to insert the flag parameters.
	 * @param flags A character array of flags.
	 */
/*	
	private void addMultiFlagParameters ( ArrayList<String> cmdList, char[] flags ) {		
		if ( (cmdList != null) && (flags != null) ) {
			for ( int c = 0; c < flags.length; c++ ) {
				cmdList.add("-" + flags[c]);
			}
		}
		
	}
*/	
	/**
	 * Provides the type of operating system this program is executing on so that
	 * OS-specific actions can be taken.
	 * @return an OSFlavor enumerated value
	 */
	private OSFlavor getOSFlavor () {
		String OS = System.getProperty("os.name");
		if ( (OS.compareTo("Linux") == 0) || (OS.compareTo("Digital Unix") == 0) ||
				(OS.compareTo("FeeeBSD") == 0) ) {
			return GrassFacade.OSFlavor.UNIX;
		}
		
		else if ( (OS.compareTo("Windows XP") == 0) || (OS.compareTo("Windows 2000") == 0) ||
				(OS.compareTo("Windows NT") == 0) || (OS.compareTo("Windows 98") == 0) ||
				(OS.compareTo("Windows 95") == 0) ) {
			return GrassFacade.OSFlavor.WIN;
		}
		
		else if ( (OS.compareTo("Mac OS") == 0) || (OS.compareTo("Mac OS X") == 0) ) {
			return GrassFacade.OSFlavor.MAC;
		}
		
		else return GrassFacade.OSFlavor.INVALID;
		
	}
	
	/**
	 * Prints an error string to standard error stream if the show errors flag
	 * is set to true.
	 * @param errors A String containing the errors to print.
	 */
	private void printErrors ( String errors ) {
		if ( show_errors && (errors != null) )
			System.err.println(errors);
				
	}
	
}

;