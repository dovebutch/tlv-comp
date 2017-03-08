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
import tlv.utilities.Severity;

/**
 * Encapsulates the currently active context of parsing in a given thread (currently for the app).  This class can be interfaced via static methods as a
 * convenience.  Each static method accesses the one (thread's) ActiveParseContext and calls the corresponding non-static method.  So, ActiveParseContext.Set(...)
 * can be used as a shorthand for ActiveParseContext.get().set(...) or in place of passing around the ActiveParseContext.  These static methods are
 * (unconventionally) begun with an upper-case letter, with the same name as their non-static counterparts.
 * 
 * @author sfhoover
 *
 */
public class ActiveParseContext extends ParseContext
{
	static private ActiveParseContext context = new ActiveParseContext();  // The one instance of this class.  (Eventually, this could be per-thread.
	static private ActiveParseContext head_context = null;
	
	protected int exit_status;  // The exit status for the program to return.
	
	
	private ActiveParseContext()
	{
		super(null);
		active_context = this;
		// Track a single head context.
		if (head_context != null) {System.err.println("Error: Multiple ParseContext's constructed."); System.exit(1);}
		head_context = this;
	}
	

	// Methods for pushing/popping context.
	//
	// These methods keep track of multiple contexts.
	//   - pushContext()/popContext() enable the context to be restored to a captured state.
	//   - aquireAlternateContext()/releaseAlternateContext enable multiple contexts to be active simultaneously.
	//
	// pushed_context forms a singly linked list, where acquired contexts are at the head, then the current context (get()),
	// then pushed contexts.
	// As contexts are popped/released, errors reported in the different contexts are accumulated appropriately.
	public void pushContext()
	{
		ParseContext new_context = new ParseContext(this);
		new_context.pushed_context = pushed_context;
		pushed_context = new_context;
	}
	
	public void popContext()
	{
		ParseContext popped_context = pushed_context;
		reportIf(popped_context == null, 0, Severity.FATAL_BUG, "CONTEXT-POP", "No context to pop.");
		copy(popped_context);
		pushed_context = popped_context.pushed_context;
		// Done w/ popped_context.
		popped_context.delete();
	}
	
	public static void PushContext()
	{
		get().pushContext();
	}
	public static void PopContext()
	{
		get().popContext();
	}
	
	/**
	 * Make sure there aren't any lingering contexts.
	 */
	public static void cleanup()
	{
		// Could pop/release any lingering contexts, but, at least for now, it's considered an error, so why bother.
		context.reportIf(context.pushed_context != null, 0, Severity.BUG, "LINGERING-CONTEXT", "ParseContext was pushed and never popped.");
	}
	
	
	public void adjustExitStatus(Severity sev)
	{
		int status = sev.getExitCode();
		// Higher status is used for more sever errors
		if (status > exit_status)
		{
			exit_status = status;
		}
	}
	
	public int getExitStatus()
	{
		return exit_status;
	}
	
	public static ActiveParseContext get()
	{
		return context;
	}
	
		
	/**
	 * @return ParseNode being parsed, if current parsing has been encapsulated in a ParseNode.
	 */
	public static ParseNode GetParseNode()
	{
		return get().getParseNode();
	}
		
	/**
	 * @return Previous ParseNode parsed, if current parsing is of a string not yet encapsulated in a ParseNode.
	 */
	public static ParseNode GetPrevParseNode()
	{
		return get().getPrevParseNode();
	}
	
	/**
	 * @return String being parsed.
	 */
	public static String GetString()
	{
		return get().getString();
	}
	
	/**
	 * @return Position of current parsing within the String being parsed.
	 */
	public static int GetPosition()
	{
		return get().getPosition();
	}
	
	/**
	 * Set context to that of a ParseNode being parsed.
	 * To indicate that the context is not known, use -1 for pos_, and assign parse_node_ to provide access to the file.
	 * @param parse_node_ The node being parsed.  Null only if it is not even known what file is being parsed.
	 * @param pos_ Position of parsing within parse_node_.getString(), or -1 if a node is not being parsed.
	 * @param line_num_ Must be consistent with parse_node_.  Checked upon error.  -1 for no checking.
	 */
	public static void Set(ParseNode parse_node_, int pos_, int line_num_)
	{
		Set(parse_node_, null, pos_, line_num_);
	}
	
	/**
	 * Set context to that of a String being parsed, subsequent to a given ParseNode.
	 * @param parse_node_ The previous node parsed, prior to parsing the given String.
	 * @param str_ The string being parsed.
	 * @param pos_ Position of parsing within str_.
	 * @param line_num_ Must be consistent with parse_node_ and str_.  Checked upon error.  -1 for no checking.
	 */
	public static void Set(ParseNode parse_node_, String str_, int pos_, int line_num_)
	{
		get().set(parse_node_, str_, pos_, line_num_);
	}
	
	/**
	 * Increment context position.
	 * @param incr
	 */
	public static void IncrementPosition(int incr)
	{
		get().incrementPosition(incr);
	}
	
	/**
	 * Set the position of parsing within the current parse string.
	 * @param pos_
	 */
	public static void SetPosition(int pos_)
	{
		get().setPosition(pos_);
	}
	
	/**
	 * Clears the context to reflect no parse context.
	 */
	public static void Clear()
	{
		Set(null, "", 0, -1);
	}
	
	public static void ReportException(int pos_offset, Severity sev, String tag, String message, Exception e)
	{
		ParseContext c = get();
		c.reportException(pos_offset, sev, tag, message, e);
	}
	
	static public void ReportIf(boolean cond, int pos_offset, Severity sev, String tag, String message)
	{
		if (cond) Report(pos_offset, sev, tag, message);
	}
	
	public static void Report(int pos_offset, Severity sev, String tag, String message)
	{
		ReportException(pos_offset, sev, tag, message, null);
	}
	

}