package nbcp.base.annotation.validator

import nbcp.base.annotation.Require
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * @author iamne
 */
class RequireValidator : ConstraintValidator<Require?, Any?> {
    override fun initialize(constraintAnnotation: Require?) {
        super.initialize(constraintAnnotation)
    }

    override fun isValid(
        value: Any?,
        constraintValidatorContext: ConstraintValidatorContext
    ): Boolean {
        if (value == null) {
            return false
        }

        if (value is String && value.isEmpty()) {
            return false
        }

        if (value is Array<*> && value.size == 0) {
            return false;
        }

        if (value is Collection<*> && value.size == 0) {
            return false;
        }


        return true
    }
}