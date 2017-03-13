// <Legal notice here>


`include "tlv.vh"


genvar inst;


//
// Scope: |pipe4
//


//
// Scope: >inst[3:0]
//

// For signal "add"
node [3:0] add_PIPE4_Inst_01H;
node [3:0] add_PIPE4_Inst_02H;
node [3:0] add_PIPE4_Inst_03H;
node [3:0] add_PIPE4_Inst_04H;

// For signal "opcode"
node [3:0] [4:0] opcode_PIPE4_Inst_01H;
node [3:0] [4:0] opcode_PIPE4_Inst_02H;
node [3:0] [4:0] opcode_PIPE4_Inst_03H;

// For signal "overflow"
node [3:0] overflow_PIPE4_Inst_04H;

// For signal "sub"
node [3:0] sub_PIPE4_Inst_04H;

// For signal "underflow"
node [3:0] underflow_PIPE4_Inst_04H;

// For signal "valid"
node [3:0] valid_PIPE4_Inst_00H;
node [3:0] valid_PIPE4_Inst_01H;
node [3:0] valid_PIPE4_Inst_02H;
node [3:0] valid_PIPE4_Inst_03H;

// For signal "write"
node [3:0] write_PIPE4_Inst_03H;

// Clock signals.
logic Clk_valid_PIPE4_Inst_02H [3:0];
logic Clk_valid_PIPE4_Inst_03H [3:0];
logic Clk_valid_PIPE4_Inst_04H [3:0];

generate



   //
   // Scope: |pipe4
   //



      //
      // Scope: >inst[3:0]
      //

      for (inst = 0; inst <= 3; inst++) begin
      // For signal "add"
      always_ff @posedge(Clk_valid_PIPE4_Inst_02H[inst]) add_PIPE4_Inst_02H[inst] <= add_PIPE4_Inst_01H[inst];
      always_ff @posedge(Clk_valid_PIPE4_Inst_03H[inst]) add_PIPE4_Inst_03H[inst] <= add_PIPE4_Inst_02H[inst];
      always_ff @posedge(Clk_valid_PIPE4_Inst_04H[inst]) add_PIPE4_Inst_04H[inst] <= add_PIPE4_Inst_03H[inst];

      // For signal "opcode"
      always_ff @posedge(Clk_valid_PIPE4_Inst_02H[inst]) opcode_PIPE4_Inst_02H[inst][4:0] <= opcode_PIPE4_Inst_01H[inst][4:0];
      always_ff @posedge(Clk_valid_PIPE4_Inst_03H[inst]) opcode_PIPE4_Inst_03H[inst][4:0] <= opcode_PIPE4_Inst_02H[inst][4:0];

      // For signal "overflow"

      // For signal "sub"

      // For signal "underflow"

      // For signal "valid"
      always_ff @posedge(Clk_H) valid_PIPE4_Inst_01H[inst] <= valid_PIPE4_Inst_00H[inst];
      always_ff @posedge(Clk_H) valid_PIPE4_Inst_02H[inst] <= valid_PIPE4_Inst_01H[inst];
      always_ff @posedge(Clk_H) valid_PIPE4_Inst_03H[inst] <= valid_PIPE4_Inst_02H[inst];

      // For signal "write"

      end



endgenerate



//
// Gated clocks.
//

generate



   //
   // Scope: |pipe4
   //



      //
      // Scope: >inst[3:0]
      //

      for (inst = 0; inst <= 3; inst++) begin
      `GATER(Clk_valid_PIPE4_Inst_02H[inst], Clk_H, 1'b1, valid_PIPE4_Inst_01H[inst])
      `GATER(Clk_valid_PIPE4_Inst_03H[inst], Clk_H, 1'b1, valid_PIPE4_Inst_02H[inst])
      `GATER(Clk_valid_PIPE4_Inst_04H[inst], Clk_H, 1'b1, valid_PIPE4_Inst_03H[inst])
      end



endgenerate



generate   // This is awkward, but we need to go into 'generate' context in the line that `includes the declarations file.
