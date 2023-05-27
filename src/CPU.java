import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class CPU {
    String[] memory;
    int[] registers;

    public CPU() {
        this.memory = new String[2048];
        int programSize = this.parseFile();
        int clockCycles = 7 + ((programSize - 1) * 2);
        this.registers = new int[33];
        Arrays.fill(registers, 0);
        Queue<Instruction> pipeline = new LinkedList<>();

        for (int i = 1; i <= clockCycles; i++) {
            System.out.println("Clock: " + i);

            if (i % 2 != 0) {
                Instruction newInstruction = fetch();
                if (newInstruction != null) {
                    pipeline.add(newInstruction);
                }
            }

            Iterator<Instruction> iterator = pipeline.iterator();
            while (iterator.hasNext()) {
                Instruction instruction = iterator.next();
                System.out.print("Instruction " + instruction.id + ", Pipeline Stage: ");
                switch (instruction.pipelineStage) {
                    case 1:
                        System.out.println("fetching (input: PC " + (this.registers[32]-1) + ", output: " + instruction.binary + ")");
                        break;
                    case 2:
                        instruction = decode(instruction);
                        System.out.println("decoding (input: " + instruction.binary + ", output: " + instruction + ")");
                        break;
                    case 3:
                        System.out.println("decoding (input: " + instruction.binary + ", output: " + instruction + ")");
                        break;
                    case 4:
                        execute(instruction);
                        System.out.println("executing (input: " + instruction + ", output: " + instruction.executed + ")");
                        break;
                    case 5:
                        System.out.println("executing (input: " + instruction + ", output: " + instruction.executed + ")");
                        break;
                    case 6:
                        memoryOperation(instruction);
                        System.out.println("memory (input: " + instruction + ", output: write/read to memory if any)");
                        break;
                    case 7:
                        writebackOperation(instruction);
                        System.out.println("writeback (input: " + instruction + ", output write to destination register if any)");
                        iterator.remove();
                        break;
                }
                instruction.pipelineStage++;
            }

            if (i == clockCycles) {
                this.printAllRegisters();
                this.printMemory();
            }

        }
    }

    public void printMemory() {
        for (int i = 0; i < this.memory.length; i++) {
            String memoryWord = this.memory[i];
            if (this.memory[i] == null) {
                memoryWord = "EMPTY";
            }
            System.out.println("Memory index " + i + ": " + memoryWord);
        }
    }

    public void printAllRegisters() {
        for (int i = 0; i < this.registers.length; i++) {
            if (i == 32) {
                System.out.println("Register PC" + ": " + this.registers[i]);
            } else {
                System.out.println("Register R" + i + ": " + this.registers[i]);
            }

        }
    }

    public void memoryOperation(Instruction instruction) {
        if (instruction.executed.storeMemoryAddress != -1) {
            this.memory[instruction.executed.storeMemoryAddress] = CPU.extendBitsSigned(CPU.getBinaryTwosCompliment(instruction.executed.data), 32);
            System.out.println("Change in data memory: At " + instruction.executed.storeMemoryAddress + " <- " + instruction.executed.data);
        }
        if (instruction.executed.loadMemoryAddress != -1) {
            instruction.executed.data = CPU.parseSignedBinary(this.memory[instruction.executed.loadMemoryAddress]);
        }
    }

    public void writebackOperation(Instruction instruction) {
        if (instruction.executed.destinationRegister != -1 && instruction.executed.destinationRegister != 0) {
            this.registers[instruction.executed.destinationRegister] = instruction.executed.data;
            System.out.println("Change in register: R" + instruction.executed.destinationRegister + " <- " + instruction.executed.data);
        }
    }

    public void execute(Instruction instruction) {
        ExecutedInstruction result = new ExecutedInstruction();
        switch (instruction.opcode) {
            case 0: // ADD
                result.data = this.registers[instruction.R2] + this.registers[instruction.R3];
                result.destinationRegister = instruction.R1;
                break;
            case 1: // SUB
                result.data = this.registers[instruction.R2] - this.registers[instruction.R3];
                result.destinationRegister = instruction.R1;
                break;
            case 2: // MULI
                result.data = this.registers[instruction.R2] * instruction.IMM;
                result.destinationRegister = instruction.R1;
                break;
            case 3: // ADDI
                result.data = this.registers[instruction.R2] + instruction.IMM;
                result.destinationRegister = instruction.R1;
                break;
            case 4: // BNE
                if (this.registers[instruction.R1] != this.registers[instruction.R2]) {
                    this.registers[32] += instruction.IMM;
                }
                break;
            case 5: // ANDI
                result.data = this.registers[instruction.R2] & instruction.IMM;
                result.destinationRegister = instruction.R1;
                break;
            case 6: // ORI
                result.data = this.registers[instruction.R2] | instruction.IMM;
                result.destinationRegister = instruction.R1;
                break;
            case 7: // J
                this.registers[32] = (this.registers[32] & 0xF0000000) | instruction.ADDRESS;
                break;
            case 8: // SLL
                result.data = this.registers[instruction.R2] << instruction.SHAMT;
                result.destinationRegister = instruction.R1;
                break;
            case 9: // SRL
                result.data = this.registers[instruction.R2] >>> instruction.SHAMT;
                result.destinationRegister = instruction.R1;
                break;
            case 10: // LW
                result.loadMemoryAddress = this.registers[instruction.R2] + instruction.IMM;
                result.destinationRegister = instruction.R1;
                break;
            case 11: // SW
                result.storeMemoryAddress = this.registers[instruction.R2] + instruction.IMM;
                result.data = this.registers[instruction.R1];
                break;
        }
        instruction.executed = result;
    }


    public Instruction fetch() {
        int pc = this.registers[32];
        String instruction = this.memory[pc];
        if (instruction == null || instruction == "") {
            return null;
        }
        this.registers[32] = pc + 1;
        return new Instruction(instruction);
    }

    public Instruction decode(Instruction instruction) {
        String opcode = instruction.binary.substring(0, 4);

        switch (opcode) {
            case "0000": {
                instruction.opcode = 0;
                instruction.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "0001": {
                instruction.opcode = 1;
                instruction.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "0010": {
                instruction.opcode = 2;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0011": {
                instruction.opcode = 3;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0100": {
                instruction.opcode = 4;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0101": {
                instruction.opcode = 5;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0110": {
                instruction.opcode = 6;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0111": {
                instruction.opcode = 7;
                instruction.type = INSTRUCTION_TYPE.J;
                break;
            }
            case "1000": {
                instruction.opcode = 8;
                instruction.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "1001": {
                instruction.opcode = 9;
                instruction.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "1010": {
                instruction.opcode = 10;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "1011": {
                instruction.opcode = 11;
                instruction.type = INSTRUCTION_TYPE.I;
                break;
            }
        }
        if (instruction.type.equals(INSTRUCTION_TYPE.R)) {
            instruction.R1 = CPU.parseSignedBinary(instruction.binary.substring(4, 9));
            instruction.R2 = CPU.parseSignedBinary(instruction.binary.substring(9, 14));
            instruction.R3 = CPU.parseSignedBinary(instruction.binary.substring(14, 19));
            instruction.SHAMT = CPU.parseSignedBinary(instruction.binary.substring(19, 32));
        } else if (instruction.type.equals(INSTRUCTION_TYPE.I)) {
            instruction.R1 = CPU.parseSignedBinary(instruction.binary.substring(4, 9));
            instruction.R2 = CPU.parseSignedBinary(instruction.binary.substring(9, 14));
            instruction.IMM = CPU.parseSignedBinary(instruction.binary.substring(14, 32));
        } else if (instruction.type.equals(INSTRUCTION_TYPE.J)) {
            instruction.ADDRESS = Integer.parseInt(instruction.binary.substring(4, 32), 2);
        }
        return instruction;
    }


    public int parseFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/program.txt"))) {
            String line;
            int memoryIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(" ");
                String type = "";
                String instruction = "";
                switch (words[0]) {
                    case "ADD":
                        instruction += "0000";
                        type = "R";
                        break;
                    case "SUB":
                        instruction += "0001";
                        type = "R";
                        break;
                    case "MULI":
                        instruction += "0010";
                        type = "I";
                        break;
                    case "ADDI":
                        instruction += "0011";
                        type = "I";
                        break;
                    case "BNE":
                        instruction += "0100";
                        type = "I";
                        break;
                    case "ANDI":
                        instruction += "0101";
                        type = "I";
                        break;
                    case "ORI":
                        instruction += "0110";
                        type = "I";
                        break;
                    case "J":
                        instruction += "0111";
                        type = "J";
                        break;
                    case "SLL":
                        instruction += "1000";
                        type = "R";
                        break;
                    case "SRL":
                        instruction += "1001";
                        type = "R";
                        break;
                    case "LW":
                        instruction += "1010";
                        type = "I";
                        break;
                    case "SW":
                        instruction += "1011";
                        type = "I";
                        break;
                }

                if (type.equals("J")) {
                    instruction += CPU.extendBits0(Integer.toBinaryString(Integer.parseInt(words[1])), 28);
                } else if (type.equals("R")) {
                    instruction += CPU.getBinaryForRegister(words[1]);
                    instruction += CPU.getBinaryForRegister(words[2]);

                    if (words[0].equals("SLL") || words[0].equals("SRL")) {
                        instruction += "00000";
                        instruction += CPU.extendBits0(Integer.toBinaryString(Integer.parseInt(words[3])), 13);
                    } else {
                        instruction += CPU.getBinaryForRegister(words[3]);
                        instruction += "0000000000000";
                    }
                } else if (type.equals("I")) {
                    instruction += CPU.getBinaryForRegister(words[1]);
                    instruction += CPU.getBinaryForRegister(words[2]);
                    instruction += CPU.extendBitsSigned(CPU.getBinaryTwosCompliment(Integer.parseInt(words[3])), 18);
                }
                this.memory[memoryIndex] = instruction;
                memoryIndex += 1;
            }
            return memoryIndex;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static String getBinaryForRegister(String reg) {
        switch (reg) {
            case "R0":
                return "00000";
            case "R1":
                return "00001";
            case "R2":
                return "00010";
            case "R3":
                return "00011";
            case "R4":
                return "00100";
            case "R5":
                return "00101";
            case "R6":
                return "00110";
            case "R7":
                return "00111";
            case "R8":
                return "01000";
            case "R9":
                return "01001";
            case "R10":
                return "01010";
            case "R11":
                return "01011";
            case "R12":
                return "01100";
            case "R13":
                return "01101";
            case "R14":
                return "01110";
            case "R15":
                return "01111";
            case "R16":
                return "10000";
            case "R17":
                return "10001";
            case "R18":
                return "10010";
            case "R19":
                return "10011";
            case "R20":
                return "10100";
            case "R21":
                return "10101";
            case "R22":
                return "10110";
            case "R23":
                return "10111";
            case "R24":
                return "11000";
            case "R25":
                return "11001";
            case "R26":
                return "11010";
            case "R27":
                return "11011";
            case "R28":
                return "11100";
            case "R29":
                return "11101";
            case "R30":
                return "11110";
            case "R31":
                return "11111";
            default:
                return null;
        }
    }

    public static String extendBits0(String binary, int num) {
        if (binary.length() < num) {
            int y = binary.length();
            for (int i = 0; i < num - y; i++) {
                binary = "0" + binary;
            }
        }
        return binary;
    }

    public static String extendBitsSigned(String binary, int num) {
        String bit;

        if (binary.charAt(0) == '0') {
            bit = "0";
        } else {
            bit = "1";
        }
        if (binary.length() < num) {
            int y = binary.length();
            for (int i = 0; i < num - y; i++) {
                binary = bit + binary;
            }
        } else {
            int y = binary.length();
            for (int i = 0; i < y - num; i++) {
                binary = binary.substring(1);
            }
        }
        return binary;
    }

    public static String getBinaryTwosCompliment(int num) {
        if (num >= 0) {
            return "0" + Integer.toBinaryString(num);
        }
        return Integer.toBinaryString(num);
    }

    public static int parseSignedBinary(String binaryString) {
        if (binaryString.charAt(0) == '1') {
            // This is a negative number
            String invertedBinaryString = binaryString.substring(1).replace('0', '2').replace('1', '0').replace('2', '1');  // Invert all bits
            int positiveNumber = Integer.parseInt(invertedBinaryString, 2) + 1;  // Convert to positive and add one (because of the two's complement)
            int number = -positiveNumber;  // Negate
            return number;
        } else {
            // This is a positive number
            int number = Integer.parseInt(binaryString, 2);
            return number;
        }
    }

}
