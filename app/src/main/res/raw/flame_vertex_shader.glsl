uniform mat4 u_MVPMatrix; // A constant representing the combined model/view/projection matrix.
uniform mat4 u_MVMatrix;

uniform float deltaT;
uniform vec3 normalVector;
uniform vec4 position;

//attribute vec4 position; // Per-vertex position information we will pass in.
attribute vec3 direction; // Per-vertex color information we will pass in.
//attribute vec3 normalVector; // Per-vertex normal information we will pass in.
attribute vec4 color;
attribute float speed;

varying vec3 v_Position; // This will be passed into the fragment shader.
varying vec3 v_Normal; // This will be passed into the fragment shader.
varying vec4 v_Color;

// The entry point for our vertex shader.
void main()
{
    vec4 newDir = vec4(direction*(0.2*sin((deltaT*speed))), 1.0);
    //vec4 newPos = vec4(position[0] + direction[0]*speed*deltaT, position[1] + direction[1]*speed*deltaT, 0.0, 1);
    vec4 newPos = position + newDir;

    gl_PointSize = 10.0;// * sin(4.0*deltaT);
    //gl_PointSize = 20.0 * sin(4.0*speed*deltaT);

    v_Color = color;

    // Transform the vertex into eye space.
    v_Position = 100.0*newPos.xyz;//vec3(u_MVMatrix * newPos);
    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(normalVector, 0.0));
    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = u_MVPMatrix * newPos;
}
