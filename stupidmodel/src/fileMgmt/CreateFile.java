/*
 * CreateFile.java
 * 
 * Package Version: 5.0
 *   Class Version: 1.0
 *  
 * 		   Project: MEDLAND Project
 * 					Arizona State University
 * 
 * 			Author: Gary R. Mayer
 *   		  Date: 26 August 2008
 * 
 * Fixes / Additions:
 * 1.0:
 * 	- None.
 * 
 * Bugs / Issues:
 * 1.0:
 *  - None.
 */

package fileMgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

/**
 * This class provides a common interface for creating and adding data to files.
 * Data that is added to a file is first stored in a String buffer. The WriteFile()
 * method is then used to write the buffered data to the actual file. No further data
 * may be added once the file is written.
 * 
 * @version Package: 5.0, Class: 1.0
 * @author Gary R. Mayer, ASU Dept of Computer Science and Engineering
 */
public class CreateFile
{
	protected File file = null;						// file object
	protected String filename = null;				// name of the file
	protected StringBuffer filedata = null;			// data buffer to write to file	
	
	/**
	 * Default constructor 
	 * creates a file, called "NewFile", in the current working directory with a header. 
	 * User must call WriteFile() when complete.
	 */
	public CreateFile () {
		this("." + File.separator, "NewFile", true);
		
	}
	
	/**
	 * Constructor.
	 * Creates a file in the current working directory with the specified name and adds a header. 
	 * User must call WriteFile() when complete.
	 * @param nm the name of the file
	 */
	public CreateFile ( String nm ) {
		this("." + File.separator, nm, true);
		
	}
	
	/**
	 * Constructor.
	 * Creates a file in the current working directory with the specified name and a header, if requested. 
	 * User must call WriteFile() when complete. 
	 * @param nm the name of the file
	 * @param make_header specifies if a header is added
	 */
	public CreateFile ( String nm, boolean make_header ) {
		this("." + File.separator, nm, make_header);
		
	}
	
	/**
	 * Constructor.
	 * @param path specifies the explicit path to a folder in which to create the file. 
	 * User must call WriteFile() when complete. 
	 * @param nm the name of the file
	 * @param make_header specifies if a header is added
	 */
	public CreateFile ( String path, String fname, boolean make_header ) {
		this.filename = fname;
		
		if ( !path.endsWith(File.separator) )
			path = path.concat(File.separator);
		
		this.file = new File(path + this.filename);
		this.filedata = new StringBuffer();

		if ( make_header )
			insertHeader();
	
	}
	
	/**
	 * Appends the String data to the the file buffer.
	 * @param data the data to append
	 * @throws IllegalStateException if the file has already been written.
	 */
	public void add ( String data ) throws IllegalStateException {
		if ( this.filedata == null ) {
			throw new IllegalStateException("Data can not be added to the buffer once the file is written.");
		}
		
		if ( data != null )
			this.filedata.append(data);
		
	}
	
	/**
	 * Appends String data to the current line in the file buffer 
	 * and automatically inserts a carriage return, '\r', and line feed, '\n'.
	 * @param line the line of data to append
	 * @throws IllegalStateException if the file has already been written.
	 */
	public void addLine ( String line ) throws IllegalStateException {
		if ( line != null ) {
			this.filedata.append(line);
			this.filedata.append("\r\n");
		}
		
	}
	
	/**
	 * Appends a carriage return, '\r', and line feed, '\n', into the buffer.
	 * @throws IllegalStateException if the file has already been written.
	 */
	public void addNewLine () throws IllegalStateException {
		this.filedata.append("\r\n");

	}

	/**
	 * Reads data from a source file into the buffer.
	 * @param source_file the source file from which to read the data.
	 * @throws FileNotFoundException if the source file is not found.
	 * @throws IllegalStateException if the file has already been written.
	 * @throws IOException if there is an error reading the source file data.
	 */
	public void readFromFile ( File source_file )
	throws FileNotFoundException, IllegalStateException, IOException {
	   	int buffer_size = 1024;
        BufferedReader reader = new BufferedReader(new FileReader(source_file));
    	char[] buffer = new char[buffer_size];
    	int num_read = 0;
    	
    	if ( this.filedata.length() != 0 )
    		this.filedata = new StringBuffer();
    
    	while( (num_read = reader.read(buffer)) != -1 ) {
    		String readData = String.valueOf(buffer, 0, num_read);
    		this.filedata.append(readData);
    		buffer = new char[buffer_size];
    	}
    
    	reader.close();
        
	}
	
	/**
	 * Writes the buffered data to the file.
	 * @throws IllegalStateException if the file has already been written.
	 * @throws IOException if there is an error writing to the file.
	 */
	public void writeFile () throws IllegalStateException, IOException {
		if ( this.filedata == null ) {
			throw new IllegalStateException("File has already been written.");
		}
		
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(this.file);
			writer.write(this.filedata.toString());
		}
		
		finally {
			writer.flush();
			writer.close();
			this.filedata = null;
		}
		
	}
	
	/**
	 * Retrieves the full path name of the file
	 * @return (String) - the pathname of the file
	 */
	public String getPathName () {
		return file.getPath();
		
	}
	
	/**
	 * Appends a default header into the file buffer. 
	 * Header includes month and year of creation and a message about being
	 * created.
	 */
	protected void insertHeader () {
		Calendar now = Calendar.getInstance();
		String date_created = now.get(Calendar.DAY_OF_MONTH) + " ";
		
		switch(now.get(Calendar.MONTH))
		{
		case (Calendar.JANUARY):
			date_created += "Jan ";
		break;
		case (Calendar.FEBRUARY):
			date_created += "Feb ";
		break;
		case (Calendar.MARCH):
			date_created += "Mar ";
		break;
		case (Calendar.APRIL):
			date_created += "Apr ";
		break;
		case (Calendar.MAY):
			date_created += "May ";
		break;
		case (Calendar.JUNE):
			date_created += "Jun ";
		break;
		case (Calendar.JULY):
			date_created += "Jul ";
		break;
		case (Calendar.AUGUST):
			date_created += "Aug ";
		break;
		case (Calendar.SEPTEMBER):
			date_created += "Sep ";
		break;
		case (Calendar.OCTOBER):
			date_created += "Oct ";
		break;
		case (Calendar.NOVEMBER):
			date_created += "Nov ";
		break;
		case (Calendar.DECEMBER):
			date_created += "Dec ";
		break;
		}
		
		date_created += now.get(Calendar.YEAR);
		
		String header =
		"#!/bin/sh" +
		"\r\n#########################################################" +
		"\r\n#" +
		"\r\n#  This file was automatically generated for" +
		"\r\n#    the dissertation of Gary Mayer." +
		"\r\n#" +
		"\r\n#  Date created: " + date_created + 
		"\r\n#" +
		"\r\n#########################################################" +
		"\r\n\r\n";
		
		this.filedata.append(header);
		
	}
	
}
