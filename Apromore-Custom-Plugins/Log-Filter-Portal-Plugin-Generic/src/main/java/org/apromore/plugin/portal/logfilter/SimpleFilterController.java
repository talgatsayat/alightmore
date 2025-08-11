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
import org.slf4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * Простой контроллер для окна фильтрации
 */
public class SimpleFilterController extends SelectorComposer<Component> {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(SimpleFilterController.class);
    
    @Wire
    private Window simpleFilterWindow;
    
    @Wire
    private Listbox activeFiltersList;
    
    private PortalContext portalContext;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        try {
            // Получаем контекст из аргументов
            portalContext = (PortalContext) comp.getAttribute("portalContext");
            
            LOGGER.info("SimpleFilterController инициализирован");
            
        } catch (Exception e) {
            LOGGER.error("Ошибка при инициализации контроллера", e);
        }
    }
    
    @Listen("onClick = button[label*='Применить']")
    public void onApplyFilters(Event event) {
        try {
            LOGGER.info("Попытка применения фильтров");
            Messagebox.show("Функция применения фильтров будет реализована", 
                          "Информация", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при применении фильтров", e);
        }
    }
    
    @Listen("onClick = button[label*='Очистить']")
    public void onClearFilters(Event event) {
        try {
            LOGGER.info("Очистка фильтров");
            if (activeFiltersList != null) {
                activeFiltersList.getItems().clear();
            }
            Messagebox.show("Все фильтры очищены", 
                          "Информация", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при очистке фильтров", e);
        }
    }
    
    @Listen("onClick = button[label*='Отмена']")
    public void onCancel(Event event) {
        try {
            LOGGER.info("Закрытие окна фильтрации");
            if (simpleFilterWindow != null) {
                simpleFilterWindow.detach();
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка при закрытии окна", e);
        }
    }
    
    @Listen("onClick = button[label*='Атрибутный']")
    public void onAttributeFilter(Event event) {
        try {
            LOGGER.info("Открытие атрибутного фильтра");
            Messagebox.show("Атрибутный фильтр: выберите атрибут и значения для фильтрации", 
                          "Атрибутный фильтр", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при открытии атрибутного фильтра", e);
        }
    }
    
    @Listen("onClick = button[label*='вариантов']")
    public void onVariantFilter(Event event) {
        try {
            LOGGER.info("Открытие фильтра вариантов");
            Messagebox.show("Фильтр вариантов: выберите варианты выполнения для сохранения", 
                          "Фильтр вариантов", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при открытии фильтра вариантов", e);
        }
    }
    
    @Listen("onClick = button[label*='Временной']")
    public void onTimeFilter(Event event) {
        try {
            LOGGER.info("Открытие временного фильтра");
            Messagebox.show("Временной фильтр: укажите временной диапазон для фильтрации", 
                          "Временной фильтр", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при открытии временного фильтра", e);
        }
    }
    
    @Listen("onClick = button[label*='Производительность']")
    public void onPerformanceFilter(Event event) {
        try {
            LOGGER.info("Открытие фильтра производительности");
            Messagebox.show("Фильтр производительности: установите критерии времени выполнения", 
                          "Фильтр производительности", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при открытии фильтра производительности", e);
        }
    }
    
    @Listen("onClick = button[label*='Частотный']")
    public void onFrequencyFilter(Event event) {
        try {
            LOGGER.info("Открытие частотного фильтра");
            Messagebox.show("Частотный фильтр: установите критерии частоты выполнения действий", 
                          "Частотный фильтр", Messagebox.OK, Messagebox.INFORMATION);
        } catch (Exception e) {
            LOGGER.error("Ошибка при открытии частотного фильтра", e);
        }
    }
}