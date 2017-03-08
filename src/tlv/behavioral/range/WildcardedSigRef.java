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

package tlv.behavioral.range;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import tlv.behavioral.*;
import tlv.parse.ActiveParseContext;
import tlv.utilities.Severity;
import tlv.parse.identifier.*;

/**
 * A BitRange for $ANY references.
 * Extends UsedBitRange (though its used for assignments as well) and adds the set of signals involved in the $ANY.
 * Never has a range (always null, null).
 * 
 * @author sfhoover
 */
public class WildcardedSigRef extends UsedBitRange
{
	// The begin end char position of this "$ANY" in the Assignment.string_without_indentation.
	// TODO: These should be reworked.  All SV code generation should be done subsequent to parsing.
	public int sv_begin_str_pos = -1;
	public int sv_end_str_pos = -1;
	
	// The bit ranges represented by this class.  These are determined as the logic network is analyzed, and
	// signals are pulled through the containing assignment statement.  Currently there will be at most one BitRange per signal,
	// and its range may grow as the network is analyzed, but there can be no holes in a signal's range.
	private TreeMap<Identifier, BitRange> ranges = new TreeMap<Identifier, BitRange>();  // Keys are scoped signal references (no range or alignment).
	
	public Map<Identifier, BitRange> getSigRangeMap()
	{
		return ranges;
	}
	
	Iterator<BitRange> getSigRangeIterator()
	{
		return ranges.values().iterator();
	}
	
	// For uses.
	public WildcardedSigRef(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier, Expression alignment_)
	{
		super(sig_scope, identifier_, identifier, alignment_, null, null);
	}
	
	// For assignments.
	public WildcardedSigRef(LogicalBehScope sig_scope, ParseMnemonic identifier_, Identifier identifier)
	{
		super(sig_scope, identifier_, identifier);

		// TODO: Enable this for TLV 1c.
		ActiveParseContext.Report(0, Severity.FATAL_ERROR, "ANY", "$ANY is not supported in TL-Verilog 1a");
	}
}
