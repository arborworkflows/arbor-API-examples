*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import com.google.gson.JsonElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.ImageIO;
import phylopen.utility.SingleDirectoryFileChooserDialog;
import phylopen.utility.ink.StylusEvent;
import phylopen.utility.ink.recognition.InkGesture;
import phylopen.utility.ink.recognition.InkGestureListener;

/**
 *
 * @author awehrer
 */
public class MainWindowController implements Initializable
{
    private final double widthHeightChangeDelta;
    private ArborUserInfo userInfo;
    private PhyloPenOptions optionsEntry;
    private UserEventRecorder eventRecorder;
    private File imageSaveDirectory;
    
    public MainWindowController()
    {
        sidebarAtRight = true;
        widthHeightChangeDelta = 100.0;
    }
    
    @FXML
    private void handleExitAction(ActionEvent event)
    {
        System.exit(0);
    }
    
    @FXML
    private void handleOptionsAction(ActionEvent event)
    {
        new PhyloPenOptionsDialog(optionsEntry).showAndWait();
    }
    
   
    
    @FXML
    private void handleOpenAction(ActionEvent event)
    {
        Optional<String> result = new ArborOpenDialog(userInfo, optionsEntry.getGirderBaseURL()).showAndWait();
        
        if (result.isPresent() && result.get() != null)
        {
            final String path = result.get();
            
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        loadTreeModel(path, false);
                    }
                    catch (IOException e) {}
                }
            });
        }
    }
    
    
   
    
    @FXML private MenuItem loginMenuItem;
    @FXML private MenuItem userMenuItem;
    @FXML private Button loginButton;
    @FXML private SplitMenuButton userButton;
    
    @FXML
    private void handleLoginAction(ActionEvent event)
    {
        if (userInfo == null)
        {
            Optional<ArborUserInfo> result = new ArborLoginDialog(optionsEntry.getGirderBaseURL()).showAndWait();
            userInfo = (result.isPresent() ? result.get() : null);
            
            if (userInfo != null)
            {
                userMenuItem.setText("Logged in as " + userInfo.getUsername());
                userMenuItem.setVisible(true);
                loginMenuItem.setVisible(false);
                
                
                userButton.setText("Logged in as " + userInfo.getUsername() + " ("
                        + userInfo.getFirstName() + " " + userInfo.getLastName() + ")");
                userButton.setVisible(true);
                userButton.setMinWidth(Region.USE_PREF_SIZE);
                userButton.setMaxWidth(Region.USE_PREF_SIZE);
                loginButton.setVisible(false);
                loginButton.setMinWidth(0.0);
                loginButton.setMaxWidth(0.0);
            }
        }
    }
    
    @FXML
    private void handleLogOutAction(ActionEvent event)
    {
        final String girderBaseURL = optionsEntry.getGirderBaseURL();
        
        JsonElement jsonData = new ArborJsonRetriever().getResponseJson(girderBaseURL + "user/authentication?token=" + userInfo.getAuthenticationToken(), "DELETE");

        if (jsonData != null)
        {
            new Alert(Alert.AlertType.CONFIRMATION, jsonData.getAsJsonObject().get("message").getAsString(), ButtonType.OK).showAndWait();
            userInfo = null;
            
            userMenuItem.setText("Logged in as Guest");
            userMenuItem.setVisible(false);
            loginMenuItem.setVisible(true);
            
            userButton.setText("");
            userButton.setVisible(false);
            userButton.setMinWidth(0.0);
            userButton.setMaxWidth(0.0);
            loginButton.setVisible(true);
            loginButton.setMinWidth(Region.USE_PREF_SIZE);
            loginButton.setMaxWidth(Region.USE_PREF_SIZE);
        }
        else
        {
            new Alert(Alert.AlertType.ERROR, "Log out failed.", ButtonType.OK).showAndWait();
        }
    }
    
    @FXML
    private void handleUpdateUserInfoAction(ActionEvent event)
    {
        Optional<Boolean> result = new ArborUpdateUserInfoDialog(userInfo, optionsEntry.getGirderBaseURL()).showAndWait();
        
        // if user information updated, update relevant labels
        if (result.isPresent() && result.get())
        {
            userButton.setText("User: " + userInfo.getUsername() + " ("
                        + userInfo.getFirstName() + " " + userInfo.getLastName() + ")");
        }
    }
    
    @FXML
    private void handleChgPassAction(ActionEvent event)
    {
        new ArborPasswordChangeDialog(userInfo.getAuthenticationToken(), optionsEntry.getGirderBaseURL()).showAndWait();
    }
    
 
    