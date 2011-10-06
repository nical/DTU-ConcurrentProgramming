//Implementation of Graphical User Interface class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU

//Hans Henrik Løvengreen  Oct 6, 2010


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
class CarField extends JPanel { 

    final static int   edge = 30;       // Field size
    
    // Colors
    final static Color defcolor      = Color.blue;
    final static Color symbolcolor   = new Color(200,200,200);
    final static Color blockcolor    = new Color(180,180,180);
    final static Color bgcolor       = new Color(250,250,250);  // Light grey
    final static Color slowcolor     = new Color(255,200,80);   // Amber
    final static Color bridgecolor   = new Color(210,210,255);  // Light blue
    final static Color overloadcolor = new Color(255,210,240);  // Pink
    final static Color opencolor     = new Color(0,200,0);      // Dark green
    final static Color closedcolor   = Color.red;
    final static Color barriercol    = Color.black;

    final static Font f = new Font("SansSerif",Font.BOLD,12);

    final static int maxstaints = 10;
    
    static Color currentBridgeColor = bridgecolor;

    private Cars cars;

    // Model of field status
    // Modified through event-thread (except for Car no. 0)
    
    private int users = 0;             // No. of current users of field
    private Color c = defcolor;        
    private char id = ' ';    
    private char symbol = ' ';
    private int xoffset = 0;           // -1,0,1 Horizontal offset
    private int yoffset = 0;           // -1,0,1 Vertical offset

    private boolean isblocked = false; // Field can be used 
    private boolean hadcrash  = false; // Car crash has occurred 
    private boolean keepcrash = false; // For detecting crashes
    private boolean slowdown  = false; // Slow field
    private boolean onbridge  = false; // Bridge field

    private boolean isstartpos = false;    
    private int     startposno = 0;
    private boolean startposopen = false;

    private boolean barriertop = false;
    private boolean barrieractive = false;

    private int staintx = 0;
    private int stainty = 0;
    private int staintd = 0;


    private static boolean light (Color c) {
        return (c.getRed() + 2* c.getGreen() + c.getBlue()) > 600;
    }

    public CarField(Pos p, Cars c) {
        cars = c;
        setPreferredSize(new Dimension(edge,edge));
         setBackground(bgcolor);
        setOpaque(true);

        addMouseListener(new MouseAdapter () {
            public void mousePressed(MouseEvent e) {
                if (isstartpos) {
                    if ((e.getModifiers() & InputEvent.SHIFT_MASK) > 0)  
                        cars.removeCar(startposno);
                    else
                        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) 
                            cars.restoreCar(startposno);
                        else
                            cars.startFieldClick(startposno);
                }
            }
        });
    }

    public void enter(int xoff, int yoff, Color newc, char ch) {
        users++;
        if (users > 1 && keepcrash && !hadcrash) {
            hadcrash = true;
            // Define a staint
            int dia = 7;
            staintx = (edge-1-dia)/2 +(int)Math.round(Math.random()*4) - 2;
            stainty = (edge-1-dia)/2 +(int)Math.round(Math.random()*4) - 2;
            staintd = dia;
        }
        c = newc;
        id = ch;
        xoffset = xoff;
        yoffset = yoff;
        repaint();  
    }

    public void exit() {
        users--;
        repaint();
    }

    public void clean() {
        hadcrash = false;
        repaint();
    }


    public void setSymbol(char c) {
        symbol = c;
    }

    public void setBlocked(){
        isblocked = true;
        setBackground(blockcolor);
    }

    public void setStartPos(int no, boolean open) {
        setSymbol((char) (no + (int) '0'));
        isstartpos = true;
        startposno = no;
        startposopen=open;
        repaint();
    }

    public void setStartPos(boolean open) {
        startposopen=open;
        repaint();
    }

    public void showBarrier(boolean active) {
        barrieractive = active;
        repaint();
    }

    public void setBarrierPos(boolean top) {
        barriertop = top;                         //  Set only once
    }

    public void setKeep(boolean keep) {
        keepcrash = keep;
        if (!keep &&  hadcrash) clean();
    }

    public void setSlow(boolean slowdown) {
        this.slowdown = slowdown;
        setBackground(slowdown? slowcolor : bgcolor);
        repaint();
    }

    public void setBridge(boolean onbridge) {
        this.onbridge = onbridge; 
        setBackground(onbridge? currentBridgeColor : bgcolor);
        repaint();
    }
    
    public static void setOverload(boolean overloaded) {
        currentBridgeColor = (overloaded ? overloadcolor : bridgecolor);
    }

    // This method may see transiently inconsistent states of the field if used by Car no. 0
    // This is considered acceptable
    public void paintComponent(Graphics g) {
        g.setColor(isblocked ? blockcolor : (slowdown ? slowcolor: onbridge ? currentBridgeColor: bgcolor));
        g.fillRect(0,0,edge,edge);

        if (symbol !=' ') {
            g.setColor(symbolcolor);
            g.setFont(f);
            FontMetrics fm = getFontMetrics(f);
            int w = fm.charWidth(id);
            int h = fm.getHeight();
            g.drawString(""+symbol,((edge-w)/2),((edge+h/2)/2));
        }

        if (hadcrash) {
            g.setColor(Color.red); 
            g.fillOval(staintx,stainty,staintd,staintd);
        }

        if (users > 1 || (users > 0 && isblocked)) {
            g.setColor(Color.red); 
            g.fillRect(0,0,edge,edge);
        }

        if (users < 0) {
            g.setColor(Color.yellow); 
            g.fillRect(0,0,edge,edge);
        }

        if (users > 0) {
            g.setColor(c);
            int deltax = xoffset*(edge/2);
            int deltay = yoffset*(edge/2);
            g.fillOval(3+deltax,3+deltay,edge-7,edge-7);
            if (id != ' ') {
                if (light(c)) 
                    g.setColor(Color.black); 
                else 
                    g.setColor(Color.white); 
                g.setFont(f);
                FontMetrics fm = getFontMetrics(f);
                int w = fm.charWidth(id);
                int h = fm.getHeight();
                g.drawString(""+id,((edge-w)/2)+deltax,((edge+h/2)/2)+deltay);
            }
        }

        if (isstartpos) {
            g.setColor(startposopen ? opencolor : closedcolor);
            g.drawRect(1,1,edge-2,edge-2);

        }

        if (barrieractive) {
            g.setColor(barriercol);
            if (barriertop) 
                g.fillRect(0,0,edge,2);
            else
                g.fillRect(0,edge-2,edge,2);
        }

    }

}

@SuppressWarnings("serial")
class Ground extends JPanel {

    private final int n = 11; 
    private final int m = 12;
    private Cars cars;

    private CarField[][] area;

    // Checking for bridge overload is done in this class
    // Initial values must correspond to control panel defaults
    private int onbridge = 0;
    private boolean checkBridge = false;
    private int limit = 3;
 
    public Ground(Cars  c) {
        cars = c;
        area = new CarField [n] [m];
        setLayout(new GridLayout(n,m));
        setBorder(BorderFactory.createLineBorder(new Color(180,180,180)));
        
        for (int i = 0; i < n ; i++)
            for (int j = 0; j < m; j++) {
                area [i][j] = new CarField(new Pos(i,j),cars);
                add (area [i][j]);
            }

        // Define Hut area
        for (int i = 2; i <= 7; i++)
            for (int j = 1; j <= 3; j++) 
                if (i < 4 || i > 5 || j < 2)
                    area[i][j].setBlocked();


        // Define Shed area
        for (int i = 10; i <= 10; i++)
            for (int j = 0; j < 2; j++) 
                area[i][j].setBlocked();

        // Set start/gate positions
        for (int i = 0; i < 9; i++) {
            Pos startpos = cars.getStartPos(i);
            area[startpos.row][startpos.col].setStartPos(i,false);
        }

        // Set barrier fields (both adjacent fields)
        for (int i = 0; i < 9; i++) {
            area[4][i+3].setBarrierPos(false);
            area[5][i+3].setBarrierPos(true);
         }

    }

    boolean isOnBridge(Pos pos) {
    	return false;
        // return pos.col > 0 && pos.col < 4 && pos.row > 8 && pos.row < 11;
    }
    
   // The following methods are normally called through the event-thread.
   // May also be called directly by Car no. 0, but then for private fields.
   // Hence no synchronization is necessary
    
    public void mark(Pos p, Color c, int no) {
        CarField f = area[p.row][p.col];
        f.enter(0,0,c,(char) (no + (int) '0'));
        if (isOnBridge(p)) onbridge++;
        bridgeCheck();
        // repaint();
    }

    public void mark(Pos p, Pos q, Color c, int no) {
        CarField fp = area[p.row][p.col];
        CarField fq = area[q.row][q.col];
        char marker = (char) (no + (int) '0');
        fp.enter(q.col-p.col,q.row-p.row,c,marker);
        fq.enter(p.col-q.col,p.row-q.row,c,marker);
        if (isOnBridge(p) || isOnBridge(q)) onbridge++;
        bridgeCheck();
        // repaint();
   }

    public void clear(Pos p) {
        CarField f = area[p.row][p.col];
        f.exit();
        if (isOnBridge(p)) onbridge--;
        // repaint();
    }

    public void clear(Pos p, Pos q) {
        CarField fp = area[p.row][p.col];
        CarField fq = area[q.row][q.col];
        fp.exit();
        fq.exit();
        if (isOnBridge(p) || isOnBridge(q)) onbridge--;
        // repaint();
    }

    // The following internal graphical methods are only called via the event-thread
        
    void setOpen(int no) {
        Pos p = cars.getStartPos(no);
        area[p.row][p.col].setStartPos(true);
        // repaint();
    }

    void setClosed(int no) {
        Pos p = cars.getStartPos(no);
        area[p.row][p.col].setStartPos(false);
        // repaint();
    }

    void showBarrier(boolean active) {
        for (int no = 0; no < 9; no++) {
            Pos p = cars.getBarrierPos(no);
            area[p.row][p.col].showBarrier(active);
            area[p.row + (no < 5 ? 1 : -1)][p.col].showBarrier(active);
        }
        // repaint();
    }    

    void setKeep(boolean keep) {
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) 
                area[i][j].setKeep(keep);
    }

    void setSlow(boolean slowdown) {
        for (int i = 0; i < n-1; i++) 
            area[i][0].setSlow(slowdown);
        // repaint();
    }
    
    void showBridge(boolean active) {
        for (int i = 9; i < n; i++) 
            for (int j = 1; j < 4; j++) 
                area[i][j].setBridge(active);
        checkBridge = active;
        bridgeCheck();
    }
    
    void setLimit(int max) {
        limit = max;
        bridgeCheck();
     }
    
    void bridgeCheck() {
        if (checkBridge) { 
            CarField.setOverload(onbridge > limit);
            for (int i = 9; i < n; i++) 
                for (int j = 1; j < 4; j++) 
                    area[i][j].repaint();
        }
    }
    
}

@SuppressWarnings("serial")
class ControlPanel extends JPanel {

    private int test_count = 20;

    Cars cars;

    JPanel button_panel = new JPanel();

    JCheckBox keep = new JCheckBox("Keep crash", false);
    JCheckBox slow = new JCheckBox("Slowdown", false);

    JPanel barrier_panel = new JPanel();

    JCheckBox barrier_on = new JCheckBox("Active", false);
 
    // JPanel bridge_panel = new JPanel();   // Combined with barrier panel

    // JCheckBox bridge_on = new JCheckBox("Show", false);
 
    JLabel     threshold_label = new JLabel("Threshold:");
    JComboBox  barrier_threshold = new JComboBox();

    int currentLimit = 3;
    JLabel     limit_label  = new JLabel("Bridge limit:");
    JComboBox  bridge_limit = new JComboBox();

    JPanel test_panel = new JPanel();

    JComboBox test_choice = new JComboBox();

    public ControlPanel (Cars c) {
        cars = c;

        Insets bmargin = new Insets(2,5,2,5);
        
        setLayout( new GridLayout(3,1) );
        
        JButton start_all = new JButton("Start all");
        start_all.setMargin(bmargin);
        start_all.addActionListener( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                cars.startAll();
            }
        });

        JButton stop_all = new JButton("Stop all");
        stop_all.setMargin(bmargin);
        stop_all.addActionListener( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                cars.stopAll();
            }
        });

        keep.addItemListener( new ItemListener () {
            public void itemStateChanged(ItemEvent e) {
                cars.setKeep(keep.isSelected());
            }
        });

        slow.addItemListener( new ItemListener () {
            public void itemStateChanged(ItemEvent e) {
                cars.setSlow(slow.isSelected());
            }
        });

        button_panel.add(start_all);
        button_panel.add(stop_all);
        button_panel.add(new JLabel("  "));
        button_panel.add(keep);
        button_panel.add(new JLabel(""));
        button_panel.add(slow);

        add(button_panel);

        barrier_panel.add(new JLabel("Barrier:"));

        barrier_panel.add(barrier_on);

        barrier_on.addItemListener( new ItemListener () {
            public void itemStateChanged(ItemEvent e) {
                cars.barrierClicked(barrier_on.isSelected());
            }
        });
 
        barrier_panel.add(new JLabel("     "));


        for (int i = 0; i <= 7; i++) 
            barrier_threshold.addItem(""+(i+2));
        barrier_threshold.setSelectedIndex(7);

        barrier_threshold.addActionListener( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                int i = barrier_threshold.getSelectedIndex(); 
                cars.barrierSet(2+i);
            }
        });

        barrier_panel.add(threshold_label);
        barrier_panel.add(barrier_threshold);


        /*
        barrier_panel.add(new JLabel("Bridge:"));
        barrier_panel.add(bridge_on);
 
        bridge_on.addItemListener( new ItemListener () {
            public void itemStateChanged(ItemEvent e) {
                cars.showBridge(bridge_on.isSelected());
            }
        });
*/


       for (int i = 0; i < 6; i++) 
            bridge_limit.addItem(""+(i+1));
        bridge_limit.setSelectedIndex(currentLimit-1);

        bridge_limit.addActionListener( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                int i = bridge_limit.getSelectedIndex();
                System.out.println("Select event " + i);
                // Ignore internal changes
                if (i+1 != currentLimit) {
                    System.out.println("Calling setLimit");
                    cars.setLimit(i+1,null);
                }
            }
        });
        
        /* NO BRIDGE CONTROL IN THIS VERSION */
        // barrier_panel.add(limit_label);
        // barrier_panel.add(bridge_limit);
        
       
        add(barrier_panel);

                 
        for (int i = 0; i < test_count; i++) 
            test_choice.addItem(""+i);

        JButton run_test = new JButton("Run test no.");
        run_test.setMargin(bmargin);
        run_test.addActionListener( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                int i = test_choice.getSelectedIndex();
                cars.runTest(i);
            }
        });

        test_panel.add(run_test);
        test_panel.add(test_choice);
        add(test_panel);

    }

    public void disableBridge() {
        limit_label.setEnabled(false);
        bridge_limit.setEnabled(false);
   }
    
    public void disableLimit() {
        bridge_limit.setEnabled(false);
    }

    public void enableLimit(int k) {
        currentLimit = k;
        if (k - 1 != bridge_limit.getSelectedIndex()) bridge_limit.setSelectedIndex(k - 1 );
        bridge_limit.setEnabled(true);
    }

}


@SuppressWarnings({ "serial" })
public class Cars extends JFrame implements CarDisplayI {

    static final int width      =   30;       // Width of text area
    static final int minhistory =   50;       // Min no. of lines kept

    // Model
    private boolean[] gateopen = new boolean[9];
    private Pos[] startpos     = new Pos[9];
    private Pos[] barrierpos   = new Pos[9];

    private boolean barrieractive = false;
    private boolean bridgepresent = false;
    private volatile boolean slowdown = false;     // Flag read concurrently by isSlow()

    private CarControlI ctr;
    private CarTestWrapper testwrap;
    private Thread test;

    private Ground gnd;
    private JPanel gp;
    private ControlPanel cp;
    private JTextArea txt;
    private JScrollPane log;

    class LinePrinter implements Runnable {
        String m;

        public LinePrinter(String line) {
            m = line;
        }

        public void run() {
            int lines = txt.getLineCount();
            if (lines > 2*minhistory) {
                try {
                    int cutpos = txt.getLineStartOffset(lines/2);
                    txt.replaceRange("",0,cutpos);
                } catch (Exception e) {}
            }
           txt.append(m+"\n");
        }
    }

    // Variables used during limit setting
    private SetLimitThread limitThread; 
    private Semaphore      limitDone;
    private int            limitValue;
    
    /*
     * Thread to carry out change of bridge limit since
     * it may be blocked by CarControl
     */
    class SetLimitThread extends Thread {
        int newmax;

        public SetLimitThread(int newmax) {
            this.newmax =  newmax;

        }

        public void run() {
            // ctr.setLimit(newmax);
            
            System.out.println("SetLimit returned");
            EventQueue.invokeLater(new Runnable() {
                public void run() { endSetLimit(); }}
            );
            
        }
    }

    public Cars() {

        startpos[0] = new Pos(4,2);
        for (int no = 1; no < 9; no++) 
            startpos[no] = new Pos(no < 5 ? 3 : 6 , 3+no);

        for (int no = 0; no < 9; no++) 
            barrierpos[no] = new Pos(no < 5 ? 4 : 5 , 3+no);

        for (int no = 0; no < 9; no++) { gateopen[no] = false; }

        // Build GUI
        gnd = new Ground(this);
        gp  = new JPanel();
        cp =  new ControlPanel(this); 
        txt = new JTextArea("",8,width);
        txt.setEditable(false);    
        log = new JScrollPane(txt);
        log.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setTitle("Cars");
        setBackground(new Color(200,200,200));

        gp.setLayout(new FlowLayout(FlowLayout.CENTER));
        gp.add(gnd);

        setLayout(new BorderLayout());
        add("North",gp);
        add("Center",cp);
        add("South",log);

        addWindowListener(new WindowAdapter () {
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
        });

        // Add control
        testwrap = new CarTestWrapper(this);

        ctr = new CarControl(this);
        
        // bridgepresent = ctr.hasBridge();
        gnd.showBridge(bridgepresent);
        if (! bridgepresent) cp.disableBridge();
        
        pack();
        setBounds(100,100,getWidth(),getHeight());
        setVisible(true);

    }

    public static void main(String [] args) {
        new Cars();
    }

    // High-level event handling -- to be called by gui thread only
    // The test thread activates these through the event queue via the
    // CarTestWrapper

    public void barrierOn() {
        gnd.showBarrier(true); 
        barrieractive = true;
        ctr.barrierOn();
    }

    public void barrierOff() {
        ctr.barrierOff();
        gnd.showBarrier(false);
        barrieractive = false;
    }

    public void barrierSet(int k) {
        ctr.barrierSet(k);
    }

    void barrierClicked(boolean on) {
        if (on) barrierOn(); else barrierOff();
    }

    public void setSlow(final boolean slowdown) {
        this.slowdown = slowdown;
        gnd.setSlow(slowdown);
    }

    public void startAll() {
        int first = barrieractive ? 0 : 1; 
        // Should not start no. 0 if no barrier
        for (int no = first; no < 9; no++) 
            startCar(no);
    }

    public void stopAll() {
        for (int no = 0; no < 9; no++) 
            stopCar(no);
    }

    void runTest(int i) {
        if (test!=null && test.isAlive()) {
            println("Test already running");
            return;
        }
        println("Run of test "+i);
        test = new CarTest(testwrap,i);
        test.start();
    }

    public void setKeep(final boolean keep) {
        gnd.setKeep(keep);
    }

    void showBridge(boolean active) {
        gnd.showBridge(active);
     }
    
    void startFieldClick(int no) {
        if (gateopen[no]) 
            stopCar(no);
        else 
            startCar(no);
    }

    public void startCar(final int no) {
        if (!gateopen[no]) {
            gnd.setOpen(no);
            gateopen[no] = true;
            ctr.startCar(no);
        }
    }

    public void stopCar(final int no) {
        if (gateopen[no]) {
            ctr.stopCar(no);
            gnd.setClosed(no);
            gateopen[no] = false;
        }
    }

    public void setSpeed(int no, int speed) {
        ctr.setSpeed(no,speed);
    }

    public void setVariation(int no, int var) {
        ctr.setVariation(no,var);
    }

    public void removeCar(int no) {
        ctr.removeCar(no);
    }

    public void restoreCar(int no) {
        ctr.restoreCar(no);
    }

    void setLimit(int max, Semaphore done) {

        if (! bridgepresent) {
            println("ERROR: No bridge at this playground!");
            if (done != null) done.V();
            return;
        }
        
        if (max < 1 || max > 6) {
            println("ERROR: Illegal limit value");
            if (done != null) done.V();
            return;
        }

        if (limitThread != null ) {
            println("WARNING: Limit setting already in progress");
            if (done != null) done.V();
            return;
        }

        cp.disableLimit();
        // Hold values for post-processing
        limitValue = max;
        limitDone = done;
        limitThread = new SetLimitThread(max);
        limitThread.start();
    }
    
    // Called when SetLimitThread has ended
    void endSetLimit() {

        System.out.println("endSetLimit start");
        if (limitDone != null ) limitDone.V();
        
        gnd.setLimit(limitValue);
        cp.enableLimit(limitValue);
        limitThread = null;
        limitDone = null;
        System.out.println("endSetLimit end");
    }

    // Implementation of CarDisplayI 
    // Mark and clear requests for car no. 0 are processed directly in order not
    // to fill the event queue (with risk of transiently inconsistent graphics)

    // Mark area at position p using color c and number no.
    public void mark(final Pos p, final Color c, final int no){
        if (no != 0)
            EventQueue.invokeLater(new Runnable() {
                public void run() { gnd.mark(p,c,no); }}
            );
        else
            gnd.mark(p,c,no);
    }

    // Mark area between adjacent positions p and q 
    public void mark(final Pos p, final Pos q, final Color c, final int no){
        if (no != 0)
            EventQueue.invokeLater(new Runnable() {
                public void run() { gnd.mark(p,q,c,no); }}
            );
        else
            gnd.mark(p,q,c,no);
    }

    // Clear area at position p
   public void clear(final Pos p){
       if (p.col < 2 || p.col > 3 || p.row < 4 || p.row > 5) 
           EventQueue.invokeLater(new Runnable() {
               public void run() { gnd.clear(p); }}
           );
       else 
           // In toddlers' yard - call directly
    	   gnd.clear(p); 
    }

    // Clear area between adjacent positions p and q.
    public void clear(final Pos p, final Pos q){
        if (p.col < 2 || p.col > 3 || p.row < 4 || p.row > 5) 
            EventQueue.invokeLater(new Runnable() {
                public void run() { gnd.clear(p,q); }}
            );
        else
        	// In toddlers' yard - call directly
            gnd.clear(p,q); 
     }

    public Pos getStartPos(int no) {      // Identify startposition of Car no.
        return startpos[no];
    }

    public Pos getBarrierPos(int no) {    // Identify pos. at barrier line
        return barrierpos[no];
    }
    
    public Pos nextPos(int no, Pos pos) {
        // Fixed tracks --- not to be modified.
        int mycol = 3+no;
        Pos nxt = pos.copy();

        if (no==0) {   // No. 0 is special, running its own tiny track
            if (pos.row==5 && pos.col > 2)         nxt.col--; 
            if (pos.col==2 && pos.row > 4)         nxt.row--;
            if (pos.row==4 && pos.col < 3)         nxt.col++; 
            if (pos.col==3 && pos.row < 5)         nxt.row++; 
        }

         
        else if (no < 5) {  // Car going around clockwise (to the right)
            int myrow = (no < 3? 8 : 9);
            if (pos.row==myrow && pos.col > 0)     nxt.col--; 
            if (pos.col==0 && pos.row > 1)         nxt.row--;
            if (pos.row==1 && pos.col < mycol)     nxt.col++;
            if (pos.col==mycol && pos.row < myrow) nxt.row++;
        }

        else if (no >= 5) {  // Car going around anti-clockwise (to the left)
            if (pos.row==0  && pos.col > 0)        nxt.col--; 
            if (pos.col==0  && pos.row < 9)        nxt.row++;
            if (pos.row==9  && pos.col < 2)        nxt.col++; 
            // Round the corner
            if (pos.row==9  && pos.col == 2)       nxt.row++;
            if (pos.row==10  && pos.col < mycol)   nxt.col++; 
            if (pos.col==mycol && pos.row > 0)     nxt.row--; 
        }

        return nxt;
    }

    public void println(String m) {
        // Print (error) message on screen 
        Runnable job = new LinePrinter(m);
        EventQueue.invokeLater(job);
    }

    public boolean isSlow(Pos pos) { 
        return pos.col== 0 && slowdown;
    }

}


/**
 * For those methods of the CarTestI interface which access the gui state,
 * this class wraps them to similar events to be processed by the 
 * gui thread.
 * For other methods, the event handler methods are called directly.
 */
class CarTestWrapper implements CarTestingI {

    Cars cars;
    
    public CarTestWrapper(Cars cars) {
        this.cars = cars;
    }
    
    public void startCar(final int no) {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.startCar(no); }}
        );
    }

    public void stopCar(final int no) {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.stopCar(no); }}
        );
    }

    public void startAll() {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.startAll(); }}
        );
    }

    public void stopAll() {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.stopAll(); }}
        );
    }


    public void barrierOn() {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.barrierOn(); }}
        );
    }

    public void barrierOff() {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.barrierOff(); }}
        );
    }

     public void barrierSet(final int k) {
         EventQueue.invokeLater(new Runnable() {
             public void run() { cars.barrierSet(k); }}
         );
     }

     
     public void setSlow(final boolean slowdown) {
         EventQueue.invokeLater(new Runnable() {
             public void run() { cars.setSlow(slowdown); }}
         );
     }
 
     public void removeCar(final int no) {
         EventQueue.invokeLater(new Runnable() {
             public void run() { cars.removeCar(no); }}
         );
      }

     public void restoreCar(final int no) {
         EventQueue.invokeLater(new Runnable() {
             public void run() { cars.restoreCar(no); }}
         );
     }
     
    public void setSpeed(final int no, final int speed) {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.setSpeed(no, speed); }}
        );
    }

    public void setVariation(final int no, final int var) {
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.setVariation(no, var); }}
        );
    }

    // This should wait until limit change carried out
    // For this, a one-time semaphore is used (as simple Future)
    public void setLimit(final int k) {
        final Semaphore done = new Semaphore(0);
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.setLimit(k, done); }}
        );
        try {
            done.P();
        } catch (InterruptedException e) {}

    }
    
    // Println already wrapped in Cars
    public void println(final String message) {
        cars.println(message);
    }

 }
















