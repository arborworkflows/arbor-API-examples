/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

/**
 *
 * @author awehrer
 */
public class ArborUserInfo
{
    private final String id;
    private final String username;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private final String authenticationToken;
    private final String authenticationTokenExpiration;

    public ArborUserInfo(String id, String username, String firstName, String lastName, String emailAddress, String authenticationToken, String authenticationTokenExpiration)
    {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.authenticationToken = authenticationToken;
        this.authenticationTokenExpiration = authenticationTokenExpiration;
    }
    
    public String getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getFirstName()
    {
        return firstName;
    }
    
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }
    
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    
    public String getEmailAddress()
    {
        return emailAddress;
    }
    
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public String getAuthenticationToken()
    {
        return authenticationToken;
    }

    public String getAuthenticationTokenExpiration()
    {
        return authenticationTokenExpiration;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");

        builder.append("Username: ");
        builder.append(getUsername());
        builder.append(lineSeparator);
        builder.append("Name: ");
        builder.append(getFirstName());
        builder.append(" ");
        builder.append(getLastName());
        builder.append(lineSeparator);
        builder.append("Authentication token: ");
        builder.append(getAuthenticationToken());

        return builder.toString();
    }
}
