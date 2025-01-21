package com.example.recoverydata.models

import java.io.Serializable

data class ContactModel( var id: String? = null,
                         var name: String? = null,
                         var mobileNumber: String? = null,
                         var isSelected: Boolean = false
) : Serializable
