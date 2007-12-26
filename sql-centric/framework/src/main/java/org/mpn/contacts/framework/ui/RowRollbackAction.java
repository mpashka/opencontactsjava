/**
 * Copyright (c) 2005 jNETx.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNETx. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with jNETx.
 *
 * $Id:$
 */
package org.mpn.contacts.framework.ui;

import org.mpn.contacts.framework.db.RowState;

import java.awt.event.ActionEvent;

/**
 * A brief description of the class.
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision:$
 */
public class RowRollbackAction extends UiFormAbstractAction {

    public RowRollbackAction(SingleRowUIForm uiForm) {
        super("Cancel", uiForm, RowState.update, RowState.add);
    }

    public void actionPerformed(ActionEvent e) {
        uiForm.rollbackRow();
    }
}
