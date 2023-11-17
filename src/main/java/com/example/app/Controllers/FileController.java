package com.example.app.Controllers;

import com.example.app.App;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.StatefulSFTPClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FileController {

    private StatefulSFTPClient client;

    @FXML
    private TreeView<String> treeView;

    HashMap<String, FileItem<String>> cachedItems;

    FileItem<String> root;

    Image directoryIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("Images/dir.png")));
    Image fileIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("Images/file.png")));

    MenuItem setRoot = new MenuItem("Set Root Directory");
    MenuItem download = new MenuItem("Download");
    MenuItem delete = new MenuItem("Delete");
    MenuItem rename = new MenuItem("Rename");

    ContextMenu contextMenu = new ContextMenu(setRoot, download, delete, rename);

    public void init() {
        root = new FileItem<>("Root", null);
        cachedItems = new HashMap<>();
        client = App.currentPi.createStatefulSTFPClient();

        treeView.setRoot(root);
        treeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
               FileItem<String> selectedItem = (FileItem<String>) treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    contextMenu.show(selectedItem.getGraphic(), event.getScreenX(), event.getScreenY());

                    setRoot.setOnAction(event1 -> setRoot(selectedItem));

                    download.setOnAction(event2 -> System.out.println("Downloading..."));

                    delete.setOnAction(event3 -> remove(selectedItem, selectedItem.getChildren().size() > 0));

                    rename.setOnAction(event4 -> rename(selectedItem, "test232-"));
                }
            }
        });

        try {
            root.setValue(client.pwd());
            root.updatePath(root.getValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cacheImmediate(root);
    }

    public void cacheImmediate(TreeItem<String> rootItem) {
        long start = System.currentTimeMillis();

        try {
            List<RemoteResourceInfo> list = client.ls();

            for (RemoteResourceInfo item : list) {
                FileItem<String> child = new FileItem<>(item.getName(), item.getPath());

                if (cachedItems.containsKey(child.getPath())) {
                    rootItem.getChildren().add(cachedItems.get(child.getPath()));
                    continue;
                }

                if (item.isDirectory()) {
                    child.getChildren().add(new TreeItem<>("temp"));

                    child.expandedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue && !child.isCached()) {
                            child.getChildren().remove(0);

                            try {
                                client.cd(child.getPath());

                                cacheImmediate(child);

                                client.cd(root.getPath());

                                child.setCached(true);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    child.setGraphic(new ImageView(directoryIcon));
                } else {
                    child.setGraphic(new ImageView(fileIcon));
                }

                cachedItems.put(child.getPath(), child);
                rootItem.getChildren().add(child);
            }
        } catch (IOException e) {
            App.createAlert("Error Opening Directory - " + e.getMessage(), Alert.AlertType.ERROR);
        }

        System.out.println("Disk cache time: " + (System.currentTimeMillis() - start) + "ms");
    }

    @FXML
    private void back() {
        try {
            client.cd("..");

            root = new FileItem<>(client.pwd(), client.pwd());
            treeView.setRoot(root);

            cacheImmediate(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setRoot(FileItem<String> newRoot) {
        try {
            client.cd("/");
            client.cd(newRoot.getPath());

            root = newRoot;
            treeView.setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void goHome() {
        try {
            client.cd("/");

            root = new FileItem<>(client.pwd(), client.pwd());
            treeView.setRoot(root);

            cacheImmediate(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void remove(FileItem<String> item, boolean directory) {
        try {
            if (directory) {
                client.rmdir(item.getPath());
            } else {
                client.rm(item.getPath());
            }

            cachedItems.remove(item.getPath());
            item.getParent().getChildren().remove(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void rename(FileItem<String> item, String name) {
        try {
            String newPath = ((FileItem<String>) item.getParent()).getPath() + "/" + name;
            client.rename(item.getPath(), newPath);
            item.updatePath(newPath);
            item.setValue(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class FileItem<String> extends TreeItem<String> {

        private String path;
        private boolean cached;

        public FileItem(String value, String path) {
            super(value);
            this.path = path;
            cached = false;
        }

        public void setCached(boolean value) { cached = value; }

        public boolean isCached() { return cached; }

        public String getPath() { return path; }

        public void updatePath(String newPath) { path = newPath; }
    }
}
