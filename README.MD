
# 🚛 AGV Warehouse System (JavaFX)

A JavaFX-based simulation system for an **Automated Guided Vehicle (AGV) cold-chain warehouse**, featuring real-time visualization, task scheduling, and temperature-controlled storage management.

---

## 📌 Overview

This project simulates a smart warehouse where AGVs transport items between temperature-controlled storage zones. It demonstrates task scheduling, automated sorting, and real-time system visualization.

---

## ✨ Features

### 🚚 AGV System
- Multiple AGVs with independent states
- Battery level monitoring
- Busy / idle status tracking

### 📦 Task Management
- Priority-based task queue
- Automatic sorting of items
- Manual task creation via UI
- Support for transport and charging tasks

### 🌡️ Cold Chain Storage
- Temperature-controlled zones:
  - ❄️ -20°C Cold storage
  - 🌤️ 20°C Room temperature storage
  - 🔥 30°C Hot storage
- Temperature validation for safe transport

### 🏭 Warehouse Visualization
- JavaFX real-time warehouse map
- Displays:
  - AGVs
  - Storage zones
  - Charging stations
  - Unsorted area
- Live updates every 200ms

### 📊 Monitoring & Logging
- Task queue viewer
- Active task tracking
- AGV status panel
- Console output viewer
- System logging support

---


## ▶️ How to Run

### Requirements
- Java 11+
- JavaFX SDK
- IDE (IntelliJ IDEA recommended)

### Run Configuration
Add JavaFX VM options:

```bash
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
````

### Run Main Class

```
ui.AGVWarehouseSystem
```

---

## ⚙️ System Workflow

### Startup

* Initializes AGVs, zones, and charging stations
* Loads sample items into unsorted area
* Auto-generates sorting tasks after startup

### Auto Sorting

* Items are grouped by required temperature
* Tasks are created automatically
* Distributed across matching storage zones

---

## 🖥️ UI Tabs

### 🚚 Warehouse Operations

* Task creation panel
* Warehouse visualization
* Status monitoring panel

### 📋 Log Viewer

* System event logs

### 💻 Console Output

* Real-time logs and debug output

---

## 🔧 Key Logic

### Task Creation Rules

* Source and destination must differ
* Temperature difference must be ≤ 5°C
* Item must exist in source zone

### Scheduling

* Priority-based execution
* Higher priority tasks executed first

---

## 🚀 Future Improvements

* Pathfinding for AGVs (A*, Dijkstra)
* Collision detection system
* Database persistence
* REST API integration
* AI-based task optimization

---




