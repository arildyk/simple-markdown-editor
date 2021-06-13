import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import com.lowagie.text.DocumentException;

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
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;
import org.xhtmlrenderer.pdf.ITextRenderer;
import Models.AppAttributeProvider;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class AppController implements Initializable {
    
    @FXML
    private WebView output;

    @FXML
    private AnchorPane mainScene;

    @FXML
    private Label fileName;

    @FXML
    private Label filePathName;

    @FXML
    private AnchorPane codeAreaContainer;

    @FXML
    private AnchorPane webViewContainer;

    @FXML
    private AnchorPane topLeftBar;

    @FXML
    private AnchorPane topRightBar;

    @FXML
    private Button maximizeButton;

    private final CodeArea input = new CodeArea();

    // A list of extensions to used in the parsing process
    private final List<Extension> extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), InsExtension.create(), ImageAttributesExtension.create(), TaskListItemsExtension.create());
    
    private final VirtualizedScrollPane<CodeArea> sp = new VirtualizedScrollPane<>(input);
    private Path path;
    private boolean fileChange = false;
    private String lastOpenedDirectory = System.getProperty("user.home"); 
    private String lastSavedDirectory = System.getProperty("user.home");
    private String lastSavedHTMLDirectory = System.getProperty("user.home");
    private String lastSavedPDFDirectory = System.getProperty("user.home");
    private String htmlContent;

    /**
     * Closes the application.
     * @param actionEvent
     * @throws IOException
     */

    public final void exit(ActionEvent event) throws IOException {
        if ((!(input.getText().trim().isEmpty()) || !(input.getText() == null)) && (fileChange == true)) {

            final Alert alert = new Alert(AlertType.NONE, "Would you like to save your file?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

            alert.setTitle("Save File?");
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

    public final void parse() {

        final Parser parser = Parser.builder().extensions(extensions).build();

        final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).attributeProviderFactory(new AttributeProviderFactory() {

            @Override
            public AttributeProvider create(AttributeProviderContext context) {

                // Add the attribute
                return new AppAttributeProvider();
            }
            
        }).build();

        final Node doc = parser.parse(input.getText());

        final String outgoing = renderer.render(doc);
        final String outString = outgoing.replace("\n", "<br>");
        final String processedString = outString.replace("><br><", ">\n<");

        final org.jsoup.nodes.Document document = Jsoup.parse(processedString);
        final Elements codes = document.getElementsByTag("code");

        for (Element code: codes) {
            final String newCode = code.html().replace("<br>", "\n");
            final String formattedCode = newCode.replace("&lt;", "<");
            code.text(formattedCode.replace("&gt;", ">"));
        }

        final Elements imageElements = document.getElementsByTag("img");

        for (Element img : imageElements) {
            final String srcValue = img.attr("src");
            img.attr("src", path.getParent().toUri() + "/" + srcValue);
        }
        
        final String finalProcess = document.toString();

        final String htmlString = "<!DOCTYPE html>\n" +
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
        "              body { font-family: EB Garamond; }\n" +
        "              img { max-width: 100%; max-height: 100%; }\n" +
        "              tr:nth-child(even) { background-color: #f2f2f2; }\n" +
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

        htmlContent = htmlString;

        fileChange = true;
    }

    public final void open() throws IOException {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.md)", "*.md"));
        fileChooser.setInitialDirectory(new File(lastOpenedDirectory));

        final File mdFileToLoad = fileChooser.showOpenDialog(null);

        if (mdFileToLoad != null) {
            path = Paths.get(mdFileToLoad.toString());

            final byte[] encoded = Files.readAllBytes(path);

            final String content = new String(encoded, StandardCharsets.UTF_8);

            fileName.setText(path.getFileName().toString());
            filePathName.setText(path.getParent().toString());

            input.replaceText(content);

            fileChange = false;
        }

        lastOpenedDirectory = mdFileToLoad.getParent();
    }

    /**
     * Opens a file chooser for the user to choose a markdown file for editing.
     * @param event
     * @throws IOException
     */

    public final void chooseMdFile(ActionEvent event) throws IOException {

        if ((!(input.getText().trim().isEmpty()) || !(input.getText() == null)) && (fileChange == true)) {

            final Alert alert = new Alert(AlertType.NONE, "Would you like to save your file?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

            alert.setTitle("Save File?");
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

    public final void saveMdFile(ActionEvent event) throws IOException {

        if (path == null) {
            saveAs(event);
        } else {
            Files.writeString(path, input.getText(), StandardCharsets.UTF_8);
        }

        fileChange = false;
    }

    private String htmlToXhtml(String html) {
        org.jsoup.nodes.Document document = Jsoup.parse(html);
        document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        return document.html();
    }

    /**
     * Opens a file chooser where a file will be created where the user will have to specify it's name and location.
     * @param event
     * @throws IOException
     */

    public final void saveAs(ActionEvent event) throws IOException {

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter markdownFilter = new FileChooser.ExtensionFilter("Markdown files (*.md)", "*.md");

        fileChooser.getExtensionFilters().add(markdownFilter);
        fileChooser.setInitialDirectory(new File(lastSavedDirectory));

        final File mdFileToSave = fileChooser.showSaveDialog(null);

        if (mdFileToSave != null) {
            path = Paths.get(mdFileToSave.toString());

            fileName.setText(path.getFileName().toString());
            filePathName.setText(path.getParent().toString());

            Files.writeString(path, input.getText(), StandardCharsets.UTF_8);

            fileChange = false;

        } 
        
        lastSavedDirectory = mdFileToSave.getParent();
    }

    public final void exportAsPDF(ActionEvent event) throws IOException, DocumentException {

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf");

        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.setInitialDirectory(new File(lastSavedPDFDirectory));

        final File mdFileToExport = fileChooser.showSaveDialog(null);

        if (mdFileToExport != null) {

            Path pdfPath = Paths.get(mdFileToExport.toString());

            File output = new File(pdfPath.toString());

            OutputStream os = new FileOutputStream(output);

            ITextRenderer iTextRenderer = new ITextRenderer();
            iTextRenderer.setDocumentFromString(htmlToXhtml(htmlContent));
            iTextRenderer.layout();
            iTextRenderer.createPDF(os);
            
            os.close();

            fileChange = false;
        }

        lastSavedPDFDirectory = mdFileToExport.getParent();
    }

    public final void exportAsHTML(ActionEvent event) throws IOException {

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter htmlFilter = new FileChooser.ExtensionFilter("HTML Files (*.html, *.htm)", "*.html", "*.htm");

        fileChooser.getExtensionFilters().add(htmlFilter);
        fileChooser.setInitialDirectory(new File(lastSavedHTMLDirectory));

        final File mdFileToExport = fileChooser.showSaveDialog(null);

        if (mdFileToExport != null) {

            Path htmlPath = Paths.get(mdFileToExport.toString());

            Files.writeString(htmlPath, htmlContent, StandardCharsets.UTF_8);

            fileChange = false;
        }

        lastSavedHTMLDirectory = mdFileToExport.getParent();
    }

    public final void newMd() {
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

    public final void newMdFile(ActionEvent event) throws IOException {

        if ((!(input.getText().trim().isEmpty()) || !(input.getText() == null)) && (fileChange == true)) {

            final Alert alert = new Alert(AlertType.NONE, "Would you like to save your file?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            
            alert.setTitle("Save File?");
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

    public final void minimize(ActionEvent event) {
        Stage stage = (Stage) mainScene.getScene().getWindow();

        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    public final void maximize(ActionEvent event) {
        final Stage stage = (Stage) mainScene.getScene().getWindow();

        if (stage.isMaximized()) {
            stage.setMaximized(false);
            AnchorPane.setRightAnchor(codeAreaContainer, 670.0);
            AnchorPane.setLeftAnchor(webViewContainer, 670.0);
            AnchorPane.setRightAnchor(topLeftBar, 670.0);
            AnchorPane.setLeftAnchor(topRightBar, 670.0);
            maximizeButton.setText("☐");
        } else {
            stage.setMaximized(true);
            AnchorPane.setRightAnchor(codeAreaContainer, stage.getWidth() / 2.0);
            AnchorPane.setLeftAnchor(webViewContainer, stage.getWidth() / 2.0);
            AnchorPane.setRightAnchor(topLeftBar, stage.getWidth() / 2.0);
            AnchorPane.setLeftAnchor(topRightBar, stage.getWidth() / 2.0);
            
            final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            maximizeButton.setText("❒");
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Open links in the markdown file in the default browser
        output.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldValuState, nState) -> {

            if (nState == State.SUCCEEDED) {

                final Document document = output.getEngine().getDocument();

                final NodeList nodeList = document.getElementsByTagName("a");

                for (int i = 0; i < nodeList.getLength(); i++) {

                    final org.w3c.dom.Node node = nodeList.item(i);

                    final EventTarget eventTarget = (EventTarget) node;

                    eventTarget.addEventListener("click", new EventListener() {
                        
                        @Override
                        public void handleEvent(Event evt) {
                            
                            final EventTarget target = evt.getCurrentTarget();
                            final HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                            final String href = anchorElement.getHref();

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

        input.getStylesheets().add("Views/application.css");
        input.setPrefHeight(710.0);
        input.setPrefWidth(670.0);

        AnchorPane.setLeftAnchor(input, 0.0);
        AnchorPane.setRightAnchor(input, 0.0);
        AnchorPane.setBottomAnchor(input, 5.0);
        AnchorPane.setTopAnchor(input, 5.0);

        input.prefWidthProperty().bind(codeAreaContainer.widthProperty());
        input.prefHeightProperty().bind(codeAreaContainer.heightProperty());
        input.setParagraphGraphicFactory(LineNumberFactory.get(input));
        input.setLineHighlighterFill(Color.web("#282828"));
        input.setLineHighlighterOn(true);
        input.setWrapText(true);

        codeAreaContainer.getChildren().add(sp);

        AnchorPane.setLeftAnchor(sp, 0.0);
        AnchorPane.setRightAnchor(sp, 0.0);
        AnchorPane.setBottomAnchor(sp, 5.0);
        
        // Parse the editor each time the user types
        input.textProperty().addListener((observableValue, oldValue, newValue) -> {
            parse();
            System.out.println(htmlContent);
        });
    }

}