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

import tlv.parse.identifier.ParseMnemonic;

/**
 * Behavioral hierarchy parse node.
 * 
 * @author sfhoover
 *
 */
public class ParseBehHier extends ParseBehScope
{
	/**
	 * This method is used to produce the ParseElement identifier for the ParseBehHier. It does not create a LogicalBehHier.
     * TODO: This should be done more generically in base classes.
	 * 
	 * @return The ParseMnemonic ParseElement identifier which gets extracted from the ParseBehHier's string
	 */
	public ParseMnemonic produceMnemonic()
	{
		if (identifier == null)
		{
			for(int i = 0; i < string.length(); i++)
			{
				if(string.charAt(i) == Prefix.BEH_HIER.getChar())
				{
					old_identifier = new ParseMnemonic(this, Prefix.BEH_HIER, i+1);
					break;
				}
			}
		}
		
		return (ParseMnemonic)old_identifier;
	}
	

	public ParseBehHier(ParseNode source_node)
	{
		super(source_node);
		setType(NodeType.BEH_HIER);
	}
	
	// TODO: I'm not sure how this would be used (including superclasses).  (Hoover)
	public ParseBehHier(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		setType(NodeType.BEH_HIER);
	}
}
