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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tlv.Main;
import tlv.behavioral.LogicalBranch;
import tlv.utilities.Severity;

/**
 * Encapsulates the context of parsing, in particular the position in the file.  This is associated with an ActiveParseContext which tracks all the
 * activity reported in this ParseContext.
 * TODO: Could extend this beyond parsing, to encapsulate the phase of execution, etc.
 * 
 * @author sfhoover
 *
 */
public class ParseContext
{
	protected ActiveParseContext active_context = null;  // The one instance of this class.  (Eventually, this could be per-thread.)
	
	private ParseNode parse_node = null;  // The node most recently parsed.
	private String str = null;   // Text being processed subsequent to the given node, or null if parsing is of parse_node.
	                             // TODO: Change convention here to set str to node's string if there's a node, so we always have a str to parse directly.
	private int line_num = -1;  // Line number being parsed, to be validated vs. parse_node.getLineNumber() and '\n's in str, or -1 for no validation.
	private int pos = -1;       // Position of current parsing within str or parse_node.getString().
	
	private boolean no_state_sigs = false;  // Defined as a file attribute, and set here while parsing the file.
	                                        // If set, pipe signals are mixed case.  There are no states.  Can be convenient for code conversion.
	protected ParseContext pushed_context = null;  // See push().
	
	
	protected ParseContext(ActiveParseContext active_context_)
	{
		active_context = active_context_;
	}
	
	public ParseContext()
	{
		this(ActiveParseContext.get());
	}
	
	/**
	 * Copy constructor.
	 * @param orig_context
	 */
	public ParseContext(ParseContext orig_context)
	{
		copy(orig_context);
	}
	
	/**
	 * Copy from given context.
	 * The following member variables are NOT copied:
	 *    - exit_status
	 *    - pushed_context
	 * @param source_context
	 */
	public void copy(ParseContext source_context)
	{
		active_context = source_context.active_context;
		parse_node     = source_context.parse_node;
		str            = source_context.str;
		line_num       = source_context.line_num;
		pos            = source_context.pos;
		no_state_sigs  = source_context.no_state_sigs;	
	}
	
	
	// Prepare for destruction.  Not necessary to call this.  Just here in case it helps garbage collection.
	public void delete()
	{
		pushed_context = null;
		parse_node = null;
	}
	
	
	/**
	 * @return ParseNode being parsed, if current parsing has been encapsulated in a ParseNode.
	 */
	public ParseNode getParseNode()
	{
		return (str == null) ? parse_node : null;
	}
		
	/**
	 * @return Previous ParseNode parsed, if current parsing is of a string not yet encapsulated in a ParseNode.
	 */
	public ParseNode getPrevParseNode()
	{
		return (str == null) ? null : parse_node;
	}
	
	/**
	 * @return String being parsed.
	 */
	public String getString()
	{
		return (str == null) ? parse_node.getString() : str;
	}
	
	/**
	 * @return remainder of string being parsed from current position.
	 */
	public String getRemainingString()
	{
		return getString().substring(pos);
	}
	
	/**
	 * @return Position of current parsing within the String being parsed.
	 */
	public int getPosition()
	{
		return pos;
	}
	
	/**
	 * Set context to that of a ParseNode being parsed.
	 * To indicate that the context is not known, use -1 for pos_, and assign parse_node_ to provide access to the file.
	 * @param parse_node_ The node being parsed.  Null only if it is not even known what file is being parsed.
	 * @param pos_ Position of parsing within parse_node_.getString(), or -1 if a node is not being parsed.
	 * @param line_num_ Must be consistent with parse_node_.  Checked upon error.  -1 for no checking.
	 * @return 'this' (can be convenient).
	 */
	public ParseContext set(ParseNode parse_node_, int pos_, int line_num_)
	{
		return set(parse_node_, null, pos_, line_num_);
	}
	
	/**
	 * Set context to that of a String being parsed, subsequent to a given ParseNode.
	 * @param parse_node_ The previous node parsed, prior to parsing the given String.
	 * @param str_ The string being parsed.
	 * @param pos_ Position of parsing within str_.
	 * @param line_num_ Must be consistent with parse_node_ and str_.  Checked upon error.  -1 for no checking.
	 * @return 'this' (can be convenient).
	 */
	public ParseContext set(ParseNode parse_node_, String str_, int pos_, int line_num_)
	{
		parse_node = parse_node_;
		str = str_;
		pos = pos_;
		line_num = line_num_;
		return this;
	}
	
	/**
	 * Get the char pointed to by pos.
	 * @return The current character.
	 */
	public char getCurrentChar()
	{
		return getString().charAt(pos);
	}
	
	/**
	 * @param offset
	 * @return The char pointed to by pos + offset, or '\0' if outside the string.
	 */
	public char getCharOffset(int offset)
	{
		return Main.safeCharAt(getString(), pos + offset);
	}
	
	/**
	 * @return True if pos is at (or after) end of string.
	 */
	public boolean doneString()
	{
		return (pos >= getString().length());
	}
	
	/**
	 * Increment context position.
	 * @param incr
	 */
	public void incrementPosition(int incr)
	{
		pos += incr;
	}
	
	/**
	 * Set the position of parsing within the current parse string.
	 * @param pos_
	 */
	public void setPosition(int pos_)
	{
		pos = pos_;
	}
	
	/**
	 * Clears the context to reflect no parse context.
	 */
	public void clear()
	{
		set(null, "", 0, -1);
	}
	
	
	
	//
	// Anchor
	//
	
	// The anchor point remembers a position in the parse context string.  It is set only by methods that contain "anchor" in their name.
	int anchor_pos = 0;
	/**
	 * Sets the anchor position to the current position plus the given offset.
	 * @param offset
	 */
	public void setAnchorPos(int offset)
	{
		anchor_pos = pos + offset;
	}
	
	/**
	 * @return The anchored string -- from the anchor position to the current position.
	 */
	public String getAnchoredString()
	{
		return getString().substring(anchor_pos, pos);
	}
	
	/**
	 * @param anchor_offset An offset for the anchor position.
	 * @param current_pos_offset An offset for the current position.
	 * @return The anchored string -- from the anchor position to the current position, where offsets can be applied.
	 */
	public String getAnchoredString(int anchor_offset, int current_pos_offset)
	{
		return getString().substring(anchor_pos + anchor_offset, pos + current_pos_offset);
	}
	
	/**
	 * Like getAnchoredString(), but also reanchors to current position.
	 * @return Anchored string.
	 */
	public String absorbAnchoredString()
	{
		String ret = getAnchoredString();
		anchor_pos = pos;
		return ret;
	}
	
	/**
	 * Like getAnchoredString(...), but also reanchors to current position.
	 * @param anchor_offset An offset for the anchor position.
	 * @param current_pos_offset An offset for the current position.
	 * @return Anchored string.
	 */
	public String absorbAnchoredString(int anchor_offset, int current_pos_offset)
	{
		String ret = getAnchoredString(anchor_offset, current_pos_offset);
		anchor_pos = pos;
		return ret;
	}
	
	
	//
	// Parsing methods.
	//
	
	/**
	 * @param pattern
	 * @return A matcher for the context.
	 */
	public Matcher getMatcher(Pattern pattern)
	{
		String str = getString();
		Matcher matcher = pattern.matcher(str);
		matcher.region(pos, str.length());
		return matcher;
	}

	
	/**
	 * Parse over the next character, expecting to find the given character.  If the character does not match, an error is reported.
	 * Position is updated to reflect the parsing (is successful).
	 * 
	 * @param exp_char The char to expect to find next.
	 * @param sev Error severity if char is not found.
	 * @param tag Tag for an error.
	 * @param additional_message.  Error message, in addition to the one provided by this method.  Use null if none (not "").
	 * @return success.
	 */
	public boolean parseLiteralChar(char exp_char, Severity sev, String tag, String additional_message)
	{
		boolean success = getCurrentChar() == exp_char;
		if (!success)
		{
			report(0, sev, tag, combinedMessage("Expected '" + exp_char + "'.", additional_message));
		}
		else
		{
			incrementPosition(1);
		}
		return success;
	}
	
	/**
	 * Parse over whitespace.
	 * @return True if there was whitespace.
	 */
	public boolean parseWhitespace()
	{
		boolean whitespace = false;
		while ((pos < getString().length()) && Character.isWhitespace(getCurrentChar()))
		{
			incrementPosition(1);
			whitespace = true;
		}
		return whitespace;
	}
	

	
	/**
	 * Parse up to and over the given delimiter char.  If it is not found, an error is reported.
	 * Position is updated to reflect the parsing.
	 * 
	 * @param delim_char The delimiter char to expect to find.
	 * @param sev Error severity if char is not found.
	 * @param tag Tag for an error.
	 * @param additional_message.  Error message, in addition to the one provided by this method.  Use null if none (not "").
	 * @return The extracted string, or null if delimiter is not found.
	 */
	public String parseUntil(char delim_char, Severity sev, String tag, String additional_message)
	{
		int delim_pos = getString().indexOf(delim_char, getPosition());
		boolean success = delim_pos >= 0;
		String ret = null;
		if (!success)
		{
			report(0, sev, tag, combinedMessage("Cannot find delimiter '" + delim_char + "'.", additional_message));
		}
		else
		{
			ret = getString().substring(getPosition(), delim_pos);
			setPosition(delim_pos + 1);
		}
		return ret;
	}
	
	/**
	 * Parse up to and over the given pattern.  If it is not found, an error is reported.
	 * Position is updated to reflect the parsing.
	 * 
	 * @param pattern The pattern to expect to find.
	 * @param sev Error severity if char is not found.
	 * @param tag Tag for an error.
	 * @param additional_message.  Error message, in addition to the one provided by this method.  Use null if none (not "").
	 * @return The extracted string, or null if delimiter is not found.
	 */
	public void parseUntil(Pattern pattern, Severity sev, String tag, String additional_message)
	{
		Matcher matcher = getMatcher(pattern);
		if (matcher.find())
		{
			setPosition(matcher.end());
		}
		else
		{
			report(0, sev, tag, combinedMessage("Cannot find delimiter \"" + pattern.toString() + "\".", additional_message));
		}
	}
	/**
	 * Parse up to and over the given pattern.  If pattern not found, advance to end-of-string iff to_end.
	 * @param pattern
	 * @param to_end
	 * @return True iff pattern found.
	 */
	public boolean parseUntil(Pattern pattern, boolean to_end)
	{
		Matcher matcher = getMatcher(pattern);
		if (matcher.find())
		{
			setPosition(matcher.end());
			return true;
		}
		else
		{
			if (to_end)
			{
				setPosition(getString().length());
			}
		}
		return false;
	}
	

	// The following methods match those of Matcher, performing their matching operation on the string of this ParseContext, and updating
	// the position appropriately.  They return the Matcher used to perform the operation which retains its meaning only until the next
	// match operation.
	
	
	/**
	 * Parses over the given pattern, updating the context's position appropriately.
	 * 
	 * @param pattern The pattern to match.
	 * @param swallow Should position be updated to absorb the pattern.
	 * @param sev Error severity if char is not found, or null to return null on no match.
	 * @param tag Tag for an error.
	 * @param additional_message.  Error message, in addition to the one provided by this method.  Use null if none (not "").
	 * @return The matcher used to match the pattern in the context's string, or null if the match was unsuccessful.
	 *         It is valid until the next matching method invocation.
	 */
	public Matcher parseMatches(Pattern pattern, boolean swallow, Severity sev, String tag, String additional_message)
	{
		Matcher matcher = getMatcher(pattern);
		if (matcher.lookingAt())
		{
			if (swallow)
			{
				setPosition(matcher.end());
			}
			return matcher;
		}
		else
		{
			if (sev != null)
			{
				report(0, sev, tag, combinedMessage("Failed to find pattern: \"" + pattern.toString() + ".", additional_message));
			}
			return null;
		}
	}
	/**
	 * Parses over the given pattern, updating the context's position appropriately.
	 * 
	 * @param pattern The pattern to match.
	 * 
	 * return the matcher or null if no match.
	 */
	public Matcher parseMatches(Pattern pattern, boolean swallow)
	{
		return (parseMatches(pattern, swallow, null, "", ""));
	}
	
	// TODO: Add similar method parseFind(..).
	
	
	
	// Methods for parsing particular syntaxes.
	static final private Pattern integer_regexp = Pattern.compile("\\d+");
	/**
	 * Parse a decimal integer, w/ no leading +/-, starting at the current position, updating the position to the end of the integer.
	 * 
	 * @param sev Error severity if char is not found.
	 * @param tag Tag for an error.
	 * @param additional_message.  Error message, in addition to the one provided by this method.  Use null if none (not "").
	 * @return the integer value parsed, or -1 in none found.
	 */
	public int parseUnsignedInt(Severity sev, String tag, String additional_message)
	{
        Matcher line_num_matcher = parseMatches(integer_regexp, true, sev, tag, additional_message);
        int ret = -1;
        if (line_num_matcher != null)
        {
        	String int_str = line_num_matcher.group();
			try
			{
				ret = new Integer(int_str);
			}
			catch (NumberFormatException e)
			{
				report(0, Severity.FATAL_BUG, "BUG", "Failed to match an integer string.");
			}

        }
        else
        {
        	report(0, sev, tag, combinedMessage("Expecting an integer.", additional_message));
        }
        return ret;
	}
	
	
	//
	// Parse Modes
	//
	
	public boolean getNoStateSigs()
	{
		return no_state_sigs;
	}
	public void setNoStateSigs(boolean val)
	{
		no_state_sigs = val;
	}
	
	
	private String sourceCodePointerBar(int line_pos, String highlight_char_str)
	{
		String ret = "\t+";
		int last_pos = (line_pos > 30) ? line_pos : 30;
		for (int p = 0; p <= last_pos; p++)
		{
			ret += (p == line_pos) ? highlight_char_str : "-";
		}
		ret += "\n";
		return ret;
	}
	
	// Quick-and-dirty limit on number of errors.
	static int error_count = 0;
	static final int ERROR_LIMIT = 100;
	
	boolean reporting_exception = false;  // Used to avoid reporting errors which reporting errors.
	
	/**
	 * Report an error/warning/inform.
	 * @param context_offset Character offset to report against from the parse location of this context.
	 * @param message Error message.
	 */
	public void reportException(int context_offset, final Severity sev, String tag, String message, Exception e)
	{
		// Three relevant positions in the parse string.
		// string pos:  Beginning of the parse string.
		// context pos: Context pos (this.pos).
		// report pos:  Position to report.
		// Print error report.
		
		// First prevent recursive error reporting.
		ParseNode parse_node = this.parse_node;  // Proxy for this.parse_node, which can be overridden.
		int pos = this.pos;
		if (reporting_exception)
		{
			// Exception reported recursively from within this method.  Squash parse context, in an attempt to avoid further recursion.
			parse_node = null;
			pos = -1;
		}
		reporting_exception = true;
		
	    SourceFile source_file = (parse_node == null)               ? null :
	    	                     (parse_node instanceof SourceFile) ? (SourceFile)parse_node :
	    	                                                          (SourceFile)parse_node.getParseBranch_ofType(NodeType.FILE);
		String error_report_str = sev.toString() + "(" + sev.getExitCode() + ") (" + tag + "): ";
		String source_file_str = ((source_file == null) ? "<unknown>" : source_file.getSourceFile().toString());

		error_report_str += "File \'" + source_file_str + "\'";
		if ((pos < 0) || (source_file == null))
		{
			error_report_str += ".  Context within file unknown.\n";
		} else
		{
			ParseNode.FileLine node_file_line = parse_node.getFileLine();
			
			// Trace macro file stack and represent in source_stack_str.
			String source_stack_str = "";
			ParseNode.FileLine main_file_line = node_file_line;
			while (!(main_file_line.parse_source instanceof SourceFile))
			{
				source_stack_str = " -> " + main_file_line.parse_source.getSourceFile().getName() + ":" + main_file_line.line_num + source_stack_str;
				main_file_line = main_file_line.parse_source.getFileLine();
			}
			
			// Set parse_str, and computed_str_line_num.
			String parse_str;
			int computed_str_line_num = main_file_line.line_num;
			
			if (str == null)
			{
				parse_str = parse_node.getString();
			} else
			{
				parse_str = str;
				computed_str_line_num += parse_node.getNumLines_Self();
			}
			
			// Handle context pos off the end of the string.
			boolean bad_pos = false;
			if (pos + context_offset > parse_str.length())
			{
				bad_pos = true;
				if (pos > parse_str.length())
				{
					this.pos = parse_str.length();
				}
				context_offset = parse_str.length() - pos;
			}
			
			// Walk parse_str to context pos.
			// Count carriage returns in str.
			// Those prior top 'pos' should be accounted for in 'line_num', but not yet in 'computed_line_num'.
			int p = 0;
			int context_line_offset = 0;
			int line_pos = 0;   // position of error within line.
			while (p < pos)
			{
				if (parse_str.charAt(p) == '\n')
				{
					context_line_offset++;
					line_pos = -1;
				}
				p++;
				line_pos++;
			}
			// Walk parse_str to pos + context_offset.
			// Those carriage returns after 'pos' up to 'pos_offset' must be included for reporting.
	        int str_lines_to_report = context_line_offset;
			while (p < pos + context_offset)
			{
				if (parse_str.charAt(p) == '\n')
				{
					str_lines_to_report++;
					line_pos = -1;
				}
				p++;
				line_pos++;
			}
			
			error_report_str += " Line " + (computed_str_line_num + str_lines_to_report) + " (char " + (line_pos + 1) + ")" + source_stack_str +
		                        ", while parsing:\n" + sourceCodePointerBar(line_pos, "v");
	        // Print parse_str.
			p = 0;
			int l = 0;
			do
			{
				// Process next line.
				int start_pos = p;
				while ((p < parse_str.length()) && (parse_str.charAt(p) != '\n'))
				{
					p++;
				}
				error_report_str += "\t" + ((l == (str_lines_to_report)) ? ">" : "|") + parse_str.substring(start_pos, p) + "\n";
				l++;
				p++;
			} while (p < parse_str.length());
			error_report_str += sourceCodePointerBar(line_pos, "^");

			// Check line_num.
			if (line_num != -1)
			{
				error_report_str += "\tNote: (Bug) Internal line tracking is inconsistent.  Line number is also identifier as " + (line_num + str_lines_to_report) + ".\n";
			}
			// Report bad_pos.
			if (bad_pos)
			{
				error_report_str += "\tNote: (Bug) Identified error position is off the end of the string.\n";
			}
		}

		error_report_str += Main.indentString("\t", message) + "\n\n";
		
		// If this is a bug, dump the stack trace.
		if (sev == Severity.BUG || sev == Severity.FATAL_BUG)
		{
			if (e != null)
			{
				error_report_str += "This resulted from the following exception: \n\t" + e.getMessage() + "\n\tIn context:\n";
			}
			StackTraceElement [] st = (e == null)
					                  ? Thread.currentThread().getStackTrace()
					                  : e.getStackTrace();
			for (StackTraceElement ste : st) {
				error_report_str += "\tat " + ste + "\n";
			}
		}
		
		// Actually report it.
		System.err.print(error_report_str);
		System.err.flush();
		if (source_file != null)
		{
			source_file.printError(error_report_str);
		}
		
		Main.breakpoint(false);

		active_context.adjustExitStatus(sev);
		
		// Exit if fatal.
		if (sev == Severity.FATAL_BUG ||
		    sev == Severity.FATAL_ERROR)
		{
			System.exit(active_context.exit_status);
		}
		
		
		reporting_exception = false;
		
		
		// Quick-and-dirty error limit.
		if (sev.ordinal() > Severity.INFORM.ordinal() && error_count++ > ERROR_LIMIT)
		{
			report(0, Severity.FATAL_ERROR, "ERR-LIMIT", "Error limit of " + ERROR_LIMIT + " reached.");
			error_count = 0;  // Avoid recursion.
		}
	}
	
	public void reportIf(boolean cond, int context_offset, Severity sev, String tag, String message)
	{
		if (cond) report(context_offset, sev, tag, message);
	}
		
	/**
	 * Report an error/warning/inform/bug.
	 * @param context_offset Character offset to report against from the parse location of this context.
	 * @param message Error message.
	 */
	public void report(int context_offset, Severity sev, String tag, String message)
	{
		reportException(context_offset, sev, tag, message, null);
	}
	
	/**
	 * Combine error message strings.  The first is expected to be a generic one, from a method that does not have
	 * the broader context, and the second is intended to provide the broader context.
	 * 
	 * @message1 First message string.
	 * @message2 Second message string.
	 * 
	 * @return the combined error message string appropriate for report*(..) methods.
	 */
	public String combinedMessage(String message1, String message2)
	{
		return message1 + ((message2 == null) ? "" : "  " + message2);
	}
}