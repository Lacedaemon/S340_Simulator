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
import static s340.software.Main.pOfi;
import s340.software.os.Program;
import s340.software.os.OperatingSystem;
import s340.software.os.ProgramBuilder;
import s340.software.os.SystemCall;

public class main1 {

    public static void main(String[] args) throws Exception {
        //	setup the hardware, the operating system, and power up
        //	do not remove this
        Machine machine = new Machine();
        OperatingSystem os = new OperatingSystem(machine);
        machine.powerUp(os);
        List<Program> programs = new LinkedList<>();
        //P1test(os, programs);
        programs.add(P2());
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
        programs.add(P1(1));
        programs.add(P1(2));
        programs.add(P1(3));
        programs.add(P1(4));

        os.schedule(programs);
    }

    public static Program P2() {
        int platter =3;
        int platterStart = 31;
        int dataSize = 19;
        int memoryLocation =81 ;
        int deviceNumber = 2;
        
        ProgramBuilder c = new ProgramBuilder();
        c.size(1000);
        c.loadi(1);
        c.store(81);
        c.loadi(2);
        c.store(82);
        c.loadi(3);
        c.store(83);
        c.loadi(4);
        c.store(84);
        c.loadi(5);
        c.store(85);
        c.loadi(6);
        c.store(86);
        c.loadi(7);
        c.store(87);
        c.loadi(8);
        c.store(88);
        c.loadi(9);
        c.store(89);
        c.loadi(10);
        c.store(90);
        c.loadi(11);
        c.store(91);
        c.loadi(12);
        c.store(92);
        c.loadi(13);
        c.store(93);
        c.loadi(14);
        c.store(94);
        c.loadi(15);
        c.store(95);
        c.loadi(16);
        c.store(96);
        c.loadi(17);
        c.store(97);
        c.loadi(18);
        c.store(98);
        c.loadi(19);
        c.store(99);
        c.loadi(20);
        c.store(100);

        c.loadi(deviceNumber);
        c.store(500);
        c.loadi(platter);
        c.store(501);
        c.loadi(platterStart);
        c.store(502);
        c.loadi(dataSize);
        c.store(503);
        c.loadi(memoryLocation);
        c.store(504);
        
        
        c.load(500);
        c.syscall(SystemCall.WRITE);
        c.loadi(101);
        c.store(508);
        c.load(500);
        c.syscall(SystemCall.READ);
        
        
        
        c.load(81);
        c.syscall(1);
        c.load(102);
        c.syscall(1);
        c.load(103);
        c.syscall(1);
        c.load(104);
        c.syscall(1);
        c.load(105);
        c.syscall(1);
        c.load(106);
        c.syscall(1);
        c.load(107);
        c.syscall(1);
        c.load(108);
        c.syscall(1);
        c.load(109);
        c.syscall(1);
        c.load(110);
        c.syscall(1);
        c.load(111);
        c.syscall(1);
        c.load(112);
        c.syscall(1);
        c.load(113);
        c.syscall(1);
        c.load(114);
        c.syscall(1);
        c.load(115);
        c.syscall(1);
        c.load(116);
        c.syscall(1);
        c.load(117);
        c.syscall(1);
        c.load(118);
        c.syscall(1);
        c.load(119);
        c.syscall(1);
        c.load(120);
        c.syscall(1);
        Program f =c.build();
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
}
