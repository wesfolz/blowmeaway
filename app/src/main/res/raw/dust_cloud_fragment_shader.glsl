precision mediump float; // Set the default precision to medium. We don't need as high of a
// precision in the fragment shader.
uniform vec3 u_LightPos; // The position of the light in eye space.
uniform sampler2D u_Texture; //input texture
uniform sampler2D second_Texture;

varying vec3 v_Position; // Interpolated position for this fragment.
// triangle per fragment.
varying vec3 v_Normal; // Interpolated normal for this fragment.


varying vec4 v_Color;

// The entry point for our fragment shader.
void main()
{
    // Will be used for attenuation.
    float distance = length(u_LightPos - v_Position);
    // Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - v_Position);
    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination.
    float diffuse;// = max(2.0*dot(v_Normal, lightVector), 0.1);

    if (gl_FrontFacing)
    {
        diffuse = max(dot(v_Normal, lightVector), 0.0);
    }
    else
    {
        diffuse = max(dot(-v_Normal, lightVector), 0.0);
    }

    // Add attenuation.
    diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance * distance)));// Add ambient lighting
    diffuse = diffuse + 0.4;

    // Multiply the color by the diffuse illumination level to get final output color.
//    gl_FragColor = texture2D(second_Texture, gl_PointCoord)*texture2D(u_Texture, gl_PointCoord)*vec4(1.0, 1.0, 1.0, 0.1);
//    gl_FragColor = diffuse*texture2D(u_Texture, gl_PointCoord)*vec4(1.0, 1.0, 1.0, 0.1);
    gl_FragColor = diffuse*vec4(0.0, 0.0, 1.0, 0.1);


}
