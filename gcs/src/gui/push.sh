#!/bin/bash
# This script just copies certain files from dist/ and puts them on AOSShare

HOME_DIR="/Users/jonesjp1"
SHARE_DIR="/Volumes"

# skipping the mac distributables at the moment because they're ~2+ GB
# cp -v dist/*.zip "${SHARE_DIR}/AOSShare/Projects and Studies/QPC-4/FruitFly/WACS Software/Custom GCS/"
# cp -v dist/*.zip "${HOME_DIR}/Box/FruitflySoftware/"
# cp -v dist/*.dmg "${SHARE_DIR}/AOSShare/Projects and Studies/QPC-4/FruitFly/WACS Software/Custom GCS/"
# cp -v dist/*.dmg "${HOME_DIR}/Box/FruitflySoftware/"
cp -v dist/*.exe "${SHARE_DIR}/AOSShare/Projects and Studies/QPC-4/FruitFly/WACS Software/Custom GCS/"
cp -v dist/*.exe "${HOME_DIR}/Box/FruitflySoftware/"
