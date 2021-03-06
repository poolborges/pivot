/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.TablePaneSkin;

/**
 * Terra TablePane skin. Deals with color selections that depend
 * on the current theme.
 */
public class TerraTablePaneSkin extends TablePaneSkin {
    public TerraTablePaneSkin() {
        setHorizontalGridColor(7);
        setVerticalGridColor(7);
        setHighlightBackgroundColor(10);
    }

    public final void setHorizontalGridColor(int horizontalGridColor) {
        Theme theme = currentTheme();
        setHorizontalGridColor(theme.getColor(horizontalGridColor));
    }

    public final void setVerticalGridColor(int verticalGridColor) {
        Theme theme = currentTheme();
        setVerticalGridColor(theme.getColor(verticalGridColor));
    }

    public final void setHighlightBackgroundColor(int highlightBackgroundColor) {
        Theme theme = currentTheme();
        setHighlightBackgroundColor(theme.getColor(highlightBackgroundColor));
    }
}
