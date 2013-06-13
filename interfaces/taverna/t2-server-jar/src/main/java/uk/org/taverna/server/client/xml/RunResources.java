package uk.org.taverna.server.client.xml;

import java.net.URI;
import java.util.Map;

public final class RunResources extends AbstractResources {

	private final String owner;

	RunResources(Map<ResourceLabel, URI> links, String owner) {
		super(links);

		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}
}
