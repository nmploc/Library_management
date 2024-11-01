# Library management system

## Setup

### Prerequisites

- IntelliJ IDEA
- JDK17
- OpenJFX 22

### Step by step

- Clone and open this repository in IntelliJ IDEA
- Config the project SDK to JDK17 (File -> Project Structure -> Project Settings -> Project)
- Open Maven tool window and run `clean` and `install`
- Open `Run/Debug Configuration` window
- Click New Application and choose JDK17
- Choose `Modify Options` and enable `Add VM Options`
- Config the vm options with this line `--module-path {path to sdk}/javafx-sdk-22.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.web`
- Save and run AppStart to start using our application
