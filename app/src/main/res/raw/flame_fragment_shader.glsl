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
    float diffuse = (10.0 - length(v_Position.xy - vec2(0.0, 0.0)));
    // Multiply the color by the diffuse illumination level to get final output color.
    //gl_FragColor = texture2D(second_Texture, gl_PointCoord)*texture2D(u_Texture, gl_PointCoord)*vec4(1.0, 1.0, 1.0, 0.1);

    vec3 color = diffuse*vec3(1.0, 1.0, 0.0);
//    gl_FragColor = texture2D(u_Texture, gl_PointCoord)*diffuseColor;
    gl_FragColor = vec4(color, diffuse);
}


