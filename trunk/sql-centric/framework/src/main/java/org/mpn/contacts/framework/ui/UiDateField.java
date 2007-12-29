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

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiTextField here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiDateField extends UiComponent<Date, JTextField> {

    static final Logger log = Logger.getLogger(UiDateField.class);

    private DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public JTextField createUiComponent() {
        JTextField jTextField = new JTextField();
        jTextField.setDragEnabled(true);
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                notifyListeners(UiDateField.this);
            }

            public void removeUpdate(DocumentEvent e) {
                notifyListeners(UiDateField.this);
            }

            public void changedUpdate(DocumentEvent e) {
                notifyListeners(UiDateField.this);
            }
        });
        return jTextField;
    }

    public Date getData() {
        String text = uiComponent.getText();
        try {
            return DATE_FORMAT.parse(text);
        } catch (ParseException e) {
            log.warn("Error parsing date " + text, e);
        }
        return null;
    }

    public void setData(Date data) {
        String dateStr = data == null ? "" : DATE_FORMAT.format(data);
        uiComponent.setText(dateStr);
    }

    public Date getDefaultValue() {
        return null;
    }

}
