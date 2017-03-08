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

package tlv.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tlv.parse.SourceFile;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionSpecBuilder;



/**
 * This class is an encapsulation of command-line argument processing.  It provides getter methods to access options, thus abstracting away the exact command-line interface.
 * We use jopt for command-line processing.  All use of jopt should be limited to this class.  If, for some reason we should need to abandon jopt,
 *  it should be only this class which would be affected.
 * 
 * @author sfhoover
 *
 */
class RawCommandLineOptions
{
	//********************************************************************************
	// This is just a more convenient alternative method for specifying command-line args, vs. using Eclipse::Run::Run Configurations::Arguments.
	// Each user has his own to avoid merge conflicts.
	
	private static String[] steves_debug_args =
		{//"-h",
		 "-p", "hsx",
		 //"--verbose",
		 "--xinj",
		 "--xclk",
		 //"--bestsv",
		 "-o", "eclipse_run/",
		 //"--conversion",
		 "-i",
			 //"examples/doc_examples/users_guide1.tlv"
			 //"examples/beh_hier/beh_hier.tlv"
			 //"examples/doc_examples/users_guide1.tlv"s
			 "examples/yuras_presentation/yuras_presentation.tlv"
			 //"examples/slide_example/slide_example.tlv"
			 //"proprietary/examples/hahitmectls/hahitmectls.tlv"
		     //"proprietary/examples/gpr_tracker/tracker.tlv"
			 //"proprietary/examples/aes/hdl/aes_dec_10.tlv"
			 //"proprietary/examples/shifter/shifter.tlv"
			 //"run/gen/aes_dec_top/aes_dec_10_xinj.tlv"//, "-o", "run/gen/aes_dec_top/"
			 //"run/gen/rsa/eau_top.tlv", "-noline" // , "-o", "run/gen/rsa/"
		     //"run/gen/ring/ring.tlv", "--xinj", "--xclk" // , "-o", "run/gen/ring"
			 //"run/gen/aplr/aplr.tlv", "--xinj", "--xclk"
		};
	
	private static String[] yuras_debug_args =
		{//"-h",
		 "-p hsx",
		 //"--xinj",
		 //"--xclk",
		 //"-o", "...",
		 "--bestsv",
		 //"-o", "eclipse_run",
		 "--verbose",
		 //"--conversion",
		 //"-i", "/nfs/hd/proj/sdgwork/ypyatnyc/hsx/lv/TLV/workspace/tlv_git_repo/hascheds_baseline.tlv"
		 //"-i", "proprietary/gpr_tracker/tracker.tlv"
		 "-i", "examples/ring/gen/ring.tlv"
		 //"-i", "examples/doc_examples/users_guide1.tlv" 
		 
		};
	//********************************************************************************
	

	/**
	 * Command-line options.
	 */
	protected String in_file_arg = null;
	protected String out_file_arg = null;
	protected String filebase_arg = null;
	protected String proj_arg = null;
	// Debug modes.
	protected int verbosity_level_arg;
	// Mode flags.
	protected boolean x_inj_arg;
	protected boolean x_inj_clock_arg;
	protected boolean best_sv_arg;
	protected boolean conversion_mode_arg;
	protected boolean noline_arg;
	
	protected List<String>  non_opt_args;
	

	
	public RawCommandLineOptions(String[] args)
	{
		// Process arguments using joptsimple libs
		OptionParser parser = new OptionParser();
		OptionSpec<String> help_spec         = parser.accepts("h",     "show help.").withOptionalArg();
		OptionSpec<String> infile_spec       = parser.accepts("i",     "input .tlv path/file (absolute or relative).").withRequiredArg().ofType( String.class );
		OptionSpec<String> outfile_spec      = parser.accepts("o",     "output .vs path/file (absolute or relative).  File name is derived from input file name if arg is a path ending with /.  Additional generated files' names are derived from this.").withRequiredArg().ofType( String.class );
		OptionSpec<String> filebase_spec     = parser.accepts("filebase", "prepended to -i and -o args.  Path and/or base filename").withRequiredArg().ofType( String.class );
		OptionSpec<String> project_spec      = parser.accepts("p",     "project name.").withRequiredArg().ofType( String.class  ).defaultsTo("hsx");
		OptionSpecBuilder x_inj_spec         = parser.accepts("xinj",  "enable X-injection at assignment statements.");
		OptionSpecBuilder x_inj_clock_spec   = parser.accepts("xclk",  "enable X-injection through clock gating condition.");
		OptionSpecBuilder best_sv_spec       = parser.accepts("bestsv","Optimize the readability/maintainability of the generated SV, unconstrained by correlation w/ TLV source.");
		OptionSpecBuilder conversion_spec    = parser.accepts("conversion","Optimize output to support conversion.  Pipesignals can be mixed case, and (project-specific) SV signal name mapping should be optimized to preserve pipesignal names.");
		OptionSpecBuilder noline_spec        = parser.accepts("noline","Disable `line directive in SV output.");
		OptionSpecBuilder verbosity_spec     = parser.accepts("verbose", "Verbose output for debug");  // TODO: Want this to take an optional verbosity level argument (0-10).
		
		// Bypass command line args, and use hard-coded ones.
		if ((args.length == 1) &&
		    args[0].equals("--stevesdebugargs"))
		{
			// Real arg just says to use debug args.
			args = steves_debug_args;
		}
		if ((args.length == 1) &&
			    args[0].equals("--yurasdebugargs"))
		{
			// Real arg just says to use debug args.
			args = yuras_debug_args;
		}
		
			
		try
		{
			ArrayList<String> arglist = new ArrayList<String>(Arrays.asList(args));
			if(arglist.isEmpty())
			{
				throw new Exception("No arguments provided. Use \"-h\" to see available options.");
			};
			// uncomment to enable printing of command line arguments.
			//System.out.println("input args:" + arglist.toString());
			
			OptionSet options = parser.parse(args);
		
			if(options.has(help_spec))
			{
				parser.printHelpOn(System.out);
				System.exit(0);
			};
			

			non_opt_args = options.nonOptionArguments();
			
			if (options.has(filebase_spec))
			{
				filebase_arg = options.valueOf(filebase_spec);
			}
			
			if (options.has(infile_spec))
			{
				in_file_arg = options.valueOf(infile_spec);
			};

			if (options.has(outfile_spec))
			{
				
				out_file_arg = options.valueOf(outfile_spec);
			};
			// If only output path is given, derive file name.
			// TODO
			
			// Derive generated output file name.
			// TODO
			
			proj_arg = options.valueOf(project_spec);
			
			x_inj_arg = options.has(x_inj_spec);

			x_inj_clock_arg = options.has(x_inj_clock_spec);
			
			best_sv_arg = options.has(best_sv_spec);
			
			conversion_mode_arg = options.has(conversion_spec);
			
			noline_arg = options.has(noline_spec);

			verbosity_level_arg = options.has(verbosity_spec) ? 10 : 0;
		}
		catch (Exception e)
		{
			// TODO: Need better error handling here.  Unrecognized command-line args fall here.
			e.printStackTrace();
			System.exit(2);
		}
	}

}




/*
 * This class does light-weight interpretation of the command-line args and presents them to the outside world.
 * It is distinct from RawCommandLineOptions in that it does not use jopt.
 */
public class CommandLineOptions extends RawCommandLineOptions
{
	private String in_file_name;
	private String out_file_name;
	private File in_file;
	private File out_file;
	
	/**
	 * Getters for above command-line options.
	 */
	public File inFile() {return in_file;}
	public File outFile() {return out_file;}
	public String project() {return proj_arg;}
	public boolean xInj() {return x_inj_arg;}
	public boolean xInjClock() {return x_inj_clock_arg;}
	public boolean bestSv() {return best_sv_arg;}
	public boolean conversionMode() {return conversion_mode_arg;}
	public boolean noLine() {return noline_arg;}
	public int verbosity() {return verbosity_level_arg;}
	
	public CommandLineOptions(String[] args)
	{
		super(args);
		
		// Process filename args.
		String filebase = (filebase_arg == null) ? "" : filebase_arg;
	
		in_file_name = filebase;
		if (in_file_arg != null)
		{
			in_file_name += in_file_arg;
		}
		else if (non_opt_args != null)
		{
			// this preserves the original behavior where arg[0] is used
			// for the input file.
			in_file_name += non_opt_args.get(0);
		} else {
			System.err.println("No input file given.");
			System.exit(1);
		}

		in_file = new File(in_file_name);

		out_file_name = filebase;
		if (out_file_arg == null)
		{
			// Get file path from in_file.
			String path = in_file.getParent();
			out_file_name += ((path == null) ? "" : path) + "/";
		} else {
			out_file_name += out_file_arg;
		};
		// Derive file name if a path ending in '/' is given.
		char last_char = out_file_name.charAt(out_file_name.length() - 1);
		if ((last_char == '/') ||
			(last_char == '\\'))
		{
			out_file_name += SourceFile.stripFileExtension(in_file.getName()) + ".vs";
		}
		
		out_file = new File(out_file_name);
	}
}
