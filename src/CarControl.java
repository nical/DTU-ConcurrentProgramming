//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU Fall 2011

//Hans Henrik Løvengreen Oct 3, 2011


import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlayField
{
    static private Semaphore[][] field;
    private PlayField()
    {
        field=new Semaphore[11][12];
        for(int i=0;i<11;i++)
        {
            for(int j=0;j<12;j++)
            {
                field[i][j]=new Semaphore(1);
            }
        }
    }
    static private PlayField playFieldInstance=null;
    static public PlayField getField()
    {
        if(playFieldInstance==null) playFieldInstance=new PlayField();
        return playFieldInstance;
    }
    public void request(int row,int col) throws InterruptedException
    {
        field[row][col].P();
    }
    public void free(int row,int col)
    {
        field[row][col].V();
    }
    
}

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
        if (!isopen) { g.V(); isopen = true; }
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

/*
class OldBarrier
{
    // Main semaphore that blocks all the cars
    private Semaphore barrierSem = new Semaphore(1);
    // A secondary semaphore that ensures that no car can make blazing fast turn
    // and pass the barrier while the other cars are not all running (and thus
    // the barrier didn't have time to close)
    private Semaphore fastCarSem = new Semaphore(1);
    // These two members are protected by synchronized(this)
    private Boolean isOpen = true;
    private int count = 0;
    private int numactivecars=9;


    // isOn does not need to be protected because it is modified only sequentially
    // by the GUI thread, and only read (atmoic) by the car threads. 
    private Boolean isOn = false;

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

    synchronized int getCountProtected()
    {
        return count;
    }

    public void sync(int no) {
        if(!isOn) return;
        try {
            // block the fast car if he made a turn while the others are still departing
            try
            {
                fastCarSem.P();
            } catch(InterruptedException exp) {
                Thread.currentThread().interrupt();
                return;
            }
            fastCarSem.V();
            
            // open the barrier if the last car has arrived
            synchronized(this)
            {
                ++count;
                if( count == numactivecars )
                {
                    isOpen = true;
                    fastCarSem.P();
                    barrierSem.V();
                }
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
            }
            if( getCountProtected() == 0 )
            {
                synchronized(this){
                    isOpen = false;
                }
                
                barrierSem.P();
                fastCarSem.V();
            }
            
        } catch(InterruptedException exp) {
            Thread.currentThread().interrupt();
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

    
    // Turns off the barrier, Waits for the last car before doing so if the barrier
      
    public void shutDown() {
        try{
        if (!isOn) return;
        
        // waits for the barrier to be open
        barrierSem.P();
        barrierSem.V();
        off();

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
}*/

class Bridge {
    Semaphore bridge;
    Semaphore atomicAccess;
    int limit;
    int lastEntered;
    boolean wSetLimit;

    public Bridge() {
        limit = 1;
        bridge = new Semaphore(limit);
        atomicAccess = new Semaphore(1);
        wSetLimit = false;
    }

    public void enter(int no) throws InterruptedException {
        try { atomicAccess.P(); } catch (InterruptedException e) {Thread.currentThread().interrupt();return;}
        //System.out.println("Car "+no+" waits for entering. s="+bridge.toString());
        try { bridge.P(); } catch (InterruptedException e) {atomicAccess.V();Thread.currentThread().interrupt();return;}
        //System.out.println("Car "+no+" enters. s="+bridge.toString());
        lastEntered = no;
        atomicAccess.V();
    }

    public void leave(int no) {
        bridge.V();
        //System.out.println("Car "+no+" leaves. s="+bridge.toString());
        if (wSetLimit && no == lastEntered) {
        bridge = new Semaphore(limit);
        wSetLimit = false;
        //} else {
        // bridge.V();

        }
    }

    public void setLimit(int k) {
        limit = k;
        wSetLimit = true;
    }
}

class Car extends Thread {

    int basespeed = 100; // Rather: degree of slowness
    int variation = 50; // Percentage of base speed

    CarDisplayI cd; // GUI part

    int no; // Car number
    Pos startpos; // Startpositon (provided by GUI)
    Pos barpos; // Barrierpositon (provided by GUI)
    Color col; // Car color
    Gate mygate; // Gate at startposition
    Barrier barrier;
    Alley alley;

    boolean inAlley;
    boolean isonBridge;
    int myDirection;

    Bridge bridge;


    int speed; // Current car speed
    Pos curpos; // Current position
    Pos newpos; // New position to go to
    PlayField field;

    public Car(int no, CarDisplayI cd, Gate g, Barrier barrier, Bridge bridge, Alley alley) {

        this.no = no;
        this.cd = cd;
        mygate = g;
        inAlley=false;
        isonBridge=false;
        this.barrier = barrier;
        this.bridge = bridge;
        this.alley = alley;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no); // For later use
        field=PlayField.getField();
        

        col = chooseColor();

         myDirection= 0;
            if ( no > 0 && no < 5 ) {
                myDirection = Alley.B;
            } else if ( no > 4 ) {
                myDirection = Alley.A;
            }

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
    
    public boolean onBridge(Pos pos) {
     return (pos.col >= 1 && pos.col <= 3 && pos.row >= 0 && pos.row <= 1);
    }

    void cleanup()
    {
        field.free(curpos.row, curpos.col);
        cd.clear(curpos);
        if(inAlley) try {
            alley.leave(myDirection);
        } catch (InterruptedException ex1) {
            Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex1);
        }
        if(isonBridge)
        {
            bridge.leave(no);
        }
        Thread.currentThread().interrupt();
    }

   public void run() {
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);

            while (true) {
                try{
                    sleep(speed());
                }
                catch(InterruptedException exp)
                {
                    cleanup();
                    break;
                }
                
                if (atBarrier(curpos)) {
                 barrier.sync(no);
                }
                
                if (atGate(curpos)) {
                 mygate.pass();
                 speed = chooseSpeed();
                }
               
                newpos = nextPos(curpos);


                // Alley

                int cellType = Alley.getCellType(newpos,myDirection);
                if ( cellType == Alley.IN ) {
                    try{
                        alley.enter(myDirection);
                    }
                    catch(InterruptedException ex)
                    {
                        //inAlley=true;
                        cleanup();
                        break;
                    }
                    inAlley=true;
                } else if ( cellType == Alley.OUT ) {
                    try
                    {
                        alley.leave(myDirection);
                    }
                    catch(InterruptedException ex)
                    {
                        //inAlley=true;
                        cleanup();
                        break;
                    }
                    inAlley=false;
                }
                
                if(onBridge(nextPos(curpos)) && !onBridge(curpos)) {
                    try{
                        bridge.enter(no);
                    } catch(InterruptedException ex)
                    {
                        //inAlley=true;
                        cleanup();
                        break;
                    }
                    isonBridge=true;
                }
                
                if(!onBridge(nextPos(curpos)) && onBridge(curpos)) {
                 bridge.leave(no);
                }



                try{
                    field.request(newpos.row, newpos.col);
                }
                catch(InterruptedException ex)
                {
                    cleanup();
                    break;
                }
                

                // Move to new position
                //boolean move=false;
                    
                cd.clear(curpos);
                cd.mark(curpos,newpos,col,no);
                //move=true;
                try{
                    sleep(speed());
                }
                catch(InterruptedException ex) {
                    cd.clear(curpos, newpos);
                    //removedwhilemoving=true;
                    field.free(newpos.row, newpos.col);
                    cd.mark(curpos, col, no);
                    cleanup();
                    break;
                }
                cd.clear(curpos,newpos);
                //move=false;
                cd.mark(newpos,col,no);
                field.free(curpos.row, curpos.col);
                curpos = newpos;
            }
        }
        catch (Exception e) {
            cd.println("Exception in Car no. " + no);
            System.err.println("Exception in Car no. " + no + ":" + e);
            e.printStackTrace();
        }
    }

}

public class CarControl implements CarControlI{

    CarDisplayI cd; // Reference to GUI
    Car[] car; // Cars
    Gate[] gate; // Gates
    Barrier barrier;
    Bridge bridge;
    Alley alley;

    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        car = new Car[9];
        gate = new Gate[9];
        bridge = new Bridge();
        // change the argument in Alley's constructor to
        // SemaphoreAlley, MonitorAlley, or FairAlley to try
        // the different implementations
        alley = new Alley( new MonitorAlley() );
        barrier = new SemaphoreBarrier();
        
        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            car[no] = new Car(no,cd,gate[no],barrier, bridge, alley);
            car[no].start();
        }
    }

    public boolean hasBridge() {
        return true;
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
    }

    public void setLimit(int k) {
        bridge.setLimit(k);
     //cd.println("Setting of bridge limit not implemented in this version");
    }

    public void removeCar(int no) { 
        if(car[no].isAlive())
        {
            car[no].interrupt();
            barrier.removeCar();
        }
    }

        public void restoreCar(int no) {
        //cd.println("Restore Car not implemented in this version");
        if(!car[no].isAlive())
        {
            car[no]=new Car(no,cd,gate[no],barrier,bridge,alley);
            car[no].start();
            barrier.addCar();
        }
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) {
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) {
        car[no].setVariation(var);
    }

}
