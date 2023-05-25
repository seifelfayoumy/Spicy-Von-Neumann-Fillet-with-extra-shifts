public class Instruction {
    INSTRUCTION_TYPE type;
    int opcode;
    int R1;
    int R2;
    int R3;
    int SHAMT;
    int IMMEDIATE;
    int Address;
}

enum INSTRUCTION_TYPE {
    R,
    I,
    J
}
