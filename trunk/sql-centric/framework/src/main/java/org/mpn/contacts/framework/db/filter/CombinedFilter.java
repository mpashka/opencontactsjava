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
package org.mpn.contacts.framework.db.filter;

import org.mpn.contacts.framework.EventGeneratorBase;
import org.mpn.contacts.framework.EventListener;
import org.mpn.contacts.framework.db.Filter;
import org.mpn.contacts.framework.db.Row;

import java.util.HashSet;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.filter.CombinedFilter here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public final class CombinedFilter extends EventGeneratorBase<Filter> implements Filter, EventListener<Filter> {

    private Set<Filter> filters = new HashSet<Filter>();

    public void addFilter(Filter filter) {
        filters.add(filter);
        filter.addListener(this);
    }

    public void onEvent(Filter source) {
        notifyListeners(source);
    }

    public boolean acceptRow(Row row) {
        for (Filter filter : filters) {
            if (!filter.acceptRow(row)) return false;
        }
        return true;
    }
}
