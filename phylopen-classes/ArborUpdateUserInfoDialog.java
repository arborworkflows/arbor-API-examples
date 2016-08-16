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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
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
public class ArborUpdateUserInfoDialog extends Dialog<Boolean>
{
    private final ArborUserInfo userInfo;
    private final TextField firstNameField;
    private final TextField lastNameField;
    private final TextField emailField;
    private final String girderBaseURL;
    
    public ArborUpdateUserInfoDialog(ArborUserInfo userInfo, String girderBaseURL)
    {
        this.setTitle("View/Update Account Information");
        
        this.userInfo = userInfo;
        this.girderBaseURL = girderBaseURL;
        
        final ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(updateButtonType);
        
        Label firstNameFieldLabel = new Label("First name");
        firstNameField = new TextField(userInfo.getFirstName());
        firstNameField.setPrefColumnCount(22);

        Label lastNameFieldLabel = new Label("Last name");
        lastNameField = new TextField(userInfo.getLastName());
        lastNameField.setPrefColumnCount(22);

        Label emailFieldLabel = new Label("Email address");
        emailField = new TextField(userInfo.getEmailAddress());
        emailField.setPrefColumnCount(22);
                        
        VBox mainContentPane = new VBox(10);
        mainContentPane.getChildren().addAll(firstNameFieldLabel, firstNameField, lastNameFieldLabel, lastNameField, emailFieldLabel, emailField);
        
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, Boolean>()
        {
            @Override
            public Boolean call(ButtonType param)
            {
                return (param.equals(updateButtonType));
            }
        });
        
        final Button registerButton = (Button) getDialogPane().lookupButton(updateButtonType);
        registerButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (!updateUserInfo())
            {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "Information failed to update.", ButtonType.OK).showAndWait();
            }
            else
            {
                userInfo.setFirstName(firstNameField.getText());
                userInfo.setLastName(lastNameField.getText());
                userInfo.setEmailAddress(emailField.getText());
                new Alert(Alert.AlertType.CONFIRMATION, "Information successfully updated.", ButtonType.OK).showAndWait();
            }
        });
        
        this.setOnShown(new EventHandler<DialogEvent>()
        {
            @Override
            public void handle(DialogEvent event)
            {
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Button closeButton = (Button) ArborUpdateUserInfoDialog.this.getDialogPane().lookupButton(ButtonType.CLOSE);
                        closeButton.requestFocus();
                    }
                });
            }
        });
    }
    
    private boolean updateUserInfo()
    {
        //String girderBaseURL = "https://arbor.kitware.com/girder/api/v1/";
        return new ArborJsonRetriever().getResponseJson(girderBaseURL + "user/" + userInfo.getId() + "?token=" + userInfo.getAuthenticationToken() + "&firstName=" + firstNameField.getText().replaceAll(" ","%20") + "&lastName=" + lastNameField.getText().replaceAll(" ","%20") + "&email=" + emailField.getText(), "PUT", null) != null;
    }
}
