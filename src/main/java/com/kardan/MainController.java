package com.kardan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;


public class MainController {
    @FXML private Pane board;
    @FXML private ToggleButton rectBtn, circleBtn, eraseBtn;
    @FXML private ColorPicker colorPicker;
    @FXML private Button clearBtn;

    private double startX, startY;
    private javafx.scene.shape.Shape currentShape;

    @FXML
    public void initialize() {
        // ensure only one tool selected at a time
        ToggleGroup tools = new ToggleGroup();
        rectBtn.setToggleGroup(tools);
        circleBtn.setToggleGroup(tools);
        eraseBtn.setToggleGroup(tools);

        // mouse pressed -> start shape or erase
        board.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();

            // find what node was clicked
            javafx.scene.Node clicked = e.getPickResult().getIntersectedNode();

            // ERASER: if erase tool AND clicked a shape that is direct child of board -> remove it
            if (eraseBtn.isSelected()) {
                if (clicked instanceof javafx.scene.shape.Shape && board.getChildren().contains(clicked)) {
                    board.getChildren().remove(clicked);
                }
                return;
            }

            // If clicked something other than the board itself (e.g. an existing shape), DON'T start a new shape
            if (clicked != board) {
                return;
            }

            // start new rectangle
            if (rectBtn.isSelected()) {
                Rectangle r = new Rectangle();
                r.setX(startX);
                r.setY(startY);
                r.setWidth(0);
                r.setHeight(0);
                r.setStroke(colorPicker.getValue());
                r.setFill(javafx.scene.paint.Color.TRANSPARENT);
                r.setStrokeWidth(2);
                currentShape = r;
                board.getChildren().add(r);
            }
            // start new ellipse
            else if (circleBtn.isSelected()) {
                Ellipse ell = new Ellipse();
                // initially center at start, radii 0
                ell.setCenterX(startX);
                ell.setCenterY(startY);
                ell.setRadiusX(0);
                ell.setRadiusY(0);
                ell.setStroke(colorPicker.getValue());
                ell.setFill(javafx.scene.paint.Color.TRANSPARENT);
                ell.setStrokeWidth(2);
                currentShape = ell;
                board.getChildren().add(ell);
            }
        });

        // mouse dragged -> resize current shape
        board.setOnMouseDragged(e -> {
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
            }

            // update stroke color live (if user changed color while drawing)
            if (currentShape != null) {
                currentShape.setStroke(colorPicker.getValue());
            }

        });

        // mouse released -> finish shape
        board.setOnMouseReleased(e -> currentShape = null);

        clearBtn.setOnAction(m -> board.getChildren().clear());
    }
}
