package com.kardan;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import com.kardan.network.NetworkClient;
import org.json.JSONArray;
import org.json.JSONObject;
//import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class MainController {
    private NetworkClient net;

    @FXML private Pane board;
    @FXML private ToggleButton rectBtn, circleBtn, eraseBtn, penBtn, triangleBtn, textBtn;
    @FXML private ColorPicker colorPicker;
    @FXML private Button clearBtn, undoBtn;
    @FXML private Slider strokeSlider;
    @FXML private Button saveBtn;
    @FXML private VBox onlineUsersBox;


    private Stack<Shape> undoStack = new Stack<>();
    private double startX, startY;
    private Shape currentShape;
    private Polyline currentPolyline;
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public void connect() {
        net = new NetworkClient("localhost", 55555, username, this::onMessage);
    }



    @FXML
    public void initialize() {
        ToggleGroup tools = new ToggleGroup();
        rectBtn.setToggleGroup(tools);
        circleBtn.setToggleGroup(tools);
        eraseBtn.setToggleGroup(tools);
        penBtn.setToggleGroup(tools);
        triangleBtn.setToggleGroup(tools);
        textBtn.setToggleGroup(tools);

        //  start shape or erase or pen
        board.setOnMousePressed(e -> {
            startX = e.getX();
            startY = e.getY();

            Node clicked = e.getPickResult().getIntersectedNode();
            //Erase
            if (eraseBtn.isSelected()) {
                if (clicked != null && board.getChildren().contains(clicked) && clicked != board) {
                    board.getChildren().remove(clicked);
                    sendAction("REMOVE", clicked);
                }
                return;
            }

            //Text
            if (textBtn.isSelected()) {
                TextField tf = new TextField();
                tf.setLayoutX(e.getX());
                tf.setLayoutY(e.getY());
                tf.setStyle("-fx-background-color: transparent");
                board.getChildren().add(tf);
                tf.requestFocus();
                tf.setOnAction(ev -> {
                    Text text = new Text(tf.getText());
                    double fontSize = strokeSlider.getValue() * 5;
                    text.setFont(Font.font(fontSize));
                    text.setX(tf.getLayoutX());
                    text.setY(tf.getLayoutY() + fontSize);
                    text.setFill(colorPicker.getValue());
                    board.getChildren().remove(tf);
                    String id = UUID.randomUUID().toString();
                    text.setUserData(id);
                    board.getChildren().add(text);
                    sendAction("ADD_TEXT", text);
                    undoStack.push(text);
                });

                return;
            }

            // Pen (freehand)
            if (penBtn.isSelected()) {
                currentPolyline = new Polyline();
                currentPolyline.setStroke(colorPicker.getValue());
                currentPolyline.setStrokeWidth(strokeSlider.getValue());
                currentPolyline.getPoints().addAll(startX, startY);
                String id = UUID.randomUUID().toString();
                currentPolyline.setUserData(id);
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
                String id = UUID.randomUUID().toString();
                r.setUserData(id);
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
                String id = UUID.randomUUID().toString();
                ell.setUserData(id);
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
                String id = UUID.randomUUID().toString();
                poly.setUserData(id);
                board.getChildren().add(poly);
                undoStack.push(poly);
            }
        });

        // Continue drawing
        board.setOnMouseDragged(e -> {
            // Pen: add points to polyline
            if (penBtn.isSelected()) {
                if (currentPolyline != null) {
                    currentPolyline.getPoints().addAll(e.getX(), e.getY());
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

            } else if (currentShape instanceof Polygon) {
                Polygon poly = (Polygon) currentShape;
                double baseY = e.getY();
                double leftX = Math.min(startX, e.getX());
                double rightX = Math.max(startX, e.getX());
                poly.getPoints().setAll(startX, startY, leftX, baseY, rightX, baseY);
            }
        });

        // Mouse released: finish
        board.setOnMouseReleased(e -> {
            // if we were drawing a polyline (pen), send points now
            if (currentPolyline != null) {
                sendAction("ADD_LINE", currentPolyline);
            }
            // if we were drawing a shape (rect/ellipse/triangle), send it now
            else if (currentShape != null) {
                // currentShape will be Rectangle, Ellipse, or Polygon
                if (currentShape instanceof Rectangle) sendAction("ADD_RECT", currentShape);
                else if (currentShape instanceof Ellipse) sendAction("ADD_ELLIPSE", currentShape);
                else if (currentShape instanceof Polygon) sendAction("ADD_TRIANGLE", currentShape);
            }

            // finish
            currentShape = null;
            currentPolyline = null;
        });


        // Clear button
        clearBtn.setOnAction(e -> {
            board.getChildren().clear();
            String msg= new JSONObject().put("type", "CLEAR").toString();
            net.sendJson(msg);
        });
        // Undo button
        undoBtn.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                Shape last = undoStack.pop();
                board.getChildren().remove(last);
                sendAction("REMOVE", last);
            }
        });
    }


    public void updateOnlineUsers(List<String> users) {
        Platform.runLater(() -> {
            onlineUsersBox.getChildren().clear();
            Label title = new Label("Online Users:");
            title.setStyle("-fx-font-weight:bold;");
            onlineUsersBox.getChildren().add(title);
            for (String u : users) {
                Label userLabel = new Label(u);
                userLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14;");
                onlineUsersBox.getChildren().add(userLabel);
            }
        });
    }


    private void onMessage(String json) {
        Platform.runLater(() -> {
            try {
                JSONObject obj = new JSONObject(json);
                String type = obj.getString("type");
                String id = obj.optString("id", "");

                switch(type) {
                    case "ADD_RECT" -> {
                        Rectangle r = new Rectangle();
                        r.setX(obj.getDouble("x"));
                        r.setY(obj.getDouble("y"));
                        r.setWidth(obj.getDouble("w"));
                        r.setHeight(obj.getDouble("h"));
                        r.setStroke(Color.web(obj.getString("color")));
                        r.setFill(Color.TRANSPARENT);
                        r.setStrokeWidth(obj.optDouble("stroke", 2.0));
                        r.setUserData(id);
                        board.getChildren().add(r);
                        break;
                    }
                    case "ADD_ELLIPSE" -> {
                        Ellipse e = new Ellipse();
                        e.setCenterX(obj.getDouble("x"));
                        e.setCenterY(obj.getDouble("y"));
                        e.setRadiusX(obj.getDouble("w"));
                        e.setRadiusY(obj.getDouble("h"));
                        e.setStroke(Color.web(obj.getString("color")));
                        e.setFill(Color.TRANSPARENT);
                        e.setStrokeWidth(obj.optDouble("stroke", 2.0));
                        e.setUserData(id);
                        board.getChildren().add(e);
                        break;
                    }
                    case "ADD_TRIANGLE" -> {
                        Polygon t = new Polygon(
                                obj.getDouble("x1"), obj.getDouble("y1"),
                                obj.getDouble("x2"), obj.getDouble("y2"),
                                obj.getDouble("x3"), obj.getDouble("y3")
                        );
                        t.setStroke(Color.web(obj.getString("color")));
                        t.setFill(Color.TRANSPARENT);
                        t.setStrokeWidth(obj.optDouble("stroke", 2.0));
                        t.setUserData(id);
                        board.getChildren().add(t);
                        break;
                    }
                    case "ADD_LINE"-> {
                        // points array -> Polyline
                        org.json.JSONArray pts = obj.getJSONArray("points");
                        Polyline pl = new Polyline();
                        for (int i = 0; i < pts.length(); i++) {
                            pl.getPoints().add(pts.getDouble(i));
                        }
                        pl.setStroke(Color.web(obj.optString("color", "#000000")));
                        pl.setStrokeWidth(obj.optInt("stroke", 2));
                        pl.setUserData(id);
                        board.getChildren().add(pl);
                        break;
                    }

                    case "ADD_TEXT" -> {
                        Text t = new Text();
                        t.setX(obj.getDouble("x"));
                        t.setY(obj.getDouble("y"));
                        t.setText(obj.getString("text"));
                        t.setFill(Color.web(obj.getString("color")));
                        t.setFont(Font.font(obj.getDouble("fontSize"))); // if float, use getDouble
                        t.setUserData(obj.getString("id"));
                        board.getChildren().add(t);
                        break;
                    }

                    case "REMOVE" -> {
                        for(Node n : board.getChildren()) {
                            if(id.equals(n.getUserData())) {
                                board.getChildren().remove(n);
                                break;
                            }
                        }
                    }
                    case "CLEAR" -> board.getChildren().clear();

                    case "ONLINE_USERS" ->{
                        JSONArray arr = obj.getJSONArray("users");
                        List<String> list= new ArrayList<>();
                        for (int i = 0; i< arr.length(); i++){
                            list.add(arr.getString(i));
                        }
                        updateOnlineUsers(list);
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void sendAction(String type, Object obj) {

        if ("REMOVE".equals(type)) {
            String id = null;
            if (obj instanceof Node) {
                Object ud = ((Node) obj).getUserData();
                if (ud != null) id = ud.toString();
            }
            if (id == null) return;
            org.json.JSONObject rem = new org.json.JSONObject();
            rem.put("type", "REMOVE");
            rem.put("id", id);
            net.sendJson(rem.toString());
            return;
        }


        if (obj == null || net == null) return;

        try {
            JSONObject json = new JSONObject();
            if (obj instanceof Shape) {
                Shape shape = (Shape) obj;
                String id = (String) shape.getUserData();
                if (id == null) {
                    id = UUID.randomUUID().toString();
                    shape.setUserData(id);
                }
                json.put("type", type);
                json.put("id", id);
                json.put("color", shape.getStroke() != null ? shape.getStroke().toString() : "#000000");
                json.put("stroke", Math.max(1, Math.round(strokeSlider.getValue())));

                if (shape instanceof Rectangle) {
                    Rectangle r = (Rectangle) shape;
                    json.put("x", r.getX());
                    json.put("y", r.getY());
                    json.put("w", r.getWidth());
                    json.put("h", r.getHeight());
                } else if (shape instanceof Ellipse) {
                    Ellipse e = (Ellipse) shape;
                    json.put("x", e.getCenterX());
                    json.put("y", e.getCenterY());
                    json.put("w", e.getRadiusX());
                    json.put("h", e.getRadiusY());
                } else if (shape instanceof Polygon) {
                    Polygon p = (Polygon) shape;
                    // polygon with 3 points (triangle)
                    json.put("x1", p.getPoints().get(0));
                    json.put("y1", p.getPoints().get(1));
                    json.put("x2", p.getPoints().get(2));
                    json.put("y2", p.getPoints().get(3));
                    json.put("x3", p.getPoints().get(4));
                    json.put("y3", p.getPoints().get(5));
                } else if (shape instanceof Polyline) {
                    Polyline pl = (Polyline) shape;
                    org.json.JSONArray arr = new org.json.JSONArray();
                    for (Double d : pl.getPoints()) arr.put(d);
                    json.put("points", arr);
                }
            }
            if (obj instanceof Text) {
                Text text = (Text) obj;
                String id = (String) text.getUserData();
                double fontSize = text.getFont().getSize();
                String content = text.getText().replace("\"", "\\\"");
                String color = text.getFill() != null ? text.getFill().toString() : "#000000";
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("type", "ADD_TEXT");
                jsonObj.put("id", id);
                jsonObj.put("x", text.getX());
                jsonObj.put("y", text.getY());
                jsonObj.put("text", content);
                jsonObj.put("color", color);
                jsonObj.put("fontSize", fontSize);

                net.sendJson(jsonObj.toString());
                return;
            }

            String out = json.toString();
            net.sendJson(out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
