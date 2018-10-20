/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.llc.mae.view;

import edu.brandeis.llc.mae.MaeStrings;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;

/**
 * Created by krim on 1/2/2016.
 */
public class MaeMainView extends JFrame {

    private WaitLayerUI waitLayer;

    public MaeMainView(JPanel menuBarView, JPanel textPanelView, JPanel statusBarView, JPanel tablePanelView) {
        super(MaeStrings.TITLE_PREFIX);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(900, 500));

        JPanel root = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JPanel bottom = tablePanelView;

        top.add(textPanelView, BorderLayout.CENTER);
        top.add(statusBarView, BorderLayout.SOUTH);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        main.setDividerLocation(350);

        // ##ES: keep bottom fixed during resize
        main.setResizeWeight(1.0);

        root.add(menuBarView, BorderLayout.NORTH);
        root.add(main, BorderLayout.CENTER);

        waitLayer = new WaitLayerUI();
        JLayer<JPanel> layer = new JLayer<>(root, waitLayer);
        setContentPane(layer);
    }

    public void showWait() {
        waitLayer.start();
    }

    public void hideWait() {
        waitLayer.stop();
    }

    public static class WaitLayerUI extends LayerUI<JPanel> {
        private boolean isRunning;
        private boolean isFadingOut;
        private Timer periodicIndicatorRotator;
        private int indicatorAngle;
        private int currentFadingStep;
        private int fadingSteps = 3;

        @Override
        public void paint(Graphics g, JComponent c) {
            int w = c.getWidth();
            int h = c.getHeight();

            // Paint the view.
            super.paint(g, c);

            if (!isRunning) return;

            Graphics2D g2 = (Graphics2D) g.create();

            float fade = (float) currentFadingStep / (float) fadingSteps;
            // Gray it out.
            Composite urComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f * fade));
            g2.fillRect(0, 0, w, h);
            g2.setComposite(urComposite);

            // Paint the wait indicator.
            int strokeLength = 20;
            int indicatorPivotX = w / 2;
            int indicatorPivotY = h / 2;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(
                    new BasicStroke(strokeLength / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setPaint(Color.white);
            g2.rotate(Math.PI * indicatorAngle / 180, indicatorPivotX, indicatorPivotY);
            int strokeCount = 12;
            for (int i = 0; i < strokeCount; i++) {
                float scale = (float) ((strokeCount - (float) i - 1.0) / (strokeCount - 1));
                g2.drawLine(indicatorPivotX + strokeLength, indicatorPivotY, indicatorPivotX + strokeLength * 2, indicatorPivotY);
                g2.rotate(-Math.PI / (strokeCount / 2), indicatorPivotX, indicatorPivotY);
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, scale * fade));
            }

            g2.dispose();
        }

        public void start() {
            if (isRunning) return;

            // Run a thread for animation.
            isRunning = true;
            isFadingOut = false;
            currentFadingStep = 0;
            int fps = 24;
            int tick = 1000 / fps;
            periodicIndicatorRotator = new Timer(tick, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isRunning) {
                        firePropertyChange("tick", 0, 1);
                        indicatorAngle += 12;
                        if (indicatorAngle >= 360) {
                            indicatorAngle = 0;
                        }
                        if (isFadingOut) {
                            if (--currentFadingStep < 1) {
                                isRunning = false;
                                periodicIndicatorRotator.stop();
                            }
                        } else if (currentFadingStep < fadingSteps) {
                            currentFadingStep++;
                        }
                    }
                }
            });
            periodicIndicatorRotator.start();
        }

        public void stop() {
            isFadingOut = true;
        }

        @Override
        public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
            if ("tick".equals(pce.getPropertyName())) {
                l.repaint();
            }
        }

        @Override
        public void eventDispatched(AWTEvent e, JLayer<? extends JPanel> l) {
            if (isRunning && e instanceof InputEvent) {
                ((InputEvent) e).consume();
            }
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            if (c instanceof JLayer) {
                JLayer layer = (JLayer) c;
                layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK |
                                AWTEvent.MOUSE_MOTION_EVENT_MASK |
                                AWTEvent.KEY_EVENT_MASK);
            }
        }
    }

}
