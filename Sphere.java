import java.nio.file.*;
import java.util.*;

public class Sphere extends Object{

    //holds vectors temporarily from text file
    ArrayList<float[]> tempVecList = new ArrayList<>();

    //Constructors
    public Sphere(Vec3D vec3D, float x, float y, float z, float radius, float mass) {
        this.vec3D = vec3D;

        vX = 0;
        vY = 0;
        vZ = 0;

        centerX = x;
        centerY = y;
        centerZ = z;
        this.radius = radius;
        this.mass = mass;
        this.type = "Sphere";

        float[] transform = {x, y, z};

        mesh = createSphere("src/objectfiles/sphere.txt", transform, radius);
        objectList.add(this);
        id++;
    }
    public Sphere(Vec3D vec3d, float x, float y, float z, float mass) {
        this(vec3d, x, y, z, 1, mass);
    }
    public Sphere(Vec3D vec3d, float radius, float mass) {
        this(vec3d, 0,0,0, radius, mass);
    }
    public Sphere(Vec3D vec3d, float mass) {
        this(vec3d, 0,0,0, 1, mass);
    }
    //Read from file
    public float[][][] createSphere(String filePath, float[] transform, float radius) {
        tempVecList.clear();
        int col = 0;
        int num = 0;
        float[][][] tempMesh = new float[840][4][4];
        float objScaleFactor = 1/127f * radius;

        try
        {
        List<String> listOfStrings = new ArrayList <String>();
        listOfStrings = Files.readAllLines(Paths.get(filePath));
        String[] array = listOfStrings.toArray(new String[128]);
        num = 0;
        for (String eachString : array)
            {
                String number[]= eachString.split(" ");

                col = 0;
                if (number[col].equals("v")) {
                    col++;
                    float num1 = objScaleFactor * Float.parseFloat(number[col]) + transform[0];
                    col++;
                    float num2 = objScaleFactor * Float.parseFloat(number[col]) + transform[1];
                    col++;
                    float num3 = objScaleFactor * Float.parseFloat(number[col]) + transform[2];

                    float[] vec = {num1, -num2, num3, 1};
                    tempVecList.add(vec);
                }
                else if (number[col].equals("f")) {
                    col++;
                    float[] vec1 = tempVecList.get(Integer.parseInt(number[col]) - 1);
                    col++;
                    float[] vec2 = tempVecList.get(Integer.parseInt(number[col]) - 1);
                    col++;
                    float[] vec3 = tempVecList.get(Integer.parseInt(number[col]) - 1);

                    float[][] tri = {vec1, vec3, vec2};
                    tempMesh[num] = tri;
                    num++;
                }
                else {
                    System.out.println("STOP!");
                }
            }
        }
        catch (Exception e)
        {
           System.out.println("Could not open Sphere");
        }
        return tempMesh;
    }
}
