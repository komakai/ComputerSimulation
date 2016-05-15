package com.example.giles.computersimulation;
import java.util.EnumMap;
import java.util.HashSet;

/**
 * Created by Giles on 2016/05/14.
 */

enum RunState {
    INITIALIZING, RUNNING, STOPPED
}

enum PcAutoAdvance {
    PC_AUTO_ADVANCE, NO_PC_AUTO_ADVANCE
}

enum StackArgCount {
    ARG_COUNT_NONE(0), ARG_COUNT_ONE(1), ARG_COUNT_TWO(2);
    int toInt() {
        return count;
    }
    StackArgCount(int count) {
        this.count = count;
    }
    private int count;
}

interface InstructionHandler {
    void handleInstruction(ComputerState state, int instructionArg) throws ComputerSimulatorException;
}

class InstructionInstance {
    InstructionInstance(int rawInstruction) {
        instruction = Instruction.get( ( rawInstruction>>24 ) & 0x000000FF );
        instructionArg = rawInstruction & 0x00FFFFFF;
    }
    InstructionInstance(Instruction instruction, int instructionArg) {
        this.instruction = instruction;
        this.instructionArg = instructionArg;
    }

    int toRawInstruction () {
        return ( (instruction.getOpCode() << 24) | (instructionArg & 0x00FFFFFF ) );
    }

    Instruction instruction = Instruction.NOP;
    int instructionArg;
}

abstract class  InstructionHandlerBase implements InstructionHandler  {
    @Override
    public void handleInstruction(ComputerState state, int instructionArg) throws ComputerSimulatorException {
        int stackArgs[] = new int[2];
        for ( int argCount = 0 ; argCount < stackArgCount.toInt(); argCount++ ) {
            stackArgs[argCount] = state.popStack();
        }
        doInstruction( state, stackArgs[0], stackArgs[1], instructionArg);
        if ( pcAutoAdvance == PcAutoAdvance.PC_AUTO_ADVANCE) {
            state.pcAdvance();
        }
    }

    abstract void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg ) throws ComputerSimulatorException;

    protected StackArgCount stackArgCount;
    protected PcAutoAdvance pcAutoAdvance;
}

class MultHandler extends InstructionHandlerBase {
    MultHandler() {
        stackArgCount = StackArgCount.ARG_COUNT_TWO;
        pcAutoAdvance = PcAutoAdvance.PC_AUTO_ADVANCE;
    }
    @Override
    void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg) throws  ComputerSimulatorException {
        state.pushStack(stackArg1*stackArg2);
    }
}

class CallHandler extends InstructionHandlerBase {
    CallHandler() {
        stackArgCount = StackArgCount.ARG_COUNT_NONE;
        pcAutoAdvance = PcAutoAdvance.NO_PC_AUTO_ADVANCE;
    }
    @Override
    void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg) throws  ComputerSimulatorException {
        state.movePc(instructionArg);
    }
}

class RetHandler extends InstructionHandlerBase {
    RetHandler() {
        stackArgCount = StackArgCount.ARG_COUNT_ONE;
        pcAutoAdvance = PcAutoAdvance.NO_PC_AUTO_ADVANCE;
    }
    @Override
    void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg) throws  ComputerSimulatorException {
        state.movePc( stackArg1 );
    }
}

class StopHandler extends InstructionHandlerBase {
    StopHandler() {
        stackArgCount = StackArgCount.ARG_COUNT_NONE;
        pcAutoAdvance = PcAutoAdvance.NO_PC_AUTO_ADVANCE;
    }
    @Override
    void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg) throws  ComputerSimulatorException {
        state.setRunState( RunState.STOPPED);
    }
}

class PrintHandler extends InstructionHandlerBase {
    PrintHandler() {
        stackArgCount = StackArgCount.ARG_COUNT_ONE;
        pcAutoAdvance = PcAutoAdvance.PC_AUTO_ADVANCE;
    }
    @Override
    void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg) throws  ComputerSimulatorException {
        state.doPrint(Integer.toString(stackArg1));
    }
}

class PushHandler extends InstructionHandlerBase {
    PushHandler() {
        stackArgCount = StackArgCount.ARG_COUNT_NONE;
        pcAutoAdvance = PcAutoAdvance.PC_AUTO_ADVANCE;
    }
    @Override
    void doInstruction(ComputerState state, int stackArg1, int stackArg2, int instructionArg) throws  ComputerSimulatorException {
        state.pushStack(instructionArg);
    }
}

class ComputerState {
    ComputerState(int memorySize) {
        memory = new int[memorySize];
    }

    int getNextOp() {
        return memory[programCounter];
    }

    int popStack() throws ComputerSimulatorException {
        stackPointer--;
        if ( stackPointer < stackBottom ) {
            throw new ComputerSimulatorException("Stack Underflow");
        }
        if (verbose) {
            doDebugOutput("Popping from to stack: " + Integer.toString(memory[stackPointer]) );
        }
        return memory[stackPointer];
    }

    void pushStack(int val) throws ComputerSimulatorException {
        if (verbose) {
            doDebugOutput("Pushing to stack: " + Integer.toString(val) );
        }
        if ( stackPointer == memory.length-1 ) {
            throw new ComputerSimulatorException("Stack Overflow");
        }
        memory[stackPointer] = val;
        stackPointer++;
    }

    void pcAdvance() throws ComputerSimulatorException {
        if (verbose) {
            doDebugOutput("Advancing program counter to: " + Integer.toString(programCounter+1) );
        }
        if ( programCounter == memory.length-1 ) {
            throw new ComputerSimulatorException("Program Counter Overflow");
        }
        programCounter++;
    }

    void movePc(int newPc) throws ComputerSimulatorException {
        if (verbose) {
            doDebugOutput("Moving program counter to: " + Integer.toString(newPc) );
        }
        if ( newPc >= memory.length ) {
            throw new ComputerSimulatorException("Program Counter Overflow");
        }
        programCounter = newPc;
    }

    void insertInstruction(Instruction instruction, int instructionArg) throws ComputerSimulatorException {
        if ( getRunState() != RunState.INITIALIZING) {
            throw new ComputerSimulatorException("Run state error");
        }
        if (programCounter >= memory.length ) {
            throw new ComputerSimulatorException("Insert address out of range");
        }
        memory[programCounter] = new InstructionInstance(instruction, instructionArg).toRawInstruction();
        if (verbose) {
            doDebugOutput("Inserting instruction: 0x" + Integer.toHexString(memory[programCounter]) + " at address: " + Integer.toString(programCounter)  );
        }
        programCounter++;
        if ( programCounter > stackBottom ) {
            stackBottom = programCounter;
        }
    }

    void setRunState(RunState runState) {
        if ( this.runState == RunState.INITIALIZING && runState == RunState.RUNNING ) {
            stackPointer = stackBottom;
        }
        this.runState = runState;
    }

    RunState getRunState() {
        return runState;
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void addOutputListener(ComputerSimulatorOutputListener outputListener) {
        listeners.add(outputListener);
    }

    public void doPrint(String output) {
        for (ComputerSimulatorOutputListener listener: listeners ) {
            listener.onOutput(output);
        }
    }

    public void doDebugOutput(String debugOutput) {
        for (ComputerSimulatorOutputListener listener: listeners ) {
            listener.onDebugOutput(debugOutput);
        }
    }

    private int[] memory;
    private int stackPointer;
    private int programCounter;
    private int stackBottom;
    private boolean verbose;
    private  RunState runState = RunState.INITIALIZING;
    private HashSet<ComputerSimulatorOutputListener> listeners = new HashSet<ComputerSimulatorOutputListener>();
}

public class ComputerSimulator {
    ComputerState state;
    static EnumMap<Instruction,InstructionHandler> instructionHandlerMap = new EnumMap<Instruction,InstructionHandler>(Instruction.class);
    static {
        instructionHandlerMap.put( Instruction.MULT, new MultHandler() );
        instructionHandlerMap.put( Instruction.CALL, new CallHandler() );
        instructionHandlerMap.put( Instruction.RET, new RetHandler() );
        instructionHandlerMap.put( Instruction.STOP, new StopHandler() );
        instructionHandlerMap.put( Instruction.PRINT, new PrintHandler() );
        instructionHandlerMap.put( Instruction.PUSH, new PushHandler() );
    }

    ComputerSimulator(int memorySize) {
        state = new ComputerState(memorySize);
    }

    public ComputerSimulator setAddress(int address) throws ComputerSimulatorException {
        state.movePc(address);
        return this;
    }

    public ComputerSimulator insertInstruction( Instruction instruction ) throws ComputerSimulatorException {
        return insertInstruction( instruction, 0 );
    }

    public ComputerSimulator insertInstruction( Instruction instruction, int instructionArg ) throws ComputerSimulatorException {
        state.insertInstruction(instruction, instructionArg);
        return this;
    }

    public void setVerbose(boolean verbose) {
        state.setVerbose(verbose);
    }

    void execute() throws ComputerSimulatorException {
        state.setRunState(RunState.RUNNING);
        while (state.getRunState() != RunState.STOPPED) {
            InstructionInstance instructionInstance = new InstructionInstance( state.getNextOp() );
            InstructionHandler instructionHandler = instructionHandlerMap.get( instructionInstance.instruction );
            if ( instructionHandler == null ) {
                throw new ComputerSimulatorException("Invalid Instruction");
            }
            instructionHandler.handleInstruction(state, instructionInstance.instructionArg);
        }
    }

    public void addOutputListener(ComputerSimulatorOutputListener outputListener) {
        state.addOutputListener(outputListener);
    }
}
