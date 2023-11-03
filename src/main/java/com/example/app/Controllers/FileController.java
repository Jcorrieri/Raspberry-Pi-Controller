package com.example.app.Controllers;

import com.example.app.App;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.StatefulSFTPClient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileController {

    TreeItem<String> root = new TreeItem<>("Root");

    private StatefulSFTPClient client;

    Image directoryIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("Images/dir.png")));
    Image fileIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("Images/file.png")));

    @FXML
    private TreeView<String> treeView;

    public void init() {
        treeView.setRoot(root);
        client = App.currentPi.createStatefulSTFPClient();

        try {
            root.setValue(client.pwd());
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

                FileItem<String> child;

                if (item.isDirectory()) {
                    child = new FileItem<>(item.getName(), client.pwd() + "/" + item.getName());
                    child.getChildren().add(new TreeItem<>("temp"));

                    child.expandedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue && !child.isCached()) {
                            child.getChildren().remove(0);

                            try {
                                client.cd(child.getPath());

                                cacheImmediate(child);

                                client.cd(root.getValue());

                                child.setCached(true);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    child.setGraphic(new ImageView(directoryIcon));
                } else {
                    child = new FileItem<>(item.getName(), client.pwd() + "/" + item.getName());
                    child.setGraphic(new ImageView(fileIcon));
                }

                rootItem.getChildren().add(child);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Disk cache time: " + (System.currentTimeMillis() - start) + "ms");
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
