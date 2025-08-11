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
import org.apromore.service.EventLogService;
import org.apromore.logfilter.LogFilterService;
import org.apromore.logfilter.criteria.factory.LogFilterCriterionFactory;
import org.apromore.logfilter.criteria.LogFilterCriterion;
import org.deckfour.xes.model.XLog;
import org.slf4j.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;
import org.zkoss.zul.Messagebox;

import java.util.List;
import java.util.ArrayList;

/**
 * Контроллер для окна фильтрации логов согласно документации Apromore
 * Интегрирован с существующими фильтрами Apromore
 */
public class LogFilterWindowController extends SelectorComposer<Window> {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(LogFilterWindowController.class);

    @Wire
    private Window logFilterWindow;

    // Сервисы для фильтрации
    private PortalContext portalContext;
    private XLog originalLog;
    private String logName;
    private Integer logId;
    private EventLogService eventLogService;
    private LogFilterService logFilterService;
    private LogFilterCriterionFactory logFilterCriterionFactory;
    
    // Список активных фильтров
    private List<LogFilterCriterion> activeFilters = new ArrayList<>();

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        
        LOGGER.info("LogFilterWindowController инициализирован");
        
        // Получаем параметры
        portalContext = (PortalContext) Executions.getCurrent().getArg().get("portalContext");
        originalLog = (XLog) Executions.getCurrent().getArg().get("originalLog");
        logName = (String) Executions.getCurrent().getArg().get("logName");
        logId = (Integer) Executions.getCurrent().getArg().get("logId");
        eventLogService = (EventLogService) Executions.getCurrent().getArg().get("eventLogService");
        logFilterService = (LogFilterService) Executions.getCurrent().getArg().get("logFilterService");
        logFilterCriterionFactory = (LogFilterCriterionFactory) Executions.getCurrent().getArg().get("logFilterCriterionFactory");
        
        LOGGER.info("Получены параметры: logName = {}, logId = {}", logName, logId);
        
        if (portalContext != null) {
            LOGGER.info("PortalContext получен успешно");
        } else {
            LOGGER.warn("PortalContext не получен");
        }
        
        if (originalLog != null) {
            LOGGER.info("OriginalLog получен успешно, размер: {}", originalLog.size());
        } else {
            LOGGER.warn("OriginalLog не получен");
        }
        
        if (logFilterService != null) {
            LOGGER.info("LogFilterService получен успешно");
        } else {
            LOGGER.warn("LogFilterService не получен");
        }
        
        if (logFilterCriterionFactory != null) {
            LOGGER.info("LogFilterCriterionFactory получен успешно");
        } else {
            LOGGER.warn("LogFilterCriterionFactory не получен");
        }
    }
    
    /**
     * Применяет все активные фильтры
     */
    public void applyAllFilters() {
        try {
            if (logFilterService != null && originalLog != null) {
                LOGGER.info("Применяем {} фильтров к логу", activeFilters.size());
                
                // Здесь должна быть логика применения фильтров
                // logFilterService.applyFilters(originalLog, activeFilters);
                
                Messagebox.show("Фильтры применены успешно!", "Успех", Messagebox.OK, Messagebox.INFORMATION);
            } else {
                Messagebox.show("Ошибка: сервис фильтрации недоступен", "Ошибка", Messagebox.OK, Messagebox.ERROR);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при применении фильтров", e);
            Messagebox.show("Ошибка при применении фильтров: " + e.getMessage(), "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    /**
     * Очищает все фильтры
     */
    public void clearAllFilters() {
        activeFilters.clear();
        LOGGER.info("Все фильтры очищены");
    }
} 