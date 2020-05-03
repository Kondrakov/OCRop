import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PathMode implements ListIterator<Point> {

    public static List<Point> standardDeltaPoints;
    static {
        standardDeltaPoints = new ArrayList<>();
        standardDeltaPoints.add(new Point(1, 0));
        standardDeltaPoints.add(new Point(0, 1));
        standardDeltaPoints.add(new Point(-1, 0));
        standardDeltaPoints.add(new Point(0, -1));
    }

    public static final String CLOCKWISE = "clockwise";
    public static final String COUNTER_CLOCKWISE = "counterClockwise";

    public String getRoundMode() {
        return roundMode;
    }

    public void setRoundMode(String roundMode) {
        this.roundMode = roundMode;
    }

    private String roundMode;

    public Point getCurrentDirection() {
        return currentDirection;
    }

    private Point currentDirection;

    private Point startDirection;

    private int currentDirectionIter;

    public PathMode(String roundMode, Point startDirection) {
        this.roundMode = roundMode;
        this.startDirection = startDirection;

        this.resetRotation();
    }

    public void rotate() {
        if (CLOCKWISE.equals(this.roundMode)) {
            this.next();
        } else if (COUNTER_CLOCKWISE.equals(this.roundMode)) {
            this.previous();
        }
    }

    public void resetRotation() {
        this.currentDirection = this.startDirection;
        for (int i = 0; i < standardDeltaPoints.size(); i++) {
            if (standardDeltaPoints.get(i).getX() == startDirection.getX()
                    && standardDeltaPoints.get(i).getY() == startDirection.getY()) {
                this.currentDirectionIter = i;
            }
        }
    }

    //ListIterator impl.:
    @Override
    public boolean hasNext() {
        if (standardDeltaPoints.size() <= this.currentDirectionIter + 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Point next() {
        if (this.hasNext()) {
            this.currentDirection = standardDeltaPoints.get(++this.currentDirectionIter);
        } else {
            this.currentDirectionIter = 0;
            this.currentDirection = standardDeltaPoints.get(this.currentDirectionIter);
        }
        return this.currentDirection;
    }

    @Override
    public boolean hasPrevious() {
        if (this.currentDirectionIter - 1 < 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Point previous() {
        if (this.hasPrevious()) {
            this.currentDirection = standardDeltaPoints.get(--this.currentDirectionIter);
        } else {
            this.currentDirectionIter = standardDeltaPoints.size() - 1;
            this.currentDirection = standardDeltaPoints.get(this.currentDirectionIter);
        }
        return this.currentDirection;
    }

    @Override
    public int nextIndex() {
        return this.currentDirectionIter + 1;
    }

    @Override
    public int previousIndex() {
        return this.currentDirectionIter - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove unsupported");
    }

    @Override
    public void set(Point integer) {
        throw new UnsupportedOperationException("set unsupported");
    }

    @Override
    public void add(Point integer) {
        throw new UnsupportedOperationException("add unsupported");
    }
}
