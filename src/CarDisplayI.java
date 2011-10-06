//Specification of Car Display interface 
//Mandatory assignment 1 
//Course 02158 Concurrent Programming, DTU

//Hans Henrik Løvengreen   Oct 6, 2010

import java.awt.Color;

interface CarDisplayI {

    // May be called concurrently 

    // Mark area at position p using color c and number no.
    public void mark(Pos p, Color c, int no); 

    // Mark area between adjacent positions p and q 
    public void mark(Pos p, Pos q, Color c, int no); 

    // Clear area at position p
    public void clear(Pos p);    

    // Clear area between adjacent positions p and q.
    public void clear(Pos p, Pos q);

    public void println(String message);  // Print (error) message on screen

    public Pos getStartPos(int no);       // Get start/gate position of Car no.
                                          // (on private part of track)

    public Pos getBarrierPos(int no);     // Get position right before 
                                          // barrier line for Car no.
                                          // (on private part of track).

    public Pos nextPos(int no, Pos pos);  // Get next position for Car no.
    
    public boolean isSlow(Pos pos);       // Get slow-down state

}



