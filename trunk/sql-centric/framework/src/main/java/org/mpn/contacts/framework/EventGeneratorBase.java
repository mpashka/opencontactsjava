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
package org.mpn.contacts.framework;

import java.util.HashSet;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.EventGeneratorBase here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class EventGeneratorBase<Type> implements EventGenerator<Type> {

    private Set<EventListener<Type>> listeners = new HashSet<EventListener<Type>>();

    public void addListener(EventListener<Type> listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener<Type> listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(Type source) {
        for (EventListener<Type> listener : listeners) {
            listener.onEvent(source);
        }
    }


}
