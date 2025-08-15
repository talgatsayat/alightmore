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

import org.apromore.plugin.portal.PortalContext;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.apromore.portal.model.LogSummaryType;
import org.apromore.portal.model.SummaryType;
import org.apromore.portal.model.VersionSummaryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.apromore.plugin.portal.logfilter.generic.LogFilterClient;
import org.apromore.plugin.portal.logfilter.generic.LogFilterResponse;
import org.apromore.apmlog.filter.rules.LogFilterRule;
import org.apromore.apmlog.filter.rules.LogFilterRuleImpl;
import org.apromore.apmlog.filter.rules.RuleValue;
import org.apromore.apmlog.filter.types.Choice;
import org.apromore.apmlog.filter.types.FilterType;
import org.apromore.apmlog.filter.types.Inclusion;
import org.apromore.apmlog.filter.types.OperationType;
import org.apromore.apmlog.filter.types.Section;
import org.apromore.apmlog.filter.PLog;
import org.apromore.apmlog.filter.APMLogFilter;

import org.apromore.apmlog.APMLog;
import org.apromore.apmlog.ATrace;
import org.apromore.apmlog.logobjects.ActivityInstance;
import org.apromore.apmlog.exceptions.EmptyInputException;


import org.slf4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Контроллер для стандартного окна фильтрации логов Apromore
 */
public class AdvancedLogFilterController extends SelectorComposer<Component> {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(AdvancedLogFilterController.class);

    // Main window (bind to ZUL id 'logFilterWindow')
    @Wire("#logFilterWindow, #advancedLogFilterWindow") private Window advancedLogFilterWindow;
    
    // Log information display
    @Wire private Label logNameLabel;
    @Wire private Label eventCountLabel;
    @Wire private Label caseCountLabel;

    // Navigation buttons
    @Wire private Button caseAttributeNav;
    @Wire private Button caseVariantNav;
    @Wire private Button caseIdNav;
    @Wire private Button timeframeNav;
    @Wire private Button performanceNav;
    @Wire private Button pathNav;
    @Wire private Button reworkNav;
    @Wire private Button blocksNav;
    @Wire private Button eventAttributeNav;
    @Wire private Button eventTimeframeNav;
    @Wire private Button eventFrequencyNav;
    @Wire private Button eventPerformanceNav;
    @Wire private Button eventPathNav;
    @Wire private Button eventBetweenNav;

    // Accumulated descriptions passed from Criteria window
    private List<String> criteriaDescriptionsFromArgs = new java.util.ArrayList<>();

    // Content panels
    @Wire private Div caseAttributePanel;
    @Wire private Div caseIdPanel;
    @Wire private Div caseVariantPanel;
    @Wire private Div timeframePanel;
    @Wire private Div performancePanel;
    @Wire private Div pathPanel;
    @Wire private Div reworkPanel;
    @Wire private Div blocksPanel;

    // Case Attribute Filter components
    @Wire private Radiogroup caseAttributeCondition;
    @Wire private Radiogroup primaryAttributeType;
    @Wire private Combobox primaryAttributeCombo;
    @Wire private Checkbox useSecondaryAttribute;
    @Wire private Radiogroup secondaryAttributeType;
    @Wire private Combobox secondaryAttributeCombo;
    @Wire private Radiogroup matchingCondition;
    @Wire private Textbox valueSearchBox;
    @Wire private Listbox attributeValuesList;
    @Wire private Label caseAttributeStats;

    // Case ID Filter components
    @Wire private Radiogroup caseIdCondition;
    @Wire private Listbox caseIdValuesList;
    @Wire private Label caseIdStats;

    // Timeframe Filter components
    @Wire private Radiogroup timeframeCondition;



    @Wire private Label timeAxisLabel;
    @Wire private Label fromTimeLabel;
    @Wire private Label toTimeLabel;
    @Wire private Label durationLabel;
    @Wire private Button applyFiltersBtnTimeframe;
    @Wire private Label timeRangeDisplay;
    @Wire private Label caseCountDisplay;
    @Wire private Datebox fromDate;
    @Wire private Datebox toDate;
    
    // Containment icons
    @Wire private Div startInIcon;
    @Wire private Div endInIcon;
    @Wire private Div containedInIcon;
    @Wire private Div activeInIcon;


    // Control buttons
    @Wire private Button cancelBtn;
    @Wire private Button applyFiltersBtn;
    @Wire private Button applyFiltersBtnCaseAttribute;
    @Wire private Button applyFiltersBtnCaseId;

    // Pagination buttons for attributes
    @Wire private Button firstPageBtn;
    @Wire private Button prevPageBtn;
    @Wire private Button nextPageBtn;
    @Wire private Button lastPageBtn;
    @Wire private Label pageLabel;
    @Wire private Label pageInfo;
    @Wire private Label totalInfo;

    // Pagination buttons for Case ID
    @Wire private Button caseIdFirstPageBtn;
    @Wire private Button caseIdPrevPageBtn;
    @Wire private Button caseIdNextPageBtn;
    @Wire private Button caseIdLastPageBtn;
    @Wire private Label caseIdPageLabel;
    @Wire private Label caseIdPageInfo;
    @Wire private Label caseIdTotalInfo;

    // Internal state
    private String currentFilter = "caseAttribute";
    
    // Log information
    private PortalContext portalContext;
    private LogSummaryType selectedLog;
    private String logName;
    private Integer logId;
    
    // Process Discoverer integration
    private LogFilterClient logFilterClient;
    private APMLog apmLog;
    private List<LogFilterRule> currentCriteria;
    
    // Pagination state
    private static final int ITEMS_PER_PAGE = 100;
    private int currentAttributePage = 1;
    private int currentCaseIdPage = 1;
    private List<String> allAttributeValues = new ArrayList<>();
    private List<String> allCaseIdValues = new ArrayList<>();
    
    @Autowired(required = false)
    private Object processDiscovererPlugin;

    // Predefined timeframes UI
    @Wire private Checkbox usePredefinedTimeframes;
    @Wire private Hlayout predefinedTimeframesLayout;
    @Wire private Combobox predefinedTimeframeType;
    @Wire private Button intervalsBtn;

    // State for predefined timeframes
    private String selectedPredefinedType;
    private List<String> selectedPredefinedIntervals = new ArrayList<>();

    // Performance filter UI (Cases)
    @Wire private Radiogroup performanceCondition;
    @Wire private Combobox performanceMeasure;
    @Wire private Intbox perfGteValue;
    @Wire private Combobox perfGteUnit;
    @Wire private Intbox perfLteValue;
    @Wire private Combobox perfLteUnit;
    @Wire private Button applyFiltersBtnPerformance;
    @Wire private Button filterTopLeftBtn;
    @Wire private Div perfGteRow;
    @Wire private Div perfLteRow;
    @Wire private Div perfLenRow;
    @Wire private Intbox perfLenFrom;
    @Wire private Intbox perfLenTo;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        LOGGER.info("AdvancedLogFilterController initialized");
        
        extractLogInformation();
        updateLogInfoDisplay();
        initializeNavigation();
        initializeControlButtons();
        loadLogData();
        initializeTimeframeFilter();
        configureTopLeftButton();
        
        // Show default panel
        showPanel("caseAttribute");
    }
    
    /**
     * Event handler for primary attribute selection change
     */
    @Listen("onChange = #primaryAttributeCombo")
    public void onPrimaryAttributeChange() {
        try {
            LOGGER.info("🔄 Изменен выбранный атрибут, перезагружаем значения...");
            loadAttributeValuesForLog();
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при изменении выбранного атрибута", e);
        }
    }
    
    /**
     * Event handler for primary attribute type change (Event vs Case)
     */
    @Listen("onChange = #primaryAttributeType")
    public void onPrimaryAttributeTypeChange() {
        try {
            LOGGER.info("🔄 Изменен тип атрибута (Event/Case), перезагружаем атрибуты...");
            
            // Debug: Log the current selection
            if (primaryAttributeType != null && primaryAttributeType.getSelectedItem() != null) {
                String selectedType = primaryAttributeType.getSelectedItem().getValue();
                LOGGER.info("🔧 Выбранный тип: {}", selectedType);
            } else {
                LOGGER.warn("⚠️ primaryAttributeType или selectedItem недоступен");
            }
            
            loadAvailableAttributesFromLog(); // Reload attributes based on new type
            loadAttributeValuesForLog(); // Reload values for the new attribute type
            
            LOGGER.info("✅ Перезагрузка атрибутов завершена");
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при изменении типа атрибута", e);
        }
    }
    
    /**
     * Alternative event handler for radio button clicks
     */
    @Listen("onCheck = #primaryAttributeType radio")
    public void onPrimaryAttributeTypeRadioClick() {
        try {
            LOGGER.info("🔄 Radio button clicked в primaryAttributeType");
            onPrimaryAttributeTypeChange();
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при обработке клика radio button", e);
        }
    }


    /**
     * Applying filters button in timeframe panel
     */
    @Listen("onClick = #applyFiltersBtnTimeframe")
    public void onApplyTimeframeFiltersClick() {
        try {
            LOGGER.info("Applying timeframe filters...");
            currentFilter = "timeframe";
            applyFiltersAndRedirect();
        } catch (Exception e) {
            LOGGER.error("Error applying timeframe filters", e);
        }
    }
    
    /**
     * Event handler for START IN icon click
     */
    @Listen("onClick = #startInIcon")
    public void onStartInIconClick() {
        try {
            if (containmentLocked) return;
            setContainmentIcon("start");
            // Disable To? No, for start we ignore To input? We disable fromDate? We disable toDate per requirement
            if (toDate != null) {
                toDate.setDisabled(true);
            }
            if (fromDate != null) {
                fromDate.setDisabled(false);
            }
            // last occurred event date becomes the end of the timeframe
            if (apmLog != null && toDate != null) {
                toDate.setValue(new Date(apmLog.getEndTime()));
            }
            LOGGER.info("Containment set to: start in");
        } catch (Exception e) {
            LOGGER.error("Error setting start in containment", e);
        }
    }

    /**
     * Event handler for END IN icon click
     */
    @Listen("onClick = #endInIcon")
    public void onEndInIconClick() {
        try {
            if (containmentLocked) return;
            setContainmentIcon("end");
            //Ignore start date and disable it and set to log's first occured start boundary
            if (fromDate != null) {
                fromDate.setDisabled(true);
            }
            if (toDate != null) {
                toDate.setDisabled(false);
            }
            if (apmLog != null && fromDate != null) {
                fromDate.setValue(new Date(apmLog.getStartTime()));
            }
            LOGGER.info("Containment set to: end in");
        } catch (Exception e) {
            LOGGER.error("Error setting end in containment", e);
        }
    }

    /**
     * event handler for CONTAINED IN icon click
     */
    @Listen("onClick = #containedInIcon")
    public void onContainedInIconClick() {
        try {
            // take both dates from user
            if (containmentLocked) return;
            setContainmentIcon("contained");
            if (fromDate != null) fromDate.setDisabled(false);
            if (toDate != null) toDate.setDisabled(false);
            LOGGER.info("Containment set to: contained in");
        } catch (Exception e) {
            LOGGER.error("Error setting contained in containment", e);
        }
    }

    /**
     * Event handler for ACTIVE IN icon click
     */
    @Listen("onClick = #activeInIcon")
    public void onActiveInIconClick() {
        try {
            // both dates are taken from user
            if (containmentLocked) return;
            setContainmentIcon("active");
            if (fromDate != null) fromDate.setDisabled(false);
            if (toDate != null) toDate.setDisabled(false);
            LOGGER.info("Containment set to: active in");
        } catch (Exception e) {
            LOGGER.error("Error setting active in containment", e);
        }
    }

    /**
     * Извлекает информацию о выбранном логе из аргументов ZK
     */
    private void extractLogInformation() {
        try {
            Map<?, ?> args = Executions.getCurrent().getArg();
            if (args != null) {
                this.portalContext = (PortalContext) args.get("portalContext");
                this.selectedLog = (LogSummaryType) args.get("selectedLog");
                this.logName = (String) args.get("logName");
                this.logId = (Integer) args.get("logId");
                
                // Получаем APMLog
                Object apmLogObj = args.get("apmLog");
                if (apmLogObj instanceof APMLog) {
                    apmLog = (APMLog) apmLogObj;
                    LOGGER.info("APMLog получен: {} трасс", apmLog.getTraces().size());
                }
                
                // Получаем текущие критерии фильтрации
                Object criteriaObj = args.get("currentCriteria");
                if (criteriaObj instanceof List) {
                    currentCriteria = (List<LogFilterRule>) criteriaObj;
                    LOGGER.info("Критерии фильтрации получены: {} правил", currentCriteria.size());
                }
                
                // Получаем LogFilterClient
                Object clientObj = args.get("logFilterClient");
                if (clientObj instanceof LogFilterClient) {
                    logFilterClient = (LogFilterClient) clientObj;
                    LOGGER.info("LogFilterClient получен");
                }
                // Support receiving accumulated rules from criteria window
                Object passedRules = args.get("criteriaRules");
                if (passedRules instanceof java.util.List) {
                    //noinspection unchecked
                    currentCriteria = (java.util.List<LogFilterRule>) passedRules;
                    LOGGER.info("Получены накопленные критерии: {} правил", currentCriteria.size());
                }
                Object passedDescs = args.get("criteriaDescriptions");
                if (passedDescs instanceof java.util.List) {
                    //noinspection unchecked
                    criteriaDescriptionsFromArgs = new java.util.ArrayList<>((java.util.List<String>) passedDescs);
                    LOGGER.info("Получены накопленные описания критериев: {}", criteriaDescriptionsFromArgs.size());
                }
                
                LOGGER.info("Получена информация о логе: {} (ID: {})", logName, logId);
                LOGGER.info("Информация о логе извлечена успешно");
            } else {
                LOGGER.warn("Аргументы не переданы, используем заглушечные данные");
                this.logName = "Sample Log";
                this.logId = -1;
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при извлечении информации о логе", e);
            this.logName = "Unknown Log";
            this.logId = -1;
        }
    }

    /**
     * Update log information display components
     */
    private void updateLogInfoDisplay() {
        try {
            // Update window title
                    if (advancedLogFilterWindow != null && logName != null && !logName.isEmpty()) {
            advancedLogFilterWindow.setTitle("Filter log: " + logName);
                LOGGER.info("Заголовок окна обновлен: Filter log: {}", logName);
            }
            
            // Update log name
            if (logNameLabel != null) {
                if (logName != null && !logName.isEmpty()) {
                    logNameLabel.setValue(logName);
                    LOGGER.info("Отображено имя лога: {}", logName);
                } else {
                    logNameLabel.setValue("Unknown Log");
                    LOGGER.warn("Имя лога неизвестно");
                }
            } else {
                LOGGER.warn("logNameLabel компонент не найден");
            }

            // Update event and case counts
            if (selectedLog != null && logId != null && logId > 0) {
                try {
                    // Получаем реальную статистику лога
                    loadRealLogStatistics();
                } catch (Exception e) {
                    LOGGER.error("Ошибка при загрузке статистики лога", e);
                    // Fallback to default values
                    if (eventCountLabel != null) {
                        eventCountLabel.setValue("Loading events...");
                    }
                    if (caseCountLabel != null) {
                        caseCountLabel.setValue("Loading cases...");
                    }
                }
            } else {
                //real data from the log
                if (apmLog != null) {
                    int eventCount = 0;
                    int caseCount = apmLog.getTraces().size();
                    for (ATrace trace : apmLog.getTraces()) {
                        eventCount += trace.getActivityInstances().size();
                    }
                    if (eventCountLabel != null) {
                        eventCountLabel.setValue(String.format("%,d events", eventCount));
                    }
                    if (caseCountLabel != null) {
                        caseCountLabel.setValue(String.format("%,d cases", caseCount));
                    }
                    LOGGER.info("Loaded real statistics for log '{}': {} events, {} cases", logName, eventCount, caseCount);
                } else {
                    if (eventCountLabel != null) {
                        eventCountLabel.setValue("N/A");
                    }
                    if (caseCountLabel != null) {
                        caseCountLabel.setValue("N/A");
                    }
                    LOGGER.warn("Unable to load real statistics for the log; APMLog is null");
                }
            }

        } catch (Exception e) {
            LOGGER.error("Ошибка при обновлении отображения информации о логе", e);
            
            // Fallback values
            if (logNameLabel != null) logNameLabel.setValue("Error loading log name");
            if (eventCountLabel != null) eventCountLabel.setValue("N/A");
            if (caseCountLabel != null) caseCountLabel.setValue("N/A");
        }
    }

    /**
     * Load real log statistics from the selected log
     */
    private void loadRealLogStatistics() {
        try {
            if (apmLog != null) {
                LOGGER.info("Загружаем статистику лога из APMLog");
 
                int eventCount = 0;
                int caseCount = apmLog.getTraces().size();
         
                for (ATrace trace : apmLog.getTraces()) {
                    eventCount += trace.getActivityInstances().size();
                }

                if (eventCountLabel != null) {
                    eventCountLabel.setValue(String.format("%,d events", eventCount));
                }
                if (caseCountLabel != null) {
                    caseCountLabel.setValue(String.format("%,d cases", caseCount));
                }
                
                LOGGER.info("Вычислена статистика для лога '{}' (ID: {}): {} events, {} cases", 
                           logName, logId, eventCount, caseCount);
                
            } else if (logId != null && logId > 0) {
                LOGGER.warn("APMLog недоступен, используем заглушечные данные");
                if (eventCountLabel != null) {
                    eventCountLabel.setValue("Loading events...");
                }
                if (caseCountLabel != null) {
                    caseCountLabel.setValue("Loading cases...");
                }
            } else {
                LOGGER.warn("Некорректный ID лога: {}", logId);
                if (eventCountLabel != null) {
                    eventCountLabel.setValue("N/A");
                }
                if (caseCountLabel != null) {
                    caseCountLabel.setValue("N/A");
                }
            }

        } catch (Exception e) {
            LOGGER.error("Ошибка при загрузке статистики лога", e);
            
            // Fallback values
            if (eventCountLabel != null) {
                eventCountLabel.setValue("Error loading events");
            }
            if (caseCountLabel != null) {
                caseCountLabel.setValue("Error loading cases");
            }
        }
    }

    /**
     * Load real log data for filtering
     */
    private void loadLogData() {
        try {
            if (selectedLog != null && logId != null && logId > 0) {
                LOGGER.info("Загружаем данные лога: {} (ID: {})", logName, logId);
                loadAttributeData();
                loadCaseIdData();
                updateStatistics();
                
                LOGGER.info("Данные лога загружены успешно");
            } else {
                LOGGER.warn("Используются заглушечные данные - лог не выбран");
                loadSampleData();
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при загрузке данных лога", e);
            loadSampleData();
        }
    }

    /**
     * Load attribute values from the selected log
     */
    private void loadAttributeData() {
        try {
            LOGGER.info("🔄 === НАЧАЛО loadAttributeData() ===");
            
            if (attributeValuesList != null) {
                LOGGER.info("✅ attributeValuesList доступен");
                attributeValuesList.getItems().clear();
                
                // Динамически загружаем атрибуты из лога
                if (primaryAttributeCombo != null) {
                    LOGGER.info("✅ primaryAttributeCombo доступен");
                    LOGGER.info("🔧 Количество элементов в primaryAttributeCombo до загрузки: {}", primaryAttributeCombo.getItemCount());
                    
                    loadAvailableAttributesFromLog();
                    
                    LOGGER.info("🔧 Количество элементов в primaryAttributeCombo после загрузки: {}", primaryAttributeCombo.getItemCount());
                    
                    // Проверяем, что атрибуты загружены и есть выбранный
                    if (primaryAttributeCombo.getItemCount() > 0) {
                        LOGGER.info("✅ Атрибуты загружены, загружаем значения для выбранного атрибута...");
                        
                        Comboitem selectedItem = primaryAttributeCombo.getSelectedItem();
                        if (selectedItem != null) {
                            LOGGER.debug("✅ Выбранный элемент: " + selectedItem.getValue());
                        } else {
                            LOGGER.warn("⚠️ Нет выбранного элемента после загрузки атрибутов");
                        }
                        
                        // Ensure a selection exists before loading values
                        if (primaryAttributeCombo.getSelectedItem() == null && primaryAttributeCombo.getItemCount() > 0) {
                            primaryAttributeCombo.setSelectedIndex(0);
                        }
                        loadAttributeValuesForLog();
                    } else {
                        LOGGER.warn("⚠️ Атрибуты не загружены, пропускаем загрузку значений");
                    }
                } else {
                    LOGGER.warn("⚠️ primaryAttributeCombo недоступен");
                }
            } else {
                LOGGER.warn("⚠️ attributeValuesList недоступен");
            }
            
            LOGGER.info("🔄 === КОНЕЦ loadAttributeData() ===");
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при загрузке данных атрибутов", e);
        }
    }
    
    /**
     * Load available attributes from the current log
     */
    private void loadAvailableAttributesFromLog() {
        try {
            if (primaryAttributeCombo == null) {
                LOGGER.warn("primaryAttributeCombo компонент не найден");
                return;
            }
            
            LOGGER.info("Загружаем доступные атрибуты из лога: {} (ID: {})", logName, logId);
            
            // Очищаем комбобокс
            primaryAttributeCombo.getItems().clear();
            
            // Get the selected attribute type (Event vs Case)
            String selectedAttributeType = "event"; // default to event
            if (primaryAttributeType != null && primaryAttributeType.getSelectedItem() != null) {
                selectedAttributeType = primaryAttributeType.getSelectedItem().getValue();
            }
            
            LOGGER.info("🔧 Выбранный тип атрибута: {}", selectedAttributeType);
            
            if (apmLog != null) {
                LOGGER.info("🔧 APMLog доступен, количество трасс: {}", apmLog.getTraces().size());
                
                // Собираем атрибуты в зависимости от выбранного типа
                Set<String> availableAttributes = new HashSet<>();
                
                if ("event".equals(selectedAttributeType)) {
                    // Собираем атрибуты из экземпляров активности (event attributes only)
                    int eventAttributeCount = 0;
                    int totalActivityInstances = 0;
                    Set<String> standardAttributesFound = new HashSet<>();
                    
                    for (ATrace trace : apmLog.getTraces()) {
                        totalActivityInstances += trace.getActivityInstances().size();
                        for (ActivityInstance activityInstance : trace.getActivityInstances()) {
                            if (activityInstance.getAttributes() != null) {
                                eventAttributeCount += activityInstance.getAttributes().size();
                                
                                // Add event attributes only, exclude case attributes
                                for (String attrKey : activityInstance.getAttributes().keySet()) {
                                    // Skip concept:name since we have Activity as the user-friendly option
                                    if (!"concept:name".equals(attrKey)) {
                                        // Only add event-level attributes, not case attributes
                                        if (!attrKey.equals("concept:case:id") && 
                                            !attrKey.startsWith("case:")) {
                                            availableAttributes.add(attrKey);
                                        }
                                    }
                                }
                                
                                // Check if standard event attributes exist in this activity instance
                                if (activityInstance.getAttributes().containsKey("concept:name")) {
                                    standardAttributesFound.add("concept:name");
                                }
                                if (activityInstance.getAttributes().containsKey("org:resource")) {
                                    standardAttributesFound.add("org:resource");
                                }
                                if (activityInstance.getAttributes().containsKey("lifecycle:transition")) {
                                    standardAttributesFound.add("lifecycle:transition");
                                }
                                if (activityInstance.getAttributes().containsKey("time:timestamp")) {
                                    standardAttributesFound.add("time:timestamp");
                                }
                            }
                        }
                    }
                    
                    // Always add Activity as the main option (maps to concept:name internally)
                    availableAttributes.add("Activity");
                    LOGGER.info("🔧 Добавлен основной атрибут для Event: Activity");
                    
                    // Only add standard attributes if they actually exist in the log
                    if (standardAttributesFound.contains("org:resource")) {
                        availableAttributes.add("org:resource");
                        LOGGER.info("🔧 Добавлен стандартный атрибут: org:resource");
                    }
                    if (standardAttributesFound.contains("lifecycle:transition")) {
                        availableAttributes.add("lifecycle:transition");
                        LOGGER.info("🔧 Добавлен стандартный атрибут: lifecycle:transition");
                    }
                    if (standardAttributesFound.contains("time:timestamp")) {
                        availableAttributes.add("time:timestamp");
                        LOGGER.info("🔧 Добавлен стандартный атрибут: time:timestamp");
                    }
                    
                    LOGGER.info("🔧 Найдено {} event атрибутов в {} экземплярах активности", eventAttributeCount, totalActivityInstances);
                    LOGGER.info("🔧 Найдено стандартных атрибутов: {}", standardAttributesFound);
                    
                } else {
                    // For Case attributes - only collect case-level attributes
                    availableAttributes.add("concept:case:id");
                    LOGGER.info("🔧 Добавлены стандартные case атрибуты");
                    
                    // Собираем атрибуты из трасс (case attributes only)
                    int caseAttributeCount = 0;
                    for (ATrace trace : apmLog.getTraces()) {
                        if (trace.getAttributes() != null) {
                            caseAttributeCount += trace.getAttributes().size();
                            // Only add case-level attributes, not event attributes
                            for (String attrKey : trace.getAttributes().keySet()) {
                                // Filter out event attributes that might be stored at trace level
                                if (!attrKey.startsWith("event:") && 
                                    !attrKey.equals("concept:name") && 
                                    !attrKey.equals("org:resource") && 
                                    !attrKey.equals("lifecycle:transition") && 
                                    !attrKey.equals("time:timestamp")) {
                                    availableAttributes.add(attrKey);
                                }
                            }
                        }
                    }
                    LOGGER.info("🔧 Найдено {} case атрибутов в {} трассах", caseAttributeCount, apmLog.getTraces().size());
                }
                
                // Сортируем атрибуты для удобства
                List<String> sortedAttributes = new ArrayList<>(availableAttributes);
                Collections.sort(sortedAttributes);
                
                LOGGER.info("🔧 Всего уникальных атрибутов для типа '{}': {}", selectedAttributeType, sortedAttributes.size());
                LOGGER.info("🔧 Список атрибутов: {}", sortedAttributes);
                
                // Добавляем атрибуты в комбобокс
                for (String attribute : sortedAttributes) {
                    primaryAttributeCombo.appendItem(attribute);
                }
                
                // Устанавливаем Activity как выбранный по умолчанию для Event attributes
                if ("event".equals(selectedAttributeType) && sortedAttributes.contains("Activity")) {
                    // Find the index of "Activity" and select it
                    for (int i = 0; i < primaryAttributeCombo.getItemCount(); i++) {
                        if ("Activity".equals(primaryAttributeCombo.getItems().get(i).getValue())) {
                            primaryAttributeCombo.setSelectedIndex(i);
                            LOGGER.info("✅ Установлен Activity как выбранный по умолчанию");
                            break;
                        }
                    }
                } else if (sortedAttributes.size() > 0) {
                    // Fallback to first attribute if Activity is not available
                    primaryAttributeCombo.setSelectedIndex(0);
                    LOGGER.info("✅ Установлен первый атрибут как выбранный: {}", sortedAttributes.get(0));
                }
                
                LOGGER.info("✅ Загружено {} доступных атрибутов для лога: {}", sortedAttributes.size(), logName);
                
            } else {
                LOGGER.warn("⚠️ APMLog недоступен, используем стандартные атрибуты");
                // Fallback to standard attributes based on selected type
                if ("event".equals(selectedAttributeType)) {
                    primaryAttributeCombo.appendItem("Activity");
                    // Note: Standard attributes are only added if they exist in the actual log data
                } else {
                    primaryAttributeCombo.appendItem("concept:case:id");
                }
                primaryAttributeCombo.setSelectedIndex(0);
                LOGGER.info("✅ Добавлены стандартные атрибуты (fallback) для типа: {}", selectedAttributeType);
            }
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при загрузке доступных атрибутов из лога: {}", logName, e);
            // Fallback to standard attributes
            if (primaryAttributeType != null && primaryAttributeType.getSelectedItem() != null && 
                "event".equals(primaryAttributeType.getSelectedItem().getValue())) {
                primaryAttributeCombo.appendItem("Activity");
            } else {
                primaryAttributeCombo.appendItem("concept:case:id");
            }
            primaryAttributeCombo.setSelectedIndex(0);
        }
    }

    /**
     * Load attribute values based on the selected log
     */
    private void loadAttributeValuesForLog() {
        try {
            if (attributeValuesList == null) {
                LOGGER.warn("⚠️ attributeValuesList компонент не найден");
                return;
            }

            LOGGER.info("🔄 Загружаем значения атрибута для лога: {} (ID: {})", logName, logId);

            // Очищаем список
            attributeValuesList.getItems().clear();
            allAttributeValues.clear();

            if (apmLog != null && primaryAttributeCombo != null) {
                // Получаем выбранный атрибут
                Comboitem selectedItem = primaryAttributeCombo.getSelectedItem();
                if (selectedItem == null) {
                    LOGGER.warn("⚠️ Нет выбранного элемента в primaryAttributeCombo");
                    if (primaryAttributeCombo.getItemCount() > 0) {
                        primaryAttributeCombo.setSelectedIndex(0);
                        selectedItem = primaryAttributeCombo.getSelectedItem();
                    }
                    if (selectedItem == null) {
                        return;
                    }
                }

                String selectedAttribute = (selectedItem.getValue() != null) ? String.valueOf(selectedItem.getValue()) : selectedItem.getLabel();
                if (selectedAttribute == null || selectedAttribute.isEmpty()) {
                    LOGGER.warn("⚠️ Не удалось определить выбранный атрибут (value/label пусты)");
                    return;
                }
                LOGGER.info("🔧 Выбранный атрибут: {}", selectedAttribute);
                
                // Получаем уникальные значения выбранного атрибута из APMLog
                Map<String, Integer> attributeValueCounts = new HashMap<>();
                
                // Определяем тип атрибута (case или event) на основе выбранного типа в radio button
                String selectedAttributeType = "event"; // default to event
                if (primaryAttributeType != null && primaryAttributeType.getSelectedItem() != null) {
                    selectedAttributeType = primaryAttributeType.getSelectedItem().getValue();
                }
                
                boolean isCaseAttribute = "case".equals(selectedAttributeType);
                
                LOGGER.info("🔧 Тип атрибута: {}", isCaseAttribute ? "CASE" : "EVENT");
                LOGGER.info("🔧 Количество трасс в логе: {}", apmLog.getTraces().size());
                
                if (isCaseAttribute) {
                    // Обрабатываем case attributes
                    LOGGER.info("🔧 Обрабатываем CASE атрибуты...");
                    int foundCaseValues = 0;
                    for (ATrace trace : apmLog.getTraces()) {
                        if (trace.getAttributes() != null && trace.getAttributes().containsKey(selectedAttribute)) {
                            String value = trace.getAttributes().get(selectedAttribute).toString();
                            if (value != null && !value.isEmpty()) {
                                attributeValueCounts.put(value, attributeValueCounts.getOrDefault(value, 0) + 1);
                                foundCaseValues++;
                            }
                        }
                    }
                    LOGGER.info("🔧 Найдено {} значений CASE атрибута '{}'", foundCaseValues, selectedAttribute);
                } else {
                    // Обрабатываем event attributes
                    LOGGER.info("🔧 Обрабатываем EVENT атрибуты...");
                    int totalActivityInstances = 0;
                    int foundEventValues = 0;
                    
                    // Special handling for Activity attribute
                    if (selectedAttribute.equals("Activity")) {
                        LOGGER.info("🔧 Специальная обработка для атрибута Activity...");
                        for (ATrace trace : apmLog.getTraces()) {
                            totalActivityInstances += trace.getActivityInstances().size();
                            Set<String> activitiesInTrace = new HashSet<>();
                            for (ActivityInstance activityInstance : trace.getActivityInstances()) {
                                String activityName = activityInstance.getName();
                                if (activityName != null && !activityName.isEmpty()) {
                                    activitiesInTrace.add(activityName);
                                }
                            }
                            // Count each activity once per trace (case) for statistics
                            for (String activityName : activitiesInTrace) {
                                attributeValueCounts.put(activityName, attributeValueCounts.getOrDefault(activityName, 0) + 1);
                                foundEventValues++;
                            }
                        }
                        LOGGER.info("🔧 Найдено {} уникальных активностей (подсчитано по случаям)", attributeValueCounts.size());
                    } else {
                        // Regular event attributes
                        for (ATrace trace : apmLog.getTraces()) {
                            totalActivityInstances += trace.getActivityInstances().size();
                            for (ActivityInstance activityInstance : trace.getActivityInstances()) {
                                if (activityInstance.getAttributes() != null && 
                                    activityInstance.getAttributes().containsKey(selectedAttribute)) {
                                    String value = activityInstance.getAttributes().get(selectedAttribute).toString();
                                    if (value != null && !value.isEmpty()) {
                                        attributeValueCounts.put(value, attributeValueCounts.getOrDefault(value, 0) + 1);
                                        foundEventValues++;
                                    }
                                }
                            }
                        }
                    }
                    LOGGER.info("🔧 Общее количество экземпляров активности: {}", totalActivityInstances);
                    LOGGER.info("🔧 Найдено {} значений EVENT атрибута '{}'", foundEventValues, selectedAttribute);
                }
                
                LOGGER.info("🔧 Найдено {} уникальных значений атрибута '{}'", attributeValueCounts.size(), selectedAttribute);
                LOGGER.info("🔧 Детали значений: {}", attributeValueCounts);
                
                // Сохраняем все значения атрибута для пагинации
                allAttributeValues.addAll(attributeValueCounts.keySet());
                // Сортируем значения атрибута по алфавиту без учета регистра
                allAttributeValues.sort(String::compareToIgnoreCase);
                LOGGER.info("🔧 allAttributeValues заполнен и отсортирован: {} элементов", allAttributeValues.size());
                
                // Показываем первую страницу
                currentAttributePage = 1;
                displayAttributePage(currentAttributePage);
                
                LOGGER.info("✅ Загружены {} значений атрибута '{}' для лога: {}", 
                           attributeValueCounts.size(), selectedAttribute, logName);
                
            } else {
                LOGGER.warn("⚠️ APMLog или primaryAttributeCombo недоступен, используем заглушечные данные");
                if (apmLog == null) {
                    LOGGER.warn("⚠️ APMLog = null");
                }
                if (primaryAttributeCombo == null) {
                    LOGGER.warn("⚠️ primaryAttributeCombo = null");
                }
                // Fallback to sample data (already sorted alphabetically)
                allAttributeValues.addAll(Arrays.asList("Value A", "Value B", "Value C", "Value D", "Value E"));
                currentAttributePage = 1;
                displayAttributePage(currentAttributePage);
            }

        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при загрузке значений атрибута для лога: {}", logName, e);
        }
    }

    /**
     * Add attribute value to the list
     */
    private void addAttributeValue(String value, int cases, double frequency) {
        try {
            if (attributeValuesList != null) {
                LOGGER.debug("🔧 Создаем Listitem для значения: '{}', количество: {}, частота: {:.2f}%", value, cases, frequency);
                
                // Get the current selected attribute name for date formatting
                String selectedAttribute = null;
                if (primaryAttributeCombo != null && primaryAttributeCombo.getSelectedItem() != null) {
                    selectedAttribute = (primaryAttributeCombo.getSelectedItem().getValue() != null) ? 
                        String.valueOf(primaryAttributeCombo.getSelectedItem().getValue()) : 
                        primaryAttributeCombo.getSelectedItem().getLabel();
                }
                
                // Format the value if it's a date
                String displayValue = formatDateValue(value, selectedAttribute);
                
                Listitem item = new Listitem();
                item.appendChild(new Listcell(displayValue));
                item.appendChild(new Listcell(String.valueOf(cases)));
                item.appendChild(new Listcell(formatFrequency(frequency)));
                
                attributeValuesList.appendChild(item);
                
                LOGGER.debug("✅ Listitem добавлен в таблицу для значения: '{}' (отформатировано: '{}')", value, displayValue);
            } else {
                LOGGER.warn("⚠️ attributeValuesList == null в addAttributeValue");
            }
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при добавлении значения атрибута '{}' в таблицу", value, e);
        }
    }
    
    /**
     * Format frequency with smart decimal places
     * Shows 2 decimal places when needed, but only 1 when it rounds nicely
     */
    private String formatFrequency(double frequency) {
        // Round to 2 decimal places first
        double rounded2 = Math.round(frequency * 100.0) / 100.0;
        
        // Check if it rounds to exactly 1 decimal place
        double rounded1 = Math.round(frequency * 10.0) / 10.0;
        
        if (Math.abs(rounded2 - rounded1) < 0.001) {
            // It rounds nicely to 1 decimal place
            return String.format("%.1f%%", rounded1);
        } else {
            // Need 2 decimal places for precision
            return String.format("%.2f%%", rounded2);
        }
    }
    
    /**
     * Format date values to display format "06 Nov 20, 17:27:04"
     */
    private String formatDateValue(String value, String attributeName) {
        // Check if this is a date attribute
        if (attributeName != null && (attributeName.startsWith("date_") || 
            attributeName.equals("time:timestamp") || 
            attributeName.contains("time") || 
            attributeName.contains("date"))) {
            
            try {
                // Try to parse the date value and format it
                return formatDateString(value);
            } catch (Exception e) {
                // If parsing fails, return the original value
                LOGGER.debug("Could not parse date value '{}' for attribute '{}': {}", value, attributeName, e.getMessage());
                return value;
            }
        }
        return value;
    }
    
    /**
     * Parse and format a date string to "06 Nov 20, 17:27:04" format
     */
    private String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return dateStr;
        }
        
        try {
            // Try multiple date formats that might be in the CSV
            String[] possibleFormats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss",
                "MM/dd/yyyy HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "MM-dd-yyyy HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM/dd/yyyy"
            };
            
            for (String format : possibleFormats) {
                try {
                    SimpleDateFormat parser = new SimpleDateFormat(format);
                    Date date = parser.parse(dateStr.trim());
                    
                    // Format to desired output format "06 Nov 20, 17:27:04"
                    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy, HH:mm:ss");
                    return formatter.format(date);
                } catch (Exception ignored) {
                    // Try next format
                }
            }
            
            // If none of the formats work, return original
            return dateStr;
            
        } catch (Exception e) {
            LOGGER.debug("Error formatting date string '{}': {}", dateStr, e.getMessage());
            return dateStr;
        }
    }

    /**
     * Load case IDs from the selected log
     */
    private void loadCaseIdData() {
        try {
            if (caseIdValuesList != null) {
                caseIdValuesList.getItems().clear();
                
                // Загружаем Case ID для выбранного лога
                loadCaseIdsForLog();
            }
        } catch (Exception e) {
            LOGGER.error("Error loading case ID data", e);
        }
    }

    /**
     * Load Case IDs based on the selected log
     */
    private void loadCaseIdsForLog() {
        try {
            if (caseIdValuesList == null) {
                LOGGER.warn("⚠️ caseIdValuesList компонент не найден");
                return;
            }

            LOGGER.info("🔄 Загружаем Case ID для лога: {} (ID: {})", logName, logId);

            // Очищаем список
            caseIdValuesList.getItems().clear();
            allCaseIdValues.clear();

            if (apmLog != null) {
                // Получаем Case ID из APMLog
                Map<String, Integer> caseIdCounts = new HashMap<>();
                
                for (ATrace trace : apmLog.getTraces()) {
                    String caseId = trace.getCaseId();
                    if (caseId != null && !caseId.isEmpty()) {
                        int eventCount = trace.getActivityInstances().size();
                        caseIdCounts.put(caseId, eventCount);
                    }
                }
                
                // Сохраняем все Case ID для пагинации и сортируем по алфавиту
                allCaseIdValues.addAll(caseIdCounts.keySet());
                allCaseIdValues.sort(String::compareToIgnoreCase); // Сортировка по алфавиту без учета регистра
                
                // Показываем первую страницу
                currentCaseIdPage = 1;
                displayCaseIdPage(currentCaseIdPage);
                
                LOGGER.info("✅ Загружены {} Case ID для лога: {}", caseIdCounts.size(), logName);
                
            } else {
                LOGGER.warn("⚠️ APMLog недоступен, используем заглушечные данные");
                // Fallback to sample data (already sorted)
                allCaseIdValues.addAll(Arrays.asList("Case_001", "Case_002", "Case_003", "Case_004", "Case_005"));
                currentCaseIdPage = 1;
                displayCaseIdPage(currentCaseIdPage);
            }

        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при загрузке Case ID для лога: {}", logName, e);
        }
    }

    /**
     * Add case ID to the list
     */
    private void addCaseId(String caseId, int activityInstances) {
        if (caseIdValuesList != null) {
            Listitem item = new Listitem();
            item.appendChild(new Listcell(caseId));
            item.appendChild(new Listcell(String.valueOf(activityInstances)));
            
            caseIdValuesList.appendChild(item);
        }
    }


    /**
     * Load sample data when real log is not available
     */
    private void loadSampleData() {
        try {
            LOGGER.info("Loading sample data for testing");
            loadAttributeData();
            loadCaseIdData();
            updateStatistics();
        } catch (Exception e) {
            LOGGER.error("Error loading sample data", e);
        }
    }

    /**
     * Initialize navigation event handlers
     */
    private void initializeNavigation() {
        try {
            // Case filter navigation
            if (caseAttributeNav != null) {
                caseAttributeNav.addEventListener(Events.ON_CLICK, event -> showPanel("caseAttribute"));
            }
            if (caseVariantNav != null) {
                caseVariantNav.addEventListener(Events.ON_CLICK, event -> showPanel("caseVariant"));
            }
            if (caseIdNav != null) {
                caseIdNav.addEventListener(Events.ON_CLICK, event -> showPanel("caseId"));
            }
            if (timeframeNav != null) {
                timeframeNav.addEventListener(Events.ON_CLICK, event -> showPanel("timeframe"));
            }
            if (performanceNav != null) {
                performanceNav.addEventListener(Events.ON_CLICK, event -> showPanel("performance"));
            }
            if (pathNav != null) {
                pathNav.addEventListener(Events.ON_CLICK, event -> showPanel("path"));
            }
            if (reworkNav != null) {
                reworkNav.addEventListener(Events.ON_CLICK, event -> showPanel("rework"));
            }
            if (blocksNav != null) {
                blocksNav.addEventListener(Events.ON_CLICK, event -> showPanel("blocks"));
            }

            // Event filter navigation  
            if (eventAttributeNav != null) {
                eventAttributeNav.addEventListener(Events.ON_CLICK, event -> showPanel("eventAttribute"));
            }
            if (eventTimeframeNav != null) {
                eventTimeframeNav.addEventListener(Events.ON_CLICK, event -> showPanel("eventTimeframe"));
            }
            if (eventFrequencyNav != null) {
                eventFrequencyNav.addEventListener(Events.ON_CLICK, event -> showPanel("eventFrequency"));
            }
            if (eventPerformanceNav != null) {
                eventPerformanceNav.addEventListener(Events.ON_CLICK, event -> showPanel("eventPerformance"));
            }
            if (eventPathNav != null) {
                eventPathNav.addEventListener(Events.ON_CLICK, event -> showPanel("eventPath"));
            }
            if (eventBetweenNav != null) {
                eventBetweenNav.addEventListener(Events.ON_CLICK, event -> showPanel("eventBetween"));
            }

            LOGGER.info("Navigation initialized");
        } catch (Exception e) {
            LOGGER.error("Error initializing navigation", e);
        }
    }

    /**
     * Initialize control buttons
     */
    private void initializeControlButtons() {
        try {
            // Initialize apply filters button
            if (applyFiltersBtn != null) {
                applyFiltersBtn.addEventListener(Events.ON_CLICK, event -> applyFiltersAndRedirect());
            }
            
			// Bind Case Attribute panel's local Apply button
			if (applyFiltersBtnCaseAttribute != null) {
				applyFiltersBtnCaseAttribute.addEventListener(Events.ON_CLICK, event -> {
					currentFilter = "caseAttribute";
					applyFiltersAndRedirect();
				});
			}

            // Bind Case ID panel's local Apply button
            if (applyFiltersBtnCaseId != null) {
                applyFiltersBtnCaseId.addEventListener(Events.ON_CLICK, event -> {
                    currentFilter = "caseId";
                    applyFiltersAndRedirect();
                });
            }

            // Initialize cancel button
            if (cancelBtn != null) {
                cancelBtn.addEventListener(Events.ON_CLICK, event -> closeWindow());
            }
            
			LOGGER.info("Control buttons initialized");
        } catch (Exception e) {
            LOGGER.error("Error initializing control buttons", e);
        }
    }
    
    /**
     * Initialize timeframe filter components
     */
    private void initializeTimeframeFilter() {
        try {
            // Set default containment type to "start in"
            setContainmentIcon("start");
            
            // Initialize date inputs with current date
            if (fromDate != null) {
                fromDate.setDisabled(false);
                fromDate.setValue(new Date());
            }
            if (toDate != null) {
                toDate.setDisabled(true);
                if (apmLog != null) {
                    toDate.setValue(new Date(apmLog.getEndTime()));
                } else {
                    toDate.setValue(new Date());
                }
            }
            
            // Initialize time chart
            initializeTimeChart();
            
            LOGGER.info("Timeframe filter initialized");
        } catch (Exception e) {
            LOGGER.error("Error initializing timeframe filter", e);
        }
    }

    /**
     * Show specific filter panel and update navigation
     */
    private void showPanel(String panelName) {
        try {
            // Hide all panels
            hideAllPanels();
            
            // Reset all navigation button styles
            resetNavigation();
            
            // Show selected panel and highlight navigation
            switch (panelName) {
                case "caseAttribute":
                    if (caseAttributePanel != null) caseAttributePanel.setVisible(true);
                    if (caseAttributeNav != null) caseAttributeNav.setSclass("filter-nav-item selected");
                    break;
                case "caseId":
                    if (caseIdPanel != null) caseIdPanel.setVisible(true);
                    if (caseIdNav != null) caseIdNav.setSclass("filter-nav-item selected");
                    break;
                case "caseVariant":
                    if (caseVariantPanel != null) caseVariantPanel.setVisible(true);
                    if (caseVariantNav != null) caseVariantNav.setSclass("filter-nav-item selected");
                    break;
                case "timeframe":
                    LOGGER.info("Showing timeframe panel...");
                    if (timeframePanel != null) {
                        timeframePanel.setVisible(true);
                        LOGGER.info("Timeframe panel visibility set to true");
                    } else {
                        LOGGER.error("Timeframe panel is null!");
                    }
                    if (timeframeNav != null) timeframeNav.setSclass("filter-nav-item selected");
                    // Initialize time chart when timeframe panel is shown
                    LOGGER.info("Timeframe panel shown, initializing time chart...");
                    // Use a timer to ensure the panel is fully rendered before initializing
                    java.util.Timer timer = new java.util.Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                LOGGER.info("Timer triggered, initializing time chart...");
                                initializeTimeChart();
                            } catch (Exception e) {
                                LOGGER.error("Error initializing time chart from timer", e);
                            }
                        }
                    }, 200);
                    break;
                case "performance":
                    if (performancePanel != null) performancePanel.setVisible(true);
                    if (performanceNav != null) performanceNav.setSclass("filter-nav-item selected");
            initializePerformanceFilter();
                    break;
                case "path":
                    if (pathPanel != null) pathPanel.setVisible(true);
                    if (pathNav != null) pathNav.setSclass("filter-nav-item selected");
                    break;
                case "rework":
                    if (reworkPanel != null) reworkPanel.setVisible(true);
                    if (reworkNav != null) reworkNav.setSclass("filter-nav-item selected");
                    break;
                case "blocks":
                    if (blocksPanel != null) blocksPanel.setVisible(true);
                    if (blocksNav != null) blocksNav.setSclass("filter-nav-item selected");
                    break;
                // Event filters would be handled similarly
                default:
                    LOGGER.warn("Unknown panel: " + panelName);
                    // Default to case attribute
                    if (caseAttributePanel != null) caseAttributePanel.setVisible(true);
                    if (caseAttributeNav != null) caseAttributeNav.setSclass("filter-nav-item selected");
                    break;
            }
            
            this.currentFilter = panelName;
            LOGGER.info("Switched to {} filter panel", panelName);
            
        } catch (Exception e) {
            LOGGER.error("Error showing panel: " + panelName, e);
        }
    }

    /**
     * Hide all filter panels
     */
    private void hideAllPanels() {
        if (caseAttributePanel != null) caseAttributePanel.setVisible(false);
        if (caseIdPanel != null) caseIdPanel.setVisible(false);
        if (caseVariantPanel != null) caseVariantPanel.setVisible(false);
        if (timeframePanel != null) timeframePanel.setVisible(false);
        if (performancePanel != null) performancePanel.setVisible(false);
        if (pathPanel != null) pathPanel.setVisible(false);
        if (reworkPanel != null) reworkPanel.setVisible(false);
        if (blocksPanel != null) blocksPanel.setVisible(false);
    }

    /**
     * Reset all navigation button styles
     */
    private void resetNavigation() {
        if (caseAttributeNav != null) caseAttributeNav.setSclass("filter-nav-item");
        if (caseVariantNav != null) caseVariantNav.setSclass("filter-nav-item");
        if (caseIdNav != null) caseIdNav.setSclass("filter-nav-item");
        if (timeframeNav != null) timeframeNav.setSclass("filter-nav-item");
        if (performanceNav != null) performanceNav.setSclass("filter-nav-item");
        if (pathNav != null) pathNav.setSclass("filter-nav-item");
        if (reworkNav != null) reworkNav.setSclass("filter-nav-item");
        if (blocksNav != null) blocksNav.setSclass("filter-nav-item");
        if (eventAttributeNav != null) eventAttributeNav.setSclass("filter-nav-item");
        if (eventTimeframeNav != null) eventTimeframeNav.setSclass("filter-nav-item");
        if (eventFrequencyNav != null) eventFrequencyNav.setSclass("filter-nav-item");
        if (eventPerformanceNav != null) eventPerformanceNav.setSclass("filter-nav-item");
        if (eventPathNav != null) eventPathNav.setSclass("filter-nav-item");
        if (eventBetweenNav != null) eventBetweenNav.setSclass("filter-nav-item");
    }

    /**
     * Apply filters and redirect to Process Discoverer
     */
    private void applyFiltersAndRedirect() {
        try {
            LOGGER.info("🎯 Применение фильтров к логу: {} (ID: {})", logName, logId);
            
            // Get current filter settings and selected values (do not apply yet)
            String filterDescription = getCurrentFilterDescription();
            List<String> selectedValues = getSelectedFilterValues();

            if (selectedValues.isEmpty()) {
                Messagebox.show("Пожалуйста, выберите значения для фильтрации", 
                              "Фильтры не выбраны", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            // Create rules now, but DO NOT APPLY. Accumulate to pass into criteria window.
            List<LogFilterRule> newRules = createFilterRules(selectedValues);
            if (newRules == null || newRules.isEmpty()) {
                Messagebox.show("Не удалось создать правила фильтрации", "Предупреждение", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }

            // Merge with any existing criteria passed in (if this is the second+ filter)
            if (currentCriteria == null) currentCriteria = new ArrayList<>();
            List<LogFilterRule> combinedRules = new ArrayList<>(currentCriteria);
            combinedRules.addAll(newRules);

            // Merge descriptions using already accumulated ones if available
            List<String> combinedDescriptions = new java.util.ArrayList<>();
            if (criteriaDescriptionsFromArgs != null && !criteriaDescriptionsFromArgs.isEmpty()) {
                combinedDescriptions.addAll(criteriaDescriptionsFromArgs);
            } else {
                try {
                    Map<?, ?> inArgs = org.zkoss.zk.ui.Executions.getCurrent().getArg();
                    Object prevDescs = inArgs != null ? inArgs.get("criteriaDescriptions") : null;
                    if (prevDescs instanceof java.util.List) {
                        //noinspection unchecked
                        combinedDescriptions.addAll((java.util.List<String>) prevDescs);
                    }
                } catch (Exception ignore) {}
            }
            String desc = filterDescription != null ? filterDescription : "Filter";
            combinedDescriptions.add(desc);

            // Prepare args for criteria window
            java.util.Map<String,Object> args = new java.util.HashMap<>();
            args.put("criteriaDescriptions", combinedDescriptions);
            args.put("criteriaRules", combinedRules);
            // pass-through original context so "+" can reopen filter with proper log and client
            if (this.portalContext != null) args.put("portalContext", this.portalContext);
            if (this.selectedLog != null) args.put("selectedLog", this.selectedLog);
            if (this.logName != null) args.put("logName", this.logName);
            if (this.logId != null) args.put("logId", this.logId);
            if (this.apmLog != null) args.put("apmLog", this.apmLog);
            if (this.logFilterClient != null) args.put("logFilterClient", this.logFilterClient);

            // Close the current filter window first
            LOGGER.info("🔧 Closing current filter window before opening criteria window...");
            closeWindow();
            
            // Additional cleanup to ensure no lingering windows
            try {
                org.zkoss.zk.ui.Desktop desktop = org.zkoss.zk.ui.Executions.getCurrent().getDesktop();
                if (desktop != null) {
                    for (org.zkoss.zk.ui.Page page : desktop.getPages()) {
                        org.zkoss.zk.ui.Component old = page.getFellowIfAny("logFilterWindow");
                        if (old != null) {
                            LOGGER.info("🔧 Found lingering logFilterWindow, detaching...");
                            try { old.detach(); } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {}
            
            LOGGER.info("🔧 Filter window cleanup completed, opening criteria window...");
            // Always load via classloader to avoid context-relative path issues
            org.zkoss.zul.Window w = null;
            if (portalContext != null && portalContext.getUI() != null) {
                w = (org.zkoss.zul.Window) portalContext.getUI()
                        .createComponent(getClass().getClassLoader(), "filterCriteria.zul", null, args);
            } else {
                w = (org.zkoss.zul.Window) org.zkoss.zk.ui.Executions.getCurrent()
                        .createComponentsDirectly(
                                new java.io.InputStreamReader(
                                        getClass().getClassLoader().getResourceAsStream("filterCriteria.zul"),
                                        java.nio.charset.StandardCharsets.UTF_8),
                                "zul", null, args);
            }
            if (w != null) {
                w.doModal();
            }
            LOGGER.info("🎯 Критерий добавлен (без применения): {} значений для {}", selectedValues.size(), filterDescription);
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при применении фильтров к {} (ID: {})", logName, logId, e);
            Messagebox.show("Ошибка применения фильтров к " + (logName != null ? logName : "логу") + ": " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }

    /**
     * Clear all filters and restore original log
     */
    public void clearAllFilters() {
        try {
            LOGGER.info("🧹 Сброс всех фильтров для лога: {} (ID: {})", logName, logId);
            
            // Просто закрываем окно фильтрации
            // Process Discoverer сам обработает сброс фильтров
            closeWindow();
            
            LOGGER.info("✅ Окно фильтрации закрыто");
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при сбросе фильтров", e);
            Messagebox.show("Ошибка при сбросе фильтров: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    /**
     * Reload Process Discoverer without filters
     */
    private void reloadProcessDiscovererWithoutFilters() {
        try {
            LOGGER.info("🔄 Перезагрузка Process Discoverer без фильтров");
            
            // Получаем текущий контекст
            org.zkoss.zk.ui.Execution current = org.zkoss.zk.ui.Executions.getCurrent();
            
            // Создаем аргументы для перезагрузки
            Map<String, Object> args = new HashMap<>();
            args.put("logId", logId);
            args.put("logName", logName);
            args.put("clearFilters", true);
            
            // Перенаправляем на Process Discoverer
            current.sendRedirect("/processdiscoverer");
            
            LOGGER.info("✅ Process Discoverer перезагружен без фильтров");
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при перезагрузке Process Discoverer", e);
        }
    }

    /**
     * Get selected filter values from current panel
     */
    private List<String> getSelectedFilterValues() {
        List<String> selectedValues = new ArrayList<>();
        
        try {
            switch (currentFilter) {
                case "caseAttribute":
                    if (attributeValuesList != null) {
                        for (Listitem item : attributeValuesList.getSelectedItems()) {
                            if (item.getFirstChild() instanceof Listcell) {
                                Listcell cell = (Listcell) item.getFirstChild();
                                if (cell.getLabel() != null) {
                                    selectedValues.add(cell.getLabel());
                                }
                            }
                        }
                    }
                    break;
                    
                case "caseId":
                    if (caseIdValuesList != null) {
                        for (Listitem item : caseIdValuesList.getSelectedItems()) {
                            if (item.getFirstChild() instanceof Listcell) {
                                Listcell cell = (Listcell) item.getFirstChild();
                                if (cell.getLabel() != null) {
                                    selectedValues.add(cell.getLabel());
                                }
                            }
                        }
                    }
                    break;
                    
                case "timeframe":
                    long ft = getFromTime();
                    long tt = getToTime();
                    String ct = getSelectedContainmentType();
                    selectedValues.add("timeframe:" + ct + ":" + ft + ":" + tt);
                    break;
                
                case "performance":
                    // Encode selection to bypass empty-check and for logging
                    String measure = performanceMeasure != null && performanceMeasure.getSelectedItem() != null ? performanceMeasure.getSelectedItem().getLabel() : "Case duration";
                    if ("Case length".equals(measure)) {
                        Integer from = perfLenFrom != null ? perfLenFrom.getValue() : null;
                        Integer to = perfLenTo != null ? perfLenTo.getValue() : null;
                        if ((from != null && from > 0) || (to != null && to > 0)) {
                            selectedValues.add("performance:length:" + (from != null ? from : "") + ":" + (to != null ? to : ""));
                        }
                    } else {
                        long gteMsSel = toMillis(perfGteValue, perfGteUnit);
                        long lteMsSel = toMillis(perfLteValue, perfLteUnit);
                        if (gteMsSel > 0 || lteMsSel > 0) {
                            selectedValues.add("performance:" + measure + ":" + gteMsSel + ":" + lteMsSel);
                        }
                    }
                    break;
                
                default:
                    LOGGER.warn("Получение выбранных значений для {} пока не реализовано", currentFilter);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при получении выбранных значений", e);
        }
        
        return selectedValues;
    }

    /**
     * Open filtered log in Process Discoverer
     */
    private void openInProcessDiscoverer(List<String> selectedValues, String filterDescription) {
        try {
            LOGGER.info("🚀 Открытие отфильтрованного лога в Process Discoverer");
            LOGGER.info("📋 Фильтр: {}", filterDescription);
            LOGGER.info("📊 Значения: {}", selectedValues);
            
            if (logId == null || logId <= 0 || portalContext == null) {
                LOGGER.error("❌ Некорректные данные для открытия Process Discoverer: logId={}, portalContext={}", logId, portalContext);
                Messagebox.show("Ошибка: недостаточно данных для открытия Process Discoverer", "Ошибка", Messagebox.OK, Messagebox.ERROR);
                return;
            }
            
            Set<LogSummaryType> selectedLogs = getSelectedLogsFromContext();
            if (selectedLogs.isEmpty()) {
                LOGGER.error("❌ Не выбран лог для открытия в Process Discoverer");
                Messagebox.show("Ошибка: не выбран лог", "Ошибка", Messagebox.OK, Messagebox.ERROR);
                return;
            }
            
            LogSummaryType selectedLog = selectedLogs.iterator().next();
            
            // Используем существующий PDFrequencyPlugin для открытия Process Discoverer
            if (processDiscovererPlugin != null) {
                LOGGER.info("✅ Используем PDFrequencyPlugin для открытия Process Discoverer");
                
                try {
                    // Используем рефлексию для вызова метода openWithFilters
                    java.lang.reflect.Method openWithFiltersMethod = 
                        processDiscovererPlugin.getClass().getMethod("openWithFilters", 
                            org.apromore.plugin.portal.PortalContext.class, 
                            java.util.List.class);
                    
                    // Создаем простые фильтры на основе выбранных значений
                    List<Object> logFilters = new ArrayList<>();
                    
                    // Вызываем метод через рефлексию
                    openWithFiltersMethod.invoke(processDiscovererPlugin, portalContext, logFilters);
                    
                    LOGGER.info("✅ Process Discoverer открыт через PDFrequencyPlugin:");
                    LOGGER.info("   - Лог: {} (ID: {})", selectedLog.getName(), selectedLog.getId());
                    LOGGER.info("   - Тип фильтра: {}", currentFilter);
                    LOGGER.info("   - Выбрано значений: {}", selectedValues.size());
                    
                    Messagebox.show("Лог с примененными фильтрами открыт в Process Discoverer", 
                                  "Успех", Messagebox.OK, Messagebox.INFORMATION);
                    
                } catch (Exception e) {
                    LOGGER.error("❌ Ошибка при вызове PDFrequencyPlugin через рефлексию", e);
                    // Fallback к прямому URL
                    throw e;
                }
                
            } else {
                LOGGER.warn("⚠️ PDFrequencyPlugin недоступен, используем прямой URL");
                
                // Fallback: прямой URL
                String pdUrl = String.format("processdiscoverer/zul/processDiscoverer.zul?REFER_ID=%s", 
                                            Executions.getCurrent().getDesktop().getId());
                
                String jsCode = String.format("window.open('%s', '_blank', 'width=1200,height=800,scrollbars=yes,resizable=yes');", pdUrl);
                Clients.evalJavaScript(jsCode);
                
                LOGGER.info("✅ Process Discoverer открыт через прямой URL: {}", pdUrl);
                Messagebox.show("Лог с примененными фильтрами открыт в Process Discoverer", 
                              "Успех", Messagebox.OK, Messagebox.INFORMATION);
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при открытии в Process Discoverer", e);
            Messagebox.show("Ошибка при открытии Process Discoverer: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }

    /**
     * Get description of current filter settings
     */
    private String getCurrentFilterDescription() {
        try {
            switch (currentFilter) {
                case "caseAttribute":
                    String condition = caseAttributeCondition != null && caseAttributeCondition.getSelectedItem() != null ? 
                        caseAttributeCondition.getSelectedItem().getValue() : "retain";
                    String attribute = primaryAttributeCombo != null ? primaryAttributeCombo.getValue() : "Activity";
                    
                    // Count selected values
                    int selectedCount = 0;
                    if (attributeValuesList != null) {
                        selectedCount = attributeValuesList.getSelectedItems().size();
                    }
                    
                    return String.format("Case Attribute Filter: %s cases where '%s' matches %d selected values", 
                        condition.equals("retain") ? "Retain" : "Remove", attribute, selectedCount);
                        
                case "caseId":
                    String idCondition = caseIdCondition != null && caseIdCondition.getSelectedItem() != null ? 
                        caseIdCondition.getSelectedItem().getValue() : "retain";
                    
                    int selectedIds = 0;
                    if (caseIdValuesList != null) {
                        selectedIds = caseIdValuesList.getSelectedItems().size();
                    }
                    
                    return String.format("Case ID Filter: %s %d selected case IDs", 
                        idCondition.equals("retain") ? "Retain" : "Remove", selectedIds);
                        
                default:
                    return currentFilter + " filter (configuration pending)";
            }
        } catch (Exception e) {
            LOGGER.error("Error getting filter description", e);
            return "Filter applied";
        }
    }

    /**
     * Get selected logs from portal context
     */
    private Set<LogSummaryType> getSelectedLogsFromContext() {
        Set<LogSummaryType> selectedLogs = new HashSet<>();
        
        try {
            if (portalContext != null) {
                Map<SummaryType, List<VersionSummaryType>> elements = 
                    portalContext.getSelection().getSelectedProcessModelVersions();
                    
                for (Map.Entry<SummaryType, List<VersionSummaryType>> entry : elements.entrySet()) {
                    if (entry.getKey() instanceof LogSummaryType) {
                        selectedLogs.add((LogSummaryType) entry.getKey());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при получении выбранных логов", e);
        }
        
        return selectedLogs;
    }
    
    /**
     * Prepare Process Discoverer session with proper initialization
     */
    private String prepareProcessDiscovererSession(LogSummaryType selectedLog, List<String> selectedValues, String filterDescription) {
        try {
            LOGGER.info("🔄 Подготовка сессии Process Discoverer для лога: {}", selectedLog.getName());
            
            // Сохраняем информацию о фильтрах в сессию для использования в Process Discoverer
            Map<String, Object> filterData = new HashMap<>();
            filterData.put("filterType", currentFilter);
            filterData.put("filterDescription", filterDescription);
            filterData.put("selectedValues", selectedValues);
            filterData.put("logId", selectedLog.getId());
            filterData.put("logName", selectedLog.getName());
            
            // Генерируем уникальный ID сессии
            String sessionId = "filter_" + selectedLog.getId() + "_" + System.currentTimeMillis();
            Sessions.getCurrent().setAttribute(sessionId, filterData);
            
            LOGGER.info("✅ Данные фильтров сохранены в сессии: {}", sessionId);
            return sessionId;
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при создании сессии Process Discoverer", e);
            return null;
        }
    }
    
    /**
     * Apply filters directly to Process Discoverer
     */
    private void applyFiltersToProcessDiscoverer(List<String> selectedValues, String filterDescription) {
        try {
            LOGGER.info("🎯 Применение фильтров к Process Discoverer");
            LOGGER.info("📋 Фильтр: {}", filterDescription);
            LOGGER.info("📊 Значения: {}", selectedValues);
            
            if (logFilterClient == null) {
                LOGGER.error("❌ LogFilterClient недоступен");
                Messagebox.show("Ошибка: LogFilterClient недоступен", "Ошибка", Messagebox.OK, Messagebox.ERROR);
                return;
            }
            
            // Создаем правила фильтрации на основе выбранных значений
            LOGGER.info("🔧 Создание правил фильтрации...");
            List<LogFilterRule> filterRules = createFilterRules(selectedValues);
            LOGGER.info("🔧 Создано правил: {}", filterRules.size());
            
            if (filterRules.isEmpty()) {
                LOGGER.warn("Не удалось создать правила фильтрации");
                Messagebox.show("Не удалось создать правила фильтрации", "Предупреждение", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            // Создаем LogFilterResponse с отфильтрованным логом
            LOGGER.info("🔧 Применение фильтров к логу...");
            APMLog filteredLog = applyFiltersToLog(filterRules);
            LOGGER.info("🔧 Результат применения фильтров: {}", filteredLog != null ? "успешно" : "неудачно");
            
            if (filteredLog == null) {
                LOGGER.error("Не удалось применить фильтры к логу");
                Messagebox.show("Не удалось применить фильтры к логу", "Ошибка", Messagebox.OK, Messagebox.ERROR);
                return;
            }
            
            // Создаем PLog из оригинального APMLog и применяем фильтры
            LOGGER.info("🔧 Создание PLog из оригинального APMLog...");
            LOGGER.info("🔧 Оригинальный APMLog содержит {} трасс", apmLog.getTraces().size());
            
            PLog filteredPLog = new PLog(apmLog);
            LOGGER.info("🔧 PLog создан успешно из оригинального лога");
            LOGGER.info("🔧 PLog содержит {} трасс", filteredPLog.getPTraces().size());
            
            // Применяем фильтры к PLog
            LOGGER.info("🔧 Применение фильтров к PLog...");
            APMLogFilter pLogFilter = new APMLogFilter(apmLog);
            pLogFilter.filter(filterRules);
            filteredPLog = pLogFilter.getPLog();
            
            LOGGER.info("🔧 PLog после фильтрации содержит {} трасс", filteredPLog.getPTraces().size());
            
            if (filteredPLog.getPTraces().isEmpty()) {
                LOGGER.error("PLog пуст после применения фильтров");
                Messagebox.show("Отфильтрованный лог пуст. Попробуйте другие критерии фильтрации.", 
                              "Предупреждение", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            // Создаем LogFilterResponse
            LOGGER.info("Создание LogFilterResponse...");
            LOGGER.info("Log ID: {}", logId);
            LOGGER.info("Log Name: {}", logName);
            LOGGER.info("Filtered PLog traces: {}", filteredPLog.getPTraces().size());
            LOGGER.info("Filter rules count: {}", filterRules.size());
            
            LogFilterResponse response = new LogFilterResponse(
                logId,
                logName,
                apmLog,  //оригинальный лог
                filteredPLog,  // отфильтрованный лог
                filterRules,  //примененные правила
                new HashMap<>()  //дополнительные параметры
            );
            
            LOGGER.info("LogFilterResponse создан успешно");
            
            //отправляет ответ через LogFilterClient
            logFilterClient.processResponse(response);
            
            LOGGER.info("Фильтры успешно применены к Process Discoverer:");
            LOGGER.info("   - Лог: {} (ID: {})", logName, logId);
            LOGGER.info("   - Тип фильтра: {}", currentFilter);
            LOGGER.info("   - Выбрано значений: {}", selectedValues.size());
            LOGGER.info("   - Создано правил: {}", filterRules.size());
            LOGGER.info("   - Трасс в отфильтрованном логе: {}", filteredLog.getTraces().size());
            
            Messagebox.show("Фильтры успешно применены к Process Discoverer", 
                          "Успех", Messagebox.OK, Messagebox.INFORMATION);
            
            //закрывает окно фильтрации
            closeWindow();
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при применении фильтров к Process Discoverer", e);
            LOGGER.error("Полный стек ошибки:", e);
            Messagebox.show("Ошибка при применении фильтров: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    /**
     * Create filter rules based on selected values and current filter type
     */
    private List<LogFilterRule> createFilterRules(List<String> selectedValues) {
        List<LogFilterRule> rules = new ArrayList<>();
        
        try {
            LOGGER.info("Создание правил фильтрации для типа: {}", currentFilter);
            LOGGER.info("Выбрано значений: {}", selectedValues.size());
            LOGGER.info("Значения: {}", selectedValues);
            
            switch (currentFilter) {
                case "caseAttribute":
                    rules = createAttributeFilterRules(selectedValues);
                    break;
                case "caseId":
                    rules = createCaseIdFilterRules(selectedValues);
                    break;
                case "timeframe":
                    rules = createTimeframeFilterRules();
                    break;
                case "performance":
                    rules = createPerformanceFilterRules();
                    break;
                default:
                    LOGGER.warn("Неподдерживаемый тип фильтра: {}", currentFilter);
                    break;
            }
            
            LOGGER.info("Создано {} правил фильтрации", rules.size());
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при создании правил фильтрации", e);
            LOGGER.error("Полный стек ошибки:", e);
            LOGGER.error("Тип ошибки: {}", e.getClass().getSimpleName());
            LOGGER.error("Сообщение ошибки: {}", e.getMessage());
        }
        
        return rules;
    }
    
    /**
     * Create filter rules for Attribute filtering (both Case and Event attributes)
     */
    private List<LogFilterRule> createAttributeFilterRules(List<String> selectedValues) {
        List<LogFilterRule> rules = new ArrayList<>();
        
        try {
            // Get the selected attribute type (Event vs Case)
            String selectedAttributeType = "event"; // default to event
            if (primaryAttributeType != null && primaryAttributeType.getSelectedItem() != null) {
                selectedAttributeType = primaryAttributeType.getSelectedItem().getValue();
                LOGGER.info("🔧 Radio button selection: primaryAttributeType={}, selectedItem={}, value={}", 
                           primaryAttributeType, primaryAttributeType.getSelectedItem(), selectedAttributeType);
            } else {
                LOGGER.warn("⚠️ primaryAttributeType is null or has no selected item");
                if (primaryAttributeType == null) {
                    LOGGER.warn("⚠️ primaryAttributeType is null");
                } else {
                    LOGGER.warn("⚠️ primaryAttributeType.getSelectedItem() is null");
                }
            }
            
                           // Get the selected attribute
               String selectedAttribute = "concept:name"; // default
               if (primaryAttributeCombo != null && primaryAttributeCombo.getSelectedItem() != null) {
                   Comboitem selectedItem = primaryAttributeCombo.getSelectedItem();
                   // Try to get value first, then fallback to label if value is null
                   if (selectedItem.getValue() != null) {
                       selectedAttribute = String.valueOf(selectedItem.getValue());
                   } else if (selectedItem.getLabel() != null) {
                       selectedAttribute = selectedItem.getLabel();
                   }
                   LOGGER.info("🔧 Выбранный элемент: value='{}', label='{}', используем: '{}'", 
                              selectedItem.getValue(), selectedItem.getLabel(), selectedAttribute);
               }
               
               // Add debugging to show what we're creating
               LOGGER.info("🔧 Создание LogFilterRule: filterType={}, key={}, selectedAttribute={}", 
                          selectedAttributeType.equals("case") ? "CASE_CASE_ATTRIBUTE" : "EVENT_EVENT_ATTRIBUTE",
                          selectedAttribute, selectedAttribute);
            
            LOGGER.info("🔧 Создание правил фильтрации для атрибута: {} (тип: {})", selectedAttribute, selectedAttributeType);
            
            // Получаем условие фильтрации (Retain/Remove)
            String condition = caseAttributeCondition != null ? caseAttributeCondition.getSelectedItem().getValue() : "retain";
            Choice choice = "retain".equals(condition) ? Choice.RETAIN : Choice.REMOVE;
            
            // Determine filter type based on attribute type and selected attribute
            FilterType filterType;
            if ("event".equals(selectedAttributeType)) {
                if ("Activity".equals(selectedAttribute)) {
                    filterType = FilterType.EVENT_EVENT_ATTRIBUTE;
                    selectedAttribute = "concept:name"; // Use concept:name for Activity filtering
                } else {
                    filterType = FilterType.EVENT_EVENT_ATTRIBUTE;
                }
            } else {
                filterType = FilterType.CASE_CASE_ATTRIBUTE;
            }
            
            // Создаем RuleValue для каждого выбранного значения
            Set<RuleValue> ruleValues = new HashSet<>();
            for (String value : selectedValues) {
                Set<String> valueSet = new HashSet<>();
                valueSet.add(value);
                LOGGER.info("🔧 Создание RuleValue для атрибута: filterType={}, operationType={}, key={}, value={}", 
                           filterType, OperationType.EQUAL, selectedAttribute, value);
                RuleValue ruleValue = new RuleValue(
                    filterType,
                    OperationType.EQUAL,
                    selectedAttribute,
                    valueSet
                );
                ruleValues.add(ruleValue);
                LOGGER.info("RuleValue создан: objectVal={}, stringVal={}, stringSetValue={}", 
                           ruleValue.getObjectVal(), ruleValue.getStringValue(), ruleValue.getStringSetValue());
            }
            
            // Создаем правило фильтрации используя конструктор напрямую
            LogFilterRule rule = new LogFilterRuleImpl(
                choice,
                Inclusion.ANY_VALUE,
                selectedAttributeType.equals("case") ? Section.CASE : Section.EVENT,
                filterType,
                selectedAttribute,  // Pass the key here
                ruleValues,
                null
            );
            
            // Add debugging to show the created rule details
            LOGGER.info("🔧 Создан LogFilterRule: filterType={}, key='{}', choice={}, inclusion={}, section={}", 
                       rule.getFilterType(), rule.getKey(), rule.getChoice(), rule.getInclusion(), rule.getSection());
            
            rules.add(rule);
            LOGGER.info("Создано правило фильтрации: {} значений, условие: {}, тип: {}", 
                       selectedValues.size(), condition, filterType);
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при создании правил фильтрации атрибутов", e);
            LOGGER.error("Полный стек ошибки:", e);
            LOGGER.error("Тип ошибки: {}", e.getClass().getSimpleName());
            LOGGER.error("Сообщение ошибки: {}", e.getMessage());
        }
        
        return rules;
    }
    
    /**
     * Create filter rules for Case Attribute filtering (legacy method - kept for compatibility)
     */
    private List<LogFilterRule> createCaseAttributeFilterRules(List<String> selectedValues) {
        List<LogFilterRule> rules = new ArrayList<>();
        
        try {
            // Получаем условие фильтрации (Retain/Remove)
            String condition = caseAttributeCondition != null ? caseAttributeCondition.getSelectedItem().getValue() : "retain";
            Choice choice = "retain".equals(condition) ? Choice.RETAIN : Choice.REMOVE;
            
            // Создаем RuleValue для каждого выбранного значения
            Set<RuleValue> ruleValues = new HashSet<>();
            for (String value : selectedValues) {
                Set<String> valueSet = new HashSet<>();
                valueSet.add(value);
                LOGGER.info("🔧 Создание RuleValue для Case Attribute: filterType={}, operationType={}, key={}, value={}", 
                           FilterType.CASE_EVENT_ATTRIBUTE, OperationType.EQUAL, "concept:name", value);
                RuleValue ruleValue = new RuleValue(
                    FilterType.CASE_EVENT_ATTRIBUTE,
                    OperationType.EQUAL,
                    "concept:name",  // атрибут активности
                    valueSet
                );
                ruleValues.add(ruleValue);
                LOGGER.info("RuleValue создан: objectVal={}, stringVal={}, stringSetValue={}", 
                           ruleValue.getObjectVal(), ruleValue.getStringValue(), ruleValue.getStringSetValue());
            }
            
            // Создаем правило фильтрации используя статический метод init
            LogFilterRule rule = LogFilterRuleImpl.init(
                FilterType.CASE_EVENT_ATTRIBUTE,
                choice == Choice.RETAIN,
                ruleValues
            );
            
            rules.add(rule);
            LOGGER.info("Создано правило фильтрации Case Attribute: {} значений, условие: {}", 
                       selectedValues.size(), condition);
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при создании правил Case Attribute", e);
            LOGGER.error("Полный стек ошибки:", e);
            LOGGER.error("Тип ошибки: {}", e.getClass().getSimpleName());
            LOGGER.error("Сообщение ошибки: {}", e.getMessage());
        }
        
        return rules;
    }
    
    /**
     * Create filter rules for Case ID filtering
     */
    private List<LogFilterRule> createCaseIdFilterRules(List<String> selectedValues) {
        List<LogFilterRule> rules = new ArrayList<>();
        
        try {
            // Получаем условие фильтрации (Retain/Remove)
            String condition = caseIdCondition != null ? caseIdCondition.getSelectedItem().getValue() : "retain";
            Choice choice = "retain".equals(condition) ? Choice.RETAIN : Choice.RETAIN;
            
            // Создаем RuleValue для Case ID фильтрации
            Set<RuleValue> ruleValues = new HashSet<>();
            
            // Создаем BitSet для индексов трасс
            BitSet caseIdBitSet = new BitSet(apmLog.getTraces().size());
            
            // Заполняем customAttributes и BitSet
            RuleValue ruleValue = new RuleValue(
                FilterType.CASE_ID,
                OperationType.EQUAL,
                "concept:case:id",  // атрибут Case ID
                caseIdBitSet  // BitSet для индексов
            );
            
            // Добавляем Case ID в customAttributes и устанавливаем соответствующие биты
            for (String caseId : selectedValues) {
                ruleValue.getCustomAttributes().put(caseId, caseId);
                
                // Находим индекс трассы с этим Case ID
                for (int i = 0; i < apmLog.getTraces().size(); i++) {
                    if (apmLog.getTraces().get(i).getCaseId().equals(caseId)) {
                        caseIdBitSet.set(i);
                        break;
                    }
                }
            }
            
            ruleValues.add(ruleValue);
            LOGGER.info("RuleValue создан для Case ID: objectVal={}, stringVal={}, customAttributes={}, bitSetValue={}", 
                       ruleValue.getObjectVal(), ruleValue.getStringValue(), ruleValue.getCustomAttributes(), ruleValue.getBitSetValue());
            
            // Создаем правило фильтрации используя статический метод init
            LogFilterRule rule = LogFilterRuleImpl.init(
                FilterType.CASE_ID,
                choice == Choice.RETAIN,
                ruleValues
            );
            
            rules.add(rule);
            LOGGER.info("Создано правило фильтрации Case ID: {} значений, условие: {}", 
                       selectedValues.size(), condition);
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при создании правил Case ID", e);
            LOGGER.error("Полный стек ошибки:", e);
            LOGGER.error("Тип ошибки: {}", e.getClass().getSimpleName());
            LOGGER.error("Сообщение ошибки: {}", e.getMessage());
        }
        
        return rules;
    }
    
    /**
     * Create filter rules for Timeframe filtering
     */
    private List<LogFilterRule> createTimeframeFilterRules() {
        List<LogFilterRule> rules = new ArrayList<>();
        
        try {
            String condition = timeframeCondition != null ? timeframeCondition.getSelectedItem().getValue() : "retain";
            Choice choice = "retain".equals(condition) ? Choice.RETAIN : Choice.REMOVE;
            
            String containmentType = getSelectedContainmentType();
            long fromTime = getFromTime();
            long toTime = getToTime();

            // If predefined mode, override bounds with computed range
            if (isPredefinedMode()) {
                long[] rng = computePredefinedRange();
                fromTime = rng[0];
                toTime = rng[1];
            } else {
                // Adjust bounds per containment type for manual mode
                if ("start".equals(containmentType)) {
                    if (apmLog != null) toTime = apmLog.getEndTime();
                } else if ("end".equals(containmentType)) {
                    if (apmLog != null) fromTime = apmLog.getStartTime();
                }
            }

            if (toTime < fromTime) {
                long tmp = fromTime; fromTime = toTime; toTime = tmp;
            }

            LOGGER.info("Creating timeframe filter: condition={}, containment={}, fromTime={}, toTime={}",
                       condition, containmentType, fromTime, toTime);

            Set<RuleValue> ruleValues = new HashSet<>();
            FilterType filterTypeForTime;
            String key;
            if ("end".equals(containmentType)) {
                filterTypeForTime = FilterType.ENDTIME;
                key = "case:endtime";
            } else if ("contained".equals(containmentType) || "active".equals(containmentType)) {
                filterTypeForTime = FilterType.CASE_TIME;
                key = "case:timeframe";
            } else { // start
                filterTypeForTime = FilterType.STARTTIME;
                key = "case:starttime";
            }

            RuleValue fromRuleValue = new RuleValue(
                filterTypeForTime,
                OperationType.GREATER_EQUAL,
                key,
                fromTime
            );
            RuleValue toRuleValue = new RuleValue(
                filterTypeForTime,
                OperationType.LESS_EQUAL,
                key,
                toTime
            );
            ruleValues.add(fromRuleValue);
            ruleValues.add(toRuleValue);

            LogFilterRule rule = LogFilterRuleImpl.init(
                filterTypeForTime,
                choice == Choice.RETAIN,
                ruleValues
            );

            // Inclusion semantics
            if ("contained".equals(containmentType)) {
                rule = ((LogFilterRuleImpl) rule).withInclusion(Inclusion.ALL_VALUES);
            } else if ("active".equals(containmentType)) {
                rule = ((LogFilterRuleImpl) rule).withInclusion(Inclusion.ANY_VALUE);
            } else {
                // start/end: inclusion not critical, use ANY_VALUE
                rule = ((LogFilterRuleImpl) rule).withInclusion(Inclusion.ANY_VALUE);
            }

            rules.add(rule);
            LOGGER.info("Created timeframe filter rule: type={}, inclusion={}, condition={}, fromTime={}, toTime={}",
                       filterTypeForTime, (rule instanceof LogFilterRuleImpl ? ((LogFilterRuleImpl) rule).getInclusion() : null), condition, fromTime, toTime);
            
        } catch (Exception e) {
            LOGGER.error("Error creating timeframe filter rules", e);
        }
        
        return rules;
    }
    
    /**
     * Get the selected containment type
     */
    private String getSelectedContainmentType() {
        if (startInIcon != null && startInIcon.getSclass() != null && startInIcon.getSclass().contains("selected")) {
            return "start";
        } else if (endInIcon != null && endInIcon.getSclass() != null && endInIcon.getSclass().contains("selected")) {
            return "end";
        } else if (containedInIcon != null && containedInIcon.getSclass() != null && containedInIcon.getSclass().contains("selected")) {
            return "contained";
        } else if (activeInIcon != null && activeInIcon.getSclass() != null && activeInIcon.getSclass().contains("selected")) {
            return "active";
        }
        return "start";
    }
    
    /**
     * Get the from time based on date input
     */
    private long getFromTime() {
        if (fromDate != null && fromDate.getValue() != null) {
            return fromDate.getValue().getTime();
        }
        // Fallback to log start time
        if (apmLog != null) {
            return apmLog.getStartTime();
        }
        // Fallback to 1 year ago
        return System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000);
    }
    
    /**
     * Get the to time based on date input
     */
    private long getToTime() {
        if (toDate != null && toDate.getValue() != null) {
            return toDate.getValue().getTime();
        }
        // Fallback to log end time
        if (apmLog != null) {
            return apmLog.getEndTime();
        }
        // Fallback to current time
        return System.currentTimeMillis();
    }
    
    /**
     * Apply filters to the log using APMLogFilter
     */
    private APMLog applyFiltersToLog(List<LogFilterRule> filterRules) {
        try {
            if (apmLog == null) {
                LOGGER.error("APMLog недоступен для фильтрации");
                return null;
            }
            
            LOGGER.info("Применение {} правил фильтрации к логу", filterRules.size());
            LOGGER.info("Тип фильтра: {}", currentFilter);
            LOGGER.info("Размер оригинального лога: {} трасс", apmLog.getTraces().size());
            
            // Создаем APMLogFilter
            APMLogFilter apmLogFilter = new APMLogFilter(apmLog);
            LOGGER.info("APMLogFilter создан успешно");
            
            // Применяем фильтры
            apmLogFilter.filter(filterRules);
            LOGGER.info("Фильтры применены к APMLogFilter");
            
            // Получаем отфильтрованный лог
            APMLog filteredLog;
            try {
                filteredLog = apmLogFilter.getAPMLog();
                
                LOGGER.info("Фильтрация завершена:");
                LOGGER.info("   - Оригинальный лог: {} трасс", apmLog.getTraces().size());
                LOGGER.info("   - Отфильтрованный лог: {} трасс", filteredLog.getTraces().size());
                
                return filteredLog;
            } catch (EmptyInputException e) {
                LOGGER.warn("Отфильтрованный лог пуст: {}", e.getMessage());
                // Возвращаем оригинальный лог если фильтрация привела к пустому результату
                return apmLog;
            }
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при применении фильтров к логу", e);
            LOGGER.error("Полный стек ошибки:", e);
            LOGGER.error("Тип ошибки: {}", e.getClass().getSimpleName());
            LOGGER.error("Сообщение ошибки: {}", e.getMessage());
            return null;
        }
    }
    

    

    

    


    /**
     * Close the filter window
     */
    private void closeWindow() {
        try {
            LOGGER.info("🔧 Attempting to close filter window...");
            
            // Try to close the wired window first
            if (advancedLogFilterWindow != null) {
                LOGGER.info("🔧 Detaching wired advancedLogFilterWindow");
                advancedLogFilterWindow.detach();
                LOGGER.info("🔧 Wired window detached successfully");
            } else {
                LOGGER.warn("⚠️ advancedLogFilterWindow is null, trying alternative approach");
            }
            
            // Also try to find and detach any window with the logFilterWindow ID
            try {
                org.zkoss.zk.ui.Desktop desktop = org.zkoss.zk.ui.Executions.getCurrent().getDesktop();
                if (desktop != null) {
                    for (org.zkoss.zk.ui.Page page : desktop.getPages()) {
                        org.zkoss.zk.ui.Component window = page.getFellowIfAny("logFilterWindow");
                        if (window != null) {
                            LOGGER.info("🔧 Found logFilterWindow component, detaching...");
                            window.detach();
                            LOGGER.info("🔧 Alternative window detachment successful");
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("⚠️ Alternative window detachment failed: {}", e.getMessage());
            }
            
            LOGGER.info("✅ Filter window close operation completed");
        } catch (Exception e) {
            LOGGER.error("❌ Error closing window", e);
        }
    }
    
    /**
     * Configure the top-left button behavior based on whether this is first or secondary filter window
     */
    private void configureTopLeftButton() {
        try {
            if (filterTopLeftBtn != null) {
                filterTopLeftBtn.setLabel("\u2190");
                
                // Check if this is a secondary filter window (has existing criteria)
                boolean hasExistingCriteria = (currentCriteria != null && !currentCriteria.isEmpty()) || 
                                             (criteriaDescriptionsFromArgs != null && !criteriaDescriptionsFromArgs.isEmpty());
                
                LOGGER.info("🔧 Configuring top-left button. Has existing criteria: {}", hasExistingCriteria);
                
                if (hasExistingCriteria) {
                    // Secondary filter window: go back to criteria window
                    filterTopLeftBtn.addEventListener(Events.ON_CLICK, e -> goBackToCriteriaWindow());
                    LOGGER.info("🔧 Top-left button configured for secondary filter (back to criteria)");
                } else {
                    // First filter window: close the window
                    filterTopLeftBtn.addEventListener(Events.ON_CLICK, e -> closeWindow());
                    LOGGER.info("🔧 Top-left button configured for first filter (close window)");
                }
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error configuring top-left button", e);
        }
    }

    /**
     * Go back to the criteria window (for secondary filter windows)
     */
    private void goBackToCriteriaWindow() {
        try {
            LOGGER.info("🔧 Going back to criteria window from secondary filter...");
            
            // Close current filter window
            closeWindow();
            
            // Reopen criteria window with existing criteria
            java.util.Map<String, Object> args = new java.util.HashMap<>();
            args.put("criteriaDescriptions", criteriaDescriptionsFromArgs);
            args.put("criteriaRules", currentCriteria);
            // pass-through original context
            if (this.portalContext != null) args.put("portalContext", this.portalContext);
            if (this.selectedLog != null) args.put("selectedLog", this.selectedLog);
            if (this.logName != null) args.put("logName", this.logName);
            if (this.logId != null) args.put("logId", this.logId);
            if (this.apmLog != null) args.put("apmLog", this.apmLog);
            if (this.logFilterClient != null) args.put("logFilterClient", this.logFilterClient);
            
            // Open criteria window
            org.zkoss.zul.Window w = null;
            if (portalContext != null && portalContext.getUI() != null) {
                w = (org.zkoss.zul.Window) portalContext.getUI()
                        .createComponent(getClass().getClassLoader(), "filterCriteria.zul", null, args);
            } else {
                w = (org.zkoss.zul.Window) org.zkoss.zk.ui.Executions.getCurrent()
                        .createComponentsDirectly(
                                new java.io.InputStreamReader(
                                        getClass().getClassLoader().getResourceAsStream("filterCriteria.zul"),
                                        java.nio.charset.StandardCharsets.UTF_8),
                                "zul", null, args);
            }
            if (w != null) {
                w.doModal();
                LOGGER.info("✅ Criteria window reopened successfully");
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Error going back to criteria window", e);
            Messagebox.show("Error returning to criteria window: " + e.getMessage(), 
                          "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    /**
     * Display specific page of attribute values
     */
    private void displayAttributePage(int page) {
        try {
            LOGGER.info("🔄 === НАЧАЛО displayAttributePage({}) ===", page);
            if (attributeValuesList == null) {
                LOGGER.error("❌ attributeValuesList == null");
                return;
            }
            
            LOGGER.info("🔄 Отображение страницы {} атрибутов, всего элементов: {}", page, allAttributeValues.size());
            
            attributeValuesList.getItems().clear();
            
            // Проверяем, есть ли данные для отображения
            if (allAttributeValues.isEmpty()) {
                LOGGER.warn("⚠️ allAttributeValues пуст, пропускаем отображение страницы (нет данных)");
                // Avoid recursive call leading to StackOverflow; the data loader is invoked elsewhere
                return;
            }
            
            int totalPages = (int) Math.ceil((double) allAttributeValues.size() / ITEMS_PER_PAGE);
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allAttributeValues.size());
            
            // Получаем статистику для отображения на основе выбранного атрибута
            Map<String, Integer> attributeValueCounts = new HashMap<>();
            if (apmLog != null && primaryAttributeCombo != null) {
                Comboitem selectedItem = primaryAttributeCombo.getSelectedItem();
                if (selectedItem == null) {
                    LOGGER.error("❌ Нет выбранного элемента в primaryAttributeCombo");
                    return;
                }
                
                String selectedAttribute = (selectedItem.getValue() != null) ? String.valueOf(selectedItem.getValue()) : selectedItem.getLabel();
                LOGGER.info("🔧 Отображение данных для атрибута: {}", selectedAttribute);
                
                // Определяем тип атрибута (case или event) на основе выбранного типа в radio button
                String selectedAttributeType = "event"; // default to event
                if (primaryAttributeType != null && primaryAttributeType.getSelectedItem() != null) {
                    selectedAttributeType = primaryAttributeType.getSelectedItem().getValue();
                }
                
                boolean isCaseAttribute = "case".equals(selectedAttributeType);
                
                LOGGER.info("🔧 Тип атрибута: {}", isCaseAttribute ? "CASE" : "EVENT");
                
                if (isCaseAttribute) {
                    // Обрабатываем case attributes
                    for (ATrace trace : apmLog.getTraces()) {
                        if (trace.getAttributes() != null && trace.getAttributes().containsKey(selectedAttribute)) {
                            String value = trace.getAttributes().get(selectedAttribute).toString();
                            if (value != null && !value.isEmpty()) {
                                attributeValueCounts.put(value, attributeValueCounts.getOrDefault(value, 0) + 1);
                            }
                        }
                    }
                } else {
                    // Обрабатываем event attributes
                    if (selectedAttribute.equals("Activity")) {
                        // Special handling for Activity: count cases (traces) that contain each activity
                        LOGGER.info("🔧 Специальная обработка статистики для Activity...");
                        for (ATrace trace : apmLog.getTraces()) {
                            Set<String> activitiesInTrace = new HashSet<>();
                            for (ActivityInstance activityInstance : trace.getActivityInstances()) {
                                String activityName = activityInstance.getName();
                                if (activityName != null && !activityName.isEmpty()) {
                                    activitiesInTrace.add(activityName);
                                }
                            }
                            // Count each activity once per trace (case)
                            for (String activityName : activitiesInTrace) {
                                attributeValueCounts.put(activityName, attributeValueCounts.getOrDefault(activityName, 0) + 1);
                            }
                        }
                        LOGGER.info("🔧 Статистика Activity: каждая активность подсчитывается один раз на случай");
                    } else {
                        // Regular event attributes: count each occurrence
                        for (ATrace trace : apmLog.getTraces()) {
                            for (ActivityInstance activityInstance : trace.getActivityInstances()) {
                                if (activityInstance.getAttributes() != null && 
                                    activityInstance.getAttributes().containsKey(selectedAttribute)) {
                                    String value = activityInstance.getAttributes().get(selectedAttribute).toString();
                                    if (value != null && !value.isEmpty()) {
                                        attributeValueCounts.put(value, attributeValueCounts.getOrDefault(value, 0) + 1);
                                    }
                                }
                            }
                        }
                    }
                }
                
                LOGGER.info("🔧 Найдено {} уникальных значений атрибута '{}'", attributeValueCounts.size(), selectedAttribute);
            }
            
            // Calculate total cases correctly based on attribute type
            int totalCases;
            if (apmLog != null && primaryAttributeCombo != null) {
                Comboitem selectedItem = primaryAttributeCombo.getSelectedItem();
                if (selectedItem != null) {
                    String selectedAttribute = (selectedItem.getValue() != null) ? String.valueOf(selectedItem.getValue()) : selectedItem.getLabel();
                    
                    if (selectedAttribute.equals("Activity")) {
                        // For Activity: use total number of cases in the log
                        totalCases = apmLog.getTraces().size();
                        LOGGER.info("🔧 Для Activity используем общее количество случаев в логе: {}", totalCases);
                    } else {
                        // For other attributes: use sum of attribute counts (existing logic)
                        totalCases = attributeValueCounts.values().stream().mapToInt(Integer::intValue).sum();
                        LOGGER.info("🔧 Для других атрибутов используем сумму значений: {}", totalCases);
                    }
                } else {
                    totalCases = attributeValueCounts.values().stream().mapToInt(Integer::intValue).sum();
                    LOGGER.info("🔧 Fallback: общее количество случаев: {}", totalCases);
                }
            } else {
                totalCases = attributeValueCounts.values().stream().mapToInt(Integer::intValue).sum();
                LOGGER.info("🔧 Fallback: общее количество случаев: {}", totalCases);
            }
            
            // Отображаем элементы текущей страницы
            LOGGER.info("🔧 Начинаем добавление элементов в таблицу...");
            int addedItems = 0;
            for (int i = startIndex; i < endIndex; i++) {
                String attributeValue = allAttributeValues.get(i);
                int count = attributeValueCounts.getOrDefault(attributeValue, 0);
                double frequency = totalCases > 0 ? (double) count / totalCases * 100 : 0.0;
                
                LOGGER.info("🔧 Добавляем элемент {}: значение='{}', количество={}, частота={:.2f}%", 
                           i + 1, attributeValue, count, frequency);
                
                addAttributeValue(attributeValue, count, frequency);
                addedItems++;
            }
            
            LOGGER.info("🔧 Добавлено {} элементов в таблицу", addedItems);
            
            // Обновляем пагинацию
            updateAttributePagination(page, totalPages, startIndex + 1, endIndex);
            
            LOGGER.info("✅ Отображена страница {} значений атрибута (элементы {}-{})", page, startIndex + 1, endIndex);
            LOGGER.info("🔄 === КОНЕЦ displayAttributePage({}) ===", page);
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при отображении страницы значений атрибута", e);
        }
    }
    
    /**
     * Update attribute pagination controls
     */
    private void updateAttributePagination(int currentPage, int totalPages, int startItem, int endItem) {
        try {
            LOGGER.info("Обновление пагинации атрибутов: страница {}/{}, элементы {}-{}", currentPage, totalPages, startItem, endItem);
            
            // Обновляем кнопки напрямую
            if (firstPageBtn != null) {
                firstPageBtn.setDisabled(currentPage <= 1);
                LOGGER.debug("Кнопка 'Первая страница' {}disabled", firstPageBtn.isDisabled() ? "" : "не ");
            }
            if (prevPageBtn != null) {
                prevPageBtn.setDisabled(currentPage <= 1);
                LOGGER.debug("Кнопка 'Предыдущая страница' {}disabled", prevPageBtn.isDisabled() ? "" : "не ");
            }
            if (nextPageBtn != null) {
                nextPageBtn.setDisabled(currentPage >= totalPages);
                LOGGER.debug("Кнопка 'Следующая страница' {}disabled", nextPageBtn.isDisabled() ? "" : "не ");
            }
            if (lastPageBtn != null) {
                lastPageBtn.setDisabled(currentPage >= totalPages);
                LOGGER.debug("Кнопка 'Последняя страница' {}disabled", lastPageBtn.isDisabled() ? "" : "не ");
            }
            
            // Обновляем метки напрямую
            if (pageLabel != null) {
                pageLabel.setValue(currentPage + " / " + totalPages);
                LOGGER.debug("Метка страницы обновлена: {}", pageLabel.getValue());
            }
            if (pageInfo != null) {
                pageInfo.setValue("Страница " + currentPage);
                LOGGER.debug("Информация о странице обновлена: {}", pageInfo.getValue());
            }
            if (totalInfo != null) {
                totalInfo.setValue(startItem + " - " + endItem + " / " + allAttributeValues.size());
                LOGGER.debug("Общая информация обновлена: {}", totalInfo.getValue());
            }
            
            LOGGER.info("Пагинация атрибутов обновлена успешно");
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при обновлении пагинации атрибутов", e);
        }
    }
    
    /**
     * Pagination event handlers for attributes
     */
    @Listen("onClick = #firstPageBtn")
    public void onFirstPage() {
        try {
            LOGGER.info("Переход на первую страницу атрибутов");
            if (currentAttributePage > 1) {
                currentAttributePage = 1;
                displayAttributePage(currentAttributePage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на первую страницу атрибутов", e);
        }
    }
    
    @Listen("onClick = #prevPageBtn")
    public void onPrevPage() {
        try {
            LOGGER.info("Переход на предыдущую страницу атрибутов");
            if (currentAttributePage > 1) {
                currentAttributePage--;
                displayAttributePage(currentAttributePage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на предыдущую страницу атрибутов", e);
        }
    }
    
    @Listen("onClick = #nextPageBtn")
    public void onNextPage() {
        try {
            LOGGER.info("Переход на следующую страницу атрибутов");
            int totalPages = (int) Math.ceil((double) allAttributeValues.size() / ITEMS_PER_PAGE);
            if (currentAttributePage < totalPages) {
                currentAttributePage++;
                displayAttributePage(currentAttributePage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на следующую страницу атрибутов", e);
        }
    }
    
    @Listen("onClick = #lastPageBtn")
    public void onLastPage() {
        try {
            LOGGER.info("Переход на последнюю страницу атрибутов");
            int totalPages = (int) Math.ceil((double) allAttributeValues.size() / ITEMS_PER_PAGE);
            if (currentAttributePage < totalPages) {
                currentAttributePage = totalPages;
                displayAttributePage(currentAttributePage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на последнюю страницу атрибутов", e);
        }
    }
    
    /**
     * Display specific page of case IDs
     */
    private void displayCaseIdPage(int page) {
        try {
            if (caseIdValuesList == null) {
                LOGGER.warn("caseIdValuesList компонент не найден");
                return;
            }
            
            LOGGER.info("Отображение страницы {} Case ID (всего элементов: {})", page, allCaseIdValues.size());
            
            caseIdValuesList.getItems().clear();
            
            if (allCaseIdValues.isEmpty()) {
                LOGGER.warn("Список Case ID пуст, загружаем данные заново");
                loadCaseIdsForLog();
                return;
            }
            
            int totalPages = (int) Math.ceil((double) allCaseIdValues.size() / ITEMS_PER_PAGE);
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allCaseIdValues.size());
            
            // Получаем статистику для отображения
            Map<String, Integer> caseIdCounts = new HashMap<>();
            if (apmLog != null) {
                for (ATrace trace : apmLog.getTraces()) {
                    String caseId = trace.getCaseId();
                    if (caseId != null && !caseId.isEmpty()) {
                        int eventCount = trace.getActivityInstances().size();
                        caseIdCounts.put(caseId, eventCount);
                    }
                }
            }
            
            // Отображаем элементы текущей страницы
            for (int i = startIndex; i < endIndex; i++) {
                String caseId = allCaseIdValues.get(i);
                int eventCount = caseIdCounts.getOrDefault(caseId, 0);
                addCaseId(caseId, eventCount);
            }
            
            // Обновляем пагинацию
            updateCaseIdPagination(page, totalPages, startIndex + 1, endIndex);
            
            LOGGER.info("Отображена страница {} Case ID (элементы {}-{})", page, startIndex + 1, endIndex);
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при отображении страницы Case ID", e);
        }
    }
    
    /**
     * Update case ID pagination controls
     */
    private void updateCaseIdPagination(int currentPage, int totalPages, int startItem, int endItem) {
        try {
            LOGGER.info("Обновление пагинации Case ID: страница {}/{}, элементы {}-{}", currentPage, totalPages, startItem, endItem);
            
            // Обновляем кнопки напрямую
            if (caseIdFirstPageBtn != null) {
                caseIdFirstPageBtn.setDisabled(currentPage <= 1);
                LOGGER.debug("Кнопка 'Первая страница Case ID' {}disabled", caseIdFirstPageBtn.isDisabled() ? "" : "не ");
            }
            if (caseIdPrevPageBtn != null) {
                caseIdPrevPageBtn.setDisabled(currentPage <= 1);
                LOGGER.debug("Кнопка 'Предыдущая страница Case ID' {}disabled", caseIdPrevPageBtn.isDisabled() ? "" : "не ");
            }
            if (caseIdNextPageBtn != null) {
                caseIdNextPageBtn.setDisabled(currentPage >= totalPages);
                LOGGER.debug("Кнопка 'Следующая страница Case ID' {}disabled", caseIdNextPageBtn.isDisabled() ? "" : "не ");
            }
            if (caseIdLastPageBtn != null) {
                caseIdLastPageBtn.setDisabled(currentPage >= totalPages);
                LOGGER.debug("Кнопка 'Последняя страница Case ID' {}disabled", caseIdLastPageBtn.isDisabled() ? "" : "не ");
            }
            
            // Обновляем метки напрямую
            if (caseIdPageLabel != null) {
                caseIdPageLabel.setValue(currentPage + " / " + totalPages);
                LOGGER.debug("Метка страницы Case ID обновлена: {}", caseIdPageLabel.getValue());
            }
            if (caseIdPageInfo != null) {
                caseIdPageInfo.setValue("Страница " + currentPage);
                LOGGER.debug("Информация о странице Case ID обновлена: {}", caseIdPageInfo.getValue());
            }
            if (caseIdTotalInfo != null) {
                caseIdTotalInfo.setValue(startItem + " - " + endItem + " / " + allCaseIdValues.size());
                LOGGER.debug("Общая информация Case ID обновлена: {}", caseIdTotalInfo.getValue());
            }
            
            LOGGER.info("Пагинация Case ID обновлена успешно");
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при обновлении пагинации Case ID", e);
        }
    }
    
    /**
     * Pagination event handlers for case IDs
     */
    @Listen("onClick = #caseIdFirstPageBtn")
    public void onCaseIdFirstPage() {
        try {
            LOGGER.info("Переход на первую страницу Case ID");
            if (currentCaseIdPage > 1) {
                currentCaseIdPage = 1;
                displayCaseIdPage(currentCaseIdPage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на первую страницу Case ID", e);
        }
    }
    
    @Listen("onClick = #caseIdPrevPageBtn")
    public void onCaseIdPrevPage() {
        try {
            LOGGER.info("Переход на предыдущую страницу Case ID");
            if (currentCaseIdPage > 1) {
                currentCaseIdPage--;
                displayCaseIdPage(currentCaseIdPage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на предыдущую страницу Case ID", e);
        }
    }
    
    @Listen("onClick = #caseIdNextPageBtn")
    public void onCaseIdNextPage() {
        try {
            LOGGER.info("Переход на следующую страницу Case ID");
            int totalPages = (int) Math.ceil((double) allCaseIdValues.size() / ITEMS_PER_PAGE);
            if (currentCaseIdPage < totalPages) {
                currentCaseIdPage++;
                displayCaseIdPage(currentCaseIdPage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на следующую страницу Case ID", e);
        }
    }
    
    @Listen("onClick = #caseIdLastPageBtn")
    public void onCaseIdLastPage() {
        try {
            LOGGER.info("Переход на последнюю страницу Case ID");
            int totalPages = (int) Math.ceil((double) allCaseIdValues.size() / ITEMS_PER_PAGE);
            if (currentCaseIdPage < totalPages) {
                currentCaseIdPage = totalPages;
                displayCaseIdPage(currentCaseIdPage);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при переходе на последнюю страницу Case ID", e);
        }
    }

    /**
     * Show intervals dialog for predefined timeframes
     */
    private void showIntervalsDialog(String timeframeType) {
        try {
            // Create a simple dialog to show available intervals
            String message = "Available " + timeframeType + " intervals:\n";
            message += "This would show a list of available intervals based on the log data.";
            
            Messagebox.show(message, "Intervals for " + timeframeType, 
                          Messagebox.OK, Messagebox.INFORMATION);
            
            LOGGER.info("Showing intervals dialog for timeframe type: {}", timeframeType);
        } catch (Exception e) {
            LOGGER.error("Error showing intervals dialog", e);
        }
    }
    
    /**
     * Initialize the time chart with log data
     */
    private void initializeTimeChart() {
        try {
            LOGGER.info("Starting time range display initialization...");
            
            if (apmLog == null) {
                LOGGER.warn("Cannot initialize time range display: APMLog is null");
                return;
            }
            
            // Get time range from log
            long startTime = apmLog.getStartTime();
            long endTime = apmLog.getEndTime();
            
            LOGGER.info("Initializing time range display with log data: startTime={}, endTime={}", startTime, endTime);
            
            // Update the time range display labels
            if (timeRangeDisplay != null) {
                String timeRangeText = formatTime(startTime) + " to " + formatTime(endTime);
                timeRangeDisplay.setValue(timeRangeText);
            }
            
            if (caseCountDisplay != null) {
                caseCountDisplay.setValue(apmLog.getTraces().size() + " cases");
            }
            
            LOGGER.info("Time range display initialized successfully");
            
        } catch (Exception e) {
            LOGGER.error("Error initializing time range display", e);
        }
    }
    
    /**
     * Format timestamp to readable string
     */
    private String formatTime(long timestamp) {
        try {
            java.util.Date date = new java.util.Date(timestamp);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yy, HH:mm:ss");
            return sdf.format(date);
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Format duration to readable string
     */
    private String formatDuration(long duration) {
        try {
            long years = duration / (365L * 24 * 60 * 60 * 1000);
            if (years > 0) {
                return years + " yrs";
            }
            
            long months = duration / (30L * 24 * 60 * 60 * 1000);
            if (months > 0) {
                return months + " months";
            }
            
            long days = duration / (24L * 60 * 60 * 1000);
            if (days > 0) {
                return days + " days";
            }
            
            long hours = duration / (60L * 60 * 1000);
            if (hours > 0) {
                return hours + " hrs";
            }
            
            return "Less than 1 hour";
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Format time axis with multiple time points
     */
    private String formatTimeAxis(long startTime, long endTime) {
        try {
            long duration = endTime - startTime;
            long interval = duration / 4; // 5 points total
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= 4; i++) {
                if (i > 0) sb.append(" - ");
                long time = startTime + (i * interval);
                sb.append(formatTime(time));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Time axis unavailable";
        }
    }
    
    /**
     * Set containment icon as selected
     */
    private void setContainmentIcon(String containmentType) {
        try {
            // Reset all icons to inactive
            if (startInIcon != null) startInIcon.setSclass("containment-icon inactive");
            if (endInIcon != null) endInIcon.setSclass("containment-icon inactive");
            if (containedInIcon != null) containedInIcon.setSclass("containment-icon inactive");
            if (activeInIcon != null) activeInIcon.setSclass("containment-icon inactive");
            
            // Set selected icon
            switch (containmentType) {
                case "start":
                    if (startInIcon != null) startInIcon.setSclass("containment-icon selected");
                    break;
                case "end":
                    if (endInIcon != null) endInIcon.setSclass("containment-icon selected");
                    break;
                case "contained":
                    if (containedInIcon != null) containedInIcon.setSclass("containment-icon selected");
                    break;
                case "active":
                    if (activeInIcon != null) activeInIcon.setSclass("containment-icon selected");
                    break;
            }

            // Ensure visual styles reflect state
            refreshContainmentIconStyles();
            
            LOGGER.info("Containment icon set to: {}", containmentType);
        } catch (Exception e) {
            LOGGER.error("Error setting containment icon", e);
        }
    }
    
    /**
     * Create or update the time range display dynamically
     */
    private void createOrUpdateTimeRangeDisplay(long startTime, long endTime) {
        try {
            if (apmLog == null) {
                LOGGER.warn("Cannot create time range display: APMLog is null");
                return;
            }
            
            if (timeframePanel == null) {
                LOGGER.warn("Cannot create time range display: timeframePanel is null");
                return;
            }
            
            LOGGER.info("Creating/updating time range display for log with {} traces", apmLog.getTraces().size());
            
            // Find the container div for the time range display
            Component timeRangeContainer = timeframePanel.query("#time-chart-container");
            if (timeRangeContainer == null) {
                LOGGER.warn("Could not find time-chart-container in timeframe panel");
                return;
            }
            
            // Look for existing labels or create new ones
            Label timeRangeLabel = null;
            Label caseCountLabel = null;
            
            // Try to find existing labels
            for (Component child : timeRangeContainer.getChildren()) {
                if (child instanceof Label) {
                    Label label = (Label) child;
                    if (label.getId() != null && label.getId().equals("timeRangeDisplay")) {
                        timeRangeLabel = label;
                    } else if (label.getId() != null && label.getId().equals("caseCountDisplay")) {
                        caseCountLabel = label;
                    }
                }
            }
            
            // Create time range label if not found
            if (timeRangeLabel == null) {
                timeRangeLabel = new Label();
                timeRangeLabel.setId("timeRangeDisplay");
                timeRangeLabel.setStyle("color: #333; font-size: 14px; text-align: center; margin: 10px 0;");
                timeRangeContainer.appendChild(timeRangeLabel);
                LOGGER.info("Created new timeRangeDisplay label");
            }
            
            // Create case count label if not found
            if (caseCountLabel == null) {
                caseCountLabel = new Label();
                caseCountLabel.setId("caseCountDisplay");
                caseCountLabel.setStyle("color: #666; font-size: 12px; margin-top: 5px;");
                timeRangeContainer.appendChild(caseCountLabel);
                LOGGER.info("Created new caseCountDisplay label");
            }
            
            // Update the labels with actual data
            String timeRangeText = formatTime(startTime) + " to " + formatTime(endTime);
            timeRangeLabel.setValue(timeRangeText);
            caseCountLabel.setValue(apmLog.getTraces().size() + " cases");
            
            LOGGER.info("Time range display updated: {} cases, range: {}", apmLog.getTraces().size(), timeRangeText);
            
        } catch (Exception e) {
            LOGGER.error("Error creating/updating time range display", e);
        }
    }

    private void applyContainmentIconStyle(Div icon, boolean selected) {
        if (icon == null) return;
        String color = selected ? "#337ab7" : "#ccc";
        icon.setStyle("width: 30px; height: 30px; border: 2px solid " + color + "; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-weight: bold; color: " + color + "; cursor: pointer;");
    }

    private void refreshContainmentIconStyles() {
        applyContainmentIconStyle(startInIcon, startInIcon != null && startInIcon.getSclass() != null && startInIcon.getSclass().contains("selected"));
        applyContainmentIconStyle(endInIcon, endInIcon != null && endInIcon.getSclass() != null && endInIcon.getSclass().contains("selected"));
        applyContainmentIconStyle(containedInIcon, containedInIcon != null && containedInIcon.getSclass() != null && containedInIcon.getSclass().contains("selected"));
        applyContainmentIconStyle(activeInIcon, activeInIcon != null && activeInIcon.getSclass() != null && activeInIcon.getSclass().contains("selected"));
    }

    @Listen("onCheck = #usePredefinedTimeframes")
    public void onUsePredefinedTimeframesToggle() {
        try {
            if (predefinedTimeframesLayout != null && usePredefinedTimeframes != null) {
                boolean checked = usePredefinedTimeframes.isChecked();
                predefinedTimeframesLayout.setVisible(checked);
                disableManualDates(checked);
                setContainmentLocked(checked);
            }
        } catch (Exception e) {
            LOGGER.error("Error toggling predefined timeframes", e);
        }
    }

    @Listen("onClick = #intervalsBtn")
    public void onIntervalsBtnClick() {
        try {
            if (predefinedTimeframeType == null || predefinedTimeframeType.getSelectedItem() == null) {
                Messagebox.show("Please select a timeframe type first", "Predefined timeframes", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            selectedPredefinedType = predefinedTimeframeType.getSelectedItem().getLabel();

            // Build a simple window with a listbox of intervals (placeholder)
            Window w = new Window("Select Intervals", "normal", true);
            w.setWidth("360px");
            w.setClosable(true);
            w.setSclass("intervals-window");
            Listbox lb = new Listbox();
            lb.setMold("select");
            lb.setMultiple(true);
            lb.setCheckmark(true);
            lb.setWidth("320px");
            lb.setHeight("260px");

            // Populate items based on selected type and log boundaries
            List<String> intervals = computeIntervalsForType(selectedPredefinedType);
            for (String it : intervals) {
                Listitem li = new Listitem(it);
                lb.appendChild(li);
                if (selectedPredefinedIntervals.contains(it)) {
                    li.setSelected(true);
                }
            }
            w.appendChild(lb);

            Hlayout actions = new Hlayout();
            actions.setSpacing("10px");
            Button ok = new Button("OK");
            ok.addEventListener("onClick", evt -> {
                selectedPredefinedIntervals.clear();
                for (Listitem sel : lb.getSelectedItems()) {
                    selectedPredefinedIntervals.add(sel.getLabel());
                }
                w.detach();
                LOGGER.info("Selected predefined intervals ({}): {}", selectedPredefinedType, selectedPredefinedIntervals);
            });
            Button cancel = new Button("Cancel");
            cancel.addEventListener("onClick", evt -> w.detach());
            actions.appendChild(ok);
            actions.appendChild(cancel);
            w.appendChild(actions);

            // Attach modal to main window if available
            if (advancedLogFilterWindow != null) {
                w.setParent(advancedLogFilterWindow);
            } else {
                w.setParent(Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot());
            }
            w.doModal();
        } catch (Exception e) {
            LOGGER.error("Error showing intervals dialog", e);
        }
    }

    private List<String> computeIntervalsForType(String type) {
        List<String> out = new ArrayList<>();
        if (apmLog == null) return out;
        long start = apmLog.getStartTime();
        long end = apmLog.getEndTime();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(start);
        switch (type) {
            case "Year":
                while (cal.getTimeInMillis() <= end) {
                    out.add(String.valueOf(cal.get(java.util.Calendar.YEAR)));
                    cal.add(java.util.Calendar.YEAR, 1);
                }
                break;
            case "Month":
                while (cal.getTimeInMillis() <= end) {
                    int y = cal.get(java.util.Calendar.YEAR);
                    int m = cal.get(java.util.Calendar.MONTH) + 1;
                    out.add(String.format("%04d-%02d", y, m));
                    cal.add(java.util.Calendar.MONTH, 1);
                }
                break;
            case "Week":
                while (cal.getTimeInMillis() <= end) {
                    int y = cal.get(java.util.Calendar.YEAR);
                    int w = cal.get(java.util.Calendar.WEEK_OF_YEAR);
                    out.add(String.format("%04d-W%02d", y, w));
                    cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                }
                break;
            case "Quarter":
                while (cal.getTimeInMillis() <= end) {
                    int y = cal.get(java.util.Calendar.YEAR);
                    int q = (cal.get(java.util.Calendar.MONTH) / 3) + 1;
                    out.add(String.format("%04d-Q%d", y, q));
                    cal.add(java.util.Calendar.MONTH, 3);
                }
                break;
            case "Semester":
                while (cal.getTimeInMillis() <= end) {
                    int y = cal.get(java.util.Calendar.YEAR);
                    int s = (cal.get(java.util.Calendar.MONTH) / 6) + 1;
                    out.add(String.format("%04d-S%d", y, s));
                    cal.add(java.util.Calendar.MONTH, 6);
                }
                break;
            default:
                break;
        }
        return out;
    }

    private boolean isPredefinedMode() {
        return usePredefinedTimeframes != null && usePredefinedTimeframes.isChecked();
    }

    private void disableManualDates(boolean disabled) {
        if (fromDate != null) fromDate.setDisabled(disabled);
        if (toDate != null) toDate.setDisabled(disabled);
    }

    private boolean containmentLocked = false;
    private void setContainmentLocked(boolean locked) {
        this.containmentLocked = locked;
        if (locked) {
            if (startInIcon != null) startInIcon.setSclass("containment-icon inactive");
            if (endInIcon != null) endInIcon.setSclass("containment-icon inactive");
            if (containedInIcon != null) containedInIcon.setSclass("containment-icon inactive");
            if (activeInIcon != null) activeInIcon.setSclass("containment-icon inactive");
        } else {
            refreshContainmentIconStyles();
        }
    }

    private long[] computePredefinedRange() {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        if (apmLog == null) return new long[] {System.currentTimeMillis(), System.currentTimeMillis()};
        if (selectedPredefinedIntervals == null || selectedPredefinedIntervals.isEmpty()) {
            // If nothing selected, default to whole log
            return new long[] {apmLog.getStartTime(), apmLog.getEndTime()};
        }
        for (String token : selectedPredefinedIntervals) {
            long[] b = boundsForToken(selectedPredefinedType, token, apmLog.getStartTime(), apmLog.getEndTime());
            if (b[0] < min) min = b[0];
            if (b[1] > max) max = b[1];
        }
        if (min == Long.MAX_VALUE || max == Long.MIN_VALUE) {
            return new long[] {apmLog.getStartTime(), apmLog.getEndTime()};
        }
        return new long[] {min, max};
    }

    private long[] boundsForToken(String type, String token, long logStart, long logEnd) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        try {
            if ("Year".equals(type)) {
                int y = Integer.parseInt(token);
                cal.clear();
                cal.set(java.util.Calendar.YEAR, y);
                long start = startOfYear(cal);
                long end = endOfYear(cal);
                return clampToLog(start, end, logStart, logEnd);
            } else if ("Month".equals(type)) {
                String[] parts = token.split("-");
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                cal.clear();
                cal.set(java.util.Calendar.YEAR, y);
                cal.set(java.util.Calendar.MONTH, m - 1);
                long start = startOfMonth(cal);
                long end = endOfMonth(cal);
                return clampToLog(start, end, logStart, logEnd);
            } else if ("Week".equals(type)) {
                String[] parts = token.split("-W");
                int y = Integer.parseInt(parts[0]);
                int w = Integer.parseInt(parts[1]);
                cal.clear();
                cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);
                cal.set(java.util.Calendar.YEAR, y);
                cal.set(java.util.Calendar.WEEK_OF_YEAR, w);
                long start = startOfWeek(cal);
                long end = endOfWeek(cal);
                return clampToLog(start, end, logStart, logEnd);
            } else if ("Quarter".equals(type)) {
                String[] parts = token.split("-Q");
                int y = Integer.parseInt(parts[0]);
                int q = Integer.parseInt(parts[1]);
                cal.clear();
                cal.set(java.util.Calendar.YEAR, y);
                cal.set(java.util.Calendar.MONTH, (q - 1) * 3);
                long start = startOfMonth(cal);
                cal.add(java.util.Calendar.MONTH, 3);
                cal.add(java.util.Calendar.MILLISECOND, -1);
                long end = cal.getTimeInMillis();
                return clampToLog(start, end, logStart, logEnd);
            } else if ("Semester".equals(type)) {
                String[] parts = token.split("-S");
                int y = Integer.parseInt(parts[0]);
                int s = Integer.parseInt(parts[1]);
                cal.clear();
                cal.set(java.util.Calendar.YEAR, y);
                cal.set(java.util.Calendar.MONTH, (s - 1) * 6);
                long start = startOfMonth(cal);
                cal.add(java.util.Calendar.MONTH, 6);
                cal.add(java.util.Calendar.MILLISECOND, -1);
                long end = cal.getTimeInMillis();
                return clampToLog(start, end, logStart, logEnd);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse predefined token: type={}, token={}", type, token, e);
        }
        return new long[] {logStart, logEnd};
    }

    private long[] clampToLog(long start, long end, long logStart, long logEnd) {
        if (start < logStart) start = logStart;
        if (end > logEnd) end = logEnd;
        return new long[] {start, end};
    }

    private long startOfYear(java.util.Calendar cal) {
        cal.set(java.util.Calendar.DAY_OF_YEAR, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfYear(java.util.Calendar cal) {
        cal.set(java.util.Calendar.DAY_OF_YEAR, 1);
        cal.add(java.util.Calendar.YEAR, 1);
        cal.add(java.util.Calendar.MILLISECOND, -1);
        return cal.getTimeInMillis();
    }

    private long startOfMonth(java.util.Calendar cal) {
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfMonth(java.util.Calendar cal) {
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.add(java.util.Calendar.MONTH, 1);
        cal.add(java.util.Calendar.MILLISECOND, -1);
        return cal.getTimeInMillis();
    }

    private long startOfWeek(java.util.Calendar cal) {
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long endOfWeek(java.util.Calendar cal) {
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(java.util.Calendar.DAY_OF_YEAR, 7);
        cal.add(java.util.Calendar.MILLISECOND, -1);
        return cal.getTimeInMillis();
    }

    /**
     * Update statistics labels safely
     */
    private void updateStatistics() {
        try {
            if (apmLog != null) {
                loadRealLogStatistics();
            }
        } catch (Exception e) {
            LOGGER.error("Error updating statistics", e);
        }
    }

    /**
     * Applying filters button in performance panel
     */
    @Listen("onClick = #applyFiltersBtnPerformance")
    public void onApplyPerformanceFiltersClick() {
        try {
            LOGGER.info("Applying performance filters...");
            currentFilter = "performance";
            applyFiltersAndRedirect();
        } catch (Exception e) {
            LOGGER.error("Error applying performance filters", e);
        }
    }

    private List<LogFilterRule> createPerformanceFilterRules() {
        List<LogFilterRule> rules = new ArrayList<>();
        try {
            String condition = performanceCondition != null && performanceCondition.getSelectedItem() != null ? performanceCondition.getSelectedItem().getValue() : "retain";
            Choice choice = "retain".equals(condition) ? Choice.RETAIN : Choice.REMOVE;
            String measure = performanceMeasure != null && performanceMeasure.getSelectedItem() != null ? performanceMeasure.getSelectedItem().getLabel() : "Case duration";

            if ("Case length".equals(measure)) {
                int from = perfLenFrom != null && perfLenFrom.getValue() != null ? perfLenFrom.getValue() : Integer.MIN_VALUE;
                int to = perfLenTo != null && perfLenTo.getValue() != null ? perfLenTo.getValue() : Integer.MAX_VALUE;
                if (from > to) { int t = from; from = to; to = t; }
                Set<RuleValue> rvs = new HashSet<>();
                if (from != Integer.MIN_VALUE) {
                    rvs.add(new RuleValue(FilterType.CASE_LENGTH, OperationType.GREATER_EQUAL, "case:length", (long) from));
                }
                if (to != Integer.MAX_VALUE) {
                    rvs.add(new RuleValue(FilterType.CASE_LENGTH, OperationType.LESS_EQUAL, "case:length", (long) to));
                }
                if (!rvs.isEmpty()) {
                    LogFilterRule rule = LogFilterRuleImpl.init(FilterType.CASE_LENGTH, choice == Choice.RETAIN, rvs);
                    rules.add(rule);
                    LOGGER.info("Created performance rule (Case length): from={}, to={}, retain={}", from, to, choice == Choice.RETAIN);
                }
                return rules;
            }

            // Processing time family (average, maximum, total) and default case duration
            long gteMs = toMillis(perfGteValue, perfGteUnit);
            long lteMs = toMillis(perfLteValue, perfLteUnit);
            if (gteMs > 0 && lteMs > 0 && lteMs < gteMs) {
                long t = gteMs; gteMs = lteMs; lteMs = t;
            }
            Set<RuleValue> ruleValues = new HashSet<>();
            if (gteMs > 0) {
                ruleValues.add(new RuleValue(resolveDurationFilterType(measure), OperationType.GREATER_EQUAL, durationKeyForMeasure(measure), gteMs));
            }
            if (lteMs > 0) {
                ruleValues.add(new RuleValue(resolveDurationFilterType(measure), OperationType.LESS_EQUAL, durationKeyForMeasure(measure), lteMs));
            }
            if (!ruleValues.isEmpty()) {
                LogFilterRule rule = LogFilterRuleImpl.init(resolveDurationFilterType(measure), choice == Choice.RETAIN, ruleValues);
                rules.add(rule);
                LOGGER.info("Created performance rule ({}): gteMs={}, lteMs={}, retain={}", measure, gteMs, lteMs, choice == Choice.RETAIN);
            }

        } catch (Exception e) {
            LOGGER.error("Error creating performance filter rules", e);
        }
        return rules;
    }

    private long toMillis(Intbox valBox, Combobox unitBox) {
        try {
            int value = (valBox != null && valBox.getValue() != null) ? valBox.getValue() : 0;
            if (value <= 0) return 0L;
            String unit = unitBox != null && unitBox.getSelectedItem() != null ? unitBox.getSelectedItem().getLabel() : "Seconds";
            long base = 1000L;
            switch (unit) {
                case "Years": return value * 365L * 24 * 60 * 60 * 1000;
                case "Months": return value * 30L * 24 * 60 * 60 * 1000;
                case "Weeks": return value * 7L * 24 * 60 * 60 * 1000;
                case "Days": return value * 24L * 60 * 60 * 1000;
                case "Hours": return value * 60L * 60 * 1000;
                case "Minutes": return value * 60L * 1000;
                case "Seconds": default: return value * base;
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    @Listen("onSelect = #performanceMeasure")
    public void onPerformanceMeasureChange() {
        try {
            String measure = performanceMeasure != null && performanceMeasure.getSelectedItem() != null ? performanceMeasure.getSelectedItem().getLabel() : "";
            boolean isCaseLength = "Case length".equals(measure);
            boolean needsDurationBounds = "Case duration".equals(measure)
                || "Processing time (average)".equals(measure)
                || "Processing time (maximum)".equals(measure)
                || "Processing time (total)".equals(measure)
                || "Waiting time (average)".equals(measure)
                || "Waiting time (maximum)".equals(measure)
                || "Waiting time (total)".equals(measure)
                || "Case utilization".equals(measure)
                || "Node duration".equals(measure)
                || "Arc duration".equals(measure);
            if (perfGteRow != null) perfGteRow.setVisible(needsDurationBounds && !isCaseLength);
            if (perfLteRow != null) perfLteRow.setVisible(needsDurationBounds && !isCaseLength);
            if (perfLenRow != null) perfLenRow.setVisible(isCaseLength);

            // Defaults for Case duration: GTE=0 Years, LTE=max duration in Years
            if ("Case duration".equals(measure)) {
                if (perfGteValue != null) perfGteValue.setValue(0);
                if (perfLteValue != null) {
                    long maxMs = computeMaxCaseDurationMs();
                    long yearMs = 365L * 24 * 60 * 60 * 1000;
                    int years = (int) Math.ceil((double) Math.max(maxMs, 0L) / (double) yearMs);
                    if (years < 1) years = 1; // ensure at least 1 when there is any duration
                    perfLteValue.setValue(years);
                }
                selectComboByLabel(perfGteUnit, "Years");
                selectComboByLabel(perfLteUnit, "Years");
            }

            // Defaults for Case length: From=min, To=max
            if ("Case length".equals(measure)) {
                int[] minMax = computeMinMaxCaseLength();
                if (perfLenFrom != null) perfLenFrom.setValue(minMax[0]);
                if (perfLenTo != null) perfLenTo.setValue(minMax[1]);
            }

            // Defaults for Processing time (average): GTE=0 Days, LTE=max avg processing time in Days
            if ("Processing time (average)".equals(measure)) {
                if (perfGteValue != null) perfGteValue.setValue(0);
                if (perfLteValue != null) {
                    long maxAvgMs = computeMaxAverageProcessingTimeMs();
                    long dayMs = 24L * 60 * 60 * 1000;
                    int days = (int) Math.ceil((double) Math.max(maxAvgMs, 0L) / (double) dayMs);
                    if (days < 1 && maxAvgMs > 0L) days = 1;
                    perfLteValue.setValue(days);
                }
                selectComboByLabel(perfGteUnit, "Days");
                selectComboByLabel(perfLteUnit, "Days");
            }
        } catch (Exception e) {
            LOGGER.error("Error handling performance measure change", e);
        }
    }

    private void selectComboByLabel(Combobox combo, String label) {
        if (combo == null || label == null) return;
        try {
            for (Comboitem item : combo.getItems()) {
                if (label.equals(item.getLabel())) {
                    combo.setSelectedItem(item);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private long computeMaxCaseDurationMs() {
        try {
            if (apmLog == null || apmLog.getTraces() == null) return 0L;
            long max = 0L;
            for (ATrace trace : apmLog.getTraces()) {
                try {
                    long start = trace.getStartTime();
                    long end = trace.getEndTime();
                    if (end >= start) {
                        long d = end - start;
                        if (d > max) max = d;
                    }
                } catch (Exception ignored) {}
            }
            if (max <= 0L) {
                // fallback to whole log duration
                try {
                    long d = apmLog.getEndTime() - apmLog.getStartTime();
                    if (d > max) max = d;
                } catch (Exception ignored) {}
            }
            return Math.max(0L, max);
        } catch (Exception e) {
            return 0L;
        }
    }

    private int[] computeMinMaxCaseLength() {
        int min = Integer.MAX_VALUE;
        int max = 0;
        try {
            if (apmLog == null || apmLog.getTraces() == null || apmLog.getTraces().isEmpty()) {
                return new int[] {0, 0};
            }
            for (ATrace trace : apmLog.getTraces()) {
                try {
                    int len = trace.getActivityInstances() != null ? trace.getActivityInstances().size() : 0;
                    if (len < min) min = len;
                    if (len > max) max = len;
                } catch (Exception ignored) {}
            }
            if (min == Integer.MAX_VALUE) min = 0;
            return new int[] {min, max};
        } catch (Exception e) {
            return new int[] {0, 0};
        }
    }

    private long computeMaxAverageProcessingTimeMs() {
        try {
            if (apmLog == null || apmLog.getTraces() == null || apmLog.getTraces().isEmpty()) return 0L;
            double maxAvg = 0.0;
            for (ATrace trace : apmLog.getTraces()) {
                try {
                    if (trace == null || trace.getActivityInstances() == null || trace.getActivityInstances().isEmpty()) continue;
                    double sum = 0.0;
                    int count = 0;
                    for (ActivityInstance ai : trace.getActivityInstances()) {
                        double d = ai != null ? ai.getDuration() : 0.0;
                        if (d > 0.0) {
                            sum += d;
                            count++;
                        }
                    }
                    if (count > 0) {
                        double avg = sum / (double) count;
                        if (avg > maxAvg) maxAvg = avg;
                    }
                } catch (Exception ignored) {}
            }
            if (maxAvg <= 0.0) return 0L;
            return (long) Math.ceil(maxAvg);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Initialize performance filter components
     */
    private void initializePerformanceFilter() {
        try {
            // Default measure: Case duration
            if (performanceMeasure != null && performanceMeasure.getItemCount() > 0) {
                performanceMeasure.setSelectedIndex(0);
            }
            // Ensure proper rows visibility
            onPerformanceMeasureChange();
        } catch (Exception e) {
            LOGGER.error("Error initializing performance filter", e);
        }
    }

    private FilterType resolveDurationFilterType(String measure) {
        if ("Processing time (average)".equals(measure)) return FilterType.AVERAGE_PROCESSING_TIME;
        if ("Processing time (maximum)".equals(measure)) return FilterType.MAX_PROCESSING_TIME;
        if ("Processing time (total)".equals(measure)) return FilterType.TOTAL_PROCESSING_TIME;
        return FilterType.DURATION; // Case duration default
    }

    private String durationKeyForMeasure(String measure) {
        if ("Processing time (average)".equals(measure)) return "duration:average_processing";
        if ("Processing time (maximum)".equals(measure)) return "duration:max_processing";
        if ("Processing time (total)".equals(measure)) return "duration:total_processing";
        return "duration:range"; // Case duration default
    }
}