package s340.software.os;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import s340.hardware.DeviceControllerOperations;
import java.util.logging.Level;
import java.util.logging.Logger;
import s340.hardware.MemoryController;
import s340.hardware.IInterruptHandler;
import s340.hardware.ISystemCallHandler;
import s340.hardware.ITrapHandler;
import s340.hardware.Machine;
import s340.hardware.Trap;
import s340.hardware.exception.MemoryFault;

/*
 * The operating system that controls the software running on the S340 CPU.
 *
 * The operating system acts as an interrupt handler, a system call handler, and
 * a trap handler.
 */
public class OperatingSystem implements IInterruptHandler, ISystemCallHandler, ITrapHandler {

    // the machine on which we are running.
    private final Machine machine;
    private final static int NUMBER = 10;
    PCB[] Process_Table = new PCB[NUMBER];

    //current process
    int currentProcess = 0;
    //List of free spaces
    List<freeSpace> freespace = new ArrayList<>();
    List<Program> program = new LinkedList<>();
    Queue<IORequest>[] waitQ = new Queue[Machine.NUM_DEVICES];

    /*
	 * Create an operating system on the given machine.
     */
    public OperatingSystem(Machine machine) throws MemoryFault, Exception {
        //List<Program> program = new LinkedList<>();
        this.machine = machine;

        for (int i= 0; i < waitQ.length; i++) {
	            waitQ[i] = new LinkedList<>();
	      }
        //Process_Table = new PCB[10];
        ProgramBuilder wait = new ProgramBuilder();
        wait.size(2);
        wait.jmp(0);
        Program waiter = wait.build();
        program.add(waiter);

        freespace.add(new freeSpace(0, Machine.MEMORY_SIZE));
        schedule(program);
    }


    /*
	 * Load a program into a given memory address
     */
    private int loadProgram(Program program, int start) throws MemoryFault {
        int address = start;
        for (int i : program.getCode()) {
            machine.memory.store(address++, i);
        }
        return address;
    }

    /*
	 * Scheduled a list of programs to be run.
	 *
	 *
	 * @param programs the programs to schedule
     */
    public synchronized void schedule(List<Program> programs) throws MemoryFault, Exception {   //Creating a process control block

        for (Program program : programs) {

            int programLength = program.getDataSize() + program.getCode().length;
            freeSpace freeblock = findFree(programLength);
            machine.memory.setBase(0);
            machine.memory.setLimit(Machine.MEMORY_SIZE);
            loadProgram(program, freeblock.start);
            //acc , x register, PC, base, limit
            PCB newPCB = new PCB(freeblock.start, programLength);

            freeblock.start = programLength + freeblock.start;
            freeblock.size = freeblock.size - programLength;

            for (int i = 0; i < Process_Table.length; i++) {
                // If the spot is null in the process table then add the Process control block into the table
                if (Process_Table[i] == null) {
                    Process_Table[i] = newPCB;
                    break;
                }
            }
        }
        //printing out process tables
        for (int i = 0; i < Process_Table.length; i++) {
            //if (Process_Table[i].status==ProcessState.end) {
            //Process_Table[i].status = ProcessState.end;
            //System.err.println(i + " " + Process_Table[i].toString());
            //}
        }
        diag();
        // leave this as the last line
        machine.cpu.runProg = true;
    }

    public freeSpace findFree(int size) {
        for (freeSpace free : freespace) {
            if (free.size >= size) {
                return free;
                //moveLeft();
            }
        }
        return null;
    }

    public void expands(List<freeSpace> o, int R) {
        //expanding in place going towards the right

        for (freeSpace f : freespace) {
            //System.err.println(f.size);
            if (f.start == Process_Table[currentProcess].limit + Process_Table[currentProcess].base && f.size >= R) {
//System.err.println("EXPANDING IN PLACE");
                Process_Table[currentProcess].limit = Process_Table[currentProcess].limit + R;
                f.start = f.start + R;
                f.size = f.size - R;
                if (f.size == 0) {
                    freespace.remove(f);
                }
                Process_Table[currentProcess].acc = 0;
               System.err.println("DONE EXPANDING");
                //System.err.println(Process_Table[currentProcess].toString());
                //System.err.println(f.toString());
                return;
            }

        }
        //System.err.println("Expanding right was not sucessful");
        Process_Table[currentProcess].acc = 1;
    }

    public void newfree(List<freeSpace> o, int R) throws MemoryFault {
        machine.memory.setBase(0);
        machine.memory.setLimit(Machine.MEMORY_SIZE);
        for (int z = 0; z < o.size(); z++) {
            freeSpace f = o.get(z);
            if (f.size >= Process_Table[currentProcess].limit + R) {
                //MAKE NEW FREE SPACE TO TAKE PLACE OF PROGRAM
                freeSpace space = new freeSpace(Process_Table[currentProcess].base, Process_Table[currentProcess].limit);
                //set base to 0 and limit to max memory thn use physical adress

                //LOAD THE PROGRAM STARTING FROM THE NEW START ENDING AT NEW LIMIT
                for (int i = 0; i < Process_Table[currentProcess].limit; i++) {
                    int oldData = machine.memory.load(i + Process_Table[currentProcess].base);
                    machine.memory.store(f.start + i, oldData);
                }
                //UPDATE THE PCB TO REFLECT NEW BASE AND LIMIT
                Process_Table[currentProcess].base = f.start;
                Process_Table[currentProcess].limit += R;

                //UPDATE THE START AND SIZE OF THE FREEBLOCK
                f.start += Process_Table[currentProcess].limit;
                f.size = f.size - Process_Table[currentProcess].limit;
                if (f.size == 0) {
                    freespace.remove(f);
                }
                //ADD THE NEW FREESPACE TO LIST
                freespace.add(space);
                Process_Table[currentProcess].acc = 0;
                return;
            }
        }
        Process_Table[currentProcess].acc = 1;
    }

    public int moveRight(List<PCB> newsort, int j, int B) throws MemoryFault {
        //System.err.println("MOVING EVERYTHING TO THE RIGHT OF CURRENT TO THE RIGHT");

        //loop through the programs to the right of the current process
        for (int w = 0; w < newsort.get(j).limit; w++) {
            //get instructions from theprogram
            int oldData = machine.memory.load(newsort.get(j).base + w);
            //store at the end of the freespace
            machine.memory.store(B - newsort.get(j).limit, oldData);//Machine.MEMORY_SIZE - w, oldData);
            B--;
        }
        //make the j-th programs base the previous programs base plus the j-th programs limit

        newsort.get(j).base = B;

        // System.err.println("DONE MOVING RIGHT");
        return B;
    }

    public int moveLeft(List<PCB> newsort, int j, int a) throws MemoryFault {

        //loop through the programs that are to the left of current process and current process
        //load the program at the start
        for (int w = newsort.get(j).base; w < newsort.get(j).limit + newsort.get(j).base; w++) {
            //System.err.println(newsort.get(j).base+w);
            if (j == 0) {
                //System.err.println("w = "+w);
            }
            //System.err.println(newsort.get(j).limit+ newsort.get(j).base);
            int oldData = machine.memory.load(w);
            machine.memory.store(a, oldData);
            a++;
            if (j == 0) {
                //System.err.print(a);
            }
        }
        //update pcb to reflect new base
//            if (j != 0) {
        newsort.get(j).base = a - newsort.get(j).limit;
//            }

        //System.err.println("DONE MOVING LEFT");
        return a;
    }

    public void freemerge(List<freeSpace> freespace) {
        Iterator<freeSpace> freeit = freespace.iterator();
        freeSpace prev = freeit.next();
        //System.err.println("MERGEING FREE SPACES!!");

        while (freeit.hasNext()) {

            freeSpace current = freeit.next();
            //System.err.println("Merge's iterator.next().size="+current.size);
            //if the end of the last process = the start of the current one

            if (prev.start + prev.size == current.start) {
                prev.size += current.size;

                freeit.remove();
            } else {
                prev = current;
            }

        }
        //System.err.println("MERGE WAS SUCESSFUL!!");
    }

    public void sbrk(int R) throws MemoryFault {

//expanding in place going towards the right
        expands(freespace, R);
        if (Process_Table[currentProcess].acc == 0) {
            //System.err.println("EXPANDED RIGHT AND BROKE OUT OF SBRK");
            return;
        }

        //sorting free spaces
        Collections.sort(freespace);

        //merging of freespaces that are next to eachother
        freemerge(freespace);
        if (Process_Table[currentProcess].acc == 0) {
            System.err.println("Merge completed");
            return;
        }
        else {
            System.out.println("[Warning] Merge was not successful");
        }
        //expanding in place going towards the right
        expands(freespace, R);
        if (Process_Table[currentProcess].acc == 0) {
            System.err.println("EXPANDED RIGHT AND BROKE OUT OF SBRK");
            return;
        }

        //moving a program to a freespace w enough room and updating freespace and program
        // System.err.println("OLD BASE IS:" +Process_Table[currentProcess].base +" OLD LIMIT WAS:" + +Process_Table[currentProcess].limit);
        newfree(freespace, R);
        if (Process_Table[currentProcess].acc == 0) {
            System.err.println("FOUND NEW FREE AND BROKE OUT OF SBRK");
            return;
        }

        List<PCB> newsort = new ArrayList<PCB>();
        //SORTING 0F PCB'S
        for (int i = 1; i < Process_Table.length; i++) {

            if (Process_Table[i] != null && Process_Table[i].status != ProcessState.end) {
                System.out.print(i + "Status" + Process_Table[i].status);
                newsort.add(Process_Table[i]);
            }
        }
        Collections.sort(newsort);
//--------------MEMORY AND PROGRAM COMPACTION--------------
        machine.memory.setBase(0);
        machine.memory.setLimit(Machine.MEMORY_SIZE);
        int index = 0;
        int j = 0;
        int c = 0;
        int a = 4;
        int b = Machine.MEMORY_SIZE - 1;;
        //find index to current process in the list
        for (index = 0; index < newsort.size(); index++) {
            if (newsort.get(index).equals(Process_Table[currentProcess])) {
                break;
            }
        }
        for (c = 0; c <= index; c++) {

            a = moveLeft(newsort, c, a);

        }
        for (j = newsort.size() - 1; j > index; j--) {

            b = moveRight(newsort, j, b);

        }
        freespace.removeAll(freespace);
        //System.err.println("Size: " + freespace.size());
        freeSpace space = new freeSpace(a, b - a);
        freespace.add(space);

        int old = Process_Table[currentProcess].limit;
        expands(freespace, R);
        if (old + R == Process_Table[currentProcess].limit) {
            Process_Table[currentProcess].acc = 0;
            System.err.println("THE COMPACTION WAS SUCESSFUL");
            return;
        } else {
            Process_Table[currentProcess].acc = 1;
            System.err.println("THERE WAS NOT ENOUGH SPACE, COMPACTION FAILED");
        }

//

    }

    public void writeConsole (int writeConsole) {
        if (waitQ[Machine.CONSOLE].isEmpty()) {
            machine.devices[Machine.CONSOLE].controlRegister.register[0] = DeviceControllerOperations.WRITE; // operation
            machine.devices[Machine.CONSOLE].controlRegister.register[1] = writeConsole; // what we want to write
            machine.devices[Machine.CONSOLE].controlRegister.latch(); // initiate the latch
        }
        else {
            IORequest r = new IORequest(DeviceControllerOperations.WRITE, writeConsole);
            waitQ[Machine.CONSOLE].add(r);
        }
    }

    public int chooseNextProcess() {
        // set i to the next process go through the table to the end
        for (int i = currentProcess + 1; i < Process_Table.length; i++) {
            // if the next process is ready and not null return that process
            if (Process_Table[i] != null && Process_Table[i].getStatus() == ProcessState.ready) {
                return i;
            }
        }
        // go up to the currentProcess
        for (int i = 1; i <= currentProcess; i++) {
            // if the process table is not null and the status is ready return i
            if (Process_Table[i] != null && Process_Table[i].getStatus() == ProcessState.ready) {
                return i;
            }
        }
        return 0;
    }


    /*
    * Handle a trap from the hardware.
    *
    * @param programCounter -- the program counter of the instruction after the
    * one that caused the trap.
    *
    * @param trapNumber -- the trap number for this trap.
     */
    @Override
    public synchronized void trap(int savedProgramCounter, int trapNumber) {
        //  leave this code here
        CheckValid.trapNumber(trapNumber);
        if (!machine.cpu.runProg) {
            return;
        }
        //  end of code to leave
        // saving the registers of the current process
        saveRegisters(savedProgramCounter);

        switch (trapNumber) {
            case Trap.TIMER: // Time goes off
                //set current process to ready
                Process_Table[currentProcess].status = ProcessState.ready;
                break;
            case Trap.END://end of trap handler
                //set current process to end
                Process_Table[currentProcess].status = ProcessState.end;

                freeSpace newfree = new freeSpace(Process_Table[currentProcess].base,
                        Process_Table[currentProcess].limit);
                freespace.add(newfree);

                // Process_Table[currentProcess] = null; // uncomment to omit ended PCBs
                break;
            default:
                System.err.println("UNHANDLED TRAP " + trapNumber);
                System.exit(1);
        }
        // set current process to choose next
        currentProcess = chooseNextProcess();

        //restore registers
        restoreRegisters();
        Process_Table[currentProcess].status = ProcessState.running;

    }

    public void saveRegisters(int savedProgramCounter) {
        Process_Table[currentProcess].acc = machine.cpu.acc;
        Process_Table[currentProcess].x = machine.cpu.x;
        Process_Table[currentProcess].pc = savedProgramCounter;
    }

    public void restoreRegisters() {
        machine.memory.setBase(Process_Table[currentProcess].base);
        machine.memory.setLimit(Process_Table[currentProcess].limit);
        machine.cpu.acc = Process_Table[currentProcess].acc;
        machine.cpu.x = Process_Table[currentProcess].x;
        machine.cpu.setPc(Process_Table[currentProcess].pc);
    }

    /*
	 * Handle a system call from the software.
	 *
	 * @param programCounter -- the program counter of the instruction after the
	 * one that caused the trap.
	 *
	 * @param callNumber -- the callNumber of the system call.
	 *
	 * @param address -- the memory address of any parameters for the system
	 * call.
     */
    @Override
    public synchronized void syscall(int savedProgramCounter, int callNumber) {


        // System.err.println("Before: " + Process_Table[currentProcess]);
        //  leave this code here
        CheckValid.syscallNumber(callNumber);
        if (!machine.cpu.runProg) {
            return;
        }
        //  end of code to leave
        saveRegisters(savedProgramCounter);
        switch (callNumber) {
            case SystemCall.SYSTEM_BREAK: {
                try {
//                    System.err.println(Process_Table[currentProcess].acc);
                    diag();
                    sbrk(Process_Table[currentProcess].acc);
                    diag();

                } catch (MemoryFault ex) {
                    Logger.getLogger(OperatingSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case SystemCall.WRITE_CONSOLE: {
                writeConsole(this.machine.cpu.acc);
            }
            default:
                // System.err.println("UNHANDLED SYSCALL " + callNumber);
                System.exit(1);
        }

        //System.err.println("After: " + Process_Table[currentProcess]);
        restoreRegisters();
//        System.err.println("PC EQUALS:" +Process_Table[currentProcess].pc);
        // System.err.println("EXITING SYSCALL");
    }

    /*
	 * Handle an interrupt from the hardware.
	 *
	 * @param programCounter -- the program counter of the instruction after the
	 * one that caused the trap.
	 *
	 * @param deviceNumber -- the device number that is interrupting.
     */
    public void diag() {
        boolean returnedFreeSpace = false; // We don't want the freeSpace output to be blank.  If this isn't changed by the end of the routine, print out a notifcation.
        System.out.println("---------------------------------");
        System.out.println("[Info] BEGIN diag() routine");
        System.out.println("[Debug] Current state of programs:");
        for (int i = 0; i < this.Process_Table.length; i++) {
            if (this.Process_Table[i] != null) {
                System.out.println("[" + i + "] " + "Base: " + this.Process_Table[i].base + " | " + "Limit: " + this.Process_Table[i].limit + " | " + "Status: " + this.Process_Table[i].status);
            }
//
        }

        System.out.println("[Debug] Current state of freeSpace's:");
        for (freeSpace f : this.freespace) {
            if (f != null) {
                System.out.println(f);
                returnedFreeSpace = true;
            }
            else if (f == null) {
                System.out.println("<null>");
                returnedFreeSpace = true;
            }
        }

        if (returnedFreeSpace == false) {
            System.out.println("[Warning] The freespace list appears to be empty");
        }

        System.out.println("[Info] END diag() routine");
        System.out.println("----------------------------------");
    }

    @Override
    public synchronized void interrupt(int savedProgramCounter, int deviceNumber) {
        //  leave this code here
        CheckValid.deviceNumber(deviceNumber);
        if (!machine.cpu.runProg) {
            return;
        }
        //  end of code to leave

    }

}
