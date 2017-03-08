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

import tlv.parse.*;
import tlv.parse.identifier.*;
import tlv.behavioral.*;


/**
 * BitRange class extends Range class, by adding a link to a PipeSignal with which the Range is associated.
 * BitRanges are typically associated with identifiers that point to Assignment and ParseWhen objects.
 * BitRanges in Assignments can be for uses or assignments, but this class does not record which it is.
 * Assignments and uses are held in different data structures in a Assignments, so the distinction is external.
 * 
 * @author ypyatnyc
 */
public class BitRange extends Range
{
	//The PipeSignal that the BitRange is part of
    protected PipeSignal pipeSignal;
    
    /**
     * Constructs a BitRange object that is not associated with any ParseNode.
	 * The parent LogicalBranch, PipeSignal, starting and ending bit index Expressions
	 * are specified in the parameters
	 *
	 * @param	branch_ 			The LogicalBranch under which BitRange is positioned
     * @param	signal_				The PipeSignal associated with this BitRange
     * @param	end_bit_index_		Expression identifying the upper bound of the Range
	 * @param	start_bit_index_	Expression identifying the lower bound of the Range
     */
    public BitRange(LogicalBranch branch_, PipeSignal signal_,  Expression end_bit_index_, Expression start_bit_index_)
    {
    	super(branch_, signal_.getLabel(), end_bit_index_, start_bit_index_);
    	
    	pipeSignal = signal_;
    }
    
    /**
	 * Constructs a BitRange object that is associated with a ParseNode contained within the identifier_.
	 * The PipeSignal associated with this BitRange is retrieved from LogicalScope object gotten from
	 * identifier_. If a PipeSignal for this BitRange does not exist, this constructor calls on a method that
	 * may construct an instance of a PipeSignal for this BitRange.
	 * 
	 * 
	 * @param	identifier_			ParseMnemonic identifying the label of the BitRange, as well as
	 * 								its position in the LogicalBranch/LogicalNode tree
	 * @param	end_bit_index_		Expression identifying the upper bound of the BitRange
	 * @param	start_bit_index_	Expression identifying the lower bound of the BitRange
	 */
/*    public BitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier,  Expression end_bit_index_, Expression start_bit_index_)
    {
    	super(identifier_, end_bit_index_, start_bit_index_);
    	
    	//- LogicalBehScope scope = parent.getSelfOrAncestorBehScope();
    	pipeSignal = sig_scope.addSignal(identifier);
    }*/
    
    /**
	 * Constructs a BitRange object that is associated with a ParseNode contained within the identifier_ parameter.
	 * The PipeSignal associated with this BitRange is retrieved from LogicalScope object gotten from
	 * identifier_. If a PipeSignal for this BitRange does not exist, this constructor calls on a method that
	 * may construct an instance of a PipeSignal for this BitRange.
	 * The null-ness of the args reflects the explicit existence of the index in the source code.  So if a single-bit
	 * index is provided this is he end_bit_index_, and start_bit_index_ is null.
	 * Non existent ranges in source code correspond to both args null.  In this case, there is a difference in the treatment
	 * of assignments and uses.  Assignments declare the range, and a missing range is interpreted as [0:0], where this constructor
	 * provides explicit expressions.  For uses, this indicates the full width, and expressions are left null.
	 * 
	 * @param	identifier_			ParseMnemonic identifying the label of the AssignedBitRange, as well as
	 * 								its position in the LogicalBranch/LogicalNode tree
	 * @param	end_bit_index_		Expression identifying the upper bound of the AssignedBitRange
	 * @param	start_bit_index_	Expression identifying the lower bound of the AssignedBitRange
	 * @param   assigned            This is an assigned bit range, and explicit expressions must be generated if not provided.
	 */
	public BitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier, Expression end_bit_index_, Expression start_bit_index_, boolean assigned)
	{
    	super(identifier_, end_bit_index_, start_bit_index_);
    	
    	pipeSignal = sig_scope.addSignal(identifier);

		if(assigned && (end_bit_index_ == null) && (start_bit_index_ == null))
		{
			endBitIndex 	= new Expression(identifier_.getParseNode());
			startBitIndex 	= new Expression(identifier_.getParseNode());
		}
	}

    
    /**
	 * Constructs an undefined BitRange object that is associated with a ParseNode contained within the identifier_.
	 * The PipeSignal associated with this BitRange is retrieved from LogicalPipeline object gotten from
	 * identifier_. If a PipeSignal for this BitRange does not exist, this constructor calls on a method that
	 * may construct an instance of a PipeSignal for this object.
	 * 
	 * 
	 * @param	identifier_			ParseMnemonic identifying the label of the BitRange, as well as
	 * 								its position in the LogicalBranch/LogicalNode tree
	 */
    public BitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier)
    {
    	this(sig_scope, identifier_, identifier, null, null, false);
    }
    
    /**
     * Returns the PipeSignal object associated with the BitRange
     * 
     * @return PipeSignal which this BitRange is part of
     */
    public PipeSignal getPipeSignal()
    {
    	return pipeSignal;
    }
    
	/**
	 * Returns the value of the LogicalStage under which this AssignedBitRange is scoped.
	 * Note that the notion of alignment is added by UsedBitRange, so UsedBitRange() overrides
	 * this method to factor in alignment.
	 * If either LogicStage scope was not found or cannot be evaluated this method will 
	 * return -1
	 * 
	 * @return integer value of the LogicalStage scope
	 */
	public int getStageValue()
	{
		LogicalStage temp_logical_stage = getLogicalStage();
		
		if(temp_logical_stage != null)
		{
			if(temp_logical_stage.isEvaluated())
				return temp_logical_stage.getValue();
		}
		
		return -1;
	}
	
	/**
	 * Returns the LogicalStage under which the AssignedBitRange is scoped.
	 * Note, that the alignment of the signal reference is not factored into this.
	 * 
	 * @return LogicalStage scope of the signal
	 */
	public LogicalStage getLogicalStage()
    {
    	if(identifier != null)
    		return (LogicalStage)identifier.getParseNode().getLogicalBranch_ofType(NodeType.STAGE);
    	else
    		return null;
    }
	

	
	/**
	 * @return string representing this UsedBitRange with appropriate indexing scope.
	 */
	/*
	public String svSignalReference()
	{
		return pipeSignal.svSignalReference(getStageValue());
	}
	*/
	
	/**
	 * @return The corresponding SV signal name.
	 */
	public String svSignalName()
	{
		return pipeSignal.svSignalName(getStageValue());
	}
	
	// Strips any number of "[*]"s from the end of index_str.  Used in translating specified SV index string as indicated
	// by TLV reference scope to the actual scope to use for the corresponding SV signal in an SV assignment. 
	private String stripWildcardIndices(String index_str)
	{
		return index_str.endsWith("[*]") ? stripWildcardIndices(index_str.substring(0, index_str.length()-3))
				                         : index_str;
	}
	
	/**
	 * @param index_str Index string for this SV signal.
	 * @param wildcards_ok If true, [*]s are stripped from the end of index_str.
	 * @return SV string representation of this range with the given SV indexing string.
	 */
	public String svSignalRef(String index_str, boolean wildcards_ok)
	{
		return  // signal name
				svSignalName() +
				// index
				(wildcards_ok ? stripWildcardIndices(index_str) : index_str) +
				// bit range
				toStringUsage();
	}

	
    /**
     * Returns the lowest LogicalWhen scope for the BitRange.
     * null is returned if LogicalWhen cannot be found in BitRange's scope
     * 
     * @return most immediate LogicalWhen scope
     */
    public LogicalWhen getLogicalWhen()
    {
    	if(identifier != null)
    		return (LogicalWhen)identifier.getParseNode().getLogicalBranch_ofType(NodeType.WHEN);
    	else
    		return null;
    }
    
	/**
	 * Get the associated Assignment.
	 * 
	 * @return Associated Assignment.
	 */
	public Assignment getAssignment()
	{
		return ((Assignment) identifier.getParseNode());
	}
	

    /**
     * Returns the highest LogicalWhen scope for the BitRange
     * This is useful in instances when there is nested LogicalWhens in the
     * BitRange's scope
     * 
     * @return highest LogicalWhen scope
     */
    public LogicalWhen getLogicalWhen_top()
    {
    	if(identifier != null)
    	{
    		LogicalWhen temp_when = (LogicalWhen)identifier.getParseNode().getLogicalBranch_ofType(NodeType.WHEN);
    	
    		while(temp_when.getBranchWhen() != null)
    			temp_when = temp_when.getBranchWhen();
    	
    		return temp_when;
    	}
    	else
    		return null;
    }
    
    /**
     * Returns the LogicalPipeline object under which the BitRange is scoped
     * 
     * @return LogicalPipeline scope for the BitRange
     */
    public LogicalPipeline getLogicalPipeline()
    {
    	if(identifier != null)
    		return (LogicalPipeline)identifier.getParseNode().getLogicalBranch_ofType(NodeType.PIPELINE);
    	else
    		return null;
    }
    
    /**
     * Returns the start bit index of BitRange
     * If the start bit index Expression of the BitRange is undefined,
     * this method returns the lowest bit index of its PipeSignal
     * 
     * @return Expression containing the BitRange's start bit index
     */
    public Expression getStartBitIndex()
	{
		if(startBitIndex != null)
			return startBitIndex;
		else
			return pipeSignal.getIndexLowest();
	}
    
    /**
     * Returns the end bit index of BitRange
     * If the end bit index Expression of the BitRange is undefined,
     * this method returns the highest bit index of its PipeSignal
     * 
     * @return Expression containing the BitRange's end bit index
     */
    public Expression getEndBitIndex()
	{
		if(endBitIndex != null)
			return endBitIndex;
		else
			return pipeSignal.getIndexHighest();
	}
}