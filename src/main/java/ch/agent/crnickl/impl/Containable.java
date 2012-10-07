package ch.agent.crnickl.impl;

/**
 * A Containable can be put into a {@link SchemaComponentContainer}.
 * 
 * @author Jean-Paul Vetterli
 *
 */
public interface Containable {
	
	/**
	 * Set a container.
	 * @param container a container or null
	 */
	void setContainer(SchemaComponentContainer container);
}
