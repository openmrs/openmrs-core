# Module config.xml DTDs

This directory contains the XML doctype definitions for the various versions of `config.xml`.

More details on the specific elements and the version history can be found here

https://wiki.openmrs.org/display/docs/Module+Config+File

## Usage
These DTD files should be added to the start of a `config.xml` so your IDE can support you with code completion/validation of
your config. Like so

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//OpenMRS//DTD OpenMRS Config 1.0//EN"
                         "http://resources.openmrs.org/doctype/config-1.5.dtd">
<module configVersion="1.5">
...
</module>
```

## Public availability/Build

The DTD files from this folder are synced daily to `http://resources.openmrs.org/doctype/config-x.x.dtd`

via our bamboo CI server and build
https://ci.openmrs.org/browse/DOC-DOC
