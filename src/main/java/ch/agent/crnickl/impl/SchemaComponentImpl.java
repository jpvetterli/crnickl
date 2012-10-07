package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.SchemaComponent;


public abstract class SchemaComponentImpl implements SchemaComponent, Containable {

	private boolean editMode;
	private SchemaComponentContainer container;
	
	public SchemaComponentImpl() {
	}
	
	@Override
	@Deprecated
	public void consolidate() throws T2DBException {
	}

	@Override
	public void setContainer(SchemaComponentContainer container) {
		this.container = container;
	}
	
	@Override
	public void edit() {
		this.editMode = true;
	}
	
	protected void checkEdit() {
		if (!editMode)
			throw new IllegalStateException();
	}
	
	protected void nameChanged(boolean attribute, String oldName, String newName) throws T2DBException {
		if (container != null)
			this.container.nameChanged(attribute, oldName, newName);
	}

}
