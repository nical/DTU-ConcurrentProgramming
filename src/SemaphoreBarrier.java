

class SemaphoreBarrier implements Barrier {

    // Main semaphore that blocks all the cars
    private Semaphore barrierSem = new Semaphore(1);
    // A secondary semaphore that ensures that no car can make blazing fast turn
    // and pass the barrier while the other cars are not all running (and thus
    // the barrier didn't have time to close)
    private Semaphore fastCarSem = new Semaphore(1);
    // A semaphore to ensure atomicity of the access to certain members.
    private Semaphore atomicAccess = new Semaphore(1);
    // These two members are protected by the atomicAccess semaphore
    private boolean isOpen = true;
    private int count = 0;
    int numactivecars=9;
    // isOn does not need to be protected because it is modified only sequentially
    // by the GUI thread, and only read (atmoic) by the car threads. 
    private boolean isOn = false;

    public void setNumactivecars(int numactivecars) {
        this.numactivecars = numactivecars;
    }
    public void removeCar()
    {
        this.numactivecars--;
        if(count==numactivecars)
        {
            isOpen=true;
            barrierSem.V();
        }
    }
    public void addCar()
    {
        this.numactivecars++;
    }

    private boolean isOnProtected() throws java.lang.InterruptedException {
        atomicAccess.P();
        boolean result = isOn;
        atomicAccess.V();
        return result;
    }

    public void sync(int no) {
        try{
            if ( !isOnProtected() ) return;
            // block the fast car if he made a turn while the others are still departing
            fastCarSem.P();
            fastCarSem.V();
            
            // open the barrier if the last car has arrived
            atomicAccess.P();
            ++count;
            if( count == numactivecars )
            {
                isOpen = true;
                atomicAccess.V();
                
                fastCarSem.P();
                barrierSem.V();
                
                atomicAccess.P();
            }
            atomicAccess.V();

            // pass the barrier
            try{
            barrierSem.P();
            }catch (InterruptedException ex){
                count--;
                Thread.currentThread().interrupt();
                return;
            }
            barrierSem.V();

            // after the last car the barrier closes
            atomicAccess.P(); 
            --count;
            int tempCount = count;
            atomicAccess.V();
            if( tempCount == 0 )
            {
                atomicAccess.P();
                isOpen = false;
                atomicAccess.V();
                
                barrierSem.P();
                fastCarSem.V();
            }
            
        } catch (Exception e) {
            System.out.println("Something bad happened in Barrier.sync");
            System.exit(1);
        }
    }

    public void on() {
        if( isOpen ){
            isOpen = false;
            try { barrierSem.P(); } catch (Exception e) {
                System.out.println("something worrying happened in Barrier.on.");
                System.exit(1);
            }
        }
        isOn = true;
    }

    public void off() {
        isOn = false;
        if( !isOpen ){
            isOpen = true;
            try { barrierSem.V(); } catch (Exception e) {
                System.out.println("something scarry happened in Barrier.off.");
                System.exit(1);
            }
        }
    }

    /**
     * Turns off the barrier, Waits for the last car before doing so.
     */
    public void shutDown() {
        try{
        atomicAccess.P();
        if (!isOn) return;
        atomicAccess.V();
        
        // waits for the barrier to be open
        barrierSem.P();
        barrierSem.V();

        // wait that all the car started, else it might mess up with the
        // fast car synchronysation;
        fastCarSem.P();
        fastCarSem.V();
        off();
        } catch (Exception e) {
            System.out.println("something terrible happened in Barrier.shutdown.");
            System.exit(1);
        }
    }

}
