package stupidmodel.common;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class InfoLog
{
	private boolean[] display;
	private boolean[] log;
	private boolean[] pause_log;
	private PrintWriter[] logger;
	private String filepath;
	private String root_filename, filetype_suffix;
	private String[] filename;
	private Hashtable DataFileRef;
	private final static int INFO_INDEX = 0;
	private final static int ERRORS_INDEX = 1;
	private final static int DATA_INDEX = 2;
	public final static int NONE = 0;
	public final static int ALL = 7;
	public final static int INFO = 1; 
	public final static int ERRORS = 2;
	public final static int DATA = 4;
	
	public InfoLog ()
	{
		this.display = new boolean[] { true, true, true };
		this.log = new boolean[] { false, false, false };
		this.pause_log = new boolean[] { false, false, false };
		this.logger = null; //new PrintWriter[] { null, null, null };
		this.filepath = null;
		this.root_filename = null;
		this.filename = null; //new String[] { null, null, null };
		this.DataFileRef = null;
		
	}
	
	/**
	 * Sets the file path and root file name information.
	 * @param path - the file path
	 * @param fname - the file's root name
	 * @param data_file_names - names of each data file
	 */
	public void SetFileInfo ( String path, String fname, String[] data_file_names )
	{
		int num_dfiles = 0;
		this.filepath = path;
		
		// if the filename has a period, see if the suffix is 3 characters.
		//	If so, treat as a file type suffix.
		int suffix_index = fname.lastIndexOf(".");
		if ( (suffix_index != -1) && (suffix_index != 0) )
		{
			if ( (fname.length() - 1) - suffix_index < 4 )
			{
				this.filetype_suffix = fname.substring(suffix_index);
				this.root_filename = fname.substring(0, suffix_index);
			}
		}
		
		else
		{
			this.root_filename = fname;
			this.filetype_suffix = ".txt";
		}
		
		if ( data_file_names != null )
		{
			num_dfiles = data_file_names.length;
			this.DataFileRef = new Hashtable(num_dfiles);
		}
		
		this.filename = new String[num_dfiles+2];
		this.logger = new PrintWriter[num_dfiles+2];
		
		this.filename[INFO_INDEX] = this.root_filename + "_inf" + this.filetype_suffix;
		this.filename[ERRORS_INDEX] = this.root_filename + "_err" + this.filetype_suffix;
		
		if ( data_file_names != null )
		{
			for ( int df = 0; df < num_dfiles; df++ )
			{
				this.filename[df+2] = this.root_filename + "_" + data_file_names[df] + "_dat" + this.filetype_suffix;
				this.DataFileRef.put((String)data_file_names[df], (Integer)new Integer(df+2));
			}
		}
		
	}

	public void SetDisplay ( int display_settings )
	{
		SetFlagArray(this.display, display_settings);
		
	}
	
	public void SetLog ( int log_settings )
	{
		SetFlagArray(this.log, log_settings);
		
	}
	
	public void StartAll ( int type, boolean overwrite )
	{
		StartLog(type, overwrite);
		//SetDisplay(type);
		
	}
	
	public void StopAll ()
	{
		StopLog(InfoLog.ALL);
		SetDisplay(InfoLog.NONE);
		
	}
	
	// info = 1, errors = 2, data = 4;
	public void StartLog ( int type_log, boolean overwrite )
	{
		if ( (type_log <= 0) || (type_log > 7) )
			return;

		
		// start a new info capture
		if ( (type_log == 1) || (type_log == 3) || (type_log == 5) || (type_log == 7) )
		{
			if ( this.filename[INFO_INDEX] == null )
				return;
			
			if ( this.logger[INFO_INDEX] == null )
				this.logger[INFO_INDEX] = CreateFile(this.filename[INFO_INDEX], overwrite);
			
			this.log[INFO_INDEX] = true;
			this.pause_log[INFO_INDEX] = false;
		}
		
		// start a new errors capture
		if ( (type_log == 2) || (type_log == 3) || (type_log == 6) || (type_log == 7) )
		{
			if ( this.filename[ERRORS_INDEX] == null )
				return;
			
			if ( this.logger[ERRORS_INDEX] == null )
				this.logger[ERRORS_INDEX] = CreateFile(this.filename[ERRORS_INDEX], overwrite);
			
			this.log[ERRORS_INDEX] = true;
			this.pause_log[ERRORS_INDEX] = false;
		}
		
		// start a new data capture
		if ( (type_log == 4) || (type_log == 5) || (type_log == 6) || (type_log == 7) )
		{
			if ( this.DataFileRef == null )
				return;
			
			int num_dfiles = this.DataFileRef.size();
			
			for ( int df = 2; df < num_dfiles+2; df++ )
			{
				if ( this.logger[df] == null )
					this.logger[df] = CreateFile(this.filename[df], overwrite);
			}
			
			this.log[DATA_INDEX] = true;
			this.pause_log[DATA_INDEX] = false;
		}
		
	}
	
	public void StopLog ( int type_log )
	{
		if ( (type_log <= 0) || (type_log > 7) )
			return;
		
		// stop an info capture
		if ( (type_log == 1) || (type_log == 3) || (type_log == 5) || (type_log == 7) )
		{
			if ( this.logger[INFO_INDEX] != null )
			{
				this.logger[INFO_INDEX].close();
				this.logger[INFO_INDEX] = null;
			}

			this.log[INFO_INDEX] = false;
			this.pause_log[INFO_INDEX] = false;
		}
		
		// stop an errors capture
		if ( (type_log == 2) || (type_log == 3) || (type_log == 6) || (type_log == 7) )
		{
			if ( this.logger[ERRORS_INDEX] != null )
			{
				this.logger[ERRORS_INDEX].close();
				this.logger[ERRORS_INDEX] = null;
			}

			this.log[ERRORS_INDEX] = false;
			this.pause_log[ERRORS_INDEX] = false;
		}
		
		// stop a data capture
		if ( (type_log == 4) || (type_log == 5) || (type_log == 6) || (type_log == 7) )
		{
			int num_dfiles = this.DataFileRef.size();
			
			for ( int df = 2; df < num_dfiles+2; df++ )
			{
				if ( this.logger[df] != null )
				{
					this.logger[df].close();
					this.logger[df] = null;
				}
			}

			this.log[DATA_INDEX] = false;
			this.pause_log[DATA_INDEX] = false;
		}
		
	}
	
	public void PauseLog ( int type_log )
	{
		// pause an info capture
		if ( (type_log == 1) || (type_log == 3) || (type_log == 5) || (type_log == 7) )
			this.pause_log[INFO_INDEX] = true;
		
		// pause an errors capture
		if ( (type_log == 2) || (type_log == 3) || (type_log == 6) || (type_log == 7) )
			this.pause_log[ERRORS_INDEX] = true;
		
		// pause a data capture
		if ( (type_log == 4) || (type_log == 5) || (type_log == 6) || (type_log == 7) )
			this.pause_log[DATA_INDEX] = true;
		
	}
	
	public void ResumeLog ( int type_log )
	{
		// pause an info capture
		if ( (type_log == 1) || (type_log == 3) || (type_log == 5) || (type_log == 7) )
			this.pause_log[INFO_INDEX] = false;
		
		// pause an errors capture
		if ( (type_log == 2) || (type_log == 3) || (type_log == 6) || (type_log == 7) )
			this.pause_log[ERRORS_INDEX] = false;
		
		// pause a data capture
		if ( (type_log == 4) || (type_log == 5) || (type_log == 6) || (type_log == 7) )
			this.pause_log[DATA_INDEX] = false;
		
	}
	
	public void LogInfoLine ( String info )
	{
		if ( this.log[INFO_INDEX] && !this.pause_log[INFO_INDEX] )
		{
			this.logger[INFO_INDEX].println(info);
			this.logger[INFO_INDEX].flush();
		}
		
		if ( this.display[INFO_INDEX] )
			System.out.println(info);
		
	}
	
	public void LogInfo ( String info )
	{
		if ( this.log[INFO_INDEX] && !this.pause_log[INFO_INDEX] )
			this.logger[INFO_INDEX].print(info);
		
		if ( this.display[INFO_INDEX] )
			System.out.print(info);
		
	}	

	public void LogErrorLine ( String error )
	{
		if ( this.log[ERRORS_INDEX] && !this.pause_log[ERRORS_INDEX] )
		{
			this.logger[ERRORS_INDEX].println(error);
			this.logger[ERRORS_INDEX].flush();
		}
		
		if ( this.display[ERRORS_INDEX] )
			System.err.println(error);
		
	}
	
	public void LogError ( String error )
	{
		if ( this.log[ERRORS_INDEX] && !this.pause_log[ERRORS_INDEX] )
			this.logger[ERRORS_INDEX].print(error);
		
		if ( this.display[ERRORS_INDEX] )
			System.err.print(error);
		
	}
	
	public void LogDataLine ( String dlog_name, String data )
	{
		if ( this.display[DATA_INDEX] )
			System.out.println(data);
		
		if ( this.DataFileRef == null )
			return;
		
		Integer df = (Integer) this.DataFileRef.get(dlog_name);
		
		if ( df != null )
		{
			if ( this.log[DATA_INDEX] && !this.pause_log[DATA_INDEX] )
			{
				this.logger[df.intValue()].println(data);
				this.logger[df.intValue()].flush();
			}
		}
				
	}
	
	public void LogData ( String dlog_name, String data )
	{
		if ( this.display[DATA_INDEX] )
			System.out.print(data);
		
		if ( this.DataFileRef == null )
			return;
		
		Integer df = (Integer) this.DataFileRef.get(dlog_name);
		
		if ( df != null )
		{
			if ( this.log[DATA_INDEX] && !this.pause_log[DATA_INDEX] )
				this.logger[df.intValue()].print(data);
		}
		
	}
	
	public void LogAll ( String info )
	{
		LogInfoLine(info);
		LogErrorLine(info);
		
		if ( this.DataFileRef == null )
			return;
		
		Enumeration dfile_names = this.DataFileRef.keys();
		while ( dfile_names.hasMoreElements() )
			LogDataLine((String) dfile_names.nextElement(), info);
		
	}
	
	public void LogAllButError ( String info )
	{
		LogInfoLine(info);
		
		if ( this.DataFileRef == null )
			return;
		
		Enumeration dfile_names = this.DataFileRef.keys();
		while ( dfile_names.hasMoreElements() )
			LogDataLine((String) dfile_names.nextElement(), info);
		
	}
	
	private PrintWriter CreateFile ( String filename, boolean overwrite )
	{
		PrintWriter pw = null;
		File file = null;
		FileWriter fw = null;
		int num = 1;

		if ( !this.filepath.endsWith(File.separator) )
			this.filepath.concat(File.separator);
		
		try
		{
			file = new File(this.filepath + filename);
			
			if ( !overwrite )
			{
				while ( file.exists() )
				{
					file = new File(this.filepath + filename + "_" + num);
					num++;
				}	
			}
			
			fw = new FileWriter(file, false);
			pw = new PrintWriter(fw, true);			// using println() autoflushes buffer
		}
		
		catch ( IOException ioe )
		{
			System.err.println("An error occurrred while creating the file " + this.filepath + filename + "\n" + ioe.getMessage());
			pw.close();
		}
		
		return pw;
		
	}
	
	//	info = 1, errors = 2, data = 4
	private void SetFlagArray ( boolean[] farray, int setting )
	{
		if ( setting < 0 )
			setting = 0;
		else if ( setting > 7 )
			setting = 7;
		
		switch (setting)
		{
		case (7):
			farray[DATA_INDEX] = true;
			farray[ERRORS_INDEX] = true;
			farray[INFO_INDEX] = true;
			break;
			
		case (6):
			farray[INFO_INDEX] = false;
			farray[ERRORS_INDEX] = true;
			farray[DATA_INDEX] = true;
			break;
			
		case (5):
			farray[INFO_INDEX] = true;
			farray[ERRORS_INDEX] = false;
			farray[DATA_INDEX] = true;
			break;
			
		case (4):
			farray[INFO_INDEX] = false;
			farray[ERRORS_INDEX] = false;
			farray[DATA_INDEX] = true;
			break;
			
		case (3):
			farray[INFO_INDEX] = true;
			farray[ERRORS_INDEX] = true;
			farray[DATA_INDEX] = false;
			break;
			
		case (2):
			farray[INFO_INDEX] = false;
			farray[ERRORS_INDEX] = true;
			farray[DATA_INDEX] = false;
			break;
			
		case (1):
			farray[INFO_INDEX] = true;
			farray[ERRORS_INDEX] = false;
			farray[DATA_INDEX] = false;
			break;
			
		case (0):
			farray[INFO_INDEX] = false;
			farray[ERRORS_INDEX] = false;
			farray[DATA_INDEX] = false;
			break;
			
		default:
			farray[INFO_INDEX] = false;
			farray[ERRORS_INDEX] = false;
			farray[DATA_INDEX] = false;
			break;
		}
		
	}
	
	public void finalize ()
	{
		int num_indicies = 2;
		
		if ( this.DataFileRef != null )
			num_indicies += this.DataFileRef.size();
		
		for ( int index = 0; index < num_indicies; index++ )
		{
			if ( this.logger[index] != null )
			{
				if ( this.logger[index].checkError() )
				{
					this.logger[index].println("Errors encountered during logging.");
					System.err.print("Errors encountered during ");
					
					if ( index == INFO_INDEX )
						System.err.println("information logging.");
					
					else if ( index == ERRORS_INDEX )
						System.err.println("errors logging.");
					
					else
						System.err.println("data logging.");
				}

				this.logger[index].close();
			}
		}
	}
	
}