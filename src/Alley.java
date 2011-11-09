
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
        if ( direction == A )
        {
            if ( position.row == 0 && position.col == 3 )
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

    private int getCurrentDirection()
    {
        synchronized(this)
        {
            return currentDirection;
        }
    }
    
    public static void enter(int direction) throws java.lang.InterruptedException
    {
        //System.out.println("Alley.enter("+direction+")");
        _instance.doEnter(direction);
    }

    public static void leave(int direction) throws java.lang.InterruptedException
    {
        //System.out.println("Alley.leave("+direction+")");
        _instance.doLeave();
    }



    private void doEnter(int direction) throws java.lang.InterruptedException
    {
        // the synchronized(this) part is in getCurrentDirection because we must
        // not P the semaphore while in the synchonized section.
        if (  direction != getCurrentDirection() ){
            sem.P();
        }
        
        synchronized(this)
        {
            currentDirection = direction;
            ++count;
        }
    }

    private void doLeave()
    {
        synchronized(this)
        {
            --count;
            if(count == 0)
            {
                currentDirection = FREE;
                sem.V();
            }
        }
    }
    
    private Semaphore sem = new Semaphore(1);
    private int currentDirection = 0;
    private int count = 0;
    // no need to insure the threadsafeness of the instanciation since it is done
    // during static initiallisation.
    private static Alley _instance = new Alley();
}
