/*
Copyright (c) 2014, Intel Corporation

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package tlv;

import tlv.config.*;
import tlv.parse.ActiveParseContext;
import tlv.parse.ParseContext;
import tlv.parse.SourceFile;
import tlv.parse.identifier.Identifier;
import tlv.utilities.Severity;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.IOException;


/**
 * Top-level functionality, called by Executable after instantiating project-specific functionality.
 * 
 * @author sfhoover
 *
 */
public class Main // TODO: This should be given a better name, but environment scripts must also be updated.
{
	//
	// Utility methods.
	//
	
	public static void breakpoint()
	{
		breakpoint(true);
	}
	public static void breakpoint(boolean report)
	{
		if (report) {ActiveParseContext.Report(0, Severity.DEBUG, "BREAKPOINT", "Main.breakpoint() called.");}
	}
	
	public static void unwrittenCode()
	{
		ActiveParseContext.Report(0, Severity.BUG, "UNWRITTEN-CODE", "Execution reached unwritten code.  Debug in debugger.");
	}
	
	// Java native "assert" must be enabled.  I'm not sure how to enable them, and I'd rather have it always enabled, so use this instead.
	public static void assertion(boolean expr)
	{
		if (!expr) ActiveParseContext.Report(0, Severity.BUG, "ASSERT", "Assertion failed");
	}
	
	public static int phaseToCyc(int phase)
	{
		return phase >> 1;  // (Note that divide by 2 wouldn't work for odd negative values.)
	}
	
	public static int cycToPhase(int cyc)
	{
		return cyc << 1;
	}
	
	public static int phaseWithinCyc(int phase)
	{
		return (phase & 1);  // Note that modulo would produce the wrong result for negative phases.
	}
	
	public static boolean isOdd(int phase)
	{
		return (phaseWithinCyc(phase) != 0);
	}
	
	public static boolean isEven(int phase)
	{
		return (phaseWithinCyc(phase) == 0);
	}


	// A version of String.charAt(..) that does not throw an exception.
	public static char safeCharAt(String str, int pos)
	{
		try
		{
			if ((pos >= str.length()) || (pos < 0))
			{
				return '\0';
			} else
			{
				return str.charAt(pos);
			}
		} catch (StringIndexOutOfBoundsException e)
		{
			assertion(false);
			return '\0';
		}
	}
	

	/**
	 * Indents a string (which can be multiple lines).
	 * @param indentation Indentation string.
	 * @param str String to indent.  Empty lines will not get indentation.
	 * @return
	 */
	public static String indentString(String indentation, String str)
	{
		return indentation +
		       ((str.length() > 0)
				   ? (str.substring(0,str.length()-1).replaceAll("\n", "\n" + indentation) +
		              str.charAt(str.length()-1))
		           : str
		        );
	}

	/**
	 * Indents a string (which can be multiple lines).  Blank lines are not indented.
	 * @param indentation Indentation string.
	 * @param str String to indent.  Empty lines will not get indentation.
	 * @return
	 */
	public static String indentNonBlank(String indentation, String str)
	{
		return ((Main.safeCharAt(str, 0) != '\n') ? indentation : "") + str.replaceAll("\n([^\n])", "\n" + indentation + "$1");
	}
	


	public static ProjSpecific projSpecific;
	
	public static CommandLineOptions command_line_options;
	
	public static void main(String[] args)
    {
		command_line_options = new CommandLineOptions(args);
		
		// Use "project" command line arg to pick from which tlv.config
		// class to use.
		projSpecific = new ProjSpecific();
		ActiveParseContext.ReportIf(projSpecific == null, 0, Severity.FATAL_ERROR, "BAD_ARG", "Project \"" + command_line_options.project() + "\" unknown");
		
		
		Identifier.init();
		
		//Added a comment
		//Create a new SourceFile instance from the file name(which may include path)
		SourceFile file = new SourceFile(command_line_options.inFile(), command_line_options.outFile());
		
		try
		{
			file.process();
		}
		catch (Exception e)
		{
			ActiveParseContext.ReportException(0, Severity.FATAL_BUG, "EXCEPTION", "Uncaught exception: " + e.getMessage() + "\nParse context above may or may not be helpful.", e);
		}
		
		ActiveParseContext.cleanup();
		System.exit(ActiveParseContext.get().getExitStatus());
    }
}
