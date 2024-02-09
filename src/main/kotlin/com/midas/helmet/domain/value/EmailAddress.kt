package com.midas.helmet.domain.value

import jakarta.validation.constraints.Email

/**
 * Created by Usman Mutawakil on 11/23/22.
 */

class EmailAddress : ValueObject {

    @Email
    val value: String

    constructor(email: String) {
        this.value = email
        this.validate()
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return value == other
    }

    override fun toString() : String {
        return value
    }
}