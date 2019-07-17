package com.pecota.kcsv.parser

import com.pecota.kcsv.annotations.CSVField
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CSVParser {

    fun toCSV(model: List<Any>): ByteArray {
        if (model.isEmpty()) {
            return ByteArray(0)
        }
        ByteArrayOutputStream().use { baos ->
            write(baos, getHeader(model.first()))
            model.forEach {
                write(baos, getRow(it))
            }
            return baos.toByteArray()
        }
    }

    private fun write(baos: ByteArrayOutputStream, entry: ByteArray) {
        baos.write(entry)
        baos.write("\n".toByteArray())
    }

    private fun getHeader(model: Any) = getFields(model).joinToString(",") {
        val csvField = it.getAnnotation(CSVField::class.java)
        return@joinToString when {
            csvField.name.isNotBlank() -> csvField.name
            else -> it.name
        }
    }.toByteArray()

    private fun getRow(model: Any) = getFields(model).joinToString(",") {
        parseValueAsString(it.get(model))
    }.toByteArray()

    private fun parseValueAsString(value: Any?): String {
        return String(
            when (value) {
                null -> ""
                is String -> parseStringValue(value)
                is Instant -> DateTimeFormatter.ISO_DATE_TIME.format(value.atZone(ZoneId.of("UTC")))
                else -> value.toString()
            }.toByteArray(), charset("UTF8")
        )
    }

    private fun parseStringValue(value: String): String {
        val s = normalizeString(value)
        return when {
            isQuoted(s) -> s
            s.contains("\"") -> {
                when {
                    s.contains(",") -> putOnQuotes(escapeQuotes(s))
                    else -> escapeQuotes(s)
                }
            }
            s.contains(",") -> "\"" + s + "\""
            else -> s
        }
    }

    private fun normalizeString(value: String) =
        value.trim().replace("\n", " ").replace("\r", " ").replace("\\s{2,}".toRegex(), " ")

    private fun isQuoted(s: String) = s[0] == '"' && s[s.length - 1] == '"'

    private fun putOnQuotes(s: String) = "\"$s\""

    private fun escapeQuotes(s: String) = s.replace("\"", "\\\"")

    private fun getFields(model: Any) = model::class.java.declaredFields.asSequence().filter {
        it.isAnnotationPresent(CSVField::class.java)
    }.map {
        it.isAccessible = true
        return@map it
    }.sortedBy {
        it.getAnnotation(CSVField::class.java).position
    }
}
