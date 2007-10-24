/**
 * Auto generated file comment
 */
package org.openmrs.synchronization.filter;

/**
 *
 */
public enum SyncClassType {
    /*
     * Items that must be sync'ed for anything to work - user, role, privilege, etc
     */
    REQUIRED,
    
    /*
     * Items related to patient data
     */
    PATIENT,

    /*
     * Items related to person data
     */
    PERSON,

    /*
     * Items related to the dictionary
     */
    DICTIONARY,
    
    /*
     * Items related to forms
     */
    FORM,
    
    /*
     * Miscellaneous items - like global properties
     */
    MISC
}
