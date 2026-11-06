# PhotoPaint XP

A raster image editor in the style of Windows XP. Written in Java (Swing/Java2D) with
no external libraries: the interface, icons and controls are all drawn in code.

## Features

- Multi-layer editing: create, duplicate, delete, merge and reorder layers, with
  per-layer visibility and opacity.
- Tools: rectangle selection, lasso (free-form) selection, move, pencil, brush, eraser,
  fill, gradient, line, rectangle, ellipse, text, eyedropper, zoom, hand.
- Adjustments and filters: brightness/contrast, hue/saturation, grayscale, invert,
  sepia, blur, sharpen, emboss, edge detection and more.
- Import an image into a new layer.
- Native `.ppxp` project format — a ZIP archive with a manifest and a separate PNG
  for each layer. It preserves canvas size, layer order, names, visibility and opacity.
- Open `.ppxp` by double-clicking in Explorer (if the format is associated with the app).
- Interface in Russian or English, styled after Windows XP (Luna).

## Download

The ready-to-run portable build is available in the **Releases** section of this repository.

1. Download the archive with the application.
2. Extract it to any folder.
3. Run `PhotoPaint XP.exe`.

No installation and no separate Java are required: the runtime is bundled with the build.

On first launch the application asks you to choose the interface language — Russian or
English. You can change it later in **Edit → Settings...**; changes apply immediately.

The chosen language is stored in `Settings\settings.properties` next to
`PhotoPaint XP.exe`. If the folder is read-only, the application works as usual, but the
language is not remembered. Other settings are not persisted.

## Requirements

- Windows 10 or Windows 11 (64-bit).

## Controls

Tools: `M` — selection, `A` — lasso, `V` — move, `N` — pencil, `B` — brush,
`E` — eraser, `F` — fill, `G` — gradient, `L` — line, `R` — rectangle,
`O` — ellipse, `T` — text, `I` — eyedropper, `Z` — zoom, `H` — hand.

Main commands: `Ctrl+N` — new document, `Ctrl+O` — open, `Ctrl+S` — save,
`Ctrl+Shift+S` — save as, `Ctrl+Z` — undo, `Ctrl+Y` — redo,
`Ctrl+A` — select all, `Ctrl+D` — deselect.

## Building from source

You need JDK 27 and PowerShell. From the `Source` folder:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

The script compiles the sources, generates the icon and builds the portable version
into the `Releases\PhotoPaint XP` folder next to the repository folder. It contains
`PhotoPaint XP.exe`, `app` and the bundled `runtime`.

To run from the compiled classes without packaging, use `Source\run.bat`
(run the build first).

## The .ppxp format

`.ppxp` is a plain ZIP archive:

- `manifest.properties` — format version, image size, active layer, number of layers
  and the parameters of each layer;
- `layer0.png`, `layer1.png`, ... — lossless layers.

The file can be opened with any archive tool, and the layers viewed as ordinary PNGs.
