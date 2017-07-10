package utilities
//Version 0.0.3
class helpers {
    static void getVersionFromFile(def job, def versionFile = "/VERSION"){
        job.with {
            wrappers {
                environmentVariables {
                    groovy("""
def build = Thread.currentThread().executable
def version = new File(build.workspace.toString()+"$versionFile").text
return [VERSION: version.trim()]
        """)
                }
            }
        }
    }
    static void getVersionFromDate(def job){
        job.with {
            wrappers {
                environmentVariables {
                    groovy("""
import java.util.Date
def date = new Date(System.currentTimeMillis())
return [VERSION: date.format('yyyyMMddHHmmss')]
        """)
                }
            }
        }
    }
    static void setBuildName(def job, def jobBuildName) {
        job.with {
            wrappers {
                buildName("$jobBuildName")
            }
        }
    }
    static void setConsoleColor(def job) {
        job.with {
            wrappers {
                colorizeOutput()
            }
        }
    }
    static void addPromotions(def job, def promoteEnvironments, def promoteJobName) {
        job.with {
            properties {
                promotions {
                    promoteEnvironments.each { environment ->
                        promotion {
                            name(environment.name)
                            icon(environment.icon)
                            conditions {
                                manual('')
                            }
                            actions {
                                downstreamParameterized {
                                    trigger(promoteJobName,"SUCCESS",false,["buildStepFailure": "FAILURE","failure":"FAILURE","unstable":"UNSTABLE"]) {
                                        predefinedProp("ENVIRONMENT",environment.env)
                                        predefinedProp("PROMOTED_URL",'$PROMOTED_URL')
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    static void addScm(def job, def scmUrl, def scmCredentials, def scmBranch, def relativePath = false, def mergeBefore = false){
        job.with {
            scm {
                git {
                    remote {
                        name "origin"
                        url "$scmUrl"
                        credentials "$scmCredentials"
                    }
                    branch("origin/$scmBranch")
                    if (relativePath) {
                        relativeTargetDir(relativePath)
                    }
                    if (mergeBefore) {
                         mergeOptions('origin', 'master')
                    }
                }
            }
        }
    }
    static void addArtifactoryPublish(def job, def repository, def pattern){
        job.with {
            configure { project ->
                project / buildWrappers << 'org.jfrog.hudson.generic.ArtifactoryGenericConfigurator' {
                    details {
                        artifactoryUrl('http://artifactory.hi.inet:8081/artifactory')
                        artifactoryName('-406811472@1427745126607')
                        repositoryKey("$repository")
                        snapshotsRepositoryKey("$repository")
                    }
                    deployPattern("$pattern")
                }
            }
        }
    }
    static void addOntrackBuildNotifier(def job, def ontrackProject, def ontrackBranch, def ontrackBuildid){
        job.with {
            configure { node ->
                node / 'publishers' / 'net.nemerosa.ontrack.jenkins.OntrackBuildNotifier' {
                    'project'("$ontrackProject")
                    'branch'("$ontrackBranch")
                    'build'("$ontrackBuildid")
                }
            }
        }
    }
    static void addOntrackValidation(def job, def ontrackProject, def ontrackBranch, def ontrackBuildid, def ontrackValidationStamp){
        job.with {
            configure { node ->
                node / 'publishers' / 'net.nemerosa.ontrack.jenkins.OntrackValidationRunNotifier' {
                    'project'("$ontrackProject")
                    'branch'("$ontrackBranch")
                    'build'("$ontrackBuildid")
                    'validationStamp'("$ontrackValidationStamp")
                }
            }
        }
    }
    static void addGithubNotifier(def job){
        job.with {
            publishers {
                githubCommitNotifier()
            }
        }
    }
    static void addSlackNotifier(def job, def slackChannel, def slackTeam, def slackToken){
        job.with {
            publishers {
                slackNotifications {
                    teamDomain("$slackTeam")
                    integrationToken("$slackToken")
                    projectChannel("$slackChannel")
                    notifyBuildStart(false)
                    notifyAborted(true)
                    notifyFailure(true)
                    notifyNotBuilt(true)
                    notifySuccess(true)
                    notifyUnstable(true)
                    notifyBackToNormal(true)
                    notifyRepeatedFailure(false)
                }
            }
        }
    }
    static void addCreateTag(def job, def tagName){
        job.with {
            publishers {
                git {
                    pushOnlyIfSuccess(true)
                    pushMerge(true)
                    forcePush(true)
                    tag("origin", "$tagName") {
                        message('Tag created by CI')
                        create(true)
                        update(false)
                    }
                }
            }
        }
    }
    static void addExtendenMailAlways(def job, def extMailRecipient, def extMailProject, def extMailBuildVersion){
        job.with {
            publishers {
                extendedEmail("$extMailRecipient", "$extMailProject build: \$BUILD_STATUS!", "Generated version: $extMailBuildVersion\n\nCHANGES\n------------\n\${CHANGES_SINCE_LAST_SUCCESS}") {
                    trigger(triggerName: 'Always')
                    configure { node ->
                        node / attachBuildLog << 'true'
                        node / compressBuildLog << 'true'
                    }
                }
            }
        }
   } 
   static void addExtendenMailFirstFailure(def job, def extMailRecipient, def extMailProject, def extMailBuildVersion){
        job.with {
            publishers {
                extendedEmail(null, "$extMailProject build: \$BUILD_STATUS!", "Generated version: $extMailBuildVersion\n\nCHANGES\n------------\n\${CHANGES_SINCE_LAST_SUCCESS}") {
                    trigger('FirstFailure',null,null,null,true,true,false,false)
                }
            }
        }
   }
   static void addPerformenceReport(def job, def report){
        job.with {
            configure { node ->
                node / 'publishers' / 'hudson.plugins.performance.PerformancePublisher' {
                    parsers {
                        'hudson.plugins.performance.JUnitParser' {
                            glob(report)
                        }
                    }
                }
            }
        }
   }
}
