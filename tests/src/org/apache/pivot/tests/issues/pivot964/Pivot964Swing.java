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
package org.apache.pivot.tests.issues.pivot964;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.swing.JPanel;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;

/**
 * Test using a Swing JFrame
 * In this case all is good.
 */
public class Pivot964Swing extends javax.swing.JFrame {
    public static final long serialVersionUID = 0;

    TestPanel panel = new TestPanel();

    /** Creates new form SVGIconDemo */
    public Pivot964Swing() {
        initComponents();
        panelDisplay.add(panel, BorderLayout.CENTER);
        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        panelDisplay = new javax.swing.JPanel();
        button1 = new javax.swing.JButton();
        button2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        panelDisplay.setLayout(new java.awt.BorderLayout());

        getContentPane().add(panelDisplay, java.awt.BorderLayout.CENTER);

        button1.setText("Visible");
        button1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SVGElement root = Pivot964Swing.this.panel.root;
                try {
                    root.setAttribute("viewBox", AnimationElement.AT_XML, "0 0 2368 1652");
                    root.updateTime(0f);
                    repaint();
                } catch (SVGElementException e) {
                    e.printStackTrace();
                } catch (SVGException e) {
                    e.printStackTrace();
                }
            }
        });

        button2.setText("Invisible");
        button2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SVGElement root = Pivot964Swing.this.panel.root;
                try {
                    root.setAttribute("viewBox", AnimationElement.AT_XML, "800 0 2368 1652");
                    root.updateTime(0f);
                    repaint();
                } catch (SVGElementException e) {
                    e.printStackTrace();
                } catch (SVGException e) {
                    e.printStackTrace();
                }
            }
        });

        panelDisplay.add(button1, java.awt.BorderLayout.EAST);
        panelDisplay.add(button2, java.awt.BorderLayout.WEST);

        pack();
    } // </editor-fold>

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Pivot964Swing().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton button1;
    private javax.swing.JButton button2;
    private javax.swing.JPanel panelDisplay;
    // End of variables declaration

}

class TestPanel extends JPanel {
    public static final long serialVersionUID = 0;

    final SVGIcon icon;
    URI uri;
    SVGElement root;

    public TestPanel() {
        StringReader reader = new StringReader(makeDynamicSVG());
        uri = SVGCache.getSVGUniverse().loadSVG(reader, "myImage");
        icon = new SVGIcon();
        icon.setAntiAlias(true);
        icon.setSvgURI(uri);
        root = icon.getSvgUniverse().getDiagram(uri).getRoot();

        setPreferredSize(new Dimension(400, 400));
    }

    @Override
    public void paintComponent(Graphics g) {
        final int width = getWidth();
        final int height = getHeight();

        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        icon.paintIcon(this, g, 0, 0);
    }

    private String makeDynamicSVG() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println(
            "<svg width=\"400\" height=\"400\" style=\"fill:none;stroke-width:16\" viewBox=\"781 -391 1177 826\">");
        pw.println(
            "<rect x=\"0\" y=\"0\" width=\"2000\" height=\"1000\" style=\"stroke:blue;fill:white\"/>");
        pw.println("</svg>");

        pw.close();
        return sw.toString();
    }

}
