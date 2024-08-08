package neurord.model;

public class Site {
	private String name;
	private String state;
	
	public Site(String name, String state) {
		this.name = name;
		this.state = state;
	}
	
	public String getName() {
		return name;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return "Site{name='" + name + "', state='" + state + "'}";
	}

}
