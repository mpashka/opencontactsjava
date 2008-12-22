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
package org.mpn.contacts.importer;

import java.util.List;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.XmlData here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class XmlData {
    private List<String> hrefs;
    private List<String> images;
    private String text;

    public XmlData(List<String> hrefs, List<String> images, String text) {
        this.hrefs = hrefs;
        this.images = images;
        this.text = text;
    }

    public List<String> getHrefs() {
        return hrefs;
    }

    public List<String> getImages() {
        return images;
    }

    public String getText() {
        return text;
    }

}
