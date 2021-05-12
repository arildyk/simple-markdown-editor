package Models;

import java.util.Map;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.renderer.html.AttributeProvider;

public class AppAttributeProvider implements AttributeProvider {
    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        if (node instanceof Paragraph) {
            attributes.put("style", "@font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Regular.ttf); } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Bold.ttf); font-weight: bold; }  @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Italic.ttf); font-style: italic; } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-ExtraBold.ttf); font-weight: bolder; } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-ExtraBoldItalic.ttf); font-weight: bolder; font-style: italic; } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-BoldItalic.ttf); font-weight: bold; font-style: italic; } font-family: EB Garamond");
        }
        
        if (node instanceof Heading) {
            attributes.put("style", "@font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Regular.ttf); } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Bold.ttf); font-weight: bold; }  @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-Italic.ttf); font-style: italic; } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-ExtraBold.ttf); font-weight: bolder; } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-ExtraBoldItalic.ttf); font-weight: bolder; font-style: italic; } @font-face { font-family: EB Garamond; src: url(Fonts/EBGaramond-BoldItalic.ttf); font-weight: bold; font-style: italic; } font-family: EB Garamond");
        }

        if (node instanceof TableBlock) {
            attributes.put("border", "1");
        }
    }
}