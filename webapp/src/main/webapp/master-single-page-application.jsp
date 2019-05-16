<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Open MRS</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="importmap-type" content="systemjs-importmap">
    <link rel="preload" href="${cookie['import-map-override-url'] == null ? '/import-map.json' : cookie['import-map-override-url'].getValue()}" as="script" crossorigin="anonymous" />
    <script type='systemjs-importmap' src="${cookie['import-map-override-url'] == null ? '/import-map.json' : cookie['import-map-override-url'].getValue()}"></script>
    <script src="https://unpkg.com/import-map-overrides@1.5.0/dist/import-map-overrides.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/systemjs/3.1.6/system.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/systemjs/3.1.6/extras/amd.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/systemjs/3.1.6/extras/named-exports.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/systemjs/3.1.6/extras/named-register.min.js"></script>
    <script>
      System.import("@openmrs/root-config")
    </script>
  </head>
  <body>
  </body>
</html>