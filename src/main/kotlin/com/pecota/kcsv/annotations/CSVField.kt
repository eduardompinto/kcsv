package com.pecota.kcsv.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CSVField(val position: Int = 0, val name: String = "")