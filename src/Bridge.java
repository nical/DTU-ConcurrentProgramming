
public class Brige
{
    public static final int IN = 1;
    public static final int OUT = 2;
    public static final int NONE = 0;
    
    public static final int A = 1;
    public static final int B = 2;
    public static final int FREE = 0;
    
    public static int getCellType(Pos position, int direction)
    {
        // TODO
        return NONE;
    }

    
    public static void enter(int direction) throws java.lang.InterruptedException
    {
        System.out.println("Bridge.enter("+direction+")");

        atomicAcceess.P();
        if (direction != currentDirection){
            atomicAcceess.V();
            
            sem.P();

            atomicAcceess.P();
        }
        currentDirection = direction;
        ++count;
        atomicAcceess.V();
    }
    
    public static void leave(int direction) throws java.lang.InterruptedException
    {
        System.out.println("Bridge.leave("+direction+")");
        atomicAcceess.P();
            --count;
            if(count == 0)
            {
                currentDirection = FREE;
                sem.V();
            }
        atomicAcceess.V();
    
    }

    
    private static Semaphore sem = new Semaphore(1);
    private static Semaphore atomicAcceess = new Semaphore(1);
    private static int currentDirection = 0;
    private static int count = 0;
}

