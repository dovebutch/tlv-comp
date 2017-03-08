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

package tlv.parse.identifier;

import tlv.Main;
import tlv.behavioral.*;
import tlv.parse.NodeType;
import tlv.parse.ParseBranch;
import tlv.parse.ParseContext;
import tlv.parse.ParseNode;
import tlv.parse.Prefix;


// TODO: Eliminate this class?  Parts are superseded by Identifier and other functionality should become part of ParseNode and perhaps
//       assigned/used ranges?
/**
 * This class is used to link ParseNode objects with their associated LogicalNode objects.
 * The ParseElement encapsulates the string which is used to define the object, and contains
 * a reference to where it is contained within the ParseBranch-ParseNode tree. ParseElement
 * are identified by a Prefix object which contains information on the ParseElement's prefix
 * character and its NodeType
 * 
 * @author ypyatnyc
 *
 */
public class ParseElement
{
	protected ParseNode parseNode;
	private LogicalNode logicalNode;
	
	//Identifiers position of the identifier and its associated
	//string within the ParseNode stringWithoutIndentation
	protected int stringStartingIndex;
	protected int stringEndingIndex;
	
	
	// TODO: These belong in ParseMnemonic?
	protected Prefix prefix;
	
	protected String label;
	
	protected boolean flagNumeric;

	/**
	 * Creates a mostly empty ParseElement object and links it to a ParseNode that is passed
	 * through its parameter
	 * 
	 * @param parseNode_ the ParseNode object that this ParseElement is associated with
	 */
	public ParseElement(ParseNode parseNode_)
	{
		parseNode = parseNode_;
		prefix = Prefix.NONE;
		
		label = "";
		
		flagNumeric 			= false;
	}
	
	/**
	 * Creates a ParseElement with a specified Prefix, associated ParseNode and starting string index
	 * within the ParseNode's string
	 * 
	 * @param parseNode_ the ParseNode object that this ParseElement is associated with
	 * @param prefix_ the identifying Prefix object
	 * @param stringStartingIndex_ the starting string index of the ParseElement with the parseNode_'s string
	 */
	public ParseElement(ParseNode parseNode_, Prefix prefix_, int stringStartingIndex_)
	{
		parseNode = parseNode_;
		logicalNode = null;
		
		prefix = prefix_;
		stringStartingIndex = stringStartingIndex_;
	}
	
	/**
	 * Sets the ParseElement's associated LogicalNode
	 * 
	 * @param logical_node_ the LogicalNode which the ParseElement gets associated with
	 */
	public void setLogicalNode(LogicalNode logical_node_)
	{
		logicalNode = logical_node_;
	}
	
	/**
	 * Returns the LogicalNode that is associated with this ParseElement
	 * 
	 * @return a LogicalNode object that the ParseElement links to
	 */
	public LogicalNode getLogicalNode()
	{
		if (logicalNode == null) {Main.breakpoint();}
		return logicalNode;
	}
	
	/**
	 * Returns the LogicalBranch associated with the ParseBranch that is the parent branch of the
	 * ParseNode associated with this ParseElement
	 * 
	 * @return LogicalBranch that is the parent of the ParseElement
	 */
	public LogicalBranch getLogicalBranch()
	{
		if(parseNode != null)
			return parseNode.getLogicalParent();
		else	
			return null;
	}
	
	public ParseNode getParseNode()
	{
		return parseNode;
	}
	
	/**
	 * Searches up the ParseElement's associated ParseNode's parent branches for a ParseBranch
	 * of NodeType t.
	 * 
	 * @param type NodeType identifying the type of ParseBranch this method should search for
	 * 
	 * @return ParseBranch of NodeType type that is most closely linked to this ParseElement's ParseNode
	 */
	public ParseBranch getParseBranch_ofType(NodeType type)
	{
		if(parseNode != null)
			return parseNode.getParseBranch_ofType(type);
		else
			return null;
	}
	
	/**
	 * Returns the Prefix object associated with the ParseElement
	 * 
	 * @return Prefix object
	 */
	public Prefix getPrefix()
	{
		return prefix;
	}
	
	/**
	 * Returns the label of the ParseElement
	 * 
	 * @return String containing the label
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Returns the starting index of where the ParseElement is identified in its associated ParseNode
	 * 
	 * @return starting index value of where the ParseElement point within its ParseNode
	 */
	public int getStringStartingIndex()
	{
		return stringStartingIndex;
	}
	
	/**
	 * Returns the ending index of where the ParseElement is identified in its associated ParseNode
	 * 
	 * @return ending index value of where the ParseElement point within its ParseNode
	 */
	public int getStringEndingIndex()
	{
		return stringEndingIndex;
	}

	
	public boolean hasNumeric()
	{
		return flagNumeric;
	}
	
	public ParseContext getParseContext()
	{
		return new ParseContext().set(parseNode, stringStartingIndex, -1);
	}
}
