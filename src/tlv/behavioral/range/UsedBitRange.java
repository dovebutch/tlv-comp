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
import tlv.behavioral.*;
import tlv.parse.*;
import tlv.parse.identifier.*;

/**
 * UsedBitRange class extends BitRange and is used to represent uses of Pipesignal bit-ranges.
 * UsedBitRange may contain and alignment Expression which has an effect on its Stage scope.
 * UsedBitRange is allowed to have its start and end bit index be null, in which case
 * the BitRange takes on the full range of the signal.
 * 
 * @author ypyatnyc
 */
public class UsedBitRange extends BitRange
{
	/**
	 * Alignment indicates the offset of the UsedRange relative to its scoped stage
	 */
	private Expression alignment;
	private int dummy_stage_number = -100;
	
	/**
	 * Set to flag this as a clock gating condition use.
	 */
	// TODO: Kill this.
	private boolean gater = false;
	
	public UsedBitRange(int stage_number_, PipeSignal signal_)
	{
		super(null, signal_, null, null);
		dummy_stage_number = stage_number_;
		setGater();
	}
	
	/**
	 * Constructs a UsedBitRange out of ParseElement identifiers
	 * 
	 * @param identifier_ 			ParseMnemonic containing the name of the pipesignal
	 * @param alignment_			Expression containing stage alignment
	 * @param end_bit_index_		Expression containing end bit index of signal's range
	 * @param start_bit_index_		Epxpression containing start bit index of signal's range
	 */
	public UsedBitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier, Expression alignment_, Expression end_bit_index_, Expression start_bit_index_)
	{
		super(sig_scope, identifier_, identifier, end_bit_index_, start_bit_index_, false);
		alignment = alignment_;
	}
	
	// For assigned WildcardedSigRef.
	public UsedBitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier)
	{
		super(sig_scope, identifier_, identifier, null, null, true);
	}
	
	/**
	 * @return Scope of the signal (not of the assignment statement).
	 */
	public LogicalBehScope getSigScope()
	{
		return getPipeSignal().getBehScope();
	}
	
	/**
	 * @return True if this use is as a clock gating condition.
	 */
	public boolean isGater() {
		return gater;
	}

	/**
	 * Flags this use as a clock gating condition.
	 */
	public void setGater() {
		this.gater = true;
	}

	/**
	 * Returns the stage alignment Expression of the UsedBitRange
	 * 
	 * @return Expression containing the stage alignment of the UsedBitRange
	 */
	public Expression getAlignment()
	{
		return alignment;
	}
	
	/**
	 * Returns the LogicalStage scope for the UsedBitRange.
	 * This method takes into account the UsedBitRange's alignment when retrieving
	 * the LogicalStage
	 * TODO: THIS METHOD IS NOT INVOKED.  (It causes infinite recursion.)
	 * @return
	 */
	/*
	public LogicalStage getLogicalStage()
	{
		Main.breakpoint();
		int stageValue = getStageValue();
		LogicalBranch temp_branch;
		
		LogicalStage temp_logical_stage = getLogicalStage();
		
		if(temp_logical_stage != null)
		{
			temp_branch = temp_logical_stage.getParent();
		
			if(temp_branch instanceof LogicalWhen)
			{
				return ((LogicalWhen) temp_branch).getStage(stageValue);
			}
			else if(temp_branch instanceof LogicalPipeline)	
			{	
				return ((LogicalPipeline) temp_branch).getStage(stageValue);
			}
		}

		return null;

		
	}
	*/
	
	
	/**
	 * Returns the value of the UsedBitRange's staged scope.
	 * Alignment is taken into account in producing the most accurate stage value for
	 * the UsedBitRange
	 * @return
	 */
	public int getStageValue()
	{
		//LogicalBranch temp_logical_branch = getParent();
		LogicalStage temp_logical_stage = null;
		
		
		//TODO: Fails to locate logical stage in the logical tree
		/*while(temp_logical_branch != null)
		{
			if(temp_logical_branch instanceof LogicalStage)
			{
				temp_logical_stage = (LogicalStage)temp_logical_branch;
				break;
			}
			else
			{
				temp_logical_branch = temp_logical_branch.getParent();
			}
		}*/
		
		if(identifier != null)
			temp_logical_stage = (LogicalStage)identifier.getParseNode().getLogicalBranch_ofType(NodeType.STAGE);
    	else
    		temp_logical_stage = null;
		
		//look up hierarchy?  Replace above 4 lines?: LogicalStage temp_logical_stage = (LogicalStage)getAncestorOfType(NodeType.STAGE);
		
		Expression temp_alignment = getAlignment();
		
		if(temp_logical_stage != null)
		{
			if(temp_alignment != null)
			{
				if(temp_logical_stage.isEvaluated() && temp_alignment.isEvaluated())
					return temp_logical_stage.getValue() + temp_alignment.getValue();
				else
					throw new Error(); // System.err.println("Bug");
			}
			else
			{
				if(temp_logical_stage.isEvaluated())
					return temp_logical_stage.getValue();
				else
					throw new Error(); // System.err.println("Bug");
			}
		}
		else if(pipeSignal.getWhen() != null)
		{
			return dummy_stage_number;
		}
		else if(type == NodeType.SV_PLUS_UNSCOPED)
		{
			if(temp_alignment.isEvaluated())
				return temp_alignment.getValue();
			return 0;
		}
		throw new Error(); // System.err.println("Bug");
		
		//return -1;
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

}
