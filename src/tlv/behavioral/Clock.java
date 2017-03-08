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

import tlv.Main;
import tlv.config.IdentifierType;

public class Clock {
	private PipeSignal enable_sig;  // null if none.
	private PipeSignal functional_enable_sig;  // null if none.
	private int stage;
	private String sv_name;
	
	public PipeSignal getEnableSignal()
	{
		return enable_sig;
	}
	
	public PipeSignal getFunctionalEnableSignal()
	{
		return functional_enable_sig;
	}
	
	public int getStage()
	{
		return stage;
	}
	
	public boolean evenStage()
	{
		return Main.isEven(stage);
	}
	
	public String getSvName()
	{
		return sv_name;
	}
	
	public String getSvReference()
	{
		return sv_name + getBehScope().getSvIndexStr();
	}
	
	private String getSvEnableSigName(PipeSignal sig)
	{
		return (sig.getIdentifier().getType() == IdentifierType.SV_SIG)
				  ? sig.getIdentifier().getName()
				  : Main.projSpecific.svSignalName(sig, stage - 2);  // TODO: Parameterize - 2.
	}

	/**
	 * @return the name of the SV enable signal for this clock, which
	 *         is driven the cycle before the gated clock edge.
	 */
	public String getSvEnableSigName()
	{
		return getSvEnableSigName(enable_sig);
	}
	
	/**
	 * @return the name of the SV functional enable signal for this clock, which
	 *         is driven the cycle before the gated clock edge.
	 */
	public String getSvFunctionalEnableSigName()
	{
		return getSvEnableSigName(functional_enable_sig);
	}
	
	/**
	 * @return Get the BehScope associated with this clock.
	 */
	public LogicalBehScope getBehScope()
	{
		return (enable_sig != null) ? enable_sig.getBehScope() : functional_enable_sig.getBehScope();
	}
	
	/**
	 * @return a String of SV code for the enable signal.
	 */
	public String getSvEnableReference()
	{
		if (enable_sig == null) {return "1'b1";}
		String ret = getSvEnableSigName() + enable_sig.getBehScope().getSvIndexStr();
		if (Main.command_line_options.xInjClock())
		{
			// Generate X on the enable if required by the project to generate X's for invalid data.
			// (It is recommended that the project's makeClockStr(...) do the X injection, always, instead.)
			ret = "(" + ret + " ? 1'b1 : 1'bx)";
		}
		return ret;
	}
	
	/**
	 * @return a String of SV code for the functional enable signal.
	 */
	public String getSvFunctionalEnableReference()
	{
		return (functional_enable_sig == null) ? "1'b1" : getSvFunctionalEnableSigName() + functional_enable_sig.getBehScope().getSvIndexStr();
	}
	
	/**
	 * @return the SV string that declares this clock.
	 */
	public String declareClockStr()
	{
	        return (Main.projSpecific.getClkSigSvType() + " " + sv_name + " " + getBehScope().getSvRangesStr() + ";");  // TODO: No " " if no range.
	}
	
	public String makeClockStr()
	{
		//return Main.projSpecific.makeClockStr(, enable_str, functional_enable_str, Main.isEven(stage));
		return Main.projSpecific.makeClockStr(this);
	}
	
	/**
	 * @return the project-specific SV string that generates this clock.
	 */
	Clock(PipeSignal _enable_sig, PipeSignal _functional_enable_sig, LogicalBehScope scope, int _stage)
	{
		enable_sig = _enable_sig;
		functional_enable_sig = _functional_enable_sig;
		stage = _stage;
		sv_name = Main.projSpecific.svClockSignalName(enable_sig, functional_enable_sig, scope, stage);
	}
}
