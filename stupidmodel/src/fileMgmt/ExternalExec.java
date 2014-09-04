/*
 * ExternalExec.java
 * 
 * Package Version: 5.0
 *   Class Version: 1.0
 *  
 * 		   Project: MEDLAND Project
 * 					Arizona State University
 * 
 * 			Author: Gary R. Mayer
 *   		  Date: 22 August 2008
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.util.Enumeration;
//import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import misc.StringPair;

/**
 * This class is used to execute programs outside of the Java Runtime Environment (JRE).
 * A list with the command and parameters may be specified along with, optionally, a unique
 * table of process environment variables and a string containing process input. The
 * execution methods do not assume anything about the returned output, errors, or exit value.
 * (i.e., they will not throw exceptions if the return value is non-zero nor if there is
 * something on the error stream). These values are returned to the calling function within
 * an OpResult class structure. This is useful for external functions, such as current GRASS
 * modules, which may not return 0 for a properly executed function and/or may place
 * information, instead of errors, on the error stream.
 * 
 * @version Package: 5.0, Class: 1.0
 * @author Gary R. Mayer, ASU Dept of Computer Science and Engineering
 */
public class ExternalExec
{
	private ProcessBuilder processBuilder = null;
	private Process process = null;
	private StreamCapture errorCapture = null;
	private StreamCapture outputCapture = null;
	
	/**
	 * Default constructor.
	 */
	public ExternalExec () {}
	
	/**
	 * Execute a command with associated parameters.
	 * @param cmd The command and parameters to execute.
	 * @param env Optional environment variables to add to the standard process environment variable
	 * map. The key and value of the table must correspond to the key and value of the environment
	 * map. Set this parameter to null if there are no environment variables to add. 
	 * @return the result of the execution operation (output, errors, and return value).
	 * @throws IllegalArgumentException if the command list is null.
	 * @throws IOException if an exception occurs while starting or running the process.
	 */
	//public OpResult execute ( List<String> cmd, Hashtable<String, String> env )
	public OpResult execute ( List<String> cmd, List<StringPair> env )
	throws IllegalArgumentException, IOException {
		return execute(cmd, env, null);
		
	}
	
	/**
	 * Execute a command with associated parameters and provide external input to the process.
	 * @param cmd The command and parameters to execute.
	 * @param env Optional environment variables to add to the standard process environment variable
	 * map. The key and value of the table must correspond to the key and value of the environment
	 * map. Set this parameter to null if there are no environment variables to add. 
	 * @param process_input An optional String of data to provide to the process as input. Set this
	 * parameter to null if there is no external input to the process.
	 * @return the result of the execution operation (output, errors, and return value).
	 * @throws IllegalArgumentException if the command list is null.
	 * @throws IOException if an exception occurs while starting or running the process.
	 */
	//public OpResult execute ( List<String> cmd, Hashtable<String, String> env, String process_input )
	public OpResult execute ( List<String> cmd, List<StringPair> env, String process_input )
	throws IllegalArgumentException, IOException {
		OpResult result = null;
		
		if ( cmd == null ) {
			throw new IllegalArgumentException("Command list may not be null.");
		}
		
		
		processBuilder = new ProcessBuilder(cmd);
		
		// Get current environment variable map and add new environment variables.
		Map<String, String> proc_env = processBuilder.environment();
		
		if ( env != null ) {
			for (StringPair mapping : env) {
				proc_env.put(mapping.key, mapping.value);
			}
		}
		/*
		if ( env != null ) {
			Enumeration<String> eKeys = env.keys();
			while ( eKeys.hasMoreElements() ) {
				String mapKey = eKeys.nextElement();
				String mapValue = env.get(mapKey);
				proc_env.put(mapKey, mapValue);
			}
		}
		*/
		try {
			// Start the command process.
			process = processBuilder.start();
			
			// If there is an external input stream into this subprocess,
			// write the data by redirecting the output stream of the process builder.
			if ( process_input != null ) {			
				OutputStream os = process.getOutputStream();
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
				out.write(process_input);
				out.flush();
			}
			 
			// Specify output and error handling streams.
			outputCapture = new StreamCapture(process.getInputStream());
			errorCapture = new StreamCapture(process.getErrorStream());
			
			// Start StreamCapture threads.
			errorCapture.start();
			outputCapture.start();
			
			// Wait for process thread to finish.
			int exitValue = process.waitFor();
			
			// Check for output and error messages. 
			String outputString = new String();
			String outputData = outputCapture.getData();
			while ( outputData != null ) {
				outputString = outputString.concat(outputData);
				outputData = outputCapture.getData();
			}
			
			String errorString = new String();
			String errorData = errorCapture.getData();
			while ( errorData != null ) {
				errorString = errorString.concat(errorData);
				errorData = errorCapture.getData();
			}
			
			// Save resultant data.
			result = new OpResult(outputString, errorString, exitValue);
		}
		
		catch (InterruptedException ie) {
			// Do nothing; OK to just continue and then exit.
		}

		finally {
			// Clean up data streams and processes.
			if ( errorCapture != null )
				errorCapture.done();
			if ( outputCapture != null )
				outputCapture.done();
			if ( process != null )
				process.destroy();
			
			errorCapture = null;
			outputCapture = null;
			process = null;
			processBuilder = null;
		}
		
		return result;
		
	}
	
	/**
	 * This class holds the resultant output stream data, error stream data, and
	 * exit value generated by a process execution and completion.
	 * @version Package: 5.0, Class: 1.0
	 * @author Gary R. Mayer, ASU Dept of Computer Science and Engineering
	 */
	public class OpResult {
		public final String output;
		public final String errors;
		public final int exitValue;
		
		public OpResult ( String output, String errors, int exit_value ) {
			this.output = output;
			this.errors = errors;
			this.exitValue = exit_value;
			
		}
		
	}

	/**
	 * This class creates an input data stream which operates in its own
	 * thread of execution.
	 * @version Package: 5.0, Class: 1.0
	 * @author Gary R. Mayer, ASU Dept of Computer Science and Engineering
	 */
	protected class StreamCapture extends Thread {
		private InputStream is;
		private InputStreamReader isr;
		private BufferedReader br;
		private int buffer_size;
		
		/**
		 * Convenience constructor to create a new stream capture from an input
		 * stream using a default buffer of 128 characters.
		 * @param is The input stream from which to capture data.
		 */
		public StreamCapture ( InputStream is ) {
			this(is, 128);
			
		}
		
		/**
		 * Creates a new stream capture from an input stream using the specified
		 * character buffer size.
		 * @param is The input stream from which to capture data.
		 * @param buffer_size The maximum number of characters read from the stream
		 * at one time.
		 */
		public StreamCapture ( InputStream is, int buffer_size ) {
			this.is = is;
			this.isr = new InputStreamReader(this.is);
			this.br = new BufferedReader(this.isr);
			this.buffer_size = buffer_size;
			
		}
		
		/**
		 * Reads the currently specified number of characters from the stream.
		 * @return A String containing the characters read from the stream.
		 * @throws IOException if there is an error reading the stream.
		 */
		protected String getData () throws IOException {
			return getData(this.buffer_size);
			
		}
		
		/**
		 * Reads the specified number of characters from the stream. Note that
		 * this does not overwrite the current buffer stream setting. Subsequent
		 * calls to GetData() will return a maximum number of characters equal to
		 * the set value. 
		 * @param num_chars The maximum number of characters to read from the
		 * stream.
		 * @return A String containing the characters read from the stream.
		 * @throws IOException if there is an error reading from the stream.
		 */
		protected String getData ( int num_chars ) throws IOException {
			String data = null;
			char[] data_buffer = new char[num_chars];
			
			try	{
				int result = this.br.read(data_buffer, 0, num_chars);
				
				// copy data in buffer if not immediate end of file
				if ( result != -1 )
					data = new String(data_buffer);
			}
			
			catch (IOException ioe) {
				data = null;
				throw ioe;
			}
			
			finally	{
				data_buffer = null;
			}
			
			return data;

		}
	
		/**
		 * Retrieves a line of data from the stream.
		 * @return A String representing the line read from the stream or
		 * null if there is no further data on the stream.
		 * @throws IOException if there is an error reading the data.
		 */
		protected String getLine () throws IOException	{
			String data = null;
			
			try	{
				data = this.br.readLine();
			}
			
			catch (IOException ioe) {
				data = null;
				throw ioe;
			}
		
			return data;
			 
		}
	
		/**
		 * Convenience function to close the stream.
		 */
		protected void done () {
			try	{
				br.close();
				isr.close();
			}
			
			catch (IOException ioe)	{
				// Error while closing stream. Ignore; it is being destroyed anyway.
			}
			
		}
		
		/**
		 * Sets the size of the buffer for stream reads. Each read will read the
		 * number of characters specified by the buffer size as long as this does
		 * not exceed the number of charcters remaining in the stream. If this is
		 * the case, it will just read the remaining characters.
		 * @param num_chars
		 */
		public void setBufferSize ( int num_chars ) {
			this.buffer_size = num_chars;
			
		}
			
	}

}
