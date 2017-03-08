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

import tlv.Main;
import tlv.behavioral.*;
import tlv.parse.identifier.ParseElement;
import tlv.utilities.Severity;

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
public class ParseNode 
{
	private NodeType type;
	
	//parent branch
	protected ParseBranch parent;
	
	protected int indentation;
	protected int numLines_self;
	
	protected String string;  // TODO: Get rid of this?  Turn it into a method.
	protected int endOfStringIndex;
	
	protected String stringWithoutIndentation;  // TODO: Phase out, or move into certain derived classes.
	protected String stringSV;
	protected String sv_end_scope_str = null;  // The SV string that ends this scope.
	

	protected int firstCommentIndex;

	
	/**
	 *Constructs an empty, undefined ParseNode 
	 */
	public ParseNode()
	{
		
		type = NodeType.UNDEFINED;
		parent = null;
		
		indentation = 0;
		numLines_self = 0;
		
		firstCommentIndex = -1;
		endOfStringIndex = -1;
		
		string = "";
		stringWithoutIndentation = "";
		stringSV = "";
		//processIndentation(1);
		
	}
	
	/**
	 * Constructs an undefined ParseNode, and sets its string to the parameter line.
	 * 
	 * @param line  A string from an TLV for which a new ParseBranch is create
	 */
	public ParseNode(String line)
	{
		
		type = NodeType.UNDEFINED;
		parent = null;
		
		indentation = 0;
		numLines_self = 1;
		
		firstCommentIndex = -1;
		endOfStringIndex = -1;
		
		string = line;
		stringSV = "";
		//processIndentation(1);
	}
	
	/**
	 * Constructs a clone ParseNode from another source_node ParseNode.
	 * 
	 * @param	source_node		this existing parse node is passed into the constructor and all of its
	 * attributes are copied over to the newly constructed ParseNode. 
	 */
	public ParseNode(ParseNode source_node)
	{
		type = source_node.getType();
		parent = source_node.getParent();
		
		indentation = source_node.getIndentation();
		numLines_self = source_node.getNumLines_Self();
		
		firstCommentIndex = source_node.getFirstCommentIndex();
		endOfStringIndex = source_node.getEndOfStringIndex();
		
		string = source_node.string;
		stringSV = source_node.stringSV;
		stringWithoutIndentation = source_node.stringWithoutIndentation;
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
	public ParseNode(ParseBranch parent_branch, ParseNode source_node)
	{
		
		this(source_node);
		
		parent = parent_branch;
	}
	
	/**
	 * This method is to be used by classes extended from ParseNode to print a new line of text
	 * in the line for line SV file
	 * 
	 * @param line  is the string that gets printed
	 */
	public void printSV(String line)
	{
		SourceFile temp_source_file = (SourceFile)getParseBranch_ofType(NodeType.FILE);
		temp_source_file.nextSVline(line);
	}
	/**
	 * This method is to be used by classes extended from ParseNode to print a new line of text
	 * into declarations SV file
	 * 
	 * @param line  is the string that gets printed
	 */
	public void printlnSVdeclaration(String line)
	{
		SourceFile temp_source_file = (SourceFile)getParseBranch_ofType(NodeType.FILE);
		temp_source_file.printlnSVdeclaration(line);
	}
	
	/**
	 * TODO: Kill this.
	   * This method is to be used by classes extended from ParseNode to print a new line of text
	   * into errors and warnings TXT file
	   * 
	   * @param line is the string that gets printed
	   */
	  public void printError(String message)
	  {
		  SourceFile temp_source_file = (SourceFile)getParseBranch_ofType(NodeType.FILE);
		  if (temp_source_file != null) {
			  FileLine file_line = getFileLine();
			  temp_source_file.printError(message, file_line);
		  } else {
			  System.err.println("Bug: Attempted to report an error with no associated source file.  Error message is\n" + message);  // Use NewLines?
		  }
	  }

	
	/**
	 * set the NodeType identifier of the ParseObject
	 *
	 * @param t  contains the NodeType that the this ParseNode is set to. 
	 */
	protected void setType(NodeType t)
	{
		type = t;
	}
	
	/**
	 * Returns the NodeType identifying the ParseNode object
	 * 
	 * @return NodeType that this ParseNode is set to
	 */
	public NodeType getType()
	{
		return type;
	}
	
	/**
	 * Returns the string which is contained within the ParseNode. 
	 * 
	 * @return the string that is contained within the ParseNode object
	 */
	public String getString()
	{
		return string;
	}
	
	/**
	 * Returns the indentation-less string which is contained within the ParseNode. 
	 * 
	 * @return the indentation-less string that is contained within the ParseNode object
	 */
	public String getStringWithoutIndentation()
	{
		return stringWithoutIndentation;
	}
	
	
	//Getters and setters
	protected void setParent(ParseBranch b)
	{
		parent = b;
	}
	
	
	public ParseBranch getParent()
	{
		return parent;
	}
	
	protected void setIndentation(int indent)
	{
		indentation = indent;
	}
	public int getIndentation()
	{
		return indentation;
	}
	
	protected void setNumLines_Self(int n)
	{
		numLines_self = n;
	}
	public int getNumLines_Self()
	{
		return numLines_self;
	}
	
	public void setFirstCommentIndex(int index_)
	{
		firstCommentIndex = index_;
	}
	public int getFirstCommentIndex()
	{
		return firstCommentIndex;
	}
	
	public void setEndOfStringIndex(int index_)
	{
		endOfStringIndex = index_;
	}
	public int getEndOfStringIndex()
	{
		return endOfStringIndex;
	}
	
	/**
	 * This method steps through the ParseContext to process indentation, it sets indentation and stringWithoutIndentation.
	 * Note that it is static and operates on the node of the ParseContext.
	 * 
	 * @param context The parse context.
	 * @param SPACES_PER_TAB_RATIO  this integer gives the tab, '\t' ,character an indentation value.
	 */
	public static void processIndentation(ParseContext context, int SPACES_PER_TAB_RATIO)
	{
		ParseNode node = context.getParseNode();
		//check for indentation
		while (!context.doneString())
		{
			//counts the number of space characters at the beginning on the string.
			//if the first character is '!' warning character, it is also parsed through
			//as if its a space character
			char ch = context.getCurrentChar();
			if (ch == ' ' ||
				(ch == '!' && (context.getPosition() == 0)))
			{
				if(ch == '!')
					node.string = node.string.replaceFirst("!", " ");
				node.indentation++;
			}
			else
			{
				break;
			}
			
			// Next char.
			context.incrementPosition(1);
		}
		
		//removes all the counted spaces to generate a stringWithoutIndentation
		node.stringWithoutIndentation = node.string.substring(context.getPosition());
	}
	
	/**
	 * Parses this ParseNode and interprets its comment context.
	 * 
	 * @param isWithinCommentContext boolean value indicating whether the ParseNode starts within a comment block
	 * 
	 * @return boolean value identifying if the line remains in comment context at end of the line
	 */
	public boolean scan_for_comments(boolean isWithinCommentContext){
		return scan_for_comments(string, isWithinCommentContext, 0);
	}
	
	/**
	 * Parses a line and interprets its comment context.
	 * 
	 * @param line String containing the line being parsed for comment context
	 * @param isWithinCommentContext boolean value indicating whether the ParseNode starts within a comment block
	 * 
	 * @return boolean value identifying if the line remains in comment context at end of the line
	 */
	public boolean scan_for_comments(String line, boolean isWithinCommentContext, int index){
		
		if(isWithinCommentContext){
			// checks for closing of a comment block, "*/"
			if(line.indexOf("*/") >= 0){
				return scan_for_comments(line.substring(line.indexOf("*/")+2), false, index + line.indexOf("*/")+2);
			}
			else{
				return true;
			}
		}
		else{
			// checks for opening of a comment block, "/*"
			if(line.indexOf("/*") >= 0){
				// checks for opening of a comment line, "//"
				if(line.indexOf("//") >= 0){
					
					// opening of a comment block, "/*", comments out opening of comment line, "//"
					if(line.indexOf("/*") < line.indexOf("//")){
						return scan_for_comments(line.substring(line.indexOf("/*")+2), true, index + line.indexOf("/*")+2);
					}
					// Opening of a comment line, "//", comments out opening of a comment block, "/*"
					else{
						return false;
					}
				}
				else{
					// updates endOfStringIndex only if there exists substantial code prior to the opening comment section
					if(getIndexOfLastNonWhiteSpace(line, line.indexOf("/*")-1) > 0)
						endOfStringIndex = index + getIndexOfLastNonWhiteSpace(line, line.indexOf("/*")-1);
					
					return scan_for_comments(line.substring(line.indexOf("/*")+2), true, index + line.indexOf("/*")+2);
				}
			}
			else{
				// checks for opening of a comment line, "//"
				if(line.indexOf("//") >= 0){
					if(getIndexOfLastNonWhiteSpace(line, line.indexOf("//")-1) > 0)
						endOfStringIndex = index + getIndexOfLastNonWhiteSpace(line, line.indexOf("//")-1);
				}else if(getIndexOfLastNonWhiteSpace(line, line.length()-1)> 0){
					endOfStringIndex = index + getIndexOfLastNonWhiteSpace(line, line.length()-1);
				}
				return false;
			}
		}
	}
	
	private int getIndexOfLastNonWhiteSpace(String line, int index)
	{
		int i = index;
		while(i > 0){
			if(	line.charAt(i) != ' ' && 
				line.charAt(i) != '\n' &&
				line.charAt(i) != '\t'){
				return i;
			}
			else
				i--;
		}
		return i;
	}
	
	/**
	 * Appends the properties from another ParseNode into this ParseNode. The properties that are appended
	 * include string, stringWithoutIndentation and the number of lines.
	 * 
	 * @param node another ParseNode object whose properties are appended to this ParseNode object. 
	 */
	public void appendNode(ParseNode node)
	{
		if(node.getEndOfStringIndex() >= 0){
			this.endOfStringIndex = this.string.length() + 1 + node.getEndOfStringIndex();
		}
		
		this.string += "\n" + node.string;
		this.stringWithoutIndentation += "\n" + node.stringWithoutIndentation;
		this.numLines_self += node.numLines_self;
	}
	
	public String getLastLine()
	{
		String sub_string = stringWithoutIndentation;
		
		while(sub_string.contains("\n"))
		{
			sub_string = sub_string.substring(sub_string.indexOf("\n")+1);
		}
		return sub_string;
	}
	
	
	//getParseTLV looks up the branch chain in attempt to find a branch of type
	//ParseTLV. Upon finding it, the ParseTLV branch is return. If no ParseTLV branch
	//exists in the branch chain, the method returns null
	/**
	 * Searches the parse ancestry above this node in attempt to find a branch of type t, and returns it if it is found.
	 * 
	 * @param t Identifies the type of node to be looking for.
	 * 
	 * @return most immediate ParseBranch of NodeType t if one is found. If a ParseBranch of that
	 * type does not exist in the parseNode ancestry, then this method returns null
	 */
	public ParseBranch getParseBranch_ofType(NodeType t)
	{
		return (parent == null) ? null : (parent.getType() == t) ? parent : parent.getParseBranch_ofType(t);
	}
	
	/**
	 * Return the LogicalBranch to which this node belongs.  In other words, search above this parse node in its ancestry for a corresponding logical node.
	 * @return A LogicalBranch that corresponds to the closest possible parse branch above this parse node in its ancestry.
	 */
	public LogicalBranch getLogicalParent()
	{
		return (parent == null) ? null : parent.getRelatedLogicalBranch();
	}
	
	/**
	 * Searches up the parse ancestry in attempt to find a branch of type t, and then returns its LogicalNode.
	 * 
	 * @param t Identifies the type of node to be looking for.
	 * 
	 * @return most immediate LogicalBranch of NodeType t if one is found. If a LogicalBranch of that
	 * type does not exist in the parseNode tree, then this method returns null
	 */
	public LogicalBranch getLogicalBranch_ofType(NodeType t)
	{
		// TODO: Want to do the search in the logical side as below, but type is not always defined there.  Is type something we're keeping?  Clean up,
		//       and replace the code below with the following commented code.  That will eliminate the last use of getOldIdentifier().
		//LogicalBranch logical_parent = getLogicalParent();
		//LogicalBranch ret = (logical_parent == null) ? null : logical_parent.getSelfOrAncestorOfType(t);

		ParseBranch temp_branch = getParseBranch_ofType(t);
		
		LogicalBranch ret2 = null;
		if(temp_branch != null)
			if(temp_branch.getOldIdentifier() != null)
			{
				ret2 = (LogicalBranch)temp_branch.getOldIdentifier().getLogicalNode();
			}
		//if (ret != ret2) {Main.breakpoint();}
		
		return ret2;
	}
	
	// Return value for getLinesPrior.  Encapsulates a line of a file.
	public class FileLine
	{
		public ParseSource parse_source;  // The ParseSource defining the source of the line.
		public int line_num;
		
		FileLine(ParseSource _parse_source, int _line_num)
		{
			parse_source = _parse_source;
			line_num = _line_num;
		}
		
		public void incrLine(int incr)
		{
			line_num += incr;
		}
	}
	

	/**
	 * @return The source file and line number of this node.
	 * 
	 * It evaluates by adding its parents' line count to the number of lines from its parent to itself, until the parent is a ParseSource.
	 */
	public FileLine getFileLine()
	{
		if (parent == null) {
			ActiveParseContext.Report(0, Severity.BUG, "BUG", "parent is null.");
		}
		
		ParseSource parse_source = null;  // The containing branch that defines the source file.
		
		// Start w/ parent count.
		// first itself
		int cnt = parent.getNumLines_Self();
		// then above
		if (parent instanceof ParseSource)
		{
			// This is a ParseSource which provides a starting source line.
			parse_source = (ParseSource)parent;
			cnt += parse_source.start_line_num;
		}
		else
		{
			// Recurse to find parents line number.
			FileLine start_file_line = parent.getFileLine();
			parse_source = start_file_line.parse_source;
			cnt += start_file_line.line_num;
		}
		
		// Add parent's children's counts, up to this node.
		cnt += parent.countChildLines(this);
				
		return new FileLine(parse_source, cnt);
	}
	
	
	//
	// Methods for SV generation.
	//
	
	/**
	 * End begin/end SV scope.  This adds an 'end' to last_sv_line to maintain line-for-line alignment, or if alignment is not required, a new line is created.
	 */
	public String endSVscope(String indentation, String last_sv_line, String end_str)
	{
		if (end_str == null) return last_sv_line;
		
		if (Main.command_line_options.bestSv())
		{
			last_sv_line += "\n" + indentation + end_str;
		} else
		{
			int last_line_begin = last_sv_line.lastIndexOf('\n') + 1;
			// Insert end_str at the end of last_sv_line, but before '//'-comment.  TODO: '//' inside '/* */' won't work right.
			int insert_pos = last_sv_line.indexOf("//", last_line_begin);
			
			if (insert_pos >= 0)
			{
				// '//'-comment exists.
				// Look back over whitespace to find an appropriate insertion point.
				// TODO: We just stick it here for now.
				last_sv_line = last_sv_line.substring(0, insert_pos) + " " + end_str + last_sv_line.substring(insert_pos);
			} else
			{
				// No '//'
				last_sv_line = (last_sv_line.length() == 0) ? end_str : (last_sv_line + " " + end_str);
			}
		}
		return last_sv_line;
	}


	// This is called by ParseSV and ParseTLV for the case where they are
	// on the second line of the file (line after version line), in which
	// case a 'include must be generated.
	protected void insertInclude()
	{
		// Generate `include if this is the first line of the file
		// after the version line.
		if (parent.parseNodes.size() == 1)
		{
		    // This appears to be the first node after the version line.
		    stringSV = "`include \"tlv_" + Main.command_line_options.project() + ".vh\"  " + stringSV;
		}
	}
}
