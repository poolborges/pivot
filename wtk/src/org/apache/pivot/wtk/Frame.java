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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Container class representing a decorated frame window.
 */
public class Frame extends Window {
    private MenuBar menuBar = null;

    private FrameListener.Listeners frameListeners = new FrameListener.Listeners();

    public Frame() {
        this(null, null);
    }

    public Frame(String title) {
        this(title, null);
    }

    public Frame(Component content) {
        this(null, content);
    }

    public Frame(String title, Component content) {
        super(content);

        setTitle(title);
        installSkin(Frame.class);
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(MenuBar menuBar) {
        MenuBar previousMenuBar = this.menuBar;

        if (previousMenuBar != menuBar) {
            this.menuBar = menuBar;

            if (previousMenuBar != null) {
                remove(previousMenuBar);
            }

            if (menuBar != null) {
                add(menuBar);
            }

            frameListeners.menuBarChanged(this, previousMenuBar);
        }
    }

    @Override
    public void moveToFront() {
        super.moveToFront();
        requestActive();
    }

    private LinkedList<Component> getAncestorPath(Component component) {
        LinkedList<Component> path = new LinkedList<>();

        for (Component ancestor = component;
             !(ancestor instanceof Display);
             ancestor = ancestor.getParent()) {
            path.insert(ancestor, 0);
        }

        return path;
    }

    @Override
    protected void descendantGainedFocus(Component descendant, Component previousFocusedComponent) {
        if (menuBar != null) {
            for (Component component : getAncestorPath(descendant)) {
                MenuHandler menuHandler = component.getMenuHandler();

                if (menuHandler != null) {
                    menuHandler.configureMenuBar(component, menuBar);
                }
            }
        }

        super.descendantGainedFocus(descendant, previousFocusedComponent);
    }

    @Override
    protected void descendantLostFocus(Component descendant) {
        if (menuBar != null) {
            for (Component component : getAncestorPath(descendant)) {
                MenuHandler menuHandler = component.getMenuHandler();

                if (menuHandler != null) {
                    menuHandler.cleanupMenuBar(component, menuBar);
                }
            }
        }

        super.descendantLostFocus(descendant);
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == menuBar) {
                throw new UnsupportedOperationException("Cannot directly remove the menu bar from a Frame.");
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<FrameListener> getFrameListeners() {
        return frameListeners;
    }
}
