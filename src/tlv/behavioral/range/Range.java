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

package tlv.behavioral.range;

import tlv.Main;
import tlv.parse.*;
import tlv.parse.identifier.*;
import tlv.utilities.Severity;
import tlv.behavioral.*;

/**
 * Range is used to encapsulate any object that may have a vector associated with it. The class consists of
 * a label and starting and ending bit indexes.
 * 
 * TODO: It might be desirable to make Ranges and Expressions static with no contextual information (like Strings and
 * Identifiers).  In this case derived classes' information would have to be moved elsewhere.  Range constructors
 * would be made private, and parse(..) could find a pre-existing range to return.
 * 
 * @author ypyatnyc
 *
 */
public class Range extends LogicalNode
{
	protected ParseMnemonic identifier;  // TODO: Phase this out, and associate Identifier outside of this class.
	protected String label;              // TODO: Phase this out.
	
	protected Expression startBitIndex;
	protected Expression endBitIndex;
    
	
	public static int numArgs(Range range)
	{
		return (range == null) ? 0 : range.numArgs();
	}
	public int numArgs()
	{
		return (endBitIndex   == null) ? 0 :
			   (startBitIndex == null) ? 1 :
				                         2;
	}
	
	/**
	 * Check the number of expressions in the given range.  ParseContext must be set to point to the range.
	 * @param expr_mask
	 */
	public static void checkRange(Range range, int expr_mask, Severity sev)
	{
		int num_exprs = numArgs(range);
		if ((expr_mask & (1 << num_exprs)) == 0)
		{
			ActiveParseContext.Report(0, sev, "NUM-RANGE-EXP", "Range with " + num_exprs + " bound expression(s) is not legal in this context.");
		}
	}
	
	/**
	 * Parses a range and constructs it.  ParseContext must point to the range text.  If no range is found
	 * and null_allowed, null is returned.
	 * @param branch_
	 * @param node_
	 * @param expr_mask Bits indicate whether corresponding number of expressions is permitted.
	 * @param null_allowed
	 * @return
	 */
	public static Range parse(LogicalBranch branch_, ParseNode node_, int expr_mask, boolean null_allowed)
	{
		ParseContext context = ActiveParseContext.get();
		String str = context.getString();
		Expression lower_expr = null;
		Expression upper_expr = null;
		
		// Check args.
		Main.assertion((expr_mask & 0x7) == expr_mask);
		
		// Check for leading whitespace.  Skip it and report error.
		if ((Main.safeCharAt(str, context.getPosition() + 0) == ' ') &&
			(Main.safeCharAt(str, context.getPosition() + 1) == '[')
		   )
		{
			context.report(0, Severity.FIXED_ERROR, "RANGE_FORMAT", "Range expression improperly preceeded by whitespace.  Whitespace ignored.");
			context.incrementPosition(1);
		}
		
		// Parse '['.
		if (Main.safeCharAt(str, context.getPosition()) != '[')
		{
			// No range.
			if ((expr_mask & 0x1) == 0)
			{
				context.report(0, Severity.ERROR, "RANGE_MISSING", "Range expression required.");
			}
			if (null_allowed)
			{
				return null;
			}
		} else
		{
			context.incrementPosition(1);
		
			// Parse upper expression.
			upper_expr = new Expression(node_, Prefix.END_BIT, context.getPosition());
			context.setPosition(upper_expr.getStringEndingIndex());
		
			// Parse ':'.
			if (str.charAt(context.getPosition()) != ':')
			{
				if ((expr_mask & 0x2) == 0)
				{
					context.report(0, Severity.ERROR, "RANGE_ILL", "Single-value range expression not permitted in this context.");
				}
			} else
			{
				if ((expr_mask & 0x4) == 0)
				{
					context.report(0, Severity.ERROR, "RANGE_ILL", "Full range not permitted in this context.");
				}
				context.incrementPosition(1);
				lower_expr = new Expression(node_, Prefix.START_BIT, context.getPosition());
				context.setPosition(lower_expr.getStringEndingIndex());
			}
			
			// Parse ']'.
			if (Main.safeCharAt(str, context.getPosition()) != ']')
			{
				context.report(0, Severity.FATAL_ERROR, "RANGE_SYNTAX", "Range expression not ended properly.");
			} else
			{
				context.incrementPosition(1);
			}
		}
		return new Range(branch_, str, upper_expr, lower_expr);
	}
	
	public ParseContext getParseContext()
	{
		return (identifier == null) ? new ParseContext().set(null, -1, -1) : identifier.getParseContext();
	}
	
	/**
	 * Constructs a range by parsing.  ParseContext must point to character following identifier, which could be '[' or whitespace if range is implicit.
	 * @param branch_
	 * @param node_
	 * @param upper_required Upper expression is required.  For "[x]", 'x' is considered an upper expression (with no lower).
	 * @param lower_required Lower expression is required.
	 *
	public Range(LogicalBranch branch_, ParseNode node_, boolean upper_required, boolean lower_required)
	{
		super(branch_);
	}*/
	
	/**
	 * Constructs a range object that is not associated with any ParseNode
	 * The parent LogicalBranch, and the range's label String
	 * are specified and set.
	 * 
	 * @param branch_	parent LogicalBranch object
	 * @param label_	string identifying the Range
	 */
	public Range(LogicalBranch branch_, String label_)
	{
		this(branch_, label_, null, null);
	}
	
	/**
	 * Constructs a range object that is not associated with any ParseNode.
	 * The parent LogicalBranch, label String, and starting and ending index Expressions
	 * are specified in the parameters
	 * 
	 * label_[end_bit_index_, start_bit_index]
	 * 
	 * @param	branch_				parent LogicalBranch object
	 * @param	label_				string identifying the Range
	 * @param	end_bit_index_		Expression identifying the upper bound of the Range
	 * @param	start_bit_index_	Expression identifying the lower bound of the Range
	 */
	public Range(LogicalBranch branch_, String label_, Expression end_bit_index_, Expression start_bit_index_)
	{
		super(branch_);
		
		label = label_;
		
		identifier = null;
		startBitIndex = start_bit_index_;
		endBitIndex = end_bit_index_;
	}
	
	/**
	 * Constructs a range object that is associated with a ParseNode contained within the identifier_.
	 * The parent LogicalBranch, identifying ParseMnemonic, and starting and ending index Expressions
	 * are specified in the parameters
	 * 
	 * identifier_[end_bit_index_, start_bit_index]
	 * 
	 * @param	identifier_			ParseMnemonic identifying the label of the Range, as well as
	 * 								its position in the LogicalBranch/LogicalNode tree
	 * @param	end_bit_index_		Expression identifying the upper bound of the Range
	 * @param	start_bit_index_	Expression identifying the lower bound of the Range
	 */
    public Range(ParseMnemonic identifier_,  Expression end_bit_index_, Expression start_bit_index_)
    {
    	//super(identifier_.getLogicalBranch(), identifier_.getPrefix().getType());
    	super(identifier_.getLogicalBranch(), identifier_.getParseNode().getType());
    	
    	identifier = identifier_;
    	label = identifier_.getLabel();
    	
    	endBitIndex = end_bit_index_;
    	
    	if(start_bit_index_ == null)
    		startBitIndex = end_bit_index_;
    	else
    		startBitIndex = start_bit_index_;
    }
    
    /**
	 * Constructs a unity index range object that is associated with a ParseNode contained within the identifier_.
	 * The parent LogicalBranch, identifying ParseMnemonic, and the bit index are specified in the parameters
	 * 
	 * identifier_[bit_index]
	 * 
	 * @param	identifier_			ParseMnemonic identifying the label of the Range, as well as
	 * 								its position in the LogicalBranch/LogicalNode tree
	 * @param	bit_index_			Expression identifying the bit index of the Range
	 */
    public Range(ParseMnemonic identifier_, Expression bit_index_)
    {
    	this(identifier_, bit_index_, bit_index_);
    }
    
    /**
	 * Constructs a range object that is associated with a ParseNode contained within the identifier_.
	 * The parent LogicalBranch and identifying ParseMnemonic are specified in the parameters
	 * 
	 * @param	identifier_			ParseMnemonic identifying the label of the Range, as well as
	 * 								its position in the LogicalBranch/LogicalNode tree
	 */
    public Range(LogicalBranch branch_ /* TODO: unused? */, ParseMnemonic identifier_)
    {
    	this(identifier_, null);
    }
    
    /**
     * Returns the ParseMnemonic object that contains the label of the Range
     * 
     * @return ParseMnemonic object containing Range's label
     */
    public ParseMnemonic getIdentifier()
    {
    	return identifier;
    }
    
    /**
     * Returns the String label of the Range
     * 
     * @return String label of the Range
     */
    public String getLabel()
	{
		return label;
	}
    
    /**
     * Returns the Expression that defines the start index of the range
     * 
     * @return start index Expression object
     */
    public Expression getStartBitIndex()
	{
		return startBitIndex;
	}
    
    /**
     * Returns the Expression that defines the end index of the range
     * 
     * @return end index Expression object
     */
    public Expression getEndBitIndex()
	{
		return endBitIndex;
	}
    
    /**
     * Returns the full String representation of the Range.
     * Examples of possible produced Strings
     * 
     * startBitIndex == null
     * endBitIndex == null
     * ""
     * 
     * startBitIndex == 0
     * endBitIndex == 0
     * ""
     * 
     * startBitIndex == 3
     * endBitIndex == 3
     * "[3]"
     * 
     * startBitIndex == 0
     * endBitIndex == 3
     * "[3:0]"
     * 
     * @return String representation of the range defined within this object
     */
    public String toStringUsage()
    {
    	if((startBitIndex == null && endBitIndex == null)
    			|| ( (startBitIndex.getLabel()).equals(endBitIndex.getLabel()) && (startBitIndex.getLabel()).equals("")))
    	{
    		return "";
    	}
    	else if(startBitIndex.getLabel().equals(endBitIndex.getLabel()))
    		return "[" + startBitIndex.getLabel() + "]";
    	else
    		return "[" + endBitIndex.getLabel() + ":" + startBitIndex.getLabel() + "]";
    }
    
    /**
     * Returns the full String representation of the Range.
     * Examples of possible produced Strings
     * 
     * startBitIndex == null
     * endBitIndex == null
     * ""
     * 
     * startBitIndex == 0
     * endBitIndex == 0
     * ""
     * 
     * startBitIndex == 3
     * endBitIndex == 3
     * "[3:3]"
     * 
     * startBitIndex == 0
     * endBitIndex == 3
     * "[3:0]"
     * 
     * @return String representation of the range defined within this object
     */
    public String toString()
    {
    	if(startBitIndex == null && endBitIndex == null)
    	{
    		return "";
    	}
    	else if(startBitIndex == null)
    	{
    		return "[" + endBitIndex.getLabel() + "]";
    	}
    	else if(startBitIndex.getLabel().equals(endBitIndex.getLabel()) && (startBitIndex.getLabel()).equals(""))
    	{
    		return "";
    	}
    	else
    		return "[" + endBitIndex.getLabel() + ":" + startBitIndex.getLabel() + "]";
    }
    
    
    //prints out a bit range from two expressions
    public static String ToString(Expression endBitIndex, Expression startBitIndex)
    {
    	if(startBitIndex == null && endBitIndex == null)
    	{
    		return "";
    	}
    	else if(startBitIndex == null)
    		return "[" + endBitIndex.getLabel() + "]";
    	else
    		return "[" + endBitIndex.getLabel() + ":" + startBitIndex.getLabel() + "]";
    }
}