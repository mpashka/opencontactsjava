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
package org.mpn.contacts;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.PanelBuilderTest here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class PanelBuilderTest {

    public void testBuildPanel() throws InterruptedException {
        JFrame jframe = new JFrame("Test");
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.add(buildPanel());
        jframe.pack();
        jframe.setLocationByPlatform(true);
        jframe.setVisible(true);

        Thread.sleep(200000);
    }

    public JPanel build() {
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 80dlu, 7dlu, " // 1st major colum
                        + "right:max(40dlu;pref), 3dlu, 80dlu",        // 2nd major column
                "");                                         // add rows dynamically
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Flange");

        JLabel identifierField = new JLabel("identifierField");
        builder.append("Identifier", identifierField);
        builder.nextLine();

        builder.append("PTI [kW]",   new JTextField());
        builder.append("Power [kW]", new JTextField());

        builder.append("s [mm]",     new JTextField());
        builder.nextLine();

        builder.appendSeparator("Diameters");

        builder.append("da [mm]",    new JTextField());
        builder.append("di [mm]",    new JTextField());

        builder.append("da2 [mm]",   new JTextField());
        builder.append("di2 [mm]",   new JTextField());

        builder.append("R [mm]",     new JTextField());
        builder.append("D [mm]",     new JTextField());

        builder.appendSeparator("Criteria");

        builder.append("Location",   new JComboBox(new Object[]{"a1","a2", "a3"}));
        builder.append("k-factor",   new JTextField());

        builder.appendSeparator("Bolts");

        builder.append("Material",   new JComboBox(new Object[]{"a1","a2", "a3"}));
        builder.nextLine();

        builder.append("Numbers",    new JTextField());
        builder.nextLine();

        builder.append("ds [mm]",    new JTextField());

        return builder.getPanel();
    }

    public JComponent buildPanel() {
//        initComponents();
        JLabel name1Field = new JLabel("name1Field");
        JTextPane comment1Area = new JTextPane();
        JTextPane comment2Area = new JTextPane();
        JTextPane comment3Area = new JTextPane();
        JTextField name2Field = new JTextField();
        JTextField name3Field = new JTextField();

        FormLayout layout = new FormLayout(
                "right:pref, 3dlu, default:grow",
                "");
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout);
        formBuilder.setDefaultDialogBorder();
        formBuilder.setRowGroupingEnabled(true);

        CellConstraints cc = new CellConstraints();

        // In this approach, we add a gap and a custom row.
        // The advantage of this approach is, that we can express
        // the row spec and comment area cell constraints freely.
        // The disadvantage is the misalignment of the leading label.
        // Also the row's height may be inconsistent with other rows.
        formBuilder.appendSeparator("Single Custom Row");
        formBuilder.append("Name", name1Field);
        formBuilder.appendRow(formBuilder.getLineGapSpec());
        formBuilder.appendRow(new RowSpec("top:31dlu")); // Assumes line is 14, gap is 3
        formBuilder.nextLine(2);
        formBuilder.append("Comment");
        formBuilder.add(new JScrollPane(comment1Area),
                    cc.xy(formBuilder.getColumn(), formBuilder.getRow(), "fill, fill"));
        formBuilder.nextLine();

        // In this approach, we append a standard row with gap before it.
        // The advantage is, that the leading label is aligned well.
        // The disadvantage is that the comment area now spans
        // multiple cells and is slightly less flexible.
        // Also the row's height may be inconsistent with other rows.
        formBuilder.appendSeparator("Standard + Custom Row");
        formBuilder.append("Name", name2Field);
        formBuilder.append("Comment");
        formBuilder.appendRow(new RowSpec("17dlu")); // Assumes line is 14, gap is 3
        formBuilder.add(new JScrollPane(comment2Area),
                    cc.xywh(formBuilder.getColumn(), formBuilder.getRow(), 1, 2));
        formBuilder.nextLine(2);

        // In this approach, we append two standard rows with associated gaps.
        // The advantage is, that the leading label is aligned well,
        // and the height is consistent with other rows.
        // The disadvantage is that the comment area now spans
        // multiple cells and is slightly less flexible.
        formBuilder.appendSeparator("Two Standard Rows");
        formBuilder.append("Name", name3Field);
        formBuilder.append("Comment");
        formBuilder.nextLine();
        formBuilder.append("");
        formBuilder.nextRow(-2);
        formBuilder.add(new JScrollPane(comment3Area),
                    cc.xywh(formBuilder.getColumn(), formBuilder.getRow(), 1, 3));
        formBuilder.nextLine(3);

        JTable jtable1 = new JTable(new Object[][] {
                {"a", "b", "c"},
                {"1", "2", "3"},
                {"q", "w", "e"},
                {"a", "b", "c"},
                {"1", "2", "3"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
        }, new Object[] {
                "Text1", "Column2", "Apples"
        });

        JTable jtable2 = new JTable(new Object[][] {
                {"a", "b", "c"},
                {"1", "2", "3"},
                {"q", "w", "e"},
                {"a", "b", "c"},
                {"1", "2", "3"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
                {"q", "w", "e"},
        }, new Object[] {
                "Tex", "Colum_", "_Apples"
        });

        formBuilder.appendSeparator("Table1");
        formBuilder.nextLine();
        formBuilder.append("Comment");
//        formBuilder.nextRow(-2);
        formBuilder.appendRow(new RowSpec("17dlu")); // Assumes line is 14, gap is 3
        formBuilder.add(new JScrollPane(jtable1),
                    cc.xywh(formBuilder.getColumn(), formBuilder.getRow(), 1, 2));
        formBuilder.nextLine();

        formBuilder.nextLine();
        formBuilder.appendSeparator("Table2");
        formBuilder.nextLine();
//        formBuilder.append("Comment");
        formBuilder.appendRow(new RowSpec("100px"));
//        formBuilder.appendRow(new RowSpec("17dlu")); // Assumes line is 14, gap is 3
//        formBuilder.appendRow(new RowSpec("17dlu")); // Assumes line is 14, gap is 3
//        formBuilder.nextRow(-2);
//        formBuilder.nextLine(2);
//        formBuilder.nextLine();
//        formBuilder.nextLine();
//        formBuilder.nextLine();
        formBuilder.add(new JScrollPane(jtable2),
                    cc.xywh(formBuilder.getColumn(), formBuilder.getRow(), 3, 1));

        return formBuilder.getPanel();
    }

}
