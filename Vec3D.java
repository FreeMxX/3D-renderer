import java.awt.*;
import java.util.*;

public class Vec3D {

    //Initial classes
	V3DMath math;
	Panel m;

    //Initial matrices (holds 3 vertice vectors)
    float[][] currentTriangle;
    float[][] translateTriangle;
    float[][] viewedTriangle;
    float[][] projectTriangle;
    float[][] scaleTriangle;

	public Vec3D(V3DMath math, Panel m) {
		this.math = math;
		this.m = m;
    }
    //draws the triangles after they have passed thorugh the pipeline
    public void drawTriangles(Graphics2D g2, float[][] t, float dp) {
        int[] xPoints = {(int)t[0][0], (int)t[1][0], (int)t[2][0]};
        int[] yPoints = {(int)t[0][1], (int)t[1][1], (int)t[2][1]};

        g2.setColor(math.getColor(dp));
        
        g2.fillPolygon(xPoints, yPoints, 3);
    }
    //pipeline to transform the triangles from 3D space to 2D space
	public void transformTriangles(Graphics2D g2, ArrayList<float[][]> list) {
        m.updateSort();
        for (int i = list.size() - 1; i >= 0; i--) {

            currentTriangle = list.get(i);
        
            float[] normal, line1, line2;

            line1 = math.vectorSub(currentTriangle[1], currentTriangle[0]);
            line2 = math.vectorSub(currentTriangle[2], currentTriangle[0]);

            normal = math.vectorCP(line1, line2);
            normal = math.vectorNorm(normal);
            
            float[] vCameraRay = math.vectorSub(currentTriangle[0], math.vCamera);
            if (math.vectorDP(normal, vCameraRay) < 0f && getDistanceFromCamera(currentTriangle) < 150) {
        
                math.vLightDirection = math.vectorNorm(math.vLightDirection);
                float dp = Math.max(0.01f, math.vectorDP(math.vLightDirection, normal));
                
                viewedTriangle = matrixMultiplyTri(currentTriangle, math.cameraView);
                int nClippedTriangles = 0;
                nClippedTriangles = math.Triangle_ClipAgainstPlane(math.plainNear1, math.plainNear2, viewedTriangle);
                
                for (int n = 0; n < nClippedTriangles; n++) {
                    translateTriangle = translateTri(0f, 0f, 0f, math.clipped[n]);
                    projectTriangle = projectTri(translateTriangle, math.m4x4);
                    scaleTriangle = scaleTri(projectTriangle);
                    
                    drawTriangles(g2, scaleTriangle, dp);
                }
            }
        }
    }
    public double getDistanceFromCamera(float[][] t1) {
        return (Math.sqrt(Math.pow((t1[0][2] + t1[1][2] + t1[2][2]) / 3.0f - math.vCamera[2], 2) + Math.pow((t1[0][0] + t1[1][0] + t1[2][0]) / 3.0f - math.vCamera[0], 2) + Math.pow((t1[0][1] + t1[1][1] + t1[2][1]) / 3.0f - math.vCamera[1], 2)));
    }
    public float[][] translateTri(float x, float y, float z, float[][] t) {
        float p1x = t[0][0] + x;
        float p1y = t[0][1] + y;
        float p1z = t[0][2] + z;

        float p2x = t[1][0] + x;
        float p2y = t[1][1] + y;
        float p2z = t[1][2] + z;

        float p3x = t[2][0] + x;
        float p3y = t[2][1] + y;
        float p3z = t[2][2] + z;

        float[][] translateTriangle = {{p1x, p1y, p1z, 1},{p2x, p2y, p2z, 1},{p3x, p3y, p3z, 1}};
        return translateTriangle;
    }
    public float[][] matrixMultiplyTri(float[][] t, float[][] m4x4) {
        float[][] triMultiplied = math.matrixMult(t.length, t[0].length, t, m4x4[0].length, m4x4.length, m4x4);//{{p1x, p1y, p1z, 1}, {p2x, p2y, p2z, 1},{p3x, p3y, p3z, 1}};

        return triMultiplied;
    }
    public float[][] projectTri(float[][] t, float[][] m4x4) {
        float w1 = t[0][0] * m4x4[0][3] + t[0][1] * m4x4[1][3] + t[0][2] * m4x4[2][3] + m4x4[3][3];
        float w2 = t[1][0] * m4x4[0][3] + t[1][1] * m4x4[1][3] + t[1][2] * m4x4[2][3] + m4x4[3][3];
        float w3 = t[2][0] * m4x4[0][3] + t[2][1] * m4x4[1][3] + t[2][2] * m4x4[2][3] + m4x4[3][3];

        float p1x = t[0][0] * m4x4[0][0] + t[0][1] * m4x4[1][0] + t[0][2] * m4x4[2][0] + m4x4[3][0];
        float p1y = t[0][0] * m4x4[0][1] + t[0][1] * m4x4[1][1] + t[0][2] * m4x4[2][1] + m4x4[3][1];
        float p1z = t[0][0] * m4x4[0][2] + t[0][1] * m4x4[1][2] + t[0][2] * m4x4[2][2] + m4x4[3][2];

        float p2x = t[1][0] * m4x4[0][0] + t[1][1] * m4x4[1][0] + t[1][2] * m4x4[2][0] + m4x4[3][0];
        float p2y = t[1][0] * m4x4[0][1] + t[1][1] * m4x4[1][1] + t[1][2] * m4x4[2][1] + m4x4[3][1];
        float p2z = t[1][0] * m4x4[0][2] + t[1][1] * m4x4[1][2] + t[1][2] * m4x4[2][2] + m4x4[3][2];

        float p3x = t[2][0] * m4x4[0][0] + t[2][1] * m4x4[1][0] + t[2][2] * m4x4[2][0] + m4x4[3][0];
        float p3y = t[2][0] * m4x4[0][1] + t[2][1] * m4x4[1][1] + t[2][2] * m4x4[2][1] + m4x4[3][1];
        float p3z = t[2][0] * m4x4[0][2] + t[2][1] * m4x4[1][2] + t[2][2] * m4x4[2][2] + m4x4[3][2];

        if (w1 != 0.0f && w2 != 0.0f && w3 != 0.0f) {
            float[][] projTriangle = {{p1x/w1, p1y/w1, p1z/w1,1},{p2x/w2, p2y/w2, p2z/w2,1},{p3x/w3, p3y/w3, p3z/w3,1}};
            return projTriangle;
        }
        else if (w1 == 0.0f){
            float[][] projTriangle = {{p1x/0.000001f, p1y/0.000001f, p1z/0.000001f, 1},{p2x/w2, p2y/w2, p2z/w2,1},{p3x/w3, p3y/w3, p3z/w3,1}};
            return projTriangle;
        }
        else if (w2 == 0.0f) {
            float[][] projTriangle = {{p1x/w1, p1y/w1, p1z/w1, 1},{p2x/0.000001f, p2y/0.000001f, p2z/0.000001f, 1},{p3x/w3, p3y/w3, p3z/w3, 1}};
            return projTriangle;
        }
        else if (w3 == 0.0f) {
            float[][]projTriangle = {{p1x/w1, p1y/w1, p1z/w1, 1},{p2x/w2, p2y/w2, p2z/w2, 1},{p3x/0.000001f, p3y/0.000001f, p3z/0.000001f, 1}};
            return projTriangle;
        }
        else {
            float[][] projTriangle = {{p1x, p1y, p1z, 1}, {p2x, p2y, p2z, 1},{p3x, p3y, p3z, 1}};
            return projTriangle;
        }
    }
    public float[][] scaleTri(float[][] t) {
        float p1x = (t[0][0] + 1.0f) * (0.5f * (float)m.WIDTH);
        float p1y = (t[0][1] + 1.0f) * (0.5f * (float)m.HEIGHT);
        float p1z = t[0][2];

        float p2x = (t[1][0] + 1.0f) * (0.5f * (float)m.WIDTH);
        float p2y = (t[1][1] + 1.0f) * (0.5f * (float)m.HEIGHT);
        float p2z = t[1][2];

        float p3x = (t[2][0] + 1.0f) * (0.5f * (float)m.WIDTH); 
        float p3y = (t[2][1] + 1.0f) * (0.5f * (float)m.HEIGHT); 
        float p3z = t[2][2];

        float[][] scaleTriangle = {{p1x, p1y, p1z, 1},{p2x, p2y, p2z, 1},{p3x, p3y, p3z, 1}};

        return scaleTriangle;
    }
    public float getLightLevel(float[][] translateTriangle) {
        float[] line1 = math.vectorCP(translateTriangle[1], translateTriangle[0]); 
        float[] line2 = math.vectorCP(translateTriangle[2], translateTriangle[0]);
        float[] normal = math.vectorNorm(math.vectorCP(line1, line2));
        float dp = 0f;

        if(math.vectorDP(normal, math.vectorSub(translateTriangle[0], math.vCamera)) < 0.0f) {
            math.vLightDirection = math.vectorNorm(math.vLightDirection);
            dp = math.vectorDP(normal, math.vLightDirection);
        }
        return dp;
    }
    public boolean normalize(float[][] translateTriangle) {
        float[] line1 = math.vectorCP(translateTriangle[1], translateTriangle[0]); 
        float[] line2 = math.vectorCP(translateTriangle[2], translateTriangle[0]);
        float[] normal = math.vectorNorm(math.vectorCP(line1, line2));
        boolean normalized = false;

        if(math.vectorDP(normal, math.vectorSub(translateTriangle[0], math.vCamera)) < 0f) {
            normalized = true;
        }
        return normalized;
    }
}
