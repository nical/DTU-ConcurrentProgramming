
class WaitingCar
{
    public Semaphore semaphore ;
    int direction;
    WaitingCar(int direction)
    {
        this.semaphore = new Semaphore(0);
        this.direction = direction;
    }
}

public class FairAlley implements AlleyBehaviour
{
    private java.util.Queue<WaitingCar> waitingCars 
        = new java.util.LinkedList<WaitingCar>();
    private int currentDirection = Alley.FREE;
    private int count = 0;
    
    public void enter(int direction) throws java.lang.InterruptedException {
        handleQueue();
        if( !mayIGoDirectly(direction) ) {
            WaitingCar waiting = new WaitingCar(direction);
            synchronized(this) {
                // add the car to the waiting queue
                waitingCars.add( waiting );
            }
            waiting.semaphore.P(); // blocks the car
        }
        synchronized(this){
            currentDirection = direction;
            ++count;
        }
    }

    public void leave(int direction) throws java.lang.InterruptedException {
        synchronized(this) {
            --count;
            if( count == 0 ) {
                currentDirection = Alley.FREE;
            }
        }
        handleQueue();
    }

    public void handleQueue() {
        synchronized(this) {
            WaitingCar next = waitingCars.peek();
            if (next!= null && currentDirection == Alley.FREE) {
                currentDirection = next.direction;
            }
            while( next != null && next.direction == currentDirection ) {
                next.semaphore.V(); // let the car go in the alley
                waitingCars.poll(); // remove it from the queue
                next = waitingCars.peek();
            }
        }
    }

    private boolean mayIGoDirectly(int direction) {
        synchronized(this) {
            return ( currentDirection == Alley.FREE 
                    || ( currentDirection == direction 
                        && waitingCars.isEmpty() )
                   );
        }
    }
      
    private int getCurrentDirectionProtected() {
        synchronized(this) {
            return currentDirection;
        }
    }

    private boolean isQueueEmptyProtected() {
        synchronized(this) {
            return waitingCars.isEmpty();
        }
    }

    private void printQueue()
    {
        java.lang.String str = "";
        java.util.Iterator<WaitingCar> it = waitingCars.iterator();
        while(it.hasNext()) {
            str = str + " " + it.next().direction;
        }
        System.out.println(""+count+" -"+str);
    }

}
