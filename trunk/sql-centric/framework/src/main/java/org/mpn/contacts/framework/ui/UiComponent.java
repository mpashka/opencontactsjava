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

import org.mpn.contacts.framework.Value;
import org.mpn.contacts.framework.EventGeneratorBase;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiComponent here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public abstract class UiComponent<DataType, UiComponentType> extends EventGeneratorBase<Value<DataType>> implements Value<DataType> {
    UiComponentType uiComponent;

    protected UiComponent() {
        this.uiComponent = createUiComponent();
    }

    public UiComponentType getUiComponent() {
        return uiComponent;
    }

    public abstract UiComponentType createUiComponent();

    public abstract DataType getDefaultValue();

    public void clear() {
        setData(getDefaultValue());
    }

}
