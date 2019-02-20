import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GheSpec extends JenkinsPipelineSpecification {

  def Ghe = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def ghApiLib = [kohsuke: [github: [GitHub: [connectToEnterprise: { url, pwd -> echo "url: ${url}"; echo "pwd: ${pwd}"; echo "Did call connectToEnterprise with url: ${url} and token: ${pwd}"; [getRepository: {string -> println "${string}"}]}]]]]

  def setup() {
    Ghe = loadPipelineScriptForTest("github_enterprise/ghe.groovy")
  }

  /** ghe() or ghe.call()  **/
  def "ghe() fails" () {
    when:
      try {
        Ghe()
      } catch (DummyException e) {}
    then:
      1 * getPipelineMock("error")("Step ghe() is not supported. Did you mean ghe.gh(), ghe.getRepo(), or ghe.pr()?") >> {throw new DummyException("can't call ghe()")}
  }

  /** ghe.gh() **/
  def "The gh() step uses the credential with id: github" () {
    setup:
      Ghe.getBinding().setVariable("USER", "testuser")
      Ghe.getBinding().setVariable("PAT", "testtoken")
      //Ghe.getBinding().setVariable("org", ghApiLib)
      Ghe.getBinding().setVariable("env", [GIT_URL: "https://my-github-enterprise.example.com/my-org/my-repo.git"])
      explicitlyMockPipelineVariable("org")
      getPipelineMock("org")(_) >> explicitlyMockPipelineVariable("kohsuke")
    when:
      def ghbuilder = Ghe.gh()
    then:
      1 * getPipelineMock("usernamePassword.call").call(['credentialsId':'github', 'passwordVariable':'PAT', 'usernameVariable':'USER'])
  }

  def "The gh() step uses the GIT_URL env var to build the API URL" () {
    setup:
      Ghe.getBinding().setVariable("USER", "testuser")
      Ghe.getBinding().setVariable("PAT", "testtoken")
      Ghe.getBinding().setVariable("org", ghApiLib)
      def baseUrl = "https://my-github-enterprise.example.com"
      Ghe.getBinding().setVariable("env", [GIT_URL: "${baseUrl}/my-org/my-repo.git"])
    when:
      def ghbuilder = Ghe.gh()
    then:
      // relies on 'echo' call in ghApiLab
      1 * getPipelineMock('echo')("url: ${baseUrl}/api/v3")

  }

  def "The gh() step uses the connectToEnterprise() step with the given URL and password to connect" () {
    setup:
      Ghe.getBinding().setVariable("USER", "testuser")
      Ghe.getBinding().setVariable("PAT", "testtoken")
      Ghe.getBinding().setVariable("org", ghApiLib)
      def baseUrl = "https://my-github-enterprise.example.com"
      Ghe.getBinding().setVariable("env", [GIT_URL: "${baseUrl}/my-org/my-repo.git"])
    when:
      def ghbuilder = Ghe.gh()
    then:
      // relies on 'echo' call in ghApiLab
      1 * getPipelineMock('echo')("Did call connectToEnterprise with url: ${baseUrl}/api/v3 and token: testtoken")
  }

  /** ghe.getRepo() **/
  // def "The getRepo() step invokes the gh() step gets the repository specified by ORG_NAME and REPO_NAME env vars" () {
  //   setup:
  //     def baseUrl = "https://my-github-enterprise.example.com"
  //     Ghe.getBinding().setVariable("org", ghApiLib)
  //     Ghe.getBinding().setVariable("USER", "testuser")
  //     Ghe.getBinding().setVariable("PAT", "testtoken")
  //     Ghe.getBinding().setVariable("env", [GIT_URL: "${baseUrl}/my-org/my-repo.git", ORG_NAME: "testorg", REPO_NAME: "testrepo"])
  //   when:
  //     repo = Ghe.getRepo()
  //   then:
  //     true

      //1 * getPipelineMock("GitHubBuilder.getRepository")("testorg/testrepo")
//  }

  /** ghe.pr() **/

}
