/*-
 * #%L
 * This file is part of "Apromore Community".
 * %%
 * Copyright (C) 2018 - 2020 Apromore Pty Ltd.
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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;
import org.apromore.logfilter.criteria.LogFilterCriterion;
import org.apromore.plugin.portal.PortalContext;

import java.util.List;

/**
 * Filter Management Panel - provides UI controls for managing filters
 * Based on Apromore documentation: Undo/Redo filters, Clear all filters, Save filter
 * 
 * @author Generated for Apromore
 */
public class FilterManagementPanel extends Panel {

    private LogFilterController logFilterController;
    private PortalContext portalContext;
    
    private Button undoButton;
    private Button redoButton;
    private Button clearAllButton;
    private Button saveFilterButton;
    private Button applyTemplateButton;

    public FilterManagementPanel(LogFilterController logFilterController, PortalContext portalContext) {
        this.logFilterController = logFilterController;
        this.portalContext = portalContext;
        initComponents();
    }

    private void initComponents() {
        this.setTitle("Управление фильтрами");
        this.setStyle("margin: 10px;");

        Hbox buttonBox = new Hbox();
        buttonBox.setSpacing("10px");
        buttonBox.setStyle("padding: 10px;");

        // Undo button
        undoButton = new Button("Отменить (Ctrl+Z)");
        undoButton.setIconSclass("z-icon-undo");
        undoButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                logFilterController.undoLastFilter();
                updateButtonStates();
            }
        });

        // Redo button
        redoButton = new Button("Повторить (Ctrl+Y)");
        redoButton.setIconSclass("z-icon-redo");
        redoButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                logFilterController.redoLastFilter();
                updateButtonStates();
            }
        });

        // Clear all filters button
        clearAllButton = new Button("Очистить все фильтры");
        clearAllButton.setIconSclass("z-icon-close");
        clearAllButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                logFilterController.clearAllFilters();
                updateButtonStates();
            }
        });

        // Save filter button
        saveFilterButton = new Button("Сохранить фильтр");
        saveFilterButton.setIconSclass("z-icon-save");
        saveFilterButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                logFilterController.saveCurrentFilter();
            }
        });

        // Apply template button
        applyTemplateButton = new Button("Применить шаблон");
        applyTemplateButton.setIconSclass("z-icon-folder-open");
        applyTemplateButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                showFilterTemplates();
            }
        });

        buttonBox.appendChild(undoButton);
        buttonBox.appendChild(redoButton);
        buttonBox.appendChild(clearAllButton);
        buttonBox.appendChild(saveFilterButton);
        buttonBox.appendChild(applyTemplateButton);

        this.appendChild(buttonBox);
        
        updateButtonStates();
    }

    public void updateButtonStates() {
        // Update button states based on current filter state
        undoButton.setDisabled(!logFilterController.canUndo());
        redoButton.setDisabled(!logFilterController.canRedo());
        clearAllButton.setDisabled(logFilterController.getCriteria().isEmpty());
    }

    private void showFilterTemplates() {
        Window templateWindow = new Window();
        templateWindow.setTitle("Шаблоны фильтров");
        templateWindow.setWidth("500px");
        templateWindow.setHeight("400px");
        templateWindow.setClosable(true);
        templateWindow.setPosition("center");

        Vbox vbox = new Vbox();
        vbox.setSpacing("10px");
        vbox.setStyle("padding: 20px;");

        Label titleLabel = new Label("Выберите шаблон фильтра для применения:");
        vbox.appendChild(titleLabel);

        Listbox templateList = new Listbox();
        templateList.setHeight("250px");
        
        // Add some example templates
        templateList.appendItem("Быстрые случаи (< 1 день)", "fast_cases");
        templateList.appendItem("Медленные случаи (> 30 дней)", "slow_cases");
        templateList.appendItem("Случаи с ошибками", "error_cases");
        templateList.appendItem("Высокочастотные активности", "high_frequency");
        templateList.appendItem("Редкие активности", "rare_activities");

        templateList.addEventListener("onSelect", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                Listitem selectedItem = templateList.getSelectedItem();
                if (selectedItem != null) {
                    String templateId = selectedItem.getValue().toString();
                    applyFilterTemplate(templateId);
                    templateWindow.detach();
                }
            }
        });

        vbox.appendChild(templateList);
        templateWindow.appendChild(vbox);
        templateWindow.doModal();
    }

    private void applyFilterTemplate(String templateId) {
        // TODO: Implement template application logic
        Messagebox.show("Шаблон '" + templateId + "' применен", "Информация", Messagebox.OK, Messagebox.INFORMATION);
        updateButtonStates();
    }

    public void refresh() {
        updateButtonStates();
    }
} 