import java.awt.*;
import java.awt.event.*;

public class V3DMath {
    
    //Initial class
    Panel m;

    //Initial floating point variables
    float fNear = 0.1f;
    float fFar = 1000.0f;
    float fFov = 82.0f;
    float tanA = (float)(fFov * 0.5f / 180.0f * 3.14159f);
    float fFovRad = (float)(1 / Math.tan(tanA));
    float fAspectRatio;
    float sensitivity = 0.025f;
    float speed = 0.5f;
    float fYawX = 0;
    float fYawY = 0;

    //3D matrix to hold clipped triangles
    float[][][] clipped = new float[4][4][2];

    //Initial matrices
    float m4x4[][] = new float[4][4];
    float cameraRotX[][] = new float[4][4];
    float cameraRotY[][] = new float[4][4];
    float worldRotation[][] = new float[4][4];
    float cameraM[][] = new float[4][4];
    float cameraView[][] = new float[4][4];

    //Initial Vectors
    float[] vCamera = {0f, -2f, -4f, 1};
    float[] vLightDirection = {-300,-500,-300, 1};
    float[] plainNear1 = {0, 0, fNear, 1};
    float[] plainNear2 = {0,0,1, 1};
	float[] lookDirection;
	float[] lookSideways;
    float[] lookForward;

    //boolean variables for keyboard input
	boolean up, down, left, right, forward, backward, turnRight, turnLeft;
	boolean paused = false;
    boolean coordinates = false;

    //sets the number of colors and the darkest color of triangles
    //triangles are rendered in grayscale... texture in a future version
    int numOfColors = 255;
    int darkestColor = 25;

    public V3DMath(Panel m) {
        this.m = m;
        fAspectRatio = (float)m.HEIGHT / (float)m.WIDTH;
        createMatrix4x4();
    }
    //initializes the viewing matrix as well as other matrices to 0
    public void createMatrix4x4() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m4x4[i][j] = 0;
                cameraRotX[i][j] = 0;
                cameraRotY[i][j] = 0;
                cameraM[i][j] = 0;
                cameraView[i][j] = 0;            
            }
        }
        m4x4[0][0] = fAspectRatio * fFovRad;
		m4x4[1][1] = fFovRad;
		m4x4[2][2] = fFar / (fFar - fNear);
	    m4x4[3][2] = (-fFar * fNear) / (fFar - fNear);
		m4x4[2][3] = 1.0f;
		m4x4[3][3] = 0.0f;
    }
	public void update(Graphics2D g2) {
        //Initialize initial vectors
		float[] vForward = {0,0,0,1};
		float[] vSideWays = {0,0,0,1};
		float[] vUp = {0,1,0,1};
		float[] vTarget = {0, 0f, 1f,1};
		float[] vSideTarget = {1, 0, 0,1};

        //create rotation matrices
        cameraRotY = makeRotationY(fYawX);
        cameraRotX = makeRotationX(fYawY);
        worldRotation = matrixMult(4, 4, cameraRotX, 4, 4, cameraRotY);
        
        //change movement and look direction with rotation matrices
        lookDirection = vectorMatrixMult(worldRotation, vTarget);
        lookForward = vectorMatrixMult(cameraRotY, vTarget);
        lookSideways = vectorMatrixMult(cameraRotY, vSideTarget);

        //create a matrix for the cameraview
        vTarget = vectorAdd(vCamera, lookDirection);
        cameraM = Matrix_PointAt(vCamera, vTarget, vUp);
        cameraView = matrixInverse(cameraM);

        //Create Vectors for movement
		if (!down && ((forward && right) || (forward && left) || (backward && right) || (backward && left))) {
            vSideWays = vectorMult(lookSideways, speed * 0.78f);
            vForward = vectorMult(lookForward, speed * 0.78f);
        }
        else if (down && ((forward && right) || (forward && left) || (backward && right) || (backward && left))) {
            vSideWays = vectorMult(lookSideways, speed * 0.39f);
            vForward = vectorMult(lookForward, speed * 0.39f);
        }
        else if (!down) {
            vSideWays = vectorMult(lookSideways, speed);
            vForward = vectorMult(lookForward, speed);
        }
        else if (down) {
            vSideWays = vectorMult(lookSideways, speed);
            vForward = vectorMult(lookForward, speed);
        }

        //Movement
        if (right || left) {
            if (right)
            vCamera = vectorAdd(vCamera, vSideWays);
            if (left)
            vCamera = vectorSub(vCamera, vSideWays);
        }
        else {
        }
        if (up || down) {
            if (up)
            vCamera[1] = (vCamera[1] - speed);
            if (down)
            vCamera[1] = (vCamera[1] + speed);
        }
        else {
        }
        if (forward || backward) {
            if (forward)
            vCamera = vectorAdd(vCamera, vForward);
            if (backward)
            vCamera = vectorSub(vCamera, vForward);
        }
        else {
        }
        if (vCamera[1] >= 100) {
            vCamera[1] = (vCamera[1] + 0.1f);
            vCamera[1] = (100f);
        }
        //Draw Coordinates to screen
        //toggle with the E key
        if (coordinates) {
            g2.drawString("X: " + (int)vCamera[0] + "\nY: " + -(int)vCamera[1] + "\nZ: " + (int)vCamera[2],20, 20);
        }
	}
    //matrix mutiplication
    public float[][] matrixMult(int row1, int col1, float[][] m1, int row2, int col2, float[][] m2) {
        int i, k, j;
        if (row2 != col1) {
            System.out.println("\nMultiplication Not Possible");
            return m1;
        }
        float c[][] = new float[row1][col2];
 
        for (i = 0; i < row1; i++) {
            for (j = 0; j < col2; j++) {
                for (k = 0; k < row2; k++)
                    c[i][j] += m1[i][k] * m2[k][j];
            }
        }
        return c;
    }
    //creates the viewing matrix
	public float[][] Matrix_PointAt(float[] pos, float[] target, float[] up) {
		//Calculate new forward direction
		float[] newForward = vectorSub(target, pos);
		newForward = vectorNorm(newForward);

		//Calculate new Up direction
		float[] a = vectorMult(newForward, vectorDP(up, newForward));
		float[] newUp = vectorSub(up, a);
		newUp = vectorNorm(newUp);

		//New Right direction is cross product
		float[] newRight = vectorCP(newUp, newForward);

		//Construct Dimensioning and Translation Matrix	
		float matrix[][] = new float[4][4];
		matrix[0][0] = newRight[0];	    matrix[0][1] = newRight[1];	    matrix[0][2] = newRight[2];	    matrix[0][3] = 0.0f;
		matrix[1][0] = newUp[0];		matrix[1][1] = newUp[1];		matrix[1][2] = newUp[2];		matrix[1][3] = 0.0f;
		matrix[2][0] = newForward[0];	matrix[2][1] = newForward[1];	matrix[2][2] = newForward[2];	matrix[2][3] = 0.0f;
		matrix[3][0] = pos[0];			matrix[3][1] = pos[1];			matrix[3][2] = pos[2];			matrix[3][3] = 1.0f;
		return matrix;
	}
    //multiply a matrix and a vector
    public float[] vectorMatrixMult(float m[][], float[] i) {
        float[] v = {0,0,0,1};
		v[0] = (i[0] * m[0][0] + i[1] * m[1][0] + i[2] * m[2][0] + i[3] * m[3][0]);
		v[1] = (i[0] * m[0][1] + i[1] * m[1][1] + i[2] * m[2][1] + i[3] * m[3][1]);
		v[2] = (i[0] * m[0][2] + i[1] * m[1][2] + i[2] * m[2][2] + i[3] * m[3][2]);
		v[3] = (i[0] * m[0][3] + i[1] * m[1][3] + i[2] * m[2][3] + i[3] * m[3][3]);
		return v;
    }
    //invert a matrix
    public float[][] matrixInverse(float m[][]) {
        float matrix[][] = new float[4][4];
		matrix[0][0] = m[0][0]; matrix[0][1] = m[1][0]; matrix[0][2] = m[2][0]; matrix[0][3] = 0.0f;
		matrix[1][0] = m[0][1]; matrix[1][1] = m[1][1]; matrix[1][2] = m[2][1]; matrix[1][3] = 0.0f;
		matrix[2][0] = m[0][2]; matrix[2][1] = m[1][2]; matrix[2][2] = m[2][2]; matrix[2][3] = 0.0f;
		matrix[3][0] = -(m[3][0] * matrix[0][0] + m[3][1] * matrix[1][0] + m[3][2] * matrix[2][0]);
		matrix[3][1] = -(m[3][0] * matrix[0][1] + m[3][1] * matrix[1][1] + m[3][2] * matrix[2][1]);
		matrix[3][2] = -(m[3][0] * matrix[0][2] + m[3][1] * matrix[1][2] + m[3][2] * matrix[2][2]);
		matrix[3][3] = 1.0f;
		return matrix;
	}
    //creates X rotation matrix
    public float[][] makeRotationX(float fTheta) {
        float rotX[][] = new float[4][4];
        rotX[0][0] = 1;
        rotX[1][1] = (float) Math.cos(fTheta * 0.5f);
        rotX[1][2] = (float) Math.sin(fTheta * 0.5f);
        rotX[2][1] = (float) (Math.sin(fTheta * 0.5f)*-1);
        rotX[2][2] = (float) Math.cos(fTheta * 0.5f);
        rotX[3][3] = 1;
        return rotX;
    }
    //creates Y rotation matrix
    public float[][] makeRotationY(float fAngleRad) {
        float matrix[][] = new float[4][4];
		matrix[0][0] = (float) Math.cos(fAngleRad);
		matrix[0][2] = (float) Math.sin(fAngleRad);
		matrix[2][0] = (float) (-1*Math.sin(fAngleRad));
		matrix[1][1] = 1.0f;
		matrix[2][2] = (float) Math.cos(fAngleRad);
		matrix[3][3] = 1.0f;
		return matrix;
    }
    //creates Z rotation matrix
    public float[][] makeRotationZ(float fTheta) {
        float rotZ[][] = new float[4][4];
        rotZ[0][0] = (float) Math.cos(fTheta);
		rotZ[0][1] = (float)Math.sin(fTheta);
		rotZ[1][0] = (float)Math.sin(fTheta)*-1;
		rotZ[1][1] = (float)Math.cos(fTheta);
		rotZ[2][2] = 1;
		rotZ[3][3] = 1;
        return rotZ;
    }
    //gets the length of a vector
    public float vectorLength(float[] p1) {
		return (float)(Math.sqrt(vectorDP(p1, p1)));
	}
    //multiply a vector and a constant
    public float[] vectorMult(float[] p1, float k) {
        float[] p2 = {0, 0, 0, 1};
        p2[0] = (p1[0]*k);
        p2[1] = (p1[1]*k);
        p2[2] = (p1[2]*k);
        return p2;
    } 
    //add two vectors
    public float[] vectorAdd(float[] p1, float[] p2) {
        float[] p3 = {0, 0, 0, 1};
        p3[0] = (p1[0] + p2[0]);
        p3[1] = (p1[1] + p2[1]);
        p3[2] = (p1[2] + p2[2]);
        return p3;
    }
    //subtract two vectors
    public float[] vectorSub(float[] p1, float[] p2) {
        float[] p3 = {0, 0, 0, 1};
        p3[0] = (p1[0] - p2[0]);
        p3[1] = (p1[1] - p2[1]);
        p3[2] = (p1[2] - p2[2]);
        return p3;
    }
    //normalize a vector
    public float[] vectorNorm(float[] p1) {
        float l = vectorLength(p1);
        p1[0] = (p1[0]/l);
        p1[1] = (p1[1]/l);
        p1[2] = (p1[2]/l);
        return p1;
    }
    //dot product of a vector
    public float vectorDP(float[] p1, float[] p2) {
        float k = p1[0]*p2[0] + p1[1]*p2[1] + p1[2]*p2[2];
        return k;
	}
    //cross product of a vector
    public float[] vectorCP(float[] p1, float[] p2) {
        float[] v = {0, 0, 0, 1};
		v[0] = (p1[1] * p2[2] - p1[2] * p2[1]);
		v[1] = (p1[2] * p2[0] - p1[0] * p2[2]);
		v[2] = (p1[0] * p2[1] - p1[1] * p2[0]);
		return v;
    }
    //return a grayscale color based on dot product 
    //between triangle normal and camera vector
    public Color getColor(float lum) {
        Color c = new Color(darkestColor, darkestColor, darkestColor);

        int brightness = (int)(numOfColors * lum);
        if ((int)(255/numOfColors)*brightness > darkestColor)
		c = new Color((int)(255/numOfColors)*brightness,(int)(255/numOfColors)*brightness,(int)(255/numOfColors)*brightness);

        return c;
    }
    //triangle clipping algorithm
    public int Triangle_ClipAgainstPlane(float[] plane_p, float[] plane_n, float[][] in_tri)
	{
        float[][] out_tri1 = {{0,0,0},{0,0,0},{0,0,0}};
        float[][] out_tri2 = {{0,0,0},{0,0,0},{0,0,0}};

        int numTri = 0;
		
		plane_n = vectorNorm(plane_n);

        float[] p1 = in_tri[0];
        p1 = vectorNorm(p1);
        float d0 = (plane_n[0] * p1[0] + plane_n[1] * p1[1] + plane_n[2] * p1[2] - vectorDP(plane_n, plane_p));
        float[] p2 = in_tri[1];
        p2 = vectorNorm(p2);
        float d1 = (plane_n[0] * p2[0] + plane_n[1] * p2[1] + plane_n[2] * p2[2] - vectorDP(plane_n, plane_p));
        float[] p3 = in_tri[2];
        p3 = vectorNorm(p3);
        float d2 = (plane_n[0] * p3[0] + plane_n[1] * p3[1] + plane_n[2] * p3[2] - vectorDP(plane_n, plane_p));

		float[][] inside_points = new float[4][4];  
        int nInsidePointCount = 0;
		float[][] outside_points = new float[4][4]; 
        int nOutsidePointCount = 0;

		if (d0 >= 0) { inside_points[nInsidePointCount++] = in_tri[0]; }
		else { outside_points[nOutsidePointCount++] = in_tri[0]; }
		if (d1 >= 0) { inside_points[nInsidePointCount++] = in_tri[1]; }
		else { outside_points[nOutsidePointCount++] = in_tri[1]; }
		if (d2 >= 0) { inside_points[nInsidePointCount++] = in_tri[2]; }
		else { outside_points[nOutsidePointCount++] = in_tri[2]; }

		if (nInsidePointCount == 0) {
			numTri = 0;
		}
		else if (nInsidePointCount == 3) {
			out_tri1 = in_tri;
            clipped[0] = out_tri1;

			numTri = 1;
		}
		else if (nInsidePointCount == 1 && nOutsidePointCount == 2) {
			out_tri1[0][0] = inside_points[0][0]; 
			out_tri1[0][1] = inside_points[0][1]; 
			out_tri1[0][2] = inside_points[0][2];

			out_tri1[1][0] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0])[0];
			out_tri1[1][1] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0])[1];
			out_tri1[1][2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0])[2];
			out_tri1[2][0] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[1])[0];
			out_tri1[2][1] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[1])[1];
			out_tri1[2][2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[1])[2];
            clipped[0] = out_tri1;

			numTri = 1;
		}
		else if (nInsidePointCount == 2 && nOutsidePointCount == 1) {
			out_tri1[0][0] = inside_points[0][0];
			out_tri1[0][1] = inside_points[0][1];
			out_tri1[0][2] = inside_points[0][2];
			out_tri1[1][0] = inside_points[1][0];
			out_tri1[1][1] = inside_points[1][1];
			out_tri1[1][2] = inside_points[1][2];
			out_tri1[2][0] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0])[0];
            out_tri1[2][1] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0])[1];
			out_tri1[2][2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0])[2];
			clipped[0] = out_tri1;

			out_tri2[0][0] = inside_points[0][0];
			out_tri2[0][1] = inside_points[0][1];
			out_tri2[0][2] = inside_points[0][2];
			out_tri2[1][0] = inside_points[1][0];
			out_tri2[1][1] = inside_points[1][1];
			out_tri2[1][2] = inside_points[1][2];
			out_tri2[2][0] = Vector_IntersectPlane(plane_p, plane_n, inside_points[1], outside_points[0])[0];
            out_tri2[2][1] = Vector_IntersectPlane(plane_p, plane_n, inside_points[1], outside_points[0])[1];
			out_tri2[2][2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[1], outside_points[0])[2];
			clipped[1] = out_tri2;

			numTri = 2;
		}
        return numTri;
	}
    //algorithm to test if two vectors intersect
    public float[] Vector_IntersectPlane(float[] plane_p, float[] plane_n, float[] lineStart, float[] lineEnd) {
        float t = 0;
		plane_n = vectorNorm(plane_n);
		float plane_d = -1 * vectorDP(plane_n, plane_p);
		float ad = vectorDP(lineStart, plane_n);
		float bd = vectorDP(lineEnd, plane_n);
        if (bd - ad != 0)
		    t = (-1 * plane_d - ad) / (bd - ad);
		float[] lineStartToEnd = vectorSub(lineEnd, lineStart);
		float[] lineToIntersect = vectorMult(lineStartToEnd, t);
		return vectorAdd(lineStart, lineToIntersect);
	}
	public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode()) == (KeyEvent.VK_W)) {
            forward = true;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_S)) {
            backward = true;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_A)) {
            left = true;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_D)) {
            right = true;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_SPACE)) {
            up = true;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_SHIFT)) {
            down = true;
        }
        //stops rotation
        if ((e.getKeyCode()) == (KeyEvent.VK_ESCAPE)) {
            if (paused)
            paused = false;
            else
            paused = true;
        }
        //shows coordinates
        if ((e.getKeyCode()) == (KeyEvent.VK_E)) {
            if (coordinates)
            coordinates = false;
            else
            coordinates = true;
        }
    }
    public void keyReleased(KeyEvent e) {
        if ((e.getKeyCode()) == (KeyEvent.VK_W)) {
            forward = false;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_S)) {
            backward = false;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_A)) {
            left = false;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_D)) {
            right = false;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_SPACE)) {
            up = false;
        }
        if ((e.getKeyCode()) == (KeyEvent.VK_SHIFT)) {
            down = false;
        }
    }
    //mouse movement algorithm
    //sets X and Y yaw based on mouse location and sensitivity
    public void mouseMoved(Graphics2D g2) {
        float fYawYMax = (float)Math.PI - 0.1f;
        float fYawYMin = (float)-Math.PI + 0.1f;
        if (!paused) {
            Panel.setBlankCursor();
            int windowX = Panel.getWindowXPosition();
            int windowY = Panel.getWindowYPosition();
            PointerInfo a = MouseInfo.getPointerInfo();
            Point b = a.getLocation();
            int mouseX = (int) b.getX();
            int mouseY = (int) b.getY();
            if (windowX + m.WIDTH/2 - 1 > mouseX || windowX + m.WIDTH/2 + 1 < mouseX) {
                if (windowX + m.WIDTH/2 - 1 > mouseX)
                fYawX = (windowX + m.WIDTH/2 - 1 - mouseX) * sensitivity;
                else if (windowX + m.WIDTH/2 + 1 < mouseX)
                fYawX = (windowX + m.WIDTH/2 + 1 - mouseX) * sensitivity;
                else
                fYawX = 0;
            }
            else if (windowX + m.WIDTH/2 - 1 < mouseX && windowX + m.WIDTH/2 + 1 > mouseX) {
                fYawX = 0;
            }
            if (windowY + m.WIDTH/2 - 1 > mouseY || windowY + m.WIDTH/2 + 1 < mouseY) {
                if (windowY + m.WIDTH/2 - 1 > mouseY) {
                    fYawY = (windowY + m.WIDTH/2 - 1 - mouseY) * sensitivity;
                    if (fYawY > fYawYMax) {
                        fYawY = fYawYMax;
                    }
                    if (fYawY < fYawYMin) {
                        fYawY = fYawYMin;
                    }
                }
                else if (windowY + m.WIDTH/2 + 1 < mouseY) {
                    fYawY = (windowY + m.WIDTH/2 + 1 - mouseY) * sensitivity;
                    if (fYawY > fYawYMax) {
                        fYawY = fYawYMax;
                    }
                    if (fYawY < fYawYMin) {
                        fYawY = fYawYMin;
                    }
                }
                else
                fYawY = 0;
            }
            else if (windowY + m.WIDTH/2 - 1 < mouseY && windowY + m.WIDTH/2 + 1 > mouseY){
                fYawY = 0;
            }
        }
    }
}