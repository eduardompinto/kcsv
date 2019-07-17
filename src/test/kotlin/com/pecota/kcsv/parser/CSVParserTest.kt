package com.pecota.kcsv.parser

import com.pecota.kcsv.annotations.CSVField
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class TestObject(
    @CSVField(1)
    val id: Long,
    @CSVField(2)
    val name: String?,
    @CSVField(3)
    val value: Double?,
    @CSVField(4, "created_at")
    val date: Instant?
)

class CSVParserTest {

    @Test
    @DisplayName("Must parse a Any object to CSV")
    fun successCase() {
        val service = CSVParser()
        val now = (0..6).map { Instant.now() }
        val data = listOf(
            TestObject(1, "first test, with comma", 1.0, now[0]),
            TestObject(2, "test without comma (no escape here)", 2.0, now[1]),
            TestObject(3, ",", 3.0, now[2]),
            TestObject(4, """ "Text with commas and quotes, ok!" """, 4.0, now[3]),
            TestObject(5, """ "Text with quotes ok!" """, 5.0, now[4]),
            TestObject(
                6, """ "Text with quotes ok!"
Multiple lines text should be in only one line""", 6.0, now[5]
            )
        )
        val csv = service.toCSV(data)
        val expectedCSV = """
            id,name,value,created_at
            1,"first test, with comma",1.0,${formatDate(now[0])}
            2,test without comma (no escape here),2.0,${formatDate(now[1])}
            3,",",3.0,${formatDate(now[2])}
            4,"Text with commas and quotes, ok!",4.0,${formatDate(now[3])}
            5,"Text with quotes ok!",5.0,${formatDate(now[4])}
            6,\"Text with quotes ok!\" Multiple lines text should be in only one line,6.0,${formatDate(now[5])}

        """
        Assertions.assertEquals(String(csv), expectedCSV.trimIndent())
    }

    private fun formatDate(instant: Instant) =
        DateTimeFormatter.ISO_DATE_TIME.format(instant.atZone(ZoneId.of("UTC")))
}
