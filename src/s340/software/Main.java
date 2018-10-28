package s340.software;

import java.util.LinkedList;
import java.util.List;
import s340.hardware.Machine;
import s340.software.os.Program;
import s340.software.os.OperatingSystem;
import s340.software.os.ProgramBuilder;
import s340.software.os.SystemCall;

public class Main {

    public static void main(String[] args) throws Exception {
        //	setup the hardware, the operating system, and power up
        //	do not remove this
        Machine machine = new Machine();
        OperatingSystem os = new OperatingSystem(machine);
        machine.powerUp(os);

    }

    public Program write_console(int input) {
        int output = input; // change this to change the output
        
        ProgramBuilder b = new ProgramBuilder();
        Program p = b.build();
        
        b.size(4);
        
        return p;
    } 
}
