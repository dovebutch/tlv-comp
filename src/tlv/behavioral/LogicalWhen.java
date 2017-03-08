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

import java.util.*;

import tlv.behavioral.range.*;
import tlv.config.IdentifierType;
import tlv.parse.*;
import tlv.parse.identifier.*;
import tlv.utilities.Severity;

//Used only in new methodology
public class LogicalWhen extends LogicalBranch
{
	//private String label; //label that is common among all identifiers for the instance of this ParseWhen	
	
	// TODO: Hoover: I'm not using these four structures Yura created.
	private Hashtable<String, LogicalWhen> nestedWhens;
	private Hashtable<String, Hashtable<String, LogicalWhen>> nestedWhens_ofSignal;
	//stages which gated by this when
	private Vector<LogicalStage> nestedStages;
	private Hashtable<String, Vector <LogicalStage>> nestedStages_ofSignal;
	
	
	/**
	 * Containing LogicalBehScope.
	 */
	private LogicalBehScope beh_scope;
	
	/**
	 * The gating signal.  (It must be a single bit with a single assignment).
	 * For nested when's this could be a manufactured signal.
	 */
	private PipeSignal gatingPipeSignal;
	
	
	/**
	 * Constructs a new LogicalWhen object
	 * 
	 * @param parse_when ParseWhen from which to create this LogicalWhen
	 * @param beh_scope_ LogicalBehScope containing this LogicalWhen
	 */
	public LogicalWhen(ParseWhen parse_when, LogicalBehScope beh_scope_)
	{
		super(parse_when.produceMnemonic());
		
		beh_scope = beh_scope_;
		/* DELETE:
		Identifier when_identifier = parse_when.getIdentifier();
		Identifier gater_identifier = identifier.typeCast((when_identifier.getType() == IdentifierType.WHEN)       ? IdentifierType.PIPE_SIG :
                                                          (when_identifier.getType() == IdentifierType.WHEN_STATE) ? IdentifierType.STATE_SIG : null);
		gatingPipeSignal = beh_scope_.getSignal(gater_identifier);
		           // TODO: This requires signal to be defined in sourcecode prior to this when.
		           //       When identifier is also created in LogicalBehScope.addWhen(...).  Sort this out.
		if (gatingPipeSignal == null)
		{
			ActiveParseContext.Report(0, Severity.ERROR, "WHEN_LABEL", "When with label: " + getLabel() + " does not correspond to a signal with the same label.");
		}
		*/
		Identifier when_sig_ident = identifier.typeCast((identifier.getType() == IdentifierType.WHEN)       ? IdentifierType.PIPE_SIG :
				                                        (identifier.getType() == IdentifierType.WHEN_STATE) ? IdentifierType.STATE_SIG :
				                                        (identifier.getType() == IdentifierType.LEGACY_WHEN)       ? IdentifierType.PIPE_SIG :
						                                (identifier.getType() == IdentifierType.LEGACY_WHEN_STATE) ? IdentifierType.STATE_SIG :
						                                (identifier.getType() == IdentifierType.SV_WHEN)    ? IdentifierType.SV_SIG : null);

		// Add signal to scope.  For SV signals, add to top scope.
		if (when_sig_ident.getType() == IdentifierType.SV_SIG)
	    {
	    	gatingPipeSignal = beh_scope_.getTopScope().addSignal(when_sig_ident);
	    } else
	    {
	    	gatingPipeSignal = beh_scope_.addSignal(when_sig_ident);
	    }
	    gatingPipeSignal.setWhen(this);
		
		nestedWhens = new Hashtable<String, LogicalWhen>();
		nestedWhens_ofSignal = new Hashtable<String, Hashtable<String, LogicalWhen>>();
		
		nestedStages = new Vector<LogicalStage>(0,0);
		nestedStages_ofSignal = new Hashtable<String, Vector<LogicalStage>>();
		
	}
	
	public PipeSignal getGatingPipeSignal() {
		return gatingPipeSignal;
	}

	public void setGatingPipeSignal(PipeSignal gatingPipeSignal) {
		gatingPipeSignal = gatingPipeSignal;
	}

	//Adds another identifier ParseMnemonic linking the LogicalWhen to another ParseWhen
	public boolean addIdentifier(ParseMnemonic identifier)
	{
		if(getLabel().compareTo(identifier.getLabel()) == 0)
		{
			parse_elements.add(identifier);
			return true;
		}
		else
			return false;
	}
	
	public LogicalStage getStage(int stage_number_)
	{
		return nestedStages.elementAt(stage_number_ - nestedStages.firstElement().getValue());
	}
	
	public LogicalStage addStage(LogicalStage logical_stage_)
	{
		int stageOffset;
		
		if(nestedStages.size() == 0)
		{
			nestedStages.insertElementAt(logical_stage_, 0);
		}
		else
		{
			stageOffset = nestedStages.firstElement().getValue();
			
			if(logical_stage_.getValue() < stageOffset)
			{
				nestedStages.insertElementAt(logical_stage_, 0);
				
				for(int i = 1; i < stageOffset-logical_stage_.getValue(); i++)
					nestedStages.insertElementAt(new LogicalStage(logical_stage_.getValue() + i, this), i);
			}
			else if(logical_stage_.getValue() >= nestedStages.size() + stageOffset)
			{
				for(int i = nestedStages.size()+stageOffset; i < logical_stage_.getValue(); i++)
				{
					nestedStages.add(new LogicalStage(i, this));
				}
	
				nestedStages.add(logical_stage_);
			}
		}
		
		return nestedStages.get(logical_stage_.getValue() - nestedStages.firstElement().getValue());
	}
	
	public LogicalStage addStage_ofSignal(String label, LogicalStage logical_stage_)
	{
		int stageOffset;
		Vector<LogicalStage> nested_stages_ofSignal = nestedStages_ofSignal.remove(label);
		
		if(nested_stages_ofSignal == null)
		{
			nested_stages_ofSignal = new Vector<LogicalStage>(0,0);
		}
		
		if(nested_stages_ofSignal.size() == 0)
		{
			nested_stages_ofSignal.insertElementAt(logical_stage_, 0);
		}
		else
		{
			stageOffset = nested_stages_ofSignal.firstElement().getValue();
			
			if(logical_stage_.getValue() < stageOffset)
			{
				nested_stages_ofSignal.insertElementAt(logical_stage_, 0);
				
				for(int i = 1; i < stageOffset-logical_stage_.getValue(); i++)
					nested_stages_ofSignal.insertElementAt(new LogicalStage(logical_stage_.getValue() + i, this), i);
			}
			else if(logical_stage_.getValue() >= nested_stages_ofSignal.size() + stageOffset)
			{
				for(int i = nested_stages_ofSignal.size()+stageOffset; i < logical_stage_.getValue(); i++)
				{
					nested_stages_ofSignal.add(new LogicalStage(i, this));
				}
	
				nested_stages_ofSignal.add(logical_stage_);
			}
		}
		
		nestedStages_ofSignal.put(label, nested_stages_ofSignal);
		
		return nestedStages_ofSignal.get(label).get(logical_stage_.getValue() - nestedStages_ofSignal.get(label).firstElement().getValue());
	}
	
	public LogicalStage addStage(ParseStage parseStage)
	{
		Expression temp_identifier = parseStage.produceExpression();
		LogicalStage temp_logical_stage = null;
		int stageOffset;
		
		if(!temp_identifier.isEvaluated())
		{
			return null;
		}
		
		if(nestedStages.size() == 0)
		{
			stageOffset = temp_identifier.getValue();
			
			temp_logical_stage = new LogicalStage(temp_identifier);
			temp_identifier.setLogicalNode(temp_logical_stage);
			
			nestedStages.insertElementAt(temp_logical_stage, 0);
		}
		else
		{
			stageOffset = nestedStages.firstElement().getValue();
			
			if(temp_identifier.getValue() < stageOffset)
			{
				temp_logical_stage = new LogicalStage(temp_identifier);
				temp_identifier.setLogicalNode(temp_logical_stage);
				nestedStages.insertElementAt(temp_logical_stage, 0);
				
				for(int i = 1; i < stageOffset-temp_identifier.getValue(); i++)
					nestedStages.insertElementAt(new LogicalStage(temp_identifier.getValue() + i, this), i);
			}
			else if(temp_identifier.getValue() >= nestedStages.size() + stageOffset)
			{
				for(int i = nestedStages.size()+stageOffset; i < temp_identifier.getValue(); i++)
				{
					nestedStages.add(new LogicalStage(i, this));
				}
				
				temp_logical_stage = new LogicalStage(temp_identifier);
				temp_identifier.setLogicalNode(temp_logical_stage);
				nestedStages.add(temp_logical_stage);
			}
			else
			{
				temp_logical_stage = nestedStages.get(temp_identifier.getValue() - stageOffset);
				temp_identifier.setLogicalNode(temp_logical_stage);
				temp_logical_stage.addIdentifier(temp_identifier);
			}
		}
		
		return nestedStages.get(temp_identifier.getValue() - nestedStages.firstElement().getValue());
		
	}
	
	public void addWhen(String signal_name, String when_label)
	{
		Hashtable <String, LogicalWhen> temp_whens = nestedWhens_ofSignal.get(signal_name);
		LogicalWhen temp_when;
		
		
		//if no whens exist for the specific signal_name, create a new list
		if(temp_whens == null)
		{
			temp_whens = new Hashtable <String, LogicalWhen>(0);
			nestedWhens_ofSignal.put(signal_name, temp_whens);
		}
		
		temp_when = temp_whens.get(when_label);
		
		if(temp_when == null)
		{
			temp_when = nestedWhens.get(when_label);
			temp_whens.put(when_label, temp_when);	
		}
	}
	
	/**
	 * Process a new ParseWhen below this Logical When.
	 * 
	 * @param parseWhen
	 * @return
	 */
	public LogicalWhen addParseWhen(ParseWhen parseWhen, LogicalBehScope beh_scope)
	{
		ParseMnemonic temp_identifier = parseWhen.produceMnemonic();
		LogicalWhen temp_logical_when = null;
		
		if(nestedWhens.contains(temp_identifier.getLabel()))
		{
			temp_logical_when = nestedWhens.get(temp_identifier.getLabel());
			temp_identifier.setLogicalNode(temp_logical_when);
			temp_logical_when.addIdentifier(temp_identifier);
		}
		else
		{
			temp_logical_when = new LogicalWhen(parseWhen, beh_scope);
			temp_identifier.setLogicalNode(temp_logical_when);
			nestedWhens.put(temp_identifier.getLabel(), temp_logical_when);
		}
		
		return temp_logical_when;
	}
	
	public void addAssignedBitRange(BitRange range)
	{
		PipeSignal temp_signal = null;
		LogicalWhen temp_when1 = this;
		LogicalWhen temp_when2;
		LogicalStage temp_stage = addStage_ofSignal(range.getLabel(), range.getLogicalStage());
		temp_stage.addAssignedRange(range);
		
		
		//temp_stage.addTransitionRange(pipesignal_, end_index_, start_index_, anchor_, type_)
		
		while(temp_when1.getBranchWhen() != null)
		{
			temp_when2 = temp_when1.getBranchWhen();
			temp_when2.addWhen(range.getLabel(), temp_when1.getLabel());
			temp_when1 = temp_when2;
		}
		
		temp_signal = getBehScope().addSignal(range.getPipeSignal().getIdentifier());
		temp_signal.addGatingWhen(temp_when1);
	}
	
	public void addUsedBitRange(UsedBitRange range)
	{
		LogicalWhen temp_when1 = this;
		LogicalWhen temp_when2;
		
		int temp_stage_value = range.getStageValue();
			
		LogicalStage temp_stage = addStage(new LogicalStage(temp_stage_value, this));
		temp_stage = addStage_ofSignal(range.getLabel(), temp_stage);
		temp_stage.addUsedRange(range);
		
		while(temp_when1.getBranchWhen() != null)
		{
			temp_when2 = temp_when1.getBranchWhen();
			temp_when2.addWhen(range.getLabel(), temp_when1.getLabel());
			temp_when1 = temp_when2;
		}
		
		//temp_signal = getBranchPipeline().addSignal(range.getLabel());
		//temp_signal.addGatingWhen(temp_when1);
	}
	
	public LogicalWhen getBranchWhen()
	{
		if(parent instanceof LogicalWhen)
			return (LogicalWhen)parent;
		else
			return null;
	}
	
	public LogicalBehScope getBehScope() {
		return beh_scope;
	}

	public void setBehScope(LogicalBehScope beh_scope) {
		this.beh_scope = beh_scope;
	}

	public Vector <LogicalStage> getNestedStages_ofSignal(String label)
	{
		return nestedStages_ofSignal.get(label);
	}
	
	public Hashtable <String, LogicalWhen> getNestedWhens_ofSignal(String label)
	{
		return nestedWhens_ofSignal.get(label);
	}
	
	public void printLogicalHierchy(String label_, int indentation)
	{
		String assigned_ranges = "";
		String used_ranges = "";
		
		Vector<BitRange> temp_assigned_ranges;
		Vector<UsedBitRange> temp_used_ranges;
		
		String indent_string = "";
		Vector <LogicalStage> temp_stages = nestedStages_ofSignal.get(label_);
		Hashtable <String, LogicalWhen> temp_whens = nestedWhens_ofSignal.get(label_);
		LogicalWhen temp_when;
		
		for(int i = 0; i < indentation; i++)
		{
			indent_string += " ";
		}
		
		System.out.println(indent_string + "?" + this.getLabel());
		if(temp_stages != null)
			for(int j = 0; j < temp_stages.size(); j++)
			{
				temp_assigned_ranges = temp_stages.get(j).getAssignedBitRanges_ofSignal(label_);
				temp_used_ranges = temp_stages.get(j).getUsedBitRanges_ofSignal(label_);
					
				if(temp_assigned_ranges != null)
					for(int k = 0; k < temp_assigned_ranges.size(); k++)
					{
						assigned_ranges += "A" + temp_assigned_ranges.get(k).toString() + ", ";
					}
					
				if(temp_used_ranges != null)
					for(int k = 0; k < temp_used_ranges.size(); k++)
					{
						used_ranges += "U" + temp_used_ranges.get(k).toString() + ", ";
					}
						
				System.out.println(indent_string + "  @" + temp_stages.get(j).getValue() + ": " + assigned_ranges + used_ranges);
					
				assigned_ranges = "";
				used_ranges = "";
			}

		if(temp_whens != null)
			for(Enumeration<LogicalWhen> e = temp_whens.elements(); e.hasMoreElements();)
			{
				temp_when = e.nextElement();
				temp_when.printLogicalHierchy(label_, indentation + 4);
			}
	}
	
	/**
	 * Generate the clocks for this LogicalWhen.
	 * TODO: Nested LogicalWhens are not currently supported.
	 */
	// TODO: Kill this.
	public void generateClocks()
	{
		for(int i = 0; i < nestedStages.size(); i++)
		{
			if (i < 2)
			{
				// The gating condition for a when must be available the cycle before...
			}
			else
			{
				// Since this must be a single-bit signal, the first stage must be an assignment,
				// and subsequent ones must be uses.
				// TODO: Check the above.
				// The staging of the gating signal is generated elsewhere.  We rely on the gating
				// signal to exist one cycle prior to each use.
				// ...
			}
		}
	}
	
	public void printAllStages(int indentation)
	{
		LogicalWhen temp_when;
		String indent_string = "";
		
		for(int i = 0; i < indentation; i++)
		{
			indent_string += " ";
		}
		
		System.out.println(indent_string + "?" + getLabel());
		
		for(int i = 0; i < nestedStages.size(); i++)
		{
			System.out.println(indent_string + "  @" + nestedStages.get(i).getValue());
		}
		
		if(nestedWhens != null)
			for(Enumeration<LogicalWhen> e = nestedWhens.elements(); e.hasMoreElements();)
			{
				temp_when = e.nextElement();
				temp_when.printAllStages(indentation + 4);
			}
	}
}
