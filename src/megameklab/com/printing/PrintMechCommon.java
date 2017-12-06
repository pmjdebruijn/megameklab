/*
 * MegaMekLab - Copyright (C) 2017 - The MegaMek Team
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
package megameklab.com.printing;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.kitfox.svg.Group;
import com.kitfox.svg.Path;
import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;
import com.kitfox.svg.Tspan;
import com.kitfox.svg.animation.AnimationElement;

import megamek.common.AmmoType;
import megamek.common.CriticalSlot;
import megamek.common.Engine;
import megamek.common.Entity;
import megamek.common.EquipmentMessages;
import megamek.common.ITechnology;
import megamek.common.LAMPilot;
import megamek.common.LandAirMech;
import megamek.common.Mech;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.QuadVee;
import megamek.common.options.IOption;
import megamek.common.options.IOptionGroup;
import megamek.common.options.Quirks;
import megameklab.com.util.ImageHelper;
import megameklab.com.util.RecordSheetEquipmentLine;
import megameklab.com.util.UnitUtil;

/**
 * @author Neoancient
 *
 */
public class PrintMechCommon extends PrintEntity {
    
    /**
     * The current mech being printed.
     */
    private final Mech mech;
    
    public PrintMechCommon(Mech mech, int startPage) {
        super(startPage);
        this.mech = mech;
    }
    
    protected String getSVGFileName() {
        if (mech.hasETypeFlag(Entity.ETYPE_QUADVEE)) {
            return "mech_quadvee.svg";
        } else if (mech.hasETypeFlag(Entity.ETYPE_QUAD_MECH)) {
            return "mech_quad_default.svg";
        } else if (mech.hasETypeFlag(Entity.ETYPE_TRIPOD_MECH)) {
            return "mech_tripod_default.svg";
        } else if (mech.hasETypeFlag(Entity.ETYPE_LAND_AIR_MECH)) {
            return "mech_biped_lam.svg";
        } else {
            return "mech_biped_default.svg";
        }
    }
    
    @Override
    protected String getRecordSheetTitle() {
        StringBuilder sb = new StringBuilder();
        // General qualifier
        if (mech.isSuperHeavy()) {
            sb.append("SuperHeavy ");
        }
        if (mech.isPrimitive()) {
            sb.append("Primitive ");
        }
        if ((mech instanceof LandAirMech) && (((LandAirMech) mech).getLAMType() == LandAirMech.LAM_BIMODAL)) {
            sb.append("Bimodal ");
        }
        // Leg configuration
        if (mech.hasETypeFlag(Entity.ETYPE_QUAD_MECH)
                && !mech.hasETypeFlag(Entity.ETYPE_QUADVEE)) {
            sb.append("Four-Legged ");
        } else if (mech.hasETypeFlag(Entity.ETYPE_TRIPOD_MECH)) {
            sb.append("Three-Legged ");
        }
        // mech type
        if (mech.hasETypeFlag(Entity.ETYPE_LAND_AIR_MECH)) {
            sb.append("Land-Air 'Mech");
        } else if (mech.hasETypeFlag(Entity.ETYPE_QUADVEE)) {
            if (mech.isOmni()) {
                sb.append("Omni");
            }
            sb.append("QuadVee");
        } else if (mech.isIndustrial()) {
            sb.append("IndustrialMech");
        } else if (mech.isOmni()) {
            sb.append("OmniMech");
        } else {
            sb.append("BattleMech");
        }
        sb.append(" Record Sheet");
        return sb.toString();
    }
    
    @Override
    protected Entity getEntity() {
        return mech;
    }
    
    @Override
    public void printImage(Graphics2D g2d, PageFormat pageFormat, int pageNum) throws SVGException {
        printShields();
        
        super.printImage(g2d, pageFormat, pageNum);

        for (int loc = 0; loc < mech.locations(); loc++) {
            SVGElement critRect = getSVGDiagram().getElement("crits_" + mech.getLocationAbbr(loc));
            if (null != critRect) {
                writeLocationCriticals(loc, (Rect) critRect);
            }
        }
        
        if (mech.getGyroType() == Mech.GYRO_HEAVY_DUTY) {
            SVGElement pip = getSVGDiagram().getElement("heavyDutyGyroPip");
            if ((null != pip) && pip.hasAttribute("visibility", AnimationElement.AT_XML)) {
                pip.setAttribute("visibility", AnimationElement.AT_XML, "visible");
            }
            pip.updateTime(0);
        }
        
        SVGElement hsRect = getSVGDiagram().getElement("heatSinkPips");
        if (null != hsRect) {
            drawHeatSinkPips((Rect) hsRect);
        }

        if (mech.hasETypeFlag(Entity.ETYPE_LAND_AIR_MECH)) {
            SVGElement si = getSVGDiagram().getElement("siPips");
            addPips(si, mech.getOInternal(Mech.LOC_CT), true, PipType.CIRCLE, 0.38, 0.957);
        }
        
    }
    
    private void printShields() throws SVGException {
        for (Mounted m : mech.getMisc()) {
            if (((MiscType) m.getType()).isShield()) {
                String loc = mech.getLocationAbbr(m.getLocation());
                SVGElement element;
                element = getSVGDiagram().getElement("armorDiagram" + loc);
                if (null != element) {
                    hideElement(element, true);
                }
                element = getSVGDiagram().getElement("shield" + loc);
                if (null != element) {
                    hideElement(element, false);
                }
                element = getSVGDiagram().getElement("shieldDC" + loc);
                if (null != element) {
                    addPips(element, m.getBaseDamageCapacity(), false, PipType.CIRCLE);
                }
                element = getSVGDiagram().getElement("shieldDA" + loc);
                if (null != element) {
                    addPips(element, m.getBaseDamageAbsorptionRate(), false, PipType.DIAMOND);
                }
                element.updateTime(0);
            }
        }
    }

    @Override
    protected void writeTextFields() throws SVGException {
        super.writeTextFields();
        if (mech.hasUMU()) {
            SVGElement svgEle = getSVGDiagram().getElement("mpJumpLabel");
            if (null != svgEle) {
                ((Tspan) svgEle).setText("Underwater:");
                ((Text) svgEle.getParent()).rebuild();
            }
        }
        hideElement("warriorDataSingle", mech.getCrew().getSlotCount() != 1);
        hideElement("warriorDataDual", mech.getCrew().getSlotCount() != 2);
        hideElement("warriorDataTriple", mech.getCrew().getSlotCount() != 3);
        setTextField("hsType", formatHeatSinkType());
        setTextField("hsCount", formatHeatSinkCount());
        
        if (mech instanceof LandAirMech) {
            LandAirMech lam = (LandAirMech) mech;
            if (lam.getLAMType() == LandAirMech.LAM_BIMODAL) {
                setTextField("mpAirMechWalk", "\u2014"); // em dash
                setTextField("mpAirMechRun", "\u2014");
                setTextField("mpAirMechCruise", "\u2014");
                setTextField("mpAirMechFlank", "\u2014");
            } else {
                setTextField("mpAirMechWalk", Integer.toString(lam.getAirMechWalkMP()));
                setTextField("mpAirMechRun", Integer.toString(lam.getAirMechRunMP()));
                setTextField("mpAirMechCruise", Integer.toString(lam.getAirMechCruiseMP()));
                setTextField("mpAirMechFlank", Integer.toString(lam.getAirMechFlankMP()));
            }
            setTextField("mpSafeThrust", Integer.toString(lam.getJumpMP()));
            setTextField("mpMaxThrust", Integer.toString((int) Math.ceil(lam.getJumpMP() * 1.5)));
            if (!lam.getCrew().getName().equalsIgnoreCase("unnamed") && (lam.getCrew() instanceof LAMPilot)) {
                setTextField("asfGunnerySkill", Integer.toString(((LAMPilot) mech.getCrew()).getGunneryAero()));
                setTextField("asfPilotingSkill", Integer.toString(((LAMPilot) mech.getCrew()).getPilotingAero()));
            } else {
                hideElement("asfGunnerySkill");
                hideElement("asfPilotingSkill");
            }
        } else if (mech instanceof QuadVee) {
            setTextField("mpCruise", Integer.toString(((QuadVee) mech).getCruiseMP(false, false, false)));
            setTextField("mpFlank", formatQuadVeeFlank());
            setTextField("lblVeeMode", ((QuadVee) mech).getMotiveTypeString() + "s");
        }
    }
    
    @Override
    protected void drawArmor() throws SVGException {
        final String FORMAT = "( %d )";
        SVGElement element = null;
        for (int loc = 0; loc < mech.locations(); loc++) {
            element = getSVGDiagram().getElement("textArmor_" + mech.getLocationAbbr(loc));
            if (null != element) {
                ((Text) element).getContent().clear();
                ((Text) element).appendText(String.format(FORMAT, mech.getOArmor(loc)));
                ((Text) element).rebuild();
            }
            element = getSVGDiagram().getElement("textIS_" + mech.getLocationAbbr(loc));
            if (null != element) {
                ((Text) element).getContent().clear();
                ((Text) element).appendText(String.format(FORMAT, mech.getOInternal(loc)));
                ((Text) element).rebuild();
            }
            if (mech.isSuperHeavy() && (loc == Mech.LOC_HEAD)) {
                element = getSVGDiagram().getElement("armorPips" + mech.getLocationAbbr(loc) + "_SH");
            } else {
                element = getSVGDiagram().getElement("armorPips" + mech.getLocationAbbr(loc));
            }
            if (null != element) {
                addPips(element, mech.getOArmor(loc),
                        (loc == Mech.LOC_HEAD) || (loc == Mech.LOC_CT) || (loc == Mech.LOC_CLEG),
                        PipType.forAT(mech.getArmorType(loc)));
                //                        setArmorPips(element, mech.getOArmor(loc), true);
                //                      (loc == Mech.LOC_HEAD) || (loc == Mech.LOC_CT));
            }
            if (loc > Mech.LOC_HEAD) {
                element = getSVGDiagram().getElement("isPips" + mech.getLocationAbbr(loc));
                if (null != element) {
                    addPips(element, mech.getOInternal(loc),
                            (loc == Mech.LOC_HEAD) || (loc == Mech.LOC_CT) || (loc == Mech.LOC_CLEG));
                }
            }
            if (mech.hasRearArmor(loc)) {
                element = getSVGDiagram().getElement("textArmor_" + mech.getLocationAbbr(loc) + "R");
                if (null != element) {
                    ((Text) element).getContent().clear();
                    ((Text) element).appendText(String.format(FORMAT, mech.getOArmor(loc, true)));
                    ((Text) element).rebuild();
                }
                element = getSVGDiagram().getElement("armorPips" + mech.getLocationAbbr(loc) + "R");
                if (null != element) {
                    addPips(element, mech.getOArmor(loc, true), loc == Mech.LOC_CT,
                            PipType.forAT(mech.getArmorType(loc)));
                }
            }
            
        }
        if (mech.isSuperHeavy()) {
            element = getSVGDiagram().getElement("isPipsHD");
            if (null != element) {
                hideElement(element, true);
            }
            element = getSVGDiagram().getElement("isPipsHD_SH");
            if (null != element) {
                hideElement(element, false);
            }
        }
        getSVGDiagram().updateTime(0);
    }
    
    @Override
    protected void writeEquipment(Rect svgRect) throws SVGException {
        Map<Integer, Map<RecordSheetEquipmentLine,Integer>> eqMap = new TreeMap<>();
        Map<String,Integer> ammo = new TreeMap<>();
        for (Mounted m : mech.getEquipment()) {
            if (m.getType() instanceof AmmoType) {
                if (m.getLocation() != Entity.LOC_NONE) {
                    String shortName = m.getType().getShortName().replace("Ammo", "");
                    shortName = shortName.replace("(Clan)", "");
                    shortName = shortName.replace("-capable", "");
                    ammo.merge(shortName, m.getBaseShotsLeft(), Integer::sum);
                }
                continue;
            }
            if ((m.getType() instanceof AmmoType)
                    || (m.getLocation() == Entity.LOC_NONE)
                    || !UnitUtil.isPrintableEquipment(m.getType(), true)) {
                continue;
            }
            if (mech.hasETypeFlag(Entity.ETYPE_QUADVEE)
                    && (m.getType() instanceof MiscType)
                    && m.getType().hasFlag(MiscType.F_TRACKS)) {
                continue;
            }
            eqMap.putIfAbsent(m.getLocation(), new HashMap<>());
            RecordSheetEquipmentLine line = new RecordSheetEquipmentLine(m);
            eqMap.get(m.getLocation()).merge(line, 1, Integer::sum);
        }
        
        Rectangle2D bbox = svgRect.getBoundingBox();
        SVGElement canvas = svgRect.getRoot();
        int viewWidth = (int)bbox.getWidth();
        int viewHeight = (int)bbox.getHeight();
        int viewX = (int)bbox.getX();
        int viewY = (int)bbox.getY();
        
        int qtyX = (int) Math.round(viewX + viewWidth * 0.037);
        int nameX = (int) Math.round(viewX + viewWidth * 0.08);
        int locX = (int) Math.round(viewX + viewWidth * 0.41);
        int heatX = (int) Math.round(viewX + viewWidth * 0.48);
        int dmgX = (int) Math.round(viewX + viewWidth * 0.53);
        int minX = (int) Math.round(viewX + viewWidth * 0.72);
        int shortX = (int) Math.round(viewX + viewWidth * 0.8);
        int medX = (int) Math.round(viewX + viewWidth * 0.88);
        int longX = (int) Math.round(viewX + viewWidth * 0.96);
        
        int indent = (int) Math.round(viewWidth * 0.02);
        
        int currY = viewY + 10;
        
        double fontSize = FONT_SIZE_MEDIUM;
        double lineHeight = getFontHeight(fontSize, canvas) * 1.2;
        
        addTextElement(canvas, qtyX, currY, "Qty", fontSize, "middle", "bold");
        addTextElement(canvas, nameX + indent, currY, "Type", fontSize, "start", "bold");
        addTextElement(canvas, locX,  currY, "Loc", fontSize, "middle", "bold");
        addTextElement(canvas, heatX, currY, "Ht", fontSize, "middle", "bold");
        addTextElement(canvas, dmgX, currY, "Dmg", fontSize, "start", "bold");
        addTextElement(canvas, minX, currY, "Min", fontSize, "middle", "bold");
        addTextElement(canvas, shortX, currY, "Sht", fontSize, "middle", "bold");
        addTextElement(canvas, medX, currY, "Med", fontSize, "middle", "bold");
        addTextElement(canvas, longX, currY, "Lng", fontSize, "middle", "bold");
        currY += lineHeight;

        for (Integer loc : eqMap.keySet()) {
            for (RecordSheetEquipmentLine line : eqMap.get(loc).keySet()) {
                for (int row = 0; row < line.nRows(); row++) {
                    int lines;
                    if (row == 0) {
                        addTextElement(canvas, qtyX, currY, Integer.toString(eqMap.get(loc).get(line)), fontSize, "middle", "normal");
                        lines = addMultilineTextElement(canvas, nameX, currY, locX - nameX, lineHeight,
                                line.getNameField(row, mech.isMixedTech()), fontSize, "start", "normal");

                    } else {
                        lines = addMultilineTextElement(canvas, nameX + indent, currY, locX - nameX, lineHeight,
                                line.getNameField(row, mech.isMixedTech()), fontSize, "start", "normal");
                    }
                    addTextElement(canvas, locX,  currY, line.getLocationField(row), fontSize, "middle", "normal");
                    addTextElement(canvas, heatX, currY, line.getHeatField(row), fontSize, "middle", "normal");
                    lines = Math.max(lines, addMultilineTextElement(canvas, dmgX, currY, minX - dmgX, lineHeight,
                            line.getDamageField(row), fontSize, "start", "normal"));
                    addTextElement(canvas, minX, currY, line.getMinField(row), fontSize, "middle", "normal");
                    addTextElement(canvas, shortX, currY, line.getShortField(row), fontSize, "middle", "normal");
                    addTextElement(canvas, medX, currY, line.getMediumField(row), fontSize, "middle", "normal");
                    addTextElement(canvas, longX, currY, line.getLongField(row), fontSize, "middle", "normal");
                    currY += lineHeight * lines;
                }
            }
        }
        
        StringJoiner quirksList = new StringJoiner(", ");
        Quirks quirks = mech.getQuirks();
        for (Enumeration<IOptionGroup> optionGroups = quirks.getGroups(); optionGroups.hasMoreElements();) {
            IOptionGroup optiongroup = optionGroups.nextElement();
            if (quirks.count(optiongroup.getKey()) > 0) {
                for (Enumeration<IOption> options = optiongroup.getOptions(); options.hasMoreElements();) {
                    IOption option = options.nextElement();
                    if (option != null && option.booleanValue()) {
                        quirksList.add(option.getDisplayableNameWithValue());
                    }
                }
            }
        }

        if ((ammo.size() > 0) || (quirksList.length() > 0)) {
            Group svgGroup = new Group();
            canvas.loaderAddChild(null, svgGroup);
            int lines = 0; 
            if (ammo.size() > 0) {
                lines = addMultilineTextElement(svgGroup, viewX + viewWidth * 0.025, 0, viewWidth * 0.95, lineHeight,
                        "Ammo: " + ammo.entrySet().stream()
                        .map(e -> String.format("(%s) %d", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(", ")), fontSize, "start", "normal");
            }
            if (quirksList.length() > 0) {
                addMultilineTextElement(svgGroup, viewX + viewWidth * 0.025, lines * lineHeight,
                        viewWidth * 0.95, lineHeight,
                        "Quirks: " + quirksList.toString(), fontSize, "start", "normal");
            }
            svgGroup.addAttribute("transform", AnimationElement.AT_XML,
                    String.format("translate(0,%f)", viewY + viewHeight - svgGroup.getBoundingBox().getHeight()));
            svgGroup.updateTime(0);
        }        

    }
    
    private void writeLocationCriticals(int loc, Rect svgRect) throws SVGException {
        Rectangle2D bbox = svgRect.getBoundingBox();
        SVGElement canvas = svgRect.getRoot();
        int viewWidth = (int)bbox.getWidth();
        int viewHeight = (int)bbox.getHeight();
        int viewX = (int)bbox.getX();
        int viewY = (int)bbox.getY();
        
        double rollX = viewX;
        double critX = viewX + viewWidth * 0.11;
        double gap = 0;
        if (mech.getNumberOfCriticals(loc) > 6) {
            gap = viewHeight * 0.05;
        }
        double lineHeight = (viewHeight - gap) / mech.getNumberOfCriticals(loc);
        double currY = viewY;
        double fontSize = lineHeight * 0.9;
        
        Mounted startingMount = null;
        double startingMountY = 0;
        double endingMountY = 0;
        double connWidth = viewWidth * 0.02;
        
        double x = viewX + viewWidth * 0.075;
        x += addTextElement(canvas, x, viewY - 1, mech.getLocationName(loc),
                fontSize * 1.25, "start", "bold");
        if (mech.isClan() && UnitUtil.hasAmmo(mech, loc) && !mech.hasCASEII(loc)) {
            addTextElement(canvas, x + fontSize / 2, viewY - 1, "(CASE)", fontSize, "start", "normal");
        }
        
        for (int slot = 0; slot < mech.getNumberOfCriticals(loc); slot++) {
            currY += lineHeight;
            if (slot == 6) {
                currY += gap;
            }
            addTextElement(canvas, rollX, currY, ((slot % 6) + 1) + ".", fontSize, "start", "bold");
            CriticalSlot crit = mech.getCritical(loc, slot);
            String style = "bold";
            String fill = "#000000";
            if ((null == crit)
                    || ((crit.getType() == CriticalSlot.TYPE_EQUIPMENT)
                            && (!crit.getMount().getType().isHittable()))) {
                style = "standard";
                fill = "#3f3f3f";
                addTextElement(canvas, critX, currY, formatCritName(crit), fontSize, "start", style, fill);
            } else if (crit.isArmored()) {
                SVGElement pip = createPip(critX, currY - fontSize * 0.8, fontSize * 0.4, 0.7);
                canvas.loaderAddChild(null, pip);
                canvas.updateTime(0);
                addTextElement(canvas, critX + fontSize, currY, formatCritName(crit), fontSize, "start", style, fill);
            } else if ((crit.getType() == CriticalSlot.TYPE_EQUIPMENT)
                    && (crit.getMount().getType() instanceof MiscType)
                    && (crit.getMount().getType().hasFlag(MiscType.F_MODULAR_ARMOR))) {
                String critName = formatCritName(crit);
                addTextElement(canvas, critX, currY, critName, fontSize, "start", style, fill);
                x = critX + getTextLength(critName, fontSize, canvas);
                double remainingW = viewX + viewWidth - x;
                double spacing = remainingW / 6.0;
                double radius = spacing * 0.25;
                double y = currY - lineHeight + spacing;
                double y2 = currY - spacing;
                x += spacing;
                for (int i = 0; i < 10; i++) {
                    if (i == 5) {
                        x -= spacing * 5.5;
                        y = y2;
                    }
                    SVGElement pip = createPip(x, y, radius, 0.5);
                    canvas.loaderAddChild(null, pip);
                    canvas.updateTime(0);
                    x += spacing;
                }
            } else {
                addTextElement(canvas, critX, currY, formatCritName(crit), fontSize, "start", style, fill);
            }
            Mounted m = null;
            if ((null != crit) && (crit.getType() == CriticalSlot.TYPE_EQUIPMENT)
                    && (crit.getMount().getType().isHittable())
                    && (crit.getMount().getType().getCriticals(mech) > (mech.isSuperHeavy()? 2 : 1))) {
                m = crit.getMount();
            }
            if ((startingMount != null) && (startingMount != m)) {
                connectSlots(canvas, critX - 1, startingMountY, connWidth, endingMountY - startingMountY);
            }
            if (m != startingMount) {
                startingMount = m;
                if (null != m) {
                    startingMountY = currY - lineHeight * 0.6;
                }
            } else {
                endingMountY = currY;
            }
        }
        if ((null != startingMount) && (mech.getNumberOfCriticals(startingMount.getType(), loc) > 1)) {
            connectSlots(canvas, critX - 1, startingMountY, connWidth, endingMountY - startingMountY);
        }
    }

    private void connectSlots(SVGElement canvas, double x, double y, double w,
            double h) throws SVGElementException, SVGException {
        Path p = new Path();
        p.addAttribute("d", AnimationElement.AT_XML,
                "M " + x + " " + y
                + " h " + (-w)
                + " v " + h
                + " h " + w);
        p.addAttribute("stroke", AnimationElement.AT_CSS, "black");
        p.addAttribute("stroke-width", AnimationElement.AT_CSS, "0.72");
        p.addAttribute("fill", AnimationElement.AT_CSS, "none");
        p.updateTime(0);
        canvas.loaderAddChild(null, p);
        canvas.updateTime(0);
    }
    
    @Override
    protected void drawFluffImage() throws SVGException {
        Rect rect = null;
        if (mech.getCrew().getSlotCount() == 3) {
            rect = (Rect) getSVGDiagram().getElement("fluffTriplePilot");
        } else if (mech.getCrew().getSlotCount() == 2) {
            rect = (Rect) getSVGDiagram().getElement("fluffDualPilot");
        } else {
            rect = (Rect) getSVGDiagram().getElement("fluffSinglePilot");
        }
        if (null != rect) {
            embedImage(ImageHelper.getFluffFile(mech, ImageHelper.imageMech),
                    rect.getParent(), rect.getBoundingBox(), true);
        }
    }
    
    private void drawHeatSinkPips(Rect svgRect) throws SVGException {
        Rectangle2D bbox = svgRect.getBoundingBox();
        SVGElement canvas = svgRect.getRoot();
        int viewWidth = (int)bbox.getWidth();
        int viewHeight = (int)bbox.getHeight();
        int viewX = (int)bbox.getX();
        int viewY = (int)bbox.getY();
        
        int hsCount = mech.heatSinks();

        // r = 3.5
        // spacing = 9.66
        // stroke width = 0.9
        double size = 9.66;
        int cols = (int) (viewWidth / size);
        int rows = (int) (viewHeight / size);
        
        // Use 10 pips/column unless there are too many sinks for the space.
        if (hsCount <= cols * 10) {
            rows = 10;
        }
        // The rare unit with this many heat sinks will require us to shrink the pips
        while (hsCount > rows * cols) {
            // Figure out how much we will have to shrink to add another column
            double nextCol = (cols + 1.0) / cols;
            // First check whether we can shrink them less than what is required for a new column
            if (cols * (int) (rows * nextCol) > hsCount) {
                rows = (int) Math.ceil((double) hsCount / cols);
                size = viewHeight / rows;
            } else {
                cols++;
                size = viewWidth / (cols * size);
                rows = (int) (viewHeight / size);
            }
        }
        double radius = size * 0.36;
        double strokeWidth = 0.9;
        for (int i = 0; i < hsCount; i++) {
            int row = i % rows;
            int col = i / rows;
            SVGElement pip = this.createPip(viewX + size * col, viewY + size * row, radius, strokeWidth);
            canvas.loaderAddChild(null, pip);
            canvas.updateTime(0);
        }
    }
    
    @Override
    protected String formatWalk() {
        if (mech.hasTSM()) {
            return mech.getWalkMP() + " [" + (mech.getWalkMP() + 1) + "]";
        } else {
            return Integer.toString(mech.getWalkMP());
        }
    }
    
    @Override
    protected String formatRun() {
        int mp = mech.getWalkMP();
        if (mech.hasTSM()) {
            mp++;
        }
        if ((mech.getMASC() != null) && (mech.getSuperCharger() != null)) {
            mp = (int) Math.ceil(mp * 2.5);
        } else if ((mech.getMASC() != null) || (mech.getSuperCharger() != null)) {
            mp *= 2;
        } else {
            mp = (int) Math.ceil(mp * 1.5);
        }
        if (mech.hasMPReducingHardenedArmor()) {
            mp--;
        }
        if (mp > mech.getRunMPwithoutMASC()) {
            return mech.getRunMPwithoutMASC() + " [" + mp + "]";
        } else {
            return Integer.toString(mech.getRunMP());
        }
    }
    
    private String formatQuadVeeFlank() {
        int mp = ((QuadVee) mech).getCruiseMP(false, false, false);
        int noSupercharger = (int) Math.ceil(mp * 1.5);
        if (mech.getSuperCharger() != null) {
            mp *= 2;
        } else {
            mp = noSupercharger;
        }
        if (mech.hasMPReducingHardenedArmor()) {
            mp--;
            noSupercharger--;
        }
        if (mp > noSupercharger) {
            return noSupercharger + " [" + mp + "]";
        } else {
            return Integer.toString(mp);
        }
    }
    
    protected String formatJump() {
        if (mech.hasUMU()) {
            return Integer.toString(mech.getActiveUMUCount());
        } else {
            return Integer.toString(mech.getJumpMP());
        }
    }

    private String formatHeatSinkType() {
        if (mech.hasLaserHeatSinks()) {
            return "Laser Heat Sinks:";
        } else if (mech.hasDoubleHeatSinks()) {
            return "Double Heat Sinks:";
        } else {
            return "Heat Sinks";
        }
    }
    
    private String formatHeatSinkCount() {
        int hsCount = mech.heatSinks();
        if (mech.hasDoubleHeatSinks()) {
            return String.format("%d (%d)", hsCount, hsCount * 2);
        } else {
            return Integer.toString(hsCount);
        }
    }
    
    private String formatCritName(CriticalSlot cs) {
        if (null == cs) {
            return "Roll Again";
        } else if (cs.getType() == CriticalSlot.TYPE_SYSTEM) {
            if (cs.getIndex() == Mech.SYSTEM_ENGINE) {
                StringBuilder sb = new StringBuilder();
                if (mech.isPrimitive()) {
                    sb.append("Primitive ");
                }
                switch (mech.getEngine().getEngineType()) {
                    case Engine.COMBUSTION_ENGINE:
                        sb.append("I.C.E."); //$NON-NLS-1$
                        break;
                    case Engine.NORMAL_ENGINE:
                        sb.append("Fusion"); //$NON-NLS-1$
                        break;
                    case Engine.XL_ENGINE:
                        sb.append("XL Fusion"); //$NON-NLS-1$
                        break;
                    case Engine.LIGHT_ENGINE:
                        sb.append("Light Fusion"); //$NON-NLS-1$
                        break;
                    case Engine.XXL_ENGINE:
                        sb.append("XXL Fusion"); //$NON-NLS-1$
                        break;
                    case Engine.COMPACT_ENGINE:
                        sb.append("Compact Fusion"); //$NON-NLS-1$
                        break;
                    case Engine.FUEL_CELL:
                        sb.append("Fuel Cell"); //$NON-NLS-1$
                        break;
                    case Engine.FISSION:
                        sb.append("Fission"); //$NON-NLS-1$
                        break;
                }
                sb.append(" Engine");
                return sb.toString();
            } else {
                String name = mech.getSystemName(cs.getIndex()).replace("Standard ", "");
                if (((cs.getIndex() >= Mech.ACTUATOR_UPPER_ARM) && (cs.getIndex() <= Mech.ACTUATOR_HAND))
                        || ((cs.getIndex() >= Mech.ACTUATOR_UPPER_LEG) && (cs.getIndex() <= Mech.ACTUATOR_FOOT))) {
                    name += " Actuator";
                } else if (cs.getIndex() == Mech.SYSTEM_COCKPIT) {
                    if (mech.getCockpitType() == Mech.COCKPIT_COMMAND_CONSOLE) {
                        if (mech.getCrewForCockpitSlot(Mech.LOC_HEAD, cs) == 0) {
                            name = EquipmentMessages.getString("SystemType.Cockpit.STANDARD_COCKPIT");
                        }
                    } else if ((mech.getCockpitType() == Mech.COCKPIT_DUAL)
                            || (mech.getCockpitType() == Mech.COCKPIT_QUADVEE)) {
                        name = mech.getCrew().getCrewType().getRoleName(mech.getCrewForCockpitSlot(Mech.LOC_HEAD, cs));
                    }
                }
                return name;
            }
        } else {
            Mounted m = cs.getMount();
            StringBuffer critName = new StringBuffer(m.getType().getShortName());
            if (mech.isMixedTech()) {
                if (mech.isClan() && (m.getType().getTechBase() == ITechnology.TECH_BASE_IS)) {
                    critName.append(" [IS]");
                } else if (!mech.isClan() && m.getType().isClan()) {
                    critName.append(" [Clan]");
                }
            }

            if (UnitUtil.isTSM(m.getType())) {
                critName.setLength(0);
                critName.append("Triple-Strength Myomer");
            }

            if (m.isRearMounted()) {
                critName.append(" (R)");
            } else if (m.isMechTurretMounted()) {
                critName.append(" (T)");
            } else if ((m.getType() instanceof AmmoType) && (((AmmoType) m.getType()).getAmmoType() != AmmoType.T_COOLANT_POD)) {
                AmmoType ammo = (AmmoType) m.getType();

                critName = new StringBuffer("Ammo (");
                appendAmmoCritName(critName, ammo);
                int shots = m.getUsableShotsLeft();
                if ((cs.getMount2() != null) && (cs.getMount2().getType() instanceof AmmoType)) {
                    if (cs.getMount2().getType() == m.getType()) {
                        shots += cs.getMount2().getUsableShotsLeft();
                    } else {
                        critName.append(shots).append(", (");
                        appendAmmoCritName(critName, (AmmoType) cs.getMount2().getType());
                        shots = cs.getMount2().getBaseShotsLeft();
                    }
                }
                critName.append(") ").append(shots);
            } else if ((cs.getMount2() != null)
                    && (m.getType() instanceof MiscType) && (m.getType().hasFlag(MiscType.F_HEAT_SINK))
                    && (m.getType() == cs.getMount2().getType())) {
                critName.insert(0, "2 ").append("s");
            } else if ((cs.getMount2() != null)
                    && (m.getType() instanceof MiscType) && (m.getType().hasFlag(MiscType.F_COMPACT_HEAT_SINK))
                    && (cs.getMount2().getType() instanceof MiscType) && (cs.getMount2().getType().hasFlag(MiscType.F_COMPACT_HEAT_SINK))) {
                int hs = 2;
                if (m.getType().hasFlag(MiscType.F_DOUBLE_HEAT_SINK)) {
                    hs++;
                }
                if (cs.getMount2().getType().hasFlag(MiscType.F_DOUBLE_HEAT_SINK)) {
                    hs++;
                }
                critName.replace(0, 1, Integer.toString(hs));
            }
            if (!mech.isMixedTech()) {
                int startPos = critName.indexOf("[Clan]");
                if (startPos < 0) {
                    startPos = critName.indexOf("(Clan)");
                }
                if (startPos >= 0) {
                    critName.delete(startPos, startPos + "[Clan]".length());
                    critName.trimToSize();
                }
            }
            return critName.toString();
        }
    }
    
    private void appendAmmoCritName(StringBuffer buffer, AmmoType ammo) {
        // Remove Text (Clan) from the name
        buffer.append(ammo.getShortName().replace('(', '.').replace(')', '.').replaceAll(".Clan.", "").trim());
        // Remove any additional Ammo text.
        if (buffer.toString().endsWith("Ammo")) {
            buffer.setLength(buffer.length() - 5);
            buffer.trimToSize();
        }

        // Remove Capable with the name
        if (buffer.indexOf("-capable") > -1) {
            int startPos = buffer.indexOf("-capable");
            buffer.delete(startPos, startPos + "-capable".length());
            buffer.trimToSize();
        }

        // Trim trailing spaces.
        while (buffer.charAt(buffer.length() - 1) == ' ') {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.trimToSize();
    }

}
