#!/bin/bash

# Script to render all PlantUML diagrams to PNG images
# Usage: ./render_diagrams.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DIAGRAMS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGES_DIR="${DIAGRAMS_DIR}/images"
OUTPUT_FORMAT="png"

echo -e "${BLUE}OpenMRS PlantUML Diagram Renderer${NC}"
echo -e "${BLUE}===================================${NC}"
echo

# Check if PlantUML is available
if ! command -v plantuml &> /dev/null; then
    echo -e "${RED}Error: PlantUML is not installed or not in PATH${NC}"
    echo "Please install PlantUML first:"
    echo "  - On macOS: brew install plantuml"
    echo "  - On Ubuntu/Debian: sudo apt-get install plantuml"
    echo "  - Or download from: http://plantuml.com/download"
    exit 1
fi

# Create images directory if it doesn't exist
mkdir -p "${IMAGES_DIR}"

# Function to create directory structure in images folder
create_image_dir_structure() {
    local puml_file="$1"
    local relative_path="${puml_file#${DIAGRAMS_DIR}/}"
    local dir_path=$(dirname "${relative_path}")
    
    if [[ "${dir_path}" != "." ]]; then
        mkdir -p "${IMAGES_DIR}/${dir_path}"
    fi
}

# Function to render a single PlantUML file
render_puml_file() {
    local puml_file="$1"
    local relative_path="${puml_file#${DIAGRAMS_DIR}/}"
    local output_dir="${IMAGES_DIR}/$(dirname "${relative_path}")"
    local filename=$(basename "${puml_file}" .puml)
    
    # Ensure output directory exists
    mkdir -p "${output_dir}"
    
    echo -e "  ${YELLOW}→${NC} Rendering: ${relative_path}"
    
    # Render the PlantUML file
    if plantuml -t${OUTPUT_FORMAT} -o "${output_dir}" "${puml_file}" > /dev/null 2>&1; then
        echo -e "    ${GREEN}✓${NC} Generated: images/$(dirname "${relative_path}")/${filename}.${OUTPUT_FORMAT}"
        return 0
    else
        echo -e "    ${RED}✗${NC} Failed to render: ${relative_path}"
        return 1
    fi
}

# Find all .puml files and render them
echo -e "${BLUE}Finding PlantUML files...${NC}"
puml_files=($(find "${DIAGRAMS_DIR}" -name "*.puml" -type f | sort))

if [[ ${#puml_files[@]} -eq 0 ]]; then
    echo -e "${YELLOW}No PlantUML files found in ${DIAGRAMS_DIR}${NC}"
    exit 0
fi

echo -e "${GREEN}Found ${#puml_files[@]} PlantUML files${NC}"
echo

# Track statistics
total_files=${#puml_files[@]}
successful_renders=0
failed_renders=0

# Render each file
echo -e "${BLUE}Rendering diagrams...${NC}"
for puml_file in "${puml_files[@]}"; do
    if render_puml_file "${puml_file}"; then
        ((successful_renders++))
    else
        ((failed_renders++))
    fi
done

echo
echo -e "${BLUE}Rendering Summary:${NC}"
echo -e "${GREEN}✓ Successfully rendered: ${successful_renders}${NC}"
if [[ ${failed_renders} -gt 0 ]]; then
    echo -e "${RED}✗ Failed to render: ${failed_renders}${NC}"
fi
echo -e "${BLUE}📁 Images saved to: ${IMAGES_DIR}${NC}"

# Generate an index file listing all generated images
index_file="${IMAGES_DIR}/index.md"
echo -e "${BLUE}📝 Generating index file: ${index_file}${NC}"

cat > "${index_file}" << 'EOF'
# OpenMRS PlantUML Diagrams Index

This directory contains automatically generated images from PlantUML source files.

## Generated Images

EOF

# Add links to all generated images
find "${IMAGES_DIR}" -name "*.${OUTPUT_FORMAT}" -type f | sort | while read -r image_file; do
    relative_image_path="${image_file#${IMAGES_DIR}/}"
    image_name=$(basename "${image_file}" .${OUTPUT_FORMAT})
    dir_name=$(dirname "${relative_image_path}")
    
    if [[ "${dir_name}" == "." ]]; then
        echo "### ${image_name}" >> "${index_file}"
        echo "![${image_name}](./${relative_image_path})" >> "${index_file}"
    else
        echo "### ${dir_name}/${image_name}" >> "${index_file}"
        echo "![${image_name}](./${relative_image_path})" >> "${index_file}"
    fi
    echo >> "${index_file}"
done

echo -e "${GREEN}✓ Index file generated${NC}"
echo
echo -e "${GREEN}All done! 🎉${NC}"

if [[ ${failed_renders} -eq 0 ]]; then
    exit 0
else
    exit 1
fi
