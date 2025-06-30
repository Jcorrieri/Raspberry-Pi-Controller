## Raspberry-Pi-Controller

This project implements a desktop interface in Java to control a Raspberry Pi system. It features a graphical UI to interact with the Pi’s components and monitor key system resources. Key modules include a file manager for uploading, downloading, renaming, and deleting files; a basic shell interface; and real-time CPU, RAM, disk usage monitoring, and a GPIO control panel (currently read-only). The application makes use of JavaFX Tasks to handle multithreaded operations, ensuring background processes such as loading and data collection do not block program usage. Users can save their system preferences for greater convenience when re-launching the application.

## Features

- GPIO Manager
    - Currently cannot change GPIO configurations  
- File Manager 
    - Upload
    - Download
    - Delete
    - Rename
- Secure Shell
- CPU, RAM, and Disk Metrics
- GUI Interface
- Save System Profile

## Screenshots

![GPIO Panel](/gpiosc.jpg?raw=true "GPIO Panel")

![File Manager](/filesc.jpg?raw=true "File Manager")

![Secure Shell](/shell.jpg?raw=true "Secure Shell")

![Metrics Panel](/metricssc.jpg?raw=true "Metrics Panel")

![Add System](/addsys.jpg?raw=true "Add System")


