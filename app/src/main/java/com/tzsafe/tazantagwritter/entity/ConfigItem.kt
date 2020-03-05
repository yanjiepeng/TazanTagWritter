package com.tzsafe.tazantagwritter.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ConfigItem (
    @Id
    var id: Long = 0,
    var type: Int = 0,
    var text: String? = null
)



