package com.example.recoverydata.models

import com.example.datarecoverynew.views.adapters.DocumentAdapter
import java.io.File

data class FilesModel(var file: File,var isChecked:Boolean,var dataType: DocumentAdapter.VIEW_TYPE= DocumentAdapter.VIEW_TYPE.CARD_VIEW)
