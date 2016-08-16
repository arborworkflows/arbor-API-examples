/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;

/**
 *
 * @author Andrew Yee (templated from ArborLoginDialog.java, @author awehrer)
 */
public class ArborRegisterDialog extends Dialog<Pair<String, String>>
{
    private Pair<String, String> ret;
    private final TextField usernameField;
    private final TextField emailField;
    private final TextField firstNameField;
    private final TextField lastNameField;
    private final PasswordField passwordField;
    private final PasswordField passwordConfirmField;
    private final String girderBaseURL;
    
    public ArborRegisterDialog(String girderBaseURL)
    {
        this.setTitle("New User Registration");
        
        this.girderBaseURL = girderBaseURL;
        
        ButtonType registerButtonType = new ButtonType("Register", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(registerButtonType);
        
        Label usernameFieldLabel = new Label("Username");
        usernameField = new TextField();
        usernameField.setPrefColumnCount(22);
        
        Label emailFieldLabel = new Label("Email address");
        emailField = new TextField();
        emailField.setPrefColumnCount(22);
        
        Label firstNameFieldLabel = new Label("First name");
        firstNameField = new TextField();
        firstNameField.setPrefColumnCount(22);

        Label lastNameFieldLabel = new Label("Last name");
        lastNameField = new TextField();
        lastNameField.setPrefColumnCount(22);

        Label passwordFieldLabel = new Label("Password");
        passwordField = new PasswordField();
        passwordField.setPrefColumnCount(22);

        Label passwordConfirmFieldLabel = new Label("Confirm password");
        passwordConfirmField = new PasswordField();
        passwordConfirmField.setPrefColumnCount(22);
                        
        VBox mainContentPane = new VBox(10);
        mainContentPane.getChildren().addAll(usernameFieldLabel, usernameField, emailFieldLabel, emailField, firstNameFieldLabel, firstNameField, lastNameFieldLabel, lastNameField, passwordFieldLabel, passwordField, passwordConfirmFieldLabel, passwordConfirmField);
        
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, Pair<String, String>>()
        {
            @Override
            public Pair<String, String> call(ButtonType param)
            {
                return ret;
            }
        });
        
        final Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);
        registerButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (!registerUser())
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
                        ArborRegisterDialog.this.usernameField.requestFocus();
                    }
                });
            }
        });
    }
    
    private boolean registerUser()
    {
        //String girderBaseURL = "https://arbor.kitware.com/girder/api/v1/";
        
        if (!passwordField.getText().equals(passwordConfirmField.getText()))
        {
            new Alert(Alert.AlertType.ERROR, "Password and confirm password fields do not match.", ButtonType.OK).showAndWait();
            return false;
        }
        else if (new ArborJsonRetriever().getResponseJson(girderBaseURL + "user?login=" + usernameField.getText() + "&email=" + emailField.getText() + "&firstName=" + firstNameField.getText().replaceAll(" ","%20") + "&lastName=" + lastNameField.getText().replaceAll(" ","%20") + "&password=" + passwordField.getText(), "POST", null) == null)
        {
            new Alert(Alert.AlertType.ERROR, "New user registration failed. Verify email address or username is not in use, all fields are filled in, and that password meets system requirements.", ButtonType.OK).showAndWait();
            return false;
        }
        
        ret = new Pair<>(usernameField.getText(), passwordField.getText());
        
        return true;
    }
}
