
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
        = new java.util.ArrayDeque<WaitingCar>();
    private int currentDirection = Alley.FREE;
    private int count = 0;
    
    public void enter(int direction) {
    try{
        if( count == 0 && waitingCars.isEmpty() ) {
            // nobody in the alley and no one waiting to get in, lets go! 
        } else {
            // add the car to the waiting queue
            WaitingCar waiting = new WaitingCar(direction);
            waitingCars.add( waiting );
            waiting.semaphore.P(); // blocks the car
        }
        currentDirection = direction;
        ++count;
    }catch( java.lang.Exception e) {}
    }

    public void leave(int direction) {
    try{
        --count;
        if( count == 0 )
        {
            if ( waitingCars.isEmpty() ) {
                currentDirection = Alley.FREE;
            }
        }
        handleQueue();
    }catch( java.lang.Exception e  ){}
    }

    public void handleQueue() {
        
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
