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
 * FilterEE Validation - Form validation for log filtering
 * 
 * This file contains validation functions and form handling
 * for the log filter interface.
 */

// Validation configuration
var ValidationConfig = {
    rules: {
        required: {
            test: function(value) {
                return value !== null && value !== undefined && value.toString().trim() !== '';
            },
            message: 'This field is required'
        },
        email: {
            test: function(value) {
                var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                return emailRegex.test(value);
            },
            message: 'Please enter a valid email address'
        },
        url: {
            test: function(value) {
                try {
                    new URL(value);
                    return true;
                } catch (e) {
                    return false;
                }
            },
            message: 'Please enter a valid URL'
        },
        number: {
            test: function(value) {
                return !isNaN(parseFloat(value)) && isFinite(value);
            },
            message: 'Please enter a valid number'
        },
        integer: {
            test: function(value) {
                return Number.isInteger(parseFloat(value));
            },
            message: 'Please enter a valid integer'
        },
        minLength: {
            test: function(value, minLength) {
                return value && value.toString().length >= minLength;
            },
            message: function(minLength) {
                return 'Minimum length is ' + minLength + ' characters';
            }
        },
        maxLength: {
            test: function(value, maxLength) {
                return value && value.toString().length <= maxLength;
            },
            message: function(maxLength) {
                return 'Maximum length is ' + maxLength + ' characters';
            }
        },
        range: {
            test: function(value, min, max) {
                var num = parseFloat(value);
                return !isNaN(num) && num >= min && num <= max;
            },
            message: function(min, max) {
                return 'Value must be between ' + min + ' and ' + max;
            }
        },
        pattern: {
            test: function(value, pattern) {
                var regex = new RegExp(pattern);
                return regex.test(value);
            },
            message: 'Please enter a valid value'
        },
        date: {
            test: function(value) {
                var date = new Date(value);
                return date instanceof Date && !isNaN(date);
            },
            message: 'Please enter a valid date'
        },
        futureDate: {
            test: function(value) {
                var date = new Date(value);
                return date instanceof Date && !isNaN(date) && date > new Date();
            },
            message: 'Please enter a future date'
        },
        pastDate: {
            test: function(value) {
                var date = new Date(value);
                return date instanceof Date && !isNaN(date) && date < new Date();
            },
            message: 'Please enter a past date'
        }
    }
};

// Validation class
function Validator() {
    this.errors = {};
    this.isValid = true;
}

Validator.prototype = {
    // Validate a single field
    validateField: function(field, rules) {
        var value = this.getValue(field);
        var fieldErrors = [];
        
        for (var ruleName in rules) {
            var rule = ValidationConfig.rules[ruleName];
            if (!rule) {
                console.warn('Unknown validation rule:', ruleName);
                continue;
            }
            
            var params = rules[ruleName];
            if (typeof params === 'boolean') {
                params = [];
            } else if (!Array.isArray(params)) {
                params = [params];
            }
            
            if (!rule.test.apply(this, [value].concat(params))) {
                var message = typeof rule.message === 'function' ? 
                    rule.message.apply(this, params) : rule.message;
                fieldErrors.push(message);
            }
        }
        
        if (fieldErrors.length > 0) {
            this.errors[field] = fieldErrors;
            this.isValid = false;
        } else {
            delete this.errors[field];
        }
        
        this.updateFieldValidation(field, fieldErrors.length === 0);
        
        return fieldErrors.length === 0;
    },
    
    // Validate multiple fields
    validateFields: function(fields) {
        var isValid = true;
        
        for (var field in fields) {
            if (!this.validateField(field, fields[field])) {
                isValid = false;
            }
        }
        
        return isValid;
    },
    
    // Get field value
    getValue: function(field) {
        var element = document.getElementById(field);
        if (!element) {
            console.warn('Field not found:', field);
            return null;
        }
        
        if (element.type === 'checkbox') {
            return element.checked;
        } else if (element.type === 'radio') {
            var radioGroup = document.querySelectorAll('input[name="' + element.name + '"]');
            for (var i = 0; i < radioGroup.length; i++) {
                if (radioGroup[i].checked) {
                    return radioGroup[i].value;
                }
            }
            return null;
        } else {
            return element.value;
        }
    },
    
    // Update field validation UI
    updateFieldValidation: function(field, isValid) {
        var element = document.getElementById(field);
        if (!element) return;
        
        // Remove existing validation classes
        element.classList.remove('valid', 'invalid');
        
        // Add validation class
        element.classList.add(isValid ? 'valid' : 'invalid');
        
        // Update validation message
        this.updateValidationMessage(field, isValid);
    },
    
    // Update validation message
    updateValidationMessage: function(field, isValid) {
        var messageId = field + '_message';
        var messageElement = document.getElementById(messageId);
        
        if (!messageElement) {
            // Create message element if it doesn't exist
            var fieldElement = document.getElementById(field);
            if (fieldElement) {
                messageElement = document.createElement('div');
                messageElement.id = messageId;
                messageElement.className = 'validation-message';
                fieldElement.parentNode.insertBefore(messageElement, fieldElement.nextSibling);
            }
        }
        
        if (messageElement) {
            if (isValid) {
                messageElement.style.display = 'none';
            } else {
                messageElement.style.display = 'block';
                messageElement.className = 'validation-message error';
                messageElement.textContent = this.errors[field] ? this.errors[field][0] : 'Invalid value';
            }
        }
    },
    
    // Clear all validation
    clearValidation: function() {
        this.errors = {};
        this.isValid = true;
        
        // Clear all validation classes and messages
        var elements = document.querySelectorAll('.valid, .invalid');
        elements.forEach(function(element) {
            element.classList.remove('valid', 'invalid');
        });
        
        var messages = document.querySelectorAll('.validation-message');
        messages.forEach(function(message) {
            message.style.display = 'none';
        });
    },
    
    // Get all errors
    getErrors: function() {
        return this.errors;
    },
    
    // Check if form is valid
    isValid: function() {
        return this.isValid;
    }
};

// Form validation for filter interface
function FilterValidator() {
    Validator.call(this);
    this.filterRules = {
        // Activity selection validation
        'activitySelect': {
            required: true
        },
        
        // Date range validation
        'startDate': {
            required: false,
            date: true
        },
        'endDate': {
            required: false,
            date: true
        },
        
        // Number range validation
        'minValue': {
            required: false,
            number: true
        },
        'maxValue': {
            required: false,
            number: true
        },
        
        // Text input validation
        'filterName': {
            required: true,
            minLength: 3,
            maxLength: 50
        }
    };
}

FilterValidator.prototype = Object.create(Validator.prototype);

FilterValidator.prototype.validateFilterForm = function() {
    return this.validateFields(this.filterRules);
};

// Real-time validation
function setupRealTimeValidation() {
    var validator = new FilterValidator();
    
    // Add event listeners for real-time validation
    var inputs = document.querySelectorAll('input, select, textarea');
    inputs.forEach(function(input) {
        input.addEventListener('blur', function() {
            var fieldName = this.id;
            if (fieldName && validator.filterRules[fieldName]) {
                validator.validateField(fieldName, validator.filterRules[fieldName]);
            }
        });
        
        input.addEventListener('input', debounce(function() {
            var fieldName = this.id;
            if (fieldName && validator.filterRules[fieldName]) {
                validator.validateField(fieldName, validator.filterRules[fieldName]);
            }
        }, 300));
    });
}

// Custom validation for filter criteria
function validateFilterCriteria(criteria) {
    var errors = [];
    
    // Validate action
    if (!criteria.action || !['retain', 'remove'].includes(criteria.action)) {
        errors.push('Invalid action specified');
    }
    
    // Validate values
    if (!criteria.values || !Array.isArray(criteria.values) || criteria.values.length === 0) {
        errors.push('At least one value must be selected');
    }
    
    // Validate matching type
    if (!criteria.matching || !['any', 'all'].includes(criteria.matching)) {
        errors.push('Invalid matching type specified');
    }
    
    // Validate date range if present
    if (criteria.startDate && criteria.endDate) {
        var startDate = new Date(criteria.startDate);
        var endDate = new Date(criteria.endDate);
        
        if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
            errors.push('Invalid date format');
        } else if (startDate > endDate) {
            errors.push('Start date must be before end date');
        }
    }
    
    // Validate number range if present
    if (criteria.minValue !== undefined && criteria.maxValue !== undefined) {
        var min = parseFloat(criteria.minValue);
        var max = parseFloat(criteria.maxValue);
        
        if (isNaN(min) || isNaN(max)) {
            errors.push('Invalid number format');
        } else if (min > max) {
            errors.push('Minimum value must be less than maximum value');
        }
    }
    
    return {
        isValid: errors.length === 0,
        errors: errors
    };
}

// Validate filter configuration
function validateFilterConfig(config) {
    var errors = [];
    
    // Validate required fields
    if (!config.name || config.name.trim() === '') {
        errors.push('Filter name is required');
    }
    
    if (!config.type || !['activity', 'resource', 'case', 'time', 'performance'].includes(config.type)) {
        errors.push('Invalid filter type');
    }
    
    if (!config.criteria || typeof config.criteria !== 'object') {
        errors.push('Filter criteria is required');
    } else {
        var criteriaValidation = validateFilterCriteria(config.criteria);
        if (!criteriaValidation.isValid) {
            errors.push.apply(errors, criteriaValidation.errors);
        }
    }
    
    return {
        isValid: errors.length === 0,
        errors: errors
    };
}

// Show validation errors
function showValidationErrors(errors) {
    if (!Array.isArray(errors) || errors.length === 0) {
        return;
    }
    
    var errorHtml = '<div class="error-state">' +
        '<div class="error-title">Validation Errors</div>' +
        '<div class="error-message">' +
        '<ul>' + errors.map(function(error) {
            return '<li>' + error + '</li>';
        }).join('') + '</ul>' +
        '</div></div>';
    
    // Add error message to page
    var container = document.getElementById('filterCriteria');
    if (container) {
        var errorDiv = document.createElement('div');
        errorDiv.innerHTML = errorHtml;
        container.appendChild(errorDiv.firstChild);
        
        // Remove after 10 seconds
        setTimeout(function() {
            if (errorDiv.firstChild && errorDiv.firstChild.parentNode) {
                errorDiv.firstChild.parentNode.removeChild(errorDiv.firstChild);
            }
        }, 10000);
    }
}

// Validate form before submission
function validateFormBeforeSubmit() {
    var validator = new FilterValidator();
    var isValid = validator.validateFilterForm();
    
    if (!isValid) {
        var errors = validator.getErrors();
        var errorMessages = [];
        
        for (var field in errors) {
            errorMessages.push.apply(errorMessages, errors[field]);
        }
        
        showValidationErrors(errorMessages);
        return false;
    }
    
    return true;
}

// Export validation functions
window.FilterValidator = FilterValidator;
window.validateFilterCriteria = validateFilterCriteria;
window.validateFilterConfig = validateFilterConfig;
window.validateFormBeforeSubmit = validateFormBeforeSubmit;
window.setupRealTimeValidation = setupRealTimeValidation;
window.showValidationErrors = showValidationErrors; 