package tunanh.test_app

fun getCardholderName(hex: String): String {
    val bytes = hex.chunked(2).map { it.toInt(16).toByte() }


    val nameTagPos = bytes.indexOf(0x5F.toByte()) + 1


    var endPos = nameTagPos
    while (endPos < bytes.size && bytes[endPos] != 0x5F.toByte()) {
        endPos++
    }


    val nameBytes = bytes.slice(nameTagPos until endPos)


    return String(nameBytes.toByteArray(), Charsets.UTF_8)

}

fun hexToAscii(hex: String): String {
    val sb = StringBuilder()
    var i = 0
    while (i < hex.length) {
        val hexPair = hex.substring(i, i + 2)
        try {
            val decimal = hexPair.toInt(16)
            if (decimal in 32..126) {
                sb.append(decimal.toChar())
            } else {
                sb.append('�')
            }
        } catch (e: NumberFormatException) {
            sb.append('�')
        }
        i += 2
    }
    return sb.toString()
}

fun main() {
    val inputData =
        "00DFEE2502001057134221094014191587D28052211000009500000F5A0842210940141915875F3401005F2016564953412043415244484F4C444552202020202020205F24032805319F20005F25032305015F2D02656E500D414342205649534120434152444F07A00000000310108407A0000000031010DFEE23009F3901059F1E080000000000000000FFEE0104DF300101DFEF4C06002700000000DFEF4D273B343232313039343031343139313538373D32383035323231313030303030393530303030303FDFEE260100".trimIndent()

    val name = hexToAscii(inputData).split("[^a-zA-Z ]".toRegex())
        .filter { it.isNotEmpty() }
        .find { it.trim().split(" ").size >= 2 }


    println(name)

}

fun extractNames(input: String): List<String> {
    val pattern = "[A-Z ]+".toRegex()
    val matches = pattern.findAll(input)
    return matches.map { it.value.trim() }.toList()
}

fun hexToString(hex: String): String {
    val hexPairs = hex.chunked(2)
    val byteArray = hexPairs.map { it.toInt(16).toByte() }.toByteArray()
    return String(byteArray, Charsets.ISO_8859_1)
}