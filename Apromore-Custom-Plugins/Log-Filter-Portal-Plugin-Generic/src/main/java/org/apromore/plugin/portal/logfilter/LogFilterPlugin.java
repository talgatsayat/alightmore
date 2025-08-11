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

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.apromore.plugin.portal.DefaultPortalPlugin;
import org.apromore.plugin.portal.PortalContext;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.apromore.portal.model.LogSummaryType;
import org.apromore.portal.model.SummaryType;
import org.apromore.portal.model.VersionSummaryType;
import org.apromore.service.EventLogService;
import org.apromore.logfilter.LogFilterService;
import org.apromore.logfilter.criteria.factory.LogFilterCriterionFactory;
import org.apromore.logfilter.criteria.factory.impl.LogFilterCriterionFactoryImpl;
import org.apromore.logfilter.criteria.LogFilterCriterion;
import org.apromore.logfilter.criteria.impl.*;
import org.apromore.logfilter.criteria.model.Action;
import org.apromore.logfilter.criteria.model.Level;
import org.apromore.logfilter.criteria.model.Type;
import org.deckfour.xes.model.XLog;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Правильный плагин фильтрации логов согласно документации Apromore
 * Интегрирован с ProM плагинами и существующими фильтрами Apromore
 */
@Component
public class LogFilterPlugin extends DefaultPortalPlugin {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(LogFilterPlugin.class);

    @Autowired 
    private EventLogService eventLogService;
    
    @Autowired 
    private LogFilterService logFilterService;
    
    @Autowired 
    private LogFilterCriterionFactory logFilterCriterionFactory;

    @Override
    public String getId() {
        return "LogFilterPlugin";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Filter log";
    }

    @Override
    public String getIconPath() {
        return "filter-icon.svg";
    }

    @Override
    public void execute(PortalContext portalContext) {
        try {
            LOGGER.info("Запуск LogFilterPlugin с интеграцией ProM");
            
            // Получаем выбранный лог
            Set<LogSummaryType> selectedLogs = getSelectedLogs(portalContext);
            
            if (selectedLogs.isEmpty()) {
                Messagebox.show("Пожалуйста, выберите один лог для фильтрации!", 
                              "Предупреждение", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            if (selectedLogs.size() > 1) {
                Messagebox.show("Пожалуйста, выберите только один лог для фильтрации!", 
                              "Предупреждение", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            LogSummaryType selectedLog = selectedLogs.iterator().next();
            Integer logId = (Integer) selectedLog.getId();
            XLog originalLog = eventLogService.getXLog(logId);
            
            if (originalLog == null) {
                Messagebox.show("Не удалось загрузить лог. Попробуйте перезапустить приложение.", 
                              "Ошибка", Messagebox.OK, Messagebox.ERROR);
                return;
            }
            
            // Создаем параметры для окна фильтрации
            Map<String, Object> args = new HashMap<>();
            args.put("portalContext", portalContext);
            args.put("originalLog", originalLog);
            args.put("logName", selectedLog.getName());
            args.put("logId", logId);
            args.put("eventLogService", eventLogService);
            args.put("logFilterService", logFilterService);
            args.put("logFilterCriterionFactory", logFilterCriterionFactory);
            
            // Создаем окно фильтрации согласно документации
            Window filterWindow = (Window) Executions.createComponents(
                "~./logFilterWindow.zul", 
                null, 
                args
            );
            
            // Показываем окно
            filterWindow.doModal();
            
            LOGGER.info("Окно фильтрации открыто для лога: {} с ProM интеграцией", selectedLog.getName());
            
        } catch (SuspendNotAllowedException e) {
            LOGGER.error("Ошибка при открытии окна фильтрации", e);
            Messagebox.show("Ошибка при открытии окна фильтрации: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        } catch (Exception e) {
            LOGGER.error("Неожиданная ошибка в LogFilterPlugin", e);
            Messagebox.show("Неожиданная ошибка: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }
    
    private Set<LogSummaryType> getSelectedLogs(PortalContext portalContext) {
        Set<LogSummaryType> selectedLogs = new HashSet<>();
        
        Map<SummaryType, List<VersionSummaryType>> elements = 
            portalContext.getSelection().getSelectedProcessModelVersions();
            
        for (Map.Entry<SummaryType, List<VersionSummaryType>> entry : elements.entrySet()) {
            if (entry.getKey() instanceof LogSummaryType) {
                selectedLogs.add((LogSummaryType) entry.getKey());
            }
        }
        
        return selectedLogs;
    }
} 