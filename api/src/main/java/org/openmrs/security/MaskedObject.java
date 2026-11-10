/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import org.openmrs.OpenmrsObject;

/**
 * Marks what {@link Authorize} returns in place of an object a registered
 * {@link DomainObjectAuthorizationRule} refused: still the declared type, so it serializes where
 * the real object would have, but answering every getter as if it held nothing - its identity
 * included, since the uuid it reports is a well-formed but random one opening with
 * {@link Authorize#MASKED_UUID_PREFIX}, and its id is null.
 * <p>
 * It exists so a presentation layer can tell <em>hidden</em> from <em>absent</em>. A reader handed
 * a shorter list cannot; one handed a masked entry can say so, which is the difference between a
 * chart that looks complete and one that admits it is not.
 * <p>
 * Every getter but its identity answers as if it held nothing, which means a consumer that treats a
 * field as always present will fail on one: fhir2's observation status translator calls
 * {@code obs.getStatus().toString()}, and a masked {@code Obs} returns null there. A reader has to
 * null-check what it reads off a mask, or test {@link Authorize#isMasked(Object)} first and render
 * the placeholder instead.
 * <p>
 * A masked object is a serialization-time artifact and must never be written back. Passing one to a
 * {@code save*} method is rejected rather than persisted (see {@code RequiredDataAdvice}), because
 * an authorization proxy reaching Hibernate is read as an unmanaged entity and inserted as new -
 * the failure that ruled out enforcing these rules inside the domain objects themselves.
 *
 * @see Authorize
 * @since 3.0.0
 */
public interface MaskedObject extends OpenmrsObject {

	/**
	 * @return the type of the object that was masked, for a reader deciding what to show in its place
	 */
	Class<?> getMaskedType();
}
