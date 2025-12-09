/*
 * Copyright (C) 2016 Guo Yunhe <guoyunhebrave@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.guoyunhe.fontweak;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Fontconfig match pattern. The most common is to set Sans, Serif, Monospace
 * font family and replace a font by another.
 *
 * @author Guo Yunhe guoyunhebrave@gmail.com
 */
public class FontMatch {
    public String familyTest;
    public String langTest;
    public String[] familyEdit;

    /**
     * Initialize with null content.
     */
    public FontMatch() {
        this.familyTest = null;
        this.langTest = null;
        this.familyEdit = null;
    }

    /**
     * Initialize with data.
     * @param familyTest Condition font family.
     * @param langTest Condition language.
     * @param familyEdit Result font family.
     */
    public FontMatch(String familyTest, String langTest, String[] familyEdit) {
        this.familyTest = familyTest;
        this.langTest = langTest;
        this.familyEdit = familyEdit;
    }

    /**
     * Read data from XML node.
     * @param node The XML node to be analyzed.
     */
    public void parseDOM(Node node) {
        System.out.println("DEBUG: FontMatch.parseDOM started for node: " + node.getNodeName());
        this.familyEdit = null;
        this.familyTest = null;
        this.langTest = null;

        NodeList children = node.getChildNodes();
        System.out.println("DEBUG: FontMatch.parseDOM - processing " + children.getLength() + " children");
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChildNode = children.item(i);
            System.out.println("DEBUG: FontMatch.parseDOM - child " + i + ": " + currentChildNode.getNodeName());
            if (currentChildNode.hasAttributes()) {
                Element child = (Element) currentChildNode;
                String nodeName = child.getNodeName();
                String name = child.getAttribute("name");
                System.out.println("DEBUG: FontMatch.parseDOM - child element: " + nodeName + " name: " + name);
                List<String> stringList = new ArrayList<String>();
                NodeList grandChildren = child.getChildNodes();
                System.out.println("DEBUG: FontMatch.parseDOM - processing " + grandChildren.getLength() + " grandchildren");

                for (int j = 0; j < grandChildren.getLength(); j++) {
                    Node grandChild = grandChildren.item(j);
                    System.out.println("DEBUG: FontMatch.parseDOM - grandchild " + j + ": " + grandChild.getNodeName());
                    if(grandChild.getNodeName().equals("string")) {
                        stringList.add(grandChild.getTextContent());
                    }
                }
                System.out.println("DEBUG: FontMatch.parseDOM - finished processing grandchildren");

                if (nodeName.equals("test")) {
                    if (name.equals("family")) {
                        this.familyTest = stringList.get(0);
                    } else if (name.equals("lang")) {
                        this.langTest = stringList.get(0);
                    }
                } else if (nodeName.equals("edit")) {
                    if (name.equals("family")) {
                        this.familyEdit = stringList.toArray(new String[0]);
                    }
                }
            }
        }

        if (!isEmpty()) {
            // Node removal handled by FontConfig
        }
        System.out.println("DEBUG: FontMatch.parseDOM finished");
    }

    /**
     * Create DOM and insert into document.
     *
     * @param doc Document to create elements
     */
    public void createDOM(Document doc) {
        if (isEmpty()) {
            return;
        }

        // fontconfig --> match
        Element matchElement = doc.createElement("match");
        Node root = doc.getElementsByTagName("fontconfig").item(0);
        root.appendChild(matchElement);

        // fontconfig --> match --> test[name="family"]
        Element familyTestElement = doc.createElement("test");
        familyTestElement.setAttribute("name", "family");
        matchElement.appendChild(familyTestElement);

        // fontconfig --> match --> test[name="family"] --> string
        Element familyTestStringElement = doc.createElement("string");
        familyTestStringElement.setTextContent(familyTest);
        familyTestElement.appendChild(familyTestStringElement);

        if (this.langTest != null && !langTest.equalsIgnoreCase("en")) {
            // fontconfig --> match --> test[name="lang"]
            Element langTestElement = doc.createElement("test");
            langTestElement.setAttribute("name", "lang");
            matchElement.appendChild(langTestElement);

            // fontconfig --> match --> test[name="lang"] --> string
            Element langTestStringElement = doc.createElement("string");
            langTestStringElement.setTextContent(langTest);
            langTestElement.appendChild(langTestStringElement);
        }

        // fontconfig --> match --> edit[name="family"]
        Element familyEditElement = doc.createElement("edit");
        familyEditElement.setAttribute("name", "family");
        familyEditElement.setAttribute("binding", "strong");
        familyEditElement.setAttribute("mode", "prepend");
        matchElement.appendChild(familyEditElement);

        // fontconfig --> match --> edit[name="family"] --> string
        for (String family : familyEdit) {
            Element familyEditStringElement = doc.createElement("string");
            familyEditStringElement.setTextContent(family);
            familyEditElement.appendChild(familyEditStringElement);
        }
    }

    /**
     * Check if the match contains necessary data.
     *
     * @return If the node contains test node and edit node for font family.
     */
    public boolean isEmpty() {
        return this.familyTest == null || this.familyEdit == null;
    }
}
