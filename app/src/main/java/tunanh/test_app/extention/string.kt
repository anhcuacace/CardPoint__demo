package tunanh.test_app.extention

fun String?.ifNullOrEmpty(action: () -> String): String {
    return if (isNullOrEmpty()) {
        action()
    } else {
        this
    }
}