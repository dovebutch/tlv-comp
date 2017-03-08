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

import tlv.behavioral.range.*;
import tlv.parse.*;
import tlv.parse.identifier.*;

import java.util.*;

/**
 * Currently without LogicalHier implemented, LogicalPipeline serves as the top LogicalScope
 * for PipeSignals. Because of this LogicalPipeline contains the vector of all PipeSignals which are
 * be scoped below it. LogicalPipeline also contains any LogicalWhens and LogicalStages nested directly
 * below it.
 * 
 * @author ypyatnyc
 *
 */
public class LogicalPipeline extends LogicalBehScope
{
	
	/**
	 * Creates a LogicalPipeline from a ParseMnemonic identifier that contains the Pipeline's label,
	 * and a reference back to its associated ParseNode
	 * 
	 * @param identifier_ ParseMnemonic containing the name of the pipeline as well as a reference to
	 * its associated ParsePipeline
	 */
	public LogicalPipeline(ParseMnemonic identifier_)
	{
		super(identifier_);
	}
	
	public LogicalPipeline(ParseMnemonic identifier_, ParsePipeline parse_pipe_, LogicalBranch parent_)
	{
		super(identifier_, parse_pipe_, parent_);
	}
	
	public LogicalPipeline(ParseMnemonic identifier_, Range range_, LogicalBranch parent_)
	{
		super(identifier_, range_, parent_);
	}
	
	public LogicalPipeline(Identifier ident_, LogicalBranch logical_branch_, NodeType t) {
		super(ident_, logical_branch_, t);
	}
}
