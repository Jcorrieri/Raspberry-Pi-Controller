package com.example.app.Controllers;

import com.example.app.App;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.StatefulSFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

        if (client == null) return;

        treeView.setRoot(root);
        treeView.setOnMouseClicked(event -> {
            FileItem<String> selectedItem = (FileItem<String>) treeView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                return;
            } else {
                setFileData(selectedItem);
            }

            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(selectedItem.getGraphic(), event.getScreenX(), event.getScreenY());

                setRoot.setOnAction(event1 -> setRoot(selectedItem));

                download.setOnAction(event2 -> {
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Select folder to download to");

                    File dir = dirChooser.showDialog(App.getPrimaryStage());
                    if (dir == null)
                        return;

                    if (selectedItem.isDirectory()) {
                        Alert alert = App.createAlert("Are you sure you want to download all contents in " +
                                selectedItem.getValue() + " ?", Alert.AlertType.WARNING);

                        alert.showAndWait().ifPresent(result -> {
                            if (result == ButtonType.OK) {
                                download(selectedItem, dir.getAbsolutePath());
                            }
                        });
                    } else {
                        download(selectedItem, dir.getAbsolutePath());
                    }
                });

                delete.setOnAction(event3 -> {
                    Alert alert = App.createAlert(null, Alert.AlertType.CONFIRMATION);

                    String headerText;
                    if (selectedItem.isDirectory()) {
                        headerText = "Are you sure you want to remove " + selectedItem.getValue() + " and all its contents?";
                    } else {
                        headerText = "Are you sure you want to remove " + selectedItem.getValue();
                    }

                    alert.setHeaderText(headerText);

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            remove(selectedItem, !selectedItem.getChildren().isEmpty());
                        }
                    });
                });

                rename.setOnAction(event4 -> {
                    TextInputDialog tid = App.createAlert(selectedItem.getValue());
                    tid.setHeaderText("Rename " + selectedItem.getValue() + "?");
                    tid.showAndWait().ifPresent(response -> rename(selectedItem, response));
                });
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
                    if (rootItem.getChildren().contains(cachedItems.get(child.getPath()))) {
                        continue;
                    }
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
                    child.setDirectory(true);
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

    private void download(FileItem<String> item, String path) {
        try {
            client.get(item.getPath(), new FileSystemFile(path));
            App.createAlert("File Downloaded", Alert.AlertType.INFORMATION).show();
        } catch (IOException e) {
            System.out.println("ERROR");
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void upload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to upload to " + root.getPath());

        File file = fileChooser.showOpenDialog(App.getPrimaryStage());
        if (file == null)
            return;

        Alert alert = App.createAlert("Confirm file upload", Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Upload");
        alert.setContentText("Confirm Upload to " + root.getPath());

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    client.put(new FileSystemFile(file), root.getPath());
                    cacheImmediate(root);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /*
        Name
        Type
        Path
        Size
        Uid
        Gid
        Cr
        Md
     */
    public void setFileData(FileItem<String> item) {
        try {
            String[] items = new String[8];
            FileAttributes itemAttr = client.stat(item.getPath());

            items[0] = item.getValue();
            items[1] = String.valueOf(itemAttr.getType());
            items[2] = item.getPath();
            items[3] = String.valueOf(itemAttr.getSize());
            items[4] = String.valueOf(itemAttr.getUID());
            items[5] = String.valueOf(itemAttr.getGID());
            items[6] = String.valueOf(new Date(itemAttr.getAtime() * 1000));
            items[7] = String.valueOf(new Date(itemAttr.getMtime() * 1000));

            App.getController().updateFileDetails(items);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class FileItem<String> extends TreeItem<String> {

        private String path;
        private boolean cached, directory;

        public FileItem(String value, String path) {
            super(value);
            this.path = path;
            cached = false;
            directory = false;
        }

        public void setCached(boolean value) { cached = value; }

        public boolean isCached() { return cached; }

        public void setDirectory(boolean value) { directory = value; }

        public boolean isDirectory() { return directory; }

        public String getPath() { return path; }

        public void updatePath(String newPath) { path = newPath; }
    }
}
