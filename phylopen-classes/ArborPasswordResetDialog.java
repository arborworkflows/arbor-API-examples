/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonElement;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author awehrer
 */
public class ArborPasswordResetDialog extends Dialog<Boolean>
{
    private TextField emailField;
    private boolean success;
    private final String girderBaseURL;
    
    public ArborPasswordResetDialog(String girderBaseURL)
    {
        this.setTitle("Password Reset");
        success = false;
        
        this.girderBaseURL = girderBaseURL;
        
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        Label emailFieldLabel = new Label("Enter your email address:");
        emailField = new TextField();
        emailField.setPrefColumnCount(22);
        VBox mainContentPane = new VBox(10);
        mainContentPane.getChildren().addAll(emailFieldLabel, emailField);
        
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, Boolean>()
        {
            @Override
            public Boolean call(ButtonType param)
            {
                return success;
            }
        });
        
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (!resetPassword())
            {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "That email address is not registered with the system.", ButtonType.OK).showAndWait();
            }
            else
            {
                success = true;
                new Alert(Alert.AlertType.CONFIRMATION, "Your password has been reset. An email should arrive shortly with your new password.", ButtonType.OK).showAndWait();
            }
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
                        ArborPasswordResetDialog.this.emailField.requestFocus();
                    }
                });
            }
        });
    }
    
    private boolean resetPassword()
    {
        //String girderBaseURL = "https://arbor.kitware.com/girder/api/v1/";
        ArborJsonRetriever jsonRetriever = new ArborJsonRetriever();
        JsonElement jsonData = jsonRetriever.getResponseJson(girderBaseURL + "/user/password?email=" + emailField.getText(), "DELETE");
        
        return jsonData != null;
    }
}
