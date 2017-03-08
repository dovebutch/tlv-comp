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
import tlv.*;
import tlv.behavioral.*;
import tlv.parse.identifier.ParseMnemonic;


/**
 * The ParsePipeline class extends from ParseBranch, and is used for containing the parts of TLV code that have
 * declaration of Pipeline scope. 
 * 
 * @author ypyatnyc
 *
 */
public class ParsePipeline extends ParseBehScope
{
	/**
	 * Constructs a ParsePipeline from a source_node ParseNode.
	 * 
	 * @param source_node  this existing parse node is passed into the constructor and all of its
	 * attributes are copied over to the newly constructed ParsePipeline.
	 * 
	 * @see ParseNode
	 */
	public ParsePipeline(ParseNode source_node)
	{
		super(source_node);
		setType(NodeType.PIPELINE);
	}
	
	/**
     * Constructs a ParsePipeline from a source_node and links it with another ParseBranch parent_branch object. Only ParseTLV, and ParseHier
     * can act as parent_branch for ParsePipelines.
     *
	 * @param	parent_branch	the newly constructed ParsePipeline will point to this ParseBranch as its parent branch.
	 * @param	source_node		this existing parse node is passed into the constructor and all of its 
	 * attributes are copied over to the newly constructed ParsePipeline. 
	 * 
	 * @see	ParseBranch
	 * @see ParseNode
     */
	public ParsePipeline(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		setType(NodeType.PIPELINE);
	}
		
	/**
	 * This method retrieve the ParsePipeline's associated LogicalPipeline
	 * 
	 * @return the LogicalPIpeline that is associated with this ParsePipeline
	 */
	public LogicalPipeline getLogicalPipeline()
	{
		return (LogicalPipeline)old_identifier.getLogicalNode();
	}
}
