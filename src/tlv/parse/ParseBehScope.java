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

import tlv.behavioral.range.Range;

/**
 * A common base class for Parse* classes which represent behavioral hierarchy.  For example, a signal reference such as
 * >core[2]|instr$valid includes a path consisting of the behavioral hierarchy of the signal.  Behavioral hierarchy includes
 * ParsePipeline and ParseBehHier.
 * 
 * @author sfhoover
 *
 */
public abstract class ParseBehScope extends ParseBranch
{
	// Range support.
	// Pipelines do not have ranges, as a language choice, but we fundamentally support the ability for them to have ranges, in
	// case we change our mind.
	private Range range;
	private int range_pos;  // Position in parse string of range (for ParseContext).

	public ParseBehScope(ParseNode source_node)
	{
		super(source_node);
	}
	
	/**
	 * Constructs by parsing; ParseContext must point to identifier.
	 * @param parent_branch
	 * @param source_node
	 */
	public ParseBehScope(ParseBranch parent_branch, ParseNode source_node)
	{
		super(parent_branch, source_node);
		
		// Parse range.
		range = Range.parse(null, source_node, 0x7, true);
		range_pos = (range == null) ? getString().length() : ActiveParseContext.GetPosition();
	}
	
	/* TODO: TEMPORARY. Phase this out.
	public ParseBehScope(ParseBranch parent_branch, ParseNode source_node, boolean dummy)
	{
		super(parent_branch, source_node, dummy);
		
		// Parse range.
		range = Range.parse(null, source_node, 0x7, true);
		range_pos = (range == null) ? getString().length() : ActiveParseContext.GetPosition();
	}
	*/

	// Set ParseContext to that of range.
	public void rangeContext()
	{
		ActiveParseContext.Set(this, 0, -1);
	}
	
	public void setRange(Range range_, int pos_)
	{
		range = range_;
		range_pos = pos_;
	}
	public Range getRange()
	{
		return range;
	}
}