====
    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.
====

jquery.min.js is version 1.7.1

Dropping the version number from the filename allows modules to generically
reference jquery.min.js in trunk, and not worry about breaking if we change
the jquery version in a new release.

=== History ===
OpenMRS 1.9: JQuery 1.7.1
OpenMRS 1.7: JQuery 1.4.2, included by default in headerFull.jsp in no-conflict mode
OpenMRS 1.6: JQuery 1.3.2