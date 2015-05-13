package wesley.folz.blowme.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import wesley.folz.blowme.graphics.Model;
import wesley.folz.blowme.ui.MyApplication;

/**
 * Created by wesley on 5/11/2015.
 */
public abstract class OBJReader
{
    public static void readOBJFile( Model model )
    {
        //opening input stream to obj file
        InputStream stream = MyApplication.getAppContext().getResources().openRawResource( model
                .RESOURCE );
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();
        ArrayList<Short> faceList = new ArrayList<>();
        ArrayList<Short> faceNormals = new ArrayList<>();
        short offset = 1;

        String line = null;
        try
        {
            while( (line = reader.readLine()) != null )
            {
                if( line.startsWith( "v " ) || line.startsWith( "vn " ) )
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
                    while( tokenizer.hasMoreTokens() )
                    {
                        if( count == 0 )
                        {
                            tokenizer.nextToken();
                        }
                        if( count % 2 == 0 )
                        {
                            String s = tokenizer.nextToken();
                            faceList.add( (short) (Short.parseShort( s ) - offset) );
                        }
                        if( count % 2 == 1 )
                        {
                            String s = tokenizer.nextToken();
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
                short[] normalOrder = new short[faceNormals.size()];
                for( int i = 0; i < faceList.size(); i++ )
                {
                    vertexOrder[i] = faceList.get( i );
                    normalOrder[i] = faceNormals.get( i );
                }

                float[] faceVertices = new float[vertices.size()];
                float[] normalVectors = new float[normals.size()];
                for( int i = 0; i < vertices.size(); i++ )
                {
                    faceVertices[i] = vertices.get( i );
                    //normalVectors[i] = normals.get( i );
                }
                model.setVertexOrder( vertexOrder );
                model.setVertexData( faceVertices );
                model.setNormalOrder( normalOrder );
                model.setNormalData( normalVectors );
                stream.close();
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

}
