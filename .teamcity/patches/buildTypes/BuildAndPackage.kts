package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.MSBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.msBuild
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'BuildAndPackage'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("BuildAndPackage")) {
    params {
        expect {
            param("octoserver", "http://172.22.71.242")
        }
        update {
            param("octoserver", "http://octo.axsdevops.io:81")
        }
    }

    expectSteps {
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
            param("octopus_octopack_package_version", "%build.number%")
            param("octopus_octopack_publish_api_key", "API-R9ULKGFIHD0PZWRKWQDMMBKYIW0")
            param("octopus_octopack_publish_package_to_http", "%octoserver%/nuget/packages")
            param("octopus_run_octopack", "true")
        }
        step {
            name = "Create Release"
            type = "octopus.create.release"
            param("octopus_deployto", "Dev")
            param("octopus_host", "%octoserver%")
            param("octopus_project_name", "Deploy Web")
            param("octopus_releasenumber", "%build.number%")
            param("octopus_version", "3.0+")
            param("octopus_waitfordeployments", "true")
            param("octoups_tenants", "Web1")
            param("secure:octopus_apikey", "credentialsJSON:4160fe41-f34c-40ec-8f1d-80811e903516")
        }
        step {
            name = "Octopus Deploy"
            type = "octopus.deploy.release"
            enabled = false
            param("octopus_deployto", "Prod")
            param("octopus_host", "%octoserver%")
            param("octopus_project_name", "Deploy Web")
            param("octopus_releasenumber", "%build.number%")
            param("octopus_version", "3.0+")
            param("octopus_waitfordeployments", "true")
            param("secure:octopus_apikey", "credentialsJSON:4160fe41-f34c-40ec-8f1d-80811e903516")
        }
    }
    steps {
        update<MSBuildStep>(1) {
            param("octopus_octopack_publish_api_key", "API-NHLIKZXMD5RLKPV9GJSCRYIBO")
        }
        update<BuildStep>(2) {
            enabled = false
            param("octoups_tenants", "")
            param("secure:octopus_apikey", "credentialsJSON:d13e22bf-2994-4748-a295-659a4de9c952")
        }
    }

    triggers {
        val trigger1 = find<VcsTrigger> {
            vcs {
                branchFilter = ""
                enableQueueOptimization = false
            }
        }
        trigger1.apply {
            enabled = false
        }
    }
}
