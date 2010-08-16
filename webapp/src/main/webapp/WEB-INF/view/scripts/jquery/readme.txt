jquery.min.js is version 1.4.2

Dropping the version number from the filename allows modules to generically
reference jquery.min.js in trunk, and not worry about breaking if we change
the jquery version in a new release.

=== History ===
OpenMRS 1.7: JQuery 1.4.2, included by default in headerFull.jsp in no-conflict mode
OpenMRS 1.6: JQuery 1.3.2