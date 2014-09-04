/*
 * CalendarMgmt.java
 * 
 * Package Version: 4.0
 *   Class Version: 1.0
 *  
 * 		   Project: MEDLAND Project
 * 					Arizona State University
 * 
 * 			Author: Gary R. Mayer
 *   		  Date: 13 February 2008
 * 
 * This file contains the CalendarMgmt class which helps the Interaction
 * Model manage the passage of time within the simulated domain. Note that it
 * has no innate sense of timing or control over model within the simulation.
 * Rather, it stores data (such as current year) that is both updated and read
 * by the IM.
 * 
 * The class is implemented as a thread-safe singleton to ensure that only one
 * calendar management object is created during the simulation.
 *  
 * Fixes / Additions:
 * 1.0:
 * 	- Initial version.
 * 
 * Bugs / Issues:
 * 1.0:
 *  - None.
 */

package stupidmodel.common;

public class CalendarMgmt
{
	private static CalendarMgmt instance = null;		// the calendar object
	private static Object padlock = new Object();		// internal lock for thread-safe control
	private final int start_year;						// simulation start
	private final int increment;						// number of years to increment each cycle
	private final int sig_digits;						// significant digits to display
	private final String prefix;						// year prefix
	private final String suffix;						// year suffix
	private int curr_year;								// current year
	private int end_year;								// year the simulation is over
	
	public static String COUNT_UP = "up";				// increment increases the date
	public static String COUNT_DOWN = "down";			// increment decreases the date
	
	/**
	 * GetInstance() - returns an instance of the CalendarMgmt object
	 * @return - a Calendarmgmt object or null if none exists
	 */
	public static CalendarMgmt GetInstance ()
	{
		synchronized (padlock)
		{
			return instance;
		}
		
	}
	
	/**
	 * GetInstance() - allows the user to set the parameters for the CalendarMgmt object and returns the new object
	 * Note: constructor thread access is synchronized against an internal lock
	 * @param start_yr (int) - the starting year for the simulation
	 * @param incr (int) - the number of years to increment for each call to Increment()
	 * @param digits (int) - significant digits to display when outputing the year
	 * @param dir (String) - up or down, dictates whether Increment() adds or subtracts from the current year
	 * @param pfx (String) - prefix to add before the year value
	 * @param sfx (String) - suffix to add after the year value
	 * @return - a CalendarMgmt object. Note, the input parameters are ignored if the object already exists and the current object is returned.
	 */
	public static CalendarMgmt GetInstance ( int start_yr, int incr, int digits, String dir, String pfx, String sfx, int end_yr )
	{
		synchronized (padlock)
		{
			if ( instance == null )
			{
				// create new default instance
				instance = new CalendarMgmt(start_yr, incr, digits, dir, pfx, sfx,  end_yr);
			}
			
			return instance;
		}
	}
	
	private CalendarMgmt ( int start_yr, int incr, int digits, String dir, String pfx, String sfx, int end_yr )
	{
		this.start_year = start_yr;
		this.end_year = end_yr;
		
		// set significant digits to enetered value or size of start year;
		//	whichever is greater.
		if ( (Integer.toString(start_year)).length() > digits )
			this.sig_digits = (Integer.toString(start_year)).length();
		
		else
			this.sig_digits = digits;
		
		// set year increment
		if ( dir == CalendarMgmt.COUNT_DOWN )
			this.increment = -incr;
		
		else
			this.increment = incr;
		
		this.prefix = pfx;
		this.suffix = sfx;
		
		this.curr_year = this.start_year;
		
	}
	
	/**
	 * Year() - returns the current year with prefix and suffix strings
	 * Note: method thread access is synchronized against the calendar object
	 * @return - String (current year)
	 */
	public synchronized String Year ()
	{
		String year = null;
		String yr = Integer.toString(this.curr_year);
		String pad = "";
		
		int num_lead_0s = this.sig_digits - yr.length();
		
		while ( num_lead_0s > 0 )
		{
			pad = pad + "0";
			num_lead_0s = num_lead_0s - 1;
		}
		
		year = this.prefix + pad + yr + this.suffix;
		
		return year;
		
	}
	
	/**
	 * NextYear() - returns the year as current year + increment with prefix and suffix strings
	 * Note: method thread access is synchronized against the calendar object
	 * @return - String (current year + increment)
	 */
	public synchronized String NextYear ()
	{
		String year = null;
		String yr = Integer.toString(this.curr_year + this.increment);
		String pad = "";
		
		int num_lead_0s = this.sig_digits - yr.length();
		
		while ( num_lead_0s > 0 )
		{
			pad = pad + "0";
			num_lead_0s = num_lead_0s - 1;
		}
		
		year = this.prefix + pad + yr + this.suffix;
		
		return year;
		
	}
	
	/**
	 * PrevYear() - returns the year as current year - increment with prefix and suffix strings
	 * Note: method thread access is synchronized against the calendar object
	 * @return - String (current year - increment)
	 */
	public synchronized String PrevYear ()
	{
		String year = null;
		String yr = Integer.toString(this.curr_year - this.increment);
		String pad = "";
		
		int num_lead_0s = this.sig_digits - yr.length();
		
		while ( num_lead_0s > 0 )
		{
			pad = pad + "0";
			num_lead_0s = num_lead_0s - 1;
		}
		
		year = this.prefix + pad + yr + this.suffix;
		
		return year;
		
	}
	
	/**
	 * IntYear() - returns only the current year value (no prefix or suffix)
	 * Note: method thread access is synchronized against the calendar object
	 * @return - int (current year)
	 */
	public synchronized int IntYear ()
	{
		return this.curr_year;
		
	}
	
	public synchronized int EndYear ()
	{
		return this.end_year;
		
	}
	
	/**
	 * Increment() - increments the current year based upon the specified increment value
	 * Note: method thread access is synchronized against the calendar object
	 * @return - String (revised current year with prefix and suffix)
	 */
	public synchronized String Increment ()
	{
		if (this.end_year > this.curr_year){
		this.curr_year += this.increment;
		}
		return this.Year();
		
	}
	
}
