/*
 * Copyright (c) 2005-2007 jNetX.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNetX. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with jNetX.
 *
 * $Id$
 */
package org.mpn.contacts.framework.ui;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.DataSource;
import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.Row;
import org.mpn.contacts.framework.EventListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiComboBoxEditable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiComboBoxCached extends UiComponent<Long, JComboBox> implements ComboBoxModel {

    static final Logger log = Logger.getLogger(UiComboBoxCached.class);

    private static final Object LIST_DELIMITER = new Object();

    private static final class CachedComboBoxRenderer implements ListCellRenderer {
        private ListCellRenderer defaultRenderer;
        private JLabel delimiter;

        public CachedComboBoxRenderer(ListCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
            delimiter = new JLabel();

            //Titled borders
            TitledBorder title;
            title = BorderFactory.createTitledBorder("All");
            title.setTitleJustification(TitledBorder.LEFT);
            delimiter.setBorder(title);

        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return value == LIST_DELIMITER ? delimiter
            : defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//            JLabel cellComponent = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//            if (value == LIST_DELIMITER) cellComponent.setText("<html><hr>");
//            return cellComponent;
        }
    }

    private DataSource referTable;
    private Field<Long> referIdField;
    private Field<String> referStringField;
    private JTextField editor;

    private Set<ListDataListener> modelListeners;

    private List<UiComboBoxItem> modelFiltered;
    private List<UiComboBoxItem> modelAll;
    private Object modelSelection;

    public UiComboBoxCached(DataSource referTable, Field<Long> referIdField, Field<String> referStringField) {
        this.referTable = referTable;
        this.referIdField = referIdField;
        this.referStringField = referStringField;

        referTable.addListener(new EventListener<DataSource>() {
            public void onEvent(DataSource source) {
                readData();
                fireModelChanged();
            }
        });
        readData();
    }

    private void readData() {
        modelAll = new ArrayList<UiComboBoxItem>();
        for (Row referTableRow : referTable) {
            Long id = referTableRow.getData(referIdField);
            String text = referTableRow.getData(referStringField);
            modelAll.add(new UiComboBoxItem(id, text));
        }
    }

    public JComboBox createUiComponent() {
        modelListeners = new HashSet<ListDataListener>();
        final JComboBox comboBox = new JComboBox(this);
        comboBox.setEditable(true);
        editor = (JTextField) comboBox.getEditor().getEditorComponent();
        editor.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                updateSelectedList();
                comboBox.setPopupVisible(true);
            }

            public void focusLost(FocusEvent e) {
                comboBox.setPopupVisible(false);
            }
        });
        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSelectedListLater();
            }

            public void removeUpdate(DocumentEvent e) {
                updateSelectedListLater();
            }

            public void changedUpdate(DocumentEvent e) {
                updateSelectedListLater();
            }
        });
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                notifyListeners(UiComboBoxCached.this);
            }
        });

        comboBox.setRenderer(new CachedComboBoxRenderer(comboBox.getRenderer()));

        return comboBox;
    }

    public Long getData() {
        Object selectedItem = uiComponent.getSelectedItem();
        return selectedItem instanceof UiComboBoxItem ? ((UiComboBoxItem) selectedItem).getId() : -1;
    }

//    public String getData() {
//        return uiComponent.getSelectedItem().toString();
//    }
//
    public void setData(Long data) {
//        log.debug("Set data : " + data);
        for (Row referTableRow : referTable) {
            Long id = referTableRow.getData(referIdField);
//            log.debug("  Predefined id:" + id);
            if (id.equals(data)) {
                String text = referTableRow.getData(referStringField);
                uiComponent.setSelectedItem(new UiComboBoxItem(id, text));
            }
        }
    }

    public Long getDefaultValue() {
        return 0l;
    }

//    public long getId() {
//        return editor.getText();
//    }

    //
    // Combo box model
    //

    private void updateSelectedListLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateSelectedList();
            }
        });
    }

    private void updateSelectedList() {
        String editorText = editor.getText().toLowerCase();
//        log.debug("Updating selection list for text:" + editorText);

        List<UiComboBoxItem> oldModel = modelFiltered;
        modelFiltered = new ArrayList<UiComboBoxItem>();
        Object newModelSelection = null;
        for (Row referTableRow : referTable) {
            Long id = referTableRow.getData(referIdField);
            String text = referTableRow.getData(referStringField);
            if (text.toLowerCase().startsWith(editorText)) {
                UiComboBoxItem predefinedItem = new UiComboBoxItem(id, text);
                modelFiltered.add(predefinedItem);
                if (text.length() == editorText.length()) {
                    newModelSelection = predefinedItem;
                }
            }
        }
        if (!modelFiltered.equals(oldModel)) {
            if (modelFiltered.isEmpty()) {
                uiComponent.setPopupVisible(false);
            }
            modelSelection = newModelSelection != null ? newModelSelection : editorText;
            fireModelChanged();
        }
    }

    private void fireModelChanged() {
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1);
        for (ListDataListener listDataListener : modelListeners) {
            listDataListener.contentsChanged(listDataEvent);
        }
    }

    public void setSelectedItem(Object anItem) {
//        log.debug("Item selected : " + anItem);
        modelSelection = anItem;
    }

    public Object getSelectedItem() {
//        log.debug("Item get ");
        return modelSelection;
    }

    public int getSize() {
        int size = 0;
        if (modelFiltered != null) size += modelFiltered.size();
        if (modelAll != null) size += modelAll.size();
        if (modelFiltered != null && modelAll != null) size++;
        return size;
    }

    public Object getElementAt(int index) {
        if (modelFiltered != null) {
            if (index < modelFiltered.size()) return modelFiltered.get(index);
            else if (index == modelFiltered.size()) return LIST_DELIMITER;
            index -= modelFiltered.size();
            index--;
        }
        return modelAll.get(index);
    }

    public void addListDataListener(ListDataListener l) {
        modelListeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        modelListeners.remove(l);
    }
}
