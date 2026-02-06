package com.kardan;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.Stack;


public class MainController {

    @FXML private Pane board;
    @FXML private ToggleButton rectBtn, circleBtn, eraseBtn, penBtn, triangleBtn;
    @FXML private ColorPicker colorPicker;
    @FXML private Button clearBtn, undoBtn;
    @FXML private Slider strokeSlider;


    private Stack<Shape> undoStack = new Stack<>();
    private double startX, startY;
    private Shape currentShape;
    private Polyline currentPolyline;

    @FXML
    public void initialize() {
        ToggleGroup tools = new ToggleGroup();
        rectBtn.setToggleGroup(tools);
        circleBtn.setToggleGroup(tools);
        eraseBtn.setToggleGroup(tools);
        penBtn.setToggleGroup(tools);
        triangleBtn.setToggleGroup(tools);

        // Mouse pressed: start shape or erase or pen
        board.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();

            Node clicked = e.getPickResult().getIntersectedNode();

            if (eraseBtn.isSelected()) {
                if (clicked instanceof Shape && board.getChildren().contains(clicked)) {
                    board.getChildren().remove(clicked);
                }
                return;
            }

            // PEN (freehand)
            if (penBtn.isSelected()) {
                currentPolyline = new Polyline();
                currentPolyline.setStroke(colorPicker.getValue());
                currentPolyline.setStrokeWidth(strokeSlider.getValue());
                currentPolyline.getPoints().addAll(startX, startY);
                board.getChildren().add(currentPolyline);
                undoStack.push(currentPolyline);
                currentShape = currentPolyline;
                return;
            }

            // Rectangle
            if (rectBtn.isSelected()) {
                Rectangle r = new Rectangle();
                r.setX(startX);
                r.setY(startY);
                r.setWidth(0);
                r.setHeight(0);
                r.setStroke(colorPicker.getValue());
                r.setFill(Color.TRANSPARENT);
                r.setStrokeWidth(strokeSlider.getValue());
                currentShape = r;
                board.getChildren().add(r);
                undoStack.push(r);
            }
            // Ellipse
            else if (circleBtn.isSelected()) {
                Ellipse ell = new Ellipse();
                ell.setCenterX(startX);
                ell.setCenterY(startY);
                ell.setRadiusX(0);
                ell.setRadiusY(0);
                ell.setStroke(colorPicker.getValue());
                ell.setFill(Color.TRANSPARENT);
                ell.setStrokeWidth(strokeSlider.getValue());
                currentShape = ell;
                board.getChildren().add(ell);
                undoStack.push(ell);
            }
            // Triangle
            else if (triangleBtn.isSelected()) {
                Polygon poly = new Polygon();
                poly.getPoints().addAll(startX, startY, startX, startY, startX, startY);
                poly.setStroke(colorPicker.getValue());
                poly.setFill(Color.TRANSPARENT);
                poly.setStrokeWidth(strokeSlider.getValue());
                currentShape = poly;
                board.getChildren().add(poly);
                undoStack.push(poly);
            }
        });

        // Mouse dragged
        board.setOnMouseDragged(e -> {
            // PEN: add points to polyline
            if (penBtn.isSelected()) {
                if (currentPolyline != null) {
                    currentPolyline.getPoints().addAll(e.getX(), e.getY());
                    currentPolyline.setStroke(colorPicker.getValue());
                }
                return;
            }

            if (currentShape == null) return;

            if (currentShape instanceof Rectangle) {
                Rectangle r = (Rectangle) currentShape;
                double x = Math.min(startX, e.getX());
                double y = Math.min(startY, e.getY());
                double w = Math.abs(e.getX() - startX);
                double h = Math.abs(e.getY() - startY);
                r.setX(x);
                r.setY(y);
                r.setWidth(w);
                r.setHeight(h);
                r.setStroke(colorPicker.getValue());
            } else if (currentShape instanceof Ellipse) {
                Ellipse ell = (Ellipse) currentShape;
                double centerX = (startX + e.getX()) / 2.0;
                double centerY = (startY + e.getY()) / 2.0;
                double radiusX = Math.abs(e.getX() - startX) / 2.0;
                double radiusY = Math.abs(e.getY() - startY) / 2.0;
                ell.setCenterX(centerX);
                ell.setCenterY(centerY);
                ell.setRadiusX(radiusX);
                ell.setRadiusY(radiusY);
                ell.setStroke(colorPicker.getValue());
            } else if (currentShape instanceof Polygon) {
                Polygon poly = (Polygon) currentShape;
                double baseY = e.getY();
                double leftX = Math.min(startX, e.getX());
                double rightX = Math.max(startX, e.getX());
                poly.getPoints().setAll(
                        startX, startY,
                        leftX, baseY,
                        rightX, baseY
                );
                poly.setStroke(colorPicker.getValue());
            }
        });

        // Mouse released: finish
        board.setOnMouseReleased(e -> {
            currentShape = null;
            currentPolyline = null;
        });

        // Clear button
        clearBtn.setOnAction(e -> board.getChildren().clear());
        undoBtn.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                Shape last = undoStack.pop();
                board.getChildren().remove(last);
            }
        });
    }
}
