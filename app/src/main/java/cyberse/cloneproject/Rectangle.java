package cyberse.cloneproject;


//top-left rectangle
public class Rectangle {
    public int left;
    public int right;
    public int top;
    public int bottom;

    public int getWidth() {
        return Math.abs(right - left);
    }

    public int getHeight() {
        return Math.abs(top - bottom);
    }
}
