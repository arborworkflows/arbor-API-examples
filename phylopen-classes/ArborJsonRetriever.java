/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import javafx.util.Pair;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author awehrer
 */
public class ArborJsonRetriever
{
    static
    {
        whitespace_charclass = "["
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL) 
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD 
            + "\\u2001" // EM QUAD 
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            + "]"
            ;
    }
    
    private static final String whitespace_charclass;
    
    public JsonElement getResponseJson(String url, String requestMethod)
    {
        return getResponseJson(url, requestMethod, null, true);
    }
    
    public JsonElement getResponseJson(String url, String requestMethod, boolean htmlFormattedJson)
    {
        return getResponseJson(url, requestMethod, null, htmlFormattedJson);
    }
    
    public JsonElement getResponseJson(String url, String requestMethod, Collection<Pair<String, String>> requestHeaderProperties)
    {
        return getResponseJson(url, requestMethod, requestHeaderProperties, true);
    }
    
    public JsonElement getResponseJson(String url, String requestMethod, Collection<Pair<String, String>> requestHeaderProperties, boolean htmlFormattedJson)
    {
        JsonElement parsedJson = null;
        
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
            
            connection.setRequestMethod(requestMethod);
            
            if (requestMethod.equals("PUT") || requestMethod.equals("POST"))
            {
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(0);
            }
            
            //connection.setRequestProperty("Girder-Token", userInfo.getAuthenticationToken());
            if (requestHeaderProperties != null)
            {
                for (Pair<String, String> pair : requestHeaderProperties)
                    connection.setRequestProperty(pair.getKey(), pair.getValue());
            }
            
            //System.out.println("Connecting...");
            
            // give it 15 seconds to respond
            connection.setReadTimeout(15 * 1000);
            connection.connect();
            
            //System.out.println("Done!");
            
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                // read the output from the server
                JsonParser parser = new JsonParser();
                
                inputStream = connection.getInputStream();
                
                if (!htmlFormattedJson)
                {
                    // http://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        sb.append(line);
                        sb.append(System.getProperty("line.separator"));
                    }
                    
                    br.close();
                    
                    parsedJson = parser.parse(sb.toString());
                }
                else
                {
                    
                    Document doc = Jsoup.parse(inputStream, null, url);
                    //System.out.println("\n\n" + doc);
                    String jsonContent = doc.text().replaceAll(whitespace_charclass + "+", " ");
                    //System.out.println("\n\n\\\"" + jsonContent.contains("\\\""));
                    //jsonContent = jsonContent.replaceAll("\\\\\"", "\\\"");
                    //System.out.println("\\\"" + jsonContent.contains("\\\""));

                    parsedJson = parser.parse(jsonContent);
                }
                
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
        
        return parsedJson;
    }
}
