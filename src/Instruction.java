public class Instruction {
    INSTRUCTION_TYPE type;
    int opcode;
    int R1;
    int R2;
    int R3;
    int SHAMT;
    int IMM;
    int ADDRESS;
    String binary;
    int pipelineStage;
    ExecutedInstruction executed;
    int id;
    static int instructionsCount = 1;

    public Instruction(String binary) {
        this.binary = binary;
        this.pipelineStage = 1;
        this.id = Instruction.instructionsCount;
        Instruction.instructionsCount++;
    }

    public String toString() {
        return "[ TYPE: " + this.type +
                ", OPCODE: " + this.opcode +
                ", R1: " + this.R1 +
                ", R2: " + this.R2 +
                ", R3: " + this.R3 +
                ", SHAMT: " + this.SHAMT +
                ", IMM: " + this.IMM +
                ", ADDRESS: " + this.ADDRESS + " ]";
    }

}


enum INSTRUCTION_TYPE {
    R,
    I,
    J
}
