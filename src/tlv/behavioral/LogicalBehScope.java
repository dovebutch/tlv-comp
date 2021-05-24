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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

//import javax.xml.ws.handler.MessageContext.Scope;

import tlv.Main;
import tlv.behavioral.range.BitRange;
import tlv.behavioral.range.UsedBitRange;
import tlv.behavioral.range.Range;
import tlv.behavioral.range.WildcardedSigRef;
import tlv.behavioral.Clock;
import tlv.config.IdentifierType;
import tlv.parse.ActiveParseContext;
import tlv.parse.Assignment;
import tlv.parse.NodeType;
import tlv.parse.ParseBehHier;
import tlv.parse.ParseBehScope;
import tlv.parse.ParseBranch;
import tlv.parse.ParseContext;
import tlv.parse.ParsePipeline;
import tlv.parse.ParseStage;
import tlv.parse.ParseSvType;
import tlv.parse.ParseWhen;
import tlv.parse.Prefix;
import tlv.parse.identifier.Expression;
import tlv.parse.identifier.Identifier;
import tlv.parse.identifier.ParseElement;
import tlv.parse.identifier.ParseMnemonic;
import tlv.parse.identifier.Identifier.Syntax;
import tlv.utilities.Severity;

/**
 * A common base class for Logical* classes which represent behavioral hierarchy.  For example, a signal reference such as
 * >core[2]|instr$valid includes a path consisting of the behavioral hierarchy of the signal.  Behavioral hierarchy includes
 * LogicalPipeline and LogicalBehHier.  Adds a Hashtable of children.
 * 
 * @author ypyatnyc
 *
 */
public class LogicalBehScope extends LogicalBranch
{
	/**
	 * A vector of LogicalStages where the index in the vector determines the containing Stage's value.
	 * The offset for these values is determined by the value of the stage at index 0 in the vector
	 */
	private Vector<LogicalStage> ungatedStages;
	/**
	 * All gating whens which are defined directly below the Pipeline scope.
	 */
	private Hashtable<String, LogicalWhen> whens;
	
	/**
	 * A list of all the PipeSignals which exist with the Pipeline scope. The PipeSignal track
	 * their UsedBitRanges and AssignedBitRanges to create the logic graph.
	 * Note, that this structure may contain wildcarded signals, which construct a flow graph.  Wildcarded signals are
	 * evaluated to individual signals, which are also contained in (or added to) this structure.  The process of
	 * expanding wildcarded signals is defined in SourceFile.createStagingRecursive() (though this reference is bound to grow stale.
	 */
	private Hashtable<Identifier, PipeSignal> signals;
	// TODO: Replace above with this:
	//private HashMap<Identifier, PipeSignal> signals;
	
	/**
	 * Provides a link to behavioral scope children.
	 */
	public Hashtable <String, LogicalBehScope> children = new Hashtable <String, LogicalBehScope>();
	
	// TODO: Need to pull these out in sorted order.  Find an appropriate data structure.
	// TODO: This structure is populated as flops are generated.  To support dynamic editing, there
	//       should be clock signals, derived from PipeSignal, with appropriate uses and assignments.
	//       With the current structure, there would be no way to know which clocks to delete when
	//       logic is deleted.
	/**
	 * Clocks at this scope (meaning, whose enable signals have this scope).
	 */
	public TreeMap <String, Clock> clocks;
	
	
	/**
	 * The range.  For now, every ParseBehScope must have the full range.  The first must provide the range, and
	 * subsequent ones must use '[*]'.
	 */
	private Range range;
	
	private String stringSV = null;  // SV string for this scope or null (for loop over the range if there is one).
	
	// Note, these must be nullified if parent scope changes.  Not currently supported.
	public  String sv_scope_str = null;         // For use by ProjSpecific only.
	private String sv_scope_ranges_str = null;  // Captures string of SV range specifications for this scope for use in signal declarations.
	
	/**
	 * Creates a LogicalBehScope from a ParseElement identifier object.
	 * 
	 * @param identifier_ ParseElement derived from the newely constructed LogicalBranch's associated
	 * ParseNode.
	 */
	public LogicalBehScope(ParseElement identifier_)
	{
		super(identifier_);
		
		ungatedStages = new Vector<LogicalStage> (0,0);
		whens  = new Hashtable<String, LogicalWhen> ();
		
		signals = new Hashtable <Identifier, PipeSignal>();
		clocks = new TreeMap <String, Clock>();
	}
	
	public LogicalBehScope(ParseElement identifier_, ParseBehScope parse_scope_, LogicalBranch parent_)
	{
		this(identifier_);
		range = parse_scope_.getRange();
		parent = parent_;
	}
	
	public LogicalBehScope(ParseElement identifier_, Range range_, LogicalBranch parent_)
	{
		this(identifier_);
		range = range_;
		parent = parent_;
	}
	
	/**
	 * Creates an "dummy" LogicalBehScope of specific NodeType and links it with a top LogicalBranch
	 *
	 * @param logical_branch_ parent LogicalBranch object
	 * @param t NodeType of the created LogicalBranch
	 */
	/*
	public LogicalBehScope(LogicalBranch logical_branch_, NodeType t)
	{
		super(logical_branch_, t);
	}
	*/
	
	/**
	 * Creates an "dummy" LogicalBranch and links it with a top LogicalBranch
	 *
	 * @param logical_branch_ parent LogicalBranch object
	 */
	/*
	public LogicalBehScope(LogicalBranch logical_branch_)
	{
		super(logical_branch_);
	}
	*/

	public LogicalBehScope(Identifier ident_, LogicalBranch logical_branch_, NodeType t) {
		super(ident_, logical_branch_, t);
		
		ungatedStages = new Vector<LogicalStage> (0,0);
		whens  = new Hashtable<String, LogicalWhen> ();
		
		signals = new Hashtable <Identifier, PipeSignal>();
		clocks = new TreeMap <String, Clock>();
	}
	
	public void setRange(Range range_)
	{
		range = range_;
	}
	public Range getRange()
	{
		return range;
	}
	
	/**
	 * @return Parent LogicalBehScope.
	 */
	private boolean parent_scope_set = false;
	private LogicalBehScope parent_scope = null;
	public LogicalBehScope getParentScope()
	{
		if (!parent_scope_set)
		{
			LogicalBranch ret_br = this;
			do
			{
				ret_br = ret_br.getParent();
				parent_scope = (ret_br == null) ? null : (LogicalBehScope)ret_br;
			} while ((ret_br != null) && (parent_scope == null));
			
			parent_scope_set = true;
		}
		return parent_scope;
	}
	
	public LogicalBehScope getTopScope()
	{
		return (parent == null) ? this : parent.getTopScope();
	}

	public LogicalBehScope findSelfOrAncestorBehScope(Identifier ident)
	{
		for(LogicalBehScope ancestor_scope = this;
			ancestor_scope != null;
			ancestor_scope = ancestor_scope.getAncestorBehScope()
		   )
		{
			// Does the scope match?
			if (ident.equals(ancestor_scope.getIdentifier()))
			{
				// Scope matches
				return ancestor_scope;
			}
		}
		return null;
	}
	
	public String toString()
	{
		return identifier.toString();
	}
	
	public String toScopedString()
	{
		LogicalBehScope parent = getParentScope();
		return (parent == null) ? "" : (parent.toScopedString() + toString());
	}
	
	public String getSvLoopVar()
	{
		return identifier.getNameVarient(Identifier.Syntax.LOWER_CASE);
	}
	
	/**
	 * Return the appropriate SV string (for loop) without indentation and without '\n' for this scope, or null if there is no range.
	 * @return
	 */
	public String getSVString()
	{
		if (stringSV == null)
		{
			stringSV = getSourceFile().loopBeginStr(this);
			/*-
			// Create string.
			// Determine loop variable name.
			// TODO: For now we use the lower-case name of the behavioral hierarchy, and this can be used in assignment statements.
			//       But, this could conflict with an SV signal name, so we probably need to create an TLV variable are require that it
			//       be used in assignment statements.
			String loop_var = getSvLoopVar();
			getSourceFile().requireGenvar(loop_var);
			stringSV = "for (" + loop_var + " = " + range.getStartBitIndex().getLabel() + "; " + loop_var + " <= " + range.getEndBitIndex().getLabel() + "; " + loop_var + "++) begin";
            */
		}
		return stringSV;
	}
	
	public boolean isTop()
	{
		// return identifier == null;   <-- Faster, but currently, no identifier for Pipelines.
		return getParentScope() == null;
	}
	
	// Returns range specifications for SV signal declarations within this scope.
	public String getSvRangesStr()
	{
		String ret = "";
		if (!isTop())
		{
			ret = getParentScope().getSvRangesStr();
		}
		
		if (getRange() != null)
		{
			ret += getRange().toString();
		}
		
		return ret;
	}
	
	private String sv_index_str = null;
	/**
	 * @return index specifications for SV signal references within this scope.
	 */
	public String getSvIndexStr()
	{
		if (sv_index_str == null)
		{
			sv_index_str = isTop()
								? ""
								: getParentScope().getSvIndexStr();
		
			if (getRange() != null)
			{
				sv_index_str += "[" + getSvLoopVar() + "]";
			}
		}
		
		return sv_index_str;
	}
	
	public String getParentIndexStr()
	{
		if (!isTop())
		{
			LogicalBehScope ancestor_parent_scope = getAncestorBehScope();
			if (!ancestor_parent_scope.isTop())
			{
				return ancestor_parent_scope.getSvIndexStr();
			}
		}
		return "";
	}
	
	/**
	 * Adds a pre-existing LogicalStage object into the ungatedStages vector. This method
	 * is used to add a "dummy" LogicalStage which may not be associated with any ParseStage
	 * This method places the LogicalStage appropriately in the vector according to its value.
	 * 
	 * @param logical_stage_ LogicalStage to be added
	 * @return LogicalStage that was just added
	 */
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
					ungatedStages.insertElementAt(new LogicalStage(logical_stage_.getValue() + i, this), i);
			}
			else if(logical_stage_.getValue() >= ungatedStages.size() + stageOffset)
			{
				for(int i = ungatedStages.size()+stageOffset; i < logical_stage_.getValue(); i++)
				{
					ungatedStages.add(new LogicalStage(i, this));
				}
	
				ungatedStages.add(logical_stage_);
			}
		}
		
		// TODO: This just gets logical_stage_ back again?  Should be an assertion, at best.
		return ungatedStages.get(logical_stage_.getValue() - ungatedStages.firstElement().getValue());
	}
	
	
	/**
	 * Derives a LogicalStage from the ParseStage parameter object. This is either done by locating in the
	 * logicalStages vector and adding parseStage's identifier to the found LogicalStage object, or if a
	 * matching LogicalStage object is not found, than it is constructed and added to logicalStages vector.
	 *
	 * @param parseStage ParseStage from which the LogicalStage needs to be derived from
	 * @return LogicalStage that is added to the LogicalPipeline
	 */
	public LogicalStage addStage(ParseStage parseStage)
	{
		Expression temp_identifier = parseStage.produceExpression();
		LogicalStage temp_logical_stage = null;
		int stageOffset;
		
		if(!temp_identifier.isEvaluated())
		{
			return null;
		}
		
		if(ungatedStages.size() == 0)
		{
			temp_logical_stage = new LogicalStage(temp_identifier);
			temp_identifier.setLogicalNode(temp_logical_stage);
			
			ungatedStages.insertElementAt(temp_logical_stage, 0);
		}
		else
		{
			stageOffset = ungatedStages.firstElement().getValue();
			
			if(temp_identifier.getValue() < stageOffset)
			{
				temp_logical_stage = new LogicalStage(temp_identifier);
				temp_identifier.setLogicalNode(temp_logical_stage);
				ungatedStages.insertElementAt(temp_logical_stage, 0);
				
				for(int i = 1; i < stageOffset-temp_identifier.getValue(); i++)
					ungatedStages.insertElementAt(new LogicalStage(temp_identifier.getValue() + i, this), i);
			}
			else if(temp_identifier.getValue() >= ungatedStages.size() + stageOffset)
			{
				for(int i = ungatedStages.size()+stageOffset; i < temp_identifier.getValue(); i++)
				{
					ungatedStages.add(new LogicalStage(i, this));
				}
				
				temp_logical_stage = new LogicalStage(temp_identifier);
				temp_identifier.setLogicalNode(temp_logical_stage);
				ungatedStages.add(temp_logical_stage);
			}
			else
			{
				temp_logical_stage = ungatedStages.get(temp_identifier.getValue() - stageOffset);
				temp_identifier.setLogicalNode(temp_logical_stage);
				temp_logical_stage.addIdentifier(temp_identifier);
			}
		}
		
		return ungatedStages.get(temp_identifier.getValue() - ungatedStages.firstElement().getValue());
	}
	
	/**
	 * Returns a LogicalStage object of a specific stage value.
	 * May need to add error checking in case this method gets called to retrieve a stage that does not exist within
	 * the Vector
	 * 
	 * @param stage_number_ the stage number of the LogicalStage that gets retrieved
	 * 
	 * @return LogicalStage with the value of stage_number_
	 */
	public LogicalStage getStage(int stage_number_)
	{
		return ungatedStages.elementAt(stage_number_ - ungatedStages.firstElement().getValue());
	}
	
	/**
	 * This method takes in a ParseWhen object and generates a LogicalWhen which it then puts
	 * in the nestedWhens Hashtable. The ParseWhen must be nested immediately under
	 * the ParseBehScope that is associated with this LogicalBehScope.
	 * 
	 * @param parseWhen the source node for creating the LogicalWhen
	 * @return the LogicalWhen that was created from the ParseWhen and added under the PipeSignal
	 */
	public LogicalWhen addWhen(ParseWhen parseWhen)
	{
		ParseMnemonic temp_identifier = parseWhen.produceMnemonic();
		LogicalWhen temp_logical_when = whens.get(parseWhen.getIdentifier().getName());
		
		if(temp_logical_when != null)
		{
			temp_identifier.setLogicalNode(temp_logical_when);
			temp_logical_when.addIdentifier(temp_identifier);
		}
		else
		{
			temp_logical_when = new LogicalWhen(parseWhen, this);
			temp_identifier.setLogicalNode(temp_logical_when);
			whens.put(temp_identifier.getLabel(), temp_logical_when);
		}
		
		return whens.get(temp_identifier.getLabel());
	}
	
	public LogicalWhen getWhen(String label_)
	{
		return whens.get(label_);
	}
	
	//This method adds an AssignedBitRange to the new LogicalStructure
	public void addAssignedBitRange(BitRange range)
	{
		//Creates or retrieves a PipeSignal with the same name as the input range
		PipeSignal temp_signal = addSignal(range.getPipeSignal().getIdentifier());
		
		//Adds or retrieves a LogicalStage object with the same stage scope as the BitRange
		LogicalStage temp_stage = addStage(range.getLogicalStage());
		
		//Adds the retrieved stage to the PipeSignal
		temp_signal.addStage(temp_stage);
		
		//Adds the range to the retrieved stage
		temp_stage.addAssignedRange(range);
	}
	
	//This method adds a UsedBitRange to the new LogicalStructure
	public void addUsedBitRange(UsedBitRange range)
	{
		//Creates or retrieves a PipeSignal with the same name as the input range
		PipeSignal temp_signal = addSignal(range.getPipeSignal().getIdentifier());
		
		//gets the stage value associated with the range(may be effected by alignment)
		int temp_stage_value = range.getStageValue();
		
		//Create and add a LogicalStage with that stage value.
		LogicalStage temp_stage = addStage(new LogicalStage(temp_stage_value, this));
		
		//Add the stage to the PipeSignal
		temp_signal.addStage(temp_stage);
		
		//Add the UsedBitRange to the LogicalStage
		temp_stage.addUsedRange(range);
	}
	
	//Older working methodology, used for creating and maintaining assignedRanges list
	//within PipeSignal objects
	public void addAssignedRange(BitRange range)
	{
		PipeSignal temp_signal = addSignal(range.getPipeSignal().getIdentifier());
		temp_signal.addAssignedRange(range);
	}
	
	//Older working methodology, used for creating and maintaining usedRanges list
	//within PipeSignal objects
	public void addUsedRange(UsedBitRange range)
	{
		PipeSignal temp_signal = addSignal(range.getPipeSignal().getIdentifier());
		temp_signal.addUsedRange(range);
	}
	
	/**
	 * @return The string representing the SV variable name to be used as an instance variable.
	 *
	public String getInstanceVar()
	{
		return getLabel();  // TODO: Give this more thought.
	}*/
	
	public Hashtable<Identifier, PipeSignal> getSignals()
	{
		return signals;
	}
	
	public PipeSignal getSignal(Identifier ident_)
	{
		return signals.get(ident_);
	}
	
	/**
	 * Adds a PipeSignal of a specified name to the containing LogicalBehScope and then returns it back
	 * Checks to see if the signals Hashtable already contains a PipeSignal of specific label.
	 * If it does not this method creates a new PipeSignal with that label and adds it to the
	 * signals hashtable. In the end the method returns the PipeSignal with the specified label.
	 * 
	 * @param label name of the PipeSignal
	 * @param unstaged True if the signal is an unstaged SV signal
	 * 
	 * @return PipeSignal returned from the signals list with with specified label
	 */
	public PipeSignal addSignal(Identifier identifier, Identifier struct)
	{	
		PipeSignal temp_signal = signals.get(identifier.getName());
		
		if(temp_signal == null)
		{
			// Signal not found, create and add it.
			LogicalPipeline pipe = (LogicalPipeline)getSelfOrAncestorOfType(NodeType.PIPELINE);
		    // TODO: Might want to allow signals to fall into a default pipeline/stage.
		    ActiveParseContext.ReportIf((pipe == null) && !isTop() /* SV signals are isTop() */, 0, Severity.FATAL_ERROR, "NO-PIPE", "Signal with no associated pipeline.");

			temp_signal = new PipeSignal(this, pipe, identifier, struct);
			signals.put(identifier, temp_signal);
		}
		
		//syntax error, cannot have same names for different typed signals/structs
		if(temp_signal.getSvDataTypeIdentifier() != null || struct != null)
			ActiveParseContext.ReportIf(temp_signal.getSvDataTypeIdentifier() == null || struct == null || !temp_signal.getSvDataTypeIdentifier().equals(struct),
					              0, Severity.FATAL_ERROR, "NAMING-CONFLICT", "Structs/Signals of different types may not share the same name.");
		
		return temp_signal;
	}
	
	public PipeSignal addSignal(Identifier identifier)
	{	
		PipeSignal temp_signal = signals.get(identifier);
		
		if(temp_signal == null)
		{
			// Signal not found, create and add it.
			LogicalPipeline pipe = (LogicalPipeline)getSelfOrAncestorOfType(NodeType.PIPELINE);
		    // TODO: Might want to allow signals to fall into a default pipeline/stage.
		    ActiveParseContext.ReportIf((pipe == null) && !isTop() /* SV signals are isTop() */, 0, Severity.FATAL_ERROR, "NO-PIPE", "Signal with no associated pipeline.");

			temp_signal = new PipeSignal(this, pipe, identifier, null);
			signals.put(identifier, temp_signal);
		}
		
		return temp_signal;
	}
	
	public PipeSignal addStruct(ParseSvType parseSvType)
	{
		Identifier signal_identifier = parseSvType.getSignalIdentifier();
		
		PipeSignal temp_signal = signals.get(signal_identifier);
		
		if(temp_signal == null)
		{
			// Signal not found, create and add it.
			LogicalPipeline pipe = (LogicalPipeline)getSelfOrAncestorOfType(NodeType.PIPELINE);
		    // TODO: Might want to allow signals to fall into a default pipeline/stage.
		    ActiveParseContext.ReportIf((pipe == null) && !isTop() /* SV signals are isTop() */, 0, Severity.FATAL_ERROR, "NO-PIPE", "Signal with no associated pipeline.");

			temp_signal = new PipeSignal(this, pipe, signal_identifier, parseSvType.getSvDataTypeIdentifier());
			signals.put(signal_identifier, temp_signal);
		}
		else if(!temp_signal.getSvDataTypeIdentifier().equals(parseSvType.getSvDataTypeIdentifier()))
		{
			//TODO: syntax error, cannot have same names for different typed signals/structs
		}
		
		return temp_signal;
	}

	
	
	
	/*
	// For searches for assignments through wildcarded assignments.  These states are maintained as the search is performed.
	// TODO: Threading needs consideration.
	
	// TODO: Crap, this is the wrong approach; scrap these two...
	 **
	 * Indicates that all uses in this scope have identified their producers and these signals have been reflected in corresponding wildcarded signal paths.
	 * TODO: This should, at some point, be replaced by a more-general evaluation level notion (and searching_flow).
	 *
	private boolean done_staging = false;
	private boolean reached_by_flow_search = false;  // Set to true once this scope is reached while generating staging for downstream scopes in wildcard flows.
	                                                 // If search encounters a true value in this variable, a loop has been found.
	                                                 // TODO: Multithreading?
	
	// OLD:
	private Identifier searched_sig = null;  // This is set to the identifier of the signal whose assignment we are searching for.  It is not cleared after the search,
	                                         // at this scope, which allows state to be held that reflects the result of the search at this scope in case the overall
											 // search leads again to this scope (prevents repeated effort and runtime explosion).  To know if we've searched this
											 // scope, we check if the signal we're searching matches this.
											 // By the end of the overall search, the existence of an assignment of this signal at this scope accurately reflects the
											 // ability to find an assignment.  If a this scope has a use, the signal should have been found (or there was an error),
											 // so a and it will be 
	private boolean search_conclusive = false;  // Indicates whether the search has produced a concrete result (assignment found (fruitful) or not).
												// When set to true, the assigned range is added if one was found, so the existence of an assigned range for this
												// signal is a definitive indication of fruitfulness.
												// (Note that fruitful does not imply that the full required range was pulled through to satisfy the current search path.)

	 **
	 * Find a wildcarded assignment statement in this scope updated to provide the required BitRange (which is not otherwise assigned in this scope)
	 * by recursively looking through wildcarded assignments.  Signal uses and assignment are added as a part of the wildcarded assignment statement, and
	 * are also added to the corresponding WildcardedSigRefs.
	 * @param required_assigned_range A BitRange representing the range that must be found in this scope through wildcarded assignment.  This reflects the
	 *                                aggregation of uses at this scope and downstream.  (The ranges association with the scope is not important, which allows
	 *                                the same range to pass through the recursive calls.)
     * @param conclusive There is an expectation as to whether the search should be fruitful, as dictated by the next arg.
     * @param must_fail This is a search that must fail.  We're just searching to find multiple possible paths and report an error if we do.
     * @param need_more_bits This scope was previously searched conclusively with signal pulled through, but now, we need more bits.
	 * @return The BitRange created from to satisfy this request, which considers the required_assigned_range, and the assigned range found.
	 *         If either requested bound could not be evaluated, this is an indication that it may not have been properly determined, and
	 *         the bound from the assignment(s), which must be consistent, will be used in the returned range.  This range is associated
	 *         by this method with the appropriate wildcarded assignment and WildcardedSigRef.
	 *         If no assignments are found, a Boolean is returned:
	 *            A true value indicates that the fanin was entirely from cycles, in which case there was no assignment to properly determine the
	 *            BitRange to return.  If conclusive && !must_fail, the requested range was pulled through.  Otherwise, nothing was pulled through.  Uses
	 *            Along this cycle were not factored into the range pulled through.  They will be separately id
	 *            A false value indicates that the requested signal cannot be provided.
	 *
	public Object * BitRange or Boolean * findAssign(BitRange required_assigned_range, boolean conclusive, boolean must_fail, boolean need_more_bits)
	{
		// Algorithm:
		//
		// This method loops over all wildcarded uses in this scope.  So, it loops over wildcarded assignment statements at this scope, and, within that,
		// over wildcarded uses.
		//
		// As this method recurses, it tracks the variables declared above (and assigned below) to break cycles and avoid rediscovery.
		// A scopes search state is initially set to INCONCLUSIVE or CONCLUSIVE, based on the 'conclusive' parameter to this method.
		//
		// We may need two iterations over each wildcarded use in this Assignment.
		// Why?: Uses can be cyclic, meaning their fanin is entirely from scopes we are currently searching through.
		//    If all uses of this wildcarded assignment are cyclic, we have no assignment on these cycles.  There can
		//    however be an assignment that feeds into the cycles.  We don't know.  If any assignment is found,
		//    this is a fruitful wildcarded assignment and the cyclic uses should pull the signal through the cycles.
		//    This scope's search becomes conclusive once the first non-cyclic use is found.
		// So: We search uses "non-aggressively", meaning cycles will not pull signals through, until the searchwe encounter a non-cyclic
		//     one.  We count how many cyclic paths were searched non-aggressively, and on the second
		//     pass, if the first pass was fruitful, we do that many aggressive searches (assuming the iterator order hasn't
		//     changed), and then stop searching.
		
		// 
		// Example:
		//   Pass |                                    Expectation | Found? | cyc_cnt |
		//   -----+------------------------------------------------+--------+---------+------------------------------------------
		//   1st  | $ANY = $cond1 ? >scope1$ANY     // ?           | Cyclic | 1       |
		//   1st  |                 >scope2$ANY     // ?           | Cyclic | 2       |
		//   1st  | $ANY = $cond2 ? >scope3$ANY     // ?           | !Found | 0       |
		//   1st  |                 >scope4$ANY     // !Found      | !Found | 0       |
		//   1st  | $ANY = $cond3 ? >scope5$ANY     // ?           | Cyclic | 1       |
		//   1st  |                 >scope6$ANY     // ?           | Found  | 1       |
		//   1st  |                 >scope7$ANY     // Found       | Found  | 1       |
		//   2nd  | $ANY = $cond3 ? >scope5$ANY     // Found       | Found  | -       |
		//   1st  | $ANY = $cond4 ? >scope8$ANY     // !Found      | !Found | 0       |

		
		// Record that we are searching this signal.
		searched_sig = required_assigned_range.getPipeSignal().getIdentifier();
		searching_for_assignment = true;
		searching_for_assignment_aggressively = aggressive;
		
		BitRange ret_range = null;
		boolean ret_cyclic = !aggressive;  // 
		
		PipeSignal any_sig = signals.get(Identifier.any_sig_keyword);
		if (any_sig != null)
		{
			Assignment found_any_assignment = null;  // The wildcarded Assignement at this scope that finds this signal.
			
			// For each wildcarded assignment in this scope.  (Note that it would have been reasonable to assume at most one wildcarded assignment per scope.)
			// Must be that all or none of the wildcarded used ranges of this assignment find an assignment.
			Iterator<BitRange> any_assigned_itr = any_sig.getAssignedRangesIterator();
			while (any_assigned_itr.hasNext())
			{
				WildcardedSigRef any_assigned = (WildcardedSigRef)(any_assigned_itr.next());
				Assignment any_assignment = any_assigned.getAssignment();
				
				boolean first_pass = true;
				//-boolean all_cyclic = (ret == null);
				int cyclic_cnt = 0;
				// TODO: Bring this outside the above loop.  Maybe, don't bother w/ cyclic_cnt.  We've tagged the cyclic ones.
				while (first_pass || (cyclic_cnt >= 0))
				{
					//boolean first_iteration = true;
					boolean found = false;
					
					// For each use in this wildcarded assignment.
					for (Assignment.ContainedBitRange wildcarded_use: any_assignment.getUsedRanges())
					{
						UsedBitRange used_range = wildcarded_use.getUsedBitRange();
						PipeSignal any_use_sig = used_range.getPipeSignal();
						if (any_use_sig.getIdentifier() == Identifier.any_sig_keyword)
						{
							// Wildcarded use.
							
							LogicalBehScope use_scope = any_use_sig.getBehScope();
							
							// We may require two searches into this use.
							// Why?
							//    If a cycle leads back to this use scope, there will be a new use at this use scope.  If the cycle picked up additional used bits along the way, there may be more
							//    bits required now at this use scope to satisfy the cycle.  We did not pull those bits at the time the cycle reached this use scope, but instead deferred that
							//    responsibility until we returned to this use scope.  and there were uses in that cycle that required more bits, the
							//for (int i = 0; i < 2; i++)...
							PipeSignal use_sig = use_scope.getSignals().get(required_assigned_range.getPipeSignal().getIdentifier());
							boolean use_previously_searched = use_scope.searched_sig.equals(use_sig);

							// Compare required_assigned_range with the signal in the use scope, if there is one, and set the following, defaulted for no sig in use scope.
							BitRange use_assigned_range = null;
							Expression use_min = null, use_max = null; // The aggregation of existing bits and required ones.
							boolean use_provides = false;
							if (use_sig != null)
							{
								// Signal exists in use scope.
								// Aggregate these together with required_assigned_range (giving priority to the use scope, which is the assignment side of the flow.
								// TODO: We should have an explicit Expression for unknown bounds, rather than carrying around an arbitrary unevaluated one.  Create an Expression Expression.unknown and use when aggregating here and in TransitionStage processing.
								use_max = Expression.greater(use_sig.getIndexHighest(), required_assigned_range.getEndBitIndex()  );
								use_min = Expression.lesser (use_sig.getIndexLowest(),  required_assigned_range.getStartBitIndex());
								use_assigned_range = use_sig.getSingleAssignment();
							}
							// Determine if we need more bits than encountered use_assigned_range provided by a previous search.
							boolean use_needs_more_bits = (((use_assigned_range != null) && use_assigned_range.isSigFromWildcardedSigRef()) &&  // use already has an assigned range from a wildcarded flow
								                           ((use_max != use_sig.getIndexHighest()) || (use_min != use_sig.getIndexLowest()))  // aggregation with required_assigned_range made a difference
								                          );

							if ((use_assigned_range == null) || use_needs_more_bits)
							{
								// No assignment at this use scope or need more bits of the assignment.
								
								// Determine required range in use scope.
								BitRange required_use_range =
									         (use_sig == null)
									             ? required_assigned_range  // Note, this range isn't associated with the use scope, but we don't look at that.
									             ? : new BitRange(use_scope, use_sig, use_max, use_min);
								
								// Expected result.
								boolean use_conclusive = ...;
								boolean expect_found = ...;
								
								// Recurse.
								Object found_obj = findAssign(required_use_range, aggressive, use_needs_more_bits);
								use_assigned_range = Main.<BitRange>castOrNull(found_obj);
								Boolean cyclic_obj = Main.<Boolean>castOrNull(found_obj);
								boolean cyclic = false;
								
								// Determine the range bounds for the wildcarded assignment statement in this scope, as affected by the range returned by this use.
								// If a required bound is unevaluated, the one passed back is the one from the assignment and it should be used.
								Expression relaxed_max = null;
								Expression relaxed_min = null;
								if (use_assigned_range != null)
								{
									relaxed_max = ((required_assigned_range.getEndBitIndex()   != null) && required_assigned_range.getEndBitIndex().isEvaluated())
											          ? required_assigned_range.getEndBitIndex()
											          : use_assigned_range.getEndBitIndex();
									relaxed_min = ((required_assigned_range.getStartBitIndex() != null) && required_assigned_range.getStartBitIndex().isEvaluated())
											          ? required_assigned_range.getStartBitIndex()
											          : use_assigned_range.getStartBitIndex();
								} else
								{
									cyclic = ((Boolean)cyclic_obj).booleanValue();
								}
								
								boolean first_conclusive = false;  // Set true for the first non-cyclic use of this wildcarded assignment.
								if (!use_conclusive) // .... ugh.. this needs work because each wildcarded assignment can have its own cyclic searches...
								{
									if (!cyclic)
									{
										first_conclusive = true;
										conclusive = true;
										found_any_assignment = any_assignment;
									}
									else
									{
										cyclic_cnt++;
									}
								}
								
								if (first_conclusive)
								{
									found = (use_assigned_range != null);
									if ((ret_range != null) && found)
									{
										any_assigned.getParseContext().report(0, Severity.FATAL_ERROR, "ANY-FLOW",
												                              "This and another " + Identifier.any_sig_keyword.toString() +
												                              " at this scope are able to assign " +
												                              required_assigned_range.getPipeSignal().getIdentifier() + ".");
									}
	
									if (found)
									{
										// Formulate an assigned range from the one that was found in the use scope, add it to any_assigned and to any_assignment (thereby adding the signal in this scope) and reflect in any_assigned.sv_string, and return it.
										ret_range = any_assignment.addAssignedBitRange(this, any_assigned.getIdentifier(), use_sig.getIdentifier(), relaxed_max, relaxed_min);
										ret_range.getPipeSignal().setSvDataTypeIdentifier(use_sig.getSvDataTypeIdentifier());
										any_assigned.getSigRangeMap().put(use_sig.getIdentifier().toString(), ret_range);
									}
								} else
								{
									// Subsequent uses (iterations) must be consistent with the first.
									
									if (found != (use_assigned_range != null))
										any_assigned.getParseContext().report(0, Severity.FATAL_ERROR, "ANY-FLOW",
												                              "Only some uses of " + Identifier.any_sig_keyword +
												                              " in this assignemnt statement are able to find an assignment of " +
												                              required_assigned_range.getPipeSignal().getIdentifier().toString());
									if (found)
									{
										// Make sure the assignment range and signal type that was found is compatible with the first found.
										boolean different_types = ((use_sig.getSvDataTypeIdentifier() == null) ^
												                   (ret.getPipeSignal().getSvDataTypeIdentifier() == null)) ||
												                  ((use_sig.getSvDataTypeIdentifier() != null) &&
												                   (!use_sig.getSvDataTypeIdentifier().equals(ret.getPipeSignal().getSvDataTypeIdentifier())));
										boolean different_max = !Expression.equals(use_assigned_range.getEndBitIndex(),   ret.getEndBitIndex());
										boolean different_min = !Expression.equals(use_assigned_range.getStartBitIndex(), ret.getStartBitIndex());
										if (different_max || different_min || different_types)
											any_assigned.getParseContext().report(0, Severity.FATAL_ERROR, "ANY-FLOW",
													 "Wildcarded assignment leads to incompatible assignments for signal " + use_sig.getIdentifier() + ".\n" +
										             (different_types ? "SV data types differ."
										            		          : ("Ranges are: " + ret.toString() + " and " + use_assigned_range.toString() + ".")));
									}
								}
								if (found)
								{
									// Create used assigned range from the one found and add it to (WildcardedSigRef)used_range and its sv_string and add the range to the assignement (thereby adding the signal to the used scope).
									UsedBitRange used_sig_range = any_assignment.addUsedBitRange(use_scope, used_range.getIdentifier(), use_sig.getIdentifier(), used_range.getAlignment(), used_range.getEndBitIndex(), used_range.getStartBitIndex());
									((WildcardedSigRef)used_range).getSigRangeMap().put(use_sig.getIdentifier().toString(), used_sig_range);
								}
							}
						}
						first_iteration = false;
					}
					if (!first_pass && (found_any_assignment == any_assignment))
					{
						// Second pass, and we've finished processing the fruitful assignment.  Others have been searched conclusively, so break out of the loop.
						break;
					}
					first_pass = false;
				}
			}
		} else
		{
			// No Wildcarded assignments at this scope.
			...
		}
		
		searching_for_assignment = false;
		return ret;
	}*/
	
	
	
	
	/*
	 * The wildcarded assignment search was originally thought through without the restriction of a single wildcarded assignment per scope.  This
	 * proved to introduce a great deal of complexity.  This was to state of that effort:
	
	private enum SearchFruitfulStatus
	{
		NONE,           // Not actively involved in a search.
		FRUITFUL_UNKNOWN,
		FRUITFUL,
		NOT_FRUITFUL
	}
	private enum SearchAgainStatus
	{
		NONE,
		ASSIGN_NEEDED,
		ASSIGN_PROHIBITTED
	}
	private SearchStatus search_status = SearchStatus.NONE;
    	// TODO: Multithreading?

	// First search, determine (and record w/ scope) which are the fruitful assignments.  Determine any unevaluated assigned bounds.
	// Second search, follow fruitful assignments to pull signal.  Need 2nd try at each scope in case of cyclic use.  W/ this approach,
	//    the search is done for nodes w/ uses and no assignment.
	
	Assignment fruitful_assignment = null;
	*/
	
	/**
	 * These recursive methods are used for three top-level invocations, which together produce the flow of this signal through wildcarded assignment statements that
	 * provides to signal at the top-level invocation.  If either required bound is unevaluated, the resulting signal assignment bound will be that of the assignments, which will be verified to all match (string comparison).
	 * The first level invocation determines which one of this scope's wildcarded assignments is fruitful and sets this.fruitful_assignment.
	 *    It sets this.search_status to FINDING_ASSIGN.  It recurses into only and all scopes without assignments of the signal that have this.search_status == NONE (to break cycles).
	 *    It reports an error if multiple fruitful paths are found.  There's some complexity here with cycles.  We are not necessarily able to determine whether cycles are fruitful.  We won't know until the
	 *    signal is pulled through the point of the cycle.  To address this, we track SearchAgainStatus and do another call if necessary from the top level (or we could record the min required search depth, and call from there).
	 * The second invocation traverses fruitful assignments only and pulls the signal through the fanin-graph.  Note that it recurses into the scopes
	 *    with the assignments, that are not marked FINDING_ASSIGN, and returns immediately.  The method does:
	 *    1) It checks to see if the signal and all required bits are already assigned at this scope, in which case we're done.  If not, it should be known which is the fruitful assignment.  If this.fruitful_assignment
	 *       is not already set, this scope must have been involved in a previous search, and there must be an existing assignment that comes from a wildcarded assignment statement, and it is the fruitful assignment.
	 *       If a wildcarded assignment exists, more bits are added to its signal uses and assignments.  Otherwise the wildcarded assignments are created.
	 *    2) Recurse.  TODO: No, must follow fruitful assignments.
	 *    3) In case the search contained a cycle back to this scope that pulled through more bits than
	 *       were requested originally, we invoke this method a second time at this same scope.  This action is also required to take care of a case where a required bound is unevaluated.  In this case, the resulting bound(s) reflect the assignments.  But those from loops do not,
	 *       so another invocation is required requesting the observed assignment (we must know which reflect an original assignment).  The resulting bounds from original assignments are verified to be identical.
     * The third invocation tears down the search by recursively updating this.search_status to NONE and this.fruitful_assignment to null.
     * Because the second-level invocation only traverses fruitful paths, it uses a separate method.
     * @param pull Indicates whether this is a third-level invocation, versus a first-level invocation, and should pull the signal through.
	 * @param sig_ident The identifier of the signal whose assignment to find (used for the first-level invocation only).
	 * @param must_find This scope must find an assignment (used for the first-level invocation only).
	 * @param must_not_find This scope must not find an assignment (used for the first-level invocation only).
	 * @return indication of forward progress (assignment identified for at least one scope).
	 */
	/*
	public boolean flowSearch(boolean pull, Identifier sig_ident, boolean must_find, boolean must_not_find)
	{
		search_status = SearchStatus.FINDING_ASSIGN;
		
		PipeSignal any_sig = signals.get(Identifier.any_sig_keyword);
		if (any_sig != null)
		{
			// For each wildcarded assignment in this scope.  (Note that it would have been reasonable to assume at most one wildcarded assignment per scope.)
			// Must be that all or none of the wildcarded used ranges of this assignment find an assignment.
			Iterator<BitRange> any_assigned_itr = any_sig.getAssignedRangesIterator();
			while (any_assigned_itr.hasNext())
			{
				WildcardedSigRef any_assigned = (WildcardedSigRef)(any_assigned_itr.next());
				Assignment any_assignment = any_assigned.getAssignment();
				
				boolean use_all_cyclic = true;  // Cleared once an endpoint or assignment is encountered for this use.
				
				// For each use in this wildcarded assignment.
				for (Assignment.ContainedBitRange wildcarded_use: any_assignment.getUsedRanges())
				{
					UsedBitRange used_range = wildcarded_use.getUsedBitRange();
					PipeSignal any_use_sig = used_range.getPipeSignal();
					if (any_use_sig.getIdentifier() == Identifier.any_sig_keyword)
					{
						// Wildcarded use.
						
						LogicalBehScope use_scope = any_use_sig.getBehScope();

						if (!pull)
						{
							// First pass, find fruitful.
							// See if we need to recurse.  We do iff there is no assignment of the signal and the scope has not already been searched.
							PipeSignal use_sig = use_scope.getSignals().get(sig_ident);
							if (((use_sig == null) ||                          // no assignment
								 (use_sig.getSingleAssignment() == null)) ||   // "      &&
								(use_scope.search_status != SearchStatus.NONE) // haven't been here already
							   )
							{
								// Recurse
								flowSearch(false, sig_ident, );
							}
						}
						else
						{
							
						}
					}
				}
			}
		}
	}
	*/
	
	/**
	 * Implements the second-level invocation described for flowSearch(..).
	 * @param sig The signal in the consuming scope.  The bounds of this signal indicate the required bounds.
	 * @return
	 */
	/*
	public boolean pullSignal(PipeSignal sig)
	{
		// Determine the require bounds.
		, ((assigned_range.getEndBitIndex()   != null) && assigned_range.getEndBitIndex().isEvaluated()  ) ? assigned_range.getEndBitIndex()   : null,
			    (assigned_range.getStartBitIndex() != null) && assigned_range.getStartBitIndex().isEvaluated()) ? assigned_range.getStartBitIndex() : null

		PipeSignal any_sig = signals.get(Identifier.any_sig_keyword);
		if (any_sig != null)
		{
			// For each use in this wildcarded assignment.
			for (Assignment.ContainedBitRange wildcarded_use: fruitful_assignment.getUsedRanges())
			{
				UsedBitRange used_range = wildcarded_use.getUsedBitRange();
				PipeSignal any_use_sig = used_range.getPipeSignal();
				if (any_use_sig.getIdentifier() == Identifier.any_sig_keyword)
				{
					// Wildcarded use.
					
					LogicalBehScope use_scope = any_use_sig.getBehScope();
					PipeSignal use_sig = use_scope.getSignals().get(sig.getIdentifier());
				}
			}
		}
	}
	*/
	
	public PipeSignal getAnySig()
	{
		return signals.get(Identifier.any_sig_keyword);   // TODO: Could make any_sig a member for faster access.
	}

	
	/**
	 * Debug information captured during pullSignal().
	 * 
	 * @author sfhoover
	 */
	static public class PullDebug
	{
		public int failed_cnt = 0;
		public int wildcard_cnt = 0;
		public int orig_cnt = 0;
	}
	
	
	
	boolean pulling_sig = false;   // Set while in pullSignal(..).
	
	/**
	 * Provide an assignment for the given signal by recursing through the wildcarded assignment fanin and pulling the bits through it.
	 * A scope may have only one wildcarded assignment statement.  At first, an attempt was made to support multiple, but this was found to add considerable complexity.
	 * Report an error if the bits cannot be provided.
	 * 
	 * Unevaluated range bounds: Another source of complexity is support for SV range expressions which cannot be evaluated.  If a required bound cannot be evaluated,
	 * the one from the assignment(s) is used, and all assignments are required and verified to have the same bound.  When the root invocation of this method returns,
	 * for all visited scopes, the signal's reflects_orig_assignment is true, and getIndexHighest/Lowest() is the original assignment(s)'s bound iff the required bound
	 * is unevaluated.
	 * 
	 * Cycles present additional complexity.  Cycles are broken by pulling_sig, which is set during traversal and is used to recognize a cycle and avoid further
	 * traversal.  When a cycle is encountered, there is no original assignment at the leaf of the traversal, and therefore there is no way to know the correct SV
	 * properties to apply to the pulled signal.  Once an original assignment is found in a recursive pull, its SV properties are reflected
	 * in the signal assignment at this scope, and it is passed downstream in the orig_sig argument.  Additional cycle traversals may be required to reflect the
	 * right SV properties (unevaluated bounds and SV type) through the cycle.
	 * 
	 * The steps performed by this method are:
	 *    1) Check to see if the signal and all required bits are already assigned at this scope, in which case we're done.
	 *    2) For each wildcarded use of the wildcarded assignment (if there is one):
	 *        a) Reflect use of this signal as part of the wildcarded use.
	 *        b) Recurse.
	 *        c) Pull SV attributes from the child's original assignment or ensure consistency with those of the child's original assignment.
	 *    4) Do a second recursive call at this scope if the signal's SV properties at this scope changed.  This can happen for one of two reasons:
	 *       a) Different SV attributes were pulled from an original assignment through a wildcarded use (above).
	 *       b) The search contained a cycle back to this scope that used more bits in this scope.
	 *       If a second recursive call is not needed, create or update the assignment of this signal as part of the wildcarded assignment.
	 * 
	 * @param sig The signal in this scope.  This signal reflects the wildcarded use in the calling scope (if not the top-level invocation) and it
	 *            reflects orig_sig if orig_sig is non-null.
	 * @param orig_sig A signal for this flow that reflects the SV properties of an original assignment necessary to know the SV properties
	 *                 for the assignment in this scope.  In other words, it has an SV type from an original assignment and for any required bounds
	 *                 that are unevaluated (SV expression) the bound is from an original assignment.  If non-null, signal assignments below this
	 *                 scope are compared against this.  Note that orig_sig and sig are updated as the wildcarded uses are recursed.
     * @param repeat Indicates whether this is a repeat invocation providing SV properties via orig_sig that were previously unknown.
	 * @param debug Carries debug information about the pull.
	 * @return true if the search led only to cycles.  If false (from recursive call) || orig_sig != null, sig has been updated to reflect SV properties
	 *         of an original assignment for the required bounds, and we can back-propagate them and check them for consistency vs other uses.
	 */
	public boolean pullSignal(PipeSignal sig, PipeSignal orig_sig, boolean repeat, PullDebug debug)
	{
		if (pulling_sig)
		{
			// Found a cycle.
			return true;
		}
		
		BitRange sig_assigned_range = sig.getSingleAssignedRange();
		PipeSignal any_sig = getAnySig();
		

		//// Determine the required bounds.
		//, ((assigned_range.getEndBitIndex()   != null) && assigned_range.getEndBitIndex().isEvaluated()  ) ? assigned_range.getEndBitIndex()   : null,
		//	    (assigned_range.getStartBitIndex() != null) && assigned_range.getStartBitIndex().isEvaluated()) ? assigned_range.getStartBitIndex() : null

		// If this is an original assignment, it reflects an original assignment; mark it as such.
		boolean is_orig_assignment = (sig.getSingleAssignedRange() != null) && !sig.fromWildcard();
		if (is_orig_assignment)
		{
			sig.max_from_orig_assignment = true;
			sig.min_from_orig_assignment = true;
			debug.orig_cnt++;
		}
		
		if (!sig.needsMoreBits())
		{
			// All the bits we need exist.  Done.
			return false;
		}
		if ((any_sig == null || any_sig.getSingleAssignedRange() == null) || is_orig_assignment)
		{
			if (is_orig_assignment)
			{
				// This original assignment does not provide the necessary bits.
				sig.getParseContext().report(0, Severity.GEN_ERROR, "PARTIAL-ASSIGN", "Assignment does not provide the required bits [" + sig.getIndexHighest() + ":" + sig.getIndexLowest() + "] for wildcarded assignments.");
				   // TODO: It would be good to have context reflecting the use.
			}
			else
			{
				// There's no assignment at this scope.
				// TODO: To relax the single-assignment requirement, we would have to factor out the bits assigned here, and request the remaining ones.  And, (sig_assigned_range != null)
				//       would no longer indicate that the signal has already been pulled through the wildcarded assignment, below.)
				//ActiveParseContext.Report(0, Severity.GEN_ERROR, "NO-ASSIGN", "Failed to find an assignment of " + sig.toScopedString());
				   // TODO: There's no context for this.  It would be good to have context reflecting the use.
				debug.failed_cnt++;
			}
			// Return true to avoid checking.
			return true;
		}
		debug.wildcard_cnt++;
		
		// Need to pull signal through wildcarded assignment.
		
		pulling_sig = true;
	
		// Initial SV properties.  If these are changed by recursion we must recurse again.
		Expression init_max = sig.getUsedIndexHighest();
		Expression init_min = sig.getUsedIndexLowest();
		Identifier init_sv_type = sig.getSvDataTypeIdentifier();  // (which should be null -- not expected that SV type is declared in use-only scopes)
		
		boolean ret = true;
		
		WildcardedSigRef any_assigned = (WildcardedSigRef)any_sig.getSingleAssignedRange();
		Assignment any_assignment = any_assigned.getAssignment();
		boolean orig_sig_in = orig_sig != null;
		
		// For each use in this wildcarded assignment.
		for (Assignment.ContainedBitRange wildcarded_use: any_assignment.getWildcardedUsedRanges())
		{
			UsedBitRange any_used_range = wildcarded_use.getUsedBitRange();
			PipeSignal any_use_sig = any_used_range.getPipeSignal();
			if (any_use_sig.getIdentifier() == Identifier.any_sig_keyword)
			{
				// Wildcarded use.
				
				LogicalBehScope use_scope = any_use_sig.getBehScope();
				PipeSignal use_sig = use_scope.getSignals().get(sig.getIdentifier());
				
				// Create the use of this signal in this scope's wildcarded assignment (thereby adding the signal to the used scope).  If it already
				// existed, remove the old use first.  The new use reflects the aggregation of all uses of the signal in this scope.
				if (sig_assigned_range != null)
				{
					// Already pulled this signal through.  Delete it.
					// TODO: For now, we don't bother deleting the used range from the assignment and the signal because the duplicates do no harm functionally.
					//       We will need a delete method eventually to support dynamic editing which we can use here.
					// Remove the use from the WildcardedSigRef.
					BitRange old_used_range = ((WildcardedSigRef)any_used_range).getSigRangeMap().remove(use_sig.getIdentifier());
					if (old_used_range == null)
					{
						ActiveParseContext.Report(0, Severity.BUG, "BUG", "No range to replace in wildcarded sig use while adding bits.");
					}
				}
				// Add new use.
				UsedBitRange used_sig_range = any_assignment.addUsedBitRange(use_scope, any_used_range.getIdentifier(), sig.getIdentifier(), any_used_range.getAlignment(), sig.getUsedIndexHighest(), sig.getUsedIndexLowest());
				use_sig = used_sig_range.getPipeSignal();
				((WildcardedSigRef)any_used_range).getSigRangeMap().put(use_sig.getIdentifier(), used_sig_range);


				// RECURSE.
				boolean all_cyclic = use_scope.pullSignal(use_sig, orig_sig, false, debug);
				
				
				if (!all_cyclic)
				{
					// Found an assignment.
					
					if (orig_sig == null)
					{
						// First assignment found.
						
						orig_sig = use_scope.getSignals().get(sig.getIdentifier());

						// Reflect orig_sig's SV properties in sig (bounds reflected in used range to be picked up in assigned range when the assignment is created below).
						sig.reflectOrigAssignment(orig_sig);
					}
					else
					{
						// Subsequent assignment found.  Check consistency.
						// See whether sig_assigned_range reflects orig_sig SV properties.
						// SV type
						boolean sv_type_mismatch = (sig.getSvDataTypeIdentifier() != orig_sig.getSvDataTypeIdentifier());
						// SV (unevaluated) bounds
						BitRange orig_assigned_range = orig_sig.getSingleAssignedRange();
						BitRange use_assigned_range = use_sig.getSingleAssignedRange();
						boolean sv_bounds_mismatch = (!use_assigned_range.getEndBitIndex().isEvaluated() &&       // if not evaluated, it should come from an original assignment
								                      (orig_assigned_range.getEndBitIndex().isEvaluated() ||
								                	   !orig_assigned_range.getEndBitIndex().getLabel().equals(use_assigned_range.getEndBitIndex().getLabel())
								                      )
								                     ) ||
								                     (!use_assigned_range.getStartBitIndex().isEvaluated() &&       // if not evaluated, it should come from an original assignment
										              (orig_assigned_range.getStartBitIndex().isEvaluated() ||
										               !orig_assigned_range.getStartBitIndex().getLabel().equals(use_assigned_range.getStartBitIndex().getLabel())
										              )
										             );
						if (use_sig.fromWildcard())
						{
							// This use signal assignment did not come through a wildcarded assignment; it is an original assignment.
							// There should not be mismatches if assignments are consistent.
							
							// Compare SV types.
							if (sv_type_mismatch)
							{
								sig.getParseContext().report(0, Severity.CONTEXT, "SV-TYPES", "See error below.");
								orig_sig.getParseContext().report(0, Severity.GEN_ERROR, "SV-TYPES", "SV type mismatches between this assignment (" + orig_sig.getSvDataTypeIdentifier() +
										                                                 ") and the one above (" + sig.getSvDataTypeIdentifier() + ").\n" +
										                                                 "Signals converge through wildcarded assignments.");
							}
							
							// Compare bounds.
							if (sv_bounds_mismatch)
							{
								sig.getParseContext().report(0, Severity.CONTEXT, "SV-BOUNDS", "See error below.");
								orig_sig.getParseContext().report(0, Severity.GEN_ERROR, "SV-BOUNDS", "SV signal bounds expression mismatch between this assignment and the one above.  Literal string match required.\n" +
										                                                 "Signals converge through wildcarded assignments.");
							}
						}
						else
						{
							// This signal assignment came through a wildcarded assignment.  It could have been part of this search,
							// or an earlier one.  If an earlier one, it already reflects an original assignment, but we leave it
							// to comparison to tell us that we don't need to recurse, and we will recurse if the original assignments were
							// inconsistent.  If it was part of this search, it may have pulled the signal through cycles only without an
							// original signal on which to base SV properties.
						}

					}
					
					ret = false;  // Assignment found.
				}
			}
		}
		
		pulling_sig = false;
		
		
		// We're done processing wildcarded uses.  If this was a repeat call, it was done to re-process wildcarded uses, and we're done that, so return.
		if (repeat) {return ret;}
		
		
		// If any SV attributes changed, repeat this call.
		if ( (sig.getUsedIndexHighest() != init_max) ||
			 (sig.getUsedIndexLowest() != init_min) ||
			 (sig.getSvDataTypeIdentifier() != init_sv_type) )
		{
			ret = pullSignal(sig, orig_sig, true, debug);
		}
			
		// Formulate an assigned range from sig.getUsedIndexHighest/Lowest() which has been updated based on wildcarded uses.  Add it to any_assigned and to any_assignment (thereby adding it to the signal
		// in this scope) and reflect in any_assigned.sv_string.

		BitRange old_assigned_range = any_assigned.getSigRangeMap().get(sig.getIdentifier());
		if (old_assigned_range != null)
		{
			any_assignment.removeAssignedBitRange(old_assigned_range);
			any_assigned.getSigRangeMap().remove(sig.getIdentifier());
		}
		BitRange range = any_assignment.addAssignedBitRange(this, any_assigned.getIdentifier(), sig.getIdentifier(), sig.getUsedIndexHighest(), sig.getUsedIndexLowest());
		ActiveParseContext.ReportIf(sig.from_wildcard, 0, Severity.BUG, "BUG", "Not expecting sig.from_wildcard to already be set when setting it.");
		sig.from_wildcard = true;
		range.getPipeSignal().setSvDataTypeIdentifier(sig.getSvDataTypeIdentifier());
		any_assigned.getSigRangeMap().put(sig.getIdentifier(), range);
		
		return ret;
	}
	
	
	
	
	
	
	/**
	 * Finds AssignedBitRange's for all signals that don't have them, by looking back through wildcarded assignments.
	 * And recurses into children.
	 */
	public void findNonLocalAssignments()
	{
		// No need to process this scope if it has no wildcarded assignments.
		if (getAnySig() != null)
		{
			for(Enumeration<PipeSignal> e = signals.elements(); e.hasMoreElements();)
			{
				PipeSignal sig = e.nextElement();
				
				// Process this signal.
				sig.findAssignedRange();
			}
		}
		
	    // Recurse into children.
		for(Enumeration<LogicalBehScope> children_el = children.elements(); children_el.hasMoreElements();)
		{
			LogicalBehScope child_scope = children_el.nextElement();
			child_scope.findNonLocalAssignments();
		}
	}
	
	/**
	 * Recurse through scopes and fix wildcarded assignment SV strings, now that we've pulled signals through them.
	 */
	public void fixWildcardedSv()
	{
		
		// Find wildcarded assignment at this scope, if any.
		PipeSignal any_sig = getAnySig();
		if ((any_sig != null) && (any_sig.getSingleAssignedRange() != null))
		{
			// This scope has an assignment to $ANY.
			WildcardedSigRef any_assigned = (WildcardedSigRef)(any_sig.getSingleAssignedRange());
			Assignment any_assignment = any_assigned.getAssignment();
			
			any_assignment.fixWildcardedSv(any_assigned);
		}
		
	    // Recurse into children.
		for(Enumeration<LogicalBehScope> children_el = children.elements(); children_el.hasMoreElements();)
		{
			LogicalBehScope child_scope = children_el.nextElement();
			child_scope.fixWildcardedSv();
		}
	}
	

	/**
	 * Create the staging of signals at this scope.
	 * @return Severity non-null if an error was encountered, and the calling scope should report it's context.
	 */
	public void /*Severity*/ createStagingRecursive()
	{
		//-ActiveParseContext context = ActiveParseContext.get();
		
		//// Make sure SourceFile.findNonLocalAssignments() left search_status in NONE state.
		//if (search_status != SearchStatus.NONE)
		//	context.report(0, Severity.BUG, "BUG", "LogicalBehScope.search_status was not cleared by previous search.");
		
		// Process signals at this scope.
		//
		
		if (signals != null)
		{
			// TODO: Crap, wrong approach.  Need a global step prior to staging that finds all missing assignments in each scope (in any order).
			// TODO: Get rid of return value, too.
			// TODO: Scrap below.  ---------------------------------
			// If this scope is consumed by any wildcarded assignments, process them first.
			/*
			PipeSignal wildcarded_sig = signals.get(Identifier.any_sig_keyword);
			if (wildcarded_sig != null)
			{
				reached_by_flow_search = true;
				Iterator<UsedBitRange> use_iter = wildcarded_sig.getUsedRangesIterator();
				while (use_iter.hasNext())
				{
					UsedBitRange range = use_iter.next();
					
					LogicalBehScope use_scope = range.getAssignment().getLogicalBehScope();
					if (use_scope.reached_by_flow_search)
					{
						// A cycle was encountered.  This is okay, but 
						range.getParseContext().report(0, Severity.CONTEXT, "CYCLIC-FLOW", "A cycle was encountered through scopes involving this wildcarded assignment.");
						return Severity.DEFERRED_ERROR;
					} else
					{
						if (!use_scope.done_staging)
						{
							// Need this scope to have processed its staging first.
							Severity sev = use_scope.createStagingRecursive();
							if (sev != null)
							{
								// Found error (CYCLIC-FLOW) is the only possibility.
								range.getParseContext().report(0, Severity.CONTEXT, "CYCLIC-FLOW", "...And this is the next producer in the cyclic path.");
							}
						}
					}
				}
			}
			
			// Wildcard-sig-consuming scopes have been evaluated, and all signals at this scope are available.
			// Process them.
			
			// TODO: Scrap above. ------------------------------------------
			*/
			
			
			
			
			
			Vector <PipeSignal> gatingSignals = new Vector <PipeSignal>();
			
			for(Enumeration<PipeSignal> e = signals.elements(); e.hasMoreElements();)
			{
				//gets a signal from signals list in this scope
				PipeSignal sig = e.nextElement();
				
				if(!sig.isEmpty())   // TODO: Do we allow empty sigs?
				{
					if(sig.getWhen() == null)
					{
						if (sig.getIdentifier().isStagedSigType())
						{
							//creates all the necessary transition stages and ranges
							sig.generateStaging();
						}
					}
					else
					{
						// Squirrel this signal for later because it is used as a when condition. Such signals are used by staging
						// of other signals processed in this loop, which might create uses of when condition signals.
						gatingSignals.add(sig);
					}
				}
			}
	
			// Process any when condition signals that were encountered above, now that their uses have been discovered.
			Iterator<PipeSignal> itr = gatingSignals.iterator();
	
			while(itr.hasNext())
			{
				PipeSignal sig = itr.next();
				
				sig.generateStaging();
			}
	
		}
		
	    // Recurse into children.
		for(Enumeration<LogicalBehScope> children_el = children.elements(); children_el.hasMoreElements();)
		{
			LogicalBehScope child_scope = children_el.nextElement();
			/*-if (!child_scope.done_staging)
			{
				if (*/child_scope.createStagingRecursive();/*- != null)
				{
					context.report(0, Severity.FATAL_ERROR, "CYCLIC-FLOW", "Found a cyclic flow through wildcarded assignments, reported in context above.");
				}
			}*/
		}
		
		//-done_staging = true;
		
		//-return null;
	}
	

	/**
	 * Recursively expand wildcarded signals at this scope, after analyzing the logic network.
	 */
	private void expandWildcardedSigs()
	{
		PipeSignal any_sig = getAnySig();
		Iterator<BitRange> any_assigned_range_itr = any_sig.getAssignedRangesIterator();
		while (any_assigned_range_itr.hasNext())
		{
			BitRange any_assigned_range = any_assigned_range_itr.next();
			if (any_assigned_range instanceof WildcardedSigRef)
			{
				Assignment assignment = any_assigned_range.getAssignment();
				
				// Okay, got an assignment, and its wildcarded assigned range (can only have one).  Expand it in the SV string.
				// ...
				
				// Expand uses in assignment.
				// ...
			}
		}
		
		// Recurse
		for (LogicalBehScope child_scope: children.values())
		{
			child_scope.expandWildcardedSigs();
		}
	}

		
	//Used for demonstrating the potential benefits of the new Hashtabling methodology
	public void printLogicalHierarchy()
	{
		PipeSignal temp_signal;
		
		for(Enumeration<PipeSignal> e = signals.elements(); e.hasMoreElements();)
		{
				//gets a signal from signals list in the pipeline
			temp_signal = e.nextElement();
			temp_signal.printLogicalHierarchy(0);	
			
		}
	}
	
	/**
	 * Processes a ParseBranch of behavioral scope type (pipeline or beh_hier) within this LogicalBehScope.
	 * Checks if a LogicalBehScope identified by the parseScope parameter already exists within 'children'.
	 * If it exists, the identifier is added to 'identifiers', and the identified LogicalPipeline object is returned.
	 * If is doesn't already exist, this method creates one and adds it to 'children'.
	 * 
	 * @param parseBranch: ParsePipeline or ParseBehHier object to process.
	 * 
	 * @return LogicalBehScope that is associated with the input ParseBranch.
	 */
	public LogicalBehScope processParseChildScope(/*ParseBranch*/ ParseBehScope parseChild)
	{
		ActiveParseContext.Set(parseChild, 0, -1); // Report errors against parseChild.
		// Finish parsing of parseChild.
		// TODO: This seems like functionality that should be part of parsing, but it's called during creation of logical structure.
		Prefix prefix = (parseChild instanceof ParsePipeline) ? Prefix.PIPELINE : Prefix.BEH_HIER;
		Main.assertion(parseChild instanceof ParseBehScope);
		ParseMnemonic mnemonic = parseChild.produceMnemonic(prefix);
		Range parse_range = parseChild.getRange();

		// Look for existing child with same identifier.
		LogicalBehScope logicalBehScope = children.get(mnemonic.getLabel());
		
		if (logicalBehScope == null)
		{
			// No pre-existing child matches.  Create new one.
			
			// Check range.
			Range.checkRange(parse_range, 0x5, Severity.RECOV_ERROR);
			
			logicalBehScope = (prefix == Prefix.PIPELINE) ? new LogicalPipeline(mnemonic /* TODO: phase out */, (ParsePipeline)parseChild, this)
			                                              : new LogicalBehHier (mnemonic /* TODO: phase out */, (ParseBehHier) parseChild, this);
			//-logicalBehScope.identifier = parseChild.getIdentifier();  // TODO: Should do this in LogicalBranch constructor.
			children.put(mnemonic.getLabel(), logicalBehScope);
		}
		else
		{
			parseChild.resetIdentifier(logicalBehScope.identifier);  // This ensures that all of a LogicalBehScope's corresponding ParseBehScopes refer to the same Identifier opject.
			// TODO: Restructuring can eliminate this.  Create some logical structure while parsing and don't avoid different identical Identifiers in the first place.
			
			// Pre-existing child.  Associate this parse child with this logical child.
			logicalBehScope.addIdentifier(mnemonic);
			
			// Check logical and parse ranges.
			if (Range.numArgs(logicalBehScope.getRange()) > 0)
			{
				// Range given originally > 0 args.
				
				// For now, range must be given as '[*]'.
				if ((Range.numArgs(parse_range) != 1) ||
					(!parse_range.getEndBitIndex().getLabel().equals("*"))
				   )
				{
					parseChild.rangeContext();
					ActiveParseContext.Report(0, Severity.RECOV_ERROR, "RANGE-MISMATCH", "The range of this behavioral hierarchy was already specified and should be specified here as '[*]'.");
				}
			} else
			{
				// No range given originally.
				
				// Parse range must also be empty.
				if (Range.numArgs(parse_range) > 0)
				{
					parseChild.rangeContext();
					ActiveParseContext.Report(0, Severity.RECOV_ERROR, "RANGE-MISMATCH", "This behavioral hierarchy is not replicated (was first scoped with no range), but range given.");
				}
			}
		}
		// Associate this logical child with this parse child.
		mnemonic.setLogicalNode(logicalBehScope);
		
		return logicalBehScope;
	}
	
	public LogicalBehScope processParseChildScope(Identifier ident_)
	{
		LogicalBehScope logicalBehScope;
		
		logicalBehScope = (ident_.getType() == IdentifierType.PIPELINE) ? new LogicalPipeline(ident_, this, NodeType.PIPELINE)
        											  					: new LogicalBehHier (ident_, this, NodeType.BEH_HIER);

		children.put(ident_.getName(), logicalBehScope);
		
		return logicalBehScope;
	}
	
}
