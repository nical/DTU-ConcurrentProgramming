
public class Alley
{
    public static final int IN = 1;
    public static final int OUT = 2;
    public static final int NONE = 0;
    
    public static final int A = 1;
    public static final int B = 2;
    public static final int FREE = 0;
    
    public static int getCellType(Pos position, int direction)
    {
        System.out.println("d"+currentDirection+" s"+sem.toString()+" c"+count
            +" | "+position.row+" "+position.col);
        if ( direction == A )
        {
            if ( position.row == 0 && position.col == 0 )
            {
                return IN;
            }
            else if ( position.row == 10 && position.col == 0 ) return OUT;            
        }
        else if ( direction == B )
        {
            if ( (position.row == 8 && position.col == 1)
                || (position.row == 9 && position.col == 1) ) return IN;
            else if ( position.row == 1 && position.col == 2 ) return OUT;
        }

        return NONE;
    }

    
    public static void enter(int direction) throws java.lang.InterruptedException
    {
        System.out.println("Alley.enter("+direction+")");

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
        System.out.println("Alley.leave("+direction+")");
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
