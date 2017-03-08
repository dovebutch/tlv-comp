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

package tlv.parse.identifier;

import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;

import tlv.config.IdentifierType;
import tlv.parse.ActiveParseContext;
import tlv.parse.ParseContext;
import tlv.utilities.Severity;
import tlv.Main;

/**
 *  An identifier, including a prefix character and a sequence of tokens in lower_case, CammelCase, or ALL_CAPS.  Includes typed numerics and expressions.
 *  The interface to objects of this class is finalized upon construction, so all identical identifiers can share Identifier objects.
 *  
 * @author sfhoover
 *
 */
public class Identifier implements Comparable<Identifier> {
    // Enumeration of syntax (case/delimitation plus numerics/expressions).
	// TODO: Use a proper enum.
	public enum Syntax {LOWER_CASE, UPPER_CASE, CAMEL_CASE, REVERSE_CAMEL_CASE, MIXED_CASE, EXPRESSION, UNDEFINED}
	// TODO: Should NUMERIC and EXPRESSION be combined?
	
    static private boolean init_done = false;
    static private boolean initing = false;
	// Keywords, operators, and [TLV/PROJ/PROJ_RTL]_ATTRIBUTES, initialized by init() (and by a TODO Proj-specific call).
	static protected Hashtable<String, Identifier> keywords;
	
	
    protected final String str;   // Name, including prefix, in it's native form.  Note, this may be a keywords or operand.
                            // This is used as a key in hashtables.
    protected final IdentifierType type;
    
    protected String varients[] = null;   // Varients of this identifier name in different Syntax'es (no prefix chars).
    //protected String lower_case_name = null;  // Lower case version of name with underscore delimitation and no prefix character
                                                //   (generated lazily).
    //protected Vector<String> tokens = null;   // Lower case tokens of identifier (generated lazily).

    
    public String toString()
    {
    	return str;
    }
    
    public int hashCode()
    {
    	return str.hashCode();
    }
    
    public boolean equals(Object id_obj)
    {
    	if (id_obj == this) return true;
    	if (id_obj == null) return false;
    	try
    	{
    		Identifier id = (Identifier)id_obj;
    		return (str.equals(id.str) && type.equals(id.type));
    	} catch (ClassCastException e)
    	{
    		return false;
    	}
    }
    
    public int compareTo(Identifier id)
    {
    	return str.compareTo(id.str);
    }

    public int prefixLength()
    {
    	return type.prefixLength();
    }
    
    public String getPrefix() {
    	return str.substring(0, type.prefixLength());
    }
    
    /**
     * 
     * @param syntax
     * @return True iff the given syntax is LOWER_CASE, UPPER_CASE, CAMEL_CASE, or REVERSE_CAMMEL_CASE.
     */
    static public boolean isStrictNameSyntax(Syntax syntax)
    {
    	return (syntax.ordinal() < 4);
    }
    
    /**
     * Translate a name in the given syntax to a different syntax.
     * @param orig_name Must be pre-validated to have the given syntax.
     * @param orig_syntax Syntax of orig_name.  Must be a strict name syntax.
     * @param ret_syntax Syntax for the return string.  Any strict name syntax.
     * @return The new formatted name.
     */
    static public String mapSyntax(String orig_name, Syntax orig_syntax, Syntax ret_syntax)
    {
    	// Check params.
    	Main.assertion(isStrictNameSyntax(orig_syntax));
    	Main.assertion(isStrictNameSyntax(ret_syntax));
    	
    	// If the requested type is the original type, return the given name.
 		if (ret_syntax == orig_syntax)
 		{
 			return orig_name;
 		}
 		
    	String ret = "";
		char ch, lc_ch, uc_ch;
		boolean ret_underscore_delim = (ret_syntax == Syntax.LOWER_CASE) ||
				                       (ret_syntax == Syntax.UPPER_CASE);
		boolean ret_is_mainly_lc  = (ret_syntax  == Syntax.LOWER_CASE) ||
				                    (ret_syntax  == Syntax.CAMEL_CASE);
		boolean orig_is_mainly_lc = (orig_syntax == Syntax.LOWER_CASE) ||
				                    (orig_syntax == Syntax.CAMEL_CASE);
		boolean underscore = true;     // Flags that underscore was found.  (Iinitially true to force case delimitation).
		boolean is_lc, is_uc;          // Flags that ch is lower/upper case.
		boolean ret_lc;                // Character for ret should be lower case.
		for (int i = 0; i < orig_name.length(); i++)
		{
			ch = orig_name.charAt(i);
			if (ch == '_')
			{
				underscore = true;
			} else
			{
				// ch is alpha-numeric.
				lc_ch = Character.toLowerCase(ch);
				uc_ch = Character.toUpperCase(ch);
				is_lc = !(ch == uc_ch);
				is_uc = !(ch == lc_ch);
				if (underscore ||
					( orig_is_mainly_lc && is_uc) ||  // orig char is reverse-case
					(!orig_is_mainly_lc && is_lc)
				   )
				{
					// Delimitation required.
					if (ret_underscore_delim)
					{
						if (ret.length() > 0)  // No underscore to precede first token.
						{
							ret += "_";
						}
						ret_lc = ret_is_mainly_lc;
					} else
					{
						ret_lc = !ret_is_mainly_lc;
					}
					underscore = false;
				} else
				{
					// No delimitation.
					ret_lc = ret_is_mainly_lc;
				}
				ret += Character.toString(ret_lc ? lc_ch : uc_ch);
			}
		}
      	return ret;
    }

    /**
     * Get a name varient of an identifier in strict syntax in a different strict syntax.
     * @param ret_syntax
     * @return The name of this identifier in the specified syntax.  The return value is recorded for future use.
     */
    public String getNameVarient(Syntax ret_syntax)
    {
    	// Note that strictness of syntax is checked in mapSyntax(..).
    	
    	// Create varients[] if necessary.
    	if (varients == null)
    	{
    		varients = new String[4];
    	}
    	// Create the name in the requested syntax if necessary.
    	if(ret_syntax.ordinal() > 3)
    	{
    		ActiveParseContext.Report(0, Severity.WARNING, "IDENTIFIER_NAME", "Cannot convert name \"" + getName() + "\" to " + ret_syntax.toString());
    	}
    	else if (varients[ret_syntax.ordinal()] == null)
    	{
    		varients[ret_syntax.ordinal()] = mapSyntax(getName(), type.getSyntax(), ret_syntax);
    	}
    	// Return the name in the requested syntax.
    	return varients[ret_syntax.ordinal()];
    }
    
    
    /**
     * @param new_type
     * @return A new Identifier derived from this one with a different type.  Converts strict types to strict or MIXED types
     */
    public Identifier typeCast(IdentifierType new_type)
    {
    	String name = (new_type.getSyntax() == Syntax.MIXED_CASE)
    			         ? getName()
    			         : getNameVarient(new_type.getSyntax());
    	return (new Identifier(new_type.getPrefix() + name, new_type));
    }
    
    /**
     * @return The provided identifier, but if an assigned sig type, return the corresponding unassigned variant.
     */
    public Identifier unassignedVariant()
    {
    	return (!isAssignedSigType()) ?
    		   this :
    		   (type == IdentifierType.ASSIGNED_PIPE_SIG)      ? typeCast(IdentifierType.PIPE_SIG)    :
    		   (type == IdentifierType.ASSIGNED_STATE_SIG)     ? typeCast(IdentifierType.STATE_SIG)   :
    		   // ASSIGNED_SIG_KEYWORD (only $ANY).  Must not create another identical Identifier for keywords because these use == comparison.
    		   (this == Identifier.assigned_any_sig_keyword)   ? Identifier.any_sig_keyword :
    		   (type == IdentifierType.MALFORMED_ASSIGNED_SIG) ? typeCast(IdentifierType.MALFORMED_SIG) :  
    		   null;
    }
    
    
    public String getName()
    {
    	return str.substring(type.prefixLength());
    }

    /*
    // Returns the component tokens as a string vector.  (Keywords are returned as a single token.)
    public Vector<String> getTokens()
    {
    	if (tokens == null) {
    		// TODO: ... tokens = ...;
    	}
    	return tokens;
    }
    */

    public IdentifierType getType()
    {
        return type;
    }

    /*// Returns a differently-formatted version of this identifier's name (tokens).
    public String reformatted(int syntax)
    {
        // TODO: ...walk char by char and generate new string...
    }

    // TODO: Constructor without syntax checking.  Input is guaranteed to be legal by caller.
    public Identifier(String ident_str); // includes prefix char

    // Constructor with syntax checking.
    public Identifier(String ident_str,      // includes prefix char
                      int legal_types = -1); // bit mask of legal types
    */
    
    
    // Type groups:
    public boolean isSigType()
    {
    	return type.isSig();
    }
    
    public boolean isLegalSigType()
    {
    	return type.isLegalSig();
    }
    
    public boolean isStagedSigType()
    {
    	return type.isStagedSig();
    }
    
    public boolean isAssignedSigType()
    {
    	return type.isAssignedSig();
    }
    
    // TODO: Change these to use masks, as above.
    public boolean isWhenType()
    {
    	return type.isWhen();
    }
    
    public boolean isAttribute()
    {
    	return type.isAttribute();
    }
    
    public boolean isStage()
    {
    	return type.isStage();
    }
    
    public boolean isBehavioral()
    {
    	return type.isBehavioral();
    }
    
    public static boolean isKeyword(IdentifierType type)
    {
    	return type.isKeyword();
    }
    
    public static Identifier parseSig(ParseContext context)
    {
    	Identifier signal_ident = Identifier.parse(IdentifierType.sigOrdinalMask());
    	if (((signal_ident.getType().ordinalMask() & IdentifierType.legalSigOrdinalMask()) == 0) &&
    	    !context.getNoStateSigs())
    	{
    		context.report(0, Severity.RISKY_ERROR, "MALFORMED-SIG", "Malformed signal name " + signal_ident.toString() + ".  (If you are converting SV code, see ^no_state_sigs attribute.)");
    	}
   	    return signal_ident;
    }



    // A varient of String.charAt that is safe to overflow.  Acts as if the string has an added '\0' terminator.
    private static char safeCharAt(String str, int pos)
    {
    	return (pos == str.length()) ? '\0' : str.charAt(pos);
    }
    
    
    // Helper for init().
    static private Identifier addKeyword(String str)
    {
    	ActiveParseContext.Set(null, str, 0, -1);
    	Identifier new_keyword = Identifier.parse(IdentifierType.getKeywordsOrdinalMask());
    	keywords.put(str, new_keyword);
    	return new_keyword;
    }
    
    
    // Keywords, which can be looked up in keywords.  The identifier returned by parse(..) can be compared against these using ==.
    static public Identifier tlv_keyword;
    static public Identifier sv_keyword;
    static public Identifier always_comb_keyword;
    static public Identifier any_sig_keyword;
    static public Identifier assigned_any_sig_keyword;
    static public Identifier retain_sig_keyword;
    static public Identifier source_keyword;
    static public Identifier end_source_keyword;
    
    /**
     * Must be called prior to constructing any Identifier objects.
     */
    static public void init()
    {
    	initing = true;
    	
		IdentifierType.init();
    	
    	// Checking.
    	// Must be able to use all IdentifierTypes as a mask.
    	// If we hit the limit, we could put everything in 'keywords' outside of the maskable range.
    	Main.assertion((1 << IdentifierType.values().length) != 0 &&
    		            IdentifierType.values().length != 0    // Just to be sure length has been set.  Should be.  TODO: Can remove.
    		           );  // TODO: Does Java throw an error on overflow?  If so, this check isn't needed (but could be rewritten to catch this case more cleanly).
    	
    	keywords = new Hashtable <String, Identifier> ();
    	tlv_keyword         = addKeyword("\\TLV");
    	sv_keyword          = addKeyword("\\SV");
    	always_comb_keyword = addKeyword("\\always_comb");
    	any_sig_keyword     = addKeyword("$ANY");
    	assigned_any_sig_keyword = addKeyword("$$ANY");
    	retain_sig_keyword  = addKeyword("$RETAIN");
    	source_keyword      = addKeyword("\\source");
    	end_source_keyword  = addKeyword("\\end_source");
    	
    	init_done = true;
    	initing = false;
    }
    
    /**
     * Construct by parsing with legal types mask.  The constructor is proxied with this method so that we can avoid construction for identical Identifiers.
     * ParseContext must point to the beginning of the identifier.
     * @param legal_types Bit mask of legal types (Eg: IdentifierType.PIPELINE.getType() || IdentifierType.BEH_HIER.getType())
     */
    public static Identifier parse(int legal_types)
    {
    	// TODO: For now, we just do construction (which would otherwise, just be a constructor).
    	// We may want to keep a Hashtable of all Identifiers, and look up the one being "constructed".  If we find a match, just return it.
    	// One thing to be careful of is that the Hashtable will prevent deletion of Identifiers.  This is fine for pre-processing usage, but
    	// would be an issue for interactive development.  A possible resolution is to periodically (on construction) remove a random (round-robin)
    	// Identifier from the Hashtable and mark it 'abondoned'.  Get methods would re-enter 'abandoned' Identifiers in the Hashtable (as
    	// long as there is no identical entry).
    	// It may be easy enough to reuse Identifiers outside of the constructor, that we don't need this.  Let's wait and see.
    	
    	// Checking.
		Main.assertion(initing ^ init_done);  // Either we're in init(), or init() is done, not both.
		
    	// Parse prefix.
		ParseContext context = ActiveParseContext.get();
		String line = context.getString();
		int start_pos = context.getPosition();
    	int pos = start_pos;
    	int line_len = line.length();
    	final char prefix_char = line.charAt(pos);
    	char ch = prefix_char;
    	{
	    	char prev_ch = '\0';
	    	while (ch != '\0' && !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch) && (ch != '_') && (ch != '('))  // TODO: Check specifically for prefix chars instead for better error reporting.
	    	{
	    		pos++;
	    		prev_ch = ch;
	    		ch = safeCharAt(line, pos);
	    	}
	    	// +/- are special.  If they are followed by a digit, they are part of a numeric identifier string, not the prefix.  (Prefixes that end with +/- may not be numeric.
	    	if (prev_ch == '+' || prev_ch == '-')
	    	{
	    		if (Character.isDigit(ch))
	    		{
	    			// Roll-back over +/-.
	    			ch = prev_ch;
	    			pos--;
	    		}
	    	}
    	}
    	
    	int prefix_end_pos = pos;
    	int prefix_length = prefix_end_pos - start_pos;
    	Syntax syntax = Syntax.UNDEFINED;
    	char first_name_char  = (line_len > prefix_end_pos) ? line.charAt(prefix_end_pos) : '\0';
    	String prefix_str = (prefix_length != 1) ? line.substring(start_pos, prefix_end_pos) : null;  // As required for various IdentifierType methods.
		boolean malformed = false;
    	if (IdentifierType.mixedCase(prefix_char, prefix_str))
    	{
    		// This prefix is one of mixed case.
    		syntax = Syntax.MIXED_CASE;
    		
    		// Walk the name.
	    	ch = safeCharAt(line, pos);
	    	while ((ch != '\0') && (Character.isLetterOrDigit(ch) || ch == '_')) {   // Note: Unlike the similar loop below, caseless letters are permitted here, though I'm' not sure there is such a thing in ASCII.
	    		pos++;
	    		ch = safeCharAt(line, pos);
	    	}
    	} else
    	// TODO: Add support for operator.  Operators could match prefix chars of Identifiers of different types, so need to look after prefix chars to distinguish.
    	if (first_name_char == '(' ||
    	    Character.isDigit(first_name_char) ||
    	    first_name_char == '+' ||
    	    first_name_char == '-')
    	{
    		// TODO: Add support for expressions.
    		syntax = Syntax.EXPRESSION;
    	} else
    	{
    		// Case of first two chars will determine type.
	    	boolean first_letter  = Character.isLowerCase(first_name_char) || Character.isUpperCase(first_name_char);  // Note: Only cased letters are letters here.
	    	char second_name_char = (line_len > prefix_end_pos + 1) ? line.charAt(prefix_end_pos + 1) : '\0';
	    	boolean second_letter = Character.isLowerCase(second_name_char) || Character.isUpperCase(second_name_char);

	    	if (!(first_letter && second_letter))
	    	{
	    		malformed = true;
	    	}
    		// First two characters are letters as they should be, and this is used to determine syntax.
    		boolean first_upper   = Character.isUpperCase(first_name_char);
    		boolean second_upper  = Character.isUpperCase(second_name_char);
    		syntax = first_upper
    				 ? second_upper
    				   ? Syntax.UPPER_CASE
    			       : Syntax.CAMEL_CASE
    			     : second_upper
    			       ? Syntax.REVERSE_CAMEL_CASE
    			       : Syntax.LOWER_CASE;

    		// Walk the name and ensure that it is not malformed.
    		boolean upper_syntax = (syntax == Syntax.UPPER_CASE) ||
    				               (syntax == Syntax.REVERSE_CAMEL_CASE);
    		boolean underscore_delim = (syntax == Syntax.UPPER_CASE) ||
    				                   (syntax == Syntax.LOWER_CASE);
    		boolean prev_underscore = false;
    		boolean prev_digit = false;
	    	ch = safeCharAt(line, pos);
	    	while ((ch != '\0') && (Character.isLowerCase(ch) || Character.isUpperCase(ch) || Character.isDigit(ch) || ch == '_')) {
	    		// Note: Caseless letters will end this loop.
	    		if (Character.isLetter(ch))
	    		{
		    		if (Character.isLowerCase(ch) ^ upper_syntax)
		    		{
		    			// Char's case is the main case of this syntax.
		    			if (prev_digit)
		    			{
		    				malformed = true;  // Missing delimitation after digit.
		    			}
		    			prev_underscore = false;
		    			prev_digit = false;
		    		} else
		    		{
		    			// Char's case is not the main case of this syntax.
		    			if (underscore_delim)
		    			{
		    				malformed = true;  // All letters must be main case.
		    			}
		    			prev_underscore = false;
		    			prev_digit = false;
		    		}
	    		} else
	    		if (ch == '_')
	    		{
	    			if (prev_underscore ||
	    				!underscore_delim)
	    			{
	    				malformed = true;
	    			}
	    			prev_underscore = true;
	    			prev_digit = false;
	    		}
	    		if (Character.isDigit(ch))
	    		{
	    			if (prev_underscore)
	    			{
	    				malformed = true;
	    			}
	    			prev_underscore = false;
	    			prev_digit = true;
	    		}
	    	
	    		// Next.
	    		pos++;
	    		ch = safeCharAt(line, pos);
	    	}
    	}
		// int name_len = pos - prefix_end_pos;
		
		String str = line.substring(start_pos, pos);
		if (malformed)
    	{
			if (!IdentifierType.mixedOk(prefix_char, prefix_str))
			{
				ActiveParseContext.Report(0, Severity.FATAL_ERROR, "PARSE-IDENT", "Malformed identifier \"" + str + "\"");
			}
			syntax = Syntax.MIXED_CASE;
    	}
		
		IdentifierType type = IdentifierType.formatToType(prefix_char, prefix_str, syntax);

		// Adjust state sigs to MALFORMED_SIG if no_state_sigs.
		if (context.getNoStateSigs())
		{
	    	if ((type == IdentifierType.STATE_SIG))
	    	{
	    		type = IdentifierType.formatToType(prefix_char, prefix_str, Syntax.MIXED_CASE);
	    	}
	    	if ((type == IdentifierType.ASSIGNED_STATE_SIG))
	    	{
	    		type = IdentifierType.formatToType(prefix_char, prefix_str, Syntax.MIXED_CASE);
	    	}
		}
		
		// Check legal_types.
		if ((type.ordinalMask() & legal_types) == 0)
		{
			ActiveParseContext.Report(0, Severity.FATAL_ERROR, "PARSE-IDENT", "Found identifier of type " + type + " which is not legal in this context.\n\tExpected types: " + IdentifierType.maskedNames(legal_types));
		}
    	
		
		// Create or find the identifier, and maintain 'keywords'.

		Identifier ret = null;  // Return value.
		
    	// We do have a special Hashtable for pre-defined Identifiers.
		if (init_done)
		{
			// If this is a keyword, look it up.
			if (isKeyword(type))
			{
				ret = keywords.get(str);
				if (ret == null)
				{
					ActiveParseContext.Report(
							0,
							Severity.FATAL_ERROR, "PARSE-IDENT",
							"Identifier beginning " + str + " recognized as keyword/attribute/operand type " + type.name() + ", but this is not a recognized one.");
				}
			}
		}
		
		// If identifier was not found, create one.
		if (ret == null)
		{
			ret = new Identifier(str, type);
		}
		
		if (!init_done)
		{
			// Add to Hashtable.
			keywords.put(str, ret);
		}
		
		// Increment ParseContext past this identifier.
		ActiveParseContext.IncrementPosition(ret.str.length());
		
		return ret;
    }
    
    // A constructor, given a new name, so that it's clear this is an expert-user method.
    public static Identifier rawConstructor(String str_, IdentifierType type_)
    {
    	return new Identifier(str_, type_);
    }
    
    
    private Identifier(String str_, IdentifierType type_)
    {
    	str = str_;
    	type = type_;
    }
    
}

