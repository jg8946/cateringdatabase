package cateringdatabaseswing;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/*A program designed to give a Swing visual interface to an earlier database project.*/
public class CateringDatabaseSwing {
    
    /*Initial setup of global user credentials.*/
    public String userName;
    enum loginLevel {NO_ACCESS, CUSTOMER, CAN_DELETE, NO_DELETE}
    loginLevel accessLevel = loginLevel.NO_ACCESS;
    
    /*Initial launch setup, calling a function to connect to the database followed by the login screen.*/
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        CateringDatabaseSwing cds = new CateringDatabaseSwing();
        Connection newConn = cds.getOrderConnection();
        cds.loginScreen(newConn);
    }
    
    /*Screen designed to affirm that the user's credentials (name and password) are
    present in the client or staff tables of the database. The user is then taken 
    to either the client or staff user menus.*/
    public void loginScreen (Connection c) {
        JFrame loginFrame = new JFrame("Catering Database - Log in:");
        loginFrame.setSize(360,150);
        loginFrame.setLayout(new FlowLayout());
        loginFrame.setLocationByPlatform(true);
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("User Name: ");
        JTextField nameField = new JTextField(20);
        JPanel passPanel = new JPanel();
        JLabel passLabel = new JLabel("Password: ");
        JTextField passField = new JTextField(20);
        JButton enterNameAndPass = new JButton("Log in");
        
        namePanel.add(nameLabel); namePanel.add(nameField); 
        passPanel.add(passLabel); passPanel.add(passField);
        
        enterNameAndPass.addActionListener(new ActionListener(){
            private Component frame;
            @Override
            public void actionPerformed(ActionEvent e) {
                Statement stmnt;
                try {
                    stmnt = c.createStatement();
                    ResultSet testResults = 
                            stmnt.executeQuery("SELECT * FROM STAFFMEMBER WHERE NAME='" 
                            + nameField.getText() + "' AND PASSWORD='" + 
                            passField.getText() + "'");
                    if (testResults.next()) {
                        userName = testResults.getString("NAME");
                        if (testResults.getString("ACCESSLEVEL").equals("Can delete")) {
                            accessLevel = loginLevel.CAN_DELETE;
                        } else {
                            accessLevel = loginLevel.NO_DELETE;
                        }
                        staffMainMenuScreen(c);
                        loginFrame.dispose();
                    } else {
                        Statement stmnt2 = c.createStatement();
                        ResultSet custResults =
                            stmnt2.executeQuery("SELECT * FROM CLIENT WHERE NAME='" 
                            + nameField.getText() + "' AND PASSWORD='" 
                            + passField.getText() + "'");
                        if (custResults.next()) {
                            userName = custResults.getString("NAME");
                            accessLevel = loginLevel.CUSTOMER;
                            clientMenuScreen(c);
                            loginFrame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(frame, "User not found.");
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        loginFrame.add(namePanel);
        loginFrame.add(passPanel);
        loginFrame.add(enterNameAndPass);
        loginFrame.setVisible(true);
    }
    
    /*Menu for employees. Staff can go to either customer, order or staff menus 
    from here, as well as logging out.*/
    public void staffMainMenuScreen(Connection c) {
        JFrame mainMenuFrame = new JFrame("Catering Database - Staff Options");
        mainMenuFrame.setSize(500,200);
        mainMenuFrame.setLayout(new FlowLayout());
        mainMenuFrame.setLocationByPlatform(true);
        mainMenuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel introPanel = new JPanel();
        JTextField intro = new JTextField("Hello, " + userName + 
                ". Please select a table to review.");
        intro.setEditable(false);
        introPanel.add(intro);
        JPanel buttonsPanel = new JPanel();
        JButton clientTableButton = new JButton("Clients");
        JButton staffTableButton = new JButton("Staff Members");
        JButton orderTableButton = new JButton("Orders");
        JButton logOutButton = new JButton("Log Out");
        buttonsPanel.add(clientTableButton);
        buttonsPanel.add(staffTableButton);
        buttonsPanel.add(orderTableButton);
        buttonsPanel.add(logOutButton);
        
        clientTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientDataOverviewScreen(c);
                    mainMenuFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        staffTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    staffDataOverviewScreen(c);
                    mainMenuFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        orderTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    orderDataOverviewScreen(c);
                    mainMenuFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginScreen(c);
                userName = "";
                accessLevel = loginLevel.NO_ACCESS; 
                mainMenuFrame.dispose();
            }
            
        });
        
        mainMenuFrame.add(introPanel);
        mainMenuFrame.add(buttonsPanel);
        mainMenuFrame.setVisible(true);
    }
    
    /*Menu screen for staff to create, modify or delete client data. 
    Only those with higher authorization can delete.*/
    public void clientDataOverviewScreen(Connection c) throws SQLException {
        JFrame clientOverviewFrame = new JFrame("Catering Database - Client Table");
        clientOverviewFrame.setSize(500,520);
        clientOverviewFrame.setLayout(new FlowLayout());
        clientOverviewFrame.setLocationByPlatform(true);
        clientOverviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JTable showDataTable = new JTable();
        DefaultTableModel dtm = new DefaultTableModel();
        showDataTable.setModel(dtm);
        showDataTable.setSize(500, 500);
        JScrollPane scroll = new JScrollPane(showDataTable);
        scroll.setSize(500, 500);
        
        String headers[] = {"NAME", "EMAIL", "PASSWORD"};
        dtm.setColumnIdentifiers(headers);
        
        Statement stmnt = c.createStatement();
        ResultSet testResults = stmnt.executeQuery("SELECT * FROM CLIENT");
        
            while (testResults.next()) {
                String name = testResults.getString("NAME");
                String pass = testResults.getString("PASSWORD");
                String eMail = testResults.getString("EMAIL");
            
                Object rowData[] = {name, eMail, pass};
                dtm.addRow(rowData);
            }
        
        JPanel clientButtons = new JPanel();
        JButton createClientButton = new JButton("New client");
        JButton editClientButton = new JButton("Edit a client");
        JButton deleteClientButton = new JButton("Delete a client");
        JButton mainMenuReturnButton = new JButton("Back to main menu");
        clientButtons.add(createClientButton); clientButtons.add(editClientButton);
        clientButtons.add(deleteClientButton); clientButtons.add(mainMenuReturnButton);
        
        createClientButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                createClientScreen(c);
                clientOverviewFrame.dispose();
            }
        });
        
        editClientButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateClientScreen(c);
                    clientOverviewFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        deleteClientButton.addActionListener(new ActionListener(){
            private Component frame;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accessLevel == loginLevel.CAN_DELETE) {
                    try {
                        deleteClientScreen(c);
                        clientOverviewFrame.dispose();
                    } catch (SQLException ex) {
                        Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Sorry, you do not have deletion privileges.");
                }
            }
            
        });
        
        mainMenuReturnButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                staffMainMenuScreen(c);
                clientOverviewFrame.dispose();
            }
        });
        
        clientOverviewFrame.add(scroll);
        clientOverviewFrame.add(clientButtons);
        clientOverviewFrame.setVisible(true);
    }
    
    /*Form for client creation.*/
    public void createClientScreen(Connection c) {
        JFrame createClientFrame = new JFrame("Catering Database - Create New Client");
        createClientFrame.setSize(650,520);
        createClientFrame.setLayout(new FlowLayout());
        createClientFrame.setLocationByPlatform(true);
        createClientFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("Client name:");
        JTextField nameField = new JTextField(20);
        namePanel.add(nameLabel); namePanel.add(nameField);
        
        JPanel passPanel = new JPanel();
        JLabel passLabel = new JLabel("Password:");
        JTextField passField = new JTextField(20);
        passPanel.add(passLabel); passPanel.add(passField);
        
        JPanel mailPanel = new JPanel();
        JLabel mailLabel = new JLabel("E-mail address:");
        JTextField mailField = new JTextField(20);
        mailPanel.add(mailLabel); mailPanel.add(mailField);
        
        JPanel clientButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmCreateButton = new JButton("Confirm creation");
        clientButtonsPanel.add(cancelButton); clientButtonsPanel.add(confirmCreateButton);
        
        confirmCreateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("INSERT INTO CLIENT (NAME, EMAIL, PASSWORD) VALUES ('" 
                    + nameField.getText() + "', '" + mailField.getText()
                    + "', '" + passField.getText() + "')");
                    clientDataOverviewScreen(c);
                    createClientFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientDataOverviewScreen(c);
                    createClientFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        createClientFrame.add(namePanel); createClientFrame.add(passPanel);
        createClientFrame.add(mailPanel); createClientFrame.add(clientButtonsPanel);
        createClientFrame.setVisible(true);
    }
    
    /*Form for updating a client's details from a list of selected names.*/
    public void updateClientScreen (Connection c) throws SQLException {
        JFrame updateClientFrame = new JFrame("Catering Database - Update a Client");
        updateClientFrame.setSize(400,400);
        updateClientFrame.setLayout(new FlowLayout());
        updateClientFrame.setLocationByPlatform(true);
        updateClientFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel getClientNamePanel = new JPanel();
        JPanel clientSpinnerPanel = new JPanel();
        JLabel getNameLabel = new JLabel("Select the name of the client to edit.");
        Statement stmnt = c.createStatement();
        ResultSet nameResults = stmnt.executeQuery("SELECT NAME FROM CLIENT");
        ArrayList<String> nameStrings = new ArrayList<String>();
        while (nameResults.next()) {
            nameStrings.add(nameResults.getString("NAME"));
            System.out.println(nameStrings);
        }
        SpinnerModel smClientNames = new SpinnerListModel(nameStrings);
        JSpinner clientSpinner = new JSpinner(smClientNames);
        Component clientSpinnerEditor = clientSpinner.getEditor();
            JFormattedTextField jftf = ((JSpinner.DefaultEditor) clientSpinnerEditor).getTextField();
            jftf.setColumns(10);
        getClientNamePanel.add(getNameLabel); clientSpinnerPanel.add(clientSpinner);
        
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("Client name:");
        JTextField nameField = new JTextField(20);
        namePanel.add(nameLabel); namePanel.add(nameField);
        
        JPanel passPanel = new JPanel();
        JLabel passLabel = new JLabel("Password:");
        JTextField passField = new JTextField(20);
        passPanel.add(passLabel); passPanel.add(passField);
        
        JPanel mailPanel = new JPanel();
        JLabel mailLabel = new JLabel("E-mail address:");
        JTextField mailField = new JTextField(20);
        mailPanel.add(mailLabel); mailPanel.add(mailField);
        
        JPanel clientButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmUpdateButton = new JButton("Confirm update");
        clientButtonsPanel.add(cancelButton); clientButtonsPanel.add(confirmUpdateButton);
        
        confirmUpdateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("UPDATE CLIENT SET NAME = '" + nameField.getText() 
                    + "', PASSWORD = '" + passField.getText() + "', EMAIL = '" 
                    + mailField.getText() + "' WHERE NAME = '" 
                    + smClientNames.getValue() + "'");
                    clientDataOverviewScreen(c);
                    updateClientFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientDataOverviewScreen(c);
                    updateClientFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        updateClientFrame.add(getClientNamePanel); updateClientFrame.add(clientSpinnerPanel); 
        updateClientFrame.add(namePanel); 
        updateClientFrame.add(passPanel); updateClientFrame.add(mailPanel); 
        updateClientFrame.add(clientButtonsPanel);
        updateClientFrame.setVisible(true);
    }
    
    /*Form for removing a client from the database.*/
    public void deleteClientScreen (Connection c) throws SQLException {
            JFrame deleteClientFrame = new JFrame("Catering Database - Delete a Client");
            deleteClientFrame.setSize(600,400);
            deleteClientFrame.setLayout(new FlowLayout());
            deleteClientFrame.setLocationByPlatform(true);
            deleteClientFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            JPanel getClientNamePanel = new JPanel();
            JPanel clientSpinnerPanel = new JPanel();
            JLabel getNameLabel = new JLabel("Select the name of the client to delete.");
            Statement stmnt = c.createStatement();
            ResultSet nameResults = stmnt.executeQuery("SELECT NAME FROM CLIENT");
            ArrayList<String> nameStrings = new ArrayList<String>();
            while (nameResults.next()) {
                nameStrings.add(nameResults.getString("NAME"));
            }
            SpinnerModel smClientNames = new SpinnerListModel(nameStrings);
            JSpinner clientSpinner = new JSpinner(smClientNames);
            Component clientSpinnerEditor = clientSpinner.getEditor();
            JFormattedTextField jftf = ((JSpinner.DefaultEditor) clientSpinnerEditor).getTextField();
            jftf.setColumns(10);
            getClientNamePanel.add(getNameLabel); clientSpinnerPanel.add(clientSpinner);
            
            JPanel clientButtonsPanel = new JPanel();
            JButton cancelButton = new JButton("Cancel");
            JButton confirmDeleteButton = new JButton("Confirm Deletion");
            clientButtonsPanel.add(cancelButton); clientButtonsPanel.add(confirmDeleteButton);
        
        confirmDeleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("DELETE FROM CLIENT WHERE NAME = '" 
                    + smClientNames.getValue() + "'");
                    clientDataOverviewScreen(c);
                    deleteClientFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientDataOverviewScreen(c);
                    deleteClientFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        deleteClientFrame.add(getClientNamePanel);
        deleteClientFrame.add(clientSpinnerPanel);
        deleteClientFrame.add(clientButtonsPanel);
        deleteClientFrame.setVisible(true);
    }
    
    /*Main menu for staff data including and overview of the table. The user can
    create, modify and (if authorized) remove staff members.*/
    public void staffDataOverviewScreen(Connection c) throws SQLException {
        JFrame staffOverviewFrame = new JFrame("Catering Database - Staff Member Table");
        staffOverviewFrame.setSize(650,520);
        staffOverviewFrame.setLayout(new FlowLayout());
        staffOverviewFrame.setLocationByPlatform(true);
        staffOverviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JTable showDataTable = new JTable();
        DefaultTableModel dtm = new DefaultTableModel();
        showDataTable.setModel(dtm);
        showDataTable.setSize(500, 500);
        JScrollPane scroll = new JScrollPane(showDataTable);
        scroll.setSize(500, 500);
        
        String headers[] = {"ACCESSLEVEL", "NAME", "EMAIL", "PASSWORD"};
        dtm.setColumnIdentifiers(headers);
        
        Statement stmnt = c.createStatement();
        ResultSet testResults = stmnt.executeQuery("SELECT * FROM STAFFMEMBER");
        
        while (testResults.next()) {
            String access = testResults.getString("ACCESSLEVEL");
            String name = testResults.getString("NAME");
            String eMail = testResults.getString("EMAIL");
            String pass = testResults.getString("PASSWORD");
            
            Object rowData[] = {access, name, eMail, pass};
            dtm.addRow(rowData);
        }
        
        JPanel staffButtons = new JPanel();
        JButton createStaffButton = new JButton("New staff member");
        JButton editStaffButton = new JButton("Edit a staff member");
        JButton deleteStaffButton = new JButton("Delete a staff member");
        JButton mainMenuReturnButton = new JButton("Back to main menu");
        staffButtons.add(createStaffButton); staffButtons.add(editStaffButton);
        staffButtons.add(deleteStaffButton); staffButtons.add(mainMenuReturnButton);
        
        createStaffButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                createStaffScreen(c);
                staffOverviewFrame.dispose();
            }
        });
        
        editStaffButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateStaffScreen(c);
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
                staffOverviewFrame.dispose();
            }
        });
        
         deleteStaffButton.addActionListener(new ActionListener(){
            private Component frame;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accessLevel == loginLevel.CAN_DELETE) {
                    try {
                        deleteStaffScreen(c);
                        staffOverviewFrame.dispose();
                    } catch (SQLException ex) {
                        Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Sorry, you do not have deletion privileges.");
                }
            }
            
        });
        
        mainMenuReturnButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                staffMainMenuScreen(c);
                staffOverviewFrame.dispose();
            }
        });
        
        staffOverviewFrame.add(scroll);
        staffOverviewFrame.add(staffButtons);
        staffOverviewFrame.setVisible(true);
    }
    
   /*Form for adding new staff.*/
    public void createStaffScreen(Connection c) {
        JFrame createStaffFrame = new JFrame("Catering Database - Create New Staff");
        createStaffFrame.setSize(650,520);
        createStaffFrame.setLayout(new FlowLayout());
        createStaffFrame.setLocationByPlatform(true);
        createStaffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel accessPanel = new JPanel();
        JLabel accessLabel = new JLabel("Access Level:");
        JRadioButton canDelete = new JRadioButton("Can delete");
        JRadioButton cannotDelete = new JRadioButton("Cannot delete");
        cannotDelete.setSelected(true);
        ButtonGroup accessButtons = new ButtonGroup();
        accessButtons.add(canDelete); accessButtons.add(cannotDelete);
        accessPanel.add(accessLabel);
        accessPanel.add(canDelete); accessPanel.add(cannotDelete);
        
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("Staff name:");
        JTextField nameField = new JTextField(20);
        namePanel.add(nameLabel); namePanel.add(nameField);
        
        JPanel mailPanel = new JPanel();
        JLabel mailLabel = new JLabel("E-mail address:");
        JTextField mailField = new JTextField(20);
        mailPanel.add(mailLabel); mailPanel.add(mailField);
        
        JPanel passPanel = new JPanel();
        JLabel passLabel = new JLabel("Password:");
        JTextField passField = new JTextField(20);
        passPanel.add(passLabel); passPanel.add(passField);
       
        JPanel staffButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmCreateButton = new JButton("Confirm creation");
        staffButtonsPanel.add(cancelButton); staffButtonsPanel.add(confirmCreateButton);
        
        confirmCreateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accessLevel = "";
                    if (canDelete.isSelected()) {
                        accessLevel = "Can delete";
                    } else {
                        accessLevel = "Cannot delete";
                    }
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("INSERT INTO STAFFMEMBER (ACCESSLEVEL, NAME, EMAIL, PASSWORD) VALUES ('" 
                    + accessLevel + "', '" + nameField.getText() + "', '" + mailField.getText()
                    + "', '" + passField.getText() + "')");
                    staffDataOverviewScreen(c);
                    createStaffFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    staffDataOverviewScreen(c);
                    createStaffFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        createStaffFrame.add(accessPanel);
        createStaffFrame.add(namePanel); createStaffFrame.add(passPanel);
        createStaffFrame.add(mailPanel); createStaffFrame.add(staffButtonsPanel);
        createStaffFrame.setVisible(true);
    }
    
    /*Form for updating an existing staff member's details.*/
        public void updateStaffScreen (Connection c) throws SQLException {
        JFrame updateStaffFrame = new JFrame("Catering Database - Update Staff Member");
        updateStaffFrame.setSize(400,400);
        updateStaffFrame.setLayout(new FlowLayout());
        updateStaffFrame.setLocationByPlatform(true);
        updateStaffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel getStaffNamePanel = new JPanel();
        JPanel staffSpinnerPanel = new JPanel();
        JLabel getNameLabel = new JLabel("Select the staff member to edit.");
        Statement stmnt = c.createStatement();
        ResultSet nameResults = stmnt.executeQuery("SELECT NAME FROM STAFFMEMBER");
        ArrayList<String> nameStrings = new ArrayList<String>();
        while (nameResults.next()) {
            nameStrings.add(nameResults.getString("NAME"));
        }
        SpinnerModel smStaffNames = new SpinnerListModel(nameStrings);
        JSpinner staffSpinner = new JSpinner(smStaffNames);
        Component staffSpinnerEditor = staffSpinner.getEditor();
            JFormattedTextField jftf = ((JSpinner.DefaultEditor) staffSpinnerEditor).getTextField();
            jftf.setColumns(10);
        getStaffNamePanel.add(getNameLabel); staffSpinnerPanel.add(staffSpinner);
        
        JPanel accessPanel = new JPanel();
        JLabel accessLabel = new JLabel("Access Level:");
        JRadioButton canDelete = new JRadioButton("Can delete");
        JRadioButton cannotDelete = new JRadioButton("Cannot delete");
        cannotDelete.setSelected(true);
        ButtonGroup accessButtons = new ButtonGroup();
        accessButtons.add(canDelete); accessButtons.add(cannotDelete);
        accessPanel.add(accessLabel);
        accessPanel.add(canDelete); accessPanel.add(cannotDelete);
        
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("Staff member name:");
        JTextField nameField = new JTextField(20);
        namePanel.add(nameLabel); namePanel.add(nameField);
        
        JPanel mailPanel = new JPanel();
        JLabel mailLabel = new JLabel("E-mail address:");
        JTextField mailField = new JTextField(20);
        mailPanel.add(mailLabel); mailPanel.add(mailField);
        
        JPanel passPanel = new JPanel();
        JLabel passLabel = new JLabel("Password:");
        JTextField passField = new JTextField(20);
        passPanel.add(passLabel); passPanel.add(passField);
        
        JPanel staffButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmUpdateButton = new JButton("Confirm update");
        staffButtonsPanel.add(cancelButton); staffButtonsPanel.add(confirmUpdateButton);
        
        confirmUpdateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String accessLevel = "";
                    if (canDelete.isSelected()) {
                        accessLevel = "Can delete";
                    } else {
                        accessLevel = "Cannot delete";
                    }
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("UPDATE STAFFMEMBER SET ACCESSLEVEL = '" 
                    + accessLevel +"', NAME = '" + nameField.getText() 
                    + "', EMAIL = '" + mailField.getText() + "', PASSWORD = '" 
                    + mailField.getText() + "' WHERE NAME = '" 
                    + smStaffNames.getValue() + "'");
                    staffDataOverviewScreen(c);
                    updateStaffFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientDataOverviewScreen(c);
                    updateStaffFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        updateStaffFrame.add(getStaffNamePanel); updateStaffFrame.add(staffSpinnerPanel); 
        updateStaffFrame.add(accessPanel); updateStaffFrame.add(namePanel); 
        updateStaffFrame.add(mailPanel); updateStaffFrame.add(passPanel); 
        updateStaffFrame.add(staffButtonsPanel);
        updateStaffFrame.setVisible(true);
    }
    
    /*Form for choosing and removing a currently existing staff member record.*/
    public void deleteStaffScreen (Connection c) throws SQLException {
            JFrame deleteStaffFrame = new JFrame("Catering Database - Delete Staff Member");
            deleteStaffFrame.setSize(600,400);
            deleteStaffFrame.setLayout(new FlowLayout());
            deleteStaffFrame.setLocationByPlatform(true);
            deleteStaffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            JPanel getStaffNamePanel = new JPanel();
            JPanel staffSpinnerPanel = new JPanel();
            JLabel getNameLabel = new JLabel("Select the staff member to delete.");
            Statement stmnt = c.createStatement();
            ResultSet nameResults = stmnt.executeQuery("SELECT NAME FROM STAFFMEMBER");
            ArrayList<String> nameStrings = new ArrayList<String>();
            while (nameResults.next()) {
                nameStrings.add(nameResults.getString("NAME"));
            }
            SpinnerModel smStaffNames = new SpinnerListModel(nameStrings);
            JSpinner staffSpinner = new JSpinner(smStaffNames);
            Component staffSpinnerEditor = staffSpinner.getEditor();
            JFormattedTextField jftf = ((JSpinner.DefaultEditor) staffSpinnerEditor).getTextField();
            jftf.setColumns(10);
            getStaffNamePanel.add(getNameLabel); staffSpinnerPanel.add(staffSpinner);
            
            JPanel staffButtonsPanel = new JPanel();
            JButton cancelButton = new JButton("Cancel");
            JButton confirmDeleteButton = new JButton("Confirm Deletion");
            staffButtonsPanel.add(cancelButton); staffButtonsPanel.add(confirmDeleteButton);
        
        confirmDeleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("DELETE FROM STAFFMEMBER WHERE NAME = '" 
                    + smStaffNames.getValue() + "'");
                    staffDataOverviewScreen(c);
                    deleteStaffFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    staffDataOverviewScreen(c);
                    deleteStaffFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        deleteStaffFrame.add(getStaffNamePanel);
        deleteStaffFrame.add(staffSpinnerPanel);
        deleteStaffFrame.add(staffButtonsPanel);
        deleteStaffFrame.setVisible(true);
    }
    
    /* Overview screen for all orders on the system's table, including options 
    to create, approve and delete (where authorised) orders.*/
    public void orderDataOverviewScreen(Connection c) throws SQLException {
        JFrame orderOverviewFrame = new JFrame("Catering Database - Orders Table");
        orderOverviewFrame.setSize(650,520);
        orderOverviewFrame.setLayout(new FlowLayout());
        orderOverviewFrame.setLocationByPlatform(true);
        orderOverviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JTable showDataTable = new JTable();
        DefaultTableModel dtm = new DefaultTableModel();
        showDataTable.setModel(dtm);
        showDataTable.setSize(2000, 600);
        JScrollPane scroll = new JScrollPane(showDataTable);
        scroll.setSize(2000, 600);
        
        String headers[] = {"ID", "CUSTOMER NAME", "ADDRESS #1", "ADDRESS #2", "POSTCODE", 
            "COST", "GOODS", "REQUESTS", "STATUS", "STAFF NAME"};
        dtm.setColumnIdentifiers(headers);
        showDataTable.getColumn(headers[0]).setWidth(500);
        showDataTable.getColumn(headers[1]).setWidth(500);
        showDataTable.getColumn(headers[2]).setWidth(500);
        showDataTable.getColumn(headers[3]).setWidth(500);
        showDataTable.getColumn(headers[4]).setWidth(500);
        showDataTable.getColumn(headers[5]).setWidth(500);
        showDataTable.getColumn(headers[6]).setWidth(500);
        showDataTable.getColumn(headers[7]).setWidth(500);
        showDataTable.getColumn(headers[8]).setWidth(500);
        showDataTable.getColumn(headers[9]).setWidth(500);
        
        Statement stmnt = c.createStatement();
        ResultSet testResults = stmnt.executeQuery("SELECT * FROM GOODSORDER");
        
        while (testResults.next()) {
            int id = testResults.getInt("ID");
            String custName = testResults.getString("CUSTNAME");
            String addressOne = testResults.getString("ADDRESSLINEONE");
            String addressTwo = testResults.getString("ADDRESSLINETWO");
            String postcode = testResults.getString("POSTCODE");
            double cost = testResults.getDouble("COST");
            String goods = testResults.getString("GOODS");
            String request = testResults.getString("REQUESTS");
            String status = testResults.getString("STATUS");
            String staffName = testResults.getString("STAFFNAME");
            
            Object rowData[] = {id, custName, addressOne, addressTwo, postcode, cost, goods, 
            request, status, staffName};
            dtm.addRow(rowData);
        }
        
        JPanel orderButtons = new JPanel();
        JButton createOrderButton = new JButton("Create new order");
        JButton approveOrderButton = new JButton("Change order approval status");
        JButton deleteOrderButton = new JButton("Delete an order");
        JButton mainMenuReturnButton = new JButton("Back to main menu");
        orderButtons.add(createOrderButton); orderButtons.add(approveOrderButton);
        orderButtons.add(deleteOrderButton); orderButtons.add(mainMenuReturnButton);
        
        createOrderButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createOrderScreen(c);
                    orderOverviewFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        approveOrderButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    approveOrderScreen(c);
                    orderOverviewFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        deleteOrderButton.addActionListener(new ActionListener(){
            private Component frame;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accessLevel == loginLevel.CAN_DELETE) {
                    try {
                       deleteOrderScreen(c);
                       orderOverviewFrame.dispose();
                    } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                    JOptionPane.showMessageDialog(frame, "Sorry, you do not have deletion privileges.");
                }
            }
        });
        
        mainMenuReturnButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                staffMainMenuScreen(c);
                orderOverviewFrame.dispose();
            }
        });
        
        orderOverviewFrame.add(scroll);
        orderOverviewFrame.add(orderButtons);
        orderOverviewFrame.setVisible(true);
    }
    
    /* Screen for creating orders. If the user is a client, their name will be automatically added;
    otherwise the staff member must choose from the client's table.*/
    public void createOrderScreen(Connection c) throws SQLException {
        JFrame orderCreationFrame = new JFrame("Catering Database - Create an order");
        orderCreationFrame.setSize(650,520);
        orderCreationFrame.setLayout(new FlowLayout());
        orderCreationFrame.setLocationByPlatform(true);
        orderCreationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel getCustNamePanel = new JPanel();
        JPanel custSpinnerPanel = new JPanel();
        JLabel getCustNameLabel = new JLabel("Select the customer for the order.");
        Statement stmnt = c.createStatement();
        ResultSet custResults = stmnt.executeQuery("SELECT NAME FROM CLIENT");
        ArrayList<String> custStrings = new ArrayList<String>();
        while (custResults.next()) {
            custStrings.add(custResults.getString("NAME"));
        }
        SpinnerModel smCustNames = new SpinnerListModel(custStrings);
        JSpinner custNamesSpinner = new JSpinner(smCustNames);
        Component custSpinnerEditor = custNamesSpinner.getEditor();
        JFormattedTextField jftf = ((JSpinner.DefaultEditor) custSpinnerEditor).getTextField();
        jftf.setColumns(10);
        getCustNamePanel.add(getCustNameLabel); custSpinnerPanel.add(custNamesSpinner);
        
        JPanel addressOnePanel = new JPanel();
        JLabel addressOneLabel = new JLabel("Enter the first address line:");
        JTextField addressOneField = new JTextField(20);
        addressOnePanel.add(addressOneLabel); addressOnePanel.add(addressOneField);
        
        JPanel addressTwoPanel = new JPanel();
        JLabel addressTwoLabel = new JLabel("Enter the second address line (optional):");
        JTextField addressTwoField = new JTextField(20);
        addressTwoPanel.add(addressTwoLabel); addressTwoPanel.add(addressTwoField);
        
        JPanel postPanel = new JPanel();
        JLabel postLabel = new JLabel("Enter the postcode:");
        JTextField postField = new JTextField(20);
        postPanel.add(postLabel); postPanel.add(postField);
        
        JPanel goodsPanel = new JPanel();
        JLabel biscuitsLabel = new JLabel("Biscuits (£2.99):");
        SpinnerModel smBiscuit = new SpinnerNumberModel(0,0,20,1);
        JSpinner biscuitSpinner = new JSpinner(smBiscuit);
        goodsPanel.add(biscuitsLabel); goodsPanel.add(biscuitSpinner);
        JLabel cakeLabel = new JLabel("Cakes (£4.99)");
        SpinnerModel smCake = new SpinnerNumberModel(0,0,20,1);
        JSpinner cakeSpinner = new JSpinner(smCake);
        goodsPanel.add(cakeLabel); goodsPanel.add(cakeSpinner);
        JLabel sandwichLabel = new JLabel("Sandwiches (£5.50)");
        SpinnerModel smSandwich = new SpinnerNumberModel(0,0,20,1);
        JSpinner sandwichSpinner = new JSpinner(smSandwich);
        goodsPanel.add(sandwichLabel); goodsPanel.add(sandwichSpinner);
        JPanel goodsPanel2 = new JPanel();
        JLabel soupLabel = new JLabel("Soup (£4.50)");
        SpinnerModel smSoup = new SpinnerNumberModel(0,0,20,1);
        JSpinner soupSpinner = new JSpinner(smSoup);
        goodsPanel2.add(soupLabel); goodsPanel2.add(soupSpinner);
        JLabel pastaLabel = new JLabel("Pasta (£6.99)");
        SpinnerModel smPasta = new SpinnerNumberModel(0,0,20,1);
        JSpinner pastaSpinner = new JSpinner(smPasta);
        goodsPanel.add(pastaLabel); goodsPanel.add(pastaSpinner);
        
        
        JPanel costPanel = new JPanel();
        JButton costButton = new JButton("Calculate order cost");
        JTextField costField = new JTextField(20);
        costPanel.add(costButton); costPanel.add(costField);
        costButton.addActionListener(new ActionListener() { /*Allows user to preview the order cost.*/
            @Override
            public void actionPerformed(ActionEvent e) {
                double getCost = returnOrderCost((int)biscuitSpinner.getValue(), 
                            (int)cakeSpinner.getValue(), (int)sandwichSpinner.getValue(), 
                            (int)soupSpinner.getValue(), (int)pastaSpinner.getValue());
                costField.setText(String.valueOf(getCost));
            }
            
        });
        
        JPanel requestsPanel = new JPanel();
        JLabel requestsLabel = new JLabel("Are there any special requests:");
        JTextField requestsArea = new JTextField(40);
        requestsPanel.add(requestsLabel); requestsPanel.add(requestsArea);
        
        JPanel orderButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmCreateButton = new JButton("Confirm creation");
        orderButtonsPanel.add(cancelButton); orderButtonsPanel.add(confirmCreateButton);
        
        confirmCreateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String chooseName;
                    if (accessLevel == loginLevel.CUSTOMER) {
                        chooseName = userName;
                    } else {
                        chooseName = (String) smCustNames.getValue();
                    }
                    String goodsString = "Biscuits x " + (int)biscuitSpinner.getValue() 
                            + " Cakes x" + (int)cakeSpinner.getValue() + " Sandwich set x " 
                            + (int)sandwichSpinner.getValue() + " Soup x " 
                            + (int)soupSpinner.getValue() + " Pasta x " + (int)pastaSpinner.getValue();
                    double costAsDouble = returnOrderCost((int)biscuitSpinner.getValue(), 
                            (int)cakeSpinner.getValue(), (int)sandwichSpinner.getValue(), 
                            (int)soupSpinner.getValue(), (int)pastaSpinner.getValue());
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("INSERT INTO GOODSORDER (CUSTNAME, ADDRESSLINEONE," +
                    " ADDRESSLINETWO, POSTCODE, COST, GOODS, REQUESTS, STATUS) VALUES " 
                    + "('" + chooseName + "', '" + addressOneField.getText() 
                    + "', '" + addressTwoField.getText() + "', '" + postField.getText() + "', " 
                    + costAsDouble + ", '" + goodsString + "', '" + requestsArea.getText() 
                    + "', 'PENDING')");
                    if (accessLevel == loginLevel.CUSTOMER) {
                        clientMenuScreen(c);
                    } else {
                        orderDataOverviewScreen(c);
                    }
                    orderCreationFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (accessLevel == loginLevel.CUSTOMER) {
                       clientMenuScreen(c);
                      } else {
                       orderDataOverviewScreen(c);
                    }
                    orderCreationFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        if (accessLevel != loginLevel.CUSTOMER) {
        orderCreationFrame.add(getCustNamePanel); orderCreationFrame.add(custSpinnerPanel);
        }
        orderCreationFrame.add(addressOnePanel); orderCreationFrame.add(addressTwoPanel);
        orderCreationFrame.add(postPanel); orderCreationFrame.add(costPanel);
        orderCreationFrame.add(goodsPanel); orderCreationFrame.add(goodsPanel2);
        orderCreationFrame.add(requestsPanel); orderCreationFrame.add(orderButtonsPanel);
        orderCreationFrame.setVisible(true);
    }
    
    /*Form that changes the status of an order, and automatically adds the staff member's name.*/
    public void approveOrderScreen(Connection c) throws SQLException {
        JFrame orderApprovalFrame = new JFrame("Catering Database - Change order status");
        orderApprovalFrame.setSize(650,520);
        orderApprovalFrame.setLayout(new FlowLayout());
        orderApprovalFrame.setLocationByPlatform(true);
        orderApprovalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel getOrderIDPanel = new JPanel();
        JLabel getOrderLabel = new JLabel("Choose the order to modify the status of:");
        getOrderIDPanel.add(getOrderLabel);
        
        JPanel idSpinnerPanel = new JPanel();
        Statement stmnt = c.createStatement();
        ResultSet idResults = stmnt.executeQuery("SELECT ID FROM GOODSORDER");
        ArrayList<Integer> idInts = new ArrayList<Integer>();
        while (idResults.next()) {
            idInts.add(idResults.getInt("ID"));
        }
        SpinnerModel smIdNumbers = new SpinnerListModel(idInts);
        JSpinner idNumbersSpinner = new JSpinner(smIdNumbers);
        Component idSpinnerEditor = idNumbersSpinner.getEditor();
        JFormattedTextField jftf = ((JSpinner.DefaultEditor) idSpinnerEditor).getTextField();
        jftf.setColumns(10);
        idSpinnerPanel.add(idNumbersSpinner);
        
        JPanel getApprovalStatusPanel = new JPanel();
        JLabel getApprovalStatusLabel = new JLabel("Choose the approval status:");
        getApprovalStatusPanel.add(getApprovalStatusLabel);
        
        JPanel approvalSpinnerPanel = new JPanel();
        ArrayList<String> statuses = new ArrayList<String>();
        statuses.add("Pending"); statuses.add("Approved"); 
        statuses.add("Rejected"); statuses.add("Taken Place");
        SpinnerModel smStatuses = new SpinnerListModel(statuses);
        JSpinner statusSpinner = new JSpinner(smStatuses);
        Component statusSpinnerEditor = statusSpinner.getEditor();
        JFormattedTextField jftfStatuses = ((JSpinner.DefaultEditor) statusSpinnerEditor).getTextField();
        jftfStatuses.setColumns(10);
        approvalSpinnerPanel.add(statusSpinner);
        
        
        JPanel approvalButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmApprovalButton = new JButton("Confirm approval status change");
        approvalButtonsPanel.add(cancelButton); approvalButtonsPanel.add(confirmApprovalButton);
        
        confirmApprovalButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("UPDATE GOODSORDER SET STATUS = '" + smStatuses.getValue()
                    + "', STAFFNAME = '" + userName + "' WHERE ID = " + smIdNumbers.getValue());
                    orderDataOverviewScreen(c);
                    orderApprovalFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    orderDataOverviewScreen(c);
                    orderApprovalFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        orderApprovalFrame.add(getOrderIDPanel); orderApprovalFrame.add(idSpinnerPanel);
        orderApprovalFrame.add(getApprovalStatusPanel); orderApprovalFrame.add(approvalSpinnerPanel);
        orderApprovalFrame.add(approvalButtonsPanel);
        orderApprovalFrame.setVisible(true);
        
        
    }
    
    /*Form that allows a staff memebr to remove an order from the database table.*/
    public void deleteOrderScreen (Connection c) throws SQLException {
        JFrame deleteOrderFrame = new JFrame("Catering Database - Delete Order");
        deleteOrderFrame.setSize(600,400);
        deleteOrderFrame.setLayout(new FlowLayout());
        deleteOrderFrame.setLocationByPlatform(true);
        deleteOrderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
        JPanel getOrderIDPanel = new JPanel();
        JPanel orderSpinnerPanel = new JPanel();
        JLabel getOrderIDLabel = new JLabel("Select the ID of the order to delete.");
        Statement stmnt = c.createStatement();
        ResultSet orderIDResults = stmnt.executeQuery("SELECT ID FROM GOODSORDER");
        ArrayList<Integer> orderIDList = new ArrayList<Integer>();
        while (orderIDResults.next()) {
            orderIDList.add(orderIDResults.getInt("ID"));
        }
        SpinnerModel smOrderIDs = new SpinnerListModel(orderIDList);
        JSpinner orderSpinner = new JSpinner(smOrderIDs);
        Component orderSpinnerEditor = orderSpinner.getEditor();
        JFormattedTextField jftf = ((JSpinner.DefaultEditor) orderSpinnerEditor).getTextField();
        jftf.setColumns(10);
        getOrderIDPanel.add(getOrderIDLabel); orderSpinnerPanel.add(orderSpinner);
            
        JPanel orderButtonsPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton confirmDeleteButton = new JButton("Confirm Deletion");
        orderButtonsPanel.add(cancelButton); orderButtonsPanel.add(confirmDeleteButton);
        
        confirmDeleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Statement stmnt = c.createStatement();
                    stmnt.executeUpdate("DELETE FROM GOODSORDER WHERE ID = " 
                    + smOrderIDs.getValue());
                    orderDataOverviewScreen(c);
                    deleteOrderFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    orderDataOverviewScreen(c);
                    deleteOrderFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        deleteOrderFrame.add(getOrderIDPanel);
        deleteOrderFrame.add(orderSpinnerPanel);
        deleteOrderFrame.add(orderButtonsPanel);
        deleteOrderFrame.setVisible(true);
    }
    
    /*Menu for an individual client that displays their current orders (if any.)
    The user can also create a new order to await staff approval.*/
    public void clientMenuScreen(Connection c) throws SQLException {
        JFrame clientMenuFrame = new JFrame("Catering Database - Orders for " + userName);
        clientMenuFrame.setSize(650,650);
        clientMenuFrame.setLayout(new FlowLayout());
        clientMenuFrame.setLocationByPlatform(true);
        clientMenuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel introPanel = new JPanel();
        JLabel introLabel = new JLabel("Welcome, " + userName + ". Your current orders are: ");
        introPanel.add(introLabel);
        
        JTable showDataTable = new JTable();
        DefaultTableModel dtm = new DefaultTableModel();
        showDataTable.setModel(dtm);
        showDataTable.setSize(2000, 600);
        JScrollPane scroll = new JScrollPane(showDataTable);
        scroll.setSize(2000, 600);
        
        String headers[] = {"ID", "CUSTOMER NAME", "ADDRESS #1", "ADDRESS #2", "POSTCODE", 
            "COST", "GOODS", "REQUESTS", "STATUS", "STAFF NAME"};
        dtm.setColumnIdentifiers(headers);
        showDataTable.getColumn(headers[0]).setWidth(500);
        showDataTable.getColumn(headers[1]).setWidth(500);
        showDataTable.getColumn(headers[2]).setWidth(500);
        showDataTable.getColumn(headers[3]).setWidth(500);
        showDataTable.getColumn(headers[4]).setWidth(500);
        showDataTable.getColumn(headers[5]).setWidth(500);
        showDataTable.getColumn(headers[6]).setWidth(500);
        showDataTable.getColumn(headers[7]).setWidth(500);
        showDataTable.getColumn(headers[8]).setWidth(500);
        showDataTable.getColumn(headers[9]).setWidth(500);
        
        Statement stmnt = c.createStatement();
        ResultSet testResults = stmnt.executeQuery("SELECT * FROM GOODSORDER WHERE CUSTNAME = '" 
                + userName + "'");
        
        while (testResults.next()) {
            int id = testResults.getInt("ID");
            String custName = testResults.getString("CUSTNAME");
            String addressOne = testResults.getString("ADDRESSLINEONE");
            String addressTwo = testResults.getString("ADDRESSLINETWO");
            String postcode = testResults.getString("POSTCODE");
            double cost = testResults.getDouble("COST");
            String goods = testResults.getString("GOODS");
            String request = testResults.getString("REQUESTS");
            String status = testResults.getString("STATUS");
            String staffName = testResults.getString("STAFFNAME");
            
            Object rowData[] = {id, custName, addressOne, addressTwo, postcode, cost, goods, 
            request, status, staffName};
            dtm.addRow(rowData);
        }
        
        JPanel orderButtons = new JPanel();
        JButton createOrderButton = new JButton("Create New Order");
        JButton logOutButton = new JButton("Log Out");
        orderButtons.add(createOrderButton); orderButtons.add(logOutButton);
        
        createOrderButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    createOrderScreen(c);
                    clientMenuFrame.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(CateringDatabaseSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        
        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginScreen(c);
                userName = "";
                accessLevel = loginLevel.NO_ACCESS; 
                clientMenuFrame.dispose();
            }
            
        });
        
        clientMenuFrame.add(introPanel); clientMenuFrame.add(scroll);
        clientMenuFrame.add(orderButtons); clientMenuFrame.setVisible(true);
    }
    
    /*Maths function that takes the amount of goods ordered from the create order 
     form, calculates the cost of each and returns the amount as a double.*/
    public double returnOrderCost(int biscuitsAmount, int cakeAmount, 
            int sandwichAmount, int soupAmount, int pastaAmount) {
        double biscuitsCost = (biscuitsAmount * 2.99);
        double cakeCost = (cakeAmount * 4.99);
        double sandwichCost = (sandwichAmount * 5.5);
        double soupCost = (soupAmount * 4.5);
        double pastaCost = (pastaAmount * 6.99);
        double totalCost = (biscuitsCost + cakeCost + sandwichCost + soupCost + pastaCost);
        return totalCost;
    }
    
    /*Function for creating an initial connection to the JDBC database.*/
    public Connection getOrderConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        String url = "jdbc:derby://localhost:1527/orderdb";
        String name = "app";
        String pass = "app";
        Connection orderConn = DriverManager.getConnection(url, name, pass);
        return orderConn;
    }
}
