package org.openmrs.api.db;

import org.openmrs.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterDaoJpa extends JpaRepository<Encounter, Integer> {}
