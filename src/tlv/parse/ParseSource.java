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
import java.io.File;
import java.util.regex.Pattern;

import tlv.*;
import tlv.behavioral.*;
import tlv.parse.identifier.ParseMnemonic;
import tlv.utilities.Severity;


/**
 * The ParseSource class represents a ParseNode that is associated with a particular block of source code.
 * This class represents a \source branch, and the derived class SourceFile, represents an TLV source code file node.
 * 
 * @author sfhoover
 *
 */
public class ParseSource extends ParseBranch
{
	/**
	 * The source file.
	 */
	protected File source_file;
	
	int start_line_num = -1;

	/**
     * Constructs a ParseSource from a source_node and links it with another ParseBranch parent_branch object.
     *
	 * @param	parent_branch	the newly constructed ParseSource will point to this ParseBranch as its parent branch.
	 * @param	source_node		this existing parse node is passed into the constructor and all of its 
	 * attributes are copied over to the newly constructed ParseSource.   TODO: Not the best mechanism.
	 * 
	 * @see	ParseBranch
	 * @see ParseNode
     */
	public ParseSource(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		setType(NodeType.SOURCE);
	}
	
	// Used by SourceFile.
	public ParseSource(File _source_file)
	{
		super();
		source_file = _source_file;
		start_line_num = 1;
	}
	
	public File getSourceFile()
	{
		return source_file;
	}
	
	/**
	 * Parse this node from the given context
	 */
	public void parse(ParseContext context)
	{
		final String parse_error_tag = "SOURCE-NODE-SYNTAX";
		final String parse_error_message = getIdentifier().toString() + " nodes require args <file-name> <line> delimited by single spaces.";

		// Skip space.
		context.parseLiteralChar(' ', Severity.RECOV_ERROR, parse_error_tag, parse_error_message);
		String file_name = context.parseUntil(' ', Severity.RECOV_ERROR, parse_error_tag, parse_error_message);
		if (file_name != null)
		{
			start_line_num = context.parseUnsignedInt(Severity.RECOV_ERROR, parse_error_tag, parse_error_message);
			// Parse file and line number substrings.
			source_file = new File(file_name);
		}
	}
}
