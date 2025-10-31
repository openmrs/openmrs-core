# OpenMRS PlantUML Diagrams

This directory contains PlantUML diagram source files (`.puml`) and their rendered PNG images.

## Directory Structure

- **APIResources/**: API resource model diagrams
- **Conceptual/**: High-level domain model diagrams  
- **DB/**: Database schema and entity relationship diagrams
- **statetransitions/**: State machine and workflow diagrams
- **images/**: Generated PNG images (mirrors the source directory structure)

## Rendering Diagrams

To render all PlantUML diagrams to PNG images, run:

```bash
./render_diagrams.sh
```

This script will:
- Find all `.puml` files recursively in the diagrams directory
- Generate PNG images maintaining the same directory structure in `./images/`
- Create an index file listing all generated images
- Provide a summary of successful and failed renders

### Prerequisites

- PlantUML must be installed and available in your PATH
- On macOS: `brew install plantuml`
- On Ubuntu/Debian: `sudo apt-get install plantuml`

### Output

Generated images are saved in the `./images/` subdirectory with the same folder structure as the source files:

```
diagrams/
├── render_diagrams.sh          # Rendering script
├── APIResources/
│   └── api-resource-model-diagram.puml
├── images/
│   ├── index.md               # Generated index file
│   └── APIResources/
│       └── api-resource-model-diagram.png
└── ...
```

### Script Features

- **Recursive processing**: Finds all `.puml` files in subdirectories
- **Directory structure preservation**: Maintains the same folder hierarchy in images/
- **Error handling**: Reports failed renders with detailed information
- **Index generation**: Creates a markdown index file with links to all images
- **Progress reporting**: Shows real-time rendering progress with colored output

### Troubleshooting

If you encounter rendering errors:

1. Check that PlantUML is installed: `plantuml -version`
2. Verify `.puml` file syntax - comments should use `'` not `//`
3. Run individual files for debugging: `plantuml path/to/diagram.puml`

The script will continue processing other files even if some fail to render.
