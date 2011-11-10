
/**
 * The alley. the implementation of the alley's behaviour is forwarded to
 * the behaviour member that is an instance of an object implementing
 * AlleyBehaviour. This makes it possible to keep every version of the alley
 * (semaphore, monitor, fair) in different java classes properly.
 */ 
public class Alley
{
    public static final int IN = 1;
    public static final int OUT = 2;
    public static final int NONE = 0;
    
    public static final int A = 1;
    public static final int B = 2;
    public static final int FREE = 0;
    
    private AlleyBehaviour behaviour;

    Alley( AlleyBehaviour b )
    {
        behaviour = b;
    }

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



    public void enter(int direction ) throws java.lang.InterruptedException {
        behaviour.enter( direction );
    }

    public void leave(int direction ) throws java.lang.InterruptedException {
        behaviour.leave(direction);
    }
    
}
