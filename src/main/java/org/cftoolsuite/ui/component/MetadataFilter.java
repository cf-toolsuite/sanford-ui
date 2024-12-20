package org.cftoolsuite.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.cftoolsuite.domain.chat.FilterMetadata;

import java.util.ArrayList;
import java.util.List;

public class MetadataFilter extends CustomField<List<FilterMetadata>> {
    private final Grid<MetadataEntry> grid;
    private final List<MetadataEntry> entries;
    private final TextField keyField;
    private final TextField valueField;
    private final Button addButton;

    public MetadataFilter() {
        entries = new ArrayList<>();
        grid = new Grid<>();

        grid.addColumn(MetadataEntry::getKey).setHeader("Key").setFlexGrow(1);
        grid.addColumn(MetadataEntry::getValue).setHeader("Value").setFlexGrow(1);
        grid.addComponentColumn(this::createRemoveButton).setWidth("100px").setFlexGrow(0);

        grid.setItems(entries);

        keyField = new TextField("Key");
        valueField = new TextField("Value");
        addButton = new Button("Add", VaadinIcon.PLUS.create());
        addButton.addClickListener(e -> addEntry());

        HorizontalLayout inputLayout = new HorizontalLayout(keyField, valueField, addButton);
        inputLayout.setWidth("100%");
        inputLayout.setAlignItems(FlexComponent.Alignment.END);

        VerticalLayout layout = new VerticalLayout(inputLayout, grid);
        layout.setSpacing(false);
        layout.setPadding(false);
        add(layout);
    }

    private Button createRemoveButton(MetadataEntry entry) {
        Button removeButton = new Button(VaadinIcon.TRASH.create());
        removeButton.addClickListener(e -> {
            entries.remove(entry);
            grid.getDataProvider().refreshAll();
            updateValue();
        });
        return removeButton;
    }

    private void addEntry() {
        String key = keyField.getValue().trim();
        String value = valueField.getValue().trim();

        if (!key.isEmpty() && !value.isEmpty()) {
            entries.add(new MetadataEntry(key, value));
            keyField.clear();
            valueField.clear();
            grid.getDataProvider().refreshAll();
            updateValue();
        }
    }

    @Override
    protected List<FilterMetadata> generateModelValue() {
        if (entries.isEmpty()) {
            return null;
        }

        List<FilterMetadata> metadata = new ArrayList<>();
        for (MetadataEntry entry : entries) {
            metadata.add(new FilterMetadata(entry.getKey(), entry.getValue()));
        }
        return metadata;
    }

    @Override
    public void setPresentationValue(List<FilterMetadata> metadata) {
        entries.clear();
        if (metadata != null) {
            metadata.forEach(fm ->
                entries.add(new MetadataEntry(fm.key(), fm.value()))
            );
        }
        grid.getDataProvider().refreshAll();
    }

    private static class MetadataEntry {
        private final String key;
        private final Object value;

        public MetadataEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}
