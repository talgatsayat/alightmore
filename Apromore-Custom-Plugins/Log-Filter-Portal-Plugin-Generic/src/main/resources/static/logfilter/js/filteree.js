/*
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
/**
 * FilterEE - Main JavaScript functionality for log filtering
 * 
 * This file contains the core functionality for the log filter interface
 * including data loading, filtering, and UI interactions.
 */

// Global variables
var filterData = {
    logName: '',
    activities: [],
    resources: [],
    cases: [],
    currentFilter: null,
    selectedValues: new Set(),
    filterHistory: [],
    filterHistoryIndex: -1
};

// Initialize the filter interface
function initializeFilter(logName, logData) {
    console.log('Initializing filter for log:', logName);
    
    filterData.logName = logName;
    filterData.activities = logData.activities || [];
    filterData.resources = logData.resources || [];
    filterData.cases = logData.cases || [];
    
    // Update window title
    var window = document.getElementById('filterCriteria');
    if (window) {
        window.setAttribute('title', 'Filter log: ' + logName);
    }
    
    // Load initial data
    loadActivityData();
    
    // Set up event listeners
    setupEventListeners();
    
    console.log('Filter initialized successfully');
}

// Load activity data into the table
function loadActivityData() {
    console.log('Loading activity data...');
    
    var table = document.getElementById('valuesTable');
    if (!table) {
        console.error('Values table not found');
        return;
    }
    
    // Clear existing items
    table.clear();
    
    // Add activity data
    filterData.activities.forEach(function(activity, index) {
        var item = table.appendItem();
        item.setAttribute('value', activity.name);
        
        // Add checkbox
        var checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = 'activity_' + index;
        checkbox.onclick = function() {
            toggleActivitySelection(activity.name);
        };
        
        var cell1 = item.getFirstChild();
        cell1.appendChild(checkbox);
        cell1.appendChild(document.createTextNode(' ' + activity.name));
        
        // Add case count
        var cell2 = item.getChild(1);
        cell2.appendChild(document.createTextNode(activity.caseCount));
        
        // Add frequency
        var cell3 = item.getChild(2);
        cell3.appendChild(document.createTextNode(activity.frequency + '%'));
    });
    
    updateItemCount();
    console.log('Activity data loaded');
}

// Toggle activity selection
function toggleActivitySelection(activityName) {
    if (filterData.selectedValues.has(activityName)) {
        filterData.selectedValues.delete(activityName);
    } else {
        filterData.selectedValues.add(activityName);
    }
    
    console.log('Selected activities:', Array.from(filterData.selectedValues));
    updateFilterPreview();
}

// Select all activities
function onSelectAll() {
    console.log('Selecting all activities');
    
    filterData.activities.forEach(function(activity) {
        filterData.selectedValues.add(activity.name);
    });
    
    // Update checkboxes
    var checkboxes = document.querySelectorAll('#valuesTable input[type="checkbox"]');
    checkboxes.forEach(function(checkbox) {
        checkbox.checked = true;
    });
    
    updateFilterPreview();
}

// Deselect all activities
function onDeselectAll() {
    console.log('Deselecting all activities');
    
    filterData.selectedValues.clear();
    
    // Update checkboxes
    var checkboxes = document.querySelectorAll('#valuesTable input[type="checkbox"]');
    checkboxes.forEach(function(checkbox) {
        checkbox.checked = false;
    });
    
    updateFilterPreview();
}

// Update filter preview
function updateFilterPreview() {
    var selectedCount = filterData.selectedValues.size;
    var totalCount = filterData.activities.length;
    
    console.log('Filter preview updated:', selectedCount + '/' + totalCount + ' activities selected');
    
    // Update UI to show selection status
    var previewElement = document.getElementById('filterPreview');
    if (previewElement) {
        previewElement.textContent = selectedCount + ' of ' + totalCount + ' activities selected';
    }
}

// Apply current filter
function applyFilter() {
    console.log('Applying filter...');
    
    if (filterData.selectedValues.size === 0) {
        showMessage('Please select at least one activity to filter', 'warning');
        return;
    }
    
    // Create filter criteria
    var criteria = {
        type: 'activity',
        action: getSelectedAction(),
        values: Array.from(filterData.selectedValues),
        matching: getSelectedMatching()
    };
    
    // Add to history
    addToHistory(criteria);
    
    // Apply filter
    applyFilterCriteria(criteria);
    
    console.log('Filter applied successfully');
}

// Get selected action (Retain/Remove)
function getSelectedAction() {
    var retainRadio = document.getElementById('retainCases');
    return retainRadio && retainRadio.checked ? 'retain' : 'remove';
}

// Get selected matching type (Any/All)
function getSelectedMatching() {
    var anyRadio = document.getElementById('anyValue');
    return anyRadio && anyRadio.checked ? 'any' : 'all';
}

// Apply filter criteria
function applyFilterCriteria(criteria) {
    console.log('Applying criteria:', criteria);
    
    // Show loading state
    showLoading(true);
    
    // Simulate filter application (replace with actual API call)
    setTimeout(function() {
        showLoading(false);
        
        // Update UI with filtered results
        updateFilteredResults(criteria);
        
        showMessage('Filter applied successfully', 'success');
    }, 1000);
}

// Update filtered results
function updateFilteredResults(criteria) {
    console.log('Updating filtered results');
    
    // Update the table with filtered data
    var table = document.getElementById('valuesTable');
    if (table) {
        // Clear existing items
        table.clear();
        
        // Add filtered data
        var filteredActivities = filterData.activities.filter(function(activity) {
            if (criteria.action === 'retain') {
                return criteria.values.includes(activity.name);
            } else {
                return !criteria.values.includes(activity.name);
            }
        });
        
        filteredActivities.forEach(function(activity, index) {
            var item = table.appendItem();
            item.setAttribute('value', activity.name);
            
            var cell1 = item.getFirstChild();
            cell1.appendChild(document.createTextNode(activity.name));
            
            var cell2 = item.getChild(1);
            cell2.appendChild(document.createTextNode(activity.caseCount));
            
            var cell3 = item.getChild(2);
            cell3.appendChild(document.createTextNode(activity.frequency + '%'));
        });
        
        updateItemCount();
    }
}

// Add filter to history
function addToHistory(criteria) {
    // Remove any future history if we're not at the end
    if (filterData.filterHistoryIndex < filterData.filterHistory.length - 1) {
        filterData.filterHistory = filterData.filterHistory.slice(0, filterData.filterHistoryIndex + 1);
    }
    
    filterData.filterHistory.push(criteria);
    filterData.filterHistoryIndex = filterData.filterHistory.length - 1;
    
    updateUndoRedoButtons();
}

// Undo last filter
function undoLastFilter() {
    if (filterData.filterHistoryIndex > 0) {
        filterData.filterHistoryIndex--;
        var criteria = filterData.filterHistory[filterData.filterHistoryIndex];
        applyFilterCriteria(criteria);
        updateUndoRedoButtons();
    }
}

// Redo last filter
function redoLastFilter() {
    if (filterData.filterHistoryIndex < filterData.filterHistory.length - 1) {
        filterData.filterHistoryIndex++;
        var criteria = filterData.filterHistory[filterData.filterHistoryIndex];
        applyFilterCriteria(criteria);
        updateUndoRedoButtons();
    }
}

// Update undo/redo buttons
function updateUndoRedoButtons() {
    var undoBtn = document.getElementById('undoButton');
    var redoBtn = document.getElementById('redoButton');
    
    if (undoBtn) {
        undoBtn.disabled = filterData.filterHistoryIndex <= 0;
    }
    
    if (redoBtn) {
        redoBtn.disabled = filterData.filterHistoryIndex >= filterData.filterHistory.length - 1;
    }
}

// Clear all filters
function clearAllFilters() {
    console.log('Clearing all filters');
    
    filterData.selectedValues.clear();
    filterData.filterHistory = [];
    filterData.filterHistoryIndex = -1;
    
    // Reset UI
    var checkboxes = document.querySelectorAll('#valuesTable input[type="checkbox"]');
    checkboxes.forEach(function(checkbox) {
        checkbox.checked = false;
    });
    
    // Reload original data
    loadActivityData();
    updateUndoRedoButtons();
    updateFilterPreview();
    
    showMessage('All filters cleared', 'info');
}

// Save current filter
function saveCurrentFilter() {
    console.log('Saving current filter');
    
    if (filterData.selectedValues.size === 0) {
        showMessage('No filter to save', 'warning');
        return;
    }
    
    var filterName = prompt('Enter a name for this filter:');
    if (filterName) {
        var filterToSave = {
            name: filterName,
            criteria: {
                type: 'activity',
                action: getSelectedAction(),
                values: Array.from(filterData.selectedValues),
                matching: getSelectedMatching()
            },
            timestamp: new Date().toISOString()
        };
        
        // Save filter (replace with actual storage)
        saveFilterToStorage(filterToSave);
        
        showMessage('Filter saved successfully', 'success');
    }
}

// Save filter to storage
function saveFilterToStorage(filter) {
    // Get existing filters
    var savedFilters = JSON.parse(localStorage.getItem('savedFilters') || '[]');
    
    // Add new filter
    savedFilters.push(filter);
    
    // Save back to storage
    localStorage.setItem('savedFilters', JSON.stringify(savedFilters));
    
    console.log('Filter saved to storage:', filter.name);
}

// Load saved filters
function loadSavedFilters() {
    var savedFilters = JSON.parse(localStorage.getItem('savedFilters') || '[]');
    console.log('Loaded saved filters:', savedFilters.length);
    return savedFilters;
}

// Update item count display
function updateItemCount() {
    var table = document.getElementById('valuesTable');
    var itemCount = document.getElementById('itemCount');
    
    if (table && itemCount) {
        var totalItems = table.getItemCount();
        var currentPage = table.getActivePage();
        var pageSize = table.getPageSize();
        var startItem = (currentPage - 1) * pageSize + 1;
        var endItem = Math.min(currentPage * pageSize, totalItems);
        
        itemCount.textContent = '[' + startItem + ' - ' + endItem + ' / ' + totalItems + ']';
    }
}

// Show loading state
function showLoading(show) {
    var table = document.getElementById('valuesTable');
    if (table) {
        if (show) {
            table.addClass('loading');
        } else {
            table.removeClass('loading');
        }
    }
}

// Show message
function showMessage(message, type) {
    console.log('Message:', message, 'Type:', type);
    
    // Create message element
    var messageDiv = document.createElement('div');
    messageDiv.className = type + '-state';
    messageDiv.innerHTML = '<div class="' + type + '-title">' + type.charAt(0).toUpperCase() + type.slice(1) + '</div>' +
                          '<div class="' + type + '-message">' + message + '</div>';
    
    // Add to page
    var container = document.getElementById('filterCriteria');
    if (container) {
        container.appendChild(messageDiv);
        
        // Remove after 5 seconds
        setTimeout(function() {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 5000);
    }
}

// Setup event listeners
function setupEventListeners() {
    console.log('Setting up event listeners');
    
    // OK button
    var okButton = document.getElementById('filterOkButton');
    if (okButton) {
        okButton.onclick = applyFilter;
    }
    
    // Cancel button
    var cancelButton = document.getElementById('filterCancelButton');
    if (cancelButton) {
        cancelButton.onclick = function() {
            window.close();
        };
    }
    
    // Create button
    var createButton = document.getElementById('filterCreateButton');
    if (createButton) {
        createButton.onclick = function() {
            // Open create filter dialog
            console.log('Create filter clicked');
        };
    }
    
    // Edit button
    var editButton = document.getElementById('filterEditButton');
    if (editButton) {
        editButton.onclick = function() {
            // Open edit filter dialog
            console.log('Edit filter clicked');
        };
    }
    
    // Remove button
    var removeButton = document.getElementById('filterRemoveButton');
    if (removeButton) {
        removeButton.onclick = function() {
            // Remove selected filter
            console.log('Remove filter clicked');
        };
    }
    
    // Remove all button
    var removeAllButton = document.getElementById('filterRemoveAllButton');
    if (removeAllButton) {
        removeAllButton.onclick = clearAllFilters;
    }
    
    // Radio button changes
    var actionRadios = document.querySelectorAll('input[name="casesAction"]');
    actionRadios.forEach(function(radio) {
        radio.onchange = updateFilterPreview;
    });
    
    var matchingRadios = document.querySelectorAll('input[name="matchingType"]');
    matchingRadios.forEach(function(radio) {
        radio.onchange = updateFilterPreview;
    });
}

// Export functions for use in ZUL
window.initializeFilter = initializeFilter;
window.onSelectAll = onSelectAll;
window.onDeselectAll = onDeselectAll;
window.applyFilter = applyFilter;
window.undoLastFilter = undoLastFilter;
window.redoLastFilter = redoLastFilter;
window.clearAllFilters = clearAllFilters;
window.saveCurrentFilter = saveCurrentFilter; 