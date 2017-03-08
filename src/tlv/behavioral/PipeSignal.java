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

import tlv.Main;
import tlv.behavioral.range.*;
import tlv.config.IdentifierType;
import tlv.config.ProjSpecific;
import tlv.parse.*;
import tlv.parse.identifier.*;
import tlv.parse.identifier.Identifier.Syntax;
import tlv.utilities.Severity;

import java.util.*;

/**
 * The PipeSignal class is not part of the LogicalBranch/Logical
 structure and instead
 * serves as a linker between the different objects within this structure. It groups together
 * all the BitRanges, and physical scope objects(LogicalPipeline, LogicalWhens, LogicalStages)
 * associated with a pipesignal.
 * 
 * @author ypyatnyc
 *
 */
public class PipeSignal
{
	//private String label;
	private Identifier identifier;
	private Identifier sv_type;  // The SV datatype of this pipesignal.
	
	// TODO: Need an Identifier for this pipesignal.
	
	private LogicalPipeline pipeline;
	private LogicalBehScope beh_scope;

	//links to a when object where this signal is used as a gating condition
	private LogicalWhen when;
	
	//links to stages/gating conditions under which the signal is assigned or consumed
	protected Hashtable <String, LogicalWhen> gating_whens;
	protected Vector <LogicalStage> ungatedStages;
	
	//Contains all of the signal's bitranges
	private Vector <BitRange> assignedBitRanges;
	private Vector <UsedBitRange> usedBitRanges;
	
	//Highest and lowest indexes for used bit ranges
	private Expression usedIndexHighest		= null;
	private Expression usedIndexLowest 		= null;
	
	//Highest and lowest index for assign bit ranges
	private Expression assignedIndexHighest = null;
	private Expression assignedIndexLowest 	= null;
	
	// These are used for pulling signals through wildcarded assignments.
	// They keeps track of whether the used/assignedIndexHighest/Lowest was pulled from max/min of an original assignment.  The search would not
	// be optimal without this because we could not distinguish evaluated bounds pulled from an original assignment (to satisfy an unevaluated
	// use) and those that were not, and we would always have to search to a leaf to figure that out.
	public boolean max_from_orig_assignment = false;
	public boolean min_from_orig_assignment = false;
	
	private int usedStageHighest;
	private int usedStageLowest;
	
	private int assignedStageHighest;
	private int assignedStageLowest;
	
	public boolean from_wildcard;  // TODO: I'd like to get rid of this, but I don't have a way to determine it.
	public boolean fromWildcard() {return from_wildcard;}
	
	//Old methodology, contains transition stages from which stagin flops and latches
	//are later generated. 
	private Vector <TransitionStage> tStages;
	private int tStageOffset;
	

	/**
	 * Creates an empty PipeSignal for specified single name and links it to the
	 * specified LogicalPipeline
	 * 
	 * @param beh_scope_
	 * @param pipeline_
	 * @param label_
	 * @param type_ SV datatype of this signal
	 */
	//Constructor used for creating struct signals
	public PipeSignal(LogicalBehScope beh_scope_, LogicalPipeline pipeline_, Identifier identifier_, Identifier type_)
	{
		pipeline = pipeline_;   // TODO: This seems to be the only constructor that sets this.
		beh_scope = beh_scope_; // TODO: This seems to be the only constructor that sets this.
		identifier = identifier_;
		sv_type = null;
		
		gating_whens = new Hashtable <String, LogicalWhen>(0);
		ungatedStages = new Vector<LogicalStage>(0,0);
		
		assignedBitRanges = new Vector <BitRange>(0,0);
		usedBitRanges = new Vector <UsedBitRange>(0,0);
		
		//bitRangeHighestIndex = null;
		//bitRangeLowestIndex = null;
		
		tStages = new Vector <TransitionStage>(0,0);
		sv_type = type_;
	}
	
	// TODO: Unused?
	/**
	 * Creates a PipeSignal from an existing UsedBitRange.
	 * The created PipeSignal gets its name from the BitRange,
	 * and also adds the BitRange to its UsedBitRanges container.
	 * 
	 * @param bit_range_ the UsedBitRange for which the PipeSignal is created
	 */
	/*
	public PipeSignal(UsedBitRange bit_range_, float dummy)
	{
		label = bit_range_.getLabel();
		
		gating_whens = new Hashtable <String, LogicalWhen>(0);
		ungatedStages = new Vector<LogicalStage>(0,0);
		
		assignedBitRanges = new Vector <AssignedBitRange>(0,0);
		usedBitRanges = new Vector <UsedBitRange>(0,0);
		
		tStages = new Vector <TransitionStage>(0,0);
		
		addUsedRange(bit_range_);
	}
	*/
	
	// TODO: Unused?
	/**
	 * Creates a PipeSignal from an existing AssignedBitRange.
	 * The created PipeSignal gets its name from the BitRange,
	 * and also adds the BitRange to its AssignedBitRanges container.
	 * 
	 * @param bit_range_ the AssignedBitRange for which the PipeSignal is created
	 */
	/*
	public PipeSignal(AssignedBitRange bit_range_, float dummy)
	{
		label = bit_range_.getLabel();
		
		gating_whens = new Hashtable <String, LogicalWhen>();
		ungatedStages = new Vector<LogicalStage>(0,0);
		
		assignedBitRanges = new Vector <AssignedBitRange>(0,0);
		usedBitRanges = new Vector <UsedBitRange>(0,0);
		
		tStages = new Vector <TransitionStage>(0,0);
		
		addAssignedRange(bit_range_);
	}
	*/
	
	/**
	 * Creates a PipeSignal from an existing LogicalWhen
	 * The created PipeSignal gets its name from when_'s label
	 * and assigns the specified LogicalWhen as the logicalWhen
	 * that the PipeSignal is condition for 
	 * 
	 * @param when_
	 */
	/*
	public PipeSignal(LogicalWhen when_, float dummy)
	{
		label = when_.getLabel();
		
		when = when_;
		
		gating_whens = new Hashtable <String, LogicalWhen>();
		ungatedStages = new Vector<LogicalStage>(0,0);
		
		assignedBitRanges = new Vector <AssignedBitRange>(0,0);
		usedBitRanges = new Vector <UsedBitRange>(0,0);
		
		tStages = new Vector <TransitionStage>(0,0);
	}
	*/

    // TODO: Phase this out.
	public String getLabel()
	{
		//return label;
		return identifier.getName();
	}
	
	public Identifier getIdentifier()
	{
		return identifier;
	}
	
	public Identifier getSvDataTypeIdentifier()
	{
		return sv_type;
	}
	
	public void setSvDataTypeIdentifier(Identifier type_)
	{
		sv_type = type_;
	}
	
	public String getTypeString()
	{
		return sv_type.getName();
	}
	
	public boolean isState()
	{
		return identifier.getType() == IdentifierType.STATE_SIG;
	}
	
	/**
	 * @return The pipeline of this signal.  Null will be returned for SV signals and only for SV signals.
	 */
	public LogicalPipeline getPipeline()
	{
		return pipeline;
	}
	
	public LogicalBehScope getBehScope()
	{
		return beh_scope;
	}
	
	public LogicalWhen getWhen()
	{
		return when;
	}
	public void setWhen(LogicalWhen when_)
	{
		when = when_;
	}
	
	public Expression getUsedIndexHighest()
	{
		return usedIndexHighest;
	}
	
	public Expression getUsedIndexLowest()
	{
		return usedIndexLowest;
	}
	
	//returns the highest index for the PipeSignal's bit range
	public Expression getIndexHighest()
	{
		if(assignedIndexHighest == null)  // TODO: Shouldn't be permitted if assigned.
			return usedIndexHighest;
		else
			return assignedIndexHighest;
	}
	
	//returns the highest index for the PipeSignal's bit range
	public Expression getIndexLowest()
	{
		if(assignedIndexLowest == null)
			return usedIndexLowest;
		else
			return assignedIndexLowest;
	}
	
	/**
	 * Checks to see if the single assignment satisfies the uses.  This is used while propagating signals through wildcarded assignments.
	 * If a used bound is unknown or null, we must have the bound from an original assignment.
	 * @return true if more bits may be needed.
	 */
	public boolean needsMoreBits()
	{
		Main.assertion((assignedIndexHighest != null) ^ (assignedIndexLowest == null));   // Either both null (no assignment) or neither.
		return // Max
			( !max_from_orig_assignment &&               // not already maxed &&
			  ( (assignedIndexHighest == null) ||        //   no assignment ||
		        (usedIndexHighest == null) ||            //   need max ||
		        !usedIndexHighest.isEvaluated() ||       //
		        ( assignedIndexHighest.isEvaluated() &&  //     both evaluated &&
		          usedIndexHighest.isEvaluated() &&      //
		          ( assignedIndexHighest.getValue() <    //     need more bits
		            usedIndexHighest.getValue()
		    	  )
		        )
			  )
			) ||
			// Min
			( !min_from_orig_assignment &&
			  ( (assignedIndexLowest == null) ||
				(usedIndexLowest == null) ||
			    !usedIndexLowest.isEvaluated() ||
			    ( assignedIndexLowest.isEvaluated() &&
			      usedIndexLowest.isEvaluated() &&
			      ( assignedIndexLowest.getValue() <
			        usedIndexLowest.getValue()
			      )
			    )
			  )
			);
	}
	
	public Iterator<UsedBitRange> getUsedRangesIterator()
	{
		return usedBitRanges.iterator();
	}
	
	public Iterator<BitRange> getAssignedRangesIterator()
	{
		return assignedBitRanges.iterator();
	}
	
	public Hashtable<String, LogicalWhen> getGatingWhens()
	{
		return gating_whens;
	}
	
	public LogicalWhen addGatingWhen(LogicalWhen when_)
	{
		LogicalWhen temp_when = gating_whens.get(when_.getLabel());
		
		if(temp_when == null)
		{
			gating_whens.put(when_.getLabel(), when_);
		}
		
		return gating_whens.get(when_.getLabel());
	}
	
	//New methodology. Adds a scope ungated LogicalStage to the PipeSignal
	public LogicalStage addStage(LogicalStage logical_stage_)
	{
		int stageOffset;
		
		if(ungatedStages.size() == 0)
		{
			ungatedStages.insertElementAt(logical_stage_, 0);
		}
		else
		{
			stageOffset = ungatedStages.firstElement().getValue();
			
			if(logical_stage_.getValue() < stageOffset)
			{
				ungatedStages.insertElementAt(logical_stage_, 0);
				
				for(int i = 1; i < stageOffset-logical_stage_.getValue(); i++)
					ungatedStages.insertElementAt(new LogicalStage(logical_stage_.getValue() + i, pipeline), i);
			}
			else if(logical_stage_.getValue() >= ungatedStages.size() + stageOffset)
			{
				for(int i = ungatedStages.size()+stageOffset; i < logical_stage_.getValue(); i++)
				{
					ungatedStages.add(new LogicalStage(i, pipeline));
				}
	
				ungatedStages.add(logical_stage_);
			}
		}
		
		return ungatedStages.get(logical_stage_.getValue() - ungatedStages.firstElement().getValue());
	}
	
	public LogicalStage getStage(int stageNum)
	{
		return ungatedStages.elementAt(stageNum-ungatedStages.firstElement().getValue());
	}
	
	
	public BitRange getSingleAssignedRange()
	{
		try
		{
			return assignedBitRanges.firstElement();
		} catch (NoSuchElementException e)
		{
			return null;
		}
	}
	
	// Returns the single assignment statement for this pipe signal, or null if there is not exactly one.
	public ParseMnemonic getSingleAssignmentMnemonic()
	{
		return (assignedBitRanges.size() == 1) ? assignedBitRanges.get(0).getIdentifier() : null;
	}
	
	public ParseContext getParseContext()
	{
		ParseMnemonic mnemonic = getSingleAssignmentMnemonic();
		return (mnemonic == null) ? getBehScope().getSourceFile().getFileContext() :
	                                mnemonic.getParseContext();
	}
	
	//New methodology. The PipeSignal has its associated scopes be updated
	//according the the AssignedBitRange's scope
	public LogicalStage addAssignedBitRange(BitRange range)
	{
		// TODO: Actually, this whole method is never executed.  "New methodology" was abandoned.  DELETE THIS, after some investigation.
		LogicalStage temp_stage;
		
		temp_stage = addStage(range.getLogicalStage());
		temp_stage.addAssignedRange(range);
		return getStage(range.getStageValue());

		// TODO: This code was never executed.  Under it's condition, the above code was NOT executed.  I don't understand the intent.  In any case, maybe we're not using the gating when structure it initializes... at least not for assignments???
		/*
		LogicalWhen temp_when = range.getLogicalWhen_top();
		if(temp_when != null)
		{
			ActiveParseContext.Report(0, Severity.BUG, "GOT-HERE", "Yep, I got here.");
			addGatingWhen(temp_when);
			return null;
		}
		*/
	}
	
	//New methodology. The PipeSignal has its associated scopes be updated
	//according the the UsedBitRange's scope
	// TODO: NOT CURRENTLY INVOKED.
	public void addUsedBitRange(UsedBitRange range)
	{
		Main.breakpoint();
		int temp_stage_value = range.getLogicalStage().getValue();
		
		if(range.getAlignment() != null)
			if(range.getAlignment().isEvaluated())
				temp_stage_value += range.getAlignment().getValue();
			
		LogicalStage temp_stage = addStage(new LogicalStage(temp_stage_value, pipeline));
		temp_stage.addUsedRange(range);
	}
	
	//Adds a UsedBitRange into the PipeSignal's usedBitRanges container
	//This method also updates the PipeSignal's boundaries such as highest and lowest
	//range indexes
	public void addUsedRange(UsedBitRange range)
	{
		Expression lowest = (range.getStartBitIndex() == null) ? range.getEndBitIndex() : range.getStartBitIndex(); // If only End, it's one bit and Start == End.
		if(Expression.isLesser(lowest, usedIndexLowest) >= 0)   // == case gives priority to null, which would be full range.
			usedIndexLowest = lowest;
		
		if(Expression.isGreater(range.getEndBitIndex(), usedIndexHighest) >= 0)
			usedIndexHighest = range.getEndBitIndex();
		
		if(usedBitRanges.size() == 0)
		{
			usedBitRanges.add(range);
			usedStageHighest = range.getStageValue();
			usedStageLowest = range.getStageValue();
		}
		else
		{	
			if(range.getStageValue() > usedStageHighest)
				usedStageHighest = range.getStageValue();
			else if(range.getStageValue() < usedStageLowest)
				usedStageLowest = range.getStageValue();
			
			for(int i = 0; i < usedBitRanges.size(); i++)		
			{	
				if(range.getStageValue() < usedBitRanges.get(i).getStageValue())
				{
					usedBitRanges.insertElementAt(range, i);
					return;
				}
				else if(range.getStageValue() == usedBitRanges.get(i).getStageValue())
				{
					if(	Expression.isLesser(range.getStartBitIndex(), usedBitRanges.get(i).getStartBitIndex()) > 0
					||	(Expression.isLesser(range.getStartBitIndex(), usedBitRanges.get(i).getStartBitIndex()) == 0 && Expression.isGreater(range.getEndBitIndex(), usedBitRanges.get(i).getEndBitIndex()) > 0))
					{
						usedBitRanges.insertElementAt(range, i);
						return;
					}
				}
			}
				
			usedBitRanges.add(range);	
		}
	}
	
	// Updates assignedIndexHighest/Lowest to reflect the given range as an assignment.
	public void reflectAssignedRange(BitRange range)
	{
		// TODO: Shouldn't have null assigned range bounds.
		if((range.getEndBitIndex() != null) &&
		   ((assignedIndexHighest == null) || Expression.isGreater(range.getEndBitIndex(), assignedIndexHighest) >= 0)
		  )
				assignedIndexHighest = range.getEndBitIndex();
		
		if((range.getStartBitIndex() != null) &&
		   ((assignedIndexLowest == null) || Expression.isLesser(range.getStartBitIndex(), assignedIndexLowest) >= 0)
		  )
				assignedIndexLowest = range.getStartBitIndex();
	}
	
	//Adds an AssignedBitRange into the PipeSignal's assignedBitRanges container.
	//This method also updates the PipeSignal's boundaries such as highest and lowest
	//range indexes
	// assignment is passed in only for the multiple assignment check, since multiple assignments are permitted within a sequential block.
	public boolean addAssignedRange(BitRange range)
	{
		// TODO: This code was written allowing for multiple assignments, and we later added the restriction.  But, I think we still allow multiple assigned ranges within certain assignment constructs,
		//       assuming all assigned range declarations are identical.  Check that a second assigned range is identical here, and do the check below if assigned ranges are from different assignments.
		//if (getSingleAssignment != null)
		//{
		//	ActiveParseContext.Report(0, Severity.FATAL_ERROR, "MULTI-ASSIGN", "Multiple assignments for the same signal.");
		//}
		reflectAssignedRange(range);

		if(sv_type != null)
		{
			/* When PipeSignal is a structure it may have overlapping assignedBitRanges if and only if they are
			 * in the same stage.  TODO: Huh??
			 */
			if(assignedBitRanges.isEmpty())
			{
				assignedBitRanges.add(range);
				assignedStageHighest = range.getStageValue();
				assignedStageLowest = range.getStageValue();
			}
			else if(range.getStageValue() != assignedStageHighest || range.getStageValue() != assignedStageLowest)
			{
				System.out.println("Cannot make multiple struct assignments at different stages");
				return false;
			}
			
			return true;
		}
		else if(assignedBitRanges.size() == 0)
		{
			assignedBitRanges.add(range);

			assignedStageHighest = range.getStageValue();
			assignedStageLowest = range.getStageValue();
			
			return true;
		}
		else if(assignedBitRanges.firstElement().getEndBitIndex() == null && assignedBitRanges.firstElement().getStartBitIndex() == null)
		{  //TODO: not true, single bit signals have starting and ending bit range of 0
			System.out.println("Syntax error, the bit range has already been declared as a single bit signal");
			
			return false;
		}
		else if(Expression.isGreater(range.getStartBitIndex(), assignedBitRanges.lastElement().getEndBitIndex()) > 0)
		{
			assignedBitRanges.add(range);
			
			assignedIndexHighest = range.getEndBitIndex();
			
			if(range.getStageValue() > assignedStageHighest)
				assignedStageHighest = range.getStageValue();
			else if(range.getStageValue() < assignedStageLowest)
				assignedStageLowest = range.getStageValue();
			
			return true;
		}
		else if(Expression.isLesser(range.getEndBitIndex(), assignedBitRanges.firstElement().getStartBitIndex()) > 0)
		{		
			assignedBitRanges.insertElementAt(range, 0);
			
			assignedIndexLowest = range.getStartBitIndex();
				
			if(range.getStageValue() > assignedStageHighest)
				assignedStageHighest = range.getStageValue();
			else if(range.getStageValue() < assignedStageLowest)
				assignedStageLowest = range.getStageValue();
			
			return true;
		}
		else
		{
			// Assignment overlaps the min/max of previous ones
			for(int i = 0; i < assignedBitRanges.size(); i++)
			{
				if(Expression.isGreater(range.getStartBitIndex(), assignedBitRanges.get(i).getEndBitIndex()) > 0 &&
				   Expression.isLesser(range.getEndBitIndex(), assignedBitRanges.get(i+1).getStartBitIndex()) > 0)
				{
					// Falls between.
					assignedBitRanges.insertElementAt(range, i+1);
					
					if(range.getStageValue() > assignedStageHighest)
						assignedStageHighest = range.getStageValue();
					else if(range.getStageValue() < assignedStageLowest)
						assignedStageLowest = range.getStageValue();
					
					return true;
				}
				else if(range.getAssignment() == assignedBitRanges.get(i).getAssignment() &&
						(Expression.isLesser(range.getStartBitIndex(), assignedBitRanges.get(i).getStartBitIndex()) > 0 ||
						(Expression.isLesser(range.getStartBitIndex(), assignedBitRanges.get(i).getStartBitIndex()) == 0 &&
						 Expression.isGreater(range.getEndBitIndex(), assignedBitRanges.get(i).getEndBitIndex()) >= 0)
						)
					   )
				{
					// Insert before this same-assignment range.
					assignedBitRanges.insertElementAt(range, i);
					return true;
				}
			}
			
			ActiveParseContext.Report(0, Severity.RISKY_ERROR, "MULT-ASSIGN", "Multiple assignments to bits of: " + range.getPipeSignal().toScopedString());
			return false;
		}
	}
	
	/**
	 * Called during wildcarded assignment search to propagate SV properties of an original signal assignment into this signal.
	 * Bounds are reflected in used range, which is to be reflected later in assigned range.
	 * @param orig_sig
	 */
	void reflectOrigAssignment(PipeSignal orig_sig)
	{
		// SV type:
		if (sv_type != null)
		{
			orig_sig.getParseContext().report(0, Severity.RECOV_ERROR,
											  "SV-TYPE", "During wildcarded assignemnt search, SV type found for a signal in a scope where it was not directly assigned, while propagating SV type from this assignment.\n" +
											  "SV type should only be declared where assigned.  Declared type ignored.");
		}
		sv_type = orig_sig.sv_type;
		
		// Bounds:
		if ((usedIndexHighest == null) || !usedIndexHighest.isEvaluated())
		{
			// Need original bound.
			usedIndexHighest = orig_sig.assignedIndexHighest;
			max_from_orig_assignment = true;
		}
		if ((usedIndexLowest == null) || !usedIndexLowest.isEvaluated())
		{
			// Need original bound.
			usedIndexLowest = orig_sig.assignedIndexLowest;
			min_from_orig_assignment = true;
		}
	}
	
	//TODO
	//Work in progress: Attempt at generating Staging elements from the nested LogicalStages
	//and LogicalWhens, rather then from creating new TransitionStages
	public void generateFlopsNew()
	{
		String thisType = "FLOP1";
		LogicalStage temp_stage = null;
		LogicalWhen temp_when = null;
		
		//First generate uses out of earliest stages of every immediate gating when
		if(gating_whens != null)
		{
			for(Enumeration<LogicalWhen> e = gating_whens.elements(); e.hasMoreElements();)
			{
						//gets a signal from signals list in the pipeline
				temp_when = e.nextElement();
				
				
				
			}
		}
		
		
		for(int i = ungatedStages.size() - 1; i >= 0; i--)
		{
			temp_stage = ungatedStages.get(i);
			
			
		}
	}
	
	//Prints a new line in the system verilog declarations file
	public void printSVdeclaration(String line)
	{
		SourceFile file = getPipeline().getSourceFile();
		file.printlnSVdeclaration(line);
	}
	
	public void printSVstaging(String line)
	{
		SourceFile file = getPipeline().getSourceFile();
		file.printlnSVstaging(line);
	}
	
	//Older methodology. Generates transition stages between two BitRanges
	public void generateFlops(BitRange aR, UsedBitRange uR)
	{	
		String thisType = "FLOP1";  // TODO: Fix this.
		
		//TODO adjust tStageOffset to make compatible with multiple assigned ranges
		tStageOffset = aR.getStageValue() + 1;

		// Last stage to which to transition.
		
		// TODO: Is there checking for use prior to assignment.
		// TODO: Gating was coded with flops in mind only, not latches.
		int lastStage = uR.getStageValue();
		
		
		Expression end_bit_ind   = uR.getEndBitIndex();
		Expression start_bit_ind = uR.getStartBitIndex();
		
		
		// This algorithm can't do the right thing if a bit expression cannot be evaluated.  If the assigned or used end/start bit expressions cannot be determined, use
		// the assigned expression, which should be the superset.  (Note, this method assumes a single assignment.)
		if (end_bit_ind == null ||
		    !aR.getEndBitIndex().isEvaluated() ||
			!uR.getEndBitIndex().isEvaluated())
		{
			end_bit_ind = aR.getEndBitIndex();
		}
		if (start_bit_ind == null ||
			!aR.getStartBitIndex().isEvaluated() ||
			!uR.getStartBitIndex().isEvaluated())
		{
			start_bit_ind = aR.getStartBitIndex();
		}
		
		
		for(int i = tStageOffset; i <= lastStage;)
		{
			//adds new transition stage when necessary
			if(i - tStageOffset >= tStages.size())
				tStages.add(new TransitionStage(this, i));
			
			//if(i < uR.getStageValue())
			if(i < lastStage)
			{
				thisType = tStages.get(i - tStageOffset).addRange(end_bit_ind, start_bit_ind, false, thisType);
				i++;	
				
				if(thisType == "FLOP1")
					continue;
				
				//adds new transition stage when necessary
				if(i - tStageOffset >= tStages.size())
					tStages.add(new TransitionStage(this, i));
				thisType = tStages.get(i - tStageOffset).addRange(end_bit_ind, start_bit_ind, uR.getStageValue() == i, thisType);
				i++;
			}
			//else if(i == uR.getStageValue())
			else if(i == lastStage)
			{
				tStages.get(i - tStageOffset).addRange(end_bit_ind, start_bit_ind, true, "LATCH");
				i++;
			}
		}		
	}
	
	
	/**
	 *  If there is no AssignedBitRange for this signal, see if we can manufacture one from wildcarded assignments.
	 */
	public void findAssignedRange()
	{
		if (getIdentifier().isStagedSigType())
		{
			// If there is no assignment, see if we can manufacture one from wildcarded assignments.
			if (assignedBitRanges.size() <= 0 &&
				usedBitRanges.size() >= 0)
			{
				/*
				// Manufacture an aggregated BitRange for the assigned range that is required.
				BitRange required_range = new BitRange(getBehScope(), this, usedIndexHighest, usedIndexLowest);
				BitRange assigned_range = getBehScope().findAssign(required_range);
				if (assigned_range == null)
				{
					// No assignment found.  We'll catch this problem later.
				}
				*/
				
				LogicalBehScope.PullDebug pull_debug = new LogicalBehScope.PullDebug();
				getBehScope().pullSignal(this, null, false, pull_debug);
				if ((pull_debug.failed_cnt > 0) && (pull_debug.orig_cnt > 0))
				{
					ActiveParseContext.Report(0, Severity.GEN_ERROR, "NO-ASSIGN",
							                  "Searched " + pull_debug.wildcard_cnt + " wildcarded assignment(s) for " + toScopedString() + ", which led to " + pull_debug.orig_cnt + " explicit assignment(s), and " + pull_debug.failed_cnt + " which did not exist.");
				}
			}
		}
	}
	
	
	//in this method the TransitionRanges and TransitionStages are generated
	//the method returns a range whose stage is the lowest
	//of all used and assigned ranges, and whose starting and ending bitRange are
	//the maximum and minimum bit ranges of the signal
	public void generateStaging()
	{
		if (!getIdentifier().isStagedSigType()) ActiveParseContext.Report(0, Severity.BUG, "BUG", "generateFlopsAndLatches() called for type " + getIdentifier().getType().name());

		boolean no_assign = assignedBitRanges.size() <= 0;		
		if (no_assign)
		{
			// No assignment.

			if (usedBitRanges.size() <= 0)
			{
			        // No uses either.
				ActiveParseContext.Report(0, Severity.BUG, "FATAL_BUG", "Signal with no uses or assignments.");
			} else
			{
			        // Report missing assignment.

				UsedBitRange first_used_range = usedBitRanges.firstElement();

				// Use context from first_used_range.
			        // Must be fatal if the use has different scope or alignment than the assignment statement.
			        boolean fatal = (first_used_range.getAssignment().getLogicalBehScope() != getBehScope()) ||
				                ((first_used_range.getAlignment() != null) &&
                                                 (!first_used_range.getAlignment().isEvaluated() ||
                                                  (first_used_range.getAlignment().getValue() != 0))
                                                );
				first_used_range.getParseContext().report(0, fatal ? Severity.FATAL_ERROR : Severity.RECOV_ERROR,
                                                                          "UNASSIGNED-SIG", "Signal " + toScopedString() + " is used but never assigned.");

				// Manufacture an assignment with the maximum width at the first used stage, so the staging can be generated properly.
				// Note that a UsedBitRange is created (with an alignment), mimicking the first use.  Non-null end/start bits are used.
				// TODO: Should be cleaned up as we remove ParseMnemonic -- I don't like the way stage is currently accessed.
				//addAssignedBitRange(new UsedBitRange(beh_scope,
				//		                             first_used_range.getIdentifier(),
				//		                             getIdentifier(),
				//		                             first_used_range.getAlignment(),
				//		                             (getIndexHighest() == null) ? new Expression(first_used_range.getIdentifier().getParseNode()) : getIndexHighest(),
				//		                             (getIndexLowest()  == null) ? new Expression(first_used_range.getIdentifier().getParseNode()) : getIndexLowest()));
				// TODO: Adding a UsedBitRange as an assignment gets into some buggy code, so we do this instead.  This will not compensate for the alignment of the first_used_range
				// so it can generate use prior to assignment issues.  Actually, the above method was abandoned and is the wrong one to call.
				//-addAssignedBitRange(new BitRange(beh_scope, first_used_range.getIdentifier(), getIdentifier(), getIndexHighest(), getIndexLowest(), true));
				first_used_range.getAssignment().addAssignedBitRange(getBehScope(), first_used_range.getIdentifier(), getIdentifier(), getIndexHighest(), getIndexLowest());
			}
		}
		
		// TODO: Assumes single assignment.
		BitRange assigned_range = assignedBitRanges.firstElement();
		if (assigned_range.getEndBitIndex() == null || assigned_range.getStartBitIndex() == null) ActiveParseContext.Report(0, Severity.BUG, "BUG", "null min/max bit index for signal " + toString());
		
		if(usedBitRanges.size() > 0)
		{
			// Check use before assignment.
			UsedBitRange first_used_bit_range = usedBitRanges.get(0);
			if (assigned_range.getStageValue() > first_used_bit_range.getStageValue())
			{
				first_used_bit_range.getParseContext().report(0, Severity.DEFERRED_ERROR, "EARLY-USE", "Signal " + toScopedString() + " is used before it is assigned.");
			}
            // TODO: State signal consumption in the producing stage is not currently supported, but will be, at which point this check can be removed.
			if (!no_assign && isState() && (assigned_range.getStageValue() == first_used_bit_range.getStageValue()))
			{
				first_used_bit_range.getParseContext().report(0, Severity.DEFERRED_ERROR, "EARLY-USE", "State signal " + toScopedString() + " is used in the same stage it is assigned.  This is not currently supported.");
			}
			for(int i = 0; i < usedBitRanges.size(); i++)
			{
				generateFlops(assigned_range, usedBitRanges.get(i));
			}
		}
		else
		{
			getParseContext().report(0, Severity.WARNING, "UNUSED-SIG", "Signal " + toScopedString() + " is assigned but never used.\nTo silence this message use \"`BOGUS_USE(" + toString() + ")");
		}
	}
	
	public void declareSvSignal(BitRange startingBitRange, int stage)
	{
		// TODO: Currently the declaration is entirely packed.  This is probably not optimal for simulation.
		//       Probably want one packed dimension, so unpacked in general, with packing on the final scope of single-bit sigs.
		String bit_range_str = startingBitRange.toString();
		if (bit_range_str.length() > 0)
		{
			bit_range_str += " ";
		}
		
		String scope_ranges_str = getBehScope().getSvRangesStr();
		if (scope_ranges_str.length() > 0)
		{
			scope_ranges_str = scope_ranges_str + " ";
		}
		
		printSVdeclaration( ((sv_type == null) ? "node " : sv_type.getName() + " ") +
	    			       	// Scope ranges
		                    scope_ranges_str +
		                    // Bit range
		                    bit_range_str +
		                    // Signal name
		                    Main.projSpecific.svSignalName(this, stage) +
		                    // Move scope_ranges_str here to make scopes unpacked.
		                    ";"
		                  );		
	}
	
	// Prints used node declarations and staging elements defined in the tStages vector.
	public void declareStaging()
	{
		if (getIdentifier().getType() != IdentifierType.SIG_KEYWORD)
		{
			// Declare the starting bit range
			BitRange starting_bit_range = getSingleAssignedRange();
			declareSvSignal(starting_bit_range, starting_bit_range.getStageValue());
		}
		
		if (getIdentifier().getType().isStagedSig())
		{
			// The subsequent bit ranges are declared
			for(int i = 0; i < tStages.size(); i++)
			{
				tStages.get(i).printSignalDeclaration();
			}
			if(tStages.size() > 0)
			{
				for(int i = 0; i < tStages.size(); i++)
				{
					tStages.get(i).printFlopOrLatch();
				}
			}
		}
	}
	

	/**
	 * Get the gating LogicalWhen for this PipeSignal, assuming there is at most one.
	 * TODO: Since this is not assumption we should rely on, this method should be phased out.
	 * 
	 * @return The signal that logically gates this PipeSignal.
	 */
	public LogicalWhen getGatingWhen()
	{
		// Determine gated clock, under assumptions above.
		// TODO: Also assuming no nested gaters.

		//String gater = "";  // Will stay "" if ungated.
		Enumeration <LogicalWhen> gaters = getGatingWhens().elements();
		LogicalWhen when = null;
		try
		{
			when = gaters.nextElement();
			// Gated signal.
			if (gaters.hasMoreElements())
			{
				System.err.println("Currently, all assignments of a signal must be gated by the same When condition.  Signal " + this + " has multiple.");
			}
			//gater = when.getLabel();
		}
		catch (NoSuchElementException e) {}

		return (when);
	}
	
	/**
	 * @param dest_stage
	 * @return SV clock signal name, where this signal is the enable to produce signals in dest_stage.
	 *
	public String mySvClockSignalName(int dest_stage)
	{
		// TODO: Check that this is an enable signal.
		return Main.projSpecific.svClockSignalName(this, dest_stage);
	}
	*/
	
	/**
	 * @param dest_stage
	 * @return string of an SV reference (signal with loop variables indexing scope) of a clock signal gated with this signal as its enable that produces signals in dest_stage.
	 *
	public String svMyClockReference(int dest_stage)
	{
		return mySvClockSignalName(dest_stage) + ((getGatingWhen() == null) ? "" : getGatingWhen().getGatingPipeSignal().getBehScope().getSvIndexStr());
	}
	*/
	
	/**
	 * @param dest_stage
	 * @return SV clock signal name used to stage this PipeSignal to dest_stage.
	 */
	public String svProducingClockSignalName(int dest_stage)
	{
		// TODO: Currently assume consistent gating for all assignments of this signal.
		// TODO: Currently gating is based on assignment condition, not uses, so there is further optimization opportunity.
		// Determine gated clock.
		LogicalWhen when = getGatingWhen();
		return (when == null)                                     ? Main.projSpecific.svUngatedClockName(Main.isEven(dest_stage)) :
		       (identifier.getType() == IdentifierType.STATE_SIG) ? Main.projSpecific.svClockSignalName(null, when.getGatingPipeSignal(), when.getGatingPipeSignal().getBehScope(), dest_stage) :
		    	                                                    Main.projSpecific.svClockSignalName(when.getGatingPipeSignal(), null, when.getGatingPipeSignal().getBehScope(), dest_stage);
	}
	
	/**
	 * @param dest_stage
	 * @return SV reference to clock signal used to stage this PipeSignal to dest_stage (clock signal with loop variables indexing scope).
	 */
	public String svProducingClockReference(int dest_stage)
	{
		return svProducingClockSignalName(dest_stage) + ((getGatingWhen() == null) ? "" : getGatingWhen().getGatingPipeSignal().getBehScope().getSvIndexStr());
	}
	
	/**
	 * @param stage
	 * @return SV signal name at the given stage.
	 */
	public String svSignalName(int stage)
	{
		return Main.projSpecific.svSignalName(this, stage);
	}
	
	/**
	 * @param stage
	 * @return SV signal reference (name plus scope indexing using loop variable names).
	 */
	public String svSignalReference(int stage)
	{
		return svSignalName(stage) + getBehScope().getSvIndexStr();
	}
	
	public String toString()
	{
		//return label + "_" + pipeline.getLabel();
		return identifier.toString();
	}
	
	public String toScopedString()
	{
		return getBehScope().toScopedString() + identifier.toString();
	}
	
	//this method tells whether a pipe signal has any assignments or uses
	public boolean isEmpty()
	{
		if(assignedBitRanges.size() == 0 && usedBitRanges.size() == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Generate the gated clocks associated with this PipeSignal.
	 * This uses structures we may not want to support long-term.
	 * 
	 * @param indentation
	 */
	// TODO: Kill this.
	public void generateClocks() {
		if(when != null)
		{
			// This signal is the gating condition for 'when'.
			//... when.generateClocks();
		}
	}
	
	//This is used for expressing the structure generated with the new Hashtable methodology
	public void printLogicalHierarchy(int indentation)
	{
		LogicalWhen temp_when;
		
		Vector<BitRange> temp_assigned_ranges;
		Vector<UsedBitRange> temp_used_ranges;
		
		String assigned_ranges = "";
		String used_ranges = "";
		
		
		System.out.println("");
		System.out.println(identifier.toString());
		
		for(int i = 0; i < ungatedStages.size(); i++)
		{	
			temp_assigned_ranges = ungatedStages.get(i).getAssignedBitRanges_ofSignal(getLabel());
			temp_used_ranges = ungatedStages.get(i).getUsedBitRanges_ofSignal(getLabel());
			
			if(temp_assigned_ranges != null)
			for(int j = 0; j < temp_assigned_ranges.size(); j++)
			{
				assigned_ranges += "A" + temp_assigned_ranges.get(j).toString() + ", ";
			}
					
			if(temp_used_ranges != null)
			for(int j = 0; j < temp_used_ranges.size(); j++)
			{
				used_ranges += "U" + temp_used_ranges.get(j).toString() + ", ";
			}
						
			System.out.println("@" + ungatedStages.get(i).getValue() + ": " + assigned_ranges + used_ranges);
			
			assigned_ranges = "";
			used_ranges = "";
		}
			
		if(gating_whens != null)
		{
			for(Enumeration<LogicalWhen> e = gating_whens.elements(); e.hasMoreElements();)
			{
						//gets a signal from signals list in the pipeline
				temp_when = e.nextElement();
				temp_when.printLogicalHierchy(getLabel(), 0);	
			}
		}
		
		System.out.println("Gating condition at stages:");
		if(when != null)
		{
			when.printAllStages(0);
		}
			
	}
	
}
