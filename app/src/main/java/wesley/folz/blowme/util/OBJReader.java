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

        ArrayList<float[]> vertices = new ArrayList<>();
        ArrayList<float[]> normals = new ArrayList<>();
        ArrayList<Float> faceList = new ArrayList<>();
        ArrayList<Float> faceNormals = new ArrayList<>();

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
                        normals.add( coordinate );
                    }
                    else if( line.startsWith( "v " ) )
                    {
                        vertices.add( coordinate );
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
                            faceList.add( vertices.get( Integer.parseInt( s ) - 1 )[0] );
                            faceList.add( vertices.get( Integer.parseInt( s ) - 1 )[1] );
                            faceList.add( vertices.get( Integer.parseInt( s ) - 1 )[2] );
                        }
                        if( count % 2 == 1 )
                        {
                            String s = tokenizer.nextToken();
                            faceNormals.add( normals.get( Integer.parseInt( s ) - 1 )[0] );
                            faceNormals.add( normals.get( Integer.parseInt( s ) - 1 )[1] );
                            faceNormals.add( normals.get( Integer.parseInt( s ) - 1 )[2] );
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
                float[] faceVertices = new float[faceList.size()];
                float[] normalVectors = new float[faceNormals.size()];
                for( int i = 0; i < faceList.size(); i++ )
                {
                    faceVertices[i] = faceList.get( i );
                    normalVectors[i] = faceNormals.get( i );
                }
                model.setVertexData( faceVertices );
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
