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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.apromore.plugin.portal.DefaultPortalPlugin;
import org.apromore.plugin.portal.PortalContext;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.apromore.portal.model.LogSummaryType;
import org.apromore.portal.model.SummaryType;
import org.apromore.portal.model.VersionSummaryType;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Прямой плагин фильтрации логов
 * Открывает окно фильтрации для работы с логами
 */
@Component("directPortalLogFilterPlugin")
public class DirectLogFilterPlugin extends DefaultPortalPlugin {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(DirectLogFilterPlugin.class);
    
    public DirectLogFilterPlugin() {
        LOGGER.info("🔧 DirectLogFilterPlugin создан! Bean ID: directPortalLogFilterPlugin");
    }
    
    @Override
    public String getId() {
        return "directPortalLogFilterPlugin";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Filter log (Direct)";
    }

    @Override
    public String getIconPath() {
        return "filter-icon.svg";
    }

    @Override
    public void execute(PortalContext portalContext) {
        try {
            LOGGER.info("🚀 Запуск DirectLogFilterPlugin - анализ выбранного лога");
            
            // Получаем выбранные логи
            Set<LogSummaryType> selectedLogs = getSelectedLogs(portalContext);
            
            if (selectedLogs.isEmpty()) {
                Messagebox.show("Please select a log to filter!", 
                              "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            if (selectedLogs.size() > 1) {
                Messagebox.show("Please select only one log for filtering!", 
                              "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            
            LogSummaryType selectedLog = selectedLogs.iterator().next();
            LOGGER.info("📊 Выбранный лог: {} (ID: {})", selectedLog.getName(), selectedLog.getId());
            
            // Подготавливаем данные для передачи в контроллер
            Map<String, Object> args = new HashMap<>();
            args.put("portalContext", portalContext);
            args.put("selectedLog", selectedLog);
            args.put("logName", selectedLog.getName());
            args.put("logId", selectedLog.getId());
            
            // Создаем окно фильтрации из ZUL файла
            Window filterWindow = (Window) portalContext.getUI().createComponent(
                getClass().getClassLoader(), 
                "logFilterWindow.zul", 
                null, 
                args
            );
            
            if (filterWindow != null) {
                filterWindow.doModal();
                LOGGER.info("✅ Окно фильтрации открыто для лога: {}", selectedLog.getName());
            } else {
                LOGGER.error("❌ Не удалось создать окно фильтрации");
                Messagebox.show("Failed to open filter window", "Error", Messagebox.OK, Messagebox.ERROR);
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при открытии окна фильтрации", e);
            try {
                Messagebox.show("Error opening filter window: " + e.getMessage(), 
                               "Error", Messagebox.OK, Messagebox.ERROR);
            } catch (Exception msgEx) {
                LOGGER.error("Не удалось показать сообщение об ошибке", msgEx);
            }
        }
    }

    /**
     * Получает выбранные логи из контекста портала
     */
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