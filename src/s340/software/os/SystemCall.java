package s340.software.os;

/*
 * System call numbers for the operating system.
 */
public interface SystemCall
{
        public final static int SYSTEM_BREAK = 0;
        public final static int WRITE_CONSOLE = 1;
        //public final static int SYSTEM_BREAK_MERGE = 2;
        //public final static int SYSTEM_BREAK_COMPACTIOM = 3;
        public final static int READ = 2;
        public final static int WRITE = 3;
    
	public final static int NUM_SYSTEM_CALLS = 4;
}
