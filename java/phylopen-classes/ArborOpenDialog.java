/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

/**
 *
 * @author awehrer
 */

// [ayee] this article describes using a query parameter to authenticate:
//       https://github.com/girder/girder/blob/master/docs/developer-cookbook.rst
//   "The authToken.token string is the token value you should pass in subsequent API
//    calls, which should either be passed as the token parameter in the query or form
//    parameters, or as the value of a custom HTTP header with the key Girder-Token..."
public class ArborOpenDialog extends Dialog<String>
{
    private TreeItem<ArborItem> selectedItem;
    private JsonArray jsonCollectionArray;
    //private List<Pair<String, String>> requestHeaderProperties;
    private String authTokenStr;
    private String authTokenStr2;
    private final String girderBaseURL;
    
    public ArborOpenDialog(ArborUserInfo userInfo, String girderBaseURL)
    {
        this.setTitle("Open");
        
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        final TreeItem<ArborItem> rootItem = new TreeItem<>(new ArborItem("Collections", null));
        rootItem.setExpanded(true);
        //rootItem.setGraphic(new ImageView(new Image("file:resources/images/stars_icon.png", 16, 16, true, true)));
        
        this.girderBaseURL = girderBaseURL;
        
        ArborJsonRetriever jsonRetriever = new ArborJsonRetriever();
        //requestHeaderProperties = null;
        
        if (userInfo != null)
        {
            //requestHeaderProperties = new ArrayList<>(1);
            //requestHeaderProperties.add(new Pair<>("Girder-Token", userInfo.getAuthenticationToken()));
            authTokenStr = "?token=" + userInfo.getAuthenticationToken();
            authTokenStr2 = "&token=" + userInfo.getAuthenticationToken();
        }
        else
        {
            authTokenStr = "";
            authTokenStr2 = "";
        }
        
        //List<Pair<String, String>> requestHeaderProperties;
        //requestHeaderProperties = new ArrayList<>(1);
        //requestHeaderProperties.add(new Pair<>("Content-Length", "0"));
        
        //System.out.println("password change: " + jsonRetriever.getResponseJson("https://arbor.kitware.com/girder/api/v1/user/password?token=bUbEpZjI6jTIm9PumOdvbnAFn3JaFT0EXtjVI28pp7A2BS1lDlPHrPgbXo5uct0d&old=OLAOP2CTn8tJ&new=", "PUT"));
        
                
        jsonCollectionArray = jsonRetriever.getResponseJson(girderBaseURL + "collection" + authTokenStr, "GET").getAsJsonArray();
        JsonArray jsonCollectionFolderArray;
        JsonArray jsonFileArray;
        JsonObject jsonObject;
        String dataFolderId, fileName;
        TreeItem<ArborItem> collectionItem, fileItem;
        
        for (JsonElement collectionElement : jsonCollectionArray)
        {
            jsonObject = collectionElement.getAsJsonObject();
            collectionItem = new TreeItem<>(new ArborItem(jsonObject.get("name").getAsString(), jsonObject.get("_id").getAsString()));
            //System.out.println("\n\n" + collectionElement);
            
            /*if (!jsonObject.get("public").getAsBoolean())
                collectionItem.setGraphic(new ImageView(new Image("file:resources/images/login_icon.png", 16, 16, true, true)));
            else
                collectionItem.setGraphic(new ImageView(new Image("file:resources/images/star_icon_16_x_16.png", 16, 16, true, false)));*/
            
            jsonCollectionFolderArray = jsonRetriever.getResponseJson(girderBaseURL + "folder?parentType=collection&parentId="
                                                            + jsonObject.get("_id")
                                                            .getAsString() + authTokenStr2, "GET").getAsJsonArray();
            dataFolderId = null;
            
            // select the data folder in the collection
            for (JsonElement folderElement : jsonCollectionFolderArray)
            {
                jsonObject = folderElement.getAsJsonObject();
                
                if (jsonObject.has("name") && jsonObject.get("name").getAsString().equals("Data"))
                {
                    dataFolderId = jsonObject.get("_id").getAsString();
                    break;
                }
            }
            
            jsonFileArray = jsonRetriever.getResponseJson(girderBaseURL + "item?folderId=" + dataFolderId + authTokenStr2, "GET").getAsJsonArray();
            
            for (JsonElement fileElement : jsonFileArray)
            {
                fileName = fileElement.getAsJsonObject().get("name").getAsString();
                
                if (fileName.toLowerCase().endsWith(".phy") || fileName.toLowerCase().endsWith(".nested-json"))
                {
                    fileItem = new TreeItem<>(new ArborItem(fileName, fileElement.getAsJsonObject().get("_id").getAsString()));
                    collectionItem.getChildren().add(fileItem);
                    
                    //System.out.println(" >> " + fileName);
                }
            }
            
            rootItem.getChildren().add(collectionItem);
        }
        
        TreeView<ArborItem> collectionList = new TreeView<>(rootItem);
        collectionList.setPrefSize(400, 400);
        getDialogPane().setContent(collectionList);
        this.setResizable(true);
        
        collectionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ArborItem>>()
        {
            @Override
            public void changed(ObservableValue<? extends TreeItem<ArborItem>> observable, TreeItem<ArborItem> oldValue, TreeItem<ArborItem> newValue)
            {
                // if the selected item is neither the root nor a collection name.
                if (newValue != null && newValue.getParent() != null && newValue.getParent().getParent() != null)
                    selectedItem = newValue;
                else
                    selectedItem = null;
                
            }
        });
        // set result converter
        this.setResultConverter(new Callback<ButtonType, String>()
        {
            @Override
            public String call(ButtonType param)
            {
                if (param.equals(ButtonType.OK))
                {
                    String itemId = selectedItem.getValue().getId();
                    String itemName = selectedItem.getValue().getName();
                    
                    String url = (itemName.toLowerCase().endsWith(".phy")) ?
                            (girderBaseURL + "item/" + itemId + "/romanesco/tree/newick/nested" + authTokenStr) :
                            (girderBaseURL + "item/" + itemId + "/download" + authTokenStr);
                    
                    /*String url = girderBaseURL + "item/" + itemId + "/romanesco/tree/"
                            + (itemName.endsWith(".phy") ? "newick" : "nested")
                            + "/nested" + authTokenStr;*/
                    
                    //System.out.println(jsonRetriever.getResponseJson(url, "GET"));
                    
                    return url;
                }
                else
                    return null;
            }
        });
        
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (ArborOpenDialog.this.selectedItem == null)
            {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "No data file selected.", ButtonType.OK).showAndWait();
            }
        });
    }
    
    private class ArborItem
    {
        private final String name;
        private final String id;
        
        public ArborItem(String name, String id)
        {
            this.name = name;
            this.id = id;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getId()
        {
            return id;
        }
        
        @Override
        public String toString()
        {
            return getName();
        }
    }
}
