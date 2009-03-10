package org.mpn.contacts.ui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.DataSource;
import org.mpn.contacts.framework.db.DbAccess;
import org.mpn.contacts.framework.db.Row;
import org.mpn.contacts.framework.db.RowValue;
import org.mpn.contacts.framework.ui.MDIDesktopPane;
import org.mpn.contacts.framework.ui.RowAddAction;
import org.mpn.contacts.framework.ui.RowCommitAction;
import org.mpn.contacts.framework.ui.RowNextAction;
import org.mpn.contacts.framework.ui.RowPreviousAction;
import org.mpn.contacts.framework.ui.RowRollbackAction;
import org.mpn.contacts.framework.ui.SingleRowUIForm;
import org.mpn.contacts.framework.ui.UiComboBoxAbstract;
import org.mpn.contacts.framework.ui.UiComboBoxFixed;
import org.mpn.contacts.framework.ui.dnd.DataSourceTable;
import org.mpn.contacts.framework.ui.dnd.DndFrame;
import org.mpn.contacts.framework.ui.dnd.RowPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

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
        Action usersFormAction = new AbstractAction("Users") {
            public void actionPerformed(ActionEvent e) {
                createUsersTable();
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
                new JButton(usersFormAction),
                new JButton(townFormAction),
                new JButton(streetsFormAction),
                new JButton(addressFormAction),
                new JButton(dndFormAction),
        });

        mainPanel.add(buttonStackBuilder.getPanel(), BorderLayout.WEST);
    }

    private static final class GenderRenderer implements ListCellRenderer {
        private ListCellRenderer rendenrer;

        public GenderRenderer(ListCellRenderer rendenrer) {
            this.rendenrer = rendenrer;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Boolean booleanValue = (Boolean) value;
            String text = booleanValue == null ? "" : (booleanValue ? "male" : "female");
            return rendenrer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }

    private static final String[] COLUMNS = {
            "User name",
            "ICQ",
            "E-mail",
            "Job",
    };

    private void createUsersTable() {
        final Map<Long, String> organizationNames = new HashMap<Long, String>();
        for (Row row : Data.organizationTable) {
            organizationNames.put(row.getData(Data.organizationTable.id), row.getData(Data.organizationName));
        }
        TableModel usersTableModel = new AbstractTableModel() {
            public int getRowCount() {
                return Data.personTable.getRowCount();
            }

            public int getColumnCount() {
                return COLUMNS.length;
            }

            public String getColumnName(int columnIndex) {
                return COLUMNS[columnIndex];
            }

            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                Object[] data = Data.personTable.getTableData().get(rowIndex);
                Long personId = (Long) data[0];
                StringBuilder value = new StringBuilder();
                if (columnIndex == 0) {
                    appendString(value, " ", (String) data[1], (String) data[2], (String) data[3]);
                } else if (columnIndex == 1) {
                    for (Row row : Data.personMessagingTable) {
                        if (row.getData(Data.personTable.id).equals(personId) && row.getData(Data.personMessagingType).equals(Data.IM_TYPE_ICQ)) {
                            appendString(value, ", ", row.getData(Data.personMessagingId));
                        }
                    }
                } else if (columnIndex == 2) {
                    for (Row row : Data.personMessagingTable) {
                        if (row.getData(Data.personTable.id).equals(personId) && row.getData(Data.personMessagingType).equals(Data.IM_TYPE_EMAIL)) {
                            appendString(value, ", ", row.getData(Data.personMessagingId));
                        }
                    }
                } else if (columnIndex == 3) {
                    for (Row row : Data.personOrganizationTable) {
                        if (row.getData(Data.personTable.id).equals(personId)) {
                            appendString(value, ", ", organizationNames.get(row.getData(Data.organizationTable.id)));
                        }
                    }
                }
                return value.toString();
            }
        };

        Map<Long, JCheckBox> groupCheckBoxes = new HashMap<Long, JCheckBox>();
        JPanel groupsPanel = new JPanel();
        groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.PAGE_AXIS));
        for (Row row : Data.personGroupTable) {
            Long groupId = row.getData(Data.personGroupTable.id);
            String groupName = row.getData(Data.personGroupName);
            if (!groupName.startsWith("Job")) {
                JCheckBox checkBox = new JCheckBox(groupName);
                groupCheckBoxes.put(groupId, checkBox);
                groupsPanel.add(checkBox);
            }
        }
        Row userRow = Data.personTable.getRow();
        JTable usersTable = new JTable(usersTableModel);

        SingleRowUIForm userUiForm = new SingleRowUIForm(userRow);

        JInternalFrame usersTableFrame = new JInternalFrame("User list", true, true, true, true);
        Container userDetailsContent = usersTableFrame.getContentPane();
        userDetailsContent.setLayout(new GridBagLayout());
        userDetailsContent.add(new JScrollPane(usersTable), new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        userDetailsContent.add(groupsPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        addNewInternalFrame(usersTableFrame);
    }

    static void appendString(StringBuilder value, String delimiter, String ... values) {
        for (String s : values) {
            if (s != null && s.length() > 0) {
                if (value.length() > 0) {
                    value.append(delimiter);
                }
                value.append(s);
            }
        }
    }

    public void createUserDetailsDialog() {
        Row userRow = Data.personTable.getRow();
        SingleRowUIForm userUiForm = new SingleRowUIForm(userRow);

        FormLayout layout = new FormLayout("right:pref:grow(0.1), 3dlu, fill:pref:grow");
        CellConstraints cc = new CellConstraints();
        JPanel userDetailsPanel = new RowPanel(userUiForm.getRow());
        DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, userDetailsPanel);
        formBuilder.setDefaultDialogBorder();

        formBuilder.appendSeparator("General");
        formBuilder.append("First name", userUiForm.createJTextField(Data.personFirstName));
        formBuilder.append("Middle name", userUiForm.createJTextField(Data.personMiddleName));
        formBuilder.append("Last name", userUiForm.createJTextField(Data.personLastName));
        formBuilder.append("Birthday", userUiForm.createJDateField(Data.personBirthday));
        UiComboBoxAbstract<Boolean> genderUiComboBox = new UiComboBoxAbstract<Boolean>();
        JComboBox genderComboBox = genderUiComboBox.getUiComponent();
        genderComboBox.setRenderer(new GenderRenderer(genderComboBox.getRenderer()));
        userUiForm.addUiComponent(genderUiComboBox, Data.personGender);
        formBuilder.append("Gender", genderComboBox);
        formBuilder.append("Note");
        formBuilder.appendRow(new RowSpec("17dlu"));
        formBuilder.add(new JScrollPane(userUiForm.createJTextArea(Data.personNote)),
                    cc.xywh(formBuilder.getColumn(), formBuilder.getRow(), 1, 2));
        formBuilder.nextLine(2);

        formBuilder.appendSeparator("Address");
        formBuilder.nextLine();
        formBuilder.appendRow(new RowSpec("75dlu"));

        RowValue<Long> personId = new RowValue<Long>(userRow, Data.personTable.id);
        DataSource personAddressDataSource = Data.addressPersonTable.getFilteredTable(Data.personTable.id, personId);
        DataSourceTable personAddressTable = new DataSourceTable(personAddressDataSource);

        formBuilder.add(new JScrollPane(personAddressTable.getJTable()),
                    cc.xywh(formBuilder.getColumn(), formBuilder.getRow(), 3, 1));

//        formBuilder.append("E-mail");
//        formBuilder.append(userUiForm.createJTextField(Data.personEmail));

        ButtonBarBuilder buttonsBarBuilder = new ButtonBarBuilder();
        buttonsBarBuilder.addGridded(new JButton(new RowPreviousAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowNextAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowAddAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowCommitAction(userUiForm)));
        buttonsBarBuilder.addGridded(new JButton(new RowRollbackAction(userUiForm)));

        JInternalFrame userDetailsWindow = new JInternalFrame("User data", true, true, true, true);
        Container userDetailsContent = userDetailsWindow.getContentPane();
        userDetailsContent.add(userDetailsPanel, BorderLayout.CENTER);
        userDetailsContent.add(buttonsBarBuilder.getPanel(), BorderLayout.SOUTH);
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
