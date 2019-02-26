import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GitSpec extends JenkinsPipelineSpecification {

  def Git = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def setup() {
    Git = loadPipelineScriptForTest("github_enterprise/github_enterprise_constructor.groovy")
  }

  /** ghe() or ghe.call()  **/
  def "PLACEHOLDER" () {

  }


}
