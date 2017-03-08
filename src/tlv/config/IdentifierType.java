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

package tlv.config;
import tlv.Main;
import tlv.parse.ActiveParseContext;
import tlv.parse.ParseContext;
import tlv.parse.ParseNode;
import tlv.parse.identifier.Identifier;
import tlv.parse.identifier.Identifier.Syntax;
import tlv.utilities.Severity;


/**
 * Base class defining, for a particular TLV version, the syntax used for Identifiers, including keywords and operators.
 */
public enum IdentifierType
{
	// TODO: Extend this class to support Identifier.EXPRESSION.
	
    // Enumeration of identifier types:
	ERROR   (      "!!!", Identifier.Syntax.MIXED_CASE, "ERROR"),                 // Used as a return value when no legal Type is appropriate.
	PIPELINE(      "|",   Identifier.Syntax.LOWER_CASE, "pipeline"),// TODO: Will be LOWER_CASE.  Must also fix this.mixedCase(..) and this.formatToType(..) methodS.
	BEH_HIER(      ">",   Identifier.Syntax.LOWER_CASE, "behavioral hierarchy"),
	GROUPING(      ">",   Identifier.Syntax.CAMEL_CASE, "grouping"),              // A form of behavioral hierarchy that does not introduce scope.  Haven't settled on a syntax.
	PIPE_SIG(      "$",   Identifier.Syntax.LOWER_CASE, "pipeline signal"),
	STATE_SIG(     "$",   Identifier.Syntax.CAMEL_CASE, "pipeline state signal"), // Retains a value over invalid when conditions.  Has a reset state.
    SIG_KEYWORD(   "$",   Identifier.Syntax.UPPER_CASE, "pipeline signal keywords"),  // $ANY, $RETAIN
	ASSIGNED_PIPE_SIG(   "$$",Identifier.Syntax.LOWER_CASE, "explicitly an assigned pipeline signal"),
	ASSIGNED_STATE_SIG(  "$$",Identifier.Syntax.CAMEL_CASE, "explicitly an assigned pipeline state signal"),
    ASSIGNED_SIG_KEYWORD("$$",Identifier.Syntax.UPPER_CASE, "explicitly an assigned pipeline signal keywords"),
	// This is enabled by ^no_state_sigs and permits arbitrary names for pipe signals while folks get comfortable with the syntax and as an intermediate step for conversion.
	// In this mode, sigs will be PIPE_SIG, MALFORMED_SIG, which are treated identically.  $UPPER_CASE sigs may or may not work.   STATE_SIG is forced to MALFORMED_SIG.
	MALFORMED_SIG( "$",   Identifier.Syntax.MIXED_CASE, "malformed pipeline signal"),
	MALFORMED_ASSIGNED_SIG( "$$", Identifier.Syntax.MIXED_CASE, "malformed explicitly assigned pipeline signal"),
	LEGACY_WHEN(      "?",Identifier.Syntax.LOWER_CASE, "depricated when (w/ pipeline signal condition)"),       // When clause specifying a pipe signal condition for validity.
	LEGACY_WHEN_STATE("?",Identifier.Syntax.CAMEL_CASE, "depricated when (w/ pipeline state signal condition)"), // When clause specifying a state pipe signal condition for validity.
	WHEN(          "?$",  Identifier.Syntax.LOWER_CASE, "when (w/ pipeline signal condition)"),       // When clause specifying a pipe signal condition for validity.
	WHEN_STATE(    "?$",  Identifier.Syntax.CAMEL_CASE, "when (w/ pipeline state signal condition)"), // When clause specifying a state pipe signal condition for validity.
	SV_WHEN(       "?*",  Identifier.Syntax.MIXED_CASE, "when (w/ SV condition)"),     // When clause specifying an SV signal condition for validity.
	PHYS_HIER(     "-",   Identifier.Syntax.LOWER_CASE, "physical hierarchy"),
	PHYS_BEH_HIER( "->",  Identifier.Syntax.LOWER_CASE, "physical and behavioral hierarchy"),// Combined physical and behavioral hierarchy.
	STAGE_NAME(    "@",   Identifier.Syntax.LOWER_CASE, "pipeline stage name"),
	STAGE_EXPR(    "@",   Identifier.Syntax.EXPRESSION, "pipeline stage expression"),
	ALIGNMENT_EXPR("#",   Identifier.Syntax.EXPRESSION, "pipeline stage alignment"), // TODO: Need to flesh out all syntax of alignments.  (#+/#-, #foo=bar, etc)
	TLV_ATTRIBUTE(     "^",  Identifier.Syntax.CAMEL_CASE, "attribute that is part of TLV language"),    // Used to control SV code generation, or propagated to downstream tools.
	PROJ_RTL_ATTRIBUTE("^^", Identifier.Syntax.MIXED_CASE, "attribute propagated as an RTL attribute, known by project"),  // TODO: Should have a ProjSpecific callback to check/modify these (for PROJ_ATTRIBUTE as well).
	MISC_RTL_ATTRIBUTE("^!", Identifier.Syntax.MIXED_CASE, "attribute propageted as an RTL attribute, unknown by project"),
	PROJ_ATTRIBUTE(    "^",  Identifier.Syntax.LOWER_CASE, "attribute defined by project"),    // Available for consumption by ProjSpecific code.
	SV_SIG  (      "*",   Identifier.Syntax.MIXED_CASE, "System Verilog signal"),
	SV_DATATYPE(   "**",  Identifier.Syntax.MIXED_CASE, "System Verilog data type"),
	KEYWORD1(      "\\",  Identifier.Syntax.MIXED_CASE, "keyword 1");      // Can add KEYWORD2, etc. with different prefix chars, and update isKeyword(...).

	
    private final char prefix_char;    // The first character of the prefix.  If prefix_str is non-null, this will match the first char (or be '\0' if prefix_str == "").  TODO: Could let prefix_str == null if prefix_char == '\0'.
    private String prefix_str;   // The prefix string, or may be null if the prefix is a single char, which is the most common case.
    //private final int syntax;  // The syntax as given in the constructor.
    private final Identifier.Syntax syntax;
    private final String desc; // The description.
    
    private boolean initialized = false;
    // Macro-types:
    private final boolean is_sig;
    private final boolean is_legal_sig;
    private final boolean is_staged_sig;
    private final boolean is_assigned_sig;
    private final boolean is_when;
    private final boolean is_attribute;
    private final boolean is_stage;
    private final boolean is_behavioral;
    private boolean is_keyword;

    
    public void assertInitialized()
    {
    	assert initialized;
    }
    
    public int ordinalMask()
    {
    	return (1 << ordinal());
    }
    
    public String getDesc()
    {
    	return desc;
    }
    
    public String getQuotedDesc()
    {
    	return "\"" + desc + "\"";
    }
    
    public Identifier.Syntax getSyntax()
    {
    	return syntax;
    }
    
    public static String maskedNames(int id_mask)
    {
    	String ret = "";
        for (IdentifierType i : IdentifierType.values())
        {
        	if ((i.ordinalMask() & id_mask) != 0)
        	{
        		// This type is included.
        		ret += ((ret.length() <= 0) ? "" : ", ") + i;
        	}
        }
        return ret;
    }
    
    public int prefixLength()
    {
    	return prefixLength(prefix_char, prefix_str);
    }
    
    public String getPrefix()
    {
    	return ((prefix_str == null) ? Character.toString(prefix_char) : prefix_str);
    }
    
    static private int prefixLength(char prefix_char, String prefix_str)
    {
    	return (prefix_str == null) ? 1 : prefix_str.length();
    }
    
    /**
     * Does this Identifier match the given prefix and syntax?
     * @param prefix_char_
     * @param prefix_str_
     * @param syntax_
     * @return
     */
    private boolean matches(char prefix_char_, String prefix_str_, Identifier.Syntax syntax_)
    {
    	int prefix_length = prefixLength();
    	return (prefix_length == prefixLength(prefix_char_, prefix_str_)) &&
    	       (((prefix_length == 1) && (prefix_char == prefix_char_)) ||
    	        ((prefix_length != 1) && (prefix_str_.equals(prefix_str)))
    	       ) &&
    	       (syntax == syntax_);
    }
    
    /**
     * When parsing identifiers, this is called first to see if the type depends on name syntax (vs. just prefix).  In other words, is mixed-case permitted.
     * Note that mixedOk(...) is used where there is a need for mixed and non-mixed variants with the same prefix.
     * @param prefix_char
     * @param prefix_str
     * @return This prefix is that of a keyword and therefore mixed case is permitted.
     */
    public static boolean mixedCase(char prefix_char, String prefix_str)
    {
    	return ((prefix_str == null) ? (prefix_char == '\\' ||
    			                        prefix_char == '*')//  ||
    			                     : (prefix_str.equals("^^") ||
    			                        prefix_str.equals("?*") ||
    			                        prefix_str.equals("**"))
    		   );
    }
    
    /**
     * When parsing identifiers, call this 
     * @param prefix_char
     * @param prefix_str
     * @return True if a mixed-case variant with the given prefix should be permitted, even though non-mixed varients exist.
     */
    public static boolean mixedOk(char prefix_char, String prefix_str)
    {
    	return (prefix_char == '$');  // MALFORMED_SIG, used for conversions and back-compatibility.
    }
    
    /**
     *  For parsing identifiers.  This is done in this class to encapsulate formatting.  It can be done efficiently here with knowledge of the mapping, but must be updated for a new mapping.
     *  Returns the type of the identifier.  Reverse of the mapping provided by this enumeration.  ActiveParseContext.get() should be pointing to the identifier being parsed.
     * @param prefix_char_
     * @param prefix_str_
     * @param syntax
     * @return Type of the identifier.
     */
    // Verified to be consistent for all IdentifierTypes by consistencyCheck().
    static public IdentifierType formatToType(char prefix_char, String prefix_str, Identifier.Syntax syntax /*, int version = TlvContext.VERSION_CURRENT */ )
    {
    	IdentifierType ret = null;
    	
    	int prefix_length = prefixLength(prefix_char, prefix_str);
    	if (prefix_length == 1)
    	{
    		// Prefix length is 1 char.
    		
    		switch (syntax)
    		{
    		case LOWER_CASE:
    			switch (prefix_char)
    			{
    			case '$': ret = PIPE_SIG;  break;
    			case '|': ret = PIPELINE;  break;
    			case '>': ret = BEH_HIER;  break;
    			case '-': ret = PHYS_HIER; break;
    			case '?': ret = LEGACY_WHEN;    break;
    			case '@': ret = STAGE_NAME; break;
    			case '^': ret = PROJ_ATTRIBUTE; break;
    			}
    			break;
    		case CAMEL_CASE:
    			switch (prefix_char)
    			{
    			case '$': ret = STATE_SIG; break;
    			case '>': ret = GROUPING; break;
    			case '?': ret = LEGACY_WHEN_STATE;break;
    			case '^': ret = TLV_ATTRIBUTE; break;
    			}
    			break;
    		case UPPER_CASE:
    			switch (prefix_char)
    			{
    			case '$': ret = SIG_KEYWORD; break;
    			}
    			break;
    		case MIXED_CASE:
    			switch (prefix_char)
    			{
    			case '\\':ret = KEYWORD1;  break;
    			case '*': ret = SV_SIG;   break;
    			case '$': ret = MALFORMED_SIG; break;
    			}
    			break;
    		case EXPRESSION:
    			switch (prefix_char)
    			{
    			case '@': ret = STAGE_EXPR; break;
    			case '#': ret = ALIGNMENT_EXPR; break;
    			}
    			break;
	        }
        } else
        if (prefix_length > 1)
        {
        	// TODO: For performance, split based on prefix_char.
        	if      (WHEN.matches              (prefix_char, prefix_str, syntax)) ret = WHEN;
        	else if (WHEN_STATE.matches        (prefix_char, prefix_str, syntax)) ret = WHEN_STATE;
        	else if (SV_WHEN.matches           (prefix_char, prefix_str, syntax)) ret = SV_WHEN;
        	else if (ASSIGNED_PIPE_SIG.matches (prefix_char, prefix_str, syntax)) ret = ASSIGNED_PIPE_SIG;
        	else if (ASSIGNED_STATE_SIG.matches(prefix_char, prefix_str, syntax)) ret = ASSIGNED_STATE_SIG;
        	else if (ASSIGNED_SIG_KEYWORD.matches (prefix_char, prefix_str, syntax)) ret = ASSIGNED_SIG_KEYWORD;
        	else if (MALFORMED_ASSIGNED_SIG.matches(prefix_char, prefix_str, syntax)) ret = MALFORMED_ASSIGNED_SIG;
        	else if (SV_DATATYPE.matches       (prefix_char, prefix_str, syntax)) ret = SV_DATATYPE;
        	else if (PROJ_RTL_ATTRIBUTE.matches(prefix_char, prefix_str, syntax)) ret = PROJ_RTL_ATTRIBUTE;
        	else if (MISC_RTL_ATTRIBUTE.matches(prefix_char, prefix_str, syntax)) ret = MISC_RTL_ATTRIBUTE;
        	else if (PHYS_BEH_HIER.matches     (prefix_char, prefix_str, syntax)) ret = PHYS_BEH_HIER;
        	else if (ERROR.matches             (prefix_char, prefix_str, syntax)) ret = ERROR;
        }
    	if (ret == null)
    	{
    		ActiveParseContext.Report(0, Severity.FATAL_ERROR, "PARSE-IDENT", "Identifier with prefix \'" + ((prefix_str != null) ? prefix_str : prefix_char) +
    				                                                    "\' with " + syntax + " syntax does not match any TLV identifier type.");
    	} /*- else
    	{
    		// Check consistency.
    		if (!ret.matches(prefix_char, prefix_str, syntax))
    		{
    			ActiveParseContext.Report(0, Severity.BUG, "PARSE-IDENT", "IdentifierType.formatToType() returning inconsistent IdentifierType: " + ret);
    		}
    	} */
    	
    	return ret;
    }
    
    
    private static int assigned_sig_ordinal_mask =
			ASSIGNED_PIPE_SIG.ordinalMask() |
			ASSIGNED_STATE_SIG.ordinalMask() |
			ASSIGNED_SIG_KEYWORD.ordinalMask() |
			MALFORMED_ASSIGNED_SIG.ordinalMask();
    public static int assignedSigOrdinalMask()
    {
    	return assigned_sig_ordinal_mask;
    }
    
    private static int staged_sig_ordinal_mask =
    		PIPE_SIG.ordinalMask() |
    		STATE_SIG.ordinalMask() |
    		ASSIGNED_PIPE_SIG.ordinalMask() |
    		ASSIGNED_STATE_SIG.ordinalMask() |
    		MALFORMED_SIG.ordinalMask() |
			MALFORMED_ASSIGNED_SIG.ordinalMask();
    public static int stagedSigOrdinalMask()
    {
    	return staged_sig_ordinal_mask;
    }
    
     private static int legal_sig_ordinal_mask =
    		PIPE_SIG.ordinalMask() |
			STATE_SIG.ordinalMask() |
			SV_SIG.ordinalMask() |
			SIG_KEYWORD.ordinalMask() |
			ASSIGNED_PIPE_SIG.ordinalMask() |
			ASSIGNED_STATE_SIG.ordinalMask() |
			ASSIGNED_SIG_KEYWORD.ordinalMask();
    public static int legalSigOrdinalMask()
    {
    	return legal_sig_ordinal_mask;
    }
    
   private static int sig_ordinal_mask =
		    legal_sig_ordinal_mask |
    		MALFORMED_SIG.ordinalMask() |
			MALFORMED_ASSIGNED_SIG.ordinalMask();
    public static int sigOrdinalMask()
    {
    	return sig_ordinal_mask;
    }
    
    public boolean isSig()
    {
    	return is_sig;
    }
    
    public boolean isLegalSig()
    {
    	return is_legal_sig;
    }
    
    public boolean isStagedSig()
    {
    	return is_staged_sig;
    }
    
    public boolean isAssignedSig()
    {
    	return is_assigned_sig;
    }
    
    public boolean isWhen()
    {
    	return is_when;
    }
    
    public boolean isAttribute()
    {
    	return is_attribute;
    }
    
    public boolean isStage()
    {
    	return is_stage;
    }
    
    public boolean isBehavioral()
    {
    	return is_behavioral;
    }
    
    static private int keywords_ordinal_mask =
    		KEYWORD1.ordinalMask() |
            TLV_ATTRIBUTE.ordinalMask() |
            PROJ_RTL_ATTRIBUTE.ordinalMask() |
            SIG_KEYWORD.ordinalMask() |
			ASSIGNED_SIG_KEYWORD.ordinalMask();
    static public int getKeywordsOrdinalMask()
    {
    	return keywords_ordinal_mask;
    }

	public boolean isKeyword()
	{
		return is_keyword;
	}

    /**
     * Initialize and check consistency of all IdentifierTypes.  Called by Identifier.init().
     */
    public static void init()
    {
    	for (IdentifierType type: IdentifierType.values())
    	{
    		// Check consistency of type.
        	Main.assertion(type.is_legal_sig    == ((type.ordinalMask() & legalSigOrdinalMask() ) != 0));
        	Main.assertion(type.is_sig          == ((type.ordinalMask() & sigOrdinalMask()      ) != 0));
        	Main.assertion(type.is_staged_sig   == ((type.ordinalMask() & stagedSigOrdinalMask()) != 0));
        	Main.assertion(type.is_assigned_sig == ((type.ordinalMask() & assignedSigOrdinalMask()) != 0));
    		Main.assertion(type.is_when ==
    			   (type == IdentifierType.WHEN ||
	    			type == IdentifierType.WHEN_STATE ||
	    			type == IdentifierType.SV_WHEN ||
	    			type == IdentifierType.LEGACY_WHEN ||
	    			type == IdentifierType.LEGACY_WHEN_STATE));
    		Main.assertion(type.is_attribute ==
    			   (type == IdentifierType.TLV_ATTRIBUTE ||
        			type == IdentifierType.PROJ_RTL_ATTRIBUTE ||
        	        type == IdentifierType.MISC_RTL_ATTRIBUTE ||
        			type == IdentifierType.PROJ_ATTRIBUTE));
    		Main.assertion(type.is_stage ==
    			   (type == IdentifierType.STAGE_NAME ||
        			type == IdentifierType.STAGE_EXPR));
    		Main.assertion(type.is_behavioral ==
    			   !(type.isStage() ||
    			     type == IdentifierType.PHYS_HIER ||
    			     type.isAttribute()));
    		type.is_keyword = ((keywords_ordinal_mask & type.ordinalMask()) != 0);

    		
    		// formatToType().
			if (formatToType(type.prefix_char, type.prefix_str, type.syntax) != type)
			{
				ActiveParseContext.Report(0, Severity.BUG, "IDENT", "IdentifierType.formatToType() returning inconsistent IdentifierType: " + type);
			}
    	}
    }
    

    
    IdentifierType(String prefix_str_, Identifier.Syntax syntax_, String desc_)
    {
    	// Checking.
    	// It is not legal to have an expression identifier type with a prefix ending with '+'/'-'.
    	char last_char = prefix_str_.charAt(prefix_str_.length() - 1);
    	if ((last_char == '+' ||
    		 last_char == '-') &&
    		syntax_ == Identifier.Syntax.EXPRESSION
    	   )
    	{
    		System.out.println("Error: Constructed expression IdentifierType with last prefix char of +/-, which is ambiguous for parsing.");
    	}
    	
    	prefix_char = prefix_str_.charAt(0);
    	if (prefix_str_.length() > 1)
    	{
    		prefix_str = prefix_str_;
    	}
    	syntax = syntax_;
    	desc = desc_;
    	
    	// Macro types.  (I'm guessing ordinal is assigned at construction, so ordinal masks for other other types may not be available yet.
    	//                Have to determine these based on inputs.  consistencyCheck() uses)
    	is_when = prefix_char == '?';
    	is_sig = prefix_str_.equals("*") ||
    			 (prefix_char == '$');
    	is_legal_sig = prefix_str_.equals("*") ||
   			           ((prefix_char == '$') && (syntax != Syntax.MIXED_CASE));
    	is_staged_sig = (prefix_char == '$') && (syntax != Syntax.UPPER_CASE);
    	is_assigned_sig = (prefix_char == '$') && (prefix_str_.length() > 1);
    	is_attribute = prefix_char == '^';
    	is_stage = prefix_char == '@';
    	is_behavioral = !(is_stage || is_attribute || (prefix_str_.equals("-") && (syntax == Syntax.LOWER_CASE)));
    }
}
