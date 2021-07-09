package cz.zcu.kiv.dfs_simulator.view.toolbar;

import cz.zcu.kiv.dfs_simulator.helpers.FxHelper;
import cz.zcu.kiv.dfs_simulator.view.content.ModelGraphLayoutPane;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class ToolbarPanel extends AnchorPane
{

    @FXML private VBox library;
    
    private final ModelGraphLayoutPane graphLayoutPane;

    public ToolbarPanel(ModelGraphLayoutPane graphLayoutPane)
    {
        this.fxInit();
        this.initLibrary();
        
        this.graphLayoutPane = graphLayoutPane;
    }
    
    private void fxInit()
    {
        FxHelper.loadFXMLAndSetController(getClass().getClassLoader().getResource("fxml/view/toolbar/ToolbarPanel.fxml"), this);
    }

    private void initLibrary()
    {
        this.addNode("Server", new LibraryServerNode(this));
        this.addNode("Client", new LibraryClientNode(this));
    }

    private void addNode(String label, Node node)
    {
        if(label != null)
        {
            Label l = new Label(label);
            // @TODO replace those hardcoded values with actual calculations?
            l.setPadding(new Insets(0, 0, -20, 21));
            
            this.library.getChildren().add(l);
        }
        
        this.library.getChildren().add(node);
    }
    
    public ModelGraphLayoutPane getGraphLayoutPane()
    {
        return this.graphLayoutPane;
    }
}
