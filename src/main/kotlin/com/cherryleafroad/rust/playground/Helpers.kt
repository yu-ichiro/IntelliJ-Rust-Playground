package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
import com.cherryleafroad.rust.playground.config.SettingsConfigurable
import com.cherryleafroad.rust.playground.scratch.ui.ScratchSettings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.rust.cargo.toolchain.RustChannel
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.cargo

object Helpers {
    fun checkCargoPlayInstalled(project: Project): Boolean {
        return project.toolchain!!.hasCargoExecutable("cargo-play")
    }

    private fun checkAndNotifyCargoExpandInstalled(project: Project): Boolean {
        val installed = project.toolchain!!.hasCargoExecutable("cargo-expand")
        if (!installed) {
            val notification = NotificationGroupManager.getInstance().getNotificationGroup("Rust Playground")
                .createNotification(
                    "Rust Playground",
                    "cargo-expand is required to use the expand feature",
                    NotificationType.INFORMATION
                )

            val install = NotificationAction.createSimple("Install") {
                val toolchain = project.toolchain!!
                toolchain.cargo().installBinaryCrate(project, "cargo-expand")
                notification.hideBalloon()
            }
            notification.addAction(install)
            notification.notify(project)
        }

        return installed
    }

    fun checkAndNotifyCargoPlayInstallation(project: Project) {
        if (!checkCargoPlayInstalled(project)) {
            cargoPlayInstallNotification(project)
        }
    }

    @Suppress("DialogTitleCapitalization")
    fun cargoPlayInstallNotification(project: Project) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Rust Playground")
            .createNotification(
                "Rust Playground",
                "Playground requires cargo-play binary crate",
                NotificationType.INFORMATION
            )

        val install = NotificationAction.createSimple("Install") {
            val toolchain = project.toolchain!!
            toolchain.cargo().installBinaryCrate(project, "cargo-play")
            notification.hideBalloon()
        }
        val settings = NotificationAction.createSimple("Settings") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable::class.java)
        }
        notification.addAction(install)
        notification.addAction(settings)
        notification.notify(project)
    }

    fun parseOptions(project: Project, file: VirtualFile, clean: Boolean): ParserResults? {
        val settings = ScratchSettings(file)

        val check = settings.CHECK.getBoolean()
        val cleanProp = settings.CLEAN.getBoolean()
        val expand = settings.EXPAND.getBoolean()
        val infer = settings.INFER.getBoolean()
        val quiet = settings.QUIET.getBoolean()
        val release = settings.RELEASE.getBoolean()
        val test = settings.TEST.getBoolean()
        val verbose = settings.VERBOSE.getBoolean()

        val toolchain = RustChannel.fromIndex(settings.TOOLCHAIN.getInt(Settings.getSelectedToolchain().index))
        val edition = Edition.fromIndex(settings.EDITION.getInt(Edition.DEFAULT.index))

        val src = mutableListOf(file.name)
        src.addAll(settings.SRC.getValue().split(" ").filter { it.isNotEmpty() })
        val args = settings.ARGS.getValue().split(" ").filter { it.isNotEmpty() }.toMutableList()
        val mode = settings.MODE.getValue()
        val cargoOption = settings.CARGO_OPTIONS.getValue().split(" ").filter { it.isNotEmpty() }.toMutableList()
        val cargoOptionNoDefault = settings.CARGO_OPTIONS_NO_DEFAULTS.getBoolean()

        val runCmd = mutableListOf<String>()

        // check for cargo-expand installation
        if (expand) {
            val installed = checkAndNotifyCargoExpandInstalled(project)
            if (!installed) {
                return null
            }
        }

        // change the toolchain
        if (toolchain != RustChannel.DEFAULT) {
            runCmd.add("+${toolchain.channel!!}")
        }

        if (clean) {
            // one time clean and exit
            runCmd.add("--mode")
            runCmd.add("clean")
        } else {
            if (args.isNotEmpty()) {
                args.add(0, "--")
            }

            if (check) {
                runCmd.add("--check")
            }
            if (cleanProp) {
                runCmd.add("--clean")
            }
            if (expand) {
                runCmd.add("--expand")
            }
            if (infer) {
                runCmd.add("--infer")
            }
            if (quiet) {
                runCmd.add("--quiet")
            }
            if (release) {
                runCmd.add("--release")
            }
            if (test) {
                runCmd.add("--test")
            }
            if (verbose) {
                runCmd.add("--verbose")
            }
            if (edition != Edition.DEFAULT) {
                runCmd.add("--edition")
                runCmd.add(edition.myName)
            }
            if (mode.isNotEmpty()) {
                runCmd.add("--mode")
                runCmd.add(mode)
            }

            // this option could interfere with other cargo options
            if (!cargoOptionNoDefault) {
                cargoOption.add(0, "--color=always")
            }

            if (cargoOption.isNotEmpty()) {
                runCmd.add("--cargo-option=\"${cargoOption.joinToString(" ")}\"")
            }
        }

        val finalCmd = runCmd + src + args

        return ParserResults(
            check, cleanProp, expand, infer,
            quiet, release, test, verbose, toolchain,
            cargoOption, edition, mode, src, args,
            runCmd, finalCmd
        )
    }
}
