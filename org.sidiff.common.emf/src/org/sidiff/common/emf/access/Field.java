package org.sidiff.common.emf.access;

import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

public class Field {

	private final EObject container;
	private final EAttribute type;
	private final String value;

	public Field(EObject container, EAttribute type, String value) {
		this.container = Objects.requireNonNull(container);
		this.type = Objects.requireNonNull(type);
		this.value = value;
	}

	public EObject getContainer() {
		return container;
	}

	public EAttribute getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(!(o instanceof Field)) {
			return false;
		}
		Field field = (Field)o;
		return this.container.equals(field.container) && this.type.equals(field.type);
	}

	@Override
	public int hashCode(){
		return Objects.hash(container, type);
	}

	@Override
	public String toString() {
		return "Field[" + container + "." + type.getName() + "=" + value + "]";
	}
}
