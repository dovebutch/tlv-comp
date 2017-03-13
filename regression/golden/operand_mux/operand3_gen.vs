// <Legal notice here>


`include "tlv.vh"





//
// Scope: |pipe1
//

// For signal "imm_data"
node [63:0] imm_data_PIPE1_03H;

// For signal "op_a"
node [63:0] op_a_PIPE1_03H;
node [63:0] op_a_PIPE1_04H;

// For signal "op_a_src"
node [3:0] op_a_src_PIPE1_03H;

// For signal "op_b"
node op_b_PIPE1_04H;

// For signal "opcode"
node opcode_PIPE1_04H;

// For signal "reg_data"
node [63:0] reg_data_PIPE1_05H;

// For signal "rslt"
node [63:0] rslt_PIPE1_04H;

// For signal "valid"
node valid_PIPE1_02H;
node valid_PIPE1_03H;

// Clock signals.
logic Clk_valid_PIPE1_04H ;

generate



   //
   // Scope: |pipe1
   //


   // For signal "imm_data"

   // For signal "op_a"
   always_ff @posedge(Clk_valid_PIPE1_04H) op_a_PIPE1_04H[63:0] <= op_a_PIPE1_03H[63:0];

   // For signal "op_a_src"

   // For signal "op_b"

   // For signal "opcode"

   // For signal "reg_data"

   // For signal "rslt"

   // For signal "valid"
   always_ff @posedge(Clk_H) valid_PIPE1_03H <= valid_PIPE1_02H;




endgenerate



//
// Gated clocks.
//

generate



   //
   // Scope: |pipe1
   //


   `GATER(Clk_valid_PIPE1_04H, Clk_H, 1'b1, valid_PIPE1_03H)



endgenerate



generate   // This is awkward, but we need to go into 'generate' context in the line that `includes the declarations file.
