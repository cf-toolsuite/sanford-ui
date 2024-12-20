import React, {useEffect, useState} from 'react';
import {ReactAdapterElement} from 'Frontend/generated/flow/ReactAdapter';
import {effect, signal} from "@vaadin/hilla-react-signals";
import Markdown from "react-markdown";

class MarkdownElement extends ReactAdapterElement {

    markdown = signal('');

    protected override render() {
        // In a React component, we could use the signal value directly,
        // but it doesn't trigger an update in the ReactAdapterElement render method.
        // Instead, pass the signal value to useState for React.
        const [content, setContent] = useState('');
        useEffect(() => effect(() => {
                setContent(this.markdown.value);
        }), []);
        return <Markdown>{content}</Markdown>;
    }
}

customElements.define('markdown-component', MarkdownElement);