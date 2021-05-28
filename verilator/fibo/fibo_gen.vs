// <Legal notice here>


`include "tlv.vh"





//
// Scope: |pipe
//

// For signal "reset"
node reset_PIPE_00H;

// For signal "val"
node [15:0] val_PIPE_00H;
node [15:0] val_PIPE_01H;
node [15:0] val_PIPE_02H;


generate



   //
   // Scope: |pipe
   //


   // For signal "reset"

   // For signal "val"
   always_ff @posedge(Clk_H) val_PIPE_01H[15:0] <= val_PIPE_00H[15:0];
   always_ff @posedge(Clk_H) val_PIPE_02H[15:0] <= val_PIPE_01H[15:0];




endgenerate



//
// Gated clocks.
//

generate



   //
   // Scope: |pipe
   //





endgenerate



generate   // This is awkward, but we need to go into 'generate' context in the line that `includes the declarations file.
