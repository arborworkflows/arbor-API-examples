/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import javafx.scene.image.Image;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 *
 * @author awehrer
 */
public class ArborImageRetriever
{
    public Image getImage(String url)
    {
        return getImage(url, -1.0, -1.0, true, true);
    }
    
    public Image getImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth)
    {
        Image image = null;
        
        if (url.toLowerCase().endsWith("download"))
        {
            URL urlObj;
            HttpURLConnection connection;
            InputStream inputStream;

            try
            {
                // create the connection
                urlObj = new URL(url);
                connection = (HttpURLConnection) urlObj.openConnection();

                if (url.toLowerCase().startsWith("https://"))
                {
                    // http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.html
                    final HostnameVerifier myVerif = new HostnameVerifier()
                    {
                        @Override
                        public boolean verify(String hostname, SSLSession session)
                        {
                            return true;
                        }
                    };

                    HttpsURLConnection conn = (HttpsURLConnection)connection;
                    conn.setHostnameVerifier(myVerif);
                }

                connection.setRequestMethod("GET");

                //System.out.println("Connecting...");

                // give it 15 seconds to respond
                connection.setReadTimeout(15 * 1000);
                connection.connect();

                //System.out.println("Done!");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    // read the output from the server
                    inputStream = connection.getInputStream();
                    image = new Image(inputStream, requestedWidth, requestedHeight, preserveRatio, smooth);
                    inputStream.close();
                }
                else
                {
                    System.out.println("HTTP Error " + connection.getResponseCode() + ": " + connection.getResponseMessage());
                }
            }
            catch (MalformedURLException e)
            {
                System.out.println("Malformed URL");
            }
            catch (SocketTimeoutException e)
            {
                System.out.println("SOCKET TIMEOUT");
            }
            catch (IOException e)
            {
                System.out.println("An I/O exception has occurred. " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        else
        {
            image = new Image(url, requestedWidth, requestedHeight, preserveRatio, smooth);
        }
        
        return image;
    }
}
