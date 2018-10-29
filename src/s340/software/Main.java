package s340.software;

import java.util.LinkedList;
import java.util.List;
import s340.hardware.Machine;
import s340.software.os.Program;
import s340.software.os.OperatingSystem;
import s340.software.os.ProgramBuilder;
import s340.software.os.SystemCall;

/**
 * Ryan Aguilar, Nathan Aragones, Matthew Marrowquin
 */
public class Main {
 
    public static void main(String[] args) throws Exception {
        //	setup the hardware, the operating system, and power up
        //	do not remove this
        Machine machine = new Machine();
        OperatingSystem os = new OperatingSystem(machine);
        machine.powerUp(os);
        List<Program> programs = new LinkedList<>();
        /*
        * Below are the tests that demonstrate the different memory management strategies we have implemented for this project.
        * Uncomment a test to run it; only run one at a time.
        * IMPORTANT: you must change Machine.MEMORY_SIZE to match the combined size of your scheduled programs!  Each test has a recommended MEMORY_SIZE that has been known to work.
         */
        pOfiTest(os,programs); // context switching
        // expandTest(os,programs); // in-place expansion [of a program]
        // newFreeTest(os,programs); // Relocate program to an large-enough free block
        // freeMergeTest(os,programs); // Combine smaller free spaces together to form bigger ones.
        // compaction(os,programs); // compact programs to the right
        // moveLeftTest(os,programs); // compact programs to the left
    }

    public static void pOfiTest(OperatingSystem os, List<Program> programs) throws Exception {
        programs.add(pOfi(10));
        programs.add(pOfi(20));
        programs.add(pOfi(30));
        programs.add(pOfi(40));
        programs.add(pOfi(50));
        os.schedule(programs);
    }

    /*
    * sbrk() Checklist
    * [*] expands() - in-place expansion
    * [*] newfree() - Relocate program to an large-enough free block
    * [*] freemerge() - merging free space
    * [ ] moveRight() - compact to the right
    * [ ] moveLeft() - compact to the left
     */
    public static void expandTest(OperatingSystem os,List<Program> programs) throws Exception {
        /*
        * 1 x needyBoi
        * MEMORY_SIZE must be at least: 32 
         */
        programs.add(p3());
        os.schedule(programs);
    }

    public static void newFreeTest(OperatingSystem os,List<Program> programs) throws Exception {
        /*
        * 2 x longBois, 1 x needyBoi
        * MEMORY_SIZE must be at least: 70
        */
       programs.add(p7());
       programs.add(p3());
       programs.add(p7());
       
       os.schedule(programs);
    }
    
    public static void freeMergeTest(OperatingSystem os,List<Program> programs) throws Exception {
        /*
        * 
        * MEMORY_SIZE must be at least: 80
        */
       programs.add(p3Big());
       programs.add(p2());
       programs.add(p2());
        
       os.schedule(programs);
    }

    public static void compaction(OperatingSystem os,List<Program> programs) throws Exception {
        /*
        * TBD
        * MEMORY_SIZE must be at least: 250
         */
        
        programs.add(p7());
        programs.add(p7());
        programs.add(w1());
        programs.add(pm());
        programs.add(w1());
        os.schedule(programs);
    }

   public static Program w1(){
       ProgramBuilder b = new ProgramBuilder();
       b.size(6);
       b.jmp(0);
       b.end();
       Program a = b.build();
       return a;
   }
   public static Program pm(){
       ProgramBuilder b = new ProgramBuilder();
       int request = 200;
       b.loadi(210);
       b.loadi(213);
       b.loadi(231);
       b.loadi(400);
       b.loadi(765);
       b.loadi(request);
       b.syscall(0);
       b.loadi(420);

       b.output();
       b.end();
       Program a = b.build();
       return a;
   }
    /*
    * n e e d y   b o i
    * Instruction size: 14
     */
    public static Program p7(){
        ProgramBuilder b = new ProgramBuilder();
        b.loadi(10);
        b.loadi(10);
        b.loadi(10);
        b.output();
        
        b.end();
        Program a = b.build();
        return a;
    }
    public static Program p3() {
        int requestedSize = 2;

        ProgramBuilder b = new ProgramBuilder();

        b.loadi(5);
        b.tax();

        b.loadi(requestedSize); // load [int] into the acc before calling an sbrk().  Without it, the negative iterator left over from the end of the loop will be passed in and bad things will happen.
        b.syscall(0);
        b.txa();
        b.subi(1);
        b.tax();
        
        b.end();

        Program p = b.build();
        //System.out.println(p);

        return p;
    }
    
    
    /*
    * n e e d y   b o i
    * Absolute unit edition; requests a larger memory chunk at a smaller interval
    * Instruction size: 14
     */
    public static Program p3Big() {
        int requestedSize = 10;

        ProgramBuilder b = new ProgramBuilder();

        b.loadi(2);
        b.tax();

        int loop = b.loadi(requestedSize); // load [int] into the acc before calling an sbrk().  Without it, the negative iterator left over from the end of the loop will be passed in and bad things will happen.
        b.syscall(0);
        b.txa();
        b.subi(1);
        b.tax();
        b.jpos(loop);

        b.end();

        Program p = b.build();
        //System.out.println(p);

        return p;
    }

    /*
    * s h o r t   b o i
    * Measured size: 6
     */
    public static Program p2() {
        ProgramBuilder b = new ProgramBuilder();

        b.loadi(50001);
        b.output();
        b.end();

        Program p = b.build();
        //System.out.println(p);

        return p;
    }

    /*
    * l o n g   b o i
    * Measured size: 10
     */
    public static Program p1() {
        ProgramBuilder b = new ProgramBuilder();

        b.loadi(-50000);
        int loop = b.inca();
        b.jneg(loop);

        b.output();

        b.end();

        Program p = b.build();
        //System.out.println(p);

        return p;
    }

    public static Program pOfi(int input) {
        ProgramBuilder b = new ProgramBuilder();

        // number to be loaded into the storage space, and subsequently summed
        int i = input;
        // location of the stored sum.  Change as needed, i.e. if instruction count changes
        int storedSum = 55;
        // location of the storeLoop iterator.  Again, change as needed.
        int storeLoopIterator = storedSum + 1;
        // location of the sumLoop iterator.
        int sumLoopIterator = storeLoopIterator + 1;
        // [location of] the start of our storage space.  Should take up 10 spaces.
        int dataStart = 45;

        // Create storage space of size 13.  The last 3 spaces are for the iterators and sum.
        b.size(13);

        // Store the iterators
        b.loadi(-10);
        b.store(storeLoopIterator);
        b.store(sumLoopIterator);

        // Load dataStart into the X register
        b.loadi(dataStart - 1);
        b.tax();

        /*
  * BEGIN storeLoop
  * The goal is to recursively load `i` into storage space through the use of the x register.
         */
        // Store i in x + 1
        int storeLoop = b.loadi(i);
        b.storex(1);
        // Increment the iterator
        b.load(storeLoopIterator);
        b.inca();
        b.store(storeLoopIterator);
        // Jump back to the start of the loop, if applicable.
        b.jneg(storeLoop);

        /*
  * END storeLoop
         */
        // Prepare the x register for incrementation in sumLoop
        b.loadi(dataStart - 1);
        b.tax();

        /*
  * BEGIN sumLoop
         */
        int sumLoop = b.loadx(1);
        b.add(storedSum);
        b.store(storedSum);
        // Increment the sumLoopIterator
        b.load(sumLoopIterator);
        b.inca();
        b.store(sumLoopIterator);
        // Jump back to the start of the loop, if applicable.
        b.jneg(sumLoop);

        /*
  * END sumLoop
         */
        // Load and output the sum.
        b.load(storedSum);
        b.output();

        b.end();

        Program p = b.build();
        //System.out.println(p);
        return p;
    }
}