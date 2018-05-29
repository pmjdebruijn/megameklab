/*
 * MegaMekLab - Copyright (C) 2018 - The MegaMek Team
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package megameklab.com.ui.aerospace;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import megamek.common.verifier.EntityVerifier;
import megamek.common.verifier.TestAdvancedAerospace;
import megameklab.com.ui.EntitySource;
import megameklab.com.util.IView;

/**
 * @author Neoancient
 *
 */
public class AdvancedAeroSummaryView extends IView {

    /**
     * 
     */
    private static final long serialVersionUID = 3332719282944878899L;
    
    private JTextField txtStructTon = new JTextField("?");
    private JTextField txtEngineTon = new JTextField("?");   
    private JTextField txtFuelTon = new JTextField("?");
    private JTextField txtControlTon = new JTextField("?");
    private JTextField txtJumpDriveTon = new JTextField("?");
    private JTextField txtHeatTon = new JTextField("?");
    private JTextField txtArmorTon = new JTextField("?");   
    private JTextField txtWeapTon = new JTextField("?");
    private JTextField txtAmmoTon = new JTextField("?");
    private JTextField txtMiscTon = new JTextField("?");
    private JTextField txtTransportTon = new JTextField("?");
    private JTextField txtCrewTon = new JTextField("?");
    private JTextField txtOtherTon = new JTextField("?");

    private EntityVerifier entityVerifier = EntityVerifier.getInstance(
            new File("data/mechfiles/UnitVerifierOptions.xml"));

    public AdvancedAeroSummaryView(EntitySource eSource) {
        super(eSource);

        Vector<JTextField> valueFields = new Vector<JTextField>();

        valueFields.add(txtStructTon);
        valueFields.add(txtEngineTon);
        valueFields.add(txtFuelTon);        
        valueFields.add(txtControlTon);
        valueFields.add(txtJumpDriveTon);
        valueFields.add(txtHeatTon);
        valueFields.add(txtArmorTon);
        valueFields.add(txtWeapTon);
        valueFields.add(txtAmmoTon);
        valueFields.add(txtMiscTon);
        valueFields.add(txtTransportTon);
        valueFields.add(txtCrewTon);
        valueFields.add(txtOtherTon);

        Dimension size = new Dimension(80,25);
        for(JTextField field : valueFields) {
            field.setEditable(false);
            field.setSize(size);
            field.setPreferredSize(size);
            field.setMinimumSize(size);
            field.setMaximumSize(size);
            field.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        valueFields.removeAllElements();

        size = new Dimension(80,25);
        for(JTextField field : valueFields) {
            field.setEditable(false);
            field.setSize(size);
            field.setPreferredSize(size);
            field.setMinimumSize(size);
            field.setMaximumSize(size);
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        size = new Dimension(120,25);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,0,0,5);
        this.add(createLabel("Category", size, SwingConstants.CENTER), gbc);
        gbc.gridy = 1;
        this.add(createLabel("SI:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 2;
        this.add(createLabel("Engine:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 3;
        this.add(createLabel("Fuel:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 4;
        this.add(createLabel("Control Systems:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 5;
        this.add(createLabel("KF Drive and Sail:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 6;
        this.add(createLabel("Heat Sinks:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 7;
        this.add(createLabel("Armor", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 8;
        this.add(createLabel("Weapons:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 9;
        this.add(createLabel("Ammo:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 10;
        this.add(createLabel("Equipment:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 11;
        this.add(createLabel("Transport:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 12;
        this.add(createLabel("Crew:", size, SwingConstants.RIGHT), gbc);
        gbc.gridy = 13;
        this.add(createLabel("Other:", size, SwingConstants.RIGHT), gbc);

        size = new Dimension(45,25);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,0,0,0);
        this.add(createLabel("Ton", size, SwingConstants.CENTER), gbc);
        gbc.gridy = 1;
        this.add(txtStructTon, gbc);
        gbc.gridy = 2;
        this.add(txtEngineTon, gbc);
        gbc.gridy = 3;
        this.add(txtFuelTon, gbc);
        gbc.gridy = 4;
        this.add(txtControlTon, gbc);
        gbc.gridy = 5;
        this.add(txtJumpDriveTon, gbc);
        gbc.gridy = 6;
        this.add(txtHeatTon, gbc);
        gbc.gridy = 7;
        this.add(txtArmorTon, gbc);
        gbc.gridy = 8;
        this.add(txtWeapTon, gbc);
        gbc.gridy = 9;
        this.add(txtAmmoTon, gbc);
        gbc.gridy = 10;
        this.add(txtMiscTon, gbc);
        gbc.gridy = 11;
        this.add(txtTransportTon, gbc);
        gbc.gridy = 12;
        this.add(txtCrewTon, gbc);
        gbc.gridy = 13;
        this.add(txtOtherTon, gbc);

        setBorder(BorderFactory.createTitledBorder("Summary"));

    }

    private JLabel createLabel(String text, Dimension size, int align) {

        JLabel label = new JLabel(text, SwingConstants.TRAILING);

        setFieldSize(label, size);
        label.setHorizontalAlignment(align);
        return label;
    }

    public void setFieldSize(JComponent box, Dimension maxSize) {
        box.setPreferredSize(maxSize);
        box.setMaximumSize(maxSize);
        box.setMinimumSize(maxSize);
    }

    public void refresh() {
        TestAdvancedAerospace testShip = 
                new TestAdvancedAerospace(getJumpship(), entityVerifier.aeroOption, null);
       
        txtStructTon.setText(Double.toString(testShip.getWeightStructure()));
        txtEngineTon.setText(Double.toString(testShip.getWeightEngine()));
        txtFuelTon.setText(Double.toString(testShip.getWeightFuel()));
        txtControlTon.setText(Double.toString(testShip.getWeightControls()));
        txtJumpDriveTon.setText(Double.toString(testShip.getWeightDrive()
                + testShip.getWeightSail()));
        txtHeatTon.setText(Double.toString(testShip.getWeightHeatSinks()));        
        txtArmorTon.setText(Double.toString(testShip.getWeightArmor()));
        txtWeapTon.setText(Double.toString(testShip.getWeightWeapon()));
        txtAmmoTon.setText(Double.toString(testShip.getWeightAmmo()));
        txtMiscTon.setText(Double.toString(testShip.getWeightMiscEquip()));
        txtTransportTon.setText(Double.toString(testShip.getWeightCarryingSpace()));
        txtCrewTon.setText(Double.toString(testShip.getWeightQuarters()));
        txtOtherTon.setText(Double.toString(testShip.getWeightMisc()));
    }
}