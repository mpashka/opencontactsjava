package org.mpn.contacts.ui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.DataSource;
import org.mpn.contacts.framework.db.Row;
import org.mpn.contacts.framework.db.DbAccess;
import org.mpn.contacts.framework.ui.*;
import org.mpn.contacts.framework.ui.dnd.RowPanel;
import org.mpn.contacts.framework.ui.dnd.DndFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class MainFrame {

    static final Logger log = Logger.getLogger(MainFrame.class);

    private static final int xOffset = 30, yOffset = 30;

    JFrame frame = new JFrame();
    JPanel mainPanel = new JPanel(new BorderLayout());
    MDIDesktopPane desktop = new MDIDesktopPane();
    int openFrameCount = 0;

    public MainFrame() {
        log.debug("DB Connecting ...");
        DbAccess.getInstance();
        log.debug("Ok");

        frame.setTitle("jContacts");
        frame.getContentPane().add(mainPanel);
        mainPanel.add(new JScrollPane(desktop), BorderLayout.CENTER);
        frame.setResizable(true);
        frame.setResizable(true);
//        frame.setLocation(new Point(100, 100));
        frame.setLocationByPlatform(true);
        frame.setSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        createButtons();
        frame.setVisible(true);
    }

    private void createButtons() {
        ButtonStackBuilder buttonStackBuilder = new ButtonStackBuilder();
        Action userFormAction = new AbstractAction("User") {
            public void actionPerformed(ActionEvent e) {
                createUserDetailsDialog();
            }
        };
        Action townFormAction = new AbstractAction("Towns") {
            public void actionPerformed(ActionEvent e) {
                createTownDialog();
            }
        };
        Action streetsFormAction = new AbstractAction("Streets") {
            public void actionPerformed(ActionEvent e) {
                createStreetsDialog();
            }

        };
        Action addressFormAction = new AbstractAction("Address") {
            public void actionPerformed(ActionEvent e) {
                createAddressDialog();
            }
        };
        Action dndFormAction = new AbstractAction("DragAndDrop") {
            public void actionPerformed(ActionEvent e) {
                DndFrame dndFrame = new DndFrame("Drag And Drop");

                desktop.add(dndFrame);
                dndFrame.setSize(300, 200);
                dndFrame.setResizable(true);
                dndFrame.setClosable(true);
                dndFrame.setVisible(true);
                dndFrame.setLocation(100, 100);

//                dndFrame.setVisible(true);
//                addNewInternalFrame(dndFrame);
            }
        };
        buttonStackBuilder.addButtons(new JButton[]{
                new JButton(userFormAction),
                new JButton(townFormAction),
                new JButton(streetsFormAction),
                new JButton(addressFormAction),
                new JButton(dndFormAction),
        });

        mainPanel.add(buttonStackBuilder.getPanel(), BorderLayout.WEST);
    }

    public void createUserDetailsDialog() {
        Row userRow = Data.personTable.getRow();
        SingleRowUIForm userUiForm = new SingleRowUIForm(userRow);

        FormLayout layout = new FormLayout("right:pref, 3dlu, max(40dlu;pref)");
        JPanel userDetailsPanel = new RowPanel(userUiForm.getRow());
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, userDetailsPanel);
        formBuilder.setDefaultDialogBorder();

        formBuilder.appendSeparator("General");
        formBuilder.append("Name");
        formBuilder.append(userUiForm.createJTextField(Data.personFirstName));
//        formBuilder.append("E-mail");
//        formBuilder.append(userUiForm.createJTextField(Data.personEmail));

        ButtonBarBuilder buttonsBarBuilder = new ButtonBarBuilder();
        buttonsBarBuilder.addGridded(new JButton(new RowPreviousAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowNextAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowAddAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowCommitAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowRollbackAction(userUiForm)));

        JInternalFrame userDetailsWindow = new JInternalFrame("User data", true, true, true, true);
        userDetailsWindow.add(userDetailsPanel, BorderLayout.CENTER);
        userDetailsWindow.add(buttonsBarBuilder.getPanel(), BorderLayout.SOUTH);
        addNewInternalFrame(userDetailsWindow);
    }

    public void createTownDialog() {
        Row townRow = Data.townTable.getRow();
        SingleRowUIForm townUiForm = new SingleRowUIForm(townRow);

        FormLayout layout = new FormLayout("right:pref, 3dlu, max(40dlu;pref)");
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout);
        formBuilder.setDefaultDialogBorder();

        formBuilder.appendSeparator("General");
        formBuilder.append("Name");
        formBuilder.append(townUiForm.createJTextField(Data.townName));
        formBuilder.append("Code");
        formBuilder.append(townUiForm.createJIntegerField(Data.townCode));

        ButtonBarBuilder buttonsBarBuilder = new ButtonBarBuilder();
        buttonsBarBuilder.addGridded(new JButton(new RowPreviousAction(townUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowNextAction(townUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowAddAction(townUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowCommitAction(townUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowRollbackAction(townUiForm)));

        JInternalFrame userDetailsWindow = new JInternalFrame("Town", true, true, true, true);
        userDetailsWindow.add(formBuilder.getPanel(), BorderLayout.CENTER);
        userDetailsWindow.add(buttonsBarBuilder.getPanel(), BorderLayout.SOUTH);
        addNewInternalFrame(userDetailsWindow);
    }

    public void createAddressDialog() {
        Row townRow = Data.addressPersonTable.getRow();
        SingleRowUIForm addressUiForm = new SingleRowUIForm(townRow);

        FormLayout layout = new FormLayout("right:pref, 3dlu, max(40dlu;pref)");
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout);
        formBuilder.setDefaultDialogBorder();

        formBuilder.appendSeparator("General");
        formBuilder.append("Home");
        formBuilder.append(addressUiForm.createJIntegerField(Data.addressPersonNumber));
        formBuilder.append("Appartments");
        formBuilder.append(addressUiForm.createJIntegerField(Data.addressPersonAppartments));
        formBuilder.append("Town");
        UiComboBoxFixed townComboBox = new UiComboBoxFixed(Data.townTable, Data.townTable.id, Data.townName);
        formBuilder.append(townComboBox.getUiComponent());
        DataSource streetsOfTownDataSource = Data.streetTable.getFilteredTable(Data.townTable.id, townComboBox);
        formBuilder.append("Street");
        formBuilder.append(addressUiForm.createComboBoxCached(Data.streetTable.id, streetsOfTownDataSource, Data.streetTable.id, Data.streetName).getUiComponent());

        ButtonBarBuilder buttonsBarBuilder = new ButtonBarBuilder();
        buttonsBarBuilder.addGridded(new JButton(new RowPreviousAction(addressUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowNextAction(addressUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowAddAction(addressUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowCommitAction(addressUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowRollbackAction(addressUiForm)));

        JInternalFrame addressWindow = new JInternalFrame();
        addressWindow.add(formBuilder.getPanel(), BorderLayout.CENTER);
        addressWindow.add(buttonsBarBuilder.getPanel(), BorderLayout.SOUTH);
        addressWindow.setTitle("Address");
        addNewInternalFrame(addressWindow);
    }

    private void createStreetsDialog() {
        UiComboBoxFixed townField = new UiComboBoxFixed(Data.townTable, Data.townTable.id, Data.townName);

        DataSource streetSubTable = Data.streetTable.getFilteredTable(Data.townTable.id, townField);
        Row streetRow = streetSubTable.getRow();
        SingleRowUIForm streetUiForm = new SingleRowUIForm(streetRow);


        FormLayout layout = new FormLayout("right:pref, 3dlu, max(40dlu;pref)");
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout);
        formBuilder.setDefaultDialogBorder();

        formBuilder.appendSeparator("General");
        formBuilder.append("Town");
        formBuilder.append(townField.getUiComponent());
        formBuilder.append("Street");
        formBuilder.append(streetUiForm.createJTextField(Data.streetName));

        ButtonBarBuilder buttonsBarBuilder = new ButtonBarBuilder();
        buttonsBarBuilder.addGridded(new JButton(new RowPreviousAction(streetUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowNextAction(streetUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowAddAction(streetUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowCommitAction(streetUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowRollbackAction(streetUiForm)));

        JInternalFrame addressWindow = new JInternalFrame();
        addressWindow.add(formBuilder.getPanel(), BorderLayout.CENTER);
        addressWindow.add(buttonsBarBuilder.getPanel(), BorderLayout.SOUTH);
        addressWindow.setTitle("Streets");
        addNewInternalFrame(addressWindow);
    }

    private void addNewInternalFrame(JInternalFrame internalFrame) {
        desktop.add(internalFrame);
        internalFrame.pack();
        internalFrame.setResizable(true);
        internalFrame.setClosable(true);
        internalFrame.setVisible(true);
        internalFrame.setLocation(openFrameCount * xOffset, openFrameCount * yOffset);
        openFrameCount++;
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}
