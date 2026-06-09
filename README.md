# FloatPicture

[![codebeat badge](https://codebeat.co/badges/b1b7bd81-7e45-487e-ad8b-539a71df2f09)](https://codebeat.co/projects/github-com-xfy9326-floatpicture-master)

[中文说明](README_CN.md)

---

FloatPicture is an Android floating window app that lets you display any custom image as an overlay on your screen (GIF not supported).

**Note: The [original repository](https://github.com/XFY9326/FloatPicture) is no longer maintained.**

*Licensed under GNU GPL 3.0*

---

## Features

### Per-image Settings

Each image has its own settings, accessible via **Options → Edit** in the image list:

- **Picture Name** — Give each image a custom name for identification
- **Resize** — Scale the image and window proportionally
- **Rotate** — Rotate the image to any angle (0–360°)
- **Transparency** — Adjust opacity from 0 to 100%
- **Window Position** — Set X/Y coordinates manually or drag to move
- **Touch & Move** — Enable dragging the image with your finger
- **Allow Over Layout** — Let the floating window extend beyond screen bounds

### Fill Screen

When enabled, the floating window expands to fill the entire screen. The image scales to cover the screen while preserving aspect ratio (like `CENTER_CROP`) — no stretching.

**How it works:**
- `FloatImageView` switches from `ScaleType.MATRIX` to `ScaleType.CENTER_CROP`
- Window layout params change from `WRAP_CONTENT` to `MATCH_PARENT` at position `(0, 0)`
- The original unscaled bitmap is used; Android's built-in center-crop handles the display

### Show Only in Specific App

Restrict a floating window so it only appears when a chosen app is in the foreground. Useful for displaying reference images that should only show over a specific application.

**Setup:**
1. Enable the toggle in **Picture Control Settings**
2. Tap **Select target app** — a dialog lists all installed launchable apps
3. Grant **Usage Access** permission when prompted (required for foreground detection)
4. Choose the target app from the list

The floating window automatically shows when you switch to the target app and hides when you leave it.

---

## Foreground App Detection

The app uses `UsageStatsManager.queryEvents()` to detect which app is currently in the foreground:

```java
UsageEvents events = usm.queryEvents(beginTime, endTime);
// Iterate events for the most recent MOVE_TO_FOREGROUND
```

**Permission required:** `PACKAGE_USAGE_STATS` — user grants via Settings → Usage Access.

---

## Permissions

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` / `TYPE_APPLICATION_OVERLAY` | Display floating windows |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | Read and cache picture files |
| `RECEIVE_BOOT_COMPLETED` | Auto-start windows on reboot |
| `FOREGROUND_SERVICE` | Keep notification service running |
| `PACKAGE_USAGE_STATS` | Detect foreground app (for app filter) |

The `<queries>` manifest element queries launchable apps for the app picker dialog.

---

## Architecture

### Window Management

All floating windows are managed through `WindowsMethods`:
- `createWindow()` — adds a view to the `WindowManager` (safe for re-attached views)
- `updateWindow()` — updates layout params or re-adds if detached
- `safeRemoveView()` — removes only if the view is currently attached

These safety checks prevent crashes when the foreground monitor asynchronously adds/removes windows.

### Data Storage

Per-image settings are stored as JSON files under `FloatPicture/Data/`:
- `PictureList.list` — maps image ID to display name
- `PictureData.list` — maps image ID to settings JSON object

Key data fields:
| Field | Type | Description |
|---|---|---|
| `SHOW_ENABLED` | boolean | Master visibility toggle |
| `POSITION_X/Y` | int | Window coordinates |
| `ZOOM` | float | Scale factor |
| `DEFAULT_ZOOM` | float | Calculated default scale |
| `ALPHA` | float | Transparency |
| `DEGREE` | float | Rotation angle |
| `TOUCH_AND_MOVE` | boolean | Drag to move |
| `ALLOW_PICTURE_OVER_LAYOUT` | boolean | Extend beyond screen |
| `FILL_SCREEN` | boolean | Full-screen mode |
| `FILTER_APP_ENABLED` | boolean | App filter toggle |
| `FILTER_APP_PACKAGE` | string | Target app package name |
| `FILTER_APP_NAME` | string | Target app display name |

### Image Rendering

Images are rendered using `ScaleType.MATRIX` by default with bitmap-level scaling via `ImageMethods.resizeBitmap()`. Matrix transformations handle zoom and rotation. For fill-screen mode, `ScaleType.CENTER_CROP` is used instead.
