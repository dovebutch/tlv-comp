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

package tlv.config;
import tlv.behavioral.*;
import tlv.parse.*;
import tlv.parse.identifier.Identifier.Syntax;
import tlv.utilities.Severity;
import tlv.Main;

/**
 * TODO: This should be overridden to define project-specific behavior.
 * TODO: This *should* be an abstract base class, but, out of laziness, this is specific to Intel
 *       methodology.  It *should* be separated in two: a base class and an Intel class.
 * TODO: Should make this generic to any language, not SV-specific.
 * TODO: It would be better to dynamically load the project-specific class so that it does not have to be part of the release .jar.
 * 
 * @author sfhoover
 *
 */
public class ProjSpecific
{
	public String getSigSvType()
	{
		return "logic";
	}
	public String getClkSigSvType()
	{
	        return getSigSvType();
	}

	public String getSvXInjectionDefine()
	{
		return "VCSSIM";
	}
	

	// TODO: These are project-specific, and should be abstract in the base class.
	
	/**
	 * Lazy evaluation of LogicalBehScope.sv_scope_str, which is a project-specific scope string used in SV signal names.
	 * @param scope
	 * @return
	 */
	public String getSvScopeStr(LogicalBehScope scope)
	{
		// Note, scope.sv_scope_str must be nullified if parent scope changes.  Not supported yet.
		if (scope.sv_scope_str == null)
		{
			// Generate and return scope.sv_scope_str.
			if (scope.isTop())
			{
				// Top-level scope.  Empty string.
				scope.sv_scope_str = "";
			}
			else
			{
				scope.sv_scope_str = getSvScopeStr(scope.getParentScope()) + "_" +
			                         scope.getIdentifier().getNameVarient((scope.getNodeType() == NodeType.PIPELINE) ? Syntax.UPPER_CASE : Syntax.CAMEL_CASE);
			}
		}
		return scope.sv_scope_str;
	}
	
	/**
	 * Return the SV signal name corresponding to the given PipeSignal at the given stage.
	 * @param signal_
	 * @param stage_number_
	 * @return
	 */
	public String svSignalName(PipeSignal signal_, int stage_number_)
	{
		return signal_.getLabel() + (Main.command_line_options.conversionMode()
		                               ? ""
		                               : getSvScopeStr(signal_.getBehScope()) + "_"
		                            ) + signalSuffix(signal_.getPipeline(), stage_number_);
	}
	
	public String svUngatedClockName(boolean even_stage)
	{
		return ("Clk_" + (even_stage ? "H" : "L"));
	}
	
	// TODO: Change "stage" to "phase" all over the place.  (Or, better, separate out phase from cycle (stage) and make code generic to any number of phases per cycle.
	public String edgeSuffix(int stage)
	{
		return Main.isOdd(stage) ? "L" : "H";
	}
	
	public String stageSuffix(int stage /*cyc*/)
	{
		ActiveParseContext.ReportIf((stage > 79) || (stage < -20), 0, Severity.FATAL_ERROR, "RANGE", "Project methodology does not permit a stage of " + stage);
		return String.format("%02d", (stage < 0) ? (stage + 100) : stage);
	}
	
	/**
	 * 
	 * TODO: Clock naming like: Clk_[V]_[non-func-gating-sig]_[F/V]_[func-gating_sig]_[scope]_[stage-suffix]
	 *                          Clk_sig_Hier_PIPE_2H
	 * 
	 * @param gater_sig: Gating signal, which must be in the same scope as the When (scope).  (When asserted, clock is required.)
	 *                   If null, functional_gater_sig is non-null.
	 *                   TODO: Timing, relative to dest_stage?
	 *                   Note, that gater_sig.getIdentifier().getType() == IdentifierType.SV_SIG must be handled and can overlap non-SV_SIG names.
	 * @param functional_gater_sig: Funtional gating signal, which must be in the same scope as the When.  (When deasserted, clock must not pulse.)
	 *                   If null, gater_sig is non-null.
	 *                   TODO: Timing, relative to dest_stage?
	 *                   Note, that gater_sig.getIdentifier().getType() == IdentifierType.SV_SIG must be handled and can overlap non-SV_SIG names.
	 * @param scope: The scope of the When and gating signals.  If all gaters are SV signals, scope is top scope.
	 * @param dest_stage: Stage of signals produced by state elements gated by this clock signal.
	 *                    Meaningless for SV-gaters (when not scoped w/i a pipeline), in which case the SV signal is assumed to have an appropriate
	 *                    phase, which means all assigned signals of the When must be flopped along, not latched.
	 * @return The SV clock signal name.
	 */
	public String svClockSignalName(PipeSignal gater_sig, PipeSignal functional_gater_sig, LogicalBehScope scope, int dest_stage)
	{
		String scope_suffix = "";
		if (scope.getParent() != null)
		{
			// Not SV-gaters at top scope, so there's pipeline context.
			String pipeline_tag = scope.getSelfOrAncestorOfType(NodeType.PIPELINE).getLabel();
			scope_suffix = getSvScopeStr(scope) + "_" + stageSuffix(Main.phaseToCyc(dest_stage));
		}
		return ("Clk_" +
		        ((gater_sig == null)                                            ? "" :
		         (gater_sig.getIdentifier().getType() != IdentifierType.SV_SIG) ? gater_sig.getIdentifier().getName() :
		        	                                                              ("V_" + gater_sig.getIdentifier().getName())
		        ) +
		        ((functional_gater_sig == null)                                            ? "" :
		         (functional_gater_sig.getIdentifier().getType() != IdentifierType.SV_SIG) ? ("F_" + functional_gater_sig.getIdentifier().getName()) :
		        	                                                                         ("V_F_" + functional_gater_sig.getIdentifier().getName())
		        ) +
		        scope_suffix + edgeSuffix(dest_stage)   // Note: Clock domain will be missing for SV-signal gaters, which have no pipeline scope.  TODO: Fix.
		       );
	}

	// TODO: These should be moved to an Intel-specific derived class.
	public String signalSuffix(LogicalPipeline pipeline_, int stage_number_)
	{
		return stageSuffix(Main.phaseToCyc(stage_number_)) + edgeSuffix(stage_number_);
	}
	
	
	/**
	 * Generate the SV string for a flop.
	 * @param out_sig
	 * @param in_sig
	 * @param clock_str
	 * @return the SV string for a flop.
	 */
	public String makeFlop(String out_sig, String in_sig, String clock_str)
	{
		return "always_ff @posedge(" + clock_str + ") " + out_sig + " <= " + in_sig + ";";
	}

	
	// TODO: Currently, SVGen uses the same latch for both phases with clk or !clk depending on phase.
	//       We should have flexibility to have SVGen use different latch flavors.
	// TODO: Need to support enabled flops/latches.

	/**
	 * Generate the SV string for a high-phase latch (transparent on high clock.
	 * @param out_sig
	 * @param in_sig
	 * @param clock_str
	 * @return the SV string for a high-phase latch (transparent on high clock.
	 */
	public String makeLatch(String out_sig, String in_sig, String clock_str)
	{
		return "always_latch if " + clock_str + " " + out_sig + " <= " + in_sig + ";";
	}

	
	/**
	 * 
	 * @param clock The clock to provide.
	 * @return String containing SV statement to create the given clock.  No terminating "\n".
	 */
	public String makeClockStr(Clock clock)
	{
	    return "`GATER(" +   // TODO: Need to define GATER in project-specific Verilog code.
	           clock.getSvReference() + ", " +
	           svUngatedClockName(clock.evenStage()) + ", " +
	           clock.getSvFunctionalEnableReference() + ", " +
	           clock.getSvEnableReference() +
	           ")";
	}

    /*
	public static void main(String[] args) 
    {
		Main.main(args, new ProjSpecific());
    }
    */
	
	public ProjSpecific()
	{
	}
}
