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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
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

    @FXML
    private Label fileName;

    @FXML
    private Label filePathName;

    private List<Extension> extensions;
    private Path path;
    private boolean fileChange = false;

    /**
     * Closes the application.
     * @param actionEvent
     * @throws IOException
     */

    public void exit(ActionEvent event) throws IOException {
        if ((!(input.getText().trim().isEmpty()) || !(input.getText() == null)) && (fileChange == true)) {
            Alert alert = new Alert(AlertType.NONE, "Would you like to save your file?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                saveMdFile(event);
            }

            if (alert.getResult() == ButtonType.NO) {
                Platform.exit();
            }

        } else { 
            Platform.exit();
        }
        
    }

    /**
     * Parses the editor into HTML content.
     */

    public void parse() {

        Parser parser = Parser.builder().extensions(extensions).build();

        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).attributeProviderFactory(new AttributeProviderFactory() {

            @Override
            public AttributeProvider create(AttributeProviderContext context) {

                // Add the attribute
                return new AppAttributeProvider();
            }
            
        }).build();

        Node doc = parser.parse(input.getText());

        String outgoing = renderer.render(doc);
        String outString = outgoing.replace("\n", "<br>");
        String processedString = outString.replace("><br><", ">\n<");

        org.jsoup.nodes.Document document = Jsoup.parse(processedString);
        Elements codes = document.getElementsByTag("code");

        for (Element code: codes) {
            String newCode = code.html().replace("<br>", "\n");
            String formattedCode = newCode.replace("&lt;", "<");
            code.text(formattedCode.replace("&gt;", ">"));
        }
        
        String finalProcess = document.toString();

        String htmlString = "<!DOCTYPE html>\n" +
        "        <html>\n" +
        "          <head>\n" +
        "            <meta charset=\"utf-8\"/>\n" +
        "            <style type=\"text/css\">\n" +
        "              @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Regular.ttf); }\n" +
        "              @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Bold.ttf); font-weight: bold; }\n" +
        "              @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Italic.ttf); font-style: italic; }\n" + 
        "              @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-ExtraBold.ttf); font-weight: bolder; }\n" + 
        "              @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-ExtraBoldItalic.ttf); font-weight: bolder; font-style: italic; }\n" + 
        "              @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-BoldItalic.ttf); font-weight: bold; font-style: italic; }\n" +
        "              body { font-family: EB Garamond; font-size: 110%; }\n" +
        "              tr:nth-child(even) { background-color: #f2f2f2; }\n" +
        "              pre { background-color: #f2f2f2; padding: 10px; border-radius: 5px; white-space: pre-wrap; word-wrap: break-word; }\n" +
        "              code { background-color: #f2f2f2; padding: 5px; border-radius: 8px; }\n" +
        "              h1 { border-bottom: 1px solid #f2f2f2; padding-bottom: .3em; }\n" +
        "              h2 { border-bottom: 1px solid #f2f2f2; padding-bottom: .3em; }\n" +
        "            </style>\n" +
        "            <link href=\"" + getClass().getResource("prism.css") + "\"" + " rel=\"stylesheet\"" + " type=\"text/css\"" +  " />\n" +
        "          </head>\n" +
        "          <body>\n" +
        "            <script src=\"" + getClass().getResource("prism.js") + "\"" + " type=\"text/javascript\"" + "></script>\n" +
        finalProcess +
        "          </body>\n" +
        "        </html>";

        output.getEngine().loadContent(htmlString);

        fileChange = true;

        System.out.println(finalProcess);

    }

    public void open() throws IOException {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.md)", "*.md"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File mdFileToLoad = fileChooser.showOpenDialog(null);

        if (mdFileToLoad != null) {
            path = Paths.get(mdFileToLoad.toString());

            byte[] encoded = Files.readAllBytes(path);

            String content = new String(encoded, StandardCharsets.UTF_8);

            fileName.setText(path.getFileName().toString());
            filePathName.setText(path.getParent().toString());

            input.setText(content);

            fileChange = false;
        }
    }

    /**
     * Opens a file chooser for the user to choose a markdown file for editing.
     * @param event
     * @throws IOException
     */

    public void chooseMdFile(ActionEvent event) throws IOException {

        if ((!(input.getText().trim().isEmpty()) || !(input.getText() == null)) && (fileChange == true)) {
            Alert alert = new Alert(AlertType.NONE, "Would you like to save your file?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                saveMdFile(event);
                open();
            }

            if (alert.getResult() == ButtonType.NO) {
                open();
            }

        } else {
            open();
        }
    }

    /**
     * Saves the edits done to the selected markdown file. 
     * If a file hasn't been previously selected, the saveAs(event) function will be called.
     * @param event
     * @throws IOException
     */

    public void saveMdFile(ActionEvent event) throws IOException {

        if (path == null) {
            saveAs(event);
        } else {
            Files.writeString(path, input.getText(), StandardCharsets.UTF_8);
        }

        fileChange = false;
    }

    /**
     * Opens a file chooser where a file will be created where the user will have to specify it's name and location.
     * @param event
     * @throws IOException
     */

    public void saveAs(ActionEvent event) throws IOException {

        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.md)", "*.md"));

        File mdFileToSave = fileChooser.showSaveDialog(null);

        if (mdFileToSave != null) {
            path = Paths.get(mdFileToSave.toString());

            fileName.setText(path.getFileName().toString());
            filePathName.setText(path.getParent().toString());

            Files.writeString(path, input.getText(), StandardCharsets.UTF_8);
        }

        fileChange = false;

    }

    public void newMd() {
        path = null;
        input.clear();
        fileName.setText("");
        filePathName.setText("");
    }

    /**
     * Clears the editor and unselects a file if it has been selected.
     * @param event
     * @throws IOException
     */

    public void newMdFile(ActionEvent event) throws IOException {

        if ((!(input.getText().trim().isEmpty()) || !(input.getText() == null)) && (fileChange == true)) {
            Alert alert = new Alert(AlertType.NONE, "Would you like to save your file?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                saveMdFile(event);
                newMd();
            }

            if (alert.getResult() == ButtonType.NO) {
                newMd();
            }

        } else { 
            newMd();
        }
    }

    /**
     * Minimizes the window.
     * @param event
     */

    public void minimize(ActionEvent event) {
        Stage stage = (Stage) mainScene.getScene().getWindow();

        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // A list of extensions to used in the parsing process
        extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), InsExtension.create(), ImageAttributesExtension.create(), TaskListItemsExtension.create());

        // Open links in the markdown file in the default browser
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

                            // Prevent from opening in the WebView
                            evt.preventDefault();

                            // Open in default browser
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                                try {
                                    Desktop.getDesktop().browse(new URI(href));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                    }, false);
                }
                
            }
        });

        // Parse the editor each time the user types
        input.textProperty().addListener((observableValue, oldValue, newValue) -> {
            parse();
        });

    }

}
