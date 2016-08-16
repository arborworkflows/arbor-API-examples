/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;

/**
 *
 * @author awehrer
 */
public class ArborLoginDialog extends Dialog<ArborUserInfo>
{
    private ArborUserInfo userInfo;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final String girderBaseURL;
    
    public ArborLoginDialog(String girderBaseURL)
    {
        this.setTitle("Log in");
        
        this.girderBaseURL = girderBaseURL;
        
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(loginButtonType);
        
        Label usernameFieldLabel = new Label("Username or email");
        usernameField = new TextField();
        usernameField.setPrefColumnCount(22);
        Label passwordFieldLabel = new Label("Password");
        passwordField = new PasswordField();
        passwordField.setPrefColumnCount(22);
        
        Label registerLabel = new Label("Don't have an account yet?");
        Hyperlink registerLink = new Hyperlink("Register here.");
        Label separatorLabel = new Label(" | ");
        Hyperlink passwordResetLink = new Hyperlink("Forgot your password?");
        
        HBox additionalOptionsPane = new HBox(2);
        additionalOptionsPane.getChildren().addAll(registerLabel, registerLink, separatorLabel, passwordResetLink);
        
        VBox mainContentPane = new VBox(10);
        mainContentPane.getChildren().addAll(usernameFieldLabel, usernameField, passwordFieldLabel, passwordField, additionalOptionsPane);
        
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, ArborUserInfo>()
        {
            @Override
            public ArborUserInfo call(ButtonType param)
            {
                return userInfo;
            }
        });
        
        final Button loginButton = (Button) getDialogPane().lookupButton(loginButtonType);
        loginButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (!loginAndStore(false))
                event.consume();
        });
        
        this.setOnShown(new EventHandler<DialogEvent>()
        {
            public void handle(DialogEvent event)
            {
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ArborLoginDialog.this.usernameField.requestFocus();
                    }
                });
            }
        });
        
        passwordResetLink.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                new ArborPasswordResetDialog(girderBaseURL).showAndWait();
            }
        });
        
        registerLink.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                Optional<Pair<String, String>> result = new ArborRegisterDialog(girderBaseURL).showAndWait();
                Pair<String, String> upPair = (result.isPresent() ? result.get() : null);
                
                if (upPair != null)
                {
                    usernameField.setText(upPair.getKey());
                    passwordField.setText(upPair.getValue());
                    
                    if (loginAndStore(true))
                        ArborLoginDialog.this.close();
                }
            }
        });
    }
    
    private boolean loginAndStore(boolean isNewUser)
    {
        //String girderBaseURL = "https://arbor.kitware.com/girder/api/v1/";
        
        List<Pair<String, String>> requestHeaderProperties = new ArrayList<>(1);
        String encoding = Base64.getEncoder().encodeToString((usernameField.getText() + ":" + passwordField.getText()).getBytes());
        requestHeaderProperties.add(new Pair<>("Authorization", "Basic " + encoding));
        
        JsonElement jsonDataPacket = new ArborJsonRetriever().getResponseJson(girderBaseURL + "user/authentication", "GET", requestHeaderProperties);
        
        //System.out.println(jsonDataPacket);
        
        if (jsonDataPacket == null)
        {
            if (isNewUser)
                new Alert(Alert.AlertType.ERROR, "Exceptional error: New user registration completed successfully, but login with the new credentials failed.", ButtonType.OK).showAndWait();
            else
                new Alert(Alert.AlertType.ERROR, "Login failed.", ButtonType.OK).showAndWait();
            
            return false;
        }
        
        JsonObject jsonDataPacketObj = jsonDataPacket.getAsJsonObject();
        
        userInfo = new ArborUserInfo(
                jsonDataPacketObj.get("user").getAsJsonObject().get("_id").getAsString(),
                jsonDataPacketObj.get("user").getAsJsonObject().get("login").getAsString(),
                jsonDataPacketObj.get("user").getAsJsonObject().get("firstName").getAsString(),
                jsonDataPacketObj.get("user").getAsJsonObject().get("lastName").getAsString(),
                jsonDataPacketObj.get("user").getAsJsonObject().get("email").getAsString(),
                jsonDataPacketObj.get("authToken").getAsJsonObject().get("token").getAsString(),
                jsonDataPacketObj.get("authToken").getAsJsonObject().get("expires").getAsString());
        
        if (isNewUser)
            new Alert(Alert.AlertType.CONFIRMATION, "Account created! Welcome, " + userInfo.getFirstName() + "!", ButtonType.OK).showAndWait();
        else
            new Alert(Alert.AlertType.CONFIRMATION, "Login successful! Welcome, " + userInfo.getFirstName() + "!", ButtonType.OK).showAndWait();
        
        return true;
    }
}
