package wesley.folz.blowme.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import wesley.folz.blowme.graphics.Model;

/**
 * Created by wesley on 5/11/2015.
 */
public abstract class OBJReader
{
    public static void readOBJFile( Context context, Model model )
    {
        //opening input stream to obj file
        InputStream stream = context.getResources().openRawResource( model.RESOURCE );
        BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

        ArrayList<float[]> vertices = new ArrayList<>();
        ArrayList<float[]> normals = new ArrayList<>();

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
                stream.close();
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

}
