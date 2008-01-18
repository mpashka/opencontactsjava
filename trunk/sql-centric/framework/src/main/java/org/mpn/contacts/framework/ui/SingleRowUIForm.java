package org.mpn.contacts.framework.ui;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.EventListener;
import org.mpn.contacts.framework.Value;
import org.mpn.contacts.framework.db.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class SingleRowUIForm {

    final static Logger log = Logger.getLogger(SingleRowUIForm.class);

    private Row row;
    private RowState state = RowState.empty;
    private List<DbUiComponent> dbUiComponents = new ArrayList<DbUiComponent>();
    private Set<UiFormListener> listeners = new HashSet<UiFormListener>();
    private boolean updating = false;

    private Set<UiActionListener> actionListeners = new HashSet<UiActionListener>();

    public SingleRowUIForm(Row row) {
        this.row = row;
//        row.addListener(this);
        state = row.hasData() ? RowState.unmodified : RowState.empty;

        row.addListener(new EventListener<Row>() {
            public void onEvent(Row source) {
                updateUiData();
            }
        });
    }

    public void addListener(UiFormListener uiFormListener) {
        listeners.add(uiFormListener);
    }

    public void removeLister(UiFormListener uiFormListener) {
        listeners.remove(uiFormListener);
    }

    private void notifyListeners() {
        for (UiFormListener formListener : listeners) {
            formListener.uiFormUpdated(this);
        }
    }

    public RowState getState() {
        return state;
    }

    public Row getRow() {
        return row;
    }

    public JTextField createJTextField(Field<String> dbFieldMetadata) {
        return createTextField(dbFieldMetadata).getUiComponent();
    }

    public JTextArea createJTextArea(Field<String> dbFieldMetadata) {
        return createTextArea(dbFieldMetadata).getUiComponent();
    }

    public JTextField createJDateField(Field<Date> dbFieldMetadata) {
        return createDateField(dbFieldMetadata).getUiComponent();
    }

    public JTextField createJIntegerField(Field<Integer> dbFieldMetadata) {
        return createIntegerField(dbFieldMetadata).getUiComponent();
    }

    public UiTextField createTextField(Field<String> dbFieldMetadata) {
        UiTextField uiTextField = new UiTextField();
        addUiComponent(uiTextField, dbFieldMetadata);
        return uiTextField;
    }

    public UiTextArea createTextArea(Field<String> dbFieldMetadata) {
        UiTextArea uiTextArea = new UiTextArea();
        addUiComponent(uiTextArea, dbFieldMetadata);
        return uiTextArea;
    }

    public UiDateField createDateField(Field<Date> dbFieldMetadata) {
        UiDateField uiDateField = new UiDateField();
        addUiComponent(uiDateField, dbFieldMetadata);
        return uiDateField;
    }

    public UiIntegerField createIntegerField(Field<Integer> dbFieldMetadata) {
        UiIntegerField uiIntegerField = new UiIntegerField();
        addUiComponent(uiIntegerField, dbFieldMetadata);
        return uiIntegerField;
    }

    public UiComboBoxFixed createComboBoxFixed(Field<Long> dbFieldMetadata, DataSource referTable, Field<Long> referIdField, Field<String> referStringField) {
        UiComboBoxFixed uiComboBoxFixed = new UiComboBoxFixed(referTable, referIdField, referStringField);
        addUiComponent(uiComboBoxFixed, dbFieldMetadata);
        return uiComboBoxFixed;
    }

    public UiComboBoxEditable createComboBoxEditable(Field<Long> dbFieldMetadata, DataSource referTable, Field<Long> referIdField, Field<String> referStringField) {
        UiComboBoxEditable uiComboBoxEditable = new UiComboBoxEditable(referTable, referIdField, referStringField);
        addUiComponent(uiComboBoxEditable, dbFieldMetadata);
        return uiComboBoxEditable;
    }

    public UiComboBoxCached createComboBoxCached(Field<Long> dbFieldMetadata, DataSource referTable, Field<Long> referIdField, Field<String> referStringField) {
        UiComboBoxCached uiComboBoxCached = new UiComboBoxCached(referTable, referIdField, referStringField);
        addUiComponent(uiComboBoxCached, dbFieldMetadata);
        return uiComboBoxCached;
    }

    public <DataType> void addUiComponent(UiComponent<DataType, ?> uiComponent, Field<DataType> dbFieldMetaData) {
        DbUiComponent<DataType> dbUiComponent = new DbUiComponent<DataType>(uiComponent, dbFieldMetaData, row);
        dbUiComponent.update();
        dbUiComponents.add(dbUiComponent);
//        dbUiComponent.addComponentListener(this);
        uiComponent.addListener(new EventListener<Value<DataType>>() {
            /**
             * Is called when ui component updated (by user)
             * @param source
             */
            public void onEvent(Value<DataType> source) {
                uiComponentUpdated();
            }
        });
    }

    public void addListener(UiActionListener listener) {
        actionListeners.add(listener);
    }

    public void removeListener(UiActionListener listener) {
        actionListeners.remove(listener);
    }

    private void updateUiData() {
        updating = true;
        for (DbUiComponent dbUiComponent : dbUiComponents) {
            dbUiComponent.update();
        }
        updating = false;
        state = RowState.unmodified;
        notifyListeners();
    }

    public void rollbackRow() {
//        updateUiData();
        row.update();
    }

    public void commitRow() {
        for (DbUiComponent dbUiComponent : dbUiComponents) {
            dbUiComponent.commit();
        }
        for (UiActionListener uiActionListener : actionListeners) {
            uiActionListener.onCommit(this, row);
        }
        state.commit(row);
        state = RowState.unmodified;
        notifyListeners();
    }

    public void addRow() {
        for (DbUiComponent dbUiComponent : dbUiComponents) {
            dbUiComponent.clear();
        }
        state = RowState.add;
        notifyListeners();
    }

    public void deleteRow() {
        // todo [!] implement delete
        throw new UnsupportedOperationException();
//        for (DbUiComponent uiComponentData : dbUiComponents) {
//            uiComponentData.clear();
//        }

    }

    public void nextRow() {
        row.next();
    }

    public void previousRow() {
        row.previous();
    }

    public void uiComponentUpdated() {
        if (updating) return;
        RowState newState = null;
        if (state == RowState.empty) {
            log.debug("UI Form modified");
            newState = RowState.add;
        } else if (state == RowState.unmodified) {
            newState = RowState.update;
        }
        if (newState != null) {
            state = newState;
            notifyListeners();
        }
    }

    /**
     * Note: currently unused
     */
    public boolean checkChanged() {
        for (DbUiComponent dbUiComponent : dbUiComponents) {
            switch (state) {
                case update:
                    if (dbUiComponent.isDirty()) return true;
                    break;

                case add:
                    if (dbUiComponent.isDirty()) return true;
                    break;
            }
        }
        return true;
    }



}
