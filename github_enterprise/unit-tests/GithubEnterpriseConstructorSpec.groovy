import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GithubEnterpriseConstructorSpec extends JenkinsPipelineSpecification {

  def GithubEnterpriseConstructor = null
  def context = [:] // Required for methods w/ the @Init annotation, but not used in the step

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def setup() {
    GithubEnterpriseConstructor = loadPipelineScriptForTest("github_enterprise/github_enterprise_constructor.groovy")
    GithubEnterpriseConstructor.getBinding().setVariable("env", [GIT_URL: "giturl", ORG_NAME: "orgname", REPO_NAME: "reponame", GIT_SHA: "gitsha", CHANGE_TARGET: false, GIT_BUILD_CAUSE: "gitbuildcause"])
    explicitlyMockPipelineVariable("ScmUserRemoteConfig")
    explicitlyMockPipelineVariable("scm")
    getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
    getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/boozallen/jenkins-templating-engine.git"
    getPipelineMock("sh")(_ as Map) >> " 1 "
  }

  def "PLACEHOLDER" () {

  }




}
