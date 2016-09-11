uniform mat4 u_MVPMatrix; // A constant representing the combined model/view/projection matrix.
uniform mat4 u_MVMatrix;

attribute vec4 position; // Per-vertex position information we will pass in.
attribute vec4 color; // Per-vertex color information we will pass in.
attribute vec3 normalVector; // Per-vertex normal information we will pass in.
attribute vec2 a_TexCoordinate;

varying vec3 v_Position; // This will be passed into the fragment shader.
varying vec4 v_Color; // This will be passed into the fragment shader.
varying vec3 v_Normal; // This will be passed into the fragment shader.
varying vec2 v_TexCoordinate;

// The entry point for our vertex shader.
void main()
{
    // Transform the vertex into eye space.
    v_Position = vec3(u_MVMatrix * position);
    // Pass through the color.
    v_Color = color;

    v_TexCoordinate = a_TexCoordinate;

    // Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(normalVector, 0.0));
    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = u_MVPMatrix * position;
}
