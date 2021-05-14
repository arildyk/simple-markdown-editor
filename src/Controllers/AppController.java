import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import Models.AppAttributeProvider;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AppController implements Initializable {
    
    @FXML
    private TextArea input;

    @FXML
    private WebView output;

    @FXML
    private AnchorPane mainScene;

    private List<Extension> extensions;
    private Path path;

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void parse() {

        Parser parser = Parser.builder().extensions(extensions).build();

        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).attributeProviderFactory(new AttributeProviderFactory() {

            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                // TODO Auto-generated method stub
                return new AppAttributeProvider();
            }
            
        }).build();

        Node doc = parser.parse(input.getText());
        String outgoing = renderer.render(doc);
        String outString = outgoing.replace("\n", "<br>");
        String processedString = outString.replace("><br><", ">\n<");

        System.out.println(processedString);

        output.getEngine().loadContent(processedString);
    }

    public void chooseMdFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.md)", "*.md"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File mdFileToLoad = fileChooser.showOpenDialog(null);

        path = Paths.get(mdFileToLoad.toString());

        byte[] encoded = Files.readAllBytes(path);

        String content = new String(encoded, StandardCharsets.UTF_8);

        input.setText(content);
    }

    public void saveMdFile(ActionEvent event) throws IOException {
        if (path == null) {
            saveAs(event);
        } else {
            Files.writeString(path, input.getText(), StandardCharsets.UTF_8);
        }
    }

    public void saveAs(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.md)", "*.md"));

        File mdFileToSave = fileChooser.showSaveDialog(null);

        path = Paths.get(mdFileToSave.toString());

        if (mdFileToSave != null) {
            Files.writeString(path, input.getText(), StandardCharsets.UTF_8);
        }
    }

    public void minimize(ActionEvent event) {
        Stage stage = (Stage) mainScene.getScene().getWindow();
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        stage.setIconified(true);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // TODO Auto-generated method stub

        extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), InsExtension.create(), ImageAttributesExtension.create(), TaskListItemsExtension.create());

        output.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldValuState, nState) -> {

            if (nState == State.SUCCEEDED) {

                Document document = output.getEngine().getDocument();

                NodeList nodeList = document.getElementsByTagName("a");

                for (int i = 0; i < nodeList.getLength(); i++) {

                    org.w3c.dom.Node node= nodeList.item(i);

                    EventTarget eventTarget = (EventTarget) node;

                    eventTarget.addEventListener("click", new EventListener() {
                        
                        @Override
                        public void handleEvent(Event evt) {
                            
                            EventTarget target = evt.getCurrentTarget();
                            HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                            String href = anchorElement.getHref();

                            //Opening the URL outside the WebView
                            evt.preventDefault();

                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                try {
                                    Desktop.getDesktop().browse(new URI(href));
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (URISyntaxException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }

                    }, false);
                }
                
            }
        });

        input.textProperty().addListener((observableValue, oldValue, newValue) -> {
            parse();
        });

    }

}
