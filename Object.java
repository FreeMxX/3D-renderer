import java.util.*;

public class Object {

    // Initial Class
    Vec3D vec3D;

    //Vectors of all objects and all triangles
    static ArrayList<float[][]> triList = new ArrayList<>();
    static ArrayList<Object> objectList = new ArrayList<>();

    //Parent class object variables
    float vX;
    float vY;
    float vZ;

    float centerX;
    float centerY;
    float centerZ;

    float mass;
    float radius = 0;
    float length = 0;
    float width = 0;
    float height = 0;

    float[][][] mesh;

    String type;
    static int id = 0;

    //Gets all vectors to go through pipline
    public static ArrayList<float[][]> getAllVectors() {
        triList.clear();
        for (int i = 0; i < objectList.size(); i++) {
            Object object = objectList.get(i);
            float[][][] temp = object.mesh;
            if (object.type.equals("Sphere")) {
                for (int j = 0; j < 840; j++) {
                    float[][] vec = temp[j];
                    triList.add(vec);
                }
            }
            if (object.type.equals("Cube")) {
                    for (int j = 0; j < 12; j++) {
                    float[][] vec = temp[j];
                    triList.add(vec);
                }
            }
            if (object.type.equals("Plane")) {
                    for (int j = 0; j < 2; j++) {
                    float[][] vec = temp[j];
                    triList.add(vec);
                }
            }
        }
        return triList;
    }
    public void update() {
        for (int i = 0; i < objectList.size(); i++) {
            transformVectors(objectList.get(i).vX, objectList.get(i).vY, objectList.get(i).vZ, i);
        }
    }
    //translates object by given X Y and Z parameters
    public static void transformVectors(float x, float y, float z, int id) {
        if (id > objectList.size()-1)
            id = objectList.size()-1;
        Object object = objectList.get(id);
        float[] translate = {object.centerX + x, object.centerY + y, object.centerZ + z};
        object.centerX += x;
        object.centerY += y;
        object.centerZ += z;
        if (object.type.equals("Sphere")) {
            object.mesh = ((Sphere) object).createSphere("src/objectfiles/sphere.txt", translate, object.radius);
            objectList.set(id, object);
        }
        if (object.type.equals("Cube")) {
            object.mesh = ((Cube) object).createCube("src/objectfiles/cube.txt", translate, object.length, object.width, object.height);
            objectList.set(id, object);
        }
        if (object.type.equals("Plane")) {
            object.mesh = ((Cube) object).createCube("src/objectfiles/plane.txt", translate, object.length, object.width, object.height);
            objectList.set(id, object);
        }
    }
}
