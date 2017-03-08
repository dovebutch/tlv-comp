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
import tlv.parse.*;
import tlv.parse.identifier.*;
import tlv.behavioral.range.*;
import tlv.config.IdentifierType;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Vector;

import javax.lang.model.util.Elements;

/* A single TransitionStage will represent half a clock cycle.
 * A range within it can either be defined as LATCH, FLOP1, or FLOP2.
 * FLOP1s are ignored when the staging elements get printed in the declarations file.
 *
 * With the new methodology this class would be completely eliminated and its functionality would be
 * be moved into LogicalStage class.
 * 
 */
class TransitionStage
{
	protected PipeSignal pipesignal;
	protected Vector <TransitionRange>  ranges = new Vector<TransitionRange> (0,0);
	
	protected int stageNumber;
	
	public TransitionStage(PipeSignal pipesignal_, int stage_number_)
	{
		pipesignal = pipesignal_;
		stageNumber = stage_number_;
	}
	
	//This method adds a new range into the transition stage ranges list.
	//addRange will return the type of the last added range. The type can either be FLOP1(intermediate), FLOP2, or LATCH
	//This method might add a use of a gater signal.
	public String addRange(Expression endIndex, Expression startIndex, boolean anchor, String type)
	{
		Expression lowestStartBit = null;
		Expression highestEndBit = null;
		
		boolean anchorFlag;

		int i = 0;
		for(; i < ranges.size(); i++)
		{
			if(anchor == true || ranges.get(i).isAnchored() == true)
				anchorFlag = true;
			else
				anchorFlag = false;


			if(Expression.isLesser(endIndex, ranges.get(i).getStartBitIndex()) > 1 && Expression.isGreater(ranges.get(i).getStartBitIndex(), endIndex) > 1)
			{
				ranges.insertElementAt(new TransitionRange(pipesignal, endIndex, startIndex, anchor, type), i);
				lowestStartBit = startIndex;
				
				break;
			}
			else if((Expression.isLesser(startIndex, ranges.get(i).getEndBitIndex()) >= -1 || Expression.isGreater(ranges.get(i).getEndBitIndex(), startIndex) >= -1) 
				&& ( Expression.isGreater(endIndex, ranges.get(i).getStartBitIndex()) >= -1 || Expression.isLesser( ranges.get(i).getStartBitIndex(), endIndex) >= -1))
			{
				if(Expression.isLesser(startIndex, ranges.get(i).getStartBitIndex()) > 0)
					lowestStartBit = startIndex;
				else
					lowestStartBit = ranges.get(i).getStartBitIndex();
					
				if(Expression.isGreater(endIndex, ranges.get(i).getEndBitIndex()) >  0)
					highestEndBit = endIndex;
				else
					highestEndBit = ranges.get(i).getEndBitIndex();
					
				if(anchorFlag && ranges.get(i).getType() == "LATCH")
					type = "LATCH";
					
				ranges.remove(i);
					
				addRange(highestEndBit, lowestStartBit, anchorFlag, type);
				
				break;
			}
		}
		if (i >= ranges.size())
		{
			// Not added yet (did not break out of loop).  Add new range.
			ranges.add(new TransitionRange(pipesignal, endIndex, startIndex, anchor, type));	
		}
		
		// If this signal is under a when condition, this staging might imply a use of the condition signal.  Create such uses.
		LogicalWhen when = pipesignal.getGatingWhen();
		if ((when != null) &&       // Gated &&
			(type == "FLOP2" ||     // there's a flop or latch in this stage
			 type == "LATCH")
			   )
		{
			PipeSignal gating_sig = when.getGatingPipeSignal();

			// Add use of this signal due to the gating.
			// TODO: There might be project variation here as to what's consumed, so we should provide a project-specific hook, here.
			//       Though consider as well that there is an issue of what's legal TLV code.  We should accept these uses prior to assignments
			//       and clock generation should avoid gating in that case.
			// We assume the consumed stage for a flop is the cycle prior for both flops and latches.
			UsedBitRange range = new UsedBitRange(stageNumber - 2, gating_sig);
			gating_sig.addUsedRange(range);
		}
		
		
		
		if(type == "FLOP1")   // TODO: UGLY!!!  This == comparison shouldn't even work!!!  Should need .equals().
			return "FLOP2";
		else
			return "FLOP1";
	}
	
	
	public void printSignalDeclaration()
	{
		// TODO: I don't think this is legal SV if the bit range is incomplete and there is scope range, is it?
		for(int i=0;i<ranges.size();i++)
		{
			if(ranges.get(i).getType() == "FLOP2" || ranges.get(i).getType() == "LATCH")
			{
				pipesignal.declareSvSignal(ranges.get(i), stageNumber);
			}
		}
	}
	
	// A simplified version of the above method which just declares each stage to use full-width.  This is used
	// when the assigned range cannot be evaluated.
	public void printSignalDeclarationSimplified()
	{
		// TODO: I don't think this is legal SV if the bit range is incomplete and there is scope range, is it?
		for(int i=0;i<ranges.size();i++)
		{
			if(ranges.get(i).getType() == "FLOP2" || ranges.get(i).getType() == "LATCH")
			{
				pipesignal.declareSvSignal(ranges.get(i), stageNumber);
			}
		}
	}

	//This method is used to print the staging flop or latch for this TransitionStage
	public void printFlopOrLatch()
	{
		String used;

		for(int i=0;i<ranges.size();i++)
		{
			//if(ranges.get(i).isAnchored() == true)
			//	used = "  //used";
			//else
				used = "";
			
			LogicalWhen when = pipesignal.getGatingWhen();
			String clock_name = pipesignal.svProducingClockSignalName(stageNumber);
			String clock_ref = pipesignal.svProducingClockReference(stageNumber);
			if ((when != null) &&                        // Gated &&
			    (ranges.get(i).getType() == "FLOP2" ||   // there's a flop or latch in this stage
			     ranges.get(i).getType() == "LATCH")
			   )
			{
				PipeSignal gating_sig = when.getGatingPipeSignal();
				LogicalBehScope gater_scope = gating_sig.getBehScope();

				// Make the clock if it hasn't already been made.
				TreeMap <String, Clock> clocks = gater_scope.clocks;
				if (!clocks.containsKey(clock_name))
				{
					// This clock doesn't exist yet.  Create it.
					if (pipesignal.getIdentifier().getType() == IdentifierType.STATE_SIG)
					{
						clocks.put(clock_name, new Clock(null, gating_sig, gater_scope, stageNumber));
					} else
					{
						clocks.put(clock_name, new Clock(gating_sig, null, gater_scope, stageNumber));
					}
				}
			}
			
			// TODO: Move to ProjSpecific.
			String range_str = ranges.get(i).toStringUsage();
			if(ranges.get(i).getType() == "FLOP2")
			{
				pipesignal.printSVstaging(
				    Main.projSpecific.makeFlop(
					   pipesignal.svSignalReference(stageNumber)     + range_str,
		               pipesignal.svSignalReference(stageNumber - 2) + range_str,
		               clock_ref) +
		            used);
			}
			else if(ranges.get(i).getType() == "LATCH")
			{
				pipesignal.printSVstaging(
				    Main.projSpecific.makeLatch(
				        pipesignal.svSignalReference(stageNumber)     + range_str,
				        pipesignal.svSignalReference(stageNumber - 1) + range_str,
				        clock_ref) +
				    used);
			}	
		}
	}
	
	public void printSVdeclaration(String line)
	{
		SourceFile file = pipesignal.getPipeline().getSourceFile();
		file.printlnSVdeclaration(line);
	}

}
