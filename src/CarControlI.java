//Specification of Car Control interface 
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU  Fall 2011

//Hans Henrik Løvengreen  Oct 3, 2011

interface CarControlI {

    /*  
     *  The following methods will be called sequentially.  
     *  No particular ordering of calls can be assumed. 
     *  
     *  All these methods should return without blocking (for long time) since 
     *  they may be called directly by the window event dispatcher thread 
     */   
    
    public boolean hasBridge();               // Test for presence of bridge
    
    public void stopCar(int no);              // Stop  Car no. by closing gate

    public void startCar(int no);             // Start Car no. by opening gate

    public void barrierOn();                  // Activate barrier

    public void barrierOff();                 // Deactivate barrier

    public void setLimit(int k);              // Set bridge limit

    public void removeCar(int no);            // Remove Car no. wherever it is
                                              // [SHIFT+Click at gate no.]

    public void restoreCar(int no);           // Restore Car no. at gate position
                                              // [CTRL+Click at gate no.]

    public void setSpeed(int no,int speed);   // Set speed of car no. (for testing)

    public void setVariation(int no,int var); // Set speed variation (percentage) 
                                              // of car no. (for testing)

    /*
     *  The barrierShutDown() method may be called concurrently with the other methods 
     *  except for barrierOn() and barrierOff(), and the method itself.
     *  
     *  barrierShutDown() should deactivate the barrier when not in use, ie. when no cars are 
     *  waiting.  If any cars are waiting when called, it must block until next release 
     *  (and then deactivate barrier).
     */

    public void barrierShutDown();             // Shut down barrier
}
