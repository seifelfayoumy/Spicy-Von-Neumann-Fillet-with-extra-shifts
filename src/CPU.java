import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CPU {
    String[] memory;
    String[] registers;

    public CPU() {
        this.memory = new String[2048];
        this.emptyDataMemory();
        int programSize = this.parseFile();
        int clockCycles = 7 + ((programSize - 1) * 2);
        int fetching =1;

        for (int i = 1; i <= clockCycles; i++) {
            if (1 % 2 != 0) {
                String instruction = this.fetch();
                System.out.println("clock: " + i);
                System.out.println(instruction);
                fetching +=1;
            }

            //decode
            //execute
            //memory
            //writeback
        }




    }

    public String fetch() {
        int pc = CPU.binaryToInt(this.memory[1056]);
        String instruction = this.memory[pc];
        pc += 1;
        String newPC = CPU.getBits(pc, 32);
        this.memory[1056] = newPC;

        return instruction;
    }

    public Instruction decode(String instruction){
        String opcode = instruction.substring(0, 4);
        Instruction decoded = new Instruction();

        switch(opcode){
            case "0000":{
                decoded.opcode = 0;
                decoded.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "0001":{
                decoded.opcode = 1;
                decoded.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "0010":{
                decoded.opcode = 2;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0011":{
                decoded.opcode = 3;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0100":{
                decoded.opcode = 4;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0101":{
                decoded.opcode = 5;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0110":{
                decoded.opcode = 6;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "0111":{
                decoded.opcode = 7;
                decoded.type = INSTRUCTION_TYPE.J;
                break;
            }
            case "1000":{
                decoded.opcode = 8;
                decoded.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "1001":{
                decoded.opcode = 9;
                decoded.type = INSTRUCTION_TYPE.R;
                break;
            }
            case "1010":{
                decoded.opcode = 10;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
            case "1011":{
                decoded.opcode = 11;
                decoded.type = INSTRUCTION_TYPE.I;
                break;
            }
        }
        if(decoded.type.equals(INSTRUCTION_TYPE.R)){
            decoded.R1 = CPU.binaryToInt(instruction.substring(4,9));
            decoded.R2 = CPU.binaryToInt(instruction.substring(9,14));
            decoded.R3 = CPU.binaryToInt(instruction.substring(14,19));
            decoded.SHAMT = CPU.binaryToInt(instruction.substring(19,32));
        } else if(decoded.type.equals(INSTRUCTION_TYPE.I)){
            decoded.R1 = CPU.binaryToInt(instruction.substring(4,9));
            decoded.R2 = CPU.binaryToInt(instruction.substring(9,14));
            decoded.IMMEDIATE = CPU.binaryToInt(instruction.substring(14,32));
        } else if(decoded.type.equals(INSTRUCTION_TYPE.J)){
            decoded.Address = CPU.binaryToInt(instruction.substring(4,32));
        }
        return decoded;
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
                    case "SFL":
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
                    instruction += CPU.extendBits(Integer.parseInt(words[1]), 18);
                } else if (type.equals("R")) {
                    instruction += CPU.getBinaryForRegister(words[1]);
                    instruction += CPU.getBinaryForRegister(words[2]);

                    if (words[0].equals("SLL") || words[0].equals("SLL")) {
                        instruction += "00000";
                        instruction += CPU.extendBits(Integer.parseInt(words[3]), 13);
                    } else {
                        instruction += CPU.getBinaryForRegister(words[3]);
                        instruction += "0000000000000";
                    }
                } else if (type.equals("I")) {
                    instruction += CPU.getBinaryForRegister(words[1]);
                    instruction += CPU.getBinaryForRegister(words[2]);
                    instruction += CPU.extendBits(Integer.parseInt(words[3]), 18);
                }

                this.memory[memoryIndex] = instruction;
                memoryIndex += 1;
            }
            return memoryIndex + 1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void printMemory() {
        for (int i = 0; i < this.memory.length; i++) {
            System.out.println(memory[i]);
        }
    }

    private static String extendBits(int number, int numBits) {
        int signBit = number & (1 << (numBits - 1));
        int mask = (1 << numBits) - 1;
        int extendedNumber = signBit != 0 ? (number | ~mask) : (number & mask);
        String binaryString = Integer.toBinaryString(extendedNumber);
        return String.format("%" + numBits + "s", binaryString).replace(' ', '0');
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

    private static int binaryToInt(String binary) {
        if (binary.length() != 32) {
            throw new IllegalArgumentException("Invalid binary string length. Expected 32 bits.");
        }

        int result = 0;
        for (int i = 0; i < binary.length(); i++) {
            char c = binary.charAt(i);
            if (c == '1') {
                result = result | (1 << (31 - i));
            } else if (c != '0') {
                throw new IllegalArgumentException("Invalid binary string. Only '0' and '1' are allowed.");
            }
        }
        return result;
    }

    public void emptyDataMemory() {
        for (int i = 1024; i <= 1057; i++) {
            this.memory[i] = CPU.extendBits(0, 32);
        }
    }

    private static String getBits(int number, int numBits) {
        StringBuilder sb = new StringBuilder();
        for (int i = numBits - 1; i >= 0; i--) {
            int bit = (number >> i) & 1;
            sb.append(bit);
        }
        return sb.toString();
    }


}
