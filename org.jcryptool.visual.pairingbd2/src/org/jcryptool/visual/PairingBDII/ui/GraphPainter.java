//-----BEGIN DISCLAIMER-----
/*******************************************************************************
* Copyright (c) 2017 JCrypTool Team and Contributors
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
//-----END DISCLAIMER-----
package org.jcryptool.visual.PairingBDII.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.jcryptool.core.util.fonts.FontService;

public class GraphPainter implements PaintListener {

    private int nodeNumber;
    private int counter;
    private Point[][] pointTree;

    private void calculateChildren(PaintEvent e, Point middle, Point node, double slice, int radius, int max) {
        double rotate = 0;
        rotate += getDirection(middle, node);

        rotate -= slice / 2;
        int x, y;
        Point[] points = new Point[slice == 2 * Math.PI ? max - 1 : max];
        if (points.length > counter) {
            points = new Point[counter];
        }
        counter -= points.length;

        // calculate point coordinates
        for (int i = 0; i < points.length; i++) {
            final double sin = Math.sin(slice / (max - 1) * i + rotate);
            final double cos = Math.cos(slice / (max - 1) * i + rotate);
            x = node.x + (int) Math.round(sin * radius);
            y = node.y - (int) Math.round(cos * radius);
            points[i] = new Point(x, y);
        }

        // setup tree
        for (int i = 0; i < pointTree.length; i++) {
            if (pointTree[i][0] == null) {
                final Point[] temp = new Point[points.length + 1];
                temp[0] = node;
                for (int j = 1; j <= points.length; j++) {
                    temp[j] = points[j - 1];
                }
                pointTree[i] = temp;
                break;
            }
        }
    }

    private void calculatePoints(PaintEvent e, Point middle, int radius, double rotate, int numberOfInnerNodes,
            int numberOfChildNodes) {
        final double slice = 2 * Math.PI;

        rotate -= slice / 2;
        int x, y;
        Point[] points = new Point[numberOfInnerNodes];
        if (points.length > counter) {
            points = new Point[counter];
        }
        counter -= points.length;

        // calculate point coordinates
        for (int i = 0; i < points.length; i++) {
            final double sin = Math.sin(slice / (numberOfInnerNodes) * i + rotate);
            final double cos = Math.cos(slice / (numberOfInnerNodes) * i + rotate);
            x = middle.x + (int) Math.round(sin * radius);
            y = middle.y - (int) Math.round(cos * radius);
            points[i] = new Point(x, y);

            if (counter > 0) {
                calculateChildren(e, middle, points[i], 2 * slice / points.length * 0.6,
                        (int) Math.round(radius * 1.2), numberOfChildNodes);
            } else {
                pointTree[i] = new Point[] {points[i]};
            }
        }

    }

    private void drawArrow(PaintEvent e, Point source, Point target, double labelPosition) {
        final double distance = 0.1;
        e.gc.setForeground(new Color(e.display, 200, 10, 10));
        source = new Point((int) Math.round(source.x * (1 - distance) + target.x * distance), (int) Math.round(source.y
                * (1 - distance) + target.y * distance));

        final double direction = getDirection(target, source);

        target = new Point(target.x + (int) Math.round(Math.sin(direction) * 10), target.y
                - (int) Math.round(Math.cos(direction) * 10));

        final double rotation = getDirection(target, source);

        e.gc.drawLine(source.x, source.y, target.x, target.y);
        e.gc.drawLine(target.x + (int) Math.round(Math.sin(Math.PI * 0.1 + rotation) * 10), target.y
                - (int) Math.round(Math.cos(Math.PI * 0.1 + rotation) * 10), target.x, target.y);
        e.gc.drawLine(target.x + (int) Math.round(Math.sin(-Math.PI * 0.1 + rotation) * 10), target.y
                - (int) Math.round(Math.cos(-Math.PI * 0.1 + rotation) * 10), target.x, target.y);

        e.gc.setFont(FontService.getSmallFont());
        e.gc.drawString("m" + nodeNumber++, //$NON-NLS-1$
                (int) Math.round(target.x * labelPosition + source.x * (1 - labelPosition)) - 5, (int) Math
                        .round(target.y * labelPosition + source.y * (1 - labelPosition)) - 5);
    }

    private void drawConnections(PaintEvent e) {
        // draw point connections
        for (int i = 0; i < pointTree.length; i++) {
            e.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            e.gc.drawLine(pointTree[i][0].x, pointTree[i][0].y, pointTree[(i + 1) % pointTree.length][0].x,
                    pointTree[(i + 1) % pointTree.length][0].y);
        }

        // draw point connections from children to root
        for (final Point[] element : pointTree) {
            for (int j = 1; j < element.length; j++) {
                e.gc.drawLine(element[j].x, element[j].y, element[0].x, element[0].y);
            }
        }

        // draw point connections to other points
        for (final Point[] element : pointTree) {
            for (int j = 1; j < element.length - 1; j++) {
                if (j % 2 == 1) {
                    e.gc.drawLine(element[j].x, element[j].y, element[j + 1].x, element[j + 1].y);
                }
            }
        }
    }

    private void drawPoint(PaintEvent e, Point p, Color color) {
        // draw point
        e.gc.setBackground(color);
        e.gc.fillOval(p.x - 4, p.y - 4, 8, 8);

        // draw number
        e.gc.setForeground(color);
        e.gc.setBackground(((Canvas) e.widget).getBackground());
        e.gc.setFont(FontService.getSmallFont());
        e.gc.drawString("" + nodeNumber++, p.x - 3, p.y + 7); //$NON-NLS-1$
    }

    private double getDirection(Point middle, Point node) {
        double direction = 0;
        if (middle.x != node.x) {
            if (middle.x > node.x) {
                // rechts
                direction = -Math.PI / 2;
            } else {
                // links
                direction = Math.PI / 2;
            }

            direction += Math.atan((double) (node.y - middle.y) / (node.x - middle.x));
        }
        if (middle.x == node.x) {
            if (middle.y < node.y) {
                // unten
                direction = Math.PI;
            } else {
                // oben
                direction = 0;
            }
        }
        return direction;
    }

    public void paintControl(PaintEvent e) {
        final Color COLOR_BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        final Color yellow = new Color(Display.getCurrent(), 230, 230, 10);
        final Color green = new Color(Display.getCurrent(), 100, 200, 10);
        final Color blue = new Color(Display.getCurrent(), 0, 0, 230);

        final int width = ((Canvas) e.widget).getSize().x;
        final int height = ((Canvas) e.widget).getSize().y;
        final Point middle = new Point(width / 2, width / 2 - 20);

        final int innerNodes = 3;
        final int childNodes = 4;

        counter = Model.getDefault().numberOfUsers;
        pointTree = new Point[innerNodes][childNodes];

        calculatePoints(e, middle, (int) Math.round(middle.x / 2.3), 0, innerNodes, childNodes);
        drawConnections(e);

        if (Model.getDefault().currentStep == 3) {
            // draw arrows heading middle
            nodeNumber = 1;
            for (final Point[] element : pointTree) {
                drawArrow(e, element[0], middle, 0.3);
            }
            for (final Point[] element : pointTree) {
                for (int j = 1; j < element.length; j++) {
                    drawArrow(e, element[j], middle, 0.3);
                }
            }
        }

        if (Model.getDefault().currentStep == 4) {
            // draw arrows heading children
            nodeNumber = 1;
            for (final Point[] element : pointTree) {
                nodeNumber += element.length;
            }

            for (final Point[] element : pointTree) {
                for (int j = 1; j < element.length; j++) {
                    drawArrow(e, element[0], element[j], 0.5);
                    if (j % 2 == 1) {
                        nodeNumber--;
                    }
                }
            }
        }

        if (Model.getDefault().currentStep == 5) {
            // draw the common keys
            e.gc.setForeground(blue);
            e.gc.setBackground(((Canvas) e.widget).getBackground());
            e.gc.setFont(FontService.getHugeBoldFont());
            e.gc.drawString("K", middle.x - 9, middle.y - 9); //$NON-NLS-1$

            // draw the common keys at every node
            for (final Point[] element : pointTree) {
                for (int j = 0; j < element.length; j++) {
                    e.gc.setForeground(blue);
                    e.gc.setBackground(((Canvas) e.widget).getBackground());
                    e.gc.setFont(FontService.getNormalBoldFont());
                    e.gc.drawString("?", element[j].x - 4, element[j].y - 20); //$NON-NLS-1$
                }
            }
        }

        if (Model.getDefault().currentStep == 6) {
            // draw the common keys at every node
            for (final Point[] element : pointTree) {
                for (int j = 0; j < element.length; j++) {
                    e.gc.setForeground(blue);
                    e.gc.setBackground(((Canvas) e.widget).getBackground());
                    e.gc.setFont(FontService.getNormalBoldFont());
                    e.gc.drawString("K", element[j].x - 4, element[j].y - 20); //$NON-NLS-1$
                }
            }
        }

        // draw inner points
        nodeNumber = 1;
        for (final Point[] element : pointTree) {
            drawPoint(e, element[0], COLOR_BLACK);
        }
        // draw outer points
        for (final Point[] element : pointTree) {
            for (int j = 1; j < element.length; j++) {
                if (Model.getDefault().isOnBNCurve) {
                    if (element.length % 2 == 0 && j == element.length - 1) {
                        drawPoint(e, element[j], new Color(e.display, 0, 0, 230));
                    } else {
                        if (j % 2 == 1) {
                            drawPoint(e, element[j], yellow);
                        } else {
                            drawPoint(e, element[j], green);
                        }
                    }

                    e.gc.setForeground(yellow);
                    e.gc.drawString("PK(i) = Z(i)", 30, height - 60); //$NON-NLS-1$
                    e.gc.setBackground(yellow);
                    e.gc.fillOval(20, height - 60 + 3, 6, 6);
                    e.gc.setBackground(((Canvas) e.widget).getBackground());

                    e.gc.setForeground(green);
                    e.gc.drawString("PK(i) = R(i)", 30, height - 40); //$NON-NLS-1$
                    e.gc.setBackground(green);
                    e.gc.fillOval(20, height - 40 + 3, 6, 6);
                    e.gc.setBackground(((Canvas) e.widget).getBackground());

                    e.gc.setForeground(blue);
                    e.gc.drawString("PK(i) = {Z(i),R(i)}", 30, height - 20); //$NON-NLS-1$
                    e.gc.setBackground(blue);
                    e.gc.fillOval(20, height - 20 + 3, 6, 6);
                    e.gc.setBackground(((Canvas) e.widget).getBackground());

                } else {
                    drawPoint(e, element[j], COLOR_BLACK);
                }
            }
        }

        yellow.dispose();
        green.dispose();
        blue.dispose();
    }
}
