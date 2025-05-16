package com.avik.personal_library

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersonalLibraryApplication

fun main(args: Array<String>) {
	runApplication<PersonalLibraryApplication>(*args)
}
