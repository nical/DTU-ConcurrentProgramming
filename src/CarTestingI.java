//Specification of Car Testing interface 
//Mandatory assignment 1 
//Course 02158 Concurrent Programming, DTU

//Hans Henrik Løvengreen   Oct 6, 2010


interface CarTestingI {
    // Corresponding GUI event

    public void startCar(int no);             // Click at red   gate no.
    public void stopCar(int no);              // Click at green gate no.

    public void startAll();                   // Click at Start All button
    public void stopAll();                    // Click at Stop  All button

    public void removeCar(int no);            // Click+shift at gate no.
    public void restoreCar(int no);           // Click+ctr   at gate no.

    public void barrierOn();                  // Click on On button
    public void barrierOff();                 // Click on Off button
    public void barrierSet(int k);            // Set threshold value

    public void setSlow(boolean slowdown);    // Set slowdown
    public void println(String message);      // Print (error) message on GUI

    public void setSpeed(int no, int speed);  // Set base speed (no GUI)
    public void setVariation(int no,int var); // Set variation  (no GUI)
}

