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

import org.apromore.apmlog.APMLog;
import org.apromore.apmlog.filter.PLog;
import org.apromore.apmlog.filter.rules.LogFilterRule;
import org.apromore.plugin.portal.logfilter.generic.LogFilterClient;
import org.apromore.plugin.portal.logfilter.generic.LogFilterOutputResult;
import org.apromore.plugin.portal.logfilter.generic.LogFilterPlugin;
import org.apromore.plugin.portal.logfilter.generic.LogFilterRequest;
import org.apromore.plugin.portal.logfilter.generic.LogFilterResponse;
import org.apromore.plugin.portal.PortalContext;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.apromore.plugin.portal.generic.PluginContext;
import org.apromore.plugin.portal.generic.PluginInputParams;
import org.apromore.plugin.portal.generic.PluginResultListener;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zul.Messagebox;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import org.apromore.plugin.property.ParameterType;

/**
 * LogFilterPlugin для Process Discoverer, который использует наше окно фильтрации
 */
@Component("logFilter")
public class ProcessDiscovererLogFilterPlugin implements LogFilterPlugin {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(ProcessDiscovererLogFilterPlugin.class);

    @Override
    public void execute(LogFilterRequest request) {
        try {
            LOGGER.info("🚀 ProcessDiscovererLogFilterPlugin: Открытие окна фильтрации");
            
            // Получаем данные из запроса
            APMLog apmLog = request.getOriginalLog();
            List<LogFilterRule> currentCriteria = request.getCriteria();
            int logId = request.getLogId();
            String logName = request.getLogName();
            LogFilterClient client = request.getClient();
            
            LOGGER.info("📊 Данные лога: {} (ID: {}), критериев: {}", logName, logId, currentCriteria != null ? currentCriteria.size() : 0);
            
            // Подготавливаем аргументы для окна фильтрации
            Map<String, Object> args = new HashMap<>();
            args.put("logName", logName);
            args.put("logId", logId);
            args.put("apmLog", apmLog);
            args.put("currentCriteria", currentCriteria);
            args.put("logFilterClient", client);
            
            // Сначала закрываем существующее окно, если оно есть
            try {
                org.zkoss.zk.ui.Execution current = org.zkoss.zk.ui.Executions.getCurrent();
                org.zkoss.zul.Window existingWindow = (org.zkoss.zul.Window) current.getDesktop().getPage("ap-pd2").getFellowIfAny("advancedLogFilterWindow");
                if (existingWindow != null) {
                    existingWindow.detach();
                    LOGGER.info("✅ Существующее окно фильтрации закрыто");
                }
            } catch (Exception e) {
                LOGGER.info("ℹ️ Существующее окно фильтрации не найдено или уже закрыто");
            }
            
            // Открываем окно фильтрации
            org.zkoss.zul.Window filterWindow = (org.zkoss.zul.Window) Executions.createComponents(getPageDefinition("processdiscoverer/zul/logFilterWindow.zul"), null, args);
            
            // Явно отображаем окно
            if (filterWindow != null) {
                filterWindow.setVisible(true);
                filterWindow.doModal();
                LOGGER.info("✅ Окно фильтрации отображено");
            }
            
            LOGGER.info("✅ Окно фильтрации открыто для Process Discoverer");
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при открытии окна фильтрации в Process Discoverer", e);
            Messagebox.show("Ошибка при открытии окна фильтрации: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }

    @Override
    public void execute(PortalContext portalContext) {
        try {
            LOGGER.info("🚀 ProcessDiscovererLogFilterPlugin: Открытие окна фильтрации для PortalContext");
            
            // Подготавливаем аргументы для окна фильтрации
            Map<String, Object> args = new HashMap<>();
            args.put("portalContext", portalContext);
            
            // Сначала закрываем существующее окно, если оно есть
            try {
                org.zkoss.zk.ui.Execution current = org.zkoss.zk.ui.Executions.getCurrent();
                org.zkoss.zul.Window existingWindow = (org.zkoss.zul.Window) current.getDesktop().getPage("ap-pd2").getFellowIfAny("advancedLogFilterWindow");
                if (existingWindow != null) {
                    existingWindow.detach();
                    LOGGER.info("✅ Существующее окно фильтрации закрыто");
                }
            } catch (Exception e) {
                LOGGER.info("ℹ️ Существующее окно фильтрации не найдено или уже закрыто");
            }
            
            // Открываем окно фильтрации
            org.zkoss.zul.Window filterWindow = (org.zkoss.zul.Window) Executions.createComponents(getPageDefinition("processdiscoverer/zul/logFilterWindow.zul"), null, args);
            
            // Явно отображаем окно
            if (filterWindow != null) {
                filterWindow.setVisible(true);
                filterWindow.doModal();
                LOGGER.info("✅ Окно фильтрации отображено");
            }
            
            LOGGER.info("✅ Окно фильтрации открыто для Process Discoverer");
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при открытии окна фильтрации в Process Discoverer", e);
            Messagebox.show("Ошибка при открытии окна фильтрации: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }

    /**
     * Создает LogSummaryType для передачи в окно фильтрации
     */
    private org.apromore.portal.model.LogSummaryType createLogSummaryType(int logId, String logName) {
        org.apromore.portal.model.LogSummaryType logSummary = new org.apromore.portal.model.LogSummaryType();
        logSummary.setId(logId);
        logSummary.setName(logName);
        return logSummary;
    }

    /**
     * Получает PageDefinition для ZUL файлов Process Discoverer
     */
    private PageDefinition getPageDefinition(String uri) throws IOException {
        String url = "static/" + uri;
        org.zkoss.zk.ui.Execution current = org.zkoss.zk.ui.Executions.getCurrent();
        PageDefinition pageDefinition = current.getPageDefinitionDirectly(
            new InputStreamReader(getClass().getClassLoader().getResourceAsStream(url)), "zul");
        return pageDefinition;
    }

    @Override
    public void execute(PluginContext filterContext, PluginInputParams inputParams, PluginResultListener resultListener) throws Exception {
        try {
            LOGGER.info("🚀 ProcessDiscovererLogFilterPlugin: execute с GenericPlugin параметрами");
            
            // Подготавливаем аргументы для окна фильтрации
            Map<String, Object> args = new HashMap<>();
            if (filterContext != null) {
                args.put("portalContext", filterContext.getPortalContext());
            }
            
            // Сначала закрываем существующее окно, если оно есть
            try {
                org.zkoss.zk.ui.Execution current = org.zkoss.zk.ui.Executions.getCurrent();
                org.zkoss.zul.Window existingWindow = (org.zkoss.zul.Window) current.getDesktop().getPage("ap-pd2").getFellowIfAny("advancedLogFilterWindow");
                if (existingWindow != null) {
                    existingWindow.detach();
                    LOGGER.info("✅ Существующее окно фильтрации закрыто");
                }
            } catch (Exception e) {
                LOGGER.info("ℹ️ Существующее окно фильтрации не найдено или уже закрыто");
            }
            
            // Открываем окно фильтрации
            org.zkoss.zul.Window filterWindow = (org.zkoss.zul.Window) Executions.createComponents(getPageDefinition("processdiscoverer/zul/logFilterWindow.zul"), null, args);
            
            // Явно отображаем окно
            if (filterWindow != null) {
                filterWindow.setVisible(true);
                filterWindow.doModal();
                LOGGER.info("✅ Окно фильтрации отображено");
            }
            
            LOGGER.info("✅ Окно фильтрации открыто для Process Discoverer");
            
        } catch (Exception e) {
            LOGGER.error("❌ Ошибка при открытии окна фильтрации в Process Discoverer", e);
            Messagebox.show("Ошибка при открытии окна фильтрации: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
        }
    }

    @Override
    public String getId() {
        return "processDiscovererLogFilter";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Log Filter";
    }

    @Override
    public RenderedImage getIcon() {
        return null;
    }

    @Override
    public String getIconPath() {
        return "filter.svg";
    }

    @Override
    public void setSimpleParams(Map params) {
        // Не используется
    }

    @Override
    public Map getSimpleParams() {
        return new HashMap();
    }

    @Override
    public InputStream getResourceAsStream(String resource) {
        return null;
    }

    @Override
    public Availability getAvailability() {
        return Availability.AVAILABLE;
    }

    @Override
    public Set<ParameterType<?>> getAvailableParameters() {
        return new HashSet<>();
    }

    @Override
    public Set<ParameterType<?>> getMandatoryParameters() {
        return new HashSet<>();
    }

    @Override
    public Set<ParameterType<?>> getOptionalParameters() {
        return new HashSet<>();
    }

    @Override
    public String getName() {
        return "ProcessDiscovererLogFilterPlugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getType() {
        return "LogFilter";
    }

    @Override
    public String getDescription() {
        return "Log Filter Plugin for Process Discoverer";
    }

    @Override
    public String getAuthor() {
        return "Apromore Team";
    }

    @Override
    public String getEMail() {
        return "support@apromore.org";
    }
} 