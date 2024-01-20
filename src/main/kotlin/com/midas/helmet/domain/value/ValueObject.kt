package com.midas.helmet.domain.value

import jakarta.validation.*

/**
 * Created by Usman Mutawakil on 9/28/22.
 */
open class ValueObject {

    companion object {
        val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        val validator: Validator = factory.validator
    }

    fun validate() {
        val validations: Set<ConstraintViolation<Any>> = validator.validate(this)
        if (validations.isNotEmpty()) {
            throw ConstraintViolationException(validations);
        }
    }
}