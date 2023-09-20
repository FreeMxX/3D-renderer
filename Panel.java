/*
This software is a java recreation that was inspired by the 3D 
rendering software created by Javidx9. Below is the
license found in his original software:

License
~~~~~~~
One Lone Coder Console Game Engine  Copyright (C) 2018  Javidx9
This program comes with ABSOLUTELY NO WARRANTY.
This is free software, and you are welcome to redistribute it
under certain conditions; See license for details.
Original works located at:
https://www.github.com/onelonecoder
https://www.onelonecoder.com
https://www.youtube.com/javidx9
GNU GPLv3
https://github.com/OneLoneCoder/videos/blob/master

Background
~~~~~~~~~~
3D rendering software is available on github for specifically 
java language is very limited. This software is a basic 3D
renderer based on and inpired by the work of Javidx9.
This videos linked in his code (C++) as well as 
the inspiration for this project are linked below.

Video
~~~~~
https://youtu.be/ih20l3pJoeU
https://youtu.be/XgMWc6LumG4
https://youtu.be/HXSuNxpCzdM
https://youtu.be/nBzCS-Y0FcY

Author
~~~~~~
FreeMxX

Last Updated: 09/19/2023
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

public class Panel extends JPanel implements Runnable {

    //Initial Variables
    final float WIDTH;
    final float HEIGHT;
    final Dimension SCREEN_SIZE;

    //Initial Classes
	static Panel gp;
    static JFrame jf;
	Graphics graphics;
	Image image;
    Thread thread;
    V3DMath v3dMath;
    Vec3D vec3D;
    Sphere ball;
    Cube cube;
	
    //Main Method: initialize an instance of the class Panel 
    //as well as object types to begin rendering
    public static void main(String[] args) {
        gp = new Panel();
        int num = 25;
        for (int i = 0; i < num; i++) {
            Sphere s = new Sphere(gp.vec3D, (float)Math.random()*num, (float)Math.random()*num, (float)Math.random()*num, (float)Math.random()*5, 1);
        }
    }
    //Panel settings
    public Panel(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        SCREEN_SIZE = new Dimension((int)WIDTH, (int)HEIGHT);
        jf = new JFrame();
        this.setFocusable(true);
        this.addKeyListener(new ActionListener());
        this.addMouseListener(new MouseListener());
        this.setPreferredSize(SCREEN_SIZE);
        v3dMath = new V3DMath(this);
        vec3D = new Vec3D(v3dMath, this);
        thread = new Thread(this);
        thread.start();
    
        jf.add(this);
		jf.setBackground(Color.BLACK);
        jf.setResizable(false);
        jf.setTitle("3D Engine");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
        jf.setLocationRelativeTo(null); 
    }
    //Constructer with no parameters
     public Panel() {
        this(1080, 720);
    }
	public void paint(Graphics g) {
        image = createImage(getWidth(), getHeight());
        graphics = image.getGraphics();
        draw((Graphics2D)graphics);
        g.drawImage(image,0,0,this);
    }
    //updates and draws to the screen
    public void draw(Graphics2D g2) {
		g2.setColor(Color.WHITE);       
        v3dMath.update(g2);
        vec3D.transformTriangles(g2, Object.getAllVectors());
        v3dMath.mouseMoved(g2);
	}
    //sorts triangles from distance of center point to the camera vector
    //algorithm is not perfect so a depthbuffer is needed in future versions
    public void updateSort() {
        Collections.sort(Object.triList, new Comparator<float[][]>() {
            public int compare(float[][] t1, float[][] t2) {
                int z1 = (int)(200000 *(Math.abs(Math.sqrt(Math.pow((t1[0][2] + t1[1][2] + t1[2][2]) / 3.0f - v3dMath.vCamera[2], 2) + Math.pow((t1[0][0] + t1[1][0] + t1[2][0]) / 3.0f - v3dMath.vCamera[0], 2) + Math.pow((t1[0][1] + t1[1][1] + t1[2][1]) / 3.0f - v3dMath.vCamera[1], 2)))));
                int z2 = (int)(200000 *(Math.abs(Math.sqrt(Math.pow((t2[0][2] + t2[1][2] + t2[2][2]) / 3.0f - v3dMath.vCamera[2], 2) + Math.pow((t2[0][0] + t2[1][0] + t2[2][0]) / 3.0f - v3dMath.vCamera[0], 2) + Math.pow((t2[0][1] + t2[1][1] + t2[2][1]) / 3.0f - v3dMath.vCamera[1], 2)))));
                if (z1 == z2)
                return 10;
                else
                return Integer.valueOf(z1).compareTo(z2);
            }
        });
    }
    //game loop
    public void run() {
		long lastTime = System.nanoTime();
        double amountOfTicks = 240;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while(thread != null) {
            long now = System.nanoTime();
            delta +=(now - lastTime)/ns;
            lastTime = now;
            if(delta >=1) {
                repaint();
                delta = 0;
            }
    	}
	}
    //Action Listener and Mouse Listener for inputs
    private class ActionListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
			v3dMath.keyPressed(e);
        }
        public void keyReleased(KeyEvent e) {
            v3dMath.keyReleased(e);
        }
    }
    private class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            
        }
    }
    // X and Y mouse position for rotation matrices
    public static int getWindowXPosition() {
        Point point = jf.getLocationOnScreen();
        int frameX = (int)point.getX();
        return frameX;
    }
    public static int getWindowYPosition() {
        Point point = jf.getLocationOnScreen();
        int frameY = (int)point.getY();
        return frameY;
    }
    //makes the cursor disappear when within the window
    //press escape to toggle
    public static void setBlankCursor() {
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        jf.getContentPane().setCursor(blankCursor);
    }
}
