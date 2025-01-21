package com.example.datarecoverynew.models

import com.example.datarecoverynew.views.adapters.LanguagesAdapter
import java.io.Serializable

data class LanguageModel(var icon: Int,
                         var name: String,
                         var dataType: LanguagesAdapter.VIEW_TYPE=LanguagesAdapter.VIEW_TYPE.CARD_VIEW
) : Serializable
