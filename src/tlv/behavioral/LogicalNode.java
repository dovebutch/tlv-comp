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

import java.util.Vector;

import tlv.parse.NodeType;
import tlv.parse.SourceFile;
import tlv.parse.identifier.*;

/**
 * LogicalNode class is used for creating a LogicalBranch/LogicalNode structure.
 * Every LogicalNode has a parent logicalBranch linked with it and NodeType identifying it.
 * When not extended into a LogicalBranch, LogicalNode does not posses any children LogicalNodes.
 * 
 * @author ypyatnyc
 *
 */
public class LogicalNode
{	
	/**
	 * Provides additional scope for the LogicalNode object
	 */
	protected LogicalBranch parent;
	protected NodeType type;
	
	public LogicalNode(LogicalBranch parent_)
	{
		this(parent_, NodeType.UNDEFINED);
	}
	
	public LogicalNode(LogicalBranch parent_, NodeType t)
	{
		parent = parent_;
		type = t;
	}
	
	public NodeType getNodeType()
	{
		return type;
	}
	
	/**
	 * Returns the LogicalNode's parent LogicalBranch
	 * 
	 * @return parent LogicalBranch
	 */
	public LogicalBranch getParent()
	{
		return parent;
	}
	

	/**
	 * Looks up the LogicalNode's parent branch in attempt to find a LogicalBranch
	 * of a specified type.
	 * 
	 * @param type the type of LogicalBranch the method attempts to retrieve
	 * @return the LogicalBranch of NodeType type which exists in this LogicalNode's hierarchy. 
	 */
	public LogicalBranch getAncestorOfType(NodeType type)
	{
		return (getParent() == null) ? null : getParent().getSelfOrAncestorOfType(type);
	}
	
	public LogicalBehScope getAncestorBehScope()
	{
		return (getParent() == null) ? null : getParent().getSelfOrAncestorBehScope();
	}
	
	public LogicalBehScope getTopScope()
	{
		return parent.getTopScope();
	}
}
