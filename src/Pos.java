//Implementation of position class 
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU  Fall 2011

//Hans Henrik Løvengreen   Oct 3, 2011

public class Pos {

    public int row;       // Note: public
    public int col;

    public Pos(int i, int j) { row = i;  col = j; }

    public static boolean equal(Pos p1, Pos p2) {
        if (p1 == null || p2 == null) return false;
        return (p1.row == p2.row) && (p1.col == p2.col);
    }

    public Pos copy() {
        return new Pos(row, col);
    }

    public boolean equals(Object p) {
    	return (p instanceof Pos && Pos.equal(this,(Pos) p));
    }

    public int hashCode() { 
    	// Borrowed from java.awt.geom.Point2D
    	long bits = java.lang.Double.doubleToLongBits(row);
    	bits ^= java.lang.Double.doubleToLongBits(col) * 31;
    	return (((int) bits) ^ ((int) (bits >> 32)));
    }
    
   public String toString() {
        return "("+row+","+col+")";
    }

}

