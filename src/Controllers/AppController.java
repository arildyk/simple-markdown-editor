import java.net.URL;
import java.util.ResourceBundle;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

import Models.FontAttributeProvider;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

public class AppController implements Initializable {
    
    @FXML
    private TextArea input;

    @FXML
    private WebView output;

    @FXML
    private Button exitButton;

    @FXML
    private AnchorPane mainScene;

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void parse() {

        // Change font
        // Add table support
        // Add task list support
        // Get link to open in another browser
        // Fix enter bug
        // Add 'onPaste' method

        Parser parser = Parser.builder().build();

        HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(new AttributeProviderFactory() {

            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                // TODO Auto-generated method stub
                return new FontAttributeProvider();
            }
            
        }).build();

        Node doc = parser.parse(input.getText());
        String outgoing = renderer.render(doc);
        output.getEngine().loadContent(outgoing);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // TODO Auto-generated method stub
    }

}
