package isabel.tool;

import java.util.List;
import java.util.Set;

public interface ValidationResult {

	public List<Violation> getViolations();

	public boolean isValid();

	public Set<Integer> getViolatedRules();
}
