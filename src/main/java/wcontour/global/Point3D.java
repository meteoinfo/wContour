package wcontour.global;

public class Point3D extends PointD{
    public double Z;
    public double M;

    /**
     * Construction
     */
    public Point3D() {}

    /**
     * Construction
     * @param x X
     * @param y Y
     * @param z Z
     */
    public Point3D(double x, double y, double z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    /**
     * Construction
     * @param x X
     * @param y Y
     * @param z Z
     * @param m M
     */
    public Point3D(double x, double y, double z, double m) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.M = m;
    }

    /**
     * Calculate distance to a point
     * @param p The point
     * @return Distance
     */
    public double distance(Point3D p) {
        return Math.sqrt((X - p.X) * (X - p.X) + (Y - p.Y) * (Y - p.Y) + (Z - p.Z) * (Z - p.Z));
    }

    /**
     * Dot product to a point
     * @param p The point
     * @return Dot product result
     */
    public double dot(Point3D p) {
        return X * p.X + Y * p.Y + Z * p.Z;
    }

    /**
     * Add to a point
     * @param p The point
     * @return Add result
     */
    public Point3D add(Point3D p) {
        return new Point3D(X + p.X, Y + p.Y, Z + p.Z);
    }

    /**
     * Subtract to a point
     * @param p The point
     * @return Subtract result
     */
    public Point3D sub(Point3D p) {
        return new Point3D(X - p.X, Y - p.Y, Z - p.Z);
    }

    /**
     * Multiply
     * @param v The value
     * @return Multiply result
     */
    public Point3D mul(double v) {
        return new Point3D(X * v, Y * v, Z * v);
    }

    /**
     * Divide
     * @param v The value
     * @return Divide result
     */
    public Point3D div(double v) {
        return new Point3D(X / v, Y / v, Z / v);
    }

    @Override
    public Object clone() {
        return new Point3D(X, Y, Z, M);
    }
}
