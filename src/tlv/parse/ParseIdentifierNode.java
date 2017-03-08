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

import tlv.parse.identifier.Identifier;

/**
 * The ParseNode objects are used for splitting up the TLV source file into easily manageable
 * hierarchical tree. The position of ParseNode in the Hierarchical tree is determined by its relative
 * indentation.
 * 
 * @author ypyatnyc
 * @version 1.0
 * @see ParseBranch
 * @see ParseTLV
 * @see ParseBehHier
 * @see ParsePipeline
 * @see ParseWhen
 * @see ParseStage
 * @see ParseAttribute
 * @see Assignment
 */
public class ParseIdentifierNode extends ParseNode
{
	protected Identifier identifier;
	
	
	protected void setIdentifier(Identifier identifier_)
	{
		identifier = identifier_;
	}
	public Identifier getIdentifier()
	{
		return identifier;
	}

	
	/**
	 *Constructs an empty, undefined ParseNode 
	 */
	public ParseIdentifierNode()
	{
		super();
	}
	
	/**
	 * Constructs an undefined ParseNode, and sets its string to the parameter line.
	 * 
	 * @param line  A string from an TLV for which a new ParseBranch is create
	 */
	public ParseIdentifierNode(String line)
	{
		super(line);
	}
	
	/**
	 * Constructs a clone ParseNode from another source_node ParseNode.
	 * 
	 * @param	source_node		this existing parse node is passed into the constructor and all of its
	 * attributes are copied over to the newly constructed ParseNode. 
	 */
	public ParseIdentifierNode(ParseNode source_node)
	{
		super(source_node);
	}
	
	/** 
     * Constructs a cloned ParseNode from existing source_node and links it with a ParseBranch parent_branch object
     *
	 * @param	parent_branch	the newly constructed ParseNode will point to this ParseBranch as its parent branch.
	 * @param	source_node		this existing parse node is passed into the constructor and all of its 
	 * attributes are copied over to the newly constructed ParseNode. 
                    
	 * @since  1.0
	 * @see	ParseBranch
     */
	public ParseIdentifierNode(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		
		// Parse identifier.
		identifier = Identifier.parse(-1);  // TODO: This constructor could take a 'legal_identifiers' mask.
	}
	// A temporary varient of the above constructor, that does not create the identifier.  It is used by ParsePhase.
	// TODO: Enable ParsePhase to create an identifier and phase this out.
	public ParseIdentifierNode(ParseBranch parent_branch, ParseNode source_node, boolean dummy)
	{
		super(parent_branch, source_node);
	}
}
