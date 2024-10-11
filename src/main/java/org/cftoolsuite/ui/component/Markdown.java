package org.cftoolsuite.ui.component;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class Markdown extends Composite<Div> {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();
    private final StringBuilder markdownBuffer = new StringBuilder();
    private final Span rawTextSpan = new Span();
    private boolean isRendered = false;

    public Markdown() {
        getContent().add(rawTextSpan);
    }

    public Markdown(String markdown) {
        this();
        setMarkdown(markdown);
    }

    public void setMarkdown(String markdown) {
        markdownBuffer.setLength(0);
        rawTextSpan.setText("");
        addMarkdown(markdown);
    }

    public void addMarkdown(String markdown) {
        markdownBuffer.append(markdown).append(" ");
        rawTextSpan.setText(markdownBuffer.toString());
    }

    public void render() {
        if (!isRendered) {
            Node document = parser.parse(markdownBuffer.toString());
            String html = renderer.render(document);
            getContent().removeAll();
            getContent().getElement().setProperty("innerHTML", html);
            isRendered = true;
        }
    }

    public void reset() {
        markdownBuffer.setLength(0);
        rawTextSpan.setText("");
        getContent().removeAll();
        getContent().add(rawTextSpan);
        isRendered = false;
    }
}