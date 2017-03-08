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

import tlv.parse.*;

/**
 * ParseMnemonic extends ParseElement and is primarily used for identifying ParseElements
 * representing String labels rather then values.
 * 
 * @author ypyatnyc
 *
 */
public class ParseMnemonic extends ParseElement
{
	
	/**
	 * Constructs a ParseMnemonic from a ParseNode, a Prefix and starting index value in the ParseNode's string.
	 * 
	 * @param parseNode_		The ParseNode from which the ParseMnemonic is extracted
	 * @param prefix_			The Prefix identifying the type of ParseMnemonic that will be extracted
	 * @param stringStartIndex_	The string index at which to start extracting the ParseMnemonic
	 */
	public ParseMnemonic(ParseNode parseNode_, Prefix prefix_, int stringStartIndex_)
	{
		super(parseNode_, prefix_, stringStartIndex_);
		
		stringStartingIndex = stringStartIndex_;
		
		String temp_string = parseNode_.getString();
		int i = stringStartIndex_;
		
		for(; i < temp_string.length(); i++)
		{
			if(temp_string.charAt(i) >= '0' && temp_string.charAt(i) <= '9')
				flagNumeric = true;
			else
			{
				if(!((temp_string.charAt(i) >= 'a' &&
				      temp_string.charAt(i) <= 'z')   ||
				     (temp_string.charAt(i) >= 'A' &&
				      temp_string.charAt(i) <= 'Z')   ||
			         (temp_string.charAt(i) == '_')
				    ))
				    // Non-alpha-numeric
				    break;
			}
				
		}
		
		stringEndingIndex = i;
		
		label = temp_string.substring(stringStartingIndex, stringEndingIndex);
	}

}
