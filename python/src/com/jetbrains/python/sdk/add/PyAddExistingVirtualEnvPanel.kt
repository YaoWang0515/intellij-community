/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.sdk.add

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.UserDataHolder
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import com.jetbrains.python.sdk.*
import icons.PythonIcons
import java.awt.BorderLayout
import javax.swing.Icon

/**
 * @author vlan
 */
class PyAddExistingVirtualEnvPanel(private val project: Project?,
                                   private val module: Module?,
                                   private val existingSdks: List<Sdk>,
                                   override var newProjectPath: String?,
                                   context:UserDataHolder ) : PyAddSdkPanel() {
  override val panelName: String = "Existing environment"
  override val icon: Icon = PythonIcons.Python.Virtualenv
  private val sdkComboBox = PySdkPathChoosingComboBox()
  private val makeSharedField = JBCheckBox("Make available to all projects")

  init {
    layout = BorderLayout()
    val formPanel = FormBuilder.createFormBuilder()
      .addLabeledComponent("Interpreter:", sdkComboBox)
      .addComponent(makeSharedField)
      .panel
    add(formPanel, BorderLayout.NORTH)
    addInterpretersAsync(sdkComboBox) {
      detectVirtualEnvs(module, existingSdks, context)
        .filterNot { it.isAssociatedWithAnotherModule(module) }
    }
  }

  override fun validateAll(): List<ValidationInfo> =
    listOf(validateSdkComboBox(sdkComboBox))
      .filterNotNull()

  override fun getOrCreateSdk(): Sdk? {
    val sdk = sdkComboBox.selectedSdk
    return when (sdk) {
      is PyDetectedSdk -> sdk.setupAssociated(existingSdks, newProjectPath ?: project?.basePath)?.apply {
        if (!makeSharedField.isSelected) {
          associateWithModule(module, newProjectPath)
        }
      }
      else -> sdk
    }
  }
}
