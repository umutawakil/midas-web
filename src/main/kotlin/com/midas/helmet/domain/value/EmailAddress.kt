package com.midas.helmet.domain.value

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Email

/**
 * Created by Usman Mutawakil on 11/23/22.
 */
@Embeddable
class EmailAddress : ValueObject {
    @Email
    @Column(name="email_address")
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
}