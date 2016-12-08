package org.openmrs.api.db.hibernate.search;

import java.util.List;

public class SkipSame {

    private LuceneQuery luceneQuery;
    private String fieldOfQuery;

    public SkipSame(LuceneQuery luceneQuery, String fieldOfQuery) {
        this.luceneQuery = luceneQuery;
        this.fieldOfQuery = fieldOfQuery;
    }

    public String getFieldName() {
        return fieldOfQuery;
    }

    public LuceneQuery getLuceneQuery() {
        return luceneQuery;
    }

    public List<Object> getProjectionList(){
        return luceneQuery.listProjection(fieldOfQuery);
    }
}
