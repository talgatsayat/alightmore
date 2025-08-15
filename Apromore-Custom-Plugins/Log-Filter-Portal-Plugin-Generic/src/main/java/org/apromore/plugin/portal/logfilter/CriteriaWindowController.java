/*-
 * #%L
 * This file is part of "Apromore Core".
 * %%
 * Copyright (C) 2018 - 2022 Apromore Pty Ltd.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

 package org.apromore.plugin.portal.logfilter;

 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.Desktop;
 import org.zkoss.zk.ui.Page;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.select.SelectorComposer;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.select.annotation.Wire;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.Window;
 import org.zkoss.zul.Label;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 // Add the missing import for ActivityInstance
 import org.apromore.apmlog.logobjects.ActivityInstance;
 
 public class CriteriaWindowController extends SelectorComposer<Window> {
 
     @Wire private Window criteriaWindow;
     @Wire private Button criteriaAddButton;
     @Wire private Button filterOkButton;
     @Wire private Button filterCancelButton;
     @Wire private Listbox criteriaList;
     
     // Log statistics components
     @Wire private Label criteriaCasePercent;
     @Wire private Label criteriaCaseFiltered;
     @Wire private Label criteriaCaseTotal;
     @Wire private Label criteriaVariantPercent;
     @Wire private Label criteriaVariantFiltered;
     @Wire private Label criteriaVariantTotal;
     @Wire private Label criteriaEventPercent;
     @Wire private Label criteriaEventFiltered;
     @Wire private Label criteriaEventTotal;
     @Wire private Label criteriaNodePercent;
     @Wire private Label criteriaNodeFiltered;
     @Wire private Label criteriaNodeTotal;
 
     private List<String> criteriaDescriptions = new ArrayList<>();
     private List<org.apromore.apmlog.filter.rules.LogFilterRule> criteriaRules = new ArrayList<>();
     // Persist original context to reopen filter window properly
     private Map<?, ?> originalArgs;
 
     @Override
     public void doAfterCompose(Window comp) throws Exception {
         super.doAfterCompose(comp);
         // Defensive: ensure any lingering filter windows are closed the moment Criteria opens
         try {
             Desktop desktop = Executions.getCurrent().getDesktop();
             if (desktop != null) {
                 for (Page page : desktop.getPages()) {
                     Component old = page.getFellowIfAny("logFilterWindow");
                     if (old != null) {
                         try { old.detach(); } catch (Exception ignored) {}
                     }
                 }
             }
         } catch (Exception ignored) {}
 
         Map<?, ?> args = Executions.getCurrent().getArg();
         originalArgs = args; // keep all args to pass back when reopening filter window
         Object passed = args != null ? args.get("criteriaDescriptions") : null;
         if (passed instanceof List) {
             //noinspection unchecked
             criteriaDescriptions = new ArrayList<>((List<String>) passed);
         }
         Object rulesPassed = args != null ? args.get("criteriaRules") : null;
         if (rulesPassed instanceof List) {
             //noinspection unchecked
             criteriaRules = new ArrayList<>((List<org.apromore.apmlog.filter.rules.LogFilterRule>) rulesPassed);
         }
         refreshCriteriaList();
         updateLogStatistics();
     }
 
     private void refreshCriteriaList() {
         if (criteriaList == null) return;
         criteriaList.getItems().clear();
         for (String desc : criteriaDescriptions) {
             Listitem item = new Listitem(desc);
             item.setParent(criteriaList);
         }
     }
     
     private void updateLogStatistics() {
         try {
             Map<?, ?> args = originalArgs;
             Object apmObj = args != null ? args.get("apmLog") : null;
             
             if (!(apmObj instanceof org.apromore.apmlog.APMLog)) {
                 return;
             }
             
             org.apromore.apmlog.APMLog apmLog = (org.apromore.apmlog.APMLog) apmObj;
             
             // Get original log statistics
             long totalCaseCount = apmLog.getTraces().size();
             long totalEventCount = apmLog.getActivityInstances().size();
             
             // Calculate total variants (unique case paths)
             java.util.Set<String> totalVariants = new java.util.HashSet<>();
             for (org.apromore.apmlog.ATrace trace : apmLog.getTraces()) {
                 StringBuilder variant = new StringBuilder();
                 for (ActivityInstance ai : trace.getActivityInstances()) {
                     if (variant.length() > 0) variant.append("->");
                     variant.append(ai.getName());
                 }
                 totalVariants.add(variant.toString());
             }
             long totalVariantCount = totalVariants.size();
             
             // Calculate total activities (unique activities)
             java.util.Set<String> totalActivities = new java.util.HashSet<>();
             for (ActivityInstance ai : apmLog.getActivityInstances()) {
                 totalActivities.add(ai.getName());
             }
             long totalNodeCount = totalActivities.size();
             
             // Get filtered statistics if criteria exist
             long filteredCaseCount = totalCaseCount;
             long filteredEventCount = totalEventCount;
             long filteredVariantCount = totalVariantCount;
             long filteredNodeCount = totalNodeCount;
             
             if (!criteriaRules.isEmpty()) {
                 try {
                     org.apromore.apmlog.filter.APMLogFilter filter = new org.apromore.apmlog.filter.APMLogFilter(apmLog);
                     filter.filter(criteriaRules);
                     org.apromore.apmlog.filter.PLog pLog = filter.getPLog();
                     
                     if (pLog != null) {
                         filteredCaseCount = pLog.getValidTraceIndexBS().cardinality();
                         
                         // Calculate filtered events
                         filteredEventCount = 0;
                         for (org.apromore.apmlog.filter.PTrace pTrace : pLog.getPTraces()) {
                             filteredEventCount += pTrace.getValidEventIndexBS().cardinality();
                         }
                         
                         // Calculate filtered variants
                         java.util.Set<String> filteredVariants = new java.util.HashSet<>();
                         for (org.apromore.apmlog.filter.PTrace pTrace : pLog.getPTraces()) {
                             StringBuilder variant = new StringBuilder();
                             for (int i = 0; i < pTrace.getActivityInstances().size(); i++) {
                                 if (pTrace.getValidEventIndexBS().get(i)) {
                                     if (variant.length() > 0) variant.append("->");
                                     variant.append(pTrace.getActivityInstances().get(i).getName());
                                 }
                             }
                             if (variant.length() > 0) {
                                 filteredVariants.add(variant.toString());
                             }
                         }
                         filteredVariantCount = filteredVariants.size();
                         
                         // Calculate filtered activities
                         java.util.Set<String> filteredActivities = new java.util.HashSet<>();
                         for (org.apromore.apmlog.filter.PTrace pTrace : pLog.getPTraces()) {
                             for (int i = 0; i < pTrace.getActivityInstances().size(); i++) {
                                 if (pTrace.getValidEventIndexBS().get(i)) {
                                     filteredActivities.add(pTrace.getActivityInstances().get(i).getName());
                                 }
                             }
                         }
                         filteredNodeCount = filteredActivities.size();
                     }
                 } catch (Exception e) {
                     // If filtering fails, use original counts
                     filteredCaseCount = totalCaseCount;
                     filteredEventCount = totalEventCount;
                     filteredVariantCount = totalVariantCount;
                     filteredNodeCount = totalNodeCount;
                 }
             }
             
             // Update UI
             updateStatistic(criteriaCaseFiltered, criteriaCaseTotal, criteriaCasePercent, filteredCaseCount, totalCaseCount);
             updateStatistic(criteriaVariantFiltered, criteriaVariantTotal, criteriaVariantPercent, filteredVariantCount, totalVariantCount);
             updateStatistic(criteriaEventFiltered, criteriaEventTotal, criteriaEventPercent, filteredEventCount, totalEventCount);
             updateStatistic(criteriaNodeFiltered, criteriaNodeTotal, criteriaNodePercent, filteredNodeCount, totalNodeCount);
             
         } catch (Exception e) {
             // If anything fails, set default values
             setDefaultStatistics();
         }
     }
     
     private void updateStatistic(Label filteredLabel, Label totalLabel, Label percentLabel, long filtered, long total) {
         if (filteredLabel != null) {
             filteredLabel.setValue(toShortString(filtered));
         }
         if (totalLabel != null) {
             totalLabel.setValue(toShortString(total));
         }
         if (percentLabel != null) {
             double percentage = total > 0 ? (double) filtered / total * 100 : 100.0;
             DecimalFormat df = new DecimalFormat("###############.#");
             String percentStr = df.format(percentage) + "%";
             if (percentStr.equals("0%") && percentage > 0) {
                 percentStr = "~0%";
             }
             percentLabel.setValue(percentStr);
         }
     }
     
     private void setDefaultStatistics() {
         if (criteriaCaseFiltered != null) criteriaCaseFiltered.setValue("0");
         if (criteriaCaseTotal != null) criteriaCaseTotal.setValue("0");
         if (criteriaCasePercent != null) criteriaCasePercent.setValue("100%");
         
         if (criteriaVariantFiltered != null) criteriaVariantFiltered.setValue("0");
         if (criteriaVariantTotal != null) criteriaVariantTotal.setValue("0");
         if (criteriaVariantPercent != null) criteriaVariantPercent.setValue("100%");
         
         if (criteriaEventFiltered != null) criteriaEventFiltered.setValue("0");
         if (criteriaEventTotal != null) criteriaEventTotal.setValue("0");
         if (criteriaEventPercent != null) criteriaEventPercent.setValue("100%");
         
         if (criteriaNodeFiltered != null) criteriaNodeFiltered.setValue("0");
         if (criteriaNodeTotal != null) criteriaNodeTotal.setValue("0");
         if (criteriaNodePercent != null) criteriaNodePercent.setValue("100%");
     }
     
     private String toShortString(long longNumber) {
         DecimalFormat df1 = new DecimalFormat("###############.#");
         String numberString = "";
         if (longNumber > 1000000) {
             numberString = "" + df1.format((double) longNumber / 1000000) + "M";
         } else if (longNumber > 1000) {
             numberString = "" + df1.format((double)longNumber / 1000) + "K";
         } else {
             numberString = longNumber + "";
         }
         return numberString;
     }
 
     @Listen("onClick = #criteriaAddButton")
     public void onAddClick() {
         // Close criteria window and reopen the filter window for adding more filters
         if (criteriaWindow != null) {
             criteriaWindow.detach();
         }
         // Ensure any existing 'logFilterWindow' is removed to avoid ID-space conflicts
         try {
             Desktop desktop = Executions.getCurrent().getDesktop();
             if (desktop != null) {
                 for (Page page : desktop.getPages()) {
                     Component old = page.getFellowIfAny("logFilterWindow");
                     if (old != null) {
                         old.detach();
                     }
                 }
             }
         } catch (Exception ignored) {}
         // Load from classpath to avoid context-relative path issues, pass original arguments and open modally
         try {
             java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("logFilterWindow.zul");
             java.util.Map<String, Object> forwardedArgs = new java.util.HashMap<>();
             if (originalArgs != null) {
                 for (Map.Entry<?, ?> ent : originalArgs.entrySet()) {
                     Object k = ent.getKey();
                     Object v = ent.getValue();
                     if (k instanceof String) {
                         forwardedArgs.put((String) k, v);
                     }
                 }
             }
             // Pass the filtered intermediate log built from current criteria
             try {
                 if (forwardedArgs.containsKey("apmLog") && !criteriaRules.isEmpty()) {
                     org.apromore.apmlog.APMLog base = (org.apromore.apmlog.APMLog) forwardedArgs.get("apmLog");
                     org.apromore.apmlog.filter.APMLogFilter f = new org.apromore.apmlog.filter.APMLogFilter(base);
                     f.filter(criteriaRules);
                     org.apromore.apmlog.APMLog interim = f.getAPMLog();
                     forwardedArgs.put("apmLog", interim);
                 }
             } catch (Exception ignore) {}
             // forward accumulated criteria and descriptions so next window can merge
             forwardedArgs.put("criteriaRules", new java.util.ArrayList<>(criteriaRules));
             forwardedArgs.put("criteriaDescriptions", new java.util.ArrayList<>(criteriaDescriptions));
 
             org.zkoss.zul.Window w = (org.zkoss.zul.Window) Executions.createComponentsDirectly(
                     new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8),
                     "zul", null, forwardedArgs);
             if (w != null) {
                 w.doModal();
             }
         } catch (java.io.IOException e) {
             // Fallback to relative load
             java.util.Map<String, Object> forwardedArgs = new java.util.HashMap<>();
             if (originalArgs != null) {
                 for (Map.Entry<?, ?> ent : originalArgs.entrySet()) {
                     Object k = ent.getKey();
                     Object v = ent.getValue();
                     if (k instanceof String) {
                         forwardedArgs.put((String) k, v);
                     }
                 }
             }
             if (forwardedArgs.containsKey("apmLog") && !criteriaRules.isEmpty()) {
                 try {
                     org.apromore.apmlog.APMLog base = (org.apromore.apmlog.APMLog) forwardedArgs.get("apmLog");
                     org.apromore.apmlog.filter.APMLogFilter f = new org.apromore.apmlog.filter.APMLogFilter(base);
                     f.filter(criteriaRules);
                     forwardedArgs.put("apmLog", f.getAPMLog());
                 } catch (Exception ignored) {}
             }
             forwardedArgs.put("criteriaRules", new java.util.ArrayList<>(criteriaRules));
             forwardedArgs.put("criteriaDescriptions", new java.util.ArrayList<>(criteriaDescriptions));
             org.zkoss.zul.Window w = (org.zkoss.zul.Window) Executions.createComponents("logFilterWindow.zul", null, forwardedArgs);
             if (w != null) {
                 w.doModal();
             }
         }
     }
 
     @Listen("onClick = #filterOkButton")
     public void onOk() {
         // Apply all accumulated criteria rules to Process Discoverer now
         Map<?, ?> args = originalArgs;
         Object clientObj = args != null ? args.get("logFilterClient") : null;
         Object apmObj = args != null ? args.get("apmLog") : null;
         Object logIdObj = args != null ? args.get("logId") : null;
         Object logNameObj = args != null ? args.get("logName") : null;
 
         if (!(clientObj instanceof org.apromore.plugin.portal.logfilter.generic.LogFilterClient) ||
             !(apmObj instanceof org.apromore.apmlog.APMLog)) {
             Clients.showNotification("Cannot apply: missing context", Clients.NOTIFICATION_TYPE_ERROR, null, null, 2500);
             return;
         }
 
         org.apromore.plugin.portal.logfilter.generic.LogFilterClient client =
                 (org.apromore.plugin.portal.logfilter.generic.LogFilterClient) clientObj;
         org.apromore.apmlog.APMLog apmLog = (org.apromore.apmlog.APMLog) apmObj;
         Integer logId = (logIdObj instanceof Integer) ? (Integer) logIdObj : null;
         String logName = (logNameObj instanceof String) ? (String) logNameObj : null;
 
         try {
             // Let PD recompute PLog/bitset to avoid mismatches; pass only rules and base log
             org.apromore.plugin.portal.logfilter.generic.LogFilterResponse response =
                     new org.apromore.plugin.portal.logfilter.generic.LogFilterResponse(
                             logId, logName, apmLog, null, criteriaRules, new java.util.HashMap<>());
             client.processResponse(response);
             Clients.showNotification("Filters applied", Clients.NOTIFICATION_TYPE_INFO, null, null, 2000);
         } catch (Exception e) {
             Clients.showNotification("Apply failed: " + e.getMessage(), Clients.NOTIFICATION_TYPE_ERROR, null, null, 3000);
         } finally {
             if (criteriaWindow != null) criteriaWindow.detach();
         }
     }
 
     @Listen("onClick = #filterCancelButton")
     public void onCancel() {
         if (criteriaWindow != null) criteriaWindow.detach();
     }
 }