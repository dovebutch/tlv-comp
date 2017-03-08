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

import tlv.behavioral.*;
import tlv.parse.*;
import tlv.parse.identifier.*;

public class TransitionRange extends BitRange
{
	//unanchored bit ranges are mere placeholders in transition stages between assignments/uses
	protected boolean anchored;
	
	//describes the transition type going into this bit range. (FLOP1, FLOP2 or LATCH)
	protected String tType;
	
    //Creates a BitRange with starting index of bStartIndex, and ending index of bEndIndex.
    public TransitionRange(PipeSignal signal_,  Expression end_bit_index_, Expression start_bit_index_, boolean anchor, String t)
    {
    	super(null, signal_, end_bit_index_, start_bit_index_);
    	anchored = anchor;
    	tType = t;
    }
    
    //Creates a BitRange for a single bit at index bIndex
    public TransitionRange(PipeSignal signal_, Expression bit_index_, boolean anchor, String t)
    {
    	this(signal_, bit_index_, bit_index_, anchor, t);
    }
    
    //Creates a bit range for a single bit PipeSignal
    public TransitionRange(PipeSignal signal_, boolean anchor, String t)
    {
    	this(signal_, null, anchor, t);
    }
    
    public String getType()
    {
    	return tType;
    }
    
    public boolean isAnchored()
    {
    	return anchored;
    }
}
