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

/**
 * DEPRICATED (by IdentifierType)
 * Prefix class is used to identify ParseElement based on prefix character.
 * 
 * @author ypyatnyc
 *
 * @see ParseElement
 */
public enum Prefix {
	NONE				(' ', NodeType.UNDEFINED),
	OTHER 				('\\',NodeType.UNDEFINED),
	BEH_HIER 			('>', NodeType.BEH_HIER),
	PIPELINE 			('|', NodeType.PIPELINE),
	STAGE 				('@', NodeType.STAGE), 
	WHEN 				('?', NodeType.WHEN),
	STAGED_SIGNAL 		('$', NodeType.ASSIGNMENT), 
	UNSTAGED_SIGNAL 	('*', NodeType.ASSIGNMENT),
	MACRO 				('`', NodeType.ASSIGNMENT),
	ALIGNMENT 			('#', NodeType.ASSIGNMENT),
	END_BIT 			('[', NodeType.ASSIGNMENT),
	START_BIT 			(':', NodeType.ASSIGNMENT);
	
	
	private final char prefixChar;
	private final NodeType nodeType;
	
	Prefix(char pC, NodeType type)
	{
		prefixChar = pC;
		nodeType = type;
	}
	/**
	 * Returns the prefix char associated with a specified enum
	 * 
	 * @return prefix char
	 */
	public char getChar()
	{
		return prefixChar;
	}
	
	/**
	 * Returns the NodeType class associated with a specified enum
	 * 
	 * @return identifying NodeType
	 */
	public NodeType getType()
	{
		return nodeType;
	}
}
