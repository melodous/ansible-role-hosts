import utilities.helpers

def jobDescription = 'Ansible hosts role'
//Branch which create tags
def primaryBranch = 'master'
//Role name
def roleName = 'ansible-hosts'
//Platforms
def platforms = "rhel6 rhel7"
//Slack Channel
def slackChannel = "#instel-roles-release"
//Slack integration token
def slackToken = "eIyCGd7EsCFDSN888jSjdEH8"
//Slack Team
def slackTeam = "tid"

//Mail destinations (read from jenkins globar var)
//def ansibleRolesMailRecipient = 'raul.melofernandez@amaris.com'

//Create folder
folder('jobs')

//Create and define jobs steps
def mainJob = multiJob("${SEED_PROJECT}-${SEED_BRANCH}-build") {
    description "$jobDescription"
    logRotator(-1, 40)
    wrappers {
        environmentVariables {
            groovy("""
def build = Thread.currentThread().executable
def version = new File(build.workspace.toString()+"/VERSION").text
def ReleaseName = ""
if ( "$SEED_BRANCH" == "master" ) {
    ReleaseName = "\${BUILD_ID}"
} else if ( "$SEED_BRANCH".startsWith("release") ) {
    ReleaseName = "\${BUILD_ID}rc"
} else {
    ReleaseName = "\${BUILD_ID}-${SEED_BRANCH}"
}
def finalReleaseName = ReleaseName.replaceAll("-","_")
new File("/tmp/${SEED_PROJECT}-${SEED_BRANCH}-parameters").write("VERSION_MASTER=\${version}BUILD_ID_MASTER=\$BUILD_ID\\nFINAL_BUILD_ID=\${finalReleaseName}")
return [VERSION: version.trim(), FINAL_BUILD_ID: finalReleaseName]
""")
        }
    }
    steps {
        phase('Lints and validations') {
            phaseJob("jobs/yamllint"){
                parameters {
                     propertiesFile("/tmp/${SEED_PROJECT}-${SEED_BRANCH}-parameters", true)
                     gitRevision()
                     killPhaseCondition('NEVER')
                }
            }
            phaseJob("jobs/ansiblelint"){
                parameters {
                     propertiesFile("/tmp/${SEED_PROJECT}-${SEED_BRANCH}-parameters", true)
                     gitRevision()
                     killPhaseCondition('NEVER')
                }
            }
        }
        phase('Infraestructure Tests') {
            for(platform in platforms.split(" ")) {
                phaseJob("jobs/testinfra-${platform}"){
                    parameters {
                        propertiesFile("/tmp/${SEED_PROJECT}-${SEED_BRANCH}-parameters", true)
                        gitRevision()
                        killPhaseCondition('NEVER')
                    }
                }
            }
        }
   }
    publishers {
        postBuildScripts {
            steps {
                shell("""
rm /tmp/${SEED_PROJECT}-${SEED_BRANCH}-parameters || true
"""
                    )
            }
            onlyIfBuildSucceeds(false)
        }
    }

}
helpers.addScm(mainJob,PROJECT_SCM_URL,PROJECT_SCM_CREDENTIALS,BRANCH)
helpers.setBuildName(mainJob, '${ENV,var="VERSION"}-${ENV,var="FINAL_BUILD_ID"}')
helpers.addGithubNotifier(mainJob)
helpers.addSlackNotifier(mainJob,slackChannel,slackTeam,slackToken)
//if primary branch  create git tag and send mail
if (BRANCH == primaryBranch) {
    helpers.addCreateTag(mainJob,"v\${VERSION}")
    helpers.addExtendenMailAlways(mainJob,mailRecipient,jobDescription,'${VERSION}')
} else {
    helpers.addExtendenMailFirstFailure(mainJob,mailRecipient,jobDescription,'${VERSION}')    
} 

def yamlLintTest = freeStyleJob("jobs/yamllint") {
    description "$jobDescription (child yamllint)"
    logRotator(-1, 40)
    parameters {
        stringParam("VERSION_MASTER")
        stringParam("BUILD_ID_MASTER")
        stringParam("FINAL_BUILD_ID")
    }
    steps {
        shell """
cd \$WORKSPACE
VENV=/tmp/.venvs/${SEED_PROJECT}-${SEED_BRANCH}/yamllint make yamllint
"""
    }

}
//Add elements to jobs
helpers.addScm(yamlLintTest,PROJECT_SCM_URL,PROJECT_SCM_CREDENTIALS,BRANCH)
helpers.setBuildName(yamlLintTest, '${ENV,var="VERSION_MASTER"}-${ENV,var="FINAL_BUILD_ID"}')
helpers.setConsoleColor(yamlLintTest)

def ansibleLintTest = freeStyleJob("jobs/ansiblelint") {
    description "$jobDescription (child ansiblelint)"
    logRotator(-1, 40)
    parameters {
        stringParam("VERSION_MASTER")
        stringParam("BUILD_ID_MASTER")
        stringParam("FINAL_BUILD_ID")
    }
    steps {
        shell """
cd \$WORKSPACE
VENV=/tmp/.venvs/${SEED_PROJECT}-${SEED_BRANCH}/ansiblelint make ansiblelint
"""
    }

}
//Add elements to jobs
helpers.addScm(ansibleLintTest,PROJECT_SCM_URL,PROJECT_SCM_CREDENTIALS,BRANCH)
helpers.setBuildName(ansibleLintTest, '${ENV,var="VERSION_MASTER"}-${ENV,var="FINAL_BUILD_ID"}')
helpers.setConsoleColor(ansibleLintTest)


//Child job for infraestructure tests
for(platform in platforms.split(" ")) {
    def testJob = freeStyleJob("jobs/testinfra-${platform}") {
        description "$jobDescription (child testinfra ${platform})"
        logRotator(-1, 40)
        parameters {
            stringParam("VERSION_MASTER")
            stringParam("BUILD_ID_MASTER")
            stringParam("FINAL_BUILD_ID")
        }
        steps {
            shell """
cd \$WORKSPACE
set +x
source $HOME/.openrc_ansible_roles
set -x
if [ '${platform}' = 'rhel6' ]; then
    sleep 60
fi
rm junit-${platform}.xml || true
VENV=/tmp/.venvs/${SEED_PROJECT}-${SEED_BRANCH}/testinfra-${platform} make test PLATFORM=${platform}
"""
        }
        publishers {
            postBuildScripts {
                steps {
                    shell("""
cd \$WORKSPACE
set +x
source $HOME/.openrc_ansible_roles
set -x
VENV=/tmp/.venvs/${SEED_PROJECT}-${SEED_BRANCH}/testinfra-${platform} make delete PLATFORM=${platform} || true
"""
                        )
                }
                onlyIfBuildSucceeds(false)
            }
        }
    }
    //Add elements to jobs
    helpers.addScm(testJob,PROJECT_SCM_URL,PROJECT_SCM_CREDENTIALS,BRANCH)
    helpers.setBuildName(testJob, '${ENV,var="VERSION_MASTER"}-${ENV,var="FINAL_BUILD_ID"}')
    helpers.setConsoleColor(testJob)
    helpers.addPerformenceReport(testJob, "junit-${platform}.xml")
    helpers.addGithubNotifier(testJob)
}
