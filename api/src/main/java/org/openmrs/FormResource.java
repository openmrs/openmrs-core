package org.openmrs;

import java.util.Date;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.customdatatype.SingleCustomValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "form_resource")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AttributeOverride(name = "uuid", column = @Column(name = "uuid", unique = true, nullable = false, length = 38))
@Audited
public class FormResource extends BaseOpenmrsObject implements CustomValueDescriptor, SingleCustomValue<FormResource> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "form_resource_id")
	private Integer formResourceId;
	
	@ManyToOne
	@JoinColumn(name = "form_id", nullable = false)
	private Form form;
	
	@Column(name = "name", length = 255, nullable = true)
	private String name;
	
	@Lob
	@Column(name = "value_reference", length = 65535, nullable = true)
	private String valueReference;
	
	@Column(name = "datatype", length = 255)
	private String datatypeClassname;
	
	@Lob
	@Column(name = "datatype_config", length = 65535)
	private String datatypeConfig;
	
	@Column(name = "preferred_handler", length = 255)
	private String preferredHandlerClassname;
	
	@Lob
	@Column(name = "handler_config", length = 65535)
	private String handlerConfig;
	
	private transient boolean dirty = false;
	
	private transient Object typedValue;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed", length = 19)
	private Date dateChanged;
	
	public FormResource() {
	}
	
	/**
	 * Copy constructor
	 */
	public FormResource(FormResource old) {
		
		Objects.requireNonNull(old, "FormResource to copy must not be null");
		
		this.form = old.getForm();
		this.name = old.getName();
		
		// Direct field copy to avoid NotYetPersistedException
		this.valueReference = old.valueReference;
		
		this.datatypeClassname = old.getDatatypeClassname();
		this.datatypeConfig = old.getDatatypeConfig();
		this.preferredHandlerClassname = old.getPreferredHandlerClassname();
		this.handlerConfig = old.getHandlerConfig();
	}
	
	@Override
	public Integer getId() {
		return getFormResourceId();
	}
	
	@Override
	public void setId(Integer id) {
		setFormResourceId(id);
	}
	
	public Form getForm() {
		return form;
	}
	
	public void setForm(Form form) {
		this.form = form;
	}
	
	public Integer getFormResourceId() {
		return formResourceId;
	}
	
	public void setFormResourceId(Integer formResourceId) {
		this.formResourceId = formResourceId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getDatatypeClassname() {
		return datatypeClassname;
	}
	
	public void setDatatypeClassname(String datatypeClassname) {
		this.datatypeClassname = datatypeClassname;
	}
	
	@Override
	public String getDatatypeConfig() {
		return datatypeConfig;
	}
	
	public void setDatatypeConfig(String datatypeConfig) {
		this.datatypeConfig = datatypeConfig;
	}
	
	@Override
	public String getPreferredHandlerClassname() {
		return preferredHandlerClassname;
	}
	
	public void setPreferredHandlerClassname(String preferredHandlerClassname) {
		this.preferredHandlerClassname = preferredHandlerClassname;
	}
	
	@Override
	public String getHandlerConfig() {
		return handlerConfig;
	}
	
	public void setHandlerConfig(String handlerConfig) {
		this.handlerConfig = handlerConfig;
	}
	
	@Override
	public FormResource getDescriptor() {
		return this;
	}
	
	@Override
	public String getValueReference() {
		if (valueReference == null) {
			throw new NotYetPersistedException();
		}
		return valueReference;
	}
	
	@Override
	public Object getValue() {
		if (typedValue == null) {
			typedValue = CustomDatatypeUtil.getDatatype(this).fromReferenceString(getValueReference());
		}
		return typedValue;
	}
	
	@Override
	public <T> void setValue(T typedValue) {
		this.typedValue = typedValue;
		dirty = true;
	}
	
	@Override
	public void setValueReferenceInternal(String valueToPersist) {
		this.valueReference = valueToPersist;
	}
	
	@Deprecated
	@JsonIgnore
	@Override
	public boolean isDirty() {
		return getDirty();
	}
	
	public boolean getDirty() {
		return dirty;
	}
	
	public User getChangedBy() {
		return changedBy;
	}
	
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	public Date getDateChanged() {
		return dateChanged;
	}
	
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
