package unblonded.packets.util;

public class Color {
    private float red;
    private float green;
    private float blue;
    private float alpha;

    public static final Color WHITE = new Color(1.0f, 1.0f, 1.0f);
    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f);
    public static final Color RED = new Color(1.0f, 0.0f, 0.0f);
    public static final Color GREEN = new Color(0.0f, 1.0f, 0.0f);
    public static final Color BLUE = new Color(0.0f, 0.0f, 1.0f);

    public Color() {
        this.red = (float)Math.random();
        this.green = (float)Math.random();
        this.blue = (float)Math.random();
        this.alpha = 1.0f;
    }

    public Color(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() == 6) {
            this.red = Integer.parseInt(hex.substring(0, 2), 16) / 255.0f;
            this.green = Integer.parseInt(hex.substring(2, 4), 16) / 255.0f;
            this.blue = Integer.parseInt(hex.substring(4, 6), 16) / 255.0f;
            this.alpha = 1.0f;
        } else {
            throw new IllegalArgumentException("Invalid hex color format");
        }
    }

    public Color(int r, int g, int b) {
        this.red = r / 255.0f;
        this.green = g / 255.0f;
        this.blue = b / 255.0f;
        this.alpha = 1.0f;
    }

    public Color(float r, float g, float b) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = 1.0f;
    }

    public Color(float r, float g, float b, float a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    public float R() { return red; }
    public float G() { return green; }
    public float B() { return blue; }
    public float A() { return alpha; }

    public int asHex() {
        int a = Math.round(alpha * 255);
        int r = Math.round(red * 255);
        int g = Math.round(green * 255);
        int b = Math.round(blue * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }


    @Override
    public String toString() {
        return "Color{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", alpha=" + alpha +
                '}';
    }
}
