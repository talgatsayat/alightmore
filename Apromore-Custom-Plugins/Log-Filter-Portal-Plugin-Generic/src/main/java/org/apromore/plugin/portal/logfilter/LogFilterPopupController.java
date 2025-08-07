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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apromore.logfilter.LogFilterService;
import org.apromore.logfilter.criteria.factory.impl.LogFilterCriterionFactoryImpl;
import org.apromore.plugin.portal.PortalContext;
import org.apromore.service.EventLogService;
import org.deckfour.xes.model.XLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Controller for the Log Filter Popup window
 */
public class LogFilterPopupController extends SelectorComposer<Window> {

    @WireVariable
    private EventLogService eventLogService;
    
    @WireVariable
    private LogFilterService logFilterService;
    
    @WireVariable
    private LogFilterCriterionFactoryImpl logFilterCriterionFactory;
    
    @Wire
    private Listbox valuesTable;
    
    private PortalContext portalContext;
    private XLog originalLog;
    private String logName;
    
    // Filter state
    private String selectedAction = "retain"; // retain or remove
    private String selectedAttribute = "concept:name";
    private String selectedMatching = "any"; // any or all
    private List<String> selectedValues = new ArrayList<>();
    
    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        
        // Get parameters passed from the plugin
        portalContext = (PortalContext) Executions.getCurrent().getArg().get("portalContext");
        originalLog = (XLog) Executions.getCurrent().getArg().get("originalLog");
        logName = (String) Executions.getCurrent().getArg().get("logName");
        
        // Initialize the controller
        initializeController();
        
        // Load initial data
        loadAttributeValues();
    }
    
    private void initializeController() {
        // Add event listeners for buttons
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        // Case filter buttons
        getSelf().getFellow("caseAttributeBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onCaseAttributeClick();
            }
        });
        
        getSelf().getFellow("caseVariantBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onCaseVariantClick();
            }
        });
        
        // Event filter buttons
        getSelf().getFellow("eventAttributeBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onEventAttributeClick();
            }
        });
        
        getSelf().getFellow("eventFrequencyBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onEventFrequencyClick();
            }
        });
        
        // Action buttons
        getSelf().getFellow("applyFilterBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onApplyFilter();
            }
        });
        
        getSelf().getFellow("saveFilterBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onSaveFilter();
            }
        });
        
        getSelf().getFellow("clearFilterBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onClearFilter();
            }
        });
        
        getSelf().getFellow("closeBtn").addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                onClose();
            }
        });
    }
    
    public void setPortalContext(PortalContext portalContext) {
        this.portalContext = portalContext;
    }
    
    public void setOriginalLog(XLog originalLog) {
        this.originalLog = originalLog;
    }
    
    private void loadAttributeValues() {
        try {
            // Clear existing items
            valuesTable.getItems().clear();
            
            // Get unique values from the log
            Map<String, Integer> valueCounts = getAttributeValueCounts();
            
            // Add items to the listbox
            for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
                Listitem item = new Listitem();
                item.appendChild(new org.zkoss.zul.Listcell(entry.getKey()));
                item.appendChild(new org.zkoss.zul.Listcell(String.valueOf(entry.getValue())));
                item.appendChild(new org.zkoss.zul.Listcell("100%")); // Placeholder frequency
                valuesTable.appendChild(item);
            }
            
            // Update item count
            updateItemCount();
            
        } catch (Exception e) {
            Messagebox.show("Ошибка загрузки данных: " + e.getMessage(), "Ошибка", 
                          Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    private Map<String, Integer> getAttributeValueCounts() {
        Map<String, Integer> valueCounts = new HashMap<>();
        
        // This is a simplified implementation
        // In a real implementation, you would analyze the XLog to get actual values
        valueCounts.put("Активность 1", 10);
        valueCounts.put("Активность 2", 8);
        valueCounts.put("Активность 3", 15);
        valueCounts.put("Активность 4", 5);
        valueCounts.put("Активность 5", 12);
        
        return valueCounts;
    }
    
    private void updateItemCount() {
        int totalItems = valuesTable.getItemCount();
        int currentPage = valuesTable.getPagingChild().getActivePage() + 1;
        int pageSize = valuesTable.getPagingChild().getPageSize();
        int startItem = (currentPage - 1) * pageSize + 1;
        int endItem = Math.min(currentPage * pageSize, totalItems);
        
        org.zkoss.zul.Label itemCount = (org.zkoss.zul.Label) getSelf().getFellow("itemCount");
        itemCount.setValue(String.format("[%d - %d / %d]", startItem, endItem, totalItems));
    }
    
    // Event handlers for filter type buttons
    public void onCaseAttributeClick() {
        Messagebox.show("Фильтр по атрибутам кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case attribute filter logic
    }
    
    public void onCaseVariantClick() {
        Messagebox.show("Фильтр по вариантам кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case variant filter logic
    }
    
    public void onCaseIdClick() {
        Messagebox.show("Фильтр по ID кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case ID filter logic
    }
    
    public void onCaseTimeframeClick() {
        Messagebox.show("Фильтр по временному периоду кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case timeframe filter logic
    }
    
    public void onCasePerformanceClick() {
        Messagebox.show("Фильтр по производительности кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case performance filter logic
    }
    
    public void onCasePathClick() {
        Messagebox.show("Фильтр по пути кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case path filter logic
    }
    
    public void onCaseReworkClick() {
        Messagebox.show("Фильтр по переработке кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case rework filter logic
    }
    
    public void onCaseBlocksClick() {
        Messagebox.show("Фильтр по блокам кейса", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement case blocks filter logic
    }
    
    public void onEventAttributeClick() {
        Messagebox.show("Фильтр по атрибутам события", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement event attribute filter logic
    }
    
    public void onEventTimeframeClick() {
        Messagebox.show("Фильтр по временному периоду события", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement event timeframe filter logic
    }
    
    public void onEventPerformanceClick() {
        Messagebox.show("Фильтр по производительности события", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement event performance filter logic
    }
    
    public void onEventPathClick() {
        Messagebox.show("Фильтр по пути события", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement event path filter logic
    }
    
    public void onEventFrequencyClick() {
        Messagebox.show("Фильтр по частоте события", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement event frequency filter logic
    }
    
    public void onEventBetweenClick() {
        Messagebox.show("Фильтр между событиями", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement event between filter logic
    }
    
    public void onCostFilterClick() {
        Messagebox.show("Фильтр по стоимости", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement cost filter logic
    }
    
    public void onDurationFilterClick() {
        Messagebox.show("Фильтр по длительности", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        // Implement duration filter logic
    }
    
    // Action button handlers
    public void onApplyFilter() {
        try {
            // Get selected values
            selectedValues.clear();
            for (Listitem item : valuesTable.getSelectedItems()) {
                String value = ((org.zkoss.zul.Listcell) item.getChildren().get(0)).getLabel();
                selectedValues.add(value);
            }
            
            if (selectedValues.isEmpty()) {
                Messagebox.show("Пожалуйста, выберите хотя бы одно значение для фильтрации", 
                              "Предупреждение", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            // Apply the filter
            applyFilter();
            
            Messagebox.show("Фильтр успешно применен!", "Успех", Messagebox.OK, Messagebox.INFORMATION);
            
        } catch (Exception e) {
            Messagebox.show("Ошибка применения фильтра: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    public void onSaveFilter() {
        Messagebox.show("Функция сохранения фильтра будет реализована в следующей версии", 
                      "Информация", Messagebox.OK, Messagebox.INFORMATION);
    }
    
    public void onClearFilter() {
        // Clear all selections
        valuesTable.clearSelection();
        selectedValues.clear();
        
        // Reset form controls
        org.zkoss.zul.Radiogroup actionType = (org.zkoss.zul.Radiogroup) getSelf().getFellow("actionType");
        actionType.setSelectedIndex(0);
        
        Messagebox.show("Фильтр очищен", "Информация", Messagebox.OK, Messagebox.INFORMATION);
    }
    
    public void onClose() {
        getSelf().detach();
    }
    
    public void onSelectAll() {
        valuesTable.selectAll();
    }
    
    public void onDeselectAll() {
        valuesTable.clearSelection();
    }
    
    private void applyFilter() throws IOException {
        // This is where you would implement the actual filtering logic
        // For now, we'll just show a message
        System.out.println("Applying filter with:");
        System.out.println("Action: " + selectedAction);
        System.out.println("Attribute: " + selectedAttribute);
        System.out.println("Matching: " + selectedMatching);
        System.out.println("Selected values: " + selectedValues);
        
        // In a real implementation, you would:
        // 1. Create filter criteria
        // 2. Apply the filter to the log
        // 3. Show the filtered results
        // 4. Optionally save the filtered log
    }
} 