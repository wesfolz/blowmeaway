uniform mat4 u_MVPMatrix;
attribute vec4 position;
void main()
{
    gl_Position = u_MVPMatrix * position;
}