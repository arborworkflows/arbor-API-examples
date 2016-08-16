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
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author andyyee, templated from ArborPasswordResetDialog (@author awehrer)
 */
public class ArborPasswordChangeDialog extends Dialog<Boolean>
{
    private PasswordField oldField;
    private PasswordField newField;
    private PasswordField newConfirmField;
    private boolean success;
    private String tokStr;
    private final String girderBaseURL;
    
    public ArborPasswordChangeDialog(String token, String girderBaseURL)
    {
        this.tokStr = token;
        
        this.girderBaseURL = girderBaseURL;
        
        this.setTitle("Password Change");
        success = false;
        
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        Label oldFieldLabel = new Label("Enter your old password:");
        oldField = new PasswordField();
        oldField.setPrefColumnCount(22);
        
        Label newFieldLabel = new Label("Enter your new password:");
        newField = new PasswordField();
        newField.setPrefColumnCount(22);
        
        Label newConfirmFieldLabel = new Label("Confirm your new password:");
        newConfirmField = new PasswordField();
        newConfirmField.setPrefColumnCount(22);
        
        VBox mainContentPane = new VBox(10);
        mainContentPane.getChildren().addAll(oldFieldLabel, oldField, newFieldLabel, newField, newConfirmFieldLabel, newConfirmField);
        
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
            if (!newField.getText().equals(newConfirmField.getText()))
            {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "New password does not match confirm password.", ButtonType.OK).showAndWait();
            }
            else if (!changePassword())
            {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "Unable to change password. Verify old password is correct, and check for unsupported characters in the new password.", ButtonType.OK).showAndWait();
            }
            else
            {
                success = true;
                new Alert(Alert.AlertType.CONFIRMATION, "Password successfully changed.", ButtonType.OK).showAndWait();
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
                        ArborPasswordChangeDialog.this.oldField.requestFocus();
                    }
                });
            }
        });
    }
    
    private boolean changePassword()
    {
        //String girderBaseURL = "https://arbor.kitware.com/girder/api/v1";
        ArborJsonRetriever jsonRetriever = new ArborJsonRetriever();
        JsonElement jsonData = jsonRetriever.getResponseJson(girderBaseURL + "/user/password?token=" + tokStr + "&old=" + oldField.getText() + "&new=" + newField.getText(), "PUT");
        
        return jsonData != null;
    }
}
