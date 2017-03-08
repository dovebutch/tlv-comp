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

import tlv.Main;
import tlv.behavioral.*;
import tlv.behavioral.range.*;
import tlv.config.IdentifierType;
import tlv.parse.identifier.*;
import tlv.utilities.Severity;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assignment class extends ParseNode and contains the string associated with an assignment or expression block in TLV source code.
 * Assignment is not a scope construct and therefore does not contain any ParseNodes underneath it. 
 * 
 * @author ypyatnyc
 *
 */
public class Assignment extends ParseNode
{
	public class ContainedBitRange
	{
		public int pos;  // The position within the assignment string of the first character of the signal identifier.
		private BitRange range;  // The bit range.
		
		public UsedBitRange getUsedBitRange()
		{
			return (range instanceof UsedBitRange) ? (UsedBitRange)range : null;
		}
		
		public BitRange getBitRange()
		{
			return range;
		}
		
		public ContainedBitRange(int _pos, BitRange _range)
		{
			pos = _pos;
			range = _range;
		}
	}
	
	/**
	 * Contains the ParseMnemonics for any unstaged BitRanges that exist in the Assignment block
	 */
	private Vector <ParseMnemonic> unstagedRanges;  // TODO: Phase out.
	
	/**
	 * Contains the ParseMnemonics for any AssignedBitRanges that exist in the Assignment block, including $ANY's.  After processing $ANY's, this list includes pulled sigs.
	 */
	private Vector <ParseMnemonic> assignedRanges;  // TODO: Phase out.
	private ArrayList <ContainedBitRange> assigned_ranges;
	
	/**
	 * Contains the ParseMnemonics for any UsedBitRanges that exist in the Assignment block.  Separate list for $ANY's.  After processing $ANY's, used_ranges includes pulled sigs.
	 */
	private Vector <ParseMnemonic> usedRanges;  // TODO: Phase out.
	private ArrayList <ContainedBitRange> used_ranges;
	private ArrayList <ContainedBitRange> wildcarded_used_ranges;
	
	/**
	 * Constructs an Assignment object from a source_node ParseNode.
	 * 
	 * @param source_node  a parse node whose attributes are copied over to the newly constructed Assignment.
	 * 
	 * @see ParseNode
	 */
	public Assignment(ParseNode source_node)
	{
		super(source_node);
		setType(source_node.getType());
		
		unstagedRanges = new Vector<ParseMnemonic>(0,0);
		assignedRanges = new Vector<ParseMnemonic>(0,0);
		assigned_ranges = new ArrayList<ContainedBitRange>();
		usedRanges = new Vector<ParseMnemonic>(0,0);
		used_ranges = new ArrayList<ContainedBitRange>();
		wildcarded_used_ranges = null;
	}
	
	/**
	 * Constructs a ParseWhen from a source_node and links it with a ParseBranch parent_branch object.
	 * 
	 * @param parent_branch	the newly constructed Assignment will point to this ParseBranch as its parent branch.
	 * @param source_node  a parse node whose attributes are copied over to the newly constructed Assignment.
	 * 
	 * @see ParseNode
	 */
	public Assignment(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		setType(source_node.getType());
		
		unstagedRanges = new Vector<ParseMnemonic>(0,0);
		assignedRanges = new Vector<ParseMnemonic>(0,0);
		assigned_ranges = new ArrayList<ContainedBitRange>();
		usedRanges = new Vector<ParseMnemonic>(0,0);
		used_ranges = new ArrayList<ContainedBitRange>();
		wildcarded_used_ranges = null;
	}
	
	/**
	 * Must be called prior to adding wildcarded used ranges.
	 */
	public void haveWildcardedUses()
	{
		if (wildcarded_used_ranges == null)
		{
			wildcarded_used_ranges = new ArrayList<ContainedBitRange>(5);
		}
	}
	
	public ArrayList <ContainedBitRange> getUsedRanges()
	{
		return used_ranges;
	}
	
	public ArrayList <ContainedBitRange> getWildcardedUsedRanges()
	{
		return wildcarded_used_ranges;
	}
	
	public ArrayList <ContainedBitRange> getAssignedRanges()
	{
		return assigned_ranges;
	}

	
	/**
	 * This method returns the LogicalStage object which this Assignment ParseBlock is scoped under
	 * 
	 * @return LogicalStage indicating the Assignment staging scope
	 */
	public LogicalStage getLogicalStage()
	{
		ParseStage temp_parse_stage = (ParseStage)getParseBranch_ofType(NodeType.STAGE);
		
		if(temp_parse_stage != null)
			return temp_parse_stage.getLogicalStage();
		else
			return null;
	}
	
	/**
	 * This method returns the most immediate LogicalWhen scope for the Assignment ParseBlock
	 * 
	 * @return LogicalWhen indicating the Assignment's lowest clock gating scope
	 */
	public LogicalWhen getLogicalWhen()
	{
		ParseWhen temp_parse_when = (ParseWhen)getParseBranch_ofType(NodeType.WHEN);
		
		if(temp_parse_when != null)
			return temp_parse_when.getLogicalWhen();
		else
			return null;
	}
	
	/**
	 * This method returns the highest level LogicalWhen scope for the Assignment
	 * 
	 * @returnLogicalWhen indicating the Assignment's highest clock gating scope
	 */
	public LogicalWhen getLogicalWhen_top()
	{
		ParseWhen temp_parse_when = (ParseWhen)getParseBranch_ofType(NodeType.WHEN);
		
		while(temp_parse_when.getParseBranch_ofType(NodeType.WHEN) != null)
		{
			temp_parse_when = (ParseWhen)temp_parse_when.getParseBranch_ofType(NodeType.WHEN);
		}
		
		return temp_parse_when.getLogicalWhen();
	}
	
	/**
	 * This method returns the LogicalPipeline which Assignment is scoped under.
	 * 
	 * @return LogicalPipeline which Assignment is scoped under
	 */
	public LogicalPipeline getLogicalPipeline()
	{
		ParsePipeline temp_parse_pipeline = (ParsePipeline)getParseBranch_ofType(NodeType.PIPELINE);
		
		if(temp_parse_pipeline != null)
			return temp_parse_pipeline.getLogicalPipeline();
		else
			return null;		
	}
	
	//Steve's version which is currently incompatible with SVplus
	//public LogicalBehScope getLogicalBehScope()
	//{
	//	if (getLogicalBranch().getSelfOrAncestorBehScope() == null) {Main.breakpoint();}
	//	return getLogicalBranch().getSelfOrAncestorBehScope();
	//}
	
	public LogicalBehScope getLogicalBehScope()
	{
		if(getLogicalParent() != null)
			return getLogicalParent().getSelfOrAncestorBehScope();
		else if(this.getType() == NodeType.SV_PLUS_UNSCOPED)
			return ((SourceFile)parent).topBehHier;
		else
			return null;
	}
	
	/**
	 * This method creates a UsedBitRange from given ParseElements and adds its identifier ParseMnemonic to the usedBitRanges vector
	 * 
	 * @param   sig_scope		LogicalBehScope of the signal
	 * @param	identifier_		ParseMnemonic containing the name of the signal
	 * @param	alignment_		Expression describing the alignment of the signal 
	 * @param	end_bit_index_	Expression describing the upper bound of the signal's bit range
	 * @param	start_bit_index_	Expression describing the lower bound of the signal's bit range
	 * 
	 * @return a UsedBitRange object that was created from the ParseElement parameters
	 */
	public UsedBitRange addUsedBitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier, Expression alignment_, Expression end_bit_index_, Expression start_bit_index_)
	{
		boolean is_any = identifier == Identifier.any_sig_keyword;
		if (identifier.getType().isKeyword() && !is_any)
		{
			ActiveParseContext.Report(0, Severity.BUG, "BUG", "Unexpected identifier type for Assignment.addAssignedBitRange.");
		}
		
		UsedBitRange range = is_any
				                  ? new WildcardedSigRef(sig_scope, identifier_, identifier, alignment_)
				                  : new UsedBitRange(sig_scope, identifier_, identifier, alignment_, end_bit_index_, start_bit_index_);
		
		LogicalWhen when = getLogicalWhen();
		
		//TODO: Remove because structure is unused
		if(when != null)
		{
			when.addUsedBitRange(range);  // TODO: Don't think there's a linkage to the scope of this range for this when in potentially different scope.  But I think these data structures are deprecated anyway.
		}
		else
		{
			sig_scope.addUsedBitRange(range);  // TODO: It looks like this redundantly adds the signal to the scope.
		}
		
		sig_scope.addUsedRange(range);  // TODO: Same as above.  Which needs to be deleted?  The one above
		
		// Add the use.
		usedRanges.add(identifier_);
		if (is_any)
		{
			haveWildcardedUses();
			wildcarded_used_ranges.add(new ContainedBitRange(-1, range));  // TODO: correct position.
		} 
		else
		{
			used_ranges.add(new ContainedBitRange(-1, range));  // TODO: correct position.
		}
		
		return range;
	}
	
	/**
	 * This method creates an AssignedBitRange from given ParseElements and adds its identifier ParseMnemonic to the assignedBitRanges vector
	 * 
	 * @param	identifier_			ParseMnemonic containing the name of the signal
	 * @param	end_bit_index_		Expression describing the upper bound of the signal's bit range
	 * @param	start_bit_index_	Expression describing the lower bound of the signal's bit range
	 * 
	 * @return a UsedBitRange object that was created from the ParseElement parameters
	 */
	public BitRange addAssignedBitRange(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier, Expression end_bit_index_, Expression start_bit_index_)
	{
		if (identifier.getType().isKeyword() && identifier != Identifier.any_sig_keyword)
		{
			ActiveParseContext.Report(0, Severity.BUG, "BUG", "Unexpected identifier type for Assignment.addAssignedBitRange.");
		}
		
		BitRange temp_range = (identifier == Identifier.any_sig_keyword)
				                  ? new WildcardedSigRef(sig_scope, identifier_, identifier)
				                  : new BitRange(sig_scope, identifier_, identifier, end_bit_index_, start_bit_index_, true);

		//retrieve logical when and logical scope from the ParseNode/ParseBranch structure
		LogicalWhen temp_when = getLogicalWhen();
		LogicalBehScope temp_scope = getLogicalBehScope();
		//-if (temp_scope != sig_scope) {ActiveParseContext.Report(0, Severity.BUG, "BUG", "Assignment scope mismatch: " + temp_scope.toString() + ", " + sig_scope.toString() + ", for sig " + temp_range.getPipeSignal().toString() + ".");}
		
		if(temp_when != null)
		{
			//gated assigned bit range
			temp_when.addAssignedBitRange(temp_range);
		}
		else
		{
			//ungated assigned bit range
			sig_scope.addAssignedBitRange(temp_range);   // TODO: This does sig_sccope.addSignal(), which has already been done in the AssignedBitRange(...) constructor above.  Sort this out.
		}
		
		sig_scope.addAssignedRange(temp_range);   // TODO: This is redundant w/ temp_scope.addAssignedBITRange(temp_range) above, which it appears was intent to replace this, but that was abandoned midstream.
		
		assignedRanges.add(identifier_);
		assigned_ranges.add(new ContainedBitRange(-1, temp_range));  // TODO: correct position.
		return temp_range;
	}
	
	public void removeAssignedBitRange(BitRange range_to_remove)
	{
		Main.unwrittenCode();
	}
	
	
	/*
	 * Helper method for checkForAssignmentOperator().  Looks for " = ", returning true iff found.
	 */
	// Scan stringWithoutIndentation for " = ", returning true when found.  Skip over comments.  Return false if end-of-string reached.
	private boolean checkForEquals()
	{
		int i = -1;  // Position in string.
		boolean slash = false;  // Last char was a slash.
		//-boolean white_space = true;
		String str = stringWithoutIndentation;
		int len = str.length();
		do
		{
			i++;
			if (i >= len) return false;
			if (slash && (str.charAt(i) == '/'))
			{
				// //-comment.  Skip till newline.
				do
				{
					i++;
					if (i >= len)
					{
						// Seems there's no final '\n', so the check below is commented out.
						// ActiveParseContext.Report(0, Severity.BUG, "BUG", "Failed to find carriage return after '//'-comment in Assignment string.  Is this the end of file?");
						return false;
					}
				} while (str.charAt(i) != '\n');
				// Now pointing to '\n' (which is whitespace, so we don't want to skip it).
			}
			else if (slash && (str.charAt(i) == '*'))
			{
				// /*-comment.  Skip till "*/".
				do
				{
					i++;
					if (i+1 >= len)
					{
						ActiveParseContext.Report(0, Severity.BUG, "BUG", "Failed to find end of '/*'-comment in Assignment string.");
						return false;
					}
				} while ((str.charAt(i) != '*') || (str.charAt(i+1) != '/'));
				i += 2;
				if (i >= len) return false;
				// Now pointing past "*/".  (We must go past '/', because it cannot start another comment.)
			}
			// Else, look for " = ";
			if (Character.isWhitespace(str.charAt(i)) && (Main.safeCharAt(str, i+1) == '=') && Character.isWhitespace(str.charAt(i+2)))
			{
				// Found " = ".
				return true;
			}
			slash = str.charAt(i) == '/';
		} while (true);
	}
	
	
	/*
	 * Helper method used to determine whether this assignment is a "parseable" one, meaning it's left- and right-hand sides can
	 * be recognized, and assigned signals need not be explicit.  Two methods are used to determine parseability, using the patterns below.
	 */
	//- static Pattern assignment_equals_pattern = Pattern.compile("\\s=\\s");  // Matches " = ".
	static Pattern assignment_start_pattern = Pattern.compile("[\\{\\$\\*]");  // Lines starting w/ a signal or a concat should be parseable.
	private boolean isParseableAssignment()
	{
		boolean parseable1 = checkForEquals();
		Matcher start_matcher = assignment_start_pattern.matcher(stringWithoutIndentation);
		boolean parseable2 = start_matcher.lookingAt();
		
		if (parseable1 != parseable2)
		{
			ActiveParseContext.Report(0, Severity.RECOV_ERROR, "PARSE-ASSIGN",
					                  (parseable1
					                     ? "Found '=' in assignment to delimit left- and right-hand sides, but left-hand side does not start with signal or concatination."
					                     : "Assignment appears to start with signal or concatination, but couldn't find '=' to delimit left- and right-hand sides.") +
					                  "\nTreating signals as uses by default.  Consider using \\SV_plus."
					                 );
		}
		return (parseable1 && parseable2);
	}
	
	private static Pattern sig_ref_start_pattern   = Pattern.compile("(\\$|\\$\\$|\\$=|>|\\||\\*)[a-zA-Z]");
	private static Pattern carriage_return_pattern = Pattern.compile("\n");
	private static Pattern always_comb_pattern     = Pattern.compile("\\\\always_comb");
	private static Pattern sv_plus_pattern         = Pattern.compile("\\\\SV_plus");
	private static Pattern sv_type_pattern         = Pattern.compile("\\*\\*[a-zA-Z]");
	private static Pattern token_pattern           = Pattern.compile("\\w*");
	
	
	/**
	 * Parse the string (or substring) of this assignment and populate stringSV.  Extract signal references and create BitRanges for them and
	 * record them in various data structures.  Called recursively to support >scope[$bar]$foo.
	 * 
	 * Keeps track of whether on the left-hand side to determine assigned sig refs.  Left-hand side ends with
	 * " = ".  If this assignment is an \always_comb or \SV_plus or the first char is '`', there's no left-side.
	 * Otherwise it must start with a sig ref or '{'.  Contents in {} are called recursively on the left-hand side only.
	 * 
	 * For the root invocation, stringSV is updated, and the return value is null.  For recursive invocations
	 * the return value is the resulting SV string.
	 * 
	 * TODO: This will replace the method below.
	 */
	public String parse2(ParseContext context, char terminal_char /* The char that will terminate the parsing.  Could be ')', ']', '}' or '\0' for the outer invocation. */, int sv_str_pos)
	{
		boolean leading_whitespace = false;
		boolean root_invocation = terminal_char == '\0';
		boolean left_hand_side = (root_invocation && (getType() == NodeType.ASSIGNMENT) && isParseableAssignment()) || (terminal_char == '}');	// True while we might be parsing the left-hand side of an assignment.
		boolean right_hand_side = false;  // True in the root invocation after finding an assignment '='.  In this context explicit assigned signals are not permitted.
		Identifier struct_identifier = null;  // Identifier of an SV type.
		
		
		String ret = "";
		context.setAnchorPos(0);
		leading_whitespace = context.parseWhitespace();
		
		int start_pos = context.getPosition();
		
		// Parse beginning of Assignment string.
		if (root_invocation)
		{
			// Parse special node types: \always_comb, \SV_plus, **sv_type, and `MACROS().
			
			// \always_comb
			if (getType() == NodeType.ALWAYS_COMB)
			{
				ret += context.absorbAnchoredString();
				context.parseMatches(always_comb_pattern, true, Severity.BUG, "BUG", "");
				ret += "always_comb begin";
				context.setAnchorPos(0);
				leading_whitespace = context.parseWhitespace();
				left_hand_side = false;
			}
			// \SV_plus
			else if (getType() == NodeType.SV_PLUS)
			{
				ret += context.absorbAnchoredString();
				context.parseMatches(sv_plus_pattern, true, Severity.BUG, "BUG", "");
				ret += "/*SV_plus*/";
				context.setAnchorPos(0);
				leading_whitespace = context.parseWhitespace();
				left_hand_side = false;
			}
			// SV type declarations (**sv_type;).
    		else if (context.parseMatches(sv_type_pattern, false) != null)
    		{
				ret += context.absorbAnchoredString();
				struct_identifier = Identifier.parse(IdentifierType.SV_DATATYPE.ordinalMask());
				leading_whitespace = context.parseWhitespace();
				context.setAnchorPos(0);
    		}
			// `MACROS()
    		else if (context.getCurrentChar() == '`')
			{
				left_hand_side = false;
			}
		}
		
		
		
		// Iterate over string until the end or terminal_char.  Each iteration starts w/ non-whitespace, where leading_whitespace must reflect whether
		//   there was leading whitespace.  It processes the next entity which could be a comment, an escape char, '=', a sig reference, a
		//   character to pass through, etc., then absorbs whitespace for the next iteration.  Text from the current context anchor
		//   must be absorbed into ret.
    	while((context.getPosition() < string.length()) && (context.getCurrentChar() != terminal_char))
    	{
//context.report(0, Severity.INFORM, "DEBUG", "Debug Info.");
    		// Process escaped char.  Note that a backslash at the end of the line is okay, and is preserved (which
    		// is convenient for SV line continuation).
    		if ((context.getCurrentChar() == '\\') && (context.getString().length() > context.getPosition() + 1))
    		{
    			ret += context.getAnchoredString() + context.getCharOffset(1);
    			context.incrementPosition(2);
    			context.setAnchorPos(0);
    		}
    		
    		// Skip comments (and '/').
    		else if (context.getCurrentChar() == '/')
    		{
    			// Skip '/';
    			context.incrementPosition(1);
    			
    			if (context.getCurrentChar() == '/')
    			{
    				// '//' comment.  Skip it.
        			context.incrementPosition(1);
    				context.parseUntil(carriage_return_pattern, true);
    			}
    			else if (context.getCurrentChar() == '*')
    			{
    				// '/*' comment.  Skip it.
        			context.incrementPosition(1);
    				context.parseUntil(Pattern.compile("\\*/"), Severity.RISKY_ERROR, "COMMENT-FMT", "");  // Probably a bug.  Node parsing should swallow whole comment, right?
    			}
    		}
    		
    		// Parse "=" w/ surrounding whitespace.
    		else if ((context.getCurrentChar() == '=') && left_hand_side && leading_whitespace)
    		{
    			context.incrementPosition(1);
    			if (Character.isWhitespace(context.getCharOffset(0) /* Note: This returns '\0' if off the end of the string. */))
    			{
    				// Found a "=", ending the left-hand side.
    				
    				left_hand_side = false;
    				right_hand_side = true;
    			
					// Insert "assign " at beginning of ret string.
    				ret += context.absorbAnchoredString();  // Make ret current.
					ret = ret.substring(0, start_pos) + "assign " + ret.substring(start_pos);
					// This shifts the assignment portion of the SV string, which could contain an $ANY, which has pointers into the string.  These must be shifted.  TODO: Big hack.
					for (ContainedBitRange assigned : getAssignedRanges())
					{
						if (assigned.getBitRange() instanceof WildcardedSigRef)
						{
							WildcardedSigRef ref = (WildcardedSigRef)(assigned.getBitRange());
							ref.sv_begin_str_pos += 7;
							ref.sv_end_str_pos += 7;
						}
					}
					
					// Insert X injection.  This is currently done by modifying the TLV string to include "(X_inj && !$when_condj) ? 'x : " into SV string.
					// This provides a use of $when_cond that gets conveniently processed as other uses, but the real original string is lost, and errors are
					// reported against the modified string.
					// TODO: X injection does not work for macro and module instantiations.
					if (Main.command_line_options.xInj())
					{
						// TODO: We should do things differently for state assignments.  In this case, we need to know whether there
						//       are any uses of the assigned signal(s) in the same stage, or if the physical gater is not the when condition.
						//       If either are true, the assignment must be given an explicit recirculation term, rather than X injection.
						//       It would also be better to tag the assigned signal name with something like "_UPD" (proj-specific) to reflect
						//       that it is not exactly the state signal.  Maybe use the non-state signal version of the signal and don't allow it
						//       to be used (so there's no project specific burden here).
						LogicalWhen when = getLogicalWhen();
						if (when != null)
						{
							// Modify the TLV string.
			
							// Need to know if we are assigning state or pipe_sig.  Look at type of first assigned signal, which should exist, and all should be
							// compatibly-typed.
							// Must have an assigned signal (commented out because assigned SV signals are not recorded.
							// if (assigned_ranges.isEmpty()) context.report(0, Severity.FATAL_ERROR, "ASSIGN", "No signal found on left-hand side of assignment statement.");
							boolean state_assignment = !assigned_ranges.isEmpty() && assigned_ranges.get(0).getBitRange().getPipeSignal().isState();
							string = string.substring(0, context.getPosition()) + " " +
							         (state_assignment ?
							        	"!" + when.getGatingPipeSignal().toString() + " ? $RETAIN : " :
							        	"`WHEN(" + when.getGatingPipeSignal().toString() + ")") +
							         string.substring(context.getPosition());
							// Conveniently, the SV ?: construct has lowest precedence, so no parens required.
							// TODO: For non-state sigs, the recirculation above is bogus.  I think synthesis would treat the X as zero, and the
							//       recirculation would exist.  This is not desired.  It can be worked around by disabling X injection.  Is this the long-term solution?
							// TODO: For state signals, the recirculation is needed only if the state signal is consumed in the stage it is produced.
							//       Otherwise, clock gating will produce the correct state signal.  Unfortunately, this isn't known at this point.
							//       For this reason, and for $ANY, SV string generation should be a subsequent parsing step.
						}
					}
				}

    		}
    		
    		// Allow for concats on the left-hand side, processed in recursive call.
    		else if ((context.getCurrentChar() == '{') && left_hand_side)
    		{
    			context.incrementPosition(1);
    			ret += context.getAnchoredString();
    			ret += parse2(context, '}', sv_str_pos + ret.length());
    		}

    		// Parse sig refs.
    		else if (!Character.isAlphabetic(context.getCharOffset(-1)) && (context.parseMatches(sig_ref_start_pattern, false) != null))
    		{
    			ret += context.absorbAnchoredString();
    			ret += parseSigRef(context, struct_identifier, left_hand_side, right_hand_side, sv_str_pos + ret.length());
    		}
    		
    		else
    		{
    			// Nothing special, just swallow the character.
    			context.incrementPosition(1);
    			
    			// To speed up parsing, swallow word chars.
    			context.parseMatches(token_pattern, true);
    		}

    		// Absorb whitespace.
    		leading_whitespace = context.parseWhitespace();
    		
    	}
    	
    	ret += context.absorbAnchoredString();
    	
    	// Make sure parsing doesn't complete w/o terminating the left-hand side.
    	if (left_hand_side && (terminal_char != '}')) context.report(0, Severity.LOGIC_ERROR, "PARSE-ASSIGN",
    			         "Parsing assignment statement with no white-space delimited \"=\".  All signals treated as assigned.  Consider \\SV_plus.");
		
    	// Record resulting SV string.
    	if (root_invocation)
    	{
    		stringSV = ret;
    		ret = null;
    	}
    	
		return ret;
	}
	
	/**
	 * Parse a signal reference pointed to by the given parse context relative to the given scope.  This is part of parse(), pulled out only because
	 * its big.
	 * Return the resulting SV string for the signal reference.
	 */
	public String parseSigRef(ParseContext context, Identifier struct_identifier, boolean left_hand_side, boolean right_hand_side, int sv_str_pos)
	{
		String ret = "";
		ActiveParseContext.ReportIf(context != ActiveParseContext.get(), 0, Severity.BUG, "BUG", "Assignment.parseSigRef(...) assumes given context is the active context.");

		// Initialize scope.
		LogicalBehScope ref_scope = null;
		String index_str = "";


		//
		// Process behavioral scope.
		//
		
		// Iterate over beh scope, until the signal identifier is absorbed.
		Identifier ident;
		boolean is_sig;
		do
		{
			ident = Identifier.parse(IdentifierType.sigOrdinalMask() | (1 << IdentifierType.BEH_HIER.ordinal()) | (1 << IdentifierType.PIPELINE.ordinal()));
			is_sig = ((1 << ident.getType().ordinal()) & IdentifierType.sigOrdinalMask()) != 0;
			if (!is_sig)
			{
				// Scope identifier.
				
				
				// Update ref_scope to reflect this scope.
				boolean base_scope = (ref_scope == null);  // true for the first specified scope, which could be an ancestor or child.
				boolean base_ancestor_scope = base_scope;  // then cleared if not found as ancestor.
				LogicalBehScope current_scope = getLogicalBehScope();
				boolean found_it = false;
				if (base_scope)
				{
					// Find this scope in ancestry of current scope.
					ref_scope = current_scope.findSelfOrAncestorBehScope(ident);  // Allow for self to allow for generic code that is passed a scope, which could be self reference.
					if (ref_scope != null)
					{
						// Found matching ancestor, assigned to ref_scope.  Initialize index_str as well.
						found_it = true;
					}
					else
					{
						// Couldn't find scope in ancestry.  It must be a child.  So, define reference scope as current scope.
						ref_scope = current_scope;
						base_ancestor_scope = false;
					}
				}
				
				if (!found_it)
				{
					// Find this scope among ref_scope's children.
					LogicalBehScope child_scope;
					for(Enumeration<LogicalBehScope> children_el = ref_scope.children.elements(); children_el.hasMoreElements();)
					{
						child_scope = children_el.nextElement();
						if (ident.equals(child_scope.getIdentifier()))
						{
							// Found it.
							ref_scope = child_scope;
							found_it = true;
							break;
						}
					}
				}
				if (!found_it)
				{
					// Attempted to make this condition non-fatal and define the scope below, but this doesn't initialize the scope
					// with any knowledge of bounds.  It does work for pipelines though (which have no range).
					// TODO: Wouldn't it be better to parse all the scopes first, then do another pass to evaluate assignment statements?
					
					if (ident.getType() != IdentifierType.PIPELINE)
					{
						// Didn't find matching scope.
						context.report(0, Severity.FATAL_ERROR, "BAD-SCOPE", "Scope \"" + ident + "\" could not be found as a child " +
										  (base_scope ? "or ancestor " : "") + "of scope \"" + ref_scope.getIdentifier().toString() + "\".");
					}
					else
					{
						ref_scope = ref_scope.processParseChildScope(ident);
					}
				}

				// Found it.  Update index_str to reflect this scope.
				// If ref_scope was newly assigned, we must initialize index_str.
				if (base_scope)
				{
					index_str = ref_scope.getParentIndexStr();
				}
				// Parse this scope's index.
				// Parse indexing for this scope.
				if ((ref_scope.getRange() != null) &&
				    (ref_scope.getRange().numArgs() > 0))
				{
					if (context.getCurrentChar() == '[')
					{
						// Index given explicitly.  Parse index string, which may itself contain signal references.
						context.incrementPosition(1);
						context.setAnchorPos(0);
						index_str += '[' + parse2(context, ']', -1000 /* sv_str_pos -- for $ANY which shouldn't be used to index */) + ']';
						context.incrementPosition(1);
					} else {
						// No index given.  This is shorthand for assuming the same index as a same-named ancestor in current scope.
						if (!base_ancestor_scope)
						{
							// ref_scope is not known to be an ancestor.  Need to confirm a matching ancestor in current scope.
							if (current_scope.findSelfOrAncestorBehScope(ident) == null)
							{
								context.report(0, Severity.FATAL_ERROR, "NO-SCOPE-RANGE", "Reference scope" + ident.toString() + " requires an index");
							}
						}
						index_str += '[' + ref_scope.getSvLoopVar() + ']';
					}
				}
			}
		} while (!is_sig);
		// Now ident is a sig.
		
		
		
		//
		// Process signal identifier.
		//
		
		Identifier signal_ident = ident.unassignedVariant();
		// TODO: Rework ParseMnemonic:
		ParseMnemonic signal_name = new ParseMnemonic(this, ident.getType().isStagedSig() ? Prefix.STAGED_SIGNAL : Prefix.UNSTAGED_SIGNAL, context.getPosition() - ident.getName().length());
		
		// Is this an explicit assignee?
		boolean explicitly_assigned = ident.isAssignedSigType();
		boolean assigned = explicitly_assigned || left_hand_side;
		//-String sv_struct_index = "";
		Expression end_bit_index = null;
		Expression start_bit_index = null;

		
		
		//
		// Process '#'
		//
		Expression alignment = null;
		if (context.getCurrentChar() == '#')
		{
			context.incrementPosition(1);
			alignment = new Expression(this, Prefix.ALIGNMENT, context.getPosition());
			context.setPosition(alignment.getStringEndingIndex());
		}
		
		
		
		if (context.getCurrentChar() == '[')
		{
			//
			// Process bit range (those exposed to TLV).
			//
			end_bit_index = new Expression(this, Prefix.END_BIT, context.getPosition() + 1);
			context.setPosition(end_bit_index.getStringEndingIndex());
			
			if (context.getCurrentChar() == ':')
			{
				start_bit_index = new Expression(this, Prefix.START_BIT, context.getPosition() + 1);
				context.setPosition(start_bit_index.getStringEndingIndex());
			}
			if (context.getCurrentChar() != ']') context.report(0, Severity.FATAL_ERROR, "RANGE", "Bit range expression not terminated with ']'.");
			context.incrementPosition(1);
		}
		/*
		else
		{
			// No bit range.
			
			//
			// Process SV field(s) and bit ranges for SV type signals.
			//
			
			while (context.getCurrentChar() == '.')
			{
				// Absorb fields into sv_struct_index;
				context.setAnchorPos(0);

				char c;
				do
				{
					context.incrementPosition(1);
					c = context.getCurrentChar();
				} while (Character.isAlphabetic(c) || (c == '_'));
				sv_struct_index += context.absorbAnchoredString();
				
				// Process range (bit range or array field index).
				if (context.getCurrentChar() == '[')
				{
					context.incrementPosition(1);
					context.setAnchorPos(0);
					sv_struct_index += '[' + parse2(context, ']') + ']';
					context.incrementPosition(1);
				}
			}
		}
		*/
		
		
		//
		// Put all the pieces together
		//
		
		// Above we assigned the following:
		//   ref_scope
		//   sig_ident
		//   must_be_sv_type
		//   end_bit_index
		//   start_bit_index
		//   explicitly_assigned
		//   alignment
		//
		// Turn these into an SV signal.

		// Process/check scope.  Assign sig_scope and index_str.
		if (((ref_scope != null) || alignment != null) &&
			(signal_ident.getType() == IdentifierType.SV_SIG) ||
			(assigned && !explicitly_assigned) ||
			(signal_ident == Identifier.retain_sig_keyword)
		   )
		{
			String signal_desc = (assigned                                       ? "implicitly-assigned signal reference" :
		                         (signal_ident == Identifier.retain_sig_keyword) ? signal_ident.toString() :
                                                                                   "SV signal references");
			if (ref_scope != null) context.report(0, Severity.RECOV_ERROR, "ILL-SCOPE", "Scope not permitted on " + signal_desc + ".  Scope ignored.");
			if (alignment != null) context.report(0, Severity.RECOV_ERROR, "ILL-ALIGN", "Alignment not permitted on " + signal_desc + ".  Alignment ignored.");
			ref_scope = null;
			alignment = null;
		}
		// If no explicit scope, use current scope.
		if (ref_scope == null)
		{
			// No legal explicit scope on this signal reference.
			ref_scope = getLogicalBehScope();
			if (ref_scope == null) {Main.breakpoint();}
			index_str = ref_scope.getSvIndexStr();
		}
		
		
		if(signal_ident.getType() == IdentifierType.SV_SIG)
		{
			// SV_SIG
			
			BitRange temp_bit_range = new BitRange(((SourceFile)getParseBranch_ofType(NodeType.FILE)).topBehHier, signal_name, signal_ident, end_bit_index, start_bit_index, false);
			unstagedRanges.add(signal_name);
			
			ret += temp_bit_range.getLabel() + temp_bit_range.toStringUsage();
		}
		else
		{
			// Not SV_SIG
			
			// Determine bit_range.
			
			BitRange bit_range = null;
			
			// $RETAIN
			if (signal_ident == Identifier.retain_sig_keyword)
			{
				// Retain only supported for single assigned range.  Get it, and check it's type.
				if (assigned_ranges.size() != 1) {context.report(0, Severity.GEN_ERROR, "RETAIN", signal_ident.toString() + " refers to multiple assigned signal ranges, which is not supported.");}
				BitRange assigned_range = assigned_ranges.get(0).getBitRange();
				IdentifierType sig_type = assigned_range.getPipeSignal().getIdentifier().getType();
				//if ((sig_type != IdentifierType.PIPE_SIG) &&
				//	(sig_type != IdentifierType.STATE_SIG))
				if ((sig_type != IdentifierType.PIPE_SIG) &&
					(sig_type != IdentifierType.STATE_SIG) /*&&
					(assigned_range.getPipeSignal().getIdentifier() != Identifier.any_sig_keyword)*/
				   )
					    context.report(0, Severity.FATAL_ERROR, "RETAIN", "Retain of non-pipe-sig/state");

				// Add used bit range.  TODO: Several of the fields of this used range are not applicable.  Cleanup.
				bit_range = addUsedBitRange(ref_scope, assigned_range.getIdentifier(), assigned_range.getPipeSignal().getIdentifier(), new Expression(2), assigned_range.getEndBitIndex(), assigned_range.getStartBitIndex());
			}
			else
			{
				// PIPE_SIG, STATE_SIG, MALFORMED_SIG Identifier.any_sig_keyword.
				if ((signal_ident.getType() != IdentifierType.PIPE_SIG) &&
					(signal_ident.getType() != IdentifierType.STATE_SIG) &&
					(signal_ident.getType() != IdentifierType.MALFORMED_SIG) &&
					(signal_ident != Identifier.any_sig_keyword)
				   )
				{
					context.report(0, Severity.BUG, "BUG", "Unexpected signal type.");
				}
				
				LogicalPipeline ref_pipe = (LogicalPipeline)ref_scope.getSelfOrAncestorOfType(NodeType.PIPELINE);
				if (ref_pipe == null)
					context.report(0, Severity.FATAL_ERROR, "ILL-SCOPE", "Referenced signal " + ref_scope + signal_ident + " must be scoped under a pipeline.");
				if ((ref_scope != null) && (ref_pipe != getLogicalPipeline()) && (alignment == null) && (!ref_pipe.getIdentifier().equals(getLogicalPipeline().getIdentifier())))
					context.report(0, Severity.RECOV_ERROR, "NO-ALIGN", "Cross-pipeline signal references require explicit alignment.  Zero assumed.");
		
				// Get or create the pipesignal.
				// Get:
				//PipeSignal temp_signal = getLogicalBehScope().getSignal(signal_ident);
				PipeSignal temp_signal = ref_scope.getSignal(signal_ident);
				if(temp_signal == null)
				{
					// Create:
					//temp_signal = getLogicalBehScope().addSignal(signal_ident, struct_identifier);
					temp_signal = ref_scope.addSignal(signal_ident, struct_identifier);
				}
				
				// Check bit indices.
				
				boolean typed = (temp_signal.getSvDataTypeIdentifier() != null);
				if ((typed || (signal_ident == Identifier.any_sig_keyword)) &&
					(end_bit_index != null || start_bit_index != null))
				{
					context.report(0, Severity.RECOV_ERROR, "ILL-RANGE", "Bit range on " + signal_ident.toString() + " not allowed.  Bit range ignored.");
					end_bit_index = null;
					start_bit_index = null;
				}
				//-if ((sv_struct_index.length() > 0) && !typed)
				//-{
				//-	context.report(0, Severity.RECOV_ERROR, "SV-TYPE", temp_signal.toScopedString() + " referenced with an SV struct field, but it is not an SV type.");
				//-}
				
				if (assigned)
				{
					// Checking.
                    if (explicitly_assigned && right_hand_side)
                    	context.report(0, Severity.LOGIC_ERROR, "ILL-ASSIGN", "Explicit assigned signal on the right hand side of =.");
                    if (!assigned_ranges.isEmpty() &&
                        (toCompatibleAssignType(assigned_ranges.get(0).getBitRange().getPipeSignal().getIdentifier().getType()) !=
                         toCompatibleAssignType(signal_ident.getType())))
                    	context.report(0, Severity.LOGIC_ERROR, "MIXED-ASSIGN", "Assigned signals in an assignment statement must be of compatible types.  Can't mix pipe signal and state signal under a when.");
                    if (!left_hand_side && (getLogicalWhen() != null))
                    {
                    	// We can't properly condition this assignment.
                    	if (signal_ident.getType() == IdentifierType.STATE_SIG)
                    		context.report(0, Severity.LOGIC_ERROR, "ASSIGN-TYPE", "Assignment of a state signal under a when condition in an unrecognized assignment construct, not supported.\nTo fix: Assign to an intermediate pipe signal, then to state.");
                    	// TODO: Do this in the preprocessor.
                    }
				}
				// Add bit range (assigned or used).
				bit_range =
			        assigned
					    ?           addAssignedBitRange(ref_scope, signal_name, signal_ident,            end_bit_index, start_bit_index)
					    : (BitRange)addUsedBitRange    (ref_scope, signal_name, signal_ident, alignment, end_bit_index, start_bit_index);
			}
			if (signal_ident != Identifier.any_sig_keyword)
			{
			    // Form string representation of this signal reference, and add it to stringSV.
				ret += bit_range.svSignalRef(index_str, true);
			}
			else
			{
				// $ANY.  Drop a placeholder in stringSV.
				((WildcardedSigRef)bit_range).sv_begin_str_pos = sv_str_pos + ret.length();
				ret += "{*" + index_str + "}";   // To be substituted later.
				((WildcardedSigRef)bit_range).sv_end_str_pos = sv_str_pos + ret.length();
			}
		}

		context.setAnchorPos(0);
		return ret;
	}

	
	
    
    final int WILDCARDED_REF_CHARS = 3;  // 3 chars of "{*}" tag in SV string.
    
    /**
     * Extracts the index string written into the SV string of this assignment for the given WildcardedSigRef.
     */
    public String extractIndexStr(WildcardedSigRef any_ref)
    {
    	return stringSV.substring(any_ref.sv_begin_str_pos + 2, any_ref.sv_end_str_pos - 1);
    }
    

	/**
	 * Helper for fixWildcardedSv().  Generates SV signal concatenation for a WildcardedSigRef.
	 * @param any_sig
	 * @return
	 */
	private String wildcardedSvSigConcat(WildcardedSigRef any_sig, String index_str)
	{
		String concat = "";
		if (any_sig.getSigRangeMap().isEmpty())
		{
			// No signals pulled through this wildcarded assigned sig.
			// TODO: Support this case.  Need to formulate the right SV statement.
			any_sig.getParseContext().report(0, Severity.GEN_ERROR, "EMPTY-ANY", "Wildcarded assignment with no signals pulled through it.  This is not yet supported.");
			concat = "{EMPTY}";
		}
		else
		{
			// Expand each signal pulled through the wildcard sig.
			for (BitRange sig_range : any_sig.getSigRangeMap().values())
			{
				concat += ", " + sig_range.svSignalRef(index_str, false);
			}
			
			// Reflect concat in new_sv_string.
			concat = "{" +
			         concat.substring(2) +  // Pulled sigs concated with leading ", " stripped.
			         "}";
		}

		return concat;
	}
	
	
	/**
	 * Fix wildcarded assignments in SV string, now that we've pulled signals through this assignment.
	 * 
	 * @param any_assigned The assigned WildcardedSigRef of this Assignment (to be verified).
	 */
	public void fixWildcardedSv(WildcardedSigRef any_assigned)
	{
		// Note, we rely on the assigned/used iterator being ordered as in the SV statement, and assignments to precede uses.
		
		// As we process wildcard sigs, we build a new_sv_string from the old.  sv_string_pos tracks our position in the old string.
		String new_sv_string = "";
		int sv_string_pos = 0;
		
		// Process the assignment.
		// TODO: There is currently no access from any_assigned to its Assignment.ContainedBitRange, so we have to iterate.
		boolean done = false;
		for (ContainedBitRange assign : getAssignedRanges())
		{
			
			if (assign.getBitRange() instanceof WildcardedSigRef)
			{
				ActiveParseContext.ReportIf(done, 0, Severity.BUG, "BUG", "Found multiple wildcarded assignments.");
				ActiveParseContext.ReportIf(assign.getBitRange() != any_assigned, 0, Severity.BUG, "BUG", "Found wrong wildcarded assignment.");
				
				// Advance in sv string.
				sv_string_pos = any_assigned.sv_begin_str_pos;
				new_sv_string = stringSV.substring(0, sv_string_pos);
				
				// Extract the index string captured prior to wildcard expansion in the SV string. 
				String index_str = extractIndexStr(any_assigned);
				sv_string_pos += WILDCARDED_REF_CHARS + index_str.length();
					
				// Build concatenation of pulled signals.
				new_sv_string += wildcardedSvSigConcat(any_assigned, index_str);
				
				done = true;
			}
		}
		ActiveParseContext.ReportIf(!done, 0, Severity.BUG, "BUG", "Failed to find wildcarded assignment.");
		
		
		for (Assignment.ContainedBitRange use : getWildcardedUsedRanges())
		{
			WildcardedSigRef any_use = (WildcardedSigRef)(use.getBitRange());
			// Advance in sv string.
			int old_sv_string_pos = sv_string_pos;
			sv_string_pos = any_use.sv_begin_str_pos;
			new_sv_string += stringSV.substring(old_sv_string_pos, sv_string_pos);
			
			// extract the index string captured prior to wildcard expansion in the SV string.
			String index_str = extractIndexStr(any_use);
			sv_string_pos += WILDCARDED_REF_CHARS + index_str.length();
			
			// Build concatenation of pulled signals.
			new_sv_string += wildcardedSvSigConcat(any_use, index_str);
		}
		
		// Finish off new_sv_string;
		new_sv_string += stringSV.substring(sv_string_pos);
		
		stringSV = new_sv_string;
	}

    
   /**
    * We cannot implement WHEN conditioning on assignments w/ a mix of PIPE_SIG and STATE_SIG.  Wildcarded sigs operate on PIPE_SIGs only.
    * SV_SIGs are gated like PIPE_SIGs (though maybe there's a separate error for conditioned SV_SIGs).
    * This method supports the checking for compatibility.
    * @param type
    * @return A type used to determine compatibility of signals in muli-assign statements (those with recognized =).  Equal types may be under the same assignment.
    */
   private IdentifierType toCompatibleAssignType(IdentifierType type)
   {
	   ActiveParseContext.ReportIf(!type.isSig(), 0, Severity.BUG, "BUG", "Expected a signal type.");
	   if ((type == IdentifierType.STATE_SIG) && getLogicalWhen() != null)
	   {
		   return type;
	   } else
	   {
		   return IdentifierType.PIPE_SIG;
	   }
   }
}
