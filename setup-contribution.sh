#!/bin/bash

# OpenMRS Contribution Setup Script
# This script helps set up your Git remotes for contributing to OpenMRS

echo "OpenMRS Contribution Setup"
echo "=========================="
echo ""

# Check if we're in the right directory
if [ ! -f "pom.xml" ] || [ ! -d ".git" ]; then
    echo "Error: Please run this script from the openmrs-core directory"
    exit 1
fi

echo "Current Git remotes:"
git remote -v
echo ""

echo "To set up your contribution workflow:"
echo "1. Fork the repository at https://github.com/openmrs/openmrs-core"
echo "2. Replace YOUR_USERNAME below with your actual GitHub username"
echo "3. Run the following commands:"
echo ""

echo "# Add your fork as origin (replace YOUR_USERNAME with your GitHub username):"
echo "git remote add origin https://github.com/YOUR_USERNAME/openmrs-core.git"
echo ""
echo "# Verify the remotes:"
echo "git remote -v"
echo ""
echo "# Your remotes should look like:"
echo "# origin    https://github.com/YOUR_USERNAME/openmrs-core.git (fetch)"
echo "# origin    https://github.com/YOUR_USERNAME/openmrs-core.git (push)"
echo "# upstream  https://github.com/openmrs/openmrs-core.git (fetch)"
echo "# upstream  https://github.com/openmrs/openmrs-core.git (push)"
echo ""

echo "After setting up the remotes, you can:"
echo "- Create feature branches: git checkout -b TRUNK-123 master"
echo "- Push to your fork: git push origin TRUNK-123"
echo "- Create pull requests from your fork to the main repository"
echo ""

echo "For more details, see CONTRIBUTING.md"
