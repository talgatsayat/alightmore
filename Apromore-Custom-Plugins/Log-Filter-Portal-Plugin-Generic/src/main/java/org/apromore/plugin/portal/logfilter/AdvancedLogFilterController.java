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

/**
 * Контроллер для стандартного окна фильтрации логов Apromore
 */
public class AdvancedLogFilterController extends SelectorComposer<Component> {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(AdvancedLogFilterController.class);

    // Main window
    @Wire private Window advancedLogFilterWindow;
    
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

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        LOGGER.info("AdvancedLogFilterController initialized");
        
        extractLogInformation();
        updateLogInfoDisplay();
        initializeNavigation();
        initializeControlButtons();
        loadLogData();
        
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
            LOGGER.info("🔄 Изменен тип атрибута (Event/Case), перезагружаем значения...");
            loadAttributeValuesForLog();
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при изменении типа атрибута", e);
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
                // Sample data for testing
                if (eventCountLabel != null) {
                    eventCountLabel.setValue("10,000 events (sample)");
                }
                if (caseCountLabel != null) {
                    caseCountLabel.setValue("1,000 cases (sample)");
                }
                LOGGER.warn("Используются заглушечные данные для статистики");
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
                
                // Получаем статистику из APMLog
                int eventCount = 0;
                int caseCount = apmLog.getTraces().size();
                
                // Подсчитываем общее количество событий
                for (ATrace trace : apmLog.getTraces()) {
                    eventCount += trace.getActivityInstances().size();
                }
                
                // Обновляем отображение
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
                
                // TODO: В будущем здесь будет интеграция с EventLogService
                // Пока используем симуляцию данных на основе реального лога
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
                        
                        // Загружаем значения для выбранного атрибута на основе лога
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
            
            if (apmLog != null) {
                LOGGER.info("🔧 APMLog доступен, количество трасс: {}", apmLog.getTraces().size());
                
                // Собираем все уникальные атрибуты из лога
                Set<String> availableAttributes = new HashSet<>();
                
                // Добавляем стандартные атрибуты, которые всегда присутствуют
                availableAttributes.add("concept:name");
                availableAttributes.add("concept:case:id");
                LOGGER.info("🔧 Добавлены стандартные атрибуты: concept:name, concept:case:id");
                
                // Собираем атрибуты из трасс (case attributes)
                int caseAttributeCount = 0;
                for (ATrace trace : apmLog.getTraces()) {
                    if (trace.getAttributes() != null) {
                        caseAttributeCount += trace.getAttributes().size();
                        availableAttributes.addAll(trace.getAttributes().keySet());
                    }
                }
                LOGGER.info("🔧 Найдено {} case атрибутов в {} трассах", caseAttributeCount, apmLog.getTraces().size());
                
                // Собираем атрибуты из экземпляров активности (event attributes)
                int eventAttributeCount = 0;
                int totalActivityInstances = 0;
                for (ATrace trace : apmLog.getTraces()) {
                    totalActivityInstances += trace.getActivityInstances().size();
                    for (ActivityInstance activityInstance : trace.getActivityInstances()) {
                        if (activityInstance.getAttributes() != null) {
                            eventAttributeCount += activityInstance.getAttributes().size();
                            availableAttributes.addAll(activityInstance.getAttributes().keySet());
                        }
                    }
                }
                LOGGER.info("🔧 Найдено {} event атрибутов в {} экземплярах активности", eventAttributeCount, totalActivityInstances);
                
                // Сортируем атрибуты для удобства
                List<String> sortedAttributes = new ArrayList<>(availableAttributes);
                Collections.sort(sortedAttributes);
                
                LOGGER.info("🔧 Всего уникальных атрибутов: {}", sortedAttributes.size());
                LOGGER.info("🔧 Список атрибутов: {}", sortedAttributes);
                
                // Добавляем атрибуты в комбобокс
                for (String attribute : sortedAttributes) {
                    primaryAttributeCombo.appendItem(attribute);
                }
                
                // Устанавливаем первый атрибут как выбранный
                if (sortedAttributes.size() > 0) {
                    primaryAttributeCombo.setSelectedIndex(0);
                    LOGGER.info("✅ Установлен первый атрибут как выбранный: {}", sortedAttributes.get(0));
                }
                
                LOGGER.info("✅ Загружено {} доступных атрибутов для лога: {}", sortedAttributes.size(), logName);
                
            } else {
                LOGGER.warn("⚠️ APMLog недоступен, используем стандартные атрибуты");
                // Fallback to standard attributes
                primaryAttributeCombo.appendItem("concept:name");
                primaryAttributeCombo.appendItem("concept:case:id");
                primaryAttributeCombo.appendItem("org:resource");
                primaryAttributeCombo.appendItem("lifecycle:transition");
                primaryAttributeCombo.appendItem("time:timestamp");
                primaryAttributeCombo.setSelectedIndex(0);
                LOGGER.info("✅ Добавлены стандартные атрибуты (fallback)");
            }
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при загрузке доступных атрибутов из лога: {}", logName, e);
            // Fallback to standard attributes
            primaryAttributeCombo.appendItem("concept:name");
            primaryAttributeCombo.appendItem("concept:case:id");
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
                    return;
                }
                
                String selectedAttribute = selectedItem.getValue();
                LOGGER.info("🔧 Выбранный атрибут: {}", selectedAttribute);
                
                // Получаем уникальные значения выбранного атрибута из APMLog
                Map<String, Integer> attributeValueCounts = new HashMap<>();
                
                // Определяем тип атрибута (case или event)
                boolean isCaseAttribute = selectedAttribute.equals("concept:case:id") || 
                                       selectedAttribute.equals("concept:name") ||
                                       selectedAttribute.startsWith("case:");
                
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
                    LOGGER.info("🔧 Общее количество экземпляров активности: {}", totalActivityInstances);
                    LOGGER.info("🔧 Найдено {} значений EVENT атрибута '{}'", foundEventValues, selectedAttribute);
                }
                
                LOGGER.info("🔧 Найдено {} уникальных значений атрибута '{}'", attributeValueCounts.size(), selectedAttribute);
                LOGGER.info("🔧 Детали значений: {}", attributeValueCounts);
                
                // Сохраняем все значения атрибута для пагинации
                allAttributeValues.addAll(attributeValueCounts.keySet());
                LOGGER.info("🔧 allAttributeValues заполнен: {} элементов", allAttributeValues.size());
                
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
                // Fallback to sample data
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
                
                Listitem item = new Listitem();
                item.appendChild(new Listcell(value));
                item.appendChild(new Listcell(String.valueOf(cases)));
                item.appendChild(new Listcell(String.format("%.1f%%", frequency)));
                
                attributeValuesList.appendChild(item);
                
                LOGGER.debug("✅ Listitem добавлен в таблицу для значения: '{}'", value);
            } else {
                LOGGER.warn("⚠️ attributeValuesList == null в addAttributeValue");
            }
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при добавлении значения атрибута '{}' в таблицу", value, e);
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
                
                // Сохраняем все Case ID для пагинации
                allCaseIdValues.addAll(caseIdCounts.keySet());
                
                // Показываем первую страницу
                currentCaseIdPage = 1;
                displayCaseIdPage(currentCaseIdPage);
                
                LOGGER.info("✅ Загружены {} Case ID для лога: {}", caseIdCounts.size(), logName);
                
            } else {
                LOGGER.warn("⚠️ APMLog недоступен, используем заглушечные данные");
                // Fallback to sample data
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
     * Update statistics labels
     */
    private void updateStatistics() {
        try {
            int totalCases = 428; // TODO: Получить из реального лога
            
            if (caseAttributeStats != null) {
                caseAttributeStats.setValue(String.format("0%% ( 0 / %d )", totalCases));
            }
            
            if (caseIdStats != null) {
                caseIdStats.setValue(String.format("0%% ( 0 / %d )", totalCases));
            }
            
        } catch (Exception e) {
            LOGGER.error("Error updating statistics", e);
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
            if (cancelBtn != null) {
                cancelBtn.addEventListener(Events.ON_CLICK, event -> clearAllFilters());
            }
            if (applyFiltersBtn != null) {
                applyFiltersBtn.addEventListener(Events.ON_CLICK, event -> applyFiltersAndRedirect());
            }
            if (applyFiltersBtnCaseAttribute != null) {
                applyFiltersBtnCaseAttribute.addEventListener(Events.ON_CLICK, event -> applyFiltersAndRedirect());
            }
            if (applyFiltersBtnCaseId != null) {
                applyFiltersBtnCaseId.addEventListener(Events.ON_CLICK, event -> applyFiltersAndRedirect());
            }
            
            // Initialize secondary attribute checkbox
            if (useSecondaryAttribute != null) {
                useSecondaryAttribute.addEventListener(Events.ON_CHECK, event -> {
                    boolean enabled = useSecondaryAttribute.isChecked();
                    if (secondaryAttributeType != null) secondaryAttributeType.setDisabled(!enabled);
                    if (secondaryAttributeCombo != null) secondaryAttributeCombo.setDisabled(!enabled);
                });
            }

            LOGGER.info("Control buttons initialized");
        } catch (Exception e) {
            LOGGER.error("Error initializing control buttons", e);
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
                    if (timeframePanel != null) timeframePanel.setVisible(true);
                    if (timeframeNav != null) timeframeNav.setSclass("filter-nav-item selected");
                    break;
                case "performance":
                    if (performancePanel != null) performancePanel.setVisible(true);
                    if (performanceNav != null) performanceNav.setSclass("filter-nav-item selected");
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
            
            // Get current filter settings
            String filterDescription = getCurrentFilterDescription();
            
            // Get selected items for filtering
            List<String> selectedValues = getSelectedFilterValues();
            
            if (selectedValues.isEmpty()) {
                Messagebox.show("Пожалуйста, выберите значения для фильтрации", 
                              "Фильтры не выбраны", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            // Проверяем, работаем ли мы с Process Discoverer
            if (logFilterClient != null) {
                // Применяем фильтры через Process Discoverer
                applyFiltersToProcessDiscoverer(selectedValues, filterDescription);
            } else {
                // Открываем Process Discoverer с фильтрами (старая логика)
                openInProcessDiscoverer(selectedValues, filterDescription);
            }
            
            // Закрываем окно фильтрации
            closeWindow();
            
            LOGGER.info("🎯 Фильтры применены: {} значений для {}", selectedValues.size(), filterDescription);
            
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
                    rules = createCaseAttributeFilterRules(selectedValues);
                    break;
                case "caseId":
                    rules = createCaseIdFilterRules(selectedValues);
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
     * Create filter rules for Case Attribute filtering
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
            Choice choice = "retain".equals(condition) ? Choice.RETAIN : Choice.REMOVE;
            
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
                    if (advancedLogFilterWindow != null) {
            advancedLogFilterWindow.detach();
            }
            LOGGER.info("Filter window closed");
        } catch (Exception e) {
            LOGGER.error("Error closing window", e);
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
                LOGGER.warn("⚠️ allAttributeValues пуст, загружаем данные...");
                loadAttributeValuesForLog();
                return; // Выходим, так как loadAttributeValuesForLog() вызовет displayAttributePage снова
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
                
                String selectedAttribute = selectedItem.getValue();
                LOGGER.info("🔧 Отображение данных для атрибута: {}", selectedAttribute);
                
                // Определяем тип атрибута (case или event)
                boolean isCaseAttribute = selectedAttribute.equals("concept:case:id") || 
                                       selectedAttribute.equals("concept:name") ||
                                       selectedAttribute.startsWith("case:");
                
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
                
                LOGGER.info("🔧 Найдено {} уникальных значений атрибута '{}'", attributeValueCounts.size(), selectedAttribute);
            }
            
            int totalCases = attributeValueCounts.values().stream().mapToInt(Integer::intValue).sum();
            LOGGER.info("🔧 Общее количество случаев: {}", totalCases);
            
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

}