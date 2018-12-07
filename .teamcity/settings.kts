import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.MSBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.msBuild
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.2"

project {
    description = "Test Builds for Octopus"

    buildType(BuildAndPackage)

    features {
        feature {
            id = "PROJECT_EXT_6"
            type = "project-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "Time Spent in Queue",
                    "sourceBuildTypeId": "Octopus_BuildAndPackage",
                    "key": "TimeSpentInQueue"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("hideFilters", "")
            param("title", "New chart title")
            param("defaultFilters", "")
            param("seriesTitle", "Serie")
        }
        feature {
            id = "PROJECT_EXT_8"
            type = "project-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "Success Rate",
                    "sourceBuildTypeId": "Octopus_BuildAndPackage",
                    "key": "SuccessRate"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("title", "New chart title")
            param("seriesTitle", "Serie")
        }
    }
}

object BuildAndPackage : BuildType({
    name = "Build and Package"

    buildNumberPattern = "1.0.%build.counter%"

    params {
        param("SolutionPath", "DeployCo.Intranet.Web.sln")
        param("octoserver", "http://172.22.71.242")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        step {
            name = "Restore Nuget Packages"
            type = "jb.nuget.installer"
            param("nuget.path", "%teamcity.tool.NuGet.CommandLine.DEFAULT%")
            param("nuget.updatePackages.mode", "sln")
            param("sln.path", "%SolutionPath%")
        }
        msBuild {
            name = "Build and Package"
            path = "%SolutionPath%"
            toolsVersion = MSBuildStep.MSBuildToolsVersion.V15_0
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
            param("octopus_octopack_publish_api_key", "API-R9ULKGFIHD0PZWRKWQDMMBKYIW0")
            param("octopus_octopack_package_version", "%build.number%")
            param("octopus_run_octopack", "true")
            param("octopus_octopack_publish_package_to_http", "%octoserver%/nuget/packages")
        }
        step {
            name = "Create Release"
            type = "octopus.create.release"
            param("octopus_waitfordeployments", "true")
            param("octopus_version", "3.0+")
            param("octopus_host", "%octoserver%")
            param("octopus_project_name", "Deploy Web")
            param("octoups_tenants", "Web1")
            param("octopus_deployto", "Dev")
            param("secure:octopus_apikey", "credentialsJSON:4160fe41-f34c-40ec-8f1d-80811e903516")
            param("octopus_releasenumber", "%build.number%")
        }
        step {
            name = "Octopus Deploy"
            type = "octopus.deploy.release"
            enabled = false
            param("octopus_waitfordeployments", "true")
            param("octopus_version", "3.0+")
            param("octopus_host", "%octoserver%")
            param("octopus_project_name", "Deploy Web")
            param("octopus_deployto", "Prod")
            param("secure:octopus_apikey", "credentialsJSON:4160fe41-f34c-40ec-8f1d-80811e903516")
            param("octopus_releasenumber", "%build.number%")
        }
    }

    triggers {
        vcs {
            branchFilter = ""
            enableQueueOptimization = false
        }
    }
})
