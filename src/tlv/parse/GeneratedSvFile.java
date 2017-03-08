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

package tlv.parse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import tlv.Main;

public class GeneratedSvFile
{
	/**
	 * The TLV context label of this generated file or null.
	 */
	public String label;
	
	public String filename;
	public File file;
	
	public String file_name;
	
	/**
	 * The associated SourceFile.
	 */
	public SourceFile source_file = null;
	
	private FileWriter fstreamDeclarations;
	private BufferedWriter sv_declarations_file;
	
	/**
	 * Outputs line using platform-specific newLine() in place of "\n".
	 * @param line
	 */
	public void println(String line)
	{
		
		try 
		{
			for(int i = 0; i < line.length(); i++)
			{
				if(line.charAt(i) == '\n')
					sv_declarations_file.newLine();
				else
					sv_declarations_file.write(line.charAt(i));
			}
			
			sv_declarations_file.newLine();
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		if (Main.command_line_options.verbosity() > 2)
		{
			System.out.print(Main.indentString("DECL |", line + "\n"));
		}

	}
	
	/**
	 * This method must be used at the end of the preprocessor flow to "close()" all the generated
	 * files. 
	 */
	public void close() throws IOException
	{
		sv_declarations_file.close();
	}

	
	public GeneratedSvFile(String label_, String filename_, SourceFile source_file_)
	{
		label = label_;
		source_file = source_file_;
		filename = filename_;
		file = new File(filename);

		//prepares output files
		try 
		{	
			fstreamDeclarations = new FileWriter(filename);
			sv_declarations_file = new BufferedWriter(fstreamDeclarations);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		// Write standard header to this file.
		println("// <Legal notice here>\n\n");
		
		println("`include \"tlv.vh\"\n\n");
		
		if (Main.command_line_options.xInj())
		{
			println("// Standard signal used to control X-injection.\n");
			println("bit X_inj;");
			println("`ifdef " + Main.projSpecific.getSvXInjectionDefine());
			println("   assign X_inj = 1'b1;");
			println("`else");
			println("   assign X_inj = 1'b0;");
			println("`endif\n");
		}
	}
}
