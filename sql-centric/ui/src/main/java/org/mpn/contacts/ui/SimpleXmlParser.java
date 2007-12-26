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
package org.mpn.contacts.ui;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.SimpleXmlParser here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class SimpleXmlParser {

    static final Logger log = Logger.getLogger(SimpleXmlParser.class);

    private static final Set<String> UNCLOSEABLE_TAGS = new HashSet<String>(Arrays.asList(
            "img", "br"
    ));

    private abstract static class State {
        private String name;

        protected State(String name) {
            this.name = name;
        }

        public abstract void processChar(char inChar);

        public String toString() {
            return name;
        }
    }

    State state;

    StringBuilder tagNameStringBuilder;
    String tagNameString;
    Deque<String> tags;
    StringBuilder text;

    final State tagStart = new State("tagStart") {
        public void processChar(char inChar) {
            tagNameStringBuilder = new StringBuilder();
            if (inChar == '/') {
                state = tagEndDeclaration;
            } else {
                state = tagName;
                state.processChar(inChar);
            }
        }
    };

    final State tagName = new State("tagName") {
        public void processChar(char inChar) {
            if (inChar == ' ') {
                state = tagDeclaration;
            } else if (inChar == '>') {
                state = tagBody;
            } else if (inChar == '/') {
                state = tagDeclarationEnd;
            } else {
                tagNameStringBuilder.append(inChar);
                return;
            }
            tagNameString = tagNameStringBuilder.toString().trim();
            tags.addLast(tagNameString);
        }
    };

    /**
     * This is / character in tag declaration. next char must be >
     */
    final State tagDeclarationEnd = new State("tagDeclarationEnd") {
        public void processChar(char inChar) {
            if (inChar == '>') {
                if (tags.getLast().equals(tagNameString)) {
                    tags.removeLast();
                } else {
                    log.error("Closed by default (/>) tag " + tagNameString + " doesn't match tag stack last " + tags.getLast());
                }
                state = tagBody;
            } else {
                log.error("Tag close char must be />, but instead of > " + inChar + " found");
            }
        }
    };

    final State tagEndDeclaration = new State("tagEndDeclaration") {
        public void processChar(char inChar) {
            if (inChar == '>') {
                tagNameString = tagNameStringBuilder.toString().trim();
                while (UNCLOSEABLE_TAGS.contains(tags.getLast()) && !tags.getLast().equals(tagNameString)) {
                    tags.removeLast();
                }
                if (tags.getLast().equals(tagNameString)) {
                    tags.removeLast();
                } else {
                    log.error("Closed tag " + tagNameString + " doesn't match tag stack last " + tags.getLast());
                }
                state = tagBody;
            } else {
                tagNameStringBuilder.append(inChar);
            }
        }
    };

    StringBuilder attributeNameStringBuilder;
    String attributeNameString;
    StringBuilder attributeValueStringBuilder;
    String attributeValueString;
    char attributeDelimiter;

    final State tagDeclaration = new State("tagDeclaration") {
        public void processChar(char inChar) {
            if (inChar == ' ') {
                // continue ...
            } else if (inChar == '/') {
                state = tagDeclarationEnd;
            } else if (inChar == '>') {
                state = tagBody;
            } else {
                attributeNameStringBuilder = new StringBuilder();
                state = attributeName;
                state.processChar(inChar);
            }
        }
    };


    final State attributeName = new State("attributeName") {
        public void processChar(char inChar) {
            if (inChar == ' ') {
                attributeNameString = attributeNameStringBuilder.toString();
                state = attributeEquals;
            } else if (inChar == '=') {
                attributeNameString = attributeNameStringBuilder.toString();
                state = attributeValueBegin;
            } else {
                attributeNameStringBuilder.append(inChar);
            }
        }
    };

    final State attributeEquals = new State("attributeEquals") {
        public void processChar(char inChar) {
            if (inChar == ' ') {
                // continue ...
            } else if (inChar == '=') {
                state = attributeValueBegin;
            } else {
                log.error("Invalid char on attribute equals : " + inChar);
            }
        }
    };

    final State attributeValueBegin = new State("attributeValueBegin") {
        public void processChar(char inChar) {
            if (inChar == ' ') {
                return;
            } else if (inChar == '\'') {
                attributeDelimiter = inChar;
            } else if (inChar == '"') {
                attributeDelimiter = inChar;
            } else {
                log.warn("attribute value without delimiters : " + attributeNameString + ", char : " + inChar);
                attributeDelimiter = ' ';
                attributeValueStringBuilder = new StringBuilder();
                state = attributeValue;
                state.processChar(inChar);
                return;
            }
            attributeValueStringBuilder = new StringBuilder();
            state = attributeValue;
        }
    };

    final State attributeValue = new State("attributeValue") {
        public void processChar(char inChar) {
            if (inChar == attributeDelimiter) {
                attributeValueString = attributeValueStringBuilder.toString();
                attributeDeclared(tagNameString, attributeNameString, attributeValueString);
                state = tagDeclaration;
            } else {
                attributeValueStringBuilder.append(inChar);
            }
        }
    };

    final State xmlEntity = new State("xmlEntity") {
        public void processChar(char inChar) {
            if (inChar == ';') {
                state = tagBody;
            } else {
                // ignore xml entities
            }
        }
    };

    final State tagBody = new State("tagBody") {
        public void processChar(char inChar) {
            if (inChar == '<') {
                state = tagStart;
            } else if (inChar == '&') {
                state = xmlEntity;
            } else {
                text.append(inChar);
            }
        }
    };


    private List<String> hrefs;
    private List<String> images;
    public void attributeDeclared(String tag, String attributeName, String attributeValue) {
//        log.debug("Attribute " + tag + ":" + attributeName + "=" + attributeValue);
        if (tag.equals("a") && attributeName.equals("href")) {
            hrefs.add(attributeValue);
        } else if (tag.equals("img") && attributeName.equals("src")) {
            images.add(attributeValue);
        }
    }

    public XmlData parse(String xml) {
//        log.debug("        parse xml : " + xml);
        hrefs = new ArrayList<String>();
        images = new ArrayList<String>();
        text = new StringBuilder();
        tags = new LinkedList<String>();

        state = tagBody;
        for (int i = 0; i < xml.length(); i++) {
            State oldState = state;
            state.processChar(xml.charAt(i));
//            if (oldState != state) {
//                log.debug("State changed : " + oldState + " -> " + state);
//            }
        }

        while (!tags.isEmpty() && UNCLOSEABLE_TAGS.contains(tags.getLast())) {
            tags.removeLast();
        }
        if (tags.size() > 0) {
            log.error("Unclosed tags found : " + tags);
        }

        String text = this.text.toString().trim();
//        log.debug("        xml text: " + text);
        return new XmlData(hrefs, images, text);
    }
}
