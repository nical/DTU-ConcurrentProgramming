
public class SemaphoreAlley implements AlleyBehaviour 
{

    public void enter(int direction) throws java.lang.InterruptedException {
        //System.out.println("Alley.enter("+direction+")"); 

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
    

    public void leave(int direction) throws java.lang.InterruptedException {
        //System.out.println("Alley.leave("+direction+")");
        atomicAcceess.P();
            --count;
            if(count == 0)
            {
                currentDirection = Alley.FREE;
                sem.V();
            }
        atomicAcceess.V();
    }

    
    private Semaphore sem = new Semaphore(1);
    private Semaphore atomicAcceess = new Semaphore(1);
    private int currentDirection = 0;
    private int count = 0;
}
