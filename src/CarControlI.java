//Specification of Car Control interface 
//Mandatory assignment 1 
//Course 02158 Concurrent Programming, DTU

//Hans Henrik Løvengreen   Oct 6, 2010

interface CarControlI {

    // The methods are not supposed to be called concurrently
    // The methods should return without blocking (too long) since 
    // they are normally called by the window event dispatcher thread

    public void stopCar(int no);    // Stop  Car no. by closing gate

    public void startCar(int no);   // Start Car no. by opening gate

    public void barrierOn();        // Activate barrier

    public void barrierOff();       // Deactivate barrier

    public void barrierSet(int k);  // Set barrier threshold

    public void removeCar(int no);  // Remove Car no. wherever it is
    // [SHIFT+Click at gate no.]

    public void restoreCar(int no); // Restore Car no. at gate position
    // [CTRL+Click at gate no.]

    public void setSpeed(int no,int speed); 
    // Set speed of car no. (for testing)

    public void setVariation(int no,int var); 
    // Set speed variation (percentage) 
    // of car no. (for testing)

}
