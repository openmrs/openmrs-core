public List<Patient> getPatientsByIdentifier(String name, String identifier,
	        List<PatientIdentifierType> identifierTypes, boolean matchIdentifierExactly) throws APIException {
		
		if (identifierTypes == null) {
			identifierTypes = Collections.emptyList();
		}
		
		return dao.getPatientsByIdentifier(name, identifier, identifierTypes, matchIdentifierExactly);
	}
