public class ExecutedInstruction {
    int destinationRegister;
    int loadMemoryAddress;
    int storeMemoryAddress;
    int data;

    public ExecutedInstruction() {
        this.destinationRegister = -1;
        this.loadMemoryAddress = -1;
        this.storeMemoryAddress = -1;
        this.data = -1;
    }

    public String toString() {
        return "[ DESTINATION_REGISTER: " + this.destinationRegister +
                ", LOAD_MEMORY_ADDRESS: " + this.loadMemoryAddress +
                ", STORE_MEMORY_ADDRESS: " + this.storeMemoryAddress +
                ", DATA: " + this.data + " ]";
    }


}
