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

import org.mpn.contacts.framework.db.RowState;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiFormAbstractAction here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public abstract class UiFormAbstractAction extends AbstractAction implements UiFormListener {
    protected final SingleRowUIForm uiForm;
    private Set<RowState> enabledStates;

    public UiFormAbstractAction(String name, SingleRowUIForm uiForm, RowState... enabledStates) {
        super(name);
        this.uiForm = uiForm;
        this.enabledStates = new HashSet<RowState>(Arrays.asList(enabledStates));
        uiForm.addListener(this);
        setEnabled(checkIsActionEnabled());
    }

    public void uiFormUpdated(SingleRowUIForm uiForm) {
        setEnabled(checkIsActionEnabled());
    }

    protected boolean checkIsActionEnabled() {
        return enabledStates.contains(uiForm.getState());
    }

}
