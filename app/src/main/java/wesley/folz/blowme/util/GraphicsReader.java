package wesley.folz.blowme.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import wesley.folz.blowme.graphics.models.Model;
import wesley.folz.blowme.ui.MainApplication;

/**
 * Created by wesley on 5/11/2015.
 */
public abstract class GraphicsReader
{
    public static void readShader(Model model)
    {
        InputStream vertexStream = MainApplication.getAppContext().getResources().openRawResource( model.VERTEX_SHADER );
        BufferedReader vertexReader = new BufferedReader( new InputStreamReader( vertexStream ) );

        InputStream fragmentStream = MainApplication.getAppContext().getResources().openRawResource( model.FRAGMENT_SHADER );
        BufferedReader fragmentReader = new BufferedReader( new InputStreamReader( fragmentStream ) );

        String line;
        model.vertexShaderCode = new String("");
        model.fragmentShaderCode = new String("");
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

    public static void readPLYFile(Model model)
    {
        //opening input stream to obj file
        InputStream stream = MainApplication.getAppContext().getResources().openRawResource(model
                .OBJ_FILE_RESOURCE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("element vertex"))
                {
                    StringTokenizer tokenizer = new StringTokenizer(line, " ");

                }

            }
        } catch (IOException e)
        {

        }
    }

    public static void readOBJFile( Model model )
    {
        //opening input stream to obj file
        InputStream stream = MainApplication.getAppContext().getResources().openRawResource( model
                .OBJ_FILE_RESOURCE);
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();
        ArrayList<Short> faceList = new ArrayList<>();
        ArrayList<Short> faceNormals = new ArrayList<>();

        HashMap<Short, Float[]> normalVertexMap = new HashMap<>();

        short offset = 1;

        String line = null;
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
                    StringTokenizer tokenizer = new StringTokenizer( line, "// " );
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
                            faceNormals.add( (short) (Short.parseShort( s ) - offset) );
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
                model.setInterleavedData(interleaveData(normalVertexMap, vertices));

                stream.close();
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
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

    private static float[] interleaveData(HashMap<Short, Float[]> normalVertexMap, ArrayList<Float> vertices)
    {
        ArrayList<Float> interleavedArrayList = new ArrayList<>();
        float[] interleavedData = new float[2 * vertices.size() + 4 * vertices.size() / 3 + 2 * vertices.size() / 3];

        for(short key=0; key < normalVertexMap.size(); key++)
        {
            Float[] normalVector = normalVertexMap.get(key);
            //    Log.e("keys", String.valueOf(key));
            //vertices
            for(int i=0; i<3; i++)
                interleavedArrayList.add(vertices.get(3*key+i));
            //per-vertex normal vectors
            for(int i=0; i<3; i++)
                interleavedArrayList.add(normalVector[i]);
            //color data
            for (int i = 0; i < 4; i++)
            {
                if (i == 3)
                    interleavedArrayList.add(1.0f);
                else if (i == 2)
                    interleavedArrayList.add(0.75f);
                else
                    interleavedArrayList.add(0.75f);
//                    interleavedArrayList.add((float) (1.0 * (float) (i % 2)));
            }
            //texture coordinates

            float xn = Math.abs(normalVector[0]);
            float yn = Math.abs(normalVector[1]);
            float zn = Math.abs(normalVector[2]);
            if (xn > yn)
            {
                if (xn > zn)//y-z plane
                {
                    interleavedArrayList.add(vertices.get(3 * key + 2));
                    interleavedArrayList.add(vertices.get(3 * key + 1));
                }
                else //x-y plane
                {
                    interleavedArrayList.add(vertices.get(3 * key));
                    interleavedArrayList.add(vertices.get(3 * key + 1));
                }
            }
            else
            {
                if (yn > zn) //x-z plane
                {
                    interleavedArrayList.add(vertices.get(3 * key));
                    interleavedArrayList.add(vertices.get(3 * key + 2));
                } else //x-y plane
                {
                    interleavedArrayList.add(vertices.get(3 * key));
                    interleavedArrayList.add(vertices.get(3 * key + 1));
                }
            }
        }
        //copy arraylist to float array
        for (int i=0; i<interleavedData.length; i++)
        {
            interleavedData[i] = interleavedArrayList.get(i);
            //Log.e("interleaved", String.valueOf(interleavedData[i]));
        }

        //Log.e("interleave", String.valueOf(interleavedArrayList.size()));

        return interleavedData;
    }

}
