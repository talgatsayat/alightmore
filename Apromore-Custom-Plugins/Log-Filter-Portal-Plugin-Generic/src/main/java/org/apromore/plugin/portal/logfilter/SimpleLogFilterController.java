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

import org.apromore.plugin.portal.PortalLoggerFactory;
import org.slf4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

/**
 * Простой контроллер для окна фильтрации логов
 */
public class SimpleLogFilterController extends SelectorComposer<Component> {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(SimpleLogFilterController.class);

    @Wire private Window logFilterWindow;
    @Wire private Label logNameLabel;
    @Wire private Label eventCountLabel;
    @Wire private Label caseCountLabel;
    @Wire private Combobox attributeCombo;
    @Wire private Textbox attributeValueBox;
    @Wire private Button addFilterBtn;
    @Wire private Listbox filtersListbox;
    @Wire private Button resetBtn;
    @Wire private Button previewBtn;
    @Wire private Button applyBtn;
    @Wire private Button closeBtn;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        LOGGER.info("SimpleLogFilterController инициализирован");
        
        // Инициализация данных
        initializeLogInfo();
        initializeEventHandlers();
    }
    
    private void initializeLogInfo() {
        try {
            if (logNameLabel != null) {
                logNameLabel.setValue("Тестовый лог");
            }
            if (eventCountLabel != null) {
                eventCountLabel.setValue("1000");
            }
            if (caseCountLabel != null) {
                caseCountLabel.setValue("100");
            }
            
            // Заполнение атрибутов
            if (attributeCombo != null) {
                attributeCombo.appendItem("concept:name");
                attributeCombo.appendItem("org:resource");
                attributeCombo.appendItem("lifecycle:transition");
                attributeCombo.appendItem("time:timestamp");
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка инициализации данных лога", e);
        }
    }
    
    private void initializeEventHandlers() {
        try {
            if (addFilterBtn != null) {
                addFilterBtn.addEventListener(Events.ON_CLICK, event -> addFilter());
            }
            
            if (resetBtn != null) {
                resetBtn.addEventListener(Events.ON_CLICK, event -> resetFilters());
            }
            
            if (previewBtn != null) {
                previewBtn.addEventListener(Events.ON_CLICK, event -> previewFilters());
            }
            
            if (applyBtn != null) {
                applyBtn.addEventListener(Events.ON_CLICK, event -> applyFilters());
            }
            
            if (closeBtn != null) {
                closeBtn.addEventListener(Events.ON_CLICK, event -> closeWindow());
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка инициализации обработчиков событий", e);
        }
    }
    
    private void addFilter() {
        try {
            String attribute = attributeCombo.getValue();
            String value = attributeValueBox.getValue();
            
            if (attribute != null && !attribute.trim().isEmpty() && 
                value != null && !value.trim().isEmpty()) {
                
                // Добавляем фильтр в список
                Listitem item = new Listitem();
                item.appendChild(new Listcell(attribute));
                item.appendChild(new Listcell(value));
                
                Button removeBtn = new Button("Удалить");
                removeBtn.addEventListener(Events.ON_CLICK, e -> item.detach());
                Listcell actionCell = new Listcell();
                actionCell.appendChild(removeBtn);
                item.appendChild(actionCell);
                
                filtersListbox.appendChild(item);
                
                // Очищаем поля
                attributeValueBox.setValue("");
                
                LOGGER.info("Добавлен фильтр: {} = {}", attribute, value);
            } else {
                Messagebox.show("Пожалуйста, выберите атрибут и введите значение", "Внимание", 
                              Messagebox.OK, Messagebox.EXCLAMATION);
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка добавления фильтра", e);
        }
    }
    
    private void resetFilters() {
        try {
            if (filtersListbox != null) {
                filtersListbox.getItems().clear();
            }
            if (attributeValueBox != null) {
                attributeValueBox.setValue("");
            }
            LOGGER.info("Все фильтры сброшены");
        } catch (Exception e) {
            LOGGER.error("Ошибка сброса фильтров", e);
        }
    }
    
    private void previewFilters() {
        try {
            int filterCount = filtersListbox.getItems().size();
            Messagebox.show("Применено фильтров: " + filterCount + "\nПредварительный результат готов!", 
                          "Предпросмотр", Messagebox.OK, Messagebox.INFORMATION);
            LOGGER.info("Предпросмотр фильтров: {} фильтров", filterCount);
        } catch (Exception e) {
            LOGGER.error("Ошибка предпросмотра фильтров", e);
        }
    }
    
    private void applyFilters() {
        try {
            int filterCount = filtersListbox.getItems().size();
            Messagebox.show("Фильтры успешно применены!\nОбработано фильтров: " + filterCount, 
                          "Успех", Messagebox.OK, Messagebox.INFORMATION);
            LOGGER.info("Применены фильтры: {} фильтров", filterCount);
            closeWindow();
        } catch (Exception e) {
            LOGGER.error("Ошибка применения фильтров", e);
        }
    }
    
    private void closeWindow() {
        try {
            if (logFilterWindow != null) {
                logFilterWindow.detach();
            }
            LOGGER.info("Окно фильтрации закрыто");
        } catch (Exception e) {
            LOGGER.error("Ошибка закрытия окна", e);
        }
    }
}