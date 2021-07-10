/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.springdata;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.springdata.repository.JpaEncounterDao;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Encounter-related database functions
 * 
 * @See org.openmrs.api.db.springdata.JpaEncounterDao
 */
public class SpringDataEncounterDAO implements JpaEncounterDao {
	@Override
	public List<Encounter> findAll() throws DAOException {
		return null;
	}

	@Override
	public List<Encounter> findAll(Sort sort) {
		return null;
	}

	@Override
	public Page<Encounter> findAll(Pageable pageable) {
		return null;
	}

	@Override
	public List<Encounter> findAllById(Iterable<Integer> iterable) {
		return null;
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public void deleteById(Integer integer) {

	}

	/**
	 * Purge an encounter from database.
	 *
	 * @param encounter encounter object to be purged
	 */
	@Override
	public void delete(Encounter encounter) {

	}

	@Override
	public void deleteAll(Iterable<? extends Encounter> iterable) {

	}

	@Override
	public void deleteAll() {

	}

	/**
	 * Saves an encounter
	 *
	 * @param encounter to be saved
	 * @throws DAOException
	 */
	@Override
	public <S extends Encounter> S save(S s) {
		return null;
	}

	@Override
	public <S extends Encounter> List<S> saveAll(Iterable<S> iterable) {
		return null;
	}

	@Override
	public Optional<Encounter> findById(Integer integer) {
		return Optional.empty();
	}

	@Override
	public boolean existsById(Integer integer) {
		return false;
	}

	@Override
	public void flush() {

	}

	@Override
	public <S extends Encounter> S saveAndFlush(S s) {
		return null;
	}

	@Override
	public void deleteInBatch(Iterable<Encounter> iterable) {

	}

	@Override
	public void deleteAllInBatch() {

	}

	@Override
	public Encounter getOne(Integer integer) {
		return null;
	}

	@Override
	public <S extends Encounter> Optional<S> findOne(Example<S> example) {
		return Optional.empty();
	}

	@Override
	public <S extends Encounter> List<S> findAll(Example<S> example) {
		return null;
	}

	@Override
	public <S extends Encounter> List<S> findAll(Example<S> example, Sort sort) {
		return null;
	}

	@Override
	public <S extends Encounter> Page<S> findAll(Example<S> example, Pageable pageable) {
		return null;
	}

	@Override
	public <S extends Encounter> long count(Example<S> example) {
		return 0;
	}

	@Override
	public <S extends Encounter> boolean exists(Example<S> example) {
		return false;
	}

	@Override
	public Encounter getEncounter(Integer encounterId) throws DAOException {
		return null;
	}

	@Override
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws DAOException {
		return null;
	}

	@Override
	public List<Encounter> getEncounters(EncounterSearchCriteria encounterSearchCriteria) {
		return null;
	}

	@Override
	public EncounterType saveEncounterType(EncounterType encounterType) {
		return null;
	}
}
