import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GheSpec extends JenkinsPipelineSpecification {

  def Ghe = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  /*
   * leaving both ghApiLib and ghApiLib2 in here for reference
   * this is two ways to handle directly calling a method from a library
   * w/o actually importing that library in the script.
   * i.e. org.kohsuke.github.GitHub.connectToEnterprise(ghUrl, PAT)
   */
  def ghApiLib = [kohsuke: [github: [GitHub: [connectToEnterprise: { url, pwd -> echo "url: ${url}"; echo "pwd: ${pwd}"; echo "Did call connectToEnterprise with url: ${url} and token: ${pwd}"}]]]]
  def ghApiLib2 = null
  def baseUrl = "https://my-github-enterprise.example.com"


  def setup() {
    Ghe = loadPipelineScriptForTest("github_enterprise/ghe.groovy")

    explicitlyMockPipelineVariable("GitHub")
    ghApiLib2 = [kohsuke: [github: [GitHub: getPipelineMock("GitHub")]]]

    Ghe.getBinding().setVariable("env", [GIT_URL: "${baseUrl}/my-org/my-repo.git", ORG_NAME: "testorg", REPO_NAME: "testrepo", CHANGE_ID: "25"])
    Ghe.getBinding().setVariable("USER", "testuser")
    Ghe.getBinding().setVariable("PAT", "testtoken")
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
      Ghe.getBinding().setVariable("org", ghApiLib)
    when:
      def ghbuilder = Ghe.gh()
    then:
      1 * getPipelineMock("usernamePassword.call").call(['credentialsId':'github', 'passwordVariable':'PAT', 'usernameVariable':'USER'])
  }

  def "The gh() step uses the GIT_URL env var to build the API URL" () {
    setup:
      Ghe.getBinding().setVariable("org", ghApiLib)
    when:
      def ghbuilder = Ghe.gh()
    then:
      // relies on 'echo' call in ghApiLib
      1 * getPipelineMock('echo')("url: ${baseUrl}/api/v3")

  }

  def "The gh() step uses the connectToEnterprise() step with the given URL and password to connect" () {
    setup:
      explicitlyMockPipelineVariable("GitHub")
      Ghe.getBinding().setVariable("org", ghApiLib2)
    when:
      def test_gh_connection = Ghe.gh()
    then:
      1 * getPipelineMock("GitHub.connectToEnterprise")("${baseUrl}/api/v3", "testtoken")
  }

  /** ghe.getRepo() **/
  def "The getRepo() step uses the GH server connection made using gh() and gets the repository env.REPO_NAME from env.ORG_NAME" () {
    setup:
      Ghe.getBinding().setVariable("org", ghApiLib2)
      explicitlyMockPipelineVariable("gh_connection")
      //the return value of withCredentials is the return value of gh()
      getPipelineMock("withCredentials")(_, _ as Closure) >> { getPipelineMock("gh_connection") }
    when:
      def test_gh_repository = Ghe.getRepo()
    then:
      1  * getPipelineMock("gh_connection.getRepository")("testorg/testrepo")
  }

  /** ghe.pr() **/
  def "The pr() step queries the repository from getRepo() for the PR whose number matches env.CHANGE_ID" () {
    setup:
      Ghe.getBinding().setVariable("org", ghApiLib2)
      explicitlyMockPipelineVariable("gh_connection")
      explicitlyMockPipelineVariable("gh_repository")
      //the return value of withCredentials is the return value of gh()
      getPipelineMock("withCredentials")(_, _ as Closure) >> { getPipelineMock("gh_connection") }
      getPipelineMock("gh_connection.getRepository")(_) >> { getPipelineMock("gh_repository") }
    when:
      def test_gh_pr = Ghe.pr()
    then:
      1 * getPipelineMock("gh_repository.getPullRequest")(25)
  }

  /*
   * Note: not testing any corner cases with CHANGE_ID b/c we expect it to be
   * handled by either github_enterprise_constructor or the external
   * getPullRequest(int) method.
   */

}
