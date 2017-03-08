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

package tlv.behavioral;

import tlv.behavioral.range.*;
import tlv.parse.*;
import tlv.parse.identifier.*;
import tlv.utilities.Severity;

import java.util.*;

/**
 * LogicalStage is primarily used in the new methodology where it is given a role of containing
 * assigned and used bit-ranges of all signel scoped under that stage. 
 */
public class LogicalStage extends LogicalBranch
{
	private boolean flagEvaluated;
	private int value;
	
	private Hashtable <String, Vector<UsedBitRange>> usedRanges;
	private Hashtable <String, Vector<BitRange>> assignedRanges;
	
	private Hashtable <String, Vector<TransitionRange>> transitionRanges;
	
	public LogicalStage(Expression identifier_)
	{
		super(identifier_);
		
		flagEvaluated = identifier_.isEvaluated();
		
		if(flagEvaluated)
			value = identifier_.getValue();
		else
		{
			ActiveParseContext.Report(0, Severity.ERROR, "STAGE-PARSE", "Unable to evaluate stage.");
			value = -1;
		}
			
		usedRanges = new Hashtable<String, Vector<UsedBitRange>>();
		assignedRanges = new Hashtable<String, Vector<BitRange>>();
		
		transitionRanges = new Hashtable<String, Vector<TransitionRange>>();
	}
	
	//Creates a blank stage which may be needed as a filler
	public LogicalStage(int stage_number_, LogicalBranch logical_branch_)
	{
		super(logical_branch_);
		
		value = stage_number_;
		flagEvaluated = true;
		
		usedRanges = new Hashtable<String, Vector<UsedBitRange>>();
		assignedRanges = new Hashtable<String, Vector<BitRange>>();
		
	}
	
	/*
	 * Adds an identifier that would link the LogicalStage to another ParseStage
	 */
	public boolean addIdentifier(Expression identifier)
	{
		if(identifier.getValue() == value)
		{
			parse_elements.add(identifier);
			return true;
		}
		else
			return false;
	}

	public int getValue()
	{
		return value;
	}
	
	public boolean isEvaluated()
	{
		return flagEvaluated;
	}
	
	public LogicalWhen getLogicalWhen()
	{
		if(parent instanceof LogicalWhen)
			return (LogicalWhen)parent;
		else
			return null;
	}
	
	
	public String addTransitionRange(PipeSignal pipesignal_, Expression end_index_, Expression start_index_, boolean anchor_, String type_)
	{
		Vector <TransitionRange> temp_transitionRanges = transitionRanges.get(pipesignal_.getLabel());
		
		if(temp_transitionRanges == null)
		{
			temp_transitionRanges = new Vector<TransitionRange>(0,0);
			transitionRanges.put(pipesignal_.getLabel(), temp_transitionRanges);
		}
		
		boolean anchorFlag;
		
		Expression lowestStartBit = null;
		Expression highestEndBit = null;
		
		for(int i = 0; i < temp_transitionRanges.size(); i++)
		{
			if(anchor_ == true || temp_transitionRanges.get(i).isAnchored() == true)
				anchorFlag = true;
			else
				anchorFlag = false;

			//TODO replace isLessThen with isLesser etc..
			if(Expression.isLesser(end_index_, temp_transitionRanges.get(i).getStartBitIndex()) > 1 && Expression.isGreater(temp_transitionRanges.get(i).getStartBitIndex(), end_index_) > 1)
			{
				temp_transitionRanges.insertElementAt(new TransitionRange(pipesignal_, end_index_, start_index_, anchor_, type_), i);
				lowestStartBit = start_index_;
				
				if(type_ == "FLOP1")
					return "FLOP2";
				else
					return "FLOP1";
				
			}
			else if((Expression.isLesser(start_index_, temp_transitionRanges.get(i).getEndBitIndex()) >= -1 || Expression.isGreater(temp_transitionRanges.get(i).getEndBitIndex(), start_index_) >= -1) 
				&& ( Expression.isGreater(end_index_, temp_transitionRanges.get(i).getStartBitIndex()) >= -1 || Expression.isLesser( temp_transitionRanges.get(i).getStartBitIndex(), end_index_) >= -1))
			{
				if(Expression.isLesser(start_index_, temp_transitionRanges.get(i).getStartBitIndex()) > 0)
					lowestStartBit = start_index_;
				else
					lowestStartBit = temp_transitionRanges.get(i).getStartBitIndex();
					
				if(Expression.isGreater(end_index_, temp_transitionRanges.get(i).getEndBitIndex()) >  0)
					highestEndBit = end_index_;
				else
					highestEndBit = temp_transitionRanges.get(i).getEndBitIndex();
					
				if(anchorFlag && temp_transitionRanges.get(i).getType() == "LATCH")
					type_ = "LATCH";
					
				temp_transitionRanges.remove(i);
					
				addTransitionRange(pipesignal_, highestEndBit, lowestStartBit, anchorFlag, type_);
				
				if(type_ == "FLOP1")
					return "FLOP2";
				else
					return "FLOP1";
			}
		}
			
		temp_transitionRanges.add(new TransitionRange(pipesignal_, end_index_, start_index_, anchor_, type_));	
		
		if(type_ == "FLOP1")
			return "FLOP2";
		else
			return "FLOP1";
		
	}
	
	public void addUsedRange(UsedBitRange range)
	{
		String temp_label = range.getLabel();
		Vector <UsedBitRange> temp_usedRanges =  usedRanges.get(temp_label);
		
		if(temp_usedRanges == null)
		{
			temp_usedRanges = new Vector<UsedBitRange>(0,0);
			temp_usedRanges.add(range);
			usedRanges.put(temp_label, temp_usedRanges);
		}
		else
		{
			temp_usedRanges.add(range);
		}
	}
	
	public void addAssignedRange(BitRange assigned_range)
	{
		String temp_label = assigned_range.getLabel();
		Vector <BitRange> temp_assignedRanges =  assignedRanges.get(temp_label);
		
		if(temp_assignedRanges == null)
		{
			temp_assignedRanges = new Vector<BitRange>(0,0);
			temp_assignedRanges.add(assigned_range);
			assignedRanges.put(temp_label, temp_assignedRanges);
		}
		else
		{
			temp_assignedRanges.add(assigned_range);
		}
	}
	
	public Vector<BitRange> getAssignedBitRanges_ofSignal(String signal_name)
	{
		if(assignedRanges != null)
			return assignedRanges.get(signal_name);
		else
			return null;
	}
	
	public Vector<UsedBitRange> getUsedBitRanges_ofSignal(String signal_name)
	{
		if(usedRanges != null)
			return usedRanges.get(signal_name);
		else
			return null;
	}
}
