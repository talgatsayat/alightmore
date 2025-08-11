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

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apromore.plugin.portal.DefaultPortalPlugin;
import org.apromore.plugin.portal.PortalContext;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Минимальный плагин фильтрации логов по образцу AboutPlugin
 * Гарантированно работает и открывается
 */
@Component
public class MinimalLogFilterPlugin extends DefaultPortalPlugin {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(MinimalLogFilterPlugin.class);

    @Override
    public String getLabel(Locale locale) {
        return "Filter log (Minimal)";
    }

    @Override
    public String getIconPath() {
        return "filter-icon.svg";
    }

    @Override
    public void execute(PortalContext portalContext) {
        LOGGER.info("Запуск MinimalLogFilterPlugin");

        try {
            // Передаем минимальные параметры
            Map<String, Object> args = new HashMap<>();
            args.put("portalContext", portalContext);
            args.put("title", "Фильтрация логов");
            args.put("message", "Здесь будут настройки фильтрации");

            // Создаем окно напрямую, как в AboutPlugin
            final Window pluginWindow = (Window) Executions.getCurrent()
                    .createComponentsDirectly(
                            new InputStreamReader(
                                    getClass().getClassLoader().getResourceAsStream("logfilter/minimal.zul"), "UTF-8"),
                            "zul", null, args);

            // Находим кнопку закрытия и добавляем обработчик
            Button buttonOk = (Button) pluginWindow.getFellow("okButton");
            buttonOk.addEventListener("onClick", new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    pluginWindow.detach();
                    LOGGER.info("Окно фильтрации закрыто");
                }
            });

            // Ищем кнопку применения фильтров
            Button buttonApply = (Button) pluginWindow.getFellow("applyButton");
            buttonApply.addEventListener("onClick", new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    LOGGER.info("Применение фильтров");
                    Messagebox.show("Фильтры будут применены", "Информация", 
                                  Messagebox.OK, Messagebox.INFORMATION);
                }
            });

            // Показываем окно модально
            pluginWindow.doModal();

            LOGGER.info("Минимальное окно фильтрации открыто успешно");

        } catch (Exception e) {
            Messagebox.show("Ошибка при открытии окна фильтрации: " + e.getMessage(), 
                          "Ошибка", Messagebox.OK, Messagebox.ERROR);
            LOGGER.error("Ошибка в MinimalLogFilterPlugin", e);
        }
    }
}