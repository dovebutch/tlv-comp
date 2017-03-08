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

import tlv.behavioral.range.Range;
import tlv.parse.NodeType;
import tlv.parse.ParseBehHier;
import tlv.parse.identifier.Identifier;
import tlv.parse.identifier.ParseElement;

public class LogicalBehHier  extends LogicalBehScope
{
	/**
	 * Creates a LogicalBehHier from a ParseElement identifier object.
	 * 
	 * @param identifier_ ParseElement derived from the newely constructed LogicalBranch's associated
	 * ParseNode.
	 */
	public LogicalBehHier(ParseElement identifier_)
	{
		super(identifier_);
	}
	
	public LogicalBehHier(ParseElement identifier_, ParseBehHier parse_hier_, LogicalBranch parent_)
	{
		super(identifier_, parse_hier_, parent_);
	}
	
	public LogicalBehHier(ParseElement identifier_, Range range_, LogicalBranch parent_)
	{
		super(identifier_, range_, parent_);
	}
	
	/**
	 * Creates an "dummy" LogicalBehHier of specific NodeType and links it with a top LogicalBranch
	 *
	 * @param logical_branch_ parent LogicalBranch object
	 * @param t NodeType of the created LogicalBranch
	 */
	/*
	public LogicalBehHier(LogicalBranch logical_branch_, NodeType t)
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
	public LogicalBehHier(LogicalBranch logical_branch_)
	{
		super(logical_branch_);
	}
	*/
	
	public LogicalBehHier(Identifier ident_, LogicalBehScope logicalBehScope, NodeType t) {
		super(ident_, logicalBehScope, t);
	}

}
