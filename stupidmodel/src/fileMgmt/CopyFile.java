/*
 * CopyFile.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This class provides a common interface for copying one File to another. 
 * 
 * @version Package: 5.0, Class: 1.0
 * @author Gary R. Mayer, ASU Dept of Computer Science and Engineering
 */
public final class CopyFile { 
	// Private constructor. No object instantiations. 
	private CopyFile () {}
	
	/**
	 * Copies a source File to a destination File, where the Java File may be either
	 * an actual file or a directory. A source file may be copied to either a
	 * destination file or a destination directory, where it would be assigned the
	 * source file's name. A source directory must be copied to a directory destination.
	 * If the source is a directory, it will recursively copy all files and directories
	 * in the specified root source path.
	 * @param source A file or directory to copy from.
	 * @param destination The file or directory to copy to.
	 * @throws IOException if the source file is not found or there is an error reading
	 * the source file.
	 */
	public static void copy ( File source, File destination ) throws IOException {
        // Source is a directory, copy all files in the source directory.
		if ( source.isDirectory()) {
			// Create destination directory if it does not exist.
            if ( !destination.exists() ) {
                destination.mkdir();
            }
            else if ( destination.isFile() ) {
            	throw new IOException("Unable to copy a source directory to a destination file.");
            }
            
            // Get all of the contents of this directory (directories and files)
            //	and copy them to the destination.
            String[] children = source.list();
            for ( int i = 0; i < children.length; i++ ) {
                copy(new File(source, children[i]),
                        new File(destination, children[i]));
            }
        }
        
		// Source is a file, copy it to the specified destination.
        else {
           	// Destination is a directory, copy source to
        	//	destination using source name.
            if ( destination.exists() && destination.isDirectory() ) {
            	destination = new File(destination.getAbsolutePath() + source.getName());
            }
            
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);
            
            // Copy the bits from instream to outstream.
            byte[] buffer = new byte[1024];
            int length = in.read(buffer);

            while ( length > 0) {
            	out.write(buffer, 0, length);
            	length = in.read(buffer);
            }
            
            in.close();
            out.close();
        }
		
    }

}
