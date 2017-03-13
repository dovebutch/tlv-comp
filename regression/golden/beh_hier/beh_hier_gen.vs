// <Legal notice here>


`include "tlv.vh"


genvar core, inst;


//
// Scope: >core[1:0]
//


//
// Scope: >inst[3:0]
//


//
// Scope: |pipe1
//

// For signal "imm_data"
node [1:0][3:0] imm_data_Core_Inst_PIPE1_03H;

// For signal "op_a"
node [1:0][3:0] [63:0] op_a_Core_Inst_PIPE1_03H;
node [1:0][3:0] [63:0] op_a_Core_Inst_PIPE1_04H;

// For signal "op_a_src"
node [1:0][3:0] op_a_src_Core_Inst_PIPE1_03H;

// For signal "op_b"
node [1:0][3:0] op_b_Core_Inst_PIPE1_04H;

// For signal "opcode"
node [1:0][3:0] opcode_Core_Inst_PIPE1_04H;

// For signal "reg_data"
node [1:0][3:0] [63:0] reg_data_Core_Inst_PIPE1_05H;

// For signal "rslt"
node [1:0][3:0] [63:0] rslt_Core_Inst_PIPE1_04H;
node [1:0][3:0] [63:0] rslt_Core_Inst_PIPE1_05H;

// For signal "valid"
node [1:0][3:0] valid_Core_Inst_PIPE1_01H;
node [1:0][3:0] valid_Core_Inst_PIPE1_02H;
node [1:0][3:0] valid_Core_Inst_PIPE1_03H;
node [1:0][3:0] valid_Core_Inst_PIPE1_04H;

// Clock signals.
logic Clk_valid_Core_Inst_PIPE1_04H [1:0][3:0];
logic Clk_valid_Core_Inst_PIPE1_05H [1:0][3:0];

//
// Scope: |pipe2
//

// For signal "out"
node [1:0][3:0] [63:0] out_Core_Inst_PIPE2_06H;

// For signal "rslt"
node [1:0][3:0] [63:0] rslt_Core_Inst_PIPE2_05H;
node [1:0][3:0] [63:0] rslt_Core_Inst_PIPE2_06H;


generate



   //
   // Scope: >core[1:0]
   //

   for (core = 0; core <= 1; core++) begin

      //
      // Scope: >inst[3:0]
      //

      for (inst = 0; inst <= 3; inst++) begin

         //
         // Scope: |pipe1
         //


         // For signal "imm_data"

         // For signal "op_a"
         always_ff @posedge(Clk_valid_Core_Inst_PIPE1_04H[core][inst]) op_a_Core_Inst_PIPE1_04H[core][inst][63:0] <= op_a_Core_Inst_PIPE1_03H[core][inst][63:0];

         // For signal "op_a_src"

         // For signal "op_b"

         // For signal "opcode"

         // For signal "reg_data"

         // For signal "rslt"
         always_ff @posedge(Clk_valid_Core_Inst_PIPE1_05H[core][inst]) rslt_Core_Inst_PIPE1_05H[core][inst][63:0] <= rslt_Core_Inst_PIPE1_04H[core][inst][63:0];

         // For signal "valid"
         always_ff @posedge(Clk_H) valid_Core_Inst_PIPE1_02H[core][inst] <= valid_Core_Inst_PIPE1_01H[core][inst];
         always_ff @posedge(Clk_H) valid_Core_Inst_PIPE1_03H[core][inst] <= valid_Core_Inst_PIPE1_02H[core][inst];
         always_ff @posedge(Clk_H) valid_Core_Inst_PIPE1_04H[core][inst] <= valid_Core_Inst_PIPE1_03H[core][inst];



         //
         // Scope: |pipe2
         //


         // For signal "out"

         // For signal "rslt"
         always_ff @posedge(Clk_H) rslt_Core_Inst_PIPE2_06H[core][inst][63:0] <= rslt_Core_Inst_PIPE2_05H[core][inst][63:0];


      end
   end


endgenerate



//
// Gated clocks.
//

generate



   //
   // Scope: >core[1:0]
   //

   for (core = 0; core <= 1; core++) begin

      //
      // Scope: >inst[3:0]
      //

      for (inst = 0; inst <= 3; inst++) begin

         //
         // Scope: |pipe1
         //


         `GATER(Clk_valid_Core_Inst_PIPE1_04H[core][inst], Clk_H, 1'b1, valid_Core_Inst_PIPE1_03H[core][inst])
         `GATER(Clk_valid_Core_Inst_PIPE1_05H[core][inst], Clk_H, 1'b1, valid_Core_Inst_PIPE1_04H[core][inst])


         //
         // Scope: |pipe2
         //



      end
   end


endgenerate



generate   // This is awkward, but we need to go into 'generate' context in the line that `includes the declarations file.
