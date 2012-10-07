package ch.agent.crnickl.impl;

import ch.agent.crnickl.T2DBException;

/**
 * A SchemaComponentContainer must know when the name of a
 * component changes.
 * 
 * @author Jean-Paul Vetterli
 *
 */
public interface SchemaComponentContainer {

	/**
	 * Tell the container that the name of a component was changed. The
	 * arguments old name and new name have different values, one of which can
	 * be null.
	 * 
	 * @param attribute
	 *            if true the changes concerns an attribute, else a series
	 * @param oldName
	 *            old name
	 * @param newName
	 *            new name
	 * @throws T2DBException if a duplicate is found
	 */
	void nameChanged(boolean attribute, String oldName, String newName) throws T2DBException;

}
