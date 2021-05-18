package Models;

import java.util.Map;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;

public class AppAttributeProvider implements AttributeProvider {

    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {

        if (node instanceof Link) {
            attributes.put("style","text-decoration: none;");
        }

        // If the node is a table
        if (node instanceof TableBlock) {
            attributes.put("style", "border-collapse: collapse; padding: 5px; margin: 5px;");
        }

        // If the node is a table cell
        if (node instanceof TableCell) {
            attributes.put("style", "border: 1px solid #ddd; padding: 5px; margin: 5px;");
        }
    }
}