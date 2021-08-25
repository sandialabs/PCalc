package gov.sandia.gnem.dbutillib.gui.util;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.ParInfoGui;
import gov.sandia.gnem.dbutillib.gui.SchemaPanel;
import gov.sandia.gnem.dbutillib.gui.SchemaPanelDB;

import javax.swing.*;

public class TestSchemaPanel {
    public static void main(String[] args) {
        try {
            // Tool Tip Checking - create schemas with all sorts of options
            ParInfo parInfo = new ParInfo();
            // Type
            parInfo.addParameter(ParInfoGui.SCHEMA_PANEL_TYPE, String.valueOf(SchemaPanel.OUTPUT));
            parInfo.addParameter(ParInfoGui.MODIFIABLE, "true");
            parInfo.addParameter(ParInfoGui.TABLES, "origin \narrival\nassoc\nevent");
            // Create the schemaPanel
            SchemaPanel schemaPanel = new SchemaPanelDB(parInfo);
            schemaPanel.setVisible(true);
            //schemaPanel.setIDGapsTableVisible(false);

            JFrame frame = new JFrame("Testing SchemaPanel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JScrollPane(schemaPanel));
            frame.setSize(800, 800);
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("ERROR");
            e.printStackTrace(System.err);
        }
    }
}
