/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rak
 */
package s340.software;

import java.util.LinkedList;
import java.util.List;
import s340.hardware.Machine;
//import static s340.software.Main.pOfi;
import s340.software.os.Program;
import s340.software.os.OperatingSystem;
import s340.software.os.ProgramBuilder;
import s340.software.os.SystemCall;

public class Main2 {

    public static void main(String[] args) throws Exception {
        //	setup the hardware, the operating system, and power up
        //	do not remove this
        Machine machine = new Machine();
        OperatingSystem os = new OperatingSystem(machine);
        machine.powerUp(os);
        List<Program> programs = new LinkedList<>();
        //P1test(os, programs);
        programs.add(P6small(1));
        programs.add(P6small(2));
        programs.add(P6small(90));
         programs.add(P6small(3));
          programs.add(P6small(4));
           programs.add(P6small(5));
            programs.add(P6small(6)); 
             programs.add(P6small(7));
              programs.add(P6small(8));
               programs.add(P6small(9));
               programs.add(P6small(10));
            
        os.schedule(programs);

    }

    public Program write_console(int input) {
        int output = input; // change this to change the output

        ProgramBuilder b = new ProgramBuilder();
        Program p = b.build();

        b.size(4);

        return p;
    }

    public static void P1test(OperatingSystem os, List<Program> programs) throws Exception {
        os.schedule(programs);
    }public static Program P6small(int val){
       int platter =3;
        int platterStart = val;
        int dataSize = 1;
        int memoryLocation =450 ;
        int deviceNumber = Machine.DISK1;
        
          
        ProgramBuilder c = new ProgramBuilder();
        c.size(1000); 
         c.loadi(1);
        c.store(memoryLocation);
         c.loadi(deviceNumber);
        c.store(900);
        c.loadi(platter);
        c.store(901);
        c.loadi(platterStart);
        c.store(902);
        c.loadi(dataSize);
        c.store(903);
        c.loadi(memoryLocation);
        c.store(904);
        
        c.loadi(900);
        c.syscall(SystemCall.WRITE);
        c.end();
         Program f = c.build();
       
      //System.out.println("Program 5 "+f);
        return f;

    }
      public static Program P5(){
       int platter =3;
        int platterStart = 30;
        int dataSize = 5;
        int memoryLocation =450 ;
        int deviceNumber = Machine.DISK1;
          
        ProgramBuilder c = new ProgramBuilder();
        c.size(1000); 
         c.loadi(1);
        c.store(memoryLocation);        
        c.loadi(2);
        c.store(memoryLocation + 1);
        c.loadi(3);
        c.store(memoryLocation + 2);
        c.loadi(4);
        c.store(memoryLocation + 3);
        c.loadi(5);
        c.store(memoryLocation + 4);
        c.loadi(deviceNumber);
        c.store(900);
        c.loadi(platter);
        c.store(901);
        c.loadi(platterStart);
        c.store(902);
        c.loadi(dataSize);
        c.store(903);
        c.loadi(memoryLocation);
        c.store(904);
        
        c.loadi(900);
        c.syscall(SystemCall.WRITE);
        c.end();
         Program f = c.build();
       
      //System.out.println("Program 5 "+f);
        return f;
}
    public static Program P4(){
       int platter =3;
        int platterStart = 20;
        int dataSize = 5;
        int memoryLocation =50 ;
        int deviceNumber = Machine.DISK1;
          ProgramBuilder c = new ProgramBuilder();
        c.size(1000); 
         c.loadi(1);
        c.store(memoryLocation);        
        c.loadi(2);
        c.store(memoryLocation + 1);
        c.loadi(3);
        c.store(memoryLocation + 2);
        c.loadi(4);
        c.store(memoryLocation + 3);
        c.loadi(5);
        c.store(memoryLocation + 4);
        
        c.loadi(deviceNumber);
        c.store(700);
        c.loadi(platter);
        c.store(701);
        c.loadi(platterStart);
        c.store(702);
        c.loadi(dataSize);
        c.store(703);
        c.loadi(memoryLocation);
        c.store(704);
        
        c.loadi(700);
        c.syscall(SystemCall.WRITE);
         Program f = c.build();
        // c.end();
        return f;
}
public static Program P3(){
       int platter =3;
        int platterStart = 10;
        int dataSize = 5;
        int memoryLocation =120 ;
        int deviceNumber = Machine.DISK1;
          ProgramBuilder c = new ProgramBuilder();
        c.size(1000);
         c.loadi(1);
        c.store(memoryLocation);        
        c.loadi(2);
        c.store(memoryLocation + 1);
        c.loadi(3);
        c.store(memoryLocation + 2);
        c.loadi(4);
        c.store(memoryLocation + 3);
        c.loadi(5);
        c.store(memoryLocation + 4);
        
        c.loadi(deviceNumber);
        c.store(800);
        c.loadi(platter);
        c.store(801);
        c.loadi(platterStart);
        c.store(802);
        c.loadi(dataSize);
        c.store(803);
        c.loadi(memoryLocation);
        c.store(804);
        
        c.loadi(800);
        c.syscall(SystemCall.WRITE);
        c.end();
         Program f = c.build();
      //System.out.println("Program 3 "+f);
        return f;
}













    public static Program P2() {
        int platter =3;
        int platterStart = 31;
        int dataSize = 19;
        int memoryLocation =450 ;
        int deviceNumber = Machine.DISK1;
        
        ProgramBuilder c = new ProgramBuilder();
        c.size(1000);
        c.loadi(1);
        c.store(memoryLocation);        
        c.loadi(2);
        c.store(memoryLocation + 1);
        c.loadi(3);
        c.store(memoryLocation + 2);
        c.loadi(4);
        c.store(memoryLocation + 3);
        c.loadi(5);
        c.store(memoryLocation + 4);
        c.loadi(6);
        c.store(memoryLocation + 5);
        c.loadi(7);
        c.store(memoryLocation + 6);
        c.loadi(8);
        c.store(memoryLocation + 7);
        c.loadi(9);
        c.store(memoryLocation+ 8);
        c.loadi(10);
        c.store(memoryLocation + 9);
        c.loadi(11);
        c.store(memoryLocation + 10);
        c.loadi(12);
        c.store(memoryLocation + 11);
        c.loadi(13);
        c.store(memoryLocation + 12);
        c.loadi(14);
        c.store(memoryLocation + 13);
        c.loadi(15);

        c.store(memoryLocation + 14);  
        c.loadi(16);
        c.store(memoryLocation + 15);
        c.loadi(17);
        c.store(memoryLocation + 16);
        c.loadi(18);
        c.store(memoryLocation + 17);
        c.loadi(19);
        c.store(memoryLocation + 18);
        c.loadi(20);
        c.store(memoryLocation + 19);

        c.loadi(deviceNumber);
        c.store(800);
        c.loadi(platter);
        c.store(801);
        c.loadi(platterStart);
        c.store(802);
        c.loadi(dataSize);
        c.store(803);
        c.loadi(memoryLocation);
        c.store(804);
        
        c.loadi(800);
        c.syscall(SystemCall.WRITE);
        c.loadi(500);
        c.store(804);
        c.loadi(800);
        c.syscall(SystemCall.READ);
        
        for(int i = 500; i < 519; i++)
        
        {
        c.load(i);
        c.syscall(1);
        }
        c.end();
        Program f = c.build();
       // System.out.println(f);
        return f;
    }

    public static Program P1(int i) {
        ProgramBuilder b = new ProgramBuilder();
        b.size(50);
        b.loadi(i);
        b.store(40);
        b.store(41);
        b.store(42);
        b.store(43);
        b.store(44);

        b.load(40);
        b.syscall(1);
        b.load(41);
        b.syscall(1);
        b.load(42);
        b.syscall(1);
        b.load(43);
        b.syscall(1);
        b.load(44);
        b.syscall(1);
        b.end();

        Program a = b.build();

        return a;
    }
    // public Program P2(){

    //}
    //public Program P3(){
    // }
    
//    public static Program diskIO() {
//    /*
//    * Program Adjustment
//    *
//    * Change the following block according to the program's instruction size
//    */
//    int dataSize = 25;
//    int diskCommandStart = 109; // set to instruction size + 1
//    int restOfDiskCommands = diskCommandStart + 1; // set to start + 1, to be incremented later
//    int payloadStart = diskCommandStart + 5; // The +5 offset is because there are 5 parameters to be passed to the disk that are located in the first five data slots.
//    int restOfPayload = payloadStart + 1; // set to start + 1, to be incremented later
//
//    ProgramBuilder b = new ProgramBuilder();
//    b.size(dataSize);
//
//    /*
//    * BEGIN SystemCall.WRITE
//    */
//
//    /*
//    * BEGIN Disk Commands
//    *
//    * Adjust as needed. The fifth argument, dataSize, can be edited above.
//    */
//    int platterNumber = 3;
//    int platterStartPosition = 31;
//    int memoryLocation = payloadStart;
//    int deviceNumber = Machine.DISK1;
//    /*
//    * END Disk Commands
//    */
//
//    /*
//    * BEGIN Loading disk commands into memory.
//    */
//    b.loadi(platterNumber);
//    b.store(diskCommandStart);
//    b.loadi(platterStartPosition);
//    b.store(restOfDiskCommands);
//    restOfDiskCommands++;
//    b.loadi(dataSize);
//    b.store(restOfDiskCommands);
//    restOfDiskCommands++;
//    b.loadi(memoryLocation);
//    b.store(restOfDiskCommands);
//    restOfDiskCommands++;
//    b.loadi(deviceNumber);
//    b.store(restOfDiskCommands);
//    /*
//    * END Loading disk commands into memory.
//    */
//
//    /*
//    * BEGIN Storing the values into memory
//    */
//    b.loadi(1);
//    b.store(payloadStart);
//    b.loadi(2);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(3);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(4);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(5);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(6);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(7);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(8);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(9);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(10);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(11);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(12);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(13);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(14);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(15);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(16);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(17);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(18);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(19);
//    b.store(restOfPayload);
//    restOfPayload++;
//    b.loadi(20);
//    b.store(restOfPayload);
//    /*
//    * END Loading the values into memory
//    */
//
//    b.syscall(SystemCall.WRITE);
//    /*
//    * END SystemCall.WRITE
//    */
//
//    /*
//    * BEGIN SystemCall.READ
//    *
//    * The disk commands should be the same, apart from the memory location, which should be different
//    */
//
//    memoryLocation = payloadStart + dataSize;
//
//    b.loadi(memoryLocation);
//    b.store(diskCommandStart + 4);
//
//    //b.syscall(SystemCall.READ);
//
//    //b.syscall(SystemCall.WRITE_CONSOLE);
//
//    Program p = b.build();
//
//    System.out.println(p);
//
//    return p;
//    }
}
