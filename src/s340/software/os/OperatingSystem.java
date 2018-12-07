package s340.software.os;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import s340.hardware.DeviceControllerOperations;
import s340.hardware.IInterruptHandler;
import s340.hardware.ISystemCallHandler;
import s340.hardware.ITrapHandler;
import s340.hardware.Machine;
import s340.hardware.Trap;
import s340.hardware.exception.MemoryFault;
import static s340.software.os.CheckValid.deviceNumber;

/*
 * The operating system that controls the software running on the S340 CPU.
 *
 * The operating system acts as an interrupt handler, a system call handler, and
 * a trap handler.
 */
public class OperatingSystem implements IInterruptHandler, ISystemCallHandler, ITrapHandler {

    // the machine on which we are running.
    private final Machine machine;
    PCB[] Process_Table = new PCB[10];
    List<PCB> newsort = new ArrayList<PCB>();
    //current process 
    int currentProcess = 0;
    //List of free spaces
    List<freeSpace> freespace = new ArrayList<>();
    List<Program> program = new LinkedList<>();
    public final int head = 0;

    LinkedList<IORequest>[] waitQ = new LinkedList[Machine.NUM_DEVICES];


    /*
	 * Create an operating system on the given machine.
     */
    public OperatingSystem(Machine machine) throws MemoryFault, Exception {
        //List<Program> program = new LinkedList<>();
        this.machine = machine;

        for (int i = 0; i < waitQ.length; i++) {
            waitQ[i] = new LinkedList<>();
        }

        //Process_Table = new PCB[10]; 
        ProgramBuilder wait = new ProgramBuilder();
        wait.size(2);
        wait.jmp(0);
        Program waiter = wait.build();
        program.add(waiter);

        freespace.add(new freeSpace(0, this.machine.MEMORY_SIZE));
        System.out.println("[Debug] Initial freeSpace size: " + freespace.get(0).size);
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

        // diag();
        // leave this as the last line
        machine.cpu.runProg = true;
    }

    public freeSpace findFree(int size) {
        for (freeSpace free : freespace) {
            if (free.size >= size) {
                return free;
            }
        }
        return null;
    }

    public void expands(List<freeSpace> o, int R) {
        //expanding in place going towards the right
        System.out.println("[Info] EXPANDING RIGHT INITIATED");

        for (freeSpace f : freespace) {
            if (f.start == Process_Table[currentProcess].limit + Process_Table[currentProcess].base && f.size >= R) {
                System.out.println("[Info] EXPANDING IN PLACE");
                Process_Table[currentProcess].limit = Process_Table[currentProcess].limit + R;
                f.start = f.start + R;
                f.size = f.size - R;
                if (f.size == 0) {
                    freespace.remove(f);
                }
                Process_Table[currentProcess].acc = 0;
                System.out.println("[Info] DONE EXPANDING");
                return;
            }

        }
        System.out.println("[Warning] Expanding right was not sucessful");
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

    /*
    * THe goal of this method is to shift any and all programs residing next to currentProcess to the right.
     */
    public int moveRight(List<PCB> newsort, int R) throws MemoryFault {
        System.out.println("[Info] BEGIN moveRight()");
        machine.memory.setBase(0);
        machine.memory.setLimit(Machine.MEMORY_SIZE);
        int index;
        int j;
        //find index to current process in the list
        for (index = 0; index < newsort.size(); index++) {
            if (newsort.get(index).equals(Process_Table[currentProcess])) {
                break;
            }
        }
        //loop through the programs to the right of the current process
        int B = Machine.MEMORY_SIZE - 1;
        for (j = newsort.size() - 1; j >= index; j--) {

            for (int w = 0; w < newsort.get(j).limit; w++) {
                //get instructions from theprogram
                int oldData = machine.memory.load(newsort.get(j).base + w);
                //store at the end of the freespace
                machine.memory.store(B, oldData);//Machine.MEMORY_SIZE - w, oldData);
                B--;
            }
            //make the j-th programs base the previous programs base plus the j-th programs limit

            newsort.get(j).base = B;

        }
        System.out.println("[Info] END moveRight()");
        return B;
    }

    public int moveLeft(List<PCB> newsort, int R) throws MemoryFault {
        machine.memory.setBase(0);
        machine.memory.setLimit(Machine.MEMORY_SIZE);
        int index = 0;
        int j = 0;
        System.out.println("[Info] BEGIN moveLeft()");
        for (index = 0; index < newsort.size(); index++) {
            if (newsort.get(index).equals(Process_Table[currentProcess])) {
                break;
            }
        }
        int a = 0;
        //loop through the programs that are to the left of current process and current process
        for (j = 0; j < index; j++) {
            //load the program at the start 
            for (int w = 0; w <= newsort.get(j).limit; w++) {

                int oldData = machine.memory.load(newsort.get(j).base + w);
                machine.memory.store(a + w, oldData);
                a++;
            }
            //update pcb to reflect new base 
            if (j != 0) {
                newsort.get(j).base = a;
            }
        }
        System.out.println("[Info] END moveLeft()");
        return a;
    }

    public void freemerge(List<freeSpace> freespace) {
        Iterator<freeSpace> freeit = freespace.iterator();
        freeSpace prev = freeit.next();
        System.out.println("[Info] BEGIN freemerge()");

        while (freeit.hasNext()) {

            freeSpace current = freeit.next();
            System.out.println("[Debug] Merge's iterator.next().size=" + current.size);
            //if the end of the last process = the start of the current one

            if (prev.start + prev.size == current.start) {
                prev.size += current.size;

                freeit.remove();
            } else {
                prev = current;
            }

        }
        System.out.println("[Info] freeMerge() was successful!");
        diag();
    }

    public void sbrk(int R) throws MemoryFault {
        //expanding in place going towards the right
        expands(freespace, R);
        if (Process_Table[currentProcess].acc == 0) {
            System.out.println("[Info] EXPANDED RIGHT AND BROKE OUT OF SBRK");
            diag();
            return;
        }

        //sorting free spaces
        Collections.sort(freespace);

        //merging of freespaces that are next to eachother
        freemerge(freespace);
        //expanding in place going towards the right
        expands(freespace, R);
        if (Process_Table[currentProcess].acc == 0) {
            System.out.println("[Info] EXPANDED RIGHT AND BROKE OUT OF SBRK");
            //diag();
            return;
        }
        //moving a program to a freespace w enough room and updating freespace and program
        //System.out.println("[Debug] OLD BASE IS:" + Process_Table[currentProcess].base + " OLD LIMIT WAS:" + +Process_Table[currentProcess].limit);
        newfree(freespace, R);
        if (Process_Table[currentProcess].acc == 0) {
            System.out.println("[Debug] NEW BASE IS:" + Process_Table[currentProcess].base + " NEW LIMIT IS:" + +Process_Table[currentProcess].limit);
            System.out.println("[Info] FOUND NEW FREE AND BROKE OUT OF SBRK");
            diag();
            return;
        }

        //SORTING 0F PCB'S
        for (int i = 0; i < Process_Table.length; i++) {
            if (Process_Table[i] != null) {
                newsort.add(Process_Table[i]);
            }
        }
        Collections.sort(newsort);

        compaction(R);

        // diag();
    }

    public void compaction(int R) throws MemoryFault {
        int b = moveRight(this.newsort, R);

        int a = moveLeft(this.newsort, R);

        this.freespace.removeAll(this.freespace);
        freeSpace space = new freeSpace(a, b - a);
        this.freespace.add(space);
        int old = this.Process_Table[currentProcess].limit;
        expands(freespace, R);
        if (old + R == this.Process_Table[currentProcess].limit) {
            this.Process_Table[currentProcess].acc = 0;
            System.out.println("[Info] THE COMPACTION WAS SUCESSFUL");
            return;
        } else {
            this.Process_Table[currentProcess].acc = 1;
            System.out.println("[Warning] THERE WAS NOT ENOUGH SPACE, COMPACTION FAILED");
            return;
        }
    }

    public void writeConsole(IORequest next) {
        Process_Table[currentProcess].status = ProcessState.waiting;
        if (waitQ[Machine.CONSOLE].isEmpty()) {

            machine.devices[Machine.CONSOLE].controlRegister.register[0] = DeviceControllerOperations.WRITE; // operation
            machine.devices[Machine.CONSOLE].controlRegister.register[1] = Process_Table[next.prognum].acc; // what we want to write
            machine.devices[Machine.CONSOLE].controlRegister.latch(); // initiate the latch
        }
       
        //System.out.println(waitQ[Machine.CONSOLE].size()+"WRITE Console");
    }
 public void deviceDiag() {
        System.out.println("[Debug] BEGIN deviceDiag()");

        for (int i = 0; i < waitQ.length; i++) {
            switch (i) {
                case Machine.KEYBOARD:
                    System.out.println("[Debug] Machine.KEYBOARD waitQ:");
                case Machine.CONSOLE:
                    System.out.println("[Debug] Machine.CONSOLE waitQ:");
                case Machine.DISK1:
                    System.out.println("[Debug] Machine.DISK1 waitQ:");
                case Machine.DISK2:
                    System.out.println("[Debug] Machine.DISK2 waitQ:");
            }}}
    public void read(IORequest request) throws MemoryFault {
        //System.out.println("SDASKLJDOIASDNLKASDLKASDJLKSHDASDKASDASDIOSHDIOASDHioasd");
        // sets the base and limit
        setBaseLimit();
        // sets what we need to be able to read
        int deviceNumber = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc);
        int platter = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 1);
        int platStart = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 2);
        int datasize = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 3);
        int memLoc = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 4) + Process_Table[request.prognum].base;
//        System.out.println(deviceNumber+"device");
//        System.out.println(platter+"plat");
//        System.out.println(platStart+"Start");
//        System.out.println(datasize+"data");
//        System.out.println(memLoc+"mem");

        Process_Table[currentProcess].status = ProcessState.waiting;

        machine.devices[deviceNumber].controlRegister.register[0] = DeviceControllerOperations.READ;
        machine.devices[deviceNumber].controlRegister.register[1] = platter;
        machine.devices[deviceNumber].controlRegister.register[2] = platStart;
        machine.devices[deviceNumber].controlRegister.register[3] = datasize;
        machine.devices[deviceNumber].controlRegister.latch();
        for (int i = 0; i < datasize; i++) {
            machine.memory.store(i + memLoc, machine.devices[deviceNumber].buffer[i]);

        }
        //System.out.println(waitQ[Machine.DISK1].size() + "   READ");
        // initiate the latch

    }

    public void write(IORequest request) throws MemoryFault {
        //System.out.println("[Debug] BEGIN WRITE");
        //sets the base and limit
        setBaseLimit();
        //sets what we need for the read and write
        // System.out.println("[Debug] Base & Limit set");
        int deviceNumber = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc);
        int platter = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 1);
        int platStart = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 2);
        int datasize = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 3);
        int memLoc = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 4) + Process_Table[request.prognum].base;
        Process_Table[currentProcess].status = ProcessState.waiting;
        //System.out.println("ACC:" + Process_Table[currentProcess].acc);
        machine.devices[deviceNumber].controlRegister.register[0] = DeviceControllerOperations.WRITE;
        machine.devices[deviceNumber].controlRegister.register[1] = platter;
        machine.devices[deviceNumber].controlRegister.register[2] = platStart;
        machine.devices[deviceNumber].controlRegister.register[3] = datasize;
        // System.out.println("[Debug] Registers set");
        for (int i = 0; i < datasize; i++) {
            machine.devices[deviceNumber].buffer[i] = machine.memory.load(memLoc + i);


        }

        machine.devices[deviceNumber].controlRegister.latch(); // initiate the latch
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

    public void diag() {
        boolean returnedFreeSpace = false; // We don't want the freeSpace output to be blank.  If this isn't changed by the end of the routine, print out a notifcation.

        System.out.println("[Info] BEGIN diag() routine");
        System.out.println("[Debug] Current state of programs:");
        for (int i = 0; i < this.Process_Table.length; i++) {
            if (this.Process_Table[i] != null) {
                System.out.println("[" + i + "] " + "Base: " + this.Process_Table[i].base + " | " + "Limit: " + this.Process_Table[i].limit);
            } else if (this.Process_Table[i] == null) {
                System.out.println("[" + i + "] <null>");
            }
        }

        System.out.println("[Debug] Current state of freeSpace's:");
        for (freeSpace f : this.freespace) {
            if (f != null) {
                System.out.println(f);
                returnedFreeSpace = true;
            } else if (f == null) {
                System.out.println("<null>");
                returnedFreeSpace = true;
            }
        }

        if (returnedFreeSpace == false) {
            System.out.println("[Warning] The freespace list appears to be empty");
        }

        System.out.println("[Info] END diag() routine");
    }


    /*
    * Handle a trap from the hardware.
    * 
    * @param programCounter -- the program counter of the instruction after the
    * one that caused the trap.
    * 
    * @param trapNumber -- the trap number for this trap.
     */
    public void saveRegisters(int savedProgramCounter) {
        Process_Table[currentProcess].acc = machine.cpu.acc;
        Process_Table[currentProcess].x = machine.cpu.x;
        Process_Table[currentProcess].pc = savedProgramCounter;
    }

    public void restoreRegisters() {
        this.machine.memory.setBase(Process_Table[currentProcess].base);
        this.machine.memory.setLimit(Process_Table[currentProcess].limit);

        Process_Table[currentProcess].status = ProcessState.running;
        this.machine.cpu.acc = Process_Table[currentProcess].acc;
        this.machine.cpu.x = Process_Table[currentProcess].x;
        this.machine.cpu.setPc(Process_Table[currentProcess].pc);
    }

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
                break;
            default:
                System.err.println("UNHANDLED TRAP " + trapNumber);
                System.exit(1);
        }
        // set current process to choose next 
        currentProcess = chooseNextProcess();

        //restore registers
        restoreRegisters();
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
    public void setBaseLimit() {
        //sets the bas and the limit in memory
        machine.memory.setBase(0);
        machine.memory.setLimit(Machine.MEMORY_SIZE);
    }

    @Override
    public synchronized void syscall(int savedProgramCounter, int callNumber) {
        saveRegisters(savedProgramCounter);
        setBaseLimit();

        //  leave this code here
        CheckValid.syscallNumber(callNumber);
        if (!machine.cpu.runProg) {
            return;
        }
        //  end of code to leave
        switch (callNumber) {
            case SystemCall.SYSTEM_BREAK: {
                try {

                    sbrk(Process_Table[currentProcess].acc);

                } catch (MemoryFault ex) {
                    Logger.getLogger(OperatingSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            //calls the write console method the be able to write to console
            case SystemCall.WRITE_CONSOLE: {
                //creates the IO Request we want to be able to write to the console
                 IORequest r = new IORequest(DeviceControllerOperations.WRITE, currentProcess);
                writeConsole(r);
                currentProcess = chooseNextProcess();
                //add to waitQ
                waitQ[Machine.CONSOLE].add(r);
                break;
            }
            // call the write method once we get a system call 
            case SystemCall.WRITE: {
                try {
                    //creates the IO Request want to write to
                    IORequest r = new IORequest(DeviceControllerOperations.WRITE, currentProcess);
                    int deviceNumber = machine.memory.load(Process_Table[currentProcess].base + Process_Table[currentProcess].acc);
                    //If the Q is empty write to disk
                    if (waitQ[deviceNumber].isEmpty()) {
                        write(r);
                    }
                    waitQ[machine.memory.load(Process_Table[currentProcess].base + Process_Table[currentProcess].acc)].add(r);
                } catch (MemoryFault ex) {
                    Logger.getLogger(OperatingSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentProcess = chooseNextProcess();
                break;
            }
            case SystemCall.READ: {
                try {
                    int deviceNumber = machine.memory.load(Process_Table[currentProcess].base + Process_Table[currentProcess].acc);
                    IORequest r = new IORequest(DeviceControllerOperations.READ, currentProcess);

                    //("READING OF");
                    if (waitQ[deviceNumber].isEmpty()) {

                        read(r);
                    }
                    waitQ[machine.memory.load(Process_Table[currentProcess].base + Process_Table[currentProcess].acc)].add(r);
                } catch (MemoryFault ex) {
                    Logger.getLogger(OperatingSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentProcess = chooseNextProcess();
                break;
            }
            default:
                System.out.println("UNHANDLED SYSCALL " + callNumber);
                System.exit(1);
        }

        restoreRegisters();
//        System.out.println("PC EQUALS:" +Process_Table[currentProcess].pc);

    }

    /*
    * This method implements the SSTF disk scheduling algorithm.
     */
    public IORequest SSTF(int deviceNumber, IORequest oldhead) {

        IORequest closest = null;
        int min = Integer.MIN_VALUE;

        //disk scheduling
        try {
            int hedlo = machine.memory.load(Process_Table[oldhead.prognum].base + Process_Table[oldhead.prognum].acc + 2) + machine.memory.load(Process_Table[oldhead.prognum].base + Process_Table[currentProcess].acc + 3);
            for (IORequest request : waitQ[deviceNumber]) {
                int zzzstart = machine.memory.load(Process_Table[request.prognum].base + Process_Table[request.prognum].acc + 2);
                //System.out.println(zzzstart);
                int distance = Math.abs(hedlo-zzzstart );

                //if its the closest to the headlocation 
                if (distance > min) {
                    closest = request;
                    min = distance;

                }
                
            }
            waitQ[deviceNumber].remove(closest);
            
            waitQ[deviceNumber].addFirst(closest);

        } catch (MemoryFault ex) {
            Logger.getLogger(OperatingSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return closest;
    }

    /*
	 * Handle an interrupt from the hardware.
	 * 
	 * @param programCounter -- the program counter of the instruction after the
	 * one that caused the trap.
	 * 
	 * @param deviceNumber -- the device number that is interrupting.
     */
    @Override
    public synchronized void interrupt(int savedProgramCounter, int deviceNumber) {

        //  leave this code here
        CheckValid.deviceNumber(deviceNumber);
        if (!machine.cpu.runProg) {
            return;
        }
        //  end of code to leave
        saveRegisters(savedProgramCounter);
        setBaseLimit();
        
        machine.interruptRegisters.register[deviceNumber] = false;
        IORequest oldhead = waitQ[deviceNumber].remove();
        Process_Table[oldhead.prognum].status = ProcessState.ready;
        
        IORequest next =null;
        
        switch (deviceNumber) {
            case 0: { //keyboard

            }
            case 1: { //console
                // If the queue is not empty write to console
                if (!waitQ[deviceNumber].isEmpty()) {
                    // setting next so that write console has something to write to and know what it is writing 
                    next = waitQ[deviceNumber].peek();
                    writeConsole(next);

                }
                break;
            }

            case 2: { //disk1
                if (!waitQ[deviceNumber].isEmpty()) {
                    //set next to the nextprocess that should be ran in the correct order
                    next = SSTF(deviceNumber, oldhead);
                    System.out.println("Program number "+next.prognum);
                }
                if (!waitQ[deviceNumber].isEmpty()) {
                    try {
                        //if requestType is 0 its a write if rr is 1 its a read
                        if (next.requestType == DeviceControllerOperations.WRITE) {
                            write(next);

                        } else {
                           // System.out.println("WHY YOU DONT WORK");
                            read(next);
                        }
                    } catch (MemoryFault ex) {
                        Logger.getLogger(OperatingSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }

                   

                }
                break;

            }
            case 3: { //disk2

            }

        }

        restoreRegisters();

    }
}
