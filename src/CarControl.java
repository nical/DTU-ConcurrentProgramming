//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU  Fall 2011

//Hans Henrik Løvengreen   Oct 3, 2011


import java.awt.Color;

class Gate {

    Semaphore g = new Semaphore(0);
    Semaphore e = new Semaphore(1);
    boolean isopen = false;

    public void pass() throws InterruptedException {
        g.P(); 
        g.V();
    }

    public void open() {
        try { e.P(); } catch (InterruptedException e) {}
        if (!isopen) { g.V();  isopen = true; }
        e.V();
    }

    public void close() {
        try { e.P(); } catch (InterruptedException e) {}
        if (isopen) { 
            try { g.P(); } catch (InterruptedException e) {}
            isopen = false;
        }
        e.V();
    }

}

class Barrier {
	
	boolean ison;
	boolean pass;
	Semaphore access;
	Semaphore[] barrier;
	boolean okforshutdown;
	
	public Barrier() {
		ison = false;
		pass = false;
		access = new Semaphore(7);
		barrier = new Semaphore[9];
		for (int i=0; i<9; i++) {
			barrier[i] = new Semaphore(0);
		}
		okforshutdown = false;
	}
	
	public void initBar() {
		pass = false;
		access = new Semaphore(7);
		barrier = new Semaphore[9];
		for (int i=0; i<9; i++) {
			barrier[i] = new Semaphore(0);
		}
		okforshutdown = false;
	}
	
	// Wait for others to arrive (if barrier active)
   public void sync(int no) {
	   if (!pass) {
		   pass = true;
		   //only 7 cars may access this zone, the 8th one is stopped here
		   try { access.P(); } catch (InterruptedException e) {}
		   pass = false;
		   //the cars are stopped by the barrier
		   try { barrier[no].P(); } catch (InterruptedException e) {}
	   }
	   //the last car may pass here and releases everyone
	   if (pass) {
		   pass = false;
		   for (int i=0; i<9; i++) {
			   if (i!=no) {
				   access.V();
				   barrier[i].V();
			   }
		   }
		   okforshutdown = true;
	   }
   }
   
   // Activate barrier
   public void on() {
	   if (!ison) {
		   initBar();
		   ison = true;
	   }
   }
   
   // Desactivate barrier 
   public void off() {
	   if (ison) {
		   ison = false;
		   for (int i=0; i<9; i++) {
			   barrier[i].V();
		   }
		   //access.V();
	   }
   }
   
   public void shutDown() {
	   if (ison) {
		   try { barrier[0].P(); } catch (InterruptedException e) {}
		   ison = false;
		   barrier[0].V();
	   }
   }

}

class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation =  50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition
    Barrier barrier;


    int speed;                       // Current car speed
    Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to

    public Car(int no, CarDisplayI cd, Gate g, Barrier barrier) {

        this.no = no;
        this.cd = cd;
        mygate = g;
        this.barrier = barrier;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no);  // For later use

        col = chooseColor();

        // do not change the special settings for car no. 0
        if (no==0) {
            basespeed = 0;  
            variation = 0; 
            setPriority(Thread.MAX_PRIORITY); 
        }
    }

    public synchronized void setSpeed(int speed) { 
        if (no != 0 && speed >= 0) {
            basespeed = speed;
        }
        else
            cd.println("Illegal speed settings");
    }

    public synchronized void setVariation(int var) { 
        if (no != 0 && 0 <= var && var <= 100) {
            variation = var;
        }
        else
            cd.println("Illegal variation settings");
    }

    synchronized int chooseSpeed() { 
        double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
        return (int)Math.round(factor*basespeed);
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;  
        return speed * (cd.isSlow(curpos)? slowfactor : 1);
    }

    Color chooseColor() { 
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos(no,pos);
    }

    boolean atGate(Pos pos) {
        return pos.equals(startpos);
    }
    
    boolean atBarrier(Pos pos) {
        return pos.equals(barpos);
    }

   public void run() {
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);

            while (true) { 
                sleep(speed());
                
                if (atBarrier(curpos) && barrier.ison) {
                	barrier.sync(no);
                }
                
                if (atGate(curpos)) { 
                	mygate.pass(); 
                	speed = chooseSpeed();
                }
                
                newpos = nextPos(curpos);
                
                //  Move to new position 
                cd.clear(curpos);
                cd.mark(curpos,newpos,col,no);
                sleep(speed());
                cd.clear(curpos,newpos);
                cd.mark(newpos,col,no);
                
                curpos = newpos;
                
            }

        } catch (Exception e) {
            cd.println("Exception in Car no. " + no);
            System.err.println("Exception in Car no. " + no + ":" + e);
            e.printStackTrace();
        }
    }

}

public class CarControl implements CarControlI{

    CarDisplayI cd;           // Reference to GUI
    Car[]  car;               // Cars
    Gate[] gate;              // Gates
    Barrier barrier;

    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        car  = new  Car[9];
        gate = new Gate[9];
        barrier = new Barrier();

        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            car[no] = new Car(no,cd,gate[no],barrier);
            car[no].start();
        } 
    }

    public boolean hasBridge() {
        return false;				// Change for bridge version
    }
    
    public void startCar(int no) {
        gate[no].open();
    }

    public void stopCar(int no) {
        gate[no].close();
    }

    public void barrierOn() {
    	barrier.on();
    }

    public void barrierOff() {
    	barrier.off();
    }

    public void barrierShutDown() { 
    	barrier.shutDown();
        //cd.println("Barrier shut down not implemented in this version");
        // Recommendation: 
        //   If not implemented call off() instead to make graphics consistent
    }

    public void setLimit(int k) { 
        cd.println("Setting of bridge limit not implemented in this version");
    }

    public void removeCar(int no) { 
        cd.println("Remove Car not implemented in this version");
    }

    public void restoreCar(int no) { 
        cd.println("Restore Car not implemented in this version");
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) { 
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) { 
        car[no].setVariation(var);
    }

}






