package neurord.model;

public interface Particle {
	Site parseSite(String siteStr);
	Site getSite(String siteName);
	String toString();
}
