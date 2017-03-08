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

import tlv.parse.NodeType;
import tlv.parse.ParseContext;
import tlv.parse.ParseNode;
import tlv.parse.Prefix;
import tlv.utilities.Severity;

// TODO: Rewrite.

/**
 * TODO: Update parsing to use ActiveParseContext.
 * Expression extends ParseElement and is used for identifying ParseElements which
 * contain a constant value
 * 
 * @author ypyatnyc
 *
 */
public class Expression extends ParseElement
{
	private int parenOffset;
	private int value;
	
	private boolean flagEvaluated;
	
	/**
	 * Constructs a dummy Expression which is currently used by AssignedBitRange to identify unity bit ranges
	 * The constructor will have no way of pointing to where in its ParseNode the Expression exists
	 *
	 * @param parseNode_ is the ParseNode associated with the ParseElement
	 */
	public Expression(ParseNode parseNode_)
	{
		super(parseNode_);
	
		value = 0;
		parenOffset = 0;
			
		flagEvaluated = true;	
	}
	
	/**
	 * Constructor for an expression that is just a value with no parse context.
	 * @param _value
	 */
	public Expression(int _value)
	{
		super(null);
		value = _value;
		parenOffset = 0;
		flagEvaluated = true;
	}
	
	
	/**
	 * Constructs an Expression object from a ParseNode, a Prefix and starting index value in the ParseNode's string.
	 * This method captures the label from the ParseNode and then calls on the produceValue() method to
	 * evaluate the captured string
	 * TODO: This class needs a re-think, and this method is a mess w/ hacks on top.
	 *
	 * @param parseNode_		The ParseNode from which the Expression is extracted
	 * @param prefix_			The Prefix identifying the type of Expression that will be extracted
	 * @param stringStartIndex_	The string index at which to start extracting the Expression

	 */
	public Expression(ParseNode parseNode_, Prefix prefix_, int stringStartIndex_)
	{
		super(parseNode_, prefix_, stringStartIndex_);
		
		flagEvaluated = false;
		boolean foundDigit = false;
		boolean foundNonDigit = false;
		parenOffset = 0;
		
		int i = stringStartIndex_;
		String string = parseNode_.getString();
		int anchor_pos = i;  // Location from which to copy string to label.
		label = "";
		char ch = string.charAt(i);
		
		if(ch == '-' || ch == '+')
			i++;
		
		for(; i < string.length(); i++)
		{
		    ch = string.charAt(i);
		    
		    // Swallow escape chars from ParseNode string.
		    if ((ch == '\\') && ((prefix_ == Prefix.END_BIT) || (prefix_ == Prefix.START_BIT)) && (i + 1 < string.length()))
		    {
		    	label += string.substring(anchor_pos, i);
		    	i++;
		    	anchor_pos = i;
		    	// Leave ch = '\\', which will treat the escaped char as nothing special, regardless of what it is.
		    }
		    boolean isDigit = ch >= '0' && ch <= '9';
			if (prefix_ == Prefix.ALIGNMENT)
			{
				if (!isDigit)
			        break;
			} else if (prefix_ == Prefix.STAGE)
			{
				if (!isDigit && !Character.isAlphabetic(ch))
					break;
			}
			if(ch == '(' ||
			   ch == '[')
				parenOffset++;
			if(parenOffset > 0)
			{
				if(ch == ')' ||
				   ch == ']')
					parenOffset--;
			}
			else if (prefix_ == Prefix.END_BIT ||
					 prefix_ == Prefix.START_BIT)
			{
			    if (ch == ':' ||
					ch == ']')
			        break;
			}
			
			if (isDigit)
			{
				foundDigit = true;
			} else {
				foundNonDigit = true;
			}
		}
		
		stringEndingIndex = i;
		
		flagNumeric = foundDigit && !foundNonDigit;
		
		label += string.substring(anchor_pos, stringEndingIndex);
		
		produceValue();
	}
	
	/**
	 * Returns the value of the Expression
	 * 
	 * @return integer value
	 */
	public int getValue()
	{
		return value;
	}
	
	/**
	 * Returns a boolean value indicating whether the expression was correctly evaluated or not
	 * @return boolean value
	 */
	public boolean isEvaluated()
	{
		return flagEvaluated;
	}
	
	/**
	 * This method attempts to evaluate the Expression's label string into an integer value.
	 * When evaluating staging Expression, the produced value is multiplied by 2 to increase 
	 * the granularity to half clocks. 
	 * 
	 * @return boolean indicating whether or not the expression was successfuly evaluated
	 */
	public boolean produceValue()
	{	
		int sign = 0;
		
		if(flagNumeric)
		{
			if(label.charAt(0) == '-')
				value = -1 * Integer.parseInt(label.substring(1));
			else if(label.charAt(0) == '+')
				value = Integer.parseInt(label.substring(1));
			else
				value = Integer.parseInt(label);
			
			flagEvaluated = true;
			
			if(prefix == Prefix.STAGE || prefix == Prefix.ALIGNMENT)
			{
				value = value * 2;
			}
			
			return true;
		}
		else if(prefix == Prefix.STAGE)
		{
			for(int i = 0; i < label.length() - 1; i++)
			{
				if(i == 0 && label.charAt(i) == '-')
				{
					sign = -1;
				}
				else if(i == 0 && label.charAt(i) == '+')
				{
					sign = 1;
				}
				else if(label.charAt(i) >= '0' && label.charAt(i) <= '9')
				{
					
				}
				else
				{
					flagEvaluated = false;
					return false;
				}
			}
			
			
			if(label.charAt(label.length() - 1) == 'H' || label.charAt(label.length() - 1) == 'h')
			{
				if(sign == 0)
				{
					value = 2*Integer.parseInt(label.substring(0, label.length() - 1));
				}
				else
				{
					value = 2*sign*Integer.parseInt(label.substring(1, label.length() - 1));
				}
				
				flagEvaluated = true;
				return true;
			}
			else if(label.charAt(label.length() - 1) == 'L' || label.charAt(label.length() - 1) == 'l')
			{
				if(sign == 0)
				{
					value = 2*Integer.parseInt(label.substring(0, label.length() - 1)) + 1;
				}
				else
				{
					value = 2*sign*Integer.parseInt(label.substring(1, label.length() - 1)) + 1;
				}
				
				flagEvaluated = true;
				return true;
			}
			else
			{
				flagEvaluated = false;
				
				return false;
			}
		}
		else
		{
			flagEvaluated = false;
			
			return false;
		}
	}
	
	//null expression is greater then non-null expression
	//unevaluated expression is greater then evaluated expression
	static public int isGreater(Expression this_, Expression that_)
	{
		if(this_ != null && that_ != null)
		{
			if(this_.isEvaluated() && that_.isEvaluated())
			{
				return this_.getValue() - that_.getValue();
			}
			else if(this_.isEvaluated())
			{
				return -2;
			}
			else if(that_.isEvaluated())
			{
				return 2;
			}
			else
			{
				return 0;
			}
		}
		else
			return 0;
	}
	
	static public int isLesser(Expression this_, Expression that_)
	{
		if(this_ != null && that_ != null)
		{
			if(this_.isEvaluated() && that_.isEvaluated())
			{
				return that_.getValue() - this_.getValue();
			}
			else if(this_.isEvaluated())
			{
				return -2;
			}
			else if(that_.isEvaluated())
			{
				return 2;
			}
			else
			{
				return 0;
			}
		}
		else
			return 0;
	}
	
	/**
	 * Evaluates whether this Expression is greater than that Expression
	 * If both Expression are evaluated, this method returns the difference between this and that,
	 * which would produce a positive value if this is greater then that.
	 * 
	 * If one expression if not evaluated, and the other expression is then the unevaluated expression is
	 * regarded to be greater.
	 * 
	 * @param that the Expression that this expression is compared against
	 * 
	 * @return integer value indicating which Expression is greater and by how much
	 */
	public int isGreaterThan(Expression that)
	{	
		if(!this.flagEvaluated && !that.flagEvaluated)
		{
			if(this.label.compareTo(that.getLabel()) != 0)
				System.out.println("Error in Identifier class. Method compareTo cannot process to unevaluated identifiers");
			
			return 0;
		}
		else if(!this.flagEvaluated)
			return 2;
		else if(!that.flagEvaluated)
			return -2;
		else if(this.value == that.value)
			return 0;
		else
			return this.value - that.value;
	}
	
	/**
	 * Evaluates whether this Expression is less than that Expression
	 * If both Expression are evaluated, this method returns the difference between that and this,
	 * which would produce a positive value if this is less than that.
	 * 
	 * If one expression if not evaluated, and the other expression is, than the unevaluated expression is
	 * regarded to be of less value.  TODO: There is no good assumption here.  Code should use assigmnent Expression as outer bounds.
	 * 
	 * @param that_ the Expression that this expression is compared against
	 * 
	 * @return integer value indicating which Expression is greater and by how much
	 */
	public int isLessThan(Expression that)
	{	
		if(!this.flagEvaluated && !that.flagEvaluated)
		{
			if(!this.label.equals(that.getLabel()))
				System.out.println("Error in Identifier class. Method compareTo cannot process to unevaluated identifiers");
			return 0;
		}
		else if(!this.flagEvaluated)
			return 2;
		else if(!that.flagEvaluated)
			return -2;
		else if(this.value == that.value)
			return 0;
		else
			return that.value - this.value;
	}
	
	/**
	 * @param this_
	 * @param that
	 * @return The lesser expression, this or that, returning null if either is null, otherwise returning an unevaluated one if possible, with priority to this.
     *         This reflects the prioritization necessary for aggregating used bit ranges.
	 */
	static public Expression lesser(Expression this_, Expression that)
	{
		if (this_ == null || that == null)
			return null;
		else if (!this_.flagEvaluated && !that.flagEvaluated)
			return this_;
		else if (!this_.flagEvaluated)
			return this_;
		else if (!this_.flagEvaluated)
			return that;
		else if (this_.value < that.value)
			return this_;
		else
			return that;
	}
	
	/**
	 * @param this_ An expression.  This one gets priority, so it should be the assigned expression for range aggregation.
	 * @param that  An expression.  This one does not get priority, so it should be the used expression for range aggregation.
	 * @return The greater expression, this or that, returning null if either is null, otherwise returning an unevaluated one if possible, with priority to this.
     *         This reflects the prioritization necessary for aggregating used bit ranges.
	 */
	static public Expression greater(Expression this_, Expression that)
	{
		if (this_ == null || that == null)
			return null;
		else if (!this_.flagEvaluated && !that.flagEvaluated)
			return this_;
		else if (!this_.flagEvaluated)
			return this_;
		else if (!that.flagEvaluated)
			return that;
		else if (this_.value > that.value)
			return this_;
		else
			return that;
	}
	
	/**
	 * @param this_
	 * @param that
	 * @return true if the given Expressions are known equivalent (same value or label if unevaluated, or both null).
	 */
	static public boolean equals(Expression this_, Expression that)
	{
		if (this_ == null || that == null)
			return (this_ == that);
		else if (this_.isEvaluated())
		{
			return that.isEvaluated() && (this_.getValue() == that.getValue());
		} else
		{
			return !that.isEvaluated() && (this_.getLabel().equals(that.getLabel()));
		}
	}
}
