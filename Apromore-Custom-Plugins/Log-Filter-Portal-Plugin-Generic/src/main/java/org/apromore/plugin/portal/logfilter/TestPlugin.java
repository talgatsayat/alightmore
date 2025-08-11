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

import org.apromore.plugin.portal.DefaultPortalPlugin;
import org.apromore.plugin.portal.PortalLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.zkoss.zul.Messagebox;
import java.awt.image.RenderedImage;

/**
 * Простой тестовый плагин для проверки регистрации
 */
@Component("TestPlugin")
public class TestPlugin extends DefaultPortalPlugin {

    private static final Logger LOGGER = PortalLoggerFactory.getLogger(TestPlugin.class);

    public TestPlugin() {
        LOGGER.info("🔧 TestPlugin создан! Bean ID: TestPlugin");
    }

    public String getId() {
        return "TestPlugin";
    }

    public String getLabel() {
        return "Test Plugin";
    }

    public RenderedImage getIcon() {
        return null; // Используем null для простоты
    }

    @Override
    public void execute(org.apromore.plugin.portal.PortalContext context) {
        LOGGER.info("Executing TestPlugin...");
        Messagebox.show("Тестовый плагин успешно запущен!", "Успех", Messagebox.OK, Messagebox.INFORMATION);
    }
} 