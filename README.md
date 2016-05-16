===================
Notes on the design
===================

Description of key classes/components

ComputerSimulator

The ComputerSimulator class is the main class encapsulating the Computer Simulator; it comprises of a class to encapsulate the machine state and a map from instructions to instruction handlers.

ComputerState

Encapsulates the machine state; to enforce data encapsulation all data members are private and a set of accessor functions is provided for all required actions such as pushing/popping the stack, moving the program counter etc.

InstructionHandlerBase/instruction handlers

The instructions are implemented using the command pattern. Functionality that is common to multiple commands (such as advancing the program counter and popping stack arguments) is implemented in an abstract base class InstructionHandlerBase. The base class contains member variables to customize the common functionality (such number of stack arguments to pop).

PRINT command implementation

The Observer pattern is used to implement the PRINT command. A single observer (the main Activity class) listens for output and echoes it to the UI.

UI

The UI is very basic comprising two output panels. The first echoes output from the PRINT instruction, the second echoes debug output.
Two buttons allow the Computer Simulator to be executed either in nomal mode (no debug output) or verbose mode (outputting appropriate debug messages relating to the operations being performed by the Simulator).

Exception Handling

An exception class ComputerSimulatorException is created for handling unexpected and invalid states. The following situations result in a ComputerSimulatorException being thrown:
Stack Overflow - an attempt is made to access the stack past the end of the available memory
Stack Underflow - an attempt is made to pop from the stack beyond the bottom of the stack
Program Counter Overflow - an attempt is made to move the program counter past the end of the available memory
Run state exception - an attempt is made to insert/modify insrtuctions after execution has started
Instruction insertion error - an attempt is made to insert an instruction past the end of the available memory

Design decisions relating to Memory and instruction storage

Memory locations are of size 4-bytes and a simple array of int is used for memory storage.
Individual instructions are coded using the top byte to encode the operation and the remaining 3 bytes to store instruction specific arguments ( jump address, value to push etc. ). Conversion back and forth between raw instruction values and operation/instruction argument pairs is handled by a utility class InstructionInstance.

General design points

Preference is given to using enums over raw strings or primitive numberic types for 2 reason:
 - improved code readability
 - improved compile-time error detection (specifically a mistyped string "MLUT" will compile but a mistyped enum Instruction.MLUT will not)

A single exception class ComputerSimulatorException is prefered to multiple exception classes one for each possible error state. This simplifies the API (i.e. by reducing the total number of classes) without any significant loss of functionality.

Potential Improvements

If time had allowed I would have liked to have done something more interesting with the UI like a stack viewer.
Instead of having multiple instruction classes it might be interesting to try to have a single class with multiple instances with the instruction handling done using a closure passed to the constructor.

 
