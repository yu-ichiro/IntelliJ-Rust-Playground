package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.services.CargoPlayProject
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.util.io.exists
import java.nio.file.Paths


class OpenCargoPlayFolderAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        val f = Paths.get(service.cargoPlayPath.cargoPlayDir)
        RevealFileAction.openDirectory(f)
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        e.presentation.isVisible = true
        e.presentation.isEnabled = Paths.get(service.cargoPlayPath.cargoPlayDir).exists()
    }
}

class OpenCargoPlayDebugBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        val f = Paths.get(service.cargoPlayPath.debugTarget)
        RevealFileAction.openFile(f)
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        e.presentation.isVisible = true
        e.presentation.isEnabled = Paths.get(service.cargoPlayPath.debugTarget).exists()
    }
}

class OpenCargoPlayReleaseBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        val f = Paths.get(service.cargoPlayPath.releaseTarget)
        RevealFileAction.openFile(f)
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        e.presentation.isVisible = true
        e.presentation.isEnabled = Paths.get(service.cargoPlayPath.releaseTarget).exists()
    }
}