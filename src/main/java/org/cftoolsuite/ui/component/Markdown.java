package org.cftoolsuite.ui.component;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;

@Tag("div")
@JavaScript("https://cdnjs.cloudflare.com/ajax/libs/prism/9000.0.1/prism.min.js")
@JavaScript("https://cdnjs.cloudflare.com/ajax/libs/prism/9000.0.1/components/prism-markdown.min.js")
@StyleSheet("https://cdnjs.cloudflare.com/ajax/libs/prism-themes/1.9.0/prism-nord.min.css")
public class Markdown extends Html {

    private StringBuilder builder = new StringBuilder();

    public Markdown() {
        super("<pre><code class=\"language-markdown\"></code></pre>");
        getElement().executeJs("Prism.highlightElement($0)", getElement());
    }

    public void setSource(String code) {
        if (code.startsWith("-")) {
            builder.append(System.lineSeparator());
            builder.append(System.lineSeparator());
        }
        builder.append(" " + code);
        setHtmlContent("<pre><code class=\"language-markdown\">" +
              escapeHtml(builder.toString()) + "</code></pre>");
        getElement().executeJs("Prism.highlightElement($0)", getElement());
    }

    private static String escapeHtml(String html) {
        return html.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#039;");
    }
}
