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

import org.apromore.plugin.portal.DefaultPortalPlugin;
import org.apromore.plugin.portal.PortalContext;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Простой плагин фильтрации логов с минимальными зависимостями
 * Гарантированно открывается всплывающим окном
 */
@Component("SimpleLogFilterPlugin")
public class SimpleLogFilterPlugin extends DefaultPortalPlugin {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(SimpleLogFilterPlugin.class);

    @Override
    public String getId() {
        return "SimpleLogFilterPlugin";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Filter log (Simple)";
    }

    @Override
    public String getIconPath() {
        return "filter-icon.svg";
    }

    @Override
    public void execute(PortalContext portalContext) {
        try {
            LOGGER.info("Запуск SimpleLogFilterPlugin");
            
            // Простейшее окно с минимальными зависимостями
            Map<String, Object> args = new HashMap<>();
            args.put("portalContext", portalContext);
            
            // Создаем простое окно
            Window filterWindow = (Window) Executions.createComponents(
                "~./simpleFilterWindow.zul", 
                null, 
                args
            );
            
            // Показываем окно модально
            filterWindow.doModal();
            
            LOGGER.info("Простое окно фильтрации открыто успешно");
            
        } catch (Exception e) {
            LOGGER.error("Ошибка в SimpleLogFilterPlugin", e);
            try {
                Messagebox.show("Ошибка при открытии окна фильтрации: " + e.getMessage(), 
                              "Ошибка", Messagebox.OK, Messagebox.ERROR);
            } catch (Exception msgEx) {
                LOGGER.error("Не удалось показать сообщение об ошибке", msgEx);
            }
        }
    }
}