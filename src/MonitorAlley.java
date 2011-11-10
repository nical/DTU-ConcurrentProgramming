
public class MonitorAlley implements AlleyBehaviour
{
    private int getCurrentDirection()
    {
        synchronized(this)
        {
            return currentDirection;
        }
    }
    
    public void enter(int direction) {
        try{
            //System.out.println("Alley.enter("+direction+")");
            if ( direction != getCurrentDirection() ){
                sem.P();
            }
            
            synchronized(this)
            {
                currentDirection = direction;
                ++count;
            }
        } catch (java.lang.Exception e ) {} 
    }

    public void leave(int direction) {
        try{
        //System.out.println("Alley.leave("+direction+")");
            synchronized(this)
            {
                --count;
                if(count == 0)
                {
                    currentDirection = Alley.FREE;
                    sem.V();
                }
            }
        } catch ( java.lang.Exception e ) {} 
    }
    
    private Semaphore sem = new Semaphore(1);
    private int currentDirection = 0;
    private int count = 0;
}
