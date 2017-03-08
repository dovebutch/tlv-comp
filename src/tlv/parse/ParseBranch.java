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

import java.util.Vector;

import tlv.Main;
import tlv.behavioral.LogicalBranch;
import tlv.parse.identifier.*;
import tlv.utilities.Severity;

/**
 * The ParseBranch class has a very similar role as ParseNode class. The biggest difference is that it has a Vector containing ParseNode objects
 * that are lower in hierarchy. Another major difference is that ParseBranch contains a single ParseElement identifier to link it to only one LogicalNode.
 * This means that all classes that extend ParseBranch also only have a single associated LogicalBranch. These classes are ParseHier, ParsePipeline,
 * ParseWhen and ParseStage. On the contrary, ParseAssignment extends ParseNode and has many different LogicalNodes associated with it(assignedBitRanges,
 * and UsedBitRanges)
 * 
 * @author ypyatnyc
 *
 */
public class ParseBranch extends ParseIdentifierNode 
{ 
	protected ParseElement old_identifier;  // TODO: Phase out in favor of identifier (in ParseIdentifierNode).
	protected LogicalBranch logical_branch;  // A logical branch that directly corresponds to this parse branch, or null.
	
	//ParseBranch's children
	protected Vector <ParseNode> parseNodes;
	private int numLines_parseNodes = -1;
	
	/**
	 *Constructs an empty, undefined ParseBranch 
	 */
	public ParseBranch()
	{
		super();
		
		logical_branch = null;
		parseNodes = new Vector<ParseNode>(0,0);
		numLines_parseNodes = 0;
	}
	
	/**
	 * Constructs an undefined ParseBranch, and sets its string to the parameter line.
	 * 
	 * @param string - A string from an TLV for which a new ParseBranch is create
	 */
	public ParseBranch(String string)
	{
		super(string);
		
		logical_branch = null;
		parseNodes = new Vector<ParseNode>(0,0);
		numLines_parseNodes = 0;
	}
	
	/**
	 * Constructs a clone ParseBranch from another source_node ParseNode.
	 * 
	 * @param source_node  this existing parse node is passed into the constructor and all of its
	 * attributes are copied over to the newly constructed ParseBranch. 
	 */
	public ParseBranch(ParseNode source_node)
	{
		super(source_node);
		
		logical_branch = null;
		parseNodes = new Vector<ParseNode>(0,0);
		numLines_parseNodes = 0;
	}
	

	/**
     * Constructs a cloned ParseBranch from existing source_node and links it with another ParseBranch parent_branch object.
     * Parses based on ActiveParseContext.
     *
	 * @param	parent_branch	the newly constructed ParseBranch will point to this ParseBranch as its parent branch.
	 * @param	source_node		this existing parse node is passed into the constructor and all of its 
	 * attributes are copied over to the newly constructed ParseBranch. 
	 * 
	 * @see	ParseBranch
     */
	public ParseBranch(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		
		logical_branch = null;
		parseNodes = new Vector<ParseNode>(0,0);
		numLines_parseNodes = 0;
	}
	
	// A temporary varient of the above constructor, that does not create the identifier.  It is used by ParsePhase.
	// TODO: Enable ParsePhase to create an identifier and phase this out.
	public ParseBranch(ParseBranch parent_branch, ParseNode source_node, boolean dummy)
	{
		super(parent_branch, source_node, dummy);
		
		logical_branch = null;
		parseNodes = new Vector<ParseNode>(0,0);
		numLines_parseNodes = 0;
	}
	
	/**
	 * @return The directly corresponding LogicalBranch, or null.
	 */
	public LogicalBranch getCorrespondingLogicalBranch()
	{
		return logical_branch;
	}
	
	public void setCorrespondingLogicalBranch(LogicalBranch b)
	{
		logical_branch = b;
	}
	
	/**
	 * Return the LogicalBranch that this parse node contributes to.
	 * @return The corresponding LogicalBranch or closest in this parse branch's ancestry.
	 */
	public LogicalBranch getRelatedLogicalBranch()
	{
		return (logical_branch == null) ? ((parent == null) ? null : parent.getRelatedLogicalBranch()) : logical_branch;
	}

	
	protected void setOldIdentifier(ParseElement identifier_)
	{
		old_identifier = identifier_;
	}
	public ParseElement getOldIdentifier()
	{
		return old_identifier;
	}
	
	/**
	 * Resets identifier to the given identifier_ object, which must be equivalent to the existing one.
	 * This can be used to ensure that all ParseBranches with the same LogicalBranch have the same identifier.
	 * TODO: This class should probably have a LogicalBranch member.  This function can be moved to it's
	 *       setter.
	 * @param identifier_
	 */
	public void resetIdentifier(Identifier identifier_)
	{
		Main.assertion(identifier_.equals(identifier));
		if (identifier_ != identifier)
		{
			// TODO: Restructuring should eliminate the need for this.  Can put a breakpoint here to ensure this doesn't happen, then stop calling this function.
			identifier = identifier_;
		}
	}
	
	/**
	 * Count lines of code from this node's file in this node's children, optionally stopping at the given node.
	 * This is called with a null parameter after parsing all nodes, and the result is recorded.  Subsequently,
	 * this is called for one of it's children to generate a partial count.
	 */
	public int countChildLines (ParseNode to_node)
	{
		int cnt = 0;
		// Add children's counts, up to to_node.
		for(int i = 0; i < parseNodes.size(); i++)
		{
			ParseNode child = parseNodes.get(i);
			
			// Until we find this node as a child.
			if (child == to_node)
			{
				return cnt;
			}
			
			// Add child count.
			cnt += child.getNumLines_Self();
			// And it's children (excluding ParseSources, which were from a different source file).
			if((child instanceof ParseBranch) && !(child instanceof ParseSource))
			{
				cnt += ((ParseBranch)child).getNumLines_ParseNodes();
			}
		}
		
		if (to_node != null)
		{
			ActiveParseContext.Report(0, Severity.BUG, "BUG", "Failed to find given node as a child.");
		}

		numLines_parseNodes = cnt;
		return cnt;
	}
	
	
	/**
	 * This method returns a value indicating the number of lines that there are within the children parseNodes of this ParseBranch
	 * 
	 * @return number of lines of all the parseNodes within this ParseBranch
	 */
	public int getNumLines_ParseNodes()
	{
		if (numLines_parseNodes < 0) {ActiveParseContext.Report(0, Severity.BUG, "BUG", "getNumLines_ParseNodes() called before setNumLines_ParseNodes().");}
		return numLines_parseNodes;
	}
	
	
	/**
	 * This methods adds a ParseNode obects to ParseBranch's parseNodes Vector. 
	 * 
	 * @param node is the ParseNode that gets added to the parseNodes Vector
	 */
	public void addNode(ParseNode node)
	{
		parseNodes.add(node);
		numLines_parseNodes += node.getNumLines_Self();
	}
	
	
	/**
	 * This method returns the last line number for the ParseBranch. If the ParseBranch contains ParseNodes under it, this value
	 * correspond to the line number of the last ParseNode in the ParseBranch's parseNodes Vector.
	 * 
	 * @return an integer value indicating on which line the ParseBranch comes out of its scope.
	 */
	public FileLine getEndingLineNumber()
	{
		FileLine line = parseNodes.lastElement().getFileLine();
		line.incrLine(parseNodes.lastElement().getNumLines_Self() - 1 +
		              ((parseNodes.lastElement() instanceof ParseBranch) ? ((ParseBranch)parseNodes.lastElement()).getNumLines_ParseNodes() : 0)
				     );
		return line;
	}
	
	
	/**
	 * Get an attribute of this branch.  TODO: Attributes aren't well thought-through yet.
	 * @param name The mixed-case identifier name to find.
	 */
	public ParseIdentifierNode getAttribute(String name)
	{
		// TODO: Probably want attributes of various flavors tucked away in appropriate data structures.
		// For now, just search through children.
		for(ParseNode child: parseNodes)
		{
			if (child.getType() == NodeType.ATTRIBUTE)
			{
				ParseIdentifierNode node = (ParseIdentifierNode)child;
				Identifier ident = node.getIdentifier();
				if (ident.getName().equals(name))
					return node;
			}
		}
		return null;
	}
	
	
	/**
	 * This method is used to access  any of the ParseNodes nested inside of the ParseBranch by their line number
	 *  
	 * @param lineNumber - the line number from which to retrieve the ParseNode(this value is relative to the starting line of the parse branch)
	 * @return returns ParseNode thats contained within the ParseBranch
	 */
	public ParseNode getParseNode(int lineNumber)
	{
		ParseBranch n = this;
		
		while(n.getParent() != null)
		{
			n = n.getParent();
		}
		
		return n.getParseNode(lineNumber, 0);
	}
	
	/**
	 * This is a helper method for getParseNode(int lineNumber method). 
	 * It is used for recursively finding a ParseNode within ParseBranch
	 * from by line number.
	 * 
	 * @see ParseBranch#getParseNode(int lineNumber)
	 */
	private ParseNode getParseNode(int lineNumber, int lineSum)
	{
		for(int i = 0; i < this.parseNodes.size(); i++)
		{
			lineSum += this.parseNodes.get(i).getNumLines_Self();
			
			if(this.parseNodes.get(i) instanceof ParseBranch)
			{
				if(lineNumber <= lineSum)
				{
					return this.parseNodes.get(i);
				}
				else if(lineNumber <= lineSum + ((ParseBranch)this.parseNodes.get(i)).getNumLines_ParseNodes())
				{
					return ((ParseBranch)this.parseNodes.get(i)).getParseNode(lineNumber, lineSum);
				}
				else
					lineSum += ((ParseBranch)this.parseNodes.get(i)).getNumLines_ParseNodes();
			}
			else
			{
				if(lineNumber <= lineSum)
				{
					return this.parseNodes.get(i);
				}
			}
		}
		return null;
	}

	/**
	 * This method is used to produce the ParseElement identifier for the ParseBranch.
	 * 
	 * @return The ParseMnemonic ParseElement identifier which gets extracted from the ParseBranch's string
	 */
	public ParseMnemonic produceMnemonic(Prefix prefix)
	{
		if (old_identifier == null)
		{
			for(int i = 0; i < string.length(); i++)
			{
				if(string.charAt(i) == prefix.getChar())
				{
					old_identifier = new ParseMnemonic(this, prefix, i+1);
					break;
				}
			}
		}
		
		return (ParseMnemonic)old_identifier;
	}
}
