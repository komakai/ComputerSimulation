package com.example.giles.computersimulation;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Giles on 2016/05/15.
 */
public enum Instruction {
    NOP(0), MULT(1), CALL(2), RET(4), STOP(5), PRINT(6), PUSH(7);
    int getOpCode() {
        return opCode;
    }
    Instruction(int opCode) {
        this.opCode = opCode;
    }
    private int opCode;
    private static final Map<Integer,Instruction> lookup = new HashMap<Integer,Instruction>();
    static {
        for(Instruction instruction : EnumSet.allOf(Instruction.class)) {
            lookup.put(instruction.getOpCode(), instruction);
        }
    }
    public static Instruction get(int opCode) {
        return lookup.get(opCode);
    }
}
