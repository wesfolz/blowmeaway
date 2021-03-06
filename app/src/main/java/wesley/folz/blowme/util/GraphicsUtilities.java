package wesley.folz.blowme.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.ui.MainApplication;

/**
 * Created by wesley on 5/11/2015.
 */
public class GraphicsUtilities
{
    public GraphicsUtilities()
    {
        shaderProgramIdMap = new HashMap<>();
        modelVBOMap = new HashMap<>();
        textureIdMap = new HashMap<>();
        orderVBOMap = new HashMap<>();
        numVerticesMap = new HashMap<>();
    }

    /**
     * Stores model VBO ID and draw order VBO ID
     *
     * @param modelName - Name of model (key in HashMaps)
     * @param resource  - model resource ID
     */
    public void storeModelData(String modelName, int resource)
    {
        readOBJFile(resource);
        enableGraphics();
        modelVBOMap.put(modelName, dataVBO);
        orderVBOMap.put(modelName, orderVBO);
        numVerticesMap.put(modelName, numVertices);
    }

    /**
     * Stores shader program in HashMap
     *
     * @param shaderName       - Name of shader (key in HashMap)
     * @param vertexResource   - Vertex shader resource file ID
     * @param fragmentResource - Fragment shader resource file ID
     * @param attributes       - Attributes to pass to shader
     */
    public void storeShader(String shaderName, int vertexResource, int fragmentResource, String[] attributes)
    {
        String vertexShaderCode = readShaderFile(vertexResource);
        String fragmentShaderCode = readShaderFile(fragmentResource);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        int programHandle = createAndLinkProgram(vertexShader, fragmentShader, attributes);

        shaderProgramIdMap.put(shaderName, programHandle);
    }

    /**
     * Stores texture ID in HashMap
     *
     * @param textureName     - Name of texture (key in HashMap)
     * @param textureResource - Texture file resource ID
     */
    public void storeTexture(String textureName, int textureResource)
    {
        int textureID = loadTextureFile(MainApplication.getAppContext(), textureResource);

        textureIdMap.put(textureName, textureID);
    }

    private void enableGraphics()
    {
        FloatBuffer dataBuffer = ByteBuffer.allocateDirect(modelData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        dataBuffer.put(modelData).position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                vertexOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        ShortBuffer drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(vertexOrder);
        drawListBuffer.position(0);

        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        //vertex coordinates, normal vectors, color, texture coordinates
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, dataBuffer.capacity() * 4,
                dataBuffer, GLES20.GL_STATIC_DRAW);

        //vertex order
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawListBuffer.capacity() * 2, drawListBuffer,
                GLES20.GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        //get references to gl buffers
        dataVBO = buffers[0];
        orderVBO = buffers[1];
    }


    /**
     * Reads in shader file
     *
     * @param resource - shader file resource ID
     * @return - Shader string
     */
    private String readShaderFile(int resource)
    {
        InputStream shaderStream = MainApplication.getAppContext().getResources().openRawResource(resource);
        BufferedReader shaderReader = new BufferedReader(new InputStreamReader(shaderStream));

        String line;
        String shaderCode = "";
        try
        {
            while ((line = shaderReader.readLine()) != null)
            {
                shaderCode += line + "\n";
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return shaderCode;
    }

    public static void readShader(Model model)
    {
        InputStream vertexStream = MainApplication.getAppContext().getResources().openRawResource( model.VERTEX_SHADER );
        BufferedReader vertexReader = new BufferedReader( new InputStreamReader( vertexStream ) );

        InputStream fragmentStream = MainApplication.getAppContext().getResources().openRawResource( model.FRAGMENT_SHADER );
        BufferedReader fragmentReader = new BufferedReader( new InputStreamReader( fragmentStream ) );

        String line;
        model.vertexShaderCode = "";
        model.fragmentShaderCode = "";
        try
        {
            while( (line = vertexReader.readLine()) != null )
            {
                model.vertexShaderCode += line + "\n";
            }

            while( (line = fragmentReader.readLine()) != null )
            {
                model.fragmentShaderCode += line + "\n";
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void draw_stencil()
    {
        GLES20.glEnable(GLES20.GL_STENCIL_TEST);

        // Fill stencil buffer with 0's
        GLES20.glClearStencil(0);
        GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);

        // Write 1's into stencil buffer where the hole will be
        GLES20.glColorMask(false, false, false, false);
        GLES20.glDepthMask(false);
        GLES20.glStencilFunc(GLES20.GL_ALWAYS, 1, ~0);
        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_REPLACE);
        //GLES20.glStencilMask(0xFF);
        //GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);

        //drawDistortion();
        //Matrix.translateM(modelMatrix, 0, 0.5f, 0, 0);
        //drawDistortion();
        //Matrix.translateM(modelMatrix, 0, -0.5f, 0, 0);

        GLES20.glColorMask(true, true, true, true);
        GLES20.glDepthMask(true);
        GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, ~0);
        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);


        // Draw cube reflection
        //GLES20.glStencilFunc(GLES20.GL_EQUAL, 1, 0xFF);
        //GLES20.glStencilMask(0x00);
        //GLES20.glDepthMask(true);
        //Matrix.translateM(background.modelMatrix, 0, 0.1f, 0.1f, 0.0f);
        //Matrix.scaleM(background.modelMatrix, 0, 2, 2, 2);
        //drawBackground();
        //background.draw();
        //Matrix.translateM(background.modelMatrix, 0, -0.1f, -0.1f, 0.0f);
        //Matrix.scaleM(background.modelMatrix, 0, 0.5f, 0.5f, 0.5f);
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);
    }

    private void renderToTexture() {
        int[] frameBuffer = new int[1];
        int[] depthRenderBuffer = new int[1];
        int[] renderTexture = new int[1];

        //generate buffers
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glGenRenderbuffers(1, depthRenderBuffer, 0);
        GLES20.glGenTextures(1, renderTexture, 0);

        //generate texutre
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTexture[0]);


        // parameters - we have to make sure we clamp the textures to the edges
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);

        // create it
        // create an empty intbuffer first
        int texW = 100;
        int texH = 100;
        int[] buf = new int[texW * texH];
        IntBuffer textureBuffer = ByteBuffer.allocateDirect(buf.length
                * 4).order(ByteOrder.nativeOrder()).asIntBuffer();

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, texW, texH, 0,
                GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, textureBuffer);

        //create render buffer and bind to 16bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, texW,
                texH);

        //draw texture
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);

        // specify texture as color attachment
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, renderTexture[0], 0);

        // attach render buffer as depth buffer
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);

        // check status
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("FrameBuffer", "FrameBuffer Error: " + status);
        }
        // Clear the texture (buffer) and then render as usual...
        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    }

    private void readOBJFile(int resource)
    {
        //opening input stream to obj file
        InputStream stream = MainApplication.getAppContext().getResources().openRawResource(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();
        ArrayList<Float> textureCoords = new ArrayList<>();
        ArrayList<Float> faces = new ArrayList<>();

        ArrayList<Short> vtnOrder = new ArrayList<>();

        HashMap<String, Short> vertexTextureNormalMap = new HashMap<>();

        short offset = 1;

        short vtnIndex = 0;

        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("v"))
                {
                    StringTokenizer tokenizer = new StringTokenizer(line, " ");
                    float[] coordinate = new float[3];
                    int count = 0;
                    while (tokenizer.hasMoreTokens())
                    {
                        if (count == 0)
                        {
                            tokenizer.nextToken();
                        }
                        coordinate[count] = Float.parseFloat(tokenizer.nextToken());
                        count++;
                    }
                    //read vertex normals
                    if (line.startsWith("vn "))
                    {
                        normals.add(coordinate[0]);
                        normals.add(coordinate[1]);
                        normals.add(coordinate[2]);
                    }
                    //read uv texture coordinates
                    else if (line.startsWith("vt "))
                    {
                        textureCoords.add(coordinate[0]);
                        //*(-1) to account for inverted coordinate systems
                        textureCoords.add(coordinate[1] * (-1));
                    }
                    //read vertices
                    else if (line.startsWith("v ")) {
                        vertices.add(coordinate[0]);
                        vertices.add(coordinate[1]);
                        vertices.add(coordinate[2]);
                    }
                }
                else
                {
                    //read faces
                    if (line.startsWith("f "))
                    {
                        StringTokenizer vtnTripleTokenizer = new StringTokenizer(line, " ");
                        //StringTokenizer tokenizer = new StringTokenizer(line, "/ ");
                        String s, vtn;
                        while (vtnTripleTokenizer.hasMoreTokens())
                        {
                            vtn = vtnTripleTokenizer.nextToken();
                            if (vtn.equals("f")) //igonre 'f' token
                            {
                                vtn = vtnTripleTokenizer.nextToken();
                            }

                            //check if the vertex,texture,normal triple is already in HashMap
                            //if not add it and increment index
                            if (!vertexTextureNormalMap.containsKey(vtn)) {
                                //add new index to HashMap
                                vertexTextureNormalMap.put(vtn, vtnIndex);
                                vtnIndex++;

                                //read each vertex, texture, normal
                                StringTokenizer tokenizer = new StringTokenizer(vtn, "/ ");
                                int count = 0;
                                while (tokenizer.hasMoreTokens()) {
                                    s = tokenizer.nextToken();
                                    switch (count % 3) {
                                        case 0: //read vertex
                                            for (int i = 0; i < 3; i++) {
                                                faces.add(vertices.get(3 * (Integer.parseInt(s)
                                                        - offset) + i));
                                            }
                                            break;

                                        case 1: //read texture
                                            for (int i = 0; i < 2; i++) {
                                                faces.add(textureCoords.get(2 * (Integer.parseInt(s)
                                                        - offset) + i));
                                            }
                                            break;

                                        case 2: //read normal
                                            for (int i = 0; i < 3; i++) {
                                                faces.add(normals.get(3 * (Integer.parseInt(s)
                                                        - offset) + i));
                                            }
                                            break;

                                    }
                                    count++;
                                }
                            }
                            //add vtn index to order array
                            vtnOrder.add(vertexTextureNormalMap.get(vtn));
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                numVertices = vtnOrder.size();
                vertexOrder = new short[numVertices];
                for (int i = 0; i < vtnOrder.size(); i++)
                {
                    vertexOrder[i] = vtnOrder.get(i);
                }

                modelData = new float[faces.size()];
                for (int i = 0; i < faces.size(); i++) {
                    modelData[i] = faces.get(i);
                }
                //modelData = interleaveData(normalVertexMap, textureCoordinateMap, vertices);

                stream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /*
        public static void readOBJFile( Model model )
        {
            //opening input stream to obj file
            InputStream stream = MainApplication.getAppContext().getResources().openRawResource( model
                    .OBJ_FILE_RESOURCE);
            BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

            ArrayList<Float> vertices = new ArrayList<>();
            ArrayList<Float> normals = new ArrayList<>();
            ArrayList<Short> faceList = new ArrayList<>();

            HashMap<Short, Float[]> normalVertexMap = new HashMap<>();


            short offset = 1;

            String line;
            try
            {
                while( (line = reader.readLine()) != null )
                {
                    if( line.startsWith( "v") )
                    {
                        StringTokenizer tokenizer = new StringTokenizer( line, " " );
                        float[] coordinate = new float[3];
                        int count = 0;
                        while( tokenizer.hasMoreTokens() )
                        {
                            if( count == 0 )
                            {
                                tokenizer.nextToken();
                            }
                            coordinate[count] = Float.parseFloat( tokenizer.nextToken() );
                            count++;
                        }
                        if( line.startsWith( "vn " ) )
                        {
                            normals.add( coordinate[0] );
                            normals.add( coordinate[1] );
                            normals.add( coordinate[2] );
                        }
                        else if( line.startsWith( "v " ) )
                        {
                            vertices.add( coordinate[0] );
                            vertices.add( coordinate[1] );
                            vertices.add( coordinate[2] );
                        }

                    }
                    else if( line.startsWith( "f " ) )
                    {
                        StringTokenizer tokenizer = new StringTokenizer( line, "/ " );
                        int count = 0;
                        String s;
                        while( tokenizer.hasMoreTokens() )
                        {
                            s = tokenizer.nextToken();
                            if( count == 0 )
                            {
                                s = tokenizer.nextToken();
                            }
                            if( count % 2 == 0 )
                            {
                                faceList.add( (short) (Short.parseShort( s ) - offset) );
                            }
                            else
                            {
                                //create map with key being last vertex index and value being normal vector
                                if(!normalVertexMap.containsKey(faceList.get(faceList.size() - 1)))
                                {
                                    int normalIndex = 3*(Integer.parseInt(s) - offset);
                                    normalVertexMap.put(faceList.get(faceList.size() - 1),
                                            new Float[]{normals.get(normalIndex), normals.get(normalIndex+1), normals.get(normalIndex+2)});
                                }
                            }
                            count++;
                        }
                    }
                }
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    short[] vertexOrder = new short[faceList.size()];
                    for( int i = 0; i < faceList.size(); i++ )
                    {
                        vertexOrder[i] = faceList.get( i );
                    }

                    model.setVertexOrder( vertexOrder );
                    //model.setInterleavedData(interleaveData(normalVertexMap, vertices));

                    stream.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
    */
    private int loadTextureFile(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle   An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes           Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle,
            final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                Log.e("openGl", "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                Log.e("openGl", "Error compiling program: " + GLES20.glGetShaderInfoLog(vertexShaderHandle));
                Log.e("openGl", "Error compiling program: " + GLES20.glGetShaderInfoLog(fragmentShaderHandle));

                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    /**
     * @param type
     * @param shaderCode
     * @return
     */
    private int loadShader(int type, String shaderCode)
    {
        // create a vertex vertexshader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment vertexshader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the vertexshader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    /**
     * Maps shader file IDs to shader program IDs
     */
    public HashMap<String, Integer> shaderProgramIdMap;

    /**
     * Maps graphics file IDs to graphics data
     */
    public HashMap<String, Integer> modelVBOMap;

    public HashMap<String, Integer> textureIdMap;

    public HashMap<String, Integer> orderVBOMap;

    public HashMap<String, Integer> numVerticesMap;

    private float[] modelData;
    private short[] vertexOrder;

    private int numVertices;
    private int dataVBO;
    private int orderVBO;

}
