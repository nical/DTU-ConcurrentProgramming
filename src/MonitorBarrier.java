
class MonitorBarrier implements Barrier {

    // Main semaphore that blocks all the cars
    private Semaphore barrierSem = new Semaphore(1);
    // A secondary semaphore that ensures that no car can make blazing fast turn
    // and pass the barrier while the other cars are not all running (and thus
    // the barrier didn't have time to close)
    private Semaphore fastCarSem = new Semaphore(1);
    // These two members are protected by synchronized(this)
    private boolean isOpen = true;
    private int count = 0;
    private int numactivecars=9;
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

    public void sync(int no) {
        synchronized(this) {
            if(!isOn) return;
        }
        try {
            // block the fast car if he made a turn while the others are still departing
            fastCarSem.P();
            fastCarSem.V();
            
            // open the barrier if the last car has arrived
            int tempCount;
            synchronized(this) {
                ++count;
                tempCount = count;
            }
            if( tempCount == numactivecars ) {
                synchronized(this) {
                    isOpen = true;
                }
                fastCarSem.P();
                barrierSem.V();
            }

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
            synchronized(this){ 
                --count;
                tempCount = count;
            }
            if( tempCount == 0 )
            {
                synchronized(this){
                    isOpen = false;
                }
                
                fastCarSem.V();
                barrierSem.P();
            }
            
        }catch(InterruptedException exp) {
            Thread.currentThread().interrupt();
        }catch (Exception e) {
            System.out.println("Something bad happened in Barrier.sync");
            System.exit(1);
        }
    }

    public void on() {
        boolean tempOpen;
        synchronized(this) {
            tempOpen = isOpen;
        }
        if( tempOpen ){
            synchronized(this) {
                isOpen = false;
            }
            try { barrierSem.P(); } catch (Exception e) {
                System.out.println("something worrying happened in Barrier.on");
                System.exit(1);
            }
        }
        synchronized(this) {
            isOn = true;
        }
    }

    public void off() {
        boolean tempOpen;
        synchronized(this) {
            isOn = false;
            tempOpen = isOpen;
        }
        if( !tempOpen ){
            synchronized(this) {
                isOpen = true;
            }
            try { barrierSem.V(); } catch (Exception e) {
                System.out.println("something scarry happened in Barrier.off.");
                System.exit(1);
            }
        }
    }

    /**
     * Turns off the barrier, Waits for the last car before doing so if the barrier
     */
    public void shutDown() {
        try{
        synchronized(this) {
            if (!isOn) return;
        }
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
