import java.nio.file.*;
import java.util.*;

public class Cube extends Object{
    
    //holds vectors temporarily from text file
    ArrayList<float[]> tempVecList = new ArrayList<>();

    //Constructors
    public Cube(Vec3D vec3D, float x, float y, float z, float l, float w, float h, float mass) {
        this.vec3D = vec3D;

        vX = 0;
        vY = 0;
        vZ = 0;

        centerX = x + w/2;
        centerY = y + h/2;
        centerZ = z + l/2;
        
        this.width = w;
        this.height = h;
        this.length = l;

        this.mass = mass;
        this.type = "Cube";

        float[] transform = {x, y, z};

        mesh = createCube("src/objectfiles/cube.txt", transform, length, width, height);
        objectList.add(this);
        id++;
    }
    public Cube(Vec3D vec3D, float l, float w, float h) {
        this(vec3D, 0, 0, 0, l, w, h, 5);
    }
    public Cube(Vec3D vec3D, float x, float y, float z, float mass) {
        this(vec3D, x, y, z,1,1,1, mass);
    }
    public Cube(Vec3D vec3D, float mass) {
        this(vec3D, 0,0, 0,1,1,1, mass);
    }
    //Read from text file
    public float[][][] createCube(String filePath, float[] transform, float l, float w, float h) {
        tempVecList.clear();
        int col = 0;
        int num = 0;
        float[][][] tempMesh = new float[12][4][4];
        
        float objScaleFactor = 1;
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
                    float num1 = objScaleFactor * Float.parseFloat(number[col]);
                    col++;
                    float num2 = objScaleFactor * Float.parseFloat(number[col]);
                    col++;
                    float num3 = objScaleFactor * Float.parseFloat(number[col]);

                    float[] vec = {w * num1 + transform[0], -h * num2 + transform[1], l * num3 + transform[2], 1};
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
        }
        return tempMesh;
    }
}

